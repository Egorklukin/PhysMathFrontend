package com.example.physmath.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.3.9:8081"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = chain.request()
            android.util.Log.d("Retrofit", "запрос: ${request.method} ${request.url}")
            val response = chain.proceed(request)
            android.util.Log.d("Retrofit", "ответ: ${response.code} ${response.message}")
            response
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: LessonApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LessonApi::class.java)
    }
}