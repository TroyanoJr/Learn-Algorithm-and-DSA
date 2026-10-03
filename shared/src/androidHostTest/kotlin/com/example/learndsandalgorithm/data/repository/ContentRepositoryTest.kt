package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.repository.ContentRepository
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContentRepositoryTest {

    private val repository: ContentRepository = ContentRepositoryImpl(
        readResourceText = { path ->
            val resourcePath = "composeResources/learndsandalgorithm.shared.generated.resources/$path"
            val classLoader = Thread.currentThread().contextClassLoader
            val stream = classLoader?.getResourceAsStream(resourcePath)
                ?: File("src/commonMain/composeResources/$path").takeIf { it.exists() }?.inputStream()
                ?: File("shared/src/commonMain/composeResources/$path").takeIf { it.exists() }?.inputStream()
                ?: throw IllegalArgumentException("Resource not found: $path")
            stream.bufferedReader().use { it.readText() }
        }
    )

    @Test
    fun testLoadAllTopics() = runBlocking {
        val topics = repository.getTopics()
        assertEquals(20, topics.size, "Should load exactly 20 topics")
    }

    @Test
    fun testLoadAllLessons() = runBlocking {
        val arraysLessons = repository.getLessonsByTopic("arrays")
        val searchLessons = repository.getLessonsByTopic("linear_search")
        val sortingLessons = repository.getLessonsByTopic("sorting")
        val totalLessons = arraysLessons.size + searchLessons.size + sortingLessons.size
        assertEquals(14, totalLessons, "Should load exactly 14 lessons total")
        assertEquals(5, arraysLessons.size, "Arrays should have 5 lessons")
        assertEquals(4, searchLessons.size, "Searching should have 4 lessons")
        assertEquals(5, sortingLessons.size, "Sorting should have 5 lessons")
    }

    @Test
    fun testLoadLessonContent() = runBlocking {
        val lesson = repository.getLessonById("lesson_arrays_1")
        assertNotNull(lesson, "Lesson 'lesson_arrays_1' should exist")
        val content = repository.getLessonContent(lesson)
        assertTrue(content.isNotBlank(), "Lesson content should not be blank")
        assertTrue(content.contains("Introduction to Arrays"), "Content should contain lesson title heading")
    }

    @Test
    fun testLoadAllQuestions() = runBlocking {
        val arraysQuestions = repository.getQuestionsByTopic("arrays")
        val searchQuestions = repository.getQuestionsByTopic("linear_search")
        val totalQuestions = arraysQuestions.size + searchQuestions.size
        assertEquals(6, totalQuestions, "Should load exactly 6 questions total")
        assertEquals(3, arraysQuestions.size, "Arrays should have 3 questions")
        assertEquals(3, searchQuestions.size, "Linear Search should have 3 questions")
    }

    @Test
    fun testFilterLessonsAndQuestionsByTopic() = runBlocking {
        val arraysLessons = repository.getLessonsByTopic("arrays")
        assertTrue(arraysLessons.all { it.topicId == "arrays" })

        val arraysQuestions = repository.getQuestionsByTopic("arrays")
        assertTrue(arraysQuestions.all { it.topicId == "arrays" })
    }
}
