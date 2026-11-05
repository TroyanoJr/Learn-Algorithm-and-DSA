package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NQueensTraceTest {

    @Test
    fun createTraceSolves4QueensAndFindsExactly2Solutions() {
        val steps = NQueensTrace.createTrace(n = 4)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(NQueensAction.FINISHED, lastStep.action)
        assertEquals(2, lastStep.solutions.size)

        // Verify the two solutions for 4-Queens: [1, 3, 0, 2] and [2, 0, 3, 1]
        val expected = listOf(
            listOf(1, 3, 0, 2),
            listOf(2, 0, 3, 1)
        )
        assertEquals(expected, lastStep.solutions)
    }
}
