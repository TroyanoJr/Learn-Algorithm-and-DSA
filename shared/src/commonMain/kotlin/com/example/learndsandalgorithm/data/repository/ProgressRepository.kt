package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.model.Progress

interface ProgressRepository {
    fun getProgress(): Progress
    fun updateLessonProgress(lessonId: String, isCompleted: Boolean)
}
