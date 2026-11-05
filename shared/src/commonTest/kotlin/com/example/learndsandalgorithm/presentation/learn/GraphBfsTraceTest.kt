package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphBfsTraceTest {

    @Test
    fun stepsForBfsExploresGraphInAlphabeticalLayerOrder() {
        val steps = GraphBfsTrace.stepsForBfs("A")
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf("A", "B", "C", "D", "E"), lastStep.visited)
    }
}
