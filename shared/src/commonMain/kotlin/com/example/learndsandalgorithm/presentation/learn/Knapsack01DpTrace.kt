package com.example.learndsandalgorithm.presentation.learn

internal data class Knapsack01Item(
    val name: String,
    val weight: Int,
    val value: Int
)

internal enum class Knapsack01Action {
    INIT,
    EVALUATE_ITEM_CAP,
    UPDATE_DP,
    FINISHED
}

internal data class Knapsack01DpStep(
    val capacity: Int = 5,
    val items: List<Knapsack01Item> = listOf(
        Knapsack01Item("A", 2, 3),
        Knapsack01Item("B", 3, 4)
    ),
    val currentItemIndex: Int? = null,
    val currentCap: Int? = null,
    val dpTable: List<Int> = List(6) { 0 },
    val excludeVal: Int? = null,
    val includeVal: Int? = null,
    val action: Knapsack01Action = Knapsack01Action.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object Knapsack01DpTrace {

    val defaultItems = listOf(
        Knapsack01Item("A", 2, 3),
        Knapsack01Item("B", 3, 4)
    )

    fun createTrace(
        items: List<Knapsack01Item> = defaultItems,
        capacity: Int = 5
    ): List<Knapsack01DpStep> {
        val steps = mutableListOf<Knapsack01DpStep>()
        val dp = MutableList(capacity + 1) { 0 }

        fun snapshot(
            idx: Int?,
            cap: Int?,
            ex: Int?,
            inc: Int?,
            action: Knapsack01Action,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = Knapsack01DpStep(
            capacity = capacity,
            items = items,
            currentItemIndex = idx,
            currentCap = cap,
            dpTable = dp.toList(),
            excludeVal = ex,
            includeVal = inc,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                idx = null,
                cap = null,
                ex = null,
                inc = null,
                action = Knapsack01Action.INIT,
                explanation = "0/1 Knapsack DP: Maximize value for capacity $capacity with items ${items.map { "${it.name}(w:${it.weight},v:${it.value})" }}. Uses 1D DP table with backward capacity iteration.",
                badgeText = "READY · CAPACITY $capacity 0/1 KNAPSACK"
            )
        )

        for ((idx, item) in items.withIndex()) {
            for (c in capacity downTo item.weight) {
                val ex = dp[c]
                val inc = item.value + dp[c - item.weight]
                val maxVal = maxOf(ex, inc)

                steps.add(
                    snapshot(
                        idx = idx,
                        cap = c,
                        ex = ex,
                        inc = inc,
                        action = Knapsack01Action.EVALUATE_ITEM_CAP,
                        explanation = "Item ${item.name} at Capacity $c: Compare Exclude (${ex}) vs Include (${item.value} + dp[${c - item.weight}] = $inc).",
                        badgeText = "EVAL ${item.name} @ CAP $c: EX=$ex vs INC=$inc"
                    )
                )

                if (inc > ex) {
                    dp[c] = maxVal
                    steps.add(
                        snapshot(
                            idx = idx,
                            cap = c,
                            ex = ex,
                            inc = inc,
                            action = Knapsack01Action.UPDATE_DP,
                            explanation = "UPDATE dp[$c] = $maxVal (Including item ${item.name} yields higher value).",
                            badgeText = "UPDATE dp[$c] = $maxVal"
                        )
                    )
                }
            }
        }

        val finalAns = dp[capacity]

        // FINISHED
        steps.add(
            snapshot(
                idx = null,
                cap = capacity,
                ex = null,
                inc = finalAns,
                action = Knapsack01Action.FINISHED,
                explanation = "0/1 Knapsack DP complete! Maximum value for capacity $capacity = $finalAns. Iterating capacity backwards in 1D DP optimizes space to O(W) while preventing multiple inclusion of the same item.",
                badgeText = "COMPLETE: MAX KNAPSACK VALUE = $finalAns",
                isTerminal = true
            )
        )

        return steps
    }
}
