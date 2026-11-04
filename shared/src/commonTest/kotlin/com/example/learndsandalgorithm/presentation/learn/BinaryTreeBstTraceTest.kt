package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BinaryTreeBstTraceTest {

    @Test
    fun validBstTraceConfirmsBstOrderingProperty() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertTrue(lastStep.isValidBst)
    }

    @Test
    fun invalidBstTraceDetectsOrderingViolation() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.INVALID_BST_VIOLATION)
        assertTrue(steps.isNotEmpty())

        val violationStep = steps.first { !it.isValidBst }
        assertFalse(violationStep.isValidBst)
        assertEquals("N4_bad", violationStep.inspectedNodeId)
    }
}
