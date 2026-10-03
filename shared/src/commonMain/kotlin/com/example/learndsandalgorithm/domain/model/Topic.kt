package com.example.learndsandalgorithm.domain.model

data class Topic(
    val id: String,
    val title: String,
    val category: TopicCategory,
    val level: TopicLevel,
    val order: Int
)
