package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BestFirstSearchTraceTest {

    @Test
    fun createTracePopsMostPromisingNodeFirstAndPrunesPq() {
        val steps = BestFirstSearchTrace.createTrace()
        assertTrue(steps.isNotEmpty())
        assertEquals(6, steps.size)

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BestFirstAction.FINISHED, lastStep.action)
        assertEquals(7, lastStep.incumbentCost)

        // Verify remaining PQ nodes were pruned after finding cost 7
        val pruneStep = steps.find { it.action == BestFirstAction.PRUNE_REMAINING_PQ }
        assertTrue(pruneStep != null)
        assertEquals(3, pruneStep.prunedNodes.size)
        assertEquals(listOf("Node 4", "Node 2", "Node 1", "Node 3"), steps[1].priorityQueue.map { it.id })

        val frames = BestFirstSearchSceneBuilder.build(steps)
        assertEquals(null, frames.first().incumbent)
        assertEquals(7, frames.last().incumbent?.cost)
        val layout = BestFirstSearchSceneLayoutCalculator.calculate(0)
        assertTrue(layout.width >= 422f)
        assertTrue(layout.pruned.right <= layout.width)
    }
}
