package com.example.learndsandalgorithm.presentation.learn

internal data class QuickSortPartitionSceneMeta(
    val event: QuickPartitionEvent,
    val array: List<Int>,
    val elementIdentityByIndex: List<String>,
    val low: Int,
    val high: Int,
    val pivotValue: Int,
    val pivotIdentity: String,
    val pivotOriginIndex: Int,
    val pivotPosition: Int,
    val i: Int,
    val j: Int,
    val lessEqualIndices: List<Int>,
    val processedGreaterIndices: List<Int>,
    val unprocessedIndices: List<Int>,
    val activeComparisonIndex: Int?,
    val comparisonValueIndex: Int?,
    val comparisonValue: Int?,
    val comparisonPassed: Boolean?,
    val swappedIndices: Pair<Int, Int>?,
    val finalPivotPosition: Int?,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean
)

internal data class QuickSortPartitionSceneFrame(
    val scene: VisualizationScene,
    val meta: QuickSortPartitionSceneMeta
)

internal object QuickSortPartitionSceneBuilder {
    const val ARRAY_SCENE_ID = "quick_sort_partition_array"
    const val POINTER_SCENE_ID = "quick_sort_partition_pointers"

    private const val ARRAY_VISUAL_PREFIX = "quick_sort_partition:element:"
    private const val POINTER_VISUAL_PREFIX = "quick_sort_partition:pointer:"

