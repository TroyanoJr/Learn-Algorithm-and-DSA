package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

internal data class RecursionTradeoffsDetailsAllocation(
    val stepsHeightDp: Float,
    val theoryHeightDp: Float,
    val gapDp: Float,
    val completeStepsFit: Boolean,
    val hasUsableTheoryHeader: Boolean
)

/**
 * Reserves the full measured Steps height first. If the bounded details viewport cannot contain
 * Steps plus the theory heading, Theory yields its space; the Steps surface marks the physical
 * constraint and recommends expanding or rotating the visualizer. Supporting a complete Steps
 * card when it exceeds the viewport would require more height from the owning modal.
 */
internal fun recursionTradeoffsDetailsAllocation(
    availableHeightDp: Float,
    requiredStepsHeightDp: Float,
    preferredGapDp: Float = 8f,
    minimumTheoryHeaderHeightDp: Float = 40f
): RecursionTradeoffsDetailsAllocation {
    val available = availableHeightDp.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val steps = requiredStepsHeightDp.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val preferredGap = preferredGapDp.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val minimumTheory = minimumTheoryHeaderHeightDp.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val gap = if (available - steps >= minimumTheory + preferredGap) preferredGap else 0f
    val theoryHeight = (available - steps - gap).coerceAtLeast(0f)
    return RecursionTradeoffsDetailsAllocation(
        stepsHeightDp = steps,
        theoryHeightDp = theoryHeight,
        gapDp = gap,
        completeStepsFit = steps <= available,
        hasUsableTheoryHeader = theoryHeight >= minimumTheory
    )
}

internal fun stableRecursionTradeoffsStepsHeightPx(
    headerHeightPx: Int,
    wrappedBodyHeightsPx: List<Int>,
    verticalPaddingPx: Int,
    headerBodyGapPx: Int
): Int = (
    verticalPaddingPx.coerceAtLeast(0) * 2 +
        headerHeightPx.coerceAtLeast(0) +
        headerBodyGapPx.coerceAtLeast(0) +
        (wrappedBodyHeightsPx.maxOrNull() ?: 0).coerceAtLeast(0)
    ).coerceAtLeast(0)

@Composable
internal fun RecursionTradeoffsVisualizationCard(
    title: String = "Recursion Patterns and Trade-offs"
) {
    val steps = remember { RecursionTradeoffsTrace.createTrace() }
    val sceneFrames = remember(steps) { IterationRecursionSceneBuilder.tradeoffs(steps) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open tradeoffs visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            TradeoffAction.INIT -> "RECURSION TRADE-OFFS: ITERATION VS RECURSION"
            TradeoffAction.ITERATIVE_LOOP -> "ITERATION PASS i = ${step.iterI} · TOTAL = ${step.iterTotal}"
            TradeoffAction.RECURSIVE_DESCENT -> "RECURSIVE DESCENT: CALL sumTo(${step.recurCallStack.lastOrNull()})"
            TradeoffAction.RECURSIVE_BASE -> "RECURSIVE BASE CASE: sumTo(0) = 0"
            TradeoffAction.RECURSIVE_ASCENT -> "RECURSIVE ASCENT: RETURN ${step.recurReturnVal}"
            TradeoffAction.FINISHED -> "TRADE-OFFS COMPLETE · BOTH SUM = ${step.iterTotal}"
        }

        val statusColor = when (step.action) {
            TradeoffAction.RECURSIVE_BASE, TradeoffAction.RECURSIVE_ASCENT, TradeoffAction.FINISHED -> GreenAccent
            TradeoffAction.ITERATIVE_LOOP, TradeoffAction.RECURSIVE_DESCENT -> OrangeAccent
            else -> BlueAccent
        }

        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            detailsScrollable = false,
            statusColor = statusColor,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            visualContent = {
                IterationRecursionSceneRenderer(
                    frame = sceneFrames[stepIndex],
                    sequence = sceneFrames,
                    viewportKey = "recursion_tradeoffs",
                    modifier = Modifier.fillMaxSize()
                )
            },
            detailsContent = {
                RecursionTradeoffsDetailsAndStepsContent(
                    steps = steps,
                    step = step,
                    stepIndex = stepIndex,
                    totalSteps = steps.size,
                    scale = 1f
                )
            }
        )
    }
}

