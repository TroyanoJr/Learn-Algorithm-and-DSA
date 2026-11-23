package com.example.learndsandalgorithm.presentation.learn

internal data class MergeSortFullSceneMeta(
    val traceStep: MergeSortFullTraceStep,
    val visibleNodeIds: Set<String>,
    val activePath: List<String>
)

internal data class MergeSortFullSceneFrame(
    val scene: VisualizationScene,
    val meta: MergeSortFullSceneMeta,
    val statusText: String,
    val snapshotIndex: Int,
    val snapshotCount: Int,
    val isCompleted: Boolean,
    val footer: String
)

internal object MergeSortFullSceneBuilder {
    const val TREE_SCENE_ID = "merge_sort_full_tree"
    const val WORKING_SCENE_ID = "merge_sort_full_working"
    const val BUFFER_SCENE_ID = "merge_sort_full_buffer"
    private const val SCENE_ID = "merge_sort_full"

    fun build(steps: List<MergeSortFullTraceStep>): List<MergeSortFullSceneFrame> {
        if (steps.isEmpty()) return emptyList()
        val input = steps.first().array
        require(steps.all { it.array == input && it.workingArray.size == input.size }) {
            "Merge Sort trace array geometry must remain stable"
        }
        require(steps.all { it.auxiliaryBuffer.size == input.size }) {
            "The auxiliary buffer must retain one fixed slot per input element"
        }

        return steps.mapIndexed { index, step ->
            val visibleIds = step.nodes.map { it.nodeId }.toSet()
            val treeEntities = step.nodes.map { node ->
                val active = node.nodeId == step.activeNodeId
                val onActivePath = node.nodeId in step.callStack
                val visualState = when {
                    active && step.event == FullMergeEvent.DIVIDE -> EntityVisualState.ACTIVE
                    active && step.event == FullMergeEvent.BASE_CASE -> EntityVisualState.TARGET
                    node.isCompleted -> EntityVisualState.VISITED
                    active || onActivePath -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }
                VisualEntity(
                    visualKey = nodeVisualKey(node.nodeId),
                    elementId = node.nodeId,
                    dataValue = rangeLabel(node.rangeStart, node.rangeEndExclusive),
                    slotKey = node.nodeId,
                    badgeLabel = when {
                        node.isBaseCase -> "BASE CASE"
                        node.isCompleted -> "SORTED"
                        !node.hasEntered -> "ROOT CALL"
                        else -> "DEPTH ${node.callDepth}"
                    },
                    visualState = visualState
                )
            }
            val treeConnections = step.nodes.mapNotNull { node ->
                val parentId = node.parentNodeId ?: return@mapNotNull null
                if (parentId !in visibleIds) return@mapNotNull null
                VisualConnection(
                    connectionKey = branchKey(parentId, node.nodeId),
                    fromVisualKey = nodeVisualKey(parentId),
                    toVisualKey = nodeVisualKey(node.nodeId),
                    type = ConnectionType.STRUCTURAL_BRANCH,
                    visualState = if (node.nodeId in step.callStack) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                )
            }
            val workingEntities = step.workingElements.mapIndexed { slot, element ->
                val comparisonIds = step.currentComparison?.let {
                    setOf(it.leftElement.identity, it.rightElement.identity)
                }.orEmpty()
                val visualState = when {
                    step.event == FullMergeEvent.COMPARE && element.identity in comparisonIds -> EntityVisualState.ACTIVE
                    (step.event == FullMergeEvent.WRITE_BUFFER || step.event == FullMergeEvent.FLUSH_REMAINING) &&
                        element.identity == step.selectedElement?.identity -> EntityVisualState.TARGET
                    step.event == FullMergeEvent.COPY_BACK && slot == step.copyBackIndex -> EntityVisualState.TARGET
                    step.event == FullMergeEvent.COMPLETE -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }
                VisualEntity(
                    visualKey = workingVisualKey(slot),
                    elementId = element.identity,
                    dataValue = element.value.toString(),
                    slotKey = workingSlotKey(slot),
                    badgeLabel = "#${element.originalIndex}",
                    occurrenceId = element.identity,
                    visualState = visualState
                )
            }
            val bufferEntities = step.auxiliaryBuffer.mapIndexed { slot, element ->
                VisualEntity(
                    visualKey = bufferVisualKey(slot),
                    elementId = bufferSlotKey(slot),
                    dataValue = element?.value?.toString().orEmpty(),
                    slotKey = bufferSlotKey(slot),
                    badgeLabel = element?.let { "#${it.originalIndex}" },
                    occurrenceId = element?.identity,
                    visualState = when {
                        element == null -> EntityVisualState.DEFAULT
                        step.event == FullMergeEvent.COPY_BACK && step.copyBackIndex == slot -> EntityVisualState.TARGET
                        step.newlyWrittenBufferSlot == slot &&
                            (step.event == FullMergeEvent.WRITE_BUFFER || step.event == FullMergeEvent.FLUSH_REMAINING) -> EntityVisualState.TARGET
                        else -> EntityVisualState.VISITED
                    }
                )
            }
            val scene = VisualizationScene(
                stepIndex = index,
                sceneId = SCENE_ID,
                subScenes = listOf(
                    StructureSubScene(
                        subPaneId = TREE_SCENE_ID,
                        type = StructureType.TREE,
                        minRequiredWidthDp = 180f,
                        minRequiredHeightDp = 140f,
                        entities = treeEntities,
                        connections = treeConnections
                    ),
                    StructureSubScene(
                        subPaneId = WORKING_SCENE_ID,
                        type = StructureType.ARRAY,
                        minRequiredWidthDp = 120f,
                        minRequiredHeightDp = 56f,
                        entities = workingEntities
                    ),
                    StructureSubScene(
                        subPaneId = BUFFER_SCENE_ID,
                        type = StructureType.ARRAY,
                        minRequiredWidthDp = 120f,
                        minRequiredHeightDp = 56f,
                        entities = bufferEntities
                    )
                ),
                globalMessage = step.badgeText
            )
            MergeSortFullSceneFrame(
                scene = scene,
                meta = MergeSortFullSceneMeta(step, visibleIds, step.callStack.toList()),
                statusText = statusText(step),
                snapshotIndex = index,
                snapshotCount = steps.size,
                isCompleted = step.isTerminal,
                footer = "TIME O(N LOG N) · AUXILIARY SPACE O(N)"
            )
        }
    }

    internal fun nodeVisualKey(nodeId: String): String = "merge_full:$nodeId"
    internal fun workingVisualKey(index: Int): String = "working:$index"
    internal fun workingSlotKey(index: Int): String = "working_slot:$index"
    internal fun bufferVisualKey(index: Int): String = "buffer:$index"
    internal fun bufferSlotKey(index: Int): String = "buffer_slot:$index"
    internal fun branchKey(parent: String, child: String): String = "branch:$parent->$child"
    internal fun rangeLabel(start: Int, endExclusive: Int): String = "[$start..$endExclusive)"

    internal fun statusText(step: MergeSortFullTraceStep): String = when (step.event) {
        FullMergeEvent.START -> "READY · FULL MERGE SORT"
        FullMergeEvent.CALL_ENTER -> {
            val activeDepth = step.nodes.firstOrNull { it.nodeId == step.activeNodeId }?.callDepth ?: 0
            "RECURSIVE CALL · DEPTH $activeDepth"
        }
        FullMergeEvent.DIVIDE -> "DIVIDE · SPLIT SUBPROBLEM"
        FullMergeEvent.BASE_CASE -> "BASE CASE · RANGE IS ALREADY SORTED"
        FullMergeEvent.MERGE_START -> "MERGE · TWO SORTED RUNS"
        FullMergeEvent.COMPARE -> "COMPARE · FRONT ELEMENTS"
        FullMergeEvent.WRITE_BUFFER -> "WRITE · AUXILIARY BUFFER"
        FullMergeEvent.FLUSH_REMAINING -> "FLUSH · REMAINING RUN"
        FullMergeEvent.COPY_BACK -> "COPY BACK · WORKING ARRAY"
        FullMergeEvent.RETURN -> "RETURN · RECURSIVE CALL"
        FullMergeEvent.COMPLETE -> "COMPLETE · ARRAY SORTED"
    }
}
