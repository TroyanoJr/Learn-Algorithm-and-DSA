package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BinaryTreeBstTraceTest {

    @Test
    fun presentationTracksDiscoveryWithoutChangingTrace() {
        val valid = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST)
        val invalid = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.INVALID_BST_VIOLATION)
        val skewed = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.BALANCED_VS_SKEWED)
        assertEquals(listOf(4, 3, 3), listOf(valid.size, invalid.size, skewed.size))
        assertTrue(valid.all { bstPresentationState(it) == BstPresentationState.VALID })
        assertTrue(invalid.dropLast(1).all { bstPresentationState(it) == BstPresentationState.CHECKING })
        assertEquals(BstPresentationState.INVALID, bstPresentationState(invalid.last()))
        assertEquals("BST VIOLATION DETECTED! Node [4] is located in the right subtree of Root [5]. Since 4 < 5, it breaks the BST ordering invariant. This is a Binary Tree, NOT a BST!", invalid.last().explanation)
        assertEquals("VIOLATION: Node [4] is in right subtree of Root [5], but 4 < 5!", invalid.last().violationMessage)
        assertEquals("Inspect Right Child [7]: Left [6] < 7, Right [8] > 7. Valid! Tree is a fully valid Binary Search Tree.", valid.last().explanation)
        assertEquals("Self-Balancing Trees (AVL/Red-Black) perform rotations to keep height O(log N).", skewed.last().explanation)
    }

    @Test
    fun persistentBadgesSurviveInspectionAndVisualStates() {
        val valid = BstSceneBuilder.buildBstComparisonScenes(BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST))
        valid.forEach { scene ->
            val nodes = scene.subScenes.single().entities.associateBy { it.dataValue }
            assertEquals("Root 5", nodes.getValue("5").badgeLabel)
            assertEquals("< 5", nodes.getValue("3").badgeLabel)
            assertEquals("> 5", nodes.getValue("7").badgeLabel)
        }
        assertEquals(EntityVisualState.ACTIVE, valid[1].subScenes.single().entities.first { it.dataValue == "5" }.visualState)
        val skewed = BstSceneBuilder.buildBstComparisonScenes(BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.BALANCED_VS_SKEWED))
        skewed.forEach { scene ->
            val nodes = scene.subScenes.single().entities.associateBy { it.dataValue }
            (1..4).forEach { assertEquals("↘ right", nodes.getValue(it.toString()).badgeLabel) }
            assertEquals("leaf", nodes.getValue("5").badgeLabel)
        }
        assertEquals(EntityVisualState.ACTIVE, skewed[1].subScenes.single().entities.first { it.dataValue == "1" }.visualState)
        val invalid = BstSceneBuilder.buildBstComparisonScenes(BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.INVALID_BST_VIOLATION))
        val violation = invalid.last().subScenes.single().entities.first { it.elementId == "node:N4_bad" }
        assertEquals(EntityVisualState.VIOLATION, violation.visualState)
        assertEquals("Violation", violation.badgeLabel)
    }

    @Test
    fun allSceneStepsRetainOriginalExplanations() {
        BstComparisonScenario.entries.forEach { scenario ->
            val steps = BinaryTreeBstTrace.stepsForScenario(scenario)
            val scenes = BstSceneBuilder.buildBstComparisonScenes(steps)
            assertEquals(steps.map { it.explanation }, scenes.map { it.globalMessage })
            assertEquals(steps.indices.toList(), scenes.map { it.stepIndex })
        }
    }

    @Test
    fun validBstTraceConfirmsBstOrderingProperty() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertTrue(lastStep.isValidBst)
    }

    @Test
    fun invalidBstTraceDetectsOrderingViolation() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.INVALID_BST_VIOLATION)
        assertTrue(steps.isNotEmpty())

        val violationStep = steps.first { !it.isValidBst }
        assertFalse(violationStep.isValidBst)
        assertEquals("N4_bad", violationStep.inspectedNodeId)
    }
}
