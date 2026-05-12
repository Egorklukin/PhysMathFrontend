package com.example.physmath.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.physmath.data.AppDao
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.entities.Question
import com.example.physmath.data.entities.Subject
import com.example.physmath.data.entities.SubjectEntity
import com.example.physmath.data.entities.TestResultEntity
import com.example.physmath.data.entities.Topic
import com.example.physmath.data.entities.toDomains
import com.example.physmath.data.entities.toEntities
import com.example.physmath.data.entities.toEntity
import com.example.physmath.data.network.LessonApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class PhysMathRepository(
    private val dao: AppDao,
    private val api: LessonApi,
    private val context: Context
) {
    val downloadedLessons: Flow<List<LessonEntity>> = dao.getDownloadedLessons()
    fun getTopicsFromLocal(subjectId: String): Flow<List<Topic>> =
        dao.getTopicsBySubject(subjectId)
            .map { it.toDomains() }

    suspend fun syncTopics(subjectId: String): Result<List<Topic>> {
        return try {
            if (!isOnline()) {
                return Result.failure(IOException("No internet connection"))
            }

            val remoteTopics = api.getTopicsBySubject(subjectId)
            dao.insertTopics(remoteTopics.toEntities())
            Result.success(remoteTopics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Получение тем с fallback на офлайн
    suspend fun getTopics(subjectId: String): Result<List<Topic>> {
        return if (isOnline()) {
            syncTopics(subjectId).also { result ->
                if (result.isFailure) {
                    Result.success(dao.getTopicsBySubject(subjectId).first().toDomains())
                }
            }
        } else {
            Result.success(dao.getTopicsBySubject(subjectId).first().toDomains())
        }
    }
    suspend fun syncLessons(): Result<List<LessonEntity>> {
        return try {
            if (!isOnline()) {
                return Result.failure(IOException("No internet connection"))
            }

            val remoteLessons = api.getLessons()

            remoteLessons.forEach { remoteLesson ->
                val existingLesson = dao.getLessonById(remoteLesson.id)

                val lessonToSave = if (existingLesson != null) {
                    remoteLesson.copy(
                        isDownloaded = existingLesson.isDownloaded,
                        downloadedAt = existingLesson.downloadedAt,
                        progress = existingLesson.progress
                    )
                } else {
                    remoteLesson.copy(isDownloaded = false, progress = 0f)
                }

                dao.insertLesson(lessonToSave)
            }

            Result.success(remoteLessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLessonProgress(lessonId: String, progress: Float) {
        dao.updateLessonProgress(lessonId, progress)
    }

    suspend fun fetchLesson(id: String): Result<LessonEntity> {
        return try {
            if (!isOnline()) {
                dao.getLessonById(id)?.let {
                    return Result.success(it)
                }
            }

            val lesson = api.getLesson(id)

            val existingLesson = dao.getLessonById(id)
            val lessonToSave = if (existingLesson != null) {
                lesson.copy(
                    isDownloaded = existingLesson.isDownloaded,
                    downloadedAt = existingLesson.downloadedAt
                )
            } else {
                lesson.copy(isDownloaded = false)
            }

            dao.insertLesson(lessonToSave)
            Result.success(lesson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncTestResult(
        lessonId: String,
        score: Int,
        totalQuestions: Int,
        answersJson: String
    ): Result<Unit> {
        return try {
            if (!isOnline()) return Result.failure(IOException("No internet"))

            val resultDto = mapOf(
                "lessonId" to lessonId,
                "score" to score,
                "totalQuestions" to totalQuestions,
                "answersJson" to answersJson
            )

            api.postTestResult(resultDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getLessonsByTopic(topicId: String): Result<List<LessonEntity>> {
        return try {
            val lessons = api.getLessonsByTopic(topicId)
            lessons.forEach { dao.insertLesson(it) }
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getQuestionsByLesson(lessonId: String): List<Question> {
        return try {
            if (!isOnline()) {
                return getOfflineQuestions(lessonId)
            }
            api.getQuestionsByLesson(lessonId, force = true)
        } catch (e: Exception) {
            Log.e("Repository", "ошибка: ${e.message}", e)
            getOfflineQuestions(lessonId)
        }
    }

    private fun getOfflineQuestions(lessonId: String): List<Question> {
        return listOf(
            Question(
                id = "offline_1",
                lessonId = lessonId,
                questionText = "⚠️ Оффлайн-режим: вопросы не загружены",
                optionsJson = "[\"Попробуйте позже\", \"Проверить соединение\"]",
                correctAnswer = "Попробуйте позже",
                explanation = "Подключитесь к интернету для загрузки вопросов",
                difficulty = "EASY",
                orderIndex = 1
            )
        )
    }
    suspend fun saveLessonForOffline(lesson: LessonEntity) {
        dao.insertLesson(lesson.copy(isDownloaded = true, downloadedAt = System.currentTimeMillis()))
    }

    suspend fun markAsNotDownloaded(lessonId: String) {
        dao.markAsNotDownloaded(lessonId)
    }
    fun getSubjectsFromLocal(): Flow<List<Subject>> =
        dao.getAllSubjects()
            .map { it.toDomains() }

    suspend fun syncSubjects(): Result<List<Subject>> {
        return try {
            if (!isOnline()) {
                return Result.failure(IOException("No internet connection"))
            }

            val remoteSubjects = api.getSubjects()

            dao.insertSubjects(remoteSubjects.toEntities())

            Result.success(remoteSubjects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun downloadAllContent(): Flow<DownloadProgress> = flow {
        emit(DownloadProgress(0, 100, "🔄 Подготовка..."))

        try {
            if (!isOnline()) {
                emit(DownloadProgress(-1, 100, "❌ Нет подключения к интернету"))
                return@flow
            }

            var progress = 0

            emit(DownloadProgress(progress, 100, "📚 Загрузка предметов..."))
            val subjects = api.getSubjects()
            dao.insertSubjects(subjects.toEntities())
            progress += 30
            emit(DownloadProgress(progress, 100, "✅ Предметы: ${subjects.size}"))

            emit(DownloadProgress(progress, 100, "📑 Загрузка тем..."))
            var topicsCount = 0
            for (subject in subjects) {
                try {
                    val topics = api.getTopicsBySubject(subject.id)
                    dao.insertTopics(topics.map { it.toEntity() })
                    topicsCount += topics.size
                } catch (e: Exception) {
                    Log.w("Repository", "ошибка загрузки тем: ${subject.id}: ${e.message}")
                }
            }
            progress += 30
            emit(DownloadProgress(progress, 100, "✅ Темы: $topicsCount"))

            emit(DownloadProgress(progress, 100, "📖 Загрузка уроков..."))
            val allTopics = dao.getAllTopics()
            var lessonsCount = 0
            for (topic in allTopics) {
                try {
                    val lessons = api.getLessonsByTopic(topic.id)

                    dao.insertLessons(lessons.map {
                        it.copy(isDownloaded = true, downloadedAt = System.currentTimeMillis())
                    })
                    lessonsCount += lessons.size
                } catch (e: Exception) {
                    Log.w("Repository", "ошибка загрузки уроков: ${topic.id}: ${e.message}")
                }
            }
            progress = 100
            emit(DownloadProgress(progress, 100, "✅ Всё загружено! Уроков: $lessonsCount"))

        } catch (e: Exception) {
            emit(DownloadProgress(-1, 100, "❌ Ошибка: ${e.message}"))
        }
    }

    suspend fun deleteAllDownloadedContent(): Result<Unit> {
        return try {
            dao.markAllLessonsNotDownloaded()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isAllContentDownloaded(): Boolean {
        val total = dao.getLessonsCount()
        val downloaded = dao.getDownloadedLessonsCount()
        return total > 0 && total == downloaded
    }

    data class DownloadProgress(
        val current: Int,
        val total: Int,
        val message: String
    )
    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}