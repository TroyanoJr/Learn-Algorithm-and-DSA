package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FractionalKnapsackTraceTest {

    @Test
    fun createTraceComputesCorrectMaxFractionalValue() {
        val steps = FractionalKnapsackTrace.createTrace()
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(FractionalKnapsackStatus.FINISHED, lastStep.status)
        assertEquals(240.0, lastStep.totalValue)
        assertEquals(0.0, lastStep.remainingCapacity)
        assertEquals(3, lastStep.selections.size)

        // Selections check: A (100%), B (100%), C (66.67%)
        assertEquals("A", lastStep.selections[0].item.name)
        assertEquals(1.0, lastStep.selections[0].fraction)

        assertEquals("B", lastStep.selections[1].item.name)
        assertEquals(1.0, lastStep.selections[1].fraction)

        assertEquals("C", lastStep.selections[2].item.name)
        assertEquals(20.0 / 30.0, lastStep.selections[2].fraction)
    }

    @Test
    fun decimalInputsRemainPreciseInTraceExplanationsAndBadges() {
        val steps = FractionalKnapsackTrace.createTrace(
            items = listOf(
                KnapsackItem("A", weight = 1.25, value = 3.75),
                KnapsackItem("B", weight = 2.0, value = 4.25)
            ),
            capacity = 2.75
        )

        assertEquals(
            listOf(
                FractionalKnapsackStatus.INIT,
                FractionalKnapsackStatus.SORTED,
                FractionalKnapsackStatus.EVALUATING,
                FractionalKnapsackStatus.TAKE_FULL,
                FractionalKnapsackStatus.EVALUATING,
                FractionalKnapsackStatus.TAKE_FRACTION,
                FractionalKnapsackStatus.FINISHED
            ),
            steps.map { it.status }
        )
        assertTrue(steps.first().explanation.contains("Capacity = 2.75"))
        assertTrue(steps.first().explanation.contains("A(w:1.25, v:3.75)"))
        assertEquals("READY · CAPACITY 2.75", steps.first().badgeText)
        assertTrue(steps[2].explanation.contains("w:1.25, v:3.75, ratio:3). Remaining capacity = 2.75"))
        assertTrue(steps[3].explanation.contains("Added weight 1.25, value 3.75. Remaining capacity = 1.5, Total value = 3.75"))
        assertEquals("TAKE FULL A (+3.75 VALUE)", steps[3].badgeText)
        assertTrue(steps[4].explanation.contains("w:2, v:4.25, ratio:2.125). Remaining capacity = 1.5"))
        assertTrue(steps[5].explanation.contains("Take 75% (1.5 / 2) fraction of B. Added weight 1.5, value 3.1875"))
        assertEquals("TAKE 75% FRACTION OF B (+3.1875 VALUE)", steps[5].badgeText)
        assertTrue(steps.last().explanation.contains("Total Value = 6.9375"))
        assertEquals("COMPLETE: TOTAL VALUE 6.9375", steps.last().badgeText)
        assertEquals(6.9375, steps.last().totalValue)
    }

    @Test
    fun evaluatingBadgeFormatsNonIntegerValueWeightRatio() {
        val item = KnapsackItem("Ratio", weight = 3.0, value = 5.0)
        val evaluatingStep = FractionalKnapsackTrace.createTrace(listOf(item), capacity = 3.0)
            .single { it.status == FractionalKnapsackStatus.EVALUATING }
        val expectedRatio = formatGreedyDecimal(item.ratio)

        assertTrue(item.ratio % 1.0 != 0.0)
        assertEquals("EVALUATE Ratio (RATIO $expectedRatio)", evaluatingStep.badgeText)
        assertTrue(evaluatingStep.explanation.contains("ratio:$expectedRatio"))
    }
}
