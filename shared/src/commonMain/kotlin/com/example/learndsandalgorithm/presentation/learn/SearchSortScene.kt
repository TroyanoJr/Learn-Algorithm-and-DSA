package com.example.learndsandalgorithm.presentation.learn

internal enum class SearchSortAlgorithm {
    LINEAR_SEARCH,
    BINARY_SEARCH,
    BUBBLE_SORT,
    SELECTION_SORT,
    INSERTION_SORT
}

internal enum class SearchSortVisualMode { CELLS, BARS }

/** Complete, immutable teaching state for one search/sort snapshot. */
internal data class SearchSortStepMeta(
    val algorithm: SearchSortAlgorithm,
    val target: Int? = null,
    val currentIndex: Int? = null,
    val inspectedIndices: Set<Int> = emptySet(),
    val checkedCount: Int = 0,
    val leftIndex: Int? = null,
    val midIndex: Int? = null,
    val rightIndex: Int? = null,
    val activeRange: IntRange? = null,
    val eliminatedIndices: Set<Int> = emptySet(),
    val foundIndex: Int? = null,
    val notFound: Boolean = false,
    val highlightedIndices: Set<Int> = emptySet(),
    val sortedIndices: Set<Int> = emptySet(),
    val passIndex: Int? = null,
    val candidateIndex: Int? = null,
    val minimumIndex: Int? = null,
    val key: Int? = null,
    val comparisonTargetIndex: Int? = null,
    val holeIndex: Int? = null,
    val selectionEvent: SelectionSortEvent? = null,
    val insertionEvent: InsertionSortEvent? = null,
    val bubbleCompletion: Boolean = false,
    val explanation: String,
    val statusText: String,
    val isTerminal: Boolean
)

internal data class SearchSortSceneFrame(
    val scene: VisualizationScene,
    val values: List<Int>,
    val visualMode: SearchSortVisualMode,
    val meta: SearchSortStepMeta
)

internal object SearchSortSceneBuilder {
    /** StepsCard displays stepIndex + 1, so scene indices map directly to one-based teaching positions. */
    fun teachingStepIndex(sceneStepIndex: Int, frameCount: Int): Int =
        if (frameCount <= 0) 0 else sceneStepIndex.coerceIn(0, frameCount - 1)

    fun buildLinearSearchFrames(steps: List<LinearSearchTraceStep>, values: List<Int>, target: Int): List<SearchSortSceneFrame> {
        val initial = frame(
            stepIndex = 0,
            values = values,
            visualMode = SearchSortVisualMode.CELLS,
            meta = SearchSortStepMeta(
                algorithm = SearchSortAlgorithm.LINEAR_SEARCH,
                target = target,
                explanation = "Target = $target. Inspect elements sequentially from index 0 until found.",
                statusText = "TARGET: $target",
                isTerminal = false
            )
        )
        return listOf(initial) + steps.mapIndexed { index, step ->
            val found = step.result == LinearSearchResult.FOUND
            val notFound = step.result == LinearSearchResult.NOT_FOUND
            val inspected = when {
                step.currentIndex != null -> (0..step.currentIndex).toSet()
                notFound -> step.values.indices.toSet()
                else -> emptySet()
            }
            frame(
                stepIndex = index + 1,
                values = step.values,
                visualMode = SearchSortVisualMode.CELLS,
                meta = SearchSortStepMeta(
                    algorithm = SearchSortAlgorithm.LINEAR_SEARCH,
                    target = step.target,
                    currentIndex = step.currentIndex,
                    inspectedIndices = inspected,
                    checkedCount = step.inspectedCount,
                    foundIndex = step.foundIndex,
                    notFound = notFound,
                    highlightedIndices = step.currentIndex?.let(::setOf).orEmpty(),
                    explanation = step.explanation,
                    statusText = when {
                        found -> "FOUND AT INDEX ${step.foundIndex} ✓"
                        notFound -> "NOT FOUND"
                        else -> "STEP ${index + 1} OF ${steps.count { it.currentIndex != null }}"
                    },
                    isTerminal = step.isTerminal
                )
            )
        }
    }

