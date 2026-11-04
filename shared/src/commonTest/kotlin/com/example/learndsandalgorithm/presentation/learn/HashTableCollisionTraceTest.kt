package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
    }
}
