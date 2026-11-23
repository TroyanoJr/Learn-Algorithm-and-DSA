package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SceneBackground = Color(0xFF13131C)
private val SceneBorder = Color(0xFF30303E)
private val DivideAccent = Color(0xFFFA6E13)
private val ConquerAccent = Color(0xFF38BDF8)
private val CombineAccent = Color(0xFF10B981)
private val ResultTextAccent = Color(0xFF34D399)
private val LabelText = Color(0xFFB8C0CC)
private val ScenePrimary = Color.White
private val SceneSecondary = Color(0xFF9CA3AF)

@Composable
internal fun DivideConquerSceneRenderer(
    frame: DivideConquerSceneFrame,
    sequence: List<DivideConquerSceneFrame>,
    modifier: Modifier = Modifier
) {
    val layout = remember(sequence) { DivideConquerSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = "divide_conquer_fundamentals")
    val textMeasurer = rememberTextMeasurer()

    VisualizerCanvasViewport(
        modifier = modifier.fillMaxSize(),
        mode = VisualizerCanvasMode.HYBRID,
        state = viewport
    ) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun drawTextInBounds(
                value: String,
                bounds: Rect2D,
                color: Color,
                sizeSp: Float,
                bold: Boolean = false,
                minSizeSp: Float = 7f
            ) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val constraints = Constraints(
                    maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                    maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                )
                var currentSize = sizeSp
                var measured = textMeasurer.measure(
                    value,
                    TextStyle(color = color, fontSize = currentSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    maxLines = 1,
                    softWrap = false,
                    constraints = constraints
                )
                while ((measured.didOverflowWidth || measured.didOverflowHeight) && currentSize > minSizeSp) {
                    currentSize = (currentSize - 0.5f).coerceAtLeast(minSizeSp)
                    measured = textMeasurer.measure(
                        value,
                        TextStyle(color = color, fontSize = currentSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                        maxLines = 1,
                        softWrap = false,
                        constraints = constraints
                    )
                }
                val left = bounds.left.dp.toPx()
                val top = bounds.top.dp.toPx()
                val width = bounds.width.dp.toPx()
                val height = bounds.height.dp.toPx()
                drawText(
                    measured,
                    topLeft = Offset(
                        left + (width - measured.size.width).coerceAtLeast(0f) / 2f,
                        top + (height - measured.size.height).coerceAtLeast(0f) / 2f
                    )
                )
            }

            fun drawCard(bounds: Rect2D, fill: Color, border: Color, radius: Float = 8f, strokeWidth: Float = 1.5f) {
                drawRoundRect(
                    color = fill,
                    topLeft = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    cornerRadius = CornerRadius(radius.dp.toPx())
                )
                drawRoundRect(
                    color = border,
                    topLeft = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    cornerRadius = CornerRadius(radius.dp.toPx()),
                    style = Stroke(width = strokeWidth.dp.toPx())
                )
            }

            val phaseColors = mapOf(
                DivideConquerPhase.DIVIDE to DivideAccent,
                DivideConquerPhase.CONQUER to ConquerAccent,
                DivideConquerPhase.COMBINE to CombineAccent
            )

            drawCard(layout.phaseHeaderBounds, SceneBackground, SceneBorder, radius = 10f)
            drawTextInBounds("THE 3 PHASES OF DIVIDE & CONQUER", layout.phaseTitleBounds, DivideAccent, 10f, bold = true)
            drawTextInBounds(
                DivideConquerSceneLayout.TREE_TITLE_TEXT,
                layout.treeTitleBounds,
                ScenePrimary,
                10f,
                bold = true
            )
            DivideConquerPhase.entries.forEachIndexed { index, phase ->
                val bounds = layout.phaseBadgeBounds.getValue(phase)
                val color = phaseColors.getValue(phase)
                val active = phase == frame.meta.phase
                drawCard(
                    bounds,
                    fill = if (active) color.copy(alpha = 0.16f) else Color(0xFF1A1A26),
                    border = if (active) color else SceneBorder,
                    radius = 5f
                )
                drawTextInBounds("${index + 1}. ${phase.name}", bounds, if (active) color else SceneSecondary, 9f, bold = true)
            }

            val subScene = frame.scene.subScenes.firstOrNull { it.subPaneId == "divide_conquer_tree" }
                ?: error("DivideConquerSceneRenderer requires divide_conquer_tree")
            subScene.connections.forEach { connection ->
                val line = layout.connectionLines[connection.connectionKey]
                    ?: error("Missing geometry for connection ${connection.connectionKey}")
                val highlighted = connection.visualState == ConnectionVisualState.HIGHLIGHTED
                val lineColor = if (highlighted) CombineAccent else ConquerAccent.copy(alpha = 0.78f)
                val labelColor = if (highlighted) CombineAccent else ConquerAccent
                drawLine(
                    color = lineColor,
                    start = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx()),
                    end = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx()),
                    strokeWidth = if (highlighted) 3.dp.toPx() else 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                connection.label?.let { label ->
                    val labelBounds = layout.connectionLabelBounds[connection.connectionKey]
                        ?: error("Missing label geometry for connection ${connection.connectionKey}")
                    drawCard(labelBounds, SceneBackground, labelColor, radius = 4f, strokeWidth = 1f)
                    drawTextInBounds(label, labelBounds, labelColor, 8f, bold = true)
                }
            }

            subScene.entities.forEach { entity ->
                val id = entity.elementId.removePrefix("node:")
                val bounds = layout.nodeBounds[id] ?: error("Missing layout bounds for $id")
                val presentation = frame.meta.nodePresentations[id] ?: error("Missing presentation data for $id")
                val (fill, border) = when {
                    entity.visualState == EntityVisualState.ACTIVE -> Color(0xFF2A1C12) to DivideAccent
                    entity.visualState == EntityVisualState.TARGET -> Color(0xFF123820) to CombineAccent
                    entity.visualState == EntityVisualState.VISITED -> Color(0xFF123820) to CombineAccent
                    presentation.isBaseCase && presentation.maxValue == null -> Color(0xFF102638) to ConquerAccent
                    else -> Color(0xFF181824) to SceneBorder
                }
                drawCard(bounds, fill, border, radius = 8f, strokeWidth = if (entity.visualState == EntityVisualState.ACTIVE || entity.visualState == EntityVisualState.TARGET) 2f else 1.25f)
                val left = bounds.left + 10f
                val contentWidth = bounds.width - 20f
                drawTextInBounds(presentation.valuesLabel, Rect2D(left, bounds.top + 4f, contentWidth, 19f), ScenePrimary, 14f, bold = true)
                drawTextInBounds(presentation.rangeLabel, Rect2D(left, bounds.top + 24f, contentWidth, 14f), LabelText, 9f)
                drawTextInBounds(
                    presentation.roleLabel,
                    Rect2D(left, bounds.top + 40f, contentWidth, 12f),
                    if (presentation.isBaseCase) ConquerAccent else LabelText,
                    8f,
                    bold = true
                )
                presentation.maxLabel?.let { maxLabel ->
                    drawTextInBounds(maxLabel, Rect2D(left, bounds.top + 54f, contentWidth, 15f), ResultTextAccent, 11f, bold = true)
                }
            }

            if (frame.meta.combinedValue != null && frame.meta.leftValue != null && frame.meta.rightValue != null) {
                drawCard(layout.combineBounds, Color(0xFF10251D), CombineAccent, radius = 8f, strokeWidth = 1.5f)
                drawTextInBounds("COMBINE RESULT", Rect2D(layout.combineBounds.left + 12f, layout.combineBounds.top + 7f, layout.combineBounds.width - 24f, 15f), CombineAccent, 9f, bold = true)
                drawTextInBounds(
                    "max(${frame.meta.leftValue}, ${frame.meta.rightValue}) = ${frame.meta.combinedValue}",
                    Rect2D(layout.combineBounds.left + 12f, layout.combineBounds.top + 26f, layout.combineBounds.width - 24f, 22f),
                    ScenePrimary,
                    13f,
                    bold = true
                )
            }
        }
    }
}
