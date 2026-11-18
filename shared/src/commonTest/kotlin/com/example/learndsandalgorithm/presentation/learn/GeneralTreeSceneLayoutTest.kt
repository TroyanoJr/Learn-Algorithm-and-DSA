package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GeneralTreeSceneLayoutTest {
    @Test fun rendererSelectionIsFundamentalsSpecific() {
        val fundamentals = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(
            TreeFundamentalsTrace.stepsForFundamentals().take(1)
        ).single()
        val binaryTreeScene = fundamentals.copy(subScenes = listOf(fundamentals.subScenes.single().copy(subPaneId = "tree_traversal_tree")))
        assertTrue(isFundamentalsGeneralTreeScene(fundamentals))
        assertTrue(!isFundamentalsGeneralTreeScene(binaryTreeScene))
    }

    @Test fun emptyInputIsSafe() {
        val result = GeneralTreeSceneLayout.calculateLayout(scene(emptyList(), emptyList()))
        assertTrue(result.nodeBounds.isEmpty())
        assertTrue(result.connectionLines.isEmpty())
        assertEquals(0f, result.requiredWidth)
        assertEquals(0f, result.requiredHeight)
    }

    @Test fun singleNodeHasMarginsAndExpectedDiameter() {
        val result = GeneralTreeSceneLayout.calculateLayout(scene(listOf("A"), emptyList()))
        val rect = result.nodeBounds.getValue("slot:A")
        assertEquals(40f, rect.width)
        assertEquals(40f, rect.height)
        assertEquals(88f, result.requiredWidth)
        assertEquals(88f, result.requiredHeight)
    }

    @Test fun handlesTwoAndThreeChildrenWithParentCentered() {
        for (count in 2..3) {
            val ids = (0..count).map { ('A'.code + it).toChar().toString() }
            val edges = ids.drop(1).mapIndexed { index, id -> connection(ids.first(), id, index) }
            val result = GeneralTreeSceneLayout.calculateLayout(scene(ids, edges))
            val root = result.nodeBounds.getValue("slot:${ids.first()}")
            val kids = ids.drop(1).map { result.nodeBounds.getValue("slot:$it") }
            assertEquals((kids.first().center.x + kids.last().center.x) / 2f, root.center.x)
            assertEquals(count, result.connectionLines.size)
        }
    }

    @Test fun fundamentalsPreservesAllNodesEdgesAndBounds() {
        val step = TreeFundamentalsTrace.stepsForFundamentals().first()
        val sub = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(listOf(step)).single().subScenes.single()
        val result = GeneralTreeSceneLayout.calculateLayout(sub)
        assertEquals(7, result.nodeBounds.size)
        assertEquals(setOf("tree_slot:A", "tree_slot:B", "tree_slot:C", "tree_slot:D", "tree_slot:E", "tree_slot:F", "tree_slot:G"), result.nodeBounds.keys)
        assertEquals(6, result.connectionLines.size)
        assertEquals(setOf("A" to "B", "A" to "C", "A" to "D", "B" to "E", "B" to "F", "C" to "G"),
            sub.connections.map { it.fromVisualKey.substringAfter("node:") to it.toVisualKey.substringAfter("node:") }.toSet())
        assertEquals(3, sub.connections.count { it.fromVisualKey == "fundamentals:node:A" })
        assertEquals(sub.connections.map { it.connectionKey }.toSet(), result.connectionLines.keys)
        assertTrue(result.nodeBounds.values.all { it.left >= 24f && it.top >= 24f && it.right <= result.requiredWidth - 24f && it.bottom <= result.requiredHeight - 24f })
        assertNoOverlap(result)
    }

    @Test fun geometryStableAcrossCalculationsAndVisualStateChanges() {
        val steps = TreeFundamentalsTrace.stepsForFundamentals()
        val scenes = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(steps)
        val first = GeneralTreeSceneLayout.calculateLayout(scenes.first().subScenes.single())
        assertEquals(first, GeneralTreeSceneLayout.calculateLayout(scenes.first().subScenes.single()))
        scenes.forEach { s -> assertEquals(first, GeneralTreeSceneLayout.calculateLayout(s.subScenes.single())) }
        val changed = scenes.first().subScenes.single().copy(entities = scenes.first().subScenes.single().entities.map {
            it.copy(visualState = EntityVisualState.ACTIVE, badgeLabel = "different badge")
        })
        assertEquals(first, GeneralTreeSceneLayout.calculateLayout(changed))
    }

    @Test fun rejectsMissingEndpointsMultipleParentsAndCycles() {
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(scene(listOf("A"), listOf(connection("A", "X", 0)))) }
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(scene(listOf("A", "B", "C"), listOf(connection("A", "C", 0), connection("B", "C", 1)))) }
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(scene(listOf("A", "B"), listOf(connection("A", "B", 0), connection("B", "A", 1)))) }
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(scene(listOf("A", "B"), emptyList())) }
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(scene(listOf("A", "B", "C"), listOf(connection("A", "B", 0)))) }
        val duplicateVisuals = scene(listOf("A", "B"), listOf(connection("A", "B", 0))).let { sub ->
            sub.copy(entities = sub.entities.map { it.copy(visualKey = "same-key") })
        }
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(duplicateVisuals) }
    }

    @Test fun rejectsDuplicateSlotAndStructuralConnectionKeys() {
        val base = scene(listOf("A", "B"), listOf(connection("A", "B", 0)))
        val duplicateSlots = base.copy(entities = base.entities.map { it.copy(slotKey = "same-slot") })
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(duplicateSlots) }

        val duplicateConnections = base.copy(connections = listOf(
            connection("A", "B", 0).copy(connectionKey = "duplicate"),
            connection("A", "B", 1).copy(connectionKey = "duplicate")
        ))
        assertFailsWith<IllegalArgumentException> { GeneralTreeSceneLayout.calculateLayout(duplicateConnections) }
    }

    private fun assertNoOverlap(layout: GeneralTreeSceneLayout.LayoutResult) {
        val rects = layout.nodeBounds.values.toList()
        for (i in rects.indices) for (j in i + 1 until rects.size) {
            val a = rects[i]; val b = rects[j]
            assertTrue(a.right <= b.left || b.right <= a.left || a.bottom <= b.top || b.bottom <= a.top, "Overlapping rectangles: $a and $b")
        }
    }

    private fun scene(ids: List<String>, edges: List<VisualConnection>) = StructureSubScene(
        subPaneId = "general_tree", type = StructureType.TREE,
        entities = ids.map(::entity), connections = edges
    )
    private fun entity(id: String) = VisualEntity("visual:$id", "node:$id", id, "slot:$id")
    private fun connection(parent: String, child: String, index: Int) = VisualConnection(
        "edge:$parent->$child:$index", "visual:$parent", "visual:$child", type = ConnectionType.STRUCTURAL_BRANCH
    )
}
