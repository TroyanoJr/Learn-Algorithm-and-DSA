package com.example.learndsandalgorithm.presentation.learn

internal data class PqNode(
    val id: String,
    val bound: Int,
    val isLeaf: Boolean = false,
    val cost: Int? = null
)

internal enum class BestFirstAction {
    INIT,
    PUSH_NODES,
    EXTRACT_BEST,
    FOUND_FEASIBLE,
    PRUNE_REMAINING_PQ,
    FINISHED
}

internal data class BestFirstSearchStep(
    val priorityQueue: List<PqNode> = emptyList(),
    val extractedNode: PqNode? = null,
    val incumbentCost: Int? = null,
    val prunedNodes: List<PqNode> = emptyList(),
    val action: BestFirstAction = BestFirstAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BestFirstSearchTrace {

    fun createTrace(): List<BestFirstSearchStep> {
        val steps = mutableListOf<BestFirstSearchStep>()
        val pq = mutableListOf<PqNode>()
        val pruned = mutableListOf<PqNode>()
        var incumbent: Int? = null

        fun snapshot(
            extracted: PqNode?,
            action: BestFirstAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = BestFirstSearchStep(
            priorityQueue = pq.sortedBy { it.bound }.toList(),
            extractedNode = extracted,
            incumbentCost = incumbent,
            prunedNodes = pruned.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                extracted = null,
                action = BestFirstAction.INIT,
                explanation = "Best-First Search & Priority Queues: Always expand the active live node with the most promising (lowest) bound to reach optimal solutions early.",
                badgeText = "READY · BEST-FIRST SEARCH (MIN-HEAP)"
            )
        )

        // Step 1: Push nodes
        val n1 = PqNode("Node 1", 12, isLeaf = false)
        val n2 = PqNode("Node 2", 8, isLeaf = false)
        val n3 = PqNode("Node 3", 15, isLeaf = false)
        val n4 = PqNode("Node 4", 6, isLeaf = true, cost = 7)

        pq.addAll(listOf(n1, n2, n3, n4))
        steps.add(
            snapshot(
                extracted = null,
                action = BestFirstAction.PUSH_NODES,
                explanation = "Enqueue candidate nodes into Priority Queue: Node 1(b:12), Node 2(b:8), Node 3(b:15), Node 4(b:6). Queue sorted by bound: [Node 4(6), Node 2(8), Node 1(12), Node 3(15)].",
                badgeText = "ENQUEUE NODES -> TOP = Node 4 (BOUND 6)"
            )
        )

        // Step 2: Extract top node (Node 4, bound 6)
        pq.sortBy { it.bound }
        val best = pq.removeAt(0)
        steps.add(
            snapshot(
                extracted = best,
                action = BestFirstAction.EXTRACT_BEST,
                explanation = "Pop top node from Priority Queue: Extracted Node 4 (minimum bound = 6). Most promising candidate!",
                badgeText = "POP TOP: Node 4 (MIN BOUND 6)"
            )
        )

        // Step 3: Feasible solution Node 4 (cost 7)
        incumbent = best.cost
        steps.add(
            snapshot(
                extracted = best,
                action = BestFirstAction.FOUND_FEASIBLE,
                explanation = "Explore Node 4: Reached feasible leaf with cost 7! Set Incumbent threshold = 7.",
                badgeText = "FOUND FEASIBLE COST = 7 (NODE 4)"
            )
        )

        // Step 4: Prune remaining nodes in PQ
        val remaining = pq.toList()
        pruned.addAll(remaining)
        pq.clear()

        steps.add(
            snapshot(
                extracted = null,
                action = BestFirstAction.PRUNE_REMAINING_PQ,
                explanation = "Inspect next PQ item Node 2 (bound 8). Since 8 >= Incumbent (7), PRUNE Node 2 and ALL remaining nodes in Priority Queue (${remaining.map { "${it.id}(b:${it.bound})" }})!",
                badgeText = "PRUNE ALL REMAINING PQ NODES (BOUNDS ≥ 7)"
            )
        )

        // FINISHED
        steps.add(
            snapshot(
                extracted = null,
                action = BestFirstAction.FINISHED,
                explanation = "Best-First Search complete! Optimal cost = 7 found in just 1 node expansion. Priority Queue ordering enabled immediate pruning of all other candidate branches.",
                badgeText = "COMPLETE: OPTIMAL COST = 7 IN 1 EXPANSION",
                isTerminal = true
            )
        )

        return steps
    }
}
