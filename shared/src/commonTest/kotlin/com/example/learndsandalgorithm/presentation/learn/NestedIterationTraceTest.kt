package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NestedIterationTraceTest {

    @Test
    fun createTraceGeneratesAll6CoordinatePairs() {
        val steps = NestedIterationTrace.createTrace(outerLimit = 2, innerLimit = 3)
        assertEquals(10, steps.size)
        assertEquals(
            listOf(
                NestedIterationAction.INIT,
                NestedIterationAction.OUTER_PASS,
                NestedIterationAction.INNER_PASS, NestedIterationAction.INNER_PASS, NestedIterationAction.INNER_PASS,
                NestedIterationAction.OUTER_PASS,
                NestedIterationAction.INNER_PASS, NestedIterationAction.INNER_PASS, NestedIterationAction.INNER_PASS,
                NestedIterationAction.FINISHED
            ),
            steps.map { it.action }
        )
        assertTrue(steps.none { it.action == NestedIterationAction.INNER_RESET })
        assertEquals(listOf(1, 2), steps.filter { it.action == NestedIterationAction.OUTER_PASS }.map { it.i })
        assertTrue(steps.filter { it.action == NestedIterationAction.OUTER_PASS }.all { it.j == 1 })

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(NestedIterationAction.FINISHED, lastStep.action)
        assertEquals(6, lastStep.totalCount)

        val expected = listOf(
            Pair(1, 1), Pair(1, 2), Pair(1, 3),
            Pair(2, 1), Pair(2, 2), Pair(2, 3)
        )
        assertEquals(expected, lastStep.printedPairs)
        assertEquals(expected, steps.filter { it.action == NestedIterationAction.INNER_PASS }.map { it.i to it.j })
    }
}
