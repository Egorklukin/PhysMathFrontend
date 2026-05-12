package com.example.physmath.ui.tests

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.AppDatabase
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.entities.Question
import com.example.physmath.data.entities.TestResultEntity
import com.example.physmath.data.network.RetrofitClient
import com.example.physmath.data.repository.PhysMathRepository
import com.google.gson.Gson
import com.example.physmath.data.entities.TestResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TestsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PhysMathRepository(
        dao = AppDatabase.getInstance(application).dao(),
        api = RetrofitClient.api,
        context = application
    )
    fun updateLessonProgress(lessonId: String, progress: Float) {
        viewModelScope.launch {
            repository.updateLessonProgress(lessonId, progress)
        }
    }
    private val _saveStatus = MutableLiveData<String>()
    fun getDownloadedLessons(): Flow<List<LessonEntity>> {
        return repository.downloadedLessons
    }

    suspend fun getQuestionsCount(lessonId: String): Int {
        return try {
            val questions = repository.getQuestionsByLesson(lessonId)
            questions.size
        } catch (e: Exception) {
            0
        }
    }

    fun saveTestResult(result: TestResult) {
        viewModelScope.launch {
            try {
                val entity = TestResultEntity(
                    lessonId = result.lessonId,
                    score = result.score,
                    totalQuestions = result.totalQuestions,
                    answersJson = Gson().toJson(result.answers),
                    timestamp = result.timestamp
                )
                AppDatabase.getInstance(getApplication()).dao().saveResult(entity)
                _saveStatus.value = "✅ Результат сохранён локально"
            } catch (e: Exception) {
                _saveStatus.value = "❌ Ошибка сохранения: ${e.message}"
            }
        }
    }
    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadQuestions(lessonId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getQuestionsByLesson(lessonId)
                _questions.value = response
            } catch (e: Exception) {
                _questions.value = emptyList()
                Log.e("TestsViewModel", "ошибка загрузки: ${e.message}", e)
            }
            _isLoading.value = false
        }
    }
    fun syncResultWithServer(result: TestResult) {
        viewModelScope.launch {
            repository.syncTestResult(
                lessonId = result.lessonId,
                score = result.score,
                totalQuestions = result.totalQuestions,
                answersJson = Gson().toJson(result.answers)
            ).onSuccess {
                _saveStatus.value = "🌐 Результат отправлен на сервер"
            }.onFailure {
                _saveStatus.value = "⚠️ Не удалось отправить на сервер: ${it.message}"
            }
        }
    }
}