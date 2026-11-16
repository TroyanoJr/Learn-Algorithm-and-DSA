package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

    @Test
    fun operationsTracePreservesExactThirteenStepTeachingSequence() {
        val steps = HashTableOperationsTrace.stepsForOperations()
        assertEquals(13, steps.size)
        assertEquals(
            listOf(
                "READY · CAP 4",
                "INSERT: CAT",
                "INSERT: DOG",
                "INSERT: FOX",
                "RESIZE TRIGGERED (alpha=1.0)",
                "NEW CAPACITY 8",
                "REHASH: CAT → [0]",
                "REHASH: FOX → [5]",
                "REHASH: DOG → [2]",
                "STORED 'RAT' AT [7]",
                "LOOKUP: FOX → Vixen [O(1)]",
                "DELETED: DOG FROM [2]",
                "COMPLETE"
            ),
            steps.map { it.badgeText }
        )
        assertEquals(
            listOf(
                HashOpType.INSERT, HashOpType.INSERT, HashOpType.INSERT, HashOpType.INSERT,
                HashOpType.RESIZE_REHASH, HashOpType.RESIZE_REHASH,
                HashOpType.RESIZE_REHASH, HashOpType.RESIZE_REHASH, HashOpType.RESIZE_REHASH,
                HashOpType.INSERT, HashOpType.LOOKUP, HashOpType.DELETE, HashOpType.INSERT
            ),
            steps.map { it.activeOp }
        )
        assertTrue(steps.all { it.explanation.isNotBlank() })
    }

    @Test
    fun allocationAndRehashSnapshotsUseTwoSemanticallyCorrectTables() {
        val steps = HashTableOperationsTrace.stepsForOperations()
        val allocation = steps[5]
        assertEquals(4, allocation.oldCapacity)
        assertEquals(listOf("CAT", "FOX", "DOG", null), allocation.oldBuckets?.map { it?.key })
        assertEquals(8, allocation.capacity)
        assertEquals(8, allocation.buckets.size)
        assertTrue(allocation.buckets.all { it == null })
        assertEquals(3, allocation.size)
        assertEquals(0.37, allocation.loadFactor)

        val expectedTransfers = listOf("CAT" to 0, "FOX" to 5, "DOG" to 2)
        expectedTransfers.forEachIndexed { offset, (key, index) ->
            val step = steps[6 + offset]
            assertEquals(key, step.activeKey)
            assertEquals(index, step.targetIndex)
            assertEquals(key, step.buckets[index]?.key)
            assertEquals(listOf("CAT", "FOX", "DOG", null), step.oldBuckets?.map { it?.key })
        }
        assertEquals("RAT", steps[9].buckets[7]?.key)
    }

    @Test
    fun thresholdLookupDeletionAndLoadFactorsRemainExact() {
        val steps = HashTableOperationsTrace.stepsForOperations()
        assertEquals(listOf(0.0, 0.25, 0.5, 0.75), steps.take(4).map { it.loadFactor })
        assertEquals(0.75, steps[4].loadFactor)
        assertTrue(steps[4].isThresholdExceeded)
        assertTrue(steps[4].explanation.contains("size = 4/4 (alpha = 1.0)"))
        assertTrue(steps[4].explanation.contains("alpha > 0.75 THRESHOLD EXCEEDED"))

        val lookup = steps[10]
        assertEquals(5, lookup.targetIndex)
        assertEquals("FOX", lookup.buckets[5]?.key)
        assertTrue(lookup.explanation.contains("Found 'Vixen' in O(1)"))

        val deletion = steps[11]
        assertEquals(2, deletion.targetIndex)
        assertNull(deletion.buckets[2])
        assertEquals(3, deletion.size)
        assertEquals(0.37, deletion.loadFactor)
        assertEquals(setOf("CAT", "FOX", "RAT"), steps.last().buckets.filterNotNull().map { it.key }.toSet())
    }
}
