package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeTraversalsTraceTest {

    @Test
    fun inOrderTraversalVisitsBstNodesInSortedOrder() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.IN_ORDER)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8), lastStep.visitedNodes)
    }

    @Test
    fun levelOrderTraversalVisitsNodesByLevelUsingQueue() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf(5, 3, 7, 2, 4, 6, 8), lastStep.visitedNodes)
    }
}
