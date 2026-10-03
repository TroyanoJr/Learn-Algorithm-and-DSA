package com.example.learndsandalgorithm.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learndsandalgorithm.data.mock.mockActivities
import com.example.learndsandalgorithm.data.mock.mockRecommendedPractice
import com.example.learndsandalgorithm.domain.usecase.GetProgress
import com.example.learndsandalgorithm.domain.usecase.GetTopics
import com.example.learndsandalgorithm.domain.usecase.GetUserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTopics: GetTopics,
    private val getProgress: GetProgress,
    private val getUserStats: GetUserStats
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val topics = getTopics()
                val progress = getProgress()
                val stats = getUserStats()

                _uiState.update {
                    it.copy(
                        userStats = stats,
                        progress = progress,
                        topics = topics,
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
