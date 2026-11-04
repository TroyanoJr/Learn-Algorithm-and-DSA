package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MergeSortFundamentalsTraceTest {

    @Test
    fun stepsForMergeCombinesTwoSortedArraysCorrectly() {
        val left = listOf(1, 4, 7)
        val right = listOf(2, 5, 8)
        val steps = MergeSortFundamentalsTrace.stepsForMerge(left, right)

        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf(1, 2, 4, 5, 7, 8), lastStep.mergedResult)
    }
}
