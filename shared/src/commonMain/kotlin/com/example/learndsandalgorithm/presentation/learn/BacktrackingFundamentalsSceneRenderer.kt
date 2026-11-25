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
import kotlin.math.sqrt

private val FundamentalRegion = Color(0xFF13131C)
private val FundamentalBorder = Color(0xFF262634)
private val FundamentalOrange = Color(0xFFFA6E13)
private val FundamentalGreen = Color(0xFF10B981)
private val FundamentalRed = Color(0xFFEF4444)
private val FundamentalBlue = Color(0xFF38BDF8)
private val FundamentalSecondary = Color(0xFF9CA3AF)

@Composable
internal fun BacktrackingFundamentalsSceneRenderer(presentation: FundamentalsTreePresentation, frame: FundamentalsTreeFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun extent(text: String, size: Int, bold: Boolean = true): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun nodeLabel(path: String): String = when (path.length) {
            0 -> "Root (\"\")"
            1 -> "Choice '$path'"
            else -> path
        }
        val nodeExtents = presentation.nodes.map { extent(nodeLabel(it.path), if (it.path.length > 1) 11 else 10) }
        val summaries = presentation.frames.map { extent("Generated: [${it.step.generatedStrings.joinToString()}]", 11) }
        val counts = presentation.frames.map { extent("${it.step.generatedStrings.size} / ${1 shl it.step.targetLength.coerceIn(0, 20)} Solutions", 11) }
        val headers = extent("CURRENT CANDIDATE STATE", 9)
        val candidates = presentation.frames.map { extent(if (it.step.currentString.isEmpty()) "(root: \"\")" else "\"${it.step.currentString}\"", 18) }
        val levels = presentation.frames.map { extent("Level ${it.step.decisionLevel} / ${it.step.targetLength}", 10) }
        val heading = extent("STATE-SPACE TREE (BINARY DECISION TREE):", 10)
        val metrics = FundamentalsLayoutMetrics(
            headerHeight = headers.second, candidateHeight = candidates.maxOfOrNull { it.second } ?: 22f,
            levelHeight = levels.maxOfOrNull { it.second } ?: 12f, headingHeight = heading.second,
            candidateWidth = maxOf(headers.first, candidates.maxOfOrNull { it.first } ?: 0f), headingWidth = heading.first,
            nodeWidth = (nodeExtents.maxOfOrNull { it.first } ?: 50f) + 12f,
            nodeHeight = (nodeExtents.maxOfOrNull { it.second } ?: 12f) + 8f,
            summaryHeight = maxOf(summaries.maxOfOrNull { it.second } ?: 14f, counts.maxOfOrNull { it.second } ?: 14f),
            summaryWidth = summaries.maxOfOrNull { it.first } ?: 0f,
            countWidth = maxOf(counts.maxOfOrNull { it.first } ?: 0f, (levels.maxOfOrNull { it.first } ?: 0f) + 16f)
        )
        val layout = BacktrackingFundamentalsSceneLayout.calculate(presentation, maxWidth.value, maxHeight.value, metrics)
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "backtracking-fundamentals", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun rect(bounds: BacktrackingFundamentalsRect, fill: Color, border: Color) {
                    val pos = Offset(bounds.x.dp.toPx(), bounds.y.dp.toPx())
                    val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                fun label(text: String, bounds: BacktrackingFundamentalsRect, color: Color, fontSize: Int, centered: Boolean = false, bold: Boolean = true) {
                    val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = fontSize.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = false)
                    val x = bounds.x.dp.toPx() + if (centered) (bounds.width.dp.toPx() - result.size.width) / 2f else 0f
                    val y = bounds.y.dp.toPx() + (bounds.height.dp.toPx() - result.size.height) / 2f
                    drawText(result, topLeft = Offset(x, y), color = color)
                }
                rect(layout.candidateRegion, FundamentalRegion, FundamentalBorder)
                label("CURRENT CANDIDATE STATE", layout.candidateHeader, FundamentalSecondary, 9)
                label(if (frame.step.currentString.isEmpty()) "(root: \"\")" else "\"${frame.step.currentString}\"", layout.candidateValue, FundamentalOrange, 18)
                val levelColor = when (frame.step.action) {
                    BacktrackingFundamentalsAction.SOLUTION_FOUND -> FundamentalGreen
                    BacktrackingFundamentalsAction.BACKTRACK -> FundamentalRed
                    else -> FundamentalOrange
                }
                val levelFill = when (frame.step.action) {
                    BacktrackingFundamentalsAction.SOLUTION_FOUND -> Color(0xFF123820)
                    BacktrackingFundamentalsAction.BACKTRACK -> Color(0xFF381215)
                    else -> Color(0xFF2A1C12)
                }
                rect(layout.level, levelFill, levelColor)
                label("Level ${frame.step.decisionLevel} / ${frame.step.targetLength}", layout.level, Color.White, 10, true)
                rect(layout.treeRegion, FundamentalRegion, FundamentalBorder)
                label("STATE-SPACE TREE (BINARY DECISION TREE):", layout.treeHeading, FundamentalBlue, 10)
                presentation.connections.forEach { edge ->
                    val parent = layout.nodes.getValue(edge.parent)
                    val child = layout.nodes.getValue(edge.child)
                    val reverse = edge.child == frame.backtrackFrom && edge.parent == frame.backtrackTo
                    val active = edge.parent in frame.activePaths && edge.child in frame.activePaths
                    val color = if (reverse) FundamentalRed else if (active) FundamentalOrange else FundamentalSecondary
                    val from = Offset(parent.centerX.dp.toPx(), parent.bottom.dp.toPx())
                    val to = Offset(child.centerX.dp.toPx(), child.y.dp.toPx())
                    drawLine(color, from, to, 1.5.dp.toPx())
                    if (reverse) {
                        val delta = from - to
                        val length = sqrt(delta.x * delta.x + delta.y * delta.y).coerceAtLeast(1f)
                        val direction = delta / length
                        val tip = from - direction * 2.dp.toPx()
                        val base = tip - direction * 5.dp.toPx()
                        val normal = Offset(-direction.y, direction.x) * 3.dp.toPx()
                        drawLine(color, tip, base + normal, 1.5.dp.toPx())
                        drawLine(color, tip, base - normal, 1.5.dp.toPx())
                    }
                }
                presentation.nodes.forEach { node ->
                    val bounds = layout.nodes.getValue(node.path)
                    val selected = node.path in frame.solutionPaths
                    val active = node.path in frame.activePaths
                    val color = if (node.path == frame.backtrackFrom) FundamentalRed else if (selected) FundamentalGreen else if (active) FundamentalOrange else FundamentalBorder
                    val fill = if (selected) Color(0xFF123820) else if (active) Color(0xFF2A1C12) else Color(0xFF181824)
                    rect(bounds, fill, color)
                    val nodeText = when {
                        selected -> FundamentalGreen
                        node.path.length == 1 && active -> FundamentalOrange
                        node.path.length == 1 -> FundamentalSecondary
                        else -> Color.White
                    }
                    label(nodeLabel(node.path), bounds, nodeText,
                        if (node.path.length > 1) 11 else 10, true, node.path.length > 1)
                }
                rect(layout.summaryRegion, FundamentalRegion, FundamentalBorder)
                label("Generated: [${frame.step.generatedStrings.joinToString()}]", layout.summary, Color.White, 11)
                label("${frame.step.generatedStrings.size} / ${1 shl frame.step.targetLength.coerceIn(0, 20)} Solutions", layout.count, FundamentalGreen, 11, true)
            }
        }
    }
}
