package com.example.learndsandalgorithm.presentation.learn

internal data class CoinChangeRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right: Float get() = x + width
    val bottom: Float get() = y + height
    fun contains(other: CoinChangeRect): Boolean =
        other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom
}

internal data class CoinDenominationLayout(
    val id: String,
    val index: Int,
    val row: Int,
    val column: Int,
    val cell: CoinChangeRect,
    val stateLabel: CoinChangeRect,
    val circle: CoinChangeRect
)

internal data class SelectedCoinLayout(
    val id: String,
    val sequenceIndex: Int,
    val row: Int,
    val column: Int,
    val circle: CoinChangeRect
)

internal data class CoinChangeTextExtent(val width: Float, val height: Float)

internal data class CoinChangeTextMeasurements(
    val targetTitle: CoinChangeTextExtent,
    val denominationList: CoinChangeTextExtent,
    val remainingHeading: CoinChangeTextExtent,
    val remainingValue: CoinChangeTextExtent,
    val denominationHeading: CoinChangeTextExtent,
    val denominationState: CoinChangeTextExtent,
    val denominationValue: CoinChangeTextExtent,
    val selectedHeading: CoinChangeTextExtent,
    val selectedTotal: CoinChangeTextExtent,
    val selectedValue: CoinChangeTextExtent,
    val emptyMessage: CoinChangeTextExtent,
    val selectedAmount: CoinChangeTextExtent
) {
    companion object {
        val NORMAL = CoinChangeTextMeasurements(
            targetTitle = CoinChangeTextExtent(132f, 14f),
            denominationList = CoinChangeTextExtent(96f, 13f),
            remainingHeading = CoinChangeTextExtent(56f, 10f),
            remainingValue = CoinChangeTextExtent(20f, 19f),
            denominationHeading = CoinChangeTextExtent(194f, 13f),
            denominationState = CoinChangeTextExtent(48f, 12f),
            denominationValue = CoinChangeTextExtent(12f, 19f),
            selectedHeading = CoinChangeTextExtent(154f, 13f),
            selectedTotal = CoinChangeTextExtent(83f, 13f),
            selectedValue = CoinChangeTextExtent(12f, 16f),
            emptyMessage = CoinChangeTextExtent(119f, 14f),
            selectedAmount = CoinChangeTextExtent(103f, 12f)
        )
    }
}

internal data class GreedyCoinChangeSceneLayoutResult(
    val width: Float,
    val height: Float,
    val targetRegion: CoinChangeRect,
    val targetTitle: CoinChangeRect,
    val denominationList: CoinChangeRect,
    val remainingBadge: CoinChangeRect,
    val remainingHeading: CoinChangeRect,
    val remainingValue: CoinChangeRect,
    val denominationRegion: CoinChangeRect,
    val denominationsHeading: CoinChangeRect,
    val denominationColumnCount: Int,
    val denominationRowCount: Int,
    val denominations: List<CoinDenominationLayout>,
    val selectedRegion: CoinChangeRect,
    val selectedHeading: CoinChangeRect,
    val totalCount: CoinChangeRect,
    val selectedCoins: List<SelectedCoinLayout>,
    val emptyMessage: CoinChangeRect,
    val selectedAmount: CoinChangeRect,
    val selectedColumnCount: Int,
    val selectedRowCount: Int
)

internal object GreedyCoinChangeSceneLayout {
    private const val OUTER_INSET = 8f
    private const val REGION_GAP = 8f
    private const val REGION_INSET = 10f
    private const val CELL_GAP = 8f

