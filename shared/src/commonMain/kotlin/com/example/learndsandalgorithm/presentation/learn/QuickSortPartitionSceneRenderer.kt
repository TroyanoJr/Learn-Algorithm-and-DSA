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

private val QuickCanvasBackground = Color(0xFF11131A)
private val QuickPanel = Color(0xFF171A23)
private val QuickBorder = Color(0xFF343A49)
private val QuickOrange = Color(0xFFFB923C)
private val QuickOrangeFill = Color(0xFF3A2714)
private val QuickGreen = Color(0xFF34D399)
private val QuickGreenFill = Color(0xFF123628)
private val QuickRed = Color(0xFFF87171)
private val QuickRedFill = Color(0xFF3B1E24)
private val QuickBlue = Color(0xFF38BDF8)
private val QuickBlueFill = Color(0xFF142B3C)
private val QuickPurple = Color(0xFFA78BFA)
private val QuickPurpleFill = Color(0xFF28213C)
private val QuickPrimary = Color(0xFFF4F6FA)
private val QuickSecondary = Color(0xFFB7C0CE)
private val QuickMuted = Color(0xFF70798A)
private val QuickSwap = Color(0xFFFBBF24)

@Composable
internal fun QuickSortPartitionSceneRenderer(
    frame: QuickSortPartitionSceneFrame,
    sequence: List<QuickSortPartitionSceneFrame>,
    modifier: Modifier = Modifier
) {
    val layout = remember(sequence) { QuickSortPartitionSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = "quick_sort_partition")
    val textMeasurer = rememberTextMeasurer()
    val stepLayout = layout.stepLayouts.getValue(frame.scene.stepIndex)
    val meta = frame.meta

    VisualizerCanvasViewport(
        modifier = modifier.fillMaxSize(),
        mode = VisualizerCanvasMode.HYBRID,
        state = viewport
    ) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun drawCard(bounds: Rect2D, fill: Color, border: Color, stroke: Float = 1f, radius: Float = 6f) {
                drawRoundRect(
                    fill,
                    Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    CornerRadius(radius.dp.toPx())
                )
                drawRoundRect(
                    border,
                    Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    CornerRadius(radius.dp.toPx()),
                    style = Stroke(stroke.dp.toPx())
                )
            }

            fun drawTextInBounds(
                value: String,
                bounds: Rect2D,
                color: Color,
                fontSize: Float,
                bold: Boolean = false,
                minimumFontSize: Float = 7.5f
            ) {
                if (value.isEmpty() || bounds.width <= 0f || bounds.height <= 0f) return
                val constraints = Constraints(
                    maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                    maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                )
                var currentSize = fontSize
                var measured = textMeasurer.measure(
                    text = value,
                    style = TextStyle(
                        color = color,
                        fontSize = currentSize.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
                    ),
                    maxLines = 1,
                    softWrap = false,
                    constraints = constraints
                )
                while ((measured.didOverflowWidth || measured.didOverflowHeight) && currentSize > minimumFontSize) {
                    currentSize = (currentSize - 0.5f).coerceAtLeast(minimumFontSize)
                    measured = textMeasurer.measure(
                        text = value,
                        style = TextStyle(
                            color = color,
                            fontSize = currentSize.sp,
                            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
                        ),
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

            fun zoneColor(region: QuickPartitionRegion): Pair<Color, Color> = when (region) {
                QuickPartitionRegion.LESS_EQUAL -> QuickGreen to QuickGreenFill
                QuickPartitionRegion.PROCESSED_GREATER -> QuickRed to QuickRedFill
                QuickPartitionRegion.CURRENT_COMPARISON -> QuickPurple to QuickPurpleFill
                QuickPartitionRegion.UNPROCESSED -> QuickBlue to QuickBlueFill
                QuickPartitionRegion.PIVOT -> QuickOrange to QuickOrangeFill
            }

            drawRect(QuickCanvasBackground)
            drawTextInBounds(
                if (meta.isTerminal) "LOMUTO PARTITION RESULT · PIVOT FIXED, NOT A FULL SORT"
                else "QUICK SORT · ONE LOMUTO PARTITION PASS",
                layout.titleBounds,
                QuickPrimary,
                13f,
                bold = true,
                minimumFontSize = 10f
            )
            val pivotInfo = if (meta.finalPivotPosition != null) {
                "PIVOT ${meta.pivotValue} · origin index ${meta.pivotOriginIndex} → final index ${meta.finalPivotPosition}"
            } else {
                "PIVOT ${meta.pivotValue} · original array index ${meta.pivotOriginIndex} · values through i satisfy ≤ pivot"
            }
            drawTextInBounds(pivotInfo, layout.pivotInfoBounds, QuickOrange, 10.5f, bold = true, minimumFontSize = 8f)

            val legendLabels = mapOf(
                QuickPartitionRegion.LESS_EQUAL to "≤ pivot",
                QuickPartitionRegion.PROCESSED_GREATER to "processed > pivot",
                QuickPartitionRegion.CURRENT_COMPARISON to "current compare",
                QuickPartitionRegion.UNPROCESSED to "unprocessed",
                QuickPartitionRegion.PIVOT to "pivot element"
            )
            QuickPartitionRegion.entries.forEach { region ->
                val bounds = layout.legendBounds.getValue(region)
                val (color, fill) = zoneColor(region)
                drawCard(bounds, fill, color, stroke = 1f, radius = 4f)
                drawTextInBounds(legendLabels.getValue(region), bounds, color, 8.5f, bold = true, minimumFontSize = 7.5f)
            }

            stepLayout.regionBounds.forEach { (region, bounds) ->
                val (color, fill) = zoneColor(region)
                drawRoundRect(
                    fill.copy(alpha = 0.8f),
                    Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    CornerRadius(3.dp.toPx())
                )
                drawRoundRect(
                    color.copy(alpha = 0.9f),
                    Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()),
                    Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()),
                    CornerRadius(3.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }

            stepLayout.partitionBoundary?.let { line ->
                drawLine(
                    QuickOrange,
                    Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx()),
                    Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            layout.arrayCellBounds.forEach { (index, bounds) ->
                val isPivot = index == meta.pivotPosition
                val isSwapped = index in stepLayout.swapCellBounds
                val isCompared = index == meta.activeComparisonIndex
                val fill = when {
                    isPivot -> QuickOrangeFill
                    isSwapped -> QuickSwap.copy(alpha = 0.2f)
                    isCompared -> QuickPurpleFill
                    index in meta.lessEqualIndices -> QuickGreenFill
                    index in meta.processedGreaterIndices -> QuickRedFill
                    index in meta.unprocessedIndices -> QuickBlueFill
                    else -> QuickPanel
                }
                val border = when {
                    isPivot -> QuickOrange
                    isSwapped -> QuickSwap
                    isCompared -> QuickPurple
                    index in meta.lessEqualIndices -> QuickGreen.copy(alpha = 0.8f)
                    index in meta.processedGreaterIndices -> QuickRed.copy(alpha = 0.8f)
                    index in meta.unprocessedIndices -> QuickBlue.copy(alpha = 0.72f)
                    else -> QuickBorder
                }
                drawCard(bounds, fill, border, stroke = if (isPivot || isSwapped || isCompared) 2f else 1f)
                val entity = frame.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }
                    .entities.single { it.slotKey == "array_slot:$index" }
                drawTextInBounds(entity.dataValue, layout.arrayValueBounds.getValue(index), QuickPrimary, 17f, bold = true, minimumFontSize = 10f)
                drawTextInBounds("[$index]", layout.arrayIndexBounds.getValue(index), QuickSecondary, 10f, bold = true, minimumFontSize = 8f)
            }

            fun drawPointer(label: String, bounds: Rect2D, line: LineSegment2D, color: Color, isSentinel: Boolean = false) {
                drawLine(
                    color,
                    Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx()),
                    Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx()),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color, radius = 2.4.dp.toPx(), center = Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx()))
                drawCard(bounds, if (isSentinel) QuickPanel else color.copy(alpha = 0.16f), color, radius = 4f)
                drawTextInBounds(label, bounds, color, 9.5f, bold = true, minimumFontSize = 8f)
            }
            drawPointer("i = ${meta.i}", stepLayout.iPointerBounds, stepLayout.iPointerLine, QuickGreen, isSentinel = meta.i < 0)
            drawPointer("j = ${meta.j}", stepLayout.jPointerBounds, stepLayout.jPointerLine, QuickBlue)

            stepLayout.partitionBoundaryLabelBounds?.let { bounds ->
                drawTextInBounds("PARTITION BOUNDARY", bounds, QuickOrange, 9f, bold = true, minimumFontSize = 8f)
            }

            drawCard(layout.arrayRegionBounds, Color.Transparent, QuickBorder.copy(alpha = 0.7f), stroke = 1f, radius = 7f)
            val resultText = when {
                meta.isTerminal -> "LEFT ≤ ${meta.pivotValue}  ·  PIVOT FIXED  ·  RIGHT > ${meta.pivotValue}"
                meta.event == QuickPartitionEvent.SCAN_COMPARE && meta.activeComparisonIndex != null -> {
                    val relation = if (meta.comparisonPassed == true) "PASS" else "FAIL"
                    "COMPARE arr[${meta.j}]=${meta.comparisonValue} ≤ pivot ${meta.pivotValue}  ·  $relation"
                }
                meta.event == QuickPartitionEvent.SCAN_COMPARE && meta.comparisonPassed == true ->
                    "PASS: ${meta.comparisonValue} ≤ ${meta.pivotValue}  ·  advance i and include j in the left region"
                meta.event == QuickPartitionEvent.SWAP_ELEMENTS ->
                    "PASS: ${meta.comparisonValue} ≤ ${meta.pivotValue}  ·  swap arr[${meta.swappedIndices?.first}] ↔ arr[${meta.swappedIndices?.second}]"
                meta.event == QuickPartitionEvent.PLACE_PIVOT ->
                    "PLACE pivot from index ${meta.pivotOriginIndex} at final index ${meta.finalPivotPosition}"
                else -> "LOMUTO INVARIANT: [low..i] ≤ pivot · j scans candidates before the pivot"
            }
            val resultColor = when {
                meta.comparisonPassed == false -> QuickRed
                meta.comparisonPassed == true && !meta.isTerminal -> QuickGreen
                meta.isTerminal || meta.event == QuickPartitionEvent.PLACE_PIVOT -> QuickOrange
                else -> QuickSecondary
            }
            drawCard(layout.comparisonResultBounds, QuickPanel, resultColor, stroke = 1.3f, radius = 5f)
            drawTextInBounds(resultText, layout.comparisonResultBounds, resultColor, 11f, bold = true, minimumFontSize = 8f)
        }
    }
}
