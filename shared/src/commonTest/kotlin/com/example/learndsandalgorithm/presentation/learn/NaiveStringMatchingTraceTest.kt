package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NaiveStringMatchingTraceTest {

    @Test
    fun stepsForNaiveMatchFindsAllPatternOccurrences() {
        val steps = NaiveStringMatchingTrace.stepsForNaiveMatch("ABABA", "ABA")
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(StringMatchEvent.MATCHING_COMPLETE, lastStep.event)
        assertEquals(listOf(0, 2), lastStep.foundMatches)
    }
}
