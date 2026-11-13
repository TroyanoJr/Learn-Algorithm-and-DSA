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
            swappedIndices = Pair(0, 1),
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

        val swapStepIdx = steps.indexOfFirst { it.event == BuildHeapEvent.SWAP_DOWN }
        assertTrue(swapStepIdx >= 0)

        val swapStep = steps[swapStepIdx]
        val (i, j) = swapStep.swappedIndices!!

        val scenePostSwap = scenes[swapStepIdx]
        val treeSubScene = scenePostSwap.subScenes.first { it.subPaneId == "heap_tree" }

        val entityAtI = treeSubScene.entities.first { it.slotKey == "tree_slot:$i" }
        val entityAtJ = treeSubScene.entities.first { it.slotKey == "tree_slot:$j" }

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

        val swapStepIdx = steps.indexOfFirst { it.event == BuildHeapEvent.SWAP_DOWN }
        assertTrue(swapStepIdx > 0, "Trace should contain a SWAP_DOWN step")

        val swapStep = steps[swapStepIdx]
        val (parentIdx, childIdx) = swapStep.swappedIndices!!

        val scenePreSwap = scenes[swapStepIdx - 1]
        val scenePostSwap = scenes[swapStepIdx]

        val treePre = scenePreSwap.subScenes.first { it.subPaneId == "heap_tree" }
        val treePost = scenePostSwap.subScenes.first { it.subPaneId == "heap_tree" }

        val targetConnKey = "branch:tree_slot:$parentIdx->tree_slot:$childIdx"

        val connPre = treePre.connections.first { it.connectionKey == targetConnKey }
        val connPost = treePost.connections.first { it.connectionKey == targetConnKey }

        assertEquals(connPre.connectionKey, connPost.connectionKey)

        val preParentEntity = treePre.entities.first { it.slotKey == "tree_slot:$parentIdx" }
        val preChildEntity = treePre.entities.first { it.slotKey == "tree_slot:$childIdx" }

        assertEquals(preParentEntity.visualKey, connPre.fromVisualKey)
        assertEquals(preChildEntity.visualKey, connPre.toVisualKey)

        val postParentEntity = treePost.entities.first { it.slotKey == "tree_slot:$parentIdx" }
        val postChildEntity = treePost.entities.first { it.slotKey == "tree_slot:$childIdx" }

        assertEquals(postParentEntity.visualKey, connPost.fromVisualKey)
        assertEquals(postChildEntity.visualKey, connPost.toVisualKey)

        assertNotEquals(connPre.fromVisualKey, connPost.fromVisualKey)
        assertNotEquals(connPre.toVisualKey, connPost.toVisualKey)

        val treePostVisualKeys = treePost.entities.map { it.visualKey }.toSet()
        assertTrue(treePostVisualKeys.contains(connPost.fromVisualKey))
        assertTrue(treePostVisualKeys.contains(connPost.toVisualKey))
    }

    // 1. Heap Sort specific rigorous tests
    @Test
    fun testHeapSortIdentitiesAndSuffixIsolation() {
        val initialArr = listOf(4, 1, 3, 2)
        val steps = HeapSortTrace.stepsForHeapSort(initialArr)
        val scenes = HeapSceneBuilder.buildSortScenes(steps)

        val buildMaxSceneIdx = steps.indexOfFirst { it.event == HeapSortEvent.BUILD_MAX_HEAP }
        assertTrue(buildMaxSceneIdx >= 0)
        val buildMaxScene = scenes[buildMaxSceneIdx]
        val treeSub = buildMaxScene.subScenes.first { it.subPaneId == "heap_tree" }
        val arraySub = buildMaxScene.subScenes.first { it.subPaneId == "heap_array" }

        val allIds = (treeSub.entities.map { it.elementId } + arraySub.entities.map { it.elementId }).toSet()
        assertEquals(initialArr.size, allIds.size)

        treeSub.entities.forEach { treeEntity ->
            val arrayEntity = arraySub.entities.first { it.slotKey.substringAfter("array_slot:") == treeEntity.slotKey.substringAfter("tree_slot:") }
            assertEquals(treeEntity.elementId, arrayEntity.elementId)
            assertEquals(treeEntity.dataValue, arrayEntity.dataValue)
        }

        scenes.forEach { scene ->
            val t = scene.subScenes.first { it.subPaneId == "heap_tree" }
            val a = scene.subScenes.first { it.subPaneId == "heap_array" }
            assertTrue(t.entities.size <= a.entities.size)
            val sceneIds = (t.entities.map { it.elementId } + a.entities.map { it.elementId }).toSet()
            assertEquals(initialArr.size, sceneIds.size)
        }
    }

    @Test
    fun testHeapSortDuplicateValuesDeterminism() {
        val steps = HeapSortTrace.stepsForHeapSort(listOf(5, 3, 5, 1))
        val scenes = HeapSceneBuilder.buildSortScenes(steps)
        scenes.forEach { scene ->
            val ids = scene.subScenes.flatMap { it.entities }.map { it.elementId }.toSet()
            assertEquals(4, ids.size)
        }
    }

    // 2. Insert specific tests
    @Test
    fun testHeapInsertIdentitiesAndConsistency() {
        val steps = HeapInsertSiftUpTrace.stepsForInsert(listOf(2, 5, 3), 1)
        val scenes = HeapSceneBuilder.buildInsertScenes(steps)

        scenes.forEach { scene ->
            val tree = scene.subScenes.first { it.subPaneId == "heap_tree" }
            val array = scene.subScenes.first { it.subPaneId == "heap_array" }
            assertEquals(tree.entities.size, array.entities.size)
            tree.entities.forEachIndexed { idx, treeEnt ->
                val arrEnt = array.entities[idx]
                assertEquals(treeEnt.elementId, arrEnt.elementId)
                assertEquals(treeEnt.dataValue, arrEnt.dataValue)
            }
        }
    }

    // 3. Extract specific tests
    @Test
    fun testHeapExtractIdentitiesAndReduction() {
        val steps = HeapExtractSiftDownTrace.stepsForExtract(listOf(1, 3, 2))
        val scenes = HeapSceneBuilder.buildExtractScenes(steps)

        scenes.forEach { scene ->
            val tree = scene.subScenes.first { it.subPaneId == "heap_tree" }
            val array = scene.subScenes.first { it.subPaneId == "heap_array" }
            assertEquals(tree.entities.size, array.entities.size)
            tree.entities.forEachIndexed { idx, treeEnt ->
                val arrEnt = array.entities[idx]
                assertEquals(treeEnt.elementId, arrEnt.elementId)
                assertEquals(treeEnt.dataValue, arrEnt.dataValue)
            }
        }
    }

    // 4. Properties specific tests
    @Test
    fun testHeapPropertiesStatesAndConnections() {
        val steps = HeapPropertiesTrace.stepsForTopic(HeapPropertyTopic.INDEX_MAPPING)
        val scenes = HeapSceneBuilder.buildPropertiesScenes(steps)

        scenes.forEach { scene ->
            val tree = scene.subScenes.first { it.subPaneId == "heap_tree" }
            val array = scene.subScenes.first { it.subPaneId == "heap_array" }
            assertTrue(tree.entities.any { it.visualState == EntityVisualState.ACTIVE })
            assertTrue(tree.entities.any { it.visualState == EntityVisualState.TARGET })
            tree.entities.forEachIndexed { idx, treeEnt ->
                val arrEnt = array.entities[idx]
                assertEquals(treeEnt.elementId, arrEnt.elementId)
                assertEquals(treeEnt.dataValue, arrEnt.dataValue)
            }
        }
    }
}
