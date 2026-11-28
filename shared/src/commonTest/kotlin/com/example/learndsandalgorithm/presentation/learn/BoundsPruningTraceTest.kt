package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoundsPruningTraceTest {

    @Test
    fun createTraceEvaluatesMinimizationAndMaximizationPruningRules() {
        val steps = BoundsPruningTrace.createTrace()
        assertTrue(steps.isNotEmpty())
        assertEquals(7, steps.size)

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
        assertEquals(40, maxPruned?.comparisonThreshold)

        assertTrue(BoundsPruningTrace.isPruned(ProblemType.MINIMIZATION, 10, 10))
        assertTrue(BoundsPruningTrace.isPruned(ProblemType.MAXIMIZATION, 40, 40))
        assertTrue(!BoundsPruningTrace.isPruned(ProblemType.MINIMIZATION, 9, 10))
        assertTrue(!BoundsPruningTrace.isPruned(ProblemType.MAXIMIZATION, 41, 40))

        val leaf = steps.first { it.evaluatedNodeId == "Leaf X" }
        assertEquals(40, leaf.comparisonThreshold)
    }
}
