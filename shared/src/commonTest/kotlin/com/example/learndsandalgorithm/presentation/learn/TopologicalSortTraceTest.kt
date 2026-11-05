package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopologicalSortTraceTest {

    @Test
    fun stepsForTopoSortSchedulesVerticesByInDegreeZero() {
        val steps = TopologicalSortTrace.stepsForTopoSort()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf("A", "B", "C", "D", "E"), lastStep.topoOrder)
    }
}
