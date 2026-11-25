package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermutationsSceneTest {
    @Test
    fun everySwapAndUndoMovesExactOccurrenceIdentities() {
        listOf(listOf(1, 2, 3), listOf(1, 1, 2)).forEach { input ->
            val frames = PermutationsSceneBuilder.build(PermutationsTrace.createTrace(input)).frames
            val expected = input.indices.map { "item-$it" }.toMutableList()
            frames.forEach { frame ->
                frame.swap?.let { (i, j) ->
                    val previous = expected[i]
                    expected[i] = expected[j]
                    expected[j] = previous
                }
                assertEquals(expected, frame.working.map { it.id })
                assertEquals(frame.step.currentPermutation, frame.working.map { it.value })
            }
            assertEquals(input.indices.map { "item-$it" }, expected)
        }
    }
    @Test
    fun originalTraceSnapshotsAndOccurrenceIdentitiesRemainConsistent() {
        val trace = PermutationsTrace.createTrace()
        val scene = PermutationsSceneBuilder.build(trace)
        assertEquals(26, scene.frames.size)
        assertEquals(trace, scene.frames.map { it.step })
        assertEquals(9, trace.count { it.action == PermutationAction.SWAP_CHOICE })
        assertEquals(9, trace.count { it.action == PermutationAction.BACKTRACK_SWAP })
        assertEquals(6, trace.count { it.action == PermutationAction.SOLUTION_FOUND })
        assertEquals(0, trace.count { it.action == PermutationAction.RECURSE })
        scene.frames.forEach { frame ->
            assertEquals(frame.step.currentPermutation, frame.working.map { it.value })
            assertEquals(3, frame.working.map { it.id }.toSet().size)
            assertEquals(frame.step.completedPermutations, frame.results.map { it.values })
            assertEquals(frame.results.indices.map { "result-$it" }, frame.results.map { it.id })
            assertEquals(frame.step.action == PermutationAction.BACKTRACK_SWAP, frame.undo)
        }
        assertEquals(listOf(1, 2, 3), scene.frames.last().working.map { it.value })
        assertEquals(listOf("item-0", "item-1", "item-2"), scene.frames.last().working.map { it.id })
        assertTrue(scene.frames.any { it.swap?.let { pair -> pair.first == pair.second } == true })
        assertTrue(scene.frames.any { it.swap?.let { pair -> pair.first != pair.second } == true && it.undo })
    }

    @Test
    fun duplicateValuesRemainDistinctAndResultsAreNotDeduplicated() {
        val scene = PermutationsSceneBuilder.build(PermutationsTrace.createTrace(listOf(1, 1, 2)))
        assertEquals(6, scene.frames.last().results.size)
        assertTrue(scene.frames.last().results.map { it.values }.distinct().size < 6)
        scene.frames.forEach { assertEquals(3, it.working.map { occurrence -> occurrence.id }.toSet().size) }
    }

    @Test
    fun resultGridIsStableContainedAndNonoverlappingAcrossSnapshotsAndViewports() {
        val scene = PermutationsSceneBuilder.build(PermutationsTrace.createTrace())
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layout = PermutationsSceneLayout.calculate(scene, viewport.width.toFloat(), viewport.height.toFloat())
            assertEquals(3, layout.cells.size)
            assertEquals(6, layout.results.size)
            assertTrue(layout.workingRegion.bottom < layout.resultRegion.y)
            assertTrue(layout.resultRegion.bottom <= layout.height)
            assertTrue(layout.emptyMessage.width > 0 && layout.emptyMessage.height > 0)
            assertTrue(layout.swapIndicator.bottom <= layout.labels.first().y)
            assertEquals(null, permutationSwapArrow(layout, 0 to 0))
            val arrow = permutationSwapArrow(layout, 0 to 2)!!
            assertEquals(layout.cells[0].centerX, arrow.left)
            assertEquals(layout.cells[2].centerX, arrow.right)
            assertTrue(arrow.y - arrow.head >= layout.swapIndicator.y)
            assertTrue(arrow.y + arrow.head <= layout.swapIndicator.bottom)
            layout.cells.forEachIndexed { i, cell ->
                assertTrue(cell.x >= layout.workingRegion.x && cell.right <= layout.workingRegion.right)
                assertTrue(cell.bottom <= layout.workingRegion.bottom)
                assertTrue(layout.labels[i].bottom < cell.y)
                layout.cells.drop(i + 1).forEach { assertFalse(overlap(cell, it)) }
            }
            layout.results.forEachIndexed { i, result ->
                assertTrue(result.width > 0 && result.height > 0)
                assertTrue(result.x >= layout.resultRegion.x && result.right <= layout.resultRegion.right)
                assertTrue(result.y >= layout.resultRegion.y && result.bottom <= layout.resultRegion.bottom)
                layout.results.drop(i + 1).forEach { assertFalse(overlap(result, it)) }
            }
            scene.frames.forEach { frame ->
                assertTrue(frame.results.size <= layout.results.size)
                frame.results.forEachIndexed { index, result ->
                    assertEquals("result-$index", result.id)
                    assertTrue(layout.results[index].width > 0f)
                }
            }
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertEquals(0f, state.translationX, "Initial horizontal translation")
            assertEquals(0f, state.translationY, "Initial vertical translation")
            assertTrue(layout.width * state.scale <= viewport.width + .01f)
            assertTrue(layout.height * state.scale <= viewport.height + .01f)
            state.reset()
            assertEquals(state.calculateFitScale(), state.scale)
        }
    }

    @Test
    fun longLabelsAndLargerMetricsExpandRatherThanOverlap() {
        val scene = PermutationsSceneBuilder.build(PermutationsTrace.createTrace(listOf(123456, 2, 3, 4)))
        val metrics = PermutationsLayoutMetrics(headingWidth = 260f, startWidth = 140f, labelWidth = 70f,
            labelHeight = 22f, valueWidth = 100f, valueHeight = 32f, resultHeadingWidth = 360f,
            resultWidth = 210f, resultHeight = 24f, emptyWidth = 250f, emptyHeight = 24f)
        val layout = PermutationsSceneLayout.calculate(scene, 240f, 224f, metrics)
        assertEquals(24, layout.results.size)
        assertTrue(layout.width >= 36f + 4f * 112f + 12f)
        assertTrue(layout.heading.right < layout.startIndex.x)
        layout.results.forEachIndexed { index, rect ->
            assertTrue(rect.right <= layout.resultRegion.right && rect.bottom <= layout.resultRegion.bottom)
            layout.results.drop(index + 1).forEach { assertFalse(overlap(rect, it)) }
        }
    }

    private fun overlap(a: PermutationRect, b: PermutationRect): Boolean =
        a.x < b.right && a.right > b.x && a.y < b.bottom && a.bottom > b.y
}
