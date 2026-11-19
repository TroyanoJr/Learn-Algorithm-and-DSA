package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchSortSceneLayoutTest {
    @Test
    fun comparisonRowsReserveWrappedTextBoundsAndFitScene() {
        val count = 3
        val height = ComparisonCanvasLayout.sceneHeight(count)
        repeat(count) { index ->
            val row = ComparisonCanvasLayout.rowBounds(index)
            val strategy = ComparisonCanvasLayout.strategyBounds(index)
            val complexity = ComparisonCanvasLayout.complexityBounds(index)
            assertTrue(strategy.left >= row.left && strategy.right <= row.right)
            assertTrue(complexity.left >= row.left && complexity.right <= row.right)
            assertTrue(strategy.bottom <= complexity.top)
            assertTrue(complexity.bottom <= row.bottom)
        }
        assertTrue(ComparisonCanvasLayout.rowBounds(count - 1).bottom <= height)
    }
    @Test
    fun searchLayoutIsStableContainedAndNonOverlapping() {
        val frames = SearchSortSceneBuilder.buildBinarySearchFrames(BinarySearchTrace.steps, BinarySearchTrace.values, BinarySearchTrace.target)
        val layout = SearchSortSceneLayout.calculate(frames)
        assertEquals(layout, SearchSortSceneLayout.calculate(frames))
        assertEquals(BinarySearchTrace.values.size, layout.cellBounds.size)
        assertContained(layout, layout.cellBounds.values + layout.indexBounds.values)
        assertNoOverlap(layout.cellBounds.values.toList())
        layout.cellBounds.forEach { (index, cell) ->
            val label = layout.indexBounds.getValue(index)
            assertTrue(label.top >= cell.bottom)
        }
    }

    @Test
    fun binaryPointerPositionsUseCellCentersAndCombineCoincidentMarkers() {
        val frames = SearchSortSceneBuilder.buildBinarySearchFrames(BinarySearchTrace.steps, BinarySearchTrace.values, BinarySearchTrace.target)
        val layout = SearchSortSceneLayout.calculate(frames)
        val firstPointers = SearchSortSceneLayout.pointerBounds(frames[1], layout)
        assertEquals(setOf("L", "M", "R"), firstPointers.keys)
        assertEquals(layout.cellBounds.getValue(0).center.x, firstPointers.getValue("L").center.x)
        assertEquals(layout.cellBounds.getValue(4).center.x, firstPointers.getValue("M").center.x)
        assertEquals(layout.cellBounds.getValue(9).center.x, firstPointers.getValue("R").center.x)

        val coincident = frames.last().copy(meta = frames.last().meta.copy(leftIndex = 5, midIndex = 5, rightIndex = 5))
        val combined = SearchSortSceneLayout.pointerBounds(coincident, layout)
        assertEquals(setOf("L,M,R"), combined.keys)
        assertEquals(layout.cellBounds.getValue(5).center.x, combined.getValue("L,M,R").center.x)
    }

    @Test
    fun barLayoutReservesStableTracksForEveryStepWithoutOverlap() {
        val sequences = listOf(
            SearchSortSceneBuilder.buildBubbleSortFrames(BubbleSortTrace.steps),
            SearchSortSceneBuilder.buildSelectionSortFrames(SelectionSortTrace.steps),
            SearchSortSceneBuilder.buildInsertionSortFrames(InsertionSortTrace.steps)
        )
        sequences.forEach { frames ->
            val layout = SearchSortSceneLayout.calculate(frames)
            assertEquals(layout, SearchSortSceneLayout.calculate(frames))
            assertEquals(frames.first().values.size, layout.barTrackBounds.size)
            assertContained(layout, layout.valueBounds.values + layout.markerBounds.values + layout.barTrackBounds.values + layout.indexBounds.values)
            assertNoOverlap(layout.barTrackBounds.values.toList())
            layout.barTrackBounds.forEach { (index, track) ->
                assertTrue(layout.valueBounds.getValue(index).bottom <= track.top)
                assertTrue(layout.indexBounds.getValue(index).top >= track.bottom)
            }
        }
    }

    @Test
    fun markerBoundsReserveSeparateBandAndCombineCoincidentInsertionLabels() {
        val frames = SearchSortSceneBuilder.buildInsertionSortFrames(InsertionSortTrace.steps)
        val layout = SearchSortSceneLayout.calculate(frames)
        frames.forEach { frame ->
            val bounds = SearchSortSceneLayout.markerBounds(frame, layout)
            assertContained(layout, bounds.values)
            bounds.forEach { (index, marker) ->
                assertTrue(marker.bottom <= layout.barTrackBounds.getValue(index).top)
                assertTrue(marker.top >= layout.valueBounds.getValue(index).bottom)
            }
        }
        val coincident = frames.first().copy(meta = frames.first().meta.copy(passIndex = 1, key = 3, comparisonTargetIndex = 0, holeIndex = 1))
        assertEquals(setOf(0, 1), SearchSortSceneLayout.markerBounds(coincident, layout).keys)
        assertEquals(layout.markerBounds.getValue(1), SearchSortSceneLayout.markerBounds(coincident, layout).getValue(1))
    }

    @Test
    fun initialFitContainsCompleteSceneOnCompactLandscapeAndEmptyInputIsSafe() {
        val frames = SearchSortSceneBuilder.buildInsertionSortFrames(InsertionSortTrace.steps)
        val layout = SearchSortSceneLayout.calculate(frames)
        listOf(640f to 320f, 740f to 360f, 840f to 393f).forEach { (width, height) ->
            val viewportWidth = width * 0.6f
            val viewportHeight = height * 0.62f
            val scale = SearchSortSceneLayout.initialFitScale(layout, viewportWidth, viewportHeight)
            assertTrue(layout.requiredSceneWidth * scale <= viewportWidth + 0.01f)
            assertTrue(layout.requiredSceneHeight * scale <= viewportHeight + 0.01f)
        }

        val emptyFrames = SearchSortSceneBuilder.buildSelectionSortFrames(SelectionSortTrace.generate(emptyList()))
        val emptyLayout = SearchSortSceneLayout.calculate(emptyFrames)
        assertTrue(emptyLayout.requiredSceneWidth > 0f)
        assertTrue(emptyLayout.requiredSceneHeight > 0f)
        assertTrue(emptyLayout.barTrackBounds.isEmpty())
    }

    private fun assertContained(layout: SearchSortSceneLayoutResult, bounds: Collection<Rect2D>) {
        assertTrue(bounds.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
    }

    private fun assertNoOverlap(bounds: List<Rect2D>) {
        bounds.indices.forEach { first ->
            for (second in first + 1 until bounds.size) {
                val a = bounds[first]
                val b = bounds[second]
                assertTrue(a.right <= b.left || b.right <= a.left || a.bottom <= b.top || b.bottom <= a.top)
            }
        }
    }
}
