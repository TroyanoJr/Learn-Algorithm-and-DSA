package com.example.learndsandalgorithm.presentation.learn

internal enum class ExtractSiftDownEvent {
    START,
    SAVE_ROOT_MOVE_LAST,
    COMPARE_CHILDREN,
    SWAP_DOWN,
    EXTRACT_COMPLETE
}

internal data class ExtractSiftDownTraceStep(
    val heap: List<Int>,
    val extractedMin: Int? = null,
    val currentIndex: Int? = null,
    val smallerChildIndex: Int? = null,
    val swappedIndices: Pair<Int, Int>? = null,
    val event: ExtractSiftDownEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HeapExtractSiftDownTrace {

    fun stepsForExtract(
        initialHeap: List<Int> = listOf(1, 5, 2, 9, 7, 8, 3)
    ): List<ExtractSiftDownTraceStep> {
        val steps = mutableListOf<ExtractSiftDownTraceStep>()
        val heap = initialHeap.toMutableList()

        fun snapshot(
            extracted: Int? = null,
            currIdx: Int? = null,
            childIdx: Int? = null,
            swapped: Pair<Int, Int>? = null,
            event: ExtractSiftDownEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = ExtractSiftDownTraceStep(
            heap = heap.toList(),
            extractedMin = extracted,
            currentIndex = currIdx,
            smallerChildIndex = childIdx,
            swappedIndices = swapped,
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        val rootVal = heap[0]

        // Step 0: Ready
        steps.add(
            snapshot(
                extracted = rootVal,
                event = ExtractSiftDownEvent.START,
                explanation = "Extract Root (Min): Extracting minimum value $rootVal from Min-Heap [${initialHeap.joinToString(", ")}]. Steps: Save root → Move last item to root → Sift Down.",
                badgeText = "READY · EXTRACT ROOT ($rootVal)"
            )
        )

        // Step 1: Move last item to root & pop last slot
        val lastVal = heap.removeAt(heap.lastIndex)
        heap[0] = lastVal
        var i = 0

        steps.add(
            snapshot(
                extracted = rootVal,
                currIdx = i,
                event = ExtractSiftDownEvent.SAVE_ROOT_MOVE_LAST,
                explanation = "MOVE LAST ITEM TO ROOT: Saved root $rootVal. Moved last item $lastVal to index 0 and removed last array slot. Array: [${heap.joinToString(", ")}].",
                badgeText = "MOVED $lastVal TO ROOT"
            )
        )

        // Sift Down loop
        while (2 * i + 1 < heap.size) {
            var childIdx = 2 * i + 1
            val rightIdx = childIdx + 1

            if (rightIdx < heap.size && heap[rightIdx] < heap[childIdx]) {
                childIdx = rightIdx
            }

            val currVal = heap[i]
            val childVal = heap[childIdx]

            // Step A: Compare with smaller child
            steps.add(
                snapshot(
                    extracted = rootVal,
                    currIdx = i,
                    childIdx = childIdx,
                    event = ExtractSiftDownEvent.COMPARE_CHILDREN,
                    explanation = "COMPARE: Node[$i] ($currVal) vs Smaller Child[$childIdx] ($childVal). Since $currVal > $childVal, swap is required!",
                    badgeText = "COMPARE $currVal > CHILD $childVal"
                )
            )

            if (currVal <= childVal) {
                break
            }

            // Step B: Swap with smaller child
            heap[i] = childVal
            heap[childIdx] = currVal

            steps.add(
                snapshot(
                    extracted = rootVal,
                    currIdx = childIdx,
                    childIdx = childIdx,
                    swapped = Pair(i, childIdx),
                    event = ExtractSiftDownEvent.SWAP_DOWN,
                    explanation = "SIFT DOWN: Swapped $currVal with smaller child $childVal at index $childIdx.",
                    badgeText = "SWAPPED $currVal ↔ $childVal"
                )
            )

            i = childIdx
        }

        // Step Complete
        steps.add(
            snapshot(
                extracted = rootVal,
                currIdx = i,
                event = ExtractSiftDownEvent.EXTRACT_COMPLETE,
                isTerminal = true,
                explanation = "EXTRACT COMPLETE! Extracted minimum = $rootVal. Min-heap property fully restored: [${heap.joinToString(", ")}] in O(log N) time.",
                badgeText = "EXTRACTED $rootVal · HEAP [${heap.joinToString(", ")}]"
            )
        )

        return steps
    }
}
