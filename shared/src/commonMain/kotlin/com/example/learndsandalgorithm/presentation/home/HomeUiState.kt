package com.example.learndsandalgorithm.presentation.home

import com.example.learndsandalgorithm.domain.model.Activity
import com.example.learndsandalgorithm.domain.model.Challenge
import com.example.learndsandalgorithm.domain.model.Progress
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.UserStats

data class ContinueLearningInfo(
    val topicId: String,
    val topicTitle: String,
    val lessonId: String,
    val lessonTitle: String,
    val lessonOrder: Int,
    val totalTopicLessons: Int,
    val categoryName: String,
    val topicProgressFloat: Float,
    val isAllComplete: Boolean = false
)

data class HomeUiState(
    val userStats: UserStats = UserStats(),
    val progress: Progress = Progress(),
    val topics: List<Topic> = emptyList(),
    val continueLearning: ContinueLearningInfo? = null,
    val dsCompletedLessons: Int = 0,
    val dsTotalLessons: Int = 0,
    val dsProgressPercent: Int = 0,
    val algoCompletedLessons: Int = 0,
    val algoTotalLessons: Int = 0,
    val algoProgressPercent: Int = 0,
    val recentTopicsProgress: Map<String, Int> = emptyMap(),
    val activities: List<Activity> = emptyList(),
    val recommendedChallenge: Challenge? = null,
    val isLoading: Boolean = false
)
