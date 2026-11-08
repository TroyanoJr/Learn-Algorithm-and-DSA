package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FractionalKnapsackTraceTest {

    @Test
    fun createTraceComputesCorrectMaxFractionalValue() {
        val steps = FractionalKnapsackTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(FractionalKnapsackStatus.FINISHED, lastStep.status)
        assertEquals(240.0, lastStep.totalValue)
        assertEquals(0.0, lastStep.remainingCapacity)
        assertEquals(3, lastStep.selections.size)

        // Selections check: A (100%), B (100%), C (66.67%)
        assertEquals("A", lastStep.selections[0].item.name)
        assertEquals(1.0, lastStep.selections[0].fraction)

        assertEquals("B", lastStep.selections[1].item.name)
        assertEquals(1.0, lastStep.selections[1].fraction)

        assertEquals("C", lastStep.selections[2].item.name)
        assertEquals(20.0 / 30.0, lastStep.selections[2].fraction)
    }
}
