package com.example.learndsandalgorithm.presentation.learn

internal enum class BacktrackingFundamentalsAction {
    INIT,
    CHOOSE_0,
    CHOOSE_1,
    SOLUTION_FOUND,
    BACKTRACK,
    FINISHED
}

internal data class BacktrackingFundamentalsStep(
    val targetLength: Int = 2,
    val currentString: String = "",
    val decisionLevel: Int = 0,
    val generatedStrings: List<String> = emptyList(),
    val action: BacktrackingFundamentalsAction = BacktrackingFundamentalsAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BacktrackingFundamentalsTrace {

    fun createTrace(targetLength: Int = 2): List<BacktrackingFundamentalsStep> {
        val steps = mutableListOf<BacktrackingFundamentalsStep>()
        val solutions = mutableListOf<String>()

        fun snapshot(
            current: String,
            level: Int,
            action: BacktrackingFundamentalsAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = BacktrackingFundamentalsStep(
            targetLength = targetLength,
            currentString = current,
            decisionLevel = level,
            generatedStrings = solutions.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                current = "",
                level = 0,
                action = BacktrackingFundamentalsAction.INIT,
                explanation = "Backtracking Fundamentals: Exploring state-space tree to generate all binary strings of length $targetLength using Choice -> Explore -> Undo.",
                badgeText = "READY · STATE-SPACE TREE (LENGTH $targetLength)"
            )
        )

        fun solve(current: String, level: Int) {
            if (current.length == targetLength) {
                solutions.add(current)
                steps.add(
                    snapshot(
                        current = current,
                        level = level,
                        action = BacktrackingFundamentalsAction.SOLUTION_FOUND,
                        explanation = "Base Case Reached: Length $targetLength string '$current' is complete. Added to solution list.",
                        badgeText = "SOLUTION FOUND: '$current'"
                    )
                )
                return
            }

            // Choice 1: Append '0' and explore
            steps.add(
                snapshot(
                    current = current + "0",
                    level = level + 1,
                    action = BacktrackingFundamentalsAction.CHOOSE_0,
                    explanation = "Level ${level + 1}: CHOOSE '0'. Current candidate string: '${current + "0"}'. Recursively explore deeper branch.",
                    badgeText = "CHOOSE '0' -> '${current + "0"}'"
                )
            )
            solve(current + "0", level + 1)

            // Backtrack from '0'
            steps.add(
                snapshot(
                    current = current,
                    level = level,
                    action = BacktrackingFundamentalsAction.BACKTRACK,
                    explanation = "UNDO (Backtrack): Revert choice '0'. Restore candidate to '$current' to try alternative choice at level ${level + 1}.",
                    badgeText = "BACKTRACK TO '$current'"
                )
            )

            // Choice 2: Append '1' and explore
            steps.add(
                snapshot(
                    current = current + "1",
                    level = level + 1,
                    action = BacktrackingFundamentalsAction.CHOOSE_1,
                    explanation = "Level ${level + 1}: CHOOSE '1'. Current candidate string: '${current + "1"}'. Recursively explore deeper branch.",
                    badgeText = "CHOOSE '1' -> '${current + "1"}'"
                )
            )
            solve(current + "1", level + 1)

            // Backtrack from '1'
            steps.add(
                snapshot(
                    current = current,
                    level = level,
                    action = BacktrackingFundamentalsAction.BACKTRACK,
                    explanation = "UNDO (Backtrack): Revert choice '1'. Restore candidate to '$current'.",
                    badgeText = "BACKTRACK TO '$current'"
                )
            )
        }

        solve("", 0)

        // FINISHED
        steps.add(
            snapshot(
                current = "",
                level = 0,
                action = BacktrackingFundamentalsAction.FINISHED,
                explanation = "Backtracking search complete! Generated ${solutions.size} binary strings: $solutions. State-space tree fully traversed.",
                badgeText = "COMPLETE: ${solutions.size} STRINGS GENERATED",
                isTerminal = true
            )
        )

        return steps
    }
}
