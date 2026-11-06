package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecursionTradeoffsTraceTest {

    @Test
    fun createTraceComparesIterativeAndRecursiveSumTo4() {
        val steps = RecursionTradeoffsTrace.createTrace(inputN = 4)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(TradeoffAction.FINISHED, lastStep.action)
        assertEquals(10, lastStep.iterTotal)
        assertEquals(10, lastStep.recurReturnVal)
    }
}
