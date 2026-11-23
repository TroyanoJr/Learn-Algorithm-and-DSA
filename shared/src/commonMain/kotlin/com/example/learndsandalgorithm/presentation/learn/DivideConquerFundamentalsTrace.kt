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
        require(initialArr.isNotEmpty()) { "initialArr must not be empty" }

        val input = initialArr.toList()
        val steps = mutableListOf<FundamentalsTraceStep>()
        val nodes = mutableListOf(
            SubproblemNode(
                id = "root",
                low = 0,
                high = input.lastIndex,
                elements = input,
                isBaseCase = input.size == 1
            )
        )

        fun snapshot(
            phase: DivideConquerPhase,
            activeId: String?,
            leftValue: Int? = null,
            rightValue: Int? = null,
            combinedValue: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) {
            steps += FundamentalsTraceStep(
                phase = phase,
                activeSubproblemId = activeId,
                nodes = nodes.toList(),
                leftValue = leftValue,
                rightValue = rightValue,
                combinedValue = combinedValue,
                explanation = explanation,
                badgeText = badgeText,
                isTerminal = isTerminal
            )
        }

        fun idForPath(path: String): String = when {
            path.isEmpty() -> "root"
            path.length == 1 -> "${path}1"
            else -> path
        }

        fun valuesText(values: List<Int>): String = values.joinToString(", ", prefix = "[", postfix = "]")

        fun divideExplanation(node: SubproblemNode, mid: Int, left: SubproblemNode, right: SubproblemNode): String {
            val prefix = when {
                node.id == "root" -> "1. DIVIDE: Split root ${valuesText(input)} at mid = index $mid into Left ${valuesText(left.elements)} and Right ${valuesText(right.elements)}"
                node.id == "L1" -> "1. DIVIDE: Split Left subproblem ${valuesText(node.elements)} at mid = index $mid into ${valuesText(left.elements)} and ${valuesText(right.elements)}"
                node.id == "R1" && left.isBaseCase && right.isBaseCase -> "1. DIVIDE: Split Right subproblem ${valuesText(node.elements)} into base cases ${valuesText(left.elements)} and ${valuesText(right.elements)}"
                node.id == "R1" -> "1. DIVIDE: Split Right subproblem ${valuesText(node.elements)} at mid = index $mid into ${valuesText(left.elements)} and ${valuesText(right.elements)}"
                node.elements.size == 2 -> "1. DIVIDE: Split ${valuesText(node.elements)} into singletons ${valuesText(left.elements)} and ${valuesText(right.elements)} (base cases)"
                else -> "1. DIVIDE: Split ${valuesText(node.elements)} at mid = index $mid into ${valuesText(left.elements)} and ${valuesText(right.elements)}"
            }
            if (left.isBaseCase && right.isBaseCase && (node.elements.size == 2 || node.id == "R1")) return "$prefix."
            val baseCaseNotes = listOf(left, right)
                .filter { it.isBaseCase }
                .joinToString(" ") { "Note: ${valuesText(it.elements)} is a base case (length 1)." }
            return if (baseCaseNotes.isEmpty()) "$prefix." else "$prefix. $baseCaseNotes"
        }

        fun divideBadge(node: SubproblemNode, left: SubproblemNode, right: SubproblemNode): String = when {
            node.id == "root" -> "DIVIDE ROOT → LEFT & RIGHT"
            node.id == "L1" -> "DIVIDE LEFT → ${valuesText(left.elements)} & ${valuesText(right.elements)}"
            node.id == "R1" -> "DIVIDE RIGHT → ${valuesText(left.elements)} & ${valuesText(right.elements)}"
            left.isBaseCase && right.isBaseCase -> "DIVIDE → BASE CASES ${valuesText(left.elements)} & ${valuesText(right.elements)}"
            else -> "DIVIDE ${valuesText(node.elements)} → ${valuesText(left.elements)} & ${valuesText(right.elements)}"
        }

        snapshot(
            phase = DivideConquerPhase.DIVIDE,
            activeId = "root",
            explanation = "Divide & Conquer: Finding the maximum element in ${valuesText(input)}. 3 Phases: Divide → Conquer → Combine.",
            badgeText = "READY · INPUT ${valuesText(input)}"
        )

        fun divide(nodeId: String, path: String) {
            val index = nodes.indexOfFirst { it.id == nodeId }
            val parent = nodes[index]
            if (parent.isBaseCase) return

            val mid = parent.low + (parent.high - parent.low) / 2
            val leftPath = "$path" + "L"
            val rightPath = "$path" + "R"
            val leftId = idForPath(leftPath)
            val rightId = idForPath(rightPath)
            val left = SubproblemNode(
                id = leftId,
                low = parent.low,
                high = mid,
                elements = input.subList(parent.low, mid + 1),
                isBaseCase = mid == parent.low
            )
            val right = SubproblemNode(
                id = rightId,
                low = mid + 1,
                high = parent.high,
                elements = input.subList(mid + 1, parent.high + 1),
                isBaseCase = mid + 1 == parent.high
            )
            nodes[index] = parent.copy(leftChildId = leftId, rightChildId = rightId)
            nodes += left
            nodes += right

            snapshot(
                phase = DivideConquerPhase.DIVIDE,
                activeId = nodeId,
                explanation = divideExplanation(parent, mid, left, right),
                badgeText = divideBadge(parent, left, right)
            )

            divide(leftId, leftPath)
            divide(rightId, rightPath)
        }

        divide("root", "")

        nodes.indices.forEach { index ->
            val node = nodes[index]
            if (node.isBaseCase) {
                nodes[index] = node.copy(maxValue = node.elements.single())
            }
        }
        val baseCases = nodes.filter { it.isBaseCase }.sortedBy { it.low }
        if (input.size == 1) {
            val value = input.single()
            snapshot(
                phase = DivideConquerPhase.CONQUER,
                activeId = null,
                isTerminal = true,
                explanation = "2. CONQUER COMPLETE: ${valuesText(input)} is a base case (length = 1); maximum = $value.",
                badgeText = "CONQUER COMPLETE: MAX = $value"
            )
            return steps
        }

        val solvedLeaves = baseCases.joinToString(", ") { node ->
            "${node.elements.joinToString(",", prefix = "[", postfix = "]")}→${node.maxValue}"
        }
        snapshot(
            phase = DivideConquerPhase.CONQUER,
            activeId = null,
            explanation = "2. CONQUER: Base cases reached (length = 1). Subproblem results solved directly: $solvedLeaves.",
            badgeText = "CONQUER: BASE CASES SOLVED"
        )

        fun combine(nodeId: String): Int {
            val node = nodes.first { it.id == nodeId }
            if (node.isBaseCase) return node.maxValue!!

            val leftId = node.leftChildId!!
            val rightId = node.rightChildId!!
            val leftValue = combine(leftId)
            val rightValue = combine(rightId)
            val combinedValue = maxOf(leftValue, rightValue)
            val index = nodes.indexOfFirst { it.id == nodeId }
            val solvedNode = nodes[index].copy(maxValue = combinedValue)
            nodes[index] = solvedNode

            val isTerminal = nodeId == "root"
            val explanation = when {
                isTerminal -> "3. COMBINE COMPLETE: max(leftMax=$leftValue, rightMax=$rightValue) = $combinedValue. Original problem solved: Maximum value in ${valuesText(input)} is $combinedValue!"
                nodeId == "L1" -> "3. COMBINE: max(leftMax=$leftValue, rightMax=$rightValue) = $combinedValue for Left half ${valuesText(solvedNode.elements)}."
                nodeId == "R1" -> "3. COMBINE: max(leftMax=$leftValue, rightMax=$rightValue) = $combinedValue for Right half ${valuesText(solvedNode.elements)}."
                else -> "3. COMBINE: max(leftMax=$leftValue, rightMax=$rightValue) = $combinedValue for subproblem ${valuesText(solvedNode.elements)}."
            }
            val badge = when {
                isTerminal -> "FINAL RESULT: MAX = $combinedValue"
                nodeId == "L1" -> "COMBINE LEFT ${valuesText(solvedNode.elements)} → $combinedValue"
                nodeId == "R1" -> "COMBINE RIGHT ${valuesText(solvedNode.elements)} → $combinedValue"
                else -> "COMBINE ${valuesText(solvedNode.elements)} → $combinedValue"
            }

            snapshot(
                phase = DivideConquerPhase.COMBINE,
                activeId = nodeId,
                leftValue = leftValue,
                rightValue = rightValue,
                combinedValue = combinedValue,
                isTerminal = isTerminal,
                explanation = explanation,
                badgeText = badge
            )
            return combinedValue
        }

        combine("root")
        return steps
    }
}
