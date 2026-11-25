package com.example.learndsandalgorithm.presentation.learn

internal data class FractionalRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right: Float get() = x + width
    val bottom: Float get() = y + height
    fun contains(other: FractionalRect): Boolean =
        other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom
}

/** Natural width and wrapped height measured with the same style used by the Canvas renderer. */
internal data class FractionalTextExtent(val naturalWidth: Float, val wrappedHeight: Float)

internal data class FractionalItemTextExtents(
    val state: FractionalTextExtent,
    val name: FractionalTextExtent,
    val weightValue: FractionalTextExtent,
    val ratio: FractionalTextExtent,
    val taken: FractionalTextExtent,
    val remaining: FractionalTextExtent,
    val contribution: FractionalTextExtent,
    val fraction: FractionalTextExtent
)

internal data class FractionalKnapsackTextExtents(
    val capacityTitle: FractionalTextExtent,
    val capacityFilled: FractionalTextExtent,
    val capacityRemaining: FractionalTextExtent,
    val itemsHeading: FractionalTextExtent,
    val items: List<FractionalItemTextExtents>,
    val resultHeading: FractionalTextExtent,
    val resultValue: FractionalTextExtent,
    val resultStatus: FractionalTextExtent,
    val usedCapacity: FractionalTextExtent,
    val optimalityExplanation: FractionalTextExtent,
    val optimalityBadge: FractionalTextExtent
) {
    companion object {
        // Conservative line metrics for pure geometry callers. Production replaces these with
        // TextMeasurer results at the active density, font scale, styles and assigned widths.
        val GEOMETRY_DEFAULT = FractionalKnapsackTextExtents(
            capacityTitle = FractionalTextExtent(0f, 12f),
            capacityFilled = FractionalTextExtent(0f, 12f),
            capacityRemaining = FractionalTextExtent(0f, 11f),
            itemsHeading = FractionalTextExtent(0f, 12f),
            items = emptyList(),
            resultHeading = FractionalTextExtent(0f, 11f),
            resultValue = FractionalTextExtent(0f, 20f),
            resultStatus = FractionalTextExtent(0f, 11f),
            usedCapacity = FractionalTextExtent(0f, 11f),
            optimalityExplanation = FractionalTextExtent(0f, 22f),
            optimalityBadge = FractionalTextExtent(0f, 13f)
        )

        val DEFAULT_ITEM = FractionalItemTextExtents(
            state = FractionalTextExtent(0f, 11f),
            name = FractionalTextExtent(0f, 12f),
            weightValue = FractionalTextExtent(0f, 11f),
            ratio = FractionalTextExtent(0f, 11f),
            taken = FractionalTextExtent(0f, 11f),
            remaining = FractionalTextExtent(0f, 11f),
            contribution = FractionalTextExtent(0f, 11f),
            fraction = FractionalTextExtent(0f, 11f)
        )
    }
}

internal data class FractionalItemLayout(
    val id: String,
    val sortedIndex: Int,
    val row: Int,
    val column: Int,
    val cell: FractionalRect,
    val stateLabel: FractionalRect,
    val card: FractionalRect,
    val name: FractionalRect,
    val weightValue: FractionalRect,
    val ratio: FractionalRect,
    val taken: FractionalRect,
    val remaining: FractionalRect,
    val contribution: FractionalRect,
    val fraction: FractionalRect,
    val gaugeTrack: FractionalRect
)

internal data class FractionalKnapsackSceneLayoutResult(
    val width: Float,
    val height: Float,
    val capacityRegion: FractionalRect,
    val capacityTitle: FractionalRect,
    val capacityFilled: FractionalRect,
    val capacityRemaining: FractionalRect,
    val capacityTrack: FractionalRect,
    val itemsRegion: FractionalRect,
    val itemsHeading: FractionalRect,
    val items: List<FractionalItemLayout>,
    val itemColumnCount: Int,
    val itemRowCount: Int,
    val resultRegion: FractionalRect,
    val resultHeading: FractionalRect,
    val resultValue: FractionalRect,
    val resultStatus: FractionalRect,
    val usedCapacity: FractionalRect,
    val optimalityExplanation: FractionalRect,
    val optimalityBadge: FractionalRect
)

internal object FractionalKnapsackSceneLayout {
    private const val OUTER_INSET = 8f
    private const val REGION_GAP = 8f
    private const val REGION_INSET = 10f
    private const val ITEM_GAP = 8f
    private const val MIN_ITEM_CARD_WIDTH = 92f
    private const val MIN_SCENE_WIDTH = 240f
    private const val LABEL_GAP = 1f
    // Avoid a float division/multiplication edge where a mathematically exact fit can
    // round a fraction of a pixel over the viewport and be clamped as if it needed panning.
    private const val AUTO_FIT_ROUNDING_GUARD = 1f

