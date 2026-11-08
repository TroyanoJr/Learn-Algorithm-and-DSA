package com.example.learndsandalgorithm.data.repository

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsProgressRepositoryTest {

    @Test
    fun testEmptyInitialState() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        assertTrue(repository.getCompletedLessonIds().isEmpty(), "Initial completed lessons should be empty")
        assertEquals(0, repository.getTotalXp(), "Initial total XP should be 0")
    }

    @Test
    fun testCompletingOneLessonStoresIdAndXp() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.markLessonCompleted("lesson_arrays_1", 50)

        assertEquals(setOf("lesson_arrays_1"), repository.getCompletedLessonIds())
        assertEquals(50, repository.getTotalXp())
    }

    @Test
    fun testCompletingSameLessonTwiceIsIdempotent() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.markLessonCompleted("lesson_arrays_1", 50)
        repository.markLessonCompleted("lesson_arrays_1", 50) // Duplicate call

        assertEquals(1, repository.getCompletedLessonIds().size, "Lesson ID should not be duplicated")
        assertEquals(setOf("lesson_arrays_1"), repository.getCompletedLessonIds())
        assertEquals(50, repository.getTotalXp(), "XP should not be duplicated")
    }

    @Test
    fun testCompletingMultipleLessonsAccumulatesXp() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.markLessonCompleted("lesson_arrays_1", 50)
        repository.markLessonCompleted("searching_1", 25)
        repository.markLessonCompleted("sorting_1", 15)

        assertEquals(setOf("lesson_arrays_1", "searching_1", "sorting_1"), repository.getCompletedLessonIds())
        assertEquals(90, repository.getTotalXp(), "XP should accumulate correctly (50 + 25 + 15)")
    }

    @Test
    fun testNewRepositoryInstanceReadsExistingStorage() {
        val sharedSettings = MapSettings()
        val repo1 = SettingsProgressRepository(sharedSettings)

        repo1.markLessonCompleted("lesson_arrays_1", 50)
        repo1.markLessonCompleted("searching_1", 25)

        // Create a separate repository instance sharing the same storage
        val repo2 = SettingsProgressRepository(sharedSettings)

        assertEquals(setOf("lesson_arrays_1", "searching_1"), repo2.getCompletedLessonIds())
        assertEquals(75, repo2.getTotalXp())
    }

    @Test
    fun testSaveAndRestoreAnsweredQuestion() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.saveQuestionAnswer("lesson_arrays_1", "q1", 0)

        val answers = repository.getQuestionAnswers("lesson_arrays_1")
        assertEquals(mapOf("q1" to 0), answers)
    }

    @Test
    fun testSaveAndRestoreIncorrectAnswerAsAnswered() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.saveQuestionAnswer("lesson_arrays_1", "q1", 3) // Option 3 is incorrect

        val answers = repository.getQuestionAnswers("lesson_arrays_1")
        assertEquals(mapOf("q1" to 3), answers)
        assertTrue(answers.containsKey("q1"), "Incorrect answer must count as answered")
    }

    @Test
    fun testAnswersIsolatedBetweenLessons() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.saveQuestionAnswer("lesson_arrays_1", "q1", 0)
        repository.saveQuestionAnswer("lesson_arrays_2", "q2", 1)

        assertEquals(mapOf("q1" to 0), repository.getQuestionAnswers("lesson_arrays_1"))
        assertEquals(mapOf("q2" to 1), repository.getQuestionAnswers("lesson_arrays_2"))
    }

    @Test
    fun testAnswersIsolatedBetweenQuestionIds() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.saveQuestionAnswer("lesson_arrays_1", "q1", 0)
        repository.saveQuestionAnswer("lesson_arrays_1", "q2", 2)

        val answers = repository.getQuestionAnswers("lesson_arrays_1")
        assertEquals(2, answers.size)
        assertEquals(0, answers["q1"])
        assertEquals(2, answers["q2"])
    }

    @Test
    fun testUpdatingExistingAnswerPreservesAnsweredStatus() {
        val settings = MapSettings()
        val repository = SettingsProgressRepository(settings)

        repository.saveQuestionAnswer("lesson_arrays_1", "q1", 1)
        repository.saveQuestionAnswer("lesson_arrays_1", "q1", 3) // Update answer

        val answers = repository.getQuestionAnswers("lesson_arrays_1")
        assertEquals(mapOf("q1" to 3), answers)
    }
}
