package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BstSceneBuilderTest {

    @Test
    fun testBinaryTreeBstTraceConversionValidBst() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST)
        val scenes = BstSceneBuilder.buildBstComparisonScenes(steps)

        assertEquals(steps.size, scenes.size)
        assertTrue(scenes.isNotEmpty())

        scenes.forEach { scene ->
            assertEquals(1, scene.subScenes.size)
            val treeSub = scene.subScenes.first()
            assertEquals(7, treeSub.entities.size)
            assertEquals(6, treeSub.connections.size)
        }
    }

    @Test
    fun testBinaryTreeBstTraceConversionInvalidBstViolation() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.INVALID_BST_VIOLATION)
        val scenes = BstSceneBuilder.buildBstComparisonScenes(steps)

        assertEquals(steps.size, scenes.size)

        // Terminal scene should contain VIOLATION state for N4_bad
        val terminalScene = scenes.last()
        val treeSub = terminalScene.subScenes.first()
        val violationEntity = treeSub.entities.first { it.elementId == "node:N4_bad" }
        assertEquals(EntityVisualState.VIOLATION, violationEntity.visualState)
    }

    @Test
    fun testBstOperationsTraceConversionSearch() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.SEARCH)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        assertEquals(steps.size, scenes.size)
        assertTrue(scenes.isNotEmpty())

        scenes.forEach { scene ->
            assertEquals(1, scene.subScenes.size)
            val treeSub = scene.subScenes.first()
            assertEquals(7, treeSub.entities.size)
        }
    }

    @Test
    fun testBstOperationsTraceConversionInsertTopology() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.INSERT)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        assertEquals(steps.size, scenes.size)

        // Step 0: Before insertion, N7->N6_new edge does NOT exist in active connections
        val initialTreeSub = scenes.first().subScenes.first()
        val initialEdge = initialTreeSub.connections.firstOrNull { it.connectionKey == "branch:tree_slot:N7->tree_slot:N6_new" }
        assertNull(initialEdge, "N7->N6_new edge must not exist before insertion")

        // Final step: N7->N6_new edge exists in active connections and node is ACTIVE
        val terminalTreeSub = scenes.last().subScenes.first()
        val terminalEdge = terminalTreeSub.connections.firstOrNull { it.connectionKey == "branch:tree_slot:N7->tree_slot:N6_new" }
        assertTrue(terminalEdge != null, "N7->N6_new edge must exist after insertion")
        val insertedEntity = terminalTreeSub.entities.first { it.elementId == "node:N6_new" }
        assertEquals(EntityVisualState.ACTIVE, insertedEntity.visualState)
    }

    @Test
    fun testBstOperationsTraceConversionDeleteLeafTopology() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.DELETE_CASE_1_LEAF)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        // Before deletion: N5->N2 edge exists in active connections
        val initialTreeSub = scenes.first().subScenes.first()
        assertTrue(initialTreeSub.connections.any { it.connectionKey == "branch:tree_slot:N5->tree_slot:N2" })

        // Final step: N5->N2 edge does NOT exist in active connections and N2 is absent
        val terminalTreeSub = scenes.last().subScenes.first()
        assertFalse(terminalTreeSub.connections.any { it.connectionKey == "branch:tree_slot:N5->tree_slot:N2" })
        val deletedEntity = terminalTreeSub.entities.first { it.elementId == "node:N2" }
        assertEquals(EntityPresenceState.DISAPPEARING, deletedEntity.presenceState)
    }

    @Test
    fun testBstOperationsTraceConversionDeleteOneChildTopology() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.DELETE_CASE_2_ONE_CHILD)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        // Before deletion: N5->N3 and N3->N4 exist in active connections
        val initialTreeSub = scenes.first().subScenes.first()
        assertTrue(initialTreeSub.connections.any { it.connectionKey == "branch:tree_slot:N5->tree_slot:N3" })
        assertTrue(initialTreeSub.connections.any { it.connectionKey == "branch:tree_slot:N3->tree_slot:N4" })

        // Final step: N5->N4 exists; N5->N3 and N3->N4 do NOT exist
        val terminalTreeSub = scenes.last().subScenes.first()
        assertTrue(terminalTreeSub.connections.any { it.connectionKey == "branch:tree_slot:N5->tree_slot:N4" })
        assertFalse(terminalTreeSub.connections.any { it.connectionKey == "branch:tree_slot:N5->tree_slot:N3" })
        assertFalse(terminalTreeSub.connections.any { it.connectionKey == "branch:tree_slot:N3->tree_slot:N4" })
    }

    @Test
    fun testBstOperationsTraceConversionDeleteTwoChildrenTopology() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.DELETE_CASE_3_TWO_CHILDREN)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        // Initial step 0: N12 is NOT marked as successor yet
        val step0Sub = scenes[0].subScenes.first()
        val node12Step0 = step0Sub.entities.first { it.elementId == "node:N12" }
        assertNull(node12Step0.badgeLabel)

        // Step 1: N12 is marked as successor
        val step1Sub = scenes[1].subScenes.first()
        val node12Step1 = step1Sub.entities.first { it.elementId == "node:N12" }
        assertEquals(EntityVisualState.VISITED, node12Step1.visualState)
        assertEquals("Successor", node12Step1.badgeLabel)

        // Step 2: N10 value is updated to 12
        val step2Sub = scenes[2].subScenes.first()
        val node10Step2 = step2Sub.entities.first { it.elementId == "node:N10" }
        assertEquals("12", node10Step2.dataValue)

        // Final step: N15->N12 edge is removed and N12 is absent
        val terminalSub = scenes.last().subScenes.first()
        assertFalse(terminalSub.connections.any { it.connectionKey == "branch:tree_slot:N15->tree_slot:N12" })
        val node12Terminal = terminalSub.entities.first { it.elementId == "node:N12" }
        assertEquals(EntityPresenceState.DISAPPEARING, node12Terminal.presenceState)
    }

    @Test
    fun testActiveStateMappingAndPriority() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST)
        val scenes = BstSceneBuilder.buildBstComparisonScenes(steps)

        val scene1 = scenes[1]
        val rootEntity = scene1.subScenes.first().entities.first { it.elementId == "node:N5" }
        assertEquals(EntityVisualState.ACTIVE, rootEntity.visualState)
    }

    @Test
    fun testDefaultStateMapping() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST)
        val scenes = BstSceneBuilder.buildBstComparisonScenes(steps)

        val scene0 = scenes[0]
        scene0.subScenes.first().entities.forEach { entity ->
            assertEquals(EntityVisualState.DEFAULT, entity.visualState)
        }
    }

    @Test
    fun testTopologyAndConnectionEndpointsPreserved() {
        val steps = BinaryTreeBstTrace.stepsForScenario(BstComparisonScenario.VALID_BST)
        val scenes = BstSceneBuilder.buildBstComparisonScenes(steps)

        val expectedIds = setOf("node:N5", "node:N3", "node:N7", "node:N2", "node:N4", "node:N6", "node:N8")
        scenes.forEach { scene ->
            val treeSub = scene.subScenes.first()
            val entityIds = treeSub.entities.map { it.elementId }.toSet()
            assertEquals(expectedIds, entityIds)
            assertEquals(6, treeSub.connections.size)
            treeSub.connections.forEach { conn ->
                assertTrue(treeSub.entities.any { it.visualKey == conn.fromVisualKey })
                assertTrue(treeSub.entities.any { it.visualKey == conn.toVisualKey })
            }
        }
    }

    @Test
    fun testEmptyInputBehavior() {
        val emptyComparison = BstSceneBuilder.buildBstComparisonScenes(emptyList())
        assertTrue(emptyComparison.isEmpty())

        val emptyOperations = BstSceneBuilder.buildBstOperationsScenes(emptyList())
        assertTrue(emptyOperations.isEmpty())
    }

    @Test
    fun testFutureInsertNodeReservesGeometryButHiddenInitially() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.INSERT)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        // Initial scene (step 0): N6_new is present in entity list (reserving geometry) but has DISAPPEARING presence state (hidden)
        val initialScene = scenes.first()
        val initialEntity = initialScene.subScenes.first().entities.first { it.elementId == "node:N6_new" }
        assertEquals(EntityPresenceState.DISAPPEARING, initialEntity.presenceState)

        // Terminal scene: N6_new becomes PRESENT (visible)
        val terminalScene = scenes.last()
        val terminalEntity = terminalScene.subScenes.first().entities.first { it.elementId == "node:N6_new" }
        assertEquals(EntityPresenceState.PRESENT, terminalEntity.presenceState)
    }

    @Test
    fun testStableSceneGeometryAcrossInsertSteps() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.INSERT)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        val firstEntityCount = scenes.first().subScenes.first().entities.size
        scenes.forEach { scene ->
            assertEquals(firstEntityCount, scene.subScenes.first().entities.size)
            assertTrue(scene.subScenes.first().referenceConnections.isNotEmpty())
        }
    }

    @Test
    fun testReferenceAndActiveConnectionsSeparation() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.INSERT)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)

        scenes.forEach { scene ->
            val treeSub = scene.subScenes.first()
            assertTrue(treeSub.referenceConnections.isNotEmpty())
            assertTrue(treeSub.connections.isNotEmpty())
        }
    }

    // ==========================================
    // TOPOLOGY INVARIANT TESTS (Batch 2.9.7.1)
    // ==========================================

    @Test
    fun testParentCannotReceiveTwoLeftChildren() {
        val step1 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 6,
            nodes = listOf(
                BstNode("P", 10, leftId = "C1"),
                BstNode("C1", 5)
            ),
            explanation = "Step 1",
            badgeText = "S1"
        )
        val step2 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 7,
            nodes = listOf(
                BstNode("P", 10, leftId = "C2"), // Attempting to assign C2 as second LEFT child of P
                BstNode("C1", 5),
                BstNode("C2", 7)
            ),
            explanation = "Step 2",
            badgeText = "S2"
        )

        val scenes = BstSceneBuilder.buildBstOperationsScenes(listOf(step1, step2))
        val refConns = scenes.first().subScenes.first().referenceConnections

        // P should have only ONE left child in referenceConnections (C1)
        val pLeftConns = refConns.filter { it.fromVisualKey == "bst_op:node:P" && it.label == "L" }
        assertEquals(1, pLeftConns.size)
        assertEquals("bst_op:node:C1", pLeftConns.first().toVisualKey)
    }

    @Test
    fun testParentCannotReceiveTwoRightChildren() {
        val step1 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 15,
            nodes = listOf(
                BstNode("P", 10, rightId = "C1"),
                BstNode("C1", 15)
            ),
            explanation = "Step 1",
            badgeText = "S1"
        )
        val step2 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 20,
            nodes = listOf(
                BstNode("P", 10, rightId = "C2"), // Attempting to assign C2 as second RIGHT child of P
                BstNode("C1", 15),
                BstNode("C2", 20)
            ),
            explanation = "Step 2",
            badgeText = "S2"
        )

        val scenes = BstSceneBuilder.buildBstOperationsScenes(listOf(step1, step2))
        val refConns = scenes.first().subScenes.first().referenceConnections

        val pRightConns = refConns.filter { it.fromVisualKey == "bst_op:node:P" && it.label == "R" }
        assertEquals(1, pRightConns.size)
        assertEquals("bst_op:node:C1", pRightConns.first().toVisualKey)
    }

    @Test
    fun testChildCannotReceiveTwoParents() {
        val step1 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 5,
            nodes = listOf(
                BstNode("P1", 10, leftId = "C"),
                BstNode("C", 5)
            ),
            explanation = "Step 1",
            badgeText = "S1"
        )
        val step2 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 5,
            nodes = listOf(
                BstNode("P1", 10),
                BstNode("P2", 20, leftId = "C"), // Attempting to assign P2 as second parent of C
                BstNode("C", 5)
            ),
            explanation = "Step 2",
            badgeText = "S2"
        )

        val scenes = BstSceneBuilder.buildBstOperationsScenes(listOf(step1, step2))
        val refConns = scenes.first().subScenes.first().referenceConnections

        val cParentConns = refConns.filter { it.toVisualKey == "bst_op:node:C" }
        assertEquals(1, cParentConns.size)
        assertEquals("bst_op:node:P1", cParentConns.first().fromVisualKey)
    }

    @Test
    fun testSelfReferenceRejected() {
        val step = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 10,
            nodes = listOf(
                BstNode("N1", 10, leftId = "N1") // Self-reference
            ),
            explanation = "Self Ref Step",
            badgeText = "S1"
        )

        val scenes = BstSceneBuilder.buildBstOperationsScenes(listOf(step))
        val refConns = scenes.first().subScenes.first().referenceConnections

        assertTrue(refConns.none { it.fromVisualKey == it.toVisualKey })
    }

    @Test
    fun testCycleRejected() {
        val step1 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 5,
            nodes = listOf(
                BstNode("A", 10, leftId = "B"),
                BstNode("B", 5)
            ),
            explanation = "Step 1",
            badgeText = "S1"
        )
        val step2 = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 5,
            nodes = listOf(
                BstNode("A", 10),
                BstNode("B", 5, leftId = "A") // B -> A would create cycle A -> B -> A
            ),
            explanation = "Step 2",
            badgeText = "S2"
        )

        val scenes = BstSceneBuilder.buildBstOperationsScenes(listOf(step1, step2))
        val refConns = scenes.first().subScenes.first().referenceConnections

        val bToA = refConns.firstOrNull { it.fromVisualKey == "bst_op:node:B" && it.toVisualKey == "bst_op:node:A" }
        assertNull(bToA, "Cycle-creating edge B -> A must be rejected in reference topology")
    }

    @Test
    fun testReferenceTopologyInvariantsSatisfied() {
        val steps = BstOperationsTrace.stepsForOperation(BstOpType.DELETE_CASE_3_TWO_CHILDREN)
        val scenes = BstSceneBuilder.buildBstOperationsScenes(steps)
        val refConns = scenes.first().subScenes.first().referenceConnections

        // At most 1 parent per child
        val childParents = refConns.groupBy { it.toVisualKey }
        childParents.forEach { (child, parents) ->
            assertTrue(parents.size <= 1, "Child $child must have at most 1 reference parent")
        }

        // At most 1 LEFT child and 1 RIGHT child per parent
        val parentOutputs = refConns.groupBy { it.fromVisualKey }
        parentOutputs.forEach { (parent, outputs) ->
            val lefts = outputs.filter { it.label == "L" }
            val rights = outputs.filter { it.label == "R" }
            assertTrue(lefts.size <= 1, "Parent $parent must have at most 1 reference LEFT child")
            assertTrue(rights.size <= 1, "Parent $parent must have at most 1 reference RIGHT child")
        }
    }
}
