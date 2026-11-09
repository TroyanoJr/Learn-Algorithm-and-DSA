package com.example.learndsandalgorithm.presentation.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.model.Question
import com.example.learndsandalgorithm.domain.model.QuestionType
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
        val isCompleted: Boolean = false,
        val questionAnswers: Map<String, Int> = emptyMap(),
        val displayedSelectedOptions: Map<String, Int> = emptyMap(),
        val answeredQuestionIds: Set<String> = emptySet(),
        val allQuestionsAnswered: Boolean = true,
        val completionBlocked: Boolean = false
    ) : LessonUiState
    data class Error(val message: String) : LessonUiState
}

class LessonViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Loading)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    private var currentQuestionOriginalIndices: Map<String, List<Int>> = emptyMap()

    fun loadLesson(lessonId: String) {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Success && currentState.lesson.id == lessonId) {
            return
        }
        if (currentState !is LessonUiState.Success) {
            _uiState.value = LessonUiState.Loading
        }
        viewModelScope.launch(mainDispatcher) {
            try {
                val lesson = contentRepository.getLessonById(lessonId)
                if (lesson == null) {
                    _uiState.value = LessonUiState.Error("Lesson not found: $lessonId")
                    return@launch
                }
                val markdownContent = contentRepository.getLessonContent(lesson)
                val topicLessons = contentRepository.getLessonsByTopic(lesson.topicId)
                val nextLesson = topicLessons.firstOrNull { it.order > lesson.order }
                val rawQuestions = contentRepository.getQuestionsByLessonId(lesson.id)
                val isCompleted = lesson.id in progressRepository.getCompletedLessonIds()

                val prepared = prepareLessonQuestionsWithMapping(rawQuestions)
                val shuffledQuestions = prepared.map { it.question }
                val mappingMap = prepared.associate { it.question.id to it.originalOptionIndices }
                currentQuestionOriginalIndices = mappingMap

                val savedAnswers = progressRepository.getQuestionAnswers(lesson.id)
                val displayedOptions = mutableMapOf<String, Int>()
                val answeredIds = savedAnswers.keys.toSet()

                shuffledQuestions.forEach { q ->
                    val savedOriginalIdx = savedAnswers[q.id]
                    if (savedOriginalIdx != null) {
                        val originalIndices = mappingMap[q.id]
                        if (originalIndices != null) {
                            val dispIdx = originalIndices.indexOf(savedOriginalIdx)
                            if (dispIdx != -1) {
                                displayedOptions[q.id] = dispIdx
                            }
                        }
                    }
                }

                val allAnswered = rawQuestions.isEmpty() || rawQuestions.all { answeredIds.contains(it.id) }

                _uiState.value = LessonUiState.Success(
                    lesson = lesson,
                    markdownContent = markdownContent,
                    nextLessonId = nextLesson?.id,
                    questions = shuffledQuestions,
                    isCompleted = isCompleted,
                    questionAnswers = savedAnswers,
                    displayedSelectedOptions = displayedOptions,
                    answeredQuestionIds = answeredIds,
                    allQuestionsAnswered = allAnswered,
                    completionBlocked = false
                )
            } catch (e: Exception) {
                _uiState.value = LessonUiState.Error(e.message ?: "Failed to load lesson")
            }
        }
    }

    fun selectQuestionAnswer(questionId: String, displayedOptionIndex: Int) {
        val state = _uiState.value
        if (state is LessonUiState.Success) {
            val originalIndices = currentQuestionOriginalIndices[questionId]
            val originalOptionIndex = originalIndices?.getOrNull(displayedOptionIndex) ?: displayedOptionIndex

            progressRepository.saveQuestionAnswer(state.lesson.id, questionId, originalOptionIndex)

            val updatedAnswers = state.questionAnswers + (questionId to originalOptionIndex)
            val updatedDisplayed = state.displayedSelectedOptions + (questionId to displayedOptionIndex)
            val updatedAnsweredIds = state.answeredQuestionIds + questionId
            val allAnswered = state.questions.isEmpty() || state.questions.all { updatedAnsweredIds.contains(it.id) }

            _uiState.value = state.copy(
                questionAnswers = updatedAnswers,
                displayedSelectedOptions = updatedDisplayed,
                answeredQuestionIds = updatedAnsweredIds,
                allQuestionsAnswered = allAnswered,
                completionBlocked = false
            )
        }
    }

    fun markLessonCompleted() {
        val state = _uiState.value
        if (state is LessonUiState.Success) {
            if (!state.allQuestionsAnswered) {
                _uiState.value = state.copy(completionBlocked = true)
                return
            }

            if (!state.isCompleted) {
                progressRepository.markLessonCompleted(state.lesson.id, state.lesson.xp)
            }
            _uiState.value = state.copy(isCompleted = true, completionBlocked = false)
        }
    }
}

internal data class ShuffledQuestionResult(
    val question: Question,
    val originalOptionIndices: List<Int>
)

internal fun prepareLessonQuestions(
    rawQuestions: List<Question>,
    shuffleQuestionOrder: (List<Question>) -> List<Question> = { it.shuffled() }
): List<Question> = prepareLessonQuestionsWithMapping(rawQuestions, shuffleQuestionOrder).map { it.question }

internal fun prepareLessonQuestionsWithMapping(
    rawQuestions: List<Question>,
    shuffleQuestionOrder: (List<Question>) -> List<Question> = { it.shuffled() }
): List<ShuffledQuestionResult> = shuffleQuestionOrder(rawQuestions).map(::shuffleQuestionOptionsWithMapping)

internal fun shuffleQuestionOptions(question: Question): Question =
    shuffleQuestionOptionsWithMapping(question).question

internal fun shuffleQuestionOptionsWithMapping(question: Question): ShuffledQuestionResult {
    if (question.type == QuestionType.TRUE_FALSE) {
        val identityIndices = question.options.indices.toList()
        return ShuffledQuestionResult(question, identityIndices)
    }

    val indexedOptions = question.options.mapIndexed { index, text -> index to text }
    val shuffled = indexedOptions.shuffled()
    val newCorrectIndex = shuffled.indexOfFirst { it.first == question.correctOptionIndex }
    val shuffledQuestion = question.copy(
        options = shuffled.map { it.second },
        correctOptionIndex = if (newCorrectIndex != -1) newCorrectIndex else question.correctOptionIndex
    )
    val originalIndices = shuffled.map { it.first }
    return ShuffledQuestionResult(shuffledQuestion, originalIndices)
}
