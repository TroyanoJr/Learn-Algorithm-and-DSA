package com.example.learndsandalgorithm.presentation.learn

internal enum class HeapSortEvent {
    START,
    BUILD_MAX_HEAP,
    SWAP_ROOT_TO_END,
    SIFT_DOWN_ROOT,
    SORT_COMPLETE
}

internal data class HeapSortTraceStep(
    val array: List<Int>,
    val heapBoundaryIndex: Int,
    val currentRootIndex: Int? = null,
    val largerChildIndex: Int? = null,
    val swappedIndices: Pair<Int, Int>? = null,
    val event: HeapSortEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HeapSortTrace {

    fun stepsForHeapSort(
        initialArr: List<Int> = listOf(4, 1, 3, 2)
    ): List<HeapSortTraceStep> {
        val steps = mutableListOf<HeapSortTraceStep>()
        val a = initialArr.toMutableList()
        val n = a.size

        fun snapshot(
            boundary: Int,
            rootIdx: Int? = null,
            childIdx: Int? = null,
            swapped: Pair<Int, Int>? = null,
            event: HeapSortEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = HeapSortTraceStep(
            array = a.toList(),
            heapBoundaryIndex = boundary,
            currentRootIndex = rootIdx,
            largerChildIndex = childIdx,
            swappedIndices = swapped,
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                boundary = n - 1,
                event = HeapSortEvent.START,
                explanation = "Heap Sort: Sorts [${initialArr.joinToString(", ")}] in-place in O(N log N) time and O(1) space using a Max-Heap.",
                badgeText = "READY · HEAP SORT"
            )
        )

        // Step 1: Build Max-Heap
        // For [4, 1, 3, 2] -> Max-Heap is [4, 2, 3, 1]
        // Simple bottom-up max heapify
        for (i in (n / 2 - 1) downTo 0) {
            siftDownMax(a, i, n)
        }

        steps.add(
            snapshot(
                boundary = n - 1,
                event = HeapSortEvent.BUILD_MAX_HEAP,
                explanation = "BUILD MAX-HEAP: Reconfigured array into Max-Heap [${a.joinToString(", ")}]. Maximum element ${a[0]} is now at root index 0.",
                badgeText = "MAX-HEAP BUILT [${a.joinToString(", ")}]"
            )
        )

        // Step 2: Extraction loop from end down to 1
        var boundary = n - 1
        while (boundary > 0) {
            val rootMax = a[0]
            val lastItem = a[boundary]

            // Swap root with end of heap
            a[0] = lastItem
            a[boundary] = rootMax

            steps.add(
                snapshot(
                    boundary = boundary - 1,
                    swapped = Pair(0, boundary),
                    event = HeapSortEvent.SWAP_ROOT_TO_END,
                    explanation = "SWAP MAX: Swapped max element $rootMax at root [0] with last item $lastItem at index $boundary. Sorted suffix grows from right.",
                    badgeText = "PLACED MAX $rootMax AT [$boundary]"
                )
            )

            // Sift down new root within shrinking boundary
            val activeBoundary = boundary
            var i = 0
            while (2 * i + 1 < activeBoundary) {
                var child = 2 * i + 1
                if (child + 1 < activeBoundary && a[child + 1] > a[child]) {
                    child++
                }

                if (a[i] >= a[child]) {
                    break
                }

                val temp = a[i]
                a[i] = a[child]
                a[child] = temp

                steps.add(
                    snapshot(
                        boundary = boundary - 1,
                        rootIdx = child,
                        childIdx = child,
                        swapped = Pair(i, child),
                        event = HeapSortEvent.SIFT_DOWN_ROOT,
                        explanation = "SIFT DOWN ROOT: Swapped $temp with larger child ${a[i]} at index $child.",
                        badgeText = "SIFT DOWN ROOT ↔ $child"
                    )
                )

                i = child
            }

            boundary--
        }

        // Complete
        steps.add(
            snapshot(
                boundary = 0,
                event = HeapSortEvent.SORT_COMPLETE,
                isTerminal = true,
                explanation = "HEAP SORT COMPLETE! Array fully sorted ascending: [${a.joinToString(", ")}]. Total complexity: O(N log N) time, O(1) auxiliary space.",
                badgeText = "SORTED: [${a.joinToString(", ")}]"
            )
        )

        return steps
    }

    private fun siftDownMax(a: MutableList<Int>, iStart: Int, size: Int) {
        var i = iStart
        while (2 * i + 1 < size) {
            var child = 2 * i + 1
            if (child + 1 < size && a[child + 1] > a[child]) {
                child++
            }
            if (a[i] >= a[child]) break
            val temp = a[i]
            a[i] = a[child]
            a[child] = temp
            i = child
        }
    }
}
