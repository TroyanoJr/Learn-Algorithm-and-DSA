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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Region = Color(0xFF13131C)
private val Pending = Color(0xFF181824)
private val Border = Color(0xFF262634)
private val Orange = Color(0xFFFA6E13)
private val Blue = Color(0xFF38BDF8)
private val Green = Color(0xFF10B981)
private val Red = Color(0xFFEF4444)
private val Primary = Color.White
private val Secondary = Color(0xFF9CA3AF)
private val Track = Color(0xFF22222E)

@Composable
internal fun GreedyFundamentalsSceneRenderer(frame: GreedyFundamentalsSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val layout = remember(frame.step.sortedDistances, maxWidth) {
            GreedyFundamentalsSceneLayout.calculate(frame.step.sortedDistances, maxWidth.value)
        }
        val measurer = rememberTextMeasurer()
        VisualizerCanvasViewport(Modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID,
            rememberVisualizerCanvasState(key = "greedy-fundamentals-dedicated", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun text(
                    value: String,
                    bounds: FundamentalsRect,
                    color: Color,
                    size: Float,
                    weight: FontWeight = FontWeight.Normal,
                    align: TextAlign = TextAlign.Center,
                    wrap: Boolean = false,
                    fallbackBounds: FundamentalsRect = bounds
                ) {
                    val style = TextStyle(color = color, fontSize = size.sp, fontWeight = weight, textAlign = align)
                    fun measure(
                        measuredStyle: TextStyle,
                        measuredBounds: FundamentalsRect,
                        maxLines: Int,
                        softWrap: Boolean,
                        unboundedHeight: Boolean = false
                    ) = runCatching {
                        measurer.measure(
                            value,
                            measuredStyle,
                            maxLines = maxLines,
                            softWrap = softWrap,
                            constraints = Constraints(
                                maxWidth = measuredBounds.width.dp.roundToPx().coerceAtLeast(1),
                                maxHeight = if (unboundedHeight) Constraints.Infinity else {
                                    measuredBounds.height.dp.roundToPx().coerceAtLeast(1)
                                }
                            )
                        )
                    }.getOrNull()

                    val maxLines = if (wrap) (bounds.height / 15f).toInt().coerceAtLeast(1) else 1
                    var current = size
                    var result = measure(style, bounds, maxLines, wrap)
                    while ((result == null || result.didOverflowWidth || result.didOverflowHeight) && current > 8f) {
                        current -= .5f
                        result = measure(style.copy(fontSize = current.sp), bounds, maxLines, wrap)
                    }

                    if (result != null && !result.didOverflowWidth && !result.didOverflowHeight) {
                        // Preserve the original measured output exactly whenever it fits.
                        drawText(result, topLeft = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx()))
                        return
                    }

                    // Exceptional content may still exceed the normal minimum size. Wrap
                    // it within its owning region, then reduce only this fallback's size
                    // until the full string fits. This path is not used by fitting labels.
                    var fallbackSize = minOf(size, 8f)
                    var fallback = measure(
                        style.copy(fontSize = fallbackSize.sp),
                        fallbackBounds,
                        maxLines = Int.MAX_VALUE,
                        softWrap = true
                    )
                    while ((fallback == null || fallback.didOverflowWidth || fallback.didOverflowHeight) && fallbackSize > 6f) {
                        fallbackSize = (fallbackSize - .5f).coerceAtLeast(6f)
                        fallback = measure(
                            style.copy(fontSize = fallbackSize.sp),
                            fallbackBounds,
                            maxLines = Int.MAX_VALUE,
                            softWrap = true
                        )
                    }

                    val finalLayout = if (fallback != null && !fallback.didOverflowWidth && !fallback.didOverflowHeight) {
                        fallback
                    } else {
                        // Last-resort measurement keeps every line in the layout result
                        // instead of clipping at the region's height. The Canvas still
                        // clips only at its outer viewport boundary.
                        measure(
                            style.copy(fontSize = fallbackSize.sp),
                            fallbackBounds,
                            maxLines = Int.MAX_VALUE,
                            softWrap = true,
                            unboundedHeight = true
                        )
                    }
                    finalLayout?.let {
                        drawText(it, topLeft = Offset(fallbackBounds.x.dp.toPx(), fallbackBounds.y.dp.toPx()))
                    }
                }
                fun card(bounds: FundamentalsRect, fill: Color, border: Color = Border, radius: Float = 10f) {
                    val top = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx())
                    val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
                    drawRoundRect(fill, top, size, CornerRadius(radius.dp.toPx()))
                    drawRoundRect(border, top, size, CornerRadius(radius.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                card(layout.budgetRegion, Region)
                text("DELIVERY ROBOT DISTANCE BUDGET", layout.budgetTitle, Orange, 10f, FontWeight.Bold, TextAlign.Left)
                text("Remaining: ${frame.step.remainingBudget} / ${frame.step.totalBudget}", layout.remainingLabel, Primary, 11f, FontWeight.Bold, TextAlign.Right)
                card(layout.progressTrack, Track, Track, 5f)
                val progressFillWidth = greedyFundamentalsProgressFillWidth(layout.progressTrack.width, frame.remainingFraction)
                if (progressFillWidth > 0f) {
                    card(
                        FundamentalsRect(layout.progressTrack.x, layout.progressTrack.y, progressFillWidth, layout.progressTrack.height),
                        if (frame.step.remainingBudget > 0) Blue else Red,
                        if (frame.step.remainingBudget > 0) Blue else Red,
                        5f
                    )
                }
                text("Used: ${frame.usedBudget} / ${frame.step.totalBudget}", layout.usedLabel, Secondary, 9f, align = TextAlign.Left)

                card(layout.candidatesRegion, Region)
                text("CANDIDATE STOPS (SHORTEST FIRST):", layout.candidatesHeading, Blue, 10f, FontWeight.Bold, TextAlign.Left)
                layout.candidates.forEach { candidate ->
                    val index = candidate.index
                    val state = frame.candidateStates[index]
                    val (stateLabel, accent, fill) = when (state) {
                        FundamentalsCandidateState.CHOSEN -> Triple("CHOSEN", Green, Color(0xFF123820))
                        FundamentalsCandidateState.REJECTED -> Triple("REJECT", Red, Color(0xFF381215))
                        FundamentalsCandidateState.TESTING -> Triple("TESTING", Orange, Color(0xFF2A1C12))
                        FundamentalsCandidateState.PENDING -> Triple("PENDING", Secondary, Pending)
                    }
                    text(stateLabel, candidate.state, accent, 9f, FontWeight.Bold)
                    card(candidate.card, fill, accent, 8f)
                    text("Stop #${index + 1}", candidate.identifier, Secondary, 9f)
                    text(
                        "Dist: ${frame.step.sortedDistances[index]}",
                        candidate.distance,
                        Primary,
                        12f,
                        FontWeight.Bold,
                        fallbackBounds = FundamentalsRect(
                            candidate.distance.x,
                            candidate.distance.y,
                            candidate.distance.width,
                            candidate.card.bottom - candidate.distance.y
                        )
                    )
                }
                card(layout.summaryRegion, Region)
                text(
                    frame.selectedLabel,
                    layout.selectedSequence,
                    Primary,
                    11f,
                    FontWeight.SemiBold,
                    TextAlign.Left,
                    wrap = true,
                    fallbackBounds = FundamentalsRect(
                        layout.selectedSequence.x,
                        layout.selectedSequence.y,
                        layout.selectedSequence.width,
                        layout.summaryRegion.bottom - layout.selectedSequence.y - 4f
                    )
                )
                text("Count: ${frame.selectedCount}", layout.count, Green, 11f, FontWeight.Bold, TextAlign.Right)
                text("Sorting: O(N log N)  ·  Scanning: O(N)", layout.complexity, Secondary, 9f)
            }
        }
    }
}
