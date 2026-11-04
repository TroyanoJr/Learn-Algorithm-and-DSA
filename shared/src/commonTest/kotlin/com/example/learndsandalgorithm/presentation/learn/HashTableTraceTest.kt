package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HashTableTraceTest {

    @Test
    fun hashKeyComputesCharacterSumModuloCapacityCorrectly() {
        // 'C'(67) + 'A'(65) + 'T'(84) = 216 -> 216 % 8 = 0
        assertEquals(0, HashTableTrace.hashKey("CAT", 8))

        // 'D'(68) + 'O'(79) + 'G'(71) = 218 -> 218 % 8 = 2
        assertEquals(2, HashTableTrace.hashKey("DOG", 8))

        // 'F'(70) + 'O'(79) + 'X'(88) = 237 -> 237 % 8 = 5
        assertEquals(5, HashTableTrace.hashKey("FOX", 8))

        // 'S'(83) + '1'(49) + '0'(48) + '2'(50) = 230 -> 230 % 8 = 6
        assertEquals(6, HashTableTrace.hashKey("S102", 8))
    }

    @Test
    fun charCodesExtractsAsciiValuesForStringKeys() {
        val catCodes = HashTableTrace.charCodes("CAT")
        assertEquals(listOf('C' to 67, 'A' to 65, 'T' to 84), catCodes)
    }

    @Test
    fun stepsForFundamentalsGeneratesCompleteTraceWithStructureAndHashMapping() {
        val steps = HashTableTrace.stepsForFundamentals(capacity = 8)

        assertTrue(steps.isNotEmpty(), "Steps list should not be empty")

        // Initial step
        val step0 = steps.first()
        assertEquals(HashTableEvent.STRUCTURE_OVERVIEW, step0.event)
        assertEquals(8, step0.buckets.size)
        assertTrue(step0.buckets.all { it == null })
        assertFalse(step0.isTerminal)

        // Structure overview demo step
        val step1 = steps[1]
        assertEquals(HashTableEvent.STRUCTURE_OVERVIEW, step1.event)
        assertNotNull(step1.buckets[2])
        assertEquals("DOG", step1.buckets[2]?.key)
        assertNotNull(step1.buckets[6])
        assertEquals("S102", step1.buckets[6]?.key)

        // Insertion of "CAT"
        val selectCat = steps.first { it.activeKey == "CAT" && it.event == HashTableEvent.SELECT_KEY }
        assertEquals("Feline", selectCat.activeValue)

        val computeCat = steps.first { it.activeKey == "CAT" && it.event == HashTableEvent.HASH_COMPUTATION }
        assertEquals(216, computeCat.hashSum)

        val moduloCat = steps.first { it.activeKey == "CAT" && it.event == HashTableEvent.INDEX_MODULO }
        assertEquals(0, moduloCat.calculatedIndex)

        val insertCat = steps.first { it.activeKey == "CAT" && it.event == HashTableEvent.INSERT_ENTRY }
        assertEquals(HashTableEntry("CAT", "Feline"), insertCat.buckets[0])

        // Terminal step
        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(HashTableEvent.COMPLETE, lastStep.event)

        val finalOccupiedCount = lastStep.buckets.count { it != null }
        assertEquals(4, finalOccupiedCount)
        assertEquals("CAT", lastStep.buckets[0]?.key)
        assertEquals("DOG", lastStep.buckets[2]?.key)
        assertEquals("FOX", lastStep.buckets[5]?.key)
        assertEquals("S102", lastStep.buckets[6]?.key)
    }
}
