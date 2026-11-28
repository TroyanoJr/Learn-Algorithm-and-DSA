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

internal data class BestFirstPrunePresentation(val id: String, val bound: Int, val threshold: Int)
internal enum class BestFirstQueueState { INITIAL_EMPTY, POPULATED, EMPTIED_BY_PRUNING, FINISHED_EMPTY }
internal data class BestFirstSearchSceneFrame(val step: BestFirstSearchStep, val queueNodes: List<PqNode>, val orderedQueueIds: List<String>, val queueCapacity: Int, val extracted: PqNode?, val showActualCost: Boolean, val incumbent: PqNode?, val pruned: List<BestFirstPrunePresentation>, val queueState: BestFirstQueueState, val isTerminal: Boolean)
internal object BestFirstSearchSceneBuilder {
    fun build(steps: List<BestFirstSearchStep>): List<BestFirstSearchSceneFrame> {
        var incumbent: PqNode? = null
        val queueCapacity = steps.maxOfOrNull { it.priorityQueue.size } ?: 0
        return steps.map { step ->
            if (step.action == BestFirstAction.FOUND_FEASIBLE) incumbent = step.extractedNode
            val queueState = when {
                step.action == BestFirstAction.INIT -> BestFirstQueueState.INITIAL_EMPTY
                step.action == BestFirstAction.PRUNE_REMAINING_PQ -> BestFirstQueueState.EMPTIED_BY_PRUNING
                step.isTerminal -> BestFirstQueueState.FINISHED_EMPTY
                else -> BestFirstQueueState.POPULATED
            }
            BestFirstSearchSceneFrame(step, step.priorityQueue, step.priorityQueue.map(PqNode::id), queueCapacity, step.extractedNode, step.action == BestFirstAction.FOUND_FEASIBLE, incumbent, step.prunedNodes.map { BestFirstPrunePresentation(it.id, it.bound, step.incumbentCost ?: 0) }, queueState, step.isTerminal)
        }
    }
}
internal data class BestFirstSearchSceneLayout(val width: Float, val height: Float = 208f, val queue: List<Rect2D>, val extracted: Rect2D, val pruned: Rect2D)
internal object BestFirstSearchSceneLayoutCalculator {
    fun calculate(queueCount: Int = 4): BestFirstSearchSceneLayout {
        val count = queueCount.coerceAtLeast(1)
        val cardWidth = 84f; val gap = 8f
        // Header, subtitle, incumbent, and pruning card need this minimum even when the queue is empty.
        val width = maxOf(422f, 36f + count * cardWidth + (count - 1) * gap)
        val queue = List(queueCount) { Rect2D(18f + it * (cardWidth + gap), 68f, cardWidth, 46f) }
        return BestFirstSearchSceneLayout(width, queue = queue, extracted = Rect2D(18f, 142f, 184f, 45f), pruned = Rect2D(218f, 142f, width - 236f, 45f))
    }
}
@Composable internal fun BestFirstSearchSceneRenderer(frame: BestFirstSearchSceneFrame, modifier: Modifier = Modifier) {
    val layout = remember(frame.queueCapacity) { BestFirstSearchSceneLayoutCalculator.calculate(frame.queueCapacity) }; val measure = rememberTextMeasurer()
    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, rememberVisualizerCanvasState(key = "best-first-search")) {
        Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
            fun label(v: String, r: Rect2D, c: Color = Color.White, size: Int = 9) { val m = measure.measure(v, TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), constraints = Constraints(maxWidth = (r.width - 10f).dp.roundToPx().coerceAtLeast(1))); drawText(textLayoutResult = m, topLeft = Offset((r.left + 5).dp.toPx(), (r.top + 7).dp.toPx()), color = c) }
            fun card(r: Rect2D, c: Color) { drawRoundRect(Color(0xFF13131C), Offset(r.left.dp.toPx(),r.top.dp.toPx()), Size(r.width.dp.toPx(),r.height.dp.toPx()), CornerRadius(7.dp.toPx())); drawRoundRect(c, Offset(r.left.dp.toPx(),r.top.dp.toPx()),Size(r.width.dp.toPx(),r.height.dp.toPx()),CornerRadius(7.dp.toPx()),style=Stroke(1.dp.toPx())) }
            label("PQ · MIN-BOUND FIRST", Rect2D(18f, 16f, 198f, 20f), Color(0xFF38BDF8), 10); label("Logical order — not a heap-array diagram", Rect2D(18f, 38f, 380f, 16f), Color(0xFF9CA3AF), 8)
            val incumbentLabel = frame.incumbent?.let { "INCUMBENT ${it.id} · cost ${it.cost}" + if (frame.isTerminal) " · OPTIMAL" else "" } ?: "INCUMBENT ∞ · no feasible solution"
            label(incumbentLabel, Rect2D(226f, 16f, layout.width - 244f, 20f), Color(0xFF10B981), 8)
            frame.queueNodes.forEachIndexed { index, node -> val r = layout.queue[index]; card(r, if (index == 0) Color(0xFFFA6E13) else Color(0xFF526071)); label(if (index == 0) "TOP (MIN)" else "priority ${index + 1}", r, if (index == 0) Color(0xFFFA6E13) else Color(0xFF9CA3AF), 7); label("${node.id} · LB ${node.bound}", r.copy(top = r.top + 17), Color.White, 9) }
            if (frame.orderedQueueIds.isEmpty()) label(when (frame.queueState) { BestFirstQueueState.INITIAL_EMPTY -> "QUEUE EMPTY · waiting for candidates"; BestFirstQueueState.EMPTIED_BY_PRUNING -> "QUEUE CLEARED · ${frame.pruned.size} nodes safely pruned"; BestFirstQueueState.FINISHED_EMPTY -> "QUEUE EMPTY · search complete"; else -> "QUEUE EMPTY" }, Rect2D(18f, 82f, layout.width - 36f, 22f), Color(0xFF9CA3AF), 9)
            frame.extracted?.let { n -> card(layout.extracted, if (frame.showActualCost) Color(0xFF10B981) else Color(0xFFFA6E13)); label("EXTRACTED ${n.id}", layout.extracted); label("lower bound ${n.bound}" + if (frame.showActualCost) " · cost ${n.cost}" else "", layout.extracted.copy(top = 160f), Color.White, 8) }
            if (frame.pruned.isNotEmpty()) { card(layout.pruned, Color(0xFFEF4444)); label("PRUNED: ${frame.pruned.joinToString { "${it.id} LB ${it.bound}" }}", layout.pruned, Color(0xFFEF4444), 8); label("minimum ${frame.pruned.minOf { it.bound }} ≥ ${frame.pruned.first().threshold} · ${frame.pruned.size} nodes", layout.pruned.copy(top = 160f), Color.White, 8) }
        }
    }
}
