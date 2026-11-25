package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NQueensSceneTest {
    @Test
    fun vectorIconBoundsAreCenteredAndRemainInsideCells() {
        listOf(24f, 42f, 80f).forEach { side ->
            val icon = backtrackingIconBounds(5f, 7f, side, side)
            assertTrue(icon.size > 0f)
            assertTrue(icon.x > 5f && icon.y > 7f)
            assertTrue(icon.x + icon.size < 5f + side && icon.y + icon.size < 7f + side)
            assertEquals(5f + side / 2f, icon.x + icon.size / 2f, .001f)
            assertEquals(7f + side / 2f, icon.y + icon.size / 2f, .001f)
        }
        val from = backtrackingIconBounds(0f, 0f, 42f, 42f)
        listOf(backtrackingIconBounds(46f, 0f, 42f, 42f), backtrackingIconBounds(46f, 46f, 42f, 42f)).forEach { to ->
            val line = backtrackingConnection(from, to)
            fun inside(x: Float, y: Float, box: BacktrackingIconBounds) = x >= box.x && x <= box.x + box.size && y >= box.y && y <= box.y + box.size
            assertFalse(inside(line.startX, line.startY, from))
            assertFalse(inside(line.endX, line.endY, to))
        }
    }
    @Test
    fun allTraceStatesDeriveActualConflictsAndRemovedQueens() {
        val trace = NQueensTrace.createTrace()
        val scene = NQueensSceneBuilder.build(trace)
        assertEquals(trace, scene.frames.map { it.step })
        assertEquals(16, scene.cells.size)
        assertEquals(16, scene.cells.map { it.id }.toSet().size)
        assertTrue(scene.frames.first().placed.isEmpty())
        assertEquals(listOf(listOf(1, 3, 0, 2), listOf(2, 0, 3, 1)), scene.frames.last().step.solutions)
        assertTrue(scene.frames.last().placed.isEmpty())
        assertTrue(scene.frames.any { it.conflicts.size > 1 })
        assertTrue(scene.frames.flatMap { it.conflicts }.any { it.kind == QueenConflictKind.COLUMN })
        assertTrue(scene.frames.flatMap { it.conflicts }.any { it.kind == QueenConflictKind.DIAGONAL })
        scene.frames.forEachIndexed { index, frame ->
            assertEquals(frame.step.queenColumns.mapIndexedNotNull { row, col -> col?.let { QueenCell(row, it) } }, frame.placed)
            if (frame.step.action == NQueensAction.BACKTRACK) {
                val removed = assertNotNull(frame.removed)
                assertTrue(removed in scene.frames[index - 1].placed)
                assertFalse(removed in frame.placed)
                assertNull(frame.testing)
            } else assertNull(frame.removed)
            frame.testing?.let { test ->
                val expected = frame.placed.filter { it.column == test.column || abs(it.row - test.row) == abs(it.column - test.column) }.toSet()
                assertEquals(expected, frame.conflicts.map { it.from }.toSet())
                frame.conflicts.forEach { edge ->
                    assertEquals(test, edge.to)
                    if (edge.kind == QueenConflictKind.COLUMN) assertEquals(edge.from.column, test.column)
                    else assertEquals(abs(edge.from.row - test.row), abs(edge.from.column - test.column))
                }
            }
        }
    }

    @Test
    fun boardAndSolutionRegionsRemainContainedAcrossResponsiveViewports() {
        val scene = NQueensSceneBuilder.build(NQueensTrace.createTrace())
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layout = NQueensSceneLayout.calculate(scene, viewport.width.toFloat(), viewport.height.toFloat())
            assertEquals(16, layout.cells.size)
            assertEquals(4, layout.columns.size)
            assertEquals(4, layout.rows.size)
            assertFalse(overlaps(layout.boardRegion, layout.solutionRegion))
            layout.cells.forEach { (_, rect) ->
                assertEquals(rect.width, rect.height)
                assertTrue(rect.width >= 42f)
                assertTrue(rect.x >= layout.boardRegion.x && rect.right <= layout.boardRegion.right)
                assertTrue(rect.y >= layout.boardRegion.y && rect.bottom <= layout.boardRegion.bottom)
            }
            layout.cells.values.toList().forEachIndexed { index, cell ->
                layout.cells.values.drop(index + 1).forEach { assertFalse(overlaps(cell, it)) }
            }
            scene.frames.forEach { frame ->
                assertTrue(frame.step.solutions.size <= layout.solutions.size)
                frame.conflicts.forEach { edge ->
                    assertTrue(layout.cells.containsKey(edge.from))
                    assertTrue(layout.cells.containsKey(edge.to))
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
    fun alternateBoardAndLargeGlyphMetricsKeepLabelsAndCellsInsideRegions() {
        val scene = NQueensSceneBuilder.build(NQueensTrace.createTrace(3))
        val metrics = QueensLayoutMetrics(headingWidth = 220f, headingHeight = 24f, indexWidth = 25f, indexHeight = 22f,
            symbolWidth = 48f, symbolHeight = 52f, solutionsHeadingWidth = 100f, countWidth = 70f,
            textHeight = 24f, solutionWidth = 160f, solutionHeight = 24f)
        val layout = NQueensSceneLayout.calculate(scene, 240f, 224f, metrics)
        assertEquals(9, layout.cells.size)
        assertTrue(layout.boardHeading.width >= metrics.headingWidth)
        layout.cells.values.forEach { cell ->
            assertEquals(cell.width, cell.height)
            assertTrue(cell.width >= metrics.symbolHeight + 12f)
            assertTrue(cell.right <= layout.boardRegion.right && cell.bottom <= layout.boardRegion.bottom)
        }
        layout.rows.forEach { assertTrue(it.x >= layout.boardRegion.x && it.right <= layout.boardRegion.right) }
        layout.columns.forEach { assertTrue(it.y >= layout.boardRegion.y && it.bottom <= layout.boardRegion.bottom) }
    }

    private fun overlaps(a: QueensRect, b: QueensRect) =
        a.x < b.right && a.right > b.x && a.y < b.bottom && a.bottom > b.y
}
