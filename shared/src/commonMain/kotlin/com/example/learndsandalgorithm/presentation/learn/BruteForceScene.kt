package com.example.learndsandalgorithm.presentation.learn

internal enum class BruteForceLesson { FUNDAMENTALS, PAIR_PROBLEMS, STRING_MATCHING }

internal data class BruteForceTeachingMeta(
    val lesson: BruteForceLesson,
    val eventName: String,
    val badgeText: String,
    val explanation: String,
    val isTerminal: Boolean,
    val range: IntRange? = null,
    val target: Int? = null,
    val currentCandidate: Int? = null,
    val currentSquare: Int? = null,
    val foundSolution: Int? = null,
    val evaluations: List<CandidateEvaluation> = emptyList(),
    val values: List<Int> = emptyList(),
    val currentI: Int? = null,
    val currentJ: Int? = null,
    val currentSum: Int? = null,
    val foundPair: Pair<Int, Int>? = null,
    val evaluatedPairs: List<EvaluatedPair> = emptyList(),
    val text: String = "",
    val pattern: String = "",
    val alignmentStart: Int? = null,
    val comparisonOffset: Int? = null,
    val comparisonMatches: Boolean? = null,
    val matchedOffsets: Set<Int> = emptySet(),
    val mismatchOffset: Int? = null,
    val foundMatches: List<Int> = emptyList(),
    val fullAlignmentMatch: Boolean = false
)

internal data class BruteForceSceneFrame(
    val scene: VisualizationScene,
    val meta: BruteForceTeachingMeta
)

internal object BruteForceSceneBuilder {
    fun fundamentals(steps: List<BruteForceFundamentalsTraceStep>): List<BruteForceSceneFrame> =
        steps.mapIndexed { index, step ->
            val meta = BruteForceTeachingMeta(
                lesson = BruteForceLesson.FUNDAMENTALS,
                eventName = step.event.name,
                badgeText = step.badgeText,
                explanation = step.explanation,
                isTerminal = step.isTerminal,
                range = step.rangeMin..step.rangeMax,
                target = step.targetSquare,
                currentCandidate = step.currentCandidate,
                currentSquare = step.currentSquare,
                foundSolution = step.foundSolution,
                evaluations = step.evaluatedCandidates
            )
            frame(index, meta, (step.rangeMin..step.rangeMax).map { candidate ->
                val evaluation = step.evaluatedCandidates.firstOrNull { it.candidate == candidate }
                VisualEntity(
                    visualKey = "fundamentals:candidate:$candidate",
                    elementId = "fundamentals:candidate:$candidate",
                    dataValue = candidate.toString(),
                    slotKey = "candidate:$candidate",
                    visualState = when {
                        evaluation?.isMatch == true -> EntityVisualState.TARGET
                        step.event == FundamentalsSearchEvent.EVALUATE_CANDIDATE && candidate == step.currentCandidate -> EntityVisualState.ACTIVE
                        evaluation != null -> EntityVisualState.VISITED
                        else -> EntityVisualState.DEFAULT
                    }
                )
            })
        }

    fun pairs(steps: List<BruteForcePairProblemsTraceStep>): List<BruteForceSceneFrame> =
        steps.mapIndexed { index, step ->
            val meta = BruteForceTeachingMeta(
                lesson = BruteForceLesson.PAIR_PROBLEMS,
                eventName = step.event.name,
                badgeText = step.badgeText,
                explanation = step.explanation,
                isTerminal = step.isTerminal,
                target = step.target,
                values = step.values,
                currentI = step.currentI,
                currentJ = step.currentJ,
                currentSum = step.currentSum,
                foundPair = step.foundPair,
                evaluatedPairs = step.evaluatedPairs
            )
            frame(index, meta, step.values.mapIndexed { slot, value ->
                VisualEntity(
                    visualKey = "pair:item:$slot",
                    elementId = "pair:item:$slot",
                    dataValue = value.toString(),
                    slotKey = "pair:$slot",
                    visualState = when {
                        step.foundPair?.let { slot == it.first || slot == it.second } == true -> EntityVisualState.TARGET
                        slot == step.currentI || slot == step.currentJ -> EntityVisualState.ACTIVE
                        else -> EntityVisualState.DEFAULT
                    }
                )
            })
        }

