package com.example.learndsandalgorithm.domain.usecase

import com.example.learndsandalgorithm.data.repository.TopicRepository
import com.example.learndsandalgorithm.domain.model.Topic

class GetTopics(private val topicRepository: TopicRepository) {
    operator fun invoke(): List<Topic> {
        return topicRepository.getTopics()
    }
}