    fun build(steps: List<QuickPartitionTraceStep>): List<QuickSortPartitionSceneFrame> {
        if (steps.isEmpty()) return emptyList()

        val first = steps.first()
        val originalIdentityByIndex = first.array.indices.map { "item:$it" }
        val valueByIdentity = originalIdentityByIndex.mapIndexed { index, identity -> identity to first.array[index] }.toMap()
        val identitiesByIndex = originalIdentityByIndex.toMutableList()
        val pivotIdentity = originalIdentityByIndex.getOrNull(first.pivotIndex)
            ?: error("Quick Sort partition trace has no pivot occurrence")

        val frames = steps.mapIndexed { stepIndex, step ->
            if (stepIndex > 0 && step.event in setOf(QuickPartitionEvent.SWAP_ELEMENTS, QuickPartitionEvent.PLACE_PIVOT)) {
                val (firstIndex, secondIndex) = step.swappedIndices
                    ?: error("${step.event} must retain its swapped indices")
                require(firstIndex in identitiesByIndex.indices && secondIndex in identitiesByIndex.indices) {
                    "Swap indices must refer to existing array cells"
                }
                val identity = identitiesByIndex[firstIndex]
                identitiesByIndex[firstIndex] = identitiesByIndex[secondIndex]
                identitiesByIndex[secondIndex] = identity
            }

            require(identitiesByIndex.size == step.array.size) { "Array size changed during one partition" }
            require(identitiesByIndex.map { valueByIdentity.getValue(it) } == step.array) {
                "Trace values do not match the recorded swap sequence at step $stepIndex"
            }

            val activeComparisonIndex = when {
                step.event == QuickPartitionEvent.SCAN_COMPARE && step.badgeText.startsWith("COMPARE") -> step.j
                else -> null
            }
            val comparisonValueIndex = when {
                step.event == QuickPartitionEvent.SCAN_COMPARE -> step.j
                step.event == QuickPartitionEvent.SWAP_ELEMENTS -> step.swappedIndices?.first
                else -> null
            }
            val comparisonValue = comparisonValueIndex?.let { step.array.getOrNull(it) }
            val comparisonPassed = comparisonValue?.let { it <= step.pivotValue }
            val pivotPosition = step.finalPivotPosition ?: step.pivotIndex

            val lessEqualIndices = if (step.i >= step.low) {
                (step.low..step.i.coerceAtMost(step.high - 1)).toList()
            } else {
                emptyList()
            }
            val processedGreaterEndExclusive = when (step.event) {
                QuickPartitionEvent.SWAP_ELEMENTS -> step.j + 1
                QuickPartitionEvent.PLACE_PIVOT, QuickPartitionEvent.PARTITION_COMPLETE -> step.array.size
                else -> step.j
            }
            val processedGreaterStart = if (step.event == QuickPartitionEvent.PLACE_PIVOT || step.event == QuickPartitionEvent.PARTITION_COMPLETE) {
                pivotPosition + 1
            } else {
                step.i + 1
            }
            val processedGreaterIndices = if (processedGreaterStart < processedGreaterEndExclusive) {
                (processedGreaterStart until processedGreaterEndExclusive).filter { it in step.low until step.array.size }
            } else emptyList()
            val unprocessedStart = when {
                step.event == QuickPartitionEvent.START || step.event == QuickPartitionEvent.SELECT_PIVOT -> step.low
                step.event == QuickPartitionEvent.PLACE_PIVOT || step.event == QuickPartitionEvent.PARTITION_COMPLETE -> step.high
                else -> step.j + 1
            }
            val unprocessedIndices = if (unprocessedStart < step.high) {
                (unprocessedStart until step.high).filter { it in step.low until step.high }
            } else {
                emptyList()
            }

            val arrayEntities = step.array.mapIndexed { index, value ->
                val visualState = when {
                    index == pivotPosition -> EntityVisualState.TARGET
                    step.swappedIndices?.let { index == it.first || index == it.second } == true -> EntityVisualState.TARGET
                    index == activeComparisonIndex -> EntityVisualState.ACTIVE
                    index in lessEqualIndices -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }
                VisualEntity(
                    visualKey = arrayVisualKey(identitiesByIndex[index]),
                    elementId = identitiesByIndex[index],
                    dataValue = value.toString(),
                    slotKey = "array_slot:$index",
                    badgeLabel = "[$index]",
                    visualState = visualState
                )
            }

            val pointerEntities = listOf(
                pointerEntity("i", step.i),
                pointerEntity("j", step.j)
            )
            val activeConnections = buildList {
                if (step.i in step.array.indices) add(pointerConnection("i", identitiesByIndex[step.i]))
                if (step.j in step.array.indices) add(pointerConnection("j", identitiesByIndex[step.j]))
            }
            val meta = QuickSortPartitionSceneMeta(
                event = step.event,
                array = step.array,
                elementIdentityByIndex = identitiesByIndex.toList(),
                low = step.low,
                high = step.high,
                pivotValue = step.pivotValue,
                pivotIdentity = pivotIdentity,
                pivotOriginIndex = step.pivotIndex,
                pivotPosition = pivotPosition,
                i = step.i,
                j = step.j,
                lessEqualIndices = lessEqualIndices,
                processedGreaterIndices = processedGreaterIndices,
                unprocessedIndices = unprocessedIndices,
                activeComparisonIndex = activeComparisonIndex,
                comparisonValueIndex = comparisonValueIndex,
                comparisonValue = comparisonValue,
                comparisonPassed = comparisonPassed,
                swappedIndices = step.swappedIndices,
                finalPivotPosition = step.finalPivotPosition,
                explanation = step.explanation,
                badgeText = step.badgeText,
                isTerminal = step.isTerminal
            )
            val arrayScene = StructureSubScene(
                subPaneId = ARRAY_SCENE_ID,
                type = StructureType.ARRAY,
                minRequiredWidthDp = step.array.size * 64f,
                minRequiredHeightDp = 250f,
                entities = arrayEntities
            )
            val pointerScene = StructureSubScene(
                subPaneId = POINTER_SCENE_ID,
                type = StructureType.ARRAY,
                minRequiredWidthDp = step.array.size * 64f,
                minRequiredHeightDp = 250f,
                entities = pointerEntities,
                connections = activeConnections
            )
            QuickSortPartitionSceneFrame(
                scene = VisualizationScene(
                    stepIndex = stepIndex,
                    sceneId = "quick_sort_partition_step_$stepIndex",
                    subScenes = listOf(arrayScene, pointerScene),
                    globalMessage = step.explanation
                ),
                meta = meta
            )
        }
        val referencePointerConnections = frames
            .flatMap { frame -> frame.scene.subScenes.single { it.subPaneId == POINTER_SCENE_ID }.connections }
            .distinctBy { it.connectionKey }
        return frames.map { frame ->
            frame.copy(
                scene = frame.scene.copy(
                    subScenes = frame.scene.subScenes.map { subScene ->
                        if (subScene.subPaneId == POINTER_SCENE_ID) {
                            subScene.copy(referenceConnections = referencePointerConnections)
                        } else subScene
                    }
                )
            )
        }
    }

    internal fun arrayVisualKey(identity: String): String = "$ARRAY_VISUAL_PREFIX$identity"

    private fun pointerEntity(name: String, index: Int) = VisualEntity(
        visualKey = "$POINTER_VISUAL_PREFIX$name",
        elementId = "pointer:$name",
        dataValue = "$name = $index",
        slotKey = if (index < 0) "pointer_lane:$name:sentinel" else "pointer_lane:$name:$index",
        badgeLabel = name,
        visualState = EntityVisualState.ACTIVE
    )

    private fun pointerConnection(name: String, elementIdentity: String) = VisualConnection(
        connectionKey = "pointer_link:$name->$elementIdentity",
        fromVisualKey = "$POINTER_VISUAL_PREFIX$name",
        toVisualKey = arrayVisualKey(elementIdentity),
        label = name,
        type = ConnectionType.POINTER_LINK,
        visualState = ConnectionVisualState.HIGHLIGHTED
    )
}
