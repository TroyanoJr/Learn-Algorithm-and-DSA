package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms a sequence of Heap trace models into
 * abstract [VisualizationScene] models.
 *
 * Tracks persistent element identities (`elem:0`..`elem:N-1`) across array index swaps,
 * generating synchronized `heap_tree` and `heap_array` sub-scenes for each step.
 */
internal object HeapSceneBuilder {

    fun buildScenes(steps: List<BuildHeapTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val firstStep = steps.first()
        require(firstStep.event == BuildHeapEvent.START) {
            "HeapSceneBuilder requires a complete trace starting with BuildHeapEvent.START"
        }
        require(firstStep.swappedIndices == null) {
            "HeapSceneBuilder requires the initial START step to have null swappedIndices"
        }

        val initialArray = firstStep.array
        val n = initialArray.size

        // Track persistent element IDs per slot: identityAtSlot[i] holds elementId currently at index i
        val identityAtSlot = Array(n) { i -> "elem:$i" }

        return steps.mapIndexed { stepIndex, step ->
            // Update identity permutation BEFORE building scene for SWAP_DOWN event
            if (step.event == BuildHeapEvent.SWAP_DOWN && step.swappedIndices != null) {
                val (i, j) = step.swappedIndices
                if (i in 0 until n && j in 0 until n) {
                    val temp = identityAtSlot[i]
                    identityAtSlot[i] = identityAtSlot[j]
                    identityAtSlot[j] = temp
                }
            }

            buildSceneForStep(
                stepIndex = stepIndex,
                step = step,
                identityAtSlot = identityAtSlot
            )
        }
    }

