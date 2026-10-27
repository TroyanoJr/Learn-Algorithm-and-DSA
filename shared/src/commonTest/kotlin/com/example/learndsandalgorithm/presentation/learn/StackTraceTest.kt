package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StackTraceTest {
    @Test
    fun noStepExceedsSixDataElementsInAnyStackDemonstration() {
        StackDemonstration.entries.forEach { demonstration ->
            val steps = StackTrace.stepsFor(demonstration)
            steps.forEach { step ->
                assertTrue(
                    step.values.size <= 6,
                    "Demonstration $demonstration step has > 6 elements: ${step.values.size}"
                )
            }
        }
    }

    @Test
    fun fundamentalsTraceFollowsLessonLifoExampleAndSafeEmptyAccess() {
        val steps = StackTrace.stepsFor(StackDemonstration.FUNDAMENTALS)
        val pushes = steps.filter { it.event == StackTraceEvent.PUSH }
        val pops = steps.filter { it.event == StackTraceEvent.POP }

        assertEquals(listOf("A", "B", "C", "D", "E"), pushes[4].values)
        assertEquals(listOf("A", "B", "C", "D"), pops.first().values)
        assertEquals("E", pops.first().returnedValue)
        assertEquals("F", pops[1].returnedValue)
        assertEquals(listOf("E", "F", "D", "C", "B", "A"), pops.map { it.returnedValue })
        assertTrue(steps.last().isTerminal)
        assertTrue(steps.last().values.isEmpty())
    }

    @Test
    fun operationsLessonShowsPeekWithoutMutationAndPopFour() {
        val steps = StackTrace.stepsFor(StackDemonstration.OPERATIONS)
        val pushSeven = steps.first { it.operationValue == "7" && it.event == StackTraceEvent.PUSH }
        val pushFour = steps.first { it.operationValue == "4" && it.event == StackTraceEvent.PUSH }
        val peek = steps.first { it.event == StackTraceEvent.PEEK }
        val popFour = steps.first { it.event == StackTraceEvent.POP }

        assertEquals(listOf("7"), pushSeven.values)
        assertEquals(listOf("7", "4"), pushFour.values)
        assertEquals(pushFour.values, peek.values)
        assertEquals("4", peek.returnedValue)
        assertEquals(listOf("7"), popFour.values)
        assertEquals("4", popFour.returnedValue)
        assertTrue(peek.explanation.contains("without removing"))
    }

    @Test
    fun implementationLessonMarksArrayAndLinkedRepresentationsOnEveryStep() {
        val steps = StackTrace.stepsFor(StackDemonstration.IMPLEMENTATIONS)
        val peek = steps.first { it.event == StackTraceEvent.PEEK }
        val pop = steps.first { it.event == StackTraceEvent.POP }

        assertTrue(steps.all { it.representation == StackRepresentation.ARRAY_AND_LINKED })
        assertEquals(listOf("10", "20", "30", "40", "50", "6"), peek.values)
        assertEquals("6", peek.returnedValue)
        assertEquals(peek.values, pop.values.plus(pop.returnedValue!!))
        assertTrue(peek.explanation.contains("without removing"))
    }

    @Test
    fun delimiterLessonMatchesLatestOpenersAndCompletesBalanced() {
        val steps = StackTrace.stepsFor(StackDemonstration.DELIMITER_MATCHING)
        val matches = steps.filter { it.event == StackTraceEvent.DELIMITER_MATCH }

        assertEquals(listOf("}", "]", ")"), matches.map { it.operationValue })
        assertEquals(listOf("{", "[", "("), matches.map { it.returnedValue })
        assertTrue(steps.last().isTerminal)
        assertEquals(true, steps.last().isValid)
        assertTrue(steps.last().explanation.contains("balanced"))
    }

    @Test
    fun delimiterMismatchPopsTheComparedOpenerAndReportsFailure() {
        val steps = StackTrace.generate(listOf(StackOperation.OpenDelimiter("["), StackOperation.CloseDelimiter(")")))
        val mismatch = steps.first { it.event == StackTraceEvent.DELIMITER_MISMATCH }

        assertEquals(emptyList(), mismatch.values)
        assertEquals("[", mismatch.returnedValue)
        assertFalse(mismatch.isTerminal)
        assertEquals(false, steps.last().isValid)
        assertTrue(steps.last().explanation.contains("invalid"))
    }

    @Test
    fun ordinaryEmptyOperationsAndCapacityOverflowRemainSafe() {
        val empty = StackTrace.generate(listOf(StackOperation.Peek, StackOperation.Pop))
        assertEquals(StackTraceEvent.UNDERFLOW, empty[1].event)
        assertEquals(StackTraceEvent.UNDERFLOW, empty[2].event)
        assertNull(empty[1].returnedValue)
        val full = StackTrace.generate(listOf(StackOperation.Push("C")), initialValues = listOf("A"), capacity = 1)
        assertEquals(StackTraceEvent.OVERFLOW, full[1].event)
        assertEquals(listOf("A"), full[1].values)
    }
}
