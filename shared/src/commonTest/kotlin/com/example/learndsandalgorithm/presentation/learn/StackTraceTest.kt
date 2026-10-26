package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StackTraceTest {
    @Test
    fun demonstrationShowsPushPeekAndLifoPopResults() {
        val steps = StackTrace.steps
        val pushes = steps.filter { it.event == StackTraceEvent.PUSH }
        val pops = steps.filter { it.event == StackTraceEvent.POP }
        val peek = steps.first { it.event == StackTraceEvent.PEEK }

        assertEquals(listOf("A", "B", "C", "D", "E"), pushes[4].values)
        assertEquals(4, pushes[4].highlightedIndex)
        assertEquals(listOf("A", "B", "C", "D", "E"), peek.values)
        assertEquals(4, peek.highlightedIndex)
        assertEquals("E", peek.returnedValue)
        assertEquals(pushes[4].values, peek.values)
        assertEquals(listOf("E", "F", "D", "C", "B", "A"), pops.map { it.returnedValue })
        assertEquals(emptyList(), steps.last().values)
        assertTrue(steps.last().isTerminal)
    }

    @Test
    fun emptyPopAndPeekAreReportedWithoutChangingState() {
        val steps = StackTrace.generate(listOf(StackOperation.Peek, StackOperation.Pop))

        assertEquals(StackTraceEvent.START, steps[0].event)
        assertEquals(StackTraceEvent.UNDERFLOW, steps[1].event)
        assertEquals(StackTraceEvent.UNDERFLOW, steps[2].event)
        assertEquals(emptyList(), steps[1].values)
        assertEquals(steps[1].values, steps[2].values)
        assertNull(steps[1].returnedValue)
        assertNull(steps[2].returnedValue)
        assertTrue(steps[1].explanation.contains("peek"))
        assertTrue(steps[2].explanation.contains("pop"))
    }

    @Test
    fun fixedCapacityRejectsPushWithoutLosingLifoContents() {
        val steps = StackTrace.generate(
            operations = listOf(StackOperation.Push("C"), StackOperation.Pop),
            initialValues = listOf("A", "B"),
            capacity = 2
        )

        assertEquals(StackTraceEvent.OVERFLOW, steps[1].event)
        assertEquals(listOf("A", "B"), steps[1].values)
        assertNull(steps[1].returnedValue)
        assertEquals(StackTraceEvent.POP, steps[2].event)
        assertEquals("B", steps[2].returnedValue)
        assertEquals(listOf("A"), steps[2].values)
    }

    @Test
    fun normalOperationsPreserveSnapshotsAndOnlyPeekLeavesStateUntouched() {
        val steps = StackTrace.generate(
            listOf(StackOperation.Push("X"), StackOperation.Peek, StackOperation.Pop)
        )

        assertEquals(emptyList(), steps[0].values)
        assertEquals(listOf("X"), steps[1].values)
        assertEquals(listOf("X"), steps[2].values)
        assertEquals(emptyList(), steps[3].values)
        assertEquals(emptyList(), steps[4].values)
        assertEquals("X", steps[2].returnedValue)
        assertEquals("X", steps[3].returnedValue)
        assertEquals(StackTraceEvent.COMPLETE, steps.last().event)
        assertTrue(steps.last().isTerminal)
        assertFalse(steps.dropLast(1).any { it.isTerminal })
    }
}
