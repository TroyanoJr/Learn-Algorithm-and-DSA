package com.example.learndsandalgorithm.presentation.learn

internal data class SceneConnectorGeometry(
    val id: String,
    val fromX: Float,
    val fromY: Float,
    val toX: Float,
    val toY: Float
)

internal data class IterationRecursionSceneLayoutResult(
    val lesson: IterationRecursionLesson,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float,
    val bounds: Map<String, Rect2D>,
    val connectors: List<SceneConnectorGeometry> = emptyList()
) {
    fun boundsFor(id: String): Rect2D = bounds.getValue(id)
}

internal data class TradeoffAuxiliaryMemoryState(
    val iterationStateCount: Int,
    val recursionActiveFrameCount: Int,
    val recursionMaximumFrameCount: Int
)

internal object IterationRecursionSceneLayout {
    private const val MARGIN = 12f

    fun calculate(frames: List<IterationRecursionSceneFrame>): IterationRecursionSceneLayoutResult {
        require(frames.isNotEmpty()) { "Iteration/recursion scene sequence cannot be empty" }
        val lesson = frames.first().meta.lesson
        require(frames.all { it.meta.lesson == lesson }) { "Scene sequences must contain exactly one lesson" }
        return when (lesson) {
            IterationRecursionLesson.ITERATION_FUNDAMENTALS -> iteration(frames)
            IterationRecursionLesson.NESTED_ITERATION -> nested(frames)
            IterationRecursionLesson.RECURSION_FUNDAMENTALS -> recursion(frames)
            IterationRecursionLesson.RECURSION_TRADEOFFS -> tradeoffs(frames)
        }
    }

    private fun iteration(frames: List<IterationRecursionSceneFrame>): IterationRecursionSceneLayoutResult {
        val count = frames.maxOf { it.meta.limit }.coerceAtLeast(1)
        val width = maxOf(480f, MARGIN * 2 + count * 92f + (count - 1) * 10f)
        val b = linkedMapOf<String, Rect2D>()
        b["header"] = Rect2D(MARGIN, MARGIN, width - MARGIN * 2, 62f)
        b["header_title"] = Rect2D(24f, 18f, width - 48f, 18f)
        b["header_status"] = Rect2D(24f, 38f, width - 48f, 28f)
        val phaseWidth = (width - MARGIN * 2 - 32f) / 3f
        listOf("CHECK CONDITION", "EXECUTE BODY", "UPDATE STATE").forEachIndexed { index, _ ->
            b["phase:$index"] = Rect2D(MARGIN + index * (phaseWidth + 16f), 88f, phaseWidth, 42f)
        }
        b["variables"] = Rect2D(MARGIN, 146f, width - MARGIN * 2, 54f)
        b["variables_text"] = Rect2D(24f, 152f, width - 48f, 42f)
        val cellWidth = (width - MARGIN * 2 - (count - 1) * 10f) / count
        repeat(count) { index ->
            b["iteration:pass:${index + 1}"] = Rect2D(MARGIN + index * (cellWidth + 10f), 226f, cellWidth, 48f)
            b["iteration:label:${index + 1}"] = Rect2D(MARGIN + index * (cellWidth + 10f), 206f, cellWidth, 16f)
        }
        val connectors = (0..1).map { index ->
            val from = b.getValue("phase:$index")
            val to = b.getValue("phase:${index + 1}")
            SceneConnectorGeometry("phase:$index->${index + 1}", from.right + 2f, from.top + from.height / 2f, to.left - 2f, to.top + to.height / 2f)
        }
        return IterationRecursionSceneLayoutResult(frames.first().meta.lesson, width, 290f, b, connectors)
    }

    private fun nested(frames: List<IterationRecursionSceneFrame>): IterationRecursionSceneLayoutResult {
        val rows = frames.maxOf { it.meta.outerLimit }.coerceAtLeast(1)
        val columns = frames.maxOf { it.meta.innerLimit }.coerceAtLeast(1)
        val width = maxOf(480f, 92f + columns * 104f + (columns - 1) * 8f + MARGIN)
        val b = linkedMapOf<String, Rect2D>()
        b["header"] = Rect2D(MARGIN, MARGIN, width - MARGIN * 2, 62f)
        b["header_title"] = Rect2D(24f, 18f, width - 48f, 18f)
        b["header_status"] = Rect2D(24f, 38f, width - 48f, 28f)
        b["reset"] = Rect2D(MARGIN, 86f, width - MARGIN * 2, 38f)
        val gridLeft = 76f
        val cellWidth = (width - gridLeft - MARGIN - (columns - 1) * 8f) / columns
        repeat(columns) { column -> b["column:${column + 1}"] = Rect2D(gridLeft + column * (cellWidth + 8f), 132f, cellWidth, 18f) }
        repeat(rows) { row ->
            b["row:${row + 1}"] = Rect2D(MARGIN, 158f + row * 58f, 54f, 46f)
            repeat(columns) { column ->
                b["nested:cell:${row + 1}:${column + 1}"] = Rect2D(gridLeft + column * (cellWidth + 8f), 158f + row * 58f, cellWidth, 46f)
            }
        }
        return IterationRecursionSceneLayoutResult(frames.first().meta.lesson, width, 170f + rows * 58f, b)
    }

