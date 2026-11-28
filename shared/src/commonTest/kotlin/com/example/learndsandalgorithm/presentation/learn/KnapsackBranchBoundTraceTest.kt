package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class KnapsackBranchBoundTraceTest {

    private fun oracle(items: List<BbKnapsackItem>, capacity: Int): Int =
        (0 until (1 shl items.size)).maxOf { mask ->
            val weight = items.indices.filter { mask and (1 shl it) != 0 }.sumOf { items[it].weight }
            if (weight <= capacity) items.indices.filter { mask and (1 shl it) != 0 }.sumOf { items[it].value } else 0
        }

    @Test
    fun createTraceComputesKnapsackBranchBoundOptimalValue85() {
        val steps = KnapsackBranchBoundTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(KnapsackBbAction.FINISHED, lastStep.action)
        assertEquals(85, lastStep.incumbentValue)
        assertEquals(listOf("Item 1", "Item 3"), lastStep.incumbentItems)

        // Verify pruning occurred
        val pruneStep = steps.find { it.action == KnapsackBbAction.PRUNE_NODE }
        assertTrue(pruneStep != null)
    }

    @Test
    fun defaultWalkUsesValidBoundsAndResolvesEveryAlternative() {
        val steps = KnapsackBranchBoundTrace.createTrace()
        assertEquals(100.0, steps.first { it.currentNode?.id == "Root" }.currentNode?.upperBound)
        assertEquals(100L, steps.first { it.currentNode?.id == "Root" }.currentNode?.upperBoundFloor)
        assertEquals(100.0, steps.first { it.currentNode?.id?.endsWith("include Item 1") == true }.currentNode?.upperBound)
        assertEquals(97.0, steps.first { it.currentNode?.id == "Root → exclude Item 1" }.currentNode?.upperBound)
        assertEquals(45.0, steps.first { it.currentNode?.id == "Root → exclude Item 1 → exclude Item 2" }.currentNode?.upperBound)
        assertEquals(45L, steps.first { it.currentNode?.id == "Root → exclude Item 1 → exclude Item 2" }.currentNode?.upperBoundFloor)
        assertTrue(steps.any { it.action == KnapsackBbAction.REJECT_INFEASIBLE })
        assertTrue(steps.filter { it.action == KnapsackBbAction.PRUNE_NODE }.all {
            it.currentNode!!.upperBound!! <= it.currentNode.comparisonThreshold!!
        })
    }

    @Test
    fun customInputsAreSortedStablyAndValidated() {
        val trace = KnapsackBranchBoundTrace.createTrace(
            listOf(BbKnapsackItem("A", 3, 6), BbKnapsackItem("B", 2, 4), BbKnapsackItem("C", 1, 1)), 3
        )
        assertEquals(listOf("A", "B", "C"), trace.first().items.map { it.name })
        assertEquals(6, trace.last().incumbentValue)
        assertFailsWith<IllegalArgumentException> {
            KnapsackBranchBoundTrace.createTrace(listOf(BbKnapsackItem("bad", 0, 1)), 1)
        }
        assertFailsWith<IllegalArgumentException> {
            KnapsackBranchBoundTrace.createTrace(listOf(BbKnapsackItem("too large", 1_000_000_001, 1)), 1)
        }
    }

    @Test
    fun branchAndBoundMatchesIndependentSubsetOracleOnEdgeFixtures() {
        val fixtures = listOf(
            emptyList<BbKnapsackItem>() to 0,
            listOf(BbKnapsackItem("A", 2, 3)) to 0,
            listOf(BbKnapsackItem("A", 5, 4), BbKnapsackItem("B", 4, 7)) to 2,
            listOf(BbKnapsackItem("A", 2, 3), BbKnapsackItem("B", 3, 4)) to 10,
            listOf(BbKnapsackItem("same", 2, 4), BbKnapsackItem("same", 3, 6), BbKnapsackItem("zero", 1, 0)) to 5,
            listOf(BbKnapsackItem("low", 5, 5), BbKnapsackItem("high", 2, 8), BbKnapsackItem("mid", 3, 6)) to 5
        )
        fixtures.forEach { (items, capacity) ->
            val trace = KnapsackBranchBoundTrace.createTrace(items, capacity)
            assertEquals(oracle(items, capacity), trace.last().incumbentValue)
            assertTrue(trace.filter { it.action == KnapsackBbAction.UPDATE_INCUMBENT }.zipWithNext().all { it.first.incumbentValue < it.second.incumbentValue })
        }
    }

    @Test
    fun fractionalFloorIsExposedSeparatelyFromDisplayedFractionalBound() {
        val trace = KnapsackBranchBoundTrace.createTrace(listOf(BbKnapsackItem("fractional", 5, 18)), 3)
        val root = trace.first { it.currentNode?.id == "Root" }.currentNode!!
        assertEquals(10.8, root.upperBound!!, absoluteTolerance = 1e-9)
        assertEquals(10L, root.upperBoundFloor)
    }

    @Test
    fun everyRecordedBoundPruneUsesTheExactIntegerUpperBoundFloor() {
        KnapsackBranchBoundTrace.createTrace().filter { it.action == KnapsackBbAction.PRUNE_NODE }.forEach { step ->
            val node = step.currentNode!!
            assertTrue(node.upperBoundFloor!! <= node.comparisonThreshold!!.toLong())
            assertTrue(node.upperBound!! >= node.upperBoundFloor.toDouble())
        }
    }

    @Test
    fun integerFloorCanPruneWhenFractionalBoundStillExceedsIncumbent() {
        val trace = KnapsackBranchBoundTrace.createTrace(
            listOf(BbKnapsackItem("A", 2, 10), BbKnapsackItem("B", 5, 18)),
            capacity = 3
        )
        val prune = trace.first { step ->
            step.action == KnapsackBbAction.PRUNE_NODE &&
                step.currentNode!!.upperBound!! > step.currentNode.comparisonThreshold!!
        }
        val node = prune.currentNode!!
        assertEquals(10L, node.upperBoundFloor)
        assertEquals(10, node.comparisonThreshold)
        assertTrue(prune.explanation.contains("floor(fractional upper bound"))
        assertTrue(prune.badgeText.contains("FLOOR 10 ≤ 10"))
    }
}
