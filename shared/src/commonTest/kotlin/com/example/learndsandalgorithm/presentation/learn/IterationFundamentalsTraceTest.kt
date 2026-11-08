package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IterationFundamentalsTraceTest {

    @Test
    fun createTraceComputesLoopSum1To4Correctly() {
        val steps = IterationFundamentalsTrace.createTrace(limit = 4)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(IterationAction.TERMINATED, lastStep.action)
        assertEquals(10, lastStep.total)
        assertEquals(5, lastStep.i)
    }
}
