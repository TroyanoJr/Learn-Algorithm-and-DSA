package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HashTableApplicationsTraceTest {

    @Test
    fun frequencyCounterTraceComputesCorrectCharacterFrequencies() {
        val steps = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.FREQUENCY_COUNTER)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)

        val freqMap = lastStep.freqMap
        assertEquals(3, freqMap.size)

        val aPair = freqMap.firstOrNull { it.char == 'a' }
        val bPair = freqMap.firstOrNull { it.char == 'b' }
        val cPair = freqMap.firstOrNull { it.char == 'c' }

        assertNotNull(aPair)
        assertNotNull(bPair)
        assertNotNull(cPair)

        assertEquals(2, aPair.count)
        assertEquals(1, bPair.count)
        assertEquals(2, cPair.count)
    }

    @Test
    fun rangeQueryTraceComparesHashTableAndBstTraversal() {
        val steps = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.RANGE_QUERY_COMPARISON)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(setOf(15, 18, 22), lastStep.bstVisitedKeys)
    }

    @Test
    fun mutableKeyTraceDemonstratesLookupFailureAfterKeyMutation() {
        val steps = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.MUTABLE_KEY_PITFALL)
        assertTrue(steps.isNotEmpty())

        val mutationStep = steps.first { it.keyMutated }
        assertNotNull(mutationStep)
        assertEquals(101, mutationStep.originalKeyId)
        assertEquals(999, mutationStep.mutatedKeyId)
        assertEquals(5, mutationStep.storedSlotIndex)

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(7, lastStep.searchedSlotIndex)
    }

    @Test
    fun scenariosPreserveExactStepCountsAndOrdering() {
        val frequency = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.FREQUENCY_COUNTER)
        val range = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.RANGE_QUERY_COMPARISON)
        val mutable = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.MUTABLE_KEY_PITFALL)
        assertEquals(7, frequency.size)
        assertEquals(10, range.size)
        assertEquals(5, mutable.size)
        assertEquals(listOf(null, 0, 1, 2, 3, 4, null), frequency.map { it.currentCharIndex })
        range.drop(1).take(8).forEachIndexed { index, step -> assertEquals((0..index).toSet(), step.hashVisitedBuckets) }
        assertEquals(listOf(false, false, true, true, true), mutable.map { it.keyMutated })
        assertTrue((frequency + range + mutable).all { it.badgeText.isNotBlank() && it.explanation.isNotBlank() })
        assertEquals(
            listOf(
                "READY · INPUT: \"aabcc\"",
                "STEP 1/5: NEW CHAR 'a' → count = 1",
                "STEP 2/5: DUPLICATE 'a' → count = 2",
                "STEP 3/5: NEW CHAR 'b' → count = 1",
                "STEP 4/5: NEW CHAR 'c' → count = 1",
                "STEP 5/5: DUPLICATE 'c' → count = 2",
                "COMPLETE"
            ),
            frequency.map { it.badgeText }
        )
        assertEquals(
            listOf("READY · RANGE [10..25]") +
                listOf(42, 15, 7, 91, 18, 3, 22, 60).mapIndexed { index, value -> "HASH TABLE SCAN: [$index] = $value" } +
                "BST RANGE MATCH: {15, 18, 22}",
            range.map { it.badgeText }
        )
        assertEquals(
            listOf(
                "READY · MUTABLE KEY PITFALL",
                "INSERT: User(id=101) AT [5]",
                "KEY MUTATED: 101 → 999",
                "LOOKUP: HASH(999) → BUCKET [7]",
                "ENTRY UNFINDABLE! (TRAPPED AT [5])"
            ),
            mutable.map { it.badgeText }
        )
    }

    @Test
    fun frequencyCounterPreservesEveryIntermediateMapState() {
        val steps = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.FREQUENCY_COUNTER)
        fun mapAt(index: Int) = steps[index].freqMap.associate { it.char to it.count }
        assertEquals(emptyMap(), mapAt(0))
        assertEquals(mapOf('a' to 1), mapAt(1))
        assertEquals(mapOf('a' to 2), mapAt(2))
        assertEquals(mapOf('a' to 2, 'b' to 1), mapAt(3))
        assertEquals(mapOf('a' to 2, 'b' to 1, 'c' to 1), mapAt(4))
        assertEquals(mapOf('a' to 2, 'b' to 1, 'c' to 2), mapAt(5))
        assertEquals(mapAt(5), mapAt(6))
    }

    @Test
    fun rangeRowsAndMutableKeySemanticsRemainExact() {
        val range = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.RANGE_QUERY_COMPARISON)
        assertTrue(range.all { it.hashBucketKeys == listOf(42, 15, 7, 91, 18, 3, 22, 60) })
        assertTrue(range.all { it.bstKeys == listOf(3, 7, 15, 18, 22, 42, 60, 91) })
        assertEquals(setOf(15, 18, 22), range.last().bstVisitedKeys)
        assertTrue(range.last().explanation.contains("O(log N + k)"))

        val mutable = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.MUTABLE_KEY_PITFALL)
        assertTrue(mutable.all { it.storedSlotIndex == 5 })
        assertEquals(listOf(null, null, null, 7, 7), mutable.map { it.searchedSlotIndex })
        assertTrue(mutable[2].badgeText.contains("101 → 999"))
        assertTrue(mutable.last().explanation.contains("remains trapped in bucket [5]"))
    }
}
