package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueueTraceTest {
    @Test
    fun noStepExceedsSixDataElementsInAnyQueueDemonstration() {
        QueueDemonstration.entries.forEach { demonstration ->
            val steps = QueueTrace.stepsFor(demonstration)
            steps.forEach { step ->
                assertTrue(
                    step.values.size <= 6,
                    "Demonstration $demonstration step has > 6 data elements: ${step.values.size}"
                )
                assertTrue(
                    step.capacity <= 6,
                    "Demonstration $demonstration capacity exceeds 6: ${step.capacity}"
                )
            }
        }
    }

    @Test
    fun fundamentalsTraceFollowsLessonEnqueueDequeueOrder() {
        val steps = QueueTrace.stepsFor(QueueDemonstration.FUNDAMENTALS)
        val enqueues = steps.filter { it.event == QueueTraceEvent.ENQUEUE }
        val dequeues = steps.filter { it.event == QueueTraceEvent.DEQUEUE }

        assertEquals(
            listOf(listOf("A"), listOf("A", "B"), listOf("A", "B", "C"), listOf("A", "B", "C", "D"),
                listOf("A", "B", "C", "D", "E"), listOf("B", "C", "D", "E", "F")),
            enqueues.map { it.values }
        )
        assertEquals(listOf("A", "B"), dequeues.map { it.returnedValue })
        assertEquals(listOf("B", "C", "D", "E"), dequeues[0].values)
        assertEquals(listOf("C", "D", "E", "F"), dequeues[1].values)
    }

    @Test
    fun operationsLessonShowsFrontWithoutMutationThenDequeuesSeven() {
        val steps = QueueTrace.stepsFor(QueueDemonstration.OPERATIONS)
        val enqueueSeven = steps.first { it.event == QueueTraceEvent.ENQUEUE && it.operationValue == "7" }
        val enqueueFour = steps.first { it.event == QueueTraceEvent.ENQUEUE && it.operationValue == "4" }
        val peek = steps.first { it.event == QueueTraceEvent.PEEK }
        val dequeueSeven = steps.first { it.event == QueueTraceEvent.DEQUEUE }

        assertEquals(listOf("7"), enqueueSeven.values)
        assertEquals(listOf("7", "4"), enqueueFour.values)
        assertEquals(enqueueFour.values, peek.values)
        assertEquals("7", peek.returnedValue)
        assertEquals(listOf("4"), dequeueSeven.values)
        assertEquals("7", dequeueSeven.returnedValue)
    }

    @Test
    fun implementationLessonShowsSameLogicalFifoInArrayAndLinkedRepresentations() {
        val steps = QueueTrace.stepsFor(QueueDemonstration.IMPLEMENTATIONS)
        val enqueues = steps.filter { it.event == QueueTraceEvent.ENQUEUE }
        val dequeue = steps.first { it.event == QueueTraceEvent.DEQUEUE }
        val finalEnqueue = enqueues.last()

        assertTrue(steps.all { it.representation == QueueRepresentation.ARRAY_AND_LINKED })
        assertEquals(listOf("10", "20", "8", "5", "6"), enqueues[2].values)
        assertEquals("10", dequeue.returnedValue)
        assertEquals(listOf("20", "8", "5", "6"), dequeue.values)
        assertEquals(listOf("20", "8", "5", "6", "9"), finalEnqueue.values)
        assertEquals("20", steps.first { it.event == QueueTraceEvent.PEEK }.returnedValue)
    }

    @Test
    fun circularBufferLessonPreservesLessonCapacityFourAndWrapsRear() {
        val steps = QueueTrace.stepsFor(QueueDemonstration.CIRCULAR_BUFFER)
        val enqueueF = steps.first { it.event == QueueTraceEvent.ENQUEUE && it.operationValue == "F" }
        val dequeues = steps.filter { it.event == QueueTraceEvent.DEQUEUE }

        assertEquals(listOf("A", "B"), dequeues.map { it.returnedValue })
        assertEquals(listOf("F", "E", "C", "D"), enqueueF.slots)
        assertEquals(listOf("C", "D", "E", "F"), enqueueF.values)
        assertEquals(2, enqueueF.frontIndex)
        assertEquals(2, enqueueF.nextRearIndex)
        assertEquals(4, enqueueF.capacity)
    }

    @Test
    fun emptyOperationsOverflowAndPeekImmutabilityRemainCorrect() {
        val empty = QueueTrace.generate(listOf(QueueOperation.Dequeue, QueueOperation.Peek), capacity = 2)
        assertEquals(QueueTraceEvent.UNDERFLOW, empty[1].event)
        assertEquals(QueueTraceEvent.EMPTY_ACCESS, empty[2].event)
        assertEquals(empty[1].slots, empty[2].slots)
        assertNull(empty[1].returnedValue)
        val full = QueueTrace.generate(listOf(QueueOperation.Enqueue("C")), capacity = 2, initialValues = listOf("A", "B"))
        assertEquals(QueueTraceEvent.OVERFLOW, full[1].event)
        assertEquals(listOf("A", "B"), full[1].values)
        val peek = QueueTrace.generate(listOf(QueueOperation.Enqueue("X"), QueueOperation.Peek), capacity = 2)[2]
        assertEquals(listOf("X"), peek.values)
        assertEquals("X", peek.returnedValue)
        assertEquals("X", peek.slots[0])
    }

    @Test
    fun capacityOneQueueWrapsBackToItsOnlySlot() {
        val steps = QueueTrace.generate(listOf(QueueOperation.Enqueue("X"), QueueOperation.Dequeue), capacity = 1)
        assertEquals(0, steps[1].frontIndex)
        assertEquals(0, steps[1].nextRearIndex)
        assertEquals(emptyList(), steps[2].values)
        assertEquals(listOf(null), steps[2].slots)
        assertFalse(steps[2].isTerminal)
        assertTrue(steps.last().isTerminal)
    }
}
