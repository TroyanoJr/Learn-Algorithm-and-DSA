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
}
