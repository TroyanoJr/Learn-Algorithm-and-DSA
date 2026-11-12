package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisualizerCanvasViewportTest {

    @Test
    fun testInitialState() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        assertEquals(1.0f, state.scale)
        assertEquals(0f, state.translationX)
        assertEquals(0f, state.translationY)
    }

    @Test
    fun testScaleClamping() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(400, 300)
        state.contentSize = IntSize(800, 600)

        // Zoom in beyond maxScale
        state.onTransform(zoomFactor = 5.0f, panChange = Offset.Zero, centroid = Offset(200f, 150f))
        assertEquals(3.5f, state.scale)

        // Zoom out below minScale
        state.onTransform(zoomFactor = 0.05f, panChange = Offset.Zero, centroid = Offset(200f, 150f))
        assertEquals(0.5f, state.scale)
    }

    @Test
    fun testTranslationClampingWhenContentFitsViewport() {
        val state = VisualizerCanvasState(initialScale = 1.0f)
        state.viewportSize = IntSize(400, 300)
        state.contentSize = IntSize(200, 150) // Fits inside viewport

        // Pan attempt
        state.onTransform(zoomFactor = 1.0f, panChange = Offset(100f, 100f), centroid = Offset(200f, 150f))
        // Should remain centered at 0 translation because content fits viewport
        assertEquals(0f, state.translationX)
        assertEquals(0f, state.translationY)
    }

    @Test
    fun testTranslationClampingWhenContentExceedsViewport() {
        val state = VisualizerCanvasState(initialScale = 1.0f)
        state.viewportSize = IntSize(400, 300)
        state.contentSize = IntSize(600, 500) // Content > Viewport

        // MinTx = -(600 - 400) = -200, MinTy = -(500 - 300) = -200
        // Initial y = 0 exposes top of content. Panning UP (negative y) reveals bottom up to -200
        state.onTransform(zoomFactor = 1.0f, panChange = Offset(-500f, -500f), centroid = Offset(200f, 150f))
        assertEquals(-200f, state.translationX)
        assertEquals(-200f, state.translationY)

        // Positive pan clamped to 0 (top-left aligned initially)
        state.onTransform(zoomFactor = 1.0f, panChange = Offset(500f, 500f), centroid = Offset(200f, 150f))
        assertEquals(0f, state.translationX)
        assertEquals(0f, state.translationY)
    }

    @Test
    fun testResetRestoresInitialState() {
        val state = VisualizerCanvasState(initialScale = 1.0f)
        state.viewportSize = IntSize(400, 300)
        state.contentSize = IntSize(600, 500)

        state.onTransform(zoomFactor = 2.0f, panChange = Offset(-50f, -50f), centroid = Offset(200f, 150f))
        assertTrue(state.scale != state.calculateFitScale() || state.translationX != 0f)

        state.reset()
        assertEquals(state.calculateFitScale(), state.scale)
        assertEquals(0f, state.translationX)
        assertEquals(0f, state.translationY)
    }
}
