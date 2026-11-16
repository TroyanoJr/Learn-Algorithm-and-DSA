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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val StackCanvasBg = Color(0xFF13131C)
private val StackCanvasBorder = Color(0xFF262634)
private val StackCanvasOrange = Color(0xFFFA6E13)
private val StackCanvasBlue = Color(0xFF38BDF8)
private val StackCanvasGreen = Color(0xFF10B981)
private val StackCanvasRed = Color(0xFFEF4444)
private val StackCanvasText = Color.White
private val StackCanvasSecondary = Color(0xFF9CA3AF)

@Composable
internal fun StackSceneRenderer(
    scene: VisualizationScene,
    step: StackTraceStep,
    sequence: List<StackTraceStep>,
    selectedView: String = "Array",
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val values = remember(sequence) { sequence.flatMap { it.values }.distinct() }
    val maxValueWidth = with(density) {
        values.maxOfOrNull { value ->
            measurer.measure(value, TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)).size.width.toDp().value
        } ?: 40f
    }
    val maxValueHeight = with(density) {
        values.maxOfOrNull { value -> measurer.measure(value, TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value } ?: 16f
    }
    val titleWidth = with(density) {
        maxOf(
            measurer.measure("ARRAY - TOP = LAST INDEX", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)).size.width.toDp().value,
            measurer.measure("LINKED - TOP = HEAD", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)).size.width.toDp().value
        )
    }
    val headTextWidth = with(density) {
        measurer.measure("HEAD = null", TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)).size.width.toDp().value + 8f
    }
    val headerHeight = with(density) {
        maxOf(
            measurer.measure("ARRAY - TOP = LAST INDEX", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value,
            measurer.measure("LINKED - TOP = HEAD", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value
        ) + 2f
    }
    val headTextHeight = with(density) {
        measurer.measure("HEAD = null", TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value
    }
    val indexLabelWidth = with(density) {
        (0 until maxOf(1, sequence.maxOfOrNull { it.values.size } ?: 0)).maxOfOrNull { index ->
            measurer.measure(index.toString(), TextStyle(fontSize = 11.sp)).size.width.toDp().value
        } ?: 24f
    }
    val nullLabelSize = with(density) {
        measurer.measure("→ null", TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)).size.let { size ->
            size.width.toDp().value + 8f to size.height.toDp().value
        }
    }
    val maxDepth = sequence.maxOfOrNull { it.values.size } ?: step.values.size
    val linked = step.representation == StackRepresentation.ARRAY_AND_LINKED && selectedView == "Linked list"
    val includeEmpty = !linked && sequence.any { it.values.isEmpty() }
    val layout = StackSceneLayout.calculate(
        maxDepth, maxValueWidth, maxValueHeight, linked, includeEmpty, titleWidth, headerHeight,
        headTextWidth, headTextHeight, indexLabelWidth, nullLabelSize.first, nullLabelSize.second
    )
    val state = rememberVisualizerCanvasState(key = selectedView)

    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, state) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun drawTextIn(text: String, bounds: Rect2D, color: Color, fontSize: Int, bold: Boolean = false) {
                val result = measurer.measure(
                    text,
                    TextStyle(color = color, fontSize = fontSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    constraints = Constraints(maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1))
                )
                drawText(textLayoutResult = result, topLeft = Offset(
                    bounds.left.dp.toPx() + (bounds.width.dp.toPx() - result.size.width) / 2f,
                    bounds.top.dp.toPx() + (bounds.height.dp.toPx() - result.size.height) / 2f
                ))
            }
            fun drawArrow(from: Offset, to: Offset, color: Color = StackCanvasBlue) {
                drawLine(color, from, to, 1.7.dp.toPx(), cap = StrokeCap.Round)
                val angle = atan2(to.y - from.y, to.x - from.x)
                val length = 7.dp.toPx()
                val spread = 0.48f
                val p1 = Offset(to.x - length * cos(angle - spread), to.y - length * sin(angle - spread))
                val p2 = Offset(to.x - length * cos(angle + spread), to.y - length * sin(angle + spread))
                drawPath(Path().apply { moveTo(to.x, to.y); lineTo(p1.x, p1.y); lineTo(p2.x, p2.y); close() }, color)
            }
            fun cell(bounds: Rect2D, state: EntityVisualState, radius: Float = 7f) {
                val fill = when (state) {
                    EntityVisualState.ACTIVE -> Color(0xFF2A1C12)
                    EntityVisualState.VISITED -> Color(0xFF161F1A)
                    EntityVisualState.VIOLATION -> Color(0xFF381212)
                    EntityVisualState.TARGET -> Color(0xFF123820)
                    EntityVisualState.DEFAULT -> StackCanvasBg
                }
                val border = when (state) {
                    EntityVisualState.ACTIVE -> StackCanvasOrange
                    EntityVisualState.VISITED -> StackCanvasGreen
                    EntityVisualState.VIOLATION -> StackCanvasRed
                    EntityVisualState.TARGET -> StackCanvasBlue
                    EntityVisualState.DEFAULT -> StackCanvasBorder
                }
                drawRoundRect(fill, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()))
                drawRoundRect(border, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()), style = Stroke(1.3.dp.toPx()))
            }

            val pane = scene.subScenes.firstOrNull { it.subPaneId == if (linked) "stack_linked" else "stack_array" }
            val entities = pane?.entities.orEmpty()
            val bySlot = entities.associateBy { it.slotKey }
            if (linked) {
                drawTextIn("LINKED - TOP = HEAD", layout.topLabelBounds.copy(width = layout.requiredSceneWidth - 24f), StackCanvasBlue, 12, true)
                val head = layout.headLabelBounds
                drawRoundRect(Color(0xFF2A1C12), Offset(head.left.dp.toPx(), head.top.dp.toPx()), Size(head.width.dp.toPx(), head.height.dp.toPx()), CornerRadius(6.dp.toPx()))
                drawRoundRect(StackCanvasOrange, Offset(head.left.dp.toPx(), head.top.dp.toPx()), Size(head.width.dp.toPx(), head.height.dp.toPx()), CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                drawTextIn(if (step.values.isEmpty()) "HEAD = null" else "HEAD →", head, StackCanvasOrange, 11, true)
                val ordered = entities.sortedBy { it.slotKey.substringAfterLast(':').toIntOrNull() ?: 0 }
                ordered.firstOrNull()?.let { first ->
                    val firstBounds = layout.linkedNodeBounds[first.slotKey]
                    if (firstBounds != null) drawArrow(Offset(head.right.dp.toPx(), head.center.y.dp.toPx()), Offset(firstBounds.left.dp.toPx(), firstBounds.center.y.dp.toPx()), StackCanvasOrange)
                }
                ordered.forEach { entity ->
                    val bounds = layout.linkedNodeBounds[entity.slotKey] ?: return@forEach
                    cell(bounds, entity.visualState, 6f)
                    drawTextIn(entity.dataValue, Rect2D(bounds.left + 2f, bounds.top + 2f, bounds.width - 17f, bounds.height - 4f), StackCanvasText, 12, true)
                    drawLine(StackCanvasBorder, Offset((bounds.right - 15f).dp.toPx(), bounds.top.dp.toPx()), Offset((bounds.right - 15f).dp.toPx(), bounds.bottom.dp.toPx()), 1.dp.toPx())
                    drawTextIn("•", Rect2D(bounds.right - 15f, bounds.top + 2f, 15f, bounds.height - 4f), StackCanvasBlue, 13, true)
                }
                val entityByKey = entities.associateBy { it.visualKey }
                pane?.connections.orEmpty().forEach { connection ->
                    val from = entityByKey[connection.fromVisualKey]?.let { layout.linkedNodeBounds[it.slotKey] }
                    val to = entityByKey[connection.toVisualKey]?.let { layout.linkedNodeBounds[it.slotKey] }
                    if (from != null && to != null) {
                        val color = if (connection.visualState == ConnectionVisualState.HIGHLIGHTED) StackCanvasOrange else StackCanvasBlue
                        drawArrow(Offset(from.right.dp.toPx(), from.center.y.dp.toPx()), Offset(to.left.dp.toPx(), to.center.y.dp.toPx()), color)
                    }
                }
                val last = ordered.lastOrNull()?.let { layout.linkedNodeBounds[it.slotKey] }
                if (last != null) {
                    drawArrow(Offset(last.right.dp.toPx(), last.center.y.dp.toPx()), Offset(layout.nullBounds.left.dp.toPx(), layout.nullBounds.center.y.dp.toPx()))
                }
                val nullRect = layout.nullBounds
                drawRoundRect(StackCanvasBg, Offset(nullRect.left.dp.toPx(), nullRect.top.dp.toPx()), Size(nullRect.width.dp.toPx(), nullRect.height.dp.toPx()), CornerRadius(5.dp.toPx()))
                drawRoundRect(StackCanvasBorder, Offset(nullRect.left.dp.toPx(), nullRect.top.dp.toPx()), Size(nullRect.width.dp.toPx(), nullRect.height.dp.toPx()), CornerRadius(5.dp.toPx()), style = Stroke(1.dp.toPx()))
                drawTextIn("→ null", nullRect, StackCanvasSecondary, 10, true)
            } else {
                val title = if (step.representation == StackRepresentation.ARRAY_AND_LINKED) "ARRAY - TOP = LAST INDEX" else "TOP ↓"
                drawTextIn(title, layout.topLabelBounds.copy(width = layout.requiredSceneWidth - 24f), StackCanvasOrange, 12, true)
                if (step.values.isEmpty()) {
                    val empty = layout.emptyStateBounds
                    cell(empty, EntityVisualState.DEFAULT)
                    drawTextIn("EMPTY", empty, StackCanvasSecondary, 12, true)
                }
                entities.forEach { entity ->
                    val bounds = layout.arrayBounds[entity.slotKey] ?: return@forEach
                    cell(bounds, entity.visualState)
                    drawTextIn(entity.dataValue, bounds, if (entity.visualState == EntityVisualState.ACTIVE) StackCanvasOrange else StackCanvasText, 13, true)
                    if (step.representation == StackRepresentation.ARRAY_AND_LINKED) {
                        val indexBounds = layout.arrayIndexBounds[entity.slotKey] ?: return@forEach
                        drawTextIn(entity.slotKey.substringAfterLast(':') , indexBounds, StackCanvasSecondary, 11)
                        if (entity.slotKey.endsWith(":${step.values.lastIndex}")) {
                            drawTextIn("TOP", Rect2D(bounds.right + 6f, bounds.top, 42f, bounds.height), StackCanvasOrange, 11, true)
                        }
                    }
                }
                drawTextIn("BOTTOM", layout.bottomLabelBounds.copy(width = layout.requiredSceneWidth - 24f), StackCanvasSecondary, 11, true)
            }
        }
    }
}
