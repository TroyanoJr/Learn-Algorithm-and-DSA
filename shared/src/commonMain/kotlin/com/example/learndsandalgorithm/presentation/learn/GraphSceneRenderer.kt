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
import androidx.compose.ui.geometry.Rect
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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private val GBranchDefaultColor = Color(0xFF38BDF8)
private val GBranchHighlightColor = Color(0xFFFA6E13)
private val GBranchPathColor = Color(0xFF10B981)
private val GCardBorderColor = Color(0xFF262634)
private val GOrangeAccent = Color(0xFFFA6E13)
private val GBlueAccent = Color(0xFF38BDF8)
private val GGreenAccent = Color(0xFF10B981)
private val GRedAccent = Color(0xFFEF4444)
private val GTextPrimary = Color(0xFFFFFFFF)

private fun lineSegmentsIntersect(
    p1: Offset, p2: Offset,
    p3: Offset, p4: Offset,
    eps: Float = 1e-4f
): Boolean {
    fun onSegment(p: Offset, a: Offset, b: Offset): Boolean {
        return p.x in min(a.x, b.x) - eps..max(a.x, b.x) + eps &&
               p.y in min(a.y, b.y) - eps..max(a.y, b.y) + eps
    }

    fun orientation(a: Offset, b: Offset, c: Offset): Float {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
    }

    val o1 = orientation(p1, p2, p3)
    val o2 = orientation(p1, p2, p4)
    val o3 = orientation(p3, p4, p1)
    val o4 = orientation(p3, p4, p2)

    val sign11 = if (o1 > eps) 1 else if (o1 < -eps) -1 else 0
    val sign12 = if (o2 > eps) 1 else if (o2 < -eps) -1 else 0
    val sign21 = if (o3 > eps) 1 else if (o3 < -eps) -1 else 0
    val sign22 = if (o4 > eps) 1 else if (o4 < -eps) -1 else 0

    if ((sign11 != 0 && sign12 != 0 && sign11 != sign12) &&
        (sign21 != 0 && sign22 != 0 && sign21 != sign22)) {
        return true
    }

    if (abs(o1) <= eps && onSegment(p3, p1, p2)) return true
    if (abs(o2) <= eps && onSegment(p4, p1, p2)) return true
    if (abs(o3) <= eps && onSegment(p1, p3, p4)) return true
    if (abs(o4) <= eps && onSegment(p2, p3, p4)) return true

    return false
}

private fun lineIntersectsRect(
    p1: Offset,
    p2: Offset,
    rect: Rect
): Boolean {
    if (rect.contains(p1) || rect.contains(p2)) return true

    val rx1 = rect.left
    val ry1 = rect.top
    val rx2 = rect.right
    val ry2 = rect.bottom

    val edges = arrayOf(
        Offset(rx1, ry1) to Offset(rx2, ry1),
        Offset(rx2, ry1) to Offset(rx2, ry2),
        Offset(rx2, ry2) to Offset(rx1, ry2),
        Offset(rx1, ry2) to Offset(rx1, ry1)
    )

    for ((e1, e2) in edges) {
        if (lineSegmentsIntersect(p1, p2, e1, e2)) return true
    }
    return false
}

private fun isWithinCanvas(rect: Rect, canvasWidth: Float, canvasHeight: Float): Boolean {
    return rect.left >= 2f && rect.top >= 2f && rect.right <= canvasWidth - 2f && rect.bottom <= canvasHeight - 2f
}

/**
 * Reusable Compose Multiplatform Canvas renderer for general graph [VisualizationScene].
 * Draws vertices, edges, arrowheads for directed edges, edge labels, badges, and visual states
 * inside a single Canvas using [GraphSceneAdapter] and [GraphSceneLayout].
 */