@Composable
private fun RecursionTradeoffsDetailsAndStepsContent(
    steps: List<RecursionTradeoffsStep>,
    step: RecursionTradeoffsStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float
) {
    val theoryScrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val inheritedTextStyle = LocalTextStyle.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val stepsHorizontalPadding = 8.dp * scale
        val stepsVerticalPadding = 6.dp * scale
        val headerBodyGap = 4.dp * scale
        val badgeExplanationGap = 6.dp * scale
        val stepsContentWidthPx = with(density) {
            (maxWidth - stepsHorizontalPadding * 2).roundToPx().coerceAtLeast(1)
        }
        val halfHeaderWidthPx = (stepsContentWidthPx / 2).coerceAtLeast(1)
        val stepsHeightPx = remember(
            steps,
            totalSteps,
            stepsContentWidthPx,
            density.density,
            density.fontScale,
            scale,
            inheritedTextStyle
        ) {
            val headerStyle = inheritedTextStyle.merge(TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
            val stepStyle = inheritedTextStyle.merge(TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
            val badgeStyle = TextStyle(fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
            val explanationStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
            val headerHeight = maxOf(
                textMeasurer.measure(
                    text = AnnotatedString("Steps"),
                    style = headerStyle,
                    constraints = Constraints(maxWidth = halfHeaderWidthPx)
                ).size.height,
                textMeasurer.measure(
                    text = AnnotatedString("STEP $totalSteps / $totalSteps"),
                    style = stepStyle,
                    constraints = Constraints(maxWidth = halfHeaderWidthPx)
                ).size.height
            )
            val bodyHeights = steps.map { candidate ->
                val badgeHeight = textMeasurer.measure(
                    text = AnnotatedString(candidate.badgeText),
                    style = badgeStyle,
                    constraints = Constraints(maxWidth = stepsContentWidthPx)
                ).size.height
                val explanationHeight = textMeasurer.measure(
                    text = AnnotatedString(candidate.explanation),
                    style = explanationStyle,
                    constraints = Constraints(maxWidth = stepsContentWidthPx)
                ).size.height
                badgeHeight + with(density) { badgeExplanationGap.roundToPx() } + explanationHeight
            }
            stableRecursionTradeoffsStepsHeightPx(
                headerHeightPx = headerHeight,
                wrappedBodyHeightsPx = bodyHeights,
                verticalPaddingPx = with(density) { stepsVerticalPadding.roundToPx() },
                headerBodyGapPx = with(density) { headerBodyGap.roundToPx() }
            )
        }
        val stepsHeight = with(density) { stepsHeightPx.toDp() }
        val theoryHorizontalPadding = 12.dp * scale
        val theoryHeaderWidthPx = with(density) {
            (maxWidth - theoryHorizontalPadding * 2).roundToPx().coerceAtLeast(1)
        }
        val theoryHeaderHeightPx = textMeasurer.measure(
            text = AnnotatedString("AUXILIARY-SPACE COMPARISON"),
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            constraints = Constraints(maxWidth = theoryHeaderWidthPx)
        ).size.height
        val minimumTheoryHeaderHeight = with(density) {
            (theoryHeaderHeightPx + (12.dp * scale).roundToPx() * 2).toDp()
        }
        val allocation = recursionTradeoffsDetailsAllocation(
            availableHeightDp = maxHeight.value,
            requiredStepsHeightDp = stepsHeight.value,
            preferredGapDp = (8.dp * scale).value,
            minimumTheoryHeaderHeightDp = minimumTheoryHeaderHeight.value
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(allocation.gapDp.dp)
        ) {
            RecursionTradeoffsStepsRegion(
                step = step,
                stepIndex = stepIndex,
                totalSteps = totalSteps,
                scale = scale,
                completeStepsFit = allocation.completeStepsFit,
                modifier = Modifier.fillMaxWidth().height(allocation.stepsHeightDp.dp)
            )
            RecursionTradeoffsTheoryRegion(
                scrollState = theoryScrollState,
                scale = scale,
                modifier = Modifier.fillMaxWidth().weight(1f).let { weighted ->
                    if (allocation.hasUsableTheoryHeader) weighted else weighted.height(0.dp)
                }
            )
        }
    }
}

@Composable
private fun RecursionTradeoffsStepsRegion(
    step: RecursionTradeoffsStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float,
    completeStepsFit: Boolean,
    modifier: Modifier
) {
    val completed = stepIndex == totalSteps - 1
    val accent = when (step.action) {
        TradeoffAction.RECURSIVE_BASE, TradeoffAction.RECURSIVE_ASCENT, TradeoffAction.FINISHED -> GreenAccent
        TradeoffAction.ITERATIVE_LOOP, TradeoffAction.RECURSIVE_DESCENT -> OrangeAccent
        else -> BlueAccent
    }
    Surface(
        color = Color(0xFF161622),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (completeStepsFit) CardBorderColor else RedAccent),
        modifier = modifier.semantics {
            if (!completeStepsFit) {
                stateDescription = "Available height is too short for the complete step. Expand the visualizer or rotate the device."
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp * scale, vertical = 6.dp * scale),
            verticalArrangement = Arrangement.spacedBy(4.dp * scale)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Steps", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "STEP ${stepIndex + 1} / $totalSteps",
                    color = if (completed) GreenAccent else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp * scale)) {
                Text(step.badgeText, color = accent, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
                Text(step.explanation, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun RecursionTradeoffsTheoryRegion(
    scrollState: androidx.compose.foundation.ScrollState,
    scale: Float,
    modifier: Modifier
) {
    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp * scale),
            verticalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            Text(
                "AUXILIARY-SPACE COMPARISON",
                color = GreenAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp * scale)
            ) {
                TheorySection(
                    heading = "ITERATION · O(1) AUXILIARY SPACE",
                    body = "Iteration reuses one accumulator and loop index for every pass. This fixed auxiliary state remains O(1), regardless of N.",
                    headingColor = GreenAccent,
                    scale = scale
                )
                TheorySection(
                    heading = "RECURSION · O(N) AUXILIARY SPACE",
                    body = "Each active recursive call allocates a stack frame. In this example, stack depth grows with N; frames are released as calls return.",
                    headingColor = OrangeAccent,
                    scale = scale
                )
                TheorySection(
                    heading = "SAME RESULT, DIFFERENT MEMORY",
                    body = "Iteration and recursion compute the same sum while using different amounts of auxiliary memory.",
                    headingColor = GreenAccent,
                    scale = scale
                )
                TheorySection(
                    heading = "JVM RECURSION NOTE",
                    body = "The JVM does not generally perform automatic general-purpose tail-call optimization for standard recursive methods, so active stack frames remain allocated until their calls return.",
                    headingColor = BlueAccent,
                    bodyColor = TextSecondary,
                    scale = scale
                )
            }
        }
    }
}

@Composable
private fun TheorySection(
    heading: String,
    body: String,
    headingColor: Color,
    scale: Float,
    bodyColor: Color = TextPrimary
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp * scale)) {
        Text(heading, color = headingColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(body, color = bodyColor, fontSize = 12.sp, lineHeight = 17.sp)
    }
}
