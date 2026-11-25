package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GridPathfindingSceneTest {
    @Test
    fun defaultPathAndRejectionsFollowTraceWithoutInventedVisits() {
        val trace = GridPathfindingTrace.createTrace()
        val scene = GridPathfindingSceneBuilder.build(trace)
        assertEquals(trace, scene.frames.map { it.step })
        assertEquals(listOf(GridCell(0, 0), GridCell(0, 1), GridCell(1, 1), GridCell(1, 2), GridCell(2, 2)), scene.frames.last().path)
        assertEquals(5, scene.frames.last().pathCells)
        assertEquals(4, scene.frames.last().moves)
        assertTrue(scene.frames.any { it.rejection == GridRejection.OBSTACLE })
        scene.frames.forEach { frame ->
            assertEquals(frame.step.currentPath, frame.path)
            assertEquals(frame.path.zipWithNext(), frame.connections.map { it.from to it.to })
            assertEquals(frame.step.currentPath.toSet(), frame.step.visitedCells.toSet())
            if (frame.rejection != null) assertEquals(frame.path.lastOrNull(), frame.rejectionOrigin)
        }
    }

    @Test
    fun deadEndProducesActualRemovalsAndNoPath() {
        val trace = GridPathfindingTrace.createTrace(listOf(listOf(1, 1), listOf(0, 0)), GridCell(1, 1))
        val scene = GridPathfindingSceneBuilder.build(trace)
        assertTrue(scene.frames.last().path.isEmpty())
        assertTrue(scene.frames.any { it.rejection == GridRejection.REVISITED })
        scene.frames.filter { it.rejection == GridRejection.REVISITED }.forEach { frame ->
            assertTrue(frame.rejectedCell in frame.path)
            assertEquals(frame.path.lastOrNull(), frame.rejectionOrigin)
        }
        assertTrue(scene.frames.last().step.badgeText.contains("NO PATH"))
        scene.frames.forEachIndexed { index, frame ->
            if (frame.step.action == GridPathAction.BACKTRACK) {
                val removed = assertNotNull(frame.removedCell)
                assertEquals(scene.frames[index - 1].path.last(), removed)
                assertFalse(removed in frame.path)
                assertEquals(frame.path.lastOrNull(), frame.restoredCell)
            }
        }
        assertTrue(scene.frames.any { it.step.action == GridPathAction.BACKTRACK && it.restoredCell == null })
    }

    @Test
    fun startAtTargetRemainsTargetWithOnePathCellAndNoMoves() {
        val scene = GridPathfindingSceneBuilder.build(GridPathfindingTrace.createTrace(listOf(listOf(1)), GridCell(0, 0)))
        assertEquals(GridCell(0, 0), scene.frames.last().step.targetCell)
        assertEquals(listOf(GridCell(0, 0)), scene.frames.last().path)
        assertEquals(1, scene.frames.last().pathCells)
        assertEquals(0, scene.frames.last().moves)
    }

    @Test
    fun widthCandidatesUseBestTwoDimensionalFit() {
        val scene = GridPathfindingSceneBuilder.build(GridPathfindingTrace.createTrace())
        val narrow = GridLayoutMetrics(panelWidth = 180f, explanationHeight = 130f, pathHeight = 55f)
        val wide = GridLayoutMetrics(panelWidth = 250f, explanationHeight = 80f, pathHeight = 42f)
        val width = 360f
        val height = 224f
        val first = GridPathfindingSceneLayout.calculate(scene, width, height, narrow)
        val second = GridPathfindingSceneLayout.calculate(scene, width, height, wide)
        val chosen = GridPathfindingSceneLayout.choose(scene, width, height, listOf(narrow, wide))
        fun fit(layout: GridPathSceneLayoutResult) = minOf(width / layout.width, height / layout.height, 1f)
        assertEquals(maxOf(fit(first), fit(second)), fit(chosen))
        assertTrue(first.explanation.width >= narrow.panelWidth)
        assertTrue(first.path.width >= narrow.panelWidth)
        assertTrue(second.explanation.width >= wide.panelWidth)
        assertTrue(second.path.width >= wide.panelWidth)
    }

    @Test
    fun largerGlyphsAndWrappedTextExpandTheirAllocatedRegions() {
        val scene = GridPathfindingSceneBuilder.build(GridPathfindingTrace.createTrace())
        val metrics = GridLayoutMetrics(headingWidth = 220f, headingHeight = 25f, indexWidth = 22f, indexHeight = 24f,
            symbolWidth = 50f, symbolHeight = 52f, explanationHeadingHeight = 24f, explanationHeight = 160f,
            pathHeight = 90f, counterHeight = 40f, operationHeight = 35f, panelWidth = 230f)
        val layout = GridPathfindingSceneLayout.calculate(scene, 240f, 224f, metrics)
        assertTrue(layout.explanation.height >= metrics.explanationHeight)
        assertTrue(layout.path.height >= metrics.pathHeight)
        assertTrue(layout.counter.height >= metrics.counterHeight)
        assertTrue(layout.operation.height >= metrics.operationHeight)
        layout.cells.values.forEach { cell ->
            assertEquals(cell.width, cell.height)
            assertTrue(cell.width >= metrics.symbolHeight + 12f)
            assertTrue(cell.right <= layout.boardRegion.right && cell.bottom <= layout.boardRegion.bottom)
        }
        assertFalse(overlap(layout.boardRegion, layout.explanationRegion))
        assertFalse(overlap(layout.boardRegion, layout.pathRegion))
    }

    @Test
    fun allOutsideDirectionsHaveContainedIndicatorsAndRectangularBoardsRemainSquare() {
        val initial = GridPathfindingTrace.createTrace().first()
        val outside = listOf(GridCell(-1, 1), GridCell(3, 1), GridCell(1, -1), GridCell(1, 3))
        val frames = listOf(initial) + outside.map { cell -> initial.copy(currentCell = cell, action = GridPathAction.HIT_WALL) }
        val scene = GridPathfindingSceneBuilder.build(frames)
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layout = GridPathfindingSceneLayout.calculate(scene, viewport.width.toFloat(), viewport.height.toFloat())
            assertEquals(outside.toSet(), layout.outside.keys)
            layout.outside.values.forEach { rect ->
                assertTrue(rect.x >= layout.boardRegion.x && rect.y >= layout.boardRegion.y)
                assertTrue(rect.right <= layout.boardRegion.right && rect.bottom <= layout.boardRegion.bottom)
                layout.cells.values.forEach { assertFalse(overlap(rect, it)) }
            }
            assertFalse(overlap(layout.boardRegion, layout.explanationRegion))
            assertFalse(overlap(layout.boardRegion, layout.pathRegion))
            assertFalse(overlap(layout.explanationRegion, layout.pathRegion))
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertEquals(0f, state.translationX, "Initial horizontal translation")
            assertEquals(0f, state.translationY, "Initial vertical translation")
            assertTrue(layout.width * state.scale <= viewport.width + .01f)
            assertTrue(layout.height * state.scale <= viewport.height + .01f)
        }
        val rectangular = GridPathfindingSceneBuilder.build(GridPathfindingTrace.createTrace(listOf(listOf(1, 1, 1), listOf(0, 0, 1)), GridCell(1, 2)))
        val layout = GridPathfindingSceneLayout.calculate(rectangular, 360f, 224f)
        assertEquals(6, layout.cells.size)
        layout.cells.values.forEach { assertEquals(it.width, it.height) }
    }

    private fun overlap(a: GridSceneRect, b: GridSceneRect) = a.x < b.right && a.right > b.x && a.y < b.bottom && a.bottom > b.y
}
