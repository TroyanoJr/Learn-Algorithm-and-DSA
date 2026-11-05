package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphRepresentationsTraceTest {

    @Test
    fun adjacencyListTraceContainsCorrectNeighborList() {
        val steps = GraphRepresentationsTrace.stepsForView(RepresentationView.ADJACENCY_LIST)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf("B", "C", "E"), lastStep.adjList["D"])
    }

    @Test
    fun adjacencyMatrixTraceContainsCorrectGridValues() {
        val steps = GraphRepresentationsTrace.stepsForView(RepresentationView.ADJACENCY_MATRIX)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        // Row D (index 3): B(1), C(2), E(4) = 1
        assertEquals(listOf(0, 1, 1, 0, 1), lastStep.adjMatrix[3])
    }
}
