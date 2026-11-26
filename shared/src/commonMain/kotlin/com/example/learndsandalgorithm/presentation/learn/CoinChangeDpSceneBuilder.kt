package com.example.learndsandalgorithm.presentation.learn

internal data class CoinChangeFrame(
    val step: CoinChangeDpStep,
    val source: Int?,
    val destination: Int?,
    val existingBefore: Int?,
    val candidate: Int?,
    val improves: Boolean?
)

internal data class CoinChangePresentation(val frames: List<CoinChangeFrame>, val tableSize: Int)

internal object CoinChangeDpSceneBuilder {
    fun build(steps: List<CoinChangeDpStep>): CoinChangePresentation {
        val frames = steps.mapIndexed { index, step ->
            val active = step.action == CoinChangeDpAction.CHECK_COIN || step.action == CoinChangeDpAction.UPDATE_DP
            val destination = if (active) step.currentAmount else null
            val source = if (destination != null && step.currentCoin != null) destination - step.currentCoin else null
            val previous = if (step.action == CoinChangeDpAction.UPDATE_DP) steps.getOrNull(index - 1) else step
            val existing = destination?.let { previous?.dpTable?.getOrNull(it) }
            val candidate = if (active) step.candidateVal else null
            CoinChangeFrame(step, source, destination, existing, candidate,
                if (existing != null && candidate != null) candidate != CoinChangeDpTrace.INF && candidate < existing else null)
        }
        return CoinChangePresentation(frames, steps.maxOfOrNull { it.dpTable.size } ?: 0)
    }
}
