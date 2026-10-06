package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.data.mock.mockProgress
import com.example.learndsandalgorithm.domain.model.Progress
import com.russhwolf.settings.Settings

class SettingsProgressRepository(
    private val settings: Settings = Settings()
) : ProgressRepository {

    companion object {
        private const val KEY_COMPLETED_LESSON_IDS = "completed_lesson_ids"
        private const val KEY_TOTAL_XP = "total_xp"
    }

    override fun getProgress(): Progress {
        val completedCount = getCompletedLessonIds().size
        return mockProgress.copy(
            overallProgress = completedCount
        )
    }

    override fun updateLessonProgress(lessonId: String, isCompleted: Boolean) {
        if (isCompleted) {
            markLessonCompleted(lessonId, 0)
        }
    }

    override fun getCompletedLessonIds(): Set<String> {
        val rawString = settings.getString(KEY_COMPLETED_LESSON_IDS, "")
        if (rawString.isBlank()) return emptySet()
        return rawString.split(",").filter { it.isNotBlank() }.toSet()
    }

    override fun getTotalXp(): Int {
        return settings.getInt(KEY_TOTAL_XP, 0)
    }

    override fun markLessonCompleted(lessonId: String, xp: Int) {
        if (lessonId.isBlank()) return
        val currentSet = getCompletedLessonIds().toMutableSet()
        if (currentSet.add(lessonId)) {
            settings.putString(KEY_COMPLETED_LESSON_IDS, currentSet.joinToString(","))
            val currentXp = getTotalXp()
            settings.putInt(KEY_TOTAL_XP, currentXp + xp)
        }
    }
}
