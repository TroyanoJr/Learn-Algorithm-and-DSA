package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoinChangeDpTraceTest {

    @Test
    fun createTraceComputesMinCoinChangeForTarget6() {
        val steps = CoinChangeDpTrace.createTrace(targetAmount = 6, coins = listOf(1, 2, 5))
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CoinChangeDpAction.FINISHED, lastStep.action)
        assertEquals(2, lastStep.dpTable[6])
    }
}
