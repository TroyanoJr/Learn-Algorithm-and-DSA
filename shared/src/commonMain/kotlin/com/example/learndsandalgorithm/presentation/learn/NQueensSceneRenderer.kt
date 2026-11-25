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

private val QueenRegion = Color(0xFF13131C)
private val QueenBorder = Color(0xFF262634)
private val QueenOrange = Color(0xFFFA6E13)
private val QueenGreen = Color(0xFF10B981)
private val QueenRed = Color(0xFFEF4444)
private val QueenSecondary = Color(0xFF9CA3AF)

@Composable
internal fun NQueensSceneRenderer(presentation: NQueensPresentation, frame: NQueensSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun measure(text: String, size: Int, bold: Boolean = true): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(texts: Iterable<String>, size: Int, bold: Boolean = true): Pair<Float, Float> {
            val values = texts.map { measure(it, size, bold) }
            return (values.maxOfOrNull { it.first } ?: 1f) to (values.maxOfOrNull { it.second } ?: 1f)
        }
        val layout = remember(presentation, maxWidth, maxHeight, density.density, density.fontScale, measurer) {
        val heading = measure("${presentation.size}x${presentation.size} CHESSBOARD STATE:", 10)
        val indexes = maximum((0 until presentation.size).map(Int::toString), 9)
        val symbols = 22f to 22f
        val solutionHeading = measure("Solutions:", 11)
        val counts = maximum(presentation.frames.map { "${it.step.solutions.size} Valid" }, 11)
        val solutions = maximum(presentation.frames.flatMap { it.step.solutions }.map { it.toString() } + listOf("[]"), 11)
        val metrics = QueensLayoutMetrics(heading.first, heading.second, indexes.first, indexes.second, symbols.first, symbols.second,
            solutionHeading.first, counts.first, maxOf(solutionHeading.second, counts.second), solutions.first, solutions.second)
        NQueensSceneLayout.calculate(presentation, maxWidth.value, maxHeight.value, metrics)
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "backtracking-nqueens", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(rect: QueensRect, fill: Color, border: Color, dashed: Boolean = false) {
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx())
                    val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx(),
                        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())) else null))
                }
                fun label(text: String, rect: QueensRect, color: Color, size: Int, bold: Boolean = true, align: Int = 0) {
                    val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
                    val extra = rect.width.dp.toPx() - result.size.width
                    val x = rect.x.dp.toPx() + when (align) { 1 -> extra / 2f; 2 -> extra; else -> 0f }
                    drawText(result, topLeft = Offset(x, rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                card(layout.boardRegion, QueenRegion, QueenBorder)
                label("${presentation.size}x${presentation.size} CHESSBOARD STATE:", layout.boardHeading, QueenOrange, 10)
                layout.columns.forEachIndexed { index, rect -> label(index.toString(), rect, QueenSecondary, 9, align = 1) }
                layout.rows.forEachIndexed { index, rect -> label(index.toString(), rect, QueenSecondary, 9, align = 1) }
                presentation.cells.forEach { cell ->
                    val placed = cell in frame.placed
                    val testing = cell == frame.testing
                    val ghost = cell == frame.removed
                    val conflict = testing && frame.step.action == NQueensAction.CONFLICT_PRUNE
                    val fill = when {
                        placed -> Color(0xFF123820); conflict -> Color(0xFF381215); testing -> Color(0xFF2A1C12)
                        (cell.row + cell.column) % 2 == 1 -> Color(0xFF1A1A26); else -> Color(0xFF242436)
                    }
                    val border = when { placed -> QueenGreen; conflict || ghost -> QueenRed; testing -> QueenOrange; else -> QueenBorder }
                    card(layout.cells.getValue(cell), fill, border, ghost)
                }
                frame.conflicts.forEach { conflict ->
                    val from = layout.cells.getValue(conflict.from)
                    val to = layout.cells.getValue(conflict.to)
                    val color = if (conflict.kind == QueenConflictKind.COLUMN) QueenRed else QueenOrange
                    val segment = backtrackingConnection(backtrackingIconBounds(from.x, from.y, from.width, from.height),
                        backtrackingIconBounds(to.x, to.y, to.width, to.height))
                    drawLine(color, Offset(segment.startX.dp.toPx(), segment.startY.dp.toPx()),
                        Offset(segment.endX.dp.toPx(), segment.endY.dp.toPx()), 2.dp.toPx(),
                        pathEffect = if (conflict.kind == QueenConflictKind.DIAGONAL) PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 3.dp.toPx())) else null)
                }
                presentation.cells.forEach { cell ->
                    val symbol = when {
                        cell in frame.placed -> BacktrackingIcon.QUEEN
                        cell == frame.removed -> BacktrackingIcon.QUEEN
                        cell == frame.testing && frame.step.action == NQueensAction.CONFLICT_PRUNE -> BacktrackingIcon.CROSS
                        cell == frame.testing -> BacktrackingIcon.SEARCH
                        else -> null
                    }
                    if (symbol != null) {
                        val rect = layout.cells.getValue(cell)
                        drawBacktrackingIcon(symbol, backtrackingIconBounds(rect.x, rect.y, rect.width, rect.height),
                            if (cell == frame.removed) QueenRed.copy(alpha = .65f) else Color.White)
                    }
                }
                card(layout.solutionRegion, QueenRegion, QueenBorder)
                label("Solutions:", layout.solutionHeading, Color.White, 11)
                label("${frame.step.solutions.size} Valid", layout.solutionCount, QueenGreen, 11, align = 2)
                if (frame.step.solutions.isEmpty()) label("[]", layout.empty, QueenSecondary, 11)
                frame.step.solutions.forEachIndexed { index, solution -> label(solution.toString(), layout.solutions[index], Color.White, 11) }
            }
        }
    }
}
