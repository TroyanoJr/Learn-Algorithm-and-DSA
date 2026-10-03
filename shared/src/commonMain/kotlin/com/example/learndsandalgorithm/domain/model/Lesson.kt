package com.example.learndsandalgorithm.domain.model

data class Lesson(
    val id: String,
    val topicId: String,
    val title: String,
    val order: Int,
    val xp: Int,
    val contentPath: String
)