    fun stringMatching(steps: List<NaiveStringMatchingTraceStep>): List<BruteForceSceneFrame> =
        steps.mapIndexed { index, step ->
            val validMaxStart = (step.text.length - step.pattern.length).coerceAtLeast(0)
            val displayStart = when {
                step.pattern.isEmpty() || step.pattern.length > step.text.length -> null
                step.currentStart != null -> step.currentStart
                step.foundMatches.isNotEmpty() -> step.foundMatches.last()
                else -> 0
            }?.coerceIn(0, validMaxStart)
            val meta = BruteForceTeachingMeta(
                lesson = BruteForceLesson.STRING_MATCHING,
                eventName = step.event.name,
                badgeText = step.badgeText,
                explanation = step.explanation,
                isTerminal = step.isTerminal,
                text = step.text,
                pattern = step.pattern,
                alignmentStart = displayStart,
                comparisonOffset = step.currentOffset,
                comparisonMatches = step.isCharMatch,
                matchedOffsets = when {
                    step.event == StringMatchEvent.ALIGNMENT_MATCH -> step.pattern.indices.toSet()
                    step.currentOffset == null -> emptySet()
                    step.isCharMatch == true -> (0..step.currentOffset).toSet()
                    else -> (0 until step.currentOffset).toSet()
                },
                mismatchOffset = step.currentOffset?.takeIf { step.isCharMatch == false },
                foundMatches = step.foundMatches,
                fullAlignmentMatch = step.event == StringMatchEvent.ALIGNMENT_MATCH
            )
            val textEntities = step.text.mapIndexed { slot, character ->
                VisualEntity("text:$slot", "text:$slot", character.toString(), "text:$slot")
            }
            val patternEntities = step.pattern.mapIndexed { offset, character ->
                VisualEntity("pattern:$offset", "pattern:$offset", character.toString(), "pattern:$offset")
            }
            frame(index, meta, textEntities, patternEntities)
        }

    private fun frame(
        index: Int,
        meta: BruteForceTeachingMeta,
        primary: List<VisualEntity>,
        secondary: List<VisualEntity> = emptyList()
    ) = BruteForceSceneFrame(
        VisualizationScene(
            stepIndex = index,
            sceneId = "${meta.lesson.name.lowercase()}_step_$index",
            subScenes = buildList {
                add(StructureSubScene("primary", StructureType.ARRAY, entities = primary))
                if (secondary.isNotEmpty()) add(StructureSubScene("pattern", StructureType.ARRAY, entities = secondary))
            },
            globalMessage = meta.explanation
        ),
        meta
    )
}

internal fun fundamentalsEvaluationText(meta: BruteForceTeachingMeta): String = when (meta.eventName) {
    "START" -> "Ready · test each x where x² = ${meta.target}"
    "EVALUATE_CANDIDATE" -> "${meta.currentCandidate} × ${meta.currentCandidate} = ${meta.currentSquare} ≠ ${meta.target}"
    "SOLUTION_FOUND" -> "${meta.currentCandidate} × ${meta.currentCandidate} = ${meta.currentSquare} = ${meta.target}"
    "SEARCH_COMPLETE" -> "Search exhausted: no x in [${meta.range?.first}..${meta.range?.last}] has x² = ${meta.target}"
    else -> "Evaluation unavailable"
}

internal fun fundamentalsStatusLabel(eventName: String): String = when (eventName) {
    "START" -> "READY"
    "EVALUATE_CANDIDATE" -> "TESTING"
    "SOLUTION_FOUND" -> "MATCH FOUND"
    "SEARCH_COMPLETE" -> "NO MATCH"
    else -> eventName
}

internal fun pairEvaluationText(meta: BruteForceTeachingMeta): String = when (meta.eventName) {
    "START" -> "Ready · find two distinct indices whose values sum to ${meta.target}"
    "EVALUATE_PAIR" -> "values[${meta.currentI}] + values[${meta.currentJ}] = ${meta.values.getOrNull(meta.currentI ?: -1)} + ${meta.values.getOrNull(meta.currentJ ?: -1)} = ${meta.currentSum} ≠ ${meta.target}"
    "PAIR_FOUND" -> "values[${meta.currentI}] + values[${meta.currentJ}] = ${meta.values.getOrNull(meta.currentI ?: -1)} + ${meta.values.getOrNull(meta.currentJ ?: -1)} = ${meta.currentSum} = ${meta.target}"
    "NO_PAIR_FOUND" -> "Search exhausted · no pair sums to ${meta.target}"
    else -> "Evaluation unavailable"
}

internal fun pairStatusLabel(eventName: String): String = when (eventName) {
    "START" -> "READY"
    "EVALUATE_PAIR" -> "TESTING PAIR"
    "PAIR_FOUND" -> "PAIR FOUND"
    "NO_PAIR_FOUND" -> "NO PAIR FOUND"
    else -> eventName
}

internal fun currentPairHistoryIndex(meta: BruteForceTeachingMeta): Int? =
    if (meta.eventName in setOf("EVALUATE_PAIR", "PAIR_FOUND") && meta.evaluatedPairs.isNotEmpty()) meta.evaluatedPairs.lastIndex else null

internal fun pairHistoryLines(pair: EvaluatedPair): Pair<String, String> =
    "(${pair.i},${pair.j})" to "${pair.valI} + ${pair.valJ} = ${pair.sum} ${if (pair.isMatch) "✓" else "✕"}"

internal fun pairHistoryColumnCount(pairs: List<EvaluatedPair>, sceneWidth: Float): Int {
    val twoColumnCardWidth = (sceneWidth - 24f - 6f) / 2f
    val textWidthAtMinimumReadableSize = pairs.maxOfOrNull { pairHistoryLines(it).second.length * 6f } ?: 0f
    return if (textWidthAtMinimumReadableSize <= twoColumnCardWidth - 16f) 2 else 1
}

internal data class BruteForceSceneLayoutResult(
    val headerBounds: Rect2D,
    val titleBounds: Rect2D,
    val goalBounds: Rect2D,
    val statusBounds: Rect2D,
    val evaluationBounds: Rect2D,
    val cellBounds: Map<Int, Rect2D>,
    val primaryTextBounds: Map<Int, Rect2D>,
    val secondaryTextBounds: Map<Int, Rect2D>,
    val labelBounds: Map<Int, Rect2D>,
    val secondaryLabelBounds: Map<Int, Rect2D>,
    val indexBounds: Map<Int, Rect2D>,
    val historyBounds: Map<Int, Rect2D>,
    val currentSumBounds: Rect2D,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object BruteForceSceneLayout {
    private const val MARGIN = 12f
    private const val WIDTH = 420f
    private const val CELL = 52f
    private const val GAP = 10f
    private const val PAIR_EVALUATION_TOP = 166f
    private const val PAIR_EVALUATION_HEIGHT = 80f
    private const val PAIR_CURRENT_SUM_TOP = 254f
    private const val PAIR_HISTORY_TOP = 286f
    private const val PAIR_HISTORY_CARD_HEIGHT = 40f
    private const val PAIR_HISTORY_ROW_GAP = 6f
    private const val PAIR_HISTORY_COLUMN_GAP = 6f

    fun calculate(frames: List<BruteForceSceneFrame>): BruteForceSceneLayoutResult {
        if (frames.isEmpty()) return BruteForceSceneLayoutResult(
            Rect2D(0f, 0f, 0f, 0f), Rect2D(0f, 0f, 0f, 0f), Rect2D(0f, 0f, 0f, 0f), Rect2D(0f, 0f, 0f, 0f),
            Rect2D(0f, 0f, 0f, 0f), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), Rect2D(0f, 0f, 0f, 0f), 0f, 0f
        )
        val lesson = frames.first().meta.lesson
        require(frames.all { it.meta.lesson == lesson }) { "Brute-force scene sequences must contain one lesson" }
        val maxItems = when (lesson) {
            BruteForceLesson.FUNDAMENTALS -> frames.maxOf { it.meta.range?.count() ?: 0 }
            BruteForceLesson.PAIR_PROBLEMS -> frames.maxOf { it.meta.values.size }
            BruteForceLesson.STRING_MATCHING -> frames.maxOf { maxOf(it.meta.text.length, it.meta.pattern.length) }
        }
        val naturalWidth = MARGIN * 2 + maxItems * CELL + (maxItems - 1).coerceAtLeast(0) * GAP
        val minimumWidth = if (lesson == BruteForceLesson.STRING_MATCHING) 520f else WIDTH
        val width = maxOf(minimumWidth, naturalWidth)
        val header = Rect2D(MARGIN, MARGIN, width - MARGIN * 2, 48f)
        val title = Rect2D(MARGIN + 10f, MARGIN + 5f, width - 170f, 17f)
        val goal = Rect2D(MARGIN + 10f, MARGIN + 24f, width - 170f, 17f)
        val status = Rect2D(width - 142f, MARGIN + 8f, 120f, 30f)
        val evaluation = when (lesson) {
            BruteForceLesson.FUNDAMENTALS -> Rect2D(MARGIN, 148f, width - MARGIN * 2, 48f)
            BruteForceLesson.PAIR_PROBLEMS -> Rect2D(MARGIN, PAIR_EVALUATION_TOP, width - MARGIN * 2, PAIR_EVALUATION_HEIGHT)
            BruteForceLesson.STRING_MATCHING -> Rect2D(0f, 0f, 0f, 0f)
        }
        val cells = linkedMapOf<Int, Rect2D>()
        val labels = linkedMapOf<Int, Rect2D>()
        val primaryTexts = linkedMapOf<Int, Rect2D>()
        val secondaryTexts = linkedMapOf<Int, Rect2D>()
        val secondaryLabels = linkedMapOf<Int, Rect2D>()
        val indices = linkedMapOf<Int, Rect2D>()
        val gridWidth = maxItems * CELL + (maxItems - 1).coerceAtLeast(0) * GAP
        val left = (width - gridWidth) / 2f
        val baseTop = 88f
        repeat(maxItems) { item ->
            val x = left + item * (CELL + GAP)
            labels[item] = Rect2D(x - 3f, baseTop - 22f, CELL + 6f, 18f)
            cells[item] = Rect2D(x, baseTop, CELL, 46f)
            primaryTexts[item] = Rect2D(x, baseTop + 4f, CELL, 18f)
            secondaryTexts[item] = Rect2D(x, baseTop + 24f, CELL, 16f)
            indices[item] = if (lesson == BruteForceLesson.FUNDAMENTALS) Rect2D(x, 0f, 0f, 0f) else Rect2D(x, baseTop + 50f, CELL, 16f)
            secondaryLabels[item] = if (lesson == BruteForceLesson.STRING_MATCHING) Rect2D(x - 3f, baseTop + 72f, CELL + 6f, 18f) else Rect2D(x, 0f, 0f, 0f)
        }
        val maxHistory = frames.maxOf { it.meta.evaluatedPairs.size }
        val historyTop = if (lesson == BruteForceLesson.PAIR_PROBLEMS) PAIR_HISTORY_TOP else 180f
        val pairHistoryEntries = if (lesson == BruteForceLesson.PAIR_PROBLEMS) frames.flatMap { it.meta.evaluatedPairs } else emptyList()
        val historyColumns = if (lesson == BruteForceLesson.PAIR_PROBLEMS) pairHistoryColumnCount(pairHistoryEntries, width) else 1
        val historyCardWidth = (width - MARGIN * 2 - PAIR_HISTORY_COLUMN_GAP * (historyColumns - 1)) / historyColumns
        val history = linkedMapOf<Int, Rect2D>()
        repeat(maxHistory) { item ->
            history[item] = if (lesson == BruteForceLesson.PAIR_PROBLEMS) {
                val column = item % historyColumns
                val row = item / historyColumns
                Rect2D(MARGIN + column * (historyCardWidth + PAIR_HISTORY_COLUMN_GAP), historyTop + row * (PAIR_HISTORY_CARD_HEIGHT + PAIR_HISTORY_ROW_GAP), historyCardWidth, PAIR_HISTORY_CARD_HEIGHT)
            } else Rect2D(0f, 0f, 0f, 0f)
        }
        val finalHistoryBottom = history.values.maxOfOrNull { it.bottom }
        val height = when (lesson) {
            BruteForceLesson.FUNDAMENTALS -> 208f
            BruteForceLesson.PAIR_PROBLEMS -> (finalHistoryBottom ?: (PAIR_CURRENT_SUM_TOP + 22f)) + MARGIN
            BruteForceLesson.STRING_MATCHING -> 252f
        }
        return BruteForceSceneLayoutResult(
            header, title, goal, status, evaluation, cells, primaryTexts, secondaryTexts, labels, secondaryLabels, indices, history,
            if (lesson == BruteForceLesson.PAIR_PROBLEMS) Rect2D(MARGIN, PAIR_CURRENT_SUM_TOP, width - MARGIN * 2, 22f) else Rect2D(0f, 0f, 0f, 0f), width, height
        )
    }

    fun patternCellBounds(frame: BruteForceSceneFrame, layout: BruteForceSceneLayoutResult): Map<Int, Rect2D> {
        val start = frame.meta.alignmentStart ?: return emptyMap()
        return frame.meta.pattern.indices.associateWith { offset ->
            val textCell = layout.cellBounds[start + offset] ?: error("Pattern alignment exceeds text geometry")
            Rect2D(textCell.left, 184f, textCell.width, textCell.height)
        }
    }

    fun patternIndexBounds(frame: BruteForceSceneFrame, layout: BruteForceSceneLayoutResult): Map<Int, Rect2D> =
        patternCellBounds(frame, layout).mapValues { (_, cell) -> Rect2D(cell.left, cell.bottom + 4f, cell.width, 16f) }

    fun comparisonMarkerBounds(frame: BruteForceSceneFrame, layout: BruteForceSceneLayoutResult): Rect2D? {
        val start = frame.meta.alignmentStart ?: return null
        val offset = frame.meta.comparisonOffset ?: return null
        val textCell = layout.cellBounds[start + offset] ?: return null
        return Rect2D(textCell.left, textCell.bottom + 26f, textCell.width, 16f)
    }

    fun initialFitScale(layout: BruteForceSceneLayoutResult, viewportWidth: Float, viewportHeight: Float): Float =
        if (layout.requiredSceneWidth <= 0f || layout.requiredSceneHeight <= 0f || viewportWidth <= 0f || viewportHeight <= 0f) 1f
        else minOf(1f, viewportWidth / layout.requiredSceneWidth, viewportHeight / layout.requiredSceneHeight)
}
