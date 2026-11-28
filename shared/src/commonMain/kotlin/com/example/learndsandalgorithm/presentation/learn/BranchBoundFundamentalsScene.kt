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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class FundamentalsNodeState { PENDING, ACTIVE, EXPLORED, PRUNED, INCUMBENT, OPTIMAL }
internal data class FundamentalsNodePresentation(
    val id: String,
    val lowerBound: Int,
    val state: FundamentalsNodeState,
    val feasibleCost: Int? = null,
    val comparisonThreshold: Int? = null
)
internal data class BranchBoundFundamentalsSceneFrame(
    val nodes: List<FundamentalsNodePresentation>,
    val connections: List<Pair<String, String>>,
    val incumbentCost: Int?,
    val incumbentId: String?,
    val activeId: String?,
    val isTerminal: Boolean
)
internal object BranchBoundFundamentalsSceneBuilder {
    private val bounds = mapOf("Root" to 8, "Node A" to 12, "Node B" to 18, "Node C" to 8, "Leaf A" to 15, "Leaf C" to 8)
    fun build(steps: List<BranchBoundFundamentalsStep>): List<BranchBoundFundamentalsSceneFrame> {
        val states = bounds.keys.associateWith { FundamentalsNodeState.PENDING }.toMutableMap()
        val discoveredLeafCosts = mutableMapOf<String, Int>()
        val comparisonThresholds = mutableMapOf<String, Int>()
        var incumbentId: String? = null
        return steps.map { step ->
            // Resolved outcomes outrank the current focus; a pruning event must stay pruned.
            states["Root"] = FundamentalsNodeState.EXPLORED
            step.exploredNodeIds.forEach { states[it] = FundamentalsNodeState.EXPLORED }
            step.prunedNodeIds.forEach { states[it] = FundamentalsNodeState.PRUNED }
            step.currentNodeId?.let { id ->
                step.comparisonThreshold?.let { threshold -> comparisonThresholds[id] = threshold }
            }
            if (step.action == BbFundamentalsAction.FOUND_FEASIBLE && step.currentNodeId != null) {
                incumbentId?.let { states[it] = FundamentalsNodeState.EXPLORED }
                incumbentId = step.currentNodeId
                discoveredLeafCosts[incumbentId!!] = step.currentNodeBound ?: error("Feasible leaf requires a cost")
                states[incumbentId!!] = FundamentalsNodeState.INCUMBENT
            }
            val active = step.currentNodeId?.takeIf { states[it] !in setOf(FundamentalsNodeState.PRUNED, FundamentalsNodeState.INCUMBENT) && !step.isTerminal }
            active?.let { states[it] = FundamentalsNodeState.ACTIVE }
            if (step.isTerminal) incumbentId?.let { states[it] = FundamentalsNodeState.OPTIMAL }
            BranchBoundFundamentalsSceneFrame(
                nodes = bounds.map { (id, bound) -> FundamentalsNodePresentation(id, bound, states.getValue(id), discoveredLeafCosts[id], comparisonThresholds[id]) },
                connections = listOf("Root" to "Node A", "Root" to "Node B", "Root" to "Node C", "Node A" to "Leaf A", "Node C" to "Leaf C"),
                incumbentCost = step.incumbentCost,
                incumbentId = incumbentId,
                activeId = active,
                isTerminal = step.isTerminal
            )
        }
    }
}
internal data class BranchBoundFundamentalsSceneLayout(val width: Float = 420f, val height: Float = 270f, val nodes: Map<String, Rect2D>, val incumbent: Rect2D)
internal object BranchBoundFundamentalsSceneLayoutCalculator {
    fun calculate(measuredCardWidth: Float = 90f): BranchBoundFundamentalsSceneLayout {
        val card = measuredCardWidth.coerceAtLeast(72f)
        val gap = 24f
        val width = card * 3f + gap * 4f
        val x = listOf(gap, gap * 2f + card, gap * 3f + card * 2f)
        val rootX = (width - card) / 2f
        return BranchBoundFundamentalsSceneLayout(width, 270f, mapOf(
            "Root" to Rect2D(rootX, 18f, card, 40f), "Node A" to Rect2D(x[0], 100f, card, 40f), "Node B" to Rect2D(x[1], 100f, card, 40f), "Node C" to Rect2D(x[2], 100f, card, 40f),
            "Leaf A" to Rect2D(x[0], 188f, card, 40f), "Leaf C" to Rect2D(x[2], 188f, card, 40f)
        ), incumbent = Rect2D(gap, 238f, width - gap * 2f, 22f))
    }
}
@Composable internal fun BranchBoundFundamentalsSceneRenderer(frame: BranchBoundFundamentalsSceneFrame, modifier: Modifier = Modifier) {
    val measure = rememberTextMeasurer()
    val density = LocalDensity.current
        val measuredCardWidth = frame.nodes.maxOf { node ->
        val label = if (node.feasibleCost != null) "${node.id} · cost ${node.feasibleCost}" else "${node.id} · LB ${node.lowerBound}"
        val comparison = node.comparisonThreshold?.let { "${node.lowerBound} ≥ $it · ${node.state.name}" }.orEmpty()
        maxOf(
            measure.measure(label, TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold)).size.width.toFloat(),
            measure.measure(comparison, TextStyle(fontSize = 7.sp)).size.width.toFloat()
        ) / density.density + 14f
    }
    val layout = remember(measuredCardWidth) { BranchBoundFundamentalsSceneLayoutCalculator.calculate(measuredCardWidth) }
    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, rememberVisualizerCanvasState(key = "branch-bound-fundamentals")) {
        Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
            fun boundaryPoint(source: Rect2D, target: Rect2D): Offset {
                val sx = source.left + source.width / 2f; val sy = source.top + source.height / 2f
                val tx = target.left + target.width / 2f; val ty = target.top + target.height / 2f
                val dx = tx - sx; val dy = ty - sy
                val scale = minOf((source.width / 2f) / kotlin.math.abs(dx).coerceAtLeast(0.001f), (source.height / 2f) / kotlin.math.abs(dy).coerceAtLeast(0.001f))
                return Offset((sx + dx * scale).dp.toPx(), (sy + dy * scale).dp.toPx())
            }
            fun line(a: String, b: String) { val source = layout.nodes.getValue(a); val target = layout.nodes.getValue(b); drawLine(Color(0xFF526071), boundaryPoint(source, target), boundaryPoint(target, source), 1.dp.toPx()) }
            frame.connections.forEach { (parent, child) -> line(parent, child) }
            drawRoundRect(Color(0xFF13131C), Offset(layout.incumbent.left.dp.toPx(), layout.incumbent.top.dp.toPx()), Size(layout.incumbent.width.dp.toPx(), layout.incumbent.height.dp.toPx()), CornerRadius(6.dp.toPx()))
            drawRoundRect(Color(0xFF10B981), Offset(layout.incumbent.left.dp.toPx(), layout.incumbent.top.dp.toPx()), Size(layout.incumbent.width.dp.toPx(), layout.incumbent.height.dp.toPx()), CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
            val incumbentLabel = if (frame.incumbentCost == null) "INCUMBENT · ∞ · no feasible solution" else "INCUMBENT · ${frame.incumbentId} · cost ${frame.incumbentCost}" + if (frame.isTerminal) " · OPTIMAL" else ""
            val incumbentText = measure.measure(incumbentLabel, TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold), constraints = Constraints(maxWidth = (layout.incumbent.width - 10f).dp.roundToPx()))
            drawText(textLayoutResult = incumbentText, topLeft = Offset((layout.incumbent.left + 5f).dp.toPx(), (layout.incumbent.top + 5f).dp.toPx()), color = Color(0xFF10B981))
            layout.nodes.forEach { (id, r) ->
                val node = frame.nodes.first { it.id == id }; val state = node.state; val color = when (state) { FundamentalsNodeState.ACTIVE -> Color(0xFFFA6E13); FundamentalsNodeState.PRUNED -> Color(0xFFEF4444); FundamentalsNodeState.INCUMBENT, FundamentalsNodeState.OPTIMAL -> Color(0xFF10B981); FundamentalsNodeState.EXPLORED -> Color(0xFF38BDF8); else -> Color(0xFF526071) }
                drawRoundRect(Color(0xFF13131C), Offset(r.left.dp.toPx(), r.top.dp.toPx()), Size(r.width.dp.toPx(), r.height.dp.toPx()), CornerRadius(7.dp.toPx()))
                drawRoundRect(color, Offset(r.left.dp.toPx(), r.top.dp.toPx()), Size(r.width.dp.toPx(), r.height.dp.toPx()), CornerRadius(7.dp.toPx()), style = Stroke(1.dp.toPx()))
                val label = when {
                    node.feasibleCost != null -> "$id · cost ${node.feasibleCost}"
                    id.startsWith("Leaf") -> "$id · pending"
                    else -> "$id · LB ${node.lowerBound}"
                }
                val text = measure.measure(label, TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold), constraints = Constraints(maxWidth = r.width.dp.roundToPx()))
                drawText(textLayoutResult = text, topLeft = Offset(r.left.dp.toPx() + 5.dp.toPx(), r.top.dp.toPx() + 6.dp.toPx()), color = Color.White)
                val status = node.comparisonThreshold?.let { "${node.lowerBound} ≥ $it · ${state.name}" } ?: state.name
                val st = measure.measure(status, TextStyle(fontSize = 7.sp), constraints = Constraints(maxWidth = r.width.dp.roundToPx()))
                drawText(textLayoutResult = st, topLeft = Offset(r.left.dp.toPx() + 5.dp.toPx(), r.top.dp.toPx() + 22.dp.toPx()), color = color)
            }
        }
    }
}
