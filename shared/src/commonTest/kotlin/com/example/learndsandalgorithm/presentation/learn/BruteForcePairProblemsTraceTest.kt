package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BruteForcePairProblemsTraceTest {

    @Test
    fun stepsForFindPairFindsIndicesOfTargetSum() {
        val steps = BruteForcePairProblemsTrace.stepsForFindPair(listOf(2, 7, 11, 15), target = 9)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(PairSearchEvent.PAIR_FOUND, lastStep.event)
        assertEquals(Pair(0, 1), lastStep.foundPair)
    }
}
