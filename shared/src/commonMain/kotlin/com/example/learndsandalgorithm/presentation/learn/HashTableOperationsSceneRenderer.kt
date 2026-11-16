package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp

private val OpsBorder = Color(0xFF262634)
private val OpsOrange = Color(0xFFFA6E13)
private val OpsBlue = Color(0xFF38BDF8)
private val OpsGreen = Color(0xFF10B981)
private val OpsRed = Color(0xFFEF4444)
private val OpsText = Color.White
private val OpsSecondary = Color(0xFF9CA3AF)

internal fun selectMaximumOperationsLoadCardHeightPx(measuredHeightsPx: List<Int>): Int =
    measuredHeightsPx.maxOrNull()?.coerceAtLeast(0) ?: 0

@Composable
internal fun HashTableOperationsSceneRenderer(
    scene: VisualizationScene,
    step: OperationsTraceStep,
    modifier: Modifier = Modifier
) {
    val sequence = remember { HashTableOperationsTrace.stepsForOperations() }
    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID) {
        SubcomposeLayout { _ ->
            val sceneWidthPx = HashTableOperationsSceneLayout.SCENE_WIDTH_DP.dp.roundToPx()
            val cardWidthDp = HashTableOperationsSceneLayout.SCENE_WIDTH_DP - HashTableOperationsSceneLayout.MARGIN_DP * 2f
            val cardWidthPx = cardWidthDp.dp.roundToPx()

            val measuredCardHeights = sequence.mapIndexed { index, sequenceStep ->
                subcompose("load_card_probe_$index") {
                    OperationsLoadFactorCard(sequenceStep, Modifier.width(cardWidthDp.dp))
                }.single().measure(Constraints.fixedWidth(cardWidthPx)).height
            }
            val maximumCardHeightPx = selectMaximumOperationsLoadCardHeightPx(measuredCardHeights)
            val layout = HashTableOperationsSceneLayout.calculate(
                step = step,
                measuredLoadCardHeightDp = maximumCardHeightPx.toDp().value
            )
            val sceneHeightPx = layout.requiredSceneHeight.dp.roundToPx()
            val structure = subcompose("operations_structure") {
                OperationsStructureCanvas(scene, step, layout)
            }.single().measure(Constraints.fixed(sceneWidthPx, sceneHeightPx))
            val displayedCard = subcompose("displayed_load_card_${scene.stepIndex}") {
                OperationsLoadFactorCard(
                    step = step,
                    modifier = Modifier.size(layout.loadCardBounds.width.dp, layout.loadCardBounds.height.dp)
                )
            }.single().measure(
                Constraints.fixed(
                    layout.loadCardBounds.width.dp.roundToPx(),
                    layout.loadCardBounds.height.dp.roundToPx()
                )
            )

            layout(sceneWidthPx, sceneHeightPx) {
                structure.place(0, 0)
                displayedCard.place(
                    layout.loadCardBounds.left.dp.roundToPx(),
                    layout.loadCardBounds.top.dp.roundToPx()
                )
            }
        }
    }
}