    private fun recursion(frames: List<IterationRecursionSceneFrame>): IterationRecursionSceneLayoutResult {
        val maxN = frames.maxOf { it.meta.inputN }.coerceAtLeast(0)
        val frameCount = maxN + 1
        val width = maxOf(520f, MARGIN * 2 + frameCount * 102f + (frameCount - 1) * 18f)
        val frameTop = 112f
        val frameHeight = 88f
        val returnLabelTop = frameTop + frameHeight + 12f
        val returnTop = returnLabelTop + 28f
        val returnHeight = 58f
        val b = linkedMapOf<String, Rect2D>()
        b["header"] = Rect2D(MARGIN, MARGIN, width - MARGIN * 2, 66f)
        b["header_title"] = Rect2D(24f, 18f, width - 48f, 18f)
        b["header_status"] = Rect2D(24f, 38f, width - 48f, 32f)
        val frameWidth = (width - MARGIN * 2 - (frameCount - 1) * 18f) / frameCount
        repeat(frameCount) { index ->
            val n = maxN - index
            val frameLeft = MARGIN + index * (frameWidth + 18f)
            b["recursion:frame:$n"] = Rect2D(frameLeft, frameTop, frameWidth, frameHeight)
            b["recursion:frame:$n:name"] = Rect2D(frameLeft + 6f, frameTop + 7f, frameWidth - 12f, 22f)
            b["recursion:frame:$n:status"] = Rect2D(frameLeft + 6f, frameTop + 34f, frameWidth - 12f, 46f)

            val returnLeft = MARGIN + (frameCount - 1 - index) * (frameWidth + 18f)
            b["recursion:return:$n"] = Rect2D(returnLeft, returnTop, frameWidth, returnHeight)
            b["recursion:return:$n:name"] = Rect2D(returnLeft + 6f, returnTop + 6f, frameWidth - 12f, 20f)
            b["recursion:return:$n:value"] = Rect2D(returnLeft + 6f, returnTop + 30f, frameWidth - 12f, 20f)
        }
        b["call_label"] = Rect2D(MARGIN, 84f, width - MARGIN * 2, 20f)
        b["return_label"] = Rect2D(MARGIN, returnLabelTop, width - MARGIN * 2, 20f)
        val connectors = (0 until frameCount - 1).map { index ->
            val from = b.getValue("recursion:frame:${maxN - index}")
            val to = b.getValue("recursion:frame:${maxN - index - 1}")
            SceneConnectorGeometry("call:${maxN - index}", from.right + 2f, from.top + from.height / 2f, to.left - 2f, to.top + to.height / 2f)
        }
        val requiredHeight = b.values.maxOf { it.bottom } + MARGIN
        return IterationRecursionSceneLayoutResult(frames.first().meta.lesson, width, requiredHeight, b, connectors)
    }

