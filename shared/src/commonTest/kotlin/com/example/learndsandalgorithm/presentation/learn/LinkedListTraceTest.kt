package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkedListTraceTest {

    @Test
    fun traversalStepsFollowExpectedScenarios() {
        val traverse = LinkedListTrace.traversalSteps("TRAVERSE")
        assertEquals(5, traverse.size)
        assertEquals(0, traverse.first().currentNode)
        assertEquals(setOf(0), traverse.first().visitedNodes)
        assertEquals(4, traverse.last().currentNode)
        assertEquals(setOf(0, 1, 2, 3, 4), traverse.last().visitedNodes)
        assertEquals("Traversal complete (5 nodes)", traverse.last().result)

        val access = LinkedListTrace.traversalSteps("ACCESS")
        assertEquals(4, access.size)
        assertEquals(3, access.last().currentNode)
        assertEquals("Result: 45 at index 3 (3 hops)", access.last().result)

        val found = LinkedListTrace.traversalSteps("FOUND")
        assertEquals(4, found.size)
        assertEquals(3, found.last().currentNode)
        assertTrue(found.last().result.startsWith("Found: 45"))

        val missing = LinkedListTrace.traversalSteps("MISSING")
        assertEquals(5, missing.size)
        assertEquals(4, missing.last().currentNode)
        assertEquals("Not found (5 comparisons)", missing.last().result)
    }

    @Test
    fun insertionAtHeadPreservesOrderLinksAndHeadPointer() {
        val steps = LinkedListTrace.insertionSteps("HEAD")
        assertEquals(3, steps.size)

        val step1 = steps[0]
        assertEquals("12", step1.head)
        assertEquals(listOf("99"), step1.detachedNodes)
        assertEquals(mapOf("99" to "NEW"), step1.badges)
        assertEquals(listOf("12", "7", "19"), step1.order)

        val step2 = steps[1]
        assertEquals("12", step2.head)
        assertTrue(step2.detachedNodes.isEmpty())
        assertTrue(step2.links.contains("99" to "12"))

        val step3 = steps[2]
        assertEquals("99", step3.head)
        assertEquals(listOf("99", "12", "7", "19"), step3.order)
        assertTrue(step3.links.contains("99" to "12"))
        assertTrue(step3.badges.isEmpty())
    }

    @Test
    fun insertionAtTailPreservesOrderLinksAndDetachedNodes() {
        val steps = LinkedListTrace.insertionSteps("TAIL")
        assertEquals(3, steps.size)

        val step1 = steps[0]
        assertEquals(listOf("55"), step1.detachedNodes)
        assertEquals(mapOf("55" to "NEW"), step1.badges)

        val step2 = steps[1]
        assertEquals("19", step2.activeNode)
        assertEquals(setOf("19"), step2.highlighted)

        val step3 = steps[2]
        assertEquals(listOf("12", "7", "19", "55"), step3.order)
        assertTrue(step3.links.contains("19" to "55"))
        assertTrue(step3.detachedNodes.isEmpty())
    }

    @Test
    fun insertionInMiddlePreservesOrderLinksAndDetachedNodes() {
        val steps = LinkedListTrace.insertionSteps("MIDDLE")
        assertEquals(3, steps.size)

        val step1 = steps[0]
        assertEquals(listOf("42"), step1.detachedNodes)

        val step2 = steps[1]
        assertTrue(step2.links.contains("42" to "19"))
        assertTrue(step2.links.contains("12" to "7"))

        val step3 = steps[2]
        assertEquals(listOf("12", "7", "42", "19"), step3.order)
        assertTrue(step3.links.contains("7" to "42"))
        assertTrue(step3.links.contains("42" to "19"))
        assertTrue(step3.detachedNodes.isEmpty())
    }

    @Test
    fun removalAtHeadPreservesHeadPointerAdvancementAndDetachedNode() {
        val steps = LinkedListTrace.removalSteps("HEAD")
        assertEquals(3, steps.size)

        val step1 = steps[0]
        assertEquals("12", step1.head)
        assertEquals(mapOf("12" to "REMOVE"), step1.badges)

        val step2 = steps[1]
        assertEquals("7", step2.head)
        assertTrue(step2.detachedNodes.isEmpty())

        val step3 = steps[2]
        assertEquals("7", step3.head)
        assertEquals(listOf("7", "19"), step3.order)
        assertEquals(listOf("12"), step3.detachedNodes)
        assertEquals(mapOf("12" to "REMOVED"), step3.badges)
    }

    @Test
    fun removalAtTailPreservesUnlinkingAndDetachedNode() {
        val steps = LinkedListTrace.removalSteps("TAIL")
        assertEquals(3, steps.size)

        val step1 = steps[0]
        assertEquals("7", step1.activeNode)
        assertEquals(mapOf("19" to "REMOVE"), step1.badges)

        val step2 = steps[1]
        assertFalse(step2.links.contains("7" to "19"))
        assertTrue(step2.links.contains("12" to "7"))

        val step3 = steps[2]
        assertEquals(listOf("12", "7"), step3.order)
        assertEquals(listOf("19"), step3.detachedNodes)
        assertEquals(mapOf("19" to "REMOVED"), step3.badges)
    }

    @Test
    fun removalInMiddlePreservesBypassingAndDetachedNode() {
        val steps = LinkedListTrace.removalSteps("MIDDLE")
        assertEquals(3, steps.size)

        val step1 = steps[0]
        assertEquals(listOf("12", "7", "19", "45"), step1.order)
        assertEquals(mapOf("19" to "REMOVE"), step1.badges)

        val step2 = steps[1]
        assertTrue(step2.links.contains("7" to "45"))

        val step3 = steps[2]
        assertEquals(listOf("12", "7", "45"), step3.order)
        assertEquals(listOf("19"), step3.detachedNodes)
        assertEquals(mapOf("19" to "REMOVED"), step3.badges)
    }
}
