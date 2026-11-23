package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val QuickOrange = Color(0xFFFB923C)
private val QuickBlue = Color(0xFF38BDF8)
private val QuickGreen = Color(0xFF34D399)
private val QuickSecondary = Color(0xFF9CA3AF)
private val QuickBorder = Color(0xFF343A49)
private const val DETAILS_BUTTON_MIN_HEIGHT_DP = 48f

@Composable
internal fun QuickSortPartitionVisualizationCard(
    title: String = "Quick Sort Fundamentals and Partitioning"
) {
    val steps = remember { QuickSortPartitionTrace.stepsForPartition() }
    val sceneFrames = remember(steps) { QuickSortPartitionSceneBuilder.build(steps) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val stepIndex = playback.safeStepIndex
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open quick sort visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            QuickPartitionEvent.START -> "READY · LOMUTO PARTITIONING"
            QuickPartitionEvent.SELECT_PIVOT -> "PIVOT = ${step.pivotValue} AT INDEX ${step.pivotIndex}"
            QuickPartitionEvent.SCAN_COMPARE -> "SCAN j = ${step.j}: COMPARING WITH PIVOT ${step.pivotValue}"
            QuickPartitionEvent.SWAP_ELEMENTS -> "SWAPPING arr[${step.swappedIndices?.first}] ↔ arr[${step.swappedIndices?.second}]"
            QuickPartitionEvent.PLACE_PIVOT -> "PLACING PIVOT AT INDEX ${step.finalPivotPosition}"
            QuickPartitionEvent.PARTITION_COMPLETE, QuickPartitionEvent.SORT_COMPLETE -> "PARTITION COMPLETE AT INDEX ${step.finalPivotPosition}"
        }
        val statusColor = when (step.event) {
            QuickPartitionEvent.PARTITION_COMPLETE, QuickPartitionEvent.SORT_COMPLETE -> QuickGreen
            QuickPartitionEvent.SWAP_ELEMENTS, QuickPartitionEvent.PLACE_PIVOT -> QuickOrange
            else -> QuickBlue
        }

        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = playback.isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = playback.isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = statusColor,
            detailsScrollable = false,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            visualContent = {
                QuickSortPartitionSceneRenderer(
                    frame = sceneFrames[stepIndex],
                    sequence = sceneFrames,
                    modifier = Modifier.fillMaxSize()
                )
            },
            detailsContent = {
                QuickSortPartitionDetailsAndStepsContent(
                    step = step,
                    stepIndex = stepIndex,
                    totalSteps = steps.size
                )
            }
        )
    }
}

