package com.example.physmath.ui.lessons

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.AppDatabase
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.network.RetrofitClient.api
import com.example.physmath.data.repository.PhysMathRepository
import com.example.physmath.utils.NetworkObserver
import kotlinx.coroutines.launch

class LessonsViewModel(application: Application): AndroidViewModel(application) {
    private var currentTopicId: String? = null
    private var currentTopicTitle: String? = null
    private var isFiltered: Boolean = false

    private val repository = PhysMathRepository(
        dao = AppDatabase.getInstance(application).dao(),
        api = api,
        context = application
    )

    fun getCurrentTopicTitle(): String? = currentTopicTitle
    private val _filteredLessons = MutableLiveData<List<LessonEntity>>()
    val lessons: LiveData<List<LessonEntity>> = _filteredLessons

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadLessonsByTopic(topicId: String, topicTitle: String? = null) {
        currentTopicId = topicId
        currentTopicTitle = topicTitle
        isFiltered = true

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getLessonsByTopic(topicId)
                result.onSuccess { lessons: List<LessonEntity> ->
                    _filteredLessons.value = lessons.sortedBy { it.orderIndex ?: 0 }
                }.onFailure { error: Throwable ->
                    _errorMessage.value = "Ошибка: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Критическая ошибка: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    fun reloadCurrentLessons() {
        if (isFiltered && currentTopicId != null) {
            loadLessonsByTopic(currentTopicId!!, currentTopicTitle)
        } else {
            syncWithServer()
        }
    }

    fun syncWithServer() {
        currentTopicId = null
        currentTopicTitle = null
        isFiltered = false

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.syncLessons()
                result.onSuccess { lessons: List<LessonEntity> ->
                    _filteredLessons.value = lessons.sortedBy { it.orderIndex ?: 0 }
                }.onFailure { error: Throwable ->
                    _errorMessage.value = formatError(error)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Критическая ошибка: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    private fun formatError(error: Throwable): String = when (error) {
        is java.io.IOException -> "Нет подключения к интернету"
        is retrofit2.HttpException -> "Ошибка сервера: ${error.code()}"
        else -> "Ошибка: ${error.message ?: "Неизвестная"}"
    }
    init {
        viewModelScope.launch {
            NetworkObserver.observe(getApplication()).collect { isOnline ->
                Log.d("NetworkDebug", "сеть: online=$isOnline")
            }
        }
    }
}