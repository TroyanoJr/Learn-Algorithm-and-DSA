package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LcsDpTraceTest {

    @Test
    fun reconstructionFollowsActualMovesAndKeepsMatrix() {
        val steps = LcsDpTrace.createTrace()
        val reconstruction = steps.filter { it.action == LcsAction.RECONSTRUCT }
        assertEquals(15, steps.count { it.action == LcsAction.COMPUTE_CELL })
        assertEquals(listOf(5 to 3, 4 to 2, 3 to 2, 2 to 1, 1 to 1),
            reconstruction.map { it.currentI!! to it.currentJ!! })
        assertEquals(listOf(4 to 2, 3 to 2, 2 to 1, 1 to 1, 0 to 0),
            reconstruction.map { it.nextI!! to it.nextJ!! })
        assertEquals(listOf("e", "e", "ce", "ce", "ace"), reconstruction.map { it.lcsString })
        assertEquals(listOf(LcsMovement.DIAGONAL, LcsMovement.UP, LcsMovement.DIAGONAL, LcsMovement.UP, LcsMovement.DIAGONAL),
            reconstruction.map { it.movement })
        assertTrue(reconstruction.all { it.dpMatrix == steps.last().dpMatrix })
        assertEquals("", LcsDpTrace.createTrace("", "abc").last().lcsString)
        assertEquals("", LcsDpTrace.createTrace("abc", "xyz").last().lcsString)
        assertEquals("aaa", LcsDpTrace.createTrace("aaaa", "aaa").last().lcsString)
    }

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
