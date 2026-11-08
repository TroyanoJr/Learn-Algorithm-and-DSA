package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DpMemoizationTraceTest {

    @Test
    fun createTraceComputesFib5WithMemoizationAndCacheHits() {
        val steps = DpMemoizationTrace.createTrace(targetN = 5)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(MemoAction.FINISHED, lastStep.action)
        assertEquals(5L, lastStep.memoArray[5])

        // Verify cache hits occurred for repeated subproblems
        val cacheHits = steps.filter { it.action == MemoAction.CACHE_HIT }
        assertTrue(cacheHits.isNotEmpty())
    }
}
