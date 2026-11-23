package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TreePanelBackground = Color(0xFF171D27)
private val TreeCanvasBackground = Color(0xFF10141B)
private val TreePrimary = Color(0xFFF3F6FA)
private val TreeSecondary = Color(0xFFB8C3D1)
private val TreeMuted = Color(0xFF8290A0)
private val TreeGreen = Color(0xFF4ADE80)
private val TreeBlue = Color(0xFF60A5FA)
private val TreeOrange = Color(0xFFFBBF24)
private val TreeRed = Color(0xFFF87171)

@Composable
internal fun TradeoffRecursionTree(
    frame: DivideConquerTradeoffsSceneFrame,
    phase: TradeoffPlaybackPhase,
    selection: TradeoffTreeSelection,
    onSelectionChanged: (TradeoffTreeSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    val model = remember(frame, phase, selection) {
        tradeoffTreePresentationModel(
            frame = frame,
            selection = selection,
            revealDepth = phase.revealDepth,
            forceCompleted = phase.kind == TradeoffPlaybackPhaseKind.COMPLETE
        )
    }
    var inspectAll by remember { mutableStateOf(false) }
    val accent = if (selection == TradeoffTreeSelection.BALANCED) TreeGreen else TreeRed

    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            Modifier.width(TradeoffRecursionTreeLayout.widthFor(maxWidth.value).dp)
                .fillMaxHeight()
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                TreeSelector("Balanced Tree", selection == TradeoffTreeSelection.BALANCED, TreeGreen, Modifier.weight(1f)) {
                    onSelectionChanged(TradeoffTreeSelection.BALANCED)
                }
                TreeSelector("Unbalanced Tree", selection == TradeoffTreeSelection.UNBALANCED, TreeRed, Modifier.weight(1f)) {
                    onSelectionChanged(TradeoffTreeSelection.UNBALANCED)
                }
            }
            CompactComplexityAnnotations(
                model = model,
                accent = accent,
                phase = phase,
                inspectAll = inspectAll,
                onToggleInspector = { inspectAll = !inspectAll }
            )
            if (inspectAll) {
                TreeInspector(model, Modifier.weight(1f).fillMaxWidth())
            } else {
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    val layout = remember(model, maxWidth, maxHeight) {
                        TradeoffRecursionTreeLayout.calculateDiagram(model, maxWidth.value, maxHeight.value)
                    }
                    PartitionDiagram(model, layout, Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun CompactComplexityAnnotations(
    model: TradeoffTreePresentationModel,
    accent: Color,
    phase: TradeoffPlaybackPhase,
    inspectAll: Boolean,
    onToggleInspector: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(TreePanelBackground)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (phase.kind == TradeoffPlaybackPhaseKind.COMPLETE) model.annotations.depth
                else "DEPTH ${phase.revealDepth} / ${model.nodes.maxOfOrNull { it.depth } ?: 0}",
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                if (inspectAll) "Diagram" else "Inspect 19/18",
                color = TreeBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onToggleInspector).padding(start = 6.dp, top = 2.dp, bottom = 2.dp)
            )
        }
        Text(
            when {
                phase.kind == TradeoffPlaybackPhaseKind.COMPLETE -> model.annotations.work
                model.selection == TradeoffTreeSelection.UNBALANCED -> "N−1 CHAIN · N=1 BASE · WORK/LEVEL O(N)"
                else -> "NEAR-EQUAL SPLITS · WORK/LEVEL O(N)"
            },
            color = accent,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PartitionDiagram(model: TradeoffTreePresentationModel, layout: TradeoffPartitionDiagramLayout, modifier: Modifier = Modifier) {
    val nodeById = remember(model.nodes) { model.nodes.associateBy { it.id } }
    val edgeByKey = remember(model.edges) { model.edges.associateBy { it.connectionKey } }
    val animatedConnectionProgress = layout.connectors.associate { connector ->
        val edge = edgeByKey.getValue(connector.connectionKey)
        val progress by animateFloatAsState(
            targetValue = if (edge.visible) 1f else 0f,
            animationSpec = tween(
                durationMillis = TRADEOFF_NODE_TRANSITION_MILLIS,
                delayMillis = if (edge.visible) tradeoffConnectionRevealDelayMillis(model, edge) else 0
            ),
            label = "${edge.connectionKey}:drawing"
        )
        connector.connectionKey to progress
    }
    Box(
        modifier.size(layout.width.dp, layout.height.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TreeCanvasBackground)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            layout.connectors.forEach { connector ->
                val edge = edgeByKey.getValue(connector.connectionKey)
                val progress = animatedConnectionProgress.getValue(connector.connectionKey)
                val color = when (edge.visualState) {
                    ConnectionVisualState.HIGHLIGHTED -> TreeOrange
                    ConnectionVisualState.PATH -> TreeGreen
                    ConnectionVisualState.RELAXING -> TreeBlue
                    ConnectionVisualState.DEFAULT -> TreeMuted
                }
                val start = Offset(connector.line.start.x.dp.toPx(), connector.line.start.y.dp.toPx())
                val end = Offset(connector.line.end.x.dp.toPx(), connector.line.end.y.dp.toPx())
                drawLine(color = TreeMuted.copy(alpha = 0.48f), start = start, end = end, strokeWidth = 1.5.dp.toPx())
                if (progress > 0f) drawLine(
                    color = color.copy(alpha = color.alpha * progress),
                    start = start,
                    end = Offset(
                        start.x + (end.x - start.x) * progress,
                        start.y + (end.y - start.y) * progress
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        layout.barBounds.values.forEach { bar ->
            val node = nodeById.getValue(bar.nodeId)
            key(model.selection, node.id) {
                val revealDelay = tradeoffNodeRevealDelayMillis(model, node)
                val revealAlpha by animateFloatAsState(
                    targetValue = if (node.visible) 1f else 0.45f,
                    animationSpec = tween(
                        TRADEOFF_NODE_TRANSITION_MILLIS,
                        delayMillis = if (node.visible) revealDelay else 0
                    ),
                    label = "${node.id}:reveal-alpha"
                )
                val revealScale by animateFloatAsState(
                    targetValue = if (node.visible) 1f else 0.92f,
                    animationSpec = tween(
                        TRADEOFF_NODE_TRANSITION_MILLIS,
                        delayMillis = if (node.visible) revealDelay else 0
                    ),
                    label = "${node.id}:reveal-scale"
                )
                Box(
                    Modifier.offset { IntOffset(bar.bounds.left.dp.roundToPx(), bar.bounds.top.dp.roundToPx()) }
                        .size(bar.bounds.width.dp, bar.bounds.height.dp)
                        .graphicsLayer {
                            alpha = revealAlpha
                            scaleX = revealScale
                            scaleY = revealScale
                        }
                ) {
                    val label = when {
                        model.selection == TradeoffTreeSelection.UNBALANCED && bar.bounds.width >= 32f && bar.bounds.height >= 22f ->
                            "${node.size}\n${node.rangeLabel}"
                        model.selection == TradeoffTreeSelection.BALANCED && bar.bounds.width >= 34f -> node.rangeLabel
                        else -> node.size.toString()
                    }
                    PartitionBar(node, label, isMultiline = label.contains('\n'))
                }
            }
        }
    }
}

@Composable
private fun PartitionBar(node: TradeoffRecursionNode, label: String, isMultiline: Boolean) {
    val targetAccent = when (node.status) {
        TradeoffNodeStatus.PENDING -> TreeBlue
        TradeoffNodeStatus.ACTIVE -> TreeOrange
        TradeoffNodeStatus.COMPLETED -> TreeGreen
    }
    val accent by animateColorAsState(targetAccent, tween(320), label = "${node.id}:bar-state")
    val fillAccent = if (node.size == 1) TreeGreen else accent
    Box(
        Modifier.fillMaxSize().padding(horizontal = 1.dp).clip(RoundedCornerShape(4.dp))
            .background(fillAccent.copy(alpha = if (node.size == 1) 0.28f else 0.18f))
            .border(if (node.size == 1) 1.5.dp else 1.dp, accent, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = TreePrimary,
            fontSize = if (isMultiline || label.contains('[')) 9.sp else 10.sp,
            lineHeight = if (isMultiline) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TreeInspector(model: TradeoffTreePresentationModel, modifier: Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("COMPLETE RECURSION TREE · ${model.nodes.size} CALLS", color = TreePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        model.nodes.sortedWith(compareBy(TradeoffRecursionNode::depth, TradeoffRecursionNode::low)).forEach { node ->
            TreeNodeCard(node, Modifier.fillMaxWidth())
        }
        Text("ALL ${model.edges.size} CONNECTIONS", color = TreePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        model.edges.forEach { edge ->
            val state = when {
                !edge.visible || edge.visualState == ConnectionVisualState.DEFAULT -> "pending / unrevealed"
                edge.visualState == ConnectionVisualState.HIGHLIGHTED -> "active"
                edge.visualState == ConnectionVisualState.PATH -> "completed"
                else -> "transitioning"
            }
            Text(
                "${edge.connectionKey}: ${edge.parentId} → ${edge.childId} · $state",
                color = if (edge.visible) TreeSecondary else TreeMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun TreeNodeCard(node: TradeoffRecursionNode, modifier: Modifier) {
    val accent = when (node.status) {
        TradeoffNodeStatus.PENDING -> if (node.visible) TreeBlue else TreeMuted
        TradeoffNodeStatus.ACTIVE -> TreeOrange
        TradeoffNodeStatus.COMPLETED -> TreeGreen
    }
    Column(
        modifier.clip(RoundedCornerShape(7.dp)).background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text("N=${node.size} · ${node.rangeLabel}", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            "Depth ${node.depth} · ${node.branch?.let { "Branch $it" } ?: "ROOT"}" +
                (node.parentId?.let { " · Parent $it" } ?: "") +
                " · ${node.status.name.lowercase()} · ID ${node.id}",
            color = TreeSecondary,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun TreeSelector(label: String, selected: Boolean, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = if (selected) accent.copy(alpha = 0.13f) else TreePanelBackground,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) accent else Color(0xFF3A4654)),
        modifier = modifier.height(32.dp).clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) accent else TreeSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
