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

private val LcsOrange = Color(0xFFFA6E13)
private val LcsBlue = Color(0xFF38BDF8)
private val LcsGreen = Color(0xFF10B981)
private val LcsGray = Color(0xFF9CA3AF)
private fun lcsFormula(frame: LcsSceneFrame): String {
    val s = frame.step; val i = s.currentI; val j = s.currentJ
    if (s.action != LcsAction.COMPUTE_CELL || i == null || j == null) return "LCS sequence: '${s.lcsString}'"
    return if (s.isMatch == true) "dp[$i][$j] = 1 + ${s.dpMatrix[i - 1][j - 1]} = ${s.dpMatrix[i][j]}"
    else "dp[$i][$j] = max(${s.dpMatrix[i - 1][j]}, ${s.dpMatrix[i][j - 1]}) = ${s.dpMatrix[i][j]}"
}

@Composable
internal fun LcsDpSceneRenderer(scene: LcsPresentation, frame: LcsSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer(); val density = LocalDensity.current
        fun measure(s: String): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(s), TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        val layout = remember(scene, maxWidth, density.density, density.fontScale, measurer) {
            val texts = scene.frames.flatMap { listOf(lcsFormula(it), "LCS: '${it.step.lcsString}' · length ${it.step.dpMatrix.last().last()}") } + "LCS DP MATRIX"
            val dimensions = texts.map(::measure)
            val cells = scene.frames.flatMap { f -> f.step.dpMatrix.flatten().map(Int::toString) }.map(::measure)
            LcsDpSceneLayout.calculate(scene, maxWidth.value, dimensions.maxOf { it.first }, dimensions.maxOf { it.second },
                cells.maxOfOrNull { it.first } ?: 10f)
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-lcs", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun text(value: String, rect: LcsRect, color: Color, center: Boolean = false) {
                    val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    drawText(result, topLeft = Offset(rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - result.size.width) / 2f else 0f,
                        rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                fun edge(from: LcsRect, to: LcsRect): Pair<Offset, Offset> {
                    val down = to.y > from.y; val up = to.y < from.y
                    val right = to.x > from.x; val left = to.x < from.x
                    val sx = if (right) from.right - 3f else if (left) from.x + 3f else from.right - 3f
                    val ex = if (right) to.x + 3f else if (left) to.right - 3f else to.right - 3f
                    val sy = if (down) from.bottom - 3f else if (up) from.y + 3f else from.y + 3f
                    val ey = if (down) to.y + 3f else if (up) to.bottom - 3f else to.y + 3f
                    return Offset(sx.dp.toPx(), sy.dp.toPx()) to Offset(ex.dp.toPx(), ey.dp.toPx())
                }
                text("LCS DP MATRIX", layout.heading, LcsOrange)
                val s = frame.step
                layout.rowHeaders.forEachIndexed { i, r -> text(if (i == 0) "∅" else s.text1[i - 1].toString(), r,
                    if (s.action == LcsAction.COMPUTE_CELL && i == s.currentI) LcsOrange else LcsGray, true) }
                layout.columnHeaders.forEachIndexed { j, r -> text(if (j == 0) "∅" else s.text2[j - 1].toString(), r,
                    if (s.action == LcsAction.COMPUTE_CELL && j == s.currentJ) LcsOrange else LcsGray, true) }
                layout.cells.forEachIndexed { i, row -> row.forEachIndexed { j, r ->
                    val active = (s.action == LcsAction.COMPUTE_CELL || s.action == LcsAction.RECONSTRUCT) && i == s.currentI && j == s.currentJ
                    val dependency = LcsCellId(i, j) in frame.dependencies
                    val final = s.action == LcsAction.FINISHED && i == scene.rows - 1 && j == scene.columns - 1
                    val color = if (active) LcsOrange else if (final) LcsGreen else if (dependency) LcsBlue else Color(0xFF343444)
                    val pos = Offset(r.x.dp.toPx(), r.y.dp.toPx()); val size = Size(r.width.dp.toPx(), r.height.dp.toPx())
                    drawRect(if (active) Color(0xFF2A1C12) else Color(0xFF181824), pos, size)
                    drawRect(color, pos, size, style = Stroke(1.dp.toPx()))
                    text(s.dpMatrix[i][j].toString(), r,
                        if (LcsCellId(i, j) in frame.computed) Color.White else LcsGray, true)
                } }
                if (s.action == LcsAction.COMPUTE_CELL && s.currentI != null && s.currentJ != null) {
                    val destination = layout.cells[s.currentI][s.currentJ]
                    frame.dependencies.forEach { id ->
                        val source = layout.cells[id.row][id.column]
                        val (start, end) = edge(source, destination)
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
                    val from = layout.cells[s.currentI][s.currentJ]; val to = layout.cells[s.nextI][s.nextJ]
                    val (start, end) = edge(from, to)
                    drawLine(LcsGreen, start, end, 2.dp.toPx())
                    val dx = end.x - start.x; val dy = end.y - start.y
                    val length = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (length > 0f) {
                        val ux = dx / length; val uy = dy / length; val h = 4.dp.toPx()
                        drawLine(LcsGreen, end, Offset(end.x - h * ux - h * uy / 2f, end.y - h * uy + h * ux / 2f), 1.dp.toPx())
                        drawLine(LcsGreen, end, Offset(end.x - h * ux + h * uy / 2f, end.y - h * uy - h * ux / 2f), 1.dp.toPx())
                    }
                }
                text(lcsFormula(frame), layout.summary[0], Color.White)
                text("LCS: '${s.lcsString}' · length ${s.dpMatrix.last().last()}", layout.summary[1], LcsGreen)
            }
        }
    }
}
