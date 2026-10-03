package com.example.learndsandalgorithm.domain.model

data class UserProfile(
    val name: String,
    val currentStreak: Int = 0,
    val lastActivityDate: String? = null
)
