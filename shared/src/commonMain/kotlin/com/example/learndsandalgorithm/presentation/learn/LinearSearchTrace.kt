package com.example.learndsandalgorithm.presentation.learn

internal enum class LinearSearchResult {
    NOT_EQUAL,
    FOUND,
    NOT_FOUND
}

internal data class LinearSearchTraceStep(
    val values: List<Int>,
    val target: Int,
    val currentIndex: Int?,
    val examinedValue: Int?,
    val result: LinearSearchResult,
    val foundIndex: Int?,
    val inspectedCount: Int,
    val isTerminal: Boolean,
    val explanation: String
)

internal object LinearSearchTrace {
    const val target = 10

    val values = listOf(4, 8, 2, 7, 5, 9, 1, 6, 3, 10)

    val steps: List<LinearSearchTraceStep> = generate(values, target)

    fun generate(input: List<Int>, target: Int): List<LinearSearchTraceStep> = buildList {
        for ((index, value) in input.withIndex()) {
            val found = value == target
            add(
                LinearSearchTraceStep(
                    values = input.toList(),
                    target = target,
                    currentIndex = index,
                    examinedValue = value,
                    result = if (found) LinearSearchResult.FOUND else LinearSearchResult.NOT_EQUAL,
                    foundIndex = if (found) index else null,
                    inspectedCount = index + 1,
                    isTerminal = found,
                    explanation = if (found) {
                        "Check index $index: value $value == target $target. Target found at index $index."
                    } else {
                        "Check index $index: value $value != target $target. Continue to the next index."
                    }
                )
            )
            if (found) return@buildList
        }

        add(
            LinearSearchTraceStep(
                values = input.toList(),
                target = target,
                currentIndex = null,
                examinedValue = null,
                result = LinearSearchResult.NOT_FOUND,
                foundIndex = null,
                inspectedCount = input.size,
                isTerminal = true,
                explanation = "Target $target not found after inspecting all ${input.size} items."
            )
        )
    }
}
