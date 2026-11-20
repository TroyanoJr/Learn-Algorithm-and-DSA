package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BruteForceSceneLayoutTest {
    @Test
    fun fundamentalsAndPairLayoutsAreStableContainedAndNonOverlapping() {
        val sequences = listOf(
            BruteForceSceneBuilder.fundamentals(BruteForceFundamentalsTrace.stepsForSquareSearch()),
            BruteForceSceneBuilder.pairs(BruteForcePairProblemsTrace.stepsForFindPair())
        )
        sequences.forEach { frames ->
            val layout = BruteForceSceneLayout.calculate(frames)
            assertEquals(layout, BruteForceSceneLayout.calculate(frames))
            val bounds = listOf(layout.headerBounds, layout.titleBounds, layout.goalBounds, layout.statusBounds, layout.currentSumBounds) +
                layout.cellBounds.values + layout.primaryTextBounds.values + layout.secondaryTextBounds.values +
                layout.labelBounds.values + layout.indexBounds.values + layout.historyBounds.values
            assertContained(layout, bounds)
            assertNoOverlap(layout.cellBounds.values.toList())
            assertNoOverlap(layout.historyBounds.values.toList())
            assertTrue(layout.goalBounds.right <= layout.statusBounds.left)
            assertTrue(layout.titleBounds.bottom <= layout.goalBounds.top)
            layout.cellBounds.forEach { (index, cell) ->
                assertTrue(layout.primaryTextBounds.getValue(index).left >= cell.left)
                assertTrue(layout.primaryTextBounds.getValue(index).right <= cell.right)
                assertTrue(layout.secondaryTextBounds.getValue(index).bottom <= cell.bottom)
            }
        }
    }

    @Test
    fun patternCharactersAlignDirectlyBelowTheirTextCharactersAtEveryShift() {
        val frames = BruteForceSceneBuilder.stringMatching(NaiveStringMatchingTrace.stepsForNaiveMatch())
        val layout = BruteForceSceneLayout.calculate(frames)
        assertEquals(layout, BruteForceSceneLayout.calculate(frames))
        frames.filter { it.meta.alignmentStart != null }.forEach { frame ->
            val pattern = BruteForceSceneLayout.patternCellBounds(frame, layout)
            assertEquals(frame.meta.pattern.length, pattern.size)
            pattern.forEach { (offset, bounds) ->
                val textBounds = layout.cellBounds.getValue(frame.meta.alignmentStart!! + offset)
                assertEquals(textBounds.left, bounds.left)
                assertEquals(textBounds.width, bounds.width)
                assertTrue(bounds.top > layout.indexBounds.getValue(frame.meta.alignmentStart!! + offset).bottom)
            }
            assertContained(layout, pattern.values + BruteForceSceneLayout.patternIndexBounds(frame, layout).values)
        }
        val starts = frames.mapNotNull { it.meta.alignmentStart }.distinct()
        assertEquals(listOf(0, 1, 2), starts)
    }

    @Test
    fun comparisonMarkersStayBetweenReservedTextAndPatternRows() {
        val frames = BruteForceSceneBuilder.stringMatching(NaiveStringMatchingTrace.stepsForNaiveMatch())
        val layout = BruteForceSceneLayout.calculate(frames)
        frames.filter { it.meta.comparisonOffset != null }.forEach { frame ->
            val marker = assertNotNull(BruteForceSceneLayout.comparisonMarkerBounds(frame, layout))
            val textIndex = frame.meta.alignmentStart!! + frame.meta.comparisonOffset!!
            val textIndexBounds = layout.indexBounds.getValue(textIndex)
            val pattern = BruteForceSceneLayout.patternCellBounds(frame, layout).getValue(frame.meta.comparisonOffset!!)
            assertTrue(marker.top >= textIndexBounds.bottom)
            assertTrue(pattern.top - marker.bottom >= 4f)
            assertContained(layout, listOf(marker))
        }
    }

    @Test
    fun matchedTextSpanAndComparisonMarkerHaveCorrectSemanticsAndReservedGap() {
        val frames = BruteForceSceneBuilder.stringMatching(NaiveStringMatchingTrace.stepsForNaiveMatch())
        val layout = BruteForceSceneLayout.calculate(frames)
        val terminal = frames.last()
        assertEquals(listOf(0, 2), terminal.meta.foundMatches)
        val matchedTextSlots = terminal.meta.foundMatches.flatMap { start -> terminal.meta.pattern.indices.map { start + it } }.toSet()
        assertEquals(setOf(0, 1, 2, 3, 4), matchedTextSlots)
        frames.filter { it.meta.comparisonOffset != null }.forEach { frame ->
            val marker = BruteForceSceneLayout.comparisonMarkerBounds(frame, layout)!!
            val patternCell = BruteForceSceneLayout.patternCellBounds(frame, layout).getValue(frame.meta.comparisonOffset!!)
            assertTrue(marker.bottom + 4f <= patternCell.top)
            assertContained(layout, listOf(marker) + BruteForceSceneLayout.patternIndexBounds(frame, layout).values)
        }
    }

    @Test
    fun emptyLongerAndSingleCharacterInputsHaveSafeStableGeometry() {
        val traces = listOf(
            NaiveStringMatchingTrace.stepsForNaiveMatch("", ""),
            NaiveStringMatchingTrace.stepsForNaiveMatch("A", "A"),
            NaiveStringMatchingTrace.stepsForNaiveMatch("A", "LONG")
        )
        traces.forEach { trace ->
            val frames = BruteForceSceneBuilder.stringMatching(trace)
            val layout = BruteForceSceneLayout.calculate(frames)
            assertTrue(layout.requiredSceneWidth > 0f && layout.requiredSceneHeight > 0f)
            assertEquals(layout, BruteForceSceneLayout.calculate(frames))
            frames.forEach { frame -> assertContained(layout, BruteForceSceneLayout.patternCellBounds(frame, layout).values) }
        }
    }

    @Test
    fun initialFitContainsCompleteScenesOnCompactLandscape() {
        val layouts = listOf(
            BruteForceSceneLayout.calculate(BruteForceSceneBuilder.fundamentals(BruteForceFundamentalsTrace.stepsForSquareSearch())),
            BruteForceSceneLayout.calculate(BruteForceSceneBuilder.pairs(BruteForcePairProblemsTrace.stepsForFindPair())),
            BruteForceSceneLayout.calculate(BruteForceSceneBuilder.stringMatching(NaiveStringMatchingTrace.stepsForNaiveMatch()))
        )
        layouts.forEach { layout ->
            val scale = BruteForceSceneLayout.initialFitScale(layout, 360f, 190f)
            assertTrue(layout.requiredSceneWidth * scale <= 360.01f)
            assertTrue(layout.requiredSceneHeight * scale <= 190.01f)
        }
    }

    private fun assertContained(layout: BruteForceSceneLayoutResult, bounds: Collection<Rect2D>) {
        assertTrue(bounds.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
    }

    private fun assertNoOverlap(bounds: List<Rect2D>) {
        bounds.indices.forEach { first ->
            for (second in first + 1 until bounds.size) {
                val a = bounds[first]
                val b = bounds[second]
                assertTrue(a.right <= b.left || b.right <= a.left || a.bottom <= b.top || b.bottom <= a.top)
            }
        }
    }
}
