package com.example.learndsandalgorithm.data.dto

import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.model.Question
import com.example.learndsandalgorithm.domain.model.QuestionDifficulty
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.TopicCategory
import com.example.learndsandalgorithm.domain.model.TopicLevel
import kotlinx.serialization.Serializable

@Serializable
internal data class TopicDto(
    val id: String,
    val title: String,
    val category: String,
    val level: String,
    val order: Int
) {
    fun toDomain(): Topic = Topic(
        id = id,
        title = title,
        category = runCatching { TopicCategory.valueOf(category) }.getOrDefault(TopicCategory.DATA_STRUCTURES),
        level = runCatching { TopicLevel.valueOf(level) }.getOrDefault(TopicLevel.FOUNDATION),
        order = order
    )
}

@Serializable
internal data class LessonDto(
    val id: String,
    val topicId: String,
    val title: String,
    val order: Int,
    val xp: Int,
    val contentPath: String
) {
    fun toDomain(): Lesson = Lesson(
        id = id,
        topicId = topicId,
        title = title,
        order = order,
        xp = xp,
        contentPath = contentPath
    )
}

@Serializable
internal data class QuestionDto(
    val id: String,
    val topicId: String,
    val statement: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val difficulty: String,
    val lessonId: String? = null
) {
    fun toDomain(): Question = Question(
        id = id,
        topicId = topicId,
        statement = statement,
        options = options,
        correctOptionIndex = correctOptionIndex,
        explanation = explanation,
        difficulty = runCatching { QuestionDifficulty.valueOf(difficulty) }.getOrDefault(QuestionDifficulty.EASY),
        lessonId = lessonId
    )
}
