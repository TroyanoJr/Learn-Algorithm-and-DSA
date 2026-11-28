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

internal enum class KnapsackItemRole { PENDING, INCUMBENT_INTEGER, ACTIVE_INTEGER, RELAXATION_FULL, RELAXATION_PARTIAL }
internal data class KnapsackItemPresentation(
    val index: Int,
    val stableId: String,
    val item: BbKnapsackItem,
    val role: KnapsackItemRole,
    val incumbentMember: Boolean,
    val activeIntegerMember: Boolean,
    val fullRelaxation: Boolean,
    val partialRelaxation: Boolean,
    val rejectedAttempt: Boolean
)
internal enum class KnapsackCapacitySegmentKind { FIXED_INTEGER, RELAXATION_FULL, RELAXATION_PARTIAL }
internal data class KnapsackCapacitySegment(val id: String, val kind: KnapsackCapacitySegmentKind, val weight: Double, val value: Double, val fraction: Double)
internal data class KnapsackBranchBoundSceneFrame(val step: KnapsackBranchBoundStep, val integerWeight: Int, val items: List<KnapsackItemPresentation>, val capacitySegments: List<KnapsackCapacitySegment>, val isTerminal: Boolean)
internal object KnapsackBranchBoundSceneBuilder {
    fun build(steps: List<KnapsackBranchBoundStep>) = steps.map { step ->
        val weight = step.incumbentItemIndices.sumOf { step.items[it].weight }
        val roles = step.items.mapIndexed { index, item ->
            val contribution = step.currentNode?.fractionalContributions?.firstOrNull { it.itemIndex == index }
            val incumbentMember = index in step.incumbentItemIndices
            val activeMember = index in step.currentNode?.itemIndices.orEmpty() && !(step.currentNode?.state == KnapsackNodeState.INFEASIBLE && index == step.currentNode.level - 1)
            val full = contribution?.fraction == 1.0
            val partial = contribution != null && contribution.fraction < 1.0
            val rejected = step.currentNode?.state == KnapsackNodeState.INFEASIBLE && index == step.currentNode.level - 1
            val role = when {
                rejected -> KnapsackItemRole.PENDING
                incumbentMember -> KnapsackItemRole.INCUMBENT_INTEGER
                activeMember -> KnapsackItemRole.ACTIVE_INTEGER
                full -> KnapsackItemRole.RELAXATION_FULL
                partial -> KnapsackItemRole.RELAXATION_PARTIAL
                else -> KnapsackItemRole.PENDING
            }
            KnapsackItemPresentation(index, item.stableId, item, role, incumbentMember, activeMember, full, partial, rejected)
        }
        val node = step.currentNode
        val segments = buildList {
            if (node != null && node.currentWeight > 0 && node.state != KnapsackNodeState.INFEASIBLE) add(KnapsackCapacitySegment("fixed", KnapsackCapacitySegmentKind.FIXED_INTEGER, node.currentWeight.toDouble(), node.currentValue.toDouble(), 1.0))
            node?.fractionalContributions?.forEach { contribution -> add(KnapsackCapacitySegment(step.items[contribution.itemIndex].stableId, if (contribution.fraction < 1.0) KnapsackCapacitySegmentKind.RELAXATION_PARTIAL else KnapsackCapacitySegmentKind.RELAXATION_FULL, contribution.weight, contribution.value, contribution.fraction)) }
        }
        KnapsackBranchBoundSceneFrame(step, weight, roles, segments, step.isTerminal)
    }
}
internal data class KnapsackBranchBoundSceneLayout(val width: Float, val height: Float, val incumbent: Rect2D, val items: List<Rect2D>, val branch: Rect2D, val capacityBar: Rect2D, val contributions: Rect2D, val formula: Rect2D)
internal object KnapsackBranchBoundSceneLayoutCalculator {
    fun calculate(itemCount: Int): KnapsackBranchBoundSceneLayout {
        val columns = minOf(3, itemCount.coerceAtLeast(1)); val cardWidth = 124f
        val width = maxOf(280f, 32f + columns * cardWidth + (columns - 1) * 8f)
        val rows = if (itemCount == 0) 0 else (itemCount + columns - 1) / columns
        val cards = List(itemCount) { index -> val row = index / columns; val column = index % columns; Rect2D(16f + column * (cardWidth + 8f), 96f + row * 74f, cardWidth, 66f) }
        val branchTop = 96f + rows * 74f + 8f
        // The default three-item scene remains 320 dp high while retaining room for two formula lines.
        val branch = Rect2D(16f, branchTop, width - 32f, 130f)
        return KnapsackBranchBoundSceneLayout(width, branch.bottom + 12f, Rect2D(16f, 16f, width - 32f, 60f), cards, branch, Rect2D(branch.left + 8f, branch.top + 56f, branch.width - 16f, 12f), Rect2D(branch.left + 8f, branch.top + 72f, branch.width - 16f, 14f), Rect2D(branch.left + 8f, branch.top + 88f, branch.width - 16f, 34f))
    }
}
@Composable internal fun KnapsackBranchBoundSceneRenderer(frame: KnapsackBranchBoundSceneFrame, modifier: Modifier = Modifier) {
    val layout = remember(frame.step.items.size) { KnapsackBranchBoundSceneLayoutCalculator.calculate(frame.step.items.size) }; val measure = rememberTextMeasurer()
    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, rememberVisualizerCanvasState(key = "knapsack-branch-bound")) {
        Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
            fun card(r: Rect2D, c: Color) { drawRoundRect(Color(0xFF13131C),Offset(r.left.dp.toPx(),r.top.dp.toPx()),Size(r.width.dp.toPx(),r.height.dp.toPx()),CornerRadius(7.dp.toPx())); drawRoundRect(c,Offset(r.left.dp.toPx(),r.top.dp.toPx()),Size(r.width.dp.toPx(),r.height.dp.toPx()),CornerRadius(7.dp.toPx()),style=Stroke(1.dp.toPx())) }
            fun text(v: String, r: Rect2D, c: Color = Color.White, size: Int = 9) { val m=measure.measure(v,TextStyle(fontSize=size.sp,fontWeight=FontWeight.Bold),constraints=Constraints(maxWidth=(r.width-10).dp.roundToPx().coerceAtLeast(1))); drawText(textLayoutResult = m, topLeft = Offset((r.left+6).dp.toPx(),(r.top+6).dp.toPx()),color=c) }
            fun number(value: Double): String {
                if (value == value.toInt().toDouble()) return value.toInt().toString()
                val rounded = kotlin.math.round(value * 10.0) / 10.0
                return if (rounded == value) rounded.toString() else "≈$rounded"
            }
            card(layout.incumbent, Color(0xFF10B981)); text("INTEGER INCUMBENT · value ${frame.step.incumbentValue}", layout.incumbent, Color(0xFF10B981), 11); text("items ${frame.step.incumbentItems.joinToString().ifEmpty { "none" }} · weight ${frame.integerWeight} / ${frame.step.capacity}", layout.incumbent.copy(top=42f), Color.White, 9)
            text("ITEMS · DESCENDING VALUE / WEIGHT", Rect2D(16f,78f,300f,18f), Color(0xFF38BDF8), 9)
            frame.items.forEachIndexed { index, presentation -> val item = presentation.item; val r=layout.items[index]; val c=when { presentation.rejectedAttempt -> Color(0xFFEF4444); presentation.incumbentMember -> Color(0xFF10B981); presentation.activeIntegerMember -> Color(0xFF38BDF8); presentation.fullRelaxation || presentation.partialRelaxation -> Color(0xFFFA6E13); else -> Color(0xFF526071) }; val labels = buildList { if (presentation.incumbentMember) add("INCUMBENT"); if (presentation.activeIntegerMember) add("ACTIVE"); if (presentation.fullRelaxation) add("RELAX FULL"); if (presentation.partialRelaxation) add("RELAX PARTIAL"); if (presentation.rejectedAttempt) add("ATTEMPT REJECTED") }.ifEmpty { listOf("PENDING") }.joinToString(" · "); card(r,c); text(item.name,r,Color.White,10); text("w ${item.weight} · v ${item.value} · r ${item.ratio}",r.copy(top=r.top+20),Color(0xFF9CA3AF),8); text(labels,r.copy(top=r.top+40),c,7) }
            val node=frame.step.currentNode; val c=when(node?.state){KnapsackNodeState.PRUNED,KnapsackNodeState.INFEASIBLE->Color(0xFFEF4444);KnapsackNodeState.INCUMBENT->Color(0xFF10B981);else->Color(0xFFFA6E13)}; card(layout.branch,c); text("ACTIVE BRANCH · ${node?.id ?: if (frame.isTerminal) "search complete" else "initializing"}",layout.branch,c,9); text("level ${node?.level ?: "—"} · ${if (node?.state == KnapsackNodeState.INFEASIBLE) "attempted" else "integer"} w ${node?.currentWeight ?: "—"} · v ${node?.currentValue ?: "—"} · UB ${node?.upperBound ?: "—"}",layout.branch.copy(top=layout.branch.top+20f),Color.White,8); val selectedLabel = (if (node?.state == KnapsackNodeState.INFEASIBLE) node.itemsTaken.dropLast(1) else node?.itemsTaken.orEmpty()).joinToString().ifEmpty { "none" }; text("selected: $selectedLabel · remaining ${if (node?.state == KnapsackNodeState.INFEASIBLE) "N/A (rejected)" else (frame.step.capacity - (node?.currentWeight ?: 0)).coerceAtLeast(0).toString()}",layout.branch.copy(top=layout.branch.top+42f),Color(0xFF9CA3AF),8)
            val capacity = frame.step.capacity
            val barLeft = layout.capacityBar.left.dp.toPx(); val barTop = layout.capacityBar.top.dp.toPx(); val barWidth = layout.capacityBar.width.dp.toPx(); val barHeight = layout.capacityBar.height.dp.toPx()
            drawRoundRect(Color(0xFF262634), Offset(barLeft, barTop), Size(barWidth, barHeight), CornerRadius(4.dp.toPx()))
            if (capacity > 0) {
                var used = 0.0
                frame.capacitySegments.forEach { segment -> val drawableWeight = segment.weight.coerceAtLeast(0.0).coerceAtMost((capacity - used).coerceAtLeast(0.0)); val widthPx = barWidth * (drawableWeight / capacity).toFloat(); if (widthPx > 0f) { val color = when (segment.kind) { KnapsackCapacitySegmentKind.FIXED_INTEGER -> Color(0xFF38BDF8); KnapsackCapacitySegmentKind.RELAXATION_FULL -> Color(0xFF10B981); KnapsackCapacitySegmentKind.RELAXATION_PARTIAL -> Color(0xFFFA6E13) }; drawRect(color, Offset(barLeft + barWidth * (used / capacity).toFloat(), barTop), Size(widthPx, barHeight)) }; used += drawableWeight }
            }
            val contributionLabel = node?.fractionalContributions.orEmpty().joinToString(" · ") { contribution -> "${contribution.itemName}: w ${number(contribution.weight)}, v ${number(contribution.value)}, ${(kotlin.math.round(contribution.fraction * 1000.0) / 10.0)}% item" }
            text(if (contributionLabel.isEmpty()) "no fractional contribution" else contributionLabel, layout.contributions, Color(0xFF9CA3AF),7)
            val terms = node?.fractionalContributions.orEmpty().map { number(it.value) }
            val formula = (listOfNotNull(node?.currentValue?.takeIf { it != 0 }?.toString()) + terms).joinToString(" + ").ifEmpty { "0" }
            text("capacity bar: blue fixed integer · green full relaxation · orange fractional", layout.formula, Color(0xFF9CA3AF),7)
            val stateSpecific = when (frame.step.action) { KnapsackBbAction.INIT -> "initializing: no bound computed"; KnapsackBbAction.FINISHED -> "search complete: no active bound"; KnapsackBbAction.REJECT_INFEASIBLE -> "attempt rejected: w ${node?.currentWeight}, v ${node?.currentValue} exceeds capacity $capacity"; else -> "no active upper bound" }
            val formulaLabel = node?.upperBound?.let { upperBound ->
                val comparison = node.comparisonThreshold?.let { threshold -> " · exact floor ${node.upperBoundFloor} ≤ $threshold" }.orEmpty()
                "UB formula: $formula = ${number(upperBound)}$comparison"
            } ?: stateSpecific
            text(formulaLabel,layout.formula.copy(top=layout.formula.top+14f),c,8)
        }
    }
}
