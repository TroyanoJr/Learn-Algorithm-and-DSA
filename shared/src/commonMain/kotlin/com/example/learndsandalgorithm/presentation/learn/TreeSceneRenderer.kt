package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TreeCanvasBranchDefaultColor = Color(0xFF38BDF8)
private val TreeCanvasBranchHighlightColor = Color(0xFFFA6E13)

/**
 * Reusable Compose Multiplatform Canvas renderer for binary tree [VisualizationScene].
 * Draws parent-child connection lines, node circles, values, and visual states inside a single Canvas
 * using the approved [TreeSceneLayout] geometry engine.
 */
@Composable
internal fun TreeSceneRenderer(
    scene: VisualizationScene,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value

        val treeSubScene = scene.subScenes.firstOrNull { it.subPaneId == "tree_traversal_tree" || it.type == StructureType.TREE }
        val layoutNodesSubScene = if (treeSubScene != null) {
            if (treeSubScene.referenceConnections.isNotEmpty()) {
                treeSubScene.copy(connections = treeSubScene.referenceConnections)
            } else {
                treeSubScene
            }
        } else null
        val nodes = if (layoutNodesSubScene != null) extractTraversalNodes(layoutNodesSubScene) else emptyList()

        val layout = TreeSceneLayout.calculateLayout(
            nodes = nodes,
            availableWidthDp = widthDp,
            availableHeightDp = heightDp
        )

        val canvasWidthDp = maxOf(widthDp, layout.requiredWidth)
        val canvasHeightDp = maxOf(heightDp, layout.requiredHeight)

        VisualizerCanvasViewport(
            modifier = modifier.fillMaxSize(),
            mode = VisualizerCanvasMode.HYBRID
        ) {
            Canvas(
                modifier = Modifier.size(canvasWidthDp.dp, canvasHeightDp.dp)
            ) {
                val entityMap = treeSubScene?.entities?.associateBy { it.visualKey } ?: emptyMap()

                // ==========================================
                // A. DRAW PARENT-CHILD CONNECTION LINES (First)
                // ==========================================
                treeSubScene?.connections?.forEach { conn ->
                    val fromEntity = entityMap[conn.fromVisualKey]
                    val toEntity = entityMap[conn.toVisualKey]
                    val areBothPresent = fromEntity?.presenceState == EntityPresenceState.PRESENT &&
                                         toEntity?.presenceState == EntityPresenceState.PRESENT
                    if (areBothPresent && fromEntity != null && toEntity != null) {
                        val fromSlotKey = "tree_slot:${fromEntity.elementId.removePrefix("node:")}"
                        val toSlotKey = "tree_slot:${toEntity.elementId.removePrefix("node:")}"
                        val fromRect = layout.nodeBounds[fromSlotKey]
                        val toRect = layout.nodeBounds[toSlotKey]

                        val line = if (fromRect != null && toRect != null) {
                            TreeSceneLayout.calculateBoundaryConnectionLine(fromRect, toRect)
                        } else {
                            layout.connectionLines[conn.connectionKey]
                        }

                        if (line != null) {
                            val startPx = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx())
                            val endPx = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx())
                            val isHigh = conn.visualState == ConnectionVisualState.HIGHLIGHTED
                            val strokeW = if (isHigh) 3.dp.toPx() else 1.5.dp.toPx()
                            val color = if (isHigh) TreeCanvasBranchHighlightColor else TreeCanvasBranchDefaultColor.copy(alpha = 0.6f)

                            drawLine(
                                color = color,
                                start = startPx,
                                end = endPx,
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // ==========================================
                // B, C, D. DRAW NODE BACKGROUNDS, VALUES, & STATES
                // ==========================================
                treeSubScene?.entities?.forEach { entity ->
                    if (entity.presenceState != EntityPresenceState.PRESENT) return@forEach
                    val slotKey = "tree_slot:${entity.elementId.removePrefix("node:")}"
                    val rect = layout.nodeBounds[slotKey]
                    if (rect != null) {
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
                            isViolation -> TreeRedAccent
                            isActive || isTarget -> TreeOrangeAccent
                            isVisited -> TreeGreenAccent
                            else -> TreeCardBorderColor
                        }

                        // Node Background Circle
                        drawCircle(color = bgColor, center = centerPx, radius = radiusPx)

                        // Node Border Stroke
                        drawCircle(
                            color = borderColor,
                            center = centerPx,
                            radius = radiusPx,
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Node Value Text
                        val textLayout = textMeasurer.measure(
                            text = entity.dataValue,
                            style = TextStyle(
                                color = if (isViolation) TreeRedAccent else if (isActive || isTarget) TreeOrangeAccent else TreeTextPrimary,
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
                        if (entity.badgeLabel != null) {
                            val badgeLayout = textMeasurer.measure(
                                text = entity.badgeLabel,
                                style = TextStyle(
                                    color = if (isViolation) TreeRedAccent else if (isActive || isTarget) TreeOrangeAccent else TreeBlueAccent,
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

private fun extractTraversalNodes(subScene: StructureSubScene): List<TraversalNode> {
    val nodeMap = mutableMapOf<String, TraversalNode>()

    subScene.entities.forEach { entity ->
        val id = entity.elementId.removePrefix("node:")
        val value = entity.dataValue.toIntOrNull() ?: 0
        nodeMap[id] = TraversalNode(id = id, value = value)
    }

    subScene.connections.forEach { conn ->
        val parentId = conn.fromVisualKey.substringAfter("node:")
        val childId = conn.toVisualKey.substringAfter("node:")
        val parentNode = nodeMap[parentId]
        if (parentNode != null) {
            if (conn.label == "L") {
                nodeMap[parentId] = parentNode.copy(leftId = childId)
            } else if (conn.label == "R") {
                nodeMap[parentId] = parentNode.copy(rightId = childId)
            } else {
                if (parentNode.leftId == null) {
                    nodeMap[parentId] = parentNode.copy(leftId = childId)
                } else if (parentNode.rightId == null) {
                    nodeMap[parentId] = parentNode.copy(rightId = childId)
                }
            }
        }
    }

    return nodeMap.values.toList()
}
