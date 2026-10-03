package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.model.UserStats

interface UserRepository {
    fun getUserStats(): UserStats
}
