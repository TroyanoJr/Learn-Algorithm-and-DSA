package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HeapSceneBuilderTest {

    @Test
    fun testEmptyTraceReturnsEmptySceneList() {
        val emptyScenes = HeapSceneBuilder.buildScenes(emptyList())
        assertTrue(emptyScenes.isEmpty())
    }

    @Test
    fun testCompleteTraceStartingWithStartIsAccepted() {
        val steps = HeapBuildTrace.stepsForBuild()
        val scenes = HeapSceneBuilder.buildScenes(steps)
        assertEquals(steps.size, scenes.size)
        assertEquals(BuildHeapEvent.START, steps.first().event)
    }

    @Test
    fun testTruncatedTraceStartingWithSwapDownIsRejected() {
        val fullSteps = HeapBuildTrace.stepsForBuild()
        val swapStepIdx = fullSteps.indexOfFirst { it.event == BuildHeapEvent.SWAP_DOWN }
        assertTrue(swapStepIdx >= 0)

        val truncatedSteps = fullSteps.subList(swapStepIdx, fullSteps.size)

        assertFailsWith<IllegalArgumentException> {
            HeapSceneBuilder.buildScenes(truncatedSteps)
        }
    }

    @Test
    fun testStartStepWithSwappedIndicesIsRejected() {
        val invalidFirstStep = BuildHeapTraceStep(
            array = listOf(9, 4, 7, 1, 0, 3),
            swappedIndices = Pair(0, 1), // Invalid: START step with non-null swappedIndices
            event = BuildHeapEvent.START,
            explanation = "Invalid Start Step",
            badgeText = "INVALID"
        )

        assertFailsWith<IllegalArgumentException> {
            HeapSceneBuilder.buildScenes(listOf(invalidFirstStep))
        }
    }

    @Test
    fun testDuplicateValuesMaintainDistinctElementIds() {
        // Initial array with duplicate 10s at index 0 and 2
        val initialArr = listOf(10, 30, 10, 20)
        val steps = HeapBuildTrace.stepsForBuild(initialArr)
        val scenes = HeapSceneBuilder.buildScenes(steps)

        assertTrue(scenes.isNotEmpty())

        val step0 = scenes.first()
        val treeSubScene = step0.subScenes.first { it.subPaneId == "heap_tree" }

        val node0 = treeSubScene.entities.first { it.slotKey == "tree_slot:0" }
        val node2 = treeSubScene.entities.first { it.slotKey == "tree_slot:2" }

        assertEquals("10", node0.dataValue)
        assertEquals("10", node2.dataValue)
        // Distinct elementIds for duplicate values
        assertEquals("elem:0", node0.elementId)
        assertEquals("elem:2", node2.elementId)
        assertNotEquals(node0.elementId, node2.elementId)
    }

    @Test
    fun testNoSwapsPreservesInitialIdentitiesInOrder() {
        val alreadyHeapArr = listOf(1, 2, 3, 4, 5)
        val steps = HeapBuildTrace.stepsForBuild(alreadyHeapArr)
        val scenes = HeapSceneBuilder.buildScenes(steps)

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "heap_tree" }
            treeSubScene.entities.forEachIndexed { slotIdx, entity ->
                assertEquals("tree_slot:$slotIdx", entity.slotKey)
                assertEquals("elem:$slotIdx", entity.elementId)
            }
        }
    }

    @Test
    fun testSingleSwapPermutesElementIdsCorrectly() {
        val initialArr = listOf(9, 4, 7, 1, 0, 3)
        val steps = HeapBuildTrace.stepsForBuild(initialArr)
        val scenes = HeapSceneBuilder.buildScenes(steps)

        // Find first SWAP_DOWN step
        val swapStepIdx = steps.indexOfFirst { it.event == BuildHeapEvent.SWAP_DOWN }
        assertTrue(swapStepIdx >= 0)

        val swapStep = steps[swapStepIdx]
        val (i, j) = swapStep.swappedIndices!!

        val scenePostSwap = scenes[swapStepIdx]
        val treeSubScene = scenePostSwap.subScenes.first { it.subPaneId == "heap_tree" }

        val entityAtI = treeSubScene.entities.first { it.slotKey == "tree_slot:$i" }
        val entityAtJ = treeSubScene.entities.first { it.slotKey == "tree_slot:$j" }

        // elem:j should now occupy slot i, and elem:i should occupy slot j
        assertEquals("elem:$j", entityAtI.elementId)
        assertEquals("elem:$i", entityAtJ.elementId)
    }

    @Test
    fun testMultipleSwapsPermuteIdentitiesConsistently() {
        val initialArr = listOf(9, 4, 7, 1, 0, 3)
        val steps = HeapBuildTrace.stepsForBuild(initialArr)
        val scenes = HeapSceneBuilder.buildScenes(steps)

        val finalScene = scenes.last()
        val finalTree = finalScene.subScenes.first { it.subPaneId == "heap_tree" }
        val finalArrayStep = steps.last().array

        finalTree.entities.forEachIndexed { slotIdx, entity ->
            // Check value matches step array at position
            assertEquals(finalArrayStep[slotIdx].toString(), entity.dataValue)
        }
    }

    @Test
    fun testStableIdentitiesAcrossEntireTrace() {
        val initialArr = listOf(9, 4, 7, 1, 0, 3)
        val steps = HeapBuildTrace.stepsForBuild(initialArr)
        val scenes = HeapSceneBuilder.buildScenes(steps)

        val expectedElementIds = (0 until initialArr.size).map { "elem:$it" }.toSet()

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "heap_tree" }
            val currentElementIds = treeSubScene.entities.map { it.elementId }.toSet()
            // Every scene contains the exact same set of elementIds
            assertEquals(expectedElementIds, currentElementIds)
        }
    }

    @Test
    fun testFixedStructuralSlotsDoNotMoveWithOccupants() {
        val initialArr = listOf(9, 4, 7, 1, 0, 3)
        val steps = HeapBuildTrace.stepsForBuild(initialArr)
        val scenes = HeapSceneBuilder.buildScenes(steps)

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "heap_tree" }
            val arraySubScene = scene.subScenes.first { it.subPaneId == "heap_array" }

            for (i in 0 until initialArr.size) {
                assertEquals("tree_slot:$i", treeSubScene.entities[i].slotKey)
                assertEquals("array_slot:$i", arraySubScene.entities[i].slotKey)
            }
        }
    }

    @Test
    fun testTwoSubScenesGeneratedSimultaneously() {
        val steps = HeapBuildTrace.stepsForBuild()
        val scenes = HeapSceneBuilder.buildScenes(steps)

        scenes.forEach { scene ->
            assertEquals(2, scene.subScenes.size)
            val subPaneIds = scene.subScenes.map { it.subPaneId }.toSet()
            assertEquals(setOf("heap_tree", "heap_array"), subPaneIds)
        }
    }

    @Test
    fun testCrossRepresentationConsistencyBetweenTreeAndArray() {
        val steps = HeapBuildTrace.stepsForBuild()
        val scenes = HeapSceneBuilder.buildScenes(steps)

        scenes.forEach { scene ->
            val treeSubScene = scene.subScenes.first { it.subPaneId == "heap_tree" }
            val arraySubScene = scene.subScenes.first { it.subPaneId == "heap_array" }

            for (i in 0 until steps.first().array.size) {
                val treeEntity = treeSubScene.entities.first { it.slotKey == "tree_slot:$i" }
                val arrayEntity = arraySubScene.entities.first { it.slotKey == "array_slot:$i" }

                // Tree node i and Array cell i MUST match elementId and dataValue
                assertEquals(treeEntity.elementId, arrayEntity.elementId)
                assertEquals(treeEntity.dataValue, arrayEntity.dataValue)
            }
        }
    }

    @Test
    fun testConsistencyWithTraceStepArray() {
        val steps = HeapBuildTrace.stepsForBuild()
        val scenes = HeapSceneBuilder.buildScenes(steps)

        steps.forEachIndexed { stepIdx, step ->
            val scene = scenes[stepIdx]
            val treeSubScene = scene.subScenes.first { it.subPaneId == "heap_tree" }

            step.array.forEachIndexed { slotIdx, expectedVal ->
                val entity = treeSubScene.entities.first { it.slotKey == "tree_slot:$slotIdx" }
                assertEquals(expectedVal.toString(), entity.dataValue)
            }
        }
    }

    @Test
    fun testTreeConnectionsUpdateVisualKeysPostSwap() {
        val initialArr = listOf(9, 4, 7, 1, 0, 3)
        val steps = HeapBuildTrace.stepsForBuild(initialArr)
        val scenes = HeapSceneBuilder.buildScenes(steps)

        // Find first SWAP_DOWN step
        val swapStepIdx = steps.indexOfFirst { it.event == BuildHeapEvent.SWAP_DOWN }
        assertTrue(swapStepIdx > 0, "Trace should contain a SWAP_DOWN step")

        val swapStep = steps[swapStepIdx]
        val (parentIdx, childIdx) = swapStep.swappedIndices!!

        // Get pre-swap scene (stepIdx - 1) and post-swap scene (stepIdx)
        val scenePreSwap = scenes[swapStepIdx - 1]
        val scenePostSwap = scenes[swapStepIdx]

        val treePre = scenePreSwap.subScenes.first { it.subPaneId == "heap_tree" }
        val treePost = scenePostSwap.subScenes.first { it.subPaneId == "heap_tree" }

        val targetConnKey = "branch:tree_slot:$parentIdx->tree_slot:$childIdx"

        val connPre = treePre.connections.first { it.connectionKey == targetConnKey }
        val connPost = treePost.connections.first { it.connectionKey == targetConnKey }

        // 1. ConnectionKey remains structurally identical across swap
        assertEquals(connPre.connectionKey, connPost.connectionKey)

        // 2. Pre-swap endpoints match occupants of slots before swap
        val preParentEntity = treePre.entities.first { it.slotKey == "tree_slot:$parentIdx" }
        val preChildEntity = treePre.entities.first { it.slotKey == "tree_slot:$childIdx" }

        assertEquals(preParentEntity.visualKey, connPre.fromVisualKey)
        assertEquals(preChildEntity.visualKey, connPre.toVisualKey)

        // 3. Post-swap endpoints match NEW occupants of those same structural slots
        val postParentEntity = treePost.entities.first { it.slotKey == "tree_slot:$parentIdx" }
        val postChildEntity = treePost.entities.first { it.slotKey == "tree_slot:$childIdx" }

        assertEquals(postParentEntity.visualKey, connPost.fromVisualKey)
        assertEquals(postChildEntity.visualKey, connPost.toVisualKey)

        // 4. Verify visual keys actually changed due to the swap (occupants swapped)
        assertNotEquals(connPre.fromVisualKey, connPost.fromVisualKey)
        assertNotEquals(connPre.toVisualKey, connPost.toVisualKey)

        // 5. Verify both connection endpoints exist in treeSubScene.entities
        val treePostVisualKeys = treePost.entities.map { it.visualKey }.toSet()
        assertTrue(treePostVisualKeys.contains(connPost.fromVisualKey))
        assertTrue(treePostVisualKeys.contains(connPost.toVisualKey))
    }
}