    private fun tradeoffs(frames: List<IterationRecursionSceneFrame>): IterationRecursionSceneLayoutResult {
        val n = frames.maxOf { it.meta.inputN }.coerceAtLeast(1)
        // One execution band at a time uses the full width. Bounds are sequence-wide;
        // drawing only active calls and completed returns does not move their identities.
        val width = maxOf(480f, 24f + (n + 1) * 82f + n * 6f)
        val innerWidth = width - 40f
        val b = linkedMapOf<String, Rect2D>()
        val iterationTop = 12f
        val iterationHeight = 82f
        b["iteration_panel"] = Rect2D(12f, iterationTop, width - 24f, iterationHeight)
        b["iteration_current"] = Rect2D(20f, 18f, innerWidth, 30f)
        b["iteration_progress_label"] = Rect2D(20f, 48f, innerWidth, 18f)
        val iterationCellWidth = (innerWidth - (n - 1) * 6f) / n
        repeat(n) { index ->
            b["tradeoff:iteration:${index + 1}"] = Rect2D(
                20f + index * (iterationCellWidth + 6f), 68f, iterationCellWidth, 20f
            )
        }

        val recursionTop = 102f
        val recursionHeight = 112f
        b["recursion_panel"] = Rect2D(12f, recursionTop, width - 24f, recursionHeight)
        b["recursion_heading"] = Rect2D(20f, 108f, innerWidth, 20f)
        b["recursion_current"] = Rect2D(20f, 128f, innerWidth, 25f)
        val stackWidth = (innerWidth - n * 6f) / (n + 1)
        repeat(n + 1) { index ->
            val value = n - index
            val left = 20f + index * (stackWidth + 6f)
            b["tradeoff:frame:$value"] = Rect2D(left, 158f, stackWidth, 48f)
            b["tradeoff:frame:$value:name"] = Rect2D(left + 4f, 162f, stackWidth - 8f, 19f)
            b["tradeoff:frame:$value:status"] = Rect2D(left + 4f, 183f, stackWidth - 8f, 18f)
        }

        b["return_history_label"] = Rect2D(12f, 220f, width - 24f, 19f)
        repeat(n + 1) { value ->
            val left = 20f + value * (stackWidth + 6f)
            b["tradeoff:return:$value"] = Rect2D(left, 242f, stackWidth, 38f)
            b["tradeoff:return:$value:name"] = Rect2D(left + 4f, 244f, stackWidth - 8f, 17f)
            b["tradeoff:return:$value:value"] = Rect2D(left + 4f, 262f, stackWidth - 8f, 16f)
        }
        return IterationRecursionSceneLayoutResult(frames.first().meta.lesson, width, 292f, b)
    }

    fun initialFitScale(layout: IterationRecursionSceneLayoutResult, viewportWidth: Float, viewportHeight: Float): Float =
        if (viewportWidth <= 0f || viewportHeight <= 0f) 1f
        else minOf(1f, viewportWidth / layout.requiredSceneWidth, viewportHeight / layout.requiredSceneHeight)

    fun textFallbackScale(
        contentWidth: Float,
        contentHeight: Float,
        availableWidth: Float,
        availableHeight: Float
    ): Float {
        if (contentWidth <= 0f || contentHeight <= 0f) return 1f
        if (availableWidth <= 0f || availableHeight <= 0f) return 0f
        return minOf(1f, availableWidth / contentWidth, availableHeight / contentHeight).coerceAtLeast(0f)
    }

    fun completedTradeoffReturns(
        frames: List<IterationRecursionSceneFrame>,
        currentStepIndex: Int
    ): Map<Int, Int> = buildMap {
        frames.take(currentStepIndex + 1).forEach { frame ->
            when (frame.meta.action) {
                "RECURSIVE_BASE" -> put(0, frame.meta.recurReturnValue ?: 0)
                "RECURSIVE_ASCENT" -> {
                    val returnedN = frame.meta.returningFrameN
                    val returnedValue = frame.meta.recurReturnValue
                    if (returnedN != null && returnedValue != null) put(returnedN, returnedValue)
                }
            }
        }
    }

    fun latestCompletedTradeoffReturn(
        frames: List<IterationRecursionSceneFrame>,
        currentStepIndex: Int
    ): Int? = completedTradeoffReturns(frames, currentStepIndex).keys.lastOrNull()

    fun tradeoffAuxiliaryMemory(frame: IterationRecursionSceneFrame): TradeoffAuxiliaryMemoryState {
        require(frame.meta.lesson == IterationRecursionLesson.RECURSION_TRADEOFFS)
        return TradeoffAuxiliaryMemoryState(
            iterationStateCount = 1,
            recursionActiveFrameCount = frame.meta.recurCallStack.size,
            recursionMaximumFrameCount = frame.meta.inputN + 1
        )
    }

    fun tradeoffReturnConnector(
        layout: IterationRecursionSceneLayoutResult,
        frame: IterationRecursionSceneFrame
    ): SceneConnectorGeometry? {
        require(frame.meta.lesson == IterationRecursionLesson.RECURSION_TRADEOFFS)
        if (frame.meta.action != "RECURSIVE_ASCENT") return null
        val returningN = frame.meta.returningFrameN ?: return null
        val receivingN = frame.meta.receivingCallerN ?: return null
        val returning = layout.bounds["tradeoff:frame:$returningN"] ?: return null
        val receiving = layout.bounds["tradeoff:frame:$receivingN"] ?: return null
        val connectorY = maxOf(returning.bottom, receiving.bottom) + 4f
        return SceneConnectorGeometry(
            id = "tradeoff:return:$returningN->$receivingN",
            fromX = returning.left + returning.width / 2f,
            fromY = connectorY,
            toX = receiving.left + receiving.width / 2f,
            toY = connectorY
        )
    }
}
