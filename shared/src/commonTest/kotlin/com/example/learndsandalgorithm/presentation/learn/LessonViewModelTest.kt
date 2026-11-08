package com.example.learndsandalgorithm.presentation.learn

import com.example.learndsandalgorithm.data.repository.SettingsProgressRepository
import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.model.Question
import com.example.learndsandalgorithm.domain.model.QuestionDifficulty
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeContentRepository(
    private val lessonMap: Map<String, Lesson> = emptyMap(),
    private val lessonContentMap: Map<String, String> = emptyMap(),
    private val lessonQuestionsMap: Map<String, List<Question>> = emptyMap()
) : ContentRepository {

    override suspend fun getTopics() = emptyList<Topic>()
    override suspend fun getLessonsByTopic(topicId: String) = lessonMap.values.filter { it.topicId == topicId }
    override suspend fun getLessonById(id: String) = lessonMap[id]
    override suspend fun getLessonContent(lesson: Lesson) = lessonContentMap[lesson.id] ?: ""
    override suspend fun getQuestionsByTopic(topicId: String) = emptyList<Question>()
    override suspend fun getQuestionsByLessonId(lessonId: String) = lessonQuestionsMap[lessonId] ?: emptyList()
}

class LessonViewModelTest {

    private val testLesson = Lesson(
        id = "lesson_arrays_1",
        topicId = "arrays",
        title = "Introduction to Arrays",
        order = 1,
        xp = 50,
        contentPath = "files/lessons/lesson_arrays_1.md"
    )

    private val q1 = Question(
        id = "q1",
        topicId = "arrays",
        lessonId = "lesson_arrays_1",
        statement = "What is index 0?",
        options = listOf("First element", "Second element", "Third element", "Fourth element"),
        correctOptionIndex = 0,
        explanation = "Arrays use 0-based indexing.",
        difficulty = QuestionDifficulty.EASY
    )

    private val q2 = Question(
        id = "q2",
        topicId = "arrays",
        lessonId = "lesson_arrays_1",
        statement = "What is time complexity of access?",
        options = listOf("O(N)", "O(1)", "O(log N)", "O(N^2)"),
        correctOptionIndex = 1,
        explanation = "Direct memory indexing gives O(1) access.",
        difficulty = QuestionDifficulty.EASY
    )

    @Test
    fun testDetectIncompleteQuestionProgress() = runBlocking {
        val contentRepo = FakeContentRepository(
            lessonMap = mapOf(testLesson.id to testLesson),
            lessonQuestionsMap = mapOf(testLesson.id to listOf(q1, q2))
        )
        val progressRepo = SettingsProgressRepository(MapSettings())
        val viewModel = LessonViewModel(contentRepo, progressRepo, mainDispatcher = Dispatchers.Unconfined)

        viewModel.loadLesson(testLesson.id)

        val state = viewModel.uiState.value as LessonUiState.Success
        assertFalse(state.allQuestionsAnswered, "Lesson with unanswered questions should not be marked allQuestionsAnswered")
        assertEquals(0, state.answeredQuestionIds.size)
    }

    @Test
    fun testDetectCompleteQuestionProgress() = runBlocking {
        val settings = MapSettings()
        val progressRepo = SettingsProgressRepository(settings)
        progressRepo.saveQuestionAnswer(testLesson.id, q1.id, 0)
        progressRepo.saveQuestionAnswer(testLesson.id, q2.id, 1)

        val contentRepo = FakeContentRepository(
            lessonMap = mapOf(testLesson.id to testLesson),
            lessonQuestionsMap = mapOf(testLesson.id to listOf(q1, q2))
        )
        val viewModel = LessonViewModel(contentRepo, progressRepo, mainDispatcher = Dispatchers.Unconfined)

        viewModel.loadLesson(testLesson.id)

        val state = viewModel.uiState.value as LessonUiState.Success
        assertTrue(state.allQuestionsAnswered, "Lesson with all questions answered should have allQuestionsAnswered = true")
        assertEquals(setOf(q1.id, q2.id), state.answeredQuestionIds)
    }

    @Test
    fun testZeroQuestionsLessonAutoSatisfiesRequirement() = runBlocking {
        val contentRepo = FakeContentRepository(
            lessonMap = mapOf(testLesson.id to testLesson),
            lessonQuestionsMap = mapOf(testLesson.id to emptyList())
        )
        val progressRepo = SettingsProgressRepository(MapSettings())
        val viewModel = LessonViewModel(contentRepo, progressRepo, mainDispatcher = Dispatchers.Unconfined)

        viewModel.loadLesson(testLesson.id)

        val state = viewModel.uiState.value as LessonUiState.Success
        assertTrue(state.allQuestionsAnswered, "Lesson with zero questions automatically satisfies question completion")
    }

