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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CanvasBranchDefaultColor = Color(0xFF38BDF8)
private val CanvasBranchHighlightColor = Color(0xFFFA6E13)

/**
 * Single Canvas-based graphical renderer for [VisualizationScene] produced by [HeapSceneBuilder].
 * Draws all tree nodes, boundary connection lines, values, labels, array cells, and indices
 * inside one unified Canvas and coordinate space.
 */
@Composable
internal fun HeapSceneRenderer(
    scene: VisualizationScene,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value

        val treeSubScene = scene.subScenes.firstOrNull { it.subPaneId == "heap_tree" }
        val arraySubScene = scene.subScenes.firstOrNull { it.subPaneId == "heap_array" }

        val treeElementCount = treeSubScene?.entities?.size ?: 0
        val arrayElementCount = arraySubScene?.entities?.size ?: 0

        val layout = HeapSceneLayout.calculateLayout(
            treeElementCount = treeElementCount,
            arrayElementCount = arrayElementCount,
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
                // 1. DRAW TREE CONNECTIONS (Boundary-to-Boundary)
                // ==========================================
                treeSubScene?.connections?.forEach { conn ->
                    val line = layout.treeConnectionLines[conn.connectionKey]
                    if (line != null) {
                        val startPx = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx())
                        val endPx = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx())
                        val isHigh = conn.visualState == ConnectionVisualState.HIGHLIGHTED
                        val strokeW = if (isHigh) 3.dp.toPx() else 1.5.dp.toPx()
                        val color = if (isHigh) CanvasBranchHighlightColor else CanvasBranchDefaultColor.copy(alpha = 0.6f)

                        drawLine(
                            color = color,
                            start = startPx,
                            end = endPx,
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // ==========================================
                // 2. DRAW TREE NODES & COLLISION-FREE BADGES
                // ==========================================
                treeSubScene?.entities?.forEach { entity ->
                    val rect = layout.treeNodeBounds[entity.slotKey]
                    if (rect != null) {
                        val centerPx = Offset(rect.center.x.dp.toPx(), rect.center.y.dp.toPx())
                        val radiusPx = (rect.width.dp.toPx()) / 2f

                        val isHigh = entity.visualState == EntityVisualState.ACTIVE || entity.visualState == EntityVisualState.TARGET
                        val isVis = entity.visualState == EntityVisualState.VISITED
                        val isViol = entity.visualState == EntityVisualState.VIOLATION

                        val bgColor = when {
                            isViol -> Color(0xFF381212)
                            isHigh -> Color(0xFF2A1C12)
                            isVis -> Color(0xFF123820)
                            else -> Color(0xFF13233A)
                        }

                        val borderColor = when {
                            isViol -> TreeRedAccent
                            isHigh -> TreeOrangeAccent
                            isVis -> TreeGreenAccent
                            else -> TreeCardBorderColor
                        }

                        // Background fill
                        drawCircle(color = bgColor, center = centerPx, radius = radiusPx)

                        // Border stroke
                        drawCircle(
                            color = borderColor,
                            center = centerPx,
                            radius = radiusPx,
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Node Value Text (Centered inside circle)
                        val textLayout = textMeasurer.measure(
                            text = entity.dataValue,
                            style = TextStyle(
                                color = if (isViol) TreeRedAccent else if (isHigh) TreeOrangeAccent else TreeTextPrimary,
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

                        // Badge Label (Collision-Free Outer Placement)
                        if (entity.badgeLabel != null) {
                            val badgeLayout = textMeasurer.measure(
                                text = entity.badgeLabel,
                                style = TextStyle(
                                    color = if (isViol) TreeRedAccent else if (isHigh) TreeOrangeAccent else TreeBlueAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            // Extract slot index from slotKey ("tree_slot:i")
                            val slotIdx = entity.slotKey.substringAfter("tree_slot:").toIntOrNull() ?: 0

                            // Placement Strategy:
                            // Slot 0 (Root): Above root node (no parent branch exists above root).
                            // Left Child (odd slot): Left side of node (incoming parent branch arrives from top-right).
                            // Right Child (even slot): Right side of node (incoming parent branch arrives from top-left).
                            val badgeTopLeft = when {
                                slotIdx == 0 -> Offset(
                                    x = centerPx.x - badgeLayout.size.width / 2f,
                                    y = centerPx.y - radiusPx - badgeLayout.size.height - 4.dp.toPx()
                                )
                                slotIdx % 2 != 0 -> Offset(
                                    x = centerPx.x - radiusPx - badgeLayout.size.width - 3.dp.toPx(),
                                    y = centerPx.y - badgeLayout.size.height / 2f
                                )
                                else -> Offset(
                                    x = centerPx.x + radiusPx + 3.dp.toPx(),
                                    y = centerPx.y - badgeLayout.size.height / 2f
                                )
                            }

                            drawText(
                                textLayoutResult = badgeLayout,
                                topLeft = badgeTopLeft
                            )
                        }
                    }
                }

                // ==========================================
                // 3. DRAW ARRAY CELLS & INDEX LABELS
                // ==========================================
                arraySubScene?.entities?.forEachIndexed { idx, entity ->
                    val rect = layout.arrayCellBounds[entity.slotKey]
                    val labelRect = layout.arrayIndexLabelBounds[entity.slotKey]

                    if (rect != null) {
                        val leftPx = rect.left.dp.toPx()
                        val topPx = rect.top.dp.toPx()
                        val widthPx = rect.width.dp.toPx()
                        val heightPx = rect.height.dp.toPx()

                        val isHigh = entity.visualState == EntityVisualState.ACTIVE || entity.visualState == EntityVisualState.TARGET
                        val isVis = entity.visualState == EntityVisualState.VISITED
                        val isViol = entity.visualState == EntityVisualState.VIOLATION

                        val bgColor = when {
                            isViol -> Color(0xFF381212)
                            isHigh -> Color(0xFF2A1C12)
                            isVis -> Color(0xFF123820)
                            else -> Color(0xFF13233A)
                        }

                        val borderColor = when {
                            isViol -> TreeRedAccent
                            isHigh -> TreeOrangeAccent
                            isVis -> TreeGreenAccent
                            else -> TreeCardBorderColor
                        }

                        val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                        // Cell Fill
                        drawRoundRect(
                            color = bgColor,
                            topLeft = Offset(leftPx, topPx),
                            size = Size(widthPx, heightPx),
                            cornerRadius = cornerRadius
                        )

                        // Cell Border
                        drawRoundRect(
                            color = borderColor,
                            topLeft = Offset(leftPx, topPx),
                            size = Size(widthPx, heightPx),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Cell Value Text
                        val cellTextLayout = textMeasurer.measure(
                            text = entity.dataValue,
                            style = TextStyle(
                                color = if (isViol) TreeRedAccent else if (isHigh) TreeOrangeAccent else TreeTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = cellTextLayout,
                            topLeft = Offset(
                                x = leftPx + (widthPx - cellTextLayout.size.width) / 2f,
                                y = topPx + (heightPx - cellTextLayout.size.height) / 2f
                            )
                        )

                        // Array Index Label below cell
                        val labelTopPx = labelRect?.top?.dp?.toPx() ?: (topPx + heightPx + 2.dp.toPx())
                        val indexTextLayout = textMeasurer.measure(
                            text = "[$idx]",
                            style = TextStyle(
                                color = if (isHigh) TreeOrangeAccent else TreeTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = indexTextLayout,
                            topLeft = Offset(
                                x = leftPx + (widthPx - indexTextLayout.size.width) / 2f,
                                y = labelTopPx
                            )
                        )
                    }
                }
            }
        }
    }
}
