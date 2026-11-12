package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TreeTraversalSceneBuilderTest {

    @Test
    fun testFullBfsTraceAccepted() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        assertEquals(steps.size, scenes.size)
        assertTrue(scenes.isNotEmpty())
    }

    @Test
    fun testCorrectNumberOfScenesAndSubScenes() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        scenes.forEach { scene ->
            assertEquals(3, scene.subScenes.size)
            val subPaneIds = scene.subScenes.map { it.subPaneId }.toSet()
            assertEquals(
                setOf("tree_traversal_tree", "tree_traversal_queue", "tree_traversal_visited"),
                subPaneIds
            )
        }
    }

    @Test
    fun testAllSevenNodesPresentInTreeSubScene() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        val expectedNodeIds = setOf("node:N5", "node:N3", "node:N7", "node:N2", "node:N4", "node:N6", "node:N8")

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "tree_traversal_tree" }
            assertEquals(7, treeSubScene.entities.size)
            val entityElementIds = treeSubScene.entities.map { it.elementId }.toSet()
            assertEquals(expectedNodeIds, entityElementIds)
        }
    }

    @Test
    fun testSixStructuralConnectionsAreCorrect() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        val expectedBranchKeys = setOf(
            "branch:tree_slot:N5->tree_slot:N3",
            "branch:tree_slot:N5->tree_slot:N7",
            "branch:tree_slot:N3->tree_slot:N2",
            "branch:tree_slot:N3->tree_slot:N4",
            "branch:tree_slot:N7->tree_slot:N6",
            "branch:tree_slot:N7->tree_slot:N8"
        )

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "tree_traversal_tree" }
            assertEquals(6, treeSubScene.connections.size)
            val actualBranchKeys = treeSubScene.connections.map { it.connectionKey }.toSet()
            assertEquals(expectedBranchKeys, actualBranchKeys)
        }
    }

    @Test
    fun testConnectionsRetainKeysAcrossSteps() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        val firstStepConnections = scenes.first().subScenes.first { it.subPaneId == "tree_traversal_tree" }.connections.map { it.connectionKey }

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "tree_traversal_tree" }
            assertEquals(firstStepConnections, treeSubScene.connections.map { it.connectionKey })
        }
    }

    @Test
    fun testConnectionEndpointsExistInTreeEntities() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "tree_traversal_tree" }
            val entityVisualKeys = treeSubScene.entities.map { it.visualKey }.toSet()

            treeSubScene.connections.forEach { connection ->
                assertTrue(entityVisualKeys.contains(connection.fromVisualKey), "fromVisualKey ${connection.fromVisualKey} should exist in tree entities")
                assertTrue(entityVisualKeys.contains(connection.toVisualKey), "toVisualKey ${connection.toVisualKey} should exist in tree entities")
            }
        }
    }

    @Test
    fun testQueueRetainsExactOrderOfQueueValues() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        steps.forEachIndexed { stepIdx, step ->
            val queueSubScene = scenes[stepIdx].subScenes.first { it.subPaneId == "tree_traversal_queue" }
            val queueValuesInSubScene = queueSubScene.entities.map { it.dataValue.toInt() }
            assertEquals(step.queueValues, queueValuesInSubScene)
        }
    }

    @Test
    fun testQueueEntityVisualKeyStableWhileInQueue() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        // Node 7 is in queue across steps 1, 2, and 3
        val queueSubScene1 = scenes[1].subScenes.first { it.subPaneId == "tree_traversal_queue" }
        val queueSubScene2 = scenes[2].subScenes.first { it.subPaneId == "tree_traversal_queue" }

        val entity1 = queueSubScene1.entities.first { it.elementId == "node:N7" }
        val entity2 = queueSubScene2.entities.first { it.elementId == "node:N7" }

        assertEquals("queue:node:N7", entity1.visualKey)
        assertEquals("queue:node:N7", entity2.visualKey)
        assertEquals(entity1.visualKey, entity2.visualKey)
        assertEquals("q_occ:N7", entity1.occurrenceId)
        assertEquals("q_occ:N7", entity2.occurrenceId)
    }

    @Test
    fun testQueueEntriesHaveIndependentOccurrenceId() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        // Step 1 queue has [3, 7]
        val queueSubScene = scenes[1].subScenes.first { it.subPaneId == "tree_traversal_queue" }
        val q3 = queueSubScene.entities.first { it.elementId == "node:N3" }
        val q7 = queueSubScene.entities.first { it.elementId == "node:N7" }

        assertEquals("q_occ:N3", q3.occurrenceId)
        assertEquals("q_occ:N7", q7.occurrenceId)
        assertNotEquals(q3.occurrenceId, q7.occurrenceId)
    }

    @Test
    fun testVisitedRetainsExactOrderOfVisitedNodes() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        steps.forEachIndexed { stepIdx, step ->
            val visitedSubScene = scenes[stepIdx].subScenes.first { it.subPaneId == "tree_traversal_visited" }
            val visitedValuesInSubScene = visitedSubScene.entities.map { it.dataValue.toInt() }
            assertEquals(step.visitedNodes, visitedValuesInSubScene)
        }
    }

    @Test
    fun testSharedElementIdAcrossPanels() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        // Step 1 has node N3 in tree, queue, and visited
        val scene1 = scenes[1]
        val treeSub = scene1.subScenes.first { it.subPaneId == "tree_traversal_tree" }
        val queueSub = scene1.subScenes.first { it.subPaneId == "tree_traversal_queue" }
        val visitedSub = scene1.subScenes.first { it.subPaneId == "tree_traversal_visited" }

        val treeN3 = treeSub.entities.first { it.elementId == "node:N3" }
        val queueN3 = queueSub.entities.first { it.elementId == "node:N3" }
        val visitedN5 = visitedSub.entities.first { it.elementId == "node:N5" }

        assertEquals("node:N3", treeN3.elementId)
        assertEquals("node:N3", queueN3.elementId)
        assertEquals("node:N5", visitedN5.elementId)
    }

    @Test
    fun testIndependentVisualKeysAcrossPanels() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        val scene1 = scenes[1]
        val treeSub = scene1.subScenes.first { it.subPaneId == "tree_traversal_tree" }
        val queueSub = scene1.subScenes.first { it.subPaneId == "tree_traversal_queue" }

        val treeN3 = treeSub.entities.first { it.elementId == "node:N3" }
        val queueN3 = queueSub.entities.first { it.elementId == "node:N3" }

        assertEquals("tree:node:N3", treeN3.visualKey)
        assertEquals("queue:node:N3", queueN3.visualKey)
        assertNotEquals(treeN3.visualKey, queueN3.visualKey)
    }

    @Test
    fun testVisualStatesReflectStepSemantics() {
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(steps)

        // Step 1: currentNodeId = "N5"
        val scene1 = scenes[1]
        val treeSub = scene1.subScenes.first { it.subPaneId == "tree_traversal_tree" }
        val visitedSub = scene1.subScenes.first { it.subPaneId == "tree_traversal_visited" }

        val activeNode = treeSub.entities.first { it.slotKey == "tree_slot:N5" }
        assertEquals(EntityVisualState.ACTIVE, activeNode.visualState)

        val justVisitedEntity = visitedSub.entities.last()
        assertEquals(EntityVisualState.ACTIVE, justVisitedEntity.visualState)
    }

    @Test
    fun testEmptyStepsListReturnsEmptyScenesList() {
        val scenes = TreeTraversalSceneBuilder.buildLevelOrderScenes(emptyList())
        assertTrue(scenes.isEmpty())
    }

    @Test
    fun testDuplicateNodeValuesInTreeRejected() {
        val duplicateValueNodes = listOf(
            TraversalNode("N1", 5),
            TraversalNode("N2", 5) // Duplicate value 5
        )
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)

        assertFailsWith<IllegalArgumentException> {
            TreeTraversalSceneBuilder.buildLevelOrderScenes(steps, duplicateValueNodes)
        }
    }

    @Test
    fun testDuplicateNodeIdsInTreeRejected() {
        val duplicateIdNodes = listOf(
            TraversalNode("N1", 5),
            TraversalNode("N1", 3) // Duplicate ID N1
        )
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)

        assertFailsWith<IllegalArgumentException> {
            TreeTraversalSceneBuilder.buildLevelOrderScenes(steps, duplicateIdNodes)
        }
    }

    @Test
    fun testNonExistentChildReferencesRejected() {
        val invalidChildNodes = listOf(
            TraversalNode("N1", 5, leftId = "INVALID_CHILD_ID")
        )
        val steps = TreeTraversalsTrace.stepsForOrder(TraversalOrder.LEVEL_ORDER)

        assertFailsWith<IllegalArgumentException> {
            TreeTraversalSceneBuilder.buildLevelOrderScenes(steps, invalidChildNodes)
        }
    }

    @Test
    fun testNonExistentQueueOrVisitedValuesRejected() {
        val invalidStep = TreeTraversalsTraceStep(
            order = TraversalOrder.LEVEL_ORDER,
            currentNodeId = "N5",
            visitedNodes = listOf(999), // 999 does not exist in treeNodes
            explanation = "Invalid Step",
            badgeText = "INVALID"
        )

        assertFailsWith<IllegalArgumentException> {
            TreeTraversalSceneBuilder.buildLevelOrderScenes(listOf(invalidStep))
        }
    }

    @Test
    fun testNonExistentCurrentNodeIdRejected() {
        val invalidStep = TreeTraversalsTraceStep(
            order = TraversalOrder.LEVEL_ORDER,
            currentNodeId = "INVALID_CURRENT_NODE_ID",
            visitedNodes = emptyList(),
            explanation = "Invalid Current Node Step",
            badgeText = "INVALID"
        )

        assertFailsWith<IllegalArgumentException> {
            TreeTraversalSceneBuilder.buildLevelOrderScenes(listOf(invalidStep))
        }
    }
}
