package com.example.learndsandalgorithm.presentation.learn

internal enum class SelectionSortEvent {
    COMPARISON,
    SWAP,
    PASS_COMPLETE,
    COMPLETE
}

internal data class SelectionSortTraceStep(
    val array: List<Int>,
    val passIndex: Int?,
    val comparisonTargetIndex: Int?,
    val minimumIndex: Int?,
    val highlightedIndices: List<Int>,
    val sortedIndices: List<Int>,
    val event: SelectionSortEvent,
    val message: String
)

internal object SelectionSortTrace {
    val initialValues = listOf(5, 3, 8, 4, 2, 9, 7, 10, 6, 1)

    val steps: List<SelectionSortTraceStep> = generate(initialValues)

    fun generate(input: List<Int>): List<SelectionSortTraceStep> = buildList {
        val values = input.toMutableList()
        var sortedPrefixEnd = -1

        for (position in 0 until values.lastIndex) {
            var minimumIndex = position

            for (comparisonIndex in position + 1 until values.size) {
                val currentMinimum = values[minimumIndex]
                val candidate = values[comparisonIndex]
                val previousMinimumIndex = minimumIndex
                if (candidate < currentMinimum) minimumIndex = comparisonIndex

                add(
                    SelectionSortTraceStep(
                        array = values.toList(),
                        passIndex = position,
                        comparisonTargetIndex = comparisonIndex,
                        minimumIndex = minimumIndex,
                        highlightedIndices = listOf(previousMinimumIndex, comparisonIndex),
                        sortedIndices = (0..sortedPrefixEnd).toList(),
                        event = SelectionSortEvent.COMPARISON,
                        message = "Pass ${position + 1}: Compare ${values[previousMinimumIndex]} at index $previousMinimumIndex " +
                            "with $candidate at index $comparisonIndex; minimum is ${values[minimumIndex]} at index $minimumIndex."
                    )
                )
            }

            if (minimumIndex != position) {
                val oldValue = values[position]
                val minimumValue = values[minimumIndex]
                values[position] = minimumValue
                values[minimumIndex] = oldValue
                sortedPrefixEnd = position

                add(
                    SelectionSortTraceStep(
                        array = values.toList(),
                        passIndex = position,
                        comparisonTargetIndex = null,
                        minimumIndex = position,
                        highlightedIndices = listOf(position, minimumIndex),
                        sortedIndices = (0..sortedPrefixEnd).toList(),
                        event = SelectionSortEvent.SWAP,
                        message = "Swap minimum $minimumValue from index $minimumIndex into index $position; " +
                            "sorted prefix ends at index $position."
                    )
                )
            } else {
                sortedPrefixEnd = position
                add(
                    SelectionSortTraceStep(
                        array = values.toList(),
                        passIndex = position,
                        comparisonTargetIndex = null,
                        minimumIndex = position,
                        highlightedIndices = emptyList(),
                        sortedIndices = (0..sortedPrefixEnd).toList(),
                        event = SelectionSortEvent.PASS_COMPLETE,
                        message = "Minimum ${values[position]} is already at index $position; " +
                            "sorted prefix ends at index $position."
                    )
                )
            }
        }

        add(
            SelectionSortTraceStep(
                array = values.toList(),
                passIndex = null,
                comparisonTargetIndex = null,
                minimumIndex = null,
                highlightedIndices = emptyList(),
                sortedIndices = values.indices.toList(),
                event = SelectionSortEvent.COMPLETE,
                message = "Selection Sort complete: all values are sorted. ✓"
            )
        )
    }
}
