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

private val SearchBackground = Color(0xFF13131C)
private val SearchCard = Color(0xFF191925)
private val SearchBorder = Color(0xFF30303E)
private val SearchBlue = Color(0xFF38BDF8)
private val SearchBlueFill = Color(0xFF13263A)
private val SearchOrange = Color(0xFFFB923C)
private val SearchOrangeFill = Color(0xFF2A1C12)
private val SearchGreen = Color(0xFF34D399)
private val SearchGreenFill = Color(0xFF123820)
private val SearchRed = Color(0xFFF87171)
private val SearchRedFill = Color(0xFF35191D)
private val SearchPrimary = Color(0xFFF8FAFC)
private val SearchSecondary = Color(0xFFB8C0CC)
private val SearchMuted = Color(0xFF667085)

@Composable
internal fun RecursiveBinarySearchSceneRenderer(
    frame: RecursiveBinarySearchSceneFrame,
    sequence: List<RecursiveBinarySearchSceneFrame>,
    modifier: Modifier = Modifier
) {
    val layout = remember(sequence) { RecursiveBinarySearchSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = "recursive_binary_search")
    val textMeasurer = rememberTextMeasurer()
    val arrayScene = frame.scene.subScenes.singleOrNull { it.subPaneId == "binary_search_array" }
        ?: error("RecursiveBinarySearchSceneRenderer requires binary_search_array")
    val entitiesByIndex = arrayScene.entities.associateBy { entity ->
        entity.slotKey.substringAfterLast(':').toInt()
    }

    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, viewport) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun text(
                value: String,
                bounds: Rect2D,
                color: Color,
                fontSize: Float,
                bold: Boolean = false,
                minimumFontSize: Float = 8f
            ) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
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

            fun card(bounds: Rect2D, fill: Color, border: Color, radius: Float = 8f, stroke: Float = 1.2f) {
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

            card(layout.headerBounds, SearchBackground, SearchBorder, radius = 9f)
            text("TARGET ${frame.meta.target}", layout.targetBounds, SearchOrange, 11f, bold = true)
            val rangeColor = if (frame.meta.rangeLeft != null && frame.meta.rangeRight != null && frame.meta.rangeLeft > frame.meta.rangeRight) SearchRed else SearchSecondary
            text(RecursiveBinarySearchSceneLayout.rangeText(frame.meta), layout.rangeBounds, rangeColor, 10f, bold = true)

            if (frame.meta.array.isEmpty()) {
                layout.emptyArrayBounds?.let { bounds ->
                    card(bounds, SearchCard, SearchBorder)
                    text("EMPTY ARRAY · NO CELLS TO INDEX", bounds, SearchSecondary, 11f, bold = true)
                }
            } else {
                frame.meta.markerLabels.forEach { (index, labels) ->
                    val bounds = layout.markerBounds[index] ?: error("Missing marker geometry for index $index")
                    val markerColor = if ("MID" in labels) SearchOrange else SearchBlue
                    text(labels.joinToString("/"), bounds, markerColor, 9f, bold = true)
                }
                frame.meta.array.indices.forEach { index ->
                    val entity = entitiesByIndex[index] ?: error("Missing stable array entity for index $index")
                    val bounds = layout.cellBounds[index] ?: error("Missing array cell geometry for index $index")
                    val (fill, border, valueColor) = when {
                        entity.visualState == EntityVisualState.TARGET -> Triple(SearchGreenFill, SearchGreen, SearchPrimary)
                        entity.visualState == EntityVisualState.ACTIVE -> Triple(SearchOrangeFill, SearchOrange, SearchPrimary)
                        entity.visualState == EntityVisualState.VIOLATION -> Triple(Color(0xFF101015), SearchBorder, SearchMuted)
                        frame.meta.activeInterval?.contains(index) == true -> Triple(SearchBlueFill, SearchBlue, SearchPrimary)
                        else -> Triple(SearchBackground, SearchBorder, SearchMuted)
                    }
                    card(bounds, fill, border, radius = 8f, stroke = if (entity.visualState == EntityVisualState.ACTIVE || entity.visualState == EntityVisualState.TARGET) 1.8f else 1.1f)
                    text(entity.dataValue, bounds, valueColor, 16f, bold = true, minimumFontSize = 12f)
                    text("[$index]", layout.indexBounds.getValue(index), if (frame.meta.activeInterval?.contains(index) == true || index == frame.meta.foundIndex) SearchSecondary else SearchMuted, 10f)
                }
            }

            val midpointColor = when (frame.meta.comparison) {
                BinarySearchComparison.EQUAL -> SearchGreen
                null -> SearchSecondary
                else -> SearchOrange
            }
            card(layout.midpointBounds, SearchCard, SearchBorder, radius = 7f)
            text(RecursiveBinarySearchSceneLayout.midpointText(frame.meta), layout.midpointBounds, midpointColor, 10f, bold = frame.meta.comparison != null)

            val stackDescription = if (frame.meta.stackDepth == 0) {
                "RECURSIVE CALL STACK · EMPTY"
            } else {
                "RECURSIVE CALL STACK · depth ${frame.meta.callDepth} · ${frame.meta.stackDepth}/${layout.maximumStackDepth} active"
            }
            text(stackDescription, layout.stackHeaderBounds, SearchBlue, 9f, bold = true)
            layout.stackRowBounds.forEach { (rowIndex, bounds) ->
                val stackCall = frame.meta.callStack.getOrNull(rowIndex)
                val isPending = stackCall == null && frame.meta.calleeCall != null && rowIndex == frame.meta.callStack.size
                when {
                    stackCall != null -> {
                        val isActive = stackCall.callId == frame.meta.activeCallId
                        card(bounds, if (isActive) SearchBlueFill else SearchCard, if (isActive) SearchBlue else SearchBorder, radius = 6f)
                        text(
                            RecursiveBinarySearchSceneLayout.callText(stackCall),
                            Rect2D(bounds.left + 8f, bounds.top, bounds.width - 16f, bounds.height),
                            if (isActive) SearchPrimary else SearchSecondary,
                            10f,
                            bold = isActive
                        )
                    }
                    isPending -> {
                        card(bounds, SearchOrangeFill, SearchOrange, radius = 6f)
                        text(
                            RecursiveBinarySearchSceneLayout.pendingCallText(frame.meta.calleeCall!!),
                            Rect2D(bounds.left + 8f, bounds.top, bounds.width - 16f, bounds.height),
                            SearchOrange,
                            10f,
                            bold = true
                        )
                    }
                    else -> {
                        card(bounds, SearchBackground, SearchBorder, radius = 6f, stroke = 0.8f)
                        val placeholder = if (frame.meta.isTerminal && rowIndex == 0) "ALL CALLS RETURNED" else "—"
                        text(placeholder, bounds, SearchMuted, 9f)
                    }
                }
            }

            val resultColor = when (frame.meta.event) {
                BinarySearchEvent.TARGET_FOUND -> SearchGreen
                BinarySearchEvent.TARGET_NOT_FOUND -> SearchRed
                BinarySearchEvent.RETURN -> if (frame.meta.returnValue == -1) SearchRed else SearchGreen
                else -> SearchSecondary
            }
            val resultFill = when (frame.meta.event) {
                BinarySearchEvent.TARGET_FOUND -> SearchGreenFill
                BinarySearchEvent.TARGET_NOT_FOUND -> SearchRedFill
                BinarySearchEvent.RETURN -> if (frame.meta.returnValue == -1) SearchRedFill else SearchGreenFill
                else -> SearchCard
            }
            card(layout.returnBounds, resultFill, resultColor, radius = 7f)
            text(RecursiveBinarySearchSceneLayout.resultText(frame.meta), layout.returnBounds, resultColor, 10f, bold = true)
            text(RecursiveBinarySearchSceneLayout.COMPLEXITY_TEXT, layout.complexityBounds, SearchGreen, 9f, bold = true)
        }
    }
}
