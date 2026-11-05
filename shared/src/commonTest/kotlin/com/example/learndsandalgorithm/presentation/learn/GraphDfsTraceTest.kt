package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphDfsTraceTest {

    @Test
    fun stepsForDfsExploresDeepBeforeBacktracking() {
        val steps = GraphDfsTrace.stepsForDfs("A")
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf("A", "B", "D", "C", "E"), lastStep.visited)
    }
}
