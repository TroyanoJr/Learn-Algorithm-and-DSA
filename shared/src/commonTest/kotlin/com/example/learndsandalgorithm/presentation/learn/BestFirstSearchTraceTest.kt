package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BestFirstSearchTraceTest {

    @Test
    fun createTracePopsMostPromisingNodeFirstAndPrunesPq() {
        val steps = BestFirstSearchTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BestFirstAction.FINISHED, lastStep.action)
        assertEquals(7, lastStep.incumbentCost)

        // Verify remaining PQ nodes were pruned after finding cost 7
        val pruneStep = steps.find { it.action == BestFirstAction.PRUNE_REMAINING_PQ }
        assertTrue(pruneStep != null)
        assertEquals(3, pruneStep.prunedNodes.size)
    }
}
