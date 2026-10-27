package com.example.learndsandalgorithm.presentation.learn

internal enum class InsertionSortEvent {
    COMPARISON,
    SHIFT,
    INSERT,
    COMPLETE
}

internal data class InsertionSortTraceStep(
    val array: List<Int>,
    val insertionIndex: Int?,
    val key: Int?,
    val comparisonTargetIndex: Int?,
    val holeIndex: Int?,
    val highlightedIndices: List<Int>,
    val sortedIndices: List<Int>,
    val event: InsertionSortEvent,
    val message: String
)

internal object InsertionSortTrace {
    val initialValues = listOf(5, 3, 8, 4, 2, 6, 7, 9, 10, 11)

    val steps: List<InsertionSortTraceStep> = generate(initialValues)

    fun generate(input: List<Int>): List<InsertionSortTraceStep> = buildList {
        val values = input.toMutableList()

        for (insertionIndex in 1 until values.size) {
            val key = values[insertionIndex]
            var comparisonIndex = insertionIndex - 1
            var holeIndex = insertionIndex

            while (comparisonIndex >= 0) {
                val comparedValue = values[comparisonIndex]
                val shouldShift = comparedValue > key
                add(
                    InsertionSortTraceStep(
                        array = values.toList(),
                        insertionIndex = insertionIndex,
                        key = key,
                        comparisonTargetIndex = comparisonIndex,
                        holeIndex = holeIndex,
                        highlightedIndices = listOf(comparisonIndex, holeIndex).distinct(),
                        sortedIndices = (0 until holeIndex).toList(),
                        event = InsertionSortEvent.COMPARISON,
                        message = "Pass $insertionIndex: Compare key $key with $comparedValue at index $comparisonIndex; " +
                            if (shouldShift) "shift it right." else "no shift is needed."
                    )
                )

                if (!shouldShift) break

                values[comparisonIndex + 1] = comparedValue
                val shiftedFrom = comparisonIndex
                val shiftedTo = comparisonIndex + 1
                comparisonIndex--
                holeIndex = comparisonIndex + 1
                add(
                    InsertionSortTraceStep(
                        array = values.toList(),
                        insertionIndex = insertionIndex,
                        key = key,
                        comparisonTargetIndex = null,
                        holeIndex = holeIndex,
                        highlightedIndices = listOf(shiftedFrom, shiftedTo),
                        sortedIndices = (0 until holeIndex).toList(),
                        event = InsertionSortEvent.SHIFT,
                        message = "Shift $comparedValue from index $shiftedFrom to index $shiftedTo; " +
                            "hold key $key at the open position."
                    )
                )
            }

            val insertionPosition = comparisonIndex + 1
            values[insertionPosition] = key
            add(
                InsertionSortTraceStep(
                    array = values.toList(),
                    insertionIndex = insertionIndex,
                    key = key,
                    comparisonTargetIndex = null,
                    holeIndex = insertionPosition,
                    highlightedIndices = listOf(insertionPosition),
                    sortedIndices = (0..insertionIndex).toList(),
                    event = InsertionSortEvent.INSERT,
                    message = "Insert key $key at index $insertionPosition; " +
                        "sorted prefix ends at index $insertionIndex."
                )
            )
        }

        add(
            InsertionSortTraceStep(
                array = values.toList(),
                insertionIndex = null,
                key = null,
                comparisonTargetIndex = null,
                holeIndex = null,
                highlightedIndices = emptyList(),
                sortedIndices = values.indices.toList(),
                event = InsertionSortEvent.COMPLETE,
                message = "Insertion Sort complete: all values are sorted. ✓"
            )
        )
    }
}
