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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val IrBackground = Color(0xFF13131C)
private val IrPanel = Color(0xFF181824)
private val IrBorder = Color(0xFF30303E)
private val IrOrange = Color(0xFFFA6E13)
private val IrBlue = Color(0xFF38BDF8)
private val IrGreen = Color(0xFF10B981)
private val IrRed = Color(0xFFEF4444)
private val IrPrimary = Color.White
private val IrSecondary = Color(0xFF9CA3AF)

@Composable
internal fun IterationRecursionSceneRenderer(
    frame: IterationRecursionSceneFrame,
    sequence: List<IterationRecursionSceneFrame>,
    viewportKey: Any,
    modifier: Modifier = Modifier
) {
    val layout = remember(sequence) { IterationRecursionSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = viewportKey)
    val measurer = rememberTextMeasurer()

    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, viewport) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun card(bounds: Rect2D, fill: Color = IrBackground, border: Color = IrBorder, radius: Float = 8f, stroke: Float = 1f) {
                drawRoundRect(fill, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()))
                drawRoundRect(border, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()), style = Stroke(stroke.dp.toPx()))
            }
            fun text(value: String, bounds: Rect2D, color: Color = IrPrimary, size: Int = 10, bold: Boolean = false, maxLines: Int = 1, centered: Boolean = false) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val style = TextStyle(color = color, fontSize = size.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
                val boundsWidthPx = bounds.width.dp.roundToPx().coerceAtLeast(1)
                val boundsHeightPx = bounds.height.dp.roundToPx().coerceAtLeast(1)
                val preferred = measurer.measure(
                    value,
                    style,
                    maxLines = maxLines,
                    softWrap = maxLines > 1,
                    constraints = Constraints(
                        maxWidth = boundsWidthPx,
                        maxHeight = boundsHeightPx
                    )
                )
                val measured = if (preferred.hasVisualOverflow) {
                    measurer.measure(
                        value,
                        style,
                        maxLines = Int.MAX_VALUE,
                        softWrap = true,
                        constraints = Constraints(maxWidth = boundsWidthPx)
                    )
                } else {
                    preferred
                }
                val fallbackScale = if (preferred.hasVisualOverflow) {
                    IterationRecursionSceneLayout.textFallbackScale(
                        contentWidth = measured.size.width.toFloat(),
                        contentHeight = measured.size.height.toFloat(),
                        availableWidth = boundsWidthPx.toFloat(),
                        availableHeight = boundsHeightPx.toFloat()
                    )
                } else {
                    1f
                }
                val drawnWidth = measured.size.width * fallbackScale
                val drawnHeight = measured.size.height * fallbackScale
                val left = bounds.left.dp.toPx() + if (centered) (boundsWidthPx - drawnWidth) / 2f else 0f
                val top = bounds.top.dp.toPx() + (boundsHeightPx - drawnHeight) / 2f
                if (fallbackScale < 1f) {
                    withTransform({
                        translate(left, top)
                        scale(fallbackScale, fallbackScale, Offset.Zero)
                    }) {
                        drawText(measured, topLeft = Offset.Zero)
                    }
                } else {
                    drawText(measured, topLeft = Offset(left, top))
                }
            }
            fun arrow(connector: SceneConnectorGeometry, color: Color = IrBlue) {
                val from = Offset(connector.fromX.dp.toPx(), connector.fromY.dp.toPx())
                val to = Offset(connector.toX.dp.toPx(), connector.toY.dp.toPx())
                drawLine(color, from, to, 1.7.dp.toPx(), cap = StrokeCap.Round)
                val angle = atan2(to.y - from.y, to.x - from.x)
                val length = 6.dp.toPx()
                val left = Offset(to.x - length * cos(angle - 0.55f), to.y - length * sin(angle - 0.55f))
                val right = Offset(to.x - length * cos(angle + 0.55f), to.y - length * sin(angle + 0.55f))
                drawPath(Path().apply { moveTo(to.x, to.y); lineTo(left.x, left.y); lineTo(right.x, right.y); close() }, color)
            }
            fun entityCard(bounds: Rect2D, entity: VisualEntity, label: String = entity.dataValue) {
                val fill = when (entity.visualState) {
                    EntityVisualState.ACTIVE -> Color(0xFF2A1C12)
                    EntityVisualState.VISITED, EntityVisualState.TARGET -> Color(0xFF123820)
                    EntityVisualState.VIOLATION -> Color(0xFF381212)
                    EntityVisualState.DEFAULT -> IrPanel
                }
                val border = when (entity.visualState) {
                    EntityVisualState.ACTIVE -> IrOrange
                    EntityVisualState.VISITED, EntityVisualState.TARGET -> IrGreen
                    EntityVisualState.VIOLATION -> IrRed
                    EntityVisualState.DEFAULT -> IrBorder
                }
                card(bounds, fill, border)
                if (label.isNotEmpty()) {
                    text(label, bounds, if (entity.visualState == EntityVisualState.DEFAULT) IrSecondary else IrPrimary, 10, true, centered = true)
                }
            }

            when (frame.meta.lesson) {
                IterationRecursionLesson.ITERATION_FUNDAMENTALS -> renderIteration(frame, layout, ::card, ::text, ::arrow, ::entityCard)
                IterationRecursionLesson.NESTED_ITERATION -> renderNested(frame, layout, ::card, ::text, ::entityCard)
                IterationRecursionLesson.RECURSION_FUNDAMENTALS -> renderRecursion(frame, layout, ::card, ::text, ::arrow, ::entityCard)
                IterationRecursionLesson.RECURSION_TRADEOFFS -> renderTradeoffs(frame, sequence, layout, ::card, ::text, ::arrow, ::entityCard)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderIteration(
    frame: IterationRecursionSceneFrame,
    layout: IterationRecursionSceneLayoutResult,
    card: (Rect2D, Color, Color, Float, Float) -> Unit,
    text: (String, Rect2D, Color, Int, Boolean, Int, Boolean) -> Unit,
    arrow: (SceneConnectorGeometry, Color) -> Unit,
    entityCard: (Rect2D, VisualEntity, String) -> Unit
) {
    val meta = frame.meta
    card(layout.boundsFor("header"), IrBackground, IrBorder, 8f, 1f)
    text("ITERATION FUNDAMENTALS · SUM 1 THROUGH ${meta.limit}", layout.boundsFor("header_title"), IrOrange, 10, true, 1, false)
    text("i = ${meta.i}  ·  limit = ${meta.limit}  ·  accumulated total = ${meta.total}", layout.boundsFor("header_status"), IrPrimary, 13, true, 2, false)
    val activePhase = when (meta.action) { "CHECK_CONDITION" -> 0; "EXECUTE_BODY" -> 1; "UPDATE_STATE" -> 2; else -> -1 }
    listOf("CHECK CONDITION", "EXECUTE BODY", "UPDATE STATE").forEachIndexed { index, label ->
        val active = index == activePhase
        card(layout.boundsFor("phase:$index"), if (active) Color(0xFF2A1C12) else IrPanel, if (active) IrOrange else IrBorder, 7f, 1f)
        text(label, layout.boundsFor("phase:$index"), if (active) IrOrange else IrSecondary, 9, true, 1, true)
    }
    layout.connectors.forEach { arrow(it, if (activePhase >= 0) IrBlue else IrBorder) }
    card(layout.boundsFor("variables"), IrBackground, if (meta.action == "TERMINATED") IrGreen else IrBorder, 8f, 1f)
    val condition = when (meta.action) {
        "INIT" -> "Initialized: total = 0, i = 1 · no loop phase has run yet"
        "TERMINATED" -> "Condition: ${meta.i} ≤ ${meta.limit} is FALSE · final result = ${meta.total}"
        "CHECK_CONDITION" -> "Condition: ${meta.i} ≤ ${meta.limit} is TRUE · accumulator unchanged"
        "EXECUTE_BODY" -> "Body: total += ${meta.i} · total is now ${meta.total}"
        else -> "Update: i++ · next i = ${meta.i}"
    }
    text(condition, layout.boundsFor("variables_text"), if (meta.action == "TERMINATED") IrGreen else IrPrimary, 10, true, 2, false)
    val entities = frame.scene.subScenes.first().entities.associateBy { it.visualKey }
    (1..meta.limit).forEach { pass ->
        text("PASS $pass", layout.boundsFor("iteration:label:$pass"), if (pass == meta.passNumber && !meta.isTerminal) IrOrange else IrSecondary, 8, true, 1, true)
        entities["iteration:pass:$pass"]?.let { entity -> entityCard(layout.boundsFor(entity.visualKey), entity, entity.dataValue) }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderNested(
    frame: IterationRecursionSceneFrame,
    layout: IterationRecursionSceneLayoutResult,
    card: (Rect2D, Color, Color, Float, Float) -> Unit,
    text: (String, Rect2D, Color, Int, Boolean, Int, Boolean) -> Unit,
    entityCard: (Rect2D, VisualEntity, String) -> Unit
) {
    val meta = frame.meta
    card(layout.boundsFor("header"), IrBackground, IrBorder, 8f, 1f)
    text("NESTED ITERATION · ${meta.outerLimit} × ${meta.innerLimit} COORDINATE MATRIX", layout.boundsFor("header_title"), IrOrange, 10, true, 1, false)
    text("outer i = ${meta.i} / ${meta.outerLimit}  ·  inner j = ${meta.j} / ${meta.innerLimit}  ·  generated ${meta.total} / ${meta.outerLimit * meta.innerLimit}", layout.boundsFor("header_status"), IrPrimary, 11, true, 2, false)
    val reset = meta.action == "OUTER_PASS"
    card(layout.boundsFor("reset"), if (reset) Color(0xFF2A1C12) else IrPanel, if (reset) IrOrange else IrBorder, 7f, 1f)
    text(if (reset) "OUTER PASS i = ${meta.i}  ·  j ← 1" else "Traversal: row i = ${meta.i}, column j = ${meta.j}", layout.boundsFor("reset"), if (reset) IrOrange else IrBlue, 10, true, 1, true)
    (1..meta.innerLimit).forEach { column -> text("j = $column", layout.boundsFor("column:$column"), if (column == meta.j) IrBlue else IrSecondary, 8, true, 1, true) }
    val entities = frame.scene.subScenes.first().entities.associateBy { it.visualKey }
    (1..meta.outerLimit).forEach { row ->
        val rowActive = row == meta.i && meta.action != "FINISHED"
        card(layout.boundsFor("row:$row"), if (rowActive) Color(0xFF2A1C12) else IrPanel, if (rowActive) IrOrange else IrBorder, 6f, 1f)
        text("i = $row", layout.boundsFor("row:$row"), if (rowActive) IrOrange else IrSecondary, 9, true, 1, true)
        (1..meta.innerLimit).forEach { column ->
            val entity = entities.getValue("nested:cell:$row:$column")
            entityCard(layout.boundsFor(entity.visualKey), entity, entity.dataValue)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderRecursion(
    frame: IterationRecursionSceneFrame,
    layout: IterationRecursionSceneLayoutResult,
    card: (Rect2D, Color, Color, Float, Float) -> Unit,
    text: (String, Rect2D, Color, Int, Boolean, Int, Boolean) -> Unit,
    arrow: (SceneConnectorGeometry, Color) -> Unit,
    entityCard: (Rect2D, VisualEntity, String) -> Unit
) {
    val meta = frame.meta
    card(layout.boundsFor("header"), IrBackground, IrBorder, 8f, 1f)
    text("RECURSION FUNDAMENTALS · sumTo(${meta.inputN}) · BASE CASE n == 0", layout.boundsFor("header_title"), IrOrange, 10, true, 1, false)
    val headline = when (meta.action) {
        "FINISHED" -> "Call stack empty · final result = ${meta.currentReturnValue}"
        "BASE_CASE_REACHED" -> "Terminal call sumTo(0) returns 0"
        "RETURN_ASCENT" -> {
            val receiver = meta.receivingCallerN?.let { "receiving caller sumTo($it)" } ?: "the final result"
            "Returning frame sumTo(${meta.returningFrameN}) returns ${meta.currentReturnValue} to $receiver"
        }
        else -> "Active depth ${meta.callStack.size} · current n = ${meta.currentN ?: "—"}"
    }
    text(headline, layout.boundsFor("header_status"), if (meta.action in setOf("BASE_CASE_REACHED", "RETURN_ASCENT", "FINISHED")) IrGreen else IrPrimary, 11, true, 2, false)
    text("CALL SEQUENCE", layout.boundsFor("call_label"), IrBlue, 9, true, 1, false)
    val callScene = frame.scene.subScenes.first { it.subPaneId == "recursive_calls" }
    val activeKeys = callScene.entities.map { it.visualKey }.toSet()
    layout.connectors.forEach { connector ->
        val n = connector.id.substringAfter("call:").toInt()
        if ("recursion:frame:$n" in activeKeys && "recursion:frame:${n - 1}" in activeKeys) arrow(connector, IrBlue)
    }
    callScene.entities.forEach { entity ->
        val bounds = layout.boundsFor(entity.visualKey)
        entityCard(bounds, entity, "")
        text(entity.dataValue, layout.boundsFor("${entity.visualKey}:name"), if (entity.visualState == EntityVisualState.DEFAULT) IrSecondary else IrPrimary, 10, true, 1, true)
        text(entity.badgeLabel.orEmpty(), layout.boundsFor("${entity.visualKey}:status"), when {
            entity.badgeLabel.orEmpty().startsWith("RETURNING") || entity.badgeLabel == "BASE CASE" -> IrGreen
            entity.badgeLabel == "ACTIVE" -> IrOrange
            else -> IrSecondary
        }, 7, true, 3, true)
    }
    text("RETURN SEQUENCE", layout.boundsFor("return_label"), IrGreen, 9, true, 1, false)
    val returnScene = frame.scene.subScenes.first { it.subPaneId == "return_history" }
    returnScene.entities.forEach { entity ->
        val bounds = layout.boundsFor(entity.visualKey)
        card(bounds, Color(0xFF123820), IrGreen, 7f, 1f)
        text(entity.badgeLabel.orEmpty(), layout.boundsFor("${entity.visualKey}:name"), IrGreen, 8, true, 1, true)
        text("returns ${entity.dataValue}", layout.boundsFor("${entity.visualKey}:value"), IrGreen, 9, true, 1, true)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderTradeoffs(
    frame: IterationRecursionSceneFrame,
    sequence: List<IterationRecursionSceneFrame>,
    layout: IterationRecursionSceneLayoutResult,
    card: (Rect2D, Color, Color, Float, Float) -> Unit,
    text: (String, Rect2D, Color, Int, Boolean, Int, Boolean) -> Unit,
    arrow: (SceneConnectorGeometry, Color) -> Unit,
    entityCard: (Rect2D, VisualEntity, String) -> Unit
) {
    val meta = frame.meta
    val completed = meta.action == "FINISHED"
    val memory = IterationRecursionSceneLayout.tradeoffAuxiliaryMemory(frame)
    card(layout.boundsFor("iteration_panel"), IrBackground, if (meta.action == "ITERATIVE_LOOP") IrOrange else IrBorder, 9f, 1f)
    val iterationHeadline = if (completed) "ITERATION · result ${meta.iterTotal}" else "ITERATION · i = ${meta.iterI}  ·  accumulator = ${meta.iterTotal}"
    text(iterationHeadline, layout.boundsFor("iteration_current"), if (completed) IrGreen else IrPrimary, 16, true, 1, false)
    text(
        "${memory.iterationStateCount} ACCUMULATOR REUSED · O(1) AUXILIARY · PROGRESS 1–${meta.inputN}",
        layout.boundsFor("iteration_progress_label"),
        IrOrange,
        12,
        true,
        1,
        false
    )
    frame.scene.subScenes.first { it.subPaneId == "iteration_memory" }.entities.forEach { entity ->
        val bounds = layout.boundsFor(entity.visualKey)
        entityCard(bounds, entity, "")
        text(entity.dataValue, bounds, if (entity.visualState == EntityVisualState.DEFAULT) IrSecondary else IrPrimary, 13, true, 1, true)
    }

    card(layout.boundsFor("recursion_panel"), IrBackground, if (meta.action.startsWith("RECURSIVE")) IrBlue else IrBorder, 9f, 1f)
    val activeDepth = memory.recursionActiveFrameCount
    val maximumDepth = memory.recursionMaximumFrameCount
    text(
        "ACTIVE FRAMES $activeDepth / MAX $maximumDepth · O(N) STACK",
        layout.boundsFor("recursion_heading"),
        IrBlue,
        12,
        true,
        1,
        false
    )
    val recursionStatus = when (meta.action) {
        "RECURSIVE_DESCENT" -> "CURRENT CALLEE sumTo(${meta.currentN}) · callers ${activeDepth - 1}"
        "RECURSIVE_BASE" -> "BASE CASE · sumTo(0) = 0 · maximum depth reached"
        "RECURSIVE_ASCENT" -> buildString {
            append("RETURNING sumTo(${meta.returningFrameN}) = ${meta.recurReturnValue}")
            append(meta.receivingCallerN?.let { " → RECEIVING sumTo($it)" } ?: " → FINAL RESULT")
        }
        else -> if (completed) "DEPTH 0 · STACK EMPTY · result ${meta.recurReturnValue}" else "DEPTH 0 · NOT YET CALLED"
    }
    text(recursionStatus, layout.boundsFor("recursion_current"), if (meta.action in setOf("RECURSIVE_BASE", "RECURSIVE_ASCENT", "FINISHED")) IrGreen else IrPrimary, 14, true, 1, false)
    val stackEntities = frame.scene.subScenes.first { it.subPaneId == "recursion_memory" }.entities
    stackEntities.forEach { entity ->
        entityCard(layout.boundsFor(entity.visualKey), entity, "")
        text(entity.dataValue, layout.boundsFor("${entity.visualKey}:name"), IrPrimary, 12, true, 1, true)
        val n = entity.dataValue.removePrefix("sumTo(").removeSuffix(")").toInt()
        val status = when {
            meta.action == "RECURSIVE_ASCENT" && n == meta.returningFrameN -> "RETURN"
            meta.action == "RECURSIVE_ASCENT" && n == meta.receivingCallerN -> "RECEIVE"
            meta.action == "RECURSIVE_BASE" && n == 0 -> "BASE"
            entity.badgeLabel == "ACTIVE" -> "ACTIVE"
            entity.badgeLabel == "WAITING" -> "CALLER"
            else -> entity.badgeLabel.orEmpty()
        }
        text(status, layout.boundsFor("${entity.visualKey}:status"), if (status.startsWith("RETURN") || status.startsWith("BASE")) IrGreen else if (status.startsWith("RECEIVE")) IrBlue else IrOrange, 11, true, 1, true)
    }
    IterationRecursionSceneLayout.tradeoffReturnConnector(layout, frame)?.let { connector ->
        arrow(connector, IrBlue)
    }

    val returns = IterationRecursionSceneLayout.completedTradeoffReturns(sequence, frame.scene.stepIndex)
    val latestReturn = IterationRecursionSceneLayout.latestCompletedTradeoffReturn(sequence, frame.scene.stepIndex)
    text("RETURN HISTORY · base → caller", layout.boundsFor("return_history_label"), IrGreen, 12, true, 1, false)
    returns.forEach { (n, value) ->
        val key = "tradeoff:return:$n"
        val isLatest = n == latestReturn
        card(
            layout.boundsFor(key),
            if (isLatest) Color(0xFF2A1C12) else Color(0xFF123820),
            if (isLatest) IrOrange else IrGreen,
            6f,
            if (isLatest) 2f else 1f
        )
        text("sumTo($n)", layout.boundsFor("$key:name"), if (isLatest) IrOrange else IrGreen, 11, true, 1, true)
        text("= $value", layout.boundsFor("$key:value"), IrPrimary, 12, true, 1, true)
    }
}
