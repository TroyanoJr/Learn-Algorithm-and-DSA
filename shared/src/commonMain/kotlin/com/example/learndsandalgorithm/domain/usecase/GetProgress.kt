package com.example.learndsandalgorithm.domain.usecase

import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.model.Progress

class GetProgress(private val progressRepository: ProgressRepository) {
    operator fun invoke(): Progress {
        return progressRepository.getProgress()
    }
}
