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
import kotlin.math.sqrt

private val MergeBackground = Color(0xFF13131C)
private val MergeCard = Color(0xFF191925)
private val MergeBorder = Color(0xFF30303E)
private val MergeOrange = Color(0xFFFB923C)
private val MergeOrangeFill = Color(0xFF2A1C12)
private val MergeBlue = Color(0xFF38BDF8)
private val MergeBlueFill = Color(0xFF13263A)
private val MergeGreen = Color(0xFF34D399)
private val MergeGreenFill = Color(0xFF123820)
private val MergePrimary = Color(0xFFF8FAFC)
private val MergeSecondary = Color(0xFFB8C0CC)
private val MergeMuted = Color(0xFF667085)

@Composable
internal fun MergeSortFundamentalsSceneRenderer(
    frame: MergeSortFundamentalsSceneFrame,
    sequence: List<MergeSortFundamentalsSceneFrame>,
    modifier: Modifier = Modifier
) {
    val layout = remember(sequence) { MergeSortFundamentalsSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = "merge_sort_fundamentals")
    val textMeasurer = rememberTextMeasurer()
    val leftScene = frame.scene.subScenes.singleOrNull { it.subPaneId == MergeSortFundamentalsSceneBuilder.LEFT_SCENE_ID }
        ?: error("Merge scene is missing LEFT input")
    val rightScene = frame.scene.subScenes.singleOrNull { it.subPaneId == MergeSortFundamentalsSceneBuilder.RIGHT_SCENE_ID }
        ?: error("Merge scene is missing RIGHT input")
    val outputScene = frame.scene.subScenes.singleOrNull { it.subPaneId == MergeSortFundamentalsSceneBuilder.OUTPUT_SCENE_ID }
        ?: error("Merge scene is missing OUTPUT slots")
    val leftEntities = leftScene.entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }
    val rightEntities = rightScene.entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }
    val outputEntities = outputScene.entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }

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
                fontSize: Float,
                bold: Boolean = false,
                minimumFontSize: Float = 8f
            ) {
                if (value.isEmpty() || bounds.width <= 0f || bounds.height <= 0f) return
                val constraints = Constraints(
                    maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                    maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                )
                var chosenSize = fontSize
                var measured = textMeasurer.measure(
                    value,
                    TextStyle(color = color, fontSize = chosenSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    maxLines = 1,
                    softWrap = false,
                    constraints = constraints
                )
                while ((measured.didOverflowWidth || measured.didOverflowHeight) && chosenSize > minimumFontSize) {
                    chosenSize = (chosenSize - 0.5f).coerceAtLeast(minimumFontSize)
                    measured = textMeasurer.measure(
                        value,
                        TextStyle(color = color, fontSize = chosenSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
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

            fun drawCard(bounds: Rect2D, fill: Color, border: Color, stroke: Float = 1.2f) {
                drawRoundRect(
                    color = fill,
                    topLeft = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                drawRoundRect(
                    color = border,
                    topLeft = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(stroke.dp.toPx())
                )
            }

            fun drawTransferRoute(points: List<Point2D>) {
                if (points.size < 2) return
                val pixelPoints = points.map { Offset(it.x.dp.toPx(), it.y.dp.toPx()) }
                pixelPoints.zipWithNext().forEachIndexed { index, (start, end) ->
                    drawLine(
                        MergeGreen,
                        start,
                        end,
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    if (index == pixelPoints.lastIndex - 1) {
                        val dx = end.x - start.x
                        val dy = end.y - start.y
                        val length = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                        val ux = dx / length
                        val uy = dy / length
                        val head = 6.dp.toPx()
                        val spread = 3.dp.toPx()
                        val baseX = end.x - ux * head
                        val baseY = end.y - uy * head
                        drawLine(
                            MergeGreen,
                            end,
                            Offset(baseX - uy * spread, baseY + ux * spread),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            MergeGreen,
                            end,
                            Offset(baseX + uy * spread, baseY - ux * spread),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            val hasTransfer = outputScene.referenceConnections.any { it.visualState == ConnectionVisualState.HIGHLIGHTED }
            if (hasTransfer) layout.transferRoutes[frame.snapshotIndex]?.let(::drawTransferRoute)

            drawCard(layout.headerBounds, MergeBackground, MergeBorder)
            drawTextInBounds(frame.statusText, layout.headerBounds, MergePrimary, 10f, bold = true)

            val indicatorColor = when (frame.step.event) {
                MergeStepEvent.COMPARE -> MergeOrange
                MergeStepEvent.TAKE_LEFT -> MergeOrange
                MergeStepEvent.TAKE_RIGHT -> MergeBlue
                MergeStepEvent.FLUSH_REMAINING -> MergeGreen
                MergeStepEvent.COMPLETE -> MergeGreen
                MergeStepEvent.START -> MergeSecondary
            }
            drawCard(layout.comparisonBounds, MergeCard, indicatorColor)
            drawTextInBounds(
                MergeSortFundamentalsSceneLayout.indicatorText(frame.step),
                layout.comparisonBounds,
                indicatorColor,
                9f,
                bold = true
            )

            drawTextInBounds(
                MergeSortFundamentalsSceneLayout.inputLabel(MergeSource.LEFT, frame.step),
                layout.leftLabelBounds,
                if (frame.step.leftExhausted) MergeMuted else MergeOrange,
                9f,
                bold = true
            )
            drawTextInBounds(
                MergeSortFundamentalsSceneLayout.inputLabel(MergeSource.RIGHT, frame.step),
                layout.rightLabelBounds,
                if (frame.step.rightExhausted) MergeMuted else MergeBlue,
                9f,
                bold = true
            )
            layout.leftMarkerBounds[frame.step.i]?.let { drawTextInBounds("i", it, MergeOrange, 9f, bold = true) }
            layout.rightMarkerBounds[frame.step.j]?.let { drawTextInBounds("j", it, MergeBlue, 9f, bold = true) }

            if (frame.step.leftArr.isEmpty()) {
                layout.emptyLeftBounds?.let { bounds ->
                    drawCard(bounds, MergeCard, MergeBorder)
                    drawTextInBounds("EMPTY LEFT INPUT", bounds, MergeMuted, 10f, bold = true)
                }
            } else {
                frame.step.leftArr.indices.forEach { index ->
                    val entity = leftEntities[index] ?: error("Missing stable LEFT entity at $index")
                    val bounds = layout.leftCellBounds[index] ?: error("Missing LEFT geometry at $index")
                    val colors = sourceColors(entity.visualState, MergeOrange, MergeOrangeFill)
                    drawCard(bounds, colors.first, colors.second, if (entity.visualState == EntityVisualState.TARGET) 1.8f else 1.1f)
                    drawTextInBounds(entity.dataValue, Rect2D(bounds.left + 2f, bounds.top + 2f, bounds.width - 4f, 23f), colors.third, 15f, bold = true, minimumFontSize = 11f)
                    layout.leftIndexBounds[index]?.let { drawTextInBounds("[$index]", it, if (entity.visualState == EntityVisualState.VISITED) MergeMuted else MergeSecondary, 8f) }
                }
            }
            if (frame.step.rightArr.isEmpty()) {
                layout.emptyRightBounds?.let { bounds ->
                    drawCard(bounds, MergeCard, MergeBorder)
                    drawTextInBounds("EMPTY RIGHT INPUT", bounds, MergeMuted, 10f, bold = true)
                }
            } else {
                frame.step.rightArr.indices.forEach { index ->
                    val entity = rightEntities[index] ?: error("Missing stable RIGHT entity at $index")
                    val bounds = layout.rightCellBounds[index] ?: error("Missing RIGHT geometry at $index")
                    val colors = sourceColors(entity.visualState, MergeBlue, MergeBlueFill)
                    drawCard(bounds, colors.first, colors.second, if (entity.visualState == EntityVisualState.TARGET) 1.8f else 1.1f)
                    drawTextInBounds(entity.dataValue, Rect2D(bounds.left + 2f, bounds.top + 2f, bounds.width - 4f, 23f), colors.third, 15f, bold = true, minimumFontSize = 11f)
                    layout.rightIndexBounds[index]?.let { drawTextInBounds("[$index]", it, if (entity.visualState == EntityVisualState.VISITED) MergeMuted else MergeSecondary, 8f) }
                }
            }

            val totalOutput = frame.step.leftArr.size + frame.step.rightArr.size
            drawTextInBounds("MERGED OUTPUT · k=${frame.step.k}/$totalOutput · O(N)", layout.outputLabelBounds, MergeGreen, 9f, bold = true)
            layout.outputMarkerBounds[frame.step.k]?.let { drawTextInBounds("k", it, MergeGreen, 9f, bold = true) }
            if (totalOutput == 0) {
                layout.emptyOutputBounds?.let { bounds ->
                    drawCard(bounds, MergeCard, MergeBorder)
                    drawTextInBounds("EMPTY MERGED OUTPUT · 0 ELEMENTS", bounds, MergeSecondary, 10f, bold = true)
                }
            } else {
                (0 until totalOutput).forEach { index ->
                    val entity = outputEntities[index] ?: error("Missing fixed OUTPUT slot $index")
                    val bounds = layout.outputCellBounds[index] ?: error("Missing OUTPUT geometry at $index")
                    val isOccupied = entity.occurrenceId != null
                    val fill = when {
                        !isOccupied -> MergeBackground
                        entity.visualState == EntityVisualState.TARGET -> MergeGreenFill
                        else -> MergeCard
                    }
                    val border = when {
                        !isOccupied -> MergeBorder
                        entity.visualState == EntityVisualState.TARGET -> MergeGreen
                        else -> Color(0xFF35624C)
                    }
                    drawCard(bounds, fill, border, if (entity.visualState == EntityVisualState.TARGET) 1.8f else 1f)
                    drawTextInBounds(
                        if (isOccupied) entity.dataValue else "·",
                        Rect2D(bounds.left + 2f, bounds.top + 1f, bounds.width - 4f, 23f),
                        if (isOccupied) MergePrimary else MergeMuted,
                        14f,
                        bold = isOccupied,
                        minimumFontSize = 10f
                    )
                    if (isOccupied) {
                        drawTextInBounds(entity.badgeLabel.orEmpty(), Rect2D(bounds.left + 1f, bounds.top + 25f, bounds.width - 2f, 12f), MergeGreen, 8f, bold = true)
                    }
                    layout.outputIndexBounds[index]?.let { drawTextInBounds("[$index]", it, MergeSecondary, 8f) }
                }
            }
        }
    }
}

private fun sourceColors(
    state: EntityVisualState,
    accent: Color,
    activeFill: Color
): Triple<Color, Color, Color> = when (state) {
    EntityVisualState.ACTIVE -> Triple(activeFill, accent, MergePrimary)
    EntityVisualState.TARGET -> Triple(accent.copy(alpha = 0.28f), accent, MergePrimary)
    EntityVisualState.VISITED -> Triple(Color(0xFF181824), MergeBorder, MergeMuted)
    EntityVisualState.DEFAULT -> Triple(MergeCard, MergeBorder, MergePrimary)
    EntityVisualState.VIOLATION -> Triple(MergeBackground, MergeBorder, MergeMuted)
}