@Composable
private fun QuickSortPartitionDetailsAndStepsContent(
    step: QuickPartitionTraceStep,
    stepIndex: Int,
    totalSteps: Int
) {
    var showMetadata by rememberSaveable(stepIndex) { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val baseTextStyle = LocalTextStyle.current
        val textMeasurer = rememberTextMeasurer()
        val fullWidthPx = with(density) { maxWidth.coerceAtLeast(1.dp).toPx().roundToInt() }
        val stepsTextWidthPx = with(density) { (maxWidth - 24.dp).coerceAtLeast(1.dp).toPx().roundToInt() }
        val operationHeaderStyle = baseTextStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
        val operationValueStyle = baseTextStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)
        val stepsExplanationStyle = baseTextStyle.copy(fontSize = 13.sp, lineHeight = 18.sp)
        val stepsHeaderStyle = baseTextStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val counterStyle = baseTextStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
        val measured = remember(
            step.explanation,
            step.badgeText,
            stepIndex,
            totalSteps,
            fullWidthPx,
            stepsTextWidthPx,
            baseTextStyle,
            density.density,
            density.fontScale
        ) {
            val headerHeight = maxOf(
                textMeasurer.measure(AnnotatedString("Steps"), stepsHeaderStyle, constraints = Constraints(maxWidth = stepsTextWidthPx / 2)).size.height,
                textMeasurer.measure(AnnotatedString("STEP ${stepIndex + 1} / $totalSteps"), counterStyle, constraints = Constraints(maxWidth = stepsTextWidthPx / 2)).size.height
            )
            val explanationHeight = textMeasurer.measure(
                AnnotatedString(step.explanation),
                stepsExplanationStyle,
                constraints = Constraints(maxWidth = stepsTextWidthPx)
            ).size.height
            val firstExplanationLineHeight = textMeasurer.measure(
                AnnotatedString(step.explanation),
                stepsExplanationStyle,
                maxLines = 1,
                constraints = Constraints(maxWidth = stepsTextWidthPx)
            ).size.height
            val operationHeaderHeight = textMeasurer.measure(
                AnnotatedString("CURRENT OPERATION"),
                operationHeaderStyle,
                constraints = Constraints(maxWidth = fullWidthPx)
            ).size.height
            val operationValueHeight = textMeasurer.measure(
                AnnotatedString(step.badgeText),
                operationValueStyle,
                constraints = Constraints(maxWidth = fullWidthPx)
            ).size.height
            val detailsButtonTextHeight = textMeasurer.measure(
                AnnotatedString("Details"),
                baseTextStyle.copy(fontSize = 10.sp),
                constraints = Constraints(maxWidth = with(density) { 64.dp.roundToPx() })
            ).size.height
            val metadataLabelHeight = textMeasurer.measure(
                AnnotatedString("In-Place Partitioning"),
                baseTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                constraints = Constraints(maxWidth = fullWidthPx)
            ).size.height
            val metadataHeight = textMeasurer.measure(
                AnnotatedString("Aux Space: O(1) in-place"),
                baseTextStyle.copy(fontSize = 9.sp),
                constraints = Constraints(maxWidth = fullWidthPx)
            ).size.height
            with(density) {
                QuickPartitionDetailsHeights(
                    preferredSteps = (headerHeight + explanationHeight).toDp().value + 28f,
                    minimumSteps = (headerHeight + firstExplanationLineHeight).toDp().value + 28f,
                    operation = quickPartitionOperationHeight(
                        header = operationHeaderHeight.toDp().value,
                        button = maxOf(DETAILS_BUTTON_MIN_HEIGHT_DP, detailsButtonTextHeight.toDp().value + 8f),
                        badge = operationValueHeight.toDp().value,
                        spacing = 2f
                    ),
                    minimumMetadata = with(density) { maxOf(metadataLabelHeight, metadataHeight).toDp().value + 3f },
                    detailsButtonHeight = maxOf(DETAILS_BUTTON_MIN_HEIGHT_DP, detailsButtonTextHeight.toDp().value + 8f)
                )
            }
        }
        val allocation = quickPartitionDetailsAllocation(
            availableHeight = maxHeight.value,
            preferredSteps = measured.preferredSteps,
            minimumSteps = measured.minimumSteps,
            operation = measured.operation,
            minimumMetadata = measured.minimumMetadata
        )
        val fallbackScrollState = key(stepIndex, "details-fallback") { rememberScrollState() }

        Column(
            modifier = if (allocation.localScrollFallback) {
                Modifier.fillMaxWidth().verticalScroll(fallbackScrollState)
            } else {
                Modifier.fillMaxSize()
            },
            verticalArrangement = Arrangement.Top
        ) {
            key(stepIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(allocation.stepsHeight.dp)
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
            Spacer(Modifier.height(6.dp))
            key(stepIndex, "operation") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("CURRENT OPERATION", color = QuickSecondary, style = operationHeaderStyle, modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { showMetadata = true },
                            modifier = Modifier.heightIn(min = maxOf(DETAILS_BUTTON_MIN_HEIGHT_DP, measured.detailsButtonHeight).dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text("Details", fontSize = 10.sp) }
                    }
                    Text(
                        step.badgeText,
                        modifier = Modifier.fillMaxWidth(),
                        style = operationValueStyle,
                        color = when (step.event) {
                            QuickPartitionEvent.PARTITION_COMPLETE, QuickPartitionEvent.SORT_COMPLETE -> QuickGreen
                            QuickPartitionEvent.SWAP_ELEMENTS, QuickPartitionEvent.PLACE_PIVOT -> QuickOrange
                            else -> QuickBlue
                        }
                    )
                }
            }
            Spacer(Modifier.height(allocation.metadataGap.dp))
            if (!allocation.localScrollFallback && allocation.metadataHeight >= allocation.minimumMetadataHeight) key(stepIndex, "complexity") {
                Column(
                    modifier = Modifier.fillMaxWidth().height(allocation.metadataHeight.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(quickPartitionMetadataLines().first(), color = QuickGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(quickPartitionMetadataLines().last(), color = QuickBlue, fontSize = 9.sp)
                }
            }
        }
    }
    if (showMetadata) {
        AlertDialog(
            onDismissRequest = { showMetadata = false },
            title = { Text("In-Place Partitioning") },
            text = {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(quickPartitionMetadataLines().first(), color = QuickGreen, fontWeight = FontWeight.Bold)
                    Text(quickPartitionMetadataLines().last(), color = QuickBlue)
                }
            },
            confirmButton = { TextButton(onClick = { showMetadata = false }) { Text("Close") } }
        )
    }
}

