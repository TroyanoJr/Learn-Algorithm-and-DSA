package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ArrayCanvasCell = Color(0xFF13131C)
private val ArrayCanvasEmpty = Color(0xFF101015)
private val ArrayCanvasBorder = Color(0xFF262634)
private val ArrayCanvasOrange = Color(0xFFFA6E13)
private val ArrayCanvasBlue = Color(0xFF38BDF8)
private val ArrayCanvasPrimary = Color.White
private val ArrayCanvasSecondary = Color(0xFF9CA3AF)

@Composable
internal fun ArraySceneRenderer(
    frame: ArraySceneFrame,
    sequence: List<ArraySceneFrame>,
    modifier: Modifier = Modifier,
    naturalHeight: Boolean = false,
    viewportKey: Any? = Unit,
    viewportState: VisualizerCanvasState? = null
) {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val values = remember(sequence) {
        sequence.flatMap { it.scene.subScenes }.flatMap { it.entities }.map { it.dataValue }.distinct()
    }
    val maxIndex = remember(sequence) {
        sequence.flatMap { it.regions }.maxOfOrNull { it.layoutSlotCount - 1 }?.coerceAtLeast(0) ?: 0
    }
    val headerStrings = remember(sequence) { sequence.flatMap { it.regions }.mapNotNull { it.headerText } }
    val continuationStrings = remember(sequence) {
        sequence.flatMap { candidate ->
            candidate.regions.flatMap { region ->
                val columns = region.columns.coerceAtLeast(1)
                (columns until region.layoutSlotCount step columns).map { "Continue at index $it ↓" }
            }
        }.distinct()
    }
    val metrics = with(density) {
        val valueSize = values.map {
            measurer.measure(it, TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)).size
        }.maxByOrNull { it.width }
        val indexSize = measurer.measure(maxIndex.toString(), TextStyle(fontSize = 11.sp)).size
        val headerSizes = headerStrings.map {
            measurer.measure(it, TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)).size
        }
        val continuationSizes = continuationStrings.map {
            measurer.measure(it, TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)).size
        }
        listOf(
            valueSize?.width?.toDp()?.value ?: 24f,
            valueSize?.height?.toDp()?.value ?: 18f,
            indexSize.width.toDp().value,
            indexSize.height.toDp().value,
            headerSizes.maxOfOrNull { it.width.toDp().value } ?: 150f,
            headerSizes.maxOfOrNull { it.height.toDp().value } ?: 16f,
            continuationSizes.maxOfOrNull { it.width.toDp().value } ?: 105f,
            continuationSizes.maxOfOrNull { it.height.toDp().value } ?: 14f
        )
    }
    val layout = remember(sequence, metrics) {
        ArraySceneLayout.calculate(
            sequence,
            valueTextWidthDp = metrics[0],
            valueTextHeightDp = metrics[1],
            indexTextWidthDp = metrics[2],
            indexTextHeightDp = metrics[3],
            headerTextWidthDp = metrics[4],
            headerTextHeightDp = metrics[5],
            continuationTextWidthDp = metrics[6],
            continuationTextHeightDp = metrics[7]
        )
    }
    val defaultState = rememberVisualizerCanvasState(key = viewportKey)
    val state = viewportState ?: defaultState
    val viewportModifier = if (naturalHeight) {
        modifier.fillMaxWidth().height(layout.requiredSceneHeight.dp)
    } else {
        modifier.fillMaxSize()
    }

    VisualizerCanvasViewport(viewportModifier, VisualizerCanvasMode.HYBRID, state) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun drawTextIn(
                text: String,
                bounds: Rect2D,
                color: Color,
                fontSize: Int,
                bold: Boolean = false,
                noWrap: Boolean = false
            ) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val textLayout = measurer.measure(
                    text = text,
                    style = TextStyle(color = color, fontSize = fontSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    softWrap = !noWrap,
                    maxLines = if (noWrap) 1 else Int.MAX_VALUE,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        bounds.left.dp.toPx() + (bounds.width.dp.toPx() - textLayout.size.width) / 2f,
                        bounds.top.dp.toPx() + (bounds.height.dp.toPx() - textLayout.size.height) / 2f
                    )
                )
            }

            fun drawCell(bounds: Rect2D, highlighted: Boolean, empty: Boolean) {
                val left = bounds.left.dp.toPx()
                val top = bounds.top.dp.toPx()
                val width = bounds.width.dp.toPx()
                val height = bounds.height.dp.toPx()
                val fill = when {
                    highlighted -> Color(0xFF2A1C12)
                    empty -> ArrayCanvasEmpty
                    else -> ArrayCanvasCell
                }
                drawRoundRect(fill, Offset(left, top), Size(width, height), CornerRadius(8.dp.toPx()))
                drawRoundRect(
                    if (highlighted) ArrayCanvasOrange else ArrayCanvasBorder,
                    Offset(left, top),
                    Size(width, height),
                    CornerRadius(8.dp.toPx()),
                    style = Stroke(if (highlighted) 2.dp.toPx() else 1.dp.toPx())
                )
            }

            frame.regions.forEach { region ->
                if (!region.visible) return@forEach
                val regionLayout = layout.regionLayouts[region.key] ?: return@forEach
                val pane = frame.scene.subScenes.firstOrNull { it.subPaneId == region.key }
                val entitiesByIndex = pane?.entities.orEmpty().associateBy {
                    it.slotKey.substringAfterLast(':').toIntOrNull()
                }
                region.headerText?.let { text ->
                    drawTextIn(text, regionLayout.headerBounds, ArrayCanvasSecondary, 12, bold = true)
                }
                regionLayout.continuationBounds.forEach { (startIndex, bounds) ->
                    if (startIndex < region.slotCount) {
                        drawTextIn("Continue at index $startIndex ↓", bounds, ArrayCanvasBlue, 11, bold = true)
                    }
                }
                for (index in 0 until minOf(region.slotCount, region.layoutSlotCount)) {
                    val cellBounds = regionLayout.cellBounds[index] ?: continue
                    val entity = entitiesByIndex[index]
                    val highlighted = index in region.highlightedIndices || entity?.visualState == EntityVisualState.ACTIVE
                    drawCell(cellBounds, highlighted, entity == null)
                    drawTextIn(
                        entity?.dataValue ?: "·",
                        cellBounds.copy(left = cellBounds.left + 2f, top = cellBounds.top + 2f, width = cellBounds.width - 4f, height = cellBounds.height - 4f),
                        when {
                            highlighted -> ArrayCanvasOrange
                            entity == null -> ArrayCanvasSecondary
                            else -> ArrayCanvasPrimary
                        },
                        15,
                        bold = true,
                        noWrap = true
                    )
                    val indexBounds = regionLayout.indexBounds[index] ?: continue
                    drawTextIn(
                        index.toString(),
                        indexBounds,
                        if (highlighted) ArrayCanvasOrange else ArrayCanvasSecondary,
                        11,
                        bold = highlighted,
                        noWrap = true
                    )
                }
            }
        }
    }
}
