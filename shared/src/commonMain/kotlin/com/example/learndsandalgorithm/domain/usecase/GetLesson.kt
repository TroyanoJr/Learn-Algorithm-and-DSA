package com.example.learndsandalgorithm.domain.usecase

import com.example.learndsandalgorithm.data.repository.LessonRepository
import com.example.learndsandalgorithm.domain.model.Lesson

class GetLesson(private val lessonRepository: LessonRepository) {
    operator fun invoke(id: String): Lesson? {
        return lessonRepository.getLessonById(id)
    }
}