    fun buildBinarySearchFrames(steps: List<BinarySearchStep>, values: List<Int>, target: Int): List<SearchSortSceneFrame> {
        val initial = frame(
            0,
            values,
            SearchSortVisualMode.CELLS,
            SearchSortStepMeta(
                algorithm = SearchSortAlgorithm.BINARY_SEARCH,
                target = target,
                explanation = "Target = $target (Sorted array). Compare midpoint with target to eliminate half.",
                statusText = "TARGET: $target (SORTED)",
                isTerminal = false
            )
        )
        return listOf(initial) + steps.mapIndexed { index, step ->
            val found = step.foundIndex != null
            frame(
                index + 1,
                step.values,
                SearchSortVisualMode.CELLS,
                SearchSortStepMeta(
                    algorithm = SearchSortAlgorithm.BINARY_SEARCH,
                    target = target,
                    leftIndex = step.leftIndex,
                    midIndex = step.midIndex,
                    rightIndex = step.rightIndex,
                    activeRange = step.activeRange,
                    eliminatedIndices = step.values.indices.filterNot { it in step.activeRange }.toSet(),
                    foundIndex = step.foundIndex,
                    highlightedIndices = step.highlightedIndices,
                    explanation = step.explanation,
                    statusText = if (found) "FOUND AT INDEX ${step.foundIndex} ✓" else "STEP ${index + 1} OF ${steps.size}",
                    isTerminal = found || index == steps.lastIndex
                )
            )
        }
    }

    fun buildBubbleSortFrames(steps: List<BubbleSortTraceStep>): List<SearchSortSceneFrame> {
        val identities = stableIdentities(steps.map { it.array }, "bubble")
        return steps.mapIndexed { index, step ->
            frame(
                index,
                step.array,
                SearchSortVisualMode.BARS,
                SearchSortStepMeta(
                    algorithm = SearchSortAlgorithm.BUBBLE_SORT,
                    highlightedIndices = step.highlightedIndices.toSet(),
                    sortedIndices = step.sortedIndices.toSet(),
                    bubbleCompletion = step.isCompletion,
                    explanation = step.message,
                    statusText = if (step.isCompletion) "Sorted ✓" else if (index == 0) "O(n²) Worst Case" else "STEP ${index + 1} OF ${steps.size}",
                    isTerminal = step.isCompletion
                ),
                identities[index]
            )
        }
    }

    fun buildSelectionSortFrames(steps: List<SelectionSortTraceStep>): List<SearchSortSceneFrame> {
        val identities = stableIdentities(steps.map { it.array }, "selection")
        return steps.mapIndexed { index, step ->
            frame(
                index,
                step.array,
                SearchSortVisualMode.BARS,
                SearchSortStepMeta(
                    algorithm = SearchSortAlgorithm.SELECTION_SORT,
                    highlightedIndices = step.highlightedIndices.toSet(),
                    sortedIndices = step.sortedIndices.toSet(),
                    passIndex = step.passIndex,
                    candidateIndex = step.comparisonTargetIndex,
                    minimumIndex = step.minimumIndex,
                    comparisonTargetIndex = step.comparisonTargetIndex,
                    selectionEvent = step.event,
                    explanation = step.message,
                    statusText = if (step.event == SelectionSortEvent.COMPLETE) "Sorted ✓" else if (index == 0) "O(n²) Comparisons" else "STEP ${index + 1} OF ${steps.size}",
                    isTerminal = step.event == SelectionSortEvent.COMPLETE
                ),
                identities[index]
            )
        }
    }

