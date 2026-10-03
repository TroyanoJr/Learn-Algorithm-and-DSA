package com.example.learndsandalgorithm.domain.repository

import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.model.Question
import com.example.learndsandalgorithm.domain.model.Topic

interface ContentRepository {
    suspend fun getTopics(): List<Topic>
    suspend fun getLessonsByTopic(topicId: String): List<Lesson>
    suspend fun getLessonById(id: String): Lesson?
    suspend fun getLessonContent(lesson: Lesson): String
    suspend fun getQuestionsByTopic(topicId: String): List<Question>
}
