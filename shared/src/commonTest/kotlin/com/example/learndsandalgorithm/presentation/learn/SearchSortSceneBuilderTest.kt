package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchSortSceneBuilderTest {
    @Test
    fun searchStepMappingKeepsInitialIntermediateAndTerminalIndicesNonnegative() {
        assertEquals(0, SearchSortSceneBuilder.teachingStepIndex(0, 11))
        assertEquals(5, SearchSortSceneBuilder.teachingStepIndex(5, 11))
        assertEquals(10, SearchSortSceneBuilder.teachingStepIndex(99, 11))
        assertEquals(0, SearchSortSceneBuilder.teachingStepIndex(-1, 0))
    }

    @Test
    fun comparisonCardModelsPreserveEveryOriginalLabel() {
        val searches = listOf(
            ComparisonCanvasRow("Linear Search", "Checks 4 → 12 → 19 → ... → 72", "9 Comparisons  O(n)"),
            ComparisonCanvasRow("Binary Search", "Checks midpoint 44 → midpoint 72", "2 Comparisons  O(log n)", positive = true)
        )
        val sorting = listOf(
            ComparisonCanvasRow("Bubble Sort", "Compare & swap adjacent items", "O(n) Best / O(n²) Worst"),
            ComparisonCanvasRow("Selection Sort", "Find minimum and swap once per pass", "O(n²) Always"),
            ComparisonCanvasRow("Insertion Sort", "Shift larger items and insert key", "O(n) Best / O(n²) Worst")
        )
        assertEquals(listOf("Checks 4 → 12 → 19 → ... → 72", "Checks midpoint 44 → midpoint 72"), searches.map { it.strategy })
        assertEquals(listOf("9 Comparisons  O(n)", "2 Comparisons  O(log n)"), searches.map { it.complexity })
        assertEquals(listOf("Compare & swap adjacent items", "Find minimum and swap once per pass", "Shift larger items and insert key"), sorting.map { it.strategy })
        assertEquals(listOf("O(n) Best / O(n²) Worst", "O(n²) Always", "O(n) Best / O(n²) Worst"), sorting.map { it.complexity })
    }
    @Test
    fun linearSearchIsDeterministicAndPreservesVisitedFoundAndTerminalMetadata() {
        val frames = SearchSortSceneBuilder.buildLinearSearchFrames(
            LinearSearchTrace.steps,
            LinearSearchTrace.values,
            LinearSearchTrace.target
        )
        assertEquals(frames, SearchSortSceneBuilder.buildLinearSearchFrames(LinearSearchTrace.steps, LinearSearchTrace.values, LinearSearchTrace.target))
        assertEquals(LinearSearchTrace.steps.size + 1, frames.size)
        assertEquals(emptySet(), frames.first().meta.inspectedIndices)

        val firstComparison = frames[1]
        assertEquals(0, firstComparison.meta.currentIndex)
        assertEquals(setOf(0), firstComparison.meta.inspectedIndices)
        assertEquals(EntityVisualState.ACTIVE, entityAt(firstComparison, 0).visualState)

        val found = frames.last()
        assertEquals(LinearSearchTrace.values.lastIndex, found.meta.foundIndex)
        assertEquals(LinearSearchTrace.values.indices.toSet(), found.meta.inspectedIndices)
        assertEquals(LinearSearchTrace.values.size, found.meta.checkedCount)
        assertTrue(found.meta.isTerminal)
        assertEquals(EntityVisualState.TARGET, entityAt(found, found.meta.foundIndex!!).visualState)
        assertEquals(LinearSearchTrace.steps.last().explanation, found.meta.explanation)
        assertStableSlotIdentities(frames)
    }

    @Test
    fun linearSearchEmptyAndSingleElementTerminalStatesAreSafe() {
        val emptyTrace = LinearSearchTrace.generate(emptyList(), 9)
        val emptyFrames = SearchSortSceneBuilder.buildLinearSearchFrames(emptyTrace, emptyList(), 9)
        assertEquals(2, emptyFrames.size)
        assertTrue(emptyFrames.last().meta.notFound)
        assertEquals(0, emptyFrames.last().meta.checkedCount)
        assertTrue(emptyFrames.last().scene.subScenes.single().entities.isEmpty())

        val singleTrace = LinearSearchTrace.generate(listOf(9), 9)
        val singleFrames = SearchSortSceneBuilder.buildLinearSearchFrames(singleTrace, listOf(9), 9)
        assertEquals(0, singleFrames.last().meta.foundIndex)
        assertEquals(EntityVisualState.TARGET, entityAt(singleFrames.last(), 0).visualState)
    }

    @Test
    fun binarySearchPreservesPointersRangeEliminationAndFoundState() {
        val frames = SearchSortSceneBuilder.buildBinarySearchFrames(BinarySearchTrace.steps, BinarySearchTrace.values, BinarySearchTrace.target)
        assertEquals(BinarySearchTrace.steps.size + 1, frames.size)
        val first = frames[1]
        assertEquals(0, first.meta.leftIndex)
        assertEquals(4, first.meta.midIndex)
        assertEquals(9, first.meta.rightIndex)
        assertEquals(0..9, first.meta.activeRange)
        assertEquals(emptySet(), first.meta.eliminatedIndices)

        val second = frames[2]
        assertEquals((0..4).toSet(), second.meta.eliminatedIndices)
        assertTrue((0..4).all { entityAt(second, it).visualState == EntityVisualState.VIOLATION })

        val found = frames.last()
        assertEquals(5, found.meta.foundIndex)
        assertTrue(found.meta.isTerminal)
        assertEquals(BinarySearchTrace.steps.last().explanation, found.meta.explanation)
        assertEquals(EntityVisualState.TARGET, entityAt(found, 5).visualState)
        assertStableSlotIdentities(frames)
    }

    @Test
    fun sortingFramesRetainEveryAlgorithmSpecificTraceField() {
        val bubble = SearchSortSceneBuilder.buildBubbleSortFrames(BubbleSortTrace.steps)
        BubbleSortTrace.steps.forEachIndexed { index, trace ->
            val meta = bubble[index].meta
            assertEquals(trace.highlightedIndices.toSet(), meta.highlightedIndices)
            assertEquals(trace.sortedIndices.toSet(), meta.sortedIndices)
            assertEquals(trace.message, meta.explanation)
            assertEquals(trace.isCompletion, meta.bubbleCompletion)
        }
        assertTrue(bubble.last().meta.isTerminal)

        val selection = SearchSortSceneBuilder.buildSelectionSortFrames(SelectionSortTrace.steps)
        SelectionSortTrace.steps.forEachIndexed { index, trace ->
            val meta = selection[index].meta
            assertEquals(trace.passIndex, meta.passIndex)
            assertEquals(trace.comparisonTargetIndex, meta.candidateIndex)
            assertEquals(trace.minimumIndex, meta.minimumIndex)
            assertEquals(trace.event, meta.selectionEvent)
            assertEquals(trace.message, meta.explanation)
        }
        assertEquals(SelectionSortEvent.COMPLETE, selection.last().meta.selectionEvent)

        val insertion = SearchSortSceneBuilder.buildInsertionSortFrames(InsertionSortTrace.steps)
        InsertionSortTrace.steps.forEachIndexed { index, trace ->
            val meta = insertion[index].meta
            assertEquals(trace.insertionIndex, meta.passIndex)
            assertEquals(trace.key, meta.key)
            assertEquals(trace.comparisonTargetIndex, meta.comparisonTargetIndex)
            assertEquals(trace.holeIndex, meta.holeIndex)
            assertEquals(trace.event, meta.insertionEvent)
            assertEquals(trace.message, meta.explanation)
        }
        assertEquals(InsertionSortEvent.COMPLETE, insertion.last().meta.insertionEvent)
        assertUniqueIdentitiesPerFrame(bubble)
        assertUniqueIdentitiesPerFrame(selection)
        assertUniqueIdentitiesPerFrame(insertion)


        val duplicated = SearchSortSceneBuilder.buildBubbleSortFrames(
            BubbleSortTrace.generate(listOf(2, 1, 2))
        )

        duplicated.forEach { frame ->
            val expectedOccurrences = mutableMapOf<Int, Int>()

            frame.scene.subScenes.single().entities.forEachIndexed { index, entity ->
                val value = frame.values[index]
                val occurrence = expectedOccurrences[value] ?: 0

                assertEquals(
                    "bubble:value:$value:occurrence:$occurrence",
                    entity.elementId
                )

                expectedOccurrences[value] = occurrence + 1
            }
        }

        assertUniqueIdentitiesPerFrame(duplicated)

        val swap = SearchSortSceneBuilder.buildBubbleSortFrames(BubbleSortTrace.generate(listOf(2, 1)))
        assertEquals(listOf(1, 2), swap[0].values.take(2).sorted())
        assertEquals("bubble:value:1:occurrence:0", swap[1].scene.subScenes.single().entities[0].elementId)
    }

    @Test
    fun emptyAndSingleElementSortScenesRemainCompleteAndMetadataSafe() {
        listOf(emptyList(), listOf(7)).forEach { input ->
            val selection = SearchSortSceneBuilder.buildSelectionSortFrames(SelectionSortTrace.generate(input)).single()
            val insertion = SearchSortSceneBuilder.buildInsertionSortFrames(InsertionSortTrace.generate(input)).single()
            assertTrue(selection.meta.isTerminal)
            assertTrue(insertion.meta.isTerminal)
            assertEquals(input, selection.values)
            assertEquals(input, insertion.values)
            assertNull(selection.meta.minimumIndex)
            assertNull(insertion.meta.holeIndex)
        }
    }

    private fun entityAt(frame: SearchSortSceneFrame, index: Int): VisualEntity =
        frame.scene.subScenes.single().entities.first { it.slotKey == "array_slot:$index" }

    private fun assertStableSlotIdentities(frames: List<SearchSortSceneFrame>) {
        val maximum = frames.maxOfOrNull { it.values.size } ?: 0
        repeat(maximum) { index ->
            val ids = frames.mapNotNull { frame -> frame.scene.subScenes.single().entities.getOrNull(index)?.elementId }.distinct()
            assertNotNull(ids.singleOrNull(), "slot $index must keep one stable identity")
        }
    }

    private fun assertUniqueIdentitiesPerFrame(frames: List<SearchSortSceneFrame>) {
        frames.forEach { frame ->
            val ids = frame.scene.subScenes.single().entities.map { it.elementId }
            assertEquals(ids.size, ids.distinct().size)
        }
    }

}
