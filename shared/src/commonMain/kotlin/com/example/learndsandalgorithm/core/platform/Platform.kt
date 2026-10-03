package com.example.learndsandalgorithm.core.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
