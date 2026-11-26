package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class DpMemoizationTraceTest {
    @Test
    fun baseCasesReturnResultsWithoutFabricatedCacheHits() {
        for (n in 0..1) {
            val trace = DpMemoizationTrace.createTrace(n)
            assertEquals(n.toLong(), trace.last().result)
            assertEquals(null, trace.last().memoArray[n])
            assertTrue(trace.none { it.action == MemoAction.CACHE_HIT })
            assertTrue(trace.last().explanation.contains("No cache hits"))
            assertTrue(trace.last().callStack.isEmpty())
        }
        assertFailsWith<IllegalArgumentException> { DpMemoizationTrace.createTrace(-1) }
    }

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