@Composable
private fun OperationsLoadFactorCard(step: OperationsTraceStep, modifier: Modifier) {
    val accent = if (step.isThresholdExceeded) OpsRed else OpsOrange
    val presentation = HashTableOperationsSceneLayout.loadFactorPresentation(step)
    Surface(modifier, color = Color(0xFF13131C), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, accent)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "LOAD FACTOR: alpha = k / N = ${presentation.currentSize} / ${presentation.currentCapacity} = ${presentation.currentLoadFactor}",
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    text = if (step.isThresholdExceeded) {
                        "Threshold: alpha <= 0.75 ➔ EXCEEDED!\nTrigger: alpha = ${presentation.triggerSize} / ${presentation.triggerCapacity} = ${presentation.triggerLoadFactor}; N → 2N"
                    } else {
                        "Threshold: alpha <= 0.75 (Collisions Low)"
                    },
                    color = if (step.isThresholdExceeded) OpsRed else OpsSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(color = when (step.activeOp) {
                    HashOpType.RESIZE_REHASH -> Color(0xFF381212)
                    HashOpType.LOOKUP -> Color(0xFF123820)
                    HashOpType.DELETE -> Color(0xFF122838)
                    HashOpType.INSERT -> Color(0xFF2A1C12)
                }, shape = RoundedCornerShape(5.dp), border = BorderStroke(1.dp, accent)) {
                    Text(
                        text = step.activeOp.name,
                        color = OpsText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OperationsStructureCanvas(scene: VisualizationScene, step: OperationsTraceStep, layout: HashTableOperationsLayoutResult) {
    val measurer = rememberTextMeasurer()
    Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
        fun drawCentered(text: String, bounds: Rect2D, color: Color, sizeSp: Int, bold: Boolean = false) {
            val measured = measurer.measure(text, TextStyle(color = color, fontSize = sizeSp.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    bounds.left.dp.toPx() + (bounds.width.dp.toPx() - measured.size.width) / 2f,
                    bounds.top.dp.toPx() + (bounds.height.dp.toPx() - measured.size.height) / 2f
                )
            )
        }
        fun drawSlot(bounds: Rect2D, entry: HashOpEntry?, highlighted: Boolean, old: Boolean = false, index: Int? = null) {
            val violation = highlighted && step.activeOp == HashOpType.DELETE
            val stroke = when { violation -> OpsRed; highlighted -> OpsOrange; entry != null && !old -> OpsBlue; else -> OpsBorder }
            val fill = when { violation -> Color(0xFF381212); highlighted -> Color(0xFF2A1C12); entry != null && !old -> Color(0xFF13233A); else -> Color(0xFF13131C) }
            val top = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx())
            val pxSize = androidx.compose.ui.geometry.Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
            drawRoundRect(fill, top, pxSize, CornerRadius(7.dp.toPx()))
            drawRoundRect(stroke, top, pxSize, CornerRadius(7.dp.toPx()), style = Stroke(1.dp.toPx()))
            if (entry == null) drawCentered("·", bounds, OpsSecondary.copy(alpha = .55f), 14, true)
            else if (old) {
                drawCentered("${entry.key} [${index ?: "?"}]", bounds, OpsSecondary, 9, true)
            }
            else {
                drawCentered(entry.key, Rect2D(bounds.left, bounds.top + 5f, bounds.width, 18f), OpsText, 11, true)
                drawCentered(entry.value, Rect2D(bounds.left, bounds.top + 24f, bounds.width, 16f), OpsBlue, 9)
            }
        }
        if (step.oldBuckets != null && step.oldCapacity != null) {
            drawCentered("OLD ARRAY (Capacity ${step.oldCapacity}):", layout.oldHeadingBounds, OpsSecondary, 9, true)
            step.oldBuckets.forEachIndexed { index, entry -> drawSlot(layout.oldSlotBounds.getValue(index), entry, highlighted = false, old = true, index = index) }
        }
        drawCentered("CURRENT HASH TABLE (Capacity ${step.capacity}):", layout.currentHeadingBounds, OpsOrange, 10, true)
        step.buckets.forEachIndexed { index, entry ->
            val bounds = layout.currentSlotBounds.getValue(index)
            val highlighted = index == step.highlightedIndex || index == step.targetIndex
            if (highlighted) drawCentered("↓", Rect2D(bounds.left, bounds.top - 16f, bounds.width, 14f), if (step.activeOp == HashOpType.DELETE) OpsRed else OpsOrange, 12, true)
            drawSlot(bounds, entry, highlighted)
            drawCentered("[$index]", layout.currentIndexBounds.getValue(index), if (highlighted) OpsOrange else OpsSecondary, 10, true)
        }
        layout.rehashArrow?.let { line ->
            val start = Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx())
            val end = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx())
            val routeY = (layout.rehashRouteY ?: line.end.y).dp.toPx()
            val route = Path().apply {
                moveTo(start.x, start.y)
                lineTo(start.x, routeY)
                lineTo(end.x, routeY)
                lineTo(end.x, end.y)
            }
            drawPath(route, OpsOrange, style = Stroke(2.dp.toPx()))
            val path = Path().apply { moveTo(end.x, end.y); lineTo(end.x - 5.dp.toPx(), end.y - 7.dp.toPx()); lineTo(end.x + 5.dp.toPx(), end.y - 7.dp.toPx()); close() }
            drawPath(path, OpsOrange)
        }
    }
}
