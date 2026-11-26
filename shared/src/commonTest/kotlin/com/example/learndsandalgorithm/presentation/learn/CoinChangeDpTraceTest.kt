package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class CoinChangeDpTraceTest {

    @Test
    fun handlesUnreachableLargeAndInvalidInputs() {
        val unreachable = CoinChangeDpTrace.createTrace(3, listOf(2)).last()
        assertEquals(-1, unreachable.candidateVal)
        assertTrue(unreachable.explanation.contains("No solution"))
        assertEquals("COMPLETE: NO SOLUTION", unreachable.badgeText)
        assertEquals(999, CoinChangeDpTrace.createTrace(999, listOf(1)).last().dpTable[999])
        assertEquals(0, CoinChangeDpTrace.createTrace(0, emptyList()).last().dpTable[0])
        assertFailsWith<IllegalArgumentException> { CoinChangeDpTrace.createTrace(-1) }
        assertFailsWith<IllegalArgumentException> { CoinChangeDpTrace.createTrace(2, listOf(0)) }
        assertFailsWith<IllegalArgumentException> { CoinChangeDpTrace.createTrace(2, listOf(-1)) }
    }

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
