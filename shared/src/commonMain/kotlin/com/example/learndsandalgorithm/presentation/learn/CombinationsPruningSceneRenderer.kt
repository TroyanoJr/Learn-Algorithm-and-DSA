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
import androidx.compose.ui.graphics.PathEffect
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

private val CombinationRegion = Color(0xFF13131C)
private val CombinationBorder = Color(0xFF262634)
private val CombinationBlue = Color(0xFF38BDF8)
private val CombinationOrange = Color(0xFFFA6E13)
private val CombinationGreen = Color(0xFF10B981)
private val CombinationRed = Color(0xFFEF4444)
private val CombinationSecondary = Color(0xFF9CA3AF)

private fun combinationDifference(frame: CombinationSceneFrame): String = when {
    frame.difference > 0 -> "REMAINING ${frame.difference}"
    frame.difference == 0 -> "EXACT TARGET"
    else -> "EXCESS ${-frame.difference}"
}
private fun combinationOperation(frame: CombinationSceneFrame): String = when {
    frame.pushed != null -> "PUSH +${frame.pushed.value}"
    frame.popped != null && frame.stack.isEmpty() -> "POP +${frame.popped.value} · [] (empty candidate stack)"
    frame.popped != null -> "POP +${frame.popped.value}"
    else -> "CURRENT STACK"
}
private fun combinationCondition(frame: CombinationSceneFrame): String =
    if (frame.excessPruningSafe) "PRUNE WHEN SUM > TARGET (nonnegative candidates)"
    else "SUM > TARGET PRUNING REQUIRES NONNEGATIVE CANDIDATES"

