package com.example.learndsandalgorithm.domain.model

data class ContentBlock(
    val type: ContentBlockType,
    val content: String,
    val language: String? = null
)
