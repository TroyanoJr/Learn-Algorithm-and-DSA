package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.data.mock.mockLessons
import com.example.learndsandalgorithm.domain.model.Lesson

class MockLessonRepository : LessonRepository {
    override fun getLessons(): List<Lesson> {
        return mockLessons
    }

    override fun getLessonsByTopicId(topicId: String): List<Lesson> {
        return mockLessons.filter { it.topicId == topicId }
    }

    override fun getLessonById(id: String): Lesson? {
        return mockLessons.find { it.id == id }
    }
}
