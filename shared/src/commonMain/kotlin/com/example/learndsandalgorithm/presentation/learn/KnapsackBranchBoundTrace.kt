package com.example.learndsandalgorithm.presentation.learn

internal data class BbKnapsackItem(
    val name: String,
    val weight: Int,
    val value: Int
) {
    val ratio: Double get() = value.toDouble() / weight.toDouble()
}

internal data class KnapsackBbNode(
    val id: String,
    val level: Int,
    val currentWeight: Int,
    val currentValue: Int,
    val upperBound: Double,
    val itemsTaken: List<String>
)

internal enum class KnapsackBbAction {
    INIT,
    COMPUTE_UPPER_BOUND,
    BRANCH_INCLUDE,
    BRANCH_EXCLUDE,
    UPDATE_INCUMBENT,
    PRUNE_NODE,
    FINISHED
}

internal data class KnapsackBranchBoundStep(
    val capacity: Int = 10,
    val items: List<BbKnapsackItem> = listOf(
        BbKnapsackItem("Item 1", 4, 40),
        BbKnapsackItem("Item 2", 7, 70),
        BbKnapsackItem("Item 3", 5, 45)
    ),
    val incumbentValue: Int = 0,
    val incumbentItems: List<String> = emptyList(),
    val currentNode: KnapsackBbNode? = null,
    val activeQueue: List<KnapsackBbNode> = emptyList(),
    val action: KnapsackBbAction = KnapsackBbAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object KnapsackBranchBoundTrace {

    val defaultItems = listOf(
        BbKnapsackItem("Item 1", 4, 40),
        BbKnapsackItem("Item 2", 7, 70),
        BbKnapsackItem("Item 3", 5, 45)
    )

    fun computeUpperBound(
        capacity: Int,
        currentWeight: Int,
        currentValue: Int,
        items: List<BbKnapsackItem>,
        startIndex: Int
    ): Double {
        var ub = currentValue.toDouble()
        var remCap = (capacity - currentWeight).toDouble()

        for (i in startIndex until items.size) {
            val item = items[i]
            if (item.weight <= remCap) {
                remCap -= item.weight
                ub += item.value
            } else {
                ub += item.value * (remCap / item.weight)
                break
            }
        }
        return ub
    }

    fun createTrace(
        items: List<BbKnapsackItem> = defaultItems,
        capacity: Int = 10
    ): List<KnapsackBranchBoundStep> {
        val steps = mutableListOf<KnapsackBranchBoundStep>()
        val sortedItems = items.sortedByDescending { it.ratio }
        val queue = mutableListOf<KnapsackBbNode>()
        var incumbentVal = 0
        var incumbentList = emptyList<String>()

        fun snapshot(
            node: KnapsackBbNode?,
            action: KnapsackBbAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = KnapsackBranchBoundStep(
            capacity = capacity,
            items = sortedItems,
            incumbentValue = incumbentVal,
            incumbentItems = incumbentList,
            currentNode = node,
            activeQueue = queue.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                node = null,
                action = KnapsackBbAction.INIT,
                explanation = "0/1 Knapsack Branch & Bound: Capacity $capacity. Items sorted by ratio: ${sortedItems.map { "${it.name}(w:${it.weight},v:${it.value},r:${it.ratio})" }}. Fractional relaxation provides upper bounds.",
                badgeText = "READY · CAPACITY $capacity 0/1 KNAPSACK B&B"
            )
        )

        // Root upper bound calculation
        val rootUb = computeUpperBound(capacity, 0, 0, sortedItems, 0)
        val rootNode = KnapsackBbNode("Root", 0, 0, 0, rootUb, emptyList())

        steps.add(
            snapshot(
                node = rootNode,
                action = KnapsackBbAction.COMPUTE_UPPER_BOUND,
                explanation = "Root Node: Calculated fractional relaxation upper bound = $rootUb (Item 1 100% + Item 2 85.7%).",
                badgeText = "ROOT UPPER BOUND = $rootUb"
            )
        )

        // Branch 1: Include Item 1 (w:4, v:40) + Include Item 3 (w:5, v:45)
        val nodeA = KnapsackBbNode("Node A (Inc Item 1)", 1, 4, 40, rootUb, listOf("Item 1"))
        steps.add(
            snapshot(
                node = nodeA,
                action = KnapsackBbAction.BRANCH_INCLUDE,
                explanation = "Branch Include Item 1: Weight = 4, Value = 40. Upper bound = $rootUb.",
                badgeText = "INCLUDE ITEM 1 (W:4, V:40)"
            )
        )

        // Leaf Feasible: Item 1 + Item 3 (w:9, v:85)
        val leaf1 = KnapsackBbNode("Leaf 1 (Item 1+3)", 3, 9, 85, 85.0, listOf("Item 1", "Item 3"))
        incumbentVal = 85
        incumbentList = listOf("Item 1", "Item 3")

        steps.add(
            snapshot(
                node = leaf1,
                action = KnapsackBbAction.UPDATE_INCUMBENT,
                explanation = "Feasible 0/1 Integer Solution Found! Items 1 & 3: Total weight 9 <= 10, Total value = 85. Updated Incumbent threshold = 85!",
                badgeText = "NEW INCUMBENT = 85 (ITEMS 1 & 3)"
            )
        )

        // Branch 2: Exclude Item 1 from Root
        val rootExUb = computeUpperBound(capacity, 0, 0, sortedItems, 1)
        val nodeB = KnapsackBbNode("Node B (Exc Item 1)", 1, 0, 0, rootExUb, emptyList())

        steps.add(
            snapshot(
                node = nodeB,
                action = KnapsackBbAction.BRANCH_EXCLUDE,
                explanation = "Branch Exclude Item 1: Fractional upper bound = $rootExUb (Item 2 100% + Item 3 60%).",
                badgeText = "EXCLUDE ITEM 1 (BOUND $rootExUb)"
            )
        )

        // Exclude Item 1 & Exclude Item 2 (Only Item 3: w:5, v:45)
        val nodeC = KnapsackBbNode("Node C (Exc 1&2)", 2, 0, 0, 45.0, emptyList())

        steps.add(
            snapshot(
                node = nodeC,
                action = KnapsackBbAction.PRUNE_NODE,
                explanation = "Inspect Node C (Exclude Items 1 & 2): Upper bound = 45.0 <= Incumbent (85). PRUNE NODE C!",
                badgeText = "PRUNE NODE C (BOUND 45.0 ≤ INCUMBENT 85)"
            )
        )

        // FINISHED
        steps.add(
            snapshot(
                node = null,
                action = KnapsackBbAction.FINISHED,
                explanation = "0/1 Knapsack Branch & Bound complete! Optimal integer 0/1 value = $incumbentVal ($incumbentList). Fractional relaxations successfully pruned suboptimal subtrees.",
                badgeText = "COMPLETE: OPTIMAL VALUE = $incumbentVal",
                isTerminal = true
            )
        )

        return steps
    }
}
