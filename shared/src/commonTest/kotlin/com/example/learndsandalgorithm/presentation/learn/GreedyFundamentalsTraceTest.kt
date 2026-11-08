package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreedyFundamentalsTraceTest {

    @Test
    fun createTraceComputesCorrectGreedyStops() {
        val steps = GreedyFundamentalsTrace.createTrace(listOf(2, 3, 4, 6), budget = 10)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(GreedyFundamentalsStatus.FINISHED, lastStep.status)
        assertEquals(listOf(2, 3, 4), lastStep.chosenDistances)
        assertEquals(1, lastStep.remainingBudget)
        assertEquals(3, lastStep.chosenDistances.size)
    }
}
