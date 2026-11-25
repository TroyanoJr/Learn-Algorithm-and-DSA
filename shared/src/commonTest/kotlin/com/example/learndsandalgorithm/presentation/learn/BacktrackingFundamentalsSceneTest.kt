package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.Offset

class BacktrackingFundamentalsSceneTest {
    @Test
    fun candidateAndMinimumLevelWidthRemainSeparatedWithSmallRawCountWidth() {
        val scene = BacktrackingFundamentalsSceneBuilder.build(BacktrackingFundamentalsTrace.createTrace())
        val metrics = FundamentalsLayoutMetrics(candidateWidth = 220f, countWidth = 10f)
        val layout = BacktrackingFundamentalsSceneLayout.calculate(scene, 240f, 224f, metrics)
        assertTrue(layout.candidateValue.width >= 220f)
        assertTrue(layout.level.width >= 72f)
        assertTrue(layout.candidateValue.right <= layout.level.x)
        assertTrue(layout.level.right <= layout.candidateRegion.right)
        listOf(80f, 160f, 320f).forEach { height ->
            val allocation = backtrackingDetailsAllocation(height)
            assertTrue(allocation.operationHeight > 0f && allocation.stepsHeight > 0f)
            assertEquals(height, allocation.operationHeight + allocation.stepsHeight + allocation.gap, .001f)
        }
    }

    @Test
    fun detailsAllocationUsesNaturalOperationHeightAndReservesSteps() {
        val short = backtrackingDetailsAllocation(240f, preferredOperation = 55f, minimumSteps = 50f)
        assertEquals(55f, short.operationHeight)
        assertTrue(short.stepsHeight >= 50f)
        val long = backtrackingDetailsAllocation(160f, preferredOperation = 260f, minimumSteps = 65f)
        assertTrue(long.stepsHeight >= 65f)
        assertTrue(long.operationHeight > 0f)
        assertEquals(160f, long.operationHeight + long.stepsHeight + long.gap, .001f)
        val constrained = backtrackingDetailsAllocation(160f, preferredOperation = 260f, minimumSteps = 90f)
        assertTrue(constrained.stepsHeight >= 90f)
    }
    @Test
    fun traceProducesBinaryTopologyAndPreservesAllSnapshots() {
        val trace = BacktrackingFundamentalsTrace.createTrace()
        val scene = BacktrackingFundamentalsSceneBuilder.build(trace)
        assertEquals(18, scene.frames.size)
        assertEquals(trace, scene.frames.map { it.step })
        assertEquals(listOf("", "0", "1", "00", "01", "10", "11"), scene.nodes.map { it.path })
        assertEquals(setOf("->0", "->1", "0->00", "0->01", "1->10", "1->11"),
            scene.connections.map { "${it.parent}->${it.child}" }.toSet())
        assertEquals(7, scene.nodes.map { it.id }.toSet().size)
        assertEquals(6, scene.connections.map { it.id }.toSet().size)
        assertEquals(listOf("00", "01", "10", "11"), scene.frames.last().step.generatedStrings)
        assertEquals(6, trace.count { it.action == BacktrackingFundamentalsAction.CHOOSE_0 || it.action == BacktrackingFundamentalsAction.CHOOSE_1 })
        assertEquals(6, trace.count { it.action == BacktrackingFundamentalsAction.BACKTRACK })
        assertEquals(4, trace.count { it.action == BacktrackingFundamentalsAction.SOLUTION_FOUND })
    }

    @Test
    fun everyBacktrackUsesPreviousChildAndRestoredParent() {
        val scene = BacktrackingFundamentalsSceneBuilder.build(BacktrackingFundamentalsTrace.createTrace())
        scene.frames.forEachIndexed { index, frame ->
            assertEquals(frame.step.generatedStrings.toSet(), frame.solutionPaths)
            assertEquals((0..frame.step.currentString.length).map { frame.step.currentString.take(it) }.toSet(), frame.activePaths)
            if (frame.step.action == BacktrackingFundamentalsAction.BACKTRACK) {
                val from = assertNotNull(frame.backtrackFrom)
                assertEquals(scene.frames[index - 1].step.currentString, from)
                assertEquals(frame.step.currentString, frame.backtrackTo)
                assertTrue(scene.connections.any { it.parent == frame.backtrackTo && it.child == from })
            } else {
                assertNull(frame.backtrackFrom)
                assertNull(frame.backtrackTo)
            }
        }
    }

    @Test
    fun treeGeometryContainsEveryNodeAndSeparatesPeersAcrossViewports() {
        val scene = BacktrackingFundamentalsSceneBuilder.build(BacktrackingFundamentalsTrace.createTrace())
        listOf(360f to 224f, 400f to 260f, 460f to 320f, 240f to 224f).forEach { (width, height) ->
            val layout = BacktrackingFundamentalsSceneLayout.calculate(scene, width, height)
            assertEquals(scene.nodes.map { it.path }.toSet(), layout.nodes.keys)
            assertTrue(layout.candidateRegion.bottom < layout.treeRegion.y)
            assertTrue(layout.treeRegion.bottom < layout.summaryRegion.y)
            assertTrue(layout.summaryRegion.bottom <= layout.height)
            scene.nodes.groupBy { it.path.length }.values.forEach { row ->
                val rectangles = row.map { layout.nodes.getValue(it.path) }.sortedBy { it.x }
                rectangles.forEach { rect ->
                    assertTrue(rect.width > 0f && rect.height > 0f)
                    assertTrue(rect.x >= layout.treeRegion.x && rect.right <= layout.treeRegion.right)
                    assertTrue(rect.y >= layout.treeRegion.y && rect.bottom <= layout.treeRegion.bottom)
                }
                rectangles.zipWithNext().forEach { (left, right) -> assertTrue(left.right < right.x) }
            }
            scene.connections.forEach { edge ->
                assertTrue(layout.nodes.getValue(edge.parent).bottom < layout.nodes.getValue(edge.child).y)
            }
        }
    }

    @Test
    fun measuredLargerLabelsExpandSceneAndProductionViewportFits() {
        val scene = BacktrackingFundamentalsSceneBuilder.build(BacktrackingFundamentalsTrace.createTrace())
        val metrics = FundamentalsLayoutMetrics(nodeWidth = 100f, nodeHeight = 32f,
            candidateWidth = 290f, headingWidth = 320f, summaryWidth = 300f, countWidth = 130f,
            candidateHeight = 30f, headingHeight = 25f, summaryHeight = 25f)
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layout = BacktrackingFundamentalsSceneLayout.calculate(scene, viewport.width.toFloat(), viewport.height.toFloat(), metrics)
            assertTrue(layout.width >= 454f)
            assertTrue(layout.candidateHeader.width >= metrics.candidateWidth)
            assertTrue(layout.treeHeading.width >= metrics.headingWidth)
            assertTrue(layout.summary.width >= metrics.summaryWidth)
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertEquals(0f, state.translationX, "Initial horizontal translation")
            assertEquals(0f, state.translationY, "Initial vertical translation")
            assertTrue(layout.width * state.scale <= viewport.width + .01f)
            assertTrue(layout.height * state.scale <= viewport.height + .01f)
            val initialScale = state.scale
            state.onTransform(2f, Offset.Zero, Offset(viewport.width / 2f, viewport.height / 2f))
            assertTrue(state.scale > initialScale)
            state.onTransform(1f, Offset(-50f, -50f), Offset(viewport.width / 2f, viewport.height / 2f))
            state.reset()
            assertEquals(state.calculateFitScale(), state.scale)
            assertEquals(0f, state.translationX)
            assertEquals(0f, state.translationY)
        }
    }
}
