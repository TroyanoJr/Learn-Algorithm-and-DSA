package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DpTabulationSceneTest {
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
            layout.cells.forEachIndexed { i, cell ->
                assertTrue(cell.x >= layout.table.x && cell.right <= layout.table.right && cell.bottom <= layout.table.bottom)
                layout.cells.drop(i + 1).forEach { assertTrue(cell.right < it.x) }
            }
            scene.frames.flatMap { it.dependencies }.forEach { edge ->
                assertTrue(edge.source in layout.cells.indices && edge.destination in layout.cells.indices)
            }
        }
    }
}
