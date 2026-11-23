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

private val FullBackground = Color(0xFF11131A)
private val FullPanel = Color(0xFF171A23)
private val FullBorder = Color(0xFF343A49)
private val FullOrange = Color(0xFFFB923C)
private val FullOrangeFill = Color(0xFF332315)
private val FullBlue = Color(0xFF38BDF8)
private val FullBlueFill = Color(0xFF142B3C)
private val FullGreen = Color(0xFF34D399)
private val FullGreenFill = Color(0xFF123628)
private val FullPurple = Color(0xFFA78BFA)
private val FullPurpleFill = Color(0xFF28213C)
private val FullPrimary = Color(0xFFF4F6FA)
private val FullSecondary = Color(0xFFB7C0CE)
private val FullMuted = Color(0xFF70798A)

@Composable
internal fun MergeSortFullSceneRenderer(
    frame: MergeSortFullSceneFrame,
    sequence: List<MergeSortFullSceneFrame>,
    modifier: Modifier = Modifier
) {
    val layout = remember(sequence) { MergeSortFullSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = "merge_sort_full")
    val textMeasurer = rememberTextMeasurer()
    val treeScene = frame.scene.subScenes.single { it.subPaneId == MergeSortFullSceneBuilder.TREE_SCENE_ID }
    val workingScene = frame.scene.subScenes.single { it.subPaneId == MergeSortFullSceneBuilder.WORKING_SCENE_ID }
    val bufferScene = frame.scene.subScenes.single { it.subPaneId == MergeSortFullSceneBuilder.BUFFER_SCENE_ID }
    val workingEntities = workingScene.entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }
    val bufferEntities = bufferScene.entities.associateBy { it.slotKey.substringAfterLast(':').toInt() }
    val step = frame.meta.traceStep

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
                minimumFontSize: Float = 7f
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

            fun drawCard(bounds: Rect2D, fill: Color, border: Color, stroke: Float = 1f, radius: Float = 5f) {
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

            fun nodeFill(entity: VisualEntity): Triple<Color, Color, Color> = when {
                entity.visualState == EntityVisualState.TARGET -> Triple(FullGreenFill, FullGreen, FullGreen)
                entity.visualState == EntityVisualState.VISITED -> Triple(FullGreenFill, FullGreen, FullSecondary)
                entity.visualState == EntityVisualState.ACTIVE &&
                    step.event == FullMergeEvent.DIVIDE && entity.elementId == step.activeNodeId -> Triple(FullOrangeFill, FullOrange, FullOrange)
                entity.visualState == EntityVisualState.ACTIVE -> Triple(FullBlueFill, FullBlue, FullPrimary)
                else -> Triple(FullPanel, FullBorder, FullSecondary)
            }

            fun arrayCellColors(entity: VisualEntity, accent: Color): Triple<Color, Color, Color> = when (entity.visualState) {
                EntityVisualState.ACTIVE -> Triple(if (step.event == FullMergeEvent.COMPARE) FullPurpleFill else FullBlueFill, FullPurple, FullPrimary)
                EntityVisualState.TARGET -> if (step.event == FullMergeEvent.COPY_BACK) {
                    Triple(FullPurpleFill, FullPurple, FullPrimary)
                } else Triple(FullGreenFill, FullGreen, FullPrimary)
                EntityVisualState.VISITED -> Triple(FullGreenFill, FullGreen, FullPrimary)
                EntityVisualState.VIOLATION -> Triple(FullBackground, FullBorder, FullMuted)
                EntityVisualState.DEFAULT -> Triple(FullPanel, FullBorder, accent)
            }

            fun drawRun(
                elements: List<FullMergeElement>,
                cellBounds: Map<Int, Rect2D>,
                markerBounds: Map<Int, Rect2D>,
                pointer: Int?,
                source: FullMergeSource
            ) {
                val markerName = if (source == FullMergeSource.LEFT) "i" else "j"
                if (pointer != null) {
                    markerBounds[pointer]?.let {
                        drawTextInBounds(markerName, it, if (source == FullMergeSource.LEFT) FullOrange else FullBlue, 10f, bold = true, minimumFontSize = 9f)
                    }
                }
                if (elements.isEmpty()) {
                    val bounds = if (source == FullMergeSource.LEFT) layout.emptyLeftRunBounds else layout.emptyRightRunBounds
                    bounds?.let { empty ->
                        drawCard(empty, FullPanel, FullBorder)
                        drawTextInBounds("NO ACTIVE RUN", empty, FullMuted, 9f, bold = true, minimumFontSize = 8f)
                    }
                    return
                }
                elements.forEachIndexed { index, element ->
                    val bounds = cellBounds[index] ?: error("Missing ${source.name} run cell $index")
                    val isCompared = step.event == FullMergeEvent.COMPARE &&
                        (if (source == FullMergeSource.LEFT) step.currentComparison?.leftElement?.identity else step.currentComparison?.rightElement?.identity) == element.identity
                    val isSelected = (step.event == FullMergeEvent.WRITE_BUFFER || step.event == FullMergeEvent.FLUSH_REMAINING) &&
                        step.selectedElement?.identity == element.identity
                    val fill = when {
                        isSelected -> if (source == FullMergeSource.LEFT) FullOrangeFill else FullBlueFill
                        isCompared -> FullPurpleFill
                        else -> FullPanel
                    }
                    val border = when {
                        isCompared -> FullPurple
                        isSelected && source == FullMergeSource.LEFT -> FullOrange
                        isSelected -> FullBlue
                        source == FullMergeSource.LEFT -> FullOrange.copy(alpha = 0.65f)
                        else -> FullBlue.copy(alpha = 0.65f)
                    }
                    drawCard(bounds, fill, border, stroke = if (isCompared || isSelected) 1.7f else 1f)
                    drawTextInBounds(element.value.toString(), Rect2D(bounds.left + 1f, bounds.top + 2f, bounds.width - 2f, 24f), FullPrimary, 15f, bold = true, minimumFontSize = 11f)
                    drawTextInBounds("origin #${element.originalIndex}", Rect2D(bounds.left + 1f, bounds.top + 27f, bounds.width - 2f, 13f), FullSecondary, 8.5f, minimumFontSize = 7.5f)
                }
            }

            drawCard(layout.treeRegionBounds, FullBackground, FullBorder, radius = 8f)
            drawCard(layout.detailRegionBounds, FullBackground, FullBorder, radius = 8f)
            drawTextInBounds("RECURSION TREE · [start, end)", layout.treeTitleBounds, FullOrange, 10.5f, bold = true, minimumFontSize = 9f)
            drawTextInBounds(
                if (step.event == FullMergeEvent.COMPLETE) "SORTED RESULT · STABLE MERGE COMPLETE" else "MERGE DETAIL · AUXILIARY BUFFER",
                layout.detailTitleBounds,
                FullGreen,
                10.5f,
                bold = true,
                minimumFontSize = 9f
            )

            treeScene.connections.forEach { connection ->
                val line = layout.connectionLines[connection.connectionKey] ?: return@forEach
                val color = when {
                    connection.visualState == ConnectionVisualState.HIGHLIGHTED && step.event == FullMergeEvent.DIVIDE -> FullOrange
                    connection.visualState == ConnectionVisualState.HIGHLIGHTED -> FullBlue
                    else -> FullBlue.copy(alpha = 0.48f)
                }
                drawLine(
                    color,
                    Offset(line.start.x.dp.toPx(), line.start.y.dp.toPx()),
                    Offset(line.end.x.dp.toPx(), line.end.y.dp.toPx()),
                    strokeWidth = if (connection.visualState == ConnectionVisualState.HIGHLIGHTED) 2.4.dp.toPx() else 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            treeScene.entities.forEach { entity ->
                val bounds = layout.nodeBounds[entity.elementId] ?: return@forEach
                val (fill, border, labelColor) = nodeFill(entity)
                drawCard(bounds, fill, border, stroke = if (entity.visualState == EntityVisualState.ACTIVE || entity.visualState == EntityVisualState.TARGET) 1.7f else 1f)
                drawTextInBounds(entity.dataValue, Rect2D(bounds.left + 2f, bounds.top + 4f, bounds.width - 4f, 18f), FullPrimary, 10f, bold = true, minimumFontSize = 9f)
                drawTextInBounds(entity.badgeLabel.orEmpty(), Rect2D(bounds.left + 2f, bounds.top + 24f, bounds.width - 4f, 14f), labelColor, 8.5f, bold = true, minimumFontSize = 7.5f)
            }

            drawTextInBounds(runLabel(FullMergeSource.LEFT, step), layout.leftRunLabelBounds, FullOrange, 9.5f, bold = true, minimumFontSize = 8f)
            drawTextInBounds(runLabel(FullMergeSource.RIGHT, step), layout.rightRunLabelBounds, FullBlue, 9.5f, bold = true, minimumFontSize = 8f)
            drawRun(
                step.leftRun,
                layout.leftRunCellBounds,
                layout.leftRunMarkerBounds,
                if (step.activeRange != null && step.leftPointer != null) step.leftPointer!! - step.activeRange.start else null,
                FullMergeSource.LEFT
            )
            drawRun(
                step.rightRun,
                layout.rightRunCellBounds,
                layout.rightRunMarkerBounds,
                if (step.splitIndex != null && step.rightPointer != null) step.rightPointer!! - step.splitIndex else null,
                FullMergeSource.RIGHT
            )

            val comparisonAccent = when (step.event) {
                FullMergeEvent.DIVIDE -> FullOrange
                FullMergeEvent.COMPARE -> FullPurple
                FullMergeEvent.COPY_BACK -> FullPurple
                FullMergeEvent.WRITE_BUFFER, FullMergeEvent.FLUSH_REMAINING -> FullGreen
                FullMergeEvent.COMPLETE -> FullGreen
                else -> FullBlue
            }
            drawCard(layout.comparisonBounds, FullPanel, comparisonAccent)
            drawTextInBounds(MergeSortFullSceneLayout.indicatorText(step), layout.comparisonBounds, comparisonAccent, 10.5f, bold = true, minimumFontSize = 9f)

            val bufferLabel = when (step.event) {
                FullMergeEvent.COPY_BACK -> "AUXILIARY BUFFER · COPY BACK ${step.copyBackProgress}/${step.activeRange?.size ?: 0}"
                else -> "AUXILIARY BUFFER · FIXED ${step.auxiliaryBuffer.size} SLOTS"
            }
            drawTextInBounds(bufferLabel, layout.bufferLabelBounds, FullGreen, 9.5f, bold = true, minimumFontSize = 8f)
            val bufferK = step.destinationPointer?.coerceIn(0, step.auxiliaryBuffer.size)
            if (bufferK != null) layout.bufferMarkerBounds[bufferK]?.let { drawTextInBounds("k", it, FullGreen, 10f, bold = true, minimumFontSize = 9f) }
            if (step.auxiliaryBuffer.isEmpty()) {
                layout.emptyBufferBounds?.let { bounds ->
                    drawCard(bounds, FullPanel, FullBorder)
                    drawTextInBounds("NO BUFFER SLOTS · N=0", bounds, FullMuted, 9f, bold = true, minimumFontSize = 8f)
                }
            } else {
                step.auxiliaryBuffer.indices.forEach { index ->
                    val entity = bufferEntities[index] ?: error("Missing fixed auxiliary buffer slot $index")
                    val bounds = layout.bufferCellBounds[index] ?: error("Missing auxiliary buffer geometry at $index")
                    val (fill, border, valueColor) = arrayCellColors(entity, FullGreen)
                    drawCard(bounds, fill, border, stroke = if (entity.visualState == EntityVisualState.TARGET) 1.7f else 1f)
                    drawTextInBounds(if (entity.occurrenceId == null) "·" else entity.dataValue, Rect2D(bounds.left + 1f, bounds.top + 2f, bounds.width - 2f, 24f), valueColor, 15f, bold = true, minimumFontSize = 11f)
                    drawTextInBounds("[$index] ${entity.badgeLabel.orEmpty()}", layout.bufferIndexBounds.getValue(index), if (entity.occurrenceId == null) FullMuted else FullGreen, 8.5f, bold = true, minimumFontSize = 7.5f)
                }
            }

            val workingTitle = when (step.event) {
                FullMergeEvent.COMPLETE -> "CURRENT RESULT · SORTED"
                FullMergeEvent.COPY_BACK -> "WORKING ARRAY · COPY BACK ${step.copyBackProgress}/${step.activeRange?.size ?: 0}"
                else -> "WORKING ARRAY · VALUES MOVE ONLY DURING COPY BACK"
            }
            drawTextInBounds(workingTitle, layout.workingLabelBounds, if (step.event == FullMergeEvent.COMPLETE) FullGreen else FullBlue, 9.5f, bold = true, minimumFontSize = 8f)
            if (step.workingArray.isEmpty()) {
                layout.emptyWorkingBounds?.let { bounds ->
                    drawCard(bounds, FullPanel, FullBorder)
                    drawTextInBounds("EMPTY ARRAY · NO INDEXED CELLS", bounds, FullMuted, 9f, bold = true, minimumFontSize = 8f)
                }
            } else {
                step.workingArray.indices.forEach { index ->
                    val entity = workingEntities[index] ?: error("Missing working array slot $index")
                    val bounds = layout.workingCellBounds[index] ?: error("Missing working array geometry at $index")
                    val accent = when (entity.visualState) {
                        EntityVisualState.TARGET -> FullPurple
                        EntityVisualState.ACTIVE -> FullPurple
                        EntityVisualState.VISITED -> FullGreen
                        else -> FullBlue
                    }
                    val (fill, border, valueColor) = arrayCellColors(entity, accent)
                    drawCard(
                        bounds,
                        fill,
                        border,
                        stroke = when {
                            step.event == FullMergeEvent.COMPLETE -> 2.2f
                            entity.visualState == EntityVisualState.TARGET -> 1.7f
                            else -> 1f
                        }
                    )
                    drawTextInBounds(entity.dataValue, Rect2D(bounds.left + 1f, bounds.top + 2f, bounds.width - 2f, 24f), valueColor, 15f, bold = true, minimumFontSize = 11f)
                    drawTextInBounds("[$index] ${entity.badgeLabel.orEmpty()}", layout.workingIndexBounds.getValue(index), if (step.event == FullMergeEvent.COMPLETE) FullGreen else FullSecondary, 8.5f, bold = true, minimumFontSize = 7.5f)
                }
            }
            drawTextInBounds(frame.footer, layout.footerBounds, FullGreen, 9f, bold = true, minimumFontSize = 8f)
        }
    }

}

private fun runLabel(source: FullMergeSource, step: MergeSortFullTraceStep): String {
    val label = if (source == FullMergeSource.LEFT) "L RUN" else "R RUN"
    val range = step.activeRange
    val split = step.splitIndex
    val pointer = if (source == FullMergeSource.LEFT) step.leftPointer else step.rightPointer
    return if (range != null && split != null && pointer != null) {
        val runRange = if (source == FullMergeSource.LEFT) "[${range.start}..$split)" else "[$split..${range.endExclusive})"
        "$label $runRange ${if (source == FullMergeSource.LEFT) "i" else "j"}=$pointer"
    } else "$label · WAITING"
}
