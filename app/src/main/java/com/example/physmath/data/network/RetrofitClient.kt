package com.example.physmath.data.network

import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.entities.Question
import com.example.physmath.data.entities.Subject
import com.example.physmath.data.entities.Topic
import com.example.physmath.ui.feedback.FeedbackRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface LessonApi {
    @GET("api/subjects")
    suspend fun getSubjects(): List<Subject>

    @GET("api/subjects/{subjectId}/topics")
    suspend fun getTopicsBySubject(
        @Path("subjectId") subjectId: String,
        @Query("search") search: String? = null
    ): List<Topic>

    @GET("api/topics/{topicId}/lessons")
    suspend fun getLessonsByTopic(@Path("topicId") topicId: String): List<LessonEntity>
    @GET("api/lessons")
    suspend fun getLessons(): List<LessonEntity>

    @GET("api/lessons/{id}")
    suspend fun getLesson(@Path("id") id: String): LessonEntity
    @POST("api/results")
    suspend fun postTestResult(@Body result: Map<String, Any>)
    @POST("api/feedback")
    suspend fun submitFeedback(@Body feedback: FeedbackRequest): Map<String, String>
    @GET("api/lessons/{lessonId}/questions")
    suspend fun getQuestionsByLesson(
        @Path("lessonId") lessonId: String,
        @Query("force") force: Boolean = false
    ): List<Question>
    @POST("api/lessons/{lessonId}/explain")
    suspend fun getExplanation(
        @Path("lessonId") lessonId: String,
        @Body request: ExplainRequest
    ): Map<String, String>

    data class ExplainRequest(
        val questionText: String,
        val userAnswer: String,
        val correctAnswer: String
    )
}