package com.example.learndsandalgorithm.presentation.learn

internal enum class MergeStepEvent {
    START,
    COMPARE,
    TAKE_LEFT,
    TAKE_RIGHT,
    FLUSH_REMAINING,
    COMPLETE
}

internal enum class MergeSource { LEFT, RIGHT }

internal data class MergeElementOrigin(val source: MergeSource, val index: Int) {
    val identity: String get() = "${source.name.lowercase()}:$index"
}

internal data class MergePointers(val i: Int, val j: Int, val k: Int)

internal data class MergeComparison(
    val leftIndex: Int,
    val rightIndex: Int,
    val leftValue: Int,
    val rightValue: Int
) {
    val valuesAreEqual: Boolean get() = leftValue == rightValue
}

internal enum class MergeTieBreakDecision { LEFT_FIRST_ON_EQUAL }

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
    val isTerminal: Boolean = false,
    val comparison: MergeComparison? = null,
    val source: MergeSource? = null,
    val sourceIndexBeforeTransfer: Int? = null,
    val destinationIndex: Int? = null,
    val selectedOrigin: MergeElementOrigin? = null,
    val pointersBeforeTransfer: MergePointers? = null,
    val pointersAfterTransfer: MergePointers? = null,
    val outputOrigins: List<MergeElementOrigin> = emptyList(),
    val tieBreakDecision: MergeTieBreakDecision? = null,
    val leftExhausted: Boolean = false,
    val rightExhausted: Boolean = false
) {
    val k: Int get() = mergedResult.size
}

internal object MergeSortFundamentalsTrace {

    fun stepsForMerge(
        left: List<Int> = listOf(1, 4, 7),
        right: List<Int> = listOf(2, 5, 8)
    ): List<MergeFundamentalsTraceStep> {
        require(left.zipWithNext().all { (a, b) -> a <= b }) { "Left input must be sorted in nondecreasing order" }
        require(right.zipWithNext().all { (a, b) -> a <= b }) { "Right input must be sorted in nondecreasing order" }

        val steps = mutableListOf<MergeFundamentalsTraceStep>()
        val result = mutableListOf<Int>()
        val origins = mutableListOf<MergeElementOrigin>()
        var i = 0
        var j = 0

        fun snapshot(
            event: MergeStepEvent,
            comparedLeft: Int? = null,
            comparedRight: Int? = null,
            taken: Int? = null,
            isTerminal: Boolean = false,
            comparison: MergeComparison? = null,
            source: MergeSource? = null,
            sourceIndexBeforeTransfer: Int? = null,
            destinationIndex: Int? = null,
            selectedOrigin: MergeElementOrigin? = null,
            pointersBeforeTransfer: MergePointers? = null,
            pointersAfterTransfer: MergePointers? = null,
            tieBreakDecision: MergeTieBreakDecision? = null,
            explanation: String,
            badgeText: String
        ) = MergeFundamentalsTraceStep(
            leftArr = left.toList(),
            rightArr = right.toList(),
            i = i,
            j = j,
            mergedResult = result.toList(),
            event = event,
            comparedLeftVal = comparedLeft,
            comparedRightVal = comparedRight,
            takenVal = taken,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal,
            comparison = comparison,
            source = source,
            sourceIndexBeforeTransfer = sourceIndexBeforeTransfer,
            destinationIndex = destinationIndex,
            selectedOrigin = selectedOrigin,
            pointersBeforeTransfer = pointersBeforeTransfer,
            pointersAfterTransfer = pointersAfterTransfer,
            outputOrigins = origins.toList(),
            tieBreakDecision = tieBreakDecision,
            leftExhausted = i >= left.size,
            rightExhausted = j >= right.size
        )

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
            val comparison = MergeComparison(i, j, leftVal, rightVal)
            val tieBreak = if (leftVal == rightVal) MergeTieBreakDecision.LEFT_FIRST_ON_EQUAL else null

            steps.add(
                snapshot(
                    event = MergeStepEvent.COMPARE,
                    comparedLeft = leftVal,
                    comparedRight = rightVal,
                    comparison = comparison,
                    tieBreakDecision = tieBreak,
                    explanation = "Compare front elements: Left[$i] = $leftVal vs Right[$j] = $rightVal.",
                    badgeText = "COMPARE $leftVal vs $rightVal"
                )
            )

            val source = if (leftVal <= rightVal) MergeSource.LEFT else MergeSource.RIGHT
            val sourceIndex = if (source == MergeSource.LEFT) i else j
            val taken = if (source == MergeSource.LEFT) leftVal else rightVal
            val before = MergePointers(i, j, result.size)
            val origin = MergeElementOrigin(source, sourceIndex)
            result.add(taken)
            origins.add(origin)
            if (source == MergeSource.LEFT) i++ else j++
            val after = MergePointers(i, j, result.size)

            val comparisonText = if (source == MergeSource.LEFT) {
                if (tieBreak != null) "$leftVal = $rightVal. Equal values: take Left[$sourceIndex] first to preserve stability."
                else "$leftVal < $rightVal. Append Left[$sourceIndex] = $leftVal to merged output."
            } else {
                "$rightVal < $leftVal. Append Right[$sourceIndex] = $rightVal to merged output."
            }
            val pointerText = if (source == MergeSource.LEFT) "Pointer i advances to $i." else "Pointer j advances to $j."

            steps.add(
                snapshot(
                    event = if (source == MergeSource.LEFT) MergeStepEvent.TAKE_LEFT else MergeStepEvent.TAKE_RIGHT,
                    comparedLeft = leftVal,
                    comparedRight = rightVal,
                    taken = taken,
                    comparison = comparison,
                    source = source,
                    sourceIndexBeforeTransfer = sourceIndex,
                    destinationIndex = before.k,
                    selectedOrigin = origin,
                    pointersBeforeTransfer = before,
                    pointersAfterTransfer = after,
                    tieBreakDecision = tieBreak,
                    explanation = "$comparisonText $pointerText",
                    badgeText = if (source == MergeSource.LEFT) "TAKE LEFT: $taken" else "TAKE RIGHT: $taken"
                )
            )
        }

        fun flush(source: MergeSource) {
            val sourceValues = if (source == MergeSource.LEFT) left else right
            val sourceIndex = if (source == MergeSource.LEFT) i else j
            val value = sourceValues[sourceIndex]
            val before = MergePointers(i, j, result.size)
            val origin = MergeElementOrigin(source, sourceIndex)
            result.add(value)
            origins.add(origin)
            if (source == MergeSource.LEFT) i++ else j++
            val after = MergePointers(i, j, result.size)
            val pointerName = if (source == MergeSource.LEFT) "i" else "j"
            val sourceLabel = if (source == MergeSource.LEFT) "Left" else "Right"

            steps.add(
                snapshot(
                    event = MergeStepEvent.FLUSH_REMAINING,
                    taken = value,
                    source = source,
                    sourceIndexBeforeTransfer = sourceIndex,
                    destinationIndex = before.k,
                    selectedOrigin = origin,
                    pointersBeforeTransfer = before,
                    pointersAfterTransfer = after,
                    explanation = "${if (source == MergeSource.LEFT) "Right" else "Left"} array exhausted. Append remaining $sourceLabel[$sourceIndex] = $value to output. Pointer $pointerName advances to ${if (source == MergeSource.LEFT) i else j}.",
                    badgeText = "FLUSH $sourceLabel: $value"
                )
            )
        }

        while (i < left.size) flush(MergeSource.LEFT)
        while (j < right.size) flush(MergeSource.RIGHT)

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
