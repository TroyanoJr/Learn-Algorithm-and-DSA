package com.example.learndsandalgorithm.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learndsandalgorithm.data.mock.mockActivities
import com.example.learndsandalgorithm.data.mock.mockUserStats
import com.example.learndsandalgorithm.data.repository.ContentRepositoryImpl
import com.example.learndsandalgorithm.data.repository.MockProgressRepository
import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.model.Progress
import com.example.learndsandalgorithm.domain.model.TopicCategory
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

                // Data Structures vs Algorithms category calculations
                val dsTopics = availableTopics.filter { it.category == TopicCategory.DATA_STRUCTURES }
                val dsLessons = dsTopics.flatMap { contentRepository.getLessonsByTopic(it.id) }
                val dsCompletedCount = dsLessons.count { it.id in completedIds }
                val dsTotalCount = dsLessons.size
                val dsPercent = if (dsTotalCount > 0) ((dsCompletedCount.toFloat() / dsTotalCount) * 100).toInt().coerceIn(0, 100) else 0

                val algoTopics = availableTopics.filter { it.category == TopicCategory.ALGORITHMS }
                val algoLessons = algoTopics.flatMap { contentRepository.getLessonsByTopic(it.id) }
                val algoCompletedCount = algoLessons.count { it.id in completedIds }
                val algoTotalCount = algoLessons.size
                val algoPercent = if (algoTotalCount > 0) ((algoCompletedCount.toFloat() / algoTotalCount) * 100).toInt().coerceIn(0, 100) else 0

                // Continue Learning active lesson selection
                var activeContinueInfo: ContinueLearningInfo? = null
                for (topic in availableTopics) {
                    val topicLessons = contentRepository.getLessonsByTopic(topic.id)
                    val nextIncomplete = topicLessons.firstOrNull { it.id !in completedIds }
                    if (nextIncomplete != null) {
                        val topicCompletedCount = topicLessons.count { it.id in completedIds }
                        val progressFloat = if (topicLessons.isNotEmpty()) topicCompletedCount.toFloat() / topicLessons.size else 0f
                        val catName = if (topic.category == TopicCategory.DATA_STRUCTURES) "Data Structures" else "Algorithms"
                        activeContinueInfo = ContinueLearningInfo(
                            topicId = topic.id,
                            topicTitle = topic.title,
                            lessonId = nextIncomplete.id,
                            lessonTitle = nextIncomplete.title,
                            lessonOrder = nextIncomplete.order,
                            totalTopicLessons = topicLessons.size,
                            categoryName = catName,
                            topicProgressFloat = progressFloat,
                            isAllComplete = false
                        )
                        break
                    }
                }

                if (activeContinueInfo == null && availableTopics.isNotEmpty()) {
                    val firstTopic = availableTopics.first()
                    val topicLessons = contentRepository.getLessonsByTopic(firstTopic.id)
                    val lastLesson = topicLessons.lastOrNull()
                    if (lastLesson != null) {
                        activeContinueInfo = ContinueLearningInfo(
                            topicId = firstTopic.id,
                            topicTitle = "Curriculum Complete! 🎉",
                            lessonId = lastLesson.id,
                            lessonTitle = "All Available Lessons Complete",
                            lessonOrder = topicLessons.size,
                            totalTopicLessons = topicLessons.size,
                            categoryName = "Curriculum",
                            topicProgressFloat = 1.0f,
                            isAllComplete = true
                        )
                    }
                }

                // Recommended Practice Selection
                val targetTopic = availableTopics.firstOrNull { topic ->
                    val topicLessons = contentRepository.getLessonsByTopic(topic.id)
                    topicLessons.any { it.id !in completedIds }
                } ?: availableTopics.firstOrNull()

                val activeRecommendedInfo = if (targetTopic != null) {
                    val quizQuestions = contentRepository.getQuestionsByTopic(targetTopic.id)
                    val qCount = quizQuestions.size
                    RecommendedPracticeInfo(
                        topicId = targetTopic.id,
                        topicTitle = "${targetTopic.title} Quiz",
                        questionCount = qCount,
                        description = "$qCount questions to reinforce your ${targetTopic.title.lowercase()} skills."
                    )
                } else null

                // Recent Topics Progress (topics with at least 1 completed lesson)
                val recentTopicsMap = mutableMapOf<String, Int>()
                for (topic in availableTopics) {
                    val topicLessons = contentRepository.getLessonsByTopic(topic.id)
                    val completedInTopic = topicLessons.count { it.id in completedIds }
                    if (completedInTopic > 0) {
                        val percent = ((completedInTopic.toFloat() / topicLessons.size) * 100).toInt()
                        recentTopicsMap[topic.title] = percent
                    }
                }

                val stats = UserStats(
                    streakDays = mockUserStats.streakDays,
                    totalXp = totalXp,
                    completedLessons = completedCount
                )

                val progress = Progress(
                    overallProgress = overallPercent,
                    dataStructuresProgress = dsPercent,
                    algorithmsProgress = algoPercent,
                    quizScore = 0
                )

                _uiState.update {
                    it.copy(
                        userStats = stats,
                        progress = progress,
                        topics = availableTopics,
                        continueLearning = activeContinueInfo,
                        dsCompletedLessons = dsCompletedCount,
                        dsTotalLessons = dsTotalCount,
                        dsProgressPercent = dsPercent,
                        algoCompletedLessons = algoCompletedCount,
                        algoTotalLessons = algoTotalCount,
                        algoProgressPercent = algoPercent,
                        recentTopicsProgress = recentTopicsMap,
                        activities = mockActivities,
                        recommendedPractice = activeRecommendedInfo,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
