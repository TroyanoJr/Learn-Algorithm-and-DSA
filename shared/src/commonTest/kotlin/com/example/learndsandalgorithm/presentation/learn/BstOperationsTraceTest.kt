package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BstOperationsTraceTest {

    @Test
    fun bstSearchTraceFindsTargetInLogNTime() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.SEARCH)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals("N12", lastStep.inspectedNodeId)
    }

    @Test
    fun bstInsertTraceAttachesNewNodeAtCorrectNullBranch() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.INSERT)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        val newNode = lastStep.nodes.firstOrNull { it.id == "N6_new" }
        assertEquals(6, newNode?.value)
    }

    @Test
    fun bstDeleteCase3FindsInOrderSuccessorAndPreservesBstInvariant() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.DELETE_CASE_3_TWO_CHILDREN)
        assertTrue(steps.isNotEmpty())

        val successorStep = steps.first { it.successorVal == 12 }
        assertEquals(12, successorStep.successorVal)

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(12, lastStep.nodes.first().value)
    }
}
