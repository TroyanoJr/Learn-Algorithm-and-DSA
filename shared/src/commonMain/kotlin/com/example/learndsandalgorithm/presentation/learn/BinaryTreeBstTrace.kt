package com.example.learndsandalgorithm.presentation.learn

internal enum class BstComparisonScenario {
    VALID_BST,
    INVALID_BST_VIOLATION,
    BALANCED_VS_SKEWED
}

internal data class BstNodeData(
    val id: String,
    val value: Int,
    val leftId: String? = null,
    val rightId: String? = null,
    val isViolation: Boolean = false
)

internal data class BinaryTreeBstTraceStep(
    val scenario: BstComparisonScenario,
    val nodes: List<BstNodeData>,
    val inspectedNodeId: String? = null,
    val isValidBst: Boolean = true,
    val violationMessage: String? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BinaryTreeBstTrace {

    fun stepsForScenario(scenario: BstComparisonScenario): List<BinaryTreeBstTraceStep> {
        return when (scenario) {
            BstComparisonScenario.VALID_BST -> generateValidBstSteps()
            BstComparisonScenario.INVALID_BST_VIOLATION -> generateInvalidBstSteps()
            BstComparisonScenario.BALANCED_VS_SKEWED -> generateSkewedSteps()
        }
    }

    private fun generateValidBstSteps(): List<BinaryTreeBstTraceStep> {
        val steps = mutableListOf<BinaryTreeBstTraceStep>()
        // Valid BST: Root 5; Left 3 (Left-Left 2, Left-Right 4); Right 7 (Right-Left 6, Right-Right 8)
        val validNodes = listOf(
            BstNodeData("N5", 5, "N3", "N7"),
            BstNodeData("N3", 3, "N2", "N4"),
            BstNodeData("N7", 7, "N6", "N8"),
            BstNodeData("N2", 2),
            BstNodeData("N4", 4),
            BstNodeData("N6", 6),
            BstNodeData("N8", 8)
        )

        fun snapshot(
            inspectedId: String? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BinaryTreeBstTraceStep(
            scenario = BstComparisonScenario.VALID_BST,
            nodes = validNodes,
            inspectedNodeId = inspectedId,
            isValidBst = true,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(
            snapshot(
                explanation = "BST Property: For every node X, all left descendants < X, and all right descendants > X.",
                badgeText = "VALID BST CHECK"
            )
        )

        steps.add(
            snapshot(
                inspectedId = "N5",
                explanation = "Inspect Root [5]: Left subtree {2, 3, 4} < 5, Right subtree {6, 7, 8} > 5. Valid!",
                badgeText = "ROOT [5] VALID"
            )
        )

        steps.add(
            snapshot(
                inspectedId = "N3",
                explanation = "Inspect Left Child [3]: Left [2] < 3, Right [4] > 3. Valid!",
                badgeText = "NODE [3] VALID"
            )
        )

        steps.add(
            snapshot(
                inspectedId = "N7",
                isTerminal = true,
                explanation = "Inspect Right Child [7]: Left [6] < 7, Right [8] > 7. Valid! Tree is a fully valid Binary Search Tree.",
                badgeText = "VALID BST CONFIRMED"
            )
        )

        return steps
    }

    private fun generateInvalidBstSteps(): List<BinaryTreeBstTraceStep> {
        val steps = mutableListOf<BinaryTreeBstTraceStep>()
        // Invalid BST: Root 5; Left 3; Right 7 (Right-Left 4 -> VIOLATION: 4 < 5 but in right subtree!)
        val invalidNodes = listOf(
            BstNodeData("N5", 5, "N3", "N7"),
            BstNodeData("N3", 3),
            BstNodeData("N7", 7, "N4_bad", "N8"),
            BstNodeData("N4_bad", 4, isViolation = true),
            BstNodeData("N8", 8)
        )

        fun snapshot(
            inspectedId: String? = null,
            isValid: Boolean = true,
            violationMsg: String? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BinaryTreeBstTraceStep(
            scenario = BstComparisonScenario.INVALID_BST_VIOLATION,
            nodes = invalidNodes,
            inspectedNodeId = inspectedId,
            isValidBst = isValid,
            violationMessage = violationMsg,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(
            snapshot(
                explanation = "Binary Tree vs BST: Not all binary trees are Binary Search Trees!",
                badgeText = "BST INVARIANT CHECK"
            )
        )

        steps.add(
            snapshot(
                inspectedId = "N5",
                explanation = "Inspect Root [5]: Checking left and right subtrees...",
                badgeText = "INSPECTING ROOT [5]"
            )
        )

        steps.add(
            snapshot(
                inspectedId = "N4_bad",
                isValid = false,
                violationMsg = "VIOLATION: Node [4] is in right subtree of Root [5], but 4 < 5!",
                isTerminal = true,
                explanation = "BST VIOLATION DETECTED! Node [4] is located in the right subtree of Root [5]. Since 4 < 5, it breaks the BST ordering invariant. This is a Binary Tree, NOT a BST!",
                badgeText = "INVALID BST (VIOLATION AT [4])"
            )
        )

        return steps
    }

    private fun generateSkewedSteps(): List<BinaryTreeBstTraceStep> {
        val steps = mutableListOf<BinaryTreeBstTraceStep>()
        // Skewed BST: 1 -> Right 2 -> Right 3 -> Right 4 -> Right 5
        val skewedNodes = listOf(
            BstNodeData("N1", 1, null, "N2"),
            BstNodeData("N2", 2, null, "N3"),
            BstNodeData("N3", 3, null, "N4"),
            BstNodeData("N4", 4, null, "N5"),
            BstNodeData("N5", 5)
        )

        fun snapshot(
            inspectedId: String? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BinaryTreeBstTraceStep(
            scenario = BstComparisonScenario.BALANCED_VS_SKEWED,
            nodes = skewedNodes,
            inspectedNodeId = inspectedId,
            isValidBst = true,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(
            snapshot(
                explanation = "Balanced vs Skewed BST: Inserting sorted data (1, 2, 3, 4, 5) creates a skewed tree.",
                badgeText = "SKEWED TREE DEGENERATION"
            )
        )

        steps.add(
            snapshot(
                inspectedId = "N1",
                explanation = "Skewed BST: Height = N - 1 = 4. Operations degrade from O(log N) to O(N) linear time (behaves like a Linked List).",
                badgeText = "SKEWED HEIGHT = 4 (O(N) TIME)"
            )
        )

        steps.add(
            snapshot(
                inspectedId = "N5",
                isTerminal = true,
                explanation = "Self-Balancing Trees (AVL/Red-Black) perform rotations to keep height O(log N).",
                badgeText = "BALANCING PRESERVES O(log N)"
            )
        )

        return steps
    }
}
