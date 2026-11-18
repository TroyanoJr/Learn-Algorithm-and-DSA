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

internal fun isFundamentalsGeneralTreeScene(scene: VisualizationScene): Boolean =
    scene.subScenes.any { it.subPaneId == "tree_fundamentals_tree" }

@Composable
internal fun GeneralTreeSceneRenderer(scene: VisualizationScene, modifier: Modifier = Modifier) {
    val subScene = scene.subScenes.firstOrNull { it.subPaneId == "tree_fundamentals_tree" }
        ?: error("GeneralTreeSceneRenderer requires tree_fundamentals_tree")
    val layout = GeneralTreeSceneLayout.calculateLayout(subScene)
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier.fillMaxSize()) {
        // Extra room protects badges that extend beyond the node-layout margins.
        val badgeSafetyMargin = 32f
        val canvasWidth = maxOf(maxWidth.value, layout.requiredWidth + badgeSafetyMargin * 2)
        val canvasHeight = maxOf(maxHeight.value, layout.requiredHeight + badgeSafetyMargin)
        VisualizerCanvasViewport(modifier = modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID) {
            Canvas(Modifier.size(canvasWidth.dp, canvasHeight.dp)) {
                val offsetX = badgeSafetyMargin.dp.toPx()
                val offsetY = 0f
                val entities = subScene.entities.associateBy { it.visualKey }
                subScene.connections.filter { it.type == ConnectionType.STRUCTURAL_BRANCH }.forEach { connection ->
                    val line = layout.connectionLines[connection.connectionKey]
                        ?: error("Missing geometry for structural connection ${connection.connectionKey}")
                    drawLine(
                        color = if (connection.visualState == ConnectionVisualState.HIGHLIGHTED) Color(0xFFFA6E13) else Color(0xFF38BDF8).copy(alpha = 0.6f),
                        start = Offset(line.start.x.dp.toPx() + offsetX, line.start.y.dp.toPx() + offsetY),
                        end = Offset(line.end.x.dp.toPx() + offsetX, line.end.y.dp.toPx() + offsetY),
                        strokeWidth = if (connection.visualState == ConnectionVisualState.HIGHLIGHTED) 3.dp.toPx() else 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                subScene.entities.forEach { entity ->
                    val rect = layout.nodeBounds[entity.slotKey]
                        ?: error("Missing geometry for entity slot ${entity.slotKey}")
                    val center = Offset(rect.center.x.dp.toPx() + offsetX, rect.center.y.dp.toPx() + offsetY)
                    val radius = GeneralTreeSceneLayout.NODE_DIAMETER_DP.dp.toPx() / 2f
                    val active = entity.visualState == EntityVisualState.ACTIVE || entity.visualState == EntityVisualState.TARGET
                    val visited = entity.visualState == EntityVisualState.VISITED
                    val violation = entity.visualState == EntityVisualState.VIOLATION
                    val fill = when { violation -> Color(0xFF381212); active -> Color(0xFF2A1C12); visited -> Color(0xFF123820); else -> Color(0xFF13233A) }
                    val stroke = when { violation -> Color(0xFFEF4444); active -> Color(0xFFFA6E13); visited -> Color(0xFF10B981); else -> Color(0xFF262634) }
                    drawCircle(fill, radius, center)
                    drawCircle(stroke, radius, center, style = Stroke(width = 1.5.dp.toPx()))
                    val value = textMeasurer.measure(entity.dataValue, TextStyle(
                        color = when { violation -> Color(0xFFEF4444); active -> Color(0xFFFA6E13); else -> Color.White },
                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                    ))
                    drawText(
                        textLayoutResult = value,
                        topLeft = Offset(center.x - value.size.width / 2f, center.y - value.size.height / 2f)
                    )
                    entity.badgeLabel?.let { label ->
                        val badge = textMeasurer.measure(label, TextStyle(
                            color = when { violation -> Color(0xFFEF4444); active -> Color(0xFFFA6E13); else -> Color(0xFF38BDF8) },
                            fontSize = 9.sp, fontWeight = FontWeight.Bold
                        ))
                        drawText(
                            textLayoutResult = badge,
                            topLeft = Offset(center.x - badge.size.width / 2f, center.y - radius - badge.size.height - 3.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