internal data class QuickPartitionDetailsHeights(
    val preferredSteps: Float,
    val minimumSteps: Float,
    val operation: Float,
    val minimumMetadata: Float,
    val detailsButtonHeight: Float
)

internal data class QuickPartitionDetailsAllocation(
    val stepsHeight: Float,
    val operationHeight: Float,
    val minimumMetadataHeight: Float,
    val metadataHeight: Float,
    val metadataGap: Float,
    val localScrollFallback: Boolean
)

internal fun quickPartitionMetadataNeedsExplicitAccess(allocation: QuickPartitionDetailsAllocation): Boolean =
    allocation.localScrollFallback || allocation.metadataHeight < allocation.minimumMetadataHeight

internal fun quickPartitionMetadataLines(): List<String> =
    listOf("In-Place Partitioning", "Aux Space: O(1) in-place")

internal fun quickPartitionOperationHeight(header: Float, button: Float, badge: Float, spacing: Float): Float =
    maxOf(header.finiteOrZero(), button.finiteOrZero()) + badge.finiteOrZero() + spacing.finiteOrZero()

private fun Float.finiteOrZero(): Float = if (isFinite()) coerceAtLeast(0f) else 0f

internal fun quickPartitionDetailsAllocation(
    availableHeight: Float,
    preferredSteps: Float,
    minimumSteps: Float,
    operation: Float,
    minimumMetadata: Float,
    sectionSpacing: Float = 6f
): QuickPartitionDetailsAllocation {
    val height = if (availableHeight.isFinite()) availableHeight.coerceAtLeast(0f) else 0f
    val stepMinimum = (if (minimumSteps.isFinite()) minimumSteps else 0f).coerceAtLeast(1f)
    val stepPreferred = (if (preferredSteps.isFinite()) preferredSteps else 0f).coerceAtLeast(stepMinimum)
    val operationHeight = (if (operation.isFinite()) operation else 0f).coerceAtLeast(1f)
    val metadataHeight = (if (minimumMetadata.isFinite()) minimumMetadata else 0f).coerceAtLeast(1f)
    val gap = (if (sectionSpacing.isFinite()) sectionSpacing else 0f).coerceAtLeast(0f)
    val primaryMinimum = stepMinimum + operationHeight + gap
    val allSectionsMinimum = primaryMinimum + metadataHeight + gap
    if (height >= allSectionsMinimum) {
        val availableSteps = height - operationHeight - metadataHeight - 2f * gap
        return QuickPartitionDetailsAllocation(
            stepsHeight = stepPreferred.coerceAtMost(availableSteps).coerceAtLeast(stepMinimum),
            operationHeight = operationHeight,
            minimumMetadataHeight = metadataHeight,
            metadataHeight = height - stepPreferred.coerceAtMost(availableSteps).coerceAtLeast(stepMinimum) - operationHeight - 2f * gap,
            metadataGap = gap,
            localScrollFallback = false
        )
    }
    if (height >= primaryMinimum) {
        return QuickPartitionDetailsAllocation(
            stepsHeight = stepMinimum,
            operationHeight = operationHeight,
            minimumMetadataHeight = metadataHeight,
            metadataHeight = (height - primaryMinimum).coerceAtLeast(0f),
            metadataGap = 0f,
            localScrollFallback = false
        )
    }
    return QuickPartitionDetailsAllocation(
        stepsHeight = stepMinimum,
        operationHeight = operationHeight,
        minimumMetadataHeight = metadataHeight,
        metadataHeight = 0f,
        metadataGap = gap,
        localScrollFallback = true
    )
}
