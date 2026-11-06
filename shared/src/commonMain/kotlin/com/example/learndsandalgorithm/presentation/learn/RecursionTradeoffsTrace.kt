package com.example.learndsandalgorithm.presentation.learn

internal enum class TradeoffAction {
    INIT,
    ITERATIVE_LOOP,
    RECURSIVE_DESCENT,
    RECURSIVE_BASE,
    RECURSIVE_ASCENT,
    FINISHED
}

internal data class RecursionTradeoffsStep(
    val inputN: Int = 4,
    val iterTotal: Int = 0,
    val iterI: Int = 1,
    val recurCallStack: List<Int> = emptyList(),
    val recurReturnVal: Int? = null,
    val action: TradeoffAction = TradeoffAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object RecursionTradeoffsTrace {

    fun createTrace(inputN: Int = 4): List<RecursionTradeoffsStep> {
        val steps = mutableListOf<RecursionTradeoffsStep>()
        var iterTotal = 0
        val stack = mutableListOf<Int>()

        fun snapshot(
            i: Int,
            retVal: Int?,
            act: TradeoffAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = RecursionTradeoffsStep(
            inputN = inputN,
            iterTotal = iterTotal,
            iterI = i,
            recurCallStack = stack.toList(),
            recurReturnVal = retVal,
            action = act,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                i = 1,
                retVal = null,
                act = TradeoffAction.INIT,
                explanation = "Recursion Patterns & Trade-offs: Comparing Iteration vs Recursion for sumTo($inputN). Both compute sum = 10, but have different space costs.",
                badgeText = "READY · ITERATION VS RECURSION ($inputN)"
            )
        )

        // Phase 1: Iterative summation
        for (i in 1..inputN) {
            iterTotal += i
            steps.add(
                snapshot(
                    i = i,
                    retVal = null,
                    act = TradeoffAction.ITERATIVE_LOOP,
                    explanation = "Iterative Loop: i = $i, total = $iterTotal. Loop uses O(1) constant auxiliary space with no call stack frames.",
                    badgeText = "ITERATION: i=$i -> TOTAL $iterTotal (SPACE O(1))"
                )
            )
        }

        // Phase 2: Recursive descent
        fun recSum(n: Int): Int {
            stack.add(n)

            if (n == 0) {
                steps.add(
                    snapshot(
                        i = inputN,
                        retVal = 0,
                        act = TradeoffAction.RECURSIVE_BASE,
                        explanation = "Recursive Base Case: sumTo(0) = 0. Maximum call stack depth = ${stack.size} frames (O(N) space).",
                        badgeText = "RECURSION BASE: sumTo(0) = 0 (STACK DEPTH ${stack.size})"
                    )
                )
                stack.removeAt(stack.lastIndex)
                return 0
            }

            steps.add(
                snapshot(
                    i = inputN,
                    retVal = null,
                    act = TradeoffAction.RECURSIVE_DESCENT,
                    explanation = "Recursive Descent: Call sumTo($n). Call stack height grows to ${stack.size} frames.",
                    badgeText = "RECURSIVE DESCENT: CALL sumTo($n)"
                )
            )

            val child = recSum(n - 1)
            val res = n + child

            steps.add(
                snapshot(
                    i = inputN,
                    retVal = res,
                    act = TradeoffAction.RECURSIVE_ASCENT,
                    explanation = "Recursive Ascent: sumTo($n) = $n + $child = $res. Unwinding call frame.",
                    badgeText = "RECURSIVE ASCENT: sumTo($n) = $res"
                )
            )

            stack.removeAt(stack.lastIndex)
            return res
        }

        val recAns = recSum(inputN)

        // FINISHED
        steps.add(
            snapshot(
                i = inputN,
                retVal = recAns,
                act = TradeoffAction.FINISHED,
                explanation = "Trade-offs Comparison Complete! Both methods produce sum = $recAns. Iteration uses O(1) space, while direct recursion uses O(N) call-stack space. Note: The JVM does not perform automatic tail-call optimization for standard recursion.",
                badgeText = "COMPLETE: ITERATION O(1) vs RECURSION O(N) STACK",
                isTerminal = true
            )
        )

        return steps
    }
}
