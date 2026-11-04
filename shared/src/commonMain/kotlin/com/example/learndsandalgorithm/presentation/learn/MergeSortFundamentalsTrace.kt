package com.example.learndsandalgorithm.presentation.learn

internal enum class MergeStepEvent {
    START,
    COMPARE,
    TAKE_LEFT,
    TAKE_RIGHT,
    FLUSH_REMAINING,
    COMPLETE
}

internal data class MergeFundamentalsTraceStep(
    val leftArr: List<Int>,
    val rightArr: List<Int>,
    val i: Int,
    val j: Int,
    val mergedResult: List<Int>,
    val event: MergeStepEvent,
    val comparedLeftVal: Int? = null,
    val comparedRightVal: Int? = null,
    val takenVal: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object MergeSortFundamentalsTrace {

    fun stepsForMerge(
        left: List<Int> = listOf(1, 4, 7),
        right: List<Int> = listOf(2, 5, 8)
    ): List<MergeFundamentalsTraceStep> {
        val steps = mutableListOf<MergeFundamentalsTraceStep>()
        val result = mutableListOf<Int>()
        var i = 0
        var j = 0

        fun snapshot(
            event: MergeStepEvent,
            comparedLeft: Int? = null,
            comparedRight: Int? = null,
            taken: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = MergeFundamentalsTraceStep(
            leftArr = left,
            rightArr = right,
            i = i,
            j = j,
            mergedResult = result.toList(),
            event = event,
            comparedLeftVal = comparedLeft,
            comparedRightVal = comparedRight,
            takenVal = taken,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = MergeStepEvent.START,
                explanation = "Merge Step: Merging two sorted arrays Left [${left.joinToString(", ")}] and Right [${right.joinToString(", ")}] into one combined sorted array.",
                badgeText = "READY · MERGE TWO HALVES"
            )
        )

        while (i < left.size && j < right.size) {
            val leftVal = left[i]
            val rightVal = right[j]

            // Step A: Compare front elements
            steps.add(
                snapshot(
                    event = MergeStepEvent.COMPARE,
                    comparedLeft = leftVal,
                    comparedRight = rightVal,
                    explanation = "Compare front elements: Left[$i] = $leftVal vs Right[$j] = $rightVal.",
                    badgeText = "COMPARE $leftVal vs $rightVal"
                )
            )

            // Step B: Take smaller element
            if (leftVal <= rightVal) {
                result.add(leftVal)
                i++
                steps.add(
                    snapshot(
                        event = MergeStepEvent.TAKE_LEFT,
                        comparedLeft = leftVal,
                        comparedRight = rightVal,
                        taken = leftVal,
                        explanation = "$leftVal <= $rightVal. Append Left[$leftVal] to merged output. Pointer i advances to $i.",
                        badgeText = "TAKE LEFT: $leftVal"
                    )
                )
            } else {
                result.add(rightVal)
                j++
                steps.add(
                    snapshot(
                        event = MergeStepEvent.TAKE_RIGHT,
                        comparedLeft = leftVal,
                        comparedRight = rightVal,
                        taken = rightVal,
                        explanation = "$rightVal < $leftVal. Append Right[$rightVal] to merged output. Pointer j advances to $j.",
                        badgeText = "TAKE RIGHT: $rightVal"
                    )
                )
            }
        }

        // Flush remaining left elements
        while (i < left.size) {
            val leftVal = left[i]
            result.add(leftVal)
            i++
            steps.add(
                snapshot(
                    event = MergeStepEvent.FLUSH_REMAINING,
                    taken = leftVal,
                    explanation = "Right array exhausted. Append remaining Left element $leftVal to output.",
                    badgeText = "FLUSH LEFT: $leftVal"
                )
            )
        }

        // Flush remaining right elements
        while (j < right.size) {
            val rightVal = right[j]
            result.add(rightVal)
            j++
            steps.add(
                snapshot(
                    event = MergeStepEvent.FLUSH_REMAINING,
                    taken = rightVal,
                    explanation = "Left array exhausted. Append remaining Right element $rightVal to output.",
                    badgeText = "FLUSH RIGHT: $rightVal"
                )
            )
        }

        // Complete
        steps.add(
            snapshot(
                event = MergeStepEvent.COMPLETE,
                isTerminal = true,
                explanation = "Merge complete! Combined sorted result: [${result.joinToString(", ")}] in O(N) time.",
                badgeText = "MERGE COMPLETE [${result.joinToString(", ")}]"
            )
        )

        return steps
    }
}
