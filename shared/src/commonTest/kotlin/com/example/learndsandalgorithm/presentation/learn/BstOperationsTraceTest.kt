package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BstOperationsTraceTest {

    @Test
    fun everyOperationRetainsTraceStepsAndTopology() {
        val expectedCounts = mapOf(
            BstOpType.SEARCH to 4,
            BstOpType.INSERT to 5,
            BstOpType.DELETE_CASE_1_LEAF to 3,
            BstOpType.DELETE_CASE_2_ONE_CHILD to 3,
            BstOpType.DELETE_CASE_3_TWO_CHILDREN to 4
        )
        expectedCounts.forEach { (operation, count) ->
            val steps = BstOperationsTrace.stepsForOperation(operation)
            val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)
            assertEquals(count, steps.size)
            assertTrue(steps.all { it.opType == operation && it.targetVal == steps.first().targetVal })
            assertTrue(steps.last().isTerminal)
            assertEquals(steps.map { it.explanation }, scenes.map { it.globalMessage })
            assertEquals(steps.indices.toList(), scenes.map { it.stepIndex })
            val stableIds = scenes.first().subScenes.single().entities.map { it.elementId }
            steps.forEachIndexed { index, step ->
                val tree = scenes[index].subScenes.single()
                assertEquals(stableIds, tree.entities.map { it.elementId })
                assertEquals(step.nodes.map { it.id }.toSet(), tree.entities
                    .filter { it.presenceState == EntityPresenceState.PRESENT }
                    .map { it.elementId.removePrefix("node:") }.toSet())
                step.inspectedNodeId?.let { id ->
                    val inspected = tree.entities.first { it.elementId == "node:$id" }
                    assertTrue(inspected.visualState == EntityVisualState.ACTIVE || inspected.visualState == EntityVisualState.VISITED)
                }
                val presentKeys = tree.entities.filter { it.presenceState == EntityPresenceState.PRESENT }.map { it.visualKey }.toSet()
                assertTrue(tree.connections.all { it.fromVisualKey in presentKeys && it.toVisualKey in presentKeys })
            }
        }
    }

    @Test
    fun bstSearchTraceFindsTargetInLogNTime() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.SEARCH)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals("N12", lastStep.inspectedNodeId)
    }

    @Test
    fun bstInsertTraceAttachesNewNodeAtCorrectNullBranch() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.INSERT)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        val newNode = lastStep.nodes.firstOrNull { it.id == "N6_new" }
        assertEquals(6, newNode?.value)
    }

    @Test
    fun bstDeleteCase3FindsInOrderSuccessorAndPreservesBstInvariant() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.DELETE_CASE_3_TWO_CHILDREN)
        assertTrue(steps.isNotEmpty())

        val successorStep = steps.first { it.successorVal == 12 }
        assertEquals(12, successorStep.successorVal)

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(12, lastStep.nodes.first().value)
    }
}
