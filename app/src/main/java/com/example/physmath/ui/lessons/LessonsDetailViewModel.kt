package com.example.physmath.ui.lessons

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.network.RetrofitClient

class LessonDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val api = RetrofitClient.api

    suspend fun fetchLessonFromServer(lessonId: String): LessonEntity {
        return api.getLesson(lessonId)
    }

}