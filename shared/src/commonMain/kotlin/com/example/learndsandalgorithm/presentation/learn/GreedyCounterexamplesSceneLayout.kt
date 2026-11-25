package com.example.learndsandalgorithm.presentation.learn

internal data class CounterexampleRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right: Float get() = x + width
    val bottom: Float get() = y + height
    fun contains(other: CounterexampleRect): Boolean =
        other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom
}

internal data class CounterexampleTextExtent(val naturalWidth: Float, val wrappedHeight: Float)

internal fun counterexampleCoinDiameter(maxNaturalWidth: Float, maxTextHeight: Float): Float =
    maxOf(32f, maxNaturalWidth + 12f, maxTextHeight + 12f)

internal data class CounterexampleSceneTextExtents(
    val target: CounterexampleTextExtent,
    val denominations: CounterexampleTextExtent,
    val headerBadge: CounterexampleTextExtent,
    val greedyHeading: CounterexampleTextExtent,
    val emptyMessage: CounterexampleTextExtent,
    val greedyCount: CounterexampleTextExtent,
    val greedyRemaining: CounterexampleTextExtent,
    val optimalHeading: CounterexampleTextExtent,
    val optimalHiddenMark: CounterexampleTextExtent,
    val optimalCount: CounterexampleTextExtent,
    val comparisonSummary: CounterexampleTextExtent,
    val insightHeading: CounterexampleTextExtent,
    val insightBody: CounterexampleTextExtent,
    val proofConclusion: CounterexampleTextExtent
) {
    companion object {
        val GEOMETRY_DEFAULT = CounterexampleSceneTextExtents(
            target = CounterexampleTextExtent(0f, 15f),
            denominations = CounterexampleTextExtent(0f, 13f),
            headerBadge = CounterexampleTextExtent(0f, 13f),
            greedyHeading = CounterexampleTextExtent(0f, 15f),
            emptyMessage = CounterexampleTextExtent(0f, 15f),
            greedyCount = CounterexampleTextExtent(0f, 13f),
            greedyRemaining = CounterexampleTextExtent(0f, 12f),
            optimalHeading = CounterexampleTextExtent(0f, 15f),
            optimalHiddenMark = CounterexampleTextExtent(0f, 15f),
            optimalCount = CounterexampleTextExtent(0f, 13f),
            comparisonSummary = CounterexampleTextExtent(0f, 12f),
            insightHeading = CounterexampleTextExtent(0f, 13f),
            insightBody = CounterexampleTextExtent(0f, 50f),
            proofConclusion = CounterexampleTextExtent(0f, 16f)
        )
    }
}

internal data class CounterexampleCoinLayout(
    val id: String,
    val index: Int,
    val value: Int,
    val circle: CounterexampleRect,
    val valueLabel: CounterexampleRect
)

internal data class GreedyCounterexamplesSceneLayoutResult(
    val width: Float,
    val height: Float,
    val headerRegion: CounterexampleRect,
    val targetLabel: CounterexampleRect,
    val denominationLabel: CounterexampleRect,
    val headerStatusBadge: CounterexampleRect,
    val greedyCard: CounterexampleRect,
    val greedyHeading: CounterexampleRect,
    val greedyCoinArea: CounterexampleRect,
    val greedyCoins: List<CounterexampleCoinLayout>,
    val greedyEmptyMessage: CounterexampleRect,
    val greedyCount: CounterexampleRect,
    val greedyRemaining: CounterexampleRect,
    val optimalCard: CounterexampleRect,
    val optimalHeading: CounterexampleRect,
    val optimalCoinArea: CounterexampleRect,
    val optimalCoins: List<CounterexampleCoinLayout>,
    val optimalHiddenMark: CounterexampleRect,
    val optimalCount: CounterexampleRect,
    val comparisonSummary: CounterexampleRect,
    val insightRegion: CounterexampleRect,
    val insightHeading: CounterexampleRect,
    val insightBody: CounterexampleRect,
    val proofConclusion: CounterexampleRect
)

internal object GreedyCounterexamplesSceneLayout {
    private const val OUTER_INSET = 8f
    private const val REGION_GAP = 8f
    private const val REGION_INSET = 10f
    private const val CARD_GAP = 8f
    private const val COIN_GAP = 4f

    fun calculate(
        presentation: GreedyCounterexamplesScenePresentation,
        frame: GreedyCounterexamplesSceneFrame,
        viewportWidth: Float,
        coinDiameter: Float = 32f,
        textExtents: CounterexampleSceneTextExtents = CounterexampleSceneTextExtents.GEOMETRY_DEFAULT,
        viewportHeight: Float = Float.POSITIVE_INFINITY
    ): GreedyCounterexamplesSceneLayoutResult {
        val compact = viewportHeight < 300f
        val regionGap = if (compact) 4f else REGION_GAP
        val regionPadding = if (compact) 4f else 8f
        val diameter = coinDiameter.coerceAtLeast(32f)
        val maximumCoinsAcross = maxOf(presentation.maximumGreedyCoinCount, presentation.maximumOptimalCoinCount, 1)
        val minimumCardWidth = maximumCoinsAcross * diameter + (maximumCoinsAcross - 1) * COIN_GAP + REGION_INSET * 2f
        val minimumSceneWidth = minimumCardWidth * 2f + CARD_GAP + OUTER_INSET * 2f
        val width = maxOf(viewportWidth, 280f, minimumSceneWidth)
        val innerWidth = width - OUTER_INSET * 2f
        val headerBadgeWidth = maxOf(minOf(112f, innerWidth * .36f), textExtents.headerBadge.naturalWidth + 8f)
            .coerceAtMost(innerWidth * .5f)
        val headerTextWidth = (innerWidth - headerBadgeWidth - REGION_INSET * 2f - 8f).coerceAtLeast(1f)
        val targetY = OUTER_INSET + regionPadding
        val denominationY = targetY + textExtents.target.wrappedHeight.atLeastOne() + if (compact) 2f else 4f
        val headerContentBottom = denominationY + textExtents.denominations.wrappedHeight.atLeastOne()
        val headerHeight = maxOf(if (compact) 0f else 52f, headerContentBottom - OUTER_INSET + regionPadding)
        val headerRegion = CounterexampleRect(OUTER_INSET, OUTER_INSET, innerWidth, headerHeight)
        val targetLabel = CounterexampleRect(
            headerRegion.x + REGION_INSET, targetY, headerTextWidth,
            textExtents.target.wrappedHeight.atLeastOne()
        )
        val denominationLabel = CounterexampleRect(
            headerRegion.x + REGION_INSET, denominationY, headerTextWidth,
            textExtents.denominations.wrappedHeight.atLeastOne()
        )
        val headerStatusBadge = CounterexampleRect(
            headerRegion.right - REGION_INSET - headerBadgeWidth,
            headerRegion.y + (headerHeight - textExtents.headerBadge.wrappedHeight.atLeastOne() - 8f) / 2f,
            headerBadgeWidth,
            textExtents.headerBadge.wrappedHeight.atLeastOne() + 8f
        )

        val cardY = headerRegion.bottom + regionGap
        val cardWidth = (innerWidth - CARD_GAP) / 2f
        val greedyHeading = CounterexampleRect(
            OUTER_INSET + 8f, cardY + regionPadding, cardWidth - 16f,
            textExtents.greedyHeading.wrappedHeight.atLeastOne()
        )
        val optimalHeading = CounterexampleRect(
            OUTER_INSET + cardWidth + CARD_GAP + 8f, cardY + regionPadding, cardWidth - 16f,
            textExtents.optimalHeading.wrappedHeight.atLeastOne()
        )
        val coinAreaY = maxOf(greedyHeading.bottom, optimalHeading.bottom) + if (compact) 3f else 6f
        val coinAreaHeight = diameter + if (compact) 2f else 6f
        val countY = coinAreaY + coinAreaHeight + if (compact) 2f else 5f
        val countHeight = maxOf(textExtents.greedyCount.wrappedHeight, textExtents.optimalCount.wrappedHeight).atLeastOne()
        val pairGreedyFooter = compact &&
            textExtents.greedyCount.naturalWidth + textExtents.greedyRemaining.naturalWidth + 8f <= cardWidth - 16f
        val greedyRemainingY = if (pairGreedyFooter) countY else countY + countHeight + 2f
        val remainingHeight = textExtents.greedyRemaining.wrappedHeight.atLeastOne()
        val cardTailPadding = if (compact) 4f else 5f
        val cardHeight = maxOf(
            greedyRemainingY + remainingHeight + cardTailPadding - cardY,
            countY + countHeight + cardTailPadding - cardY
        )
        val greedyCard = CounterexampleRect(OUTER_INSET, cardY, cardWidth, cardHeight)
        val optimalCard = CounterexampleRect(greedyCard.right + CARD_GAP, cardY, cardWidth, cardHeight)
        val greedyCoinArea = CounterexampleRect(greedyCard.x + REGION_INSET, coinAreaY, greedyCard.width - REGION_INSET * 2f, coinAreaHeight)
        val optimalCoinArea = CounterexampleRect(optimalCard.x + REGION_INSET, coinAreaY, optimalCard.width - REGION_INSET * 2f, coinAreaHeight)

        fun coinLayouts(coins: List<CounterexampleCoinOccurrence>, card: CounterexampleRect, coinArea: CounterexampleRect): List<CounterexampleCoinLayout> {
            val totalWidth = coins.size * diameter + (coins.size - 1).coerceAtLeast(0) * COIN_GAP
            // The original Compose Rows wrap their children and are centered by the parent Column.
            // Thus the visible sequence is intentionally recentered as each coin is added.
            val startX = card.x + (card.width - totalWidth) / 2f
            val circleY = coinArea.y + (coinArea.height - diameter) / 2f
            return coins.map { coin ->
                val circle = CounterexampleRect(startX + coin.index * (diameter + COIN_GAP), circleY, diameter, diameter)
                CounterexampleCoinLayout(
                    id = coin.id,
                    index = coin.index,
                    value = coin.value,
                    circle = circle,
                    valueLabel = CounterexampleRect(circle.x + 4f, circle.y + 4f, diameter - 8f, diameter - 8f)
                )
            }
        }

        val greedyCoins = coinLayouts(frame.greedyCoins, greedyCard, greedyCoinArea)
        val optimalCoins = coinLayouts(frame.optimalCoins, optimalCard, optimalCoinArea)
        val greedyCount = CounterexampleRect(
            greedyCard.x + 8f, countY,
            if (pairGreedyFooter) textExtents.greedyCount.naturalWidth + 4f else greedyCard.width - 16f,
            textExtents.greedyCount.wrappedHeight.atLeastOne()
        )
        val greedyRemaining = CounterexampleRect(
            if (pairGreedyFooter) greedyCount.right else greedyCard.x + 8f,
            greedyRemainingY,
            if (pairGreedyFooter) greedyCard.right - 8f - greedyCount.right else greedyCard.width - 16f,
            remainingHeight
        )
        val optimalCount = CounterexampleRect(
            optimalCard.x + 8f, countY, optimalCard.width - 16f,
            textExtents.optimalCount.wrappedHeight.atLeastOne()
        )
        val optimalHiddenMark = CounterexampleRect(
            optimalCoinArea.x, optimalCoinArea.y + (optimalCoinArea.height - textExtents.optimalHiddenMark.wrappedHeight.atLeastOne()) / 2f,
            optimalCoinArea.width, textExtents.optimalHiddenMark.wrappedHeight.atLeastOne()
        )

        val summaryY = greedyCard.bottom + regionGap
        val summaryHeight = maxOf(if (compact) 0f else 24f, textExtents.comparisonSummary.wrappedHeight + 8f)
        val comparisonSummary = CounterexampleRect(OUTER_INSET, summaryY, innerWidth, summaryHeight)
        val insightY = comparisonSummary.bottom + regionGap
        val insightHeading = CounterexampleRect(
            OUTER_INSET + REGION_INSET, insightY + regionPadding, innerWidth - REGION_INSET * 2f,
            textExtents.insightHeading.wrappedHeight.atLeastOne()
        )
        val insightBody = CounterexampleRect(
            insightHeading.x, insightHeading.bottom + if (compact) 2f else 4f, insightHeading.width,
            textExtents.insightBody.wrappedHeight.atLeastOne()
        )
        val insightHeight = maxOf(if (compact) 0f else 84f, insightBody.bottom - insightY + regionPadding)
        val insightRegion = CounterexampleRect(OUTER_INSET, insightY, innerWidth, insightHeight)
        val proofConclusionHeight = maxOf(if (compact) 0f else 24f, textExtents.proofConclusion.wrappedHeight + 8f)
        val proofConclusion = CounterexampleRect(
            OUTER_INSET, insightRegion.bottom + regionGap, innerWidth, proofConclusionHeight
        )

        return GreedyCounterexamplesSceneLayoutResult(
            width = width,
            height = proofConclusion.bottom + OUTER_INSET,
            headerRegion = headerRegion,
            targetLabel = targetLabel,
            denominationLabel = denominationLabel,
            headerStatusBadge = headerStatusBadge,
            greedyCard = greedyCard,
            greedyHeading = greedyHeading,
            greedyCoinArea = greedyCoinArea,
            greedyCoins = greedyCoins,
            greedyEmptyMessage = CounterexampleRect(greedyCoinArea.x, greedyCoinArea.y, greedyCoinArea.width, greedyCoinArea.height),
            greedyCount = greedyCount,
            greedyRemaining = greedyRemaining,
            optimalCard = optimalCard,
            optimalHeading = optimalHeading,
            optimalCoinArea = optimalCoinArea,
            optimalCoins = optimalCoins,
            optimalHiddenMark = optimalHiddenMark,
            optimalCount = optimalCount,
            comparisonSummary = comparisonSummary,
            insightRegion = insightRegion,
            insightHeading = insightHeading,
            insightBody = insightBody,
            proofConclusion = proofConclusion
        )
    }

    private fun Float.atLeastOne(): Float = coerceAtLeast(1f)
}
