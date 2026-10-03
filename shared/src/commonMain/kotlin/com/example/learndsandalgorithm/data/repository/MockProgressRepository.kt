package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.data.mock.mockProgress
import com.example.learndsandalgorithm.domain.model.Progress

class MockProgressRepository : ProgressRepository {
    private var currentProgress = mockProgress

    override fun getProgress(): Progress {
        return currentProgress
    }

    override fun updateLessonProgress(lessonId: String, isCompleted: Boolean) {
        // Actualización temporal en memoria para el MVP
        currentProgress = currentProgress.copy(
            overallProgress = currentProgress.overallProgress + 1
        )
    }
}
