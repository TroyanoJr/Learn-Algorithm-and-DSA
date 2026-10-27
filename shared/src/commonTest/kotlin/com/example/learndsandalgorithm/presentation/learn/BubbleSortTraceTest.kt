package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BubbleSortTraceTest {
    @Test
    fun traceHasExpectedComparisonCountAndAdjacentSequence() {
        val comparisons = BubbleSortTrace.steps.filterNot { it.isCompletion }

        assertEquals(17, comparisons.size)
        assertEquals((0..8).toList() + (0..7).toList(), comparisons.map { it.highlightedIndices.first() })
        assertEquals(
            listOf(
                "Pass 1: Compare 1 & 2 → no swap",
                "Compare 2 & 3 → no swap",
                "Compare 3 & 4 → no swap",
                "Compare 4 & 5 → no swap",
                "Compare 5 & 6 → no swap",
                "Compare 6 & 7 → no swap",
                "Compare 7 & 9 → no swap",
                "Compare 9 & 8 → swap",
                "Compare 9 & 10 → no swap",
                "Pass 2: Compare 1 & 2 → no swap",
                "Compare 2 & 3 → no swap",
                "Compare 3 & 4 → no swap",
                "Compare 4 & 5 → no swap",
                "Compare 5 & 6 → no swap",
                "Compare 6 & 7 → no swap",
                "Compare 7 & 8 → no swap",
                "Compare 8 & 9 → no swap"
            ),
            comparisons.map { it.message }
        )
        comparisons.forEach { step ->
            assertEquals(2, step.highlightedIndices.size)
            assertEquals(step.highlightedIndices.first() + 1, step.highlightedIndices.last())
        }
    }

    @Test
    fun swapAndSortedSuffixAreRecordedAccurately() {
        val comparisons = BubbleSortTrace.steps.filterNot { it.isCompletion }
        val swap = comparisons[7]

        assertEquals(listOf(7, 8), swap.highlightedIndices)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), swap.array)
        assertTrue(swap.message.contains("Compare 9 & 8 → swap"))
        assertTrue(comparisons.take(8).all { it.sortedIndices.isEmpty() })
        assertEquals(listOf(9), comparisons[8].sortedIndices)
        assertTrue(comparisons.slice(9..15).all { it.sortedIndices == listOf(9) })
        assertEquals(listOf(8, 9), comparisons[16].sortedIndices)
    }

    @Test
    fun finalComparisonIsVisibleBeforeSeparateCompletionState() {
        val comparison = BubbleSortTrace.steps[BubbleSortTrace.steps.lastIndex - 1]
        val completion = BubbleSortTrace.steps.last()

        assertFalse(comparison.isCompletion)
        assertEquals(listOf(7, 8), comparison.highlightedIndices)
        assertEquals(listOf(8, 9), comparison.sortedIndices)
        assertEquals("Compare 8 & 9 → no swap", comparison.message)

        assertTrue(completion.isCompletion)
        assertTrue(completion.highlightedIndices.isEmpty())
        assertEquals((0..9).toList(), completion.sortedIndices)
        assertEquals(comparison.array, completion.array)
    }

    @Test
    fun sortedInputExitsAfterOnePass() {
        val steps = BubbleSortTrace.generate(listOf(1, 2, 3, 4))
        val comparisons = steps.filterNot { it.isCompletion }

        assertEquals(3, comparisons.size)
        assertEquals(listOf(0, 1, 2), comparisons.map { it.highlightedIndices.first() })
        assertEquals(listOf(3), comparisons.last().sortedIndices)
        assertTrue(steps.last().isCompletion)
        assertEquals((0..3).toList(), steps.last().sortedIndices)
    }
}
