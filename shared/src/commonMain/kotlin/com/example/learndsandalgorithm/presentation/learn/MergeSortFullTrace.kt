package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.abs

internal enum class FullMergeEvent {
    START,
    CALL_ENTER,
    DIVIDE,
    BASE_CASE,
    MERGE_START,
    COMPARE,
    WRITE_BUFFER,
    FLUSH_REMAINING,
    COPY_BACK,
    RETURN,
    COMPLETE
}

/** All ranges in this trace are half-open: [rangeStart, rangeEndExclusive). */
internal data class MergeIndexRange(val start: Int, val endExclusive: Int) {
    val size: Int get() = endExclusive - start
}

internal data class FullMergeElement(
    val identity: String,
    val originalIndex: Int,
    val value: Int
)

internal data class MergeTreeNode(
    val nodeId: String,
    val parentNodeId: String?,
    val leftChildId: String?,
    val rightChildId: String?,
    val rangeStart: Int,
    val rangeEndExclusive: Int,
    val callDepth: Int,
    val isBaseCase: Boolean,
    val hasEntered: Boolean,
    val isCompleted: Boolean,
    val originalElementIds: List<String>,
    val currentValues: List<Int>
) {
    val range: MergeIndexRange get() = MergeIndexRange(rangeStart, rangeEndExclusive)
}

internal data class FullMergePointers(val left: Int, val right: Int, val destination: Int)

internal data class FullMergeComparison(
    val leftElement: FullMergeElement,
    val rightElement: FullMergeElement,
    val leftIndex: Int,
    val rightIndex: Int
)

internal enum class FullMergeSource { LEFT, RIGHT }

internal enum class FullMergePlaybackMode { STANDARD, DETAILED }

internal data class FullMergePlaybackStep(
    val originalTraceIndex: Int,
    val traceStep: MergeSortFullTraceStep
)

internal data class MergeSortFullTraceStep(
    val array: List<Int>,
    val workingElements: List<FullMergeElement>,
    val workingArray: List<Int>,
    val nodes: List<MergeTreeNode>,
    val activeNodeId: String?,
    val event: FullMergeEvent,
    val activeRange: MergeIndexRange?,
    val splitIndex: Int?,
    val leftRun: List<FullMergeElement>,
    val rightRun: List<FullMergeElement>,
    val leftPointer: Int?,
    val rightPointer: Int?,
    val destinationPointer: Int?,
    val pointersBeforeTransfer: FullMergePointers?,
    val pointersAfterTransfer: FullMergePointers?,
    val currentComparison: FullMergeComparison?,
    val selectedElement: FullMergeElement?,
    val selectedSource: FullMergeSource?,
    val auxiliaryBuffer: List<FullMergeElement?>,
    val newlyWrittenBufferSlot: Int?,
    val copyBackIndex: Int?,
    val copyBackProgress: Int,
    val completedRanges: List<MergeIndexRange>,
    val returnToNodeId: String?,
    val callStack: List<String>,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
) {
    val phase: FullMergeEvent get() = event
    val currentValuesInWorkingArray: List<Int> get() = workingArray
}

internal object MergeSortFullTrace {
    private const val DEFAULT_INPUT_NOTE = "Full Merge Sort: Recursively divides the array down to singleton base cases, then merges sorted sub-lists back up."

