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

private val KnapsackRegion = Color(0xFF13131C)
private val KnapsackBorder = Color(0xFF262634)
private val KnapsackNeutral = Color(0xFF181824)
private val KnapsackOrange = Color(0xFFFA6E13)
private val KnapsackBlue = Color(0xFF38BDF8)
private val KnapsackGreen = Color(0xFF10B981)
private val KnapsackRed = Color(0xFFEF4444)
private val KnapsackPrimary = Color.White
private val KnapsackSecondary = Color(0xFF9CA3AF)
private val KnapsackSelected = Color(0xFF123820)
private val KnapsackPartial = Color(0xFF122C38)
private val KnapsackActive = Color(0xFF2A1C12)
private val KnapsackTrack = Color(0xFF22222E)

@Composable
internal fun FractionalKnapsackSceneRenderer(
    presentation: FractionalKnapsackScenePresentation,
    frame: FractionalKnapsackSceneFrame
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        // Measure from full-width fields first. Compact sharing is selected only after natural
        // widths are known; measuring against provisional narrow shared fields inflates heights.
        val baseLayout = remember(presentation, maxWidth) {
            FractionalKnapsackSceneLayout.calculate(presentation, maxWidth.value)
        }

        fun extent(text: String, widthDp: Float, size: Float, weight: FontWeight): FractionalTextExtent {
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
                constraints = Constraints(maxWidth = with(density) { widthDp.dp.roundToPx() }.coerceAtLeast(1))
            )
            return with(density) {
                FractionalTextExtent(
                    naturalWidth = natural.size.width.toDp().value,
                    wrappedHeight = ceil(wrapped.size.height.toDp().value)
                )
            }
        }

        fun maxExtent(values: List<Pair<String, Pair<Float, FontWeight>>>, width: Float): FractionalTextExtent {
            val extents = values.map { (text, style) -> extent(text, width, style.first, style.second) }
            return FractionalTextExtent(
                naturalWidth = extents.maxOfOrNull { it.naturalWidth } ?: 0f,
                wrappedHeight = extents.maxOfOrNull { it.wrappedHeight } ?: 1f
            )
        }

        val itemExtents = presentation.sortedOccurrences.map { occurrence ->
            val placement = baseLayout.items[occurrence.sortedIndex]
            val itemFrames = presentation.frames.map { it.items[occurrence.sortedIndex] }
            val item = occurrence.item
            fun values(field: (FractionalItemFrame) -> String, fontSize: Float, weight: FontWeight) =
                itemFrames.map { field(it) to (fontSize to weight) }
            FractionalItemTextExtents(
                state = maxExtent(itemFrames.map { itemFrame ->
                    val percent = formatGreedyPercent(itemFrame.fraction)
                    val state = when (itemFrame.state) {
                        FractionalItemVisualState.FULL -> "100%"
                        FractionalItemVisualState.PARTIAL -> percent
                        FractionalItemVisualState.EVALUATING -> "TESTING"
                        FractionalItemVisualState.PENDING -> "PENDING"
                    }
                    state to (9f to FontWeight.Bold)
                }, placement.stateLabel.width),
                name = extent("Item ${item.name}", placement.name.width, 10f, FontWeight.Bold),
                weightValue = extent(
                    "w:${formatGreedyDecimal(item.weight)} · v:${formatGreedyDecimal(item.value)}",
                    placement.weightValue.width, 9f, FontWeight.Normal
                ),
                ratio = extent("RATIO ${formatGreedyDecimal(item.ratio)}", placement.ratio.width, 9f, FontWeight.Bold),
                taken = maxExtent(values({ "TAKEN ${formatGreedyDecimal(it.takenWeight)}" }, 9f, FontWeight.Normal), placement.taken.width),
                remaining = maxExtent(values({ "LEFT ${formatGreedyDecimal(it.remainingWeight)}" }, 9f, FontWeight.Normal), placement.remaining.width),
                contribution = maxExtent(values({ "+${formatGreedyDecimal(it.valueContribution)} VALUE" }, 9f, FontWeight.Bold), placement.contribution.width),
                fraction = maxExtent(values({ "FRACTION ${formatGreedyPercent(it.fraction)}" }, 9f, FontWeight.Normal), placement.fraction.width)
            )
        }

        val headingText = "ITEMS SORTED BY VALUE / WEIGHT RATIO:"
        val explanationText = "Ratio greedy is optimal when items can be divided."
        val badgeText = "OPTIMAL FOR FRACTIONAL"
        val resultContentWidth = (maxWidth.value - 16f - 20f).coerceAtLeast(1f)
        val badgeNaturalExtent = extent(badgeText, resultContentWidth, 8f, FontWeight.Bold)
        val resultBadgeWidth = maxOf(
            minOf(138f, resultContentWidth * 0.45f),
            (badgeNaturalExtent.naturalWidth + 12f).coerceAtMost(resultContentWidth)
        ).coerceAtMost(resultContentWidth)
        val resultExplanationWidth = if (resultContentWidth < 280f) resultContentWidth else resultContentWidth - resultBadgeWidth - 8f
        val measuredExtents = FractionalKnapsackTextExtents(
            capacityTitle = maxExtent(presentation.frames.map {
                "KNAPSACK CAPACITY: ${formatGreedyDecimal(it.step.capacity)} W" to (10f to FontWeight.Bold)
            }, baseLayout.capacityTitle.width),
            capacityFilled = maxExtent(presentation.frames.map {
                "FILLED ${formatGreedyDecimal(it.selectedWeight)} / ${formatGreedyDecimal(it.step.capacity)}" to (10f to FontWeight.Bold)
            }, baseLayout.capacityFilled.width),
            capacityRemaining = maxExtent(presentation.frames.map {
                "REMAINING ${formatGreedyDecimal(it.remainingCapacity)}" to (9f to FontWeight.Bold)
            }, baseLayout.capacityRemaining.width),
            itemsHeading = extent(headingText, baseLayout.itemsHeading.width, 10f, FontWeight.Bold),
            items = itemExtents,
            resultHeading = extent("TOTAL KNAPSACK VALUE", resultContentWidth, 9f, FontWeight.Bold),
            resultValue = maxExtent(presentation.frames.map {
                "${formatGreedyDecimal(it.totalValue)} Value Units" to (16f to FontWeight.Bold)
            }, resultContentWidth),
            resultStatus = maxExtent(presentation.frames.map {
                (if (it.isFinalResult) "FINAL RESULT" else "CURRENT RUNNING TOTAL") to (9f to FontWeight.Bold)
            }, resultContentWidth),
            usedCapacity = maxExtent(presentation.frames.map {
                "WEIGHT ${formatGreedyDecimal(it.selectedWeight)} / ${formatGreedyDecimal(it.step.capacity)} · REMAINING ${formatGreedyDecimal(it.remainingCapacity)}" to (9f to FontWeight.Normal)
            }, resultContentWidth),
            optimalityExplanation = extent(explanationText, resultExplanationWidth, 9f, FontWeight.Normal),
            optimalityBadge = extent(badgeText, (resultBadgeWidth - 6f).coerceAtLeast(1f), 8f, FontWeight.Bold)
        )
        val layout = remember(presentation, maxWidth, maxHeight, measuredExtents) {
            FractionalKnapsackSceneLayout.calculate(
                presentation = presentation,
                viewportWidth = maxWidth.value,
                textExtents = measuredExtents,
                viewportHeight = maxHeight.value
            )
        }
        VisualizerCanvasViewport(
            modifier = Modifier.fillMaxSize(),
            mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(
                key = "greedy-fractional-knapsack-dedicated",
                initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT
            )
        ) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun measure(
                    text: String,
                    bounds: FractionalRect,
                    size: Float,
                    weight: FontWeight,
                    align: TextAlign,
                    wrap: Boolean
                ): TextLayoutResult =
                    textMeasurer.measure(
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
                    bounds: FractionalRect,
                    color: Color,
                    fontSize: Float,
                    weight: FontWeight = FontWeight.Normal,
                    align: TextAlign = TextAlign.Center,
                    wrap: Boolean = true
                ) {
                    val result = measure(text, bounds, fontSize, weight, align, wrap)
                    drawText(result, topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx()), color = color)
                }

                fun region(bounds: FractionalRect) {
                    val topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx())
                    val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
                    drawRoundRect(KnapsackRegion, topLeft, size, CornerRadius(10.dp.toPx()))
                    drawRoundRect(KnapsackBorder, topLeft, size, CornerRadius(10.dp.toPx()), style = Stroke(1.dp.toPx()))
                }

                fun card(bounds: FractionalRect, fill: Color, outline: Color) {
                    val topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx())
                    val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
                    drawRoundRect(fill, topLeft, size, CornerRadius(8.dp.toPx()))
                    drawRoundRect(outline, topLeft, size, CornerRadius(8.dp.toPx()), style = Stroke(1.dp.toPx()))
                }

                fun bar(bounds: FractionalRect, progress: Double, color: Color, rounded: Float = 5f) {
                    val topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx())
                    val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
                    drawRoundRect(KnapsackTrack, topLeft, size, CornerRadius(rounded.dp.toPx()))
                    val fillWidth = size.width * progress.coerceIn(0.0, 1.0).toFloat()
                    if (fillWidth > 0f) {
                        drawRoundRect(
                            color,
                            topLeft,
                            Size(fillWidth, size.height),
                            CornerRadius(rounded.dp.toPx())
                        )
                    }
                }

                region(layout.capacityRegion)
                label(
                    "KNAPSACK CAPACITY: ${formatGreedyDecimal(frame.step.capacity)} W",
                    layout.capacityTitle, KnapsackOrange, 10f, FontWeight.Bold, TextAlign.Left
                )
                label(
                    "FILLED ${formatGreedyDecimal(frame.selectedWeight)} / ${formatGreedyDecimal(frame.step.capacity)}",
                    layout.capacityFilled, KnapsackPrimary, 10f, FontWeight.Bold, TextAlign.Right
                )
                label(
                    "REMAINING ${formatGreedyDecimal(frame.remainingCapacity)}",
                    layout.capacityRemaining, KnapsackSecondary, 9f, FontWeight.Bold, TextAlign.Left
                )
                val capacityProgress = if (frame.step.capacity > 0.0) frame.selectedWeight / frame.step.capacity else 0.0
                bar(layout.capacityTrack, capacityProgress, if (capacityProgress >= 1.0) KnapsackGreen else KnapsackBlue)

                region(layout.itemsRegion)
                label(
                    "ITEMS SORTED BY VALUE / WEIGHT RATIO:",
                    layout.itemsHeading, KnapsackBlue, 10f, FontWeight.Bold, TextAlign.Left
                )
                layout.items.forEach { placement ->
                    val itemFrame = frame.items[placement.sortedIndex]
                    val item = itemFrame.occurrence.item
                    val style = when (itemFrame.state) {
                        FractionalItemVisualState.FULL -> Triple(KnapsackSelected, KnapsackGreen, KnapsackGreen)
                        FractionalItemVisualState.PARTIAL -> Triple(KnapsackPartial, KnapsackBlue, KnapsackBlue)
                        FractionalItemVisualState.EVALUATING -> Triple(KnapsackActive, KnapsackOrange, KnapsackOrange)
                        FractionalItemVisualState.PENDING -> Triple(KnapsackNeutral, KnapsackBorder, KnapsackSecondary)
                    }
                    val percentage = formatGreedyPercent(itemFrame.fraction)
                    val stateText = when (itemFrame.state) {
                        FractionalItemVisualState.FULL -> "100%"
                        FractionalItemVisualState.PARTIAL -> percentage
                        FractionalItemVisualState.EVALUATING -> "TESTING"
                        FractionalItemVisualState.PENDING -> "PENDING"
                    }
                    card(placement.card, style.first, style.second)
                    label(stateText, placement.stateLabel, style.third, 9f, FontWeight.Bold)
                    label("Item ${item.name}", placement.name, KnapsackPrimary, 10f, FontWeight.Bold)
                    label(
                        "w:${formatGreedyDecimal(item.weight)} · v:${formatGreedyDecimal(item.value)}",
                        placement.weightValue, KnapsackSecondary, 9f
                    )
                    label(
                        "RATIO ${formatGreedyDecimal(item.ratio)}",
                        placement.ratio, KnapsackOrange, 9f, FontWeight.Bold
                    )
                    label(
                        "TAKEN ${formatGreedyDecimal(itemFrame.takenWeight)}",
                        placement.taken, KnapsackPrimary, 9f
                    )
                    label(
                        "LEFT ${formatGreedyDecimal(itemFrame.remainingWeight)}",
                        placement.remaining, KnapsackSecondary, 9f
                    )
                    label(
                        "+${formatGreedyDecimal(itemFrame.valueContribution)} VALUE",
                        placement.contribution, style.third, 9f, FontWeight.Bold
                    )
                    label("FRACTION $percentage", placement.fraction, style.third, 9f)
                    bar(placement.gaugeTrack, itemFrame.fraction, style.second, rounded = 3f)
                }

                region(layout.resultRegion)
                label("TOTAL KNAPSACK VALUE", layout.resultHeading, KnapsackSecondary, 9f, FontWeight.Bold, TextAlign.Left)
                label(
                    "${formatGreedyDecimal(frame.totalValue)} Value Units",
                    layout.resultValue, KnapsackGreen, 16f, FontWeight.Bold, TextAlign.Left
                )
                label(
                    if (frame.isFinalResult) "FINAL RESULT" else "CURRENT RUNNING TOTAL",
                    layout.resultStatus,
                    if (frame.isFinalResult) KnapsackGreen else KnapsackSecondary,
                    9f,
                    FontWeight.Bold,
                    TextAlign.Left
                )
                label(
                    "WEIGHT ${formatGreedyDecimal(frame.selectedWeight)} / ${formatGreedyDecimal(frame.step.capacity)} · REMAINING ${formatGreedyDecimal(frame.remainingCapacity)}",
                    layout.usedCapacity, KnapsackSecondary, 9f, align = TextAlign.Left
                )
                label(
                    "Ratio greedy is optimal when items can be divided.",
                    layout.optimalityExplanation, KnapsackSecondary, 9f, align = TextAlign.Left, wrap = true
                )
                card(layout.optimalityBadge, KnapsackSelected, KnapsackGreen)
                label(
                    "OPTIMAL FOR FRACTIONAL",
                    layout.optimalityBadge.copy(
                        x = layout.optimalityBadge.x + 3f,
                        y = layout.optimalityBadge.y + 3f,
                        width = layout.optimalityBadge.width - 6f,
                        height = layout.optimalityBadge.height - 6f
                    ),
                    KnapsackGreen,
                    8f,
                    FontWeight.Bold
                )
            }
        }
    }
}
