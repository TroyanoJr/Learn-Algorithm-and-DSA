package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UniquePathsDpSceneTest {
    @Test
    fun rectangularGridsAndHeadersRespectBounds() {
        listOf(Triple(1, 1, 1L), Triple(1, 5, 1L), Triple(5, 1, 1L),
            Triple(2, 3, 3L), Triple(3, 4, 10L), Triple(4, 3, 10L)).forEach { (rows, cols, expected) ->
            val scene = UniquePathsDpSceneBuilder.build(UniquePathsDpTrace.createTrace(rows, cols))
            assertEquals(expected, scene.frames.last().step.dpGrid.last().last())
            val layout = UniquePathsDpSceneLayout.calculate(scene, 1f, 1f, 14f, 18f, 35f)
            assertTrue(layout.rowHeaders.all { it.x >= 0f && it.right <= layout.width })
            assertTrue(layout.cells.all { row -> row.zipWithNext().all { (a, b) -> a.right < b.x } })
        }
    }
    @Test
    fun snapshotsCarryRealDependenciesAndBaseCells() {
        val scene = UniquePathsDpSceneBuilder.build(UniquePathsDpTrace.createTrace())
        assertEquals(7, scene.frames.size)
        assertEquals(6L, scene.frames.last().step.dpGrid[2][2])
        assertTrue(scene.frames.first().computed.isEmpty())
        assertEquals(5, scene.frames[1].bases.size)
        scene.frames.filter { it.step.action == UniquePathsDpAction.COMPUTE_CELL }.forEach { frame ->
            val r = frame.step.currentR!!; val c = frame.step.currentC!!
            assertEquals(listOf(PathCellId(r - 1, c), PathCellId(r, c - 1)), frame.dependencies)
            assertEquals(frame.step.topVal!! + frame.step.leftVal!!, frame.step.dpGrid[r][c])
        }
        scene.frames.zipWithNext().filter { it.second.step.action == UniquePathsDpAction.COMPUTE_CELL }
            .forEach { (before, after) ->
                val r = after.step.currentR!!; val c = after.step.currentC!!
                for (i in after.step.dpGrid.indices) for (j in after.step.dpGrid[i].indices) {
                    if (i != r || j != c) assertEquals(before.step.dpGrid[i][j], after.step.dpGrid[i][j])
                }
            }
        assertTrue(scene.frames.last().dependencies.isEmpty())
        val layout = UniquePathsDpSceneLayout.calculate(scene, 360f)
        assertTrue(layout.cells.flatten().all { it.x >= 0 && it.right <= layout.width && it.bottom <= layout.height })
        assertTrue(layout.formula.bottom <= layout.height)
        scene.frames.filter { it.dependencies.isNotEmpty() }.forEach { frame ->
            val destination = PathCellId(frame.step.currentR!!, frame.step.currentC!!)
            frame.dependencies.forEach { source ->
                val arrow = layout.dependencyArrow(source, destination)
                val from = layout.cells[source.row][source.column]
                val to = layout.cells[destination.row][destination.column]
                assertTrue(arrow.startX in from.x..from.right && arrow.startY in from.y..from.bottom)
                assertTrue(arrow.endX in to.x..to.right && arrow.endY in to.y..to.bottom)
                assertTrue(arrow.endX > arrow.startX || arrow.endY > arrow.startY)
                val dx = arrow.endX - arrow.startX
                val dy = arrow.endY - arrow.startY
                val length = kotlin.math.sqrt(dx * dx + dy * dy)
                val ux = dx / length; val uy = dy / length
                val points = listOf(
                    (arrow.endX - 4f * ux - 2f * uy) to (arrow.endY - 4f * uy + 2f * ux),
                    (arrow.endX - 4f * ux + 2f * uy) to (arrow.endY - 4f * uy - 2f * ux)
                )
                assertTrue(points.all { (x, y) -> x >= 0f && x <= layout.width && y >= 0f && y <= layout.height })
            }
        }
        assertFailsWith<IllegalArgumentException> { UniquePathsDpTrace.createTrace(0, 3) }
        assertFailsWith<IllegalArgumentException> { UniquePathsDpTrace.createTrace(3, -1) }
        assertTrue(scene.frames.last().step.explanation.contains("grid space: O(R * C)"))
    }
}
