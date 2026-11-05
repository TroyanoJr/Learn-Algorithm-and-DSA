package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphUnweightedShortestPathTraceTest {

    @Test
    fun stepsForShortestPathReconstructsShortestRoute() {
        val steps = GraphUnweightedShortestPathTrace.stepsForShortestPath("A", "E")
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf("A", "B", "D", "E"), lastStep.reconstructedPath)
        assertEquals(3, lastStep.distances["E"])
    }
}
