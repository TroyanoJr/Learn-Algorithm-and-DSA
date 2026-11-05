package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BacktrackingComplexityTraceTest {

    @Test
    fun createTraceComparesPrunedVsUnprunedCalls() {
        val steps = BacktrackingComplexityTrace.createTrace(array = listOf(1, 2, 5, 8), limit = 2)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(ComplexityAction.FINISHED, lastStep.action)

        // Unpruned evaluates all 4 elements
        assertEquals(4, lastStep.unprunedCalls)

        // Pruned halts at element 5 (> 2), resulting in 3 calls
        assertEquals(3, lastStep.prunedCalls)
        assertTrue(lastStep.prunedCalls < lastStep.unprunedCalls)
    }
}
