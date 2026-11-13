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
    fun testEmptyInputBehavior() {
        val scenes = TreeFundamentalsSceneBuilder.buildFundamentalsScenes(emptyList())
        assertTrue(scenes.isEmpty())
    }
}
