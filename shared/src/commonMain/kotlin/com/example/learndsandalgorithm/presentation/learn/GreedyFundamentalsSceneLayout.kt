package com.example.learndsandalgorithm.presentation.learn

internal data class FundamentalsRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    fun contains(other: FundamentalsRect) = other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom
}

internal data class FundamentalsCandidateLayout(
    val index: Int,
    val row: Int,
    val columnIndex: Int,
    val column: FundamentalsRect,
    val state: FundamentalsRect,
    val card: FundamentalsRect,
    val identifier: FundamentalsRect,
    val distance: FundamentalsRect
)

internal data class GreedyFundamentalsSceneLayoutResult(
    val width: Float,
    val height: Float,
    val budgetRegion: FundamentalsRect,
    val budgetTitle: FundamentalsRect,
    val remainingLabel: FundamentalsRect,
    val progressTrack: FundamentalsRect,
    val usedLabel: FundamentalsRect,
    val candidatesRegion: FundamentalsRect,
    val candidatesHeading: FundamentalsRect,
    val candidateColumnCount: Int,
    val candidateRowCount: Int,
    val candidates: List<FundamentalsCandidateLayout>,
    val summaryRegion: FundamentalsRect,
    val selectedSequence: FundamentalsRect,
    val count: FundamentalsRect,
    val complexity: FundamentalsRect
)

internal object GreedyFundamentalsSceneLayout {
    private const val CANDIDATE_GAP = 8f
    private const val MIN_CANDIDATE_WIDTH = 72f
    private const val CANDIDATE_ROW_HEIGHT = 72f
    private const val SUMMARY_TEXT_SIZE = 11f
    private const val SUMMARY_LINE_HEIGHT = 15f

    fun calculate(sortedDistances: List<Int>, width: Float): GreedyFundamentalsSceneLayoutResult {
        val sceneWidth = width.coerceAtLeast(240f)
        val contentWidth = sceneWidth - 16f
        val inset = 10f
        val budget = FundamentalsRect(8f, 8f, contentWidth, 68f)
        val candidateTop = budget.bottom + 8f
        // The original Compose column has 6dp between its heading and the status row.
        // The previous Canvas layout reserved 16dp, then consumed its entire candidate
        // region with a 78dp column despite the card ending 14dp earlier.
        val candidateHeaderHeight = 32f
        val innerCandidateWidth = contentWidth - inset * 2f
        val candidateColumnCount = if (sortedDistances.isEmpty()) 0 else {
            val columnsThatFit = ((innerCandidateWidth + CANDIDATE_GAP) /
                (MIN_CANDIDATE_WIDTH + CANDIDATE_GAP)).toInt().coerceAtLeast(1)
            minOf(sortedDistances.size, columnsThatFit)
        }
        val candidateRowCount = if (candidateColumnCount == 0) 0 else {
            (sortedDistances.size + candidateColumnCount - 1) / candidateColumnCount
        }
        val candidateRowsHeight = if (candidateRowCount == 0) 8f else {
            candidateRowCount * CANDIDATE_ROW_HEIGHT + (candidateRowCount - 1) * CANDIDATE_GAP
        }
        val candidateHeight = candidateHeaderHeight + candidateRowsHeight
        val candidatesRegion = FundamentalsRect(8f, candidateTop, contentWidth, candidateHeight)
        val summaryTop = candidatesRegion.bottom + 8f
        val countWidth = maxOf(82f, sceneWidth * .21f)
        val sequenceWidth = contentWidth - 16f - countWidth - 8f
        val maximumSelectedLabel = "Selected Stops: [${sortedDistances.joinToString()}]"
        val estimatedCharactersPerLine = (sequenceWidth / (SUMMARY_TEXT_SIZE * .55f)).toInt().coerceAtLeast(1)
        val summaryLineCount = ((maximumSelectedLabel.length + estimatedCharactersPerLine - 1) /
            estimatedCharactersPerLine).coerceAtLeast(1)
        val summaryHeight = maxOf(40f, 16f + summaryLineCount * SUMMARY_LINE_HEIGHT)
        val summary = FundamentalsRect(8f, summaryTop, contentWidth, summaryHeight)
        val complexity = FundamentalsRect(8f, summary.bottom + 4f, contentWidth, 18f)
        val columnWidth = if (candidateColumnCount == 0) innerCandidateWidth else {
            (innerCandidateWidth - CANDIDATE_GAP * (candidateColumnCount - 1)) / candidateColumnCount
        }
        val candidateLayouts = sortedDistances.indices.map { index ->
            val row = index / candidateColumnCount
            val columnIndex = index % candidateColumnCount
            val x = candidatesRegion.x + inset + columnIndex * (columnWidth + CANDIDATE_GAP)
            val rowTop = candidatesRegion.y + candidateHeaderHeight + row * (CANDIDATE_ROW_HEIGHT + CANDIDATE_GAP)
            val column = FundamentalsRect(x, rowTop, columnWidth, CANDIDATE_ROW_HEIGHT)
            FundamentalsCandidateLayout(
                index = index,
                row = row,
                columnIndex = columnIndex,
                column = column,
                state = FundamentalsRect(x, rowTop, columnWidth, 12f),
                card = FundamentalsRect(x + 2f, rowTop + 16f, columnWidth - 4f, 48f),
                identifier = FundamentalsRect(x + 4f, rowTop + 25f, columnWidth - 8f, 13f),
                distance = FundamentalsRect(x + 4f, rowTop + 39f, columnWidth - 8f, 18f)
            )
        }
        return GreedyFundamentalsSceneLayoutResult(
            width = sceneWidth,
            height = complexity.bottom + 8f,
            budgetRegion = budget,
            budgetTitle = FundamentalsRect(budget.x + inset, budget.y + 9f, contentWidth * .58f, 17f),
            remainingLabel = FundamentalsRect(budget.x + contentWidth * .59f, budget.y + 8f, contentWidth * .37f, 18f),
            progressTrack = FundamentalsRect(budget.x + inset, budget.y + 32f, budget.width - inset * 2f, 10f),
            usedLabel = FundamentalsRect(budget.x + inset, budget.y + 47f, budget.width - inset * 2f, 14f),
            candidatesRegion = candidatesRegion,
            candidatesHeading = FundamentalsRect(candidatesRegion.x + inset, candidatesRegion.y + 10f, innerCandidateWidth, 16f),
            candidateColumnCount = candidateColumnCount,
            candidateRowCount = candidateRowCount,
            candidates = candidateLayouts,
            summaryRegion = summary,
            selectedSequence = FundamentalsRect(summary.x + 8f, summary.y + 8f, sequenceWidth, summaryLineCount * SUMMARY_LINE_HEIGHT),
            count = FundamentalsRect(summary.x + 8f + sequenceWidth + 8f, summary.y + 8f, countWidth, 20f),
            complexity = complexity
        )
    }
}

internal fun greedyFundamentalsProgressFillWidth(trackWidth: Float, remainingFraction: Float): Float =
    trackWidth.coerceAtLeast(0f) * remainingFraction.coerceIn(0f, 1f)
