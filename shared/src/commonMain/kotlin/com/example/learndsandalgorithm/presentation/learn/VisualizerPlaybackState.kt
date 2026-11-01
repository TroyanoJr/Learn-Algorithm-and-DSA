package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

internal class VisualizerPlaybackState(
    val initialStep: Int,
    val stepCount: Int,
    savedStepIndex: Int = initialStep
) {
    val minStepIndex: Int = initialStep
    val maxStepIndex: Int = if (stepCount > 0) {
        (stepCount - 1).coerceAtLeast(minStepIndex)
    } else {
        minStepIndex
    }

    var stepIndex by mutableIntStateOf(savedStepIndex.coerceIn(minStepIndex, maxStepIndex))
    var isPlaying by mutableStateOf(false)

    val safeStepIndex: Int get() = stepIndex.coerceIn(minStepIndex, maxStepIndex)
    val isCompleted: Boolean get() = stepCount == 0 || safeStepIndex >= maxStepIndex
    val canGoPrevious: Boolean get() = safeStepIndex > minStepIndex

    fun reset() {
        isPlaying = false
        stepIndex = initialStep
    }

    fun previous() {
        isPlaying = false
        if (canGoPrevious) {
            stepIndex--
        }
    }

    fun next() {
        isPlaying = false
        if (!isCompleted) {
            if (safeStepIndex < 0) {
                stepIndex = 0
            } else if (safeStepIndex < maxStepIndex) {
                stepIndex++
            }
        }
    }

    fun togglePlayback() {
        if (!isCompleted) {
            isPlaying = !isPlaying
        } else {
            isPlaying = false
        }
    }

    companion object {
        fun saver(initialStep: Int, stepCount: Int): Saver<VisualizerPlaybackState, Int> = Saver(
            save = { it.safeStepIndex },
            restore = { saved ->
                VisualizerPlaybackState(
                    initialStep = initialStep,
                    stepCount = stepCount,
                    savedStepIndex = saved
                )
            }
        )
    }
}

@Composable
internal fun rememberVisualizerPlaybackState(
    stepCount: Int,
    initialStep: Int = 0,
    stepDelayMillis: Long = 1000L,
    key: Any? = null
): VisualizerPlaybackState {
    val state = rememberSaveable(
        key,
        saver = VisualizerPlaybackState.saver(initialStep, stepCount)
    ) {
        VisualizerPlaybackState(
            initialStep = initialStep,
            stepCount = stepCount,
            savedStepIndex = initialStep
        )
    }

    LaunchedEffect(state.isPlaying, state.safeStepIndex, state.stepCount, key) {
        if (state.isPlaying && !state.isCompleted) {
            delay(stepDelayMillis)
            if (state.isPlaying) {
                if (state.safeStepIndex < 0) {
                    state.stepIndex = 0
                } else if (state.safeStepIndex < state.maxStepIndex) {
                    state.stepIndex++
                } else {
                    state.isPlaying = false
                }
            }
        } else if (state.isCompleted) {
            state.isPlaying = false
        }
    }

    return state
}
