package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.model.Topic

interface TopicRepository {
    fun getTopics(): List<Topic>
    fun getTopicById(id: String): Topic?
}
