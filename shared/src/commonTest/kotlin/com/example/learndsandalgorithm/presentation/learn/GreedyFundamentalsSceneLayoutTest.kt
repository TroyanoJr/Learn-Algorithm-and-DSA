package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreedyFundamentalsSceneLayoutTest {
    private val expectedDefaultStates = listOf(
        listOf("PENDING", "PENDING", "PENDING", "PENDING"),
        listOf("PENDING", "PENDING", "PENDING", "PENDING"),
        listOf("TESTING", "PENDING", "PENDING", "PENDING"),
        listOf("CHOSEN", "PENDING", "PENDING", "PENDING"),
        listOf("CHOSEN", "TESTING", "PENDING", "PENDING"),
        listOf("CHOSEN", "CHOSEN", "PENDING", "PENDING"),
        listOf("CHOSEN", "CHOSEN", "TESTING", "PENDING"),
        listOf("CHOSEN", "CHOSEN", "CHOSEN", "PENDING"),
        listOf("CHOSEN", "CHOSEN", "CHOSEN", "REJECTED"),
        listOf("CHOSEN", "CHOSEN", "CHOSEN", "REJECTED")
    )

    @Test
    fun allTenSnapshotsPreserveBudgetContentAndExactCandidateProgression() {
        val steps = GreedyFundamentalsTrace.createTrace()
        val frames = GreedyFundamentalsSceneSequenceBuilder.build(steps)

        assertEquals(10, frames.size)
        assertEquals(listOf(10, 10, 10, 8, 8, 5, 5, 1, 1, 1), frames.map { it.step.remainingBudget })
        assertEquals(listOf(0, 0, 0, 2, 2, 5, 5, 9, 9, 9), frames.map { it.usedBudget })
        assertEquals(listOf(1f, 1f, 1f, .8f, .8f, .5f, .5f, .1f, .1f, .1f), frames.map { it.remainingFraction })
        assertEquals(expectedDefaultStates, frames.map { frame -> frame.candidateStates.map { it.name } })

        frames.forEach { frame ->
            assertEquals(frame.step.totalBudget - frame.step.remainingBudget, frame.usedBudget)
            assertEquals("Selected Stops: [${frame.step.chosenDistances.joinToString()}]", frame.selectedLabel)
            assertEquals(frame.step.chosenDistances.size, frame.selectedCount)
        }
        assertEquals(listOf(2, 3, 4), frames.last().step.chosenDistances)
        assertEquals(3, frames.last().step.chosenDistances.size)
    }

    @Test
    fun fillWidthTracksEveryFrameInForwardBackwardAndResetOrder() {
        val frames = GreedyFundamentalsSceneSequenceBuilder.build(GreedyFundamentalsTrace.createTrace())
        val trackWidth = GreedyFundamentalsSceneLayout.calculate(frames.first().step.sortedDistances, 360f).progressTrack.width
        val expectedForward = listOf(1f, 1f, 1f, .8f, .8f, .5f, .5f, .1f, .1f, .1f).map { trackWidth * it }

        val forwardWidths = frames.map { greedyFundamentalsProgressFillWidth(trackWidth, it.remainingFraction) }
        val backwardWidths = frames.asReversed().map { greedyFundamentalsProgressFillWidth(trackWidth, it.remainingFraction) }
        val resetWidth = greedyFundamentalsProgressFillWidth(trackWidth, frames.first().remainingFraction)

        assertEquals(expectedForward, forwardWidths)
        assertEquals(expectedForward.reversed(), backwardWidths)
        assertEquals(trackWidth, resetWidth)
        assertEquals(0f, greedyFundamentalsProgressFillWidth(trackWidth, -1f))
        assertEquals(trackWidth, greedyFundamentalsProgressFillWidth(trackWidth, 2f))
    }

    @Test
    fun playbackPreviousNextAndResetSelectTheMatchingFillFrame() {
        val frames = GreedyFundamentalsSceneSequenceBuilder.build(GreedyFundamentalsTrace.createTrace())
        val trackWidth = GreedyFundamentalsSceneLayout.calculate(frames.first().step.sortedDistances, 360f).progressTrack.width
        val playback = VisualizerPlaybackState(initialStep = 0, stepCount = frames.size)
        fun currentFillWidth() = greedyFundamentalsProgressFillWidth(
            trackWidth,
            frames[playback.safeStepIndex].remainingFraction
        )

        assertEquals(trackWidth, currentFillWidth())
        repeat(3) { playback.next() }
        assertEquals(3, playback.safeStepIndex)
        assertEquals(trackWidth * .8f, currentFillWidth())
        playback.previous()
        assertEquals(2, playback.safeStepIndex)
        assertEquals(trackWidth, currentFillWidth())
        repeat(frames.size) { playback.next() }
        assertEquals(trackWidth * .1f, currentFillWidth())
        playback.reset()
        assertEquals(0, playback.safeStepIndex)
        assertEquals(trackWidth, currentFillWidth())
    }

    @Test
    fun rejectionHistoryOnlyMarksCandidatesActuallyRejectedByReachedSnapshots() {
        val frames = GreedyFundamentalsSceneSequenceBuilder.build(
            GreedyFundamentalsTrace.createTrace(distances = listOf(2, 3, 4, 6, 8, 9), budget = 10)
        )

        val rejectionFrame = frames.single { it.step.status == GreedyFundamentalsStatus.REJECTED }
        val terminal = frames.last()
        val rejectionFrameIndex = frames.indexOf(rejectionFrame)
        assertEquals(listOf("CHOSEN", "CHOSEN", "CHOSEN", "REJECTED", "PENDING", "PENDING"), terminal.candidateStates.map { it.name })
        assertEquals(FundamentalsCandidateState.PENDING, frames[rejectionFrameIndex - 1].candidateStates[3])
        assertEquals(FundamentalsCandidateState.REJECTED, rejectionFrame.candidateStates[3])
        assertEquals(FundamentalsCandidateState.PENDING, rejectionFrame.candidateStates[4])
        assertEquals(FundamentalsCandidateState.PENDING, rejectionFrame.candidateStates[5])
    }

    @Test
    fun duplicateDistancesKeepOccurrenceIdentityAndAllAcceptedCandidatesStayChosen() {
        val duplicateFrames = GreedyFundamentalsSceneSequenceBuilder.build(
            GreedyFundamentalsTrace.createTrace(distances = listOf(2, 2, 2), budget = 4)
        )
        assertEquals(
            listOf(FundamentalsCandidateState.CHOSEN, FundamentalsCandidateState.CHOSEN, FundamentalsCandidateState.REJECTED),
            duplicateFrames.last().candidateStates
        )

        val acceptedFrames = GreedyFundamentalsSceneSequenceBuilder.build(
            GreedyFundamentalsTrace.createTrace(distances = listOf(1, 2, 3), budget = 6)
        )
        assertEquals(
            listOf(FundamentalsCandidateState.CHOSEN, FundamentalsCandidateState.CHOSEN, FundamentalsCandidateState.CHOSEN),
            acceptedFrames.last().candidateStates
        )
    }

    @Test
    fun defaultGeometryIsContainedStableAndFitsActualViewportState() {
        val frames = GreedyFundamentalsSceneSequenceBuilder.build(GreedyFundamentalsTrace.createTrace())
        val panes = listOf(360 to 224, 400 to 260, 460 to 320)

        panes.forEach { (paneWidth, paneHeight) ->
            val expected = GreedyFundamentalsSceneLayout.calculate(frames.first().step.sortedDistances, paneWidth.toFloat())
            frames.forEach { frame ->
                val layout = GreedyFundamentalsSceneLayout.calculate(frame.step.sortedDistances, paneWidth.toFloat())
                assertEquals(expected, layout, "Static geometry changed at snapshot ${frame.step.status}")
                assertSceneContainsLayout(layout)
            }

            val viewport = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            viewport.updateViewportSize(IntSize(paneWidth, paneHeight))
            viewport.updateContentSize(IntSize(expected.width.toInt(), expected.height.toInt()))
            val expectedScale = minOf(1f, paneWidth / expected.width, paneHeight / expected.height)
            assertEquals(expectedScale, viewport.scale, .0001f)
        }
    }

    @Test
    fun fourteenCandidatesUseContainedRowsWithoutNegativeOrUnusableCells() {
        val steps = GreedyFundamentalsTrace.createTrace(distances = (1..14).toList(), budget = 100)
        val frames = GreedyFundamentalsSceneSequenceBuilder.build(steps)
        val layout = GreedyFundamentalsSceneLayout.calculate(frames.first().step.sortedDistances, 360f)

        assertEquals(14, layout.candidates.size)
        assertEquals((0..13).toList(), layout.candidates.map { it.index })
        assertEquals(4, layout.candidateColumnCount)
        assertEquals(4, layout.candidateRowCount)
        assertTrue(layout.candidates.all { it.column.width >= 72f })
        assertTrue(layout.candidates.all { it.column.width > 0f && it.column.height > 0f })
        assertTrue(layout.candidates.all { layout.candidatesRegion.contains(it.column) })
        assertTrue(layout.selectedSequence.height >= 30f, "Long selected-sequence allowance must support wrapping")
        frames.forEach { frame ->
            val snapshotLayout = GreedyFundamentalsSceneLayout.calculate(frame.step.sortedDistances, 360f)
            assertEquals(layout, snapshotLayout, "Fourteen-candidate geometry changed at ${frame.step.status}")
            assertSceneContainsLayout(snapshotLayout)
        }
        assertEquals(14, frames.first().step.sortedDistances.size)
    }

    private fun assertSceneContainsLayout(layout: GreedyFundamentalsSceneLayoutResult) {
        val scene = FundamentalsRect(0f, 0f, layout.width, layout.height)
        listOf(layout.budgetRegion, layout.candidatesRegion, layout.summaryRegion, layout.complexity).forEach {
            assertTrue(scene.contains(it), "Scene ${layout.width}×${layout.height} does not contain $it")
        }
        assertTrue(layout.budgetRegion.contains(layout.budgetTitle))
        assertTrue(layout.budgetRegion.contains(layout.remainingLabel))
        assertTrue(layout.budgetRegion.contains(layout.progressTrack))
        assertTrue(layout.budgetRegion.contains(layout.usedLabel))
        assertTrue(layout.candidatesRegion.contains(layout.candidatesHeading))
        assertTrue(layout.candidates.all { candidate ->
            layout.candidatesRegion.contains(candidate.column) &&
                candidate.column.contains(candidate.state) &&
                candidate.column.contains(candidate.card) &&
                candidate.card.contains(candidate.identifier) &&
                candidate.card.contains(candidate.distance)
        })
        assertTrue(layout.summaryRegion.contains(layout.selectedSequence))
        assertTrue(layout.summaryRegion.contains(layout.count))
        assertTrue(layout.complexity.width > 0f)
        assertSeparated(layout.budgetRegion, layout.candidatesRegion)
        assertSeparated(layout.candidatesRegion, layout.summaryRegion)
        assertSeparated(layout.summaryRegion, layout.complexity)
        layout.candidates.forEachIndexed { index, candidate ->
            layout.candidates.drop(index + 1).forEach { sibling -> assertSeparated(candidate.column, sibling.column) }
        }
    }

    private fun assertSeparated(first: FundamentalsRect, second: FundamentalsRect) {
        val separated = first.right <= second.x || second.right <= first.x ||
            first.bottom <= second.y || second.bottom <= first.y
        assertTrue(separated, "Overlapping bounds: $first and $second")
    }
}
