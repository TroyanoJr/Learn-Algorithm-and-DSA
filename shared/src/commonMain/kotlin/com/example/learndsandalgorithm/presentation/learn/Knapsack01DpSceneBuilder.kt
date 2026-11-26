package com.example.learndsandalgorithm.presentation.learn

internal enum class KnapsackDecision { INCLUDE_WINS, KEEP_EXISTING, UPDATED, NONE }
internal data class KnapsackDpFrame(val step: Knapsack01DpStep, val source: Int?, val destination: Int?,
    val sourceValueBeforeUpdate: Int?, val bestCandidate: Int?, val storedAtDestination: Int?, val decision: KnapsackDecision)
internal data class KnapsackDpPresentation(val frames: List<KnapsackDpFrame>, val tableSize: Int, val itemCount: Int)
internal object Knapsack01DpSceneBuilder {
    fun build(steps: List<Knapsack01DpStep>): KnapsackDpPresentation {
        val frames = steps.map { step ->
            val evaluating = step.action == Knapsack01Action.EVALUATE_ITEM_CAP || step.action == Knapsack01Action.UPDATE_DP
            val item = step.currentItemIndex?.let { step.items.getOrNull(it) }
            val destination = if (evaluating) step.currentCap else null
            val source = if (destination != null && item != null) destination - item.weight else null
            val sourceValue = if (item != null && step.includeVal != null) step.includeVal - item.value else null
            val decision = when {
                step.action == Knapsack01Action.UPDATE_DP -> KnapsackDecision.UPDATED
                evaluating && step.includeVal != null && step.excludeVal != null && step.includeVal > step.excludeVal -> KnapsackDecision.INCLUDE_WINS
                evaluating -> KnapsackDecision.KEEP_EXISTING
                else -> KnapsackDecision.NONE
            }
            KnapsackDpFrame(step, source, destination, sourceValue,
                if (step.includeVal != null && step.excludeVal != null) maxOf(step.includeVal, step.excludeVal) else null,
                destination?.let { step.dpTable.getOrNull(it) }, decision)
        }
        return KnapsackDpPresentation(frames, steps.maxOfOrNull { it.dpTable.size } ?: 0, steps.maxOfOrNull { it.items.size } ?: 0)
    }
}
