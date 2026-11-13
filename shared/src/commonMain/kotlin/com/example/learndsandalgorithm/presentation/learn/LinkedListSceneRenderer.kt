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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

/**
 * Canvas-based renderer for Linked List [VisualizationScene] produced by [LinkedListSceneBuilder].
 */
@Composable
internal fun LinkedListSceneRenderer(
    scene: VisualizationScene,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value

        val subScene = scene.subScenes.firstOrNull { it.subPaneId == "linked_list_pane" }
            ?: scene.subScenes.firstOrNull()

        val entities = subScene?.entities ?: emptyList()
        val connections = subScene?.connections ?: emptyList()

        val layout = LinkedListSceneLayout.calculateLayout(
            subScene = subScene,
            availableWidthDp = widthDp,
            availableHeightDp = heightDp
        )

        val canvasWidthDp = maxOf(widthDp, layout.requiredSceneWidth)
        val canvasHeightDp = maxOf(heightDp, layout.requiredSceneHeight)

        VisualizerCanvasViewport(
            modifier = modifier.fillMaxSize(),
            mode = VisualizerCanvasMode.HYBRID
        ) {
            Canvas(
                modifier = Modifier.size(canvasWidthDp.dp, canvasHeightDp.dp)
            ) {
                // ==========================================
                // 1. DRAW CONNECTIONS & ARROWHEADS
                // ==========================================
                connections.forEach { conn ->
                    val line = layout.connectionLines[conn.connectionKey]
                    if (line != null) {
                        val startPx = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx())
                        val endPx = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx())
                        val isHigh = conn.visualState == ConnectionVisualState.HIGHLIGHTED
                        val strokeW = if (isHigh) 2.5.dp.toPx() else 1.5.dp.toPx()
                        val color = if (isHigh) OrangeAccent else BlueAccent.copy(alpha = 0.7f)

                        drawLine(
                            color = color,
                            start = startPx,
                            end = endPx,
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )

                        // Arrowhead
                        val angle = atan2(endPx.y - startPx.y, endPx.x - startPx.x)
                        val arrowLen = 8.dp.toPx()
                        val arrowAngle = 0.523599f
                        val p1 = Offset(
                            x = endPx.x - arrowLen * cos(angle - arrowAngle),
                            y = endPx.y - arrowLen * sin(angle - arrowAngle)
                        )
                        val p2 = Offset(
                            x = endPx.x - arrowLen * cos(angle + arrowAngle),
                            y = endPx.y - arrowLen * sin(angle + arrowAngle)
                        )
                        val arrowPath = Path().apply {
                            moveTo(endPx.x, endPx.y)
                            lineTo(p1.x, p1.y)
                            lineTo(p2.x, p2.y)
                            close()
                        }
                        drawPath(path = arrowPath, color = color)
                    }
                }

                // ==========================================
                // 2. DRAW HEAD POINTER & NULL POINTER & NODES
                // ==========================================
                entities.forEach { entity ->
                    val slotKey = entity.slotKey
                    val rect = layout.nodeBounds[slotKey]

                    if (rect != null) {
                        val leftPx = rect.left.dp.toPx()
                        val topPx = rect.top.dp.toPx()
                        val widthPx = rect.width.dp.toPx()
                        val heightPx = rect.height.dp.toPx()
                        val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())

                        when {
                            entity.elementId == "head_ptr" -> {
                                // HEAD Box
                                drawRoundRect(
                                    color = Color(0xFF2A1C12),
                                    topLeft = Offset(leftPx, topPx),
                                    size = Size(widthPx, heightPx),
                                    cornerRadius = cornerRadius
                                )
                                drawRoundRect(
                                    color = OrangeAccent,
                                    topLeft = Offset(leftPx, topPx),
                                    size = Size(widthPx, heightPx),
                                    cornerRadius = cornerRadius,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                val headText = textMeasurer.measure(
                                    text = "HEAD",
                                    style = TextStyle(color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                )
                                drawText(
                                    textLayoutResult = headText,
                                    topLeft = Offset(
                                        x = leftPx + (widthPx - headText.size.width) / 2f,
                                        y = topPx + (heightPx - headText.size.height) / 2f
                                    )
                                )
                            }
                            entity.elementId == "null_ptr" -> {
                                // NULL Box
                                val isHigh = entity.visualState == EntityVisualState.VISITED
                                drawRoundRect(
                                    color = if (isHigh) Color(0xFF092317) else DarkCardBgColor,
                                    topLeft = Offset(leftPx, topPx),
                                    size = Size(widthPx, heightPx),
                                    cornerRadius = cornerRadius
                                )
                                drawRoundRect(
                                    color = if (isHigh) GreenAccent else CardBorderColor,
                                    topLeft = Offset(leftPx, topPx),
                                    size = Size(widthPx, heightPx),
                                    cornerRadius = cornerRadius,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                val nullText = textMeasurer.measure(
                                    text = "null",
                                    style = TextStyle(color = if (isHigh) GreenAccent else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                )
                                drawText(
                                    textLayoutResult = nullText,
                                    topLeft = Offset(
                                        x = leftPx + (widthPx - nullText.size.width) / 2f,
                                        y = topPx + (heightPx - nullText.size.height) / 2f
                                    )
                                )
                            }
                            else -> {
                                // LinkedList Node (DATA + NEXT fields)
                                val isActive = entity.visualState == EntityVisualState.ACTIVE
                                val isVisited = entity.visualState == EntityVisualState.VISITED
                                val nodeBg = when {
                                    isActive -> Color(0xFF2A1C12)
                                    isVisited -> Color(0xFF161F1A)
                                    else -> DarkCardBgColor
                                }
                                val nodeBorder = when {
                                    isActive -> OrangeAccent
                                    isVisited -> GreenAccent
                                    else -> CardBorderColor
                                }

                                drawRoundRect(
                                    color = nodeBg,
                                    topLeft = Offset(leftPx, topPx),
                                    size = Size(widthPx, heightPx),
                                    cornerRadius = cornerRadius
                                )
                                drawRoundRect(
                                    color = nodeBorder,
                                    topLeft = Offset(leftPx, topPx),
                                    size = Size(widthPx, heightPx),
                                    cornerRadius = cornerRadius,
                                    style = Stroke(width = if (isActive) 1.5.dp.toPx() else 1.dp.toPx())
                                )

                                // Vertical separator between DATA and NEXT
                                val dataRect = layout.dataFieldBounds[slotKey]
                                val nextRect = layout.nextFieldBounds[slotKey]
                                if (dataRect != null && nextRect != null) {
                                    val dividerX = nextRect.left.dp.toPx()
                                    drawLine(
                                        color = CardBorderColor,
                                        start = Offset(dividerX, topPx),
                                        end = Offset(dividerX, topPx + heightPx),
                                        strokeWidth = 1.dp.toPx()
                                    )

                                    // DATA Field Text
                                    val dataLabel = textMeasurer.measure(
                                        text = "DATA",
                                        style = TextStyle(color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    )
                                    val dataVal = textMeasurer.measure(
                                        text = entity.dataValue,
                                        style = TextStyle(color = if (isActive || isVisited) TextPrimary else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    )
                                    val dataCenterX = dataRect.left.dp.toPx() + (dataRect.width.dp.toPx()) / 2f
                                    drawText(
                                        textLayoutResult = dataLabel,
                                        topLeft = Offset(dataCenterX - dataLabel.size.width / 2f, topPx + 3.dp.toPx())
                                    )
                                    drawText(
                                        textLayoutResult = dataVal,
                                        topLeft = Offset(dataCenterX - dataVal.size.width / 2f, topPx + 15.dp.toPx())
                                    )

                                    // NEXT Field Text
                                    val nextLabel = textMeasurer.measure(
                                        text = "NEXT",
                                        style = TextStyle(color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    )
                                    val nextText = textMeasurer.measure(
                                        text = "•",
                                        style = TextStyle(color = if (isActive) OrangeAccent else TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    )
                                    val nextCenterX = nextRect.left.dp.toPx() + (nextRect.width.dp.toPx()) / 2f
                                    drawText(
                                        textLayoutResult = nextLabel,
                                        topLeft = Offset(nextCenterX - nextLabel.size.width / 2f, topPx + 3.dp.toPx())
                                    )
                                    drawText(
                                        textLayoutResult = nextText,
                                        topLeft = Offset(nextCenterX - nextText.size.width / 2f, topPx + 14.dp.toPx())
                                    )
                                }

                                // Badge label above node
                                if (!entity.badgeLabel.isNullOrBlank()) {
                                    val badgeRect = layout.badgeBounds[slotKey]
                                    if (badgeRect != null) {
                                        val badgeLayout = textMeasurer.measure(
                                            text = entity.badgeLabel,
                                            style = TextStyle(
                                                color = if (entity.badgeLabel == "REMOVED") RedAccent else OrangeAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        drawText(
                                            textLayoutResult = badgeLayout,
                                            topLeft = Offset(
                                                x = leftPx + (widthPx - badgeLayout.size.width) / 2f,
                                                y = badgeRect.top.dp.toPx()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
