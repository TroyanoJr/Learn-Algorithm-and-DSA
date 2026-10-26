package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueueTraceTest {
    @Test
    fun circularBufferTraceMatchesLessonWraparoundAndFifoOrder() {
        val steps = QueueTrace.steps
        val enqueueE = steps.first { it.event == QueueTraceEvent.ENQUEUE && it.values.lastOrNull() == "E" }
        val peeks = steps.filter { it.event == QueueTraceEvent.PEEK }
        val dequeues = steps.filter { it.event == QueueTraceEvent.DEQUEUE }

        assertEquals(listOf("A", "B", "C"), steps[3].values)
        assertEquals(listOf("A", "B", "C"), peeks.first().values)
        assertEquals("A", peeks.first().returnedValue)
        assertEquals(peeks.first().values, steps[steps.indexOf(peeks.first()) - 1].values)
        assertEquals(0, enqueueE.highlightedSlot)
        assertEquals(listOf("E", null, "C", "D"), enqueueE.slots)
        assertEquals(listOf("C", "D", "E"), enqueueE.values)
        assertEquals(2, enqueueE.frontIndex)
        assertEquals(1, enqueueE.nextRearIndex)
        assertEquals(listOf("A", "B", "C", "D", "E"), dequeues.map { it.returnedValue })
        assertTrue(steps.last().isTerminal)
    }

    @Test
    fun enqueuePeekAndDequeuePreserveSnapshotsAndFifo() {
        val steps = QueueTrace.generate(
            listOf(
                QueueOperation.Enqueue("7"),
                QueueOperation.Enqueue("4"),
                QueueOperation.Peek,
                QueueOperation.Dequeue
            ),
            capacity = 3
        )

        assertEquals(listOf("7"), steps[1].values)
        assertEquals(listOf("7", "4"), steps[2].values)
        assertEquals(0, steps[3].highlightedSlot)
        assertEquals("7", steps[3].returnedValue)
        assertEquals(steps[2].values, steps[3].values)
        assertEquals(listOf("4"), steps[4].values)
        assertEquals("7", steps[4].returnedValue)
        assertEquals(1, steps[4].frontIndex)
    }

    @Test
    fun emptyDequeueAndPeekAreSafeAndDoNotMutateQueue() {
        val steps = QueueTrace.generate(
            listOf(QueueOperation.Dequeue, QueueOperation.Peek),
            capacity = 2
        )

        assertEquals(QueueTraceEvent.UNDERFLOW, steps[1].event)
        assertEquals(QueueTraceEvent.EMPTY_ACCESS, steps[2].event)
        assertEquals(emptyList(), steps[1].values)
        assertEquals(steps[1].slots, steps[2].slots)
        assertNull(steps[1].returnedValue)
        assertNull(steps[2].returnedValue)
        assertTrue(steps[1].explanation.contains("dequeue"))
        assertTrue(steps[2].explanation.contains("peek"))
    }

    @Test
    fun fullQueueRejectsEnqueueWithoutChangingStorage() {
        val steps = QueueTrace.generate(
            listOf(QueueOperation.Enqueue("C")),
            capacity = 2,
            initialValues = listOf("A", "B")
        )

        assertEquals(QueueTraceEvent.OVERFLOW, steps[1].event)
        assertEquals(listOf("A", "B"), steps[1].values)
        assertEquals(listOf("A", "B"), steps[1].slots)
        assertEquals(0, steps[1].frontIndex)
        assertEquals(0, steps[1].nextRearIndex)
    }

    @Test
    fun singleSlotQueueWrapsAndReturnsToEmpty() {
        val steps = QueueTrace.generate(
            listOf(QueueOperation.Enqueue("X"), QueueOperation.Dequeue),
            capacity = 1
        )

        assertEquals(listOf("X"), steps[1].values)
        assertEquals(0, steps[1].frontIndex)
        assertEquals(0, steps[1].nextRearIndex)
        assertEquals(listOf(null), steps[2].slots)
        assertEquals(emptyList(), steps[2].values)
        assertEquals(0, steps[2].frontIndex)
        assertEquals(0, steps[2].nextRearIndex)
        assertEquals(QueueTraceEvent.COMPLETE, steps.last().event)
        assertFalse(steps.dropLast(1).last().isTerminal)
    }
}
