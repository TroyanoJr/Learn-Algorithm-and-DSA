package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BruteForceFundamentalsTraceTest {

    @Test
    fun stepsForSquareSearchEvaluatesCandidatesAndFindsMatch() {
        val steps = BruteForceFundamentalsTrace.stepsForSquareSearch(1..5, target = 16)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(FundamentalsSearchEvent.SOLUTION_FOUND, lastStep.event)
        assertEquals(4, lastStep.foundSolution)
        assertEquals(4, lastStep.evaluatedCandidates.size)
    }

    @Test
    fun noMatchEvaluatesEntireRangeAndTerminatesCleanly() {
        val steps = BruteForceFundamentalsTrace.stepsForSquareSearch(1..3, target = 20)
        assertEquals(listOf(1, 2, 3), steps.last().evaluatedCandidates.map { it.candidate })
        assertEquals(FundamentalsSearchEvent.SEARCH_COMPLETE, steps.last().event)
        assertEquals(null, steps.last().foundSolution)
        assertTrue(steps.last().isTerminal)
    }
}
