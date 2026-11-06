package com.example.learndsandalgorithm.presentation.learn

internal enum class BbFundamentalsAction {
    INIT,
    EXPLORE_BRANCH,
    FOUND_FEASIBLE,
    PRUNE_BRANCH,
    FINISHED
}

internal data class BranchBoundFundamentalsStep(
    val incumbentCost: Int? = null,
    val incumbentSolution: String? = null,
    val currentNodeId: String? = null,
    val currentNodeBound: Int? = null,
    val exploredNodeIds: List<String> = emptyList(),
    val prunedNodeIds: List<String> = emptyList(),
    val action: BbFundamentalsAction = BbFundamentalsAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BranchBoundFundamentalsTrace {

    fun createTrace(): List<BranchBoundFundamentalsStep> {
        val steps = mutableListOf<BranchBoundFundamentalsStep>()
        val explored = mutableListOf<String>()
        val pruned = mutableListOf<String>()
        var incumbent: Int? = null
        var incumbentSol: String? = null

        fun snapshot(
            nodeId: String?,
            bound: Int?,
            action: BbFundamentalsAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = BranchBoundFundamentalsStep(
            incumbentCost = incumbent,
            incumbentSolution = incumbentSol,
            currentNodeId = nodeId,
            currentNodeBound = bound,
            exploredNodeIds = explored.toList(),
            prunedNodeIds = pruned.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                nodeId = "Root",
                bound = 10,
                action = BbFundamentalsAction.INIT,
                explanation = "Branch and Bound Fundamentals: Minimization problem on state-space tree. Goal: find minimum cost solution. Incumbent cost = INF.",
                badgeText = "READY · MINIMIZATION SEARCH"
            )
        )

        // Branch A
        explored.add("Node A")
        steps.add(
            snapshot(
                nodeId = "Node A",
                bound = 12,
                action = BbFundamentalsAction.EXPLORE_BRANCH,
                explanation = "Explore Node A: Lower bound = 12 < Incumbent (INF). Recurse deeper into Branch A.",
                badgeText = "EXPLORE NODE A (BOUND 12 < INF)"
            )
        )

        // Leaf A
        explored.add("Leaf A")
        incumbent = 15
        incumbentSol = "Leaf A (Cost 15)"
        steps.add(
            snapshot(
                nodeId = "Leaf A",
                bound = 15,
                action = BbFundamentalsAction.FOUND_FEASIBLE,
                explanation = "Feasible Leaf A reached! Solution cost = 15. Set Incumbent threshold = 15.",
                badgeText = "NEW INCUMBENT = 15 (LEAF A)"
            )
        )

        // Branch B
        pruned.add("Node B")
        steps.add(
            snapshot(
                nodeId = "Node B",
                bound = 18,
                action = BbFundamentalsAction.PRUNE_BRANCH,
                explanation = "Inspect Node B: Lower bound = 18 >= Incumbent (15). PRUNE BRANCH B! No solution in subtree B can beat 15.",
                badgeText = "PRUNE NODE B (18 ≥ INCUMBENT 15)"
            )
        )

        // Branch C
        explored.add("Node C")
        steps.add(
            snapshot(
                nodeId = "Node C",
                bound = 8,
                action = BbFundamentalsAction.EXPLORE_BRANCH,
                explanation = "Explore Node C: Lower bound = 8 < Incumbent (15). Recurse deeper into Branch C.",
                badgeText = "EXPLORE NODE C (BOUND 8 < INCUMBENT 15)"
            )
        )

        // Leaf C
        explored.add("Leaf C")
        incumbent = 8
        incumbentSol = "Leaf C (Cost 8)"
        steps.add(
            snapshot(
                nodeId = "Leaf C",
                bound = 8,
                action = BbFundamentalsAction.FOUND_FEASIBLE,
                explanation = "Feasible Leaf C reached! Solution cost = 8 < Incumbent (15). Update Incumbent threshold = 8!",
                badgeText = "UPDATED INCUMBENT = 8 (LEAF C)"
            )
        )

        // FINISHED
        steps.add(
            snapshot(
                nodeId = null,
                bound = null,
                action = BbFundamentalsAction.FINISHED,
                explanation = "Branch and Bound search complete! Optimal solution: $incumbentSol. Node B was pruned because its bound (18) failed to beat the incumbent threshold.",
                badgeText = "COMPLETE: OPTIMAL COST = 8",
                isTerminal = true
            )
        )

        return steps
    }
}
