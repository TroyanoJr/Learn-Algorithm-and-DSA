package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity

private val CoinRegion = Color(0xFF13131C)
private val CoinPending = Color(0xFF181824)
private val CoinBorder = Color(0xFF262634)
private val CoinOrange = Color(0xFFFA6E13)
private val CoinBlue = Color(0xFF38BDF8)
private val CoinGreen = Color(0xFF10B981)
private val CoinRed = Color(0xFFEF4444)
private val CoinPrimary = Color.White
private val CoinSecondary = Color(0xFF9CA3AF)
private val CoinSelectedFill = Color(0xFF123820)
private val CoinRejectedFill = Color(0xFF381215)
private val CoinActiveFill = Color(0xFF2A1C12)

@Composable
internal fun GreedyCoinChangeSceneRenderer(
    presentation: GreedyCoinChangeScenePresentation,
    frame: GreedyCoinChangeSceneFrame
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun naturalExtent(text: String, fontSize: Float, weight: FontWeight): CoinChangeTextExtent {
            val measured = textMeasurer.measure(
                AnnotatedString(text),
                TextStyle(fontSize = fontSize.sp, fontWeight = weight),
                maxLines = 1,
                softWrap = false,
                constraints = Constraints()
            )
            return with(density) {
                CoinChangeTextExtent(
                    width = measured.size.width.toDp().value,
                    height = measured.size.height.toDp().value
                )
            }
        }
        fun maximumExtent(texts: Iterable<String>, fontSize: Float, weight: FontWeight): CoinChangeTextExtent {
            val measured = texts.map { naturalExtent(it, fontSize, weight) }
            return CoinChangeTextExtent(
                width = measured.maxOfOrNull { it.width } ?: 0f,
                height = measured.maxOfOrNull { it.height } ?: 0f
            )
        }
        val allDenominations = presentation.frames.flatMap { it.step.denominations }.distinct()
        val allRemainingAmounts = presentation.frames.map { it.step.remainingAmount }
        val allSelectedCoinCounts = presentation.frames.map { it.selectedCoinOccurrences.size }.distinct()
        val allSelectedAmounts = presentation.frames.map { it.selectedAmount }
        val denominationListLabels = presentation.frames.map { "Denominations: [${it.step.denominations.joinToString()}]" }
        val measurements = CoinChangeTextMeasurements(
            targetTitle = maximumExtent(presentation.frames.map { "TARGET AMOUNT: ${it.step.targetAmount}" }, 11f, FontWeight.Bold),
            denominationList = maximumExtent(denominationListLabels, 10f, FontWeight.Normal),
            remainingHeading = naturalExtent("REMAINING", 8f, FontWeight.Bold),
            remainingValue = maximumExtent(allRemainingAmounts.map(Int::toString), 16f, FontWeight.Bold),
            denominationHeading = naturalExtent("DENOMINATIONS (LARGEST FIRST):", 10f, FontWeight.Bold),
            denominationState = maximumExtent(listOf("TAKE", "SKIP", "ACTIVE", "PENDING"), 9f, FontWeight.Bold),
            denominationValue = maximumExtent(allDenominations.map(Int::toString), 16f, FontWeight.Bold),
            selectedHeading = naturalExtent("SELECTED COINS SEQUENCE:", 10f, FontWeight.Bold),
            selectedTotal = maximumExtent(allSelectedCoinCounts.map { "Total: $it coins" }, 10f, FontWeight.Normal),
            selectedValue = maximumExtent(
                presentation.frames.flatMap { it.selectedCoinOccurrences }.map { it.value.toString() },
                13f,
                FontWeight.Bold
            ),
            emptyMessage = naturalExtent("No coins selected yet.", 11f, FontWeight.Normal),
            selectedAmount = maximumExtent(
                allSelectedAmounts.indices.map { index ->
                    "Amount: ${allSelectedAmounts[index]} / ${presentation.frames[index].step.targetAmount}"
                },
                9f,
                FontWeight.Normal
            )
        )
        val denominationDiameter = maxOf(48f, measurements.denominationValue.width + 12f, measurements.denominationValue.height + 12f)
        val selectedCoinDiameter = maxOf(36f, measurements.selectedValue.width + 12f, measurements.selectedValue.height + 12f)
        val layout = remember(presentation, maxWidth, maxHeight, measurements, denominationDiameter, selectedCoinDiameter) {
            GreedyCoinChangeSceneLayout.calculate(
                presentation = presentation,
                viewportWidth = maxWidth.value,
                denominationDiameter = denominationDiameter,
                selectedCoinDiameter = selectedCoinDiameter,
                measurements = measurements,
                viewportHeight = maxHeight.value
            )
        }
        VisualizerCanvasViewport(
            modifier = Modifier.fillMaxSize(),
            mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(
                key = "greedy-coin-change-dedicated",
                initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT
            )
        ) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun measure(
                    value: String,
                    bounds: CoinChangeRect,
                    size: Float,
                    weight: FontWeight,
                    align: TextAlign,
                    softWrap: Boolean
                ) = textMeasurer.measure(
                    AnnotatedString(value),
                    TextStyle(color = CoinPrimary, fontSize = size.sp, fontWeight = weight, textAlign = align),
                    maxLines = if (softWrap) Int.MAX_VALUE else 1,
                    softWrap = softWrap,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )

                fun label(
                    value: String,
                    bounds: CoinChangeRect,
                    color: Color,
                    fontSize: Float,
                    weight: FontWeight = FontWeight.Normal,
                    align: TextAlign = TextAlign.Center,
                    wrap: Boolean = false
                ) {
                    val result = measure(value, bounds, fontSize, weight, align, wrap)
                    drawText(result, topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx()), color = color)
                }

                fun region(bounds: CoinChangeRect) {
                    drawRoundRect(
                        color = CoinRegion,
                        topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx()),
                        size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                    drawRoundRect(
                        color = CoinBorder,
                        topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx()),
                        size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                        cornerRadius = CornerRadius(10.dp.toPx()),
                        style = Stroke(1.dp.toPx())
                    )
                }

                fun coin(circle: CoinChangeRect, fill: Color, border: Color, value: String, fontSize: Float) {
                    val left = circle.x.dp.toPx()
                    val top = circle.y.dp.toPx()
                    val diameter = minOf(circle.width, circle.height).dp.toPx()
                    val center = Offset(left + diameter / 2f, top + diameter / 2f)
                    drawCircle(fill, diameter / 2f, center)
                    drawCircle(border, diameter / 2f, center, style = Stroke(1.dp.toPx()))

                    val inset = 4.dp.toPx()
                    val measured = textMeasurer.measure(
                        AnnotatedString(value),
                        TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        maxLines = 1,
                        softWrap = false,
                        constraints = Constraints()
                    )
                    // Layout reserves the sequence-wide measured width and height plus 12dp;
                    // the 4dp inset still leaves a positive margin on both axes.
                    drawText(
                        measured,
                        topLeft = Offset(center.x - measured.size.width / 2f, center.y - measured.size.height / 2f),
                        color = CoinPrimary
                    )
                }

                region(layout.targetRegion)
                label("TARGET AMOUNT: ${frame.step.targetAmount}", layout.targetTitle, CoinOrange, 11f, FontWeight.Bold, TextAlign.Left)
                label("Denominations: [${frame.step.denominations.joinToString()}]", layout.denominationList, CoinSecondary, 10f, align = TextAlign.Left, wrap = true)
                val remainingTone = if (frame.step.remainingAmount == 0) CoinGreen else CoinOrange
                drawRoundRect(
                    color = if (frame.step.remainingAmount == 0) CoinSelectedFill else CoinActiveFill,
                    topLeft = Offset((layout.remainingBadge.x + 1f).dp.toPx(), (layout.remainingBadge.y + 1f).dp.toPx()),
                    size = Size((layout.remainingBadge.width - 2f).dp.toPx(), (layout.remainingBadge.height - 2f).dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                drawRoundRect(
                    color = remainingTone,
                    topLeft = Offset(layout.remainingBadge.x.dp.toPx(), layout.remainingBadge.y.dp.toPx()),
                    size = Size(layout.remainingBadge.width.dp.toPx(), layout.remainingBadge.height.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
                label("REMAINING", layout.remainingHeading, CoinSecondary, 8f, FontWeight.Bold)
                label("${frame.step.remainingAmount}", layout.remainingValue, CoinPrimary, 16f, FontWeight.Bold)

                region(layout.denominationRegion)
                label("DENOMINATIONS (LARGEST FIRST):", layout.denominationsHeading, CoinBlue, 10f, FontWeight.Bold, TextAlign.Left)
                layout.denominations.forEach { denomination ->
                    val state = frame.denominationStates[denomination.index]
                    val (stateLabel, accent, fill) = when (state) {
                        CoinDenominationVisualState.TAKE -> Triple("TAKE", CoinGreen, CoinSelectedFill)
                        CoinDenominationVisualState.SKIP -> Triple("SKIP", CoinRed, CoinRejectedFill)
                        CoinDenominationVisualState.ACTIVE -> Triple("ACTIVE", CoinOrange, CoinActiveFill)
                        CoinDenominationVisualState.PENDING -> Triple("PENDING", CoinSecondary, CoinPending)
                    }
                    label(stateLabel, denomination.stateLabel, accent, 9f, FontWeight.Bold)
                    coin(
                        denomination.circle,
                        fill,
                        if (state == CoinDenominationVisualState.PENDING) CoinBorder else accent,
                        frame.step.denominations[denomination.index].toString(),
                        16f
                    )
                }

                region(layout.selectedRegion)
                label("SELECTED COINS SEQUENCE:", layout.selectedHeading, CoinGreen, 10f, FontWeight.Bold, TextAlign.Left)
                label("Total: ${frame.selectedCoinOccurrences.size} coins", layout.totalCount, CoinSecondary, 10f, align = TextAlign.Right)
                if (frame.selectedCoinOccurrences.isEmpty()) {
                    label("No coins selected yet.", layout.emptyMessage, CoinSecondary, 11f, align = TextAlign.Left)
                } else {
                    frame.selectedCoinOccurrences.forEach { occurrence ->
                        val placement = layout.selectedCoins[occurrence.sequenceIndex]
                        coin(placement.circle, CoinSelectedFill, CoinGreen, occurrence.value.toString(), 13f)
                    }
                }
                label(
                    "Amount: ${frame.selectedAmount} / ${frame.step.targetAmount}",
                    layout.selectedAmount,
                    CoinSecondary,
                    9f,
                    align = TextAlign.Left
                )
            }
        }
    }
}
