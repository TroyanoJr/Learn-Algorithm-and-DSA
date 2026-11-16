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

private val QueueCanvasBg = Color(0xFF13131C)
private val QueueCanvasBorder = Color(0xFF262634)
private val QueueCanvasOrange = Color(0xFFFA6E13)
private val QueueCanvasBlue = Color(0xFF38BDF8)
private val QueueCanvasGreen = Color(0xFF10B981)
private val QueueCanvasRed = Color(0xFFEF4444)
private val QueueCanvasText = Color.White
private val QueueCanvasSecondary = Color(0xFF9CA3AF)

@Composable
internal fun QueueSceneRenderer(
    scene: VisualizationScene,
    step: QueueTraceStep,
    sequence: List<QueueTraceStep>,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val values = remember(sequence) { sequence.flatMap { it.values }.distinct() }
    val maxValueWidth = with(density) {
        maxOf(
            values.maxOfOrNull { value -> measurer.measure(value, TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)).size.width.toDp().value } ?: 28f,
            measurer.measure("FRONT/REAR", TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold)).size.width.toDp().value
        )
    }
    val frontLabelWidth = with(density) {
        measurer.measure("FRONT / REAR = null", TextStyle(fontSize = 10.sp)).size.width.toDp().value + 8f
    }
    val terminalLabelWidth = with(density) {
        measurer.measure("→ null · REAR", TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold)).size.width.toDp().value + 8f
    }
    val frontTextHeight = with(density) {
        measurer.measure("FRONT / REAR = null", TextStyle(fontSize = 10.sp)).size.height.toDp().value
    }
    val pointerTextHeight = with(density) {
        measurer.measure("FRONT/REAR", TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value
    }
    val valueTextHeight = with(density) {
        values.maxOfOrNull { measurer.measure(it, TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value } ?: 24f
    }
    val indexTextHeight = with(density) {
        measurer.measure("99", TextStyle(fontSize = 9.sp)).size.height.toDp().value
    }
    val linkedValueHeight = with(density) {
        values.maxOfOrNull { measurer.measure(it, TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value } ?: 14f
    }
    val linkedNextHeight = with(density) {
        measurer.measure("NEXT •", TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold)).size.height.toDp().value
    }
    val maxItems = sequence.maxOfOrNull { it.values.size } ?: step.size
    val showLinked = step.representation == QueueRepresentation.ARRAY_AND_LINKED
    val layoutArgs = QueueSceneLayoutArgs(
        capacity = step.capacity,
        maxItems = maxItems,
        showLinked = showLinked,
        maxValueWidthDp = maxValueWidth,
        frontLabelWidthDp = frontLabelWidth,
        frontTextHeightDp = frontTextHeight,
        terminalLabelWidthDp = terminalLabelWidth,
        pointerTextHeightDp = pointerTextHeight,
        valueTextHeightDp = valueTextHeight,
        indexTextHeightDp = indexTextHeight,
        linkedValueHeightDp = linkedValueHeight,
        linkedNextHeightDp = linkedNextHeight
    )
    val preliminaryLayout = QueueSceneLayout.calculate(layoutArgs)
    val headingMaxWidthPx = with(density) { (preliminaryLayout.requiredSceneWidth - 24f).dp.roundToPx().coerceAtLeast(1) }
    val linkedHeadingHeight = with(density) {
        sequence.maxOfOrNull { candidate ->
            val title = "LINKED NODES · FRONT = ${candidate.values.firstOrNull() ?: "null"} · REAR = ${candidate.values.lastOrNull() ?: "null"}"
            measurer.measure(
                title,
                TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                constraints = Constraints(maxWidth = headingMaxWidthPx)
            ).size.height.toDp().value
        } ?: 14f
    }
    val layout = QueueSceneLayout.calculate(layoutArgs.copy(linkedHeadingHeightDp = linkedHeadingHeight))
    val state = rememberVisualizerCanvasState(key = step.representation)

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
            fun arrow(from: Offset, to: Offset, color: Color = QueueCanvasBlue) {
                drawLine(color, from, to, 1.7.dp.toPx(), cap = StrokeCap.Round)
                val angle = atan2(to.y - from.y, to.x - from.x)
                val length = 7.dp.toPx()
                val spread = 0.48f
                val p1 = Offset(to.x - length * cos(angle - spread), to.y - length * sin(angle - spread))
                val p2 = Offset(to.x - length * cos(angle + spread), to.y - length * sin(angle + spread))
                drawPath(Path().apply { moveTo(to.x, to.y); lineTo(p1.x, p1.y); lineTo(p2.x, p2.y); close() }, color)
            }
            fun cell(bounds: Rect2D, fill: Color, border: Color, radius: Float = 8f) {
                drawRoundRect(fill, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()))
                drawRoundRect(border, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()), style = Stroke(1.2.dp.toPx()))
            }

            val arrayPane = scene.subScenes.firstOrNull { it.subPaneId == "queue_array" }
            val occupiedBySlot = arrayPane?.entities.orEmpty().associateBy { it.slotKey.substringAfterLast(':').toIntOrNull() }
            for (index in 0 until step.capacity) {
                val bounds = layout.arraySlotBounds.getValue(index)
                val value = step.slots.getOrNull(index)
                val isHighlighted = step.highlightedSlot == index
                val isOccupied = value != null
                cell(bounds,
                    when { isHighlighted -> Color(0xFF2A1C12); isOccupied -> Color(0xFF13233A); else -> QueueCanvasBg },
                    if (isHighlighted) QueueCanvasOrange else QueueCanvasBorder,
                    9f)
                val marker = when {
                    step.frontIndex == index && step.nextRearIndex == index -> "FRONT/REAR"
                    step.frontIndex == index -> "FRONT"
                    step.nextRearIndex == index -> "REAR"
                    else -> ""
                }
                if (marker.isNotEmpty()) drawTextIn(marker, layout.frontRearLabelBounds.getValue(index), if (step.frontIndex == index) QueueCanvasOrange else QueueCanvasBlue, 8, true)
                drawTextIn(value ?: "·", layout.arrayValueBounds.getValue(index), if (isHighlighted) QueueCanvasOrange else QueueCanvasText, 16, true)
                drawTextIn(index.toString(), layout.arrayIndexBounds.getValue(index), QueueCanvasSecondary, 9)
                val item = occupiedBySlot[index]
                if (item != null && item.dataValue != value) error("Queue scene item does not match slot $index")
            }

            if (showLinked) {
                val logical = scene.subScenes.firstOrNull { it.subPaneId == "queue_linked" }
                val entities = logical?.entities.orEmpty().sortedBy { it.slotKey.substringAfterLast(':').toIntOrNull() ?: 0 }
                drawTextIn("LINKED NODES · FRONT = ${step.values.firstOrNull() ?: "null"} · REAR = ${step.values.lastOrNull() ?: "null"}", layout.linkedHeadingBounds, QueueCanvasBlue, 9, true)
                if (entities.isEmpty()) {
                    drawTextIn("FRONT / REAR = null", layout.linkedFrontBounds, QueueCanvasSecondary, 10)
                } else {
                    drawTextIn("FRONT →", layout.linkedFrontBounds, QueueCanvasOrange, 9, true)
                    entities.forEachIndexed { index, entity ->
                        val bounds = layout.linkedNodeBounds[entity.slotKey] ?: return@forEachIndexed
                        val active = entity.visualState == EntityVisualState.ACTIVE
                        cell(bounds, if (active) Color(0xFF2A1C12) else Color(0xFF13233A), if (active) QueueCanvasOrange else QueueCanvasBorder, 7f)
                        drawTextIn(entity.dataValue, Rect2D(bounds.left + 2f, bounds.top + 2f, bounds.width - 4f, linkedValueHeight + 2f), QueueCanvasText, 9, true)
                        drawTextIn("NEXT •", Rect2D(bounds.left + 2f, bounds.top + linkedValueHeight + 4f, bounds.width - 4f, linkedNextHeight + 2f), QueueCanvasBlue, 7, true)
                        val nextEntity = entities.getOrNull(index + 1)
                        val next = nextEntity?.let { layout.linkedNodeBounds[it.slotKey] }
                        val endpoint = next?.left?.dp?.toPx() ?: layout.linkedNullBounds.left.dp.toPx()
                        val connection = logical?.connections?.firstOrNull { it.fromVisualKey == entity.visualKey && it.toVisualKey == nextEntity?.visualKey }
                        val color = if (connection?.visualState == ConnectionVisualState.HIGHLIGHTED) QueueCanvasOrange else QueueCanvasBlue
                        arrow(Offset(bounds.right.dp.toPx(), bounds.center.y.dp.toPx()), Offset(endpoint, bounds.center.y.dp.toPx()), color)
                    }
                    drawTextIn("→ null · REAR", layout.linkedNullBounds, QueueCanvasBlue, 8, true)
                }
            }
        }
    }
}
