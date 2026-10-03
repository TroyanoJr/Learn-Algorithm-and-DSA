package com.example.learndsandalgorithm.domain.usecase

import com.example.learndsandalgorithm.data.repository.LessonRepository
import com.example.learndsandalgorithm.domain.model.Lesson

class GetLessonsByTopic(private val lessonRepository: LessonRepository) {
    operator fun invoke(topicId: String): List<Lesson> {
        return lessonRepository.getLessonsByTopicId(topicId)
    }
}
