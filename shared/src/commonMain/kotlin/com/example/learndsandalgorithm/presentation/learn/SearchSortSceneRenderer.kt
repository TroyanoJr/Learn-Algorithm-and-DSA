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

private val SearchSortCell = Color(0xFF13131C)
private val SearchSortBorder = Color(0xFF262634)
private val SearchSortOrange = Color(0xFFFA6E13)
private val SearchSortBlue = Color(0xFF38BDF8)
private val SearchSortGreen = Color(0xFF10B981)
private val SearchSortRed = Color(0xFFEF4444)
private val SearchSortPrimary = Color.White
private val SearchSortSecondary = Color(0xFF9CA3AF)
private val SearchSortEliminated = Color(0xFF4B5563)

@Composable
internal fun SearchSortSceneRenderer(
    frame: SearchSortSceneFrame,
    sequence: List<SearchSortSceneFrame>,
    modifier: Modifier = Modifier,
    viewportKey: Any? = Unit
) {
    val layout = remember(sequence) { SearchSortSceneLayout.calculate(sequence) }
    val state = rememberVisualizerCanvasState(key = viewportKey)
    val measurer = rememberTextMeasurer()
    val maxValue = remember(sequence) { sequence.flatMap { it.values }.maxOrNull()?.coerceAtLeast(1) ?: 1 }
    val entities = frame.scene.subScenes.singleOrNull()?.entities.orEmpty().associateBy {
        it.slotKey.substringAfterLast(':').toInt()
    }

    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, state) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun text(
                value: String,
                bounds: Rect2D,
                color: Color,
                fontSize: Int,
                bold: Boolean = false
            ) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val measured = measurer.measure(
                    text = value,
                    style = TextStyle(color = color, fontSize = fontSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    maxLines = 1,
                    softWrap = false,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )
                drawText(
                    measured,
                    topLeft = Offset(
                        bounds.left.dp.toPx() + (bounds.width.dp.toPx() - measured.size.width) / 2f,
                        bounds.top.dp.toPx() + (bounds.height.dp.toPx() - measured.size.height) / 2f
                    )
                )
            }

            fun colors(index: Int): Triple<Color, Color, Color> {
                val stateAtIndex = entities[index]?.visualState ?: EntityVisualState.DEFAULT
                return when (stateAtIndex) {
                    EntityVisualState.TARGET -> Triple(Color(0xFF092317), SearchSortGreen, SearchSortGreen)
                    EntityVisualState.ACTIVE -> Triple(Color(0xFF2A1C12), SearchSortOrange, SearchSortOrange)
                    EntityVisualState.VISITED -> Triple(Color(0xFF161F1A), SearchSortGreen, SearchSortPrimary)
                    EntityVisualState.VIOLATION -> Triple(Color(0xFF0F0F14), Color(0xFF1E1E26), SearchSortEliminated)
                    EntityVisualState.DEFAULT -> Triple(SearchSortCell, SearchSortBorder, SearchSortPrimary)
                }
            }

            if (frame.visualMode == SearchSortVisualMode.CELLS) {
                val markerLabels = buildMap<Int, MutableList<String>> {
                    fun marker(label: String, index: Int?) {
                        if (index != null) getOrPut(index) { mutableListOf() }.add(label)
                    }
                    marker("L", frame.meta.leftIndex)
                    marker("M", frame.meta.midIndex)
                    marker("R", frame.meta.rightIndex)
                }
                frame.values.indices.forEach { index ->
                    val bounds = layout.cellBounds.getValue(index)
                    val (fill, border, valueColor) = colors(index)
                    drawRoundRect(fill, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(9.dp.toPx()))
                    drawRoundRect(border, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(9.dp.toPx()), style = Stroke(if (index in frame.meta.highlightedIndices || index == frame.meta.foundIndex) 1.5.dp.toPx() else 1.dp.toPx()))
                    text(frame.values[index].toString(), bounds, valueColor, 15, true)
                    text(index.toString(), layout.indexBounds.getValue(index), if (index in frame.meta.highlightedIndices) SearchSortOrange else SearchSortSecondary, 10, index in frame.meta.highlightedIndices)
                    markerLabels[index]?.let { labels ->
                        val pointerBounds = Rect2D(bounds.left, 14f, bounds.width, 20f)
                        val pointerColor = if ("M" in labels) SearchSortOrange else SearchSortBlue
                        text(labels.joinToString(","), pointerBounds, pointerColor, 10, true)
                    }
                }
            } else {
                frame.values.indices.forEach { index ->
                    val track = layout.barTrackBounds.getValue(index)
                    val valueBounds = layout.valueBounds.getValue(index)
                    val (_, border, _) = colors(index)
                    val isSorted = index in frame.meta.sortedIndices
                    val isHighlighted = index in frame.meta.highlightedIndices
                    val barColor = when {
                        isSorted -> SearchSortGreen
                        isHighlighted -> SearchSortOrange
                        else -> Color(0xFF262638)
                    }
                    val barHeight = (track.height * (frame.values[index].toFloat() / maxValue)).coerceAtLeast(10f)
                    val bar = Rect2D(track.left, track.bottom - barHeight, track.width, barHeight)
                    text(frame.values[index].toString(), valueBounds, if (isSorted || isHighlighted) SearchSortPrimary else SearchSortSecondary, 12, true)
                    drawRoundRect(barColor, Offset(bar.left.dp.toPx(), bar.top.dp.toPx()), Size(bar.width.dp.toPx(), bar.height.dp.toPx()), CornerRadius(5.dp.toPx(), 5.dp.toPx()))
                    if (isSorted || isHighlighted) {
                        drawRoundRect(border, Offset(bar.left.dp.toPx(), bar.top.dp.toPx()), Size(bar.width.dp.toPx(), bar.height.dp.toPx()), CornerRadius(5.dp.toPx(), 5.dp.toPx()), style = Stroke(1.5.dp.toPx()))
                    }
                    text(index.toString(), layout.indexBounds.getValue(index), SearchSortSecondary, 9)

                    val markers = buildList {
                        if (index == frame.meta.holeIndex && frame.meta.algorithm == SearchSortAlgorithm.INSERTION_SORT && frame.meta.key != null) add("K")
                        if (index == frame.meta.minimumIndex) add("MIN")
                        if (index == frame.meta.candidateIndex) add("C")
                        if (index == frame.meta.comparisonTargetIndex && frame.meta.algorithm == SearchSortAlgorithm.INSERTION_SORT) add("CMP")
                        if (index == frame.meta.holeIndex) add("H")
                    }
                    if (markers.isNotEmpty()) {
                        text(markers.distinct().joinToString(" / "), SearchSortSceneLayout.markerBounds(frame, layout).getValue(index), SearchSortBlue, 8, true)
                    }
                }
            }
        }
    }
}

