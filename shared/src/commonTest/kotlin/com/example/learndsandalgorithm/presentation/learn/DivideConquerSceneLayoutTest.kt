package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DivideConquerSceneLayoutTest {
    @Test
    fun defaultTreeReservesStableBoundsForTheEntireSequence() {
        val frames = DivideConquerSceneBuilder.build(DivideConquerFundamentalsTrace.stepsForMaxFinder())
        val layout = DivideConquerSceneLayout.calculate(frames)

        assertEquals(layout, DivideConquerSceneLayout.calculate(frames))
        assertEquals(frames.last().meta.nodePresentations.keys, layout.nodeBounds.keys)
        frames.forEach { frame ->
            frame.scene.subScenes.single().entities.forEach { entity ->
                val id = entity.elementId.removePrefix("node:")
                assertTrue(id in layout.nodeBounds)
                assertEquals(layout.nodeBounds.getValue(id), layout.nodeBounds[id])
            }
        }
        assertTrue(layout.requiredSceneWidth > 0f)
        assertTrue(layout.requiredSceneHeight > 0f)
        assertEquals(685.92f, layout.requiredSceneWidth, 0.1f)
        assertEquals(588f, layout.requiredSceneHeight)
        assertTrue(layout.requiredSceneHeight < 800f)
        assertTrue(layout.nodeBounds.getValue("root").width > 0f)
        assertTrue(layout.nodeBounds.getValue("root").width > layout.nodeBounds.getValue("LLL").width)
        assertEquals("RECURSIVE SUBPROBLEM TREE (max(arr, low, high)):", DivideConquerSceneLayout.TREE_TITLE_TEXT)
        assertTrue(layout.treeTitleBounds.width >= 300f)
        assertContained(
            layout,
            listOf(layout.phaseHeaderBounds, layout.phaseTitleBounds, layout.treeTitleBounds, layout.combineBounds) +
                layout.nodeBounds.values + layout.phaseBadgeBounds.values + layout.connectionLabelBounds.values
        )
        assertTrue(layout.treeTitleBounds.bottom < layout.nodeBounds.getValue("root").top)
        assertNoOverlap(listOf(layout.phaseHeaderBounds, layout.treeTitleBounds) + layout.nodeBounds.values + layout.connectionLabelBounds.values)
        assertEdgeLabelsDoNotOverlapCards(layout)
        assertTrue(layout.connectionLines.values.all { it.start.y > layout.treeTitleBounds.bottom && it.end.y > layout.treeTitleBounds.bottom })
    }

    @Test
    fun variableWidthTreeCardsAndConnectionsStayInsideWithoutOverlapOrCrossingCards() {
        val inputs = listOf(
            listOf(42),
            listOf(4, 2),
            listOf(7, -1, 3, 3),
            listOf(-9, -2, -2, -4, -12),
            listOf(8, -3, 14, 0, 2, 11, -1, 4),
            listOf(Int.MIN_VALUE, 2_147_483_646, 0, Int.MAX_VALUE, -11, 17, -1)
        )
        inputs.forEach { input ->
            val frames = DivideConquerSceneBuilder.build(DivideConquerFundamentalsTrace.stepsForMaxFinder(input))
            val layout = DivideConquerSceneLayout.calculate(frames)
            assertEquals(layout, DivideConquerSceneLayout.calculate(frames.reversed()))
            val nodes = layout.nodeBounds.values.toList()
            assertNoOverlap(nodes)
            assertEdgeLabelsDoNotOverlapCards(layout)
            assertContained(layout, nodes + listOf(layout.phaseHeaderBounds, layout.phaseTitleBounds, layout.combineBounds) +
                listOf(layout.treeTitleBounds) + layout.phaseBadgeBounds.values + layout.connectionLabelBounds.values)
            assertTrue(layout.treeTitleBounds.bottom < layout.nodeBounds.getValue("root").top)
            layout.connectionLines.forEach { (key, line) ->
                assertPointContained(layout, line.start)
                assertPointContained(layout, line.end)
                val parentId = key.substringAfter("tree_slot:").substringBefore("->tree_slot:")
                val childId = key.substringAfter("->tree_slot:")
                layout.nodeBounds.forEach { (otherId, bounds) ->
                    if (otherId != parentId && otherId != childId) {
                        assertTrue(!segmentCrossesInterior(line, bounds), "Connection $key crosses unrelated card $otherId")
                    }
                }
            }
            assertTrue(layout.phaseBadgeBounds.keys.containsAll(DivideConquerPhase.entries))
            assertEquals(layout.requiredSceneWidth - layout.combineBounds.left * 2f, layout.combineBounds.width)
        }
    }

    @Test
    fun nodeCardWidthRespondsToValuesRangesAndMaximumResultText() {
        val shortFrames = DivideConquerSceneBuilder.build(DivideConquerFundamentalsTrace.stepsForMaxFinder(listOf(1)))
        val wideFrames = DivideConquerSceneBuilder.build(
            DivideConquerFundamentalsTrace.stepsForMaxFinder(listOf(Int.MIN_VALUE, Int.MAX_VALUE))
        )
        val shortLayout = DivideConquerSceneLayout.calculate(shortFrames)
        val wideLayout = DivideConquerSceneLayout.calculate(wideFrames)

        assertTrue(wideLayout.nodeBounds.getValue("root").width > shortLayout.nodeBounds.getValue("root").width)
        assertTrue(wideLayout.requiredSceneWidth >= shortLayout.requiredSceneWidth)
        assertTrue(wideLayout.requiredSceneHeight > shortLayout.requiredSceneHeight)
    }

    @Test
    fun existingFixedCircleTreeLayoutRemainsUnchangedForOtherVisualizers() {
        val nodes = listOf(
            TraversalNode("N1", 10, leftId = "N2", rightId = "N3"),
            TraversalNode("N2", 5),
            TraversalNode("N3", 15)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        assertEquals(3, result.nodeBounds.size)
        assertEquals(2, result.connectionLines.size)
        assertTrue(result.nodeBounds.values.all { it.width == TreeSceneLayout.MIN_NODE_SIZE_DP })
    }

    private fun assertContained(layout: DivideConquerSceneLayoutResult, bounds: Collection<Rect2D>) {
        assertTrue(bounds.all {
            it.left >= 0f && it.top >= 0f &&
                it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight
        })
    }

    private fun assertPointContained(layout: DivideConquerSceneLayoutResult, point: Point2D) {
        assertTrue(point.x in 0f..layout.requiredSceneWidth)
        assertTrue(point.y in 0f..layout.requiredSceneHeight)
    }

    private fun assertNoOverlap(bounds: List<Rect2D>) {
        bounds.indices.forEach { first ->
            for (second in first + 1 until bounds.size) {
                val a = bounds[first]
                val b = bounds[second]
                assertTrue(
                    a.right <= b.left || b.right <= a.left || a.bottom <= b.top || b.bottom <= a.top,
                    "Rectangles overlap: $a and $b"
                )
            }
        }
    }

    private fun assertEdgeLabelsDoNotOverlapCards(layout: DivideConquerSceneLayoutResult) {
        layout.connectionLabelBounds.values.forEach { label ->
            layout.nodeBounds.forEach { (nodeId, card) ->
                val overlaps = label.left < card.right && card.left < label.right &&
                    label.top < card.bottom && card.top < label.bottom
                assertTrue(!overlaps, "Edge label $label overlaps node card $nodeId: $card")
            }
        }
    }

    private fun segmentCrossesInterior(line: LineSegment2D, rect: Rect2D): Boolean {
        val samples = 200
        return (1 until samples).any { index ->
            val t = index.toFloat() / samples
            val x = line.start.x + (line.end.x - line.start.x) * t
            val y = line.start.y + (line.end.y - line.start.y) * t
            x > rect.left && x < rect.right && y > rect.top && y < rect.bottom
        }
    }
}
