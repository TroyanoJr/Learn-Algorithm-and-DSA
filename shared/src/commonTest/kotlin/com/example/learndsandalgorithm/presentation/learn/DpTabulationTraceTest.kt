package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DpTabulationTraceTest {

    @Test
    fun createTraceComputesClimbingStairs5WithTabulation() {
        val steps = DpTabulationTrace.createTrace(targetN = 5)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(TabulationAction.FINISHED, lastStep.action)
        assertEquals(8L, lastStep.currentWays)
        assertEquals(8L, lastStep.dpTable[5])
    }
}
