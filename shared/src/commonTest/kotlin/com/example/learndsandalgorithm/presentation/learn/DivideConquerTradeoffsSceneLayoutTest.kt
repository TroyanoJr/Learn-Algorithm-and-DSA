package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DivideConquerTradeoffsSceneLayoutTest {

    @Test
    fun splitTreesAndAnnotationsStayInsidePanelsWithoutNodeOrLabelOverlap() {
        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        )
        val layout = DivideConquerTradeoffsSceneLayout.calculate(frames)
        val balanced = layout.balancedTree!!
        val unbalanced = layout.unbalancedTree!!

        assertContained(layout.sceneBounds, layout.titleBounds)
        layout.legendBounds.values.forEach { assertContained(layout.sceneBounds, it) }
        layout.legendBounds.values.forEach { legend -> assertFalse(layout.titleBounds.intersects(legend)) }
        val trees = listOf(
            balanced to frames.last().meta.balancedNodes,
            unbalanced to frames.last().meta.unbalancedNodes
        )
        trees.forEach { (panel, nodes) ->
            assertContained(layout.sceneBounds, panel.panelBounds)
            listOf(panel.titleBounds, panel.depthBounds, panel.workBounds, panel.causeBounds).forEach { assertContained(panel.panelBounds, it) }
            listOf(panel.titleBounds, panel.depthBounds, panel.workBounds, panel.causeBounds).forEachIndexed { index, bounds ->
                listOf(panel.titleBounds, panel.depthBounds, panel.workBounds, panel.causeBounds).drop(index + 1).forEach { other ->
                    assertFalse(bounds.intersects(other))
                }
            }
            panel.nodeBounds.values.forEach { assertContained(panel.panelBounds, it) }
            val distinctTops = panel.nodeBounds.values.map { it.top }.distinct().sorted()
            assertEquals(32f, distinctTops.zipWithNext().minOf { (top, next) -> next - top })
            assertEquals(8f, distinctTops.zipWithNext().minOf { (top, next) -> next - top - 24f })
            panel.nodeBounds.entries.forEachIndexed { index, (_, bounds) ->
                panel.nodeBounds.entries.drop(index + 1).forEach { (_, other) ->
                    assertFalse(bounds.intersects(other), "Node bounds overlap: $bounds / $other")
                }
            }
            assertTrue(panel.nodeBounds.values.all { it.width == 38f && it.height == 24f })
            panel.edges.forEach { edge ->
                assertContained(panel.panelBounds, edge.labelBounds)
                assertContained(panel.panelBounds, edge.line.start)
                assertContained(panel.panelBounds, edge.line.end)
                val childId = edge.connectionKey.removeSuffix(":branch")
                val child = nodes.single { it.id == childId }
                val parent = nodes.single { it.id == child.parentId }
                val parentBounds = panel.nodeBounds.getValue(parent.id)
                val childBounds = panel.nodeBounds.getValue(child.id)
                assertEquals(Point2D(parentBounds.center.x, parentBounds.bottom), edge.line.start)
                assertEquals(Point2D(childBounds.center.x, childBounds.top), edge.line.end)
                assertEquals("${child.branch}:${child.size}", edge.label)
                assertFalse(panel.nodeBounds.any { (id, bounds) ->
                    id != parent.id && id != child.id && edge.line.intersectsInterior(bounds)
                }, "Connection ${edge.connectionKey} crosses an unrelated node")
                assertFalse(panel.nodeBounds.values.any { it.intersects(edge.labelBounds) })
            }
            panel.edges.forEachIndexed { index, edge ->
                panel.edges.drop(index + 1).forEach { other ->
                    assertFalse(edge.labelBounds.intersects(other.labelBounds), "Edge labels overlap: ${edge.label} / ${other.label}")
                    val firstChild = nodes.single { it.id == edge.connectionKey.removeSuffix(":branch") }
                    val secondChild = nodes.single { it.id == other.connectionKey.removeSuffix(":branch") }
                    val firstIds = setOf(firstChild.id, firstChild.parentId)
                    val secondIds = setOf(secondChild.id, secondChild.parentId)
                    if (firstIds.intersect(secondIds).isEmpty()) {
                        assertFalse(edge.line.intersectsProperly(other.line), "Connections cross: ${edge.connectionKey} / ${other.connectionKey}")
                    }
                }
            }

            assertEquals(19, panel.nodeBounds.size)
            assertEquals(18, panel.edges.size)
            assertEquals(nodes.map { it.id }.toSet(), panel.nodeBounds.keys)
            val leaves = nodes.filter { it.highExclusive - it.low == 1 }.sortedBy { it.low }
            val leafCenters = leaves.map { panel.nodeBounds.getValue(it.id).center.x }
            assertTrue(leafCenters.zipWithNext().all { (left, right) -> left < right })
            val minimumSameDepthCenterGap = nodes.groupBy { it.depth }.values
                .mapNotNull { level ->
                    val centers = level.map { panel.nodeBounds.getValue(it.id).center.x }.sorted()
                    centers.zipWithNext().minOfOrNull { (left, right) -> right - left }
                }.minOrNull()
            assertTrue(minimumSameDepthCenterGap != null && minimumSameDepthCenterGap >= 38f)
            nodes.forEach { node ->
                val firstLeaf = leaves.single { it.low == node.low }
                val lastLeaf = leaves.single { it.highExclusive == node.highExclusive }
                val expectedCenter = (panel.nodeBounds.getValue(firstLeaf.id).center.x + panel.nodeBounds.getValue(lastLeaf.id).center.x) / 2f
                assertEquals(expectedCenter, panel.nodeBounds.getValue(node.id).center.x)
            }
            nodes.filter { it.size > 1 }.forEach { node ->
                val children = nodes.filter { it.parentId == node.id }.sortedBy { it.low }
                assertEquals(2, children.size)
                val parentX = panel.nodeBounds.getValue(node.id).center.x
                val childXs = children.map { panel.nodeBounds.getValue(it.id).center.x }
                assertTrue(parentX in childXs.min()..childXs.max())
            }
        }
        assertFalse(balanced.panelBounds.intersects(unbalanced.panelBounds))
        assertEquals(772f, layout.requiredSceneWidth)
        assertEquals(487f, layout.requiredSceneHeight)
        assertEquals(366f, balanced.panelBounds.width)
        assertEquals(366f, unbalanced.panelBounds.width)
        assertEquals(413f, balanced.panelBounds.height)
        assertEquals(413f, unbalanced.panelBounds.height)
        assertEquals(38f, balanced.nodeBounds.values.first().width)
        assertEquals(24f, balanced.nodeBounds.values.first().height)
        assertEquals(38f, unbalanced.nodeBounds.values.first().width)
        assertEquals(24f, unbalanced.nodeBounds.values.first().height)
        assertTrue(layout.requiredSceneHeight < 600f)

        val expectedCenters = listOf(0f, 40f, 80f, 100f, 140f, 160f, 200f, 240f, 260f, 300f)
        listOf(balanced to frames.last().meta.balancedNodes, unbalanced to frames.last().meta.unbalancedNodes).forEach { (panel, nodes) ->
            val centerByIndex = nodes.filter { it.highExclusive - it.low == 1 }
                .associate { leaf -> leaf.low to panel.nodeBounds.getValue(leaf.id).center.x }
            val origin = centerByIndex.getValue(0)
            expectedCenters.forEachIndexed { index, expected ->
                assertEquals(expected, centerByIndex.getValue(index) - origin)
            }
        }
    }

    @Test
    fun splitGeometryIsStableWhenFramesAreReversed() {
        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        )
        val forward = DivideConquerTradeoffsSceneLayout.calculate(frames)
        val reverse = DivideConquerTradeoffsSceneLayout.calculate(frames.reversed())

        assertEquals(forward.sceneBounds, reverse.sceneBounds)
        assertEquals(forward.balancedTree, reverse.balancedTree)
        assertEquals(forward.unbalancedTree, reverse.unbalancedTree)

        val perSnapshotLayouts = frames.map { DivideConquerTradeoffsSceneLayout.calculate(listOf(it)) }
        assertTrue(perSnapshotLayouts.all { it.sceneBounds == forward.sceneBounds })
        assertTrue(perSnapshotLayouts.all { it.balancedTree == forward.balancedTree })
        assertTrue(perSnapshotLayouts.all { it.unbalancedTree == forward.unbalancedTree })
        assertTrue(perSnapshotLayouts.all { it.balancedTree!!.edges.map(TradeoffTreeEdgeLayout::connectionKey) == forward.balancedTree!!.edges.map(TradeoffTreeEdgeLayout::connectionKey) })
        assertTrue(perSnapshotLayouts.all { it.unbalancedTree!!.edges.map(TradeoffTreeEdgeLayout::connectionKey) == forward.unbalancedTree!!.edges.map(TradeoffTreeEdgeLayout::connectionKey) })
    }

    @Test
    fun splitGeometryHandlesEmptyAndInvalidRangesWithoutUnsafeLeafLookups() {
        val empty = DivideConquerTradeoffsSceneLayout.calculate(emptyList())
        assertEquals(Rect2D(0f, 0f, 0f, 0f), empty.sceneBounds)

        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        )
        val invalidFrames = frames.map { frame ->
            val invalidNodes = frame.meta.balancedNodes.mapIndexed { index, node ->
                if (index == 0) node.copy(low = -1, highExclusive = 100) else node
            }
            frame.copy(meta = frame.meta.copy(balancedNodes = invalidNodes))
        }
        val layout = DivideConquerTradeoffsSceneLayout.calculate(invalidFrames)
        assertTrue(layout.sceneBounds.width.isFinite() && layout.sceneBounds.height.isFinite())
        assertTrue(layout.balancedTree!!.nodeBounds.values.all { bounds ->
            bounds.left.isFinite() && bounds.top.isFinite() && bounds.width.isFinite() && bounds.height.isFinite()
        })
        layout.balancedTree.nodeBounds.values.forEach { assertContained(layout.balancedTree.panelBounds, it) }
    }

    @Test
    fun compactSplitGeometrySupportsOtherPositiveInputSizes() {
        val template = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        for (inputSize in 1..16) {
            val frames = DivideConquerTradeoffsSceneBuilder.build(template.map { it.copy(inputN = inputSize) })
            val layout = DivideConquerTradeoffsSceneLayout.calculate(frames)
            val sequenceLayout = layout
            val trees = listOf(
                layout.balancedTree!! to frames.last().meta.balancedNodes,
                layout.unbalancedTree!! to frames.last().meta.unbalancedNodes
            )

            assertTrue(layout.requiredSceneWidth.isFinite() && layout.requiredSceneHeight.isFinite())
            trees.forEach { (panel, nodes) ->
                panel.nodeBounds.values.forEach { assertContained(panel.panelBounds, it) }
                panel.nodeBounds.values.forEachIndexed { index, bounds ->
                    panel.nodeBounds.values.drop(index + 1).forEach { other -> assertFalse(bounds.intersects(other)) }
                }
                panel.edges.forEach { edge ->
                    assertContained(panel.panelBounds, edge.line.start)
                    assertContained(panel.panelBounds, edge.line.end)
                    assertContained(panel.panelBounds, edge.labelBounds)
                    assertFalse(panel.nodeBounds.values.any { it.intersects(edge.labelBounds) })
                    val childId = edge.connectionKey.removeSuffix(":branch")
                    val child = nodes.single { it.id == childId }
                    val parent = nodes.single { it.id == child.parentId }
                    assertEquals(Point2D(panel.nodeBounds.getValue(parent.id).center.x, panel.nodeBounds.getValue(parent.id).bottom), edge.line.start)
                    assertEquals(Point2D(panel.nodeBounds.getValue(child.id).center.x, panel.nodeBounds.getValue(child.id).top), edge.line.end)
                }
                panel.edges.forEachIndexed { index, edge ->
                    panel.edges.drop(index + 1).forEach { other -> assertFalse(edge.labelBounds.intersects(other.labelBounds)) }
                }
            }
            val frameLayouts = frames.map { DivideConquerTradeoffsSceneLayout.calculate(listOf(it)) }
            assertTrue(frameLayouts.all { it.sceneBounds == sequenceLayout.sceneBounds })
            assertTrue(frameLayouts.all { it.balancedTree == sequenceLayout.balancedTree })
            assertTrue(frameLayouts.all { it.unbalancedTree == sequenceLayout.unbalancedTree })
        }
    }

    @Test
    fun comparisonTableAndStableSortIllustrationsHaveContainedNonOverlappingGeometry() {
        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.ALGORITHM_COMPARISON)
        )
        val layout = DivideConquerTradeoffsSceneLayout.calculate(frames)
        val scene = layout.sceneBounds

        assertContained(scene, layout.titleBounds)
        assertContained(scene, layout.comparisonHeaderBounds!!)
        assertEquals("COMPLEXITY & TRADE-OFF MATRIX:", DivideConquerTradeoffsSceneLayout.COMPARISON_MATRIX_HEADING)
        assertTrue(layout.comparisonHeaderBounds.height >= 18f)
        layout.comparisonColumnHeaderBounds.forEach { assertContained(scene, it) }
        layout.comparisonCells.values.flatten().forEach { assertContained(scene, it) }
        layout.comparisonRowBounds.values.forEach { assertContained(scene, it) }
        assertFalse(layout.titleBounds.intersects(layout.comparisonHeaderBounds))
        layout.comparisonColumnHeaderBounds.forEach { assertFalse(it.intersects(layout.comparisonHeaderBounds)) }
        assertEquals(5, layout.comparisonCells.size)
        assertEquals(setOf("typical_time", "worst_time", "auxiliary_memory", "stability", "practical_tradeoff"), layout.comparisonCells.keys)
        layout.comparisonCells.forEach { (rowId, cells) ->
            assertEquals(3, cells.size)
            val row = layout.comparisonRowBounds.getValue(rowId)
            cells.forEach { cell ->
                assertContained(row, cell)
                assertTrue(cell.height >= when (rowId) {
                    "typical_time" -> 40f
                    "worst_time", "stability" -> 48f
                    else -> 58f
                })
            }
            cells.forEachIndexed { index, cell ->
                cells.drop(index + 1).forEach { other -> assertFalse(cell.intersects(other)) }
        }
        }
        val allCells = layout.comparisonCells.values.flatten()
        allCells.forEachIndexed { index, cell ->
            allCells.drop(index + 1).forEach { other -> assertFalse(cell.intersects(other)) }
        }
        val panels = listOf(layout.mergeExampleBounds!!, layout.quickExampleBounds!!)
        panels.forEach { assertContained(scene, it) }
        assertFalse(panels[0].intersects(panels[1]))
        listOf(
            layout.mergeInputLabelBounds!!,
            layout.mergeOutputLabelBounds!!,
            layout.mergeBufferLabelBounds!!,
            layout.quickInputLabelBounds!!,
            layout.quickOutputLabelBounds!!,
            layout.quickDisclaimerBounds!!
        ).forEachIndexed { index, bounds ->
            assertContained(panels[if (index < 3) 0 else 1], bounds)
        }
        val chipBounds = layout.mergeInputRecordBounds + layout.mergeOutputRecordBounds + layout.mergeBufferRecordBounds +
            layout.quickInputRecordBounds + layout.quickOutputRecordBounds
        chipBounds.forEach { assertContained(scene, it) }
        chipBounds.forEach { chip -> allCells.forEach { cell -> assertFalse(chip.intersects(cell)) } }
        chipBounds.forEachIndexed { index, bounds ->
            chipBounds.drop(index + 1).forEach { other ->
                if (bounds.top == other.top) assertFalse(bounds.intersects(other))
            }
        }
        assertTrue(layout.comparisonRowBounds.values.none { row -> panels.any { it.intersects(row) } })
        assertEquals(868f, layout.requiredSceneWidth)
        assertEquals(492f, layout.requiredSceneHeight)
        assertContained(scene, layout.comparisonHeaderBounds)
        assertTrue(layout.requiredSceneHeight > 0f)

        val labels = listOf(
            layout.mergeInputLabelBounds!! to layout.mergeInputRecordBounds,
            layout.mergeOutputLabelBounds!! to layout.mergeOutputRecordBounds,
            layout.mergeBufferLabelBounds!! to layout.mergeBufferRecordBounds,
            layout.quickInputLabelBounds!! to layout.quickInputRecordBounds,
            layout.quickOutputLabelBounds!! to layout.quickOutputRecordBounds
        )
        labels.forEachIndexed { index, (label, records) ->
            assertContained(panels[if (index < 3) 0 else 1], label)
            records.forEach {
                assertContained(panels[if (index < 3) 0 else 1], it)
                assertFalse(label.intersects(it))
                assertFalse(layout.quickDisclaimerBounds!!.intersects(it) && index >= 3)
            }
        }
    }

    @Test
    fun compactComparisonGeometryRemainsStableForAllFrames() {
        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.ALGORITHM_COMPARISON)
        )
        val layouts = frames.map { DivideConquerTradeoffsSceneLayout.calculate(listOf(it)) }
        val sequenceLayout = DivideConquerTradeoffsSceneLayout.calculate(frames)

        assertEquals(4, frames.size)
        assertTrue(layouts.all { it.sceneBounds == sequenceLayout.sceneBounds })
        assertTrue(layouts.all { it.comparisonCells == sequenceLayout.comparisonCells })
    }

    @Test
    fun tradeoffMetadataAccessRefreshesItsScenarioBadgeAndPreservesLabels() {
        val balanced = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT).last()
        val comparison = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.ALGORITHM_COMPARISON).last()
        assertEquals(listOf("ANALYSIS SCENARIO", balanced.badgeText, "Divide & Conquer Complexity"), tradeoffMetadataLines(balanced))
        assertEquals(listOf("ANALYSIS SCENARIO", comparison.badgeText, "Divide & Conquer Complexity"), tradeoffMetadataLines(comparison))
        assertTrue(tradeoffMetadataLines(balanced) != tradeoffMetadataLines(comparison))
    }

    private fun assertContained(outer: Rect2D, inner: Rect2D) {
        assertTrue(inner.width >= 0f && inner.height >= 0f)
        assertTrue(inner.left >= outer.left - 0.001f)
        assertTrue(inner.top >= outer.top - 0.001f)
        assertTrue(inner.right <= outer.right + 0.001f)
        assertTrue(inner.bottom <= outer.bottom + 0.001f)
    }

    private fun assertContained(outer: Rect2D, point: Point2D) {
        assertTrue(point.x >= outer.left - 0.001f && point.x <= outer.right + 0.001f)
        assertTrue(point.y >= outer.top - 0.001f && point.y <= outer.bottom + 0.001f)
    }

    private fun Rect2D.intersects(other: Rect2D): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

    private fun LineSegment2D.intersectsInterior(rect: Rect2D): Boolean {
        var low = 0f
        var high = 1f
        val dx = end.x - start.x
        val dy = end.y - start.y
        fun clip(p: Float, q: Float): Boolean {
            if (p == 0f) return q >= 0f
            val ratio = q / p
            if (p < 0f) {
                if (ratio > high) return false
                if (ratio > low) low = ratio
            } else {
                if (ratio < low) return false
                if (ratio < high) high = ratio
            }
            return true
        }
        if (!clip(-dx, start.x - rect.left)) return false
        if (!clip(dx, rect.right - start.x)) return false
        if (!clip(-dy, start.y - rect.top)) return false
        if (!clip(dy, rect.bottom - start.y)) return false
        if (low > high) return false
        val midpoint = (low + high) / 2f
        val x = start.x + midpoint * dx
        val y = start.y + midpoint * dy
        return x > rect.left && x < rect.right && y > rect.top && y < rect.bottom
    }

    private fun LineSegment2D.intersectsProperly(other: LineSegment2D): Boolean {
        fun orientation(a: Point2D, b: Point2D, c: Point2D): Float =
            (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
        val first = orientation(start, end, other.start)
        val second = orientation(start, end, other.end)
        val third = orientation(other.start, other.end, start)
        val fourth = orientation(other.start, other.end, end)
        return first * second < 0f && third * fourth < 0f
    }
}
