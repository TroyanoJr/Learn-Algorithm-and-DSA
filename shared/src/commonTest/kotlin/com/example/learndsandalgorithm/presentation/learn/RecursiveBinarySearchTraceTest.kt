package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecursiveBinarySearchTraceTest {

    @Test
    fun defaultTraceUsesRecursiveCallsAndReturnsAfterTheMatch() {
        val steps = RecursiveBinarySearchTrace.stepsForSearch()

        assertEquals(
            listOf(
                BinarySearchEvent.START,
                BinarySearchEvent.CALL_ENTER,
                BinarySearchEvent.CALCULATE_MID,
                BinarySearchEvent.COMPARE_TARGET,
                BinarySearchEvent.RECURSE_RIGHT,
                BinarySearchEvent.CALL_ENTER,
                BinarySearchEvent.CALCULATE_MID,
                BinarySearchEvent.COMPARE_TARGET,
                BinarySearchEvent.TARGET_FOUND,
                BinarySearchEvent.RETURN,
                BinarySearchEvent.RETURN
            ),
            steps.map { it.event }
        )
        assertEquals(
            "Recursive Binary Search: Searching for target 12 in sorted array [2, 5, 8, 12, 16]. Search range [0..4].",
            steps.first().explanation
        )
        assertEquals("Range [0..4]: Calculate mid = 0 + (4 - 0) / 2 = 2. Element arr[2] = 8.", steps[2].explanation)
        assertEquals("MATCH FOUND! arr[3] == 12. Target 12 found at index 3 in O(log N) time.", steps[8].explanation)

        val found = steps.single { it.event == BinarySearchEvent.TARGET_FOUND }
        assertEquals(3, found.resultIndex)
        assertEquals(3, found.foundIndex)
        assertNull(found.returnValue)
        assertTrue(!found.isTerminal)
        assertEquals(3, steps.last().resultIndex)
        assertEquals(3, steps.last().returnValue)
        assertTrue(steps.last().isTerminal)
        assertEquals(BinarySearchEvent.RETURN, steps.last().event)
        assertEquals(emptyList(), steps.last().callStack)
        assertEquals(0, steps.last().stackDepth)
        assertEquals(1, steps.count { it.isTerminal })
    }

    @Test
    fun callDepthAndReturnPropagationTrackTheRealRecursionStack() {
        val steps = RecursiveBinarySearchTrace.stepsForSearch()
        val enters = steps.filter { it.event == BinarySearchEvent.CALL_ENTER }
        assertEquals(listOf(0, 1), enters.map { it.callDepth })
        enters.forEach { step ->
            assertEquals(step.callDepth + 1, step.stackDepth)
            assertEquals(step.stackDepth, step.callStack.size)
            assertEquals(step.callStack.last().callId, step.activeCallId)
            assertEquals(step.left to step.right, step.callStack.last().left to step.callStack.last().right)
        }

        val recurse = steps.single { it.event == BinarySearchEvent.RECURSE_RIGHT }
        assertEquals(0, recurse.activeCallId)
        assertEquals(1, recurse.calleeCall?.callId)
        assertEquals(0, recurse.calleeCall?.callerCallId)
        assertEquals(3 to 4, recurse.calleeCall?.left to recurse.calleeCall?.right)
        assertEquals(1, recurse.calleeCall?.depth)

        val returns = steps.filter { it.event == BinarySearchEvent.RETURN }
        assertEquals(listOf(1, 0), returns.map { it.callDepth })
        assertEquals(listOf(1, 0), returns.map { it.stackDepth })
        assertEquals(listOf(1, 0), returns.map { it.returnedCall?.callId })
        assertEquals(0, returns.first().activeCallId)
        assertEquals(0, returns.first().callerCallId)
        assertNull(returns.last().activeCallId)
        assertNull(returns.last().callerCallId)
        assertTrue(returns.all { it.returnValue == 3 })
    }

    @Test
    fun absentTargetRecursesBothDirectionsAndTerminatesAfterEmptyRangeUnwinds() {
        val steps = RecursiveBinarySearchTrace.stepsForSearch(listOf(2, 5, 8, 12, 16), target = 10)

        assertTrue(steps.any { it.event == BinarySearchEvent.RECURSE_RIGHT })
        assertTrue(steps.any { it.event == BinarySearchEvent.RECURSE_LEFT })
        val emptyCall = steps.single { it.event == BinarySearchEvent.TARGET_NOT_FOUND }
        assertEquals(3 to 2, emptyCall.left to emptyCall.right)
        assertTrue(emptyCall.isEmptyRangeBaseCase)
        assertNull(emptyCall.returnValue)
        assertEquals(-1, emptyCall.resultIndex)
        assertEquals(2, emptyCall.callDepth)

        val returns = steps.filter { it.event == BinarySearchEvent.RETURN }
        assertEquals(listOf(2, 1, 0), returns.map { it.callDepth })
        assertEquals(listOf(2, 1, 0), returns.map { it.stackDepth })
        assertEquals(listOf(-1, -1, -1), returns.map { it.returnValue })
        assertEquals(-1, steps.last().resultIndex)
        assertTrue(steps.last().isTerminal)
        assertEquals(BinarySearchEvent.RETURN, steps.last().event)
    }

    @Test
    fun emptyArrayUsesEmptyRangeBaseCaseWithoutIndexing() {
        val steps = RecursiveBinarySearchTrace.stepsForSearch(emptyList(), target = 5)
        assertEquals(
            listOf(BinarySearchEvent.START, BinarySearchEvent.CALL_ENTER, BinarySearchEvent.TARGET_NOT_FOUND, BinarySearchEvent.RETURN),
            steps.map { it.event }
        )
        assertEquals(0 to -1, steps[1].left to steps[1].right)
        assertTrue(steps[2].isEmptyRangeBaseCase)
        assertTrue(steps.all { it.mid == null && it.midValue == null })
        assertEquals(-1, steps.last().returnValue)
        assertTrue(steps.last().isTerminal)
    }

    @Test
    fun singletonDuplicatesNegativeAndExtremeValuesReturnAnActualMatchingIndex() {
        val cases = listOf(
            listOf(7) to 7,
            listOf(7) to 6,
            listOf(-9, -3, -3, 4) to -3,
            listOf(-9, -3, -3, 4) to -20,
            listOf(-9, -3, -3, 4) to 20,
            listOf(Int.MIN_VALUE, -1, Int.MAX_VALUE) to Int.MIN_VALUE,
            listOf(Int.MIN_VALUE, -1, Int.MAX_VALUE) to Int.MAX_VALUE
        )
        cases.forEach { (array, target) ->
            val steps = RecursiveBinarySearchTrace.stepsForSearch(array, target)
            val terminal = steps.last()
            assertTrue(terminal.isTerminal)
            assertEquals(BinarySearchEvent.RETURN, terminal.event)
            if (terminal.returnValue == -1) {
                assertTrue(target !in array)
            } else {
                val index = assertNotNull(terminal.returnValue)
                assertTrue(index in array.indices)
                assertEquals(target, array[index])
            }
        }
    }

    @Test
    fun sortedNondecreasingInputIsRequiredAndNoSnapshotIndexesOutsideTheArray() {
        assertFailsWith<IllegalArgumentException> {
            RecursiveBinarySearchTrace.stepsForSearch(listOf(1, 4, 3, 8), target = 3)
        }

        val arrays = listOf(
            emptyList(),
            listOf(4),
            listOf(-4, -1, 0, 5, 9),
            listOf(Int.MIN_VALUE, -1, Int.MAX_VALUE)
        )
        arrays.forEach { array ->
            RecursiveBinarySearchTrace.stepsForSearch(array, target = 0).forEach { step ->
                if (step.mid != null) assertTrue(step.mid in array.indices)
                if (step.midValue != null) assertEquals(array[step.mid!!], step.midValue)
                step.callStack.forEach { frame ->
                    if (frame.mid != null) assertTrue(frame.mid in array.indices)
                }
            }
        }
    }

    @Test
    fun resultAndReturnValuesDoNotAppearBeforeTheirTraceEvent() {
        val cases = listOf(
            RecursiveBinarySearchTrace.stepsForSearch(target = 12),
            RecursiveBinarySearchTrace.stepsForSearch(target = 10)
        )
        cases.forEach { steps ->
            steps.filter { it.event != BinarySearchEvent.RETURN }.forEach { step ->
                assertNull(step.returnValue)
            }
            val found = steps.indexOfFirst { it.event == BinarySearchEvent.TARGET_FOUND }
            if (found >= 0) {
                assertTrue(steps.take(found).all { it.foundIndex == null })
                assertEquals(steps[found].resultIndex, steps[found].foundIndex)
            }
            assertTrue(steps.last().isTerminal)
        }
    }
}
