package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.data.dto.LessonDto
import com.example.learndsandalgorithm.data.dto.QuestionDto
import com.example.learndsandalgorithm.data.dto.TopicDto
import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.model.Question
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import kotlinx.serialization.json.Json
import learndsandalgorithm.shared.generated.resources.Res

class ContentRepositoryImpl(
    private val readResourceText: suspend (String) -> String = { path ->
        Res.readBytes(path).decodeToString()
    }
) : ContentRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var cachedTopics: List<Topic>? = null
    private var cachedLessons: List<Lesson>? = null
    private var cachedQuestions: List<Question>? = null

    override suspend fun getTopics(): List<Topic> {
        cachedTopics?.let { return it }
        val topics = try {
            val topicsJson = readResourceText("files/topics.json")
            val dtos = json.decodeFromString<List<TopicDto>>(topicsJson)
            dtos.map { it.toDomain() }.sortedBy { it.order }
        } catch (e: Exception) {
            emptyList()
        }
        cachedTopics = topics
        return topics
    }

    override suspend fun getLessonsByTopic(topicId: String): List<Lesson> {
        if (topicId.isBlank()) return emptyList()
        val allLessons = getAllLessons()
        return allLessons.filter { it.topicId == topicId }.sortedBy { it.order }
    }

    override suspend fun getLessonById(id: String): Lesson? {
        if (id.isBlank()) return null
        val allLessons = getAllLessons()
        return allLessons.find { it.id == id }
    }

    override suspend fun getLessonContent(lesson: Lesson): String {
        if (lesson.contentPath.isBlank()) {
            return "# Error\nInvalid content path."
        }
        return try {
            readResourceText(lesson.contentPath)
        } catch (e: Exception) {
            "# Error\nLesson content could not be loaded."
        }
    }

    override suspend fun getQuestionsByTopic(topicId: String): List<Question> {
        if (topicId.isBlank()) return emptyList()
        val allQuestions = getAllQuestions()
        return allQuestions.filter { it.topicId == topicId }
    }

    private suspend fun getAllLessons(): List<Lesson> {
        cachedLessons?.let { return it }
        val lessons = try {
            val lessonsJson = readResourceText("files/lessons.json")
            val dtos = json.decodeFromString<List<LessonDto>>(lessonsJson)
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
        cachedLessons = lessons
        return lessons
    }

    private suspend fun getAllQuestions(): List<Question> {
        cachedQuestions?.let { return it }
        val questions = try {
            val questionsJson = readResourceText("files/questions.json")
            val dtos = json.decodeFromString<List<QuestionDto>>(questionsJson)
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
        cachedQuestions = questions
        return questions
    }
}
