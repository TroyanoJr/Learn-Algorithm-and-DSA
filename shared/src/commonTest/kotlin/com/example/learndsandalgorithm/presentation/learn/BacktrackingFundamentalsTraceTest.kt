package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BacktrackingFundamentalsTraceTest {

    @Test
    fun createTraceGeneratesAllBinaryStringsOfLength2() {
        val steps = BacktrackingFundamentalsTrace.createTrace(targetLength = 2)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(BacktrackingFundamentalsAction.FINISHED, lastStep.action)
        assertEquals(listOf("00", "01", "10", "11"), lastStep.generatedStrings)
        assertEquals(4, lastStep.generatedStrings.size)
    }
}
