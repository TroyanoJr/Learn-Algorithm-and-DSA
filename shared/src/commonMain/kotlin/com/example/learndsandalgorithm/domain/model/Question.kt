package com.example.learndsandalgorithm.domain.model

data class Question(
    val id: String,
    val topicId: String,
    val statement: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val difficulty: QuestionDifficulty,
    val lessonId: String? = null
)
