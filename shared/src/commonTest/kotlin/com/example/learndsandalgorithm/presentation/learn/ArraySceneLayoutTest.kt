package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArraySceneLayoutTest {
    @Test
    fun resizeSceneContainsBothArraysInReadableSingleRowsWithoutOverlap() {
        val frames = ArraySceneBuilder.buildResizeFrames(ArrayTrace.resizeSteps())
        val layout = ArraySceneLayout.calculate(frames)
        val repeated = ArraySceneLayout.calculate(frames)
        assertEquals(layout, repeated)
        assertEquals(setOf("primary", "secondary"), layout.regionLayouts.keys)

        layout.regionLayouts.values.forEach { region ->
            val bounds = listOf(region.headerBounds) + region.cellBounds.values + region.indexBounds.values + region.continuationBounds.values
            assertTrue(bounds.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
            region.cellBounds.values.forEach { cell ->
                assertTrue(region.indexBounds.values.any { index -> index.left == cell.left && index.top >= cell.bottom })
            }
            region.cellBounds.values.sortedWith(compareBy<Rect2D> { it.top }.thenBy { it.left }).zipWithNext().forEach { (a, b) ->
                assertTrue(a.right <= b.left || a.bottom <= b.top || b.right <= a.left || b.bottom <= a.top)
            }
        }
        val old = layout.regionLayouts.getValue("primary")
        val new = layout.regionLayouts.getValue("secondary")
        assertTrue(old.cellBounds.values.maxOf { it.bottom } < new.headerBounds.top)
        assertTrue(old.cellBounds.values.all { it.height >= 44f })
        assertTrue(new.cellBounds.values.all { it.height >= 44f })
        assertEquals(emptySet(), old.continuationBounds.keys)
        assertEquals(emptySet(), new.continuationBounds.keys)
        assertEquals(5, old.cellBounds.size)
        assertEquals(10, new.cellBounds.size)
        assertTrue(layout.requiredSceneWidth > 0f && layout.requiredSceneHeight > 0f)
    }

    @Test
    fun traversalAndEditSequencesHaveStableContainedCellAndIndexGeometry() {
        val scenarios = listOf(
            ArraySceneBuilder.buildTraversalFrames(ArrayTrace.traversalSteps()),
            ArraySceneBuilder.buildInsertDeleteFrames(ArrayTrace.insertDeleteSteps("INSERT"), "INSERT"),
            ArraySceneBuilder.buildInsertDeleteFrames(ArrayTrace.insertDeleteSteps("DELETE"), "DELETE")
        )
        scenarios.forEach { frames ->
            val layout = ArraySceneLayout.calculate(frames)
            assertEquals(layout, ArraySceneLayout.calculate(frames))
            val region = layout.regionLayouts.getValue("array")
            val all = region.cellBounds.values + region.indexBounds.values + region.continuationBounds.values + region.headerBounds
            assertTrue(all.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
            assertTrue(region.cellBounds.keys.all { it in region.indexBounds.keys })
            assertEquals(0, region.continuationBounds.size)
        }
    }

    @Test
    fun initialFitUsesBothNaturalSceneDimensionsAtShortLandscapeSizes() {
        val frames = ArraySceneBuilder.buildResizeFrames(ArrayTrace.resizeSteps())
        val layout = ArraySceneLayout.calculate(frames)
        listOf(640f to 320f, 740f to 360f, 840f to 393f).forEach { (width, height) ->
            val scale = ArraySceneLayout.initialFitScale(layout, width * 0.55f, height * 0.62f)
            assertTrue(scale in 0f..1f)
            assertTrue(layout.requiredSceneWidth * scale <= width * 0.55f + 0.01f)
            assertTrue(layout.requiredSceneHeight * scale <= height * 0.62f + 0.01f)
        }
        assertEquals(
            minOf(1f, 300f / layout.requiredSceneWidth, 180f / layout.requiredSceneHeight),
            ArraySceneLayout.initialFitScale(layout, 300f, 180f)
        )
    }

    @Test
    fun largerMeasuredTextExpandsBoundsRatherThanClipping() {
        val frames = ArraySceneBuilder.buildResizeFrames(ArrayTrace.resizeSteps())
        val normal = ArraySceneLayout.calculate(frames)
        val scaled = ArraySceneLayout.calculate(
            frames,
            valueTextWidthDp = 52f,
            valueTextHeightDp = 31f,
            indexTextWidthDp = 30f,
            indexTextHeightDp = 22f,
            headerTextWidthDp = 360f,
            headerTextHeightDp = 28f,
            continuationTextWidthDp = 180f,
            continuationTextHeightDp = 22f
        )
        assertTrue(scaled.requiredSceneWidth >= normal.requiredSceneWidth)
        assertTrue(scaled.requiredSceneHeight >= normal.requiredSceneHeight)
        scaled.regionLayouts.values.forEach { region ->
            assertTrue((region.cellBounds.values + region.indexBounds.values + region.continuationBounds.values + region.headerBounds).all {
                it.left >= 0f && it.top >= 0f && it.right <= scaled.requiredSceneWidth && it.bottom <= scaled.requiredSceneHeight
            })
        }
    }
}