    @Test
    fun testBlockingLessonCompletionWhenUnansweredQuestionsRemain() = runBlocking {
        val contentRepo = FakeContentRepository(
            lessonMap = mapOf(testLesson.id to testLesson),
            lessonQuestionsMap = mapOf(testLesson.id to listOf(q1, q2))
        )
        val progressRepo = SettingsProgressRepository(MapSettings())
        val viewModel = LessonViewModel(contentRepo, progressRepo, mainDispatcher = Dispatchers.Unconfined)

        viewModel.loadLesson(testLesson.id)

        viewModel.markLessonCompleted()

        val state = viewModel.uiState.value as LessonUiState.Success
        assertTrue(state.completionBlocked, "Completion attempt should set completionBlocked = true")
        assertFalse(state.isCompleted, "Lesson should not be marked completed")
        assertFalse(progressRepo.getCompletedLessonIds().contains(testLesson.id), "Completed lesson ID should not be stored in repository")
        assertEquals(0, progressRepo.getTotalXp(), "XP should not be awarded")
    }

    @Test
    fun testAllowCompletionAfterAllQuestionsAreAnswered() = runBlocking {
        val contentRepo = FakeContentRepository(
            lessonMap = mapOf(testLesson.id to testLesson),
            lessonQuestionsMap = mapOf(testLesson.id to listOf(q1, q2))
        )
        val progressRepo = SettingsProgressRepository(MapSettings())
        val viewModel = LessonViewModel(contentRepo, progressRepo, mainDispatcher = Dispatchers.Unconfined)

        viewModel.loadLesson(testLesson.id)

        // Select answers for both questions
        viewModel.selectQuestionAnswer(q1.id, 0)
        viewModel.selectQuestionAnswer(q2.id, 1)

        val stateBefore = viewModel.uiState.value as LessonUiState.Success
        assertTrue(stateBefore.allQuestionsAnswered)

        viewModel.markLessonCompleted()

        val stateAfter = viewModel.uiState.value as LessonUiState.Success
        assertTrue(stateAfter.isCompleted)
        assertFalse(stateAfter.completionBlocked)
        assertTrue(progressRepo.getCompletedLessonIds().contains(testLesson.id))
        assertEquals(50, progressRepo.getTotalXp(), "XP should be awarded upon completion")
    }

    @Test
    fun testPreservingPreviouslyCompletedLessons() = runBlocking {
        val settings = MapSettings()
        val progressRepo = SettingsProgressRepository(settings)
        progressRepo.markLessonCompleted(testLesson.id, 50)

        val contentRepo = FakeContentRepository(
            lessonMap = mapOf(testLesson.id to testLesson),
            lessonQuestionsMap = mapOf(testLesson.id to listOf(q1, q2))
        )
        val viewModel = LessonViewModel(contentRepo, progressRepo, mainDispatcher = Dispatchers.Unconfined)

        viewModel.loadLesson(testLesson.id)

        val state = viewModel.uiState.value as LessonUiState.Success
        assertTrue(state.isCompleted, "Previously completed lesson must remain isCompleted = true")
    }

    @Test
    fun testPreventingDuplicateXpAwards() = runBlocking {
        val contentRepo = FakeContentRepository(
            lessonMap = mapOf(testLesson.id to testLesson),
            lessonQuestionsMap = mapOf(testLesson.id to emptyList())
        )
        val progressRepo = SettingsProgressRepository(MapSettings())
        val viewModel = LessonViewModel(contentRepo, progressRepo, mainDispatcher = Dispatchers.Unconfined)

        viewModel.loadLesson(testLesson.id)

        viewModel.markLessonCompleted()
        viewModel.markLessonCompleted() // Repeated completion call

        assertEquals(50, progressRepo.getTotalXp(), "Repeated completion calls should not award duplicate XP")
    }

    @Test
    fun testRestoringAnswerAfterOptionShuffling() {
        val rawQ = Question(
            id = "q1",
            topicId = "arrays",
            statement = "Sample Question",
            options = listOf("Option A", "Option B", "Option C", "Option D"),
            correctOptionIndex = 0,
            explanation = "Ex",
            difficulty = QuestionDifficulty.EASY
        )

        val prepared1 = prepareLessonQuestionsWithMapping(
            rawQuestions = listOf(rawQ),
            shuffleQuestionOrder = { it }
        )
        val result1 = prepared1.first()

        val displayedChoice = 1 // User taps 2nd option in session 1
        val originalChoice = result1.originalOptionIndices[displayedChoice]

        val prepared2 = prepareLessonQuestionsWithMapping(
            rawQuestions = listOf(rawQ),
            shuffleQuestionOrder = { it }
        )
        val result2 = prepared2.first()

        val restoredDisplayedChoice = result2.originalOptionIndices.indexOf(originalChoice)
        val restoredOptionText = result2.question.options[restoredDisplayedChoice]

        assertEquals(
            result1.question.options[displayedChoice],
            restoredOptionText,
            "Restored answer text must match the user's selected choice regardless of option reshuffling"
        )
    }
}
