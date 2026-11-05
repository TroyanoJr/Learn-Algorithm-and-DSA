package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreedyCoinChangeTraceTest {

    @Test
    fun createTraceComputesCorrectCoinsForTarget11() {
        val steps = GreedyCoinChangeTrace.createTrace(target = 11, denominations = listOf(4, 3, 1))
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CoinChangeStatus.FINISHED, lastStep.status)
        assertEquals(listOf(4, 4, 3), lastStep.selectedCoins)
        assertEquals(0, lastStep.remainingAmount)
        assertEquals(3, lastStep.selectedCoins.size)
    }
}