    fun calculate(
        presentation: GreedyCoinChangeScenePresentation,
        viewportWidth: Float,
        denominationDiameter: Float = 48f,
        selectedCoinDiameter: Float = 36f,
        measurements: CoinChangeTextMeasurements = CoinChangeTextMeasurements.NORMAL,
        viewportHeight: Float = Float.POSITIVE_INFINITY
    ): GreedyCoinChangeSceneLayoutResult {
        val compact = viewportHeight < 300f
        val remainingBadgeWidth = maxOf(
            84f,
            measurements.remainingValue.width + 20f,
            measurements.remainingHeading.width + 8f
        )
        val targetTextWidth = maxOf(measurements.targetTitle.width, measurements.denominationList.width)
        val selectedHeaderWidth = measurements.selectedHeading.width + measurements.selectedTotal.width + 8f
        val minimumContentWidth = maxOf(
            targetTextWidth + remainingBadgeWidth + 28f,
            selectedHeaderWidth + REGION_INSET * 2f,
            measurements.selectedAmount.width + REGION_INSET * 2f,
            measurements.denominationHeading.width + REGION_INSET * 2f,
            measurements.emptyMessage.width + REGION_INSET * 2f,
            maxOf(denominationDiameter, measurements.denominationState.width) + REGION_INSET * 2f,
            selectedCoinDiameter + REGION_INSET * 2f
        )
        val contentMinimumWidth = minimumContentWidth + OUTER_INSET * 2f
        val width = maxOf(viewportWidth, 240f, contentMinimumWidth)
        val innerWidth = (width - OUTER_INSET * 2f).coerceAtLeast(1f)
        val badgeWidth = remainingBadgeWidth
        val targetTitleHeight = maxOf(17f, measurements.targetTitle.height)
        val denominationListTop = maxOf(30f, 8f + targetTitleHeight + 5f)
        val denominationListHeight = maxOf(16f, measurements.denominationList.height)
        val remainingBadgeHeight = maxOf(if (compact) 40f else 48f, measurements.remainingHeading.height + measurements.remainingValue.height + 10f)
        val targetHeight = maxOf(if (compact) 56f else 64f, denominationListTop + denominationListHeight + 8f, 8f + remainingBadgeHeight + 8f)
        val target = CoinChangeRect(OUTER_INSET, OUTER_INSET, innerWidth, targetHeight)
        val badgeX = target.right - REGION_INSET - badgeWidth
        val leftTextWidth = maxOf(targetTextWidth, badgeX - target.x - REGION_INSET - 8f).coerceAtLeast(1f)
        val denominationTop = target.bottom + REGION_GAP
        val availableWidth = (innerWidth - REGION_INSET * 2f).coerceAtLeast(1f)
        val denominationDiameter = maxOf(
            48f,
            denominationDiameter,
            measurements.denominationValue.width + 12f,
            measurements.denominationValue.height + 12f
        )
        val denominationCellMinimum = maxOf(denominationDiameter, measurements.denominationState.width)
        val denominationColumns = if (presentation.denominationOccurrences.isEmpty()) 0 else {
            val fit = ((availableWidth + CELL_GAP) / (denominationCellMinimum + CELL_GAP)).toInt().coerceAtLeast(1)
            minOf(presentation.denominationOccurrences.size, fit)
        }
        val denominationRows = if (denominationColumns == 0) 0 else {
            (presentation.denominationOccurrences.size + denominationColumns - 1) / denominationColumns
        }
        val headingHeight = maxOf(14f, measurements.denominationHeading.height)
        val stateHeight = maxOf(12f, measurements.denominationState.height)
        val denominationHeaderHeight = if (compact) 6f + headingHeight + 2f + stateHeight + 2f else 8f + headingHeight + 4f + stateHeight + 4f
        val denominationRowGap = if (compact) 6f else 8f
        val denominationRowPitch = denominationDiameter + stateHeight + denominationRowGap
        val denominationRegionHeight = if (denominationRows == 0) denominationHeaderHeight + 8f else {
                denominationHeaderHeight + denominationRows * denominationDiameter +
                (denominationRows - 1) * (stateHeight + denominationRowGap) + (if (compact) 6f else 8f)
        }
        val denominationRegion = CoinChangeRect(OUTER_INSET, denominationTop, innerWidth, denominationRegionHeight)
        val denominationCellWidth = if (denominationColumns == 0) availableWidth else {
            (availableWidth - CELL_GAP * (denominationColumns - 1)) / denominationColumns
        }
        val denominationLayouts = presentation.denominationOccurrences.map { occurrence ->
            val row = occurrence.index / denominationColumns
            val column = occurrence.index % denominationColumns
            val cellX = denominationRegion.x + REGION_INSET + column * (denominationCellWidth + CELL_GAP)
            val circleTop = denominationRegion.y + denominationHeaderHeight + row * denominationRowPitch
            val stateLabel = CoinChangeRect(cellX, circleTop - stateHeight - (if (compact) 2f else 4f), denominationCellWidth, stateHeight)
            CoinDenominationLayout(
                id = occurrence.id,
                index = occurrence.index,
                row = row,
                column = column,
                cell = CoinChangeRect(cellX, stateLabel.y, denominationCellWidth, stateHeight + (if (compact) 2f else 4f) + denominationDiameter),
                stateLabel = stateLabel,
                circle = CoinChangeRect(
                    cellX + (denominationCellWidth - denominationDiameter) / 2f,
                    circleTop,
                    denominationDiameter,
                    denominationDiameter
                )
            )
        }
        val selectedTop = denominationRegion.bottom + REGION_GAP
        val selectedDiameter = maxOf(
            36f,
            selectedCoinDiameter,
            measurements.selectedValue.width + 12f,
            measurements.selectedValue.height + 12f
        )
        val fittingSelectedColumns = ((availableWidth + CELL_GAP) / (selectedDiameter + CELL_GAP)).toInt().coerceAtLeast(1)
        val selectedColumns = if (compact) minOf(presentation.maximumSelectedCoinCount.coerceAtLeast(1), fittingSelectedColumns) else fittingSelectedColumns
        val selectedRows = if (presentation.maximumSelectedCoinCount == 0) 0 else {
            (presentation.maximumSelectedCoinCount + selectedColumns - 1) / selectedColumns
        }
        val selectedCellWidth = if (compact) selectedDiameter else (availableWidth - CELL_GAP * (selectedColumns - 1)) / selectedColumns
        val selectedHeadingHeight = maxOf(14f, measurements.selectedHeading.height, measurements.selectedTotal.height)
        val selectedCoinAreaTop = selectedTop + (if (compact) 8f else 10f) + selectedHeadingHeight + (if (compact) 6f else 8f)
        val selectedCoinLayouts = (0 until presentation.maximumSelectedCoinCount).map { index ->
            val row = index / selectedColumns
            val column = index % selectedColumns
            val cellX = OUTER_INSET + REGION_INSET + column * (selectedCellWidth + CELL_GAP)
            val circleTop = selectedCoinAreaTop + row * (selectedDiameter + CELL_GAP)
            SelectedCoinLayout(
                id = "selected-coin-$index",
                sequenceIndex = index,
                row = row,
                column = column,
                circle = CoinChangeRect(
                    cellX + (selectedCellWidth - selectedDiameter) / 2f,
                    circleTop,
                    selectedDiameter,
                    selectedDiameter
                )
            )
        }
        val selectedAmountY: Float
        val selectedRegionHeight: Float
        val emptyMessage: CoinChangeRect
        val emptyMessageHeight = maxOf(18f, measurements.emptyMessage.height)
        val coinBlockHeight = if (selectedRows == 0) 0f else selectedRows * selectedDiameter + (selectedRows - 1) * CELL_GAP
        val selectedContentHeight = maxOf(emptyMessageHeight, coinBlockHeight)
        val selectedGroupWidth = minOf(presentation.maximumSelectedCoinCount, selectedColumns) * selectedDiameter +
            (minOf(presentation.maximumSelectedCoinCount, selectedColumns) - 1).coerceAtLeast(0) * CELL_GAP
        val amountBesideCoins = compact && selectedRows <= 1 &&
            maxOf(selectedGroupWidth, measurements.emptyMessage.width) + 8f + measurements.selectedAmount.width <= availableWidth
        emptyMessage = CoinChangeRect(
            OUTER_INSET + REGION_INSET,
            selectedCoinAreaTop + (selectedContentHeight - emptyMessageHeight) / 2f,
            if (amountBesideCoins) maxOf(selectedGroupWidth, measurements.emptyMessage.width) else availableWidth,
            emptyMessageHeight
        )
        selectedAmountY = if (amountBesideCoins) selectedCoinAreaTop + (selectedContentHeight - maxOf(14f, measurements.selectedAmount.height)) / 2f
        else selectedCoinAreaTop + selectedContentHeight + 6f
        val selectedAmountHeight = maxOf(14f, measurements.selectedAmount.height)
        selectedRegionHeight = maxOf(selectedAmountY + selectedAmountHeight, selectedCoinAreaTop + selectedContentHeight) - selectedTop + (if (compact) 8f else REGION_INSET)
        val selectedRegion = CoinChangeRect(OUTER_INSET, selectedTop, innerWidth, selectedRegionHeight)
        val remainingBadge = CoinChangeRect(badgeX, target.y + 8f, badgeWidth, remainingBadgeHeight)
        return GreedyCoinChangeSceneLayoutResult(
            width = width,
            height = selectedRegion.bottom + OUTER_INSET,
            targetRegion = target,
            targetTitle = CoinChangeRect(target.x + REGION_INSET, target.y + 8f, leftTextWidth, targetTitleHeight),
            denominationList = CoinChangeRect(target.x + REGION_INSET, target.y + denominationListTop, leftTextWidth, denominationListHeight),
            remainingBadge = remainingBadge,
            remainingHeading = CoinChangeRect(remainingBadge.x + 4f, remainingBadge.y + 5f, remainingBadge.width - 8f, maxOf(11f, measurements.remainingHeading.height)),
            remainingValue = CoinChangeRect(remainingBadge.x + 4f, remainingBadge.y + (if (compact) 16f else 20f), remainingBadge.width - 8f, maxOf(20f, measurements.remainingValue.height)),
            denominationRegion = denominationRegion,
            denominationsHeading = CoinChangeRect(denominationRegion.x + REGION_INSET, denominationRegion.y + (if (compact) 6f else 8f), availableWidth, headingHeight),
            denominationColumnCount = denominationColumns,
            denominationRowCount = denominationRows,
            denominations = denominationLayouts,
            selectedRegion = selectedRegion,
            selectedHeading = CoinChangeRect(selectedRegion.x + REGION_INSET, selectedRegion.y + (if (compact) 8f else 10f), measurements.selectedHeading.width, maxOf(14f, measurements.selectedHeading.height)),
            totalCount = CoinChangeRect(selectedRegion.right - REGION_INSET - measurements.selectedTotal.width, selectedRegion.y + (if (compact) 8f else 10f), measurements.selectedTotal.width, maxOf(14f, measurements.selectedTotal.height)),
            selectedCoins = selectedCoinLayouts,
            emptyMessage = emptyMessage,
            selectedAmount = CoinChangeRect(if (amountBesideCoins) selectedRegion.right - REGION_INSET - measurements.selectedAmount.width else selectedRegion.x + REGION_INSET, selectedAmountY, if (amountBesideCoins) measurements.selectedAmount.width else availableWidth, selectedAmountHeight),
            selectedColumnCount = selectedColumns,
            selectedRowCount = selectedRows
        )
    }
}
