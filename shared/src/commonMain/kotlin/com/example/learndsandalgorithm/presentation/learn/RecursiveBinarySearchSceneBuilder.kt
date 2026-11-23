package com.example.learndsandalgorithm.presentation.learn

internal data class RecursiveBinarySearchSceneMeta(
    val array: List<Int>,
    val target: Int,
    val event: BinarySearchEvent,
    val rangeLeft: Int?,
    val rangeRight: Int?,
    val activeInterval: IntRange?,
    val excludedIndices: Set<Int>,
    val leftIndex: Int?,
    val rightIndex: Int?,
    val midIndex: Int?,
    val midValue: Int?,
    val comparison: BinarySearchComparison?,
    val markerLabels: Map<Int, List<String>>,
    val foundIndex: Int?,
    val resultIndex: Int?,
    val returnValue: Int?,
    val callDepth: Int,
    val stackDepth: Int,
    val activeCallId: Int?,
    val callerCallId: Int?,
    val calleeCall: BinarySearchCallFrame?,
    val returnedCall: BinarySearchCallFrame?,
    val callStack: List<BinarySearchCallFrame>,
    val maximumStackDepth: Int,
    val isEmptyRangeBaseCase: Boolean,
    val isTerminal: Boolean,
    val explanation: String,
    val badgeText: String
)

internal data class RecursiveBinarySearchSceneFrame(
    val scene: VisualizationScene,
    val meta: RecursiveBinarySearchSceneMeta,
    val statusText: String,
    val snapshotIndex: Int,
    val snapshotCount: Int,
    val isCompleted: Boolean,
    val footer: String
)

internal object RecursiveBinarySearchSceneBuilder {
    private const val ARRAY_SUB_SCENE_ID = "binary_search_array"
    private const val VISUAL_KEY_PREFIX = "recursive_binary_search:index:"

    fun build(steps: List<BinarySearchTraceStep>): List<RecursiveBinarySearchSceneFrame> {
        if (steps.isEmpty()) return emptyList()
        val expectedArray = steps.first().array
        require(steps.all { it.array == expectedArray }) { "Binary search array must remain stable through the trace" }
        val maximumStackDepth = steps.maxOf { step ->
            maxOf(step.stackDepth, (step.calleeCall?.depth?.plus(1) ?: 0))
        }.coerceAtLeast(1)

        return steps.mapIndexed { index, step ->
            val activeCall = step.callStack.lastOrNull()
            val intervalLeft = if (step.event == BinarySearchEvent.START) step.left else activeCall?.left
            val intervalRight = if (step.event == BinarySearchEvent.START) step.right else activeCall?.right
            val activeInterval = if (
                intervalLeft != null && intervalRight != null && intervalLeft <= intervalRight &&
                intervalLeft in step.array.indices && intervalRight in step.array.indices
            ) intervalLeft..intervalRight else null
            val excludedIndices = step.array.indices.filterNot { indexInRange ->
                activeInterval?.contains(indexInRange) == true
            }.toSet()
            val leftMarker = intervalLeft?.takeIf { it in step.array.indices && activeInterval != null }
            val rightMarker = intervalRight?.takeIf { it in step.array.indices && activeInterval != null }
            val midIndex = activeCall?.mid?.takeIf { it in step.array.indices }
            val markerLabels = linkedMapOf<Int, MutableList<String>>()
            leftMarker?.let { markerLabels.getOrPut(it) { mutableListOf() } += "L" }
            midIndex?.let { markerLabels.getOrPut(it) { mutableListOf() } += "MID" }
            rightMarker?.let { markerLabels.getOrPut(it) { mutableListOf() } += "R" }

            val presentations = step.array.mapIndexed { itemIndex, value ->
                val visualState = when {
                    itemIndex == step.foundIndex -> EntityVisualState.TARGET
                    itemIndex == midIndex -> EntityVisualState.ACTIVE
                    itemIndex in excludedIndices -> EntityVisualState.VIOLATION
                    else -> EntityVisualState.DEFAULT
                }
                VisualEntity(
                    visualKey = "$VISUAL_KEY_PREFIX$itemIndex",
                    elementId = "array:item:$itemIndex",
                    dataValue = value.toString(),
                    slotKey = "binary_search:array_slot:$itemIndex",
                    badgeLabel = itemIndex.toString(),
                    visualState = visualState
                )
            }
            val arraySubScene = StructureSubScene(
                subPaneId = ARRAY_SUB_SCENE_ID,
                type = StructureType.ARRAY,
                minRequiredWidthDp = 120f,
                minRequiredHeightDp = 60f,
                entities = presentations
            )
            val statusText = when (step.event) {
                BinarySearchEvent.START -> "READY · TARGET = ${step.target}"
                BinarySearchEvent.CALL_ENTER -> "CALL DEPTH ${step.callDepth}"
                BinarySearchEvent.CALCULATE_MID -> "CALCULATE MID = ${step.mid}"
                BinarySearchEvent.COMPARE_TARGET -> "COMPARE arr[${step.mid}] WITH ${step.target}"
                BinarySearchEvent.RECURSE_LEFT -> "RECURSE LEFT · CALL #${step.calleeCall?.callId}"
                BinarySearchEvent.RECURSE_RIGHT -> "RECURSE RIGHT · CALL #${step.calleeCall?.callId}"
                BinarySearchEvent.TARGET_FOUND -> "MATCH FOUND AT INDEX ${step.foundIndex}"
                BinarySearchEvent.TARGET_NOT_FOUND -> "EMPTY RANGE · TARGET ABSENT"
                BinarySearchEvent.RETURN -> if (step.isTerminal) "RESULT RETURNED TO INITIAL CALLER" else "RETURN FROM CALL #${step.returnedCall?.callId}"
            }
            val meta = RecursiveBinarySearchSceneMeta(
                array = step.array,
                target = step.target,
                event = step.event,
                rangeLeft = activeCall?.left ?: if (step.event == BinarySearchEvent.START) step.left else null,
                rangeRight = activeCall?.right ?: if (step.event == BinarySearchEvent.START) step.right else null,
                activeInterval = activeInterval,
                excludedIndices = excludedIndices,
                leftIndex = leftMarker,
                rightIndex = rightMarker,
                midIndex = midIndex,
                midValue = activeCall?.midValue,
                comparison = activeCall?.comparison,
                markerLabels = markerLabels.mapValues { it.value.toList() },
                foundIndex = step.foundIndex,
                resultIndex = step.resultIndex,
                returnValue = step.returnValue,
                callDepth = step.callDepth,
                stackDepth = step.stackDepth,
                activeCallId = step.activeCallId,
                callerCallId = step.callerCallId,
                calleeCall = step.calleeCall,
                returnedCall = step.returnedCall,
                callStack = step.callStack,
                maximumStackDepth = maximumStackDepth,
                isEmptyRangeBaseCase = step.isEmptyRangeBaseCase,
                isTerminal = step.isTerminal,
                explanation = step.explanation,
                badgeText = step.badgeText
            )
            RecursiveBinarySearchSceneFrame(
                scene = VisualizationScene(
                    stepIndex = index,
                    sceneId = "recursive_binary_search_step_$index",
                    subScenes = listOf(arraySubScene),
                    globalMessage = step.explanation
                ),
                meta = meta,
                statusText = statusText,
                snapshotIndex = index,
                snapshotCount = steps.size,
                isCompleted = step.isTerminal,
                footer = "TIME O(log N) · RECURSIVE SPACE O(log N)"
            )
        }
    }
}
