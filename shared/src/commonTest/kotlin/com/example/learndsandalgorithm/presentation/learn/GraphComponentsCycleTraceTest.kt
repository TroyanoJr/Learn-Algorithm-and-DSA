package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphComponentsCycleTraceTest {

    @Test
    fun stepsForConnectedComponentsFindsAllThreeComponents() {
        val steps = GraphComponentsCycleTrace.stepsForTopic(ComponentCycleTopic.CONNECTED_COMPONENTS)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(3, lastStep.components.size)
        assertEquals(listOf("A", "B", "C"), lastStep.components[0])
        assertEquals(listOf("D", "E"), lastStep.components[1])
        assertEquals(listOf("F"), lastStep.components[2])
    }

    @Test
    fun stepsForUndirectedCycleDetectsCycleInTriangle() {
        val steps = GraphComponentsCycleTrace.stepsForTopic(ComponentCycleTopic.UNDIRECTED_CYCLE)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertTrue(lastStep.cycleDetected)
    }
}
