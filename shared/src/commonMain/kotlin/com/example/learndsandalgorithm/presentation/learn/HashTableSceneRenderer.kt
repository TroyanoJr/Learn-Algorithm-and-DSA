package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.runtime.remember
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.hypot

private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

internal data class HashTableCalculationRegion(
    val bounds: Rect2D,
    val requiredSceneHeight: Float
)

internal data class HashTableCalculationCandidate(
    val width: Float,
    val cardHeights: List<Float>
)

internal fun calculateHashTableCalculationRegion(
    sceneWidth: Float,
    structuralLayouts: List<HashTableSceneLayoutResult>,
    candidates: List<HashTableCalculationCandidate>,
    structureGap: Float = 8f,
    bottomMargin: Float = HashTableSceneLayout.VERTICAL_MARGIN_DP,
    horizontalClearance: Float = 4f
): HashTableCalculationRegion {
    val left = HashTableSceneLayout.HORIZONTAL_MARGIN_DP
    val availableWidth = (sceneWidth - left * 2f).coerceAtLeast(0f)
    val occupied = structuralLayouts.flatMap { frame ->
        frame.bucketBounds.values + frame.chainNodeBounds.values + frame.chainNullBounds.values +
            frame.indexLabelBounds.values + frame.targetArrowBounds.values + frame.probeLabelBounds.values +
            frame.chainConnectionLines.values.map { line ->
                val arrowPadding = 6f
                Rect2D(
                    left = minOf(line.start.x, line.end.x) - arrowPadding,
                    top = minOf(line.start.y, line.end.y) - arrowPadding,
                    width = kotlin.math.abs(line.end.x - line.start.x) + arrowPadding * 2f,
                    height = kotlin.math.abs(line.end.y - line.start.y) + arrowPadding * 2f
                )
            }
    }
    val structuralHeight = structuralLayouts.maxOfOrNull { it.requiredSceneHeight } ?: 0f
    return candidates.map { candidate ->
        val width = candidate.width.coerceAtMost(availableWidth)
        val right = left + width
        val occupiedBottom = occupied
            .filter { bounds ->
                bounds.right + horizontalClearance > left && bounds.left - horizontalClearance < right
            }
            .maxOfOrNull { it.bottom } ?: 0f
        val bounds = Rect2D(
            left = left,
            top = occupiedBottom + structureGap,
            width = width,
            height = candidate.cardHeights.maxOrNull() ?: 0f
        )
        HashTableCalculationRegion(
            bounds = bounds,
            requiredSceneHeight = maxOf(structuralHeight, bounds.bottom + bottomMargin)
        )
    }.minWithOrNull(
        compareBy<HashTableCalculationRegion> { it.requiredSceneHeight }
            .thenByDescending { it.bounds.width }
    ) ?: HashTableCalculationRegion(
        bounds = Rect2D(left, structuralHeight, 0f, 0f),
        requiredSceneHeight = structuralHeight
    )
}

/**
 * Canvas-based renderer for Hash Table [VisualizationScene] produced by [HashTableSceneBuilder],
 * rendering Fundamentals, Separate Chaining, and Linear Probing scenes strictly using layout bounds.
 */
