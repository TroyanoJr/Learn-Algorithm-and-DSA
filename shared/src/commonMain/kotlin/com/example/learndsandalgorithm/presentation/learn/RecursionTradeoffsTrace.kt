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
                explanation = "Compare iteration and recursion for sumTo($inputN). Both compute sum = ${inputN * (inputN + 1) / 2}, but use different auxiliary memory.",
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
                    explanation = "Add $i to the reused accumulator, producing total = $iterTotal. The loop keeps O(1) auxiliary state.",
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
                        explanation = "sumTo(0) returns 0, stopping the descent. The stack reaches ${stack.size} active frames, demonstrating O(N) auxiliary space.",
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
                    explanation = "Enter sumTo($n); because $n > 0, it calls sumTo(${n - 1}). Active stack depth grows to ${stack.size}.",
                    badgeText = "RECURSIVE DESCENT: CALL sumTo($n)"
                )
            )

            val child = recSum(n - 1)
            val res = n + child
            val recipient = stack.dropLast(1).lastOrNull()?.let { "sumTo($it)" } ?: "the final result"

            steps.add(
                snapshot(
                    i = inputN,
                    retVal = res,
                    act = TradeoffAction.RECURSIVE_ASCENT,
                    explanation = "sumTo($n) combines $n + $child = $res and returns $res to $recipient. Its stack frame is then released.",
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
                explanation = "Both methods return $recAns: iteration uses O(1) auxiliary state, while recursion uses O(N) stack space. Standard JVM recursion has no automatic general-purpose tail-call optimization.",
                badgeText = "COMPLETE: ITERATION O(1) vs RECURSION O(N) STACK",
                isTerminal = true
            )
        )

        return steps
    }
}
