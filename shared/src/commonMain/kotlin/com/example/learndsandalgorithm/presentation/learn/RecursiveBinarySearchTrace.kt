package com.example.learndsandalgorithm.presentation.learn

internal enum class BinarySearchEvent {
    START,
    CALCULATE_MID,
    COMPARE_TARGET,
    RECURSE_LEFT,
    RECURSE_RIGHT,
    TARGET_FOUND,
    TARGET_NOT_FOUND
}

internal data class BinarySearchTraceStep(
    val array: List<Int>,
    val target: Int,
    val left: Int,
    val right: Int,
    val mid: Int? = null,
    val event: BinarySearchEvent,
    val resultIndex: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object RecursiveBinarySearchTrace {

    fun stepsForSearch(
        arr: List<Int> = listOf(2, 5, 8, 12, 16),
        target: Int = 12
    ): List<BinarySearchTraceStep> {
        val steps = mutableListOf<BinarySearchTraceStep>()

        fun snapshot(
            left: Int,
            right: Int,
            mid: Int? = null,
            event: BinarySearchEvent,
            resultIndex: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BinarySearchTraceStep(
            array = arr,
            target = target,
            left = left,
            right = right,
            mid = mid,
            event = event,
            resultIndex = resultIndex,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        var left = 0
        var right = arr.lastIndex

        // Step 0: Ready
        steps.add(
            snapshot(
                left = left,
                right = right,
                event = BinarySearchEvent.START,
                explanation = "Recursive Binary Search: Searching for target $target in sorted array [${arr.joinToString(", ")}]. Search range [0..${arr.lastIndex}].",
                badgeText = "READY · TARGET = $target"
            )
        )

        while (left <= right) {
            val mid = left + (right - left) / 2
            val midValue = arr[mid]

            // Step A: Calculate Mid
            steps.add(
                snapshot(
                    left = left,
                    right = right,
                    mid = mid,
                    event = BinarySearchEvent.CALCULATE_MID,
                    explanation = "Range [$left..$right]: Calculate mid = $left + ($right - $left) / 2 = $mid. Element arr[$mid] = $midValue.",
                    badgeText = "MID = INDEX $mid (VAL $midValue)"
                )
            )

            // Step B: Compare Target
            if (midValue == target) {
                steps.add(
                    snapshot(
                        left = left,
                        right = right,
                        mid = mid,
                        event = BinarySearchEvent.TARGET_FOUND,
                        resultIndex = mid,
                        isTerminal = true,
                        explanation = "MATCH FOUND! arr[$mid] == $target. Target $target found at index $mid in O(log N) time.",
                        badgeText = "FOUND AT INDEX $mid"
                    )
                )
                return steps
            } else if (midValue > target) {
                steps.add(
                    snapshot(
                        left = left,
                        right = right,
                        mid = mid,
                        event = BinarySearchEvent.COMPARE_TARGET,
                        explanation = "arr[$mid] = $midValue > target $target. Target must be in left half. Recurse on range [$left..${mid - 1}].",
                        badgeText = "TARGET < MID → RECURSE LEFT"
                    )
                )
                right = mid - 1
            } else {
                steps.add(
                    snapshot(
                        left = left,
                        right = right,
                        mid = mid,
                        event = BinarySearchEvent.COMPARE_TARGET,
                        explanation = "arr[$mid] = $midValue < target $target. Target must be in right half. Recurse on range [${mid + 1}..$right].",
                        badgeText = "TARGET > MID → RECURSE RIGHT"
                    )
                )
                left = mid + 1
            }
        }

        // Base case: left > right -> Target absent
        steps.add(
            snapshot(
                left = left,
                right = right,
                event = BinarySearchEvent.TARGET_NOT_FOUND,
                resultIndex = -1,
                isTerminal = true,
                explanation = "BASE CASE: left ($left) > right ($right). Search range invalid! Target $target is absent from array (returns -1).",
                badgeText = "NOT FOUND (RETURN -1)"
            )
        )

        return steps
    }
}
