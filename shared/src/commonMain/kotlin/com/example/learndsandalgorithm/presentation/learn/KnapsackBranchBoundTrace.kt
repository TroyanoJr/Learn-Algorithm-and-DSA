package com.example.learndsandalgorithm.presentation.learn

/** Positive-weight, non-negative-value 0/1 item. Duplicate display names are supported. */
internal data class BbKnapsackItem(val name: String, val weight: Int, val value: Int, val stableId: String = "") {
    val ratio: Double get() = value.toDouble() / weight.toDouble()
}

internal enum class KnapsackNodeState { PENDING, ACTIVE, FEASIBLE, INCUMBENT, PRUNED, INFEASIBLE, FINISHED }

internal data class KnapsackFractionalContribution(
    val itemName: String, val itemIndex: Int, val weight: Double, val value: Double, val fraction: Double
)

internal data class KnapsackBbNode(
    val id: String,
    val level: Int,
    val currentWeight: Int,
    val currentValue: Int,
    val upperBound: Double?,
    val upperBoundFloor: Long? = null,
    val itemsTaken: List<String>,
    val itemIndices: List<Int> = emptyList(),
    val state: KnapsackNodeState = KnapsackNodeState.PENDING,
    val decision: String? = null,
    /** Incumbent at the moment a prune was made; never recomputed later. */
    val comparisonThreshold: Int? = null,
    val fractionalContributions: List<KnapsackFractionalContribution> = emptyList()
)

internal enum class KnapsackBbAction {
    INIT, COMPUTE_UPPER_BOUND, BRANCH_INCLUDE, BRANCH_EXCLUDE, REJECT_INFEASIBLE,
    UPDATE_INCUMBENT, PRUNE_NODE, FINISHED
}