@Composable
internal fun HashTableSceneRenderer(
    scene: VisualizationScene,
    modifier: Modifier = Modifier,
    mode: HashTableSceneMode = HashTableSceneMode.COMPLETE,
    sequenceEntities: List<VisualEntity> = emptyList(),
    sequence: List<VisualizationScene> = emptyList()
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val structureOnly = mode == HashTableSceneMode.COLLISION_STRUCTURE && scene.hashTableMeta?.strategy != null
    // Measure the complete strategy sequence so values appearing later cannot resize playback.
    val metrics = if (!structureOnly) HashTableStructuralMetrics() else with(density) {
        val entries = sequenceEntities.filter { it.entryKey != null }.distinctBy { it.entryKey to it.entryValue }
        val keys = entries.map { textMeasurer.measure(it.entryKey.orEmpty(), TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)) }
        val values = entries.map { textMeasurer.measure(it.entryValue.orEmpty(), TextStyle(fontSize = 9.sp)) }
        val keyHeight = (keys.maxOfOrNull { it.size.height } ?: textMeasurer.measure("Ag", TextStyle(fontSize = 11.sp)).size.height).toDp().value
        val valueHeight = (values.maxOfOrNull { it.size.height } ?: textMeasurer.measure("Ag", TextStyle(fontSize = 9.sp)).size.height).toDp().value
        val labels = listOf("COLLISION", "TARGET", "PROBE", "• null", "[6]", "↓").map {
            textMeasurer.measure(it, TextStyle(fontSize = if (it == "↓") 13.sp else 10.sp, fontWeight = FontWeight.Bold))
        }
        HashTableStructuralMetrics(
            bucketWidth = maxOf(48f, ((keys + values + labels).maxOfOrNull { it.size.width } ?: 0).toDp().value + 8f),
            nodeHeight = maxOf(38f, keyHeight + valueHeight + 10f),
            cellHeight = maxOf(52f, keyHeight + valueHeight + 10f),
            labelHeight = maxOf(18f, labels.maxOf { it.size.height }.toDp().value + 4f)
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value

        val subScene = scene.subScenes.firstOrNull { it.subPaneId == "hash_table_pane" }
            ?: scene.subScenes.firstOrNull()

        val entities = subScene?.entities ?: emptyList()
        val buckets = entities.filter { it.slotKey.startsWith("bucket_slot:") || it.slotKey.startsWith("linear_slot:") }
        val capacity = if (buckets.isNotEmpty()) buckets.size else 7

        val meta = scene.hashTableMeta

        val layout = HashTableSceneLayout.calculateLayout(
            capacity = capacity,
            availableWidthDp = widthDp,
            availableHeightDp = heightDp,
            hasActiveKey = meta?.activeKey != null,
            hasBreakdown = meta?.charBreakdown != null,
            subScene = subScene,
            strategy = meta?.strategy,
            maxChainDepth = meta?.maxChainDepth ?: 0,
            mode = mode,
            metrics = metrics
        )

        val canvasWidthDp = maxOf(widthDp, layout.requiredSceneWidth)
        val canvasHeightDp = maxOf(heightDp, layout.requiredSceneHeight)

        val isSeparateChaining = meta?.strategy == CollisionStrategy.SEPARATE_CHAINING || entities.any { it.slotKey.startsWith("chain_slot:") }
        val isLinearProbing = meta?.strategy == CollisionStrategy.LINEAR_PROBING

        val diagram: @Composable () -> Unit = {
            Canvas(
                modifier = Modifier.size(canvasWidthDp.dp, (if (sequence.isEmpty()) canvasHeightDp else layout.requiredSceneHeight).dp)
            ) {
                // ==========================================
                // 1. DRAW HASH FUNCTION / COLLISION CARD (Region Upper)
                // ==========================================
                if (!structureOnly) {
                    val cardRect = layout.hashCardBounds
                    val cardLeft = cardRect.left.dp.toPx()
                    val cardTop = cardRect.top.dp.toPx()
                    val cardWidth = cardRect.width.dp.toPx()
                    val cardHeight = cardRect.height.dp.toPx()
                    val cardCorner = CornerRadius(10.dp.toPx(), 10.dp.toPx())

                    drawRoundRect(
                        color = Color(0xFF13131C),
                        topLeft = Offset(cardLeft, cardTop),
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = cardCorner
                    )
                    drawRoundRect(
                        color = CardBorderColor,
                        topLeft = Offset(cardLeft, cardTop),
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = cardCorner,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Title & Formula
                    val titleRect = layout.titleBounds
                    val formulaRect = layout.formulaBounds

                    val titleText = when {
                        isSeparateChaining -> "SEPARATE CHAINING (LINKED LISTS)"
                        isLinearProbing -> "OPEN ADDRESSING (LINEAR PROBING)"
                        else -> "HASH FUNCTION MAPPING"
                    }

                    val titleLayout = textMeasurer.measure(
                        text = titleText,
                        style = TextStyle(color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        constraints = Constraints(maxWidth = titleRect.width.dp.toPx().toInt())
                    )
                    val formulaText = if (isSeparateChaining || isLinearProbing) "hash(key) % capacity" else "index = hash(key) % capacity"
                    val formulaLayout = textMeasurer.measure(
                        text = formulaText,
                        style = TextStyle(color = TextSecondary, fontSize = 10.sp),
                        constraints = Constraints(maxWidth = formulaRect.width.dp.toPx().toInt())
                    )
                    drawText(
                        textLayoutResult = titleLayout,
                        topLeft = Offset(titleRect.left.dp.toPx(), titleRect.top.dp.toPx())
                    )
                    drawText(
                        textLayoutResult = formulaLayout,
                        topLeft = Offset(formulaRect.left.dp.toPx(), formulaRect.top.dp.toPx())
                    )

                    if (meta?.activeKey != null) {
                        val keyBadgeRect = layout.keyBadgeBounds
                        if (keyBadgeRect != null) {
                            val kLeft = keyBadgeRect.left.dp.toPx()
                            val kTop = keyBadgeRect.top.dp.toPx()
                            val kWidth = keyBadgeRect.width.dp.toPx()
                            val kHeight = keyBadgeRect.height.dp.toPx()

                            val hashSuffix = if (meta.initialHashIndex != null) " (Hash Index = ${meta.initialHashIndex})" else ""
                            val kvText = "Key: \"${meta.activeKey}\" → Value: \"${meta.activeValue}\"$hashSuffix"
                            val kvLayout = textMeasurer.measure(
                                text = kvText,
                                style = TextStyle(color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                constraints = Constraints(maxWidth = kWidth.toInt())
                            )
                            drawRoundRect(
                                color = Color(0xFF1F1F2C),
                                topLeft = Offset(kLeft, kTop),
                                size = Size(kWidth, kHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                            drawRoundRect(
                                color = OrangeAccent,
                                topLeft = Offset(kLeft, kTop),
                                size = Size(kWidth, kHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawText(
                                textLayoutResult = kvLayout,
                                topLeft = Offset(kLeft + 8.dp.toPx(), kTop + 5.dp.toPx())
                            )
                        }

                        // Collision Badge / Table Full Badge on Top Card
                        val colBadgeRect = layout.collisionBadgeBounds ?: layout.keyBadgeBounds
                        val isTableFull = meta.badgeText == "TABLE FULL"
                        if ((meta.collisionEvent == CollisionTraceEvent.COLLISION_DETECTED || isTableFull) && colBadgeRect != null) {
                            val cLeft = colBadgeRect.left.dp.toPx()
                            val cTop = colBadgeRect.top.dp.toPx()
                            val cWidth = colBadgeRect.width.dp.toPx()
                            val cHeight = colBadgeRect.height.dp.toPx()

                            val badgeLabelText = if (isTableFull) "TABLE FULL" else "COLLISION DETECTED"
                            val colBadgeLayout = textMeasurer.measure(
                                text = badgeLabelText,
                                style = TextStyle(color = RedAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                constraints = Constraints(maxWidth = maxOf(1, cWidth.toInt()))
                            )
                            drawRoundRect(
                                color = Color(0xFF381212),
                                topLeft = Offset(cLeft, cTop),
                                size = Size(cWidth, cHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                            drawRoundRect(
                                color = RedAccent,
                                topLeft = Offset(cLeft, cTop),
                                size = Size(cWidth, cHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawText(
                                textLayoutResult = colBadgeLayout,
                                topLeft = Offset(cLeft + (cWidth - colBadgeLayout.size.width) / 2f, cTop + (cHeight - colBadgeLayout.size.height) / 2f)
                            )
                        }

                        val bdRect = layout.breakdownBounds
                        if (bdRect != null && meta.charBreakdown != null) {
                            val calcStr = meta.charBreakdown.joinToString(" + ") { "${it.first}(${it.second})" }
                            val bdLeft = bdRect.left.dp.toPx()
                            val bdTop = bdRect.top.dp.toPx()
                            val bdWidth = bdRect.width.dp.toPx()
                            val bdHeight = bdRect.height.dp.toPx()

                            val calcLayout = textMeasurer.measure(
                                text = calcStr,
                                style = TextStyle(color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                constraints = Constraints(maxWidth = bdWidth.toInt())
                            )

                            drawRoundRect(
                                color = Color(0xFF1A1A26),
                                topLeft = Offset(bdLeft, bdTop),
                                size = Size(bdWidth, bdHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                            drawRoundRect(
                                color = CardBorderColor,
                                topLeft = Offset(bdLeft, bdTop),
                                size = Size(bdWidth, bdHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawText(
                                textLayoutResult = calcLayout,
                                topLeft = Offset(bdLeft + 8.dp.toPx(), bdTop + (bdHeight - calcLayout.size.height) / 2f)
                            )
                        }

                        val sumRect = layout.sumBounds
                        if (sumRect != null && meta.hashSum != null) {
                            val sumText = "= ${meta.hashSum}"
                            val sumLayout = textMeasurer.measure(
                                text = sumText,
                                style = TextStyle(color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                constraints = Constraints(maxWidth = sumRect.width.dp.toPx().toInt())
                            )
                            drawText(
                                textLayoutResult = sumLayout,
                                topLeft = Offset(sumRect.left.dp.toPx(), sumRect.top.dp.toPx() + 4.dp.toPx())
                            )
                        }

                        val modRect = layout.moduloBounds
                        if (modRect != null && meta.calculatedIndex != null) {
                            val idxText = "% ${meta.capacity} = Index [${meta.calculatedIndex}]"
                            val idxLayout = textMeasurer.measure(
                                text = idxText,
                                style = TextStyle(color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                constraints = Constraints(maxWidth = modRect.width.dp.toPx().toInt())
                            )
                            drawText(
                                textLayoutResult = idxLayout,
                                topLeft = Offset(modRect.left.dp.toPx(), modRect.top.dp.toPx() + 4.dp.toPx())
                            )
                        }
                    } else {
                        val placeholder = textMeasurer.measure(
                            text = "Select a key to view hash calculation step-by-step.",
                            style = TextStyle(color = TextSecondary, fontSize = 11.sp),
                            constraints = Constraints(maxWidth = (cardWidth - 24f).toInt())
                        )
                        drawText(
                            textLayoutResult = placeholder,
                            topLeft = Offset(cardLeft + 12.dp.toPx(), cardTop + 45.dp.toPx())
                        )
                    }

                    // ==========================================
                }

                // 2. DRAW STRUCTURE REGION (Chaining vs Probing)
                // ==========================================
                // Draw all pre-calculated connection lines (including final node-to-null connections)
                layout.chainConnectionLines.forEach { (_, connLine) ->
                    val startPx = Offset(connLine.start.x.dp.toPx(), connLine.start.y.dp.toPx())
                    val endPx = Offset(connLine.end.x.dp.toPx(), connLine.end.y.dp.toPx())
                    drawLine(
                        color = BlueAccent.copy(alpha = 0.7f),
                        start = startPx,
                        end = endPx,
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    val angle = atan2(endPx.y - startPx.y, endPx.x - startPx.x)
                    val arrowLen = if (structureOnly) minOf(6.dp.toPx(), hypot(endPx.x - startPx.x, endPx.y - startPx.y) * 0.8f) else 6.dp.toPx()
                    val arrowAngle = 0.523599f
                    val p1 = Offset(x = endPx.x - arrowLen * cos(angle - arrowAngle), y = endPx.y - arrowLen * sin(angle - arrowAngle))
                    val p2 = Offset(x = endPx.x - arrowLen * cos(angle + arrowAngle), y = endPx.y - arrowLen * sin(angle + arrowAngle))
                    val arrowPath = Path().apply {
                        moveTo(endPx.x, endPx.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        close()
                    }
                    drawPath(path = arrowPath, color = BlueAccent.copy(alpha = 0.7f))
                }

                if (isSeparateChaining) {
                    // Draw Separate Chaining Base Buckets + Vertical Node Chains
                    for (i in 0 until capacity) {
                        val bucketSlotKey = "bucket_slot:$i"
                        val rect = layout.bucketBounds[bucketSlotKey]
                        val arrowRect = layout.targetArrowBounds[bucketSlotKey]
                        val nullRect = layout.chainNullBounds["chain_null:$i"]
                        val bucketEntity = entities.firstOrNull { it.slotKey == bucketSlotKey }

                        if (rect != null) {
                            val leftPx = rect.left.dp.toPx()
                            val topPx = rect.top.dp.toPx()
                            val widthPx = rect.width.dp.toPx()
                            val heightPx = rect.height.dp.toPx()
                            val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                            val isHigh = bucketEntity?.visualState == EntityVisualState.ACTIVE
                            val isCollision = bucketEntity?.visualState == EntityVisualState.VIOLATION
                            val isVisited = bucketEntity?.visualState == EntityVisualState.VISITED

                            // Target Arrow
                            if (isHigh || isCollision) {
                                val arrowText = textMeasurer.measure(
                                    text = "↓",
                                    style = TextStyle(color = if (isCollision) RedAccent else OrangeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                    constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt()))
                                )
                                val aTop = arrowRect?.top?.dp?.toPx() ?: (topPx - 18.dp.toPx())
                                drawText(
                                    textLayoutResult = arrowText,
                                    topLeft = Offset(leftPx + (widthPx - arrowText.size.width) / 2f, aTop)
                                )
                            }

                            // Bucket Index Header Box
                            drawRoundRect(
                                color = when {
                                    isCollision -> Color(0xFF381212)
                                    isHigh -> Color(0xFF2A1C12)
                                    isVisited -> Color(0xFF13233A)
                                    else -> Color(0xFF13131C)
                                },
                                topLeft = Offset(leftPx, topPx),
                                size = Size(widthPx, heightPx),
                                cornerRadius = cornerRadius
                            )
                            drawRoundRect(
                                color = when {
                                    isCollision -> RedAccent
                                    isHigh -> OrangeAccent
                                    isVisited -> BlueAccent
                                    else -> CardBorderColor
                                },
                                topLeft = Offset(leftPx, topPx),
                                size = Size(widthPx, heightPx),
                                cornerRadius = cornerRadius,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            val indexLayout = textMeasurer.measure(
                                text = "[$i]",
                                style = TextStyle(
                                    color = if (isCollision) RedAccent else if (isHigh) OrangeAccent else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt()))
                            )
                            drawText(
                                textLayoutResult = indexLayout,
                                topLeft = Offset(leftPx + (widthPx - indexLayout.size.width) / 2f, topPx + (heightPx - indexLayout.size.height) / 2f)
                            )

                            // Chain Nodes under Bucket i
                            val chainEntities = entities.filter { it.slotKey.startsWith("chain_slot:$i:") }
                            val nullTop = nullRect?.top?.dp?.toPx() ?: (topPx + heightPx + 6.dp.toPx())

                            if (chainEntities.isEmpty()) {
                                val nullText = textMeasurer.measure(text = "null", style = TextStyle(color = if (structureOnly) TextSecondary else TextSecondary.copy(alpha = 0.4f), fontSize = if (structureOnly) 10.sp else 9.sp), constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt())))
                                drawText(
                                    textLayoutResult = nullText,
                                    topLeft = Offset(leftPx + (widthPx - nullText.size.width) / 2f, nullTop)
                                )
                            } else {
                                chainEntities.forEachIndexed { _, chainEntity ->
                                    val chainSlotKey = chainEntity.slotKey
                                    val nodeRect = layout.chainNodeBounds[chainSlotKey]
                                    if (nodeRect != null) {
                                        val nLeft = nodeRect.left.dp.toPx()
                                        val nTop = nodeRect.top.dp.toPx()
                                        val nWidth = nodeRect.width.dp.toPx()
                                        val nHeight = nodeRect.height.dp.toPx()

                                        val isNodeActive = chainEntity.visualState == EntityVisualState.ACTIVE
                                        val isNodeCollision = chainEntity.visualState == EntityVisualState.VIOLATION

                                        val nBg = when {
                                            isNodeCollision -> Color(0xFF381212)
                                            isNodeActive -> Color(0xFF2A1C12)
                                            else -> Color(0xFF13233A)
                                        }
                                        val nBorder = when {
                                            isNodeCollision -> RedAccent
                                            isNodeActive -> OrangeAccent
                                            else -> BlueAccent
                                        }

                                        drawRoundRect(
                                            color = nBg,
                                            topLeft = Offset(nLeft, nTop),
                                            size = Size(nWidth, nHeight),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )
                                        drawRoundRect(
                                            color = nBorder,
                                            topLeft = Offset(nLeft, nTop),
                                            size = Size(nWidth, nHeight),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                            style = Stroke(width = 1.dp.toPx())
                                        )

                                        val keyStr = chainEntity.entryKey ?: ""
                                        val valStr = chainEntity.entryValue ?: ""

                                        val kLayout = textMeasurer.measure(text = keyStr, style = TextStyle(color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold), constraints = Constraints(maxWidth = maxOf(1, (nWidth - if (structureOnly) 8.dp.toPx() else 0f).toInt())))
                                        val vLayout = textMeasurer.measure(text = valStr, style = TextStyle(color = BlueAccent, fontSize = 9.sp), constraints = Constraints(maxWidth = maxOf(1, (nWidth - if (structureOnly) 8.dp.toPx() else 0f).toInt())))

                                        drawText(textLayoutResult = kLayout, topLeft = Offset(nLeft + (nWidth - kLayout.size.width) / 2f, if (structureOnly) nTop + (nHeight - kLayout.size.height - vLayout.size.height - 2.dp.toPx()) / 2f else nTop + 4.dp.toPx()))
                                        drawText(textLayoutResult = vLayout, topLeft = Offset(nLeft + (nWidth - vLayout.size.width) / 2f, if (structureOnly) nTop + (nHeight - kLayout.size.height - vLayout.size.height - 2.dp.toPx()) / 2f + kLayout.size.height + 2.dp.toPx() else nTop + 22.dp.toPx()))
                                    }
                                }

                                // Render terminal null annotation using chainNullBounds
                                val endText = textMeasurer.measure(text = "• null", style = TextStyle(color = TextSecondary, fontSize = if (structureOnly) 10.sp else 8.sp, fontWeight = FontWeight.Bold), constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt())))
                                drawText(
                                    textLayoutResult = endText,
                                    topLeft = Offset(leftPx + (widthPx - endText.size.width) / 2f, nullTop)
                                )
                            }
                        }
                    }
                } else {
                    // Draw Linear Probing or Standard Buckets
                    buckets.forEachIndexed { index, bucketEntity ->
                        val bucketSlotKey = "bucket_slot:$index"
                        val linearSlotKey = "linear_slot:$index"
                        val entrySlotKey = "entry_slot:$index"

                        val rect = layout.bucketBounds[bucketSlotKey] ?: layout.bucketBounds[linearSlotKey]
                        val labelRect = layout.indexLabelBounds[bucketSlotKey] ?: layout.indexLabelBounds[linearSlotKey]
                        val probeLabelRect = layout.probeLabelBounds[linearSlotKey]
                        val entryEntity = entities.firstOrNull { it.slotKey == entrySlotKey || it.slotKey == linearSlotKey }

                        if (rect != null) {
                            val leftPx = rect.left.dp.toPx()
                            val topPx = rect.top.dp.toPx()
                            val widthPx = rect.width.dp.toPx()
                            val heightPx = rect.height.dp.toPx()
                            val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())

                            val entityState = entryEntity?.visualState ?: bucketEntity.visualState
                            val badgeLabel = entryEntity?.badgeLabel ?: bucketEntity.badgeLabel

                            val isCollision = entityState == EntityVisualState.VIOLATION
                            val isHigh = entityState == EntityVisualState.ACTIVE
                            val isVisited = entityState == EntityVisualState.VISITED
                            val isOccupied = entryEntity?.entryKey != null

                            val bgColor = when {
                                isCollision -> Color(0xFF381212)
                                badgeLabel == "PROBE" -> Color(0xFF13283A)
                                isHigh -> Color(0xFF2A1C12)
                                isOccupied || isVisited -> Color(0xFF13233A)
                                else -> DarkCardBgColor
                            }

                            val borderColor = when {
                                isCollision -> RedAccent
                                badgeLabel == "PROBE" -> BlueAccent
                                isHigh -> OrangeAccent
                                isOccupied || isVisited -> BlueAccent
                                else -> CardBorderColor
                            }

                            // Probe Upper Label
                            if (badgeLabel != null && probeLabelRect != null) {
                                val pTextLayout = textMeasurer.measure(
                                    text = badgeLabel,
                                    style = TextStyle(
                                        color = when (badgeLabel) {
                                            "COLLISION" -> RedAccent
                                            "PROBE" -> BlueAccent
                                            "TARGET" -> OrangeAccent
                                            else -> TextSecondary
                                        },
                                        fontSize = if (structureOnly) 10.sp else 8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt()))
                                )
                                drawText(
                                    textLayoutResult = pTextLayout,
                                    topLeft = Offset(leftPx + (widthPx - pTextLayout.size.width) / 2f, probeLabelRect.top.dp.toPx())
                                )
                            }

                            // Cell Fill
                            drawRoundRect(
                                color = bgColor,
                                topLeft = Offset(leftPx, topPx),
                                size = Size(widthPx, heightPx),
                                cornerRadius = cornerRadius
                            )

                            // Cell Border
                            drawRoundRect(
                                color = borderColor,
                                topLeft = Offset(leftPx, topPx),
                                size = Size(widthPx, heightPx),
                                cornerRadius = cornerRadius,
                                style = Stroke(width = if (isHigh || isCollision) 1.5.dp.toPx() else 1.dp.toPx())
                            )

                            // Key & Value or Empty Dot (using structured entryKey and entryValue strictly)
                            if (isOccupied && entryEntity != null && entryEntity.entryKey != null) {
                                val keyStr = entryEntity.entryKey
                                val valStr = entryEntity.entryValue ?: ""

                                val keyLayout = textMeasurer.measure(
                                    text = keyStr,
                                    style = TextStyle(color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt()))
                                )
                                val valLayout = textMeasurer.measure(
                                    text = valStr,
                                    style = TextStyle(color = BlueAccent, fontSize = 9.sp),
                                    constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt()))
                                )

                                drawText(
                                    textLayoutResult = keyLayout,
                                    topLeft = Offset(
                                        x = leftPx + (widthPx - keyLayout.size.width) / 2f,
                                        y = if (structureOnly) topPx + (heightPx - keyLayout.size.height - valLayout.size.height - 2.dp.toPx()) / 2f else topPx + 10.dp.toPx()
                                    )
                                )
                                drawText(
                                    textLayoutResult = valLayout,
                                    topLeft = Offset(
                                        x = leftPx + (widthPx - valLayout.size.width) / 2f,
                                        y = if (structureOnly) topPx + (heightPx - keyLayout.size.height - valLayout.size.height - 2.dp.toPx()) / 2f + keyLayout.size.height + 2.dp.toPx() else topPx + 28.dp.toPx()
                                    )
                                )
                            } else {
                                val dotLayout = textMeasurer.measure(
                                    text = "·",
                                    style = TextStyle(color = TextSecondary.copy(alpha = 0.5f), fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                    constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt()))
                                )
                                drawText(
                                    textLayoutResult = dotLayout,
                                    topLeft = Offset(
                                        x = leftPx + (widthPx - dotLayout.size.width) / 2f,
                                        y = topPx + (heightPx - dotLayout.size.height) / 2f
                                    )
                                )
                            }

                            // Index Label below bucket
                            if (labelRect != null) {
                                val indexLayout = textMeasurer.measure(
                                    text = "[$index]",
                                    style = TextStyle(
                                        color = if (isHigh || isCollision) OrangeAccent else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    constraints = Constraints(maxWidth = maxOf(1, (widthPx - if (structureOnly) 8.dp.toPx() else 0f).toInt()))
                                )
                                drawText(
                                    textLayoutResult = indexLayout,
                                    topLeft = Offset(
                                        x = leftPx + (widthPx - indexLayout.size.width) / 2f,
                                        y = labelRect.top.dp.toPx()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        VisualizerCanvasViewport(modifier = modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID) {
            if (sequence.isEmpty()) diagram() else {
                val envelope = remember(sequence, widthDp, metrics) {
                    sequence.map { snapshot ->
                        HashTableSceneLayout.calculateLayout(capacity, widthDp,
                            subScene = snapshot.subScenes.first(), strategy = snapshot.hashTableMeta?.strategy,
                            maxChainDepth = snapshot.hashTableMeta?.maxChainDepth ?: 0,
                            mode = mode, metrics = metrics)
                    }
                }
                val calculations = remember(sequence) { sequence.map { collisionCalculationMetadata(it.hashTableMeta!!) } }
                SubcomposeLayout { _ ->
                    val width = canvasWidthDp.dp.roundToPx()
                    val height = layout.requiredSceneHeight.dp.roundToPx()
                    // Intrinsic measurements include the native card's borders, padding,
                    // text wrapping and badges for all snapshots at the current font scale.
                    val cards = calculations.mapIndexed { index, calculation ->
                        subcompose(index) { HashTableCollisionCalculation(calculation) }.single()
                    }
                    val naturalWidth = cards.maxOf { it.maxIntrinsicWidth(Constraints.Infinity) }
                    val availableWidth = canvasWidthDp - HashTableSceneLayout.HORIZONTAL_MARGIN_DP * 2f
                    val columns = layout.bucketBounds.values.sortedBy { it.left }
                    val minimumWidth = minOf(naturalWidth.toDp().value, 220f)
                    val candidateWidths = (
                        columns.map { column -> column.right - HashTableSceneLayout.HORIZONTAL_MARGIN_DP } +
                            minOf(naturalWidth.toDp().value, availableWidth)
                        ).filter { it >= minimumWidth }
                        .map { minOf(it, availableWidth) }
                        .distinct()
                    val candidates = candidateWidths.map { candidateWidth ->
                        val candidateWidthPx = candidateWidth.dp.roundToPx()
                        HashTableCalculationCandidate(
                            width = candidateWidth,
                            cardHeights = cards.map { card ->
                                card.maxIntrinsicHeight(candidateWidthPx).toDp().value
                            }
                        )
                    }
                    val calculationRegion = calculateHashTableCalculationRegion(
                        sceneWidth = canvasWidthDp,
                        structuralLayouts = envelope,
                        candidates = candidates
                    )
                    val regionWidth = calculationRegion.bounds.width.dp.roundToPx()
                    val structure = subcompose("diagram", diagram).single().measure(Constraints.fixed(width, height))
                    val card = cards[scene.stepIndex].measure(Constraints.fixedWidth(regionWidth))
                    layout(width, calculationRegion.requiredSceneHeight.dp.roundToPx()) {
                        structure.place(0, 0)
                        card.place(
                            calculationRegion.bounds.left.dp.roundToPx(),
                            calculationRegion.bounds.top.dp.roundToPx()
                        )
                    }
                }
            }
        }
    }
}
