package com.example.physmath.ui.main

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.AppDatabase
import com.example.physmath.data.entities.Subject
import com.example.physmath.data.network.RetrofitClient
import com.example.physmath.data.repository.PhysMathRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val repository = PhysMathRepository(
        dao = AppDatabase.getInstance(application).dao(),
        api = RetrofitClient.api,
        context = application
    )

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> = _subjects

    init {
        loadSubjectsLocalFirst()
    }

    private fun loadSubjectsLocalFirst() {
        viewModelScope.launch {
            try {
                val localSubjects = repository.getSubjectsFromLocal().first()
                _subjects.value = localSubjects
                _isEmpty.value = localSubjects.isEmpty()
            } catch (e: Exception) {
                _subjects.value = emptyList()
                _isEmpty.value = true
            }

            if (repository.isOnline()) {
                _isLoading.value = true
                try {
                    val result = repository.syncSubjects()
                    if (result.isSuccess) {
                        _subjects.value = result.getOrNull()
                        _isEmpty.value = result.getOrNull()?.isEmpty() ?: true
                        _errorMessage.value = null
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "⚠️ Работаем в офлайн-режиме"
                }
                _isLoading.value = false
            }
        }
    }

    fun syncWithServer() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.syncSubjects()
                result.onSuccess { subjects ->
                    _subjects.value = subjects
                    _isEmpty.value = subjects.isEmpty()
                }.onFailure { error ->
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
}