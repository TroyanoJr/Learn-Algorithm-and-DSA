package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoundsPruningTraceTest {

    @Test
    fun createTraceEvaluatesMinimizationAndMaximizationPruningRules() {
        val steps = BoundsPruningTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BoundsPruningAction.FINISHED, lastStep.action)
        assertEquals(48, lastStep.maxIncumbent)

        // Verify minimization pruning step
        val minPruned = steps.find { it.evaluatedNodeId == "Node B" }
        assertEquals(true, minPruned?.isPruned)

        // Verify maximization pruning step
        val maxPruned = steps.find { it.evaluatedNodeId == "Node Y" }
        assertEquals(true, maxPruned?.isPruned)
    }
}
