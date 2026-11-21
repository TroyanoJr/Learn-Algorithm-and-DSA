package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecursionTradeoffsTraceTest {

    @Test
    fun createTraceComparesIterativeAndRecursiveSumTo4() {
        val steps = RecursionTradeoffsTrace.createTrace(inputN = 4)
        assertEquals(15, steps.size)
        assertEquals(
            listOf(
                TradeoffAction.INIT,
                TradeoffAction.ITERATIVE_LOOP, TradeoffAction.ITERATIVE_LOOP, TradeoffAction.ITERATIVE_LOOP, TradeoffAction.ITERATIVE_LOOP,
                TradeoffAction.RECURSIVE_DESCENT, TradeoffAction.RECURSIVE_DESCENT, TradeoffAction.RECURSIVE_DESCENT, TradeoffAction.RECURSIVE_DESCENT,
                TradeoffAction.RECURSIVE_BASE,
                TradeoffAction.RECURSIVE_ASCENT, TradeoffAction.RECURSIVE_ASCENT, TradeoffAction.RECURSIVE_ASCENT, TradeoffAction.RECURSIVE_ASCENT,
                TradeoffAction.FINISHED
            ),
            steps.map { it.action }
        )
        assertEquals(listOf(1, 3, 6, 10), steps.filter { it.action == TradeoffAction.ITERATIVE_LOOP }.map { it.iterTotal })
        assertEquals(listOf(1, 2, 3, 4), steps.filter { it.action == TradeoffAction.ITERATIVE_LOOP }.map { it.iterI })
        assertEquals(listOf(1, 2, 3, 4, 5), steps.filter { it.action == TradeoffAction.RECURSIVE_DESCENT || it.action == TradeoffAction.RECURSIVE_BASE }.map { it.recurCallStack.size })
        assertEquals(listOf(1, 3, 6, 10), steps.filter { it.action == TradeoffAction.RECURSIVE_ASCENT }.map { it.recurReturnVal })
        assertTrue(steps.first().explanation.contains("sum = 10"))

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(TradeoffAction.FINISHED, lastStep.action)
        assertEquals(10, lastStep.iterTotal)
        assertEquals(10, lastStep.recurReturnVal)
        assertEquals(emptyList(), lastStep.recurCallStack)
    }

    @Test
    fun everySnapshotHasConciseOperationSpecificExplanation() {
        val steps = RecursionTradeoffsTrace.createTrace(inputN = 4)

        assertEquals(15, steps.size)
        assertTrue(steps.all { it.explanation.isNotBlank() && it.badgeText.isNotBlank() })
        assertTrue(steps.all { step -> step.explanation.count { it == '.' } in 1..2 })

        val iteration = steps.filter { it.action == TradeoffAction.ITERATIVE_LOOP }
        assertTrue(iteration.all { "reused accumulator" in it.explanation })
        assertTrue(iteration.all { "O(1)" in it.explanation })
        iteration.forEach { step -> assertTrue("total = ${step.iterTotal}" in step.explanation) }

        val descent = steps.filter { it.action == TradeoffAction.RECURSIVE_DESCENT }
        descent.forEach { step ->
            val n = step.recurCallStack.last()
            assertTrue("sumTo($n)" in step.explanation)
            assertTrue("sumTo(${n - 1})" in step.explanation)
            assertTrue("depth grows to ${step.recurCallStack.size}" in step.explanation)
        }

        val base = steps.single { it.action == TradeoffAction.RECURSIVE_BASE }
        assertTrue("sumTo(0) returns 0" in base.explanation)
        assertTrue("${base.recurCallStack.size} active frames" in base.explanation)
        assertTrue("O(N)" in base.explanation)

        steps.filter { it.action == TradeoffAction.RECURSIVE_ASCENT }.forEach { step ->
            val n = step.recurCallStack.last()
            val recipient = step.recurCallStack.dropLast(1).lastOrNull()?.let { "sumTo($it)" } ?: "the final result"
            assertTrue("sumTo($n)" in step.explanation)
            assertTrue("returns ${step.recurReturnVal} to $recipient" in step.explanation)
            assertTrue("frame is then released" in step.explanation)
        }

        val finished = steps.last()
        assertTrue("O(1)" in finished.explanation)
        assertTrue("O(N)" in finished.explanation)
        assertTrue("tail-call optimization" in finished.explanation)
    }

    @Test
    fun stepsHeightUsesLongestWrappedSnapshotAndDoesNotDependOnCurrentStep() {
        val trace = RecursionTradeoffsTrace.createTrace()
        assertEquals(15, trace.size)
        val measuredBodyHeights = listOf(72, 84, 96, 108, 120, 132, 144, 156, 168, 180, 192, 204, 216, 228, 240)
        val requiredHeight = stableRecursionTradeoffsStepsHeightPx(
            headerHeightPx = 20,
            wrappedBodyHeightsPx = measuredBodyHeights,
            verticalPaddingPx = 8,
            headerBodyGapPx = 6
        )
        assertEquals(282, requiredHeight)
        val stableHeightsAcrossPlayback = trace.map {
            stableRecursionTradeoffsStepsHeightPx(
                headerHeightPx = 20,
                wrappedBodyHeightsPx = measuredBodyHeights,
                verticalPaddingPx = 8,
                headerBodyGapPx = 6
            )
        }
        assertTrue(stableHeightsAcrossPlayback.all { it == requiredHeight })
        assertTrue(
            stableRecursionTradeoffsStepsHeightPx(20, measuredBodyHeights + 300, 8, 6) > requiredHeight
        )
    }

    @Test
    fun allocationGivesTheoryOnlyTheBoundedHeightRemainingAfterSteps() {
        val allocation = recursionTradeoffsDetailsAllocation(
            availableHeightDp = 220f,
            requiredStepsHeightDp = 130f,
            preferredGapDp = 8f,
            minimumTheoryHeaderHeightDp = 40f
        )
        assertEquals(130f, allocation.stepsHeightDp)
        assertEquals(82f, allocation.theoryHeightDp)
        assertEquals(8f, allocation.gapDp)
        assertTrue(allocation.completeStepsFit)
        assertTrue(allocation.hasUsableTheoryHeader)
        assertEquals(220f, allocation.stepsHeightDp + allocation.theoryHeightDp + allocation.gapDp)

        val compressed = recursionTradeoffsDetailsAllocation(
            availableHeightDp = 180f,
            requiredStepsHeightDp = 130f,
            preferredGapDp = 8f,
            minimumTheoryHeaderHeightDp = 40f
        )
        assertEquals(42f, compressed.theoryHeightDp)
        assertEquals(8f, compressed.gapDp)
        assertTrue(compressed.hasUsableTheoryHeader)

        val impossible = recursionTradeoffsDetailsAllocation(
            availableHeightDp = 120f,
            requiredStepsHeightDp = 130f,
            preferredGapDp = 8f,
            minimumTheoryHeaderHeightDp = 40f
        )
        assertEquals(130f, impossible.stepsHeightDp)
        assertEquals(0f, impossible.theoryHeightDp)
        assertFalse(impossible.completeStepsFit)
        assertFalse(impossible.hasUsableTheoryHeader)

        listOf(
            recursionTradeoffsDetailsAllocation(-4f, -10f, -8f, -40f),
            recursionTradeoffsDetailsAllocation(Float.POSITIVE_INFINITY, Float.NaN),
            recursionTradeoffsDetailsAllocation(90f, 80f)
        ).forEach { result ->
            assertTrue(result.stepsHeightDp.isFinite() && result.stepsHeightDp >= 0f)
            assertTrue(result.theoryHeightDp.isFinite() && result.theoryHeightDp >= 0f)
            assertTrue(result.gapDp.isFinite() && result.gapDp >= 0f)
        }
    }

    @Test
    fun measuredStepsHeightRespondsToNarrowerWidthWrapping() {
        val headerHeightPx = 20
        val wideWrappedBodiesPx = listOf(52, 64, 76, 88)
        val narrowWrappedBodiesPx = listOf(76, 100, 124, 172)

        val wideHeight = stableRecursionTradeoffsStepsHeightPx(
            headerHeightPx,
            wideWrappedBodiesPx,
            verticalPaddingPx = 8,
            headerBodyGapPx = 6
        )
        val narrowHeight = stableRecursionTradeoffsStepsHeightPx(
            headerHeightPx,
            narrowWrappedBodiesPx,
            verticalPaddingPx = 8,
            headerBodyGapPx = 6
        )

        assertTrue(narrowHeight > wideHeight)
        assertEquals(172, narrowHeight - (8 * 2 + headerHeightPx + 6))
    }
}
