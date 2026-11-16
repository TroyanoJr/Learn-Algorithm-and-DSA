package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StackSceneLayoutTest {
    @Test
    fun sequenceLayoutsAreStableAndContainStackCellsAndLabels() {
        val sequence = StackTrace.stepsFor(StackDemonstration.FUNDAMENTALS)
        val maxDepth = sequence.maxOf { it.values.size }
        val layouts = sequence.map { StackSceneLayout.calculate(maxDepth) }
        assertEquals(1, layouts.distinct().size)
        layouts.forEach { layout ->
            val all = layout.arrayBounds.values + layout.arrayIndexBounds.values + layout.topLabelBounds + layout.bottomLabelBounds
            assertTrue(all.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
            layout.arrayBounds.values.sortedBy { it.top }.zipWithNext().forEach { (a, b) -> assertTrue(a.bottom <= b.top) }
            val topIndex = maxDepth - 1
            if (topIndex >= 0) assertEquals(layout.arrayBounds.getValue("stack_array:$topIndex").top, layout.arrayBounds.values.minOf { it.top })
        }
    }

    @Test
    fun linkedNodesAndArrowClearanceFitTheSceneAtStableMaximumDepth() {
        val steps = StackTrace.stepsFor(StackDemonstration.IMPLEMENTATIONS)
        val maxDepth = steps.maxOf { it.values.size }
        val layout = StackSceneLayout.calculate(maxDepth, includeLinked = true)
        assertEquals(1, steps.map { StackSceneLayout.calculate(maxDepth, includeLinked = true) }.distinct().size)
        val nodes = layout.linkedNodeBounds.values
        assertEquals(maxDepth, nodes.size)
        nodes.sortedBy { it.left }.zipWithNext().forEach { (a, b) ->
            assertTrue(a.right < b.left)
            assertTrue(b.left - a.right >= 7f)
        }
        assertTrue(layout.headLabelBounds.right <= nodes.first().left)
        assertTrue(nodes.last().right < layout.nullBounds.left)
        val all = nodes + layout.headLabelBounds + layout.nullBounds
        assertTrue(all.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
        layout.linkedConnectionLines.values.forEach { line ->
            assertTrue(line.start.x >= 0f && line.end.x <= layout.requiredSceneWidth)
            assertTrue(line.start.y >= 0f && line.start.y <= layout.requiredSceneHeight)
        }
    }

    @Test
    fun fitUsesBothViewportDimensionsAndCapsAtNaturalScale() {
        val layout = StackSceneLayout.calculate(6)
        assertEquals(minOf(1f, 360f / layout.requiredSceneWidth, 200f / layout.requiredSceneHeight), StackSceneLayout.initialFitScale(layout, 360f, 200f))
        assertEquals(1f, StackSceneLayout.initialFitScale(layout, 1000f, 1000f))
        val linked = StackSceneLayout.calculate(6, includeLinked = true)
        assertTrue(StackSceneLayout.initialFitScale(linked, 360f, 200f) in 0f..1f)
    }

    @Test
    fun emptyStateAndBottomLabelHaveReservedNonOverlappingBounds() {
        val layout = StackSceneLayout.calculate(maxDepth = 0, includeEmptyState = true)
        assertTrue(layout.emptyStateBounds.left >= 0f)
        assertTrue(layout.emptyStateBounds.top >= 0f)
        assertTrue(layout.emptyStateBounds.right <= layout.requiredSceneWidth)
        assertTrue(layout.emptyStateBounds.bottom <= layout.requiredSceneHeight)
        assertTrue(layout.emptyStateBounds.bottom + 6f <= layout.bottomLabelBounds.top)
        assertTrue(layout.bottomLabelBounds.bottom <= layout.requiredSceneHeight)
    }

    @Test
    fun measuredIndexAndNullLabelsExpandTheirContainingGeometry() {
        val layout = StackSceneLayout.calculate(
            maxDepth = 4,
            includeLinked = true,
            headerWidthDp = 260f,
            indexLabelWidthDp = 38f,
            nullLabelWidthDp = 74f,
            nullLabelHeightDp = 24f
        )
        assertTrue(layout.arrayIndexBounds.values.all { it.width >= 38f })
        assertTrue(layout.nullBounds.width >= 74f)
        assertTrue(layout.topLabelBounds.right < layout.requiredSceneWidth)
        assertTrue(layout.nullBounds.right < layout.requiredSceneWidth)
        assertTrue(layout.nullBounds.bottom <= layout.requiredSceneHeight)
    }
}
