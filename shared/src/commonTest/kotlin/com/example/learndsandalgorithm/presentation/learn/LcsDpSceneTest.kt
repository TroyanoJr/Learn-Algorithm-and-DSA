package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LcsDpSceneTest {
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
    }
}
