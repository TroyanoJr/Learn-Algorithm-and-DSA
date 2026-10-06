package com.example.learndsandalgorithm.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learndsandalgorithm.data.mock.mockActivities
import com.example.learndsandalgorithm.data.mock.mockRecommendedPractice
import com.example.learndsandalgorithm.data.mock.mockUserStats
import com.example.learndsandalgorithm.data.repository.ContentRepositoryImpl
import com.example.learndsandalgorithm.data.repository.MockProgressRepository
import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.model.Progress
import com.example.learndsandalgorithm.domain.model.UserStats
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import com.example.learndsandalgorithm.domain.usecase.GetProgress
import com.example.learndsandalgorithm.domain.usecase.GetTopics
import com.example.learndsandalgorithm.domain.usecase.GetUserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    @Suppress("UNUSED_PARAMETER")
    constructor(
        getTopics: GetTopics,
        getProgress: GetProgress,
        getUserStats: GetUserStats
    ) : this(
        contentRepository = ContentRepositoryImpl(),
        progressRepository = MockProgressRepository()
    )

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val completedIds = progressRepository.getCompletedLessonIds()
                val totalXp = progressRepository.getTotalXp()
                val completedCount = completedIds.size

                val allTopics = contentRepository.getTopics()
                val availableTopics = allTopics.filter { topic ->
                    contentRepository.getLessonsByTopic(topic.id).isNotEmpty()
                }

                val allLessons = availableTopics.flatMap { contentRepository.getLessonsByTopic(it.id) }
                val totalAvailableLessons = allLessons.size

                val overallPercent = if (totalAvailableLessons > 0) {
                    ((completedCount.toFloat() / totalAvailableLessons) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }

                val stats = UserStats(
                    streakDays = mockUserStats.streakDays,
                    totalXp = totalXp,
                    completedLessons = completedCount
                )

                val progress = Progress(
                    overallProgress = overallPercent,
                    dataStructuresProgress = overallPercent,
                    algorithmsProgress = overallPercent,
                    quizScore = 32
                )

                _uiState.update {
                    it.copy(
                        userStats = stats,
                        progress = progress,
                        topics = availableTopics,
                        activities = mockActivities,
                        recommendedChallenge = mockRecommendedPractice,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
