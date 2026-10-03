package com.example.learndsandalgorithm.domain.usecase

import com.example.learndsandalgorithm.data.repository.UserRepository
import com.example.learndsandalgorithm.domain.model.UserStats

class GetUserStats(private val userRepository: UserRepository) {
    operator fun invoke(): UserStats {
        return userRepository.getUserStats()
    }
}
