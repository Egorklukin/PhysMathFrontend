package com.example.physmath.data.entities

import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Question(
    val id: String,

    @SerializedName("lessonId")
    val lessonId: String,

    @SerializedName("questionText")
    val questionText: String,

    @SerializedName("optionsJson")
    val optionsJson: String,         // "[\"A\", \"B\", \"C\"]"

    @SerializedName("correctAnswer")
    val correctAnswer: String,

    val explanation: String,
    val difficulty: String,

    @SerializedName("orderIndex")
    val orderIndex: Int
) {
    // парсинг json строку в список
    fun getOptions(): List<String> {
        return try {
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(optionsJson, listType)
        } catch (e: Exception) {
            listOf("Ошибка загрузки вариантов")
        }
    }
}