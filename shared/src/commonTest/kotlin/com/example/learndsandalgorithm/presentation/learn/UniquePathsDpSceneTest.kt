package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UniquePathsDpSceneTest {
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
        assertTrue(scene.frames.last().dependencies.isEmpty())
        val layout = UniquePathsDpSceneLayout.calculate(scene, 360f)
        assertTrue(layout.cells.flatten().all { it.x >= 0 && it.right <= layout.width && it.bottom <= layout.height })
        assertTrue(layout.formula.bottom <= layout.height)
        assertFailsWith<IllegalArgumentException> { UniquePathsDpTrace.createTrace(0, 3) }
        assertFailsWith<IllegalArgumentException> { UniquePathsDpTrace.createTrace(3, -1) }
        assertTrue(scene.frames.last().step.explanation.contains("grid space: O(R * C)"))
    }
}
