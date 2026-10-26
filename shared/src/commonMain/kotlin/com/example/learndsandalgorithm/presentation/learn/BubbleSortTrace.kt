package com.example.learndsandalgorithm.presentation.learn

internal data class BubbleSortTraceStep(
    val array: List<Int>,
    val highlightedIndices: List<Int>,
    val sortedIndices: List<Int>,
    val message: String,
    val isCompletion: Boolean = false
)

internal object BubbleSortTrace {
    val initialValues = listOf(1, 2, 3, 4, 5, 6, 7, 9, 8, 10)

    val steps: List<BubbleSortTraceStep> = generate(initialValues)

    fun generate(input: List<Int>): List<BubbleSortTraceStep> = buildList {
        val values = input.toMutableList()
        val sortedSuffix = mutableListOf<Int>()
        var completed = false

        for (pass in 0 until values.lastIndex) {
            var swapped = false
            val comparisonEnd = values.lastIndex - pass

            for (leftIndex in 0 until comparisonEnd) {
                val left = values[leftIndex]
                val right = values[leftIndex + 1]
                val didSwap = left > right
                if (didSwap) {
                    values[leftIndex] = right
                    values[leftIndex + 1] = left
                    swapped = true
                }

                val passPrefix = if (leftIndex == 0) "Pass ${pass + 1}: " else ""
                val result = if (didSwap) "swap" else "no swap"
                val sortedIndices = if (leftIndex == comparisonEnd - 1) {
                    sortedSuffix + comparisonEnd
                } else {
                    sortedSuffix
                }
                add(
                    BubbleSortTraceStep(
                        array = values.toList(),
                        highlightedIndices = listOf(leftIndex, leftIndex + 1),
                        sortedIndices = sortedIndices.sorted(),
                        message = "${passPrefix}Compare $left & $right → $result"
                    )
                )
            }

            sortedSuffix.add(comparisonEnd)
            if (!swapped) {
                completed = true
                break
            }
        }

        add(
            BubbleSortTraceStep(
                array = values.toList(),
                highlightedIndices = emptyList(),
                sortedIndices = values.indices.toList(),
                message = if (completed) {
                    "No swaps this pass; array sorted! ✓"
                } else {
                    "All values sorted! ✓"
                },
                isCompletion = true
            )
        )
    }
}
