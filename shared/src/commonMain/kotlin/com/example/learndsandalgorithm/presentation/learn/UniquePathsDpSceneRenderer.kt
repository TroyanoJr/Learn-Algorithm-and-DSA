package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

private val PathOrange = Color(0xFFFA6E13)
private val PathBlue = Color(0xFF38BDF8)
private val PathGreen = Color(0xFF10B981)
private val PathGray = Color(0xFF9CA3AF)
private fun pathFormula(s: UniquePathsDpStep): String = when (s.action) {
    UniquePathsDpAction.INIT -> "dp[r][c] = dp[r−1][c] + dp[r][c−1]"
    UniquePathsDpAction.FILL_BASE_CASES -> "First row and first column: 1"
    UniquePathsDpAction.COMPUTE_CELL -> "dp[${s.currentR}][${s.currentC}] = ${s.topVal} + ${s.leftVal} = ${s.dpGrid[s.currentR!!][s.currentC!!]}"
    UniquePathsDpAction.FINISHED -> "Total unique paths: ${s.dpGrid.last().last()}"
}
@Composable
internal fun UniquePathsDpSceneRenderer(scene: UniquePathsPresentation, frame: UniquePathsFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer(); val density = LocalDensity.current
        fun measure(s: String): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(s), TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        val layout = remember(scene, maxWidth, density.density, density.fontScale, measurer) {
            val texts = (scene.frames.map { pathFormula(it.step) } + "UNIQUE PATHS · 2D DP GRID").map(::measure)
            val cells = scene.frames.flatMap { it.step.dpGrid.flatten() }.map { measure(it.toString()) }
            UniquePathsDpSceneLayout.calculate(scene, maxWidth.value, texts.maxOf { it.first }, texts.maxOf { it.second },
                cells.maxOfOrNull { it.first } ?: 10f)
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-unique-paths", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun text(value: String, rect: PathRect, color: Color, center: Boolean = false) {
                    val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    drawText(result, topLeft = Offset(rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - result.size.width) / 2f else 0f,
                        rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                text("UNIQUE PATHS · 2D DP GRID", layout.heading, PathOrange)
                layout.rowHeaders.forEachIndexed { i, rect -> text("r$i", rect, PathGray, true) }
                layout.columnHeaders.forEachIndexed { i, rect -> text("c$i", rect, PathGray, true) }
                val s = frame.step
                layout.cells.forEachIndexed { r, row -> row.forEachIndexed { c, rect ->
                    val id = PathCellId(r, c)
                    val active = s.action == UniquePathsDpAction.COMPUTE_CELL && s.currentR == r && s.currentC == c
                    val final = s.action == UniquePathsDpAction.FINISHED && r == scene.rows - 1 && c == scene.columns - 1
                    val color = if (active) PathOrange else if (final) PathGreen else if (id in frame.dependencies) PathBlue else if (id in frame.bases) PathGreen else Color(0xFF343444)
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRect(if (active) Color(0xFF2A1C12) else Color(0xFF181824), pos, size)
                    drawRect(color, pos, size, style = Stroke(1.dp.toPx()))
                    text(s.dpGrid[r][c].toString(), rect, if (id in frame.computed) Color.White else PathGray, true)
                } }
                if (s.action == UniquePathsDpAction.COMPUTE_CELL && s.currentR != null && s.currentC != null) {
                    val destination = layout.cells[s.currentR][s.currentC]
                    frame.dependencies.forEach { id ->
                        val source = layout.cells[id.row][id.column]
                        val vertical = id.row != s.currentR
                        val start = if (vertical) Offset((source.right - 3f).dp.toPx(), (source.bottom - 3f).dp.toPx())
                            else Offset((source.right - 3f).dp.toPx(), (source.y + 3f).dp.toPx())
                        val end = if (vertical) Offset((destination.right - 3f).dp.toPx(), (destination.y + 3f).dp.toPx())
                            else Offset((destination.x + 3f).dp.toPx(), (destination.y + 3f).dp.toPx())
                        drawLine(PathBlue, start, end, 1.dp.toPx())
                        val dx = end.x - start.x; val dy = end.y - start.y
                        val length = kotlin.math.sqrt(dx * dx + dy * dy)
                        if (length > 0f) {
                            val ux = dx / length; val uy = dy / length; val h = 4.dp.toPx()
                            drawLine(PathBlue, end, Offset(end.x - h * ux - h * uy / 2f, end.y - h * uy + h * ux / 2f), 1.dp.toPx())
                            drawLine(PathBlue, end, Offset(end.x - h * ux + h * uy / 2f, end.y - h * uy - h * ux / 2f), 1.dp.toPx())
                        }
                    }
                }
                text(pathFormula(s), layout.formula, PathGreen)
            }
        }
    }
}
