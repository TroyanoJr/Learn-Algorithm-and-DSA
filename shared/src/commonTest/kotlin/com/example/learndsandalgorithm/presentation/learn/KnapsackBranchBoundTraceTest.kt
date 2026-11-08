package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KnapsackBranchBoundTraceTest {

    @Test
    fun createTraceComputesKnapsackBranchBoundOptimalValue85() {
        val steps = KnapsackBranchBoundTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(KnapsackBbAction.FINISHED, lastStep.action)
        assertEquals(85, lastStep.incumbentValue)
        assertEquals(listOf("Item 1", "Item 3"), lastStep.incumbentItems)

        // Verify pruning occurred
        val pruneStep = steps.find { it.action == KnapsackBbAction.PRUNE_NODE }
        assertTrue(pruneStep != null)
    }
}
