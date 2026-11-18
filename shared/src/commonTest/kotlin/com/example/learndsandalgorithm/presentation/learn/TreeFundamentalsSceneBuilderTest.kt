package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeFundamentalsSceneBuilderTest {

    @Test
    fun testTreeFundamentalsTraceConversion() {
        val steps = TreeFundamentalsTrace.stepsForFundamentals()
        val scenes = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(steps)

        assertEquals(steps.size, scenes.size)
        assertTrue(scenes.isNotEmpty())

        scenes.forEach { scene ->
            assertEquals(1, scene.subScenes.size)
            val treeSub = scene.subScenes.first()
            assertEquals("tree_fundamentals_tree", treeSub.subPaneId)
            assertEquals(7, treeSub.entities.size)
            assertEquals(6, treeSub.connections.size)
        }
    }

    @Test
    fun testNodeIdentifiersAndValuesPreserved() {
        val steps = TreeFundamentalsTrace.stepsForFundamentals()
        val scenes = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(steps)

        val expectedIds = setOf("node:A", "node:B", "node:C", "node:D", "node:E", "node:F", "node:G")
        scenes.forEach { scene ->
            val entityIds = scene.subScenes.first().entities.map { it.elementId }.toSet()
            assertEquals(expectedIds, entityIds)
        }
    }

    @Test
    fun testVisualStateAndBadgeMapping() {
        val steps = TreeFundamentalsTrace.stepsForFundamentals()
        val scenes = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(steps)

        // Step 1: Root concept highlights "A"
        val scene1 = scenes[1]
        val nodeA = scene1.subScenes.first().entities.first { it.elementId == "node:A" }
        assertEquals(EntityVisualState.ACTIVE, nodeA.visualState)
        assertEquals("Root (d=0, h=2)", nodeA.badgeLabel)
    }

    @Test
    fun testDynamicBadgesAndHighlightsRemainPresent() {
        val scenes = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(TreeFundamentalsTrace.stepsForFundamentals())

        val parentScene = scenes[2].subScenes.first()
        assertEquals(EntityVisualState.ACTIVE, parentScene.entities.first { it.elementId == "node:B" }.visualState)
        assertEquals("Parent", parentScene.entities.first { it.elementId == "node:B" }.badgeLabel)
        assertEquals("Leaf", parentScene.entities.first { it.elementId == "node:E" }.badgeLabel)

        val subtreeScene = scenes[5].subScenes.first()
        assertEquals("Subtree Root", subtreeScene.entities.first { it.elementId == "node:B" }.badgeLabel)
        assertEquals(EntityVisualState.ACTIVE, subtreeScene.entities.first { it.elementId == "node:E" }.visualState)

        val leafScene = scenes[4].subScenes.first()
        assertEquals(setOf("node:D", "node:E", "node:F", "node:G"), leafScene.entities
            .filter { it.visualState == EntityVisualState.ACTIVE }
            .map { it.elementId }
            .toSet())
    }

    @Test
    fun testEmptyInputBehavior() {
        val scenes = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(emptyList())
        assertTrue(scenes.isEmpty())
    }
}
