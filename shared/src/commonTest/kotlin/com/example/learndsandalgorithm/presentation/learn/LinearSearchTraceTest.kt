package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LinearSearchTraceTest {
    @Test
    fun defaultTraceExaminesValuesInOrderAndFindsTarget() {
        val steps = LinearSearchTrace.steps
        val last = steps.last()

        assertEquals(LinearSearchTrace.values.indexOf(LinearSearchTrace.target), last.foundIndex)
        assertEquals(LinearSearchTrace.values.indices.toList(), steps.mapNotNull { it.currentIndex })
        assertEquals(LinearSearchTrace.values.take(steps.size), steps.mapNotNull { it.examinedValue })
        assertTrue(last.isTerminal)
        assertEquals(LinearSearchResult.FOUND, last.result)
        assertEquals(steps.size, last.inspectedCount)
    }

    @Test
    fun targetAtFirstMiddleAndLastPositionStopsAtFirstMatch() {
        listOf(
            listOf(7, 2, 9, 4, 6) to 7,
            listOf(7, 2, 9, 4, 6) to 9,
            listOf(7, 2, 9, 4, 6) to 6
        ).forEach { (values, target) ->
            val steps = LinearSearchTrace.generate(values, target)
            val matchIndex = values.indexOf(target)
            val match = steps.last()

            assertEquals(matchIndex, match.currentIndex)
            assertEquals(target, match.examinedValue)
            assertEquals(matchIndex, match.foundIndex)
            assertEquals(matchIndex + 1, match.inspectedCount)
            assertEquals(LinearSearchResult.FOUND, match.result)
            assertTrue(match.isTerminal)
            assertEquals((0..matchIndex).toList(), steps.map { it.currentIndex })
            assertEquals(values.take(matchIndex + 1), steps.map { it.examinedValue })
            steps.forEachIndexed { index, step ->
                assertEquals(values, step.values)
                if (index == matchIndex) {
                    assertEquals("Check index $index: value $target == target $target. Target found at index $index.", step.explanation)
                } else {
                    assertEquals("Check index $index: value ${values[index]} != target $target. Continue to the next index.", step.explanation)
                    assertEquals(LinearSearchResult.NOT_EQUAL, step.result)
                }
            }
        }
    }

    @Test
    fun absentTargetProducesOrderedComparisonsThenNotFoundTerminalState() {
        val values = listOf(4, 8, 2)
        val steps = LinearSearchTrace.generate(values, 5)
        val comparisons = steps.dropLast(1)
        val terminal = steps.last()

        assertEquals(listOf(0, 1, 2), comparisons.map { it.currentIndex })
        assertEquals(values, comparisons.mapNotNull { it.examinedValue })
        assertTrue(comparisons.all { it.result == LinearSearchResult.NOT_EQUAL && !it.isTerminal })
        assertTrue(comparisons.all { it.foundIndex == null })
        assertEquals("Check index 1: value 8 != target 5. Continue to the next index.", comparisons[1].explanation)
        assertNull(terminal.currentIndex)
        assertNull(terminal.examinedValue)
        assertNull(terminal.foundIndex)
        assertEquals(LinearSearchResult.NOT_FOUND, terminal.result)
        assertEquals(values.size, terminal.inspectedCount)
        assertTrue(terminal.isTerminal)
        assertEquals("Target 5 not found after inspecting all 3 items.", terminal.explanation)
    }

    @Test
    fun emptyInputHasOnlyNotFoundTerminalState() {
        val steps = LinearSearchTrace.generate(emptyList(), 5)

        assertEquals(1, steps.size)
        assertEquals(LinearSearchResult.NOT_FOUND, steps.single().result)
        assertNull(steps.single().currentIndex)
        assertNull(steps.single().examinedValue)
        assertEquals(0, steps.single().inspectedCount)
        assertTrue(steps.single().isTerminal)
    }

    @Test
    fun singleElementMatchAndMissAreSafe() {
        val match = LinearSearchTrace.generate(listOf(3), 3)
        val miss = LinearSearchTrace.generate(listOf(3), 8)

        assertEquals(1, match.size)
        assertEquals(0, match.single().currentIndex)
        assertEquals(3, match.single().examinedValue)
        assertEquals(0, match.single().foundIndex)
        assertEquals(LinearSearchResult.FOUND, match.single().result)
        assertEquals(2, miss.size)
        assertEquals(LinearSearchResult.NOT_EQUAL, miss[0].result)
        assertEquals(LinearSearchResult.NOT_FOUND, miss[1].result)
        assertEquals(1, miss[1].inspectedCount)
    }

    @Test
    fun duplicateTargetStopsAtItsFirstOccurrence() {
        val values = listOf(9, 2, 5, 5)
        val steps = LinearSearchTrace.generate(values, 5)

        assertEquals(3, steps.size)
        assertEquals(2, steps.last().currentIndex)
        assertEquals(2, steps.last().foundIndex)
        assertEquals(5, steps.last().examinedValue)
        assertEquals(3, steps.last().inspectedCount)
        assertEquals(LinearSearchResult.FOUND, steps.last().result)
        assertFalse(steps.any { it.currentIndex == 3 })
    }
}
