package com.example.learndsandalgorithm.presentation.learn

internal enum class BinarySearchEvent {
    START,
    CALL_ENTER,
    CALCULATE_MID,
    COMPARE_TARGET,
    RECURSE_LEFT,
    RECURSE_RIGHT,
    TARGET_FOUND,
    TARGET_NOT_FOUND,
    RETURN
}

internal enum class BinarySearchComparison {
    LESS,
    EQUAL,
    GREATER
}

internal data class BinarySearchCallFrame(
    val callId: Int,
    val callerCallId: Int?,
    val left: Int,
    val right: Int,
    val depth: Int,
    val mid: Int? = null,
    val midValue: Int? = null,
    val comparison: BinarySearchComparison? = null
)

internal data class BinarySearchTraceStep(
    val array: List<Int>,
    val target: Int,
    val left: Int,
    val right: Int,
    val mid: Int? = null,
    val midValue: Int? = null,
    val comparison: BinarySearchComparison? = null,
    val event: BinarySearchEvent,
    val resultIndex: Int? = null,
    val foundIndex: Int? = null,
    val returnValue: Int? = null,
    val callDepth: Int = 0,
    val stackDepth: Int = 0,
    val activeCallId: Int? = null,
    val callerCallId: Int? = null,
    val calleeCall: BinarySearchCallFrame? = null,
    val returnedCall: BinarySearchCallFrame? = null,
    val callStack: List<BinarySearchCallFrame> = emptyList(),
    val isEmptyRangeBaseCase: Boolean = false,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object RecursiveBinarySearchTrace {

    fun stepsForSearch(
        arr: List<Int> = listOf(2, 5, 8, 12, 16),
        target: Int = 12
    ): List<BinarySearchTraceStep> {
        val input = arr.toList()
        require(input.zipWithNext().all { (first, second) -> first <= second }) {
            "arr must be sorted in nondecreasing order"
        }

        val steps = mutableListOf<BinarySearchTraceStep>()
        val callStack = mutableListOf<BinarySearchCallFrame>()
        var nextCallId = 0
        var foundIndex: Int? = null

        fun snapshot(
            event: BinarySearchEvent,
            left: Int,
            right: Int,
            frame: BinarySearchCallFrame? = callStack.lastOrNull(),
            mid: Int? = frame?.mid,
            midValue: Int? = frame?.midValue,
            comparison: BinarySearchComparison? = frame?.comparison,
            stackSnapshot: List<BinarySearchCallFrame> = callStack.toList(),
            calleeCall: BinarySearchCallFrame? = null,
            returnedCall: BinarySearchCallFrame? = null,
            resultIndex: Int? = null,
            returnValue: Int? = null,
            isEmptyRangeBaseCase: Boolean = false,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) {
            steps += BinarySearchTraceStep(
                array = input,
                target = target,
                left = left,
                right = right,
                mid = mid,
                midValue = midValue,
                comparison = comparison,
                event = event,
                resultIndex = resultIndex,
                foundIndex = foundIndex,
                returnValue = returnValue,
                callDepth = returnedCall?.depth ?: frame?.depth ?: 0,
                stackDepth = stackSnapshot.size,
                activeCallId = stackSnapshot.lastOrNull()?.callId,
                callerCallId = returnedCall?.callerCallId ?: frame?.callerCallId,
                calleeCall = calleeCall,
                returnedCall = returnedCall,
                callStack = stackSnapshot,
                isEmptyRangeBaseCase = isEmptyRangeBaseCase,
                explanation = explanation,
                badgeText = badgeText,
                isTerminal = isTerminal
            )
        }

        snapshot(
            event = BinarySearchEvent.START,
            left = 0,
            right = input.lastIndex,
            frame = null,
            mid = null,
            midValue = null,
            comparison = null,
            stackSnapshot = emptyList(),
            explanation = "Recursive Binary Search: Searching for target $target in sorted array [${input.joinToString(", ")}]. Search range [0..${input.lastIndex}].",
            badgeText = "READY · TARGET = $target"
        )

        fun returnFromCall(frame: BinarySearchCallFrame, value: Int): Int {
            val popped = callStack.removeAt(callStack.lastIndex)
            check(popped.callId == frame.callId) { "Recursive search calls must return in stack order" }
            val caller = callStack.lastOrNull()
            val destination = caller?.let { "caller #${it.callId}" } ?: "initial caller"
            val resultText = if (value == -1) "-1 (not found)" else "index $value"
            val completion = if (caller == null) {
                " Search complete. Time complexity O(log N); recursive stack space O(log N)."
            } else {
                " The result propagates one call frame upward."
            }
            snapshot(
                event = BinarySearchEvent.RETURN,
                left = frame.left,
                right = frame.right,
                frame = frame,
                mid = frame.mid,
                midValue = frame.midValue,
                comparison = frame.comparison,
                stackSnapshot = callStack.toList(),
                returnedCall = frame,
                resultIndex = value,
                returnValue = value,
                isTerminal = caller == null,
                explanation = "RETURN $resultText from call #${frame.callId} to $destination.$completion",
                badgeText = if (caller == null) "FINAL RETURN: $resultText" else "RETURN $resultText → CALL #${caller.callId}"
            )
            return value
        }

        fun search(left: Int, right: Int, depth: Int): Int {
            val frame = BinarySearchCallFrame(
                callId = nextCallId++,
                callerCallId = callStack.lastOrNull()?.callId,
                left = left,
                right = right,
                depth = depth
            )
            callStack += frame
            snapshot(
                event = BinarySearchEvent.CALL_ENTER,
                left = left,
                right = right,
                frame = frame,
                explanation = "CALL depth $depth: search($left, $right, depth=$depth) enters with inclusive range [$left..$right]${if (frame.callerCallId == null) " from the initial caller" else " from caller #${frame.callerCallId}"}.",
                badgeText = "CALL #${frame.callId} · DEPTH $depth"
            )

            if (left > right) {
                snapshot(
                    event = BinarySearchEvent.TARGET_NOT_FOUND,
                    left = left,
                    right = right,
                    frame = frame,
                    isEmptyRangeBaseCase = true,
                    resultIndex = -1,
                    explanation = "BASE CASE: left ($left) > right ($right). The inclusive search range is empty, so target $target is absent from the array; this call returns -1 without indexing the array.",
                    badgeText = "EMPTY RANGE · BASE CASE"
                )
                return returnFromCall(frame, -1)
            }

            val mid = left + (right - left) / 2
            val midValue = input[mid]
            val calculatedFrame = frame.copy(mid = mid, midValue = midValue)
            callStack[callStack.lastIndex] = calculatedFrame
            snapshot(
                event = BinarySearchEvent.CALCULATE_MID,
                left = left,
                right = right,
                frame = calculatedFrame,
                mid = mid,
                midValue = midValue,
                comparison = null,
                explanation = "Range [$left..$right]: Calculate mid = $left + ($right - $left) / 2 = $mid. Element arr[$mid] = $midValue.",
                badgeText = "MID = INDEX $mid (VAL $midValue)"
            )

            val relation = when {
                midValue < target -> BinarySearchComparison.LESS
                midValue > target -> BinarySearchComparison.GREATER
                else -> BinarySearchComparison.EQUAL
            }
            val comparedFrame = calculatedFrame.copy(comparison = relation)
            callStack[callStack.lastIndex] = comparedFrame
            val comparisonExplanation = when (relation) {
                BinarySearchComparison.EQUAL -> "Compare target $target with arr[$mid] = $midValue: the values are equal."
                BinarySearchComparison.GREATER -> if (right > left) {
                    "arr[$mid] = $midValue > target $target. Target must be in the left half. Recurse on range [$left..${mid - 1}]."
                } else {
                    "arr[$mid] = $midValue > target $target. The only candidate is too large; recurse on the empty range [$left..${mid - 1}]."
                }
                BinarySearchComparison.LESS -> if (right > left) {
                    "arr[$mid] = $midValue < target $target. Target must be in the right half. Recurse on range [${mid + 1}..$right]."
                } else {
                    "arr[$mid] = $midValue < target $target. The only candidate is too small; recurse on the empty range [${mid + 1}..$right]."
                }
            }
            snapshot(
                event = BinarySearchEvent.COMPARE_TARGET,
                left = left,
                right = right,
                frame = comparedFrame,
                mid = mid,
                midValue = midValue,
                comparison = relation,
                explanation = comparisonExplanation,
                badgeText = "COMPARE arr[$mid] WITH $target"
            )

            if (relation == BinarySearchComparison.EQUAL) {
                foundIndex = mid
                snapshot(
                    event = BinarySearchEvent.TARGET_FOUND,
                    left = left,
                    right = right,
                    frame = comparedFrame,
                    mid = mid,
                    midValue = midValue,
                    comparison = relation,
                    resultIndex = mid,
                    explanation = "MATCH FOUND! arr[$mid] == $target. Target $target found at index $mid in O(log N) time.",
                    badgeText = "FOUND AT INDEX $mid"
                )
                return returnFromCall(comparedFrame, mid)
            }

            val childLeft = if (relation == BinarySearchComparison.GREATER) left else mid + 1
            val childRight = if (relation == BinarySearchComparison.GREATER) mid - 1 else right
            val child = BinarySearchCallFrame(
                callId = nextCallId,
                callerCallId = comparedFrame.callId,
                left = childLeft,
                right = childRight,
                depth = depth + 1
            )
            val recurseLeft = relation == BinarySearchComparison.GREATER
            snapshot(
                event = if (recurseLeft) BinarySearchEvent.RECURSE_LEFT else BinarySearchEvent.RECURSE_RIGHT,
                left = left,
                right = right,
                frame = comparedFrame,
                mid = mid,
                midValue = midValue,
                comparison = relation,
                calleeCall = child,
                explanation = "RECURSE ${if (recurseLeft) "LEFT" else "RIGHT"}: call search(${child.left}, ${child.right}, depth=${child.depth}) from caller #${comparedFrame.callId}.",
                badgeText = "RECURSE ${if (recurseLeft) "LEFT" else "RIGHT"} → CALL #${child.callId}"
            )
            val result = search(child.left, child.right, child.depth)
            return returnFromCall(comparedFrame, result)
        }

        search(0, input.lastIndex, 0)
        return steps
    }
}
