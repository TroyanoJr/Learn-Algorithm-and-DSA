package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MergeSortFundamentalsTraceTest {

    @Test
    fun defaultMergeKeepsExpectedEventsAndCompletesWithEveryValue() {
        val steps = MergeSortFundamentalsTrace.stepsForMerge()
        val terminal = steps.last()

        assertEquals(
            listOf(
                MergeStepEvent.START,
                MergeStepEvent.COMPARE, MergeStepEvent.TAKE_LEFT,
                MergeStepEvent.COMPARE, MergeStepEvent.TAKE_RIGHT,
                MergeStepEvent.COMPARE, MergeStepEvent.TAKE_LEFT,
                MergeStepEvent.COMPARE, MergeStepEvent.TAKE_RIGHT,
                MergeStepEvent.COMPARE, MergeStepEvent.TAKE_LEFT,
                MergeStepEvent.FLUSH_REMAINING,
                MergeStepEvent.COMPLETE
            ),
            steps.map { it.event }
        )
        assertEquals(listOf(1, 2, 4, 5, 7, 8), terminal.mergedResult)
        assertEquals(terminal.leftArr.size + terminal.rightArr.size, terminal.mergedResult.size)
        assertEquals(terminal.mergedResult.size, terminal.outputOrigins.size)
        assertTrue(terminal.isTerminal)
        assertTrue(terminal.leftExhausted && terminal.rightExhausted)
    }

    @Test
    fun everyValidAlignmentRecordsCorrectComparisonAndTransferOffsets() {
        val left = listOf(-3, 4, 10)
        val right = listOf(0, 5)
        val steps = MergeSortFundamentalsTrace.stepsForMerge(left, right)

        steps.forEachIndexed { index, step ->
            if (step.event != MergeStepEvent.COMPARE) return@forEachIndexed
            val comparison = assertNotNull(step.comparison)
            assertEquals(left[comparison.leftIndex], comparison.leftValue)
            assertEquals(right[comparison.rightIndex], comparison.rightValue)
            assertEquals(step.i, comparison.leftIndex)
            assertEquals(step.j, comparison.rightIndex)
            assertEquals(step.mergedResult.size, step.k)

            val take = steps[index + 1]
            assertTrue(take.event == MergeStepEvent.TAKE_LEFT || take.event == MergeStepEvent.TAKE_RIGHT)
            assertEquals(comparison, take.comparison)
            val source = assertNotNull(take.source)
            val sourceIndex = assertNotNull(take.sourceIndexBeforeTransfer)
            val destination = assertNotNull(take.destinationIndex)
            val before = assertNotNull(take.pointersBeforeTransfer)
            val after = assertNotNull(take.pointersAfterTransfer)

            assertEquals(step.i, before.i)
            assertEquals(step.j, before.j)
            assertEquals(step.k, before.k)
            assertEquals(step.k, destination)
            assertEquals(take.i, after.i)
            assertEquals(take.j, after.j)
            assertEquals(take.k, after.k)
            assertEquals(before.k + 1, after.k)
            assertEquals(take.outputOrigins.last(), take.selectedOrigin)
            assertEquals(take.destinationIndex, take.mergedResult.lastIndex)
            when (source) {
                MergeSource.LEFT -> {
                    assertEquals(left[sourceIndex], take.mergedResult[destination])
                    assertEquals(left[sourceIndex], take.takenVal)
                    assertEquals(before.i + 1, after.i)
                    assertEquals(before.j, after.j)
                }
                MergeSource.RIGHT -> {
                    assertEquals(right[sourceIndex], take.mergedResult[destination])
                    assertEquals(right[sourceIndex], take.takenVal)
                    assertEquals(before.j + 1, after.j)
                    assertEquals(before.i, after.i)
                }
            }
        }
        assertEquals(listOf(-3, 0, 4, 5, 10), steps.last().mergedResult)
    }

    @Test
    fun equalValuesTakeLeftFirstAndKeepStableSourceOrigins() {
        val left = listOf(-2, 3, 3, 8)
        val right = listOf(-2, 3, 3, 9)
        val steps = MergeSortFundamentalsTrace.stepsForMerge(left, right)
        val terminal = steps.last()

        steps.forEachIndexed { index, comparison ->
            if (comparison.event != MergeStepEvent.COMPARE || comparison.comparison?.valuesAreEqual != true) return@forEachIndexed
            assertEquals(MergeTieBreakDecision.LEFT_FIRST_ON_EQUAL, comparison.tieBreakDecision)
            val next = steps[index + 1]
            assertEquals(MergeStepEvent.TAKE_LEFT, next.event)
            assertEquals("left:${comparison.i}", next.selectedOrigin?.identity)
        }

        val sortedOrigins = terminal.outputOrigins.map { origin ->
            (if (origin.source == MergeSource.LEFT) left[origin.index] else right[origin.index]) to origin
        }
        assertEquals(listOf(-2, -2, 3, 3, 3, 3, 8, 9), terminal.mergedResult)
        assertEquals(
            listOf(MergeElementOrigin(MergeSource.LEFT, 0), MergeElementOrigin(MergeSource.RIGHT, 0)),
            sortedOrigins.filter { it.first == -2 }.map { it.second }
        )
        assertEquals(
            listOf(
                MergeElementOrigin(MergeSource.LEFT, 1),
                MergeElementOrigin(MergeSource.LEFT, 2),
                MergeElementOrigin(MergeSource.RIGHT, 1),
                MergeElementOrigin(MergeSource.RIGHT, 2)
            ),
            sortedOrigins.filter { it.first == 3 }.map { it.second }
        )
        assertEquals(listOf(0, 0, 1, 2, 1, 2, 3, 3), terminal.outputOrigins.map { it.index })
    }

    @Test
    fun emptySingletonUnequalAndExtremeInputsAreSafeAndComplete() {
        val cases = listOf(
            emptyList<Int>() to emptyList(),
            emptyList<Int>() to listOf(-4, 0, 8),
            listOf(-4, 0, 8) to emptyList(),
            listOf(Int.MIN_VALUE) to listOf(Int.MAX_VALUE),
            listOf(7) to listOf(7),
            listOf(Int.MIN_VALUE, -1, 0, Int.MAX_VALUE) to listOf(-2)
        )
        cases.forEach { (left, right) ->
            val steps = MergeSortFundamentalsTrace.stepsForMerge(left, right)
            val terminal = steps.last()
            assertTrue(terminal.isTerminal)
            assertEquals((left + right).sorted(), terminal.mergedResult)
            assertEquals(left.size + right.size, terminal.mergedResult.size)
            assertEquals(left.size + right.size, terminal.outputOrigins.size)
            assertTrue(steps.all { it.i in 0..left.size && it.j in 0..right.size })
            if (left.isEmpty() && right.isEmpty()) {
                assertEquals(listOf(MergeStepEvent.START, MergeStepEvent.COMPLETE), steps.map { it.event })
            }
        }
    }

    @Test
    fun unsortedInputsAreRejectedWithoutSortingThem() {
        assertFailsWith<IllegalArgumentException> {
            MergeSortFundamentalsTrace.stepsForMerge(listOf(2, 1), listOf(3))
        }
        assertFailsWith<IllegalArgumentException> {
            MergeSortFundamentalsTrace.stepsForMerge(listOf(1), listOf(3, 2))
        }
    }

    @Test
    fun finalFlushExplanationUsesTheSourceIndex() {
        val steps = MergeSortFundamentalsTrace.stepsForMerge(listOf(1), listOf(2, 3, 4))
        val flush = steps.last { it.event == MergeStepEvent.FLUSH_REMAINING }
        assertEquals(MergeSource.RIGHT, flush.source)
        assertEquals(2, flush.sourceIndexBeforeTransfer)
        assertTrue(flush.explanation.contains("Right[2] = 4"))
        assertTrue(flush.explanation.contains("Left array exhausted"))
    }
}