    fun calculate(
        presentation: FractionalKnapsackScenePresentation,
        viewportWidth: Float,
        minimumItemCardWidth: Float = MIN_ITEM_CARD_WIDTH,
        textExtents: FractionalKnapsackTextExtents = FractionalKnapsackTextExtents.GEOMETRY_DEFAULT,
        viewportHeight: Float = Float.POSITIVE_INFINITY
    ): FractionalKnapsackSceneLayoutResult {
        val compact = viewportHeight < 300f
        val regionGap = if (compact) 4f else REGION_GAP
        val regionPadding = if (compact) 4f else 8f
        val itemCount = presentation.sortedOccurrences.size
        val width = maxOf(viewportWidth, MIN_SCENE_WIDTH)
        val innerWidth = width - OUTER_INSET * 2f
        val contentWidth = innerWidth - REGION_INSET * 2f
        val cardMinimum = minimumItemCardWidth.coerceAtLeast(MIN_ITEM_CARD_WIDTH)
        val maximumColumns = minOf(3, itemCount.coerceAtLeast(1))
        val fittedColumns = ((contentWidth + ITEM_GAP) / (cardMinimum + ITEM_GAP)).toInt().coerceAtLeast(1)
        val itemColumns = if (itemCount == 0) 0 else minOf(itemCount, maximumColumns, fittedColumns)
        val itemRows = if (itemColumns == 0) 0 else (itemCount + itemColumns - 1) / itemColumns
        val cardWidth = if (itemColumns == 0) contentWidth else {
            (contentWidth - ITEM_GAP * (itemColumns - 1)) / itemColumns
        }

        val compactCapacityRow = compact && listOf(
            textExtents.capacityTitle, textExtents.capacityFilled, textExtents.capacityRemaining
        ).sumOf { it.naturalWidth.toDouble() }.toFloat() + 16f <= contentWidth
        val capacityTitleWidth = if (compactCapacityRow) textExtents.capacityTitle.naturalWidth + 4f else contentWidth * 0.58f
        val capacityFilledWidth = if (compactCapacityRow) textExtents.capacityFilled.naturalWidth + 4f else contentWidth - capacityTitleWidth
        val titleHeight = textExtents.capacityTitle.wrappedHeight.atLeastOne()
        val filledHeight = textExtents.capacityFilled.wrappedHeight.atLeastOne()
        val remainingHeight = textExtents.capacityRemaining.wrappedHeight.atLeastOne()
        val capacityRowHeight = maxOf(titleHeight, filledHeight, if (compactCapacityRow) remainingHeight else 0f)
        val capacityTitleY = OUTER_INSET + regionPadding
        val capacityFilledY = capacityTitleY
        val capacityRemainingY = if (compactCapacityRow) capacityTitleY else capacityTitleY + capacityRowHeight + 4f
        val capacityTrackY = if (compactCapacityRow) capacityTitleY + capacityRowHeight + 3f else capacityRemainingY + remainingHeight + 4f
        val capacityHeight = capacityTrackY - OUTER_INSET + 10f + regionPadding
        val capacityRegion = FractionalRect(OUTER_INSET, OUTER_INSET, innerWidth, capacityHeight)
        val capacityTrack = FractionalRect(OUTER_INSET + REGION_INSET, capacityTrackY, contentWidth, 10f)

        val rowMetrics = (0 until itemRows).map { row ->
            (row * itemColumns until minOf(itemCount, (row + 1) * itemColumns)).map { index ->
                textExtents.items.getOrNull(index) ?: FractionalKnapsackTextExtents.DEFAULT_ITEM
            }
        }
        val rowHeights = rowMetrics.map { metrics ->
            metrics.maxOfOrNull { itemMetrics ->
                itemCardHeight(itemMetrics, cardWidth, compact) +
                    if (pairNameState(itemMetrics, cardWidth, compact)) 0f else itemMetrics.state.wrappedHeight.atLeastOne() + 2f
            } ?: 0f
        }
        val headingHeight = textExtents.itemsHeading.wrappedHeight.atLeastOne()
        val itemsRegionY = capacityRegion.bottom + regionGap
        val itemsHeaderBottom = itemsRegionY + regionPadding + headingHeight
        val itemRowsTop = itemsHeaderBottom + if (compact) 3f else 6f
        val itemsRegionHeight = regionPadding + headingHeight + (if (compact) 3f else 6f) + rowHeights.sum() +
            ITEM_GAP * (itemRows - 1).coerceAtLeast(0) + regionPadding
        val itemsRegion = FractionalRect(OUTER_INSET, itemsRegionY, innerWidth, itemsRegionHeight)

        val rowTopByIndex = mutableMapOf<Int, Float>()
        var accumulatedRowHeight = 0f
        repeat(itemRows) { row ->
            for (index in row * itemColumns until minOf(itemCount, (row + 1) * itemColumns)) {
                rowTopByIndex[index] = itemRowsTop + accumulatedRowHeight
            }
            accumulatedRowHeight += rowHeights[row] + ITEM_GAP
        }
        val itemLayouts = presentation.sortedOccurrences.map { occurrence ->
            val index = occurrence.sortedIndex
            val row = if (itemColumns == 0) 0 else index / itemColumns
            val column = if (itemColumns == 0) 0 else index % itemColumns
            val x = itemsRegion.x + REGION_INSET + column * (cardWidth + ITEM_GAP)
            val cellY = rowTopByIndex[index] ?: itemRowsTop
            val metrics = textExtents.items.getOrNull(index) ?: FractionalKnapsackTextExtents.DEFAULT_ITEM
            val stateHeight = metrics.state.wrappedHeight.atLeastOne()
            val pairHeader = pairNameState(metrics, cardWidth, compact)
            val pairQuantity = pairTakenRemaining(metrics, cardWidth, compact)
            val card = FractionalRect(x, cellY + if (pairHeader) 0f else stateHeight + 2f, cardWidth, itemCardHeight(metrics, cardWidth, compact))
            var fieldY = card.y + 3f
            fun field(extent: FractionalTextExtent): FractionalRect {
                val rect = FractionalRect(x + 3f, fieldY, card.width - 6f, extent.wrappedHeight.atLeastOne())
                fieldY = rect.bottom + LABEL_GAP
                return rect
            }
            val name = if (pairHeader) {
                val rect = FractionalRect(x + 3f, fieldY, (card.width - 6f) / 2f, metrics.name.wrappedHeight.atLeastOne())
                fieldY += maxOf(metrics.name.wrappedHeight, metrics.state.wrappedHeight).atLeastOne() + LABEL_GAP
                rect
            } else field(metrics.name)
            val weightValue = field(metrics.weightValue)
            val ratio = field(metrics.ratio)
            val taken = if (pairQuantity) {
                val rect = FractionalRect(x + 3f, fieldY, (card.width - 6f) / 2f, metrics.taken.wrappedHeight.atLeastOne())
                fieldY += maxOf(metrics.taken.wrappedHeight, metrics.remaining.wrappedHeight).atLeastOne() + LABEL_GAP
                rect
            } else field(metrics.taken)
            val remaining = if (pairQuantity) FractionalRect(
                x + 3f + (card.width - 6f) / 2f, taken.y,
                (card.width - 6f) / 2f, metrics.remaining.wrappedHeight.atLeastOne()
            ) else field(metrics.remaining)
            val contribution = field(metrics.contribution)
            val fraction = field(metrics.fraction)
            val gaugeTrack = FractionalRect(x + 4f, fieldY + 1f, card.width - 8f, 5f)
            FractionalItemLayout(
                id = occurrence.id,
                sortedIndex = index,
                row = row,
                column = column,
                cell = FractionalRect(x, cellY, cardWidth, rowHeights.getOrElse(row) { 0f }),
                stateLabel = if (pairHeader) FractionalRect(
                    x + 3f + (cardWidth - 6f) / 2f, name.y,
                    (cardWidth - 6f) / 2f, stateHeight
                ) else FractionalRect(x, cellY, cardWidth, stateHeight),
                card = card,
                name = name,
                weightValue = weightValue,
                ratio = ratio,
                taken = taken,
                remaining = remaining,
                contribution = contribution,
                fraction = fraction,
                gaugeTrack = gaugeTrack
            )
        }

        val resultY = itemsRegion.bottom + regionGap
        val resultHeadingHeight = textExtents.resultHeading.wrappedHeight.atLeastOne()
        val resultValueHeight = textExtents.resultValue.wrappedHeight.atLeastOne()
        val resultStatusHeight = textExtents.resultStatus.wrappedHeight.atLeastOne()
        val usedCapacityHeight = textExtents.usedCapacity.wrappedHeight.atLeastOne()
        val pairResultHeader = compact && textExtents.resultHeading.naturalWidth + textExtents.resultStatus.naturalWidth + 8f <= contentWidth
        val headingWidth = if (pairResultHeader) textExtents.resultHeading.naturalWidth + 4f else contentWidth
        val resultHeading = FractionalRect(OUTER_INSET + REGION_INSET, resultY + regionPadding, headingWidth, resultHeadingHeight)
        val resultValue = FractionalRect(resultHeading.x, resultHeading.bottom + 3f, contentWidth, resultValueHeight)
        val resultStatus = if (pairResultHeader) FractionalRect(
            resultHeading.x + headingWidth, resultHeading.y, contentWidth - headingWidth, resultStatusHeight
        ) else FractionalRect(resultHeading.x, resultValue.bottom + 3f, contentWidth, resultStatusHeight)
        val effectiveStatus = if (pairResultHeader) resultValue.bottom else resultStatus.bottom
        val usedCapacity = FractionalRect(resultHeading.x, effectiveStatus + 3f, contentWidth, usedCapacityHeight)
        val badgeNaturalWidth = textExtents.optimalityBadge.naturalWidth + 12f
        val badgeWidth = maxOf(minOf(138f, contentWidth * 0.45f), badgeNaturalWidth.coerceAtMost(contentWidth))
            .coerceAtMost(contentWidth)
        val stackResultFooter = contentWidth < 280f
        val explanationWidth = if (stackResultFooter) contentWidth else contentWidth - badgeWidth - 8f
        val footerY = usedCapacity.bottom + regionGap
        val explanationHeight = textExtents.optimalityExplanation.wrappedHeight.atLeastOne()
        val badgeHeight = maxOf(19f, textExtents.optimalityBadge.wrappedHeight + 6f)
        val explanation = FractionalRect(resultHeading.x, footerY, explanationWidth, explanationHeight)
        val badgeY = if (stackResultFooter) explanation.bottom + 6f else footerY
        val optimalityBadge = FractionalRect(
            resultHeading.x + contentWidth - badgeWidth,
            badgeY,
            badgeWidth,
            badgeHeight
        )
        val footerBottom = maxOf(explanation.bottom, optimalityBadge.bottom)
        val resultHeight = footerBottom - resultY + regionPadding
        val resultRegion = FractionalRect(OUTER_INSET, resultY, innerWidth, resultHeight)

        return FractionalKnapsackSceneLayoutResult(
            width = width,
            height = resultRegion.bottom + OUTER_INSET + AUTO_FIT_ROUNDING_GUARD,
            capacityRegion = capacityRegion,
            capacityTitle = FractionalRect(OUTER_INSET + REGION_INSET, capacityTitleY, capacityTitleWidth, titleHeight),
            capacityFilled = FractionalRect(OUTER_INSET + REGION_INSET + capacityTitleWidth, capacityFilledY, capacityFilledWidth, filledHeight),
            capacityRemaining = FractionalRect(
                OUTER_INSET + REGION_INSET + if (compactCapacityRow) capacityTitleWidth + capacityFilledWidth else 0f,
                capacityRemainingY,
                if (compactCapacityRow) contentWidth - capacityTitleWidth - capacityFilledWidth else contentWidth,
                remainingHeight
            ),
            capacityTrack = capacityTrack,
            itemsRegion = itemsRegion,
            itemsHeading = FractionalRect(itemsRegion.x + REGION_INSET, itemsRegion.y + regionPadding, contentWidth, headingHeight),
            items = itemLayouts,
            itemColumnCount = itemColumns,
            itemRowCount = itemRows,
            resultRegion = resultRegion,
            resultHeading = resultHeading,
            resultValue = resultValue,
            resultStatus = resultStatus,
            usedCapacity = usedCapacity,
            optimalityExplanation = explanation,
            optimalityBadge = optimalityBadge
        )
    }

