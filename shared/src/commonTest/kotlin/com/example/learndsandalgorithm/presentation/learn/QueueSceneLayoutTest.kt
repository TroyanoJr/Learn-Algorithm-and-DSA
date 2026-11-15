package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueueSceneLayoutTest {
    @Test
    fun allQueueSnapshotsShareCapacityGeometryAndStayContained() {
        QueueDemonstration.entries.forEach { demo ->
            val steps = QueueTrace.stepsFor(demo)
            val showLinked = steps.first().representation == QueueRepresentation.ARRAY_AND_LINKED
            val maxItems = steps.maxOf { it.values.size }
            val layouts = steps.map { QueueSceneLayout.calculate(it.capacity, maxItems, showLinked) }
            assertEquals(1, layouts.distinct().size)
            val layout = layouts.first()
            val rects = layout.arraySlotBounds.values + layout.arrayIndexBounds.values + layout.arrayValueBounds.values + layout.frontRearLabelBounds.values +
                layout.linkedHeadingBounds + layout.linkedNodeBounds.values + layout.linkedFrontBounds + layout.linkedNullBounds
            assertTrue(rects.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
            layout.arraySlotBounds.values.sortedBy { it.left }.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
            layout.linkedNodeBounds.values.sortedBy { it.left }.zipWithNext().forEach { (a, b) -> assertTrue(a.right < b.left) }
            if (showLinked && layout.linkedNodeBounds.isNotEmpty()) {
                assertTrue(layout.linkedHeadingBounds.bottom < layout.linkedNodeBounds.values.minOf { it.top })
            }
            steps.forEach { step ->
                val front = layout.frontRearLabelBounds.getValue(step.frontIndex)
                val rear = layout.frontRearLabelBounds.getValue(step.nextRearIndex)
                assertEquals(layout.arraySlotBounds.getValue(step.frontIndex).left, front.left)
                assertEquals(layout.arraySlotBounds.getValue(step.nextRearIndex).left, rear.left)
            }
        }
    }

    @Test
    fun nextArrowSegmentsHaveClearanceAndRemainWithinBounds() {
        val layout = QueueSceneLayout.calculate(capacity = 5, maxItems = 5, showLinked = true)
        layout.linkedNodeBounds.values.sortedBy { it.left }.zipWithNext().forEach { (a, b) ->
            assertTrue(b.left - a.right >= 7f)
        }
        layout.linkedConnectionLines.values.forEach { line ->
            assertTrue(line.start.x >= 0f && line.end.x <= layout.requiredSceneWidth)
            assertTrue(line.start.y >= 0f && line.start.y <= layout.requiredSceneHeight)
        }
        assertTrue(layout.linkedNullBounds.left > layout.linkedNodeBounds.values.maxOf { it.right })
    }

    @Test
    fun fitIncludesCompleteSceneAndUsesBothAxes() {
        val layout = QueueSceneLayout.calculate(5, 5, true)
        assertEquals(minOf(1f, 360f / layout.requiredSceneWidth, 200f / layout.requiredSceneHeight), QueueSceneLayout.initialFitScale(layout, 360f, 200f))
        assertEquals(1f, QueueSceneLayout.initialFitScale(layout, 1000f, 1000f))
        val circular = QueueSceneLayout.calculate(4, 4, false)
        assertTrue(QueueSceneLayout.initialFitScale(circular, 320f, 180f) in 0f..1f)
    }
}
