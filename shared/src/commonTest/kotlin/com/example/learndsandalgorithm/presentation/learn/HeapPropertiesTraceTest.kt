package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeapPropertiesTraceTest {

    @Test
    fun minHeapTraceVerifiesParentChildRelationship() {
        val steps = HeapPropertiesTrace.stepsForTopic(HeapPropertyTopic.MIN_HEAP_STRUCTURE)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(listOf(2, 5, 3, 9, 7, 8), lastStep.array)
    }

    @Test
    fun indexMappingTraceComputesCorrectParentAndChildIndices() {
        val steps = HeapPropertiesTrace.stepsForTopic(HeapPropertyTopic.INDEX_MAPPING)
        assertTrue(steps.isNotEmpty())

        val step1 = steps.first()
        assertEquals(1, step1.inspectedIndex)
        assertEquals(0, step1.parentIndex)
        assertEquals(3, step1.leftChildIndex)
        assertEquals(4, step1.rightChildIndex)
    }
}
