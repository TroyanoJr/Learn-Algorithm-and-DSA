package com.example.learndsandalgorithm.domain.model

data class LessonProgress(
    val lessonId: String,
    val startedAt: String? = null,
    val completedAt: String? = null
)
