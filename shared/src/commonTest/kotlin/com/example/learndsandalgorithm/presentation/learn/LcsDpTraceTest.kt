package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LcsDpTraceTest {
    @Test
    fun computationsOnlyChangeTheCurrentCellInRowMajorOrder() {
        val steps = LcsDpTrace.createTrace()
        val computations = steps.filter { it.action == LcsAction.COMPUTE_CELL }
        assertEquals((1..5).flatMap { i -> (1..3).map { j -> i to j } },
            computations.map { it.currentI!! to it.currentJ!! })
        steps.zipWithNext().filter { it.second.action == LcsAction.COMPUTE_CELL }.forEach { (before, after) ->
            val i = after.currentI!!; val j = after.currentJ!!
            for (r in after.dpMatrix.indices) for (c in after.dpMatrix[r].indices) {
                if (r != i || c != j) assertEquals(before.dpMatrix[r][c], after.dpMatrix[r][c])
            }
            val expected = if (after.isMatch == true) 1 + after.dpMatrix[i - 1][j - 1]
                else maxOf(after.dpMatrix[i - 1][j], after.dpMatrix[i][j - 1])
            assertEquals(expected, after.dpMatrix[i][j])
        }
    }
    @Test
    fun reconstructionUsesLeftAndPrefersUpOnTies() {
        val left = LcsDpTrace.createTrace("a", "ab").filter { it.action == LcsAction.RECONSTRUCT }
        assertEquals(LcsMovement.LEFT, left.first().movement)
        val tie = LcsDpTrace.createTrace("ab", "ba").filter { it.action == LcsAction.RECONSTRUCT }
        assertEquals(LcsMovement.UP, tie.first().movement)
        assertEquals("a", LcsDpTrace.createTrace("ab", "ba").last().lcsString)
    }

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