    fun stepsForFullSort(
        arr: List<Int> = listOf(38, 27, 38, 3, 9)
    ): List<MergeSortFullTraceStep> {
        val input = arr.toList()
        val originalElements = input.mapIndexed { index, value ->
            FullMergeElement(identity = "input:$index", originalIndex = index, value = value)
        }
        val working = originalElements.toMutableList()
        val buffer = MutableList<FullMergeElement?>(input.size) { null }
        val nodes = linkedMapOf<String, MergeTreeNode>()
        val completedRanges = linkedSetOf<MergeIndexRange>()
        val callStack = mutableListOf<String>()
        val steps = mutableListOf<MergeSortFullTraceStep>()
        val rootId = nodeId(0, input.size)
        nodes[rootId] = MergeTreeNode(
            nodeId = rootId,
            parentNodeId = null,
            leftChildId = null,
            rightChildId = null,
            rangeStart = 0,
            rangeEndExclusive = input.size,
            callDepth = 0,
            isBaseCase = input.size <= 1,
            hasEntered = false,
            isCompleted = false,
            originalElementIds = originalElements.map { it.identity },
            currentValues = input.toList()
        )

        var activeNodeId: String? = null
        var activeRange: MergeIndexRange? = null
        var splitIndex: Int? = null
        var leftRun = emptyList<FullMergeElement>()
        var rightRun = emptyList<FullMergeElement>()
        var leftPointer: Int? = null
        var rightPointer: Int? = null
        var destinationPointer: Int? = null
        var comparison: FullMergeComparison? = null
        var selectedElement: FullMergeElement? = null
        var selectedSource: FullMergeSource? = null
        var newlyWrittenBufferSlot: Int? = null
        var copyBackIndex: Int? = null
        var copyBackProgress = 0
        var pointersBefore: FullMergePointers? = null
        var pointersAfter: FullMergePointers? = null
        var returnToNodeId: String? = null
        lateinit var flushOne: (FullMergeSource, Int, Int, Int) -> Unit
        lateinit var returnFromCall: (String, String?, Int, Int) -> Unit

        fun snapshot(
            event: FullMergeEvent,
            explanation: String,
            badgeText: String,
            terminal: Boolean = false
        ) {
            val nodeSnapshot = nodes.values.map { node ->
                val nodeElements = working.subList(node.rangeStart, node.rangeEndExclusive).toList()
                node.copy(
                    originalElementIds = originalElements.subList(node.rangeStart, node.rangeEndExclusive).map { it.identity },
                    currentValues = nodeElements.map { it.value }
                )
            }
            steps.add(
                MergeSortFullTraceStep(
                    array = input.toList(),
                    workingElements = working.toList(),
                    workingArray = working.map { it.value },
                    nodes = nodeSnapshot,
                    activeNodeId = activeNodeId,
                    event = event,
                    activeRange = activeRange,
                    splitIndex = splitIndex,
                    leftRun = leftRun.toList(),
                    rightRun = rightRun.toList(),
                    leftPointer = leftPointer,
                    rightPointer = rightPointer,
                    destinationPointer = destinationPointer,
                    pointersBeforeTransfer = pointersBefore,
                    pointersAfterTransfer = pointersAfter,
                    currentComparison = comparison,
                    selectedElement = selectedElement,
                    selectedSource = selectedSource,
                    auxiliaryBuffer = buffer.toList(),
                    newlyWrittenBufferSlot = newlyWrittenBufferSlot,
                    copyBackIndex = copyBackIndex,
                    copyBackProgress = copyBackProgress,
                    completedRanges = completedRanges.toList().sortedWith(compareBy<MergeIndexRange> { it.start }.thenBy { it.endExclusive }),
                    returnToNodeId = returnToNodeId,
                    callStack = callStack.toList(),
                    explanation = explanation,
                    badgeText = badgeText,
                    isTerminal = terminal
                )
            )
        }

        snapshot(
            event = FullMergeEvent.START,
            explanation = "$DEFAULT_INPUT_NOTE Input [${input.joinToString(", ")}].",
            badgeText = "READY · INPUT [${input.joinToString(", ")}]"
        )

        fun sort(low: Int, high: Int, parentId: String?, depth: Int) {
            val id = nodeId(low, high)
            val existing = nodes[id]
            nodes[id] = (existing ?: MergeTreeNode(
                nodeId = id,
                parentNodeId = parentId,
                leftChildId = null,
                rightChildId = null,
                rangeStart = low,
                rangeEndExclusive = high,
                callDepth = depth,
                isBaseCase = high - low <= 1,
                hasEntered = false,
                isCompleted = false,
                originalElementIds = originalElements.subList(low, high).map { it.identity },
                currentValues = working.subList(low, high).map { it.value }
            )).copy(hasEntered = true)
            activeNodeId = id
            activeRange = MergeIndexRange(low, high)
            splitIndex = null
            leftRun = emptyList()
            rightRun = emptyList()
            leftPointer = null
            rightPointer = null
            destinationPointer = null
            pointersBefore = null
            pointersAfter = null
            comparison = null
            selectedElement = null
            selectedSource = null
            newlyWrittenBufferSlot = null
            copyBackIndex = null
            copyBackProgress = 0
            returnToNodeId = null
            callStack.add(id)
            snapshot(
                event = FullMergeEvent.CALL_ENTER,
                explanation = "Enter recursive sort for range [${low}..${high}).",
                badgeText = "CALL · [${low}..${high}) · DEPTH $depth"
            )

            if (high - low <= 1) {
                nodes[id] = nodes.getValue(id).copy(isBaseCase = true, isCompleted = true)
                completedRanges.add(MergeIndexRange(low, high))
                snapshot(
                    event = FullMergeEvent.BASE_CASE,
                    explanation = if (low == high) {
                        "BASE CASE: Empty range [${low}..${high}) has no elements to divide."
                    } else {
                        "BASE CASE: Range [${low}..${high}) contains one element (${working[low].value}), which is already sorted."
                    },
                    badgeText = if (low == high) "BASE CASE · EMPTY RANGE" else "BASE CASE · SINGLETON"
                )
                returnFromCall(id, parentId, low, high)
                return
            }

            val middle = low + (high - low) / 2
            val leftId = nodeId(low, middle)
            val rightId = nodeId(middle, high)
            nodes[id] = nodes.getValue(id).copy(leftChildId = leftId, rightChildId = rightId)
            splitIndex = middle
            snapshot(
                event = FullMergeEvent.DIVIDE,
                explanation = "DIVIDE [${low}..${high}) at $middle into left [${low}..${middle}) and right [${middle}..${high}).",
                badgeText = "DIVIDE · [${low}..${high}) → [${low}..${middle}) + [${middle}..${high})"
            )

            sort(low, middle, id, depth + 1)
            sort(middle, high, id, depth + 1)

            activeNodeId = id
            activeRange = MergeIndexRange(low, high)
            splitIndex = middle
            leftRun = working.subList(low, middle).toList()
            rightRun = working.subList(middle, high).toList()
            leftPointer = low
            rightPointer = middle
            destinationPointer = low
            comparison = null
            selectedElement = null
            selectedSource = null
            pointersBefore = null
            pointersAfter = null
            newlyWrittenBufferSlot = null
            copyBackIndex = null
            copyBackProgress = 0
            for (index in low until high) buffer[index] = null
            snapshot(
                event = FullMergeEvent.MERGE_START,
                explanation = "Both child ranges are sorted. Merge [${low}..${middle}) and [${middle}..${high}) into the auxiliary buffer before copying back.",
                badgeText = "MERGE START · [${low}..${high}) · O(N) BUFFER"
            )

            while (leftPointer!! < middle && rightPointer!! < high) {
                val leftIndex = leftPointer!!
                val rightIndex = rightPointer!!
                val leftElement = working[leftIndex]
                val rightElement = working[rightIndex]
                comparison = FullMergeComparison(leftElement, rightElement, leftIndex, rightIndex)
                selectedElement = null
                selectedSource = null
                newlyWrittenBufferSlot = null
                pointersBefore = FullMergePointers(leftIndex, rightIndex, destinationPointer!!)
                pointersAfter = pointersBefore
                snapshot(
                    event = FullMergeEvent.COMPARE,
                    explanation = "Compare left[${leftIndex - low}] = ${leftElement.value} with right[${rightIndex - middle}] = ${rightElement.value}.",
                    badgeText = "COMPARE · ${leftElement.value} vs ${rightElement.value}"
                )
                val source = if (leftElement.value <= rightElement.value) FullMergeSource.LEFT else FullMergeSource.RIGHT
                val chosen = if (source == FullMergeSource.LEFT) leftElement else rightElement
                val before = FullMergePointers(leftPointer!!, rightPointer!!, destinationPointer!!)
                buffer[destinationPointer!!] = chosen
                if (source == FullMergeSource.LEFT) leftPointer = leftPointer!! + 1 else rightPointer = rightPointer!! + 1
                destinationPointer = destinationPointer!! + 1
                pointersBefore = before
                pointersAfter = FullMergePointers(leftPointer!!, rightPointer!!, destinationPointer!!)
                selectedElement = chosen
                selectedSource = source
                newlyWrittenBufferSlot = before.destination
                snapshot(
                    event = FullMergeEvent.WRITE_BUFFER,
                    explanation = if (leftElement.value == rightElement.value && source == FullMergeSource.LEFT) {
                        "Equal values: take the LEFT occurrence ${chosen.identity} first to preserve stability, then write ${chosen.value} to buffer[${before.destination}]."
                    } else {
                        "Take the ${source.name.lowercase()} value ${chosen.value} (${chosen.identity}) and write it to buffer[${before.destination}]."
                    },
                    badgeText = "WRITE BUFFER[${before.destination}] · ${chosen.value}"
                )
            }

            while (leftPointer!! < middle) {
                flushOne(FullMergeSource.LEFT, low, middle, high)
            }
            while (rightPointer!! < high) {
                flushOne(FullMergeSource.RIGHT, low, middle, high)
            }

            copyBackProgress = 0
            comparison = null
            selectedSource = null
            for (index in low until high) {
                val element = buffer[index] ?: error("Missing auxiliary buffer value at index $index")
                working[index] = element
                copyBackIndex = index
                selectedElement = element
                newlyWrittenBufferSlot = null
                copyBackProgress++
                val completesRange = copyBackProgress == high - low
                if (completesRange) {
                    nodes[id] = nodes.getValue(id).copy(isCompleted = true)
                    completedRanges.add(MergeIndexRange(low, high))
                }
                snapshot(
                    event = FullMergeEvent.COPY_BACK,
                    explanation = "Copy buffer[$index] = ${element.value} (${element.identity}) back to working[$index] (${copyBackProgress}/${high - low}).${if (completesRange) " Range [${low}..${high}) is now sorted." else ""}",
                    badgeText = "COPY BACK · $copyBackProgress/${high - low}"
                )
            }

            returnFromCall(id, parentId, low, high)
        }

        flushOne = { source, low, middle, high ->
            val sourceIndex = if (source == FullMergeSource.LEFT) leftPointer!! else rightPointer!!
            val element = working[sourceIndex]
            val destination = destinationPointer!!
            val before = FullMergePointers(leftPointer!!, rightPointer!!, destination)
            buffer[destination] = element
            if (source == FullMergeSource.LEFT) leftPointer = leftPointer!! + 1 else rightPointer = rightPointer!! + 1
            destinationPointer = destination + 1
            pointersBefore = before
            pointersAfter = FullMergePointers(leftPointer!!, rightPointer!!, destinationPointer!!)
            comparison = null
            selectedElement = element
            selectedSource = source
            newlyWrittenBufferSlot = destination
            snapshot(
                event = FullMergeEvent.FLUSH_REMAINING,
                explanation = "${if (source == FullMergeSource.LEFT) "Right" else "Left"} half is exhausted. Flush ${source.name.lowercase()} occurrence ${element.identity} (${element.value}) to buffer[$destination] for [${low}..${high}).",
                badgeText = "FLUSH ${source.name} · BUFFER[$destination] · ${element.value}"
            )
        }

        returnFromCall = { id, parentId, low, high ->
            activeNodeId = id
            activeRange = MergeIndexRange(low, high)
            returnToNodeId = parentId
            snapshot(
                event = FullMergeEvent.RETURN,
                explanation = if (parentId == null) {
                    "Return sorted root range [${low}..${high}) to the caller."
                } else {
                    "Return completed range [${low}..${high}) to parent ${parentId}."
                },
                badgeText = if (parentId == null) "RETURN · ROOT RANGE" else "RETURN · [${low}..${high})"
            )
            callStack.removeAt(callStack.lastIndex)
            activeNodeId = callStack.lastOrNull()
            activeRange = activeNodeId?.let { node ->
                nodes.getValue(node).let { MergeIndexRange(it.rangeStart, it.rangeEndExclusive) }
            }
            returnToNodeId = null
        }

        sort(0, input.size, null, 0)
        activeNodeId = null
        activeRange = null
        splitIndex = null
        comparison = null
        selectedElement = null
        selectedSource = null
        newlyWrittenBufferSlot = null
        copyBackIndex = null
        snapshot(
            event = FullMergeEvent.COMPLETE,
            explanation = "FULL MERGE SORT COMPLETE! Array fully sorted to [${working.joinToString { it.value.toString() }}]. Total Complexity: O(N log N) time, O(N) auxiliary space.",
            badgeText = "SORTED: [${working.joinToString { it.value.toString() }}]",
            terminal = true
        )
        return steps
    }

