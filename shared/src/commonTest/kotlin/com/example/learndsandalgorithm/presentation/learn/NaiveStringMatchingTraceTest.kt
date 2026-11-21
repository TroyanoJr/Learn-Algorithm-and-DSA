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
        assertEquals(
            listOf("Worst-case: O(N * M)", "Empty pattern: enumerate N + 1 boundaries in O(N) time"),
            naiveStringMatchingComplexityLines("")
        )
        assertEquals(listOf("Worst-case: O(N * M)"), naiveStringMatchingComplexityLines("A"))
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

    @Test
    fun comparisonsCoverValidAlignmentsAndMapTextAndPatternOffsets() {
        val steps = NaiveStringMatchingTrace.stepsForNaiveMatch("ABABA", "ABA")
        val comparisons = steps.filter { it.event == StringMatchEvent.COMPARE_CHAR }

        assertEquals(listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 2 to 0, 2 to 1, 2 to 2), comparisons.map { it.currentStart!! to it.currentOffset!! })
        comparisons.forEach { step ->
            val start = step.currentStart!!
            val offset = step.currentOffset!!
            assertTrue(start in 0..(step.text.length - step.pattern.length))
            assertTrue(offset in step.pattern.indices)
            assertTrue(step.explanation.contains("text[${start + offset}] ('${step.text[start + offset]}') with pattern[$offset] ('${step.pattern[offset]}')"))
            assertEquals(step.isCharMatch, step.text[start + offset] == step.pattern[offset])
        }
    }

    @Test
    fun lastAlignmentMismatchDoesNotDescribeAnotherShift() {
        val steps = NaiveStringMatchingTrace.stepsForNaiveMatch("AB", "A")
        assertEquals(
            listOf(
                StringMatchEvent.START,
                StringMatchEvent.COMPARE_CHAR,
                StringMatchEvent.ALIGNMENT_MATCH,
                StringMatchEvent.COMPARE_CHAR,
                StringMatchEvent.ALIGNMENT_MISMATCH,
                StringMatchEvent.MATCHING_COMPLETE
            ),
            steps.map { it.event }
        )

        val mismatch = steps.single { it.event == StringMatchEvent.ALIGNMENT_MISMATCH }
        assertEquals(1, mismatch.currentStart)
        assertEquals(0, mismatch.currentOffset)
        assertEquals(false, mismatch.isCharMatch)
        assertTrue(mismatch.explanation.contains("text[1] ('B') != pattern[0] ('A')"))
        assertTrue(mismatch.explanation.contains("no further valid alignments", ignoreCase = true))
        assertTrue(!mismatch.explanation.contains("Shift pattern"))
    }
}
