package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BranchBoundFundamentalsTraceTest {

    @Test
    fun createTraceFindsOptimalMinimizationCostAndPrunesHighBoundNode() {
        val steps = BranchBoundFundamentalsTrace.createTrace()
        assertTrue(steps.isNotEmpty())
        assertEquals(7, steps.size)
        assertEquals(8, steps.first().currentNodeBound)

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BbFundamentalsAction.FINISHED, lastStep.action)
        assertEquals(8, lastStep.incumbentCost)

        // Verify Node B was pruned due to lower bound 18 >= incumbent 15
        assertTrue(lastStep.prunedNodeIds.contains("Node B"))
        assertEquals("Leaf C (Cost 8)", lastStep.incumbentSolution)

        val nodeB = steps.first { it.currentNodeId == "Node B" }
        assertEquals(15, nodeB.comparisonThreshold)
        assertEquals(15, steps.first { it.currentNodeId == "Node C" }.comparisonThreshold)
        assertEquals(null, steps.first { it.currentNodeId == "Leaf C" }.comparisonThreshold)

        val frames = BranchBoundFundamentalsSceneBuilder.build(steps)
        assertEquals(15, frames.last().nodes.first { it.id == "Node B" }.comparisonThreshold)
    }
}
