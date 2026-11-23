package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MergeSortFullTraceTest {

    @Test
    fun defaultInputRunsDepthFirstRecursiveMergeSort() {
        val input = listOf(38, 27, 38, 3, 9)
        val steps = MergeSortFullTrace.stepsForFullSort()
        val terminal = steps.last()

        assertEquals(input, steps.first().array)
        assertEquals(FullMergeEvent.START, steps.first().event)
        assertEquals(
            listOf(FullMergeEvent.START, FullMergeEvent.CALL_ENTER, FullMergeEvent.DIVIDE, FullMergeEvent.CALL_ENTER),
            steps.take(4).map { it.event }
        )
        assertEquals("merge_node:0:5", steps[1].activeNodeId)
        assertEquals(listOf("merge_node:0:5"), steps[1].callStack)
        assertEquals(MergeIndexRange(0, 5), steps[2].activeRange)
        assertEquals(2, steps[2].splitIndex)
        assertEquals("merge_node:0:2", steps[2].nodes.single { it.nodeId == "merge_node:0:5" }.leftChildId)
        assertEquals("merge_node:2:5", steps[2].nodes.single { it.nodeId == "merge_node:0:5" }.rightChildId)
        assertEquals(FullMergeEvent.COMPLETE, terminal.event)
        assertTrue(terminal.isTerminal)
        assertEquals(listOf(3, 9, 27, 38, 38), terminal.workingArray)
        assertEquals(terminal.array.sorted(), terminal.workingArray)
        assertEquals(9, terminal.nodes.size)
        assertEquals(listOf(0, 2), terminal.workingElements.filter { it.value == 38 }.map { it.originalIndex })
        assertTrue(terminal.nodes.all { it.isCompleted })
        assertTrue(terminal.explanation.contains("O(N log N) time"))
        assertTrue(terminal.explanation.contains("O(N) auxiliary space"))
        assertTrue(steps.any { it.event == FullMergeEvent.BASE_CASE })
        assertTrue(steps.any { it.event == FullMergeEvent.MERGE_START })
        assertTrue(steps.any { it.event == FullMergeEvent.COPY_BACK })
    }

    @Test
    fun detailedPlaybackContainsEveryTraceStepAndStandardIsAChronologicalSubset() {
        val trace = MergeSortFullTrace.stepsForFullSort()
        val detailed = MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.DETAILED)
        val standard = MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.STANDARD)

        assertEquals(trace.indices.toList(), detailed.map { it.originalTraceIndex })
        assertEquals(trace, detailed.map { it.traceStep })
        assertTrue(standard.size < detailed.size)
        assertEquals(standard.map { it.originalTraceIndex }.sorted(), standard.map { it.originalTraceIndex })
        assertEquals(standard.map { it.originalTraceIndex }.distinct(), standard.map { it.originalTraceIndex })
        assertEquals(trace.first(), standard.first().traceStep)
        assertEquals(trace.last(), standard.last().traceStep)
        assertEquals(detailed.last().traceStep.workingElements, standard.last().traceStep.workingElements)
        standard.forEach { visible -> assertEquals(trace[visible.originalTraceIndex], visible.traceStep) }
    }

    @Test
    fun standardPlaybackPreservesEveryMergeAndMeaningfulIntermediateProgress() {
        val trace = MergeSortFullTrace.stepsForFullSort()
        val standard = MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.STANDARD)
        val visible = standard.map { it.traceStep }
        val mergeRanges = trace.filter { it.event == FullMergeEvent.MERGE_START }.map { it.activeRange }.toSet()

        mergeRanges.forEach { range ->
            assertTrue(visible.any { it.event == FullMergeEvent.MERGE_START && it.activeRange == range })
            assertTrue(visible.any { it.event == FullMergeEvent.COMPARE && it.activeRange == range })
            assertTrue(visible.any {
                it.activeRange == range &&
                    (it.event == FullMergeEvent.WRITE_BUFFER || it.event == FullMergeEvent.FLUSH_REMAINING)
            })
            val copyBack = visible.filter { it.event == FullMergeEvent.COPY_BACK && it.activeRange == range }
            assertTrue(copyBack.isNotEmpty())
            assertEquals(1, copyBack.first().copyBackProgress)
            assertEquals(range?.size, copyBack.last().copyBackProgress)
            assertTrue(visible.any { it.event == FullMergeEvent.RETURN && it.activeRange == range })
        }
        assertTrue(visible.any {
            it.event == FullMergeEvent.COMPARE &&
                it.currentComparison?.leftElement?.value == it.currentComparison?.rightElement?.value
        })
    }

    @Test
    fun modeSwitchMappingAlwaysSelectsTheMatchingVisibleFrame() {
        val trace = MergeSortFullTrace.stepsForFullSort()
        val standard = MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.STANDARD)
        val detailed = MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.DETAILED)

        trace.indices.forEach { originalIndex ->
            listOf(standard, detailed).forEach { visibleSteps ->
                val visibleIndex = MergeSortFullPlayback.nearestVisibleIndex(originalIndex, visibleSteps)
                assertTrue(visibleIndex in visibleSteps.indices)
                val selected = visibleSteps[visibleIndex]
                assertEquals(trace[selected.originalTraceIndex], selected.traceStep)
                val selectedDistance = kotlin.math.abs(selected.originalTraceIndex - originalIndex)
                assertTrue(visibleSteps.none {
                    kotlin.math.abs(it.originalTraceIndex - originalIndex) < selectedDistance
                })
            }
        }
    }

    @Test
    fun modeTransitionsKeepDisplayedStepAndControllerPositionAligned() {
        val trace = MergeSortFullTrace.stepsForFullSort()
        val modes = listOf(FullMergePlaybackMode.STANDARD, FullMergePlaybackMode.DETAILED)
        val stepsByMode = modes.associateWith { MergeSortFullPlayback.stepsFor(trace, it) }

        modes.forEach { sourceMode ->
            val sourceSteps = stepsByMode.getValue(sourceMode)
            val targetMode = if (sourceMode == FullMergePlaybackMode.STANDARD) {
                FullMergePlaybackMode.DETAILED
            } else {
                FullMergePlaybackMode.STANDARD
            }
            val targetSteps = stepsByMode.getValue(targetMode)

            sourceSteps.indices.forEach { controllerIndex ->
                val displayedSource = sourceSteps[controllerIndex]
                val mappedControllerIndex = MergeSortFullPlayback.nearestVisibleIndex(
                    displayedSource.originalTraceIndex,
                    targetSteps
                )
                val displayedAfterSwitch = targetSteps[mappedControllerIndex]

                assertTrue(mappedControllerIndex in targetSteps.indices)
                assertEquals(trace[displayedAfterSwitch.originalTraceIndex], displayedAfterSwitch.traceStep)
                assertEquals(
                    MergeSortFullPlayback.nearestVisibleIndex(displayedSource.originalTraceIndex, targetSteps),
                    mappedControllerIndex
                )

                val nextIndex = (mappedControllerIndex + 1).coerceAtMost(targetSteps.lastIndex)
                val previousIndex = (mappedControllerIndex - 1).coerceAtLeast(0)
                assertTrue(nextIndex in targetSteps.indices)
                assertTrue(previousIndex in targetSteps.indices)
                assertEquals(trace[targetSteps[nextIndex].originalTraceIndex], targetSteps[nextIndex].traceStep)
                assertEquals(trace[targetSteps[previousIndex].originalTraceIndex], targetSteps[previousIndex].traceStep)
                assertEquals(trace.first(), targetSteps[0].traceStep)
                assertEquals(targetSteps.last().traceStep.event, FullMergeEvent.COMPLETE)
                assertEquals(targetSteps.lastIndex, targetSteps.indexOfLast { it.traceStep.isTerminal })
            }
        }
    }

    @Test
    fun standardAndDetailedPlaybackCountsStayStable() {
        val trace = MergeSortFullTrace.stepsForFullSort()
        assertEquals(48, MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.STANDARD).size)
        assertEquals(65, MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.DETAILED).size)
    }

    @Test
    fun recursiveChildrenEnterLeftBeforeRightAndReturnToTheirParent() {
        val steps = MergeSortFullTrace.stepsForFullSort(listOf(5, 1, 4, 2))
        val rootDivide = steps.single { it.event == FullMergeEvent.DIVIDE && it.activeRange == MergeIndexRange(0, 4) }
        assertEquals(2, rootDivide.splitIndex)
        assertTrue(rootDivide.nodes.none { it.nodeId == "merge_node:0:2" || it.nodeId == "merge_node:2:4" })

        val leftEnter = steps.indexOfFirst { it.event == FullMergeEvent.CALL_ENTER && it.activeRange == MergeIndexRange(0, 2) }
        val leftReturn = steps.indexOfFirst { it.event == FullMergeEvent.RETURN && it.activeRange == MergeIndexRange(0, 2) }
        val rightEnter = steps.indexOfFirst { it.event == FullMergeEvent.CALL_ENTER && it.activeRange == MergeIndexRange(2, 4) }
        assertTrue(leftEnter > steps.indexOf(rootDivide))
        assertTrue(leftReturn < rightEnter)
        assertEquals("merge_node:0:4", steps[leftReturn].returnToNodeId)
        assertEquals("merge_node:0:2", steps[leftReturn].callStack.last())
        assertEquals("merge_node:0:4", steps[rightEnter].nodes.last().parentNodeId)

        steps.forEach { step ->
            if (step.event == FullMergeEvent.CALL_ENTER) {
                assertEquals(step.activeNodeId, step.callStack.last())
                val node = step.nodes.single { it.nodeId == step.activeNodeId }
                assertTrue(node.hasEntered)
                if (node.parentNodeId != null) assertTrue(step.nodes.any { it.nodeId == node.parentNodeId })
            }
        }
    }

    @Test
    fun comparisonsWritesFlushesAndCopyBackAdvanceCorrectPointers() {
        val steps = MergeSortFullTrace.stepsForFullSort(listOf(1, 4, 7, 2, 5))
        steps.forEachIndexed { index, step ->
            if (step.event == FullMergeEvent.COMPARE) {
                val comparison = assertNotNull(step.currentComparison)
                assertEquals(step.workingElements[comparison.leftIndex], comparison.leftElement)
                assertEquals(step.workingElements[comparison.rightIndex], comparison.rightElement)
                assertEquals(FullMergeEvent.WRITE_BUFFER, steps[index + 1].event)
                val write = steps[index + 1]
                val before = assertNotNull(write.pointersBeforeTransfer)
                val after = assertNotNull(write.pointersAfterTransfer)
                assertEquals(comparison.leftIndex, before.left)
                assertEquals(comparison.rightIndex, before.right)
                assertEquals(step.destinationPointer, before.destination)
                assertEquals(before.destination, write.newlyWrittenBufferSlot)
                assertEquals(write.selectedElement, write.auxiliaryBuffer[before.destination])
                assertEquals(before.destination + 1, after.destination)
                if (write.selectedSource == FullMergeSource.LEFT) {
                    assertEquals(before.left + 1, after.left)
                    assertEquals(before.right, after.right)
                    assertTrue(comparison.leftElement.value <= comparison.rightElement.value)
                } else {
                    assertEquals(before.left, after.left)
                    assertEquals(before.right + 1, after.right)
                    assertTrue(comparison.rightElement.value < comparison.leftElement.value)
                }
            }
            if (step.event == FullMergeEvent.FLUSH_REMAINING) {
                val before = assertNotNull(step.pointersBeforeTransfer)
                val after = assertNotNull(step.pointersAfterTransfer)
                assertEquals(before.destination, step.newlyWrittenBufferSlot)
                assertEquals(step.selectedElement, step.auxiliaryBuffer[before.destination])
                assertEquals(before.destination + 1, after.destination)
                assertNull(step.currentComparison)
            }
            if (step.event == FullMergeEvent.COPY_BACK) {
                val indexToCopy = assertNotNull(step.copyBackIndex)
                assertEquals(step.auxiliaryBuffer[indexToCopy], step.workingElements[indexToCopy])
                assertEquals(step.selectedElement, step.workingElements[indexToCopy])
                val sameRangeProgress = steps.take(index + 1).count {
                    it.event == FullMergeEvent.COPY_BACK && it.activeRange == step.activeRange
                }
                assertEquals(sameRangeProgress, step.copyBackProgress)
                assertTrue(step.copyBackProgress in 1..(step.activeRange?.size ?: 0))
                if (step.copyBackProgress < (step.activeRange?.size ?: 0)) {
                    assertTrue(step.activeRange !in step.completedRanges)
                } else {
                    assertTrue(step.activeRange in step.completedRanges)
                    assertTrue(step.nodes.single { it.nodeId == step.activeNodeId }.isCompleted)
                }
            }
        }
    }

    @Test
    fun duplicateOccurrencesKeepTheirOriginalStableOrder() {
        val input = listOf(4, 2, 4, 2, 4, 2, 4)
        val steps = MergeSortFullTrace.stepsForFullSort(input)
        val terminal = steps.last()
        assertEquals(input.sorted(), terminal.workingArray)
        for (value in input.distinct()) {
            val originalIndices = terminal.workingElements.filter { it.value == value }.map { it.originalIndex }
            assertEquals(originalIndices.sorted(), originalIndices)
            assertEquals(originalIndices.map { "input:$it" }, terminal.workingElements.filter { it.value == value }.map { it.identity })
        }
        val tieWrites = steps.filter { it.event == FullMergeEvent.WRITE_BUFFER && it.currentComparison?.leftElement?.value == it.currentComparison?.rightElement?.value }
        assertTrue(tieWrites.isNotEmpty())
        assertTrue(tieWrites.all { it.selectedSource == FullMergeSource.LEFT })
    }

    @Test
    fun parentRangesBecomeSortedOnlyAfterTheirCopyBackFinishes() {
        val steps = MergeSortFullTrace.stepsForFullSort(listOf(9, 8, 7, 6, 5, 4, 3))
        val ranges = steps.last().nodes.filter { !it.isBaseCase }.map { it.range }
        ranges.forEach { range ->
            val copyBack = steps.filter { it.event == FullMergeEvent.COPY_BACK && it.activeRange == range }
            assertEquals(range.size, copyBack.size)
            copyBack.dropLast(1).forEach { step ->
                assertTrue(range !in step.completedRanges)
                assertFalseCompleted(step, range)
            }
            assertTrue(range in copyBack.last().completedRanges)
            assertTrue(copyBack.last().nodes.single { it.range == range }.isCompleted)
            assertTrue(copyBack.last().workingArray.subList(range.start, range.endExclusive).zipWithNext().all { (a, b) -> a <= b })
        }
    }

    @Test
    fun emptySingletonSortedAndExtremeInputsCompleteWithoutInvalidRanges() {
        val cases = listOf(
            emptyList<Int>(),
            listOf(7),
            listOf(1, 2, 3, 4),
            listOf(4, 3, 2, 1),
            listOf(-9, -9, 0, 4, 4),
            listOf(Int.MAX_VALUE, Int.MIN_VALUE, 0, -1),
            listOf(38, 27, 43, 3, 9, 82, 10),
            (20 downTo 0).toList()
        )
        cases.forEach { input ->
            val steps = MergeSortFullTrace.stepsForFullSort(input)
            val complete = steps.last()
            assertEquals(input.sorted(), complete.workingArray)
            assertEquals(input.size, complete.workingElements.size)
            assertEquals(input.indices.map { "input:$it" }.toSet(), complete.workingElements.map { it.identity }.toSet())
            steps.forEach { step ->
                assertTrue(step.nodes.all { it.rangeStart in 0..input.size && it.rangeEndExclusive in it.rangeStart..input.size })
                assertTrue(step.auxiliaryBuffer.size == input.size)
                assertTrue(step.workingElements.size == input.size)
            }
            if (input.size <= 1) {
                assertTrue(steps.none { it.event == FullMergeEvent.MERGE_START || it.event == FullMergeEvent.COMPARE || it.event == FullMergeEvent.COPY_BACK })
            }
        }
    }

    private fun assertFalseCompleted(step: MergeSortFullTraceStep, range: MergeIndexRange) {
        assertTrue(step.nodes.single { it.range == range }.isCompleted.not())
    }
}
