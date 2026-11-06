package com.example.learndsandalgorithm.presentation.learn

internal enum class RecursionAction {
    INIT,
    CALL_DESCENT,
    BASE_CASE_REACHED,
    RETURN_ASCENT,
    FINISHED
}

internal data class RecursionFundamentalsStep(
    val inputN: Int = 3,
    val currentN: Int? = null,
    val callStack: List<Int> = emptyList(),
    val returnValues: List<Pair<Int, Int>> = emptyList(),
    val currentReturnValue: Int? = null,
    val action: RecursionAction = RecursionAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object RecursionFundamentalsTrace {

    fun createTrace(inputN: Int = 3): List<RecursionFundamentalsStep> {
        val steps = mutableListOf<RecursionFundamentalsStep>()
        val stack = mutableListOf<Int>()
        val returns = mutableListOf<Pair<Int, Int>>()

        fun snapshot(
            n: Int?,
            retVal: Int?,
            act: RecursionAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = RecursionFundamentalsStep(
            inputN = inputN,
            currentN = n,
            callStack = stack.toList(),
            returnValues = returns.toList(),
            currentReturnValue = retVal,
            action = act,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                n = null,
                retVal = null,
                act = RecursionAction.INIT,
                explanation = "Recursion Fundamentals: Computing sumTo($inputN) = 1 + 2 + ... + $inputN. Base case n == 0 returns 0.",
                badgeText = "READY · sumTo($inputN)"
            )
        )

        fun sumTo(n: Int): Int {
            stack.add(n)

            if (n == 0) {
                steps.add(
                    snapshot(
                        n = 0,
                        retVal = 0,
                        act = RecursionAction.BASE_CASE_REACHED,
                        explanation = "Call sumTo(0): BASE CASE REACHED! Returns 0 immediately without making further recursive calls.",
                        badgeText = "BASE CASE: sumTo(0) = 0"
                    )
                )
                returns.add(Pair(0, 0))
                stack.removeAt(stack.lastIndex)
                return 0
            }

            steps.add(
                snapshot(
                    n = n,
                    retVal = null,
                    act = RecursionAction.CALL_DESCENT,
                    explanation = "Call sumTo($n): Recurse to sumTo(${n - 1}). Pushed frame sumTo($n) onto call stack (depth ${stack.size}).",
                    badgeText = "DESCENT: CALL sumTo($n)"
                )
            )

            val childAns = sumTo(n - 1)
            val result = n + childAns

            returns.add(Pair(n, result))

            steps.add(
                snapshot(
                    n = n,
                    retVal = result,
                    act = RecursionAction.RETURN_ASCENT,
                    explanation = "Return sumTo($n) = $n + sumTo(${n - 1}) = $n + $childAns = $result. Popped frame sumTo($n) from call stack.",
                    badgeText = "ASCENT: RETURN sumTo($n) = $result"
                )
            )

            stack.removeAt(stack.lastIndex)
            return result
        }

        val finalAns = sumTo(inputN)

        // FINISHED
        steps.add(
            snapshot(
                n = inputN,
                retVal = finalAns,
                act = RecursionAction.FINISHED,
                explanation = "Recursion complete! Final result sumTo($inputN) = $finalAns. All call stack frames fully unwound.",
                badgeText = "COMPLETE: sumTo($inputN) = $finalAns",
                isTerminal = true
            )
        )

        return steps
    }
}
