package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActivitySelectionTraceTest {

    @Test
    fun createTraceSelectsCorrectMaxNonOverlappingActivities() {
        val steps = ActivitySelectionTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(ActivitySelectionStatus.FINISHED, lastStep.status)
        val selectedNames = lastStep.selectedActivities.map { it.name }
        assertEquals(listOf("A", "D", "G", "J"), selectedNames)
        assertEquals(4, lastStep.selectedActivities.size)
        assertEquals(16, lastStep.lastFinish)
    }
}
