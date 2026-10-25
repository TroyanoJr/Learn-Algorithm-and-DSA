package com.example.learndsandalgorithm.presentation.learn

internal data class BinarySearchStep(
    val values: List<Int>,
    val leftIndex: Int,
    val rightIndex: Int,
    val midIndex: Int,
    val activeRange: IntRange,
    val highlightedIndices: Set<Int>,
    val foundIndex: Int? = null,
    val explanation: String
)

internal object BinarySearchTrace {
    const val target = 11

    val values = listOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19)

    val steps = listOf(
        BinarySearchStep(
            values = values,
            leftIndex = 0,
            rightIndex = 9,
            midIndex = 4,
            activeRange = 0..9,
            highlightedIndices = setOf(4),
            explanation = "Mid = index 4 (val 9) < 11. Eliminate indices 0–4; search 5–9."
        ),
        BinarySearchStep(
            values = values,
            leftIndex = 5,
            rightIndex = 9,
            midIndex = 7,
            activeRange = 5..9,
            highlightedIndices = setOf(7),
            explanation = "Mid = index 7 (val 15) > 11. Eliminate indices 7–9; search 5–6."
        ),
        BinarySearchStep(
            values = values,
            leftIndex = 5,
            rightIndex = 6,
            midIndex = 5,
            activeRange = 5..6,
            highlightedIndices = setOf(5),
            foundIndex = 5,
            explanation = "Mid = index 5 (val 11) == 11. Target 11 found at index 5! Binary Search complete. ✓"
        )
    )
}
