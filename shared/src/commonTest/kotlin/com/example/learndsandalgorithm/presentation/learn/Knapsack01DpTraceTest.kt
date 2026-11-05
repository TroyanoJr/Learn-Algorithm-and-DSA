package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Knapsack01DpTraceTest {

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
