package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class Knapsack01DpTraceTest {
    @Test
    fun invalidWeightsAndCapacityAreRejectedAndZeroWeightIsUsedOnce() {
        assertFailsWith<IllegalArgumentException> { Knapsack01DpTrace.createTrace(capacity = -1) }
        assertFailsWith<IllegalArgumentException> { Knapsack01DpTrace.createTrace(listOf(Knapsack01Item("X", -1, 3)), 2) }
        val trace = Knapsack01DpTrace.createTrace(listOf(Knapsack01Item("X", 0, 3)), 0)
        assertEquals(3, trace.last().dpTable[0])
        assertEquals(1, trace.count { it.action == Knapsack01Action.UPDATE_DP })
    }

    @Test
    fun createTraceComputes01KnapsackMaxVal7() {
        val items = listOf(Knapsack01Item("A", 2, 3), Knapsack01Item("B", 3, 4))
        val steps = Knapsack01DpTrace.createTrace(items = items, capacity = 5)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(Knapsack01Action.FINISHED, lastStep.action)
        assertEquals(7, lastStep.dpTable[5])
    }
}
