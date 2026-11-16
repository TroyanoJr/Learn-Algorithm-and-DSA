package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HashTableCollisionTraceTest {

    @Test
    fun hashKeyComputesCorrectIndicesWithCapacitySeven() {
        assertEquals(6, HashTableCollisionTrace.hashKey("CAT", 7))
        assertEquals(1, HashTableCollisionTrace.hashKey("DOG", 7))
        assertEquals(6, HashTableCollisionTrace.hashKey("FOX", 7)) // Collides with CAT at index 6
        assertEquals(0, HashTableCollisionTrace.hashKey("RAT", 7))
    }

    @Test
    fun separateChainingTraceAppendsCollidingEntriesToSameBucketList() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING, capacity = 7)
        assertTrue(steps.isNotEmpty())
        assertEquals(15, steps.size)

        // Initial step
        val step0 = steps.first()
        assertEquals(CollisionTraceEvent.START, step0.event)
        assertEquals(7, step0.chainingBuckets.size)
        assertTrue(step0.chainingBuckets.all { it.isEmpty() })

        // Insertion of CAT at bucket 6
        val insertCat = steps.first { it.activeKey == "CAT" && it.event == CollisionTraceEvent.INSERT_ENTRY }
        assertEquals(1, insertCat.chainingBuckets[6].size)
        assertEquals("CAT", insertCat.chainingBuckets[6][0].key)

        // Collision of FOX at bucket 6
        val collisionFox = steps.first { it.activeKey == "FOX" && it.event == CollisionTraceEvent.COLLISION_DETECTED }
        assertEquals(6, collisionFox.highlightedBucketIndex)

        // Chaining of FOX at bucket 6
        val chainFox = steps.first { it.activeKey == "FOX" && it.event == CollisionTraceEvent.CHAIN_NODE }
        assertEquals(2, chainFox.chainingBuckets[6].size)
        assertEquals("CAT", chainFox.chainingBuckets[6][0].key)
        assertEquals("FOX", chainFox.chainingBuckets[6][1].key)

        // Final step
        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CollisionTraceEvent.COMPLETE, lastStep.event)
    }

    @Test
    fun linearProbingTraceProbesNextSequentialSlotsAndHandlesClustering() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 7)
        assertTrue(steps.isNotEmpty())
        assertEquals(17, steps.size)

        // Initial step
        val step0 = steps.first()
        assertEquals(CollisionTraceEvent.START, step0.event)
        assertEquals(7, step0.linearSlots.size)

        // Collision for FOX at slot 6
        val collisionFox = steps.first { it.activeKey == "FOX" && it.event == CollisionTraceEvent.COLLISION_DETECTED }
        assertEquals(6, collisionFox.initialHashIndex)

        // FOX probed and stored at slot 0
        val insertFox = steps.first { it.activeKey == "FOX" && it.event == CollisionTraceEvent.INSERT_ENTRY }
        assertEquals(0, insertFox.highlightedBucketIndex)
        assertEquals("FOX", insertFox.linearSlots[0]?.key)

        // Collision for RAT at slot 0 (taken by FOX's probe)
        val collisionRat = steps.first { it.activeKey == "RAT" && it.event == CollisionTraceEvent.COLLISION_DETECTED }
        assertEquals(0, collisionRat.initialHashIndex)

        // RAT probes slot 1 (taken by DOG), then slot 2 (empty)
        val probeRatFull = steps.first { it.activeKey == "RAT" && it.event == CollisionTraceEvent.PROBE_NEXT_SLOT && it.currentProbeIndex == 1 }
        assertNotNull(probeRatFull)

        val insertRat = steps.first { it.activeKey == "RAT" && it.event == CollisionTraceEvent.INSERT_ENTRY }
        assertEquals(2, insertRat.highlightedBucketIndex)
        assertEquals("RAT", insertRat.linearSlots[2]?.key)

        // Terminal step
        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CollisionTraceEvent.COMPLETE, lastStep.event)

        // Verify final slot contents
        assertEquals("FOX", lastStep.linearSlots[0]?.key)
        assertEquals("DOG", lastStep.linearSlots[1]?.key)
        assertEquals("RAT", lastStep.linearSlots[2]?.key)
        assertEquals("CAT", lastStep.linearSlots[6]?.key)
    }

    @Test
    fun testCaseALinearProbingCapacityOneRejectsSecondInsertion() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 1)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CollisionTraceEvent.COMPLETE, lastStep.event)
        assertEquals("TABLE FULL", lastStep.badgeText)
        assertEquals("DOG", lastStep.activeKey)
        assertEquals(HashTableCollisionTrace.hashKey("DOG", 1), lastStep.initialHashIndex)
        assertNull(lastStep.currentProbeIndex)
        assertNull(lastStep.highlightedBucketIndex)
        assertEquals(setOf(0), lastStep.probedIndices)

        // First item "CAT" must be preserved at slot 0
        assertEquals("CAT", lastStep.linearSlots[0]?.key)
        // Second item "DOG" must be rejected and not overwrite slot 0
        assertTrue(lastStep.explanation.contains("Could not insert key 'DOG'"))

        // No INSERT_ENTRY step after table full terminal step
        val insertDogIdx = steps.indexOfFirst { it.activeKey == "DOG" && it.event == CollisionTraceEvent.INSERT_ENTRY }
        assertEquals(-1, insertDogIdx)
    }

    @Test
    fun testCaseBLinearProbingCapacityTwoDetectsFullTableWithoutOverwrite() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 2)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(CollisionTraceEvent.COMPLETE, lastStep.event)
        assertEquals("TABLE FULL", lastStep.badgeText)
        assertEquals("FOX", lastStep.activeKey)
        assertEquals(HashTableCollisionTrace.hashKey("FOX", 2), lastStep.initialHashIndex)
        assertNull(lastStep.currentProbeIndex)
        assertNull(lastStep.highlightedBucketIndex)

        // Specific assertions for Capacity 2 Full Table
        assertEquals("CAT", lastStep.linearSlots[0]?.key)
        assertEquals("DOG", lastStep.linearSlots[1]?.key)
        assertEquals(setOf(0, 1), lastStep.probedIndices)
        assertEquals(2, lastStep.linearSlots.count { it != null })
        assertTrue(lastStep.explanation.contains("Could not insert key 'FOX'"))

        // No INSERT_ENTRY step for FOX
        val insertFoxIdx = steps.indexOfFirst { it.activeKey == "FOX" && it.event == CollisionTraceEvent.INSERT_ENTRY }
        assertEquals(-1, insertFoxIdx)
    }

    @Test
    fun testCaseCWraparoundDoesNotOverwriteOccupiedSlots() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 7)
        val foxCollision = steps.first { it.activeKey == "FOX" && it.event == CollisionTraceEvent.COLLISION_DETECTED }
        assertEquals(6, foxCollision.initialHashIndex)

        val foxInsert = steps.first { it.activeKey == "FOX" && it.event == CollisionTraceEvent.INSERT_ENTRY }
        assertEquals(0, foxInsert.highlightedBucketIndex)
        assertEquals("FOX", foxInsert.linearSlots[0]?.key)
        assertEquals("CAT", foxInsert.linearSlots[6]?.key)
    }

    @Test
    fun testCaseDNormalSequenceSnapshotCounts() {
        val chainingSteps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING, capacity = 7)
        assertEquals(15, chainingSteps.size)

        val probingSteps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 7)
        assertEquals(17, probingSteps.size)

        assertEquals(32, chainingSteps.size + probingSteps.size)
    }

    @Test
    fun testCaseEInvalidCapacityIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING, capacity = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING, capacity = -5)
        }
    }
}
