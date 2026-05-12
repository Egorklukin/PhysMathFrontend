package com.example.physmath.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.AppDatabase
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.repository.PhysMathRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = PhysMathRepository(
        dao = AppDatabase.getInstance(app).dao(),
        api = com.example.physmath.data.network.RetrofitClient.api,
        context = app
    )

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    private val _downloadProgress = MutableLiveData<PhysMathRepository.DownloadProgress>()
    val downloadProgress: LiveData<PhysMathRepository.DownloadProgress> = _downloadProgress

    private val _isAllDownloaded = MutableLiveData<Boolean>()
    val isAllDownloaded: LiveData<Boolean> = _isAllDownloaded

    init {
        checkDownloadStatus()
    }

    private fun checkDownloadStatus() {
        viewModelScope.launch {
            _isAllDownloaded.value = repository.isAllContentDownloaded()
        }
    }

    fun downloadAllContent() {
        viewModelScope.launch {
            repository.downloadAllContent().collect { progress ->
                _downloadProgress.value = progress

                when (progress.current) {
                    100 -> {
                        _toast.value = "✅ Весь контент загружен!"
                        checkDownloadStatus()
                    }
                    -1 -> {
                        _toast.value = progress.message
                    }
                }
            }
        }
    }

    fun deleteAllDownloadedContent() {
        viewModelScope.launch {
            val result = repository.deleteAllDownloadedContent()
            result.onSuccess {
                _toast.value = "🗑️ Кэш очищен"
                checkDownloadStatus()
            }.onFailure {
                _toast.value = "❌ Ошибка: ${it.message}"
            }
        }
    }

    private val db = AppDatabase.getInstance(app)

    private val context: android.content.Context
        get() = getApplication()

    fun exportProgress(uri: Uri) {
        viewModelScope.launch {
            try {
                val lessons = db.dao().getAllLessons().first()
                val json = Gson().toJson(lessons)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                }
                _toast.postValue("Экспорт завершен")
            } catch (e: Exception) {
                _toast.postValue("Ошибка экспорта: ${e.message}")
            }
        }
    }

    fun importProgress(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: throw IOException("Empty file")

                val list = Gson().fromJson(json, Array<LessonEntity>::class.java).toList()
                list.forEach { db.dao().insertLesson(it) }

                _toast.postValue("Импорт завершен")
            } catch (e: Exception) {
                _toast.postValue("Sorry, data from this file can't be import")
            }
        }
    }
}