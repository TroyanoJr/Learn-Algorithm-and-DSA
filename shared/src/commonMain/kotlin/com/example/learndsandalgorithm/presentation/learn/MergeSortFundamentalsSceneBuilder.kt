package com.example.learndsandalgorithm.presentation.learn

internal data class MergeSortFundamentalsSceneFrame(
    val scene: VisualizationScene,
    val step: MergeFundamentalsTraceStep,
    val statusText: String,
    val snapshotIndex: Int,
    val snapshotCount: Int,
    val isCompleted: Boolean,
    val footer: String
)

internal object MergeSortFundamentalsSceneBuilder {
    const val LEFT_SCENE_ID = "merge_fundamentals_left"
    const val RIGHT_SCENE_ID = "merge_fundamentals_right"
    const val OUTPUT_SCENE_ID = "merge_fundamentals_output"
    private const val SCENE_ID = "merge_sort_fundamentals"

    fun build(steps: List<MergeFundamentalsTraceStep>): List<MergeSortFundamentalsSceneFrame> {
        if (steps.isEmpty()) return emptyList()
        val left = steps.first().leftArr
        val right = steps.first().rightArr
        require(steps.all { it.leftArr == left && it.rightArr == right }) {
            "Merge input arrays must remain stable through the trace"
        }
        require(steps.all { it.outputOrigins.size == it.mergedResult.size }) {
            "Every written output value must retain its source origin"
        }

        val outputSize = left.size + right.size
        return steps.mapIndexed { index, step ->
            require(step.mergedResult.size <= outputSize) { "Merge trace wrote beyond its fixed output slots" }
            val leftEntities = left.mapIndexed { elementIndex, value ->
                sourceEntity(MergeSource.LEFT, elementIndex, value, step)
            }
            val rightEntities = right.mapIndexed { elementIndex, value ->
                sourceEntity(MergeSource.RIGHT, elementIndex, value, step)
            }
            val outputEntities = (0 until outputSize).map { outputIndex ->
                val origin = step.outputOrigins.getOrNull(outputIndex)
                val value = step.mergedResult.getOrNull(outputIndex)
                val isNewlyWritten = outputIndex == step.destinationIndex && step.event != MergeStepEvent.COMPLETE
                VisualEntity(
                    visualKey = outputSlotKey(outputIndex),
                    elementId = outputSlotKey(outputIndex),
                    dataValue = value?.toString().orEmpty(),
                    slotKey = outputSlotKey(outputIndex),
                    badgeLabel = origin?.let(::originLabel),
                    occurrenceId = origin?.let { "output_occurrence:${it.identity}" },
                    visualState = when {
                        origin == null -> EntityVisualState.DEFAULT
                        step.event == MergeStepEvent.COMPLETE -> EntityVisualState.TARGET
                        isNewlyWritten -> EntityVisualState.TARGET
                        else -> EntityVisualState.VISITED
                    }
                )
            }
            val connections = step.outputOrigins.mapIndexed { outputIndex, origin ->
                val destination = outputSlotKey(outputIndex)
                VisualConnection(
                    connectionKey = transferConnectionKey(origin, outputIndex),
                    fromVisualKey = sourceVisualKey(origin.source, origin.index),
                    toVisualKey = destination,
                    label = originLabel(origin),
                    type = ConnectionType.POINTER_LINK,
                    visualState = if (
                        step.event != MergeStepEvent.COMPLETE &&
                        step.selectedOrigin == origin &&
                        step.destinationIndex == outputIndex
                    ) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT,
                    directed = true
                )
            }
            val scene = VisualizationScene(
                stepIndex = index,
                sceneId = SCENE_ID,
                subScenes = listOf(
                    StructureSubScene(
                        subPaneId = LEFT_SCENE_ID,
                        type = StructureType.ARRAY,
                        minRequiredWidthDp = 120f,
                        minRequiredHeightDp = 80f,
                        entities = leftEntities
                    ),
                    StructureSubScene(
                        subPaneId = RIGHT_SCENE_ID,
                        type = StructureType.ARRAY,
                        minRequiredWidthDp = 120f,
                        minRequiredHeightDp = 80f,
                        entities = rightEntities
                    ),
                    StructureSubScene(
                        subPaneId = OUTPUT_SCENE_ID,
                        type = StructureType.ARRAY,
                        minRequiredWidthDp = 120f,
                        minRequiredHeightDp = 80f,
                        entities = outputEntities,
                        referenceConnections = connections
                    )
                ),
                globalMessage = step.badgeText
            )
            MergeSortFundamentalsSceneFrame(
                scene = scene,
                step = step,
                statusText = step.badgeText,
                snapshotIndex = index,
                snapshotCount = steps.size,
                isCompleted = step.isTerminal,
                footer = "MERGE WORK · O(N) TIME"
            )
        }
    }

    internal fun sourceVisualKey(source: MergeSource, index: Int): String =
        "merge:${source.name.lowercase()}:$index"

    internal fun outputSlotKey(index: Int): String = "output_slot:$index"

    internal fun transferConnectionKey(origin: MergeElementOrigin, outputIndex: Int): String =
        "transfer:${origin.identity}->${outputSlotKey(outputIndex)}"

    internal fun originLabel(origin: MergeElementOrigin): String =
        "${if (origin.source == MergeSource.LEFT) "L" else "R"}[${origin.index}]"

    private fun sourceEntity(
        source: MergeSource,
        index: Int,
        value: Int,
        step: MergeFundamentalsTraceStep
    ): VisualEntity {
        val pointer = if (source == MergeSource.LEFT) step.i else step.j
        val comparisonIndex = when (source) {
            MergeSource.LEFT -> step.comparison?.leftIndex
            MergeSource.RIGHT -> step.comparison?.rightIndex
        }
        val origin = MergeElementOrigin(source, index)
        val state = when {
            step.selectedOrigin == origin -> EntityVisualState.TARGET
            comparisonIndex == index && step.event == MergeStepEvent.COMPARE -> EntityVisualState.ACTIVE
            index < pointer -> EntityVisualState.VISITED
            else -> EntityVisualState.DEFAULT
        }
        return VisualEntity(
            visualKey = sourceVisualKey(source, index),
            elementId = origin.identity,
            dataValue = value.toString(),
            slotKey = "${source.name.lowercase()}:$index",
            badgeLabel = "[$index]",
            visualState = state
        )
    }
}
