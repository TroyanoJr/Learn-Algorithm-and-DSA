package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamicProgrammingDetailsPanelTest {
    @Test
    fun completeMeasuredHeightsDetermineFitAndDeficit() {
        val samples = listOf(
            DpMemoizationTrace.createTrace().map { it.explanation },
            DpTabulationTrace.createTrace().map { it.explanation },
            Knapsack01DpTrace.createTrace().map { it.explanation },
            CoinChangeDpTrace.createTrace().map { it.explanation },
            LcsDpTrace.createTrace().map { it.explanation },
            UniquePathsDpTrace.createTrace().map { it.explanation }
        )
        samples.forEach { explanations ->
            assertTrue(explanations.all { it.isNotBlank() })
            val longest = explanations.maxBy { it.length }
            // Synthetic line metrics exercise the allocation contract, not Compose text layout.
            val lines = (longest.length + 19) / 20
            val requiredSteps = 15f + lines * 16f
            val exact = DpDetailsFit(60f, requiredSteps, 6f, 66f + requiredSteps)
            assertTrue(exact.fits)
            assertEquals(0f, exact.deficit)
            val narrow = exact.copy(available = exact.required - 12f)
            assertFalse(narrow.fits)
            assertEquals(12f, narrow.deficit)
            assertTrue(exact.copy(stepsHeight = requiredSteps * 1.5f).deficit > 0f)
        }
    }
}
