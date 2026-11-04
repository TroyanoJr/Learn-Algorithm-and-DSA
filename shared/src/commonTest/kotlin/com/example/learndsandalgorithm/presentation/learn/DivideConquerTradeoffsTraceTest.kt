package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DivideConquerTradeoffsTraceTest {

    @Test
    fun stepsForPartitionSplitComparesTreeDepths() {
        val steps = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(5, lastStep.goodLeft)
        assertEquals(5, lastStep.goodRight)
        assertEquals(1, lastStep.badLeft)
        assertEquals(9, lastStep.badRight)
    }

    @Test
    fun stepsForAlgorithmComparisonGeneratesComparisonMatrix() {
        val steps = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.ALGORITHM_COMPARISON)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(true, lastStep.mergeSortStable)
        assertEquals(false, lastStep.quickSortStable)
    }
}
