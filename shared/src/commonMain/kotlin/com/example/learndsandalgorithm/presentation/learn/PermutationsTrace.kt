package com.example.learndsandalgorithm.presentation.learn

internal enum class PermutationAction {
    INIT,
    SWAP_CHOICE,
    RECURSE,
    SOLUTION_FOUND,
    BACKTRACK_SWAP,
    FINISHED
}

internal data class PermutationStep(
    val array: List<Int> = listOf(1, 2, 3),
    val startIndex: Int = 0,
    val swapI: Int? = null,
    val swapJ: Int? = null,
    val currentPermutation: List<Int> = listOf(1, 2, 3),
    val completedPermutations: List<List<Int>> = emptyList(),
    val action: PermutationAction = PermutationAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object PermutationsTrace {

    fun createTrace(initialArray: List<Int> = listOf(1, 2, 3)): List<PermutationStep> {
        val steps = mutableListOf<PermutationStep>()
        val arr = initialArray.toMutableList()
        val completed = mutableListOf<List<Int>>()

        fun snapshot(
            startIndex: Int,
            swapI: Int?,
            swapJ: Int?,
            action: PermutationAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = PermutationStep(
            array = initialArray,
            startIndex = startIndex,
            swapI = swapI,
            swapJ = swapJ,
            currentPermutation = arr.toList(),
            completedPermutations = completed.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                startIndex = 0,
                swapI = null,
                swapJ = null,
                action = PermutationAction.INIT,
                explanation = "Permutations: Generating all arrangements of $initialArray via swapping and state restoration backtracking. Total = ${initialArray.size}! arrangements.",
                badgeText = "READY · $initialArray (TOTAL ${initialArray.size}!)"
            )
        )

        fun permute(start: Int) {
            if (start == arr.size - 1) {
                completed.add(arr.toList())
                steps.add(
                    snapshot(
                        startIndex = start,
                        swapI = null,
                        swapJ = null,
                        action = PermutationAction.SOLUTION_FOUND,
                        explanation = "Base Case Reached: Fixed all positions. Recorded permutation: ${arr.toList()}.",
                        badgeText = "PERMUTATION FOUND: ${arr.toList()}"
                    )
                )
                return
            }

            for (i in start until arr.size) {
                // Swap choice
                val temp = arr[start]
                arr[start] = arr[i]
                arr[i] = temp

                steps.add(
                    snapshot(
                        startIndex = start,
                        swapI = start,
                        swapJ = i,
                        action = PermutationAction.SWAP_CHOICE,
                        explanation = "Fix element at index $start by swapping arr[$start] (${arr[i]}) with arr[$i] (${arr[start]}). Array state: ${arr.toList()}.",
                        badgeText = "SWAP arr[$start] ↔ arr[$i] -> ${arr.toList()}"
                    )
                )

                permute(start + 1)

                // Backtrack swap
                val temp2 = arr[start]
                arr[start] = arr[i]
                arr[i] = temp2

                steps.add(
                    snapshot(
                        startIndex = start,
                        swapI = start,
                        swapJ = i,
                        action = PermutationAction.BACKTRACK_SWAP,
                        explanation = "UNDO (Backtrack): Swap back arr[$start] ↔ arr[$i] to restore array order to ${arr.toList()} before testing next choice.",
                        badgeText = "BACKTRACK SWAP -> RESTORE ${arr.toList()}"
                    )
                )
            }
        }

        permute(0)

        // FINISHED
        steps.add(
            snapshot(
                startIndex = 0,
                swapI = null,
                swapJ = null,
                action = PermutationAction.FINISHED,
                explanation = "Permutations complete! Total ${completed.size} arrangements generated: $completed. Backtracking fully restored original array.",
                badgeText = "COMPLETE: ${completed.size} PERMUTATIONS GENERATED",
                isTerminal = true
            )
        )

        return steps
    }
}
