package com.example.learndsandalgorithm.presentation.learn

internal enum class QuickPartitionEvent {
    START,
    SELECT_PIVOT,
    SCAN_COMPARE,
    SWAP_ELEMENTS,
    PLACE_PIVOT,
    PARTITION_COMPLETE,
    SORT_COMPLETE
}

internal data class QuickPartitionTraceStep(
    val array: List<Int>,
    val low: Int,
    val high: Int,
    val pivotIndex: Int,
    val pivotValue: Int,
    val i: Int,
    val j: Int,
    val swappedIndices: Pair<Int, Int>? = null,
    val event: QuickPartitionEvent,
    val finalPivotPosition: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object QuickSortPartitionTrace {

    fun stepsForPartition(
        initialArr: List<Int> = listOf(10, 7, 8, 9, 1, 5)
    ): List<QuickPartitionTraceStep> {
        val steps = mutableListOf<QuickPartitionTraceStep>()
        val arr = initialArr.toMutableList()
        val low = 0
        val high = arr.lastIndex
        val pivotVal = arr[high]

        fun snapshot(
            i: Int,
            j: Int,
            event: QuickPartitionEvent,
            swapped: Pair<Int, Int>? = null,
            finalPivotPos: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = QuickPartitionTraceStep(
            array = arr.toList(),
            low = low,
            high = high,
            pivotIndex = high,
            pivotValue = pivotVal,
            i = i,
            j = j,
            swappedIndices = swapped,
            event = event,
            finalPivotPosition = finalPivotPos,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        var i = low - 1

        // Step 0: Ready
        steps.add(
            snapshot(
                i = i,
                j = low,
                event = QuickPartitionEvent.START,
                explanation = "Quick Sort (Lomuto Partitioning): Rearranging array [${arr.joinToString(", ")}] around pivot. In-place partitioning requires no extra merge space.",
                badgeText = "READY · INPUT [${arr.joinToString(", ")}]"
            )
        )

        // Step 1: Select Pivot
        steps.add(
            snapshot(
                i = i,
                j = low,
                event = QuickPartitionEvent.SELECT_PIVOT,
                explanation = "Select pivot element at arr[$high] = $pivotVal. i boundary = $i (left of array). Pointer j will scan index $low to ${high - 1}.",
                badgeText = "PIVOT = $pivotVal AT INDEX $high"
            )
        )

        // Scan j from low to high - 1
        for (j in low until high) {
            val valJ = arr[j]

            // Compare step
            steps.add(
                snapshot(
                    i = i,
                    j = j,
                    event = QuickPartitionEvent.SCAN_COMPARE,
                    explanation = "Scan j = $j: Compare arr[$j] ($valJ) <= pivot ($pivotVal).",
                    badgeText = "COMPARE arr[$j]=$valJ <= PIVOT($pivotVal)"
                )
            )

            if (valJ <= pivotVal) {
                i++
                if (i != j) {
                    val temp = arr[i]
                    arr[i] = arr[j]
                    arr[j] = temp

                    steps.add(
                        snapshot(
                            i = i,
                            j = j,
                            event = QuickPartitionEvent.SWAP_ELEMENTS,
                            swapped = Pair(i, j),
                            explanation = "$valJ <= $pivotVal: Advance boundary i to $i and SWAP arr[$i] ($temp) with arr[$j] ($valJ).",
                            badgeText = "SWAP arr[$i] ↔ arr[$j]"
                        )
                    )
                } else {
                    steps.add(
                        snapshot(
                            i = i,
                            j = j,
                            event = QuickPartitionEvent.SCAN_COMPARE,
                            explanation = "$valJ <= $pivotVal: Advance boundary i to $i (element $valJ is already in left partition).",
                            badgeText = "IN PARTITION: arr[$i]=$valJ"
                        )
                    )
                }
            }
        }

        // Place Pivot at i + 1
        val finalPivotPos = i + 1
        val temp = arr[finalPivotPos]
        arr[finalPivotPos] = arr[high]
        arr[high] = temp

        steps.add(
            snapshot(
                i = i,
                j = high,
                event = QuickPartitionEvent.PLACE_PIVOT,
                swapped = Pair(finalPivotPos, high),
                finalPivotPos = finalPivotPos,
                explanation = "Scan complete! SWAP pivot arr[$high] ($pivotVal) into its final sorted position at index $finalPivotPos (arr[$finalPivotPos]=$temp).",
                badgeText = "PIVOT PLACED AT INDEX $finalPivotPos"
            )
        )

        // Complete Partition
        steps.add(
            snapshot(
                i = i,
                j = high,
                event = QuickPartitionEvent.PARTITION_COMPLETE,
                finalPivotPos = finalPivotPos,
                isTerminal = true,
                explanation = "Lomuto Partition complete! Left partition (< $pivotVal): [${arr.subList(0, finalPivotPos).joinToString(", ")}], Pivot: $pivotVal at [$finalPivotPos], Right partition (> $pivotVal): [${arr.subList(finalPivotPos + 1, arr.size).joinToString(", ")}].",
                badgeText = "PARTITION COMPLETE AT [$finalPivotPos]"
            )
        )

        return steps
    }
}
