package com.example.learndsandalgorithm.presentation.learn

internal data class ArrayResizeStep(
    val stageLabel: String,
    val storageSlots: List<String?>,
    val preservedStorageSlots: List<String?>? = null,
    val highlightedIndices: Set<Int>,
    val explanationText: String
)

internal data class ArrayTraversalStep(
    val currentIndex: Int,
    val arrayData: List<String>,
    val visitedCount: Int,
    val explanationText: String,
    val badgeText: String,
    val isCompleted: Boolean
)

internal data class ArrayInsertDeleteStep(
    val stepIndex: Int,
    val displayItems: List<String>,
    val highlightedIndices: Set<Int>,
    val explanationText: String,
    val badgeText: String,
    val isCompleted: Boolean
)

internal object ArrayTrace {
    val defaultInitialResizeValues = listOf<String?>("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")
    val defaultTraversalData = listOf("10", "25", "42", "67", "91", "116", "135", "158", "176", "204")
    val defaultInsertDeleteData = listOf("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")

    fun resizeSteps(initialValues: List<String?> = defaultInitialResizeValues): List<ArrayResizeStep> {
        val fullArray = initialValues + "110"
        val copiedArray = fullArray + List(11) { null }
        return listOf(
            ArrayResizeStep(
                stageLabel = "CURRENT STORAGE",
                storageSlots = initialValues + null,
                preservedStorageSlots = null,
                highlightedIndices = setOf(10),
                explanationText = "Ten values use ten of eleven slots. Append 110 into the free slot at index 10."
            ),
            ArrayResizeStep(
                stageLabel = "CAPACITY FULL",
                storageSlots = fullArray,
                preservedStorageSlots = null,
                highlightedIndices = setOf(10),
                explanationText = "All eleven slots are occupied. Appending 120 now requires a larger array."
            ),
            ArrayResizeStep(
                stageLabel = "NEW STORAGE",
                storageSlots = List(22) { null },
                preservedStorageSlots = fullArray,
                highlightedIndices = emptySet(),
                explanationText = "Allocate new storage with 22 slots. The old eleven-value array is still intact."
            ),
            ArrayResizeStep(
                stageLabel = "11 VALUES COPIED",
                storageSlots = copiedArray,
                preservedStorageSlots = null,
                highlightedIndices = (0..10).toSet(),
                explanationText = "Copy each of the eleven existing values to the same index in the new storage."
            ),
            ArrayResizeStep(
                stageLabel = "120 APPENDED",
                storageSlots = fullArray + "120" + List(10) { null },
                preservedStorageSlots = null,
                highlightedIndices = setOf(11),
                explanationText = "Append 120 at index 11. Size is 12 and capacity is 22."
            )
        )
    }

    fun traversalSteps(arrayData: List<String> = defaultTraversalData): List<ArrayTraversalStep> {
        val totalElements = arrayData.size
        return buildList {
            // Unstarted / Ready step at -1
            add(
                ArrayTraversalStep(
                    currentIndex = -1,
                    arrayData = arrayData,
                    visitedCount = 0,
                    explanationText = "Start at index 0, then visit each element from left to right.",
                    badgeText = "READY",
                    isCompleted = false
                )
            )
            for (index in 0 until totalElements) {
                add(
                    ArrayTraversalStep(
                        currentIndex = index,
                        arrayData = arrayData,
                        visitedCount = index + 1,
                        explanationText = "Visiting index $index: value = ${arrayData[index]}.",
                        badgeText = "STEP ${index + 1} OF $totalElements",
                        isCompleted = false
                    )
                )
            }
            // Terminal step at totalElements
            add(
                ArrayTraversalStep(
                    currentIndex = totalElements,
                    arrayData = arrayData,
                    visitedCount = totalElements,
                    explanationText = "Traversal complete! All $totalElements elements have been visited.",
                    badgeText = "COMPLETED",
                    isCompleted = true
                )
            )
        }
    }

    fun insertDeleteSteps(
        operationMode: String = "INSERT",
        baseArray: List<String> = defaultInsertDeleteData
    ): List<ArrayInsertDeleteStep> {
        val insertionStates = buildList {
            val states = mutableListOf(baseArray)
            var working = (baseArray + "  ").toMutableList()
            states.add(working.toList())
            for (sourceIndex in baseArray.lastIndex downTo 2) {
                working = working.toMutableList().apply {
                    this[sourceIndex + 1] = this[sourceIndex]
                    this[sourceIndex] = "  "
                }
                states.add(working.toList())
            }
            states.add(working.toMutableList().apply { this[2] = "25" })
            addAll(states)
        }

        val deletionStates = buildList {
            val states = mutableListOf(baseArray)
            var working = baseArray.toMutableList().apply { this[1] = "  " }
            states.add(working.toList())
            for (gapIndex in 1 until baseArray.lastIndex) {
                working = working.toMutableList().apply {
                    this[gapIndex] = this[gapIndex + 1]
                    this[gapIndex + 1] = "  "
                }
                states.add(working.toList())
            }
            states.add(working.dropLast(1))
            addAll(states)
        }

        val operationStates = if (operationMode == "INSERT") insertionStates else deletionStates
        val totalSteps = operationStates.lastIndex

        return operationStates.mapIndexed { stepIndex, displayItems ->
            val explanationText = when (operationMode) {
                "INSERT" -> when (stepIndex) {
                    0 -> "Insert 25 at index 2 in the 10-value array. One free capacity slot is available at index 10."
                    1 -> "Create the free slot at index 10. Size remains 10; no values have moved yet."
                    in 2..9 -> {
                        val sourceIndex = 11 - stepIndex
                        "Shift ${baseArray[sourceIndex]} from index $sourceIndex to ${sourceIndex + 1}; the gap moves to index $sourceIndex."
                    }
                    else -> "Place 25 in the gap at index 2. The resulting array contains 11 elements."
                }
                else -> when (stepIndex) {
                    0 -> "Delete 20 at index 1. The following values will shift left."
                    1 -> "Remove 20 and leave a temporary gap at index 1."
                    in 2..9 -> {
                        val sourceIndex = stepIndex
                        "Shift ${baseArray[sourceIndex]} from index $sourceIndex to ${sourceIndex - 1}; the gap moves to index $sourceIndex."
                    }
                    else -> "Drop the unused trailing slot. The resulting array contains 9 elements."
                }
            }

            val highlightedIndices = when (operationMode) {
                "INSERT" -> when (stepIndex) {
                    0 -> setOf(2)
                    1 -> setOf(10)
                    in 2..9 -> {
                        val sourceIndex = 11 - stepIndex
                        setOf(sourceIndex, sourceIndex + 1)
                    }
                    else -> setOf(2)
                }
                else -> when (stepIndex) {
                    0, 1 -> setOf(1)
                    in 2..9 -> setOf(stepIndex - 1, stepIndex)
                    else -> setOf(8)
                }
            }

            val badgeText = when (stepIndex) {
                0 -> "READY"
                totalSteps -> "COMPLETED"
                else -> "STEP $stepIndex OF $totalSteps"
            }

            ArrayInsertDeleteStep(
                stepIndex = stepIndex,
                displayItems = displayItems,
                highlightedIndices = highlightedIndices,
                explanationText = explanationText,
                badgeText = badgeText,
                isCompleted = stepIndex >= totalSteps
            )
        }
    }
}
