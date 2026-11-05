package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DijkstraTraceTest {

    @Test
    fun stepsForDijkstraComputesShortestPathAndDistances() {
        val steps = DijkstraTrace.stepsForDijkstra("A", "D")
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(4, lastStep.distances["D"])
        assertEquals(listOf("A", "C", "B", "D"), lastStep.reconstructedPath)
    }
}
