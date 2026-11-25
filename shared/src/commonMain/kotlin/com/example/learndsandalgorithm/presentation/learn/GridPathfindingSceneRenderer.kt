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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.sqrt

private val GridRegion = Color(0xFF13131C)
private val GridBorder = Color(0xFF262634)
private val GridOrange = Color(0xFFFA6E13)
private val GridBlue = Color(0xFF38BDF8)
private val GridGreen = Color(0xFF10B981)
private val GridRed = Color(0xFFEF4444)
private val GridSecondary = Color(0xFF9CA3AF)
private const val GridExplanation = "O(4^(R×C)) is a broad upper bound assuming 4 directions per step. Each cell is visited at most once per simple path, and actual exploration depends heavily on grid dimensions, walls, boundaries, and visited-cell restrictions."
private fun gridPathLabel(frame: GridSceneFrame): String = "Active Path: ${frame.path.map { "(${it.r},${it.c})" }}"
private fun gridCounterLabel(frame: GridSceneFrame): String = "${frame.pathCells} Steps · Path cells: ${frame.pathCells} · Moves: ${frame.moves}"
private fun gridOperationLabel(frame: GridSceneFrame): String = when {
    frame.rejection == GridRejection.OUT_OF_BOUNDS -> "REJECTED: OUT OF BOUNDS ${frame.rejectedCell?.let { "(${it.r},${it.c})" }}"
    frame.rejection == GridRejection.OBSTACLE -> "REJECTED: OBSTACLE ${frame.rejectedCell?.let { "(${it.r},${it.c})" }}"
    frame.rejection == GridRejection.REVISITED -> "REJECTED: REVISITED ${frame.rejectedCell?.let { "(${it.r},${it.c})" }}"
    frame.removedCell != null -> "BACKTRACK FROM (${frame.removedCell.r},${frame.removedCell.c})"
    else -> "PATH EXPLORATION"
}