@Composable
internal fun GraphSceneRenderer(
    scene: VisualizationScene,
    nodeClearanceDp: Float = GraphSceneLayout.MIN_CLEARANCE_DP,
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

        val layout = remember(adapterResult, widthDp, heightDp, nodeClearanceDp) {
            if (adapterResult != null) {
                GraphSceneLayout.calculateLayout(
                    vertices = adapterResult.vertices,
                    edges = adapterResult.edges,
                    availableWidthDp = widthDp,
                    availableHeightDp = heightDp,
                    minClearanceDp = nodeClearanceDp
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
                val canvasW = canvasWidthDp.dp.toPx()
                val canvasH = canvasHeightDp.dp.toPx()

                val arrowLengthPx = 10.dp.toPx()
                val arrowAngleRad = 0.523599f

                val rectIntersectsArrowhead: (Rect, Offset, Float) -> Boolean = { rect, endPx, angle ->
                    val point1 = Offset(
                        x = endPx.x - arrowLengthPx * cos(angle - arrowAngleRad),
                        y = endPx.y - arrowLengthPx * sin(angle - arrowAngleRad)
                    )
                    val point2 = Offset(
                        x = endPx.x - arrowLengthPx * cos(angle + arrowAngleRad),
                        y = endPx.y - arrowLengthPx * sin(angle + arrowAngleRad)
                    )
                    rect.contains(endPx) || rect.contains(point1) || rect.contains(point2) ||
                    lineIntersectsRect(endPx, point1, rect) || lineIntersectsRect(endPx, point2, rect) || lineIntersectsRect(point1, point2, rect)
                }

                val scoreBadgeCandidate: (Rect, List<Rect>, Collection<Rect>, Map<String, LineSegment2D>, Map<String, VisualConnection>) -> Int = { candRect, vertexRects, badgeRects, edgeLinesMap, edgeConnMap ->
                    var score = 0
                    vertexRects.forEach { if (candRect.overlaps(it)) score += 10 }
                    badgeRects.forEach { if (candRect.overlaps(it)) score += 5 }
                    edgeLinesMap.forEach { (eKey, line) ->
                        val s = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx())
                        val e = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx())
                        if (lineIntersectsRect(s, e, candRect)) score += 3
                        val conn = edgeConnMap[eKey]
                        if (conn?.directed == true) {
                            val angle = atan2(e.y - s.y, e.x - s.x)
                            if (rectIntersectsArrowhead(candRect, e, angle)) score += 3
                        }
                    }
                    score
                }

                val scoreLabelCandidate: (Rect, List<Rect>, Collection<Rect>, List<Rect>, Map<String, LineSegment2D>, Map<String, VisualConnection>, String) -> Int = { candRect, vertexRects, badgeRects, labelRects, edgeLinesMap, edgeConnMap, targetEdgeKey ->
                    var score = 0
                    vertexRects.forEach { if (candRect.overlaps(it)) score += 10 }
                    badgeRects.forEach { if (candRect.overlaps(it)) score += 5 }
                    labelRects.forEach { if (candRect.overlaps(it)) score += 5 }
                    edgeLinesMap.forEach { (eKey, line) ->
                        val s = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx())
                        val e = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx())
                        if (lineIntersectsRect(s, e, candRect)) {
                            score += if (eKey == targetEdgeKey) 4 else 3
                        }
                        val conn = edgeConnMap[eKey]
                        if (conn?.directed == true) {
                            val angle = atan2(e.y - s.y, e.x - s.x)
                            if (rectIntersectsArrowhead(candRect, e, angle)) {
                                score += if (eKey == targetEdgeKey) 4 else 3
                            }
                        }
                    }
                    score
                }

                val placedVertexCircleRects = mutableListOf<Rect>()
                vertexBounds.forEach { (_, rect) ->
                    val cX = rect.center.x.dp.toPx()
                    val cY = rect.center.y.dp.toPx()
                    val rPx = (rect.width.dp.toPx()) / 2f
                    placedVertexCircleRects.add(
                        Rect(cX - rPx, cY - rPx, cX + rPx, cY + rPx)
                    )
                }

                val placedBadgeRects = mutableMapOf<String, Rect>()
                val placedLabelRects = mutableListOf<Rect>()

                // Pre-calculate Badge Layouts & Non-overlapping Placements with guaranteed in-bounds filtering
                vertexBounds.forEach { (vertexKey, rect) ->
                    val entity = vertexMap[vertexKey]
                    if (entity != null && !entity.badgeLabel.isNullOrBlank() && entity.presenceState != EntityPresenceState.DISAPPEARING) {
                        val centerPx = Offset(rect.center.x.dp.toPx(), rect.center.y.dp.toPx())
                        val radiusPx = (rect.width.dp.toPx()) / 2f

                        val isActive = entity.visualState == EntityVisualState.ACTIVE
                        val isViolation = entity.visualState == EntityVisualState.VIOLATION
                        val isTarget = entity.visualState == EntityVisualState.TARGET

                        val badgeLayout = textMeasurer.measure(
                            text = entity.badgeLabel!!,
                            style = TextStyle(
                                color = if (isViolation) GRedAccent else if (isActive || isTarget) GOrangeAccent else GBlueAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        val bw = badgeLayout.size.width.toFloat()
                        val bh = badgeLayout.size.height.toFloat()
                        val padX = 3.dp.toPx()
                        val padY = 1.dp.toPx()

                        val candidates = listOf(
                            Offset(centerPx.x - bw / 2f, centerPx.y - radiusPx - bh - 4.dp.toPx()), // ABOVE
                            Offset(centerPx.x - bw / 2f, centerPx.y + radiusPx + 4.dp.toPx()),       // BELOW
                            Offset(centerPx.x + radiusPx + 4.dp.toPx(), centerPx.y - bh / 2f),       // RIGHT
                            Offset(centerPx.x - radiusPx - bw - 4.dp.toPx(), centerPx.y - bh / 2f),  // LEFT
                            Offset(centerPx.x - bw / 2f, centerPx.y - radiusPx - bh - 12.dp.toPx())  // HIGHER ABOVE
                        )

                        val validCandidates = candidates.map { cand ->
                            Rect(cand.x - padX, cand.y - padY, cand.x + bw + padX, cand.y + bh + padY)
                        }.filter { isWithinCanvas(it, canvasW, canvasH) }

                        val bestBgRect = if (validCandidates.isNotEmpty()) {
                            validCandidates.minByOrNull { bgRect ->
                                scoreBadgeCandidate(
                                    bgRect,
                                    placedVertexCircleRects.filter { vRect ->
                                        vRect != Rect(centerPx.x - radiusPx, centerPx.y - radiusPx, centerPx.x + radiusPx, centerPx.y + radiusPx)
                                    },
                                    placedBadgeRects.values,
                                    edgeLines,
                                    edgeMap
                                )
                            }!!
                        } else {
                            val clampedX = centerPx.x.coerceIn(2f + padX, canvasW - bw - 2f - padX)
                            val clampedY = (centerPx.y - radiusPx - bh - 4f).coerceIn(2f + padY, canvasH - bh - 2f - padY)
                            Rect(clampedX - padX, clampedY - padY, clampedX + bw + padX, clampedY + bh + padY)
                        }

                        placedBadgeRects[vertexKey] = bestBgRect
                    }
                }

                // ==========================================
                // A. DRAW EDGES, ARROWHEADS, AND EDGE LABELS
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
                        val point1 = Offset(
                            x = endPx.x - arrowLengthPx * cos(angle - arrowAngleRad),
                            y = endPx.y - arrowLengthPx * sin(angle - arrowAngleRad)
                        )
                        val point2 = Offset(
                            x = endPx.x - arrowLengthPx * cos(angle + arrowAngleRad),
                            y = endPx.y - arrowLengthPx * sin(angle + arrowAngleRad)
                        )

                        val arrowPath = Path().apply {
                            moveTo(endPx.x, endPx.y)
                            lineTo(point1.x, point1.y)
                            lineTo(point2.x, point2.y)
                            close()
                        }
                        drawPath(path = arrowPath, color = color)
                    }

                    // Optional edge label with comprehensive in-bounds candidate scoring
                    if (!conn?.label.isNullOrBlank()) {
                        val midPx = Offset((startPx.x + endPx.x) / 2f, (startPx.y + endPx.y) / 2f)
                        val dX = endPx.x - startPx.x
                        val dY = endPx.y - startPx.y
                        val lineLen = maxOf(1f, sqrt(dX * dX + dY * dY))
                        val perpX = -dY / lineLen
                        val perpY = dX / lineLen

                        val labelLayout = textMeasurer.measure(
                            text = conn.label!!,
                            style = TextStyle(
                                color = GBlueAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        val lw = labelLayout.size.width.toFloat()
                        val lh = labelLayout.size.height.toFloat()
                        val padX = 3.dp.toPx()
                        val padY = 1.dp.toPx()

                        val offsetsDp = listOf(14f, -14f, 22f, -22f, 30f, -30f, 38f, -38f)
                        val validCandidates = offsetsDp.map { off ->
                            val center = Offset(midPx.x + perpX * off.dp.toPx(), midPx.y + perpY * off.dp.toPx())
                            val tl = Offset(center.x - lw / 2f, center.y - lh / 2f)
                            Rect(tl.x - padX, tl.y - padY, tl.x + lw + padX, tl.y + lh + padY)
                        }.filter { isWithinCanvas(it, canvasW, canvasH) }

                        val bestBgRect = if (validCandidates.isNotEmpty()) {
                            validCandidates.minByOrNull { bgRect ->
                                scoreLabelCandidate(
                                    bgRect,
                                    placedVertexCircleRects,
                                    placedBadgeRects.values,
                                    placedLabelRects,
                                    edgeLines,
                                    edgeMap,
                                    edgeKey
                                )
                            }!!
                        } else {
                            val clampedX = midPx.x.coerceIn(2f + padX, canvasW - lw - 2f - padX)
                            val clampedY = midPx.y.coerceIn(2f + padY, canvasH - lh - 2f - padY)
                            Rect(clampedX - padX, clampedY - padY, clampedX + lw + padX, clampedY + lh + padY)
                        }

                        placedLabelRects.add(bestBgRect)

                        drawRoundRect(
                            color = Color(0xFF13131C).copy(alpha = 0.9f),
                            topLeft = Offset(bestBgRect.left, bestBgRect.top),
                            size = Size(
                                width = bestBgRect.width,
                                height = bestBgRect.height
                            ),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        drawText(
                            textLayoutResult = labelLayout,
                            topLeft = Offset(bestBgRect.left + padX, bestBgRect.top + padY)
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
                            isVisited -> Color(0xFF123820) // restored VISITED state
                            else -> Color(0xFF13233A)
                        }

                        val borderColor = when {
                            isViolation -> GRedAccent
                            isActive || isTarget -> GOrangeAccent
                            isVisited -> GGreenAccent // restored VISITED state
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
                            val badgeRect = placedBadgeRects[vertexKey]
                            val badgeTopLeft = if (badgeRect != null) {
                                Offset(badgeRect.left + 3.dp.toPx(), badgeRect.top + 1.dp.toPx())
                            } else {
                                Offset(
                                    x = centerPx.x - badgeLayout.size.width / 2f,
                                    y = centerPx.y - radiusPx - badgeLayout.size.height - 3.dp.toPx()
                                )
                            }
                            val padX = 3.dp.toPx()
                            val padY = 1.dp.toPx()

                            drawRoundRect(
                                color = Color(0xFF13131C).copy(alpha = 0.9f),
                                topLeft = Offset(badgeTopLeft.x - padX, badgeTopLeft.y - padY),
                                size = Size(
                                    width = badgeLayout.size.width + padX * 2f,
                                    height = badgeLayout.size.height + padY * 2f
                                ),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )

                            drawText(
                                textLayoutResult = badgeLayout,
                                topLeft = badgeTopLeft
                            )
                        }
                    }
                }
            }
        }
    }
}
