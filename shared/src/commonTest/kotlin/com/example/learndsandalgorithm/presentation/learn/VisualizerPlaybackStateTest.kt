package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.saveable.SaverScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VisualizerPlaybackStateTest {

    @Test
    fun initialStateZeroBehavesCorrectly() {
        val state = VisualizerPlaybackState(initialStep = 0, stepCount = 5)

        assertEquals(0, state.safeStepIndex)
        assertEquals(0, state.minStepIndex)
        assertEquals(4, state.maxStepIndex)
        assertFalse(state.isCompleted)
        assertFalse(state.canGoPrevious)
        assertFalse(state.isPlaying)
    }

    @Test
    fun initialStateMinusOneBehavesCorrectlyForUnstartedVisualizers() {
        val state = VisualizerPlaybackState(initialStep = -1, stepCount = 5)

        assertEquals(-1, state.safeStepIndex)
        assertEquals(-1, state.minStepIndex)
        assertEquals(4, state.maxStepIndex)
        assertFalse(state.isCompleted)
        assertFalse(state.canGoPrevious)

        state.next()
        assertEquals(0, state.safeStepIndex)
        assertTrue(state.canGoPrevious)

        state.previous()
        assertEquals(-1, state.safeStepIndex)
        assertFalse(state.canGoPrevious)
    }

    @Test
    fun previousAndNextBoundariesRespectStepLimits() {
        val state = VisualizerPlaybackState(initialStep = 0, stepCount = 3)

        assertEquals(0, state.safeStepIndex)
        state.previous() // Should remain at 0
        assertEquals(0, state.safeStepIndex)

        state.next() // Advance to 1
        assertEquals(1, state.safeStepIndex)
        assertTrue(state.canGoPrevious)

        state.next() // Advance to 2 (last step)
        assertEquals(2, state.safeStepIndex)
        assertTrue(state.isCompleted)

        state.next() // Should stay at 2
        assertEquals(2, state.safeStepIndex)
    }

    @Test
    fun resetRestoresInitialStepAndPausesPlayback() {
        val state = VisualizerPlaybackState(initialStep = -1, stepCount = 4)
        state.next()
        state.next()
        state.isPlaying = true

        assertEquals(1, state.safeStepIndex)
        assertTrue(state.isPlaying)

        state.reset()
        assertEquals(-1, state.safeStepIndex)
        assertFalse(state.isPlaying)
    }

    @Test
    fun togglePlaybackEnablesAndDisablesPlaybackUntilCompleted() {
        val state = VisualizerPlaybackState(initialStep = 0, stepCount = 2)

        assertFalse(state.isPlaying)
        state.togglePlayback()
        assertTrue(state.isPlaying)

        state.togglePlayback()
        assertFalse(state.isPlaying)

        state.next() // Move to step 1 (completed)
        assertTrue(state.isCompleted)

        state.togglePlayback() // Cannot play when completed
        assertFalse(state.isPlaying)
    }

    @Test
    fun emptyTracesAreMarkedCompletedSafely() {
        val state = VisualizerPlaybackState(initialStep = 0, stepCount = 0)

        assertTrue(state.isCompleted)
        assertFalse(state.canGoPrevious)
        assertEquals(0, state.safeStepIndex)

        state.next()
        assertEquals(0, state.safeStepIndex)

        state.togglePlayback()
        assertFalse(state.isPlaying)
    }

    @Test
    fun saverPreservesStepIndexAndKeepsPlayingTransient() {
        val state = VisualizerPlaybackState(initialStep = 0, stepCount = 5)
        state.next()
        state.next()
        state.isPlaying = true

        assertEquals(2, state.safeStepIndex)
        assertTrue(state.isPlaying)

        val saver = VisualizerPlaybackState.saver(initialStep = 0, stepCount = 5)
        val dummyScope = SaverScope { true }
        val saved = with(saver) { dummyScope.save(state) }
        assertEquals(2, saved)

        val restored = saver.restore(saved!!)
        requireNotNull(restored)

        assertEquals(2, restored.safeStepIndex)
        assertFalse(restored.isPlaying) // Must be transient and false after recreation
    }
}
