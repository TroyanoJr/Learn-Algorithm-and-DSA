package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DivideConquerSceneBuilderTest {
    @Test
    fun framesPreserveTraceContentAndCurrentStepTopology() {
        val trace = DivideConquerFundamentalsTrace.stepsForMaxFinder()
        val frames = DivideConquerSceneBuilder.build(trace)

        assertEquals(trace.size, frames.size)
        frames.zip(trace).forEachIndexed { index, (frame, step) ->
            assertEquals(index, frame.scene.stepIndex)
            assertEquals(step.explanation, frame.scene.globalMessage)
            assertEquals(step.phase, frame.meta.phase)
            assertEquals(step.activeSubproblemId, frame.meta.activeSubproblemId)
            assertEquals(step.badgeText, frame.meta.badgeText)
            assertEquals(step.leftValue, frame.meta.leftValue)
            assertEquals(step.rightValue, frame.meta.rightValue)
            assertEquals(step.combinedValue, frame.meta.combinedValue)
            assertEquals(step.isTerminal, frame.meta.isTerminal)

            val tree = frame.scene.subScenes.single()
            assertEquals("divide_conquer_tree", tree.subPaneId)
            assertEquals(StructureType.TREE, tree.type)
            assertEquals(step.nodes.map { "node:${it.id}" }, tree.entities.map { it.elementId })
            assertEquals(step.nodes.map { it.id }.toSet(), frame.meta.nodePresentations.keys)
            val presentVisualKeys = tree.entities.map { it.visualKey }.toSet()
            tree.connections.forEach { edge ->
                assertTrue(edge.fromVisualKey in presentVisualKeys)
                assertTrue(edge.toVisualKey in presentVisualKeys)
                assertTrue(edge.label == "L" || edge.label == "R")
                assertEquals(ConnectionType.STRUCTURAL_BRANCH, edge.type)
            }
        }
    }

    @Test
    fun identitiesAndReferenceGeometryStayStableWithoutPresentingFutureNodes() {
        val trace = DivideConquerFundamentalsTrace.stepsForMaxFinder()
        val frames = DivideConquerSceneBuilder.build(trace)
        val expectedReferenceEdges = setOf(
            "branch:tree_slot:root->tree_slot:L1",
            "branch:tree_slot:root->tree_slot:R1",
            "branch:tree_slot:L1->tree_slot:LL",
            "branch:tree_slot:L1->tree_slot:LR",
            "branch:tree_slot:LL->tree_slot:LLL",
            "branch:tree_slot:LL->tree_slot:LLR",
            "branch:tree_slot:R1->tree_slot:RL",
            "branch:tree_slot:R1->tree_slot:RR"
        )
        val referenceEdges = frames.first().scene.subScenes.single().referenceConnections
        assertEquals(expectedReferenceEdges, referenceEdges.map { it.connectionKey }.toSet())
        frames.forEach { frame ->
            assertEquals(referenceEdges, frame.scene.subScenes.single().referenceConnections)
            assertEquals(trace[frame.scene.stepIndex].nodes.map { "node:${it.id}" }, frame.scene.subScenes.single().entities.map { it.elementId })
        }

        val appearances = frames.flatMap { frame -> frame.scene.subScenes.single().entities }
            .groupBy { it.elementId }
        appearances.forEach { (id, entities) ->
            assertTrue(entities.all { it.visualKey == entities.first().visualKey })
            assertTrue(entities.all { it.slotKey == "tree_slot:${id.removePrefix("node:")}" })
            assertTrue(entities.all { it.badgeLabel == entities.first().badgeLabel })
            assertTrue(entities.all { it.dataValue == entities.first().dataValue })
        }

        val firstFrame = frames.first()
        assertEquals(listOf("node:root"), firstFrame.scene.subScenes.single().entities.map { it.elementId })
        assertTrue(firstFrame.scene.subScenes.single().connections.isEmpty())
        val beforeRightSplit = frames[3].scene.subScenes.single()
        assertTrue(beforeRightSplit.entities.none { it.elementId == "node:RL" || it.elementId == "node:RR" })
        assertTrue(beforeRightSplit.connections.none { it.connectionKey.contains("tree_slot:RL") || it.connectionKey.contains("tree_slot:RR") })
    }

    @Test
    fun combineConnectionsAndSolvedNodeStatesAreHighlighted() {
        val frames = DivideConquerSceneBuilder.build(DivideConquerFundamentalsTrace.stepsForMaxFinder())
        val combineFrames = frames.filter { it.meta.phase == DivideConquerPhase.COMBINE }
        assertEquals(4, combineFrames.size)
        combineFrames.forEach { frame ->
            val activeId = assertNotNull(frame.meta.activeSubproblemId)
            val activeVisualKey = "divide_conquer:node:$activeId"
            val activeEntity = frame.scene.subScenes.single().entities.single { it.visualKey == activeVisualKey }
            assertEquals(EntityVisualState.TARGET, activeEntity.visualState)
            val activeEdges = frame.scene.subScenes.single().connections.filter { it.fromVisualKey == activeVisualKey }
            assertEquals(2, activeEdges.size)
            assertTrue(activeEdges.all { it.visualState == ConnectionVisualState.HIGHLIGHTED })
            assertTrue(frame.scene.subScenes.single().connections.filter { it !in activeEdges }.all { it.visualState == ConnectionVisualState.DEFAULT })
        }

        val divideToBaseCases = frames[3].meta.nodePresentations
        assertTrue(divideToBaseCases.getValue("LLL").isBaseCase)
        assertEquals(null, divideToBaseCases.getValue("LLL").maxValue)
        assertTrue(divideToBaseCases.getValue("LLR").isBaseCase)
        assertEquals(null, divideToBaseCases.getValue("LLR").maxValue)
        val conquerFrame = frames.single { it.meta.phase == DivideConquerPhase.CONQUER }
        assertEquals(4, conquerFrame.meta.nodePresentations.getValue("LLL").maxValue)
        assertEquals(2, conquerFrame.meta.nodePresentations.getValue("LLR").maxValue)
        assertTrue(conquerFrame.meta.nodePresentations.values.filter { it.isBaseCase }.all { it.maxValue != null })
        assertTrue(conquerFrame.meta.nodePresentations.values.filterNot { it.isBaseCase }.all { it.maxValue == null })
        assertTrue(frames[5].scene.subScenes.single().entities
            .filter { it.elementId in setOf("node:LLL", "node:LLR", "node:LR", "node:RL", "node:RR") }
            .all { it.visualState == EntityVisualState.VISITED })
    }

    @Test
    fun combineResultIsRenderedFromSceneMetadata() {
        val finalFrame = DivideConquerSceneBuilder.build(DivideConquerFundamentalsTrace.stepsForMaxFinder()).last()
        assertEquals(9, finalFrame.meta.combinedValue)
        assertEquals("3. COMBINE COMPLETE: max(leftMax=9, rightMax=7) = 9. Original problem solved: Maximum value in [4, 2, 9, 7, 5] is 9!", finalFrame.meta.explanation)
        assertEquals(9, finalFrame.meta.nodePresentations.getValue("root").maxValue)
        assertTrue(finalFrame.meta.isTerminal)
    }
}
