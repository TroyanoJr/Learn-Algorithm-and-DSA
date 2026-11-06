package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BranchBoundFundamentalsTraceTest {

    @Test
    fun createTraceFindsOptimalMinimizationCostAndPrunesHighBoundNode() {
        val steps = BranchBoundFundamentalsTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BbFundamentalsAction.FINISHED, lastStep.action)
        assertEquals(8, lastStep.incumbentCost)

        // Verify Node B was pruned due to lower bound 18 >= incumbent 15
        assertTrue(lastStep.prunedNodeIds.contains("Node B"))
    }
}
