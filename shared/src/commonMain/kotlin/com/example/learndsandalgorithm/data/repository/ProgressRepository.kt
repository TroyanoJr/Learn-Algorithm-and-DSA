package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.model.Progress

interface ProgressRepository {
    fun getProgress(): Progress
    fun updateLessonProgress(lessonId: String, isCompleted: Boolean)
    fun getCompletedLessonIds(): Set<String>
    fun getTotalXp(): Int
    fun markLessonCompleted(lessonId: String, xp: Int)
}
