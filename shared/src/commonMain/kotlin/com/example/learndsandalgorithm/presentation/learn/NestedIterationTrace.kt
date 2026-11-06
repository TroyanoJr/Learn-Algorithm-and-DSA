package com.example.learndsandalgorithm.presentation.learn

internal enum class NestedIterationAction {
    INIT,
    OUTER_PASS,
    INNER_PASS,
    INNER_RESET,
    FINISHED
}

internal data class NestedIterationStep(
    val outerLimit: Int = 2,
    val innerLimit: Int = 3,
    val i: Int = 1,
    val j: Int = 1,
    val printedPairs: List<Pair<Int, Int>> = emptyList(),
    val totalCount: Int = 0,
    val action: NestedIterationAction = NestedIterationAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object NestedIterationTrace {

    fun createTrace(
        outerLimit: Int = 2,
        innerLimit: Int = 3
    ): List<NestedIterationStep> {
        val steps = mutableListOf<NestedIterationStep>()
        val pairs = mutableListOf<Pair<Int, Int>>()

        fun snapshot(
            outerI: Int,
            innerJ: Int,
            act: NestedIterationAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = NestedIterationStep(
            outerLimit = outerLimit,
            innerLimit = innerLimit,
            i = outerI,
            j = innerJ,
            printedPairs = pairs.toList(),
            totalCount = pairs.size,
            action = act,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                outerI = 1,
                innerJ = 1,
                act = NestedIterationAction.INIT,
                explanation = "Nested Iteration: Outer loop i in [1..$outerLimit], Inner loop j in [1..$innerLimit]. Total iterations = $outerLimit x $innerLimit = ${outerLimit * innerLimit}.",
                badgeText = "READY · NESTED LOOPS ($outerLimit x $innerLimit)"
            )
        )

        for (i in 1..outerLimit) {
            steps.add(
                snapshot(
                    outerI = i,
                    innerJ = 1,
                    act = NestedIterationAction.OUTER_PASS,
                    explanation = "Outer Loop Pass i = $i: Reset inner loop j = 1 to start new inner pass.",
                    badgeText = "OUTER PASS i = $i (RESET j = 1)"
                )
            )

            for (j in 1..innerLimit) {
                pairs.add(Pair(i, j))

                steps.add(
                    snapshot(
                        outerI = i,
                        innerJ = j,
                        act = NestedIterationAction.INNER_PASS,
                        explanation = "Inner Loop Pass (i = $i, j = $j): Printed coordinate pair ($i, $j). Total pairs: ${pairs.size}.",
                        badgeText = "INNER PASS ($i, $j) -> PAIR #${pairs.size}"
                    )
                )
            }
        }

        // FINISHED
        steps.add(
            snapshot(
                outerI = outerLimit,
                innerJ = innerLimit,
                act = NestedIterationAction.FINISHED,
                explanation = "Nested iteration complete! Generated all ${pairs.size} coordinate pairs: $pairs. Time complexity: O(M * N).",
                badgeText = "COMPLETE: ${pairs.size} PAIRS GENERATED",
                isTerminal = true
            )
        )

        return steps
    }
}
