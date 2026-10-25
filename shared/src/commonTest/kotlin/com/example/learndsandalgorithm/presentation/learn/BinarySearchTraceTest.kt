package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BinarySearchTraceTest {
    @Test
    fun tracePreservesInitialBoundsAndMidpoint() {
        val step = BinarySearchTrace.steps[0]

        assertEquals(11, BinarySearchTrace.target)
        assertEquals(listOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19), step.values)
        assertEquals(0, step.leftIndex)
        assertEquals(9, step.rightIndex)
        assertEquals(4, step.midIndex)
        assertEquals(0..9, step.activeRange)
        assertEquals(setOf(4), step.highlightedIndices)
        assertEquals(
            "Mid = index 4 (val 9) < 11. Eliminate indices 0–4; search 5–9.",
            step.explanation
        )
    }

    @Test
    fun traceReducesSearchRangeAfterEachComparison() {
        val first = BinarySearchTrace.steps[0]
        val second = BinarySearchTrace.steps[1]
        val third = BinarySearchTrace.steps[2]

        assertEquals(5..9, second.activeRange)
        assertEquals(5, second.leftIndex)
        assertEquals(9, second.rightIndex)
        assertEquals(7, second.midIndex)
        assertEquals(setOf(7), second.highlightedIndices)
        assertEquals(
            "Mid = index 7 (val 15) > 11. Eliminate indices 7–9; search 5–6.",
            second.explanation
        )

        assertEquals(5..6, third.activeRange)
        assertEquals(5, third.leftIndex)
        assertEquals(6, third.rightIndex)
        assertEquals(5, third.midIndex)
        assertEquals(setOf(5), third.highlightedIndices)
        assertEquals(5, third.foundIndex)
        assertEquals(11, third.values[third.foundIndex!!])
        assertEquals(
            "Mid = index 5 (val 11) == 11. Target 11 found at index 5! Binary Search complete. ✓",
            third.explanation
        )
        assertTrue(first.activeRange.contains(second.activeRange.first))
        assertTrue(second.activeRange.contains(third.activeRange.first))
    }

    @Test
    fun everyMarkerAndRangeIsWithinItsArray() {
        assertEquals(3, BinarySearchTrace.steps.size)
        BinarySearchTrace.steps.forEach { step ->
            val validIndices = step.values.indices

            assertEquals(10, step.values.size)
            assertTrue(step.values.zipWithNext().all { (left, right) -> left < right })
            assertTrue(step.activeRange.first in validIndices)
            assertTrue(step.activeRange.last in validIndices)
            assertTrue(step.leftIndex in validIndices)
            assertTrue(step.rightIndex in validIndices)
            assertTrue(step.midIndex in validIndices)
            assertEquals(step.leftIndex, step.activeRange.first)
            assertEquals(step.rightIndex, step.activeRange.last)
            assertEquals((step.leftIndex + step.rightIndex) / 2, step.midIndex)
            assertTrue(step.midIndex in step.activeRange)
            assertTrue(step.highlightedIndices.all { it in validIndices })
            assertTrue(step.foundIndex == null || step.foundIndex in validIndices)
        }
    }
}
