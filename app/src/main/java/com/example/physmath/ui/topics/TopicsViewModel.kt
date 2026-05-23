package com.example.physmath.ui.topics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.AppDatabase
import com.example.physmath.data.entities.Topic
import com.example.physmath.data.network.RetrofitClient
import com.example.physmath.data.repository.PhysMathRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopicViewModel(private val repository: PhysMathRepository) : AndroidViewModel(Application()) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> = _topics

    fun loadTopics(subjectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val localTopics = repository.getTopicsFromLocal(subjectId).first()
                _topics.value = localTopics
                _isEmpty.value = localTopics.isEmpty()

                if (repository.isOnline()) {
                    val result = repository.syncTopics(subjectId)
                    result.onSuccess { remoteTopics ->
                        _topics.value = remoteTopics
                        _isEmpty.value = remoteTopics.isEmpty()
                        _errorMessage.value = null
                    }.onFailure { _ ->
                        _errorMessage.value = "⚠️ Работаем в офлайн-режиме"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
                _topics.value = emptyList()
                _isEmpty.value = true
            }

            _isLoading.value = false
        }
    }

    fun searchTopics(subjectId: String, query: String) {
        viewModelScope.launch {
            try {
                if (repository.isOnline()) {
                    val result = withContext(Dispatchers.IO) {
                        RetrofitClient.api.getTopicsBySubject(subjectId, search = query)
                    }
                    _topics.value = result
                    _isEmpty.value = result.isEmpty()
                } else {
                    val localTopics = repository.getTopicsFromLocal(subjectId).first()
                    val filtered = localTopics.filter {
                        it.title.contains(query, ignoreCase = true)
                    }
                    _topics.value = filtered
                    _isEmpty.value = filtered.isEmpty()
                }
            } catch (_: Exception) {
                val localTopics = repository.getTopicsFromLocal(subjectId).first()
                val filtered = localTopics.filter {
                    it.title.contains(query, ignoreCase = true)
                }
                _topics.value = filtered
                _isEmpty.value = filtered.isEmpty()
                _errorMessage.value = if (filtered.isEmpty()) {
                    "🔍 Темы не найдены"
                } else {
                    "⚠️ Показываем кэшированные результаты"
                }
            }
        }
    }

    fun refreshTopics(subjectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getTopics(subjectId)
                result.onSuccess { topics ->
                    _topics.value = topics
                    _isEmpty.value = topics.isEmpty()
                }.onFailure { error ->
                    _errorMessage.value = "Не удалось обновить: ${error.message}"
                    val local = repository.getTopicsFromLocal(subjectId).first()
                    if (local.isNotEmpty()) {
                        _topics.value = local
                        _isEmpty.value = false
                        _errorMessage.value = "📦 Показываем кэшированные данные"
                    } else {
                        _isEmpty.value = true
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
                _topics.value = emptyList()
                _isEmpty.value = true
            }

            _isLoading.value = false
        }
    }

}
class TopicViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicViewModel::class.java)) {
            val repo = PhysMathRepository(
                dao = AppDatabase.getInstance(application).dao(),
                api = RetrofitClient.api,
                context = application
            )
            @Suppress("UNCHECKED_CAST")
            return TopicViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}