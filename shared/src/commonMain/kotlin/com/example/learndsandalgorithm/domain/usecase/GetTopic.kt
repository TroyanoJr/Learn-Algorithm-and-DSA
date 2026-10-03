package com.example.learndsandalgorithm.domain.usecase

import com.example.learndsandalgorithm.data.repository.TopicRepository
import com.example.learndsandalgorithm.domain.model.Topic

class GetTopic(private val topicRepository: TopicRepository) {
    operator fun invoke(id: String): Topic? {
        return topicRepository.getTopicById(id)
    }
}
