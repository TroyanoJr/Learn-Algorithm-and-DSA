package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MergeSortFundamentalsSceneLayoutTest {

    @Test
    fun defaultLayoutIsCompactDeterministicAndStableWhenFramesAreReversed() {
        val frames = build(listOf(1, 4, 7), listOf(2, 5, 8))
        val layout = MergeSortFundamentalsSceneLayout.calculate(frames)
        val reversed = MergeSortFundamentalsSceneLayout.calculate(frames.reversed())

        assertEquals(layout, reversed)
        assertEquals(371.4f, layout.requiredSceneWidth)
        assertEquals(268f, layout.requiredSceneHeight)
        assertTrue(layout.requiredSceneWidth < 400f, "Default scene should stay compact: ${layout.requiredSceneWidth}dp")
        assertTrue(layout.requiredSceneHeight < 280f, "Default scene should stay compact: ${layout.requiredSceneHeight}dp")
        assertEquals(6, layout.outputCellBounds.size)
        assertEquals(3, layout.leftCellBounds.size)
        assertEquals(3, layout.rightCellBounds.size)
        assertContained(layout)
        assertNoCellOverlaps(layout.leftCellBounds.values.toList())
        assertNoCellOverlaps(layout.rightCellBounds.values.toList())
        assertNoCellOverlaps(layout.outputCellBounds.values.toList())
        assertNoMarkerOverlaps(layout.leftMarkerBounds.values.toList())
        assertNoMarkerOverlaps(layout.rightMarkerBounds.values.toList())
        assertNoMarkerOverlaps(layout.outputMarkerBounds.values.toList())
    }

    @Test
    fun transferConnectorsTerminateAtTheSelectedSourceAndOutputSlot() {
        val frames = build(listOf(-5, 0, 12), listOf(-3, 7))
        val layout = MergeSortFundamentalsSceneLayout.calculate(frames)
        frames.forEach { frame ->
            val step = frame.step
            val connector = layout.transferRoutes[frame.snapshotIndex]
            if (step.source == null) {
                assertEquals(null, connector)
            } else {
                val route = assertNotNull(connector)
                val sourceCell = when (step.source) {
                    MergeSource.LEFT -> layout.leftCellBounds[step.sourceIndexBeforeTransfer]
                    MergeSource.RIGHT -> layout.rightCellBounds[step.sourceIndexBeforeTransfer]
                }
                val destinationCell = layout.outputCellBounds[step.destinationIndex]
                val sourceBounds = assertNotNull(sourceCell)
                val destinationBounds = assertNotNull(destinationCell)
                assertEquals(sourceBounds.center.x, route.first().x)
                assertEquals(sourceBounds.bottom, route.first().y)
                assertEquals(destinationBounds.center.x, route.last().x)
                assertEquals(destinationBounds.top, route.last().y)
                assertTrue(route.first().y < route.last().y)
                assertTrue(route[2].x > layout.outputLabelBounds.right)
                assertTrue(route[1].y < layout.outputLabelBounds.top)
                assertTrue(route[3].y > layout.outputMarkerBounds.values.maxOf { it.bottom })
            }
        }
    }

    @Test
    fun emptySingletonUnequalAndWideIntegerInputsStayInsideNaturalBounds() {
        val cases = listOf(
            emptyList<Int>() to emptyList(),
            emptyList<Int>() to listOf(Int.MIN_VALUE),
            listOf(Int.MIN_VALUE) to emptyList(),
            listOf(Int.MIN_VALUE, -1, 0, Int.MAX_VALUE) to listOf(-2),
            listOf(-9, -2) to listOf(-8, 0, 1, 7, Int.MAX_VALUE)
        )
        cases.forEach { (left, right) ->
            val frames = build(left, right)
            val layout = MergeSortFundamentalsSceneLayout.calculate(frames)
            assertEquals(left.size, layout.leftCellBounds.size)
            assertEquals(right.size, layout.rightCellBounds.size)
            assertEquals(left.size + right.size, layout.outputCellBounds.size)
            assertContained(layout)
            assertNoCellOverlaps(layout.leftCellBounds.values.toList())
            assertNoCellOverlaps(layout.rightCellBounds.values.toList())
            assertNoCellOverlaps(layout.outputCellBounds.values.toList())
        }
    }

    @Test
    fun rowsAndLabelsDoNotOverlapAndEveryRenderedElementIsContained() {
        val frames = build(listOf(Int.MIN_VALUE, -1, 3), listOf(-2, 0, Int.MAX_VALUE))
        val layout = MergeSortFundamentalsSceneLayout.calculate(frames)
        val horizontalRows = listOf(
            layout.leftCellBounds.values.toList(),
            layout.rightCellBounds.values.toList(),
            layout.outputCellBounds.values.toList()
        )
        horizontalRows.forEach(::assertNoCellOverlaps)
        assertTrue(layout.leftLabelBounds.right <= layout.rightLabelBounds.left)
        assertTrue(layout.leftLabelBounds.bottom <= layout.leftMarkerBounds.values.first().top)
        assertTrue(layout.rightLabelBounds.bottom <= layout.rightMarkerBounds.values.first().top)
        assertTrue(layout.leftCellBounds.values.maxOfOrNull { it.bottom }!! < layout.outputLabelBounds.top)
        assertTrue(layout.outputLabelBounds.bottom <= layout.outputMarkerBounds.values.first().top)
        assertContained(layout)
    }

    private fun build(left: List<Int>, right: List<Int>) =
        MergeSortFundamentalsSceneBuilder.build(MergeSortFundamentalsTrace.stepsForMerge(left, right))

    private fun assertContained(layout: MergeSortFundamentalsSceneLayoutResult) {
        val rectangles = listOf(
            layout.headerBounds,
            layout.comparisonBounds,
            layout.leftLabelBounds,
            layout.rightLabelBounds,
            layout.outputLabelBounds
        ) + layout.leftMarkerBounds.values + layout.rightMarkerBounds.values + layout.outputMarkerBounds.values +
            layout.leftCellBounds.values + layout.rightCellBounds.values + layout.outputCellBounds.values +
            layout.leftIndexBounds.values + layout.rightIndexBounds.values + layout.outputIndexBounds.values +
            listOfNotNull(layout.emptyLeftBounds, layout.emptyRightBounds, layout.emptyOutputBounds)
        rectangles.forEach { bounds ->
            assertTrue(bounds.left >= 0f && bounds.top >= 0f, "Negative bounds: $bounds")
            assertTrue(bounds.right <= layout.requiredSceneWidth + 0.001f, "Horizontal overflow: $bounds")
            assertTrue(bounds.bottom <= layout.requiredSceneHeight + 0.001f, "Vertical overflow: $bounds")
        }
        layout.transferRoutes.values.forEach { route ->
            route.forEach { point ->
                assertTrue(point.x in 0f..layout.requiredSceneWidth)
                assertTrue(point.y in 0f..layout.requiredSceneHeight)
            }
        }
    }

    private fun assertNoCellOverlaps(cells: List<Rect2D>) {
        cells.forEachIndexed { index, first ->
            cells.drop(index + 1).forEach { second ->
                assertTrue(first.right <= second.left || second.right <= first.left || first.bottom <= second.top || second.bottom <= first.top,
                    "Cell bounds overlap: $first and $second")
            }
        }
    }

    private fun assertNoMarkerOverlaps(markers: List<Rect2D>) {
        markers.forEachIndexed { index, first ->
            markers.drop(index + 1).forEach { second ->
                assertTrue(first.right <= second.left || second.right <= first.left, "Marker bounds overlap: $first and $second")
            }
        }
    }
}
