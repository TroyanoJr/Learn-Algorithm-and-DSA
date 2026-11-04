package com.example.learndsandalgorithm.presentation.learn

internal enum class BuildHeapEvent {
    START,
    START_INDEX_SIFT,
    COMPARE_CHILDREN,
    SWAP_DOWN,
    INDEX_SIFT_DONE,
    BUILD_COMPLETE
}

internal data class BuildHeapTraceStep(
    val array: List<Int>,
    val activeParentIndex: Int? = null,
    val currentSiftIndex: Int? = null,
    val smallerChildIndex: Int? = null,
    val swappedIndices: Pair<Int, Int>? = null,
    val event: BuildHeapEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HeapBuildTrace {

    fun stepsForBuild(
        initialArr: List<Int> = listOf(9, 4, 7, 1, 0, 3)
    ): List<BuildHeapTraceStep> {
        val steps = mutableListOf<BuildHeapTraceStep>()
        val a = initialArr.toMutableList()
        val n = a.size
        val startParentIdx = n / 2 - 1 // 6 / 2 - 1 = 2

        fun snapshot(
            parentIdx: Int? = null,
            siftIdx: Int? = null,
            childIdx: Int? = null,
            swapped: Pair<Int, Int>? = null,
            event: BuildHeapEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BuildHeapTraceStep(
            array = a.toList(),
            activeParentIndex = parentIdx,
            currentSiftIndex = siftIdx,
            smallerChildIndex = childIdx,
            swappedIndices = swapped,
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = BuildHeapEvent.START,
                explanation = "Bottom-Up Build Heap: Building Min-Heap from unsorted array [${initialArr.joinToString(", ")}]. Starts at last non-leaf index $startParentIdx down to 0 in O(N) linear time.",
                badgeText = "READY · BUILD HEAP (O(N))"
            )
        )

        // Bottom-Up Loop from startParentIdx down to 0
        for (parentIdx in startParentIdx downTo 0) {
            steps.add(
                snapshot(
                    parentIdx = parentIdx,
                    siftIdx = parentIdx,
                    event = BuildHeapEvent.START_INDEX_SIFT,
                    explanation = "SIFT DOWN INDEX $parentIdx: Processing parent node at index $parentIdx (value ${a[parentIdx]}).",
                    badgeText = "SIFT DOWN INDEX $parentIdx (VAL ${a[parentIdx]})"
                )
            )

            var i = parentIdx
            while (2 * i + 1 < n) {
                var child = 2 * i + 1
                if (child + 1 < n && a[child + 1] < a[child]) {
                    child++
                }

                val currVal = a[i]
                val childVal = a[child]

                steps.add(
                    snapshot(
                        parentIdx = parentIdx,
                        siftIdx = i,
                        childIdx = child,
                        event = BuildHeapEvent.COMPARE_CHILDREN,
                        explanation = "Compare node[$i] ($currVal) with smaller child[$child] ($childVal).",
                        badgeText = "COMPARE $currVal vs CHILD $childVal"
                    )
                )

                if (currVal <= childVal) {
                    break
                }

                // Swap
                a[i] = childVal
                a[child] = currVal

                steps.add(
                    snapshot(
                        parentIdx = parentIdx,
                        siftIdx = child,
                        childIdx = child,
                        swapped = Pair(i, child),
                        event = BuildHeapEvent.SWAP_DOWN,
                        explanation = "Swapped $currVal with smaller child $childVal. Node $currVal moves down to index $child.",
                        badgeText = "SWAPPED $currVal ↔ $childVal"
                    )
                )

                i = child
            }

            steps.add(
                snapshot(
                    parentIdx = parentIdx,
                    siftIdx = i,
                    event = BuildHeapEvent.INDEX_SIFT_DONE,
                    explanation = "Index $parentIdx sift-down complete! Subtree rooted at $parentIdx now satisfies min-heap property.",
                    badgeText = "INDEX $parentIdx DONE"
                )
            )
        }

        // Step Complete
        steps.add(
            snapshot(
                event = BuildHeapEvent.BUILD_COMPLETE,
                isTerminal = true,
                explanation = "BOTTOM-UP BUILD HEAP COMPLETE! Resulting Min-Heap: [${a.joinToString(", ")}]. Overall time complexity: O(N) linear time.",
                badgeText = "BUILD COMPLETE [${a.joinToString(", ")}]"
            )
        )

        return steps
    }
}
