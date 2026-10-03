package com.example.learndsandalgorithm.domain.model

data class QuizAttempt(
    val id: String,
    val mode: PracticeMode,
    val topicId: String? = null,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val xpEarned: Int,
    val completedAt: String
)
