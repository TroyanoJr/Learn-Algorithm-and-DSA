package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.data.mock.mockProgress
import com.example.learndsandalgorithm.domain.model.Progress

class MockProgressRepository : ProgressRepository {
    private var currentProgress = mockProgress
    private val completedLessonIds = mutableSetOf<String>()
    private var totalXp = 0

    override fun getProgress(): Progress {
        return currentProgress
    }

    override fun updateLessonProgress(lessonId: String, isCompleted: Boolean) {
        // Actualización temporal en memoria para el MVP
        currentProgress = currentProgress.copy(
            overallProgress = currentProgress.overallProgress + 1
        )
    }

    override fun getCompletedLessonIds(): Set<String> {
        return completedLessonIds.toSet()
    }

    override fun getTotalXp(): Int {
        return totalXp
    }

    override fun markLessonCompleted(lessonId: String, xp: Int) {
        if (lessonId.isBlank()) return
        if (completedLessonIds.add(lessonId)) {
            totalXp += xp
        }
    }
}
