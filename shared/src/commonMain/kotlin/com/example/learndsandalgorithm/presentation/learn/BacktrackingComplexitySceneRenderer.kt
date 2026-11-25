package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

private val ComplexityRegion = Color(0xFF13131C)
private val ComplexityBorder = Color(0xFF262634)
private val ComplexityOrange = Color(0xFFFA6E13)
private val ComplexityGreen = Color(0xFF10B981)
private val ComplexityBlue = Color(0xFF38BDF8)
private val ComplexityRed = Color(0xFFEF4444)
private val ComplexitySecondary = Color(0xFF9CA3AF)
private val ComplexityParagraphs = listOf(
    "• This comparison illustrates pruning efficiency on this specific input instance, not a universal performance guarantee.",
    "• Pruning reduces work when a valid condition safely eliminates branches early.",
    "• In the worst case (when no branches are pruned), worst-case time complexity remains exponential (O(2^N) or O(N!))."
)
private fun complexityContext(frame: ComplexitySceneFrame) = if (frame.sortedInput)
    "Sorted input makes the skipped suffix safe. O(H) describes theoretical recursive stack space; this demonstration uses iterative loops."
else "Input is not sorted: stopping at the first rejected value may skip later valid values. O(H) is theoretical recursive stack space; this demonstration uses iterative loops."
private fun complexitySavings(frame: ComplexitySceneFrame): String = if (frame.savedCalls == null) {
    if (frame.step.currentIndex == null) "Awaiting evaluation."
    else "Current index ${frame.step.currentIndex} · value ${frame.step.currentValue}"
} else "Saved ${frame.savedCalls} call(s) · ${formatGreedyDecimal(frame.savingsPercent ?: 0.0)}% reduction"

