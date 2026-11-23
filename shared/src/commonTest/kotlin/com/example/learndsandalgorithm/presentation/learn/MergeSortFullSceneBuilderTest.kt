package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MergeSortFullSceneBuilderTest {

    @Test
    fun recursiveCallStatusUsesTheActiveNodeDepth() {
        val frames = build((16 downTo 0).toList())
        val calls = frames.filter { it.meta.traceStep.event == FullMergeEvent.CALL_ENTER }

        assertTrue(calls.isNotEmpty())
        calls.forEach { frame ->
            val step = frame.meta.traceStep
            val activeNode = step.nodes.single { it.nodeId == step.activeNodeId }
            assertEquals(
                "RECURSIVE CALL · DEPTH ${activeNode.callDepth}",
                frame.statusText
            )
            assertEquals(
                "RECURSIVE CALL · DEPTH ${activeNode.callDepth}",
                MergeSortFullSceneBuilder.statusText(step)
            )
        }
    }

    @Test
    fun nodeAndSlotKeysStayStableWhileNodesAppearProgressively() {
        val frames = build(listOf(8, 3, 5, 1, 2))
        val sourceIdentities = (0 until 5).map { "input:$it" }.toSet()
        frames.forEachIndexed { index, frame ->
            val tree = tree(frame)
            val working = working(frame)
            val buffer = buffer(frame)
            assertEquals(frame.meta.visibleNodeIds, tree.entities.map { it.elementId }.toSet())
            assertEquals(sourceIdentities, (working.entities.mapNotNull { it.occurrenceId } + buffer.entities.mapNotNull { it.occurrenceId }).toSet())
            assertEquals((0 until 5).map { "working_slot:$it" }.toSet(), working.entities.map { it.slotKey }.toSet())
            assertEquals((0 until 5).map { "buffer_slot:$it" }.toSet(), buffer.entities.map { it.elementId }.toSet())
            assertEquals((0 until 5).map { "buffer_slot:$it" }.toSet(), buffer.entities.map { it.slotKey }.toSet())
            assertEquals(index, frame.scene.stepIndex)
        }

        val divideIndex = frames.indexOfFirst { it.meta.traceStep.event == FullMergeEvent.DIVIDE }
        val divide = frames[divideIndex]
        val divideRoot = tree(divide).entities.single { it.elementId == divide.meta.traceStep.activeNodeId }
        assertNotNull(divideRoot)
        assertTrue("merge_node:0:2" !in divide.meta.visibleNodeIds)
        assertTrue("merge_node:2:5" !in divide.meta.visibleNodeIds)
        val childEntry = frames[divideIndex + 1]
        assertEquals(FullMergeEvent.CALL_ENTER, childEntry.meta.traceStep.event)
        assertTrue("merge_node:0:2" in childEntry.meta.visibleNodeIds)
    }

    @Test
    fun onlyRealVisibleParentChildEdgesAreBuiltAndActivePathIsHighlighted() {
        val frames = build(listOf(4, 1, 3, 2))
        frames.forEach { frame ->
            val tree = tree(frame)
            val visible = tree.entities.map { it.visualKey }.toSet()
            tree.connections.forEach { edge ->
                assertTrue(edge.fromVisualKey in visible)
                assertTrue(edge.toVisualKey in visible)
                val child = tree.entities.single { it.visualKey == edge.toVisualKey }
                val childNode = frame.meta.traceStep.nodes.single { it.nodeId == child.elementId }
                assertEquals(
                    MergeSortFullSceneBuilder.branchKey(childNode.parentNodeId!!, childNode.nodeId),
                    edge.connectionKey
                )
            }
            val activeNodes = tree.entities.filter { it.elementId in frame.meta.activePath }
            assertEquals(frame.meta.traceStep.callStack.toSet(), activeNodes.map { it.elementId }.toSet())
            if (frame.meta.traceStep.event == FullMergeEvent.DIVIDE && frame.meta.traceStep.activeNodeId != null) {
                assertEquals(EntityVisualState.ACTIVE, tree.entities.single { it.elementId == frame.meta.traceStep.activeNodeId }.visualState)
            }
            if (frame.meta.traceStep.event == FullMergeEvent.BASE_CASE) {
                assertEquals(EntityVisualState.TARGET, tree.entities.single { it.elementId == frame.meta.traceStep.activeNodeId }.visualState)
            }
        }
    }

    @Test
    fun workingElementsAndBufferSlotsMatchImmutableTraceProvenance() {
        val frames = build(listOf(3, 1, 3, 2, 1, 2))
        frames.forEach { frame ->
            val step = frame.meta.traceStep
            val working = working(frame).entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }
            val buffer = buffer(frame).entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }
            step.workingElements.forEachIndexed { index, element ->
                val entity = assertNotNull(working[index])
                assertEquals(element.identity, entity.elementId)
                assertEquals(element.value.toString(), entity.dataValue)
                assertEquals(element.identity, entity.occurrenceId)
            }
            step.auxiliaryBuffer.forEachIndexed { index, element ->
                val entity = assertNotNull(buffer[index])
                assertEquals(element?.value?.toString().orEmpty(), entity.dataValue)
                assertEquals(element?.identity, entity.occurrenceId)
                assertEquals(element?.let { "#${it.originalIndex}" }, entity.badgeLabel)
                val expectedState = when {
                    element == null -> EntityVisualState.DEFAULT
                    step.event == FullMergeEvent.COPY_BACK && step.copyBackIndex == index -> EntityVisualState.TARGET
                    step.newlyWrittenBufferSlot == index && step.event in setOf(FullMergeEvent.WRITE_BUFFER, FullMergeEvent.FLUSH_REMAINING) -> EntityVisualState.TARGET
                    else -> EntityVisualState.VISITED
                }
                assertEquals(expectedState, entity.visualState)
            }
        }
    }

    @Test
    fun comparisonsSelectionsAndCopyBackHighlightsUseTheirActualOccurrences() {
        val frames = build(listOf(2, 5, 2, 7) + listOf(1, 2, 2, 8))
        frames.forEach { frame ->
            val step = frame.meta.traceStep
            val working = working(frame).entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }
            if (step.event == FullMergeEvent.COMPARE) {
                val comparison = assertNotNull(step.currentComparison)
                assertEquals(EntityVisualState.ACTIVE, working[comparison.leftIndex]?.visualState)
                assertEquals(EntityVisualState.ACTIVE, working[comparison.rightIndex]?.visualState)
            }
            if (step.event == FullMergeEvent.WRITE_BUFFER || step.event == FullMergeEvent.FLUSH_REMAINING) {
                val selected = assertNotNull(step.selectedElement)
                assertTrue(working.values.any { it.elementId == selected.identity && it.visualState == EntityVisualState.TARGET })
                val output = buffer(frame).entities.single { it.slotKey == "buffer_slot:${step.newlyWrittenBufferSlot}" }
                assertEquals(selected.identity, output.occurrenceId)
                assertEquals(EntityVisualState.TARGET, output.visualState)
            }
            if (step.event == FullMergeEvent.COPY_BACK) {
                val selected = assertNotNull(step.selectedElement)
                assertEquals(selected.identity, working[step.copyBackIndex]?.elementId)
                assertEquals(EntityVisualState.TARGET, working[step.copyBackIndex]?.visualState)
                val source = buffer(frame).entities.single { it.slotKey == "buffer_slot:${step.copyBackIndex}" }
                assertEquals(selected.identity, source.occurrenceId)
                assertEquals(EntityVisualState.TARGET, source.visualState)
            }
        }
    }

    @Test
    fun emptyAndSingletonScenesHaveNoPhantomCellsOrEdges() {
        val empty = build(emptyList())
        empty.forEach { frame ->
            assertTrue(tree(frame).entities.isNotEmpty())
            assertTrue(tree(frame).connections.isEmpty())
            assertTrue(working(frame).entities.isEmpty())
            assertTrue(buffer(frame).entities.isEmpty())
        }

        val singleton = build(listOf(Int.MIN_VALUE))
        singleton.forEach { frame ->
            assertEquals(1, working(frame).entities.size)
            assertEquals(1, buffer(frame).entities.size)
            assertTrue(tree(frame).connections.isEmpty())
            assertEquals(1, tree(frame).entities.size)
        }
        assertEquals(listOf(FullMergeEvent.START, FullMergeEvent.CALL_ENTER, FullMergeEvent.BASE_CASE, FullMergeEvent.RETURN, FullMergeEvent.COMPLETE), singleton.map { it.meta.traceStep.event })
    }

    private fun build(input: List<Int>) = MergeSortFullSceneBuilder.build(MergeSortFullTrace.stepsForFullSort(input))

    private fun tree(frame: MergeSortFullSceneFrame) = frame.scene.subScenes.single { it.subPaneId == MergeSortFullSceneBuilder.TREE_SCENE_ID }
    private fun working(frame: MergeSortFullSceneFrame) = frame.scene.subScenes.single { it.subPaneId == MergeSortFullSceneBuilder.WORKING_SCENE_ID }
    private fun buffer(frame: MergeSortFullSceneFrame) = frame.scene.subScenes.single { it.subPaneId == MergeSortFullSceneBuilder.BUFFER_SCENE_ID }
}
