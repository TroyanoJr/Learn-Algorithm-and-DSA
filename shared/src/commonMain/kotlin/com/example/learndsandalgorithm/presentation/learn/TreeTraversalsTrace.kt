package com.example.learndsandalgorithm.presentation.learn

internal enum class TraversalOrder {
    PRE_ORDER,
    IN_ORDER,
    POST_ORDER,
    LEVEL_ORDER
}

internal data class TraversalNode(
    val id: String,
    val value: Int,
    val leftId: String? = null,
    val rightId: String? = null
)

internal data class TreeTraversalsTraceStep(
    val order: TraversalOrder,
    val currentNodeId: String?,
    val visitedNodes: List<Int>,
    val queueValues: List<Int> = emptyList(),
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object TreeTraversalsTrace {

    val treeNodes = listOf(
        TraversalNode("N5", 5, "N3", "N7"),
        TraversalNode("N3", 3, "N2", "N4"),
        TraversalNode("N7", 7, "N6", "N8"),
        TraversalNode("N2", 2),
        TraversalNode("N4", 4),
        TraversalNode("N6", 6),
        TraversalNode("N8", 8)
    )

    fun stepsForOrder(order: TraversalOrder): List<TreeTraversalsTraceStep> {
        return when (order) {
            TraversalOrder.PRE_ORDER -> generatePreOrderSteps()
            TraversalOrder.IN_ORDER -> generateInOrderSteps()
            TraversalOrder.POST_ORDER -> generatePostOrderSteps()
            TraversalOrder.LEVEL_ORDER -> generateLevelOrderSteps()
        }
    }

    private fun generatePreOrderSteps(): List<TreeTraversalsTraceStep> {
        val steps = mutableListOf<TreeTraversalsTraceStep>()
        val visited = mutableListOf<Int>()

        fun snapshot(
            currId: String?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TreeTraversalsTraceStep(
            order = TraversalOrder.PRE_ORDER,
            currentNodeId = currId,
            visitedNodes = visited.toList(),
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, false, "Pre-order Traversal (Root → Left → Right): Visits Root first, then Left subtree, then Right subtree.", "READY · PRE-ORDER"))

        val sequence = listOf(
            "N5" to "Visit Root [5] first",
            "N3" to "Traverse Left: Visit Node [3]",
            "N2" to "Traverse Left-Left: Visit Leaf [2]",
            "N4" to "Backtrack & Traverse Left-Right: Visit Leaf [4]",
            "N7" to "Backtrack to Root & Traverse Right: Visit Node [7]",
            "N6" to "Traverse Right-Left: Visit Leaf [6]",
            "N8" to "Traverse Right-Right: Visit Leaf [8]"
        )

        sequence.forEach { (id, expl) ->
            val valNode = treeNodes.first { it.id == id }.value
            visited.add(valNode)
            steps.add(snapshot(id, false, expl, "VISITED [$valNode]"))
        }

        steps.add(snapshot(null, true, "Pre-order Traversal complete! Visited sequence: [${visited.joinToString(", ")}]. Useful for cloning/serializing trees.", "COMPLETE: [${visited.joinToString(", ")}]"))
        return steps
    }

    private fun generateInOrderSteps(): List<TreeTraversalsTraceStep> {
        val steps = mutableListOf<TreeTraversalsTraceStep>()
        val visited = mutableListOf<Int>()

        fun snapshot(
            currId: String?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TreeTraversalsTraceStep(
            order = TraversalOrder.IN_ORDER,
            currentNodeId = currId,
            visitedNodes = visited.toList(),
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, false, "In-order Traversal (Left → Root → Right): Visits BST keys in strictly SORTED ASCENDING order!", "READY · IN-ORDER"))

        val sequence = listOf(
            "N2" to "Go deepest Left: Visit Leaf [2]",
            "N3" to "Visit Parent Node [3]",
            "N4" to "Visit Right Child [4]",
            "N5" to "Visit Root Node [5]",
            "N6" to "Traverse Right subtree: Visit Leaf [6]",
            "N7" to "Visit Parent Node [7]",
            "N8" to "Visit Right Child [8]"
        )

        sequence.forEach { (id, expl) ->
            val valNode = treeNodes.first { it.id == id }.value
            visited.add(valNode)
            steps.add(snapshot(id, false, expl, "VISITED [$valNode]"))
        }

        steps.add(snapshot(null, true, "In-order Traversal complete! Visited sequence is SORTED: [${visited.joinToString(", ")}].", "SORTED RESULT: [${visited.joinToString(", ")}]"))
        return steps
    }

    private fun generatePostOrderSteps(): List<TreeTraversalsTraceStep> {
        val steps = mutableListOf<TreeTraversalsTraceStep>()
        val visited = mutableListOf<Int>()

        fun snapshot(
            currId: String?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TreeTraversalsTraceStep(
            order = TraversalOrder.POST_ORDER,
            currentNodeId = currId,
            visitedNodes = visited.toList(),
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, false, "Post-order Traversal (Left → Right → Root): Visits children first before parent. Useful for bottom-up deletion.", "READY · POST-ORDER"))

        val sequence = listOf(
            "N2" to "Visit Left Leaf [2]",
            "N4" to "Visit Right Leaf [4]",
            "N3" to "Visit Parent Node [3] after children",
            "N6" to "Visit Right subtree Left Leaf [6]",
            "N8" to "Visit Right subtree Right Leaf [8]",
            "N7" to "Visit Parent Node [7] after children",
            "N5" to "Visit Root Node [5] LAST"
        )

        sequence.forEach { (id, expl) ->
            val valNode = treeNodes.first { it.id == id }.value
            visited.add(valNode)
            steps.add(snapshot(id, false, expl, "VISITED [$valNode]"))
        }

        steps.add(snapshot(null, true, "Post-order Traversal complete! Visited sequence: [${visited.joinToString(", ")}].", "COMPLETE: [${visited.joinToString(", ")}]"))
        return steps
    }

    private fun generateLevelOrderSteps(): List<TreeTraversalsTraceStep> {
        val steps = mutableListOf<TreeTraversalsTraceStep>()
        val visited = mutableListOf<Int>()

        fun snapshot(
            currId: String?,
            queue: List<Int>,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TreeTraversalsTraceStep(
            order = TraversalOrder.LEVEL_ORDER,
            currentNodeId = currId,
            visitedNodes = visited.toList(),
            queueValues = queue,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, listOf(5), false, "Level-order Traversal (BFS): Visits nodes level-by-level, top-to-bottom using a Queue.", "READY · LEVEL-ORDER (BFS)"))

        val sequence = listOf(
            Triple("N5", listOf(3, 7), "Dequeue [5] (Level 0), enqueue children [3, 7]"),
            Triple("N3", listOf(7, 2, 4), "Dequeue [3] (Level 1), enqueue children [2, 4]"),
            Triple("N7", listOf(2, 4, 6, 8), "Dequeue [7] (Level 1), enqueue children [6, 8]"),
            Triple("N2", listOf(4, 6, 8), "Dequeue [2] (Level 2, Leaf)"),
            Triple("N4", listOf(6, 8), "Dequeue [4] (Level 2, Leaf)"),
            Triple("N6", listOf(8), "Dequeue [6] (Level 2, Leaf)"),
            Triple("N8", emptyList(), "Dequeue [8] (Level 2, Leaf). Queue is now empty!")
        )

        sequence.forEach { (id, q, expl) ->
            val valNode = treeNodes.first { it.id == id }.value
            visited.add(valNode)
            steps.add(snapshot(id, q, false, expl, "DEQUEUED [$valNode]"))
        }

        steps.add(snapshot(null, emptyList(), true, "Level-order Traversal complete! Visited sequence: [${visited.joinToString(", ")}].", "BFS COMPLETE: [${visited.joinToString(", ")}]"))
        return steps
    }
}
