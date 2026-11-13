package com.example.learndsandalgorithm.presentation.learn

internal enum class BstOpType {
    SEARCH,
    INSERT,
    DELETE_CASE_1_LEAF,
    DELETE_CASE_2_ONE_CHILD,
    DELETE_CASE_3_TWO_CHILDREN
}

internal data class BstNode(
    val id: String,
    val value: Int,
    val leftId: String? = null,
    val rightId: String? = null,
    val isHighlighted: Boolean = false,
    val isSuccessor: Boolean = false
)

internal data class BstOperationsTraceStep(
    val opType: BstOpType,
    val targetVal: Int,
    val nodes: List<BstNode>,
    val inspectedNodeId: String? = null,
    val successorVal: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BstOperationsTrace {

    fun stepsForOperation(opType: BstOpType): List<BstOperationsTraceStep> {
        return when (opType) {
            BstOpType.SEARCH -> generateSearchSteps()
            BstOpType.INSERT -> generateInsertSteps()
            BstOpType.DELETE_CASE_1_LEAF -> generateDeleteLeafSteps()
            BstOpType.DELETE_CASE_2_ONE_CHILD -> generateDeleteOneChildSteps()
            BstOpType.DELETE_CASE_3_TWO_CHILDREN -> generateDeleteTwoChildrenSteps()
        }
    }

    private fun generateSearchSteps(): List<BstOperationsTraceStep> {
        val steps = mutableListOf<BstOperationsTraceStep>()
        val initialNodes = listOf(
            BstNode("N10", 10, "N5", "N15"),
            BstNode("N5", 5, "N2", "N7"),
            BstNode("N15", 15, "N12", "N20"),
            BstNode("N2", 2),
            BstNode("N7", 7),
            BstNode("N12", 12),
            BstNode("N20", 20)
        )

        fun snapshot(
            inspectedId: String?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BstOperationsTraceStep(
            opType = BstOpType.SEARCH,
            targetVal = 12,
            nodes = initialNodes,
            inspectedNodeId = inspectedId,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, false, "SEARCH Target 12: Comparing target with current node value in O(log N) average time.", "READY · SEARCH 12"))
        steps.add(snapshot("N10", false, "Inspect Root [10]: 12 > 10. Recurse into Right Subtree.", "12 > 10 → RIGHT"))
        steps.add(snapshot("N15", false, "Inspect Right Node [15]: 12 < 15. Recurse into Left Subtree.", "12 < 15 → LEFT"))
        steps.add(snapshot("N12", true, "MATCH FOUND! Node [12] == 12. Search successful in O(log N) time.", "FOUND: 12"))

        return steps
    }

    private fun generateInsertSteps(): List<BstOperationsTraceStep> {
        val steps = mutableListOf<BstOperationsTraceStep>()
        val treeBefore = listOf(
            BstNode("N10", 10, "N5", "N15"),
            BstNode("N5", 5, "N2", "N7"),
            BstNode("N15", 15),
            BstNode("N2", 2),
            BstNode("N7", 7)
        )
        val treeAfter = listOf(
            BstNode("N10", 10, "N5", "N15"),
            BstNode("N5", 5, "N2", "N7"),
            BstNode("N15", 15),
            BstNode("N2", 2),
            BstNode("N7", 7, "N6_new", null),
            BstNode("N6_new", 6, isHighlighted = true)
        )

        fun snapshot(
            nodes: List<BstNode>,
            inspectedId: String?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BstOperationsTraceStep(
            opType = BstOpType.INSERT,
            targetVal = 6,
            nodes = nodes,
            inspectedNodeId = inspectedId,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(treeBefore, null, false, "INSERT Target 6: Traversing down tree to locate insertion position.", "READY · INSERT 6"))
        steps.add(snapshot(treeBefore, "N10", false, "Inspect Root [10]: 6 < 10. Traverse Left.", "6 < 10 → LEFT"))
        steps.add(snapshot(treeBefore, "N5", false, "Inspect Node [5]: 6 > 5. Traverse Right.", "6 > 5 → RIGHT"))
        steps.add(snapshot(treeBefore, "N7", false, "Inspect Node [7]: 6 < 7. Left child is null! Create and attach new node [6].", "6 < 7 → ATTACH LEFT"))
        steps.add(snapshot(treeAfter, "N6_new", true, "INSERTION COMPLETE! Node [6] attached as left child of [7]. BST invariant preserved.", "INSERTED: 6"))

        return steps
    }

    private fun generateDeleteLeafSteps(): List<BstOperationsTraceStep> {
        val steps = mutableListOf<BstOperationsTraceStep>()
        val before = listOf(
            BstNode("N10", 10, "N5", "N15"),
            BstNode("N5", 5, "N2", "N7"),
            BstNode("N15", 15),
            BstNode("N2", 2, isHighlighted = true),
            BstNode("N7", 7)
        )
        val after = listOf(
            BstNode("N10", 10, "N5", "N15"),
            BstNode("N5", 5, null, "N7"),
            BstNode("N15", 15),
            BstNode("N7", 7)
        )

        fun snapshot(
            nodes: List<BstNode>,
            inspectedId: String?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BstOperationsTraceStep(
            opType = BstOpType.DELETE_CASE_1_LEAF,
            targetVal = 2,
            nodes = nodes,
            inspectedNodeId = inspectedId,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(before, null, false, "DELETE CASE 1 (LEAF NODE): Node [2] has 0 children.", "DELETE LEAF [2]"))
        steps.add(snapshot(before, "N2", false, "Locate Leaf [2]: Node has no children. Set parent's [5] left link to null.", "LEAF LOCATED"))
        steps.add(snapshot(after, null, true, "DELETION COMPLETE! Leaf [2] removed directly in O(log N) time.", "REMOVED: [2]"))

        return steps
    }

    private fun generateDeleteOneChildSteps(): List<BstOperationsTraceStep> {
        val steps = mutableListOf<BstOperationsTraceStep>()
        // Node 3 has single child 4. Bypass 3 by linking parent 5 to 4.
        val before = listOf(
            BstNode("N5", 5, "N3", "N7"),
            BstNode("N3", 3, null, "N4", isHighlighted = true),
            BstNode("N7", 7),
            BstNode("N4", 4)
        )
        val after = listOf(
            BstNode("N5", 5, "N4", "N7"),
            BstNode("N7", 7),
            BstNode("N4", 4)
        )

        fun snapshot(
            nodes: List<BstNode>,
            inspectedId: String?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BstOperationsTraceStep(
            opType = BstOpType.DELETE_CASE_2_ONE_CHILD,
            targetVal = 3,
            nodes = nodes,
            inspectedNodeId = inspectedId,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(before, null, false, "DELETE CASE 2 (SINGLE CHILD): Node [3] has exactly 1 child [4].", "DELETE [3] (1 CHILD)"))
        steps.add(snapshot(before, "N3", false, "Bypass Node [3]: Connect parent [5] directly to single child [4].", "BYPASS NODE [3]"))
        steps.add(snapshot(after, null, true, "DELETION COMPLETE! Node [3] removed, parent [5] linked directly to child [4].", "REMOVED: [3]"))

        return steps
    }

    private fun generateDeleteTwoChildrenSteps(): List<BstOperationsTraceStep> {
        val steps = mutableListOf<BstOperationsTraceStep>()
        // Delete Root 10 (has 2 children 5 and 15).
        // In-order successor: minimum value in right subtree = 12.
        val beforeInitial = listOf(
            BstNode("N10", 10, "N5", "N15", isHighlighted = true),
            BstNode("N5", 5),
            BstNode("N15", 15, "N12", "N20"),
            BstNode("N12", 12, isSuccessor = false),
            BstNode("N20", 20)
        )
        val beforeSuccessor = listOf(
            BstNode("N10", 10, "N5", "N15", isHighlighted = true),
            BstNode("N5", 5),
            BstNode("N15", 15, "N12", "N20"),
            BstNode("N12", 12, isSuccessor = true),
            BstNode("N20", 20)
        )
        val stepCopy = listOf(
            BstNode("N10", 12, "N5", "N15", isHighlighted = true),
            BstNode("N5", 5),
            BstNode("N15", 15, "N12", "N20"),
            BstNode("N12", 12, isSuccessor = true),
            BstNode("N20", 20)
        )
        val finalTree = listOf(
            BstNode("N10", 12, "N5", "N15"),
            BstNode("N5", 5),
            BstNode("N15", 15, null, "N20"),
            BstNode("N20", 20)
        )

        fun snapshot(
            nodes: List<BstNode>,
            inspectedId: String?,
            succVal: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BstOperationsTraceStep(
            opType = BstOpType.DELETE_CASE_3_TWO_CHILDREN,
            targetVal = 10,
            nodes = nodes,
            inspectedNodeId = inspectedId,
            successorVal = succVal,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(beforeInitial, null, null, false, "DELETE CASE 3 (TWO CHILDREN): Node [10] has two children. Must find IN-ORDER SUCCESSOR.", "DELETE [10] (2 CHILDREN)"))
        steps.add(snapshot(beforeSuccessor, "N12", 12, false, "In-order Successor: Minimum value in right subtree is Node [12].", "FOUND SUCCESSOR [12]"))
        steps.add(snapshot(stepCopy, "N10", 12, false, "Copy Successor Value: Replace node value 10 with successor value 12.", "COPIED 12 TO ROOT"))
        steps.add(snapshot(finalTree, null, 12, true, "DELETION COMPLETE! Original successor node [12] deleted. BST invariant fully preserved!", "REMOVED SUCCESSOR NODE"))

        return steps
    }
}