@Composable
internal fun BacktrackingComplexitySceneRenderer(presentation: ComplexityPresentation, frame: ComplexitySceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun extent(text: String, size: Int, bold: Boolean = true, widthDp: Float? = null): Pair<Float, Float> {
            val constraints = if (widthDp == null) Constraints() else Constraints(maxWidth = with(density) { widthDp.dp.roundToPx() }.coerceAtLeast(1))
            val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = widthDp != null, constraints = constraints)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(texts: Iterable<String>, size: Int, bold: Boolean = true, width: Float? = null): Pair<Float, Float> {
            val all = texts.map { extent(it, size, bold, width) }
            return (all.maxOfOrNull { it.first } ?: 1f) to (all.maxOfOrNull { it.second } ?: 1f)
        }
        val title = extent("PRUNING EFFICIENCY TEST", 10)
        val badge = extent("STACK SPACE O(H)", 9)
        val inputs = maximum(presentation.frames.map { "Array: ${it.step.array} · Condition: x ≤ ${it.step.limit}" }, 10, false)
        val headings = maximum(listOf("UNPRUNED (BRUTE FORCE)", "PRUNED (BACKTRACKING)"), 10)
        val calls = maximum(presentation.frames.flatMap { listOf("${it.step.unprunedCalls} Calls", "${it.step.prunedCalls} Calls") }, 16)
        val values = maximum(presentation.elements.map { it.value.toString() }, 10)
        val states = maximum(ComplexityElementState.entries.map { it.name }, 9)
        val explanationHeading = extent("PRUNING & WORST-CASE COMPLEXITY:", 10)
        val initial = ComplexityLayoutMetrics(title.first, badge.first + 12f, maxOf(title.second, badge.second + 6f),
            inputs.first, inputs.second, headings.first, headings.second, calls.second, 1f,
            maxOf(values.first, states.first) + 8f, values.second + states.second + 4f,
            explanationHeading.first, explanationHeading.second, listOf(1f, 1f, 1f, 1f), 1f)
        val descriptions = listOf("Evaluates all ${presentation.elements.size} elements unconditionally",
            "Halts recursion when val > ${frame.step.limit}")
        // More horizontal space can put all evaluated elements on one row and reduce
        // paragraph wrapping; compare the complete measured scenes at each width.
        val candidates = listOf(1f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f).map { multiplier ->
            val candidateWidth = maxWidth.value * multiplier
            val preliminary = BacktrackingComplexitySceneLayout.calculate(presentation, candidateWidth, maxHeight.value, initial)
            val cardTextWidth = preliminary.unprunedExplanation.width
            val paragraphWidth = preliminary.explanationHeading.width
            val descriptionHeight = maximum(descriptions, 9, false, cardTextWidth).second
            val paragraphHeights = ComplexityParagraphs.map { extent(it, 9, false, paragraphWidth).second } +
                maximum(presentation.frames.map(::complexityContext), 9, false, paragraphWidth).second
            val savingsHeight = maximum(presentation.frames.map(::complexitySavings), 9, true, paragraphWidth).second
            val metrics = initial.copy(strategyExplanationHeight = descriptionHeight, paragraphHeights = paragraphHeights, savingsHeight = savingsHeight)
            BacktrackingComplexitySceneLayout.calculate(presentation, candidateWidth, maxHeight.value, metrics)
        }
        val layout = BacktrackingComplexitySceneLayout.choose(maxWidth.value, maxHeight.value, candidates)
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "backtracking-complexity", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(rect: ComplexityRect, fill: Color, border: Color) {
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                fun label(text: String, rect: ComplexityRect, color: Color, size: Int, bold: Boolean = true, centered: Boolean = false, wrap: Boolean = false) {
                    val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal), softWrap = wrap,
                        constraints = if (wrap) Constraints(maxWidth = rect.width.dp.roundToPx().coerceAtLeast(1)) else Constraints())
                    val x = rect.x.dp.toPx() + if (centered) (rect.width.dp.toPx() - result.size.width) / 2f else 0f
                    drawText(result, topLeft = Offset(x, rect.y.dp.toPx() + if (wrap) 0f else (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                card(layout.header, ComplexityRegion, ComplexityBorder)
                label("PRUNING EFFICIENCY TEST", layout.headerTitle, ComplexityOrange, 10)
                card(layout.badge, Color(0xFF123820), ComplexityGreen)
                label("STACK SPACE O(H)", layout.badge, ComplexityGreen, 9, centered = true)
                label("Array: ${frame.step.array} · Condition: x ≤ ${frame.step.limit}", layout.input, ComplexitySecondary, 10, false)
                card(layout.unprunedCard, ComplexityRegion, ComplexityBorder)
                card(layout.prunedCard, ComplexityRegion, if (frame.step.prunedCalls > 0) ComplexityGreen else ComplexityBorder)
                label("UNPRUNED (BRUTE FORCE)", layout.unprunedHeading, ComplexityOrange, 10, centered = true)
                label("PRUNED (BACKTRACKING)", layout.prunedHeading, ComplexityGreen, 10, centered = true)
                label("${frame.step.unprunedCalls} Calls", layout.unprunedCalls, Color.White, 16, centered = true)
                label("${frame.step.prunedCalls} Calls", layout.prunedCalls, ComplexityGreen, 16, centered = true)
                label("Evaluates all ${presentation.elements.size} elements unconditionally", layout.unprunedExplanation, ComplexitySecondary, 9, false, wrap = true)
                label("Halts recursion when val > ${frame.step.limit}", layout.prunedExplanation, ComplexitySecondary, 9, false, wrap = true)
                fun element(index: Int, rect: ComplexityRect, state: ComplexityElementState) {
                    val color = when (state) {
                        ComplexityElementState.EVAL -> ComplexityOrange
                        ComplexityElementState.VALID -> ComplexityGreen
                        ComplexityElementState.HALT -> ComplexityRed
                        ComplexityElementState.SKIP, ComplexityElementState.PENDING -> ComplexitySecondary
                    }
                    card(rect, Color(0xFF181824), color)
                    val valueRect = ComplexityRect(rect.x, rect.y + 1f, rect.width, values.second)
                    val stateRect = ComplexityRect(rect.x, valueRect.bottom + 1f, rect.width, states.second)
                    label(presentation.elements[index].value.toString(), valueRect, Color.White, 10, centered = true)
                    label(state.name, stateRect, color, 9, centered = true)
                }
                presentation.elements.indices.forEach { i ->
                    element(i, layout.unprunedElements[i], frame.unpruned[i])
                    element(i, layout.prunedElements[i], frame.pruned[i])
                }
                card(layout.explanationRegion, ComplexityRegion, ComplexityBorder)
                label("PRUNING & WORST-CASE COMPLEXITY:", layout.explanationHeading, ComplexityBlue, 10)
                (ComplexityParagraphs + complexityContext(frame)).forEachIndexed { i, paragraph ->
                    label(paragraph, layout.paragraphs[i], Color.White, 9, false, wrap = true)
                }
                label(complexitySavings(frame), layout.savings, ComplexityGreen, 9, wrap = true)
            }
        }
    }
}
