package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private val FullMergeOrange = Color(0xFFFA6E13)
private val FullMergeBlue = Color(0xFF38BDF8)
private val FullMergeGreen = Color(0xFF10B981)
private val FullMergePurple = Color(0xFFA78BFA)
private val FullMergeSecondary = Color(0xFF9CA3AF)

@Composable
internal fun MergeSortFullVisualizationCard(
    title: String = "Merge Sort Implementation and Complexity"
) {
    val completeTrace = remember { MergeSortFullTrace.stepsForFullSort() }
    val sceneFrames = remember(completeTrace) { MergeSortFullSceneBuilder.build(completeTrace) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    var playbackMode by rememberSaveable { mutableStateOf(FullMergePlaybackMode.STANDARD) }
    var controllerGeneration by rememberSaveable { mutableIntStateOf(0) }
    var modeAnchorTraceIndex by rememberSaveable { mutableIntStateOf(0) }
    val visibleSteps = remember(completeTrace, playbackMode) {
        MergeSortFullPlayback.stepsFor(completeTrace, playbackMode)
    }
    val initialVisibleStepIndex = MergeSortFullPlayback.nearestVisibleIndex(modeAnchorTraceIndex, visibleSteps)
    val playback = rememberMergeSortFullPlaybackController(
        stepCount = visibleSteps.size,
        initialVisibleStepIndex = initialVisibleStepIndex,
        controllerGeneration = controllerGeneration
    )
    val visibleStepIndex = playback.safeStepIndex
    val visibleStep = visibleSteps[visibleStepIndex]
    val originalTraceIndex = visibleStep.originalTraceIndex
    val step = visibleStep.traceStep
    val resetPlayback = {
        playback.reset()
        modeAnchorTraceIndex = 0
    }

    if (!autoExpand) {
        ArrayWindowButton(text = "Open full merge sort visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusColor = when (step.event) {
            FullMergeEvent.DIVIDE -> FullMergeOrange
            FullMergeEvent.COMPARE, FullMergeEvent.CALL_ENTER, FullMergeEvent.MERGE_START -> FullMergeBlue
            FullMergeEvent.COPY_BACK -> FullMergePurple
            FullMergeEvent.COMPLETE, FullMergeEvent.BASE_CASE, FullMergeEvent.RETURN,
            FullMergeEvent.WRITE_BUFFER, FullMergeEvent.FLUSH_REMAINING -> FullMergeGreen
            FullMergeEvent.START -> FullMergeSecondary
        }
        VisualizerModal(
            title = title,
            statusText = MergeSortFullSceneBuilder.statusText(step),
            isCompleted = playback.isCompleted,
            stepIndex = visibleStepIndex,
            stepCount = visibleSteps.size,
            isPlaying = playback.isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = statusColor,
            detailsScrollable = false,
            onReset = resetPlayback,
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                resetPlayback()
                isExpanded = false
            },
            visualContent = {
                MergeSortFullSceneRenderer(
                    frame = sceneFrames[originalTraceIndex],
                    sequence = sceneFrames,
                    modifier = Modifier.fillMaxSize()
                )
            },
            detailsContent = {
                MergeSortFullDetailsAndStepsContent(
                    step = step,
                    stepIndex = visibleStepIndex,
                    totalSteps = visibleSteps.size,
                    playbackMode = playbackMode,
                    onPlaybackModeChange = { requestedMode ->
                        if (requestedMode != playbackMode) {
                            modeAnchorTraceIndex = pauseMergeSortFullPlaybackAndCaptureAnchor(
                                playback = playback,
                                originalTraceIndex = originalTraceIndex
                            )
                            controllerGeneration++
                            playbackMode = requestedMode
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun rememberMergeSortFullPlaybackController(
    stepCount: Int,
    initialVisibleStepIndex: Int,
    controllerGeneration: Int
): VisualizerPlaybackState {
    // A fresh saveable identity per transition lets this controller start at the
    // mapped visible cursor while keeping its true navigation minimum at zero.
    val playback = key(controllerGeneration) {
        rememberSaveable(
            stepCount,
            saver = VisualizerPlaybackState.saver(initialStep = 0, stepCount = stepCount)
        ) {
            createMergeSortFullPlaybackController(stepCount, initialVisibleStepIndex)
        }
    }

    LaunchedEffect(playback.isPlaying, playback.safeStepIndex, playback.stepCount, controllerGeneration) {
        if (playback.isPlaying && !playback.isCompleted) {
            delay(1000L)
            if (playback.isPlaying) {
                if (playback.safeStepIndex < 0) {
                    playback.stepIndex = 0
                } else if (playback.safeStepIndex < playback.maxStepIndex) {
                    playback.stepIndex++
                } else {
                    playback.isPlaying = false
                }
            }
        } else if (playback.isCompleted) {
            playback.isPlaying = false
        }
    }
    return playback
}

internal fun createMergeSortFullPlaybackController(
    stepCount: Int,
    initialVisibleStepIndex: Int
): VisualizerPlaybackState = VisualizerPlaybackState(
    initialStep = 0,
    stepCount = stepCount,
    savedStepIndex = initialVisibleStepIndex
)

/** Pauses the current controller and captures the frame that is actually on screen. */
internal fun pauseMergeSortFullPlaybackAndCaptureAnchor(
    playback: VisualizerPlaybackState,
    originalTraceIndex: Int
): Int {
    playback.isPlaying = false
    return originalTraceIndex
}

@Composable
private fun MergeSortFullDetailsAndStepsContent(
    step: MergeSortFullTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    playbackMode: FullMergePlaybackMode,
    onPlaybackModeChange: (FullMergePlaybackMode) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val baseTextStyle = LocalTextStyle.current
        val textMeasurer = rememberTextMeasurer()
        val stepsTextWidthPx = with(density) {
            (maxWidth - 24.dp).coerceAtLeast(1.dp).toPx().roundToInt()
        }
        val operationTextWidthPx = with(density) {
            maxWidth.coerceAtLeast(1.dp).toPx().roundToInt()
        }
        val stepsExplanationStyle = baseTextStyle.copy(fontSize = 13.sp, lineHeight = 18.sp)
        val stepsHeaderStyle = baseTextStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val stepCounterStyle = baseTextStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
        val operationHeaderStyle = baseTextStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
        val operationValueStyle = baseTextStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)
        val metadataLineStyle = baseTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
        val measuredContentHeights = remember(
            step.explanation,
            step.badgeText,
            stepIndex,
            totalSteps,
            stepsTextWidthPx,
            operationTextWidthPx,
            textMeasurer,
            baseTextStyle,
            density.density,
            density.fontScale
        ) {
            val explanationHeightPx = textMeasurer.measure(
                text = AnnotatedString(step.explanation),
                style = stepsExplanationStyle,
                constraints = Constraints(maxWidth = stepsTextWidthPx)
            ).size.height
            val firstExplanationLineHeightPx = textMeasurer.measure(
                text = AnnotatedString(step.explanation),
                style = stepsExplanationStyle,
                maxLines = 1,
                constraints = Constraints(maxWidth = stepsTextWidthPx)
            ).size.height
            val stepsHeaderHeightPx = maxOf(
                textMeasurer.measure(
                    text = AnnotatedString("Steps"),
                    style = stepsHeaderStyle,
                    constraints = Constraints(maxWidth = stepsTextWidthPx / 2)
                ).size.height,
                textMeasurer.measure(
                    text = AnnotatedString("STEP ${stepIndex + 1} / $totalSteps"),
                    style = stepCounterStyle,
                    constraints = Constraints(maxWidth = stepsTextWidthPx / 2)
                ).size.height
            )
            val operationHeaderHeightPx = textMeasurer.measure(
                text = AnnotatedString("CURRENT OPERATION"),
                style = operationHeaderStyle,
                constraints = Constraints(maxWidth = operationTextWidthPx)
            ).size.height
            val operationValueHeightPx = textMeasurer.measure(
                text = AnnotatedString(step.badgeText),
                style = operationValueStyle,
                constraints = Constraints(maxWidth = operationTextWidthPx)
            ).size.height
            val metadataLineHeightPx = textMeasurer.measure(
                text = AnnotatedString("TIME COMPLEXITY: O(N log N)"),
                style = metadataLineStyle,
                constraints = Constraints(maxWidth = operationTextWidthPx)
            ).size.height
            with(density) {
                MergeSortFullMeasuredContentHeights(
                    stepsPreferredHeightDp = ((explanationHeightPx + stepsHeaderHeightPx).toDp() + 28.dp).value,
                    minimumStepsHeightDp = ((firstExplanationLineHeightPx + stepsHeaderHeightPx).toDp() + 28.dp).value,
                    currentOperationHeightDp = ((operationHeaderHeightPx + operationValueHeightPx).toDp() + 4.dp).value,
                    minimumMetadataHeightDp = metadataLineHeightPx.toDp().value
                )
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("PLAYBACK DETAIL", color = FullMergeSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FullMergePlaybackMode.entries.forEach { mode ->
                    val selected = mode == playbackMode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPlaybackModeChange(mode) },
                        color = if (selected) FullMergeBlue.copy(alpha = 0.18f) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (selected) FullMergeBlue else FullMergeSecondary.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = if (mode == FullMergePlaybackMode.STANDARD) "Standard" else "Detailed",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp),
                            color = if (selected) FullMergeBlue else FullMergeSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val fallbackScrollState = key(stepIndex, playbackMode, "fallback-scroll") {
                    rememberScrollState()
                }
                val allocation = mergeSortFullDetailsAllocation(
                    availableHeightDp = maxHeight.value,
                    preferredStepsHeightDp = measuredContentHeights.stepsPreferredHeightDp,
                    minimumStepsHeightDp = measuredContentHeights.minimumStepsHeightDp,
                    currentOperationHeightDp = measuredContentHeights.currentOperationHeightDp,
                    minimumMetadataHeightDp = measuredContentHeights.minimumMetadataHeightDp
                )
                Column(
                    modifier = if (allocation.useLocalScrollFallback) {
                        Modifier.fillMaxWidth().verticalScroll(fallbackScrollState)
                    } else {
                        Modifier.fillMaxSize()
                    },
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    key(stepIndex, playbackMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(allocation.stepsHeightDp.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            StepsCard(
                                label = "Steps",
                                explanation = step.explanation,
                                stepIndex = stepIndex,
                                totalSteps = totalSteps,
                                isCompleted = stepIndex == totalSteps - 1
                            )
                        }
                    }
                    key(stepIndex, playbackMode) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "CURRENT OPERATION",
                                color = FullMergeSecondary,
                                style = operationHeaderStyle
                            )
                            Text(
                                step.badgeText,
                                modifier = Modifier.fillMaxWidth(),
                                style = operationValueStyle,
                                color = when (step.event) {
                                    FullMergeEvent.DIVIDE -> FullMergeOrange
                                    FullMergeEvent.COPY_BACK -> FullMergePurple
                                    FullMergeEvent.COMPLETE, FullMergeEvent.BASE_CASE, FullMergeEvent.RETURN,
                                    FullMergeEvent.WRITE_BUFFER, FullMergeEvent.FLUSH_REMAINING -> FullMergeGreen
                                    else -> FullMergeBlue
                                },
                            )
                        }
                    }
                    key(stepIndex, playbackMode, "metadata") {
                        Column(
                            modifier = if (allocation.useLocalScrollFallback) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("TIME COMPLEXITY: O(N log N)", color = FullMergeGreen, style = metadataLineStyle)
                            Text("SPACE COMPLEXITY: O(N)", color = FullMergeBlue, style = metadataLineStyle)
                            Text("Auxiliary merge buffer stores up to N occurrences.", color = FullMergeSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

internal data class MergeSortFullMeasuredContentHeights(
    val stepsPreferredHeightDp: Float,
    val minimumStepsHeightDp: Float,
    val currentOperationHeightDp: Float,
    val minimumMetadataHeightDp: Float
)

internal data class MergeSortFullDetailsAllocation(
    val stepsHeightDp: Float,
    val currentOperationHeightDp: Float,
    val minimumMetadataHeightDp: Float,
    val useLocalScrollFallback: Boolean
)

internal fun mergeSortFullDetailsAllocation(
    availableHeightDp: Float,
    preferredStepsHeightDp: Float,
    minimumStepsHeightDp: Float,
    currentOperationHeightDp: Float,
    minimumMetadataHeightDp: Float,
    sectionSpacingDp: Float = 6f
): MergeSortFullDetailsAllocation {
    val available = when {
        availableHeightDp.isNaN() || availableHeightDp <= 0f -> 0f
        availableHeightDp == Float.POSITIVE_INFINITY -> Float.MAX_VALUE
        else -> availableHeightDp
    }
    fun measuredHeight(value: Float): Float =
        if (value.isFinite()) value.coerceAtLeast(0f) else 0f

    val stepsMinimum = measuredHeight(minimumStepsHeightDp).coerceAtLeast(1f)
    val stepsPreferred = measuredHeight(preferredStepsHeightDp).coerceAtLeast(stepsMinimum)
    val operationHeight = measuredHeight(currentOperationHeightDp).coerceAtLeast(1f)
    val metadataMinimum = measuredHeight(minimumMetadataHeightDp).coerceAtLeast(1f)
    val gap = measuredHeight(sectionSpacingDp)
    val allSectionsMinimum = stepsMinimum + operationHeight + metadataMinimum + (gap * 2f)

    if (available >= allSectionsMinimum) {
        val stepCapacity = available - operationHeight - metadataMinimum - (gap * 2f)
        return MergeSortFullDetailsAllocation(
            stepsHeightDp = stepsPreferred.coerceAtMost(stepCapacity).coerceAtLeast(stepsMinimum),
            currentOperationHeightDp = operationHeight,
            minimumMetadataHeightDp = metadataMinimum,
            useLocalScrollFallback = false
        )
    }

    // In fallback mode the content column scrolls locally. Keep the Steps and
    // operation regions together whenever their minimums fit; otherwise retain
    // each measured minimum and make the unavoidable overflow explicitly scrollable.
    val stepCapacityBeforeOperation = available - operationHeight - gap
    val stepsHeight = if (stepCapacityBeforeOperation >= stepsMinimum) {
        stepsPreferred.coerceAtMost(stepCapacityBeforeOperation).coerceAtLeast(stepsMinimum)
    } else {
        stepsMinimum
    }
    return MergeSortFullDetailsAllocation(
        stepsHeightDp = stepsHeight,
        currentOperationHeightDp = operationHeight,
        minimumMetadataHeightDp = metadataMinimum,
        useLocalScrollFallback = true
    )
}