    private fun pairNameState(metrics: FractionalItemTextExtents, width: Float, compact: Boolean) =
        compact && metrics.name.naturalWidth <= (width - 6f) / 2f && metrics.state.naturalWidth <= (width - 6f) / 2f

    private fun pairTakenRemaining(metrics: FractionalItemTextExtents, width: Float, compact: Boolean) =
        compact && metrics.taken.naturalWidth <= (width - 6f) / 2f && metrics.remaining.naturalWidth <= (width - 6f) / 2f

    private fun itemCardHeight(metrics: FractionalItemTextExtents, width: Float, compact: Boolean): Float {
        val headerHeight = if (pairNameState(metrics, width, compact))
            maxOf(metrics.name.wrappedHeight, metrics.state.wrappedHeight).atLeastOne() else metrics.name.wrappedHeight.atLeastOne()
        val quantityHeight = if (pairTakenRemaining(metrics, width, compact))
            maxOf(metrics.taken.wrappedHeight, metrics.remaining.wrappedHeight).atLeastOne() else
            metrics.taken.wrappedHeight.atLeastOne() + metrics.remaining.wrappedHeight.atLeastOne() + LABEL_GAP
        val textHeight = headerHeight + quantityHeight + listOf(
            metrics.weightValue, metrics.ratio, metrics.contribution, metrics.fraction
        ).sumOf { it.wrappedHeight.atLeastOne().toDouble() }.toFloat()
        return 3f + textHeight + 6f * LABEL_GAP + 1f + 5f + 3f
    }

    private fun Float.atLeastOne(): Float = coerceAtLeast(1f)
}
