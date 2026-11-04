package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HashTableOperationsTraceTest {

    @Test
    fun hashKeyComputesModuloAccordingToCapacity() {
        assertEquals(0, HashTableOperationsTrace.hashKey("CAT", 4))
        assertEquals(0, HashTableOperationsTrace.hashKey("CAT", 8))
        assertEquals(2, HashTableOperationsTrace.hashKey("DOG", 4))
        assertEquals(2, HashTableOperationsTrace.hashKey("DOG", 8))
    }

    @Test
    fun stepsForOperationsTriggersResizeAndRehashWhenThresholdExceeded() {
        val steps = HashTableOperationsTrace.stepsForOperations(initialCapacity = 4, threshold = 0.75)
        assertTrue(steps.isNotEmpty())

        // Initial step
        val step0 = steps.first()
        assertEquals(4, step0.capacity)
        assertEquals(0, step0.size)
        assertEquals(0.0, step0.loadFactor)

        // Threshold exceed step when adding 4th item (RAT)
        val thresholdStep = steps.first { it.isThresholdExceeded }
        assertNotNull(thresholdStep)
        assertEquals(HashOpType.RESIZE_REHASH, thresholdStep.activeOp)

        // Capacity doubled to 8
        val doubledStep = steps.first { it.capacity == 8 && it.activeOp == HashOpType.RESIZE_REHASH }
        assertEquals(4, doubledStep.oldCapacity)
        assertEquals(8, doubledStep.capacity)

        // Rehash steps
        val rehashCat = steps.first { it.activeKey == "CAT" && it.capacity == 8 && it.activeOp == HashOpType.RESIZE_REHASH }
        assertEquals(0, rehashCat.targetIndex)

        // Lookup FOX step
        val lookupFox = steps.first { it.activeOp == HashOpType.LOOKUP && it.activeKey == "FOX" }
        assertEquals("Vixen", lookupFox.activeValue)

        // Delete DOG step
        val deleteDog = steps.first { it.activeOp == HashOpType.DELETE && it.activeKey == "DOG" }
        assertNotNull(deleteDog)

        // Terminal step
        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
    }
}
