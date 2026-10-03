package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.model.Lesson

interface LessonRepository {
    fun getLessons(): List<Lesson>
    fun getLessonsByTopicId(topicId: String): List<Lesson>
    fun getLessonById(id: String): Lesson?
}