@Composable
internal fun CombinationsPruningSceneRenderer(presentation: CombinationsPresentation, frame: CombinationSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun measure(text: String, size: Int, bold: Boolean = true): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(strings: Iterable<String>, size: Int, bold: Boolean = true): Pair<Float, Float> {
            val all = strings.map { measure(it, size, bold) }
            return (all.maxOfOrNull { it.first } ?: 1f) to (all.maxOfOrNull { it.second } ?: 1f)
        }
        val target = maximum(presentation.frames.map { "TARGET SUM: ${it.step.target}" }, 11)
        val current = maximum(presentation.frames.map { "Current Sum: ${it.step.currentSum}" }, 11)
        val difference = maximum(presentation.frames.map(::combinationDifference), 9)
        val heading = measure("CURRENT COMBINATION STACK:", 10)
        val candidateHeading = measure("CANDIDATES:", 9)
        val items = maximum(presentation.candidates.map { "+${it.value}" } + presentation.frames.mapNotNull { it.popped?.let { item -> "+${item.value}" } }, 12)
        val empty = measure("[] (empty candidate stack)", 11, false)
        val operations = maximum(presentation.frames.map(::combinationOperation), 9)
        val solutionHeading = measure("Found Solutions:", 11)
        val counts = maximum(presentation.frames.map { "Count: ${it.step.validCombinations.size}" }, 11)
        val solutions = maximum(presentation.frames.flatMap { it.step.validCombinations }.map { it.toString() }, 11)
        val conditions = maximum(presentation.frames.map(::combinationCondition), 9)
        val metrics = CombinationLayoutMetrics(target.first, current.first, maxOf(target.second, current.second),
            difference.first, difference.second, heading.first, heading.second, candidateHeading.first, candidateHeading.second,
            items.first, items.second, empty.first, empty.second, operations.first, operations.second,
            solutionHeading.first, counts.first, maxOf(solutionHeading.second, counts.second, solutions.second), solutions.first,
            conditions.first, conditions.second)
        val layout = CombinationsPruningSceneLayout.calculate(presentation, maxWidth.value, maxHeight.value, metrics)
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "backtracking-combinations", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(rect: CombinationRect, fill: Color, border: Color, dashed: Boolean = false) {
                    val position = Offset(rect.x.dp.toPx(), rect.y.dp.toPx())
                    val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(fill, position, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, position, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx(),
                        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())) else null))
                }
                fun label(text: String, rect: CombinationRect, color: Color, size: Int, bold: Boolean = true, align: Int = 0) {
                    val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
                    val spare = rect.width.dp.toPx() - result.size.width
                    val x = rect.x.dp.toPx() + when (align) { 1 -> spare / 2f; 2 -> spare; else -> 0f }
                    drawText(result, topLeft = Offset(x, rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                val tone = when { frame.difference == 0 -> CombinationGreen; frame.difference < 0 -> CombinationRed; else -> CombinationBlue }
                card(layout.header, CombinationRegion, CombinationBorder)
                label("TARGET SUM: ${frame.step.target}", layout.target, CombinationOrange, 11)
                label("Current Sum: ${frame.step.currentSum}", layout.current, if (frame.difference < 0) CombinationRed else Color.White, 11, align = 2)
                label(combinationDifference(frame), layout.difference, tone, 9)
                val track = layout.progress
                drawRoundRect(Color(0xFF22222E), Offset(track.x.dp.toPx(), track.y.dp.toPx()), Size(track.width.dp.toPx(), track.height.dp.toPx()), CornerRadius(5.dp.toPx()))
                val fraction = if (frame.step.target == 0) { if (frame.step.currentSum == 0) 1f else 0f } else
                    (frame.step.currentSum.toFloat() / frame.step.target).coerceIn(0f, 1f)
                if (fraction > 0f) drawRoundRect(tone, Offset(track.x.dp.toPx(), track.y.dp.toPx()), Size((track.width * fraction).dp.toPx(), track.height.dp.toPx()), CornerRadius(5.dp.toPx()))
                card(layout.stackRegion, CombinationRegion, CombinationBorder)
                label("CURRENT COMBINATION STACK:", layout.stackHeading, CombinationBlue, 10)
                label("CANDIDATES:", layout.candidateHeading, CombinationSecondary, 9)
                presentation.candidates.forEachIndexed { index, candidate ->
                    val active = frame.pushed?.candidateIndex == index
                    card(layout.candidates[index], if (active) Color(0xFF2A1C12) else Color(0xFF181824), if (active) CombinationOrange else CombinationBorder)
                    label(candidate.value.toString(), layout.candidates[index], if (active) CombinationOrange else Color.White, 12, align = 1)
                }
                label(combinationOperation(frame), layout.operation, if (frame.popped != null) CombinationRed else CombinationOrange, 9)
                if (frame.stack.isEmpty() && frame.popped == null) label("[] (empty candidate stack)", layout.empty, CombinationSecondary, 11, false)
                frame.stack.forEachIndexed { index, item ->
                    val fill = when { frame.difference == 0 -> Color(0xFF123820); frame.difference < 0 -> Color(0xFF381215); else -> Color(0xFF2A1C12) }
                    val border = if (item.id == frame.pushed?.id) CombinationOrange else if (frame.difference <= 0) tone else CombinationOrange
                    card(layout.stack[index], fill, border)
                    label("+${item.value}", layout.stack[index], Color.White, 12, align = 1)
                }
                frame.popped?.let { item ->
                    val rect = layout.stack[frame.stack.size]
                    card(rect, CombinationRegion, CombinationRed, dashed = true)
                    label("+${item.value}", rect, CombinationRed, 12, align = 1)
                }
                card(layout.solutionsRegion, CombinationRegion, CombinationBorder)
                label("Found Solutions:", layout.solutionsHeading, Color.White, 11)
                label("Count: ${frame.step.validCombinations.size}", layout.solutionsCount, CombinationGreen, 11, align = 2)
                if (frame.step.validCombinations.isEmpty()) label("[]", layout.emptySolution, CombinationSecondary, 11)
                frame.step.validCombinations.forEachIndexed { index, solution ->
                    card(layout.solutions[index], Color(0xFF123820), CombinationGreen)
                    label(solution.toString(), layout.solutions[index], CombinationGreen, 11, align = 1)
                }
                label(combinationCondition(frame), layout.condition, CombinationSecondary, 9)
            }
        }
    }
}