    private fun buildSceneForStep(
        stepIndex: Int,
        step: BuildHeapTraceStep,
        identityAtSlot: Array<String>
    ): VisualizationScene {
        val n = step.array.size

        val treeEntities = mutableListOf<VisualEntity>()
        val arrayEntities = mutableListOf<VisualEntity>()
        val treeConnections = mutableListOf<VisualConnection>()

        val swapped = step.swappedIndices

        for (i in 0 until n) {
            val elementId = identityAtSlot.getOrElse(i) { "elem:$i" }
            val valStr = step.array.getOrElse(i) { 0 }.toString()

            val isSwapped = swapped != null && (swapped.first == i || swapped.second == i)
            val isCurrentSift = step.currentSiftIndex == i || step.activeParentIndex == i
            val isSmallerChild = step.smallerChildIndex == i

            val entityState = when {
                isSwapped -> EntityVisualState.TARGET
                isCurrentSift -> EntityVisualState.ACTIVE
                isSmallerChild -> EntityVisualState.TARGET
                else -> EntityVisualState.DEFAULT
            }

            val treeBadge = when {
                step.activeParentIndex == i && step.currentSiftIndex == i -> "Sift P:$i"
                step.smallerChildIndex == i -> "Min C:$i"
                else -> null
            }

            // Tree Entity
            treeEntities.add(
                VisualEntity(
                    visualKey = "heap_tree:$elementId",
                    elementId = elementId,
                    dataValue = valStr,
                    slotKey = "tree_slot:$i",
                    badgeLabel = treeBadge,
                    visualState = entityState
                )
            )

            // Array Entity
            arrayEntities.add(
                VisualEntity(
                    visualKey = "heap_array:$elementId",
                    elementId = elementId,
                    dataValue = valStr,
                    slotKey = "array_slot:$i",
                    badgeLabel = if (step.activeParentIndex == i) "i=$i" else null,
                    visualState = entityState
                )
            )

            // Tree Connections (Parent i -> Children 2i+1 and 2i+2)
            val leftChild = 2 * i + 1
            if (leftChild < n) {
                val leftElemId = identityAtSlot.getOrElse(leftChild) { "elem:$leftChild" }
                val isLeftBranchActive = (isCurrentSift && isSmallerChild) ||
                    (swapped != null && ((swapped.first == i && swapped.second == leftChild) || (swapped.first == leftChild && swapped.second == i)))

                treeConnections.add(
                    VisualConnection(
                        connectionKey = "branch:tree_slot:$i->tree_slot:$leftChild",
                        fromVisualKey = "heap_tree:$elementId",
                        toVisualKey = "heap_tree:$leftElemId",
                        label = "L",
                        type = ConnectionType.STRUCTURAL_BRANCH,
                        visualState = if (isLeftBranchActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                    )
                )
            }

            val rightChild = 2 * i + 2
            if (rightChild < n) {
                val rightElemId = identityAtSlot.getOrElse(rightChild) { "elem:$rightChild" }
                val isRightBranchActive = (isCurrentSift && isSmallerChild) ||
                    (swapped != null && ((swapped.first == i && swapped.second == rightChild) || (swapped.first == rightChild && swapped.second == i)))

                treeConnections.add(
                    VisualConnection(
                        connectionKey = "branch:tree_slot:$i->tree_slot:$rightChild",
                        fromVisualKey = "heap_tree:$elementId",
                        toVisualKey = "heap_tree:$rightElemId",
                        label = "R",
                        type = ConnectionType.STRUCTURAL_BRANCH,
                        visualState = if (isRightBranchActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                    )
                )
            }
        }

        val treeSubScene = StructureSubScene(
            subPaneId = "heap_tree",
            type = StructureType.TREE,
            didacticPriority = 1,
            minRequiredWidthDp = 200f,
            minRequiredHeightDp = 180f,
            entities = treeEntities,
            connections = treeConnections
        )

        val arraySubScene = StructureSubScene(
            subPaneId = "heap_array",
            type = StructureType.ARRAY,
            didacticPriority = 2,
            minRequiredWidthDp = 200f,
            minRequiredHeightDp = 60f,
            entities = arrayEntities,
            connections = emptyList()
        )

        return VisualizationScene(
            stepIndex = stepIndex,
            sceneId = "heap_scene_step_$stepIndex",
            subScenes = listOf(treeSubScene, arraySubScene),
            globalMessage = step.explanation
        )
    }

    fun buildPropertiesScenes(steps: List<HeapPropertiesTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        return steps.mapIndexed { stepIndex, step ->
            val arr = step.array
            val n = arr.size
            val treeEntities = mutableListOf<VisualEntity>()
            val arrayEntities = mutableListOf<VisualEntity>()
            val treeConnections = mutableListOf<VisualConnection>()

            for (i in 0 until n) {
                val elementId = "elem:$i"
                val valStr = arr[i].toString()

                val isInspected = step.inspectedIndex == i
                val isParent = step.parentIndex == i
                val isLeft = step.leftChildIndex == i
                val isRight = step.rightChildIndex == i

                val entityState = when {
                    isInspected -> EntityVisualState.ACTIVE
                    isParent || isLeft || isRight -> EntityVisualState.TARGET
                    else -> EntityVisualState.DEFAULT
                }

                val badge = when {
                    isInspected -> "Inspected"
                    isParent -> "Parent"
                    isLeft -> "Left"
                    isRight -> "Right"
                    else -> null
                }

                treeEntities.add(
                    VisualEntity(
                        visualKey = "heap_tree:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "tree_slot:$i",
                        badgeLabel = badge,
                        visualState = entityState
                    )
                )

                arrayEntities.add(
                    VisualEntity(
                        visualKey = "heap_array:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "array_slot:$i",
                        badgeLabel = if (isInspected) "i=$i" else null,
                        visualState = entityState
                    )
                )

                val leftChild = 2 * i + 1
                if (leftChild < n) {
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$leftChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:elem:$leftChild",
                            visualState = if (isInspected && step.leftChildIndex == leftChild) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }

                val rightChild = 2 * i + 2
                if (rightChild < n) {
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$rightChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:elem:$rightChild",
                            visualState = if (isInspected && step.rightChildIndex == rightChild) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            val treeSubScene = StructureSubScene(
                subPaneId = "heap_tree",
                type = StructureType.TREE,
                didacticPriority = 1,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 180f,
                entities = treeEntities,
                connections = treeConnections
            )

            val arraySubScene = StructureSubScene(
                subPaneId = "heap_array",
                type = StructureType.ARRAY,
                didacticPriority = 2,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 60f,
                entities = arrayEntities,
                connections = emptyList()
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "properties_scene_step_$stepIndex",
                subScenes = listOf(treeSubScene, arraySubScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildInsertScenes(steps: List<InsertSiftUpTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val maxN = steps.maxOf { it.heap.size } + 1
        val identityAtSlot = Array(maxN) { i -> "elem:$i" }

        return steps.mapIndexed { stepIndex, step ->
            val heap = step.heap
            val n = heap.size

            if (step.swappedIndices != null) {
                val (i, j) = step.swappedIndices
                if (i in 0 until n && j in 0 until n) {
                    val temp = identityAtSlot[i]
                    identityAtSlot[i] = identityAtSlot[j]
                    identityAtSlot[j] = temp
                }
            } else if (step.event == InsertSiftUpEvent.APPEND_VALUE) {
                if (n - 1 >= 0) {
                    identityAtSlot[n - 1] = "elem:${n - 1}"
                }
            }

            val treeEntities = mutableListOf<VisualEntity>()
            val arrayEntities = mutableListOf<VisualEntity>()
            val treeConnections = mutableListOf<VisualConnection>()

            val swapped = step.swappedIndices

            for (i in 0 until n) {
                val elementId = identityAtSlot.getOrElse(i) { "elem:$i" }
                val valStr = heap[i].toString()

                val isSwapped = swapped != null && (swapped.first == i || swapped.second == i)
                val isCurr = step.currentIndex == i
                val isParent = step.parentIndex == i

                val entityState = when {
                    isSwapped -> EntityVisualState.TARGET
                    isCurr || isParent -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }

                val badge = when {
                    isCurr && step.event == InsertSiftUpEvent.APPEND_VALUE -> "Inserted"
                    isCurr -> "Current"
                    isParent -> "Parent"
                    else -> null
                }

                treeEntities.add(
                    VisualEntity(
                        visualKey = "heap_tree:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "tree_slot:$i",
                        badgeLabel = badge,
                        visualState = entityState
                    )
                )

                arrayEntities.add(
                    VisualEntity(
                        visualKey = "heap_array:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "array_slot:$i",
                        badgeLabel = if (isCurr) "i=$i" else null,
                        visualState = entityState
                    )
                )

                val leftChild = 2 * i + 1
                if (leftChild < n) {
                    val leftElemId = identityAtSlot.getOrElse(leftChild) { "elem:$leftChild" }
                    val isLeftActive = swapped != null && ((swapped.first == i && swapped.second == leftChild) || (swapped.first == leftChild && swapped.second == i))
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$leftChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:$leftElemId",
                            visualState = if (isLeftActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }

                val rightChild = 2 * i + 2
                if (rightChild < n) {
                    val rightElemId = identityAtSlot.getOrElse(rightChild) { "elem:$rightChild" }
                    val isRightActive = swapped != null && ((swapped.first == i && swapped.second == rightChild) || (swapped.first == rightChild && swapped.second == i))
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$rightChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:$rightElemId",
                            visualState = if (isRightActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            val treeSubScene = StructureSubScene(
                subPaneId = "heap_tree",
                type = StructureType.TREE,
                didacticPriority = 1,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 180f,
                entities = treeEntities,
                connections = treeConnections
            )

            val arraySubScene = StructureSubScene(
                subPaneId = "heap_array",
                type = StructureType.ARRAY,
                didacticPriority = 2,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 60f,
                entities = arrayEntities,
                connections = emptyList()
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "insert_scene_step_$stepIndex",
                subScenes = listOf(treeSubScene, arraySubScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildExtractScenes(steps: List<ExtractSiftDownTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val maxN = steps.maxOf { it.heap.size } + 1
        val identityAtSlot = Array(maxN) { i -> "elem:$i" }

        return steps.mapIndexed { stepIndex, step ->
            val heap = step.heap
            val n = heap.size

            if (step.swappedIndices != null) {
                val (i, j) = step.swappedIndices
                if (i in 0 until n && j in 0 until n) {
                    val temp = identityAtSlot[i]
                    identityAtSlot[i] = identityAtSlot[j]
                    identityAtSlot[j] = temp
                }
            } else if (step.event == ExtractSiftDownEvent.SAVE_ROOT_MOVE_LAST) {
                identityAtSlot[0] = identityAtSlot[n]
            }

            val treeEntities = mutableListOf<VisualEntity>()
            val arrayEntities = mutableListOf<VisualEntity>()
            val treeConnections = mutableListOf<VisualConnection>()

            val swapped = step.swappedIndices

            for (i in 0 until n) {
                val elementId = identityAtSlot.getOrElse(i) { "elem:$i" }
                val valStr = heap[i].toString()

                val isSwapped = swapped != null && (swapped.first == i || swapped.second == i)
                val isCurr = step.currentIndex == i
                val isChild = step.smallerChildIndex == i

                val entityState = when {
                    isSwapped -> EntityVisualState.TARGET
                    isCurr || isChild -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }

                val badge = when {
                    isCurr -> "Current"
                    isChild -> "Child"
                    else -> null
                }

                treeEntities.add(
                    VisualEntity(
                        visualKey = "heap_tree:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "tree_slot:$i",
                        badgeLabel = badge,
                        visualState = entityState
                    )
                )

                val leftChild = 2 * i + 1
                if (leftChild < n) {
                    val leftElemId = identityAtSlot.getOrElse(leftChild) { "elem:$leftChild" }
                    val isLeftActive = swapped != null && ((swapped.first == i && swapped.second == leftChild) || (swapped.first == leftChild && swapped.second == i))
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$leftChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:$leftElemId",
                            visualState = if (isLeftActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }

                val rightChild = 2 * i + 2
                if (rightChild < n) {
                    val rightElemId = identityAtSlot.getOrElse(rightChild) { "elem:$rightChild" }
                    val isRightActive = swapped != null && ((swapped.first == i && swapped.second == rightChild) || (swapped.first == rightChild && swapped.second == i))
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$rightChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:$rightElemId",
                            visualState = if (isRightActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            for (i in 0 until n) {
                val elementId = identityAtSlot.getOrElse(i) { "elem:$i" }
                val valStr = heap[i].toString()
                val isCurr = step.currentIndex == i
                val entityState = if (isCurr) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT

                arrayEntities.add(
                    VisualEntity(
                        visualKey = "heap_array:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "array_slot:$i",
                        badgeLabel = if (isCurr) "i=$i" else null,
                        visualState = entityState
                    )
                )
            }

            val treeSubScene = StructureSubScene(
                subPaneId = "heap_tree",
                type = StructureType.TREE,
                didacticPriority = 1,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 180f,
                entities = treeEntities,
                connections = treeConnections
            )

            val arraySubScene = StructureSubScene(
                subPaneId = "heap_array",
                type = StructureType.ARRAY,
                didacticPriority = 2,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 60f,
                entities = arrayEntities,
                connections = emptyList()
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "extract_scene_step_$stepIndex",
                subScenes = listOf(treeSubScene, arraySubScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildSortScenes(steps: List<HeapSortTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val initialArr = steps.first().array
        val n = initialArr.size
        val identityAtSlot = Array(n) { i -> "elem:$i" }
        val elementInitialValues = (0 until n).associate { i -> "elem:$i" to initialArr[i] }

        return steps.mapIndexed { stepIndex, step ->
            val arr = step.array
            val boundary = step.heapBoundaryIndex

            if (step.event == HeapSortEvent.BUILD_MAX_HEAP) {
                val availableIds = (0 until n).map { "elem:$it" }.toMutableList()
                val newIdentityAtSlot = Array(n) { "" }

                for (i in 0 until n) {
                    val targetVal = arr[i]
                    val matchIdx = availableIds.indexOfFirst { id -> elementInitialValues[id] == targetVal }
                    if (matchIdx >= 0) {
                        newIdentityAtSlot[i] = availableIds.removeAt(matchIdx)
                    }
                }
                for (i in 0 until n) {
                    if (newIdentityAtSlot[i].isEmpty() && availableIds.isNotEmpty()) {
                        newIdentityAtSlot[i] = availableIds.removeAt(0)
                    }
                }
                for (i in 0 until n) {
                    identityAtSlot[i] = newIdentityAtSlot[i]
                }
            } else if (step.swappedIndices != null) {
                val (i, j) = step.swappedIndices
                if (i in 0 until n && j in 0 until n) {
                    val temp = identityAtSlot[i]
                    identityAtSlot[i] = identityAtSlot[j]
                    identityAtSlot[j] = temp
                }
            }

            val treeEntities = mutableListOf<VisualEntity>()
            val arrayEntities = mutableListOf<VisualEntity>()
            val treeConnections = mutableListOf<VisualConnection>()

            val swapped = step.swappedIndices

            for (i in 0..boundary) {
                val elementId = identityAtSlot.getOrElse(i) { "elem:$i" }
                val valStr = arr[i].toString()

                val isSwapped = swapped != null && (swapped.first == i || swapped.second == i)
                val isCurr = step.currentRootIndex == i
                val isChild = step.largerChildIndex == i

                val entityState = when {
                    isSwapped -> EntityVisualState.TARGET
                    isCurr || isChild -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }

                treeEntities.add(
                    VisualEntity(
                        visualKey = "heap_tree:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "tree_slot:$i",
                        badgeLabel = if (isCurr) "Root" else null,
                        visualState = entityState
                    )
                )

                val leftChild = 2 * i + 1
                if (leftChild <= boundary) {
                    val leftElemId = identityAtSlot.getOrElse(leftChild) { "elem:$leftChild" }
                    val isLeftActive = swapped != null && ((swapped.first == i && swapped.second == leftChild) || (swapped.first == leftChild && swapped.second == i))
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$leftChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:$leftElemId",
                            visualState = if (isLeftActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }

                val rightChild = 2 * i + 2
                if (rightChild <= boundary) {
                    val rightElemId = identityAtSlot.getOrElse(rightChild) { "elem:$rightChild" }
                    val isRightActive = swapped != null && ((swapped.first == i && swapped.second == rightChild) || (swapped.first == rightChild && swapped.second == i))
                    treeConnections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$i->tree_slot:$rightChild",
                            fromVisualKey = "heap_tree:$elementId",
                            toVisualKey = "heap_tree:$rightElemId",
                            visualState = if (isRightActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            for (i in 0 until n) {
                val elementId = identityAtSlot.getOrElse(i) { "elem:$i" }
                val valStr = arr[i].toString()
                val isSortedSuffix = i > boundary
                val isCurr = step.currentRootIndex == i

                val entityState = when {
                    isSortedSuffix -> EntityVisualState.VISITED
                    isCurr -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }

                val badge = when {
                    isSortedSuffix -> "Sorted"
                    isCurr -> "Root"
                    else -> null
                }

                arrayEntities.add(
                    VisualEntity(
                        visualKey = "heap_array:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "array_slot:$i",
                        badgeLabel = badge,
                        visualState = entityState
                    )
                )
            }

            val treeSubScene = StructureSubScene(
                subPaneId = "heap_tree",
                type = StructureType.TREE,
                didacticPriority = 1,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 180f,
                entities = treeEntities,
                connections = treeConnections
            )

            val arraySubScene = StructureSubScene(
                subPaneId = "heap_array",
                type = StructureType.ARRAY,
                didacticPriority = 2,
                minRequiredWidthDp = 200f,
                minRequiredHeightDp = 60f,
                entities = arrayEntities,
                connections = emptyList()
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "sort_scene_step_$stepIndex",
                subScenes = listOf(treeSubScene, arraySubScene),
                globalMessage = step.explanation
            )
        }
    }
}
