package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BruteForceSceneBuilderTest {
    @Test
    fun rejectedCurrentCandidateEntityAndDisplayLabelStayRejected() {
        val frame = BruteForceSceneBuilder.fundamentals(
            BruteForceFundamentalsTrace.stepsForSquareSearch()
        ).first { frame -> frame.meta.evaluations.any { it.candidate == frame.meta.currentCandidate && !it.isMatch } }
        val candidate = frame.meta.currentCandidate!!
        assertEquals(EntityVisualState.VISITED, frame.entities().first { it.dataValue == candidate.toString() }.visualState)
        assertEquals("REJECT", fundamentalsDisplayLabel(false, true, true))
        assertEquals("MATCH", fundamentalsDisplayLabel(true, true, true))
        assertEquals("TEST", fundamentalsDisplayLabel(false, false, true))
        assertEquals("", fundamentalsDisplayLabel(false, false, false))
    }

    @Test
    fun fundamentalsFramesPreserveTraceAndStableCandidateIdentities() {
        val trace = BruteForceFundamentalsTrace.stepsForSquareSearch()
        val frames = BruteForceSceneBuilder.fundamentals(trace)
        assertEquals(trace.size, frames.size)
        assertEquals(trace.map { it.explanation }, frames.map { it.meta.explanation })
        assertEquals(trace.map { it.badgeText }, frames.map { it.meta.badgeText })
        assertEquals(trace.indices.toList(), frames.map { it.scene.stepIndex })
        frames.forEachIndexed { index, frame ->
            assertEquals(trace[index].evaluatedCandidates, frame.meta.evaluations)
            assertEquals((1..5).map { "fundamentals:candidate:$it" }, frame.entities().map { it.elementId })
        }
        assertEquals(EntityVisualState.TARGET, frames.last().entities().first { it.dataValue == "4" }.visualState)

        val noMatchTrace = BruteForceFundamentalsTrace.stepsForSquareSearch(1..2, 50)
        val noMatch = BruteForceSceneBuilder.fundamentals(noMatchTrace).last()
        assertTrue(noMatch.meta.isTerminal)
        assertNull(noMatch.meta.foundSolution)
        assertTrue(noMatch.entities().all { it.visualState == EntityVisualState.VISITED })
    }

    @Test
    fun pairFramesPreservePointersHistorySumsAndFailureBeforeSuccess() {
        val trace = BruteForcePairProblemsTrace.stepsForFindPair()
        val frames = BruteForceSceneBuilder.pairs(trace)
        assertEquals(trace.size, frames.size)
        assertEquals(trace.map { it.explanation }, frames.map { it.meta.explanation })
        assertEquals(PairSearchEvent.EVALUATE_PAIR.name, frames[1].meta.eventName)
        assertEquals(PairSearchEvent.PAIR_FOUND.name, frames.last().meta.eventName)
        assertTrue(frames[1].meta.evaluatedPairs.single().isMatch.not())
        assertTrue(frames.last().meta.evaluatedPairs.last().isMatch)
        frames.forEachIndexed { index, frame ->
            assertEquals(trace[index].currentI, frame.meta.currentI)
            assertEquals(trace[index].currentJ, frame.meta.currentJ)
            assertEquals(trace[index].currentSum, frame.meta.currentSum)
            assertEquals(trace[index].evaluatedPairs, frame.meta.evaluatedPairs)
            assertEquals(trace[index].values.indices.map { "pair:item:$it" }, frame.entities().map { it.elementId })
        }
    }

    @Test
    fun stringFramesPreserveAlignmentComparisonsOverlapsAndTerminalState() {
        val trace = NaiveStringMatchingTrace.stepsForNaiveMatch()
        val frames = BruteForceSceneBuilder.stringMatching(trace)
        assertEquals(trace.size, frames.size)
        assertEquals(trace.map { it.explanation }, frames.map { it.meta.explanation })
        assertEquals(listOf(0, 2), frames.last().meta.foundMatches)
        assertTrue(frames.last().meta.isTerminal)
        val mismatch = frames.first { it.meta.mismatchOffset != null }
        assertEquals(false, mismatch.meta.comparisonMatches)
        assertEquals(mismatch.meta.comparisonOffset, mismatch.meta.mismatchOffset)
        val fullMatches = frames.filter { it.meta.fullAlignmentMatch }
        assertEquals(listOf(0, 2), fullMatches.map { it.meta.alignmentStart })
        assertTrue(fullMatches.all { it.meta.matchedOffsets == setOf(0, 1, 2) })
        assertTrue(frames.first().meta.alignmentStart == 0 && frames.first().meta.comparisonOffset == null)
        assertEquals(listOf(0, 1, 2), frames.mapNotNull { it.meta.alignmentStart }.distinct())
        assertEquals(listOf(0, 2), frames.last().meta.foundMatches)
        frames.forEach { frame ->
            val textIds = frame.scene.subScenes.first().entities.map { it.elementId }
            assertEquals(frame.meta.text.indices.map { "text:$it" }, textIds)
            frame.scene.subScenes.getOrNull(1)?.entities.orEmpty().forEachIndexed { offset, entity ->
                assertEquals("pattern:$offset", entity.elementId)
                assertNotEquals("text:$offset", entity.elementId)
            }
        }
    }

    @Test
    fun stringEdgeCasesProduceSafeFramesWithoutInventedAlignment() {
        val cases = listOf(
            NaiveStringMatchingTrace.stepsForNaiveMatch("ABC", ""),
            NaiveStringMatchingTrace.stepsForNaiveMatch("AB", "ABCD"),
            NaiveStringMatchingTrace.stepsForNaiveMatch("AAAA", "B")
        )
        cases.forEach { trace ->
            val frames = BruteForceSceneBuilder.stringMatching(trace)
            assertEquals(trace.size, frames.size)
            assertTrue(frames.last().meta.isTerminal)
            assertTrue(frames.all { frame ->
                frame.meta.alignmentStart == null || frame.meta.alignmentStart in 0..(frame.meta.text.length - frame.meta.pattern.length).coerceAtLeast(0)
            })
        }
        assertTrue(BruteForceSceneBuilder.stringMatching(cases[0]).all { it.meta.alignmentStart == null })
        assertTrue(BruteForceSceneBuilder.stringMatching(cases[1]).all { it.meta.alignmentStart == null })
    }

    private fun BruteForceSceneFrame.entities(): List<VisualEntity> = scene.subScenes.first().entities
}
