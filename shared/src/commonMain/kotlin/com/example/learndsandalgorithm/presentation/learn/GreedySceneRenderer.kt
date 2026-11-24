package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object GreedyRendererText {
    const val DECISION_HEADING = "GREEDY DECISION"
    const val COUNTEREXAMPLE_HEADING = "Why Greedy Fails Here"
    const val OVERFLOW_HEADING_PREFIX = "Full scene text · "
    const val ADDITIONAL_DETAIL_HEADING = "Additional scene detail"
    const val STEPS_HEADING = "Steps"

    val educationalStrings: List<String> = listOf(
        DECISION_HEADING,
        COUNTEREXAMPLE_HEADING,
        OVERFLOW_HEADING_PREFIX,
        ADDITIONAL_DETAIL_HEADING,
        STEPS_HEADING
    )
}

internal data class GreedyDetailsSection(
    val elementId: String,
    val heading: String,
    val explanation: String,
    val showsStepProgress: Boolean
)

/** Pure Details projection used by the target Greedy lessons and covered by common tests. */
internal fun greedyDetailsProjection(
    additionalExplanation: String?,
    overflowItems: List<GreedySceneItem>,
    singleProgressIndicator: Boolean,
    overflowHeading: String? = null,
    stepsFirst: Boolean = false
): List<GreedyDetailsSection> = buildList {
    val stepsSection = GreedyDetailsSection(
        elementId = "steps",
        heading = GreedyRendererText.STEPS_HEADING,
        explanation = "",
        showsStepProgress = true
    )
    if (stepsFirst) add(stepsSection)
    additionalExplanation?.let { explanation ->
        add(
            GreedyDetailsSection(
                elementId = "counterexample-explanation",
                heading = GreedyRendererText.COUNTEREXAMPLE_HEADING,
                explanation = explanation,
                showsStepProgress = !singleProgressIndicator
            )
        )
    }
    stableGreedyOverflowItems(overflowItems).forEach { item ->
        add(
            GreedyDetailsSection(
                elementId = item.id,
                heading = overflowHeading ?: if (singleProgressIndicator) GreedyRendererText.ADDITIONAL_DETAIL_HEADING else GreedyRendererText.OVERFLOW_HEADING_PREFIX + item.id,
                explanation = item.label,
                showsStepProgress = !singleProgressIndicator
            )
        )
    }
    if (!stepsFirst) add(stepsSection)
}

