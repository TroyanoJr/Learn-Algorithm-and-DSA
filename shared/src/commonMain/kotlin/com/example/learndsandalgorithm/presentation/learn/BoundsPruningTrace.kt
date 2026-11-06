package com.example.learndsandalgorithm.presentation.learn

internal enum class ProblemType {
    MINIMIZATION,
    MAXIMIZATION
}

internal enum class BoundsPruningAction {
    INIT,
    EVALUATE_MINIMIZATION,
    EVALUATE_MAXIMIZATION,
    UPDATE_INCUMBENT,
    FINISHED
}

internal data class BoundsPruningStep(
    val problemType: ProblemType = ProblemType.MINIMIZATION,
    val minIncumbent: Int = 10,
    val maxIncumbent: Int = 40,
    val evaluatedNodeId: String? = null,
    val evaluatedBound: Int? = null,
    val isPruned: Boolean? = null,
    val action: BoundsPruningAction = BoundsPruningAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BoundsPruningTrace {

    fun createTrace(): List<BoundsPruningStep> {
        val steps = mutableListOf<BoundsPruningStep>()
        val minInc = 10
        var maxInc = 40

        fun snapshot(
            type: ProblemType,
            nodeId: String?,
            bound: Int?,
            pruned: Boolean?,
            action: BoundsPruningAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = BoundsPruningStep(
            problemType = type,
            minIncumbent = minInc,
            maxIncumbent = maxInc,
            evaluatedNodeId = nodeId,
            evaluatedBound = bound,
            isPruned = pruned,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                type = ProblemType.MINIMIZATION,
                nodeId = null,
                bound = null,
                pruned = null,
                action = BoundsPruningAction.INIT,
                explanation = "Bounds, Incumbents, and Pruning: Minimization uses lower bounds (prune if bound ≥ incumbent). Maximization uses upper bounds (prune if bound ≤ incumbent).",
                badgeText = "READY · PRUNING RULES EVALUATION"
            )
        )

        // Minimization Node A (bound 8 < 10)
        steps.add(
            snapshot(
                type = ProblemType.MINIMIZATION,
                nodeId = "Node A",
                bound = 8,
                pruned = false,
                action = BoundsPruningAction.EVALUATE_MINIMIZATION,
                explanation = "Minimization - Node A (Lower Bound 8): 8 < Min Incumbent (10). KEEP & EXPLORE subtree A.",
                badgeText = "MIN: NODE A (BOUND 8 < 10) -> KEEP"
            )
        )

        // Minimization Node B (bound 12 >= 10)
        steps.add(
            snapshot(
                type = ProblemType.MINIMIZATION,
                nodeId = "Node B",
                bound = 12,
                pruned = true,
                action = BoundsPruningAction.EVALUATE_MINIMIZATION,
                explanation = "Minimization - Node B (Lower Bound 12): 12 ≥ Min Incumbent (10). PRUNE SUBTREE B!",
                badgeText = "MIN: NODE B (BOUND 12 ≥ 10) -> PRUNE"
            )
        )

        // Maximization Node X (bound 50 > 40)
        steps.add(
            snapshot(
                type = ProblemType.MAXIMIZATION,
                nodeId = "Node X",
                bound = 50,
                pruned = false,
                action = BoundsPruningAction.EVALUATE_MAXIMIZATION,
                explanation = "Maximization - Node X (Upper Bound 50): 50 > Max Incumbent (40). KEEP & EXPLORE subtree X.",
                badgeText = "MAX: NODE X (BOUND 50 > 40) -> KEEP"
            )
        )

        // Maximization Node Y (bound 35 <= 40)
        steps.add(
            snapshot(
                type = ProblemType.MAXIMIZATION,
                nodeId = "Node Y",
                bound = 35,
                pruned = true,
                action = BoundsPruningAction.EVALUATE_MAXIMIZATION,
                explanation = "Maximization - Node Y (Upper Bound 35): 35 ≤ Max Incumbent (40). PRUNE SUBTREE Y!",
                badgeText = "MAX: NODE Y (BOUND 35 ≤ 40) -> PRUNE"
            )
        )

        // Update Max Incumbent
        maxInc = 48
        steps.add(
            snapshot(
                type = ProblemType.MAXIMIZATION,
                nodeId = "Leaf X",
                bound = 48,
                pruned = false,
                action = BoundsPruningAction.UPDATE_INCUMBENT,
                explanation = "Feasible Leaf X reached! Solution value = 48 > Max Incumbent (40). Update Max Incumbent threshold = 48!",
                badgeText = "UPDATE MAX INCUMBENT = 48"
            )
        )

        // FINISHED
        steps.add(
            snapshot(
                type = ProblemType.MAXIMIZATION,
                nodeId = null,
                bound = null,
                pruned = null,
                action = BoundsPruningAction.FINISHED,
                explanation = "Pruning rules evaluation complete! Demonstrated exact threshold conditions for lower bound (minimization) and upper bound (maximization) pruning.",
                badgeText = "COMPLETE: PRUNING RULES VERIFIED",
                isTerminal = true
            )
        )

        return steps
    }
}
