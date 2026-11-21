package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IterationFundamentalsTraceTest {

    @Test
    fun createTraceComputesLoopSum1To4Correctly() {
        val steps = IterationFundamentalsTrace.createTrace(limit = 4)
        assertEquals(14, steps.size)
        assertEquals(
            listOf(
                IterationAction.INIT,
                IterationAction.CHECK_CONDITION, IterationAction.EXECUTE_BODY, IterationAction.UPDATE_STATE,
                IterationAction.CHECK_CONDITION, IterationAction.EXECUTE_BODY, IterationAction.UPDATE_STATE,
                IterationAction.CHECK_CONDITION, IterationAction.EXECUTE_BODY, IterationAction.UPDATE_STATE,
                IterationAction.CHECK_CONDITION, IterationAction.EXECUTE_BODY, IterationAction.UPDATE_STATE,
                IterationAction.TERMINATED
            ),
            steps.map { it.action }
        )
        assertEquals(listOf(1, 3, 6, 10), steps.filter { it.action == IterationAction.EXECUTE_BODY }.map { it.total })
        assertEquals(listOf(2, 3, 4, 5), steps.filter { it.action == IterationAction.UPDATE_STATE }.map { it.i })
        assertTrue(steps.filter { it.action == IterationAction.CHECK_CONDITION }.all { it.conditionSatisfied })

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(IterationAction.TERMINATED, lastStep.action)
        assertEquals(10, lastStep.total)
        assertEquals(5, lastStep.i)
        assertEquals(false, lastStep.conditionSatisfied)
    }
}
