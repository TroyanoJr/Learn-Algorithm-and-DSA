package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MergeSortFullSceneLayoutTest {

    @Test
    fun defaultGeometryIsCompactDeterministicAndStableAcrossFrames() {
        val frames = MergeSortFullSceneBuilder.build(MergeSortFullTrace.stepsForFullSort())
        val layout = MergeSortFullSceneLayout.calculate(frames)
        assertEquals(layout, MergeSortFullSceneLayout.calculate(frames.reversed()))
        assertEquals(738f, layout.requiredSceneWidth, 0.02f)
        assertEquals(314f, layout.requiredSceneHeight)
        assertTrue(layout.requiredSceneWidth < 800f, "Default width was ${layout.requiredSceneWidth}dp")
        assertTrue(layout.requiredSceneHeight < 340f, "Default height was ${layout.requiredSceneHeight}dp")
        assertEquals(frames.last().meta.traceStep.nodes.size, layout.nodeBounds.size)
        assertEquals(9, layout.nodeBounds.size)
        assertEquals(5, layout.bufferCellBounds.size)
        assertEquals(5, layout.workingCellBounds.size)
        assertContained(layout)
        assertInside(layout.treeTitleBounds, layout.treeRegionBounds)
        assertInside(layout.detailTitleBounds, layout.detailRegionBounds)
        layout.nodeBounds.values.forEach { assertInside(it, layout.treeRegionBounds) }
        assertNoOverlap(layout.nodeBounds.values.toList())
        assertNoOverlap(layout.leftRunCellBounds.values.toList() + layout.rightRunCellBounds.values.toList())
        assertNoOverlap(layout.bufferCellBounds.values.toList())
        assertNoOverlap(layout.workingCellBounds.values.toList())
        assertNoMarkerOverlap(layout.leftRunMarkerBounds.values.toList())
        assertNoMarkerOverlap(layout.rightRunMarkerBounds.values.toList())
        assertNoMarkerOverlap(layout.bufferMarkerBounds.values.toList())
    }

    @Test
    fun topologyEdgesConnectNodeBoundariesWithoutCrossingUnrelatedNodes() {
        val frames = build(listOf(9, 2, 8, 1, 7, 3, 6, 4, 5))
        val layout = MergeSortFullSceneLayout.calculate(frames)
        val allNodes = frames.last().meta.traceStep.nodes.associateBy { it.nodeId }
        assertEquals(allNodes.keys, layout.nodeBounds.keys)
        allNodes.values.forEach { node ->
            val childIds = listOfNotNull(node.leftChildId, node.rightChildId)
            childIds.forEach { childId ->
                val key = MergeSortFullSceneBuilder.branchKey(node.nodeId, childId)
                val edge = assertNotNull(layout.connectionLines[key])
                val parent = layout.nodeBounds.getValue(node.nodeId)
                val child = layout.nodeBounds.getValue(childId)
                assertEquals(parent.center.x, edge.start.x)
                assertEquals(parent.bottom, edge.start.y)
                assertEquals(child.center.x, edge.end.x)
                assertEquals(child.top, edge.end.y)
                assertTrue(edge.start.y < edge.end.y)
                layout.nodeBounds.filterKeys { it != node.nodeId && it != childId }.values.forEach { unrelated ->
                    assertTrue(!lineCrossesInterior(edge, unrelated), "Edge $key crosses node $unrelated")
                }
            }
        }
        assertTrue(layout.treeRegionBounds.right < layout.detailRegionBounds.left)
        assertTrue(layout.requiredSceneHeight >= (layout.nodeBounds.values.maxOf { it.bottom } + 20f))
    }

    @Test
    fun detailRowsKeepLabelsMarkersCellsAndComparisonBoundsSeparate() {
        val frames = build(listOf(Int.MIN_VALUE, -1, 4, Int.MAX_VALUE) + listOf(-3, 2, 9))
        val layout = MergeSortFullSceneLayout.calculate(frames)
        assertTrue(layout.detailTitleBounds.bottom <= layout.leftRunLabelBounds.top)
        assertTrue(layout.detailTitleBounds.bottom <= layout.rightRunLabelBounds.top)
        assertTrue(layout.leftRunLabelBounds.bottom <= layout.leftRunMarkerBounds.values.first().top)
        assertTrue(layout.leftRunMarkerBounds.values.first().bottom <= layout.leftRunCellBounds.values.first().top)
        assertTrue(layout.rightRunLabelBounds.bottom <= layout.rightRunMarkerBounds.values.first().top)
        assertTrue(layout.rightRunMarkerBounds.values.first().bottom <= layout.rightRunCellBounds.values.first().top)
        assertNoOverlap(layout.leftRunCellBounds.values.toList() + layout.rightRunCellBounds.values.toList())
        assertTrue(layout.leftRunCellBounds.values.maxOfOrNull { it.bottom }!! <= layout.comparisonBounds.top)
        assertTrue(layout.rightRunCellBounds.values.maxOfOrNull { it.bottom }!! <= layout.comparisonBounds.top)
        assertTrue(layout.comparisonBounds.bottom <= layout.bufferLabelBounds.top)
        assertTrue(layout.bufferLabelBounds.bottom <= layout.bufferMarkerBounds.values.first().top)
        assertTrue(layout.bufferMarkerBounds.values.first().bottom <= layout.bufferCellBounds.values.first().top)
        assertTrue(layout.workingLabelBounds.top > layout.bufferIndexBounds.values.maxOf { it.bottom })
        assertTrue(layout.workingLabelBounds.bottom <= layout.workingCellBounds.values.first().top)
        assertTrue(layout.footerBounds.top > layout.workingCellBounds.values.maxOf { it.bottom })
        layout.bufferIndexBounds.forEach { (index, bounds) -> assertInside(bounds, layout.bufferCellBounds.getValue(index)) }
        layout.workingIndexBounds.forEach { (index, bounds) -> assertInside(bounds, layout.workingCellBounds.getValue(index)) }
        val detailElements = listOf(
            layout.detailTitleBounds,
            layout.leftRunLabelBounds,
            layout.rightRunLabelBounds,
            layout.comparisonBounds,
            layout.bufferLabelBounds,
            layout.workingLabelBounds,
            layout.footerBounds
        ) + layout.leftRunMarkerBounds.values + layout.rightRunMarkerBounds.values +
            layout.bufferMarkerBounds.values + layout.leftRunCellBounds.values + layout.rightRunCellBounds.values +
            layout.bufferCellBounds.values + layout.workingCellBounds.values
        detailElements.forEach { assertInside(it, layout.detailRegionBounds) }
        assertContained(layout)
    }

    @Test
    fun emptySingletonAndLongerInputsGrowNaturallyWithoutClipping() {
        val cases = listOf(
            emptyList<Int>(),
            listOf(Int.MIN_VALUE),
            listOf(8, 2),
            (16 downTo 0).toList(),
            listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE, 5, 5, 9, 12, 17, 21, 30, 41)
        )
        val dimensions = cases.map { input ->
            val frames = build(input)
            val layout = MergeSortFullSceneLayout.calculate(frames)
            assertContained(layout)
            assertEquals(frames.last().meta.traceStep.nodes.size, layout.nodeBounds.size)
            assertEquals(input.size, layout.bufferCellBounds.size)
            assertEquals(input.size, layout.workingCellBounds.size)
            assertEquals(input.size, frames.first().scene.subScenes.single { it.subPaneId == MergeSortFullSceneBuilder.BUFFER_SCENE_ID }.entities.size)
            if (input.isEmpty()) {
                assertTrue(layout.emptyBufferBounds != null && layout.emptyWorkingBounds != null)
            }
            layout.requiredSceneWidth to layout.requiredSceneHeight
        }
        assertTrue(dimensions[3].first > dimensions[0].first)
        assertTrue(dimensions[3].second > dimensions[0].second)
        assertTrue(dimensions[2].first <= dimensions[3].first)
    }

    private fun build(input: List<Int>) = MergeSortFullSceneBuilder.build(MergeSortFullTrace.stepsForFullSort(input))

    private fun assertContained(layout: MergeSortFullSceneLayoutResult) {
        val rectangles = listOf(
            layout.treeRegionBounds,
            layout.detailRegionBounds,
            layout.treeTitleBounds,
            layout.detailTitleBounds,
            layout.leftRunLabelBounds,
            layout.rightRunLabelBounds,
            layout.comparisonBounds,
            layout.bufferLabelBounds,
            layout.workingLabelBounds,
            layout.footerBounds
        ) + layout.nodeBounds.values + layout.leftRunMarkerBounds.values + layout.rightRunMarkerBounds.values +
            layout.bufferMarkerBounds.values + layout.leftRunCellBounds.values + layout.rightRunCellBounds.values +
            layout.bufferCellBounds.values + layout.bufferIndexBounds.values + layout.workingCellBounds.values +
            layout.workingIndexBounds.values + listOfNotNull(
                layout.emptyLeftRunBounds,
                layout.emptyRightRunBounds,
                layout.emptyBufferBounds,
                layout.emptyWorkingBounds
            )
        rectangles.forEach { bounds ->
            assertTrue(bounds.left >= 0f && bounds.top >= 0f, "Negative bounds $bounds")
            assertTrue(bounds.right <= layout.requiredSceneWidth + 0.001f, "Right edge overflows: $bounds")
            assertTrue(bounds.bottom <= layout.requiredSceneHeight + 0.001f, "Bottom edge overflows: $bounds")
        }
        layout.connectionLines.values.forEach { edge ->
            assertTrue(edge.start.x in 0f..layout.requiredSceneWidth && edge.end.x in 0f..layout.requiredSceneWidth)
            assertTrue(edge.start.y in 0f..layout.requiredSceneHeight && edge.end.y in 0f..layout.requiredSceneHeight)
        }
    }

    private fun assertInside(inner: Rect2D, outer: Rect2D) {
        assertTrue(inner.left >= outer.left && inner.top >= outer.top)
        assertTrue(inner.right <= outer.right && inner.bottom <= outer.bottom)
    }

    private fun assertNoOverlap(bounds: List<Rect2D>) {
        bounds.forEachIndexed { index, first ->
            bounds.drop(index + 1).forEach { second ->
                assertTrue(
                    first.right <= second.left || second.right <= first.left || first.bottom <= second.top || second.bottom <= first.top,
                    "Bounds overlap: $first and $second"
                )
            }
        }
    }

    private fun assertNoMarkerOverlap(bounds: List<Rect2D>) {
        bounds.forEachIndexed { index, first ->
            bounds.drop(index + 1).forEach { second ->
                assertTrue(first.right <= second.left || second.right <= first.left, "Markers overlap: $first and $second")
            }
        }
    }

    private fun lineCrossesInterior(edge: LineSegment2D, rect: Rect2D): Boolean {
        val minY = minOf(edge.start.y, edge.end.y)
        val maxY = maxOf(edge.start.y, edge.end.y)
        val minX = minOf(edge.start.x, edge.end.x)
        val maxX = maxOf(edge.start.x, edge.end.x)
        return minX < rect.right && maxX > rect.left && minY < rect.bottom && maxY > rect.top
    }
}