    fun buildInsertionSortFrames(steps: List<InsertionSortTraceStep>): List<SearchSortSceneFrame> {
        val identities = stableIdentities(steps.map { it.array }, "insertion")
        return steps.mapIndexed { index, step ->
            frame(
                index,
                step.array,
                SearchSortVisualMode.BARS,
                SearchSortStepMeta(
                    algorithm = SearchSortAlgorithm.INSERTION_SORT,
                    highlightedIndices = step.highlightedIndices.toSet(),
                    sortedIndices = step.sortedIndices.toSet(),
                    passIndex = step.insertionIndex,
                    key = step.key,
                    comparisonTargetIndex = step.comparisonTargetIndex,
                    holeIndex = step.holeIndex,
                    insertionEvent = step.event,
                    explanation = step.message,
                    statusText = if (step.event == InsertionSortEvent.COMPLETE) "Sorted ✓" else if (index == 0) "O(n) Best Case" else "STEP ${index + 1} OF ${steps.size}",
                    isTerminal = step.event == InsertionSortEvent.COMPLETE
                ),
                identities[index]
            )
        }
    }

    private fun frame(
        stepIndex: Int,
        values: List<Int>,
        visualMode: SearchSortVisualMode,
        meta: SearchSortStepMeta,
        identities: List<String> = values.indices.map { "${meta.algorithm.name.lowercase()}:item:$it" }
    ): SearchSortSceneFrame {
        require(identities.size == values.size) { "Every rendered value requires one stable identity" }
        val entities = values.mapIndexed { index, value ->
            val state = when {
                index == meta.foundIndex -> EntityVisualState.TARGET
                index in meta.highlightedIndices -> EntityVisualState.ACTIVE
                index in meta.sortedIndices || index in meta.inspectedIndices -> EntityVisualState.VISITED
                index in meta.eliminatedIndices -> EntityVisualState.VIOLATION
                else -> EntityVisualState.DEFAULT
            }
            VisualEntity(
                visualKey = identities[index],
                elementId = identities[index],
                dataValue = value.toString(),
                slotKey = "array_slot:$index",
                visualState = state
            )
        }
        return SearchSortSceneFrame(
            scene = VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "${meta.algorithm.name.lowercase()}_step_$stepIndex",
                subScenes = listOf(
                    StructureSubScene(
                        subPaneId = "array",
                        type = StructureType.ARRAY,
                        minRequiredWidthDp = 120f,
                        minRequiredHeightDp = if (visualMode == SearchSortVisualMode.BARS) 170f else 100f,
                        entities = entities
                    )
                ),
                globalMessage = meta.explanation
            ),
            values = values.toList(),
            visualMode = visualMode,
            meta = meta
        )
    }

    /** Assigns identity by value and occurrence rank within each snapshot; this does not imply physical identity continuity. */
    private fun stableIdentities(snapshots: List<List<Int>>, prefix: String): List<List<String>> {
        return snapshots.map { values ->
            val occurrenceByValue = mutableMapOf<Int, Int>()
            values.map { value ->
                val occurrence = occurrenceByValue.getOrDefault(value, 0)
                occurrenceByValue[value] = occurrence + 1
                "$prefix:value:$value:occurrence:$occurrence"
            }
        }
    }
}

