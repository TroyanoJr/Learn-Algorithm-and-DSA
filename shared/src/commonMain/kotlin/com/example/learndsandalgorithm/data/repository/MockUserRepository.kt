package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.data.mock.mockUserStats
import com.example.learndsandalgorithm.domain.model.UserStats

class MockUserRepository : UserRepository {
    override fun getUserStats(): UserStats {
        return mockUserStats
    }
}
