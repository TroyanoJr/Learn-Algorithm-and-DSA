package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LcsDpTraceTest {

    @Test
    fun createTraceComputesLcsForAbcdeAndAce() {
        val steps = LcsDpTrace.createTrace(text1 = "abcde", text2 = "ace")
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(LcsAction.FINISHED, lastStep.action)
        assertEquals(3, lastStep.dpMatrix[5][3])
        assertEquals("ace", lastStep.lcsString)
    }
}
