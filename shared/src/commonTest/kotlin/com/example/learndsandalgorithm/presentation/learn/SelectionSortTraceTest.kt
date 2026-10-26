package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionSortTraceTest {
    @Test
    fun defaultTraceSortsExistingExampleAndCompletesSeparately() {
        val steps = SelectionSortTrace.steps
        val completion = steps.last()

        assertEquals(SelectionSortTrace.initialValues.sorted(), completion.array)
        assertEquals(SelectionSortEvent.COMPLETE, completion.event)
        assertEquals(completion.array.indices.toList(), completion.sortedIndices)
        assertTrue(steps.dropLast(1).all { it.event != SelectionSortEvent.COMPLETE })
        steps.forEach { assertEquals(SelectionSortTrace.initialValues.size, it.array.size) }
    }

    @Test
    fun comparisonsTrackCurrentMinimumAndSwapsOnlyWhenNeeded() {
        val steps = SelectionSortTrace.generate(listOf(4, 2, 3))

        assertEquals(
            listOf(
                SelectionSortEvent.COMPARISON,
                SelectionSortEvent.COMPARISON,
                SelectionSortEvent.SWAP,
                SelectionSortEvent.COMPARISON,
                SelectionSortEvent.SWAP,
                SelectionSortEvent.COMPLETE
            ),
            steps.map { it.event }
        )
        assertEquals(listOf(0, 1), steps[0].highlightedIndices)
        assertEquals(1, steps[0].minimumIndex)
        assertEquals(listOf(1, 2), steps[1].highlightedIndices)
        assertEquals(1, steps[1].minimumIndex)
        assertEquals(listOf(2, 4, 3), steps[2].array)
        assertEquals(listOf(0), steps[2].sortedIndices)
        assertEquals(2, steps[3].comparisonTargetIndex)
        assertEquals(2, steps[3].minimumIndex)
        assertEquals(listOf(2, 3, 4), steps[4].array)
        assertEquals(listOf(0, 1), steps[4].sortedIndices)
    }

    @Test
    fun alreadySortedInputDoesNotFabricateSwaps() {
        val steps = SelectionSortTrace.generate(listOf(1, 2, 3))
        val swaps = steps.filter { it.event == SelectionSortEvent.SWAP }
        val passEnds = steps.filter { it.event == SelectionSortEvent.PASS_COMPLETE }

        assertTrue(swaps.isEmpty())
        assertEquals(3, steps.count { it.event == SelectionSortEvent.COMPARISON })
        assertEquals(listOf(listOf(0), listOf(0, 1)), passEnds.map { it.sortedIndices })
        assertEquals(listOf(1, 2, 3), steps.last().array)
    }

    @Test
    fun duplicateAndReverseOrderedInputsSortCorrectly() {
        val duplicateTrace = SelectionSortTrace.generate(listOf(2, 1, 2, 1))
        val reverseTrace = SelectionSortTrace.generate(listOf(4, 3, 2, 1))

        assertEquals(listOf(1, 1, 2, 2), duplicateTrace.last().array)
        assertEquals(listOf(1, 2, 3, 4), reverseTrace.last().array)
        assertEquals(6, reverseTrace.count { it.event == SelectionSortEvent.COMPARISON })
        assertTrue(duplicateTrace.filter { it.event == SelectionSortEvent.SWAP }.all { step ->
            step.array[step.passIndex!!] <= step.array[step.highlightedIndices.last()]
        })
    }

    @Test
    fun emptyAndSingleElementInputsCompleteWithoutComparisons() {
        listOf(emptyList(), listOf(7)).forEach { input ->
            val steps = SelectionSortTrace.generate(input)

            assertEquals(1, steps.size)
            assertEquals(SelectionSortEvent.COMPLETE, steps.single().event)
            assertEquals(input, steps.single().array)
            assertFalse(steps.single().sortedIndices.any { it !in input.indices })
        }
    }
}
