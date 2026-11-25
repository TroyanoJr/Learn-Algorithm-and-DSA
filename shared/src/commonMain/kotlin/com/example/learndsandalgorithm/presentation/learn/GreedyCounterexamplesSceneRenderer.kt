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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.ceil

private val CounterRegion = Color(0xFF13131C)
private val CounterBorder = Color(0xFF262634)
private val CounterOrange = Color(0xFFFA6E13)
private val CounterBlue = Color(0xFF38BDF8)
private val CounterGreen = Color(0xFF10B981)
private val CounterRed = Color(0xFFEF4444)
private val CounterPrimary = Color.White
private val CounterSecondary = Color(0xFF9CA3AF)
private val CounterSelected = Color(0xFF123820)
private val CounterRejected = Color(0xFF381215)
private val CounterActiveHeader = Color(0xFF13233A)

@Composable
internal fun GreedyCounterexamplesSceneRenderer(
    presentation: GreedyCounterexamplesScenePresentation,
    frame: GreedyCounterexamplesSceneFrame
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current

        fun extent(text: String, width: Float, size: Float, weight: FontWeight): CounterexampleTextExtent {
            val natural = textMeasurer.measure(
                AnnotatedString(text),
                TextStyle(fontSize = size.sp, fontWeight = weight),
                maxLines = 1,
                softWrap = false,
                constraints = Constraints()
            )
            val wrapped = textMeasurer.measure(
                AnnotatedString(text),
                TextStyle(fontSize = size.sp, fontWeight = weight),
                maxLines = Int.MAX_VALUE,
                softWrap = true,
                constraints = Constraints(maxWidth = with(density) { width.dp.roundToPx() }.coerceAtLeast(1))
            )
            return with(density) {
                CounterexampleTextExtent(
                    naturalWidth = natural.size.width.toDp().value,
                    wrappedHeight = ceil(wrapped.size.height.toDp().value)
                )
            }
        }

        fun maxExtent(values: List<Pair<String, Pair<Float, FontWeight>>>, width: Float): CounterexampleTextExtent {
            val extents = values.map { (text, style) -> extent(text, width, style.first, style.second) }
            return CounterexampleTextExtent(
                naturalWidth = extents.maxOfOrNull { it.naturalWidth } ?: 0f,
                wrappedHeight = extents.maxOfOrNull { it.wrappedHeight } ?: 1f
            )
        }

        val coinTextExtent = remember(presentation, density) {
            presentation.frames.flatMap { it.greedyCoins + it.optimalCoins }
                .distinctBy { it.id to it.value }
                .map { occurrence -> extent(occurrence.value.toString(), 10_000f, 12f, FontWeight.Bold) }
                .fold(CounterexampleTextExtent(0f, 0f)) { maximum, current ->
                    CounterexampleTextExtent(
                        maxOf(maximum.naturalWidth, current.naturalWidth),
                        maxOf(maximum.wrappedHeight, current.wrappedHeight)
                    )
                }
        }
        val coinDiameter = counterexampleCoinDiameter(
            ceil(coinTextExtent.naturalWidth), ceil(coinTextExtent.wrappedHeight)
        )

        fun measureSceneText(layout: GreedyCounterexamplesSceneLayoutResult): CounterexampleSceneTextExtents {
            fun acrossFrames(text: (GreedyCounterexamplesSceneFrame) -> String, bounds: CounterexampleRect, size: Float, weight: FontWeight) =
                maxExtent(presentation.frames.map { text(it) to (size to weight) }, bounds.width)
            val headerBadgeTextBounds = layout.headerStatusBadge.copy(
                width = layout.headerStatusBadge.width - 6f,
                height = layout.headerStatusBadge.height - 6f
            )

            return CounterexampleSceneTextExtents(
                target = acrossFrames({ "COUNTEREXAMPLE DEMO: TARGET AMOUNT ${it.step.targetAmount}" }, layout.targetLabel, 11f, FontWeight.Bold),
                denominations = acrossFrames({ "Denominations: [${it.step.denominations.joinToString()}]" }, layout.denominationLabel, 10f, FontWeight.Normal),
                headerBadge = acrossFrames(::counterexampleHeaderBadgeText, headerBadgeTextBounds, 9f, FontWeight.Bold),
                greedyHeading = extent("GREEDY (LARGEST-FIRST)", layout.greedyHeading.width, 10f, FontWeight.Bold),
                emptyMessage = extent("No coins yet", layout.greedyEmptyMessage.width, 11f, FontWeight.Bold),
                greedyCount = acrossFrames({ "Result: ${it.greedyCoins.size} coins" }, layout.greedyCount, 10f, FontWeight.Bold),
                greedyRemaining = acrossFrames({ "Remaining: ${it.step.greedyRemaining}" }, layout.greedyRemaining, 9f, FontWeight.Normal),
                optimalHeading = extent("OPTIMAL (NON-GREEDY)", layout.optimalHeading.width, 10f, FontWeight.Bold),
                optimalHiddenMark = extent("?", layout.optimalHiddenMark.width, 14f, FontWeight.Bold),
                optimalCount = maxExtent(
                    presentation.frames.map { it.optimalCoins.size.let { count -> "Result: $count coins" to (10f to FontWeight.Bold) } } +
                        listOf("REVEALED AT COMPARISON" to (8f to FontWeight.Bold)),
                    layout.optimalCount.width
                ),
                comparisonSummary = acrossFrames({ comparisonSummaryText(it) }, layout.comparisonSummary.copy(width = layout.comparisonSummary.width - 8f), 9f, FontWeight.Bold),
                insightHeading = extent("WHY GREEDY FAILS HERE:", layout.insightHeading.width, 9f, FontWeight.Bold),
                insightBody = acrossFrames({
                    if (it.failureDemonstrated) GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS
                    else "Optimal result and explanation are revealed at comparison."
                }, layout.insightBody, 10f, FontWeight.Normal),
                proofConclusion = acrossFrames({ proofConclusionText(it) }, layout.proofConclusion.copy(width = layout.proofConclusion.width - 8f), 9f, FontWeight.Bold)
            )
        }

        // Establish natural widths in the original full-width cells before evaluating whether
        // count and remaining amount can safely share the compact footer.
        val initialLayout = remember(presentation, frame, maxWidth, coinDiameter) {
            GreedyCounterexamplesSceneLayout.calculate(presentation, frame, maxWidth.value, coinDiameter)
        }
        val firstMetrics = remember(presentation, initialLayout, density) { measureSceneText(initialLayout) }
        val measuredLayout = remember(presentation, frame, maxWidth, maxHeight, coinDiameter, firstMetrics) {
            GreedyCounterexamplesSceneLayout.calculate(presentation, frame, maxWidth.value, coinDiameter, firstMetrics, maxHeight.value)
        }
        val textExtents = remember(presentation, measuredLayout, density) { measureSceneText(measuredLayout) }
        val layout = remember(presentation, frame, maxWidth, maxHeight, coinDiameter, textExtents) {
            GreedyCounterexamplesSceneLayout.calculate(presentation, frame, maxWidth.value, coinDiameter, textExtents, maxHeight.value)
        }

        VisualizerCanvasViewport(
            modifier = Modifier.fillMaxSize(),
            mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(
                key = "greedy-counterexamples-dedicated",
                initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT
            )
        ) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun measure(
                    text: String,
                    bounds: CounterexampleRect,
                    size: Float,
                    weight: FontWeight,
                    align: TextAlign,
                    wrap: Boolean
                ): TextLayoutResult = textMeasurer.measure(
                    AnnotatedString(text),
                    TextStyle(fontSize = size.sp, fontWeight = weight, textAlign = align),
                    maxLines = if (wrap) Int.MAX_VALUE else 1,
                    softWrap = wrap,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )

                fun label(
                    text: String,
                    bounds: CounterexampleRect,
                    color: Color,
                    fontSize: Float,
                    weight: FontWeight = FontWeight.Normal,
                    align: TextAlign = TextAlign.Center,
                    wrap: Boolean = true
                ) {
                    val result = measure(text, bounds, fontSize, weight, align, wrap)
                    drawText(result, topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx()), color = color)
                }

                fun rounded(bounds: CounterexampleRect, fill: Color, outline: Color, radius: Float = 10f) {
                    val topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx())
                    val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
                    drawRoundRect(fill, topLeft, size, CornerRadius(radius.dp.toPx()))
                    drawRoundRect(outline, topLeft, size, CornerRadius(radius.dp.toPx()), style = Stroke(1.dp.toPx()))
                }

                fun coin(placement: CounterexampleCoinLayout, fill: Color, outline: Color) {
                    val circleDiameter = minOf(placement.circle.width, placement.circle.height).dp.toPx()
                    val center = Offset(
                        placement.circle.x.dp.toPx() + circleDiameter / 2f,
                        placement.circle.y.dp.toPx() + circleDiameter / 2f
                    )
                    drawCircle(fill, circleDiameter / 2f, center)
                    drawCircle(outline, circleDiameter / 2f, center, style = Stroke(1.dp.toPx()))
                    val valueLayout = textMeasurer.measure(
                        AnnotatedString(placement.value.toString()),
                        TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        maxLines = 1,
                        softWrap = false,
                        constraints = Constraints(
                            maxWidth = placement.valueLabel.width.dp.roundToPx().coerceAtLeast(1),
                            maxHeight = placement.valueLabel.height.dp.roundToPx().coerceAtLeast(1)
                        )
                    )
                    drawText(
                        valueLayout,
                        topLeft = Offset(center.x - valueLayout.size.width / 2f, center.y - valueLayout.size.height / 2f),
                        color = CounterPrimary
                    )
                }

                rounded(layout.headerRegion, CounterRegion, CounterBorder)
                label(
                    "COUNTEREXAMPLE DEMO: TARGET AMOUNT ${frame.step.targetAmount}",
                    layout.targetLabel, CounterOrange, 11f, FontWeight.Bold, TextAlign.Left
                )
                label(
                    "Denominations: [${frame.step.denominations.joinToString()}]",
                    layout.denominationLabel, CounterSecondary, 10f, align = TextAlign.Left
                )
                rounded(
                    layout.headerStatusBadge,
                    if (frame.finalVerdict) CounterRejected else CounterActiveHeader,
                    if (frame.finalVerdict) CounterRed else CounterBlue,
                    radius = 6f
                )
                label(
                    counterexampleHeaderBadgeText(frame),
                    layout.headerStatusBadge.copy(
                        x = layout.headerStatusBadge.x + 3f,
                        y = layout.headerStatusBadge.y + 3f,
                        width = layout.headerStatusBadge.width - 6f,
                        height = layout.headerStatusBadge.height - 6f
                    ),
                    if (frame.finalVerdict) CounterRed else CounterBlue,
                    9f,
                    FontWeight.Bold
                )

                rounded(layout.greedyCard, CounterRegion, if (frame.failureDemonstrated) CounterRed else CounterBorder)
                label("GREEDY (LARGEST-FIRST)", layout.greedyHeading, CounterRed, 10f, FontWeight.Bold)
                if (frame.greedyCoins.isEmpty()) {
                    label("No coins yet", layout.greedyEmptyMessage, CounterSecondary, 11f, FontWeight.Bold)
                } else {
                    layout.greedyCoins.forEach { coin(it, CounterRejected, CounterRed) }
                }
                label("Result: ${frame.greedyCoins.size} coins", layout.greedyCount, CounterRed, 10f, FontWeight.Bold)
                label("Remaining: ${frame.step.greedyRemaining}", layout.greedyRemaining, CounterSecondary, 9f)

                rounded(layout.optimalCard, CounterRegion, if (frame.optimalRevealed) CounterGreen else CounterBorder)
                label("OPTIMAL (NON-GREEDY)", layout.optimalHeading, CounterGreen, 10f, FontWeight.Bold)
                if (frame.optimalRevealed) {
                    layout.optimalCoins.forEach { coin(it, CounterSelected, CounterGreen) }
                    label("Result: ${frame.optimalCoins.size} coins", layout.optimalCount, CounterGreen, 10f, FontWeight.Bold)
                } else {
                    label("?", layout.optimalHiddenMark, CounterSecondary, 14f, FontWeight.Bold)
                    label("REVEALED AT COMPARISON", layout.optimalCount, CounterSecondary, 8f, FontWeight.Bold)
                }

                rounded(layout.comparisonSummary, CounterRegion, if (frame.failureDemonstrated) CounterRed else CounterBorder, 8f)
                label(
                    comparisonSummaryText(frame),
                    layout.comparisonSummary.copy(
                        x = layout.comparisonSummary.x + 4f,
                        y = layout.comparisonSummary.y + 4f,
                        width = layout.comparisonSummary.width - 8f,
                        height = layout.comparisonSummary.height - 8f
                    ),
                    if (frame.failureDemonstrated) CounterRed else CounterSecondary,
                    9f,
                    FontWeight.Bold
                )

                rounded(layout.insightRegion, CounterRegion, CounterBorder)
                label("WHY GREEDY FAILS HERE:", layout.insightHeading, CounterOrange, 9f, FontWeight.Bold, TextAlign.Left)
                label(
                    if (frame.failureDemonstrated) GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS
                    else "Optimal result and explanation are revealed at comparison.",
                    layout.insightBody,
                    if (frame.failureDemonstrated) CounterPrimary else CounterSecondary,
                    10f,
                    align = TextAlign.Left
                )
                rounded(layout.proofConclusion, CounterRegion, if (frame.finalVerdict) CounterRed else CounterBlue, 8f)
                label(
                    proofConclusionText(frame),
                    layout.proofConclusion.copy(
                        x = layout.proofConclusion.x + 4f,
                        y = layout.proofConclusion.y + 4f,
                        width = layout.proofConclusion.width - 8f,
                        height = layout.proofConclusion.height - 8f
                    ),
                    if (frame.finalVerdict) CounterRed else CounterBlue,
                    9f,
                    FontWeight.Bold
                )
            }
        }
    }
}

internal fun counterexampleHeaderBadgeText(frame: GreedyCounterexamplesSceneFrame): String =
    if (frame.finalVerdict) "FAILED GREEDY" else "COMPARING"

private fun comparisonSummaryText(frame: GreedyCounterexamplesSceneFrame): String = when {
    frame.failureDemonstrated -> "${frame.greedyCoins.size} GREEDY COINS > ${frame.optimalCoins.size} OPTIMAL COINS"
    frame.optimalRevealed -> "COMPARISON DOES NOT ESTABLISH A GREEDY FAILURE"
    else -> "GREEDY PICKS SHOWN · OPTIMAL RESULT NOT YET REVEALED"
}

private fun proofConclusionText(frame: GreedyCounterexamplesSceneFrame): String = when {
    frame.finalVerdict -> "COUNTEREXAMPLE PROVEN · GREEDY IS SUBOPTIMAL"
    frame.failureDemonstrated -> "COMPARISON SHOWS GREEDY IS NOT MINIMAL"
    frame.optimalRevealed -> "COMPARISON DOES NOT ESTABLISH FAILURE"
    else -> "A COUNTEREXAMPLE REFUTES A UNIVERSAL CLAIM"
}
