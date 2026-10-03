package com.example.learndsandalgorithm.domain.usecase

import com.example.learndsandalgorithm.data.repository.LessonRepository
import com.example.learndsandalgorithm.domain.model.Lesson

class GetLessons(private val lessonRepository: LessonRepository) {
    operator fun invoke(): List<Lesson> {
        return lessonRepository.getLessons()
    }
}
