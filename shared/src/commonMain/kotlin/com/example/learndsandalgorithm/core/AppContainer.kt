package com.example.learndsandalgorithm.core

import com.example.learndsandalgorithm.data.repository.ContentRepositoryImpl
import com.example.learndsandalgorithm.data.repository.MockLessonRepository
import com.example.learndsandalgorithm.data.repository.MockProgressRepository
import com.example.learndsandalgorithm.data.repository.MockTopicRepository
import com.example.learndsandalgorithm.data.repository.MockUserRepository
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import com.example.learndsandalgorithm.domain.usecase.GetLesson
import com.example.learndsandalgorithm.domain.usecase.GetLessons
import com.example.learndsandalgorithm.domain.usecase.GetLessonsByTopic
import com.example.learndsandalgorithm.domain.usecase.GetProgress
import com.example.learndsandalgorithm.domain.usecase.GetTopic
import com.example.learndsandalgorithm.domain.usecase.GetTopics
import com.example.learndsandalgorithm.domain.usecase.GetUserStats

class AppContainer {
    // Repositories
    val topicRepository by lazy { MockTopicRepository() }
    val lessonRepository by lazy { MockLessonRepository() }
    val progressRepository by lazy { MockProgressRepository() }
    val userRepository by lazy { MockUserRepository() }
    val contentRepository: ContentRepository by lazy { ContentRepositoryImpl() }

    // Use Cases
    val getTopics by lazy { GetTopics(topicRepository) }
    val getTopic by lazy { GetTopic(topicRepository) }
    val getLessons by lazy { GetLessons(lessonRepository) }
    val getLessonsByTopic by lazy { GetLessonsByTopic(lessonRepository) }
    val getLesson by lazy { GetLesson(lessonRepository) }
    val getProgress by lazy { GetProgress(progressRepository) }
    val getUserStats by lazy { GetUserStats(userRepository) }
}
