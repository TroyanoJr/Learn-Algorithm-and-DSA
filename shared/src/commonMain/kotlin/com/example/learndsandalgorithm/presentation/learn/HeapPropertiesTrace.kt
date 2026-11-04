package com.example.learndsandalgorithm.presentation.learn

internal enum class HeapPropertyTopic {
    MIN_HEAP_STRUCTURE,
    INDEX_MAPPING,
    MAX_HEAP_STRUCTURE
}

internal data class HeapPropertiesTraceStep(
    val topic: HeapPropertyTopic,
    val array: List<Int>,
    val inspectedIndex: Int? = null,
    val parentIndex: Int? = null,
    val leftChildIndex: Int? = null,
    val rightChildIndex: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HeapPropertiesTrace {

    fun stepsForTopic(topic: HeapPropertyTopic): List<HeapPropertiesTraceStep> {
        return when (topic) {
            HeapPropertyTopic.MIN_HEAP_STRUCTURE -> generateMinHeapSteps()
            HeapPropertyTopic.INDEX_MAPPING -> generateIndexMappingSteps()
            HeapPropertyTopic.MAX_HEAP_STRUCTURE -> generateMaxHeapSteps()
        }
    }

    private fun generateMinHeapSteps(): List<HeapPropertiesTraceStep> {
        val steps = mutableListOf<HeapPropertiesTraceStep>()
        val minHeap = listOf(2, 5, 3, 9, 7, 8)

        fun snapshot(
            inspected: Int? = null,
            parent: Int? = null,
            left: Int? = null,
            right: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = HeapPropertiesTraceStep(
            topic = HeapPropertyTopic.MIN_HEAP_STRUCTURE,
            array = minHeap,
            inspectedIndex = inspected,
            parentIndex = parent,
            leftChildIndex = left,
            rightChildIndex = right,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, null, null, null, false, "Min-Heap Property: Every parent node is <= its children. Root [0] holds minimum value 2.", "READY · MIN-HEAP"))
        steps.add(snapshot(0, null, 1, 2, false, "Inspect Root [0]=2: Left child [1]=5, Right child [2]=3. 2 <= 5 and 2 <= 3. Valid!", "ROOT [0]=2 <= CHILDREN"))
        steps.add(snapshot(1, 0, 3, 4, false, "Inspect Node [1]=5: Left child [3]=9, Right child [4]=7. 5 <= 9 and 5 <= 7. Valid!", "NODE [1]=5 <= CHILDREN"))
        steps.add(snapshot(2, 0, 5, null, true, "Inspect Node [2]=3: Left child [5]=8, no right child. 3 <= 8. Min-heap property holds for all nodes!", "MIN-HEAP CONFIRMED"))

        return steps
    }

    private fun generateIndexMappingSteps(): List<HeapPropertiesTraceStep> {
        val steps = mutableListOf<HeapPropertiesTraceStep>()
        val arr = listOf(2, 5, 3, 9, 7, 8)

        fun snapshot(
            inspected: Int,
            parent: Int?,
            left: Int?,
            right: Int?,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = HeapPropertiesTraceStep(
            topic = HeapPropertyTopic.INDEX_MAPPING,
            array = arr,
            inspectedIndex = inspected,
            parentIndex = parent,
            leftChildIndex = left,
            rightChildIndex = right,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(1, 0, 3, 4, false, "Index Mapping for i=1 (value 5): Parent = (1-1)/2 = 0 (val 2). Left = 2*1+1 = 3 (val 9). Right = 2*1+2 = 4 (val 7).", "MAPPING FOR INDEX 1"))
        steps.add(snapshot(2, 0, 5, 6, true, "Index Mapping for i=2 (value 3): Parent = (2-1)/2 = 0 (val 2). Left = 2*2+1 = 5 (val 8). Right = 6 (out of bounds).", "MAPPING FOR INDEX 2"))

        return steps
    }

    private fun generateMaxHeapSteps(): List<HeapPropertiesTraceStep> {
        val steps = mutableListOf<HeapPropertiesTraceStep>()
        val maxHeap = listOf(9, 7, 8, 5, 2, 3)

        fun snapshot(
            inspected: Int? = null,
            parent: Int? = null,
            left: Int? = null,
            right: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = HeapPropertiesTraceStep(
            topic = HeapPropertyTopic.MAX_HEAP_STRUCTURE,
            array = maxHeap,
            inspectedIndex = inspected,
            parentIndex = parent,
            leftChildIndex = left,
            rightChildIndex = right,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, null, null, null, false, "Max-Heap Property: Every parent node is >= its children. Root [0] holds maximum value 9.", "READY · MAX-HEAP"))
        steps.add(snapshot(0, null, 1, 2, true, "Inspect Root [0]=9: Left child [1]=7, Right child [2]=8. 9 >= 7 and 9 >= 8. Max-heap property verified!", "MAX-HEAP CONFIRMED"))

        return steps
    }
}
