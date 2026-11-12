package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms a sequence of [BuildHeapTraceStep] into
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
}
