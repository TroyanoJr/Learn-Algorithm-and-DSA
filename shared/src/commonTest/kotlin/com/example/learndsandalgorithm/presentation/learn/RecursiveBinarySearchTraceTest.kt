package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecursiveBinarySearchTraceTest {

    @Test
    fun stepsForSearchFindsTargetInSortedArray() {
        val steps = RecursiveBinarySearchTrace.stepsForSearch(listOf(2, 5, 8, 12, 16), target = 12)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BinarySearchEvent.TARGET_FOUND, lastStep.event)
        assertEquals(3, lastStep.resultIndex)
    }

    @Test
    fun stepsForSearchReturnsMinusOneWhenTargetAbsent() {
        val steps = RecursiveBinarySearchTrace.stepsForSearch(listOf(2, 5, 8, 12, 16), target = 10)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BinarySearchEvent.TARGET_NOT_FOUND, lastStep.event)
        assertEquals(-1, lastStep.resultIndex)
    }
}
