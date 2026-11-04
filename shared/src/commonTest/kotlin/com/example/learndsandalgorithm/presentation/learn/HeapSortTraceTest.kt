package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeapSortTraceTest {

    @Test
    fun stepsForHeapSortSortsArrayInAscendingOrder() {
        val initial = listOf(4, 1, 3, 2)
        val steps = HeapSortTrace.stepsForHeapSort(initial)

        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(HeapSortEvent.SORT_COMPLETE, lastStep.event)
        assertEquals(listOf(1, 2, 3, 4), lastStep.array)
    }
}
