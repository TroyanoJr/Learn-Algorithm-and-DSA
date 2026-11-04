package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeFundamentalsTraceTest {

    @Test
    fun stepsForFundamentalsGeneratesTerminologySteps() {
        val steps = TreeFundamentalsTrace.stepsForFundamentals()
        assertTrue(steps.isNotEmpty())

        val rootStep = steps.first { it.concept == TreeConcept.ROOT && it.highlightedNodeIds.contains("A") }
        assertEquals("A", rootStep.highlightedNodeIds.first())

        val leafStep = steps.first { it.concept == TreeConcept.LEAVES }
        assertEquals(setOf("E", "F", "G", "D"), leafStep.highlightedNodeIds)

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(TreeConcept.DEPTH_AND_HEIGHT, lastStep.concept)
    }
}
