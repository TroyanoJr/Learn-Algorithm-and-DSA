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

    @Test
    fun emptyPatternMatchesEveryBoundaryWithoutInvalidOffsets() {
        val steps = NaiveStringMatchingTrace.stepsForNaiveMatch("ABC", "")
        assertEquals(listOf(0, 1, 2, 3), steps.last().foundMatches)
        assertTrue(steps.all { it.currentOffset == null })
        assertTrue(steps.last().isTerminal)
    }

    @Test
    fun longerPatternAndNoMatchInputsTerminateWithoutInvalidComparisons() {
        val longer = NaiveStringMatchingTrace.stepsForNaiveMatch("AB", "ABCD")
        assertEquals(2, longer.size)
        assertTrue(longer.none { it.currentStart != null || it.currentOffset != null })
        assertEquals(emptyList(), longer.last().foundMatches)
        assertTrue(longer.first().explanation.contains("no valid alignments"))

        val noMatch = NaiveStringMatchingTrace.stepsForNaiveMatch("AAAA", "B")
        assertEquals(emptyList(), noMatch.last().foundMatches)
        assertEquals(StringMatchEvent.MATCHING_COMPLETE, noMatch.last().event)
        assertTrue(noMatch.last().isTerminal)
    }
}
