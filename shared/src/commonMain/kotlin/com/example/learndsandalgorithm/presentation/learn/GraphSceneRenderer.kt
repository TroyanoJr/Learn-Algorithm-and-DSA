package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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

private val GBranchDefaultColor = Color(0xFF38BDF8)
private val GBranchHighlightColor = Color(0xFFFA6E13)
private val GBranchPathColor = Color(0xFF10B981)
private val GCardBorderColor = Color(0xFF262634)
private val GOrangeAccent = Color(0xFFFA6E13)
private val GBlueAccent = Color(0xFF38BDF8)
private val GGreenAccent = Color(0xFF10B981)
private val GRedAccent = Color(0xFFEF4444)
private val GTextPrimary = Color(0xFFFFFFFF)

/**
 * Reusable Compose Multiplatform Canvas renderer for general graph [VisualizationScene].
 * Draws vertices, edges, arrowheads for directed edges, edge labels, badges, and visual states
 * inside a single Canvas using [GraphSceneAdapter] and [GraphSceneLayout].
 */
@Composable
internal fun GraphSceneRenderer(
    scene: VisualizationScene,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier) {
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value

        val graphSubScene = remember(scene) {
            val graphSubScenes = scene.subScenes.filter { it.type == StructureType.GRAPH }
            graphSubScenes.firstOrNull { it.subPaneId == "graph_main_pane" } ?: graphSubScenes.firstOrNull()
        }

        val adapterResult = remember(graphSubScene) {
            if (graphSubScene != null) {
                GraphSceneAdapter.adapt(graphSubScene)
            } else {
                null
            }
        }

        val layout = remember(adapterResult, widthDp, heightDp) {
            if (adapterResult != null) {
                GraphSceneLayout.calculateLayout(
                    vertices = adapterResult.vertices,
                    edges = adapterResult.edges,
                    availableWidthDp = widthDp,
                    availableHeightDp = heightDp
                )
            } else {
                GraphLayoutResult(emptyMap(), emptyMap(), widthDp, heightDp)
            }
        }

        val canvasWidthDp = maxOf(widthDp, layout.requiredWidth)
        val canvasHeightDp = maxOf(heightDp, layout.requiredHeight)

        VisualizerCanvasViewport(
            modifier = Modifier.fillMaxSize(),
            mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = scene.sceneId)
        ) {
            Canvas(
                modifier = Modifier.size(canvasWidthDp.dp, canvasHeightDp.dp)
            ) {
                val vertexBounds = layout.vertexBounds
                val edgeLines = layout.edgeLines
                val edgeMap = adapterResult?.edgeConnectionMap ?: emptyMap()
                val vertexMap = adapterResult?.vertexEntityMap ?: emptyMap()

                // ==========================================
                // A. DRAW EDGES AND ARROWHEADS
                // ==========================================
                edgeLines.forEach { (edgeKey, line) ->
                    val conn = edgeMap[edgeKey]
                    val isHigh = conn?.visualState == ConnectionVisualState.HIGHLIGHTED
                    val isPath = conn?.visualState == ConnectionVisualState.PATH
                    val strokeW = if (isHigh || isPath) 3.dp.toPx() else 1.5.dp.toPx()
                    val color = when {
                        isHigh -> GBranchHighlightColor
                        isPath -> GBranchPathColor
                        else -> GBranchDefaultColor.copy(alpha = 0.6f)
                    }

                    val startPx = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx())
                    val endPx = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx())

                    drawLine(
                        color = color,
                        start = startPx,
                        end = endPx,
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )

                    // Draw arrowhead if directed
                    if (conn?.directed == true) {
                        val angle = atan2(endPx.y - startPx.y, endPx.x - startPx.x)
                        val arrowLength = 10.dp.toPx()
                        val arrowAngle = 0.523599f // 30 degrees in radians

                        val point1 = Offset(
                            x = endPx.x - arrowLength * cos(angle - arrowAngle),
                            y = endPx.y - arrowLength * sin(angle - arrowAngle)
                        )
                        val point2 = Offset(
                            x = endPx.x - arrowLength * cos(angle + arrowAngle),
                            y = endPx.y - arrowLength * sin(angle + arrowAngle)
                        )

                        val arrowPath = Path().apply {
                            moveTo(endPx.x, endPx.y)
                            lineTo(point1.x, point1.y)
                            lineTo(point2.x, point2.y)
                            close()
                        }
                        drawPath(path = arrowPath, color = color)
                    }

                    // Optional edge label
                    if (!conn?.label.isNullOrBlank()) {
                        val midPx = Offset((startPx.x + endPx.x) / 2f, (startPx.y + endPx.y) / 2f)
                        val labelLayout = textMeasurer.measure(
                            text = conn.label!!,
                            style = TextStyle(
                                color = GBlueAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = labelLayout,
                            topLeft = Offset(midPx.x - labelLayout.size.width / 2f, midPx.y - labelLayout.size.height / 2f)
                        )
                    }
                }

                // ==========================================
                // B. DRAW VERTICES, LABELS, AND BADGES
                // ==========================================
                vertexBounds.forEach { (vertexKey, rect) ->
                    val entity = vertexMap[vertexKey]
                    if (entity != null && entity.presenceState != EntityPresenceState.DISAPPEARING) {
                        val centerPx = Offset(rect.center.x.dp.toPx(), rect.center.y.dp.toPx())
                        val radiusPx = (rect.width.dp.toPx()) / 2f

                        val isActive = entity.visualState == EntityVisualState.ACTIVE
                        val isVisited = entity.visualState == EntityVisualState.VISITED
                        val isViolation = entity.visualState == EntityVisualState.VIOLATION
                        val isTarget = entity.visualState == EntityVisualState.TARGET

                        val bgColor = when {
                            isViolation -> Color(0xFF381212)
                            isActive || isTarget -> Color(0xFF2A1C12)
                            isVisited -> Color(0xFF123820)
                            else -> Color(0xFF13233A)
                        }

                        val borderColor = when {
                            isViolation -> GRedAccent
                            isActive || isTarget -> GOrangeAccent
                            isVisited -> GGreenAccent
                            else -> GCardBorderColor
                        }

                        // Vertex Background Circle
                        drawCircle(color = bgColor, center = centerPx, radius = radiusPx)

                        // Vertex Border Stroke
                        drawCircle(
                            color = borderColor,
                            center = centerPx,
                            radius = radiusPx,
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Vertex Label Text
                        val textLayout = textMeasurer.measure(
                            text = entity.dataValue,
                            style = TextStyle(
                                color = if (isViolation) GRedAccent else if (isActive || isTarget) GOrangeAccent else GTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                x = centerPx.x - textLayout.size.width / 2f,
                                y = centerPx.y - textLayout.size.height / 2f
                            )
                        )

                        // Badge Label (if present)
                        if (!entity.badgeLabel.isNullOrBlank()) {
                            val badgeLayout = textMeasurer.measure(
                                text = entity.badgeLabel!!,
                                style = TextStyle(
                                    color = if (isViolation) GRedAccent else if (isActive || isTarget) GOrangeAccent else GBlueAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = badgeLayout,
                                topLeft = Offset(
                                    x = centerPx.x - badgeLayout.size.width / 2f,
                                    y = centerPx.y - radiusPx - badgeLayout.size.height - 3.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
