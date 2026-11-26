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

private val TabOrange = Color(0xFFFA6E13)
private val TabBlue = Color(0xFF38BDF8)
private val TabGreen = Color(0xFF10B981)
private val TabBorder = Color(0xFF262634)
private val TabSecondary = Color(0xFF9CA3AF)
private fun tabFormula(step: DpTabulationStep): String {
    val i = step.currentIndex
    return if (step.action == TabulationAction.FILL_TABLE && i != null)
        "dp[$i] = dp[${i - 1}] + dp[${i - 2}] = ${step.prev1} + ${step.prev2} = ${step.currentWays}"
    else "Table O(N) space · rolling-only O(1) auxiliary space"
}

@Composable
internal fun DpTabulationSceneRenderer(scene: TabulationPresentation, frame: TabulationSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer(); val density = LocalDensity.current
        fun measure(value: String, size: Int): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(values: Iterable<String>, size: Int): Pair<Float, Float> {
            val all = values.map { measure(it, size) }
            return (all.maxOfOrNull { it.first } ?: 1f) to (all.maxOfOrNull { it.second } ?: 1f)
        }
        val layout = remember(scene, maxWidth, maxHeight, density.density, density.fontScale, measurer) {
            val heading = measure("DP TABULATION TABLE dp[]:  →", 10)
            val indices = maximum((0 until scene.tableSize).map { "dp[$it]" }, 9)
            val values = maximum(scene.frames.flatMap { it.step.dpTable }.map(Long::toString), 12)
            val rolling = measure("ROLLING VARIABLES (O(1) ALTERNATIVE):", 10)
            val variables = maximum(scene.frames.flatMap { listOf("prev2 = ${it.step.prev2}", "prev1 = ${it.step.prev1}", "current = ${it.step.currentWays ?: "–"}") } + listOf("prev2 = –", "prev1 = –"), 11)
            val formula = maximum(scene.frames.map { tabFormula(it.step) }, 10)
            DpTabulationSceneLayout.calculate(scene, maxWidth.value, maxHeight.value,
                TabMetrics(heading.first, heading.second, maxOf(indices.first, values.first), values.second, indices.second,
                    rolling.first, variables.first, variables.second, formula.first, formula.second))
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-tabulation", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(rect: TabRect, fill: Color, border: Color) {
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                fun text(value: String, rect: TabRect, color: Color, size: Int, center: Boolean = false) {
                    val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    val x = rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - result.size.width) / 2f else 0f
                    drawText(result, topLeft = Offset(x, rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                card(layout.table, Color(0xFF13131C), TabBorder)
                text("DP TABULATION TABLE dp[]:  →", layout.heading, TabOrange, 10)
                frame.dependencies.forEachIndexed { index, edge ->
                    val start = layout.cells[edge.source].centerX.dp.toPx(); val end = layout.cells[edge.destination].centerX.dp.toPx()
                    val y = (layout.arrowLane.y + 4f + index * 5f).dp.toPx(); val head = 3.dp.toPx()
                    drawLine(TabBlue, Offset(start, y), Offset(end, y), 1.dp.toPx())
                    drawLine(TabBlue, Offset(end, y), Offset(end - head, y - head), 1.dp.toPx())
                    drawLine(TabBlue, Offset(end, y), Offset(end - head, y + head), 1.dp.toPx())
                }
                frame.step.dpTable.forEachIndexed { index, value ->
                    val dest = frame.step.action == TabulationAction.FILL_TABLE && frame.step.currentIndex == index
                    val source = frame.dependencies.any { it.source == index }
                    val final = frame.step.action == TabulationAction.FINISHED && index == frame.step.targetN
                    val computed = frame.step.action != TabulationAction.INIT && (index <= 2 || (frame.step.currentIndex ?: -1) >= index)
                    val color = if (dest || final) TabGreen else if (source || computed) TabBlue else TabBorder
                    text("dp[$index]", layout.indices[index], if (dest) TabOrange else TabSecondary, 9, true)
                    card(layout.cells[index], if (dest || final) Color(0xFF123820) else if (computed) Color(0xFF122C38) else Color(0xFF181824), color)
                    text(value.toString(), layout.cells[index], if (computed) Color.White else TabSecondary, 12, true)
                }
                card(layout.rolling, Color(0xFF13131C), TabBorder)
                text("ROLLING VARIABLES (O(1) ALTERNATIVE):", layout.rollingHeading, TabBlue, 10)
                val values = listOf("prev2 = ${if (frame.step.rollingInitialized) frame.step.prev2 else "–"}",
                    "prev1 = ${if (frame.step.rollingInitialized) frame.step.prev1 else "–"}", "current = ${frame.step.currentWays ?: "–"}")
                values.forEachIndexed { i, value -> text(value, layout.variables[i], if (i == 2) TabGreen else Color.White, 11, true) }
                text(tabFormula(frame.step), layout.formula, TabSecondary, 10)
            }
        }
    }
}
