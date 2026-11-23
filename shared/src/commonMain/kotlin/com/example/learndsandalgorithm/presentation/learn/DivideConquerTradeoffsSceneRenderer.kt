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

private val TradeoffCanvasBackground = Color(0xFF10141B)
private val TradeoffPanel = Color(0xFF171D27)
private val TradeoffPanelRaised = Color(0xFF1D2632)
private val TradeoffBorder = Color(0xFF3A4654)
private val TradeoffPrimary = Color(0xFFF3F6FA)
private val TradeoffSecondary = Color(0xFFB8C3D1)
private val TradeoffMuted = Color(0xFF8290A0)
private val TradeoffGreen = Color(0xFF4ADE80)
private val TradeoffGreenFill = Color(0xFF153526)
private val TradeoffBlue = Color(0xFF60A5FA)
private val TradeoffBlueFill = Color(0xFF172D48)
private val TradeoffOrange = Color(0xFFFBBF24)
private val TradeoffOrangeFill = Color(0xFF3D3018)
private val TradeoffRed = Color(0xFFF87171)
private val TradeoffRedFill = Color(0xFF3D2026)
private val TradeoffPurple = Color(0xFFC4B5FD)
private val TradeoffPurpleFill = Color(0xFF2C2544)

@Composable
internal fun DivideConquerTradeoffsSceneRenderer(
    frame: DivideConquerTradeoffsSceneFrame,
    sequence: List<DivideConquerTradeoffsSceneFrame>,
    modifier: Modifier = Modifier
) {
    val layout = remember(sequence) { DivideConquerTradeoffsSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = "divide_conquer_tradeoffs:${frame.meta.source.scenario}")
    val textMeasurer = rememberTextMeasurer()

    VisualizerCanvasViewport(
        modifier = modifier.fillMaxSize(),
        mode = VisualizerCanvasMode.HYBRID,
        state = viewport
    ) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun drawCard(bounds: Rect2D, fill: Color, border: Color, stroke: Float = 1f, radius: Float = 7f) {
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
                centered: Boolean = false,
                minFontSize: Float = 6.5f
            ) {
                if (value.isEmpty() || bounds.width <= 0f || bounds.height <= 0f) return
                var currentSize = fontSize
                var result = textMeasurer.measure(
                    text = value,
                    style = TextStyle(
                        color = color,
                        fontSize = currentSize.sp,
                        lineHeight = (currentSize + 1.5f).sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
                    ),
                    softWrap = true,
                    maxLines = Int.MAX_VALUE,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )
                while (result.didOverflowWidth || result.didOverflowHeight) {
                    currentSize = if (currentSize > minFontSize) {
                        (currentSize - 0.5f).coerceAtLeast(minFontSize)
                    } else {
                        (currentSize - 0.5f).coerceAtLeast(1f)
                    }
                    if (currentSize == 1f && (result.didOverflowWidth || result.didOverflowHeight)) {
                        result = textMeasurer.measure(
                            text = value,
                            style = TextStyle(color = color, fontSize = currentSize.sp, lineHeight = (currentSize + 1.5f).sp,
                                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                            softWrap = true,
                            maxLines = Int.MAX_VALUE,
                            constraints = Constraints(
                                maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                                maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                            )
                        )
                        break
                    }
                    result = textMeasurer.measure(
                        text = value,
                        style = TextStyle(
                            color = color,
                            fontSize = currentSize.sp,
                            lineHeight = (currentSize + 1.5f).sp,
                            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
                        ),
                        softWrap = true,
                        maxLines = Int.MAX_VALUE,
                        constraints = Constraints(
                            maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                            maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                        )
                    )
                }
                check(!result.didOverflowWidth && !result.didOverflowHeight) {
                    "Trade-off scene text does not fit its assigned bounds: $value"
                }
                val left = bounds.left.dp.toPx()
                val top = bounds.top.dp.toPx()
                val width = bounds.width.dp.toPx()
                val height = bounds.height.dp.toPx()
                drawText(
                    result,
                    topLeft = Offset(
                        left + if (centered) (width - result.size.width).coerceAtLeast(0f) / 2f else 5.dp.toPx(),
                        top + if (centered) (height - result.size.height).coerceAtLeast(0f) / 2f else 2.dp.toPx()
                    )
                )
            }

            val nodeColor: (TradeoffNodeStatus) -> Color = { status ->
                when (status) {
                    TradeoffNodeStatus.PENDING -> TradeoffBlue
                    TradeoffNodeStatus.ACTIVE -> TradeoffOrange
                    TradeoffNodeStatus.COMPLETED -> TradeoffGreen
                }
            }
            val nodeFill: (TradeoffNodeStatus) -> Color = { status ->
                when (status) {
                    TradeoffNodeStatus.PENDING -> TradeoffBlueFill
                    TradeoffNodeStatus.ACTIVE -> TradeoffOrangeFill
                    TradeoffNodeStatus.COMPLETED -> TradeoffGreenFill
                }
            }
            var drawTreePanel: (TradeoffTreePanelLayout, List<TradeoffRecursionNode>, StructureSubScene, String) -> Unit = { _, _, _, _ -> }
            var drawRecords: (List<Rect2D>, List<TradeoffStableRecord>, Color, Boolean) -> Unit = { _, _, _, _ -> }
            var drawStabilityPanel: (Rect2D, String, Rect2D, List<Rect2D>, List<TradeoffStableRecord>, Rect2D, List<Rect2D>, List<TradeoffStableRecord>, String, Color) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> }
            var drawSplitScene: (DivideConquerTradeoffsSceneLayoutResult) -> Unit = { }
            var drawComparisonScene: (DivideConquerTradeoffsSceneLayoutResult) -> Unit = { }

            drawRect(TradeoffCanvasBackground)

            drawSplitScene = splitScene@{ sceneLayout ->
                val title = sceneLayout.titleBounds
                drawTextInBounds(
                    "PARTITION SHAPE DETERMINES RECURSION DEPTH · N = ${frame.meta.source.inputN}",
                    title,
                    TradeoffPrimary,
                    12f,
                    bold = true,
                    minFontSize = 8f
                )
                sceneLayout.legendBounds.forEach { (status, bounds) ->
                    val color = nodeColor(status)
                    drawCard(bounds, nodeFill(status), color, radius = 5f)
                    drawTextInBounds(status.name, bounds, color, 7.5f, bold = true, centered = true, minFontSize = 6f)
                }
                val balanced = sceneLayout.balancedTree ?: return@splitScene
                val unbalanced = sceneLayout.unbalancedTree ?: return@splitScene
                drawTreePanel(
                    balanced,
                    frame.meta.balancedNodes,
                    frame.scene.subScenes.single { it.subPaneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID },
                    "BALANCED RECURSION TREE · N/2 + N/2"
                )
                drawTreePanel(
                    unbalanced,
                    frame.meta.unbalancedNodes,
                    frame.scene.subScenes.single { it.subPaneId == DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID },
                    "UNBALANCED RECURSION CHAIN · 1 + (N−1)"
                )
            }

            drawTreePanel = { panel, nodes, scene, title ->
                drawCard(panel.panelBounds, TradeoffPanel, TradeoffBorder)
                drawTextInBounds(title, panel.titleBounds, TradeoffPrimary, 9f, bold = true, minFontSize = 7f)
                val connectionStates = scene.connections.associate { it.connectionKey to it.visualState }
                panel.edges.filter { it.connectionKey in connectionStates }.forEach { edge ->
                    val connectionColor = when (connectionStates[edge.connectionKey]) {
                        ConnectionVisualState.HIGHLIGHTED -> TradeoffOrange
                        ConnectionVisualState.PATH -> TradeoffGreen
                        else -> TradeoffMuted
                    }
                    drawLine(
                        connectionColor,
                        Offset(edge.line.start.x.dp.toPx(), edge.line.start.y.dp.toPx()),
                        Offset(edge.line.end.x.dp.toPx(), edge.line.end.y.dp.toPx()),
                        strokeWidth = 1.1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawTextInBounds(edge.label, edge.labelBounds, connectionColor, 6.4f, centered = true, minFontSize = 5.5f)
                }
                nodes.filter { it.visible }.forEach { node ->
                    val bounds = panel.nodeBounds[node.id] ?: return@forEach
                    val color = nodeColor(node.status)
                    drawCard(bounds, nodeFill(node.status), color, stroke = if (node.status == TradeoffNodeStatus.ACTIVE) 1.8f else 1f, radius = 4f)
                    drawTextInBounds("N=${node.size}", Rect2D(bounds.left, bounds.top + 1f, bounds.width, 11f), color, 7.4f, bold = true, centered = true, minFontSize = 6f)
                    drawTextInBounds(node.rangeLabel, Rect2D(bounds.left, bounds.top + 12f, bounds.width, 10f), TradeoffSecondary, 6.5f, centered = true, minFontSize = 5.5f)
                }
                val n = frame.meta.source.inputN
                val depthText = if (scene.subPaneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID) {
                    "DEPTH O(log N) · ${frame.meta.source.goodTreeDepth ?: 4} edges / ${frame.meta.source.goodTreeDepth?.plus(1) ?: 5} node levels"
                } else {
                    "DEPTH O(N) · ${frame.meta.source.badTreeDepth ?: n} calls / ${((frame.meta.unbalancedNodes.maxOfOrNull { it.depth } ?: 0) + 1)} node levels"
                }
                val workText = if (scene.subPaneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID) {
                    "WORK / LEVEL O(N) · TOTAL O(N log N)"
                } else {
                    "N + … + 1 = ${n * (n + 1) / 2} · TOTAL O(N²)"
                }
                drawTextInBounds(depthText, panel.depthBounds, TradeoffGreen, 8f, bold = true, minFontSize = 6.5f)
                drawTextInBounds(workText, panel.workBounds, TradeoffSecondary, 7.5f, bold = true, minFontSize = 6f)
                val causeText = if (scene.subPaneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID) {
                    "Near-equal splits keep recursion shallow"
                } else {
                    "Sorted input + last pivot → 1 + (N−1)"
                }
                drawTextInBounds(causeText, panel.causeBounds, TradeoffSecondary, 7f, bold = true, minFontSize = 6f)
            }

            drawComparisonScene = { sceneLayout ->
                drawTextInBounds(
                    "MERGE SORT vs QUICK SORT · GUARANTEES, STORAGE, STABILITY",
                    sceneLayout.titleBounds,
                    TradeoffPrimary,
                    13f,
                    bold = true,
                    minFontSize = 9f
                )
                drawTextInBounds(
                    DivideConquerTradeoffsSceneLayout.COMPARISON_MATRIX_HEADING,
                    sceneLayout.comparisonHeaderBounds!!,
                    TradeoffBlue,
                    10f,
                    bold = true,
                    minFontSize = 8f
                )
                val headers = sceneLayout.comparisonColumnHeaderBounds
                listOf("PROPERTY", "MERGE SORT", "QUICK SORT").forEachIndexed { index, value ->
                    val color = if (index == 1) TradeoffGreen else if (index == 2) TradeoffOrange else TradeoffSecondary
                    drawCard(headers[index], TradeoffPanelRaised, TradeoffBorder, radius = 4f)
                    drawTextInBounds(value, headers[index], color, 8.5f, bold = true, centered = true, minFontSize = 7f)
                }
                frame.meta.comparisonRows.forEach { row ->
                    val cells = sceneLayout.comparisonCells.getValue(row.id)
                    val current = frame.meta.activeComparisonSection
                    val activeMerge = current == "merge" && row.id != "practical_tradeoff"
                    val activeQuick = current == "quick" && row.id != "practical_tradeoff"
                    val activeDecision = current == "decision" && row.id == "practical_tradeoff"
                    listOf(row.property, row.mergeSort, row.quickSort).forEachIndexed { index, value ->
                        val active = index == 0 && activeDecision || index == 1 && activeMerge || index == 2 && activeQuick
                        val color = when (index) {
                            0 -> TradeoffPrimary
                            1 -> TradeoffGreen
                            else -> TradeoffOrange
                        }
                        val fill = when {
                            active && index == 1 -> TradeoffGreenFill
                            active && index == 2 -> TradeoffOrangeFill
                            else -> TradeoffPanel
                        }
                        drawCard(cells[index], fill, if (active) color else TradeoffBorder, stroke = if (active) 1.3f else 0.8f, radius = 3f)
                        drawTextInBounds(
                            value,
                            cells[index],
                            color,
                            if (index == 0) 8.2f else 8.5f,
                            bold = index == 0 || active,
                            minFontSize = 6.5f
                        )
                    }
                }
                drawStabilityPanel(
                    sceneLayout.mergeExampleBounds!!,
                    "MERGE SORT · STABLE",
                    sceneLayout.mergeInputLabelBounds!!,
                    sceneLayout.mergeInputRecordBounds,
                    frame.meta.inputRecords,
                    sceneLayout.mergeOutputLabelBounds!!,
                    sceneLayout.mergeOutputRecordBounds,
                    frame.meta.mergeSortedRecords,
                    "STABLE SORTED OUTPUT",
                    TradeoffGreen
                )
                drawTextInBounds("ILLUSTRATIVE AUXILIARY BUFFER · FINAL MERGED CONTENTS · O(N)", sceneLayout.mergeBufferLabelBounds!!, TradeoffBlue, 7.5f, bold = true, minFontSize = 6f)
                drawRecords(sceneLayout.mergeBufferRecordBounds, frame.meta.mergeBufferRecords, TradeoffBlue, true)

                drawStabilityPanel(
                    sceneLayout.quickExampleBounds!!,
                    "QUICK SORT · IN-PLACE PARTITION · UNSTABLE",
                    sceneLayout.quickInputLabelBounds!!,
                    sceneLayout.quickInputRecordBounds,
                    frame.meta.inputRecords,
                    sceneLayout.quickOutputLabelBounds!!,
                    sceneLayout.quickOutputRecordBounds,
                    frame.meta.quickPartitionRecords,
                    "AFTER ONE PARTITION",
                    TradeoffOrange
                )
                drawTextInBounds(
                    "ONE LOMUTO PARTITION ONLY · 5A and 5B swap relative order; array is not fully sorted",
                    sceneLayout.quickDisclaimerBounds!!,
                    TradeoffSecondary,
                    7f,
                    bold = true,
                    minFontSize = 6f
                )
            }

            drawStabilityPanel = { panel, title, inputLabel, inputBounds, inputRecords, outputLabel, outputBounds, outputRecords, outputDescriptor, accent ->
                drawCard(panel, TradeoffPanel, accent.copy(alpha = 0.8f), stroke = 1.2f)
                drawTextInBounds(title, Rect2D(panel.left + 8f, panel.top + 8f, panel.width - 16f, 16f), accent, 8f, bold = true, minFontSize = 6.5f)
                drawTextInBounds("INPUT · 5A, 5B are equal-key records", inputLabel, TradeoffSecondary, 7f, bold = true, minFontSize = 6f)
                drawRecords(inputBounds, inputRecords, accent, false)
                drawTextInBounds(outputDescriptor, outputLabel, TradeoffSecondary, 7f, bold = true, minFontSize = 6f)
                drawRecords(outputBounds, outputRecords, accent, false)
            }

            drawRecords = { bounds, records, accent, compact ->
                bounds.zip(records).forEach { (rect, record) ->
                    val equalKey = record.identity == "record:A" || record.identity == "record:B"
                    drawCard(rect, if (equalKey) accent.copy(alpha = 0.18f) else TradeoffPanelRaised, if (equalKey) TradeoffOrange else TradeoffBorder, stroke = 0.9f, radius = 4f)
                    drawTextInBounds(record.label, rect, if (equalKey) TradeoffOrange else TradeoffPrimary, if (compact) 7f else 8f, bold = true, centered = true, minFontSize = 6f)
                }
            }

            when (frame.meta.source.scenario) {
                TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> drawSplitScene(layout)
                TradeoffScenario.ALGORITHM_COMPARISON -> drawComparisonScene(layout)
            }
        }
    }
}
