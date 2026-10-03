package com.example.learndsandalgorithm.domain.model

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val topicId: String,
    val questionCount: Int = 0
)
