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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

private val KnOrange = Color(0xFFFA6E13)
private val KnBlue = Color(0xFF38BDF8)
private val KnGreen = Color(0xFF10B981)
private val KnSource = Color(0xFFA78BFA)
private val KnBorder = Color(0xFF262634)
private val KnSecondary = Color(0xFF9CA3AF)

private fun knSummary(frame: KnapsackDpFrame): List<String> {
    val step = frame.step
    val item = step.currentItemIndex?.let { step.items.getOrNull(it) }
    if (frame.destination == null || item == null) return listOf(
        if (step.action == Knapsack01Action.FINISHED) "Complete · maximum value ${step.dpTable[step.capacity]}" else "Choose each item at most once",
        "Target dp[${step.capacity}]: ${step.dpTable[step.capacity]}",
        "Capacity iteration: high → low"
    )
    val source = if (frame.source == frame.destination) "previous dp[${frame.source}]" else "dp[${frame.source}]"
    return listOf(
        "Exclude ${step.excludeVal} · Include $source(${frame.sourceValueBeforeUpdate})+${item.value}=${step.includeVal}",
        "Best ${frame.bestCandidate} · Stored ${frame.storedAtDestination} · ${when (frame.decision) {
            KnapsackDecision.INCLUDE_WINS -> "include wins"
            KnapsackDecision.KEEP_EXISTING -> "keep existing"
            KnapsackDecision.UPDATED -> "update committed"
            KnapsackDecision.NONE -> "no decision"
        }}",
        "Target dp[${step.capacity}]: ${step.dpTable[step.capacity]} · Capacity iteration: high → low"
    )
}

@Composable
internal fun Knapsack01DpSceneRenderer(scene: KnapsackDpPresentation, frame: KnapsackDpFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun measure(value: String, size: Int): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(values: Iterable<String>, size: Int): Pair<Float, Float> {
            val all = values.map { measure(it, size) }
            return (all.maxOfOrNull { it.first } ?: 1f) to (all.maxOfOrNull { it.second } ?: 1f)
        }
        val layout = remember(scene, maxWidth, maxHeight, density.density, density.fontScale, measurer) {
            val header = maximum(scene.frames.map { "0/1 KNAPSACK ITEMS · CAPACITY ${it.step.capacity}" }, 10)
            val items = maximum(scene.frames.flatMap { it.step.items }.map { "${it.name} (w:${it.weight}, v:${it.value})" }, 10)
            val heading = measure("1D BACKWARD DP TABLE dp[c] (MAX VALUE):", 10)
            val cells = maximum(scene.frames.flatMap { it.step.dpTable }.map(Int::toString) + (0 until scene.tableSize).map { "c=$it" }, 11)
            val summary = maximum(scene.frames.flatMap(::knSummary), 10)
            Knapsack01DpSceneLayout.calculate(scene, maxWidth.value, maxHeight.value,
                KnapsackDpMetrics(header.first, header.second, items.first, items.second, heading.first, heading.second,
                    cells.first, cells.second, cells.second, summary.first, summary.second))
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-knapsack", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(rect: KnapsackDpRect, fill: Color, border: Color) {
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                fun text(value: String, rect: KnapsackDpRect, color: Color, size: Int, center: Boolean = false) {
                    val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    val x = rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - result.size.width) / 2f else 0f
                    drawText(result, topLeft = Offset(x, rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                card(layout.itemRegion, Color(0xFF13131C), KnBorder)
                text("0/1 KNAPSACK ITEMS · CAPACITY ${frame.step.capacity}", layout.itemHeading, KnOrange, 10)
                frame.step.items.forEachIndexed { index, item ->
                    val current = frame.step.currentItemIndex == index && frame.destination != null
                    card(layout.items[index], Color(0xFF181824), if (current) KnOrange else KnBorder)
                    text("${item.name} (w:${item.weight}, v:${item.value})", layout.items[index], if (current) KnOrange else Color.White, 10, true)
                }
                card(layout.tableRegion, Color(0xFF13131C), KnBorder)
                text("1D BACKWARD DP TABLE dp[c] (MAX VALUE):", layout.tableHeading, KnBlue, 10)
                val source = frame.source; val destination = frame.destination
                if (source != null && destination != null) {
                    val arrow = layout.dependencyArrow(source, destination)
                    val x1 = arrow.startX.dp.toPx(); val x2 = arrow.endX.dp.toPx()
                    val y = arrow.laneY.dp.toPx(); val h = 3.dp.toPx()
                    val top = arrow.destinationY.dp.toPx()
                    if (source == destination) {
                        drawLine(KnBlue, Offset(x1, top), Offset(x1, y), 1.dp.toPx())
                        drawLine(KnBlue, Offset(x1, top), Offset(layout.cells[source].x.dp.toPx(), top), 1.dp.toPx())
                        drawCircle(KnBlue, h, Offset(x1 + h, y), style = Stroke(1.dp.toPx()))
                    } else {
                        drawLine(KnBlue, Offset(x1, arrow.sourceY.dp.toPx()), Offset(x1, y), 1.dp.toPx())
                        drawLine(KnBlue, Offset(x1, arrow.sourceY.dp.toPx()), Offset(layout.cells[source].x.dp.toPx(), arrow.sourceY.dp.toPx()), 1.dp.toPx())
                        drawLine(KnBlue, Offset(x1, y), Offset(x2, y), 1.dp.toPx())
                        drawLine(KnBlue, Offset(x2, y), Offset(x2, top), 1.dp.toPx())
                        drawLine(KnBlue, Offset(x2, top), Offset(layout.cells[destination].x.dp.toPx(), top), 1.dp.toPx())
                        drawLine(KnBlue, Offset(x2, top), Offset(x2 - h, top - h), 1.dp.toPx())
                        drawLine(KnBlue, Offset(x2, top), Offset(x2 + h, top - h), 1.dp.toPx())
                    }
                }
                frame.step.dpTable.forEachIndexed { i, value ->
                    val dest = destination == i; val src = source == i
                    text("c=$i", layout.indices[i], if (dest) KnOrange else KnSecondary, 10, true)
                    val final = frame.step.action == Knapsack01Action.FINISHED && i == frame.step.capacity
                    val border = if (final || dest && frame.decision == KnapsackDecision.UPDATED) KnGreen
                        else if (dest) KnOrange else if (src) KnSource else if (value > 0) KnBlue else KnBorder
                    card(layout.cells[i], Color(0xFF181824), border)
                    text(value.toString(), layout.cells[i], Color.White, 11, true)
                }
                card(layout.decisionRegion, Color(0xFF13131C), KnBorder)
                knSummary(frame).forEachIndexed { i, value -> text(value, layout.summary[i], if (i == 2) KnGreen else Color.White, 10) }
            }
        }
    }
}