internal data class ComparisonCanvasRow(val name: String, val strategy: String, val complexity: String, val positive: Boolean = false)
internal object ComparisonCanvasLayout {
    const val WIDTH_DP = 560f
    const val HEADER_HEIGHT_DP = 34f
    const val ROW_HEIGHT_DP = 94f
    fun rowBounds(index: Int) = Rect2D(8f, HEADER_HEIGHT_DP + index * ROW_HEIGHT_DP, WIDTH_DP - 16f, ROW_HEIGHT_DP - 8f)
    fun strategyBounds(index: Int) = Rect2D(20f, HEADER_HEIGHT_DP + index * ROW_HEIGHT_DP + 28f, WIDTH_DP - 40f, 24f)
    fun complexityBounds(index: Int) = Rect2D(20f, HEADER_HEIGHT_DP + index * ROW_HEIGHT_DP + 54f, WIDTH_DP - 40f, 22f)
    fun sceneHeight(rowCount: Int) = HEADER_HEIGHT_DP + rowCount * ROW_HEIGHT_DP + 12f
}

@Composable
internal fun ComparisonCanvas(
    title: String,
    rows: List<ComparisonCanvasRow>,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val measurer = rememberTextMeasurer()
    val viewportState = rememberVisualizerCanvasState(key = title)
    val width = ComparisonCanvasLayout.WIDTH_DP
    val rowHeight = ComparisonCanvasLayout.ROW_HEIGHT_DP
    val headerHeight = ComparisonCanvasLayout.HEADER_HEIGHT_DP
    val height = ComparisonCanvasLayout.sceneHeight(rows.size)
    VisualizerCanvasViewport(modifier.fillMaxWidth().height(height.dp), VisualizerCanvasMode.HYBRID, viewportState) {
      Canvas(Modifier.size(width.dp, height.dp)) {
        fun drawLabel(value: String, x: Float, y: Float, maxWidth: Float, color: Color, size: Int, bold: Boolean = false) {
            val measured = measurer.measure(
                value,
                TextStyle(color = color, fontSize = size.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
            maxLines = Int.MAX_VALUE,
            softWrap = true,
                constraints = Constraints(maxWidth = maxWidth.dp.roundToPx().coerceAtLeast(1))
            )
            drawText(measured, topLeft = Offset(x.dp.toPx(), y.dp.toPx()))
        }
        drawLabel(title, 12f, 10f, 330f, SearchSortOrange, 10, true)
        badge?.let { drawLabel(it, 400f, 10f, 148f, SearchSortBlue, 10, true) }
        rows.forEachIndexed { index, row ->
            val rowBounds = ComparisonCanvasLayout.rowBounds(index)
            val top = rowBounds.top
            drawRoundRect(SearchSortCell, Offset(rowBounds.left.dp.toPx(), rowBounds.top.dp.toPx()), Size(rowBounds.width.dp.toPx(), rowBounds.height.dp.toPx()), CornerRadius(9.dp.toPx()))
            drawLabel(row.name, 20f, top + 8f, 190f, SearchSortPrimary, 13, true)
            drawLabel(row.strategy, 20f, top + 28f, 520f, SearchSortSecondary, 10)
            drawLabel(row.complexity, 20f, top + 54f, 520f, if (row.positive) SearchSortGreen else SearchSortOrange, 10, true)
        }
      }
    }
}
