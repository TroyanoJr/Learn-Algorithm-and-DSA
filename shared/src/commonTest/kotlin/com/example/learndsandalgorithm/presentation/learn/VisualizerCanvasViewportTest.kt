package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VisualizerCanvasViewportTest {

    @Test
    fun activityTimelineAutoFitsEverySnapshotAtLargestProportionalScale() {
        val viewports = listOf(360 to 224, 460 to 320, 400 to 260)
        val snapshots = ActivitySelectionTrace.createTrace()

        viewports.forEach { (width, height) ->
            var expectedScale: Float? = null
            snapshots.forEach { snapshot ->
                val scene = ActivitySelectionSceneBuilder.build(snapshot, width.toFloat(), height.toFloat())
                val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
                state.updateViewportSize(IntSize(width, height))
                state.updateContentSize(IntSize(scene.width.toInt(), scene.height.toInt()))
                val expected = minOf(width / scene.width, height / scene.height, 1f)

                assertEquals(expected, state.scale, 0.001f)
                assertEquals(0f, state.translationX)
                assertEquals(0f, state.translationY)
                assertTrue(scene.width * state.scale <= width + 0.001f)
                assertTrue(scene.height * state.scale <= height + 0.001f)
                if (expectedScale == null) expectedScale = state.scale
                assertEquals(expectedScale!!, state.scale, 0.001f)
            }
        }

        val short = ActivitySelectionSceneBuilder.build(snapshots.first(), 360f, 224f)
        assertEquals(215f, short.height)
        assertEquals(1f, minOf(360f / short.width, 224f / short.height, 1f))

        val zoomScene = ActivitySelectionSceneBuilder.build(snapshots.first(), 400f, 260f)
        val zoomState = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
        zoomState.updateViewportSize(IntSize(400, 260))
        zoomState.updateContentSize(IntSize(zoomScene.width.toInt(), zoomScene.height.toInt()))
        val fitScale = minOf(400f / zoomScene.width, 260f / zoomScene.height, 1f)
        assertEquals(fitScale, zoomState.scale, 0.001f)
        zoomState.onTransform(2f, Offset(0f, -1000f), Offset(200f, 130f))
        assertTrue(zoomState.scale > fitScale)
        assertTrue(zoomState.translationY < 0f)
        zoomState.reset()
        assertEquals(fitScale, zoomState.scale, 0.001f)
        assertEquals(0f, zoomState.translationX)
        assertEquals(0f, zoomState.translationY)
    }

    @Test
    fun naturalScaleRemainsStableAcrossMeasurementUpdatesAndDefaultStillFits() {
        val scene = ActivitySelectionSceneBuilder.build(ActivitySelectionTrace.createTrace().first(), 460f, 320f)
        assertEquals(314f, scene.height)
        val natural = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.NATURAL)
        natural.updateViewportSize(IntSize(460, 320))
        natural.updateContentSize(IntSize(460, 314))
        assertEquals(1f, natural.scale)
        assertEquals(0f, natural.translationY)
        natural.updateViewportSize(IntSize(360, 224))
        natural.updateContentSize(IntSize(360, 314))
        assertEquals(1f, natural.scale)
        natural.onTransform(1f, Offset(0f, -50f), Offset(180f, 112f))
        assertEquals(-50f, natural.translationY)
        natural.updateContentSize(IntSize(360, 300))
        assertEquals(1f, natural.scale)
        assertEquals(-50f, natural.translationY)
        natural.updateViewportSize(IntSize(460, 320))
        assertEquals(0f, natural.translationY)

        val default = VisualizerCanvasState()
        default.updateViewportSize(IntSize(360, 224))
        default.updateContentSize(IntSize(360, 314))
        assertEquals(224f / 314f, default.scale, 0.001f)
        default.onTransform(2f, Offset.Zero, Offset(180f, 112f))
        default.reset()
        assertEquals(224f / 314f, default.scale, 0.001f)
    }

    @Test
    fun centerPlacementRemainsCenteredAndTopUsesInsetAtScaleOneAndFitScale() {
        val center = VisualizerCanvasState()
        center.viewportSize = IntSize(400, 300)
        center.contentSize = IntSize(200, 150)
        assertEquals(75f, center.initialY)

        val top = VisualizerCanvasState(verticalAlignment = VisualizerCanvasVerticalAlignment.TOP, topInsetPx = 12f)
        top.viewportSize = IntSize(400, 300)
        top.contentSize = IntSize(200, 150)
        assertEquals(12f, top.initialY)

        val fit = VisualizerCanvasState(verticalAlignment = VisualizerCanvasVerticalAlignment.TOP, topInsetPx = 12f)
        fit.viewportSize = IntSize(400, 400)
        fit.contentSize = IntSize(600, 500)
        val visualTop = fit.initialY - (fit.contentSize.height * fit.scale - fit.contentSize.height) / 2f
        assertEquals(12f, visualTop, 0.001f)
        assertTrue(fit.contentSize.height * fit.scale + visualTop <= fit.viewportSize.height)
    }

    @Test
    fun topResetAndZoomKeepGestureCentroidAndClampPan() {
        val state = VisualizerCanvasState(verticalAlignment = VisualizerCanvasVerticalAlignment.TOP, topInsetPx = 12f)
        state.viewportSize = IntSize(400, 400)
        state.contentSize = IntSize(600, 500)
        val centroid = Offset(200f, 120f)
        val beforeOrigin = state.initialY - (state.contentSize.height * state.scale - state.contentSize.height) / 2f + state.translationY
        val contentPoint = (centroid.y - beforeOrigin) / state.scale
        state.onTransform(2f, Offset.Zero, centroid)
        val afterOrigin = state.initialY - (state.contentSize.height * state.scale - state.contentSize.height) / 2f + state.translationY
        assertEquals(contentPoint, (centroid.y - afterOrigin) / state.scale, 0.01f)
        state.onTransform(1f, Offset(0f, -5000f), centroid)
        assertTrue(state.translationY >= -1000f)
        state.reset()
        val resetTop = state.initialY - (state.contentSize.height * state.scale - state.contentSize.height) / 2f
        assertEquals(12f, resetTop, 0.001f)
    }

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
