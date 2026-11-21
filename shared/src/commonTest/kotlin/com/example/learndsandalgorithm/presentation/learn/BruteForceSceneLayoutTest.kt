package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
            val bounds = listOf(layout.headerBounds, layout.titleBounds, layout.goalBounds, layout.statusBounds, layout.evaluationBounds, layout.currentSumBounds) +
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
    fun generatedStringPatternAndComparisonBoundsStayInsideTheScene() {
        val traces = listOf(
            NaiveStringMatchingTrace.stepsForNaiveMatch("ABABA", "ABA"),
            NaiveStringMatchingTrace.stepsForNaiveMatch("AAAA", "AA"),
            NaiveStringMatchingTrace.stepsForNaiveMatch("AB", "A"),
            NaiveStringMatchingTrace.stepsForNaiveMatch("", ""),
            NaiveStringMatchingTrace.stepsForNaiveMatch("ABC", ""),
            NaiveStringMatchingTrace.stepsForNaiveMatch("AB", "ABCD"),
            NaiveStringMatchingTrace.stepsForNaiveMatch("AAAA", "B")
        )

        traces.forEach { trace ->
            val frames = BruteForceSceneBuilder.stringMatching(trace)
            val layout = BruteForceSceneLayout.calculate(frames)
            frames.forEach { frame ->
                val activePatternCells = BruteForceSceneLayout.patternCellBounds(frame, layout)
                val activePatternIndices = BruteForceSceneLayout.patternIndexBounds(frame, layout)
                val expectedActiveOffsets = if (frame.meta.alignmentStart == null) emptySet() else frame.meta.pattern.indices.toSet()
                assertEquals(expectedActiveOffsets, activePatternCells.keys)
                assertEquals(expectedActiveOffsets, activePatternIndices.keys)
                assertContained(layout, activePatternCells.values + activePatternIndices.values)

                val marker = BruteForceSceneLayout.comparisonMarkerBounds(frame, layout)
                if (frame.meta.comparisonOffset == null) {
                    assertNull(marker)
                } else {
                    assertNotNull(marker)
                    assertTrue(frame.meta.alignmentStart!! + frame.meta.comparisonOffset!! in frame.meta.text.indices)
                    assertTrue(frame.meta.comparisonOffset in frame.meta.pattern.indices)
                    assertContained(layout, listOf(marker))
                }

                if (frame.meta.eventName == StringMatchEvent.MATCHING_COMPLETE.name) {
                    assertNull(frame.meta.alignmentStart)
                    assertNull(frame.meta.comparisonOffset)
                    assertNull(frame.meta.comparisonMatches)
                    assertNull(frame.meta.mismatchOffset)
                    assertTrue(!frame.meta.fullAlignmentMatch)
                    assertTrue(activePatternCells.isEmpty())
                    assertNull(marker)

                    val presentation = assertNotNull(BruteForceSceneLayout.terminalStringMatchPresentation(frame, layout))
                    assertEquals(frame.meta.pattern.indices.toList(), presentation.patternCells.map { it.offset })
                    assertEquals(frame.meta.pattern.toList(), presentation.patternCells.map { it.character })
                    assertEquals(
                        if (frame.meta.foundMatches.isEmpty()) "NO MATCH" else "FOUND AT [${frame.meta.foundMatches.joinToString(", ")}]",
                        presentation.resultText
                    )
                    val presentationBounds = listOf(presentation.patternLabelBounds) + presentation.patternCells.flatMap { listOf(it.cellBounds, it.indexBounds) }
                    assertContained(layout, presentationBounds)
                    assertNoOverlap(
                        (layout.cellBounds.values + layout.indexBounds.values + presentationBounds).toList()
                    )
                } else {
                    assertNull(BruteForceSceneLayout.terminalStringMatchPresentation(frame, layout))
                }
            }
        }
    }

    @Test
    fun terminalPatternPresentationKeepsAllMatchesAndHandlesEmptyAndLongPatterns() {
        fun terminal(text: String, pattern: String): Pair<BruteForceSceneFrame, BruteForceSceneLayoutResult> {
            val frames = BruteForceSceneBuilder.stringMatching(NaiveStringMatchingTrace.stepsForNaiveMatch(text, pattern))
            return frames.last() to BruteForceSceneLayout.calculate(frames)
        }

        val (overlapping, overlappingLayout) = terminal("ABABA", "ABA")
        val overlappingPresentation = assertNotNull(BruteForceSceneLayout.terminalStringMatchPresentation(overlapping, overlappingLayout))
        assertEquals(520f, overlappingLayout.requiredSceneWidth)
        assertEquals(252f, overlappingLayout.requiredSceneHeight)
        assertEquals(listOf(0, 2), overlapping.meta.foundMatches)
        assertEquals("FOUND AT [0, 2]", overlappingPresentation.resultText)
        assertEquals("ABA".toList(), overlappingPresentation.patternCells.map { it.character })
        assertEquals(listOf(0, 1, 2), overlappingPresentation.patternCells.map { it.offset })
        assertEquals(setOf(0, 1, 2, 3, 4), overlappingPresentation.matchedTextIndices)
        assertTrue(BruteForceSceneLayout.patternCellBounds(overlapping, overlappingLayout).isEmpty())
        assertNull(BruteForceSceneLayout.comparisonMarkerBounds(overlapping, overlappingLayout))

        val (multipleOverlaps, multipleOverlapsLayout) = terminal("AAAA", "AA")
        val multipleOverlapsPresentation = assertNotNull(BruteForceSceneLayout.terminalStringMatchPresentation(multipleOverlaps, multipleOverlapsLayout))
        assertEquals(listOf(0, 1, 2), multipleOverlaps.meta.foundMatches)
        assertEquals("FOUND AT [0, 1, 2]", multipleOverlapsPresentation.resultText)
        assertEquals(setOf(0, 1, 2, 3), multipleOverlapsPresentation.matchedTextIndices)
        assertEquals("AA".toList(), multipleOverlapsPresentation.patternCells.map { it.character })

        val (noMatch, noMatchLayout) = terminal("AAAA", "B")
        val noMatchPresentation = assertNotNull(BruteForceSceneLayout.terminalStringMatchPresentation(noMatch, noMatchLayout))
        assertEquals(emptyList(), noMatch.meta.foundMatches)
        assertEquals("NO MATCH", noMatchPresentation.resultText)
        assertEquals(listOf('B'), noMatchPresentation.patternCells.map { it.character })
        assertTrue(noMatchPresentation.matchedTextIndices.isEmpty())

        val (longPattern, longPatternLayout) = terminal("AB", "ABCD")
        val longPatternPresentation = assertNotNull(BruteForceSceneLayout.terminalStringMatchPresentation(longPattern, longPatternLayout))
        assertEquals("NO MATCH", longPatternPresentation.resultText)
        assertEquals("ABCD".toList(), longPatternPresentation.patternCells.map { it.character })
        assertNull(longPattern.meta.alignmentStart)
        assertNull(BruteForceSceneLayout.comparisonMarkerBounds(longPattern, longPatternLayout))

        val (emptyPattern, emptyPatternLayout) = terminal("ABC", "")
        val emptyPatternPresentation = assertNotNull(BruteForceSceneLayout.terminalStringMatchPresentation(emptyPattern, emptyPatternLayout))
        assertEquals(listOf(0, 1, 2, 3), emptyPattern.meta.foundMatches)
        assertEquals("FOUND AT [0, 1, 2, 3]", emptyPatternPresentation.resultText)
        assertTrue(emptyPatternPresentation.patternCells.isEmpty())

        val (emptyInput, emptyInputLayout) = terminal("", "")
        val emptyInputPresentation = assertNotNull(BruteForceSceneLayout.terminalStringMatchPresentation(emptyInput, emptyInputLayout))
        assertEquals(listOf(0), emptyInput.meta.foundMatches)
        assertEquals("FOUND AT [0]", emptyInputPresentation.resultText)
        assertTrue(emptyInputPresentation.patternCells.isEmpty())
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

    @Test
    fun pairEvaluationPanelAndLongSearchHistoryHaveReservedNonOverlappingGeometry() {
        val trace = BruteForcePairProblemsTrace.stepsForFindPair(listOf(1, 2, 3, 4, 5, 6, 7, 8), 100)
        val frames = BruteForceSceneBuilder.pairs(trace)
        val layout = BruteForceSceneLayout.calculate(frames)
        assertEquals(layout, BruteForceSceneLayout.calculate(frames))
        assertEquals(28, layout.historyBounds.size)
        assertEquals(2, pairHistoryColumnCount(frames.flatMap { it.meta.evaluatedPairs }, layout.requiredSceneWidth))
        assertEquals(936f, layout.requiredSceneHeight)
        assertContained(layout, listOf(layout.evaluationBounds, layout.currentSumBounds) + layout.cellBounds.values + layout.indexBounds.values + layout.historyBounds.values)
        assertNoOverlap(listOf(layout.evaluationBounds, layout.currentSumBounds) + layout.cellBounds.values + layout.indexBounds.values + layout.historyBounds.values)
        assertEquals(40f, layout.historyBounds.getValue(0).height)
        assertEquals(layout.historyBounds.getValue(0).top, layout.historyBounds.getValue(1).top)
        assertTrue(layout.historyBounds.getValue(1).left > layout.historyBounds.getValue(0).left)
        assertEquals(layout.historyBounds.getValue(0).top + 46f, layout.historyBounds.getValue(2).top)
        assertEquals(layout.historyBounds.getValue(1).top + 46f, layout.historyBounds.getValue(3).top)
        assertTrue(layout.historyBounds.values.all { it.width == (layout.requiredSceneWidth - 24f - 6f) / 2f })
        frames.forEach { frame ->
            assertEquals(layout, BruteForceSceneLayout.calculate(frames))
            assertEquals(frame.meta.evaluatedPairs.size, frame.meta.evaluatedPairs.indices.count { it in layout.historyBounds })
        }
        val scale = BruteForceSceneLayout.initialFitScale(layout, 360f, 190f)
        assertTrue(layout.requiredSceneWidth * scale <= 360.01f)
        assertTrue(layout.requiredSceneHeight * scale <= 190.01f)
    }

    @Test
    fun longArithmeticSelectsStableWideHistoryFallback() {
        val trace = BruteForcePairProblemsTrace.stepsForFindPair(listOf(Int.MIN_VALUE, -2_147_483_647, 0), 1_234_567_890)
        val frames = BruteForceSceneBuilder.pairs(trace)
        val pairs = frames.flatMap { it.meta.evaluatedPairs }
        assertTrue(pairs.isNotEmpty())
        assertEquals(1, pairHistoryColumnCount(pairs, 420f))

        val layout = BruteForceSceneLayout.calculate(frames)
        assertTrue(layout.historyBounds.values.all { it.width == 396f })
        assertEquals(layout.historyBounds.getValue(0).top + 46f, layout.historyBounds.getValue(1).top)
        assertEquals(layout.historyBounds.getValue(1).top + 46f, layout.historyBounds.getValue(2).top)
        assertNoOverlap(layout.historyBounds.values.toList())
        assertContained(layout, layout.historyBounds.values + listOf(layout.evaluationBounds, layout.currentSumBounds))
        assertEquals(layout, BruteForceSceneLayout.calculate(frames))
    }

    @Test
    fun compactLessonsUseLessReservedCanvasSpaceWithoutChangingStringGeometry() {
        val fundamentals = BruteForceSceneLayout.calculate(BruteForceSceneBuilder.fundamentals(BruteForceFundamentalsTrace.stepsForSquareSearch()))
        val pairs = BruteForceSceneLayout.calculate(BruteForceSceneBuilder.pairs(BruteForcePairProblemsTrace.stepsForFindPair()))
        val strings = BruteForceSceneLayout.calculate(BruteForceSceneBuilder.stringMatching(NaiveStringMatchingTrace.stepsForNaiveMatch()))
        assertTrue(fundamentals.requiredSceneWidth < 520f)
        assertEquals(208f, fundamentals.requiredSceneHeight)
        assertEquals(Rect2D(12f, 148f, fundamentals.requiredSceneWidth - 24f, 48f), fundamentals.evaluationBounds)
        assertContained(fundamentals, listOf(fundamentals.evaluationBounds))
        assertNoOverlap(listOf(fundamentals.evaluationBounds) + fundamentals.cellBounds.values)
        assertNoOverlap(listOf(fundamentals.evaluationBounds) + fundamentals.labelBounds.values)
        assertTrue(fundamentals.indexBounds.values.all { it.width == 0f && it.height == 0f })
        assertTrue(pairs.requiredSceneWidth < 520f)
        assertEquals(420f, pairs.requiredSceneWidth)
        assertEquals(384f, pairs.requiredSceneHeight)
        assertEquals(Rect2D(12f, 166f, 396f, 80f), pairs.evaluationBounds)
        assertEquals(Rect2D(91f, 88f, 52f, 46f), pairs.cellBounds.getValue(0))
        assertEquals(Rect2D(12f, 254f, 396f, 22f), pairs.currentSumBounds)
        assertEquals(2, pairHistoryColumnCount(BruteForceSceneBuilder.pairs(BruteForcePairProblemsTrace.stepsForFindPair()).flatMap { it.meta.evaluatedPairs }, pairs.requiredSceneWidth))
        assertEquals(pairs.historyBounds.getValue(0).top, pairs.historyBounds.getValue(1).top)
        assertEquals(pairs.historyBounds.getValue(0).top + 46f, pairs.historyBounds.getValue(2).top)
        assertTrue(pairs.indexBounds.values.all { it.width > 0f && it.height > 0f })
        assertContained(pairs, listOf(pairs.currentSumBounds) + pairs.historyBounds.values)
        assertNoOverlap(pairs.historyBounds.values.toList())
        val lastHistoryBottom = pairs.historyBounds.values.maxOf { it.bottom }
        assertTrue(pairs.requiredSceneHeight - lastHistoryBottom >= 12f)
        assertEquals(520f, strings.requiredSceneWidth)
        assertEquals(252f, strings.requiredSceneHeight)
        assertEquals(Rect2D(0f, 0f, 0f, 0f), strings.evaluationBounds)
        assertEquals(Rect2D(110f, 88f, 52f, 46f), strings.cellBounds.getValue(0))
        assertTrue(strings.indexBounds.values.all { it.width > 0f && it.height > 0f })
        listOf(fundamentals, pairs, strings).forEach { layout ->
            val bounds = listOf(layout.headerBounds, layout.titleBounds, layout.goalBounds, layout.statusBounds, layout.currentSumBounds) +
                layout.cellBounds.values + layout.primaryTextBounds.values + layout.secondaryTextBounds.values +
                layout.labelBounds.values + layout.secondaryLabelBounds.values + layout.indexBounds.values + layout.historyBounds.values
            assertContained(layout, bounds)
            assertNoOverlap(layout.historyBounds.values.toList())
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