    private fun nodeId(start: Int, endExclusive: Int): String = "merge_node:$start:$endExclusive"
}

/**
 * Projects the authoritative trace into a learning view without duplicating or
 * synthesizing algorithm states. Detailed mode is the trace itself. Standard
 * mode keeps each merge understandable while reducing repetitive transfers.
 */
internal object MergeSortFullPlayback {
    fun stepsFor(
        trace: List<MergeSortFullTraceStep>,
        mode: FullMergePlaybackMode
    ): List<FullMergePlaybackStep> {
        if (trace.isEmpty()) return emptyList()
        if (mode == FullMergePlaybackMode.DETAILED) {
            return trace.mapIndexed(::FullMergePlaybackStep)
        }

        val selected = linkedSetOf<Int>()
        trace.forEachIndexed { index, step ->
            when (step.event) {
                FullMergeEvent.START,
                FullMergeEvent.DIVIDE,
                FullMergeEvent.BASE_CASE,
                FullMergeEvent.MERGE_START,
                FullMergeEvent.COMPLETE -> selected += index

                FullMergeEvent.RETURN -> if ((step.activeRange?.size ?: 0) > 1) selected += index
                else -> Unit
            }
        }

        val mergeRanges = trace.asSequence()
            .filter { it.event == FullMergeEvent.MERGE_START }
            .mapNotNull { it.activeRange }
            .distinct()
            .toList()
        mergeRanges.forEach { range ->
            val comparisons = trace.indices.filter {
                trace[it].event == FullMergeEvent.COMPARE && trace[it].activeRange == range
            }
            val meaningfulComparisons = representativeIndices(comparisons).toMutableSet()
            comparisons.filterTo(meaningfulComparisons) { index ->
                val pair = trace[index].currentComparison
                pair != null && pair.leftElement.value == pair.rightElement.value
            }
            meaningfulComparisons.forEach { comparisonIndex ->
                selected += comparisonIndex
                val writeIndex = comparisonIndex + 1
                if (writeIndex in trace.indices &&
                    trace[writeIndex].event == FullMergeEvent.WRITE_BUFFER &&
                    trace[writeIndex].activeRange == range
                ) {
                    selected += writeIndex
                }
            }

            val transfers = trace.indices.filter {
                trace[it].activeRange == range &&
                    (trace[it].event == FullMergeEvent.WRITE_BUFFER || trace[it].event == FullMergeEvent.FLUSH_REMAINING)
            }
            selected += representativeIndices(transfers)

            val copyBack = trace.indices.filter {
                trace[it].event == FullMergeEvent.COPY_BACK && trace[it].activeRange == range
            }
            selected += representativeIndices(copyBack)
        }

        return selected.sorted().map { FullMergePlaybackStep(it, trace[it]) }
    }

    fun nearestVisibleIndex(
        originalTraceIndex: Int,
        visibleSteps: List<FullMergePlaybackStep>
    ): Int {
        if (visibleSteps.isEmpty()) return 0
        return visibleSteps.indices.minWithOrNull(
            compareBy<Int> { abs(visibleSteps[it].originalTraceIndex - originalTraceIndex) }
                .thenBy { visibleSteps[it].originalTraceIndex }
        ) ?: 0
    }

    private fun representativeIndices(indices: List<Int>): Set<Int> {
        if (indices.isEmpty()) return emptySet()
        return setOf(indices.first(), indices[indices.lastIndex / 2], indices.last())
    }
}
