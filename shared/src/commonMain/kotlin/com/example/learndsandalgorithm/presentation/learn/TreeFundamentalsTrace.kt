package com.example.learndsandalgorithm.presentation.learn

internal enum class TreeConcept {
    ROOT,
    PARENT_CHILD,
    SIBLINGS,
    LEAVES,
    SUBTREE,
    DEPTH_AND_HEIGHT
}

internal data class ConceptNode(
    val id: String,
    val value: String,
    val parentId: String?,
    val depth: Int,
    val height: Int,
    val isLeaf: Boolean
)

internal data class TreeFundamentalsTraceStep(
    val concept: TreeConcept,
    val nodes: List<ConceptNode>,
    val highlightedNodeIds: Set<String>,
    val focusedParentId: String? = null,
    val focusedSubtreeRootId: String? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object TreeFundamentalsTrace {

    fun stepsForFundamentals(): List<TreeFundamentalsTraceStep> {
        val steps = mutableListOf<TreeFundamentalsTraceStep>()

        // Tree Hierarchy:
        // Level 0: A (Root, depth 0, height 2)
        // Level 1: B (depth 1, height 1), C (depth 1, height 1), D (depth 1, height 0, Leaf)
        // Level 2: E (under B, depth 2, height 0, Leaf), F (under B, depth 2, height 0, Leaf), G (under C, depth 2, height 0, Leaf)
        val allNodes = listOf(
            ConceptNode("A", "A (Root)", null, 0, 2, false),
            ConceptNode("B", "B", "A", 1, 1, false),
            ConceptNode("C", "C", "A", 1, 1, false),
            ConceptNode("D", "D (Leaf)", "A", 1, 0, true),
            ConceptNode("E", "E (Leaf)", "B", 2, 0, true),
            ConceptNode("F", "F (Leaf)", "B", 2, 0, true),
            ConceptNode("G", "G (Leaf)", "C", 2, 0, true)
        )

        fun snapshot(
            concept: TreeConcept,
            highlighted: Set<String>,
            parentId: String? = null,
            subtreeId: String? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TreeFundamentalsTraceStep(
            concept = concept,
            nodes = allNodes,
            highlightedNodeIds = highlighted,
            focusedParentId = parentId,
            focusedSubtreeRootId = subtreeId,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                concept = TreeConcept.ROOT,
                highlighted = emptySet(),
                explanation = "Tree Fundamentals: A tree is a hierarchical structure of nodes connected by edges.",
                badgeText = "READY · TREE TERMINOLOGY"
            )
        )

        // Step 1: Root Node
        steps.add(
            snapshot(
                concept = TreeConcept.ROOT,
                highlighted = setOf("A"),
                explanation = "ROOT NODE: 'A' is the topmost node with no parent. Depth = 0.",
                badgeText = "ROOT: NODE 'A'"
            )
        )

        // Step 2: Parent & Child
        steps.add(
            snapshot(
                concept = TreeConcept.PARENT_CHILD,
                highlighted = setOf("B", "E", "F"),
                parentId = "B",
                explanation = "PARENT & CHILD: Node 'B' is parent to children 'E' and 'F'. Nodes connected below a parent are its children.",
                badgeText = "PARENT 'B' → CHILDREN 'E', 'F'"
            )
        )

        // Step 3: Siblings
        steps.add(
            snapshot(
                concept = TreeConcept.SIBLINGS,
                highlighted = setOf("B", "C", "D"),
                parentId = "A",
                explanation = "SIBLINGS: Nodes 'B', 'C', and 'D' share the exact same parent node 'A'.",
                badgeText = "SIBLINGS: 'B', 'C', 'D'"
            )
        )

        // Step 4: Leaf Nodes
        steps.add(
            snapshot(
                concept = TreeConcept.LEAVES,
                highlighted = setOf("E", "F", "G", "D"),
                explanation = "LEAF NODES (TERMINAL): Nodes 'E', 'F', 'G', and 'D' have no children (their child references are null).",
                badgeText = "LEAVES: 'E', 'F', 'G', 'D'"
            )
        )

        // Step 5: Subtree
        steps.add(
            snapshot(
                concept = TreeConcept.SUBTREE,
                highlighted = setOf("B", "E", "F"),
                subtreeId = "B",
                explanation = "SUBTREE: Node 'B' and all its descendants ('E', 'F') form a valid subtree rooted at 'B'.",
                badgeText = "SUBTREE ROOTED AT 'B'"
            )
        )

        // Step 6: Depth & Height
        steps.add(
            snapshot(
                concept = TreeConcept.DEPTH_AND_HEIGHT,
                highlighted = setOf("G"),
                isTerminal = true,
                explanation = "DEPTH & HEIGHT: Node 'G' has Depth = 2 (2 edges from Root A) and Height = 0 (Leaf). Overall Tree Height = 2 (max path A→C→G).",
                badgeText = "NODE 'G': DEPTH=2, HEIGHT=0"
            )
        )

        return steps
    }
}
