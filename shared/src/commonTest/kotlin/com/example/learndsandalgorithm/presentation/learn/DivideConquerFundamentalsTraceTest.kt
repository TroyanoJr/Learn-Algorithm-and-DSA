package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DivideConquerFundamentalsTraceTest {

    @Test
    fun stepsForMaxFinderGeneratesAllThreePhases() {
        val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder(listOf(4, 2, 9, 7, 5))
        assertTrue(steps.isNotEmpty())

        // Initial Step
        val step0 = steps.first()
        assertEquals(DivideConquerPhase.DIVIDE, step0.phase)

        // Verify Divide phase steps
        val divideSteps = steps.filter { it.phase == DivideConquerPhase.DIVIDE }
        assertTrue(divideSteps.isNotEmpty())

        // Verify Conquer phase steps
        val conquerStep = steps.firstOrNull { it.phase == DivideConquerPhase.CONQUER }
        assertNotNull(conquerStep)

        // Verify Combine phase steps
        val combineSteps = steps.filter { it.phase == DivideConquerPhase.COMBINE }
        assertTrue(combineSteps.isNotEmpty())

        // Verify Terminal step
        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(9, lastStep.combinedValue)
    }
}
