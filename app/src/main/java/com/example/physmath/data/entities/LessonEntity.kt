package com.example.physmath.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val contentMd: String,

    @SerializedName("topic_id")
    val topicId: String? = null,

    @SerializedName("subject_id")
    val subjectId: String? = null,

    @SerializedName("order_index")
    val orderIndex: Int = 0,

    var isDownloaded: Boolean = false,
    var progress: Float = 0f,
    var downloadedAt: Long? = null
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey val lessonId: String,
    val score: Int,
    val totalQuestions: Int,
    val answersJson: String,
    val timestamp: Long = System.currentTimeMillis()
)