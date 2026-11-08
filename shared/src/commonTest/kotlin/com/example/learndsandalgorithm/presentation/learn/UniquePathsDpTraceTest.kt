package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UniquePathsDpTraceTest {

    @Test
    fun createTraceComputesUniquePathsIn3x3Grid() {
        val steps = UniquePathsDpTrace.createTrace(rows = 3, cols = 3)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(UniquePathsDpAction.FINISHED, lastStep.action)
        assertEquals(6L, lastStep.dpGrid[2][2])
    }
}
