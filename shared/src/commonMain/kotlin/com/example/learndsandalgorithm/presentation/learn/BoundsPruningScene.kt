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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class BoundsNodeState { PENDING, KEEP, PRUNED, INCUMBENT }
internal data class BoundsNodePresentation(val id: String, val bound: Int, val value: Int? = null, val state: BoundsNodeState, val threshold: Int? = null)
internal data class BoundsPruningSceneFrame(
    val step: BoundsPruningStep,
    val minNodes: List<BoundsNodePresentation>,
    val maxNodes: List<BoundsNodePresentation>,
    val activeId: String?,
    val isTerminal: Boolean
)
internal object BoundsPruningSceneBuilder {
    fun build(steps: List<BoundsPruningStep>): List<BoundsPruningSceneFrame> {
        val states = mutableMapOf("Node A" to BoundsNodeState.PENDING, "Node B" to BoundsNodeState.PENDING, "Node X" to BoundsNodeState.PENDING, "Node Y" to BoundsNodeState.PENDING, "Leaf X" to BoundsNodeState.PENDING)
        // Branch bounds describe the fixed problem tree, while a feasible leaf is hidden until its trace event.
        val bounds = steps.mapNotNull { step -> step.evaluatedNodeId?.let { id -> step.evaluatedBound?.let { id to it } } }.toMap().toMutableMap()
        val thresholds = mutableMapOf<String, Int>()
        return steps.map { step ->
            val id = step.evaluatedNodeId
            id?.let { nodeId ->
                step.evaluatedBound?.let { bound -> bounds[nodeId] = bound }
                step.comparisonThreshold?.let { threshold -> thresholds[nodeId] = threshold }
            }
            if (step.action == BoundsPruningAction.UPDATE_INCUMBENT) states["Leaf X"] = BoundsNodeState.INCUMBENT
            else if (id != null && step.isPruned != null) states[id] = if (step.isPruned) BoundsNodeState.PRUNED else BoundsNodeState.KEEP
            BoundsPruningSceneFrame(step,
                listOfNotNull(
                    bounds["Node A"]?.let { BoundsNodePresentation("Node A", it, state = states.getValue("Node A"), threshold = thresholds["Node A"]) },
                    bounds["Node B"]?.let { BoundsNodePresentation("Node B", it, state = states.getValue("Node B"), threshold = thresholds["Node B"]) }
                ),
                listOfNotNull(
                    bounds["Node X"]?.let { BoundsNodePresentation("Node X", it, state = states.getValue("Node X"), threshold = thresholds["Node X"]) },
                    bounds["Node Y"]?.let { BoundsNodePresentation("Node Y", it, state = states.getValue("Node Y"), threshold = thresholds["Node Y"]) },
                    if (states.getValue("Leaf X") == BoundsNodeState.INCUMBENT) bounds["Leaf X"]?.let { BoundsNodePresentation("Leaf X", it, it, BoundsNodeState.INCUMBENT, thresholds["Leaf X"]) } else null
                ),
                activeId = id?.takeIf { !step.isTerminal }, isTerminal = step.isTerminal)
        }
    }
}
internal data class BoundsPruningSceneLayout(val width: Float = 420f, val height: Float = 270f, val min: List<Rect2D>, val max: List<Rect2D>)
internal object BoundsPruningSceneLayoutCalculator { fun calculate() = BoundsPruningSceneLayout(min = listOf(Rect2D(20f, 84f, 180f, 45f), Rect2D(20f, 148f, 180f, 45f)), max = listOf(Rect2D(220f, 84f, 180f, 45f), Rect2D(220f, 148f, 180f, 45f), Rect2D(220f, 212f, 180f, 45f))) }
@Composable internal fun BoundsPruningSceneRenderer(frame: BoundsPruningSceneFrame, modifier: Modifier = Modifier) {
    val layout = remember { BoundsPruningSceneLayoutCalculator.calculate() }; val measure = rememberTextMeasurer()
    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, rememberVisualizerCanvasState(key = "bounds-pruning")) {
        Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
            fun text(v: String, x: Float, y: Float, color: Color, bold: Boolean = false) { val m = measure.measure(v, TextStyle(fontSize = 10.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), constraints = Constraints(maxWidth = 180.dp.roundToPx())); drawText(textLayoutResult = m, topLeft = Offset(x.dp.toPx(), y.dp.toPx()), color = color) }
            fun card(r: Rect2D, node: BoundsNodePresentation, active: Boolean, maximization: Boolean) { val c = when { active -> Color(0xFFFA6E13); node.state == BoundsNodeState.PRUNED -> Color(0xFFEF4444); node.state == BoundsNodeState.KEEP || node.state == BoundsNodeState.INCUMBENT -> Color(0xFF10B981); else -> Color(0xFF526071) }; drawRoundRect(Color(0xFF13131C), Offset(r.left.dp.toPx(), r.top.dp.toPx()), Size(r.width.dp.toPx(), r.height.dp.toPx()), CornerRadius(7.dp.toPx())); drawRoundRect(c, Offset(r.left.dp.toPx(), r.top.dp.toPx()), Size(r.width.dp.toPx(), r.height.dp.toPx()), CornerRadius(7.dp.toPx()), style = Stroke(1.dp.toPx())); text("${node.id} · ${if (node.value != null) "VALUE" else if (maximization) "UB" else "LB"} ${node.value ?: node.bound}", r.left + 8, r.top + 8, Color.White, true); val verdict = node.threshold?.let { threshold -> if (maximization) "${node.value ?: node.bound} ${if (node.state == BoundsNodeState.INCUMBENT) ">" else if (node.state == BoundsNodeState.PRUNED) "≤" else ">"} $threshold · ${node.state.name}" else "${node.bound} ${if (node.state == BoundsNodeState.PRUNED) "≥" else "<"} $threshold · ${node.state.name}" } ?: if (active) "EVALUATING" else "PENDING"; text(verdict, r.left + 8, r.top + 25, c) }
            text("MINIMIZATION · lower bound ≥ incumbent", 20f, 18f, Color(0xFF38BDF8), true); text("MAXIMIZATION · upper bound ≤ incumbent", 220f, 18f, Color(0xFF10B981), true)
            text("incumbent ${frame.step.minIncumbent}", 20f, 42f, Color.White, true); text("incumbent ${frame.step.maxIncumbent}", 220f, 42f, Color.White, true)
            frame.minNodes.forEachIndexed { index, node -> card(layout.min[index], node, frame.activeId == node.id, false) }
            frame.maxNodes.forEach { node ->
                val index = when (node.id) { "Node X" -> 0; "Node Y" -> 1; else -> 2 }
                card(layout.max[index], node, frame.activeId == node.id, true)
            }
        }
    }
}
