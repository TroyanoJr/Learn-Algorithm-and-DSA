package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.data.mock.mockTopics
import com.example.learndsandalgorithm.domain.model.Topic

class MockTopicRepository : TopicRepository {
    override fun getTopics(): List<Topic> {
        return mockTopics
    }

    override fun getTopicById(id: String): Topic? {
        return mockTopics.find { it.id == id }
    }
}
