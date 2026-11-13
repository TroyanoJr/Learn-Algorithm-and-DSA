package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        state.contentSize = IntSize(400, 300) // Content matches viewport -> initial scale 1.0f

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
        state.contentSize = IntSize(600, 500) // Content > Viewport (auto-fit scales to 0.6f)

        // Zoom in so content exceeds viewport at scale > 1.0f
        state.onTransform(zoomFactor = 2.0f, panChange = Offset.Zero, centroid = Offset(200f, 150f))

        // Test clamping with scaled content > viewport
        state.onTransform(zoomFactor = 1.0f, panChange = Offset(-500f, -500f), centroid = Offset(200f, 150f))
        assertEquals(-260.0f, state.translationX, 0.001f)

        // Positive pan clamped to maxTx (60.0f)
        state.onTransform(zoomFactor = 1.0f, panChange = Offset(1000f, 1000f), centroid = Offset(200f, 150f))
        assertEquals(60.0f, state.translationX, 0.001f)
    }

    @Test
    fun testResetRestoresInitialState() {
        val state = VisualizerCanvasState(initialScale = 1.0f)
        state.viewportSize = IntSize(400, 300)
        state.contentSize = IntSize(400, 300)

        state.onTransform(zoomFactor = 2.0f, panChange = Offset(-50f, -50f), centroid = Offset(200f, 150f))
        assertTrue(state.scale != state.calculateFitScale() || state.translationX != 0f)

        state.reset()
        assertEquals(state.calculateFitScale(), state.scale)
        assertEquals(0f, state.translationX)
        assertEquals(0f, state.translationY)
    }

    @Test
    fun testInitialFitBelowMinScale() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(200, 200)
        state.contentSize = IntSize(1000, 1000)

        // Expected fitScale = min(200/1000, 200/1000, 1.0) = 0.2f
        assertEquals(0.2f, state.scale, 0.001f)
    }

    @Test
    fun testRecalculateFitWhenContentChangesBeforeInteraction() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(400, 400)
        state.contentSize = IntSize(400, 400)
        assertEquals(1.0f, state.scale, 0.001f)

        // Change content dimensions before manual interaction -> scale should recalculate
        state.contentSize = IntSize(800, 800)
        assertEquals(0.5f, state.scale, 0.001f)
    }

    @Test
    fun testRecalculateFitWhenViewportChangesBeforeInteraction() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(400, 400)
        state.contentSize = IntSize(800, 800)
        assertEquals(0.5f, state.scale, 0.001f)

        // Reduce viewport dimensions before manual interaction -> scale should recalculate
        state.viewportSize = IntSize(200, 200)
        assertEquals(0.25f, state.scale, 0.001f)
    }

    @Test
    fun testPreserveManualZoomWhenContentChangesAfterInteraction() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(400, 400)
        state.contentSize = IntSize(800, 800)
        assertEquals(0.5f, state.scale, 0.001f)

        // Manual user interaction
        state.onTransform(zoomFactor = 2.0f, panChange = Offset.Zero, centroid = Offset(200f, 200f))
        val manScale = state.scale
        assertTrue(manScale > 0.5f)

        // Change content dimensions after manual interaction -> scale must be preserved
        state.contentSize = IntSize(1000, 1000)
        assertEquals(manScale, state.scale, 0.001f)
    }

    @Test
    fun testResetRestoresFitScaleBelowMinimum() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(200, 200)
        state.contentSize = IntSize(1000, 1000)
        assertEquals(0.2f, state.scale, 0.001f)

        // Zoom in manually
        state.onTransform(zoomFactor = 3.0f, panChange = Offset.Zero, centroid = Offset(100f, 100f))
        assertTrue(state.scale > 0.2f)

        // Reset -> should restore automatic fit scale 0.2f (below minScale 0.5f)
        state.reset()
        assertEquals(0.2f, state.scale, 0.001f)
        assertFalse(state.hasUserInteracted)
    }

    @Test
    fun testPanDoesNotIncreaseScaleBelowMinScale() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(200, 200)
        state.contentSize = IntSize(1000, 1000)
        assertEquals(0.2f, state.scale, 0.001f)

        // Execute a pan gesture with zoomFactor = 1.0f
        state.onTransform(zoomFactor = 1.0f, panChange = Offset(10f, 10f), centroid = Offset(100f, 100f))
        // Assert scale remains 0.2f (not forced to minScale 0.5f)
        assertEquals(0.2f, state.scale, 0.001f)
    }

    @Test
    fun testManualZoomCanReturnToAutoFitBelowMinScale() {
        val state = VisualizerCanvasState(initialScale = 1.0f, minScale = 0.5f, maxScale = 3.5f)
        state.viewportSize = IntSize(200, 200)
        state.contentSize = IntSize(1000, 1000)
        assertEquals(0.2f, state.scale, 0.001f)

        // Zoom in above 0.2f
        state.onTransform(zoomFactor = 2.0f, panChange = Offset.Zero, centroid = Offset(100f, 100f))
        assertEquals(0.4f, state.scale, 0.001f)

        // Zoom out sufficiently
        state.onTransform(zoomFactor = 0.1f, panChange = Offset.Zero, centroid = Offset(100f, 100f))
        // Assert scale returns to autoFitScale 0.2f (not clamped at minScale 0.5f)
        assertEquals(0.2f, state.scale, 0.001f)
    }
}