@Composable
internal fun GreedyDetailsAndStepsContent(
    badge: String,
    explanation: String,
    stepIndex: Int,
    totalSteps: Int,
    complexity: String,
    badgeTone: GreedySceneTone,
    additionalExplanation: String?,
    overflowItems: List<GreedySceneItem>,
    stepCompleted: Boolean,
    singleProgressIndicator: Boolean = false,
    overflowHeading: String? = null,
    stepsFirst: Boolean = false
) {
    val badgeColor = when (badgeTone) {
        GreedySceneTone.SELECTED -> VisualizerGreenAccent
        GreedySceneTone.REJECTED -> VisualizerRedAccent
        GreedySceneTone.ACCENT -> VisualizerBlueAccent
        else -> VisualizerOrangeAccent
    }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(GreedyRendererText.DECISION_HEADING, color = VisualizerTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(badge, color = badgeColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text(complexity, modifier = Modifier.padding(start = 4.dp), color = VisualizerGreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        greedyDetailsProjection(additionalExplanation, overflowItems, singleProgressIndicator, overflowHeading, stepsFirst).forEach { section ->
            key(section.elementId) {
                val sectionText = if (section.elementId == "steps") explanation else section.explanation
                if (section.showsStepProgress) {
                    StepsCard(
                        label = section.heading,
                        explanation = sectionText,
                        stepIndex = stepIndex,
                        totalSteps = totalSteps,
                        isCompleted = if (section.elementId == "steps") stepIndex == totalSteps - 1 else stepCompleted
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(section.heading, color = VisualizerTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(sectionText, color = VisualizerTextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun GreedyVisualizationModal(
    title: String,
    statusText: String,
    statusTone: GreedySceneTone,
    playback: VisualizerPlaybackState,
    explanation: String,
    badge: String,
    complexity: String,
    viewportKey: String,
    openButtonText: String,
    sceneForViewport: (Float, Float) -> GreedyScene,
    additionalExplanation: String? = null,
    singleProgressIndicatorInDetails: Boolean = false,
    overflowHeading: String? = null,
    stepsFirstInDetails: Boolean = false,
    initialScaleMode: VisualizerCanvasInitialScale = VisualizerCanvasInitialScale.AUTO_FIT,
    resetViewportWithPlayback: Boolean = false
) {
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val stepIndex = playback.safeStepIndex
    val stepsCount = playback.stepCount
    var overflowState by remember { mutableStateOf(GreedyOverflowState(stepIndex, emptyList())) }
    var viewportResetGeneration by remember { mutableStateOf(0) }
    val statusColor = when (statusTone) {
        GreedySceneTone.SELECTED -> VisualizerGreenAccent
        GreedySceneTone.REJECTED -> VisualizerRedAccent
        GreedySceneTone.ACCENT -> VisualizerBlueAccent
        else -> VisualizerOrangeAccent
    }
    if (!autoExpand) {
        ArrayWindowButton(text = openButtonText) { isExpanded = true }
    }
    if (isExpanded) {
        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = playback.isCompleted,
            stepIndex = stepIndex,
            stepCount = stepsCount,
            isPlaying = playback.isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = statusColor,
            onReset = {
                playback.reset()
                if (resetViewportWithPlayback) viewportResetGeneration++
            },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            visualContent = {
                GreedySceneRenderer(
                    sceneForViewport = sceneForViewport,
                    viewportKey = viewportKey,
                    snapshotIndex = stepIndex,
                    initialScaleMode = initialScaleMode,
                    viewportResetGeneration = viewportResetGeneration,
                    onTextOverflow = { measuredSnapshotIndex, measuredOverflow ->
                        val nextState = GreedyOverflowState(measuredSnapshotIndex, measuredOverflow)
                        if (overflowState != nextState) overflowState = nextState
                    }
                )
            },
            detailsContent = {
                GreedyDetailsAndStepsContent(
                    badge = badge,
                    explanation = explanation,
                    stepIndex = stepIndex,
                    totalSteps = stepsCount,
                    complexity = complexity,
                    badgeTone = statusTone,
                    additionalExplanation = additionalExplanation,
                    overflowItems = visibleGreedyOverflowItems(overflowState, stepIndex),
                    stepCompleted = playback.isCompleted,
                    singleProgressIndicator = singleProgressIndicatorInDetails,
                    overflowHeading = overflowHeading,
                    stepsFirst = stepsFirstInDetails
                )
            }
        )
    }
}

@Composable
internal fun GreedySceneRenderer(
    sceneForViewport: (Float, Float) -> GreedyScene,
    viewportKey: Any,
    snapshotIndex: Int,
    initialScaleMode: VisualizerCanvasInitialScale = VisualizerCanvasInitialScale.AUTO_FIT,
    viewportResetGeneration: Int = 0,
    onTextOverflow: (Int, List<GreedySceneItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
    val scene = sceneForViewport(maxWidth.value, maxHeight.value)
    val viewport = rememberVisualizerCanvasState(key = viewportKey, initialScaleMode = initialScaleMode)
    LaunchedEffect(viewportResetGeneration) {
        if (viewportResetGeneration > 0) viewport.reset()
    }
    val textMeasurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val colors = remember {
        mapOf(
            GreedySceneTone.NEUTRAL to Triple(Color(0xFF13131C), Color(0xFF343444), Color.White),
            GreedySceneTone.ACTIVE to Triple(Color(0xFF2A1C12), Color(0xFFFA6E13), Color.White),
            GreedySceneTone.SELECTED to Triple(Color(0xFF123820), Color(0xFF10B981), Color.White),
            GreedySceneTone.REJECTED to Triple(Color(0xFF381215), Color(0xFFEF4444), Color.White),
            GreedySceneTone.ACCENT to Triple(Color(0xFF13233A), Color(0xFF38BDF8), Color.White),
            GreedySceneTone.MUTED to Triple(Color(0xFF181824), Color(0xFF4B5563), Color(0xFF9CA3AF))
        )
    }
    val animatedItems = scene.items.map { item ->
        key(item.id) {
            val (fill, border, textColor) = colors.getValue(item.tone)
            val targetProgress = item.fillFraction ?: if (item.tone == GreedySceneTone.SELECTED || item.tone == GreedySceneTone.REJECTED ||
                (item.id.startsWith("activity-bar-") && item.tone == GreedySceneTone.ACTIVE)) 1f else 0f
            val fraction by animateFloatAsState(targetProgress, tween(420))
            val animatedLeft by animateFloatAsState(item.bounds.left, tween(420))
            val isSequenceCoin = item.shape == GreedySceneShape.CIRCLE && (item.id.startsWith("selected-coin-") || item.id.startsWith("greedy-coin-") || item.id.startsWith("optimal-coin-"))
            val appearance = remember(item.id) { Animatable(if (isSequenceCoin) 0f else 1f) }
            LaunchedEffect(item.id) { if (isSequenceCoin) appearance.animateTo(1f, tween(420)) }
            GreedyAnimatedItem(item, fill, border, textColor, fraction, appearance.value,
                if (item.id.startsWith("timeline-last-finish-marker")) animatedLeft else item.bounds.left)
        }
    }
    val measuredText = remember(scene, density.density, density.fontScale) {
        measureGreedySceneText(scene) { item, mode, availableWidthDp ->
            val maxWidth = if (mode == GreedyTextMeasureMode.NATURAL_WIDTH) {
                Constraints.Infinity
            } else {
                with(density) { availableWidthDp.dp.roundToPx().coerceAtLeast(1) }
            }
            val layout = textMeasurer.measure(
                text = item.label,
                style = greedySceneTextStyle(item, Color.White),
                softWrap = mode == GreedyTextMeasureMode.WRAPPED,
                constraints = Constraints(maxWidth = maxWidth, maxHeight = Constraints.Infinity)
            )
            with(density) {
                GreedyTextMeasurement(
                    width = layout.size.width.toDp().value,
                    height = layout.size.height.toDp().value,
                    horizontalOverflow = scene.constrainedTextMeasurement && mode == GreedyTextMeasureMode.WRAPPED && layout.hasVisualOverflow
                )
            }
        }
    }
    val overflow = remember(scene, measuredText) { overflowingGreedySceneItems(scene, measuredText) }
    SideEffect { onTextOverflow(snapshotIndex, stableGreedyOverflowItems(overflow)) }
    val drawableIds = remember(overflow) { scene.items.map { it.id }.toSet() - overflow.map { it.id }.toSet() }
    VisualizerCanvasViewport(Modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, viewport) {
        Canvas(Modifier.size(scene.width.dp, scene.height.dp)) {
            animatedItems.sortedBy { if (it.item.id.startsWith("timeline-last-finish-marker")) 1 else 0 }.forEach { animated ->
                val item = animated.item
                val rect = item.bounds
                val topLeft = Offset(animated.left.dp.toPx(), rect.top.dp.toPx())
                val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                if (item.filled) {
                    if (item.shape == GreedySceneShape.CIRCLE) {
                        val center = topLeft + Offset(size.width / 2f, size.height / 2f)
                        drawCircle(animated.fill.copy(alpha = animated.appearance), radius = size.width / 2f, center = center)
                        if (animated.progress > 0f) drawCircle(animated.border.copy(alpha = 0.35f * animated.progress * animated.appearance), radius = size.width / 2f, center = center)
                        drawCircle(animated.border.copy(alpha = animated.appearance), radius = size.width / 2f, center = center, style = Stroke(1.5.dp.toPx()))
                    } else {
                        drawRoundRect(if (item.progressTrack) Color(0xFF242431) else animated.fill, topLeft, size, CornerRadius(8.dp.toPx()))
                        if (item.fractionGauge) {
                            val gaugeInset = 8.dp.toPx()
                            val gaugeWidth = (size.width - gaugeInset * 2f).coerceAtLeast(1f)
                            val gaugeHeight = 4.dp.toPx()
                            val gaugeTopLeft = Offset(topLeft.x + gaugeInset, topLeft.y + size.height - 8.dp.toPx())
                            val gaugeSize = Size(gaugeWidth, gaugeHeight)
                            drawRoundRect(Color(0xFF343444), gaugeTopLeft, gaugeSize, CornerRadius(gaugeHeight / 2f))
                            if (animated.progress > 0f) {
                                drawRoundRect(
                                    animated.border,
                                    gaugeTopLeft,
                                    Size(gaugeWidth * animated.progress.coerceIn(0f, 1f), gaugeHeight),
                                    CornerRadius(gaugeHeight / 2f)
                                )
                            }
                        } else if (animated.progress > 0f) {
                            drawRoundRect(
                                animated.border.copy(alpha = if (item.progressTrack) 1f else 0.45f),
                                topLeft,
                                Size(size.width * animated.progress.coerceIn(0f, 1f), size.height),
                                CornerRadius(8.dp.toPx())
                            )
                        }
                        drawRoundRect(animated.border, topLeft, size, CornerRadius(8.dp.toPx()), style = Stroke(1.5.dp.toPx()))
                    }
                }
                if (item.id in drawableIds) {
                    val textBounds = item.textBounds
                    val measured = textMeasurer.measure(
                        text = item.label,
                        style = greedySceneTextStyle(item, animated.textColor.copy(alpha = animated.appearance)),
                        softWrap = if (scene.constrainedTextMeasurement) item.wrapText else true,
                        constraints = Constraints(
                            maxWidth = with(density) { (textBounds.width - item.textHorizontalPaddingDp).dp.roundToPx().coerceAtLeast(1) },
                            maxHeight = with(density) { (textBounds.height - item.textVerticalPaddingDp).dp.roundToPx().coerceAtLeast(1) }
                        )
                    )
                    drawText(
                        measured,
                        topLeft = Offset(
                            textBounds.left.dp.toPx() + (textBounds.width.dp.toPx() - measured.size.width) / 2f,
                            textBounds.top.dp.toPx() + (textBounds.height.dp.toPx() - measured.size.height) / 2f
                        )
                    )
                }
            }
        }
    }
    }
}

private fun greedySceneTextStyle(item: GreedySceneItem, color: Color) = TextStyle(
    color = color,
    fontSize = item.fontSize.sp,
    fontWeight = if (item.bold) FontWeight.Bold else FontWeight.Normal,
    textAlign = TextAlign.Center,
    lineHeight = (item.fontSize + 3).sp
)

private data class GreedyAnimatedItem(
    val item: GreedySceneItem,
    val fill: Color,
    val border: Color,
    val textColor: Color,
    val progress: Float,
    val appearance: Float,
    val left: Float
)
