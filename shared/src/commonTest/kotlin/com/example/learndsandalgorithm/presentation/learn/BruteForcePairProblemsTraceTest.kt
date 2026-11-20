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

    @Test
    fun defaultDemoShowsFailureBeforeLaterSuccessAndEverySumIsCorrect() {
        val steps = BruteForcePairProblemsTrace.stepsForFindPair()
        val comparisons = steps.drop(1)
        assertTrue(comparisons.first().event == PairSearchEvent.EVALUATE_PAIR)
        assertTrue(comparisons.last().event == PairSearchEvent.PAIR_FOUND)
        assertTrue(comparisons.last().evaluatedPairs.dropLast(1).all { !it.isMatch })
        comparisons.forEach { step ->
            step.evaluatedPairs.forEach { pair ->
                assertEquals(pair.valI + pair.valJ, pair.sum)
                assertEquals(step.values[pair.i], pair.valI)
                assertEquals(step.values[pair.j], pair.valJ)
            }
        }
    }

    @Test
    fun noMatchChecksEveryDistinctPairAndTerminates() {
        val values = listOf(1, 2, 4)
        val steps = BruteForcePairProblemsTrace.stepsForFindPair(values, 99)
        assertEquals(PairSearchEvent.NO_PAIR_FOUND, steps.last().event)
        assertEquals(3, steps.last().evaluatedPairs.size)
        assertEquals(listOf(0 to 1, 0 to 2, 1 to 2), steps.last().evaluatedPairs.map { it.i to it.j })
        assertTrue(steps.last().isTerminal)
    }
}
