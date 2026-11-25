package com.example.learndsandalgorithm.presentation.learn

internal data class CounterexampleCoinOccurrence(
    val id: String,
    val index: Int,
    val value: Int
)

internal data class GreedyCounterexamplesSceneFrame(
    val step: GreedyCounterexamplesStep,
    val greedyCoins: List<CounterexampleCoinOccurrence>,
    val optimalCoins: List<CounterexampleCoinOccurrence>,
    val optimalRevealed: Boolean,
    val failureDemonstrated: Boolean,
    val finalVerdict: Boolean
)

internal data class GreedyCounterexamplesScenePresentation(
    val frames: List<GreedyCounterexamplesSceneFrame>,
    val maximumGreedyCoinCount: Int,
    val maximumOptimalCoinCount: Int
)

internal object DedicatedGreedyCounterexamplesSceneBuilder {
    fun build(steps: List<GreedyCounterexamplesStep>): GreedyCounterexamplesScenePresentation {
        require(steps.isNotEmpty())
        val frames = steps.map { step ->
            val revealed = step.status == CounterexampleStatus.COMPARE_OPTIMAL || step.status == CounterexampleStatus.FINISHED
            val optimalCoins = if (revealed) step.optimalCoins.mapIndexed { index, value ->
                CounterexampleCoinOccurrence("optimal-coin-$index", index, value)
            } else emptyList()
            val greedyCoins = step.greedyCoins.mapIndexed { index, value ->
                CounterexampleCoinOccurrence("greedy-coin-$index", index, value)
            }
            val comparisonConfirmsFailure = revealed &&
                greedyCoins.sumOf { it.value } == step.targetAmount &&
                optimalCoins.sumOf { it.value } == step.targetAmount &&
                greedyCoins.size > optimalCoins.size
            GreedyCounterexamplesSceneFrame(
                step = step,
                greedyCoins = greedyCoins,
                optimalCoins = optimalCoins,
                optimalRevealed = revealed,
                failureDemonstrated = comparisonConfirmsFailure,
                finalVerdict = step.status == CounterexampleStatus.FINISHED && comparisonConfirmsFailure
            )
        }
        return GreedyCounterexamplesScenePresentation(
            frames = frames,
            maximumGreedyCoinCount = frames.maxOf { it.greedyCoins.size },
            maximumOptimalCoinCount = steps.maxOf { it.optimalCoins.size }
        )
    }
}
