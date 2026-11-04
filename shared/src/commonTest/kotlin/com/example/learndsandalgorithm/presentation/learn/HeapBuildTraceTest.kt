package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeapBuildTraceTest {

    @Test
    fun stepsForBuildConstructsValidMinHeapInLinearTime() {
        val initial = listOf(9, 4, 7, 1, 0, 3)
        val steps = HeapBuildTrace.stepsForBuild(initial)

        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BuildHeapEvent.BUILD_COMPLETE, lastStep.event)
        assertEquals(listOf(0, 1, 3, 9, 4, 7), lastStep.array)
    }
}
