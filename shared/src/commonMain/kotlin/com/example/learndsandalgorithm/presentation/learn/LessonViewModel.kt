package com.example.learndsandalgorithm.presentation.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.model.Question
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LessonUiState {
    data object Loading : LessonUiState
    data class Success(
        val lesson: Lesson,
        val markdownContent: String,
        val nextLessonId: String? = null,
        val questions: List<Question> = emptyList(),
        val isCompleted: Boolean = false
    ) : LessonUiState
    data class Error(val message: String) : LessonUiState
}

class LessonViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Loading)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    fun loadLesson(lessonId: String) {
        _uiState.value = LessonUiState.Loading
        viewModelScope.launch {
            try {
                val lesson = contentRepository.getLessonById(lessonId)
                if (lesson == null) {
                    _uiState.value = LessonUiState.Error("Lesson not found: $lessonId")
                    return@launch
                }
                val markdownContent = contentRepository.getLessonContent(lesson)
                val topicLessons = contentRepository.getLessonsByTopic(lesson.topicId)
                val nextLesson = topicLessons.firstOrNull { it.order > lesson.order }
                val questions = contentRepository.getQuestionsByLessonId(lesson.id)
                val isCompleted = lesson.id in progressRepository.getCompletedLessonIds()

                _uiState.value = LessonUiState.Success(
                    lesson = lesson,
                    markdownContent = markdownContent,
                    nextLessonId = nextLesson?.id,
                    questions = questions,
                    isCompleted = isCompleted
                )
            } catch (e: Exception) {
                _uiState.value = LessonUiState.Error(e.message ?: "Failed to load lesson")
            }
        }
    }

    fun markLessonCompleted() {
        val state = _uiState.value
        if (state is LessonUiState.Success) {
            progressRepository.markLessonCompleted(state.lesson.id, state.lesson.xp)
            _uiState.value = state.copy(isCompleted = true)
        }
    }
}