internal data class KnapsackBranchBoundStep(
    val capacity: Int = 10,
    val items: List<BbKnapsackItem> = emptyList(),
    val incumbentValue: Int = 0,
    val incumbentItems: List<String> = emptyList(),
    val incumbentItemIndices: List<Int> = emptyList(),
    val currentNode: KnapsackBbNode? = null,
    /** DFS has no priority queue: this remains empty rather than faking one. */
    val activeQueue: List<KnapsackBbNode> = emptyList(),
    val action: KnapsackBbAction = KnapsackBbAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object KnapsackBranchBoundTrace {
    val defaultItems = listOf(BbKnapsackItem("Item 1", 4, 40), BbKnapsackItem("Item 2", 7, 70), BbKnapsackItem("Item 3", 5, 45))

    private fun validate(items: List<BbKnapsackItem>, capacity: Int) {
        require(capacity >= 0) { "Capacity must be nonnegative." }
        require(items.all { it.weight > 0 }) { "Item weights must be positive." }
        require(items.all { it.value >= 0 }) { "Item values must be nonnegative." }
        require(items.all { it.weight <= 1_000_000_000 && it.value <= 1_000_000_000 }) { "Weights and values must not exceed 1,000,000,000." }
    }

    /** Uses the suffix of an already descending-ratio-sorted list. */
    fun computeUpperBound(capacity: Int, currentWeight: Int, currentValue: Int, items: List<BbKnapsackItem>, startIndex: Int): Double {
        require(startIndex in 0..items.size)
        if (currentWeight > capacity) return Double.NEGATIVE_INFINITY
        var bound = currentValue.toDouble()
        var remaining = (capacity - currentWeight).toDouble()
        for (index in startIndex until items.size) {
            val item = items[index]
            if (item.weight <= remaining) { remaining -= item.weight; bound += item.value }
            else { bound += item.value * (remaining / item.weight); break }
        }
        return bound
    }

    /**
     * Exact floor of the fractional relaxation. Since every feasible 0/1 completion has an
     * integer value, floor(upperBound) <= incumbent is a conservative, safe prune predicate.
     */
    private fun fractionalUpperBoundFloor(capacity: Int, currentWeight: Int, currentValue: Int, items: List<BbKnapsackItem>, startIndex: Int): Long {
        if (currentWeight > capacity) return Long.MIN_VALUE
        var bound = currentValue.toLong()
        var remaining = (capacity - currentWeight).toLong()
        for (index in startIndex until items.size) {
            val item = items[index]
            if (item.weight.toLong() <= remaining) {
                bound = checkedLongAdd(bound, item.value.toLong())
                remaining -= item.weight.toLong()
            } else {
                bound = checkedLongAdd(bound, remaining * item.value.toLong() / item.weight.toLong())
                break
            }
        }
        return bound
    }

    fun createTrace(items: List<BbKnapsackItem> = defaultItems, capacity: Int = 10): List<KnapsackBranchBoundStep> {
        validate(items, capacity)
        // Stable tie break makes equal-ratio examples deterministic and keeps input order.
        val ordered = items.withIndex().sortedWith { a, b ->
            val comparison = b.value.value.toLong() * a.value.weight.toLong() - a.value.value.toLong() * b.value.weight.toLong()
            if (comparison == 0L) a.index.compareTo(b.index) else if (comparison > 0L) 1 else -1
        }.map { indexed -> indexed.value.copy(stableId = indexed.value.stableId.ifEmpty { "item-${indexed.index}" }) }
        val steps = mutableListOf<KnapsackBranchBoundStep>()
        var incumbent = 0
        var incumbentItems = emptyList<String>()
        var incumbentIndices = emptyList<Int>()
        fun names(indices: List<Int>) = indices.map { ordered[it].name }
        fun contributions(weight: Int, next: Int): List<KnapsackFractionalContribution> {
            var remaining = (capacity - weight).toDouble()
            if (remaining < 0) return emptyList()
            return buildList {
                for (i in next until ordered.size) {
                    if (remaining <= 0.0) break
                    val item = ordered[i]; val fraction = minOf(1.0, remaining / item.weight)
                    add(KnapsackFractionalContribution(item.name, i, item.weight * fraction, item.value * fraction, fraction))
                    remaining -= item.weight * fraction
                }
            }
        }
        fun node(path: String, level: Int, weight: Int, value: Int, chosen: List<Int>, state: KnapsackNodeState, decision: String? = null, threshold: Int? = null): KnapsackBbNode {
            val suffix = level.coerceIn(0, ordered.size)
            val bound = if (weight <= capacity) computeUpperBound(capacity, weight, value, ordered, suffix) else null
            val floor = if (weight <= capacity) fractionalUpperBoundFloor(capacity, weight, value, ordered, suffix) else null
            return KnapsackBbNode(path, level, weight, value, bound, floor, names(chosen), chosen, state, decision, threshold, contributions(weight, suffix))
        }
        fun snapshot(current: KnapsackBbNode?, action: KnapsackBbAction, explanation: String, badge: String, terminal: Boolean = false) {
            steps += KnapsackBranchBoundStep(capacity, ordered, incumbent, incumbentItems, incumbentIndices, current, emptyList(), action, explanation, badge, terminal)
        }

        snapshot(null, KnapsackBbAction.INIT, "0/1 Knapsack Branch & Bound: capacity $capacity. Items are stably sorted by descending value-to-weight ratio before search.", "READY · CAPACITY $capacity")
        val root = node("Root", 0, 0, 0, emptyList(), KnapsackNodeState.ACTIVE)
        snapshot(root, KnapsackBbAction.COMPUTE_UPPER_BOUND, "Root fractional upper bound = ${root.upperBound}. It is optimistic, not an integer selection.", "ROOT UPPER BOUND = ${root.upperBound}")

        fun visit(level: Int, weight: Int, value: Int, chosen: List<Int>, path: String) {
            if (level == ordered.size) {
                if (value > incumbent) {
                    incumbent = value; incumbentItems = names(chosen); incumbentIndices = chosen
                    snapshot(node(path, level, weight, value, chosen, KnapsackNodeState.INCUMBENT, "Feasible integer solution"), KnapsackBbAction.UPDATE_INCUMBENT, "Feasible integer solution value $value improves the incumbent.", "NEW INCUMBENT = $value")
                }
                return
            }
            val item = ordered[level]
            val includeWeight = safeAdd(weight, item.weight, "weight")
            val includeValue = safeAdd(value, item.value, "value")
            val includePath = "$path → include ${item.name}"
            if (includeWeight > capacity) {
                val rejected = KnapsackBbNode(includePath, level + 1, includeWeight, includeValue, null, null, names(chosen + level), chosen + level, KnapsackNodeState.INFEASIBLE, "Weight $includeWeight > capacity $capacity")
                snapshot(rejected, KnapsackBbAction.REJECT_INFEASIBLE, "Reject ${item.name}: attempted weight $includeWeight exceeds capacity $capacity.", "INFEASIBLE · $includeWeight > $capacity")
            } else {
                val included = node(includePath, level + 1, includeWeight, includeValue, chosen + level, KnapsackNodeState.ACTIVE, "Include ${item.name}")
                snapshot(included, KnapsackBbAction.BRANCH_INCLUDE, "Include ${item.name}: weight $includeWeight, integer value $includeValue, upper bound ${included.upperBound}.", "INCLUDE ${item.name}")
                if (fractionalUpperBoundFloor(capacity, includeWeight, includeValue, ordered, level + 1) <= incumbent.toLong()) {
                    val pruned = included.copy(state = KnapsackNodeState.PRUNED, comparisonThreshold = incumbent)
                    snapshot(pruned, KnapsackBbAction.PRUNE_NODE, "floor(fractional upper bound ${pruned.upperBound}) = ${pruned.upperBoundFloor} ≤ incumbent $incumbent; prune this integer branch.", "PRUNE · FLOOR ${pruned.upperBoundFloor} ≤ $incumbent")
                }
                else visit(level + 1, includeWeight, includeValue, chosen + level, includePath)
            }
            val excluded = node("$path → exclude ${item.name}", level + 1, weight, value, chosen, KnapsackNodeState.ACTIVE, "Exclude ${item.name}")
            snapshot(excluded, KnapsackBbAction.BRANCH_EXCLUDE, "Exclude ${item.name}: upper bound ${excluded.upperBound}.", "EXCLUDE ${item.name}")
            if (fractionalUpperBoundFloor(capacity, weight, value, ordered, level + 1) <= incumbent.toLong()) {
                val pruned = excluded.copy(state = KnapsackNodeState.PRUNED, comparisonThreshold = incumbent)
                snapshot(pruned, KnapsackBbAction.PRUNE_NODE, "floor(fractional upper bound ${pruned.upperBound}) = ${pruned.upperBoundFloor} ≤ incumbent $incumbent; prune this integer branch.", "PRUNE · FLOOR ${pruned.upperBoundFloor} ≤ $incumbent")
            }
            else visit(level + 1, weight, value, chosen, "$path → exclude ${item.name}")
        }
        visit(0, 0, 0, emptyList(), "Root")
        snapshot(null, KnapsackBbAction.FINISHED, "Search exhausted or safely pruned every branch. Optimal 0/1 value = $incumbent with ${incumbentItems.joinToString()}.", "COMPLETE: OPTIMAL VALUE = $incumbent", true)
        return steps
    }

    private fun safeAdd(left: Int, right: Int, label: String): Int {
        val total = left.toLong() + right.toLong()
        require(total <= Int.MAX_VALUE) { "$label accumulation exceeds supported Int range." }
        return total.toInt()
    }

    private fun checkedLongAdd(left: Long, right: Long): Long {
        require(right <= Long.MAX_VALUE - left) { "Fractional bound exceeds supported numeric range." }
        return left + right
    }
}
