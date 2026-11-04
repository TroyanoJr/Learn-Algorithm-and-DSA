package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuickSortPartitionTraceTest {

    @Test
    fun stepsForPartitionRearrangesElementsAroundPivotInPlace() {
        val initial = listOf(10, 7, 8, 9, 1, 5)
        val steps = QuickSortPartitionTrace.stepsForPartition(initial)

        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(QuickPartitionEvent.PARTITION_COMPLETE, lastStep.event)

        val finalPivotIndex = lastStep.finalPivotPosition!!
        val pivotVal = lastStep.pivotValue

        // Verify elements left of pivot are <= pivot
        for (k in 0 until finalPivotIndex) {
            assertTrue(lastStep.array[k] <= pivotVal, "Element at $k should be <= $pivotVal")
        }

        // Verify elements right of pivot are >= pivot
        for (k in (finalPivotIndex + 1) until lastStep.array.size) {
            assertTrue(lastStep.array[k] >= pivotVal, "Element at $k should be >= $pivotVal")
        }
    }
}
