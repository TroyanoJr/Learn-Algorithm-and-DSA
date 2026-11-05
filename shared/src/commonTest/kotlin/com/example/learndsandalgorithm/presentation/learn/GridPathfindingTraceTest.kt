package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GridPathfindingTraceTest {

    @Test
    fun createTraceFindsValidGridPathToTarget() {
        val steps = GridPathfindingTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(GridPathAction.FINISHED, lastStep.action)

        // Verify that target (2, 2) was reached in path
        val path = lastStep.currentPath
        assertTrue(path.isNotEmpty())
        assertEquals(GridCell(0, 0), path.first())
        assertEquals(GridCell(2, 2), path.last())
    }
}
