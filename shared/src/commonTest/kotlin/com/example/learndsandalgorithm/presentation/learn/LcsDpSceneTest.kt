package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LcsDpSceneTest {
    @Test
    fun compactLayoutImprovesEffectiveCellWidthAtShortViewport() {
        val scene = LcsDpSceneBuilder.build(LcsDpTrace.createTrace())
        val layout = LcsDpSceneLayout.calculate(scene, 360f)
        val scale = minOf(360f / layout.width, 224f / layout.height, 1f)
        // The previous default-metric layout was 360 x 234 with 28 dp cells.
        val previousEffectiveWidth = 28f * (224f / 234f)
        assertTrue(layout.cells.first().first().width * scale > previousEffectiveWidth)
        assertTrue(layout.cells.zipWithNext().all { (a, b) -> a.first().bottom < b.first().y })
        assertTrue(layout.summary.last().bottom <= layout.height)
    }
    @Test
    fun computedZeroIsNotConfusedWithUnprocessedZeroAndHeadersFit() {
        val scene = LcsDpSceneBuilder.build(LcsDpTrace.createTrace("ab", "xy"))
        val first = scene.frames.first { it.step.action == LcsAction.COMPUTE_CELL }
        assertEquals(0, first.step.dpMatrix[1][1])
        assertEquals(0, first.step.dpMatrix[1][2])
        assertTrue(LcsCellId(1, 1) in first.computed)
        assertTrue(LcsCellId(1, 2) !in first.computed)
        val layout = LcsDpSceneLayout.calculate(scene, 1f, 1f, 14f, 18f, 35f)
        assertTrue(layout.rowHeaders.all { it.x >= 0f && it.right <= layout.width })
        assertTrue(layout.cells.all { row -> row.zipWithNext().all { (a, b) -> a.right < b.x } })
    }
    @Test
    fun distinguishesComputedZerosAndRealDependencies() {
        val scene = LcsDpSceneBuilder.build(LcsDpTrace.createTrace())
        assertEquals(6, scene.rows)
        assertEquals(4, scene.columns)
        val first = scene.frames.first { it.step.action == LcsAction.COMPUTE_CELL }
        assertEquals(listOf(LcsCellId(0, 0)), first.dependencies)
        assertTrue(LcsCellId(1, 1) in first.computed)
        assertTrue(LcsCellId(1, 2) !in first.computed)
        val mismatch = scene.frames.first { it.step.action == LcsAction.COMPUTE_CELL && it.step.isMatch == false }
        val i = mismatch.step.currentI!!; val j = mismatch.step.currentJ!!
        assertEquals(listOf(LcsCellId(i - 1, j), LcsCellId(i, j - 1)), mismatch.dependencies)
        val layout = LcsDpSceneLayout.calculate(scene, 360f)
        assertTrue(layout.cells.flatten().all { it.x >= 0 && it.right <= layout.width && it.bottom <= layout.height })
        assertTrue(layout.summary.all { it.bottom <= layout.height })
        scene.frames.forEach { frame ->
            val s = frame.step
            val destination = if (s.currentI != null && s.currentJ != null) LcsCellId(s.currentI, s.currentJ) else null
            val edges = if (s.action == LcsAction.RECONSTRUCT && destination != null && s.nextI != null && s.nextJ != null)
                listOf(destination to LcsCellId(s.nextI, s.nextJ))
            else if (destination != null) frame.dependencies.map { it to destination } else emptyList()
            edges.forEach { (from, to) ->
                val arrow = layout.arrow(from, to)
                val a = layout.cells[from.row][from.column]; val b = layout.cells[to.row][to.column]
                assertTrue(arrow.startX in a.x..a.right && arrow.startY in a.y..a.bottom)
                assertTrue(arrow.endX in b.x..b.right && arrow.endY in b.y..b.bottom)
                assertTrue(arrow.startX != arrow.endX || arrow.startY != arrow.endY)
                val dx = arrow.endX - arrow.startX
                val dy = arrow.endY - arrow.startY
                val length = kotlin.math.sqrt(dx * dx + dy * dy)
                val ux = dx / length
                val uy = dy / length
                val points = listOf(
                    (arrow.endX - 4f * ux - 2f * uy) to (arrow.endY - 4f * uy + 2f * ux),
                    (arrow.endX - 4f * ux + 2f * uy) to (arrow.endY - 4f * uy - 2f * ux)
                )
                assertTrue(points.all { (x, y) -> x >= 0f && x <= layout.width && y >= 0f && y <= layout.height })
            }
        }
    }
}
