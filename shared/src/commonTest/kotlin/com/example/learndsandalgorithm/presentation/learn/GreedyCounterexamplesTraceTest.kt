package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreedyCounterexamplesTraceTest {

    @Test
    fun createTraceProvesGreedyCounterexample() {
        val steps = GreedyCounterexamplesTrace.createTrace(target = 6, denominations = listOf(4, 3, 1))
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CounterexampleStatus.FINISHED, lastStep.status)

        // Verify greedy result: [4, 1, 1] (3 coins)
        assertEquals(listOf(4, 1, 1), lastStep.greedyCoins)
        assertEquals(3, lastStep.greedyCoins.size)

        // Verify optimal result: [3, 3] (2 coins)
        assertEquals(listOf(3, 3), lastStep.optimalCoins)
        assertEquals(2, lastStep.optimalCoins.size)

        // Confirm optimal is strictly better than greedy for target 6
        assertTrue(lastStep.optimalCoins.size < lastStep.greedyCoins.size)
    }
}
