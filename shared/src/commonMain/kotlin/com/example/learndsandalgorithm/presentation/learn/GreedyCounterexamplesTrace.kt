package com.example.learndsandalgorithm.presentation.learn

internal enum class CounterexampleStatus {
    INIT,
    GREEDY_STEP_1,
    GREEDY_STEP_2,
    GREEDY_STEP_3,
    COMPARE_OPTIMAL,
    FINISHED
}

internal data class GreedyCounterexamplesStep(
    val targetAmount: Int = 6,
    val denominations: List<Int> = listOf(4, 3, 1),
    val greedyRemaining: Int = 6,
    val greedyCoins: List<Int> = emptyList(),
    val optimalCoins: List<Int> = listOf(3, 3),
    val status: CounterexampleStatus = CounterexampleStatus.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GreedyCounterexamplesTrace {

    fun createTrace(
        target: Int = 6,
        denominations: List<Int> = listOf(4, 3, 1)
    ): List<GreedyCounterexamplesStep> {
        val steps = mutableListOf<GreedyCounterexamplesStep>()

        // Step 0: INIT
        steps.add(
            GreedyCounterexamplesStep(
                targetAmount = target,
                denominations = denominations,
                greedyRemaining = 6,
                greedyCoins = emptyList(),
                status = CounterexampleStatus.INIT,
                explanation = "Greedy Counterexample: Make amount $target with coins $denominations. Compare largest-first greedy choice vs optimal DP solution.",
                badgeText = "READY · TARGET $target WITH $denominations"
            )
        )

        // Step 1: GREEDY_STEP_1
        steps.add(
            GreedyCounterexamplesStep(
                targetAmount = target,
                denominations = denominations,
                greedyRemaining = 2,
                greedyCoins = listOf(4),
                status = CounterexampleStatus.GREEDY_STEP_1,
                explanation = "Greedy Choice 1: Pick largest coin 4 ≤ 6. Remaining amount: 6 - 4 = 2. Greedy coin list: [4].",
                badgeText = "GREEDY STEP 1: PICK 4 (REM 2)"
            )
        )

        // Step 2: GREEDY_STEP_2
        steps.add(
            GreedyCounterexamplesStep(
                targetAmount = target,
                denominations = denominations,
                greedyRemaining = 1,
                greedyCoins = listOf(4, 1),
                status = CounterexampleStatus.GREEDY_STEP_2,
                explanation = "Greedy Choice 2: Coin 4 > 2 (skip), Coin 3 > 2 (skip). Pick coin 1 ≤ 2. Remaining amount: 2 - 1 = 1. Greedy coin list: [4, 1].",
                badgeText = "GREEDY STEP 2: PICK 1 (REM 1)"
            )
        )

        // Step 3: GREEDY_STEP_3
        steps.add(
            GreedyCounterexamplesStep(
                targetAmount = target,
                denominations = denominations,
                greedyRemaining = 0,
                greedyCoins = listOf(4, 1, 1),
                status = CounterexampleStatus.GREEDY_STEP_3,
                explanation = "Greedy Choice 3: Pick coin 1 ≤ 1. Remaining amount: 1 - 1 = 0. Greedy finished with 3 coins: [4, 1, 1].",
                badgeText = "GREEDY RESULT: 3 COINS [4, 1, 1]"
            )
        )

        // Step 4: COMPARE_OPTIMAL
        steps.add(
            GreedyCounterexamplesStep(
                targetAmount = target,
                denominations = denominations,
                greedyRemaining = 0,
                greedyCoins = listOf(4, 1, 1),
                optimalCoins = listOf(3, 3),
                status = CounterexampleStatus.COMPARE_OPTIMAL,
                explanation = "Optimal Solution: Pick coin 3, then coin 3. Total amount: 3 + 3 = 6 using only 2 coins: [3, 3]! 2 coins < 3 coins.",
                badgeText = "OPTIMAL RESULT: 2 COINS [3, 3]"
            )
        )

        // Step 5: FINISHED
        steps.add(
            GreedyCounterexamplesStep(
                targetAmount = target,
                denominations = denominations,
                greedyRemaining = 0,
                greedyCoins = listOf(4, 1, 1),
                optimalCoins = listOf(3, 3),
                status = CounterexampleStatus.FINISHED,
                explanation = "COUNTEREXAMPLE PROVEN: Greedy choice (4) was locally optimal at step 1, but prevented reaching the global optimum (3+3=6, 2 coins vs 3 coins). Greedy choice property fails here!",
                badgeText = "COUNTEREXAMPLE PROVEN: GREEDY IS SUBOPTIMAL",
                isTerminal = true
            )
        )

        return steps
    }
}