internal data class SearchSortSceneLayoutResult(
    val cellBounds: Map<Int, Rect2D>,
    val valueBounds: Map<Int, Rect2D>,
    val indexBounds: Map<Int, Rect2D>,
    val barTrackBounds: Map<Int, Rect2D>,
    val markerBounds: Map<Int, Rect2D>,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object SearchSortSceneLayout {
    private const val MARGIN = 14f
    private const val GAP = 7f
    private const val CELL_WIDTH = 40f
    private const val CELL_HEIGHT = 42f
    private const val POINTER_HEIGHT = 20f
    private const val INDEX_HEIGHT = 16f
    private const val BAR_TRACK_HEIGHT = 116f

    fun calculate(frames: List<SearchSortSceneFrame>): SearchSortSceneLayoutResult {
        if (frames.isEmpty()) return SearchSortSceneLayoutResult(emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), 0f, 0f)
        val count = frames.maxOf { it.values.size }
        val mode = frames.first().visualMode
        require(frames.all { it.visualMode == mode }) { "Search/sort visual mode must remain sequence-stable" }
        val itemWidth = if (mode == SearchSortVisualMode.CELLS) CELL_WIDTH else 34f
        val width = MARGIN * 2 + count * itemWidth + (count - 1).coerceAtLeast(0) * GAP
        val cells = linkedMapOf<Int, Rect2D>()
        val values = linkedMapOf<Int, Rect2D>()
        val indices = linkedMapOf<Int, Rect2D>()
        val tracks = linkedMapOf<Int, Rect2D>()
        val markers = linkedMapOf<Int, Rect2D>()
        repeat(count) { index ->
            val left = MARGIN + index * (itemWidth + GAP)
            if (mode == SearchSortVisualMode.CELLS) {
                cells[index] = Rect2D(left, MARGIN + POINTER_HEIGHT + 4f, itemWidth, CELL_HEIGHT)
                values[index] = cells.getValue(index)
                indices[index] = Rect2D(left, cells.getValue(index).bottom + 4f, itemWidth, INDEX_HEIGHT)
            } else {
                values[index] = Rect2D(left, MARGIN, itemWidth, 18f)
                markers[index] = Rect2D(left - 4f, MARGIN + 20f, itemWidth + 8f, 20f)
                tracks[index] = Rect2D(left + 4f, MARGIN + 44f, itemWidth - 8f, BAR_TRACK_HEIGHT)
                indices[index] = Rect2D(left, MARGIN + 44f + BAR_TRACK_HEIGHT + 4f, itemWidth, INDEX_HEIGHT)
            }
        }
        val height = if (mode == SearchSortVisualMode.CELLS) MARGIN * 2 + POINTER_HEIGHT + 4f + CELL_HEIGHT + 4f + INDEX_HEIGHT
        else MARGIN * 2 + 44f + BAR_TRACK_HEIGHT + 4f + INDEX_HEIGHT
        return SearchSortSceneLayoutResult(cells, values, indices, tracks, markers, width, height)
    }

    fun markerBounds(frame: SearchSortSceneFrame, layout: SearchSortSceneLayoutResult): Map<Int, Rect2D> =
        if (frame.visualMode != SearchSortVisualMode.BARS) emptyMap()
        else frame.values.indices.filter { index ->
            index == frame.meta.minimumIndex || index == frame.meta.candidateIndex ||
                index == frame.meta.comparisonTargetIndex || index == frame.meta.holeIndex ||
                (frame.meta.algorithm == SearchSortAlgorithm.INSERTION_SORT && index == frame.meta.holeIndex && frame.meta.key != null)
        }.associateWith { layout.markerBounds.getValue(it) }

    fun pointerBounds(frame: SearchSortSceneFrame, layout: SearchSortSceneLayoutResult): Map<String, Rect2D> {
        val labelsByIndex = linkedMapOf<Int, MutableList<String>>()
        fun add(label: String, index: Int?) {
            if (index != null && index in layout.cellBounds) labelsByIndex.getOrPut(index) { mutableListOf() }.add(label)
        }
        add("L", frame.meta.leftIndex)
        add("M", frame.meta.midIndex)
        add("R", frame.meta.rightIndex)
        return labelsByIndex.map { (index, labels) ->
            val cell = layout.cellBounds.getValue(index)
            labels.joinToString(",") to Rect2D(cell.left, MARGIN, cell.width, POINTER_HEIGHT)
        }.toMap(linkedMapOf())
    }

    fun initialFitScale(layout: SearchSortSceneLayoutResult, viewportWidth: Float, viewportHeight: Float): Float =
        if (layout.requiredSceneWidth <= 0f || layout.requiredSceneHeight <= 0f || viewportWidth <= 0f || viewportHeight <= 0f) 1f
        else minOf(1f, viewportWidth / layout.requiredSceneWidth, viewportHeight / layout.requiredSceneHeight)
}
