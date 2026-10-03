package com.example.learndsandalgorithm.presentation.home

import com.example.learndsandalgorithm.domain.model.Activity
import com.example.learndsandalgorithm.domain.model.Challenge
import com.example.learndsandalgorithm.domain.model.Progress
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.UserStats

data class HomeUiState(
    val userStats: UserStats = UserStats(),
    val progress: Progress = Progress(),
    val topics: List<Topic> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val recommendedChallenge: Challenge? = null,
    val isLoading: Boolean = false
)
