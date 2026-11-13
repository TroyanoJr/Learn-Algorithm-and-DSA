package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkedListSceneLayoutTest {

    @Test
    fun testEmptyLinkedListLayout() {
        val layout = LinkedListSceneLayout.calculateLayout(
            subScene = StructureSubScene(
                subPaneId = "pane",
                type = StructureType.LINKED_LIST,
                entities = listOf(
                    VisualEntity("linked_list:head_ptr", "head_ptr", "HEAD", "slot:head"),
                    VisualEntity("linked_list:null_ptr", "null_ptr", "null", "slot:null")
                ),
                connections = listOf(
                    VisualConnection("link:head_ptr->null_ptr", "linked_list:head_ptr", "linked_list:null_ptr")
                )
            ),
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertTrue(layout.nodeBounds.containsKey("slot:head"))
        assertTrue(layout.nodeBounds.containsKey("slot:null"))
        assertEquals(1, layout.connectionLines.size)
        assertTrue(layout.requiredSceneWidth > 0f)
    }

    @Test
    fun testSingleNodeLinkedListLayout() {
        val scenes = LinkedListSceneBuilder.buildIntroductionScenes(listOf("42"))
        val layout = LinkedListSceneLayout.calculateLayout(
            subScene = scenes.first().subScenes.first(),
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertTrue(layout.nodeBounds.containsKey("slot:head"))
        assertTrue(layout.nodeBounds.containsKey("slot:0"))
        assertTrue(layout.nodeBounds.containsKey("slot:null"))
        assertTrue(layout.connectionLines.size >= 2)
        layout.nodeBounds.values.forEach { rect ->
            assertTrue(rect.left >= 0f)
            assertTrue(rect.top >= 0f)
        }
    }

    @Test
    fun testMultipleNodesAndDetachedNodesLayout() {
        val steps = LinkedListTrace.insertionSteps("HEAD")
        val scenes = LinkedListSceneBuilder.buildInsertionScenes(steps)
        val layout = LinkedListSceneLayout.calculateLayout(
            subScene = scenes.first().subScenes.first(),
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertTrue(layout.connectionLines.isNotEmpty())
        layout.nodeBounds.values.forEach { rect ->
            assertTrue(rect.left >= 0f)
            assertTrue(rect.top >= 0f)
        }
    }
}
