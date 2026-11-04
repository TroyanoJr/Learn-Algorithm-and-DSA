package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeapInsertSiftUpTraceTest {

    @Test
    fun stepsForInsertPerformsSiftUpAndRestoresMinHeap() {
        val initial = listOf(2, 5, 3, 9, 7, 8)
        val steps = HeapInsertSiftUpTrace.stepsForInsert(initial, valueToInsert = 1)

        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(InsertSiftUpEvent.SIFT_UP_COMPLETE, lastStep.event)
        assertEquals(listOf(1, 5, 2, 9, 7, 8, 3), lastStep.heap)
    }
}
