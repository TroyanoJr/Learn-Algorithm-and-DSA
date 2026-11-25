package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CombinationsPruningSceneTest {
    @Test
    fun progressUsesBoundedTrackForExactExcessAndZeroTarget() {
        assertEquals(.4f, combinationProgress(2, 5))
        assertEquals(1f, combinationProgress(5, 5))
        assertEquals(1f, combinationProgress(6, 5))
        assertEquals(1f, combinationProgress(0, 0))
        assertEquals(0f, combinationProgress(1, 0))
    }
    @Test
    fun candidateAndEmptyTextHeightsAreReserved() {
        val scene = CombinationsPruningSceneBuilder.build(CombinationsPruningTrace.createTrace())
        val metrics = CombinationLayoutMetrics(candidateHeight = 55f, emptyHeight = 60f)
        val layout = CombinationsPruningSceneLayout.calculate(scene, 360f, 224f, metrics)
        assertTrue(layout.candidateHeading.height >= metrics.candidateHeight)
        assertTrue(layout.empty.height >= metrics.emptyHeight)
        assertTrue(layout.operation.y >= layout.candidates.maxOf { it.bottom })
        assertTrue(layout.empty.bottom <= layout.stackRegion.bottom)
    }
    @Test
    fun everySnapshotPreservesStackPushPopAndSolutions() {
        val trace = CombinationsPruningTrace.createTrace()
        val scene = CombinationsPruningSceneBuilder.build(trace)
        assertEquals(trace, scene.frames.map { it.step })
        assertEquals(listOf(2, 3), scene.candidates.map { it.value })
        assertEquals(listOf("candidate-0", "candidate-1"), scene.candidates.map { it.id })
        scene.frames.forEachIndexed { index, frame ->
            assertEquals(frame.step.currentCombination, frame.stack.map { it.value })
            assertEquals(frame.step.currentSum, frame.stack.sumOf { it.value })
            assertEquals(frame.step.target - frame.step.currentSum, frame.difference)
            assertTrue(frame.excessPruningSafe)
            if (frame.step.action == CombinationAction.TRY_CANDIDATE) {
                val pushed = assertNotNull(frame.pushed)
                assertEquals(frame.step.startIndex, pushed.candidateIndex)
                assertEquals(scene.candidates[pushed.candidateIndex].value, pushed.value)
                assertEquals(pushed, frame.stack.last())
            }
            if (frame.step.action == CombinationAction.BACKTRACK) {
                val popped = assertNotNull(frame.popped)
                assertEquals(scene.frames[index - 1].stack.last(), popped)
                assertFalse(frame.stack.any { it.id == popped.id })
            }
        }
        assertTrue(scene.frames.any { it.step.action == CombinationAction.PRUNE })
        assertTrue(scene.frames.any { it.step.action == CombinationAction.SOLUTION_FOUND })
        assertTrue(scene.frames.zipWithNext().any { (previous, current) -> previous.step.action == CombinationAction.PRUNE && current.popped != null })
        assertTrue(scene.frames.zipWithNext().any { (previous, current) -> previous.step.action == CombinationAction.SOLUTION_FOUND && current.popped != null })
        assertTrue(scene.frames.any { it.popped != null && it.difference > 0 && it.stack.isEmpty() })
        assertEquals(listOf(listOf(2, 3)), scene.frames.last().step.validCombinations)
        assertTrue(scene.frames.last().stack.isEmpty())
    }

    @Test
    fun duplicateCandidateOccurrencesAndReuseRemainDistinct() {
        val scene = CombinationsPruningSceneBuilder.build(CombinationsPruningTrace.createTrace(listOf(2, 2), 4))
        assertEquals(listOf("candidate-0", "candidate-1"), scene.candidates.map { it.id })
        assertTrue(scene.frames.any { it.stack.size == 2 && it.stack[0].candidateIndex == it.stack[1].candidateIndex && it.stack[0].id != it.stack[1].id })
        assertTrue(scene.frames.filter { it.pushed != null }.map { it.pushed!!.candidateIndex }.toSet().containsAll(listOf(0, 1)))
        scene.frames.forEach { assertEquals(it.step.currentCombination, it.stack.map { entry -> entry.value }) }
    }

    @Test
    fun negativeCandidateMetadataDoesNotClaimExcessPruningIsSafe() {
        val initial = CombinationsPruningTrace.createTrace().first().copy(candidates = listOf(3, -1))
        val scene = CombinationsPruningSceneBuilder.build(listOf(initial))
        assertFalse(scene.frames.single().excessPruningSafe)
    }

    @Test
    fun geometryContainsCandidatesGhostAndSolutionsAtAllViewports() {
        val scene = CombinationsPruningSceneBuilder.build(CombinationsPruningTrace.createTrace())
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layout = CombinationsPruningSceneLayout.calculate(scene, viewport.width.toFloat(), viewport.height.toFloat())
            assertTrue(layout.header.bottom < layout.stackRegion.y)
            assertTrue(layout.stackRegion.bottom < layout.solutionsRegion.y)
            assertTrue(layout.solutionsRegion.bottom <= layout.height)
            assertEquals(scene.candidates.size, layout.candidates.size)
            layout.candidates.forEach { assertTrue(it.x >= layout.stackRegion.x && it.right <= layout.stackRegion.right) }
            layout.stack.forEachIndexed { index, cell ->
                assertTrue(cell.y >= layout.operation.bottom && cell.bottom <= layout.stackRegion.bottom)
                layout.stack.drop(index + 1).forEach { assertFalse(overlaps(cell, it)) }
            }
            scene.frames.forEach { frame ->
                val required = frame.stack.size + if (frame.popped == null) 0 else 1
                assertTrue(required <= layout.stack.size)
                assertTrue(frame.step.validCombinations.size <= layout.solutions.size)
            }
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertEquals(0f, state.translationX, "Initial horizontal translation")
            assertEquals(0f, state.translationY, "Initial vertical translation")
            assertTrue(layout.width * state.scale <= viewport.width + .01f)
            assertTrue(layout.height * state.scale <= viewport.height + .01f)
        }
    }

    @Test
    fun largeTextAndLongCandidateSetsRemainContained() {
        val scene = CombinationsPruningSceneBuilder.build(CombinationsPruningTrace.createTrace(listOf(11, 12, 13, 14), 25))
        val metrics = CombinationLayoutMetrics(targetWidth = 150f, currentWidth = 160f, headerHeight = 25f,
            differenceWidth = 150f, differenceHeight = 25f, headingWidth = 250f, headingHeight = 24f,
            candidateHeadingWidth = 120f, itemWidth = 75f, itemHeight = 28f, emptyWidth = 220f,
            operationWidth = 240f, operationHeight = 24f, solutionHeadingWidth = 140f, solutionCountWidth = 90f,
            solutionHeight = 25f, solutionWidth = 160f, conditionWidth = 270f, conditionHeight = 24f)
        val layout = CombinationsPruningSceneLayout.calculate(scene, 240f, 224f, metrics)
        assertEquals(4, layout.candidates.size)
        (layout.candidates + layout.stack).forEachIndexed { index, rect ->
            assertTrue(rect.x >= layout.stackRegion.x && rect.right <= layout.stackRegion.right && rect.bottom <= layout.stackRegion.bottom)
            (layout.candidates + layout.stack).drop(index + 1).forEach { assertFalse(overlaps(rect, it)) }
        }
        layout.solutions.forEach { assertTrue(it.right <= layout.solutionsRegion.right && it.bottom <= layout.solutionsRegion.bottom) }
        assertTrue(layout.condition.right <= layout.solutionsRegion.right && layout.condition.bottom <= layout.solutionsRegion.bottom)
    }

    private fun overlaps(a: CombinationRect, b: CombinationRect) =
        a.x < b.right && a.right > b.x && a.y < b.bottom && a.bottom > b.y
}
