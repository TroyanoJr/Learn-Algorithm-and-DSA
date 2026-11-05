package com.example.learndsandalgorithm.presentation.learn

internal data class KnapsackItem(
    val name: String,
    val weight: Double,
    val value: Double
) {
    val ratio: Double get() = if (weight > 0) value / weight else 0.0
}

internal data class ItemSelection(
    val item: KnapsackItem,
    val takenWeight: Double,
    val takenValue: Double,
    val fraction: Double
)

internal enum class FractionalKnapsackStatus {
    INIT,
    SORTED,
    EVALUATING,
    TAKE_FULL,
    TAKE_FRACTION,
    FINISHED
}

internal data class FractionalKnapsackStep(
    val capacity: Double = 50.0,
    val remainingCapacity: Double = 50.0,
    val totalValue: Double = 0.0,
    val items: List<KnapsackItem> = emptyList(),
    val sortedItems: List<KnapsackItem> = emptyList(),
    val currentItemIndex: Int? = null,
    val currentItem: KnapsackItem? = null,
    val selections: List<ItemSelection> = emptyList(),
    val status: FractionalKnapsackStatus = FractionalKnapsackStatus.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object FractionalKnapsackTrace {

    val defaultItems = listOf(
        KnapsackItem("A", 10.0, 60.0),
        KnapsackItem("B", 20.0, 100.0),
        KnapsackItem("C", 30.0, 120.0)
    )

    fun createTrace(
        items: List<KnapsackItem> = defaultItems,
        capacity: Double = 50.0
    ): List<FractionalKnapsackStep> {
        val steps = mutableListOf<FractionalKnapsackStep>()
        val sorted = items.sortedByDescending { it.ratio }
        val selections = mutableListOf<ItemSelection>()
        var remainingCap = capacity
        var totalVal = 0.0

        // Step 0: INIT
        steps.add(
            FractionalKnapsackStep(
                capacity = capacity,
                remainingCapacity = remainingCap,
                totalValue = totalVal,
                items = items,
                sortedItems = sorted,
                status = FractionalKnapsackStatus.INIT,
                explanation = "Fractional Knapsack: Capacity = $capacity. Items: ${items.map { "${it.name}(w:${it.weight.toInt()}, v:${it.value.toInt()})" }}. Goal: maximize total value.",
                badgeText = "READY · CAPACITY ${capacity.toInt()}"
            )
        )

        // Step 1: SORTED
        steps.add(
            FractionalKnapsackStep(
                capacity = capacity,
                remainingCapacity = remainingCap,
                totalValue = totalVal,
                items = items,
                sortedItems = sorted,
                status = FractionalKnapsackStatus.SORTED,
                explanation = "Calculate value/weight ratio for each item and sort descending: ${sorted.map { "${it.name}: ${it.ratio}" }}. Highest ratio gives most value per weight unit.",
                badgeText = "SORTED BY RATIO (DESCENDING)"
            )
        )

        for ((idx, item) in sorted.withIndex()) {
            if (remainingCap <= 0) break

            // EVALUATING
            steps.add(
                FractionalKnapsackStep(
                    capacity = capacity,
                    remainingCapacity = remainingCap,
                    totalValue = totalVal,
                    items = items,
                    sortedItems = sorted,
                    currentItemIndex = idx,
                    currentItem = item,
                    selections = selections.toList(),
                    status = FractionalKnapsackStatus.EVALUATING,
                    explanation = "Evaluate item ${item.name} (w:${item.weight.toInt()}, v:${item.value.toInt()}, ratio:${item.ratio}). Remaining capacity = ${remainingCap.toInt()}.",
                    badgeText = "EVALUATE ${item.name} (RATIO ${item.ratio})"
                )
            )

            if (item.weight <= remainingCap) {
                val takenW = item.weight
                val takenV = item.value
                val frac = 1.0
                selections.add(ItemSelection(item, takenW, takenV, frac))
                remainingCap -= takenW
                totalVal += takenV

                // TAKE_FULL
                steps.add(
                    FractionalKnapsackStep(
                        capacity = capacity,
                        remainingCapacity = remainingCap,
                        totalValue = totalVal,
                        items = items,
                        sortedItems = sorted,
                        currentItemIndex = idx,
                        currentItem = item,
                        selections = selections.toList(),
                        status = FractionalKnapsackStatus.TAKE_FULL,
                        explanation = "Take 100% of ${item.name}. Added weight ${takenW.toInt()}, value ${takenV.toInt()}. Remaining capacity = ${remainingCap.toInt()}, Total value = ${totalVal.toInt()}.",
                        badgeText = "TAKE FULL ${item.name} (+${takenV.toInt()} VALUE)"
                    )
                )
            } else {
                val fraction = remainingCap / item.weight
                val takenW = remainingCap
                val takenV = item.value * fraction
                selections.add(ItemSelection(item, takenW, takenV, fraction))
                totalVal += takenV
                remainingCap = 0.0

                // TAKE_FRACTION
                val pctStr = "${(fraction * 100).toInt()}%"
                steps.add(
                    FractionalKnapsackStep(
                        capacity = capacity,
                        remainingCapacity = remainingCap,
                        totalValue = totalVal,
                        items = items,
                        sortedItems = sorted,
                        currentItemIndex = idx,
                        currentItem = item,
                        selections = selections.toList(),
                        status = FractionalKnapsackStatus.TAKE_FRACTION,
                        explanation = "Take $pctStr (${takenW.toInt()} / ${item.weight.toInt()}) fraction of ${item.name}. Added weight ${takenW.toInt()}, value ${takenV.toInt()}. Capacity filled (0 remaining).",
                        badgeText = "TAKE $pctStr FRACTION OF ${item.name} (+${takenV.toInt()} VALUE)"
                    )
                )
            }
        }

        // FINISHED
        steps.add(
            FractionalKnapsackStep(
                capacity = capacity,
                remainingCapacity = remainingCap,
                totalValue = totalVal,
                items = items,
                sortedItems = sorted,
                selections = selections.toList(),
                status = FractionalKnapsackStatus.FINISHED,
                explanation = "Fractional knapsack complete! Total Value = ${totalVal.toInt()}. Value-per-weight ratio strategy is globally optimal when item fractioning is allowed. Complexity: O(N log N).",
                badgeText = "COMPLETE: TOTAL VALUE ${totalVal.toInt()}",
                isTerminal = true
            )
        )

        return steps
    }
}
