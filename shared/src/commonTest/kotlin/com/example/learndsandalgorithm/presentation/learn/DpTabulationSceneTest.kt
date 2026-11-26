package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DpTabulationSceneTest {
    @Test
    fun enlargedMeasuredContentFitsAllocatedRegions() {
        val scene = DpTabulationSceneBuilder.build(DpTabulationTrace.createTrace())
        val metrics = TabMetrics(headingWidth = 180f, headingHeight = 20f, cellWidth = 52f,
            valueHeight = 22f, indexHeight = 18f, rollingHeadingWidth = 240f, variableWidth = 95f,
            variableHeight = 22f, formulaWidth = 300f, formulaHeight = 20f)
        val layout = DpTabulationSceneLayout.calculate(scene, 240f, 224f, metrics)
        assertTrue(layout.cells.all { it.width >= metrics.cellWidth && it.height >= metrics.valueHeight })
        assertTrue(layout.variables.all { it.width >= metrics.variableWidth && it.height >= metrics.variableHeight })
        assertTrue(layout.formula.width >= metrics.formulaWidth && layout.formula.height >= metrics.formulaHeight)
        assertTrue(layout.spaceBadge.bottom <= layout.rolling.bottom)
    }
    @Test
    fun dependenciesRepresentOnlyActualFillOperations() {
        val trace = DpTabulationTrace.createTrace()
        val scene = DpTabulationSceneBuilder.build(trace)
        assertEquals(6, scene.frames.size)
        assertEquals(trace, scene.frames.map { it.step })
        scene.frames.forEach { frame ->
            val i = frame.step.currentIndex
            if (frame.step.action == TabulationAction.FILL_TABLE) {
                assertEquals(listOf(i!! - 2, i - 1), frame.dependencies.map { it.source })
                assertTrue(frame.dependencies.all { it.destination == i })
                assertEquals(frame.step.dpTable[i], frame.step.prev1 + frame.step.prev2)
            } else assertTrue(frame.dependencies.isEmpty())
        }
    }

    @Test
    fun layoutKeepsAllCellsAndDependencyLaneContained() {
        val scene = DpTabulationSceneBuilder.build(DpTabulationTrace.createTrace())
        listOf(360f to 224f, 400f to 260f, 460f to 320f, 240f to 224f).forEach { (w, h) ->
            val layout = DpTabulationSceneLayout.calculate(scene, w, h)
            assertEquals(6, layout.cells.size)
            assertTrue(layout.arrowLane.bottom < layout.indices.first().y)
            assertTrue(layout.table.bottom < layout.rolling.y)
            assertTrue(layout.rolling.bottom <= layout.height)
            assertTrue(layout.spaceBadge.y >= layout.formula.bottom)
            assertTrue(layout.spaceBadge.bottom <= layout.rolling.bottom)
            assertTrue(layout.cells.all { it.width >= 40f })
            layout.cells.forEachIndexed { i, cell ->
                assertTrue(cell.x >= layout.table.x && cell.right <= layout.table.right && cell.bottom <= layout.table.bottom)
                layout.cells.drop(i + 1).forEach { assertTrue(cell.right < it.x) }
            }
            scene.frames.flatMap { it.dependencies }.forEach { edge ->
                assertTrue(edge.source in layout.cells.indices && edge.destination in layout.cells.indices)
            }
            scene.frames.filter { it.dependencies.size == 2 }.forEach { frame ->
                val arrows = frame.dependencies.mapIndexed { i, edge -> layout.dependencyArrow(edge, i) }
                arrows.forEachIndexed { i, arrow ->
                    val edge = frame.dependencies[i]
                    val source = layout.cells[edge.source]
                    val destination = layout.cells[edge.destination]
                    assertEquals(source.x - 2f, arrow.startX)
                    assertEquals(if (i == 0) destination.x - 2f else destination.right + 2f, arrow.endX)
                    assertTrue(arrow.startX >= layout.table.x && arrow.endX + 3f <= layout.table.right)
                    assertEquals(source.y, arrow.sourceY)
                    assertEquals(destination.y, arrow.destinationY)
                    assertTrue(arrow.laneY >= layout.arrowLane.y && arrow.laneY <= layout.arrowLane.bottom)
                }
                assertTrue(arrows[1].endX - arrows[0].endX >= 6f)
            }
        }
    }
}
