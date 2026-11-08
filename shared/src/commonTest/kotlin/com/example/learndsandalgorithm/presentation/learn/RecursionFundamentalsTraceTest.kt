package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecursionFundamentalsTraceTest {

    @Test
    fun createTraceComputesSumTo3Recursively() {
        val steps = RecursionFundamentalsTrace.createTrace(inputN = 3)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(RecursionAction.FINISHED, lastStep.action)
        assertEquals(6, lastStep.currentReturnValue)

        // Verify base case reached step
        val baseCaseStep = steps.find { it.action == RecursionAction.BASE_CASE_REACHED }
        assertTrue(baseCaseStep != null)
        assertEquals(0, baseCaseStep.currentN)
    }
}
