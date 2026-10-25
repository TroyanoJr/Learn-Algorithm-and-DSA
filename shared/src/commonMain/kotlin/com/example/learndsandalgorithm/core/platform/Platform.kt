package com.example.learndsandalgorithm.core.platform

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun LandscapeOrientationEffect(enabled: Boolean)
