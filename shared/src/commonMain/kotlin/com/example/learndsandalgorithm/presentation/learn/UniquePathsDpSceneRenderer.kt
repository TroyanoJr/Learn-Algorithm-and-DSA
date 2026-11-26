package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
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
    UniquePathsDpAction.COMPUTE_CELL -> "Cell (${s.currentR},${s.currentC}): Top ${s.topVal} + Left ${s.leftVal} = ${s.dpGrid[s.currentR!!][s.currentC!!]}"
    UniquePathsDpAction.FINISHED -> "Total unique paths: ${s.dpGrid.last().last()}"
}
@Composable
internal fun UniquePathsDpSceneRenderer(scene: UniquePathsPresentation, frame: UniquePathsFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer(); val density = LocalDensity.current
        fun measure(s: String, size: Int = 10): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(s), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        val layout = remember(scene, maxWidth, density.density, density.fontScale, measurer) {
            val texts = (scene.frames.map { pathFormula(it.step) } + "UNIQUE PATHS · 2D DP GRID").map { measure(it) }
            val cells = scene.frames.flatMap { it.step.dpGrid.flatten() }.map { measure(it.toString(), 12) }
            val headers = ((0 until scene.rows).map { "r$it" } + (0 until scene.columns).map { "c$it" }).map { measure(it).first }
            UniquePathsDpSceneLayout.calculate(scene, maxWidth.value, texts.maxOf { it.first },
                maxOf(texts.maxOf { it.second }, cells.maxOfOrNull { it.second } ?: 0f),
                maxOf(cells.maxOfOrNull { it.first } ?: 10f, headers.maxOrNull() ?: 0f), headers.maxOrNull() ?: 10f)
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-unique-paths", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun text(value: String, rect: PathRect, color: Color, center: Boolean = false, size: Int = 10) {
                    val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    drawText(result, topLeft = Offset(rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - result.size.width) / 2f else 0f,
                        rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                val matrixBottom = layout.cells.last().last().bottom + 3f
                val panelColor = Color(0xFF13131C)
                val panelBorder = Color(0xFF262634)
                val matrixPos = Offset(8.dp.toPx(), 4.dp.toPx())
                val matrixSize = Size((layout.width - 16f).dp.toPx(), (matrixBottom - 4f).dp.toPx())
                drawRoundRect(panelColor, matrixPos, matrixSize, CornerRadius(6.dp.toPx()))
                drawRoundRect(panelBorder, matrixPos, matrixSize, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                val formulaPos = Offset(8.dp.toPx(), (layout.formula.y - 3f).dp.toPx())
                val formulaSize = Size((layout.width - 16f).dp.toPx(), (layout.formula.height + 6f).dp.toPx())
                drawRoundRect(panelColor, formulaPos, formulaSize, CornerRadius(6.dp.toPx()))
                drawRoundRect(panelBorder, formulaPos, formulaSize, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                text("UNIQUE PATHS · 2D DP GRID", layout.heading, PathOrange)
                layout.rowHeaders.forEachIndexed { i, rect -> text("r$i", rect, PathGray, true) }
                layout.columnHeaders.forEachIndexed { i, rect -> text("c$i", rect, PathGray, true) }
                val s = frame.step
                layout.cells.forEachIndexed { r, row -> row.forEachIndexed { c, rect ->
                    val id = PathCellId(r, c)
                    val active = s.action == UniquePathsDpAction.COMPUTE_CELL && s.currentR == r && s.currentC == c
                    val final = s.action == UniquePathsDpAction.FINISHED && r == scene.rows - 1 && c == scene.columns - 1
                    val color = if (active || final || s.action == UniquePathsDpAction.FILL_BASE_CASES && id in frame.bases) PathGreen
                        else if (id in frame.dependencies) PathOrange else if (id in frame.computed) PathBlue else Color(0xFF343444)
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(if (active) Color(0xFF123820) else Color(0xFF181824), pos, size, CornerRadius(4.dp.toPx()))
                    drawRoundRect(color, pos, size, CornerRadius(4.dp.toPx()), style = Stroke(1.dp.toPx()))
                    text(s.dpGrid[r][c].toString(), rect, if (id in frame.computed) Color.White else PathGray, true, 12)
                } }
                if (s.action == UniquePathsDpAction.COMPUTE_CELL && s.currentR != null && s.currentC != null) {
                    val destination = PathCellId(s.currentR, s.currentC)
                    frame.dependencies.forEach { id ->
                        val arrow = layout.dependencyArrow(id, destination)
                        val start = Offset(arrow.startX.dp.toPx(), arrow.startY.dp.toPx())
                        val end = Offset(arrow.endX.dp.toPx(), arrow.endY.dp.toPx())
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
