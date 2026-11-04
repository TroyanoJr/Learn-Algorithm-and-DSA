package com.example.learndsandalgorithm.presentation.learn

internal enum class InsertSiftUpEvent {
    START,
    APPEND_VALUE,
    COMPARE_PARENT,
    SWAP_UP,
    SIFT_UP_COMPLETE
}

internal data class InsertSiftUpTraceStep(
    val heap: List<Int>,
    val insertedValue: Int,
    val currentIndex: Int? = null,
    val parentIndex: Int? = null,
    val swappedIndices: Pair<Int, Int>? = null,
    val event: InsertSiftUpEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HeapInsertSiftUpTrace {

    fun stepsForInsert(
        initialHeap: List<Int> = listOf(2, 5, 3, 9, 7, 8),
        valueToInsert: Int = 1
    ): List<InsertSiftUpTraceStep> {
        val steps = mutableListOf<InsertSiftUpTraceStep>()
        val heap = initialHeap.toMutableList()

        fun snapshot(
            currIdx: Int? = null,
            parentIdx: Int? = null,
            swapped: Pair<Int, Int>? = null,
            event: InsertSiftUpEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = InsertSiftUpTraceStep(
            heap = heap.toList(),
            insertedValue = valueToInsert,
            currentIndex = currIdx,
            parentIndex = parentIdx,
            swappedIndices = swapped,
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = InsertSiftUpEvent.START,
                explanation = "Heap Insertion: Inserting value $valueToInsert into Min-Heap [${initialHeap.joinToString(", ")}]. 2 Steps: Append at end → Sift Up.",
                badgeText = "READY · INSERT $valueToInsert"
            )
        )

        // Step 1: Append value
        heap.add(valueToInsert)
        var i = heap.lastIndex

        steps.add(
            snapshot(
                currIdx = i,
                event = InsertSiftUpEvent.APPEND_VALUE,
                explanation = "APPEND: Placed $valueToInsert at next available array position index $i. Next, sift up along ancestor path.",
                badgeText = "APPENDED $valueToInsert AT INDEX $i"
            )
        )

        // Sift Up loop
        while (i > 0) {
            val parentIdx = (i - 1) / 2
            val parentVal = heap[parentIdx]
            val currVal = heap[i]

            // Step A: Compare with Parent
            steps.add(
                snapshot(
                    currIdx = i,
                    parentIdx = parentIdx,
                    event = InsertSiftUpEvent.COMPARE_PARENT,
                    explanation = "COMPARE: Node[$i] ($currVal) < Parent[$parentIdx] ($parentVal)? Since $currVal < $parentVal, swap is required!",
                    badgeText = "COMPARE $currVal < PARENT $parentVal"
                )
            )

            if (currVal >= parentVal) {
                break
            }

            // Step B: Swap with Parent
            heap[i] = parentVal
            heap[parentIdx] = currVal

            steps.add(
                snapshot(
                    currIdx = parentIdx,
                    parentIdx = parentIdx,
                    swapped = Pair(i, parentIdx),
                    event = InsertSiftUpEvent.SWAP_UP,
                    explanation = "SIFT UP: Swapped $currVal with parent $parentVal. Node $currVal moves up to index $parentIdx.",
                    badgeText = "SWAPPED $currVal ↔ $parentVal"
                )
            )

            i = parentIdx
        }

        // Step 4: Sift Up Complete
        steps.add(
            snapshot(
                currIdx = i,
                event = InsertSiftUpEvent.SIFT_UP_COMPLETE,
                isTerminal = true,
                explanation = "SIFT UP COMPLETE! Reached root (index 0). Min-heap property fully restored: [${heap.joinToString(", ")}] in O(log N) time.",
                badgeText = "INSERT COMPLETE [${heap.joinToString(", ")}]"
            )
        )

        return steps
    }
}
