package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InsertionSortTraceTest {
    @Test
    fun defaultTraceSortsExistingExampleAndCompletesSeparately() {
        val steps = InsertionSortTrace.steps
        val completion = steps.last()

        assertEquals(InsertionSortTrace.initialValues.sorted(), completion.array)
        assertEquals(InsertionSortEvent.COMPLETE, completion.event)
        assertEquals(completion.array.indices.toList(), completion.sortedIndices)
        assertTrue(steps.dropLast(1).all { it.event != InsertionSortEvent.COMPLETE })
        steps.forEach { assertEquals(InsertionSortTrace.initialValues.size, it.array.size) }
    }

    @Test
    fun comparisonShiftAndInsertionCaptureTrueArraySnapshots() {
        val steps = InsertionSortTrace.generate(listOf(5, 3))

        assertEquals(
            listOf(
                InsertionSortEvent.COMPARISON,
                InsertionSortEvent.SHIFT,
                InsertionSortEvent.INSERT,
                InsertionSortEvent.COMPLETE
            ),
            steps.map { it.event }
        )
        assertEquals(listOf(5, 3), steps[0].array)
        assertEquals(3, steps[0].key)
        assertEquals(0, steps[0].comparisonTargetIndex)
        assertEquals(1, steps[0].holeIndex)
        assertEquals("Pass 1: Compare key 3 with 5 at index 0; shift it right.", steps[0].message)

        assertEquals(listOf(5, 5), steps[1].array)
        assertEquals(3, steps[1].key)
        assertEquals(0, steps[1].holeIndex)
        assertEquals(emptyList(), steps[1].sortedIndices)

        assertEquals(listOf(3, 5), steps[2].array)
        assertEquals(listOf(0, 1), steps[2].sortedIndices)
        assertEquals(listOf(3, 5), steps[3].array)
    }

    @Test
    fun alreadySortedInputComparesWithoutShifting() {
        val steps = InsertionSortTrace.generate(listOf(1, 2, 3))

        assertEquals(2, steps.count { it.event == InsertionSortEvent.COMPARISON })
        assertTrue(steps.none { it.event == InsertionSortEvent.SHIFT })
        assertEquals(listOf(listOf(1, 2, 3), listOf(1, 2, 3)), steps.filter {
            it.event == InsertionSortEvent.INSERT
        }.map { it.array })
        assertEquals(listOf(listOf(0, 1), listOf(0, 1, 2)), steps.filter {
            it.event == InsertionSortEvent.INSERT
        }.map { it.sortedIndices })
    }

    @Test
    fun duplicatesRemainInOrderWithoutShiftingEquals() {
        val steps = InsertionSortTrace.generate(listOf(2, 2, 1, 1))
        val equalComparisons = steps.filter {
            it.event == InsertionSortEvent.COMPARISON && it.key == it.array[it.comparisonTargetIndex!!]
        }

        assertEquals(listOf(1, 1, 2, 2), steps.last().array)
        assertTrue(equalComparisons.isNotEmpty())
        assertTrue(equalComparisons.all { it.message.endsWith("no shift is needed.") })
    }

    @Test
    fun reverseOrderedInputShiftsEachEarlierValueAndDoesNotComparePastStart() {
        val steps = InsertionSortTrace.generate(listOf(4, 3, 2, 1))
        val comparisons = steps.filter { it.event == InsertionSortEvent.COMPARISON }

        assertEquals(listOf(1, 2, 3, 4), steps.last().array)
        assertEquals(6, comparisons.size)
        assertEquals(6, steps.count { it.event == InsertionSortEvent.SHIFT })
        assertTrue(comparisons.all { it.comparisonTargetIndex!! >= 0 })
        assertEquals(listOf(0, 1, 2, 3), steps.filter { it.event == InsertionSortEvent.INSERT }
            .last().sortedIndices)
    }

    @Test
    fun emptyAndSingleElementInputsCompleteWithoutComparisons() {
        listOf(emptyList(), listOf(7)).forEach { input ->
            val steps = InsertionSortTrace.generate(input)

            assertEquals(1, steps.size)
            assertEquals(InsertionSortEvent.COMPLETE, steps.single().event)
            assertEquals(input, steps.single().array)
            assertEquals(input.indices.toList(), steps.single().sortedIndices)
        }
    }
}
