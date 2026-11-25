package com.example.learndsandalgorithm.presentation.learn

/** A node's decision path is its identity; the empty path identifies the root. */
internal data class FundamentalsTreeNode(val path: String) {
    val id: String get() = "node:$path"
    val parentPath: String? get() = if (path.isEmpty()) null else path.dropLast(1)
}

internal data class FundamentalsTreeConnection(val parent: String, val child: String) {
    val id: String get() = "edge:$parent->$child"
}

internal data class FundamentalsTreeFrame(
    val step: BacktrackingFundamentalsStep,
    val activePaths: Set<String>,
    val solutionPaths: Set<String>,
    val backtrackFrom: String?,
    val backtrackTo: String?
)

internal data class FundamentalsTreePresentation(
    val nodes: List<FundamentalsTreeNode>,
    val connections: List<FundamentalsTreeConnection>,
    val frames: List<FundamentalsTreeFrame>
)

internal object BacktrackingFundamentalsSceneBuilder {
    fun build(steps: List<BacktrackingFundamentalsStep>): FundamentalsTreePresentation {
        // Derive the represented topology from the actual trace, including completed paths.
        val paths = linkedSetOf("")
        steps.forEach { step ->
            (listOf(step.currentString) + step.generatedStrings).forEach { path ->
                for (length in 1..path.length) paths.add(path.take(length))
            }
        }
        val ordered = paths.sortedWith(compareBy<String> { it.length }.thenBy { it })
        val nodes = ordered.map(::FundamentalsTreeNode)
        val connections = nodes.mapNotNull { node ->
            node.parentPath?.let { FundamentalsTreeConnection(it, node.path) }
        }
        val frames = steps.mapIndexed { index, step ->
            val previous = steps.getOrNull(index - 1)?.currentString
            val from = previous?.takeIf {
                step.action == BacktrackingFundamentalsAction.BACKTRACK &&
                    it.length == step.currentString.length + 1 && it.dropLast(1) == step.currentString
            }
            FundamentalsTreeFrame(
                step = step,
                activePaths = (0..step.currentString.length).mapTo(linkedSetOf()) { step.currentString.take(it) },
                solutionPaths = step.generatedStrings.toSet(),
                backtrackFrom = from,
                backtrackTo = if (from != null) step.currentString else null
            )
        }
        return FundamentalsTreePresentation(nodes, connections, frames)
    }
}
