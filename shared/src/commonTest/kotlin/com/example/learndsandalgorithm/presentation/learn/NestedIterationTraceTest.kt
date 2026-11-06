package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NestedIterationTraceTest {

    @Test
    fun createTraceGeneratesAll6CoordinatePairs() {
        val steps = NestedIterationTrace.createTrace(outerLimit = 2, innerLimit = 3)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(NestedIterationAction.FINISHED, lastStep.action)
        assertEquals(6, lastStep.totalCount)

        val expected = listOf(
            Pair(1, 1), Pair(1, 2), Pair(1, 3),
            Pair(2, 1), Pair(2, 2), Pair(2, 3)
        )
        assertEquals(expected, lastStep.printedPairs)
    }
}
