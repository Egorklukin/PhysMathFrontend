package com.example.physmath.data.entities

data class TestResult(
    val lessonId: String,
    val score: Int,
    val totalQuestions: Int,
    val answers: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)