package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BranchBoundGeometrySceneTest {
    private fun fitScale(width: Float, height: Float, paneWidth: Float, paneHeight: Float): Float =
        minOf(1f, paneWidth / width, paneHeight / height)
    @Test fun fundamentalsSceneHasCompleteStableTopology() {
        val frames = BranchBoundFundamentalsSceneBuilder.build(BranchBoundFundamentalsTrace.createTrace())
        assertEquals(7, frames.size)
        assertEquals(setOf("Root", "Node A", "Node B", "Node C", "Leaf A", "Leaf C"), frames.first().nodes.map { it.id }.toSet())
        assertEquals(5, frames.first().connections.size)
        assertEquals(null, frames[1].nodes.first { it.id == "Leaf A" }.feasibleCost)
        assertEquals(15, frames[2].nodes.first { it.id == "Leaf A" }.feasibleCost)
        assertEquals(null, frames[2].nodes.first { it.id == "Leaf C" }.feasibleCost)
        assertEquals(FundamentalsNodeState.PRUNED, frames[3].nodes.first { it.id == "Node B" }.state)
        assertEquals(15, frames[3].nodes.first { it.id == "Node B" }.comparisonThreshold)
        assertEquals(FundamentalsNodeState.OPTIMAL, frames.last().nodes.first { it.id == "Leaf C" }.state)
        val layout = BranchBoundFundamentalsSceneLayoutCalculator.calculate()
        assertTrue(layout.nodes.values.all { it.left >= 0 && it.top >= 0 && it.width > 0 && it.height > 0 })
        val expandedLayout = BranchBoundFundamentalsSceneLayoutCalculator.calculate(156f)
        assertTrue(expandedLayout.width > layout.width)
        assertTrue(expandedLayout.nodes.values.all { it.right <= expandedLayout.width })
    }

    @Test fun defaultBranchAndBoundLessonsPreserveApprovedTotalSnapshotCount() {
        val total = BranchBoundFundamentalsTrace.createTrace().size +
            BoundsPruningTrace.createTrace().size +
            BestFirstSearchTrace.createTrace().size +
            KnapsackBranchBoundTrace.createTrace().size
        assertEquals(37, total)
    }

    @Test fun boundsAndBestFirstScenesPreserveSequenceSemantics() {
        val bounds = BoundsPruningSceneBuilder.build(BoundsPruningTrace.createTrace())
        assertEquals(7, bounds.size); assertEquals(BoundsNodeState.INCUMBENT, bounds[5].maxNodes.first { it.id == "Leaf X" }.state)
        assertTrue(bounds.take(5).all { frame -> frame.maxNodes.none { it.id == "Leaf X" } })
        assertEquals(10, bounds[1].minNodes.first { it.id == "Node A" }.threshold)
        assertEquals(BoundsNodeState.KEEP, bounds[1].minNodes.first { it.id == "Node A" }.state)
        assertEquals(10, bounds[2].minNodes.first { it.id == "Node B" }.threshold)
        assertEquals(BoundsNodeState.PRUNED, bounds[2].minNodes.first { it.id == "Node B" }.state)
        assertEquals(40, bounds[3].maxNodes.first { it.id == "Node X" }.threshold)
        assertEquals(BoundsNodeState.KEEP, bounds[3].maxNodes.first { it.id == "Node X" }.state)
        assertEquals(40, bounds[4].maxNodes.first { it.id == "Node Y" }.threshold)
        assertEquals(BoundsNodeState.PRUNED, bounds[4].maxNodes.first { it.id == "Node Y" }.state)
        val best = BestFirstSearchSceneBuilder.build(BestFirstSearchTrace.createTrace())
        assertEquals(6, best.size); assertEquals(listOf("Node 4", "Node 2", "Node 1", "Node 3"), best[1].orderedQueueIds)
        assertEquals(best[1].orderedQueueIds, best[1].queueNodes.map { it.id })
        assertEquals(BestFirstQueueState.INITIAL_EMPTY, best.first().queueState)
        assertEquals(BestFirstQueueState.EMPTIED_BY_PRUNING, best[4].queueState)
        assertEquals(3, best[4].pruned.size)
        assertEquals(null, best[2].incumbent)
        assertEquals(false, best[2].showActualCost)
        assertEquals(7, best[3].incumbent?.cost)
        assertEquals(7, best.last().incumbent?.cost)
        val fiveQueue = BestFirstSearchSceneLayoutCalculator.calculate(5)
        assertEquals(5, fiveQueue.queue.size)
        assertTrue(fiveQueue.queue.all { it.right <= fiveQueue.width })
        val boundsLayout = BoundsPruningSceneLayoutCalculator.calculate()
        assertTrue(boundsLayout.min.all { it.width > 0 && it.height > 0 })
        assertEquals(3, boundsLayout.max.size)
        assertTrue(boundsLayout.max.all { it.right <= boundsLayout.width && it.bottom <= boundsLayout.height })
    }

    @Test fun knapsackSceneKeepsIntegerAndFractionalStatesSeparate() {
        val frames = KnapsackBranchBoundSceneBuilder.build(KnapsackBranchBoundTrace.createTrace())
        assertTrue(frames.size > 7)
        val final = frames.last()
        assertEquals(9, final.integerWeight)
        assertEquals(listOf("Item 1", "Item 3"), final.step.incumbentItems)
        assertTrue(final.items.filter { it.incumbentMember }.map { it.item.name }.containsAll(listOf("Item 1", "Item 3")))
        val root = frames.first { it.step.currentNode?.id == "Root" }
        assertTrue(root.items.any { it.fullRelaxation })
        assertTrue(root.items.any { it.partialRelaxation })
        assertEquals(listOf(4.0, 6.0), root.capacitySegments.map { it.weight })
        assertEquals(listOf(40.0, 60.0), root.capacitySegments.map { it.value })
        assertEquals(listOf(0.4, 0.6), root.capacitySegments.map { it.weight / root.step.capacity })
        val rejected = frames.first { it.step.action == KnapsackBbAction.REJECT_INFEASIBLE }
        assertTrue(rejected.items.any { it.rejectedAttempt })
        assertTrue(rejected.capacitySegments.isEmpty())
        val layout = KnapsackBranchBoundSceneLayoutCalculator.calculate(3)
        assertTrue(layout.items.all { it.width > 0 && it.height > 0 })
        assertTrue(layout.height <= 320f)
        assertTrue(layout.capacityBar.left >= layout.branch.left && layout.capacityBar.right <= layout.branch.right)
        assertTrue(layout.capacityBar.top >= layout.branch.top && layout.formula.bottom <= layout.branch.bottom)
        val fourItems = KnapsackBranchBoundSceneLayoutCalculator.calculate(4)
        assertTrue(fourItems.items.all { it.right <= fourItems.width && it.bottom <= fourItems.height })
        val empty = KnapsackBranchBoundSceneLayoutCalculator.calculate(0)
        assertTrue(empty.width > 0 && empty.height > 0)
    }

    @Test fun defaultLayoutsFitBothAxesAtRepresentativePanes() {
        val layouts = listOf(
            BranchBoundFundamentalsSceneLayoutCalculator.calculate().let { it.width to it.height },
            BoundsPruningSceneLayoutCalculator.calculate().let { it.width to it.height },
            BestFirstSearchSceneLayoutCalculator.calculate().let { it.width to it.height },
            KnapsackBranchBoundSceneLayoutCalculator.calculate(3).let { it.width to it.height }
        )
        val panes = listOf(360f to 224f, 400f to 260f, 460f to 320f, 240f to 224f)
        layouts.forEach { (width, height) -> panes.forEach { (paneWidth, paneHeight) ->
            val scale = fitScale(width, height, paneWidth, paneHeight)
            assertTrue(scale > 0f && scale <= 1f)
            assertTrue(width * scale <= paneWidth + 0.01f)
            assertTrue(height * scale <= paneHeight + 0.01f)
        } }
    }
}
