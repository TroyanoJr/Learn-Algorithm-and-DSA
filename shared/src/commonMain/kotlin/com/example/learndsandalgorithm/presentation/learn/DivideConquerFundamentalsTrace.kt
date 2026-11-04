package com.example.learndsandalgorithm.presentation.learn

internal enum class DivideConquerPhase {
    DIVIDE,
    CONQUER,
    COMBINE
}

internal data class SubproblemNode(
    val id: String,
    val low: Int,
    val high: Int,
    val elements: List<Int>,
    val isBaseCase: Boolean,
    val leftChildId: String? = null,
    val rightChildId: String? = null,
    val maxValue: Int? = null
)

internal data class FundamentalsTraceStep(
    val phase: DivideConquerPhase,
    val activeSubproblemId: String?,
    val nodes: List<SubproblemNode>,
    val leftValue: Int? = null,
    val rightValue: Int? = null,
    val combinedValue: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object DivideConquerFundamentalsTrace {

    fun stepsForMaxFinder(initialArr: List<Int> = listOf(4, 2, 9, 7, 5)): List<FundamentalsTraceStep> {
        val steps = mutableListOf<FundamentalsTraceStep>()

        val rootNode = SubproblemNode(
            id = "root",
            low = 0,
            high = initialArr.lastIndex,
            elements = initialArr,
            isBaseCase = initialArr.size <= 1
        )

        val nodeList = mutableListOf(rootNode)

        fun snapshot(
            phase: DivideConquerPhase,
            activeId: String?,
            leftVal: Int? = null,
            rightVal: Int? = null,
            combinedVal: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = FundamentalsTraceStep(
            phase = phase,
            activeSubproblemId = activeId,
            nodes = nodeList.toList(),
            leftValue = leftVal,
            rightValue = rightVal,
            combinedValue = combinedVal,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                phase = DivideConquerPhase.DIVIDE,
                activeId = "root",
                explanation = "Divide & Conquer: Finding the maximum element in [${initialArr.joinToString(", ")}]. 3 Phases: Divide → Conquer → Combine.",
                badgeText = "READY · INPUT [${initialArr.joinToString(", ")}]"
            )
        )

        // Phase 1: Divide root [4, 2, 9, 7, 5] (low=0, high=4) -> mid = 2. Left: [4, 2, 9], Right: [7, 5]
        val leftNode1 = SubproblemNode("L1", 0, 2, listOf(4, 2, 9), false)
        val rightNode1 = SubproblemNode("R1", 3, 4, listOf(7, 5), false)

        nodeList.add(leftNode1)
        nodeList.add(rightNode1)

        steps.add(
            snapshot(
                phase = DivideConquerPhase.DIVIDE,
                activeId = "root",
                explanation = "1. DIVIDE: Split root [4, 2, 9, 7, 5] at mid = index 2 into Left [4, 2, 9] and Right [7, 5].",
                badgeText = "DIVIDE ROOT → LEFT & RIGHT"
            )
        )

        // Sub-divide Left [4, 2, 9] (low=0, high=2) -> mid = 1. Left-Left: [4, 2], Left-Right: [9]
        val llNode = SubproblemNode("LL", 0, 1, listOf(4, 2), false)
        val lrNode = SubproblemNode("LR", 2, 2, listOf(9), true, maxValue = 9)
        nodeList.add(llNode)
        nodeList.add(lrNode)

        steps.add(
            snapshot(
                phase = DivideConquerPhase.DIVIDE,
                activeId = "L1",
                explanation = "1. DIVIDE: Split Left subproblem [4, 2, 9] at mid = index 1 into [4, 2] and [9]. Note: [9] is a base case (length 1).",
                badgeText = "DIVIDE LEFT → [4, 2] & [9]"
            )
        )

        // Sub-divide [4, 2] (low=0, high=1) -> mid = 0. [4] and [2]
        val lllNode = SubproblemNode("LLL", 0, 0, listOf(4), true, maxValue = 4)
        val llrNode = SubproblemNode("LLR", 1, 1, listOf(2), true, maxValue = 2)
        nodeList.add(lllNode)
        nodeList.add(llrNode)

        steps.add(
            snapshot(
                phase = DivideConquerPhase.DIVIDE,
                activeId = "LL",
                explanation = "1. DIVIDE: Split [4, 2] into singletons [4] and [2] (base cases).",
                badgeText = "DIVIDE → BASE CASES [4] & [2]"
            )
        )

        // Sub-divide Right [7, 5] (low=3, high=4) -> mid = 3. [7] and [5]
        val rlNode = SubproblemNode("RL", 3, 3, listOf(7), true, maxValue = 7)
        val rrNode = SubproblemNode("RR", 4, 4, listOf(5), true, maxValue = 5)
        nodeList.add(rlNode)
        nodeList.add(rrNode)

        steps.add(
            snapshot(
                phase = DivideConquerPhase.DIVIDE,
                activeId = "R1",
                explanation = "1. DIVIDE: Split Right subproblem [7, 5] into base cases [7] and [5].",
                badgeText = "DIVIDE RIGHT → [7] & [5]"
            )
        )

        // Phase 2: Conquer (Base Cases solved)
        steps.add(
            snapshot(
                phase = DivideConquerPhase.CONQUER,
                activeId = null,
                explanation = "2. CONQUER: Base cases reached (length = 1). Subproblem results solved directly: [4]→4, [2]→2, [9]→9, [7]→7, [5]→5.",
                badgeText = "CONQUER: BASE CASES SOLVED"
            )
        )

        // Phase 3: Combine
        // Combine [4] and [2] -> max(4, 2) = 4
        updateNodeMax(nodeList, "LL", 4)
        steps.add(
            snapshot(
                phase = DivideConquerPhase.COMBINE,
                activeId = "LL",
                leftVal = 4,
                rightVal = 2,
                combinedVal = 4,
                explanation = "3. COMBINE: max(leftMax=4, rightMax=2) = 4 for subproblem [4, 2].",
                badgeText = "COMBINE [4, 2] → 4"
            )
        )

        // Combine [4, 2] (max 4) and [9] (max 9) -> max(4, 9) = 9
        updateNodeMax(nodeList, "L1", 9)
        steps.add(
            snapshot(
                phase = DivideConquerPhase.COMBINE,
                activeId = "L1",
                leftVal = 4,
                rightVal = 9,
                combinedVal = 9,
                explanation = "3. COMBINE: max(leftMax=4, rightMax=9) = 9 for Left half [4, 2, 9].",
                badgeText = "COMBINE LEFT [4, 2, 9] → 9"
            )
        )

        // Combine [7] and [5] -> max(7, 5) = 7
        updateNodeMax(nodeList, "R1", 7)
        steps.add(
            snapshot(
                phase = DivideConquerPhase.COMBINE,
                activeId = "R1",
                leftVal = 7,
                rightVal = 5,
                combinedVal = 7,
                explanation = "3. COMBINE: max(leftMax=7, rightMax=5) = 7 for Right half [7, 5].",
                badgeText = "COMBINE RIGHT [7, 5] → 7"
            )
        )

        // Final Combine: Left max (9) and Right max (7) -> max(9, 7) = 9
        updateNodeMax(nodeList, "root", 9)
        steps.add(
            snapshot(
                phase = DivideConquerPhase.COMBINE,
                activeId = "root",
                leftVal = 9,
                rightVal = 7,
                combinedVal = 9,
                isTerminal = true,
                explanation = "3. COMBINE COMPLETE: max(leftMax=9, rightMax=7) = 9. Original problem solved: Maximum value in [4, 2, 9, 7, 5] is 9!",
                badgeText = "FINAL RESULT: MAX = 9"
            )
        )

        return steps
    }

    private fun updateNodeMax(list: MutableList<SubproblemNode>, id: String, max: Int) {
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(maxValue = max)
        }
    }
}
