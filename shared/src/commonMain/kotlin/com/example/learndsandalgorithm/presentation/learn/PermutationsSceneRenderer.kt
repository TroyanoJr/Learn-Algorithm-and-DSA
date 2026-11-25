package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

private val PermRegion = Color(0xFF13131C)
private val PermBorder = Color(0xFF262634)
private val PermOrange = Color(0xFFFA6E13)
private val PermRed = Color(0xFFEF4444)
private val PermGreen = Color(0xFF10B981)
private val PermBlue = Color(0xFF38BDF8)
private val PermSecondary = Color(0xFF9CA3AF)

@Composable
internal fun PermutationsSceneRenderer(presentation: PermutationsPresentation, frame: PermutationsSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun extent(text: String, size: Int, bold: Boolean): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maxExtent(texts: Iterable<String>, size: Int, bold: Boolean = true): Pair<Float, Float> {
            val all = texts.map { extent(it, size, bold) }
            return (all.maxOfOrNull { it.first } ?: 1f) to (all.maxOfOrNull { it.second } ?: 1f)
        }
        val heading = extent("WORKING ARRAY STATE:", 10, true)
        val starts = maxExtent(presentation.frames.map { "StartIndex: ${it.step.startIndex}" }, 10, false)
        val labels = maxExtent(listOf("FIX", "UNDO FIX", "SWAP", "UNDO", "START") + (0 until presentation.maximumArraySize).map { "idx $it" }, 9)
        val values = maxExtent(presentation.frames.flatMap { it.working }.map { it.value.toString() }, 16)
        val headings = maxExtent(presentation.frames.map { "GENERATED PERMUTATIONS (TOTAL ${it.results.size} / ${presentation.maximumResultCount}):" }, 10)
        val results = maxExtent(presentation.frames.flatMap { it.results }.map { "[${it.values.joinToString(",")}]" }, 10)
        val empty = extent("No permutations generated yet.", 10, false)
        val layout = PermutationsSceneLayout.calculate(presentation, maxWidth.value, maxHeight.value,
            PermutationsLayoutMetrics(heading.first, starts.first, maxOf(heading.second, starts.second), labels.first,
                labels.second, values.first, values.second, headings.first, headings.second, results.first, results.second,
                empty.first, empty.second))
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "backtracking-permutations", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(bounds: PermutationRect, fill: Color, border: Color) {
                    val pos = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx())
                    val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(8.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(8.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                fun label(text: String, bounds: PermutationRect, color: Color, size: Int, bold: Boolean = true, align: Int = 0) {
                    val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
                    val spare = bounds.width.dp.toPx() - result.size.width
                    val x = bounds.x.dp.toPx() + when (align) { 1 -> spare / 2f; 2 -> spare; else -> 0f }
                    val y = bounds.y.dp.toPx() + (bounds.height.dp.toPx() - result.size.height) / 2f
                    drawText(result, topLeft = Offset(x, y), color = color)
                }
                card(layout.workingRegion, PermRegion, PermBorder)
                label("WORKING ARRAY STATE:", layout.heading, PermOrange, 10)
                label("StartIndex: ${frame.step.startIndex}", layout.startIndex, PermSecondary, 10, false, 2)
                val swap = frame.swap
                if (swap != null && swap.first != swap.second) {
                    val left = layout.cells[minOf(swap.first, swap.second)].centerX.dp.toPx()
                    val right = layout.cells[maxOf(swap.first, swap.second)].centerX.dp.toPx()
                    val y = (layout.swapIndicator.y + 7f).dp.toPx()
                    val color = if (frame.undo) PermRed else PermOrange
                    val arrow = 4.dp.toPx()
                    drawLine(color, Offset(left, y), Offset(right, y), 1.5.dp.toPx())
                    drawLine(color, Offset(left, y), Offset(left + arrow, y - arrow), 1.5.dp.toPx())
                    drawLine(color, Offset(left, y), Offset(left + arrow, y + arrow), 1.5.dp.toPx())
                    drawLine(color, Offset(right, y), Offset(right - arrow, y - arrow), 1.5.dp.toPx())
                    drawLine(color, Offset(right, y), Offset(right - arrow, y + arrow), 1.5.dp.toPx())
                }
                frame.working.forEachIndexed { index, occurrence ->
                    val isI = swap?.first == index
                    val isJ = swap?.second == index
                    val start = frame.step.startIndex == index
                    val state = when { isI && isJ && frame.undo -> "UNDO FIX"; isI && isJ -> "FIX"; (isI || isJ) && frame.undo -> "UNDO"; isI || isJ -> "SWAP"; start -> "START"; else -> "idx $index" }
                    val accent = when { (isI || isJ) && frame.undo -> PermRed; isI || isJ -> PermOrange; start -> PermBlue; else -> PermSecondary }
                    label(state, layout.labels[index], accent, 9, align = 1)
                    val solved = frame.step.action == PermutationAction.SOLUTION_FOUND
                    val fill = when { solved -> Color(0xFF123820); isI || isJ -> Color(0xFF2A1C12); else -> Color(0xFF181824) }
                    val border = when { solved -> PermGreen; isI || isJ -> accent; else -> PermBorder }
                    card(layout.cells[index], fill, border)
                    label(occurrence.value.toString(), layout.cells[index], Color.White, 16, align = 1)
                }
                card(layout.resultRegion, PermRegion, PermBorder)
                label("GENERATED PERMUTATIONS (TOTAL ${frame.results.size} / ${presentation.maximumResultCount}):", layout.resultHeading, PermBlue, 10)
                if (frame.results.isEmpty()) label("No permutations generated yet.", layout.emptyMessage, PermSecondary, 10, false)
                frame.results.forEachIndexed { index, result ->
                    val bounds = layout.results[index]
                    card(bounds, Color(0xFF123820), PermGreen)
                    label("[${result.values.joinToString(",")}]", bounds, PermGreen, 10, align = 1)
                }
            }
        }
    }
}