@Composable
internal fun GridPathfindingSceneRenderer(presentation: GridPathPresentation, frame: GridSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun style(size: Int, bold: Boolean, lineHeight: Boolean = false) = TextStyle(fontSize = size.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, lineHeight = if (lineHeight) 13.sp else androidx.compose.ui.unit.TextUnit.Unspecified)
        fun extent(text: String, size: Int, bold: Boolean = true, wrap: Boolean = false, lineHeight: Boolean = false, panelWidth: Float = 250f): Pair<Float, Float> {
            val width = with(density) { panelWidth.dp.roundToPx() }.coerceAtLeast(1)
            val result = measurer.measure(AnnotatedString(text), style(size, bold, lineHeight), softWrap = wrap,
                constraints = if (wrap) Constraints(maxWidth = width) else Constraints())
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(texts: Iterable<String>, size: Int, bold: Boolean = true, wrap: Boolean = false, panelWidth: Float = 250f): Pair<Float, Float> {
            val all = texts.map { extent(it, size, bold, wrap, panelWidth = panelWidth) }
            return (all.maxOfOrNull { it.first } ?: 1f) to (all.maxOfOrNull { it.second } ?: 1f)
        }
        val heading = extent("${presentation.rows}x${presentation.cols} MATRIX GRID MAP:", 10)
        val indexes = maximum((0 until maxOf(presentation.rows, presentation.cols)).map(Int::toString), 9)
        val symbols = maximum(listOf("📍", "🎯", "🟩", "⬛", "⬜", "❌"), 16, false)
        val explanationHeading = extent("COMPLEXITY & BOUNDS EXPLANATION:", 9)
        // Measure the full paragraphs at several viable widths and compare the real
        // two-dimensional fit. The final panel is at least as wide as its measurement.
        val choices = listOf(150f, 180f, 210f, 250f).map { candidateWidth ->
            val actualWidth = maxOf(candidateWidth, explanationHeading.first)
            val explanation = extent(GridExplanation, 9, false, true, true, actualWidth)
            val paths = maximum(presentation.frames.map(::gridPathLabel), 11, true, true, actualWidth)
            val counters = maximum(presentation.frames.map(::gridCounterLabel), 10, true, true, actualWidth)
            val operations = maximum(presentation.frames.map(::gridOperationLabel), 9, true, true, actualWidth)
            GridLayoutMetrics(heading.first, heading.second, indexes.first, indexes.second, symbols.first, symbols.second,
                explanationHeading.second, explanation.second, paths.second, counters.second, operations.second, actualWidth)
        }
        val layout = GridPathfindingSceneLayout.choose(presentation, maxWidth.value, maxHeight.value, choices)
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "backtracking-grid", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(rect: GridSceneRect, fill: Color, border: Color, dashed: Boolean = false) {
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx(),
                        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())) else null))
                }
                fun label(text: String, rect: GridSceneRect, color: Color, size: Int, bold: Boolean = true, centered: Boolean = false, wrap: Boolean = false, lineHeight: Boolean = false) {
                    val width = rect.width.dp.roundToPx().coerceAtLeast(1)
                    val result = measurer.measure(AnnotatedString(text), style(size, bold, lineHeight), softWrap = wrap,
                        constraints = if (wrap) Constraints(maxWidth = width) else Constraints())
                    val x = rect.x.dp.toPx() + if (centered) (rect.width.dp.toPx() - result.size.width) / 2f else 0f
                    drawText(result, topLeft = Offset(x, rect.y.dp.toPx() + if (wrap) 0f else (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                fun arrow(a: GridSceneRect, b: GridSceneRect, color: Color) {
                    val from = Offset(a.centerX.dp.toPx(), a.centerY.dp.toPx()); val to = Offset(b.centerX.dp.toPx(), b.centerY.dp.toPx())
                    val delta = to - from; val length = sqrt(delta.x * delta.x + delta.y * delta.y).coerceAtLeast(1f)
                    val unit = delta / length; val tip = to - unit * 10.dp.toPx(); val base = tip - unit * 5.dp.toPx(); val normal = Offset(-unit.y, unit.x) * 3.dp.toPx()
                    drawLine(color, from, tip, 2.dp.toPx()); drawLine(color, tip, base + normal, 2.dp.toPx()); drawLine(color, tip, base - normal, 2.dp.toPx())
                }
                card(layout.boardRegion, GridRegion, GridBorder)
                label("${presentation.rows}x${presentation.cols} MATRIX GRID MAP:", layout.heading, GridOrange, 10)
                layout.rows.forEachIndexed { i, rect -> label(i.toString(), rect, GridSecondary, 9, centered = true) }
                layout.columns.forEachIndexed { i, rect -> label(i.toString(), rect, GridSecondary, 9, centered = true) }
                presentation.cells.forEach { cell ->
                    val p = cell.position; val rejected = p == frame.rejectedCell; val current = p == frame.path.lastOrNull() && frame.rejection == null
                    val target = p == frame.step.targetCell; val inPath = p in frame.path
                    val fill = when { rejected -> Color(0xFF381215); current && !target -> Color(0xFF2A1C12); inPath -> Color(0xFF123820); cell.wall -> Color(0xFF22222E); else -> Color(0xFF181824) }
                    val border = when { rejected || p == frame.removedCell -> GridRed; current && !target -> GridOrange; inPath || target -> GridGreen; else -> GridBorder }
                    card(layout.cells.getValue(p), fill, border, p == frame.removedCell)
                }
                frame.connections.forEach { arrow(layout.cells.getValue(it.from), layout.cells.getValue(it.to), GridGreen) }
                if (frame.rejectionOrigin != null && frame.rejectedCell != null) {
                    val from = layout.cells[frame.rejectionOrigin]; val to = layout.cells[frame.rejectedCell] ?: layout.outside[frame.rejectedCell]
                    if (from != null && to != null) arrow(from, to, GridRed)
                }
                if (frame.removedCell != null && frame.restoredCell != null) {
                    val from = layout.cells[frame.removedCell]; val to = layout.cells[frame.restoredCell]
                    if (from != null && to != null) arrow(from, to, GridRed)
                }
                frame.rejectedCell?.let { rejected -> layout.outside[rejected]?.let { card(it, GridRegion, GridRed, true); label("❌", it, GridRed, 16, false, true) } }
                presentation.cells.forEach { cell ->
                    val p = cell.position
                    val symbol = when {
                        p == frame.rejectedCell -> "❌"
                        p == frame.removedCell -> "📍"
                        p == frame.step.targetCell -> "🎯"
                        p == frame.path.lastOrNull() || (frame.step.action == GridPathAction.INIT && p == frame.step.currentCell) -> "📍"
                        p in frame.path -> "🟩"
                        cell.wall -> "⬛"
                        else -> "⬜"
                    }
                    label(symbol, layout.cells.getValue(p), if (p == frame.removedCell) GridSecondary.copy(alpha = .5f) else Color.White, 16, false, true)
                }
                card(layout.explanationRegion, GridRegion, GridBorder)
                label("COMPLEXITY & BOUNDS EXPLANATION:", layout.explanationHeading, GridBlue, 9)
                label(GridExplanation, layout.explanation, Color.White, 9, false, wrap = true, lineHeight = true)
                card(layout.pathRegion, GridRegion, GridBorder)
                label(gridPathLabel(frame), layout.path, Color.White, 11, wrap = true)
                label(gridCounterLabel(frame), layout.counter, GridGreen, 10, wrap = true)
                label(gridOperationLabel(frame), layout.operation, if (frame.rejection != null || frame.removedCell != null) GridRed else GridOrange, 9, wrap = true)
            }
        }
    }
}
