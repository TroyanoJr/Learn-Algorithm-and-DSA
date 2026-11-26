package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class DpTabulationTraceTest {
    @Test
    fun conventionalBaseCasesAndNegativeValidation() {
        listOf(0 to 1L, 1 to 1L, 2 to 2L, 5 to 8L).forEach { (n, result) ->
            val trace = DpTabulationTrace.createTrace(n)
            assertEquals(result, trace.last().currentWays)
            assertEquals(result, trace.last().dpTable[n])
            assertFalse(trace.first().rollingInitialized)
            assertTrue(trace[1].rollingInitialized)
        }
        assertEquals(6, DpTabulationTrace.createTrace().size)
        assertFailsWith<IllegalArgumentException> { DpTabulationTrace.createTrace(-1) }
    }

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
