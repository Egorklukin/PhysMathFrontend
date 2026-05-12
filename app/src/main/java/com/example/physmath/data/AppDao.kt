package com.example.physmath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.entities.SubjectEntity
import com.example.physmath.data.entities.TestResultEntity
import com.example.physmath.data.entities.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveResult(result: TestResultEntity)
    @Query("SELECT * FROM test_results WHERE lessonId = :id")
    suspend fun getResultByLesson(id: String): TestResultEntity?
    @Query("SELECT * FROM lessons WHERE isDownloaded = 1 ORDER BY title")
    fun getDownloadedLessons(): Flow<List<LessonEntity>>
    @Query("SELECT * FROM lessons WHERE id = :lessonId AND isDownloaded = 1")
    suspend fun getDownloadedLesson(lessonId: String): LessonEntity?
    @Query("UPDATE lessons SET isDownloaded = 1, downloadedAt = :timestamp WHERE id = :lessonId")
    suspend fun markAsDownloaded(lessonId: String, timestamp: Long = System.currentTimeMillis())
    @Query("UPDATE lessons SET isDownloaded = 0, downloadedAt = NULL WHERE id = :lessonId")
    suspend fun markAsNotDownloaded(lessonId: String)
    @Query("SELECT id FROM lessons WHERE isDownloaded = 1")
    suspend fun getDownloadedLessonIds(): List<String>
    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<LessonEntity>>
    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): LessonEntity?
    @Query("UPDATE lessons SET progress = :progress WHERE id = :lessonId")
    suspend fun updateLessonProgress(lessonId: String, progress: Float)

    @Query("UPDATE lessons SET isDownloaded = 1, downloadedAt = :timestamp")
    suspend fun markAllLessonsDownloaded(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE lessons SET isDownloaded = 0, downloadedAt = NULL")
    suspend fun markAllLessonsNotDownloaded()

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getLessonsCount(): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE isDownloaded = 1")
    suspend fun getDownloadedLessonsCount(): Int

    @Query("SELECT * FROM subjects ORDER BY title")
    fun getAllSubjects(): Flow<List<SubjectEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)
    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Query("SELECT * FROM topics")
    suspend fun getAllTopics(): List<TopicEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY title")
    fun getTopicsBySubject(subjectId: String): Flow<List<TopicEntity>>
}