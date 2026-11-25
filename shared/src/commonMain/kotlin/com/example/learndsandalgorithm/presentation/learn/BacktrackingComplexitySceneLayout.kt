package com.example.learndsandalgorithm.presentation.learn

internal data class ComplexityRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
}
internal data class ComplexityLayoutMetrics(
    val headerTitleWidth: Float = 150f, val badgeWidth: Float = 110f, val headerHeight: Float = 12f,
    val inputWidth: Float = 220f, val inputHeight: Float = 12f,
    val strategyHeadingWidth: Float = 130f, val strategyHeadingHeight: Float = 12f,
    val callHeight: Float = 20f, val strategyExplanationHeight: Float = 24f,
    val elementWidth: Float = 42f, val elementHeight: Float = 25f,
    val explanationHeadingWidth: Float = 210f, val explanationHeadingHeight: Float = 12f,
    val paragraphHeights: List<Float> = listOf(24f, 24f, 24f, 24f),
    val savingsHeight: Float = 12f
)
internal data class ComplexitySceneLayoutResult(
    val width: Float, val height: Float, val header: ComplexityRect, val headerTitle: ComplexityRect,
    val badge: ComplexityRect, val input: ComplexityRect, val unprunedCard: ComplexityRect, val prunedCard: ComplexityRect,
    val unprunedHeading: ComplexityRect, val prunedHeading: ComplexityRect,
    val unprunedCalls: ComplexityRect, val prunedCalls: ComplexityRect,
    val unprunedExplanation: ComplexityRect, val prunedExplanation: ComplexityRect,
    val unprunedElements: List<ComplexityRect>, val prunedElements: List<ComplexityRect>,
    val explanationRegion: ComplexityRect, val explanationHeading: ComplexityRect,
    val paragraphs: List<ComplexityRect>, val savings: ComplexityRect
)
internal object BacktrackingComplexitySceneLayout {
    fun choose(viewportWidth: Float, viewportHeight: Float, candidates: List<ComplexitySceneLayoutResult>): ComplexitySceneLayoutResult =
        candidates.maxBy { minOf(viewportWidth / it.width, viewportHeight / it.height, 1f) }

    fun calculate(presentation: ComplexityPresentation, viewportWidth: Float, viewportHeight: Float,
                  m: ComplexityLayoutMetrics = ComplexityLayoutMetrics()): ComplexitySceneLayoutResult {
        val elementWidth = m.elementWidth.coerceAtLeast(1f)
        val cardMinimum = maxOf(m.strategyHeadingWidth + 16f, elementWidth + 16f)
        val width = maxOf(viewportWidth, 16f + 2f * cardMinimum + 8f, m.inputWidth + 36f,
            m.headerTitleWidth + m.badgeWidth + 46f, m.explanationHeadingWidth + 36f)
        val inner = width - 16f
        val cardWidth = (inner - 8f) / 2f
        val columns = ((cardWidth - 16f + 4f) / (elementWidth + 4f)).toInt().coerceAtLeast(1)
        val rows = ((presentation.elements.size.coerceAtLeast(1) + columns - 1) / columns)
        val headerHeight = 8f + m.headerHeight + 4f + m.inputHeight + 8f
        val cardHeight = 6f + m.strategyHeadingHeight + 3f + m.callHeight + 3f + m.strategyExplanationHeight + 4f +
            rows * m.elementHeight + (rows - 1) * 4f + 6f
        val explanationHeight = 8f + m.explanationHeadingHeight + 4f + m.paragraphHeights.sum() +
            (m.paragraphHeights.size - 1).coerceAtLeast(0) * 2f + 4f + m.savingsHeight + 8f
        val normalHeight = 8f + headerHeight + 8f + cardHeight + 8f + explanationHeight + 8f
        val gap = if (minOf(viewportWidth / width, viewportHeight / (normalHeight - 8f), 1f) >
            minOf(viewportWidth / width, viewportHeight / normalHeight, 1f)) 4f else 8f
        val header = ComplexityRect(8f, 8f, inner, headerHeight)
        val title = ComplexityRect(18f, 16f, m.headerTitleWidth, m.headerHeight)
        val badge = ComplexityRect(header.right - 10f - m.badgeWidth, title.y, m.badgeWidth, m.headerHeight)
        val input = ComplexityRect(18f, title.bottom + 4f, inner - 20f, m.inputHeight)
        val cardY = header.bottom + gap
        val unpruned = ComplexityRect(8f, cardY, cardWidth, cardHeight)
        val pruned = ComplexityRect(unpruned.right + 8f, cardY, cardWidth, cardHeight)
        fun heading(card: ComplexityRect) = ComplexityRect(card.x + 8f, card.y + 6f, card.width - 16f, m.strategyHeadingHeight)
        fun calls(card: ComplexityRect) = ComplexityRect(card.x + 8f, heading(card).bottom + 3f, card.width - 16f, m.callHeight)
        fun description(card: ComplexityRect) = ComplexityRect(card.x + 8f, calls(card).bottom + 3f, card.width - 16f, m.strategyExplanationHeight)
        fun elements(card: ComplexityRect) = presentation.elements.map { element ->
            ComplexityRect(card.x + 8f + (element.index % columns) * (elementWidth + 4f),
                description(card).bottom + 4f + (element.index / columns) * (m.elementHeight + 4f), elementWidth, m.elementHeight)
        }
        val explanation = ComplexityRect(8f, unpruned.bottom + gap, inner, explanationHeight)
        val explanationHeading = ComplexityRect(16f, explanation.y + 8f, inner - 16f, m.explanationHeadingHeight)
        var y = explanationHeading.bottom + 4f
        val paragraphs = m.paragraphHeights.map { height ->
            val rect = ComplexityRect(16f, y, inner - 16f, height); y = rect.bottom + 2f; rect
        }
        val savings = ComplexityRect(16f, y + 2f, inner - 16f, m.savingsHeight)
        return ComplexitySceneLayoutResult(width, explanation.bottom + 8f, header, title, badge, input, unpruned, pruned,
            heading(unpruned), heading(pruned), calls(unpruned), calls(pruned), description(unpruned), description(pruned),
            elements(unpruned), elements(pruned), explanation, explanationHeading, paragraphs, savings)
    }
}
