package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MergeSortFullTraceTest {

    @Test
    fun stepsForFullSortDecomposesAndSortsArray() {
        val arr = listOf(38, 27, 43, 3, 9, 82, 10)
        val steps = MergeSortFullTrace.stepsForFullSort(arr)

        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(FullMergeEvent.COMPLETE, lastStep.event)
        assertEquals(listOf(3, 9, 10, 27, 38, 43, 82), lastStep.level0.first().elements)
    }
}
