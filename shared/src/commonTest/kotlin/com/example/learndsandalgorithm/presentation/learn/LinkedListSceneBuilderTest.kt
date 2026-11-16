package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkedListSceneBuilderTest {

    @Test
    fun testIntroductionScenesGeneration() {
        val nodeValues = listOf("12", "7", "19")
        val scenes = LinkedListSceneBuilder.buildIntroductionScenes(nodeValues)
        assertEquals(3, scenes.size)

        scenes.forEach { scene ->
            val subScene = scene.subScenes.first()
            assertTrue(subScene.entities.any { it.elementId == "head_ptr" })
            assertTrue(subScene.entities.any { it.elementId == "null_ptr" })
            assertEquals(nodeValues.size, subScene.entities.count { it.elementId.startsWith("elem:") })
            assertTrue(subScene.connections.any { it.fromVisualKey == "linked_list:head_ptr" })
            assertTrue(subScene.connections.any { it.toVisualKey == "linked_list:null_ptr" })
        }
    }

    @Test
    fun testTraversalScenesGeneration() {
        val nodeValues = LinkedListTrace.initialTraversalNodeValues
        val steps = LinkedListTrace.traversalSteps("TRAVERSE")
        val scenes = LinkedListSceneBuilder.buildTraversalScenes(steps, nodeValues)

        assertEquals(steps.size, scenes.size)
        scenes.forEach { scene ->
            val subScene = scene.subScenes.first()
            assertTrue(subScene.entities.isNotEmpty())
            assertTrue(subScene.connections.isNotEmpty())
        }
    }

    @Test
    fun testInsertionScenesStableIdentitiesAndConnections() {
        val steps = LinkedListTrace.insertionSteps("HEAD")
        val scenes = LinkedListSceneBuilder.buildInsertionScenes(steps)

        assertEquals(steps.size, scenes.size)
        val firstScene = scenes.first()
        val firstSub = firstScene.subScenes.first()
        assertTrue(firstSub.entities.any { it.badgeLabel == "NEW" })
        assertTrue(firstSub.connections.isNotEmpty())
    }

    @Test
    fun testRemovalScenesGenerationAndExplicitConnections() {
        val steps = LinkedListTrace.removalSteps("HEAD")
        val scenes = LinkedListSceneBuilder.buildRemovalScenes(steps)

        assertEquals(steps.size, scenes.size)
        val lastScene = scenes.last()
        val lastSub = lastScene.subScenes.first()
        assertTrue(lastSub.entities.any { it.badgeLabel == "REMOVED" })
        assertTrue(lastSub.connections.isNotEmpty())
    }
}
