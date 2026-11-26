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

private val LcsOrange = Color(0xFFFA6E13)
private val LcsBlue = Color(0xFF38BDF8)
private val LcsGreen = Color(0xFF10B981)
private val LcsDependency = Color(0xFFA78BFA)
private val LcsGray = Color(0xFF9CA3AF)
private fun lcsFormula(frame: LcsSceneFrame): String {
    val s = frame.step; val i = s.currentI; val j = s.currentJ
    if (s.action == LcsAction.RECONSTRUCT) return "${s.movement} · ${s.acceptedCharacter?.let { "accepted '$it'" } ?: "no character accepted"} · ($i,$j) → (${s.nextI},${s.nextJ})"
    if (s.action != LcsAction.COMPUTE_CELL || i == null || j == null) return "LCS DP matrix"
    return if (s.isMatch == true) "dp[$i][$j] = 1 + ${s.dpMatrix[i - 1][j - 1]} = ${s.dpMatrix[i][j]}"
    else "dp[$i][$j] = max(${s.dpMatrix[i - 1][j]}, ${s.dpMatrix[i][j - 1]}) = ${s.dpMatrix[i][j]}"
}

@Composable
internal fun LcsDpSceneRenderer(scene: LcsPresentation, frame: LcsSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer(); val density = LocalDensity.current
        fun measure(s: String, size: Int = 10): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(s), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        val layout = remember(scene, maxWidth, density.density, density.fontScale, measurer) {
            val texts = scene.frames.flatMap { listOf(lcsFormula(it), "Subsequence: '${it.step.lcsString}' · reconstructed ${it.step.lcsString.length} / ${it.step.dpMatrix.last().last()}") } + "LCS DP MATRIX"
            val dimensions = texts.map { measure(it) }
            val cells = scene.frames.flatMap { f -> f.step.dpMatrix.flatten().map(Int::toString) }.map { measure(it, 11) }
            val headers = (scene.frames.first().step.text1.toList() + scene.frames.first().step.text2.toList()).map { measure(it.toString()).first } + measure("∅").first
            LcsDpSceneLayout.calculate(scene, maxWidth.value, dimensions.maxOf { it.first },
                maxOf(dimensions.maxOf { it.second }, cells.maxOfOrNull { it.second } ?: 0f),
                maxOf(cells.maxOfOrNull { it.first } ?: 10f, headers.maxOrNull() ?: 0f), headers.maxOrNull() ?: 10f)
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-lcs", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun text(value: String, rect: LcsRect, color: Color, center: Boolean = false, size: Int = 10) {
                    val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    drawText(result, topLeft = Offset(rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - result.size.width) / 2f else 0f,
                        rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                fun edge(from: LcsCellId, to: LcsCellId): Pair<Offset, Offset> {
                    val arrow = layout.arrow(from, to)
                    return Offset(arrow.startX.dp.toPx(), arrow.startY.dp.toPx()) to
                        Offset(arrow.endX.dp.toPx(), arrow.endY.dp.toPx())
                }
                val matrixBottom = layout.cells.last().last().bottom + 3f
                val panelColor = Color(0xFF13131C)
                val panelBorder = Color(0xFF262634)
                val matrixPos = Offset(8.dp.toPx(), 3.dp.toPx())
                val matrixSize = Size((layout.width - 16f).dp.toPx(), (matrixBottom - 3f).dp.toPx())
                drawRoundRect(panelColor, matrixPos, matrixSize, CornerRadius(6.dp.toPx()))
                drawRoundRect(panelBorder, matrixPos, matrixSize, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                val summaryPos = Offset(8.dp.toPx(), (layout.summary.first().y - 3f).dp.toPx())
                val summarySize = Size((layout.width - 16f).dp.toPx(),
                    (layout.summary.last().bottom - layout.summary.first().y + 6f).dp.toPx())
                drawRoundRect(panelColor, summaryPos, summarySize, CornerRadius(6.dp.toPx()))
                drawRoundRect(panelBorder, summaryPos, summarySize, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                text("LCS DP MATRIX", layout.heading, LcsOrange)
                val s = frame.step
                layout.rowHeaders.forEachIndexed { i, r -> text(if (i == 0) "∅" else s.text1[i - 1].toString(), r,
                    if (s.action in listOf(LcsAction.COMPUTE_CELL, LcsAction.RECONSTRUCT) && i == s.currentI) LcsOrange else LcsBlue, true) }
                layout.columnHeaders.forEachIndexed { j, r -> text(if (j == 0) "∅" else s.text2[j - 1].toString(), r,
                    if (s.action in listOf(LcsAction.COMPUTE_CELL, LcsAction.RECONSTRUCT) && j == s.currentJ) LcsOrange else LcsBlue, true) }
                layout.cells.forEachIndexed { i, row -> row.forEachIndexed { j, r ->
                    val active = (s.action == LcsAction.COMPUTE_CELL || s.action == LcsAction.RECONSTRUCT) && i == s.currentI && j == s.currentJ
                    val dependency = LcsCellId(i, j) in frame.dependencies
                    val final = s.action == LcsAction.FINISHED && i == scene.rows - 1 && j == scene.columns - 1
                    val next = s.action == LcsAction.RECONSTRUCT && i == s.nextI && j == s.nextJ
                    val accepted = s.action == LcsAction.RECONSTRUCT && s.acceptedCharacter != null
                    val color = if (active && s.isMatch == true || final || next && accepted) LcsGreen
                        else if (active || next) LcsOrange else if (dependency) LcsDependency
                        else if (LcsCellId(i, j) in frame.computed) LcsBlue else Color(0xFF343444)
                    val pos = Offset(r.x.dp.toPx(), r.y.dp.toPx()); val size = Size(r.width.dp.toPx(), r.height.dp.toPx())
                    drawRoundRect(if (active) Color(0xFF2A1C12) else Color(0xFF181824), pos, size, CornerRadius(4.dp.toPx()))
                    drawRoundRect(color, pos, size, CornerRadius(4.dp.toPx()), style = Stroke(1.dp.toPx()))
                    text(s.dpMatrix[i][j].toString(), r,
                        if (LcsCellId(i, j) in frame.computed) Color.White else LcsGray, true, 11)
                } }
                if (s.action == LcsAction.COMPUTE_CELL && s.currentI != null && s.currentJ != null) {
                    val destination = LcsCellId(s.currentI, s.currentJ)
                    frame.dependencies.forEach { id ->
                        val (start, end) = edge(id, destination)
                        val dx = end.x - start.x; val dy = end.y - start.y
                        val length = kotlin.math.sqrt(dx * dx + dy * dy)
                        drawLine(LcsBlue, start, end, 1.dp.toPx())
                        if (length > 0f) {
                            val ux = dx / length; val uy = dy / length; val h = 4.dp.toPx()
                            drawLine(LcsBlue, end, Offset(end.x - h * ux - h * uy / 2f, end.y - h * uy + h * ux / 2f), 1.dp.toPx())
                            drawLine(LcsBlue, end, Offset(end.x - h * ux + h * uy / 2f, end.y - h * uy - h * ux / 2f), 1.dp.toPx())
                        }
                    }
                }
                if (s.action == LcsAction.RECONSTRUCT && s.currentI != null && s.currentJ != null && s.nextI != null && s.nextJ != null) {
                    val from = LcsCellId(s.currentI, s.currentJ); val to = LcsCellId(s.nextI, s.nextJ)
                    val (start, end) = edge(from, to)
                    val movementColor = if (s.acceptedCharacter != null) LcsGreen else LcsOrange
                    drawLine(movementColor, start, end, 2.dp.toPx())
                    val dx = end.x - start.x; val dy = end.y - start.y
                    val length = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (length > 0f) {
                        val ux = dx / length; val uy = dy / length; val h = 4.dp.toPx()
                        drawLine(movementColor, end, Offset(end.x - h * ux - h * uy / 2f, end.y - h * uy + h * ux / 2f), 1.dp.toPx())
                        drawLine(movementColor, end, Offset(end.x - h * ux + h * uy / 2f, end.y - h * uy - h * ux / 2f), 1.dp.toPx())
                    }
                }
                text(lcsFormula(frame), layout.summary[0], Color.White)
                text("Subsequence: '${s.lcsString}' · reconstructed ${s.lcsString.length} / ${s.dpMatrix.last().last()}", layout.summary[1], LcsGreen)
            }
        }
    }
}
