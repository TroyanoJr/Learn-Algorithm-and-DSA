package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArrayTraceTest {

    @Test
    fun resizeStepsGeneratesCorrectCapacityTransitionsAndPreservedSlots() {
        val steps = ArrayTrace.resizeSteps()

        assertEquals(5, steps.size)

        // Step 0: CURRENT STORAGE
        assertEquals("CURRENT STORAGE", steps[0].stageLabel)
        assertEquals(11, steps[0].storageSlots.size)
        assertNull(steps[0].preservedStorageSlots)
        assertEquals(setOf(10), steps[0].highlightedIndices)

        // Step 1: CAPACITY FULL
        assertEquals("CAPACITY FULL", steps[1].stageLabel)
        assertEquals(11, steps[1].storageSlots.size)
        assertNull(steps[1].preservedStorageSlots)

        // Step 2: NEW STORAGE (22 slots)
        assertEquals("NEW STORAGE", steps[2].stageLabel)
        assertEquals(22, steps[2].storageSlots.size)
        assertTrue(steps[2].storageSlots.all { it == null })
        assertNotNull(steps[2].preservedStorageSlots)
        assertEquals(11, steps[2].preservedStorageSlots?.size)

        // Step 3: 11 VALUES COPIED
        assertEquals("11 VALUES COPIED", steps[3].stageLabel)
        assertEquals(22, steps[3].storageSlots.size)
        assertEquals((0..10).toSet(), steps[3].highlightedIndices)

        // Step 4: 120 APPENDED
        assertEquals("120 APPENDED", steps[4].stageLabel)
        assertEquals(22, steps[4].storageSlots.size)
        assertEquals("120", steps[4].storageSlots[11])
        assertEquals(setOf(11), steps[4].highlightedIndices)
    }

    @Test
    fun traversalStepsFollowsIndexProgressionFromUnstartedToTerminal() {
        val steps = ArrayTrace.traversalSteps()

        assertEquals(12, steps.size)

        // Unstarted step (-1)
        val unstarted = steps.first()
        assertEquals(-1, unstarted.currentIndex)
        assertEquals(0, unstarted.visitedCount)
        assertEquals("READY", unstarted.badgeText)
        assertFalse(unstarted.isCompleted)

        // Intermediate steps (0..9)
        for (i in 0 until 10) {
            val step = steps[i + 1]
            assertEquals(i, step.currentIndex)
            assertEquals(i + 1, step.visitedCount)
            assertEquals("STEP ${i + 1} OF 10", step.badgeText)
            assertFalse(step.isCompleted)
        }

        // Terminal step (10)
        val terminal = steps.last()
        assertEquals(10, terminal.currentIndex)
        assertEquals(10, terminal.visitedCount)
        assertEquals("COMPLETED", terminal.badgeText)
        assertTrue(terminal.isCompleted)
    }

    @Test
    fun insertionStepsShiftValuesRightAndPlaceNewValueAtTargetIndex() {
        val steps = ArrayTrace.insertDeleteSteps("INSERT")

        assertEquals(11, steps.size)

        // Step 0: Initial
        assertEquals(10, steps[0].displayItems.size)
        assertEquals("READY", steps[0].badgeText)
        assertFalse(steps[0].isCompleted)

        // Step 1: Gap created at index 10
        assertEquals(11, steps[1].displayItems.size)
        assertEquals("  ", steps[1].displayItems[10])

        // Step 2..9: Shifting right
        for (stepIdx in 2..9) {
            val step = steps[stepIdx]
            val expectedGapIndex = 11 - stepIdx
            assertEquals("  ", step.displayItems[expectedGapIndex])
        }

        // Step 10: Insertion complete (25 at index 2)
        val finalStep = steps.last()
        assertEquals(11, finalStep.displayItems.size)
        assertEquals("25", finalStep.displayItems[2])
        assertEquals("COMPLETED", finalStep.badgeText)
        assertTrue(finalStep.isCompleted)
    }

    @Test
    fun deletionStepsShiftValuesLeftAndRemoveTrailingSlot() {
        val steps = ArrayTrace.insertDeleteSteps("DELETE")

        assertEquals(11, steps.size)

        // Step 0: Initial
        assertEquals(10, steps[0].displayItems.size)
        assertEquals("READY", steps[0].badgeText)

        // Step 1: 20 removed at index 1
        assertEquals("  ", steps[1].displayItems[1])

        // Step 2..9: Shifting left
        for (stepIdx in 2..9) {
            val step = steps[stepIdx]
            val expectedGapIndex = stepIdx
            assertEquals("  ", step.displayItems[expectedGapIndex])
        }

        // Step 10: Deletion complete (trailing gap dropped, 9 items remaining)
        val finalStep = steps.last()
        assertEquals(9, finalStep.displayItems.size)
        assertFalse(finalStep.displayItems.contains("20"))
        assertEquals("COMPLETED", finalStep.badgeText)
        assertTrue(finalStep.isCompleted)
    }
}
