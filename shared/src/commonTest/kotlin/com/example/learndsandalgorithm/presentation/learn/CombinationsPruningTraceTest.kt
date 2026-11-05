package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CombinationsPruningTraceTest {

    @Test
    fun createTraceFindsCombinationSumSolutionAndPrunesInvalidSums() {
        val steps = CombinationsPruningTrace.createTrace(candidates = listOf(2, 3), target = 5)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CombinationAction.FINISHED, lastStep.action)
        assertEquals(listOf(listOf(2, 3)), lastStep.validCombinations)

        // Verify that pruning actions occurred when sum > target
        val pruneSteps = steps.filter { it.action == CombinationAction.PRUNE }
        assertTrue(pruneSteps.isNotEmpty())
    }
}
