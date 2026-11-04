package com.example.learndsandalgorithm.presentation.learn

internal enum class FullMergeEvent {
    START,
    DIVIDE,
    BASE_CASES,
    MERGE_LEVEL,
    COMPLETE
}

internal data class MergeTreeNode(
    val level: Int,
    val elements: List<Int>,
    val isSorted: Boolean = false
)

internal data class MergeSortFullTraceStep(
    val array: List<Int>,
    val level0: List<MergeTreeNode>,
    val level1: List<MergeTreeNode>,
    val level2: List<MergeTreeNode>,
    val level3: List<MergeTreeNode>,
    val activeLevel: Int,
    val auxiliaryBuffer: List<Int>,
    val event: FullMergeEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object MergeSortFullTrace {

    fun stepsForFullSort(
        arr: List<Int> = listOf(38, 27, 43, 3, 9, 82, 10)
    ): List<MergeSortFullTraceStep> {
        val steps = mutableListOf<MergeSortFullTraceStep>()

        // Level 0: [38, 27, 43, 3, 9, 82, 10]
        // Level 1: [38, 27, 43], [3, 9, 82, 10]
        // Level 2: [38, 27], [43], [3, 9], [82, 10]
        // Level 3: [38], [27], [43], [3], [9], [82], [10]

        fun snapshot(
            level0: List<MergeTreeNode>,
            level1: List<MergeTreeNode>,
            level2: List<MergeTreeNode>,
            level3: List<MergeTreeNode>,
            activeLevel: Int,
            auxBuffer: List<Int>,
            event: FullMergeEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = MergeSortFullTraceStep(
            array = arr,
            level0 = level0,
            level1 = level1,
            level2 = level2,
            level3 = level3,
            activeLevel = activeLevel,
            auxiliaryBuffer = auxBuffer,
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                level0 = listOf(MergeTreeNode(0, arr)),
                level1 = emptyList(),
                level2 = emptyList(),
                level3 = emptyList(),
                activeLevel = 0,
                auxBuffer = emptyList(),
                event = FullMergeEvent.START,
                explanation = "Full Merge Sort: Recursively divides array [${arr.joinToString(", ")}] down to single-element base cases, then merges sorted sub-lists back up.",
                badgeText = "READY · INPUT [${arr.joinToString(", ")}]"
            )
        )

        // Step 1: Divide Level 0 -> Level 1 ([38, 27, 43] & [3, 9, 82, 10])
        val l1_1 = MergeTreeNode(1, listOf(38, 27, 43))
        val l1_2 = MergeTreeNode(1, listOf(3, 9, 82, 10))

        steps.add(
            snapshot(
                level0 = listOf(MergeTreeNode(0, arr)),
                level1 = listOf(l1_1, l1_2),
                level2 = emptyList(),
                level3 = emptyList(),
                activeLevel = 1,
                auxBuffer = emptyList(),
                event = FullMergeEvent.DIVIDE,
                explanation = "DIVIDE Level 1: Split array into Left [38, 27, 43] and Right [3, 9, 82, 10].",
                badgeText = "DIVIDE → LEVEL 1"
            )
        )

        // Step 2: Divide Level 1 -> Level 2 ([38, 27], [43], [3, 9], [82, 10])
        val l2_1 = MergeTreeNode(2, listOf(38, 27))
        val l2_2 = MergeTreeNode(2, listOf(43))
        val l2_3 = MergeTreeNode(2, listOf(3, 9))
        val l2_4 = MergeTreeNode(2, listOf(82, 10))

        steps.add(
            snapshot(
                level0 = listOf(MergeTreeNode(0, arr)),
                level1 = listOf(l1_1, l1_2),
                level2 = listOf(l2_1, l2_2, l2_3, l2_4),
                level3 = emptyList(),
                activeLevel = 2,
                auxBuffer = emptyList(),
                event = FullMergeEvent.DIVIDE,
                explanation = "DIVIDE Level 2: Split sub-lists into [38, 27], [43], [3, 9], [82, 10].",
                badgeText = "DIVIDE → LEVEL 2"
            )
        )

        // Step 3: Base Cases Level 3 ([38], [27], [43], [3], [9], [82], [10])
        val l3_nodes = listOf(
            MergeTreeNode(3, listOf(38), true),
            MergeTreeNode(3, listOf(27), true),
            MergeTreeNode(3, listOf(43), true),
            MergeTreeNode(3, listOf(3), true),
            MergeTreeNode(3, listOf(9), true),
            MergeTreeNode(3, listOf(82), true),
            MergeTreeNode(3, listOf(10), true)
        )

        steps.add(
            snapshot(
                level0 = listOf(MergeTreeNode(0, arr)),
                level1 = listOf(l1_1, l1_2),
                level2 = listOf(l2_1, l2_2, l2_3, l2_4),
                level3 = l3_nodes,
                activeLevel = 3,
                auxBuffer = emptyList(),
                event = FullMergeEvent.BASE_CASES,
                explanation = "BASE CASES Level 3: All sub-lists reached length 1 ([38], [27], [43], [3], [9], [82], [10]). Single elements are inherently sorted!",
                badgeText = "BASE CASES (LENGTH 1)"
            )
        )

        // Step 4: Merge Level 3 -> Level 2 sorted ([27, 38], [43], [3, 9], [10, 82])
        val l2_sorted = listOf(
            MergeTreeNode(2, listOf(27, 38), true),
            MergeTreeNode(2, listOf(43), true),
            MergeTreeNode(2, listOf(3, 9), true),
            MergeTreeNode(2, listOf(10, 82), true)
        )

        steps.add(
            snapshot(
                level0 = listOf(MergeTreeNode(0, arr)),
                level1 = listOf(l1_1, l1_2),
                level2 = l2_sorted,
                level3 = l3_nodes,
                activeLevel = 2,
                auxBuffer = listOf(27, 38, 43, 3, 9, 10, 82),
                event = FullMergeEvent.MERGE_LEVEL,
                explanation = "MERGE Level 2: Merged adjacent pairs into sorted sub-lists: [27, 38], [43], [3, 9], [10, 82]. Uses O(N) auxiliary buffer space.",
                badgeText = "MERGE LEVEL 2"
            )
        )

        // Step 5: Merge Level 2 -> Level 1 sorted ([27, 38, 43] & [3, 9, 10, 82])
        val l1_sorted = listOf(
            MergeTreeNode(1, listOf(27, 38, 43), true),
            MergeTreeNode(1, listOf(3, 9, 10, 82), true)
        )

        steps.add(
            snapshot(
                level0 = listOf(MergeTreeNode(0, arr)),
                level1 = l1_sorted,
                level2 = l2_sorted,
                level3 = l3_nodes,
                activeLevel = 1,
                auxBuffer = listOf(27, 38, 43, 3, 9, 10, 82),
                event = FullMergeEvent.MERGE_LEVEL,
                explanation = "MERGE Level 1: Merged halves into sorted [27, 38, 43] and [3, 9, 10, 82]. O(N) work performed at this level.",
                badgeText = "MERGE LEVEL 1"
            )
        )

        // Step 6: Final Merge Level 0 sorted ([3, 9, 10, 27, 38, 43, 82])
        val l0_sorted = listOf(MergeTreeNode(0, listOf(3, 9, 10, 27, 38, 43, 82), true))

        steps.add(
            snapshot(
                level0 = l0_sorted,
                level1 = l1_sorted,
                level2 = l2_sorted,
                level3 = l3_nodes,
                activeLevel = 0,
                auxBuffer = listOf(3, 9, 10, 27, 38, 43, 82),
                event = FullMergeEvent.COMPLETE,
                isTerminal = true,
                explanation = "FULL MERGE SORT COMPLETE! Array fully sorted to [3, 9, 10, 27, 38, 43, 82]. Total Complexity: O(N log N) time, O(N) auxiliary space.",
                badgeText = "SORTED: [3, 9, 10, 27, 38, 43, 82]"
            )
        )

        return steps
    }
}
