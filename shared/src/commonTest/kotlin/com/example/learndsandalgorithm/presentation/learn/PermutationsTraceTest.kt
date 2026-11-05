package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PermutationsTraceTest {

    @Test
    fun createTraceGeneratesAll6PermutationsOf3Elements() {
        val steps = PermutationsTrace.createTrace(listOf(1, 2, 3))
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(PermutationAction.FINISHED, lastStep.action)
        assertEquals(6, lastStep.completedPermutations.size)

        // Check that all 6 generated permutations are distinct
        assertEquals(6, lastStep.completedPermutations.distinct().size)
    }
}
