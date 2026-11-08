package com.example.learndsandalgorithm.presentation.learn

internal enum class MemoAction {
    INIT,
    CALL_SUBPROBLEM,
    CACHE_HIT,
    COMPUTE_AND_STORE,
    FINISHED
}

internal data class DpMemoizationStep(
    val targetN: Int = 5,
    val currentN: Int? = null,
    val memoArray: List<Long?> = List(6) { null },
    val callStack: List<Int> = emptyList(),
    val cacheHit: Boolean = false,
    val action: MemoAction = MemoAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object DpMemoizationTrace {

    fun createTrace(targetN: Int = 5): List<DpMemoizationStep> {
        val steps = mutableListOf<DpMemoizationStep>()
        val memo = MutableList<Long?>(targetN + 1) { null }
        val stack = mutableListOf<Int>()

        fun snapshot(
            n: Int?,
            hit: Boolean,
            action: MemoAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = DpMemoizationStep(
            targetN = targetN,
            currentN = n,
            memoArray = memo.toList(),
            callStack = stack.toList(),
            cacheHit = hit,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                n = null,
                hit = false,
                action = MemoAction.INIT,
                explanation = "Top-Down DP (Memoization): Computing Fib($targetN). Memoization table initialized with uncomputed states (null).",
                badgeText = "READY · FIB($targetN) MEMOIZATION"
            )
        )

        fun fib(n: Int): Long {
            stack.add(n)

            if (n <= 1) {
                steps.add(
                    snapshot(
                        n = n,
                        hit = false,
                        action = MemoAction.CALL_SUBPROBLEM,
                        explanation = "Call Fib($n): Base case reached! Returning $n.",
                        badgeText = "BASE CASE Fib($n) = $n"
                    )
                )
                stack.removeAt(stack.lastIndex)
                return n.toLong()
            }

            // Check cache
            if (memo[n] != null) {
                val cached = memo[n]!!
                steps.add(
                    snapshot(
                        n = n,
                        hit = true,
                        action = MemoAction.CACHE_HIT,
                        explanation = "Call Fib($n): CACHE HIT! Found Fib($n) = $cached in memoization table. Returning $cached in O(1) time without recomputing!",
                        badgeText = "CACHE HIT! Fib($n) = $cached"
                    )
                )
                stack.removeAt(stack.lastIndex)
                return cached
            }

            steps.add(
                snapshot(
                    n = n,
                    hit = false,
                    action = MemoAction.CALL_SUBPROBLEM,
                    explanation = "Call Fib($n): Cache miss for index $n. Recurse to compute Fib(${n - 1}) + Fib(${n - 2}).",
                    badgeText = "CALL Fib($n) (CACHE MISS)"
                )
            )

            val left = fib(n - 1)
            val right = fib(n - 2)
            val result = left + right

            memo[n] = result

            steps.add(
                snapshot(
                    n = n,
                    hit = false,
                    action = MemoAction.COMPUTE_AND_STORE,
                    explanation = "Computed Fib($n) = Fib(${n - 1}) + Fib(${n - 2}) = $left + $right = $result. Stored memo[$n] = $result.",
                    badgeText = "STORE memo[$n] = $result"
                )
            )

            stack.removeAt(stack.lastIndex)
            return result
        }

        val finalAns = fib(targetN)

        // FINISHED
        steps.add(
            snapshot(
                n = targetN,
                hit = false,
                action = MemoAction.FINISHED,
                explanation = "Memoization complete! Fib($targetN) = $finalAns. Repeated subproblems (like Fib(2), Fib(3)) were reused from cache, reducing runtime from O(2^N) to O(N).",
                badgeText = "COMPLETE: Fib($targetN) = $finalAns",
                isTerminal = true
            )
        )

        return steps
    }
}
