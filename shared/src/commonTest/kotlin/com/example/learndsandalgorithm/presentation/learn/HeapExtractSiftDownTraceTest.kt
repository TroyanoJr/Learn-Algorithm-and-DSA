package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeapExtractSiftDownTraceTest {

    @Test
    fun stepsForExtractRemovesRootAndRestoresMinHeap() {
        val initial = listOf(1, 5, 2, 9, 7, 8, 3)
        val steps = HeapExtractSiftDownTrace.stepsForExtract(initial)

        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(ExtractSiftDownEvent.EXTRACT_COMPLETE, lastStep.event)
        assertEquals(1, lastStep.extractedMin)
        assertEquals(listOf(2, 5, 3, 9, 7, 8), lastStep.heap)
    }
}
