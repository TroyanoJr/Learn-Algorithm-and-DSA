package com.example.learndsandalgorithm.presentation.learn

internal enum class FractionalItemVisualState { PENDING, EVALUATING, FULL, PARTIAL }

internal data class FractionalItemOccurrence(
    val id: String,
    val originalIndex: Int,
    val sortedIndex: Int,
    val item: KnapsackItem
)

internal data class FractionalItemFrame(
    val occurrence: FractionalItemOccurrence,
    val state: FractionalItemVisualState,
    val takenWeight: Double,
    val remainingWeight: Double,
    val valueContribution: Double,
    val fraction: Double
)

internal data class FractionalKnapsackSceneFrame(
    val step: FractionalKnapsackStep,
    val items: List<FractionalItemFrame>,
    val selectedWeight: Double,
    val remainingCapacity: Double,
    val totalValue: Double,
    val isFinalResult: Boolean
)

internal data class FractionalKnapsackScenePresentation(
    val sortedOccurrences: List<FractionalItemOccurrence>,
    val frames: List<FractionalKnapsackSceneFrame>
)

internal object DedicatedFractionalKnapsackSceneBuilder {
    fun build(steps: List<FractionalKnapsackStep>): FractionalKnapsackScenePresentation {
        require(steps.isNotEmpty())
        val first = steps.first()
        val sortedOccurrences = occurrenceMatch(first.items, first.sortedItems)
        val frames = steps.map { step ->
            val selectionsBySortedIndex = mutableMapOf<Int, ItemSelection>()
            val assignedIndices = mutableSetOf<Int>()
            step.selections.forEach { selection ->
                val match = sortedOccurrences.firstOrNull { occurrence ->
                    occurrence.sortedIndex !in assignedIndices && occurrence.item == selection.item
                }
                if (match != null) {
                    assignedIndices += match.sortedIndex
                    selectionsBySortedIndex[match.sortedIndex] = selection
                }
            }
            val itemFrames = sortedOccurrences.map { occurrence ->
                val selection = selectionsBySortedIndex[occurrence.sortedIndex]
                val state = when {
                    selection != null && selection.fraction >= 1.0 -> FractionalItemVisualState.FULL
                    selection != null -> FractionalItemVisualState.PARTIAL
                    step.currentItemIndex == occurrence.sortedIndex && step.status == FractionalKnapsackStatus.EVALUATING -> FractionalItemVisualState.EVALUATING
                    else -> FractionalItemVisualState.PENDING
                }
                FractionalItemFrame(
                    occurrence = occurrence,
                    state = state,
                    takenWeight = selection?.takenWeight ?: 0.0,
                    remainingWeight = (occurrence.item.weight - (selection?.takenWeight ?: 0.0)).coerceAtLeast(0.0),
                    valueContribution = selection?.takenValue ?: 0.0,
                    fraction = selection?.fraction ?: 0.0
                )
            }
            FractionalKnapsackSceneFrame(
                step = step,
                items = itemFrames,
                selectedWeight = step.capacity - step.remainingCapacity,
                remainingCapacity = step.remainingCapacity,
                totalValue = step.totalValue,
                isFinalResult = step.status == FractionalKnapsackStatus.FINISHED
            )
        }
        return FractionalKnapsackScenePresentation(sortedOccurrences, frames)
    }

    private fun occurrenceMatch(
        originalItems: List<KnapsackItem>,
        sortedItems: List<KnapsackItem>
    ): List<FractionalItemOccurrence> {
        val claimedOriginalIndices = mutableSetOf<Int>()
        return sortedItems.mapIndexed { sortedIndex, item ->
            val originalIndex = originalItems.indices.firstOrNull { index ->
                index !in claimedOriginalIndices && originalItems[index] == item
            } ?: sortedIndex
            claimedOriginalIndices += originalIndex
            FractionalItemOccurrence(
                id = "item-$originalIndex",
                originalIndex = originalIndex,
                sortedIndex = sortedIndex,
                item = item
            )
        }
    }
}
