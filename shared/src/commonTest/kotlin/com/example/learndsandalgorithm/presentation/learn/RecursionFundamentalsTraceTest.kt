package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecursionFundamentalsTraceTest {

    @Test
    fun createTraceComputesSumTo3Recursively() {
        val steps = RecursionFundamentalsTrace.createTrace(inputN = 3)
        assertEquals(9, steps.size)
        assertEquals(
            listOf(
                RecursionAction.INIT,
                RecursionAction.CALL_DESCENT, RecursionAction.CALL_DESCENT, RecursionAction.CALL_DESCENT,
                RecursionAction.BASE_CASE_REACHED,
                RecursionAction.RETURN_ASCENT, RecursionAction.RETURN_ASCENT, RecursionAction.RETURN_ASCENT,
                RecursionAction.FINISHED
            ),
            steps.map { it.action }
        )
        assertEquals(listOf(3, 2, 1), steps.filter { it.action == RecursionAction.CALL_DESCENT }.map { it.currentN })
        assertEquals(
            listOf(0, 1, 3, 6),
            steps.filter { it.action == RecursionAction.BASE_CASE_REACHED || it.action == RecursionAction.RETURN_ASCENT }
                .map { it.currentReturnValue }
        )

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(RecursionAction.FINISHED, lastStep.action)
        assertEquals(6, lastStep.currentReturnValue)

        // Verify base case reached step
        val baseCaseStep = steps.find { it.action == RecursionAction.BASE_CASE_REACHED }
        assertTrue(baseCaseStep != null)
        assertEquals(0, baseCaseStep.currentN)
        assertEquals(listOf(3, 2, 1, 0), baseCaseStep.callStack)
        steps.filter { it.action == RecursionAction.RETURN_ASCENT }.forEach { returning ->
            assertEquals(returning.currentN, returning.callStack.last())
            assertTrue(returning.returnValues.any { it.first == returning.currentN && it.second == returning.currentReturnValue })
            assertTrue(returning.explanation.contains("is returning its computed value"))
            assertTrue(returning.explanation.contains("will be removed after this return step"))
            assertTrue(!returning.explanation.contains("Popped frame"))
        }
        assertEquals(emptyList(), lastStep.callStack)
        assertEquals(listOf(0 to 0, 1 to 1, 2 to 3, 3 to 6), lastStep.returnValues)
    }
}
