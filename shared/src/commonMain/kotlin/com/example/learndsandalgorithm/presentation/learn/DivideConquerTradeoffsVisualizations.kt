package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val OrangeAccent = Color(0xFFFBBF24)
private val GreenAccent = Color(0xFF4ADE80)
private val TextSecondary = Color(0xFFB8C3D1)
private const val DETAILS_BUTTON_MIN_HEIGHT_DP = 48f
internal const val TRADEOFF_PLAYBACK_DELAY_MILLIS = 3000L

@Composable
internal fun DivideConquerTradeoffsVisualizationCard(
    title: String = "Partition Split Balance"
) {
    var selectedTree by rememberSaveable { mutableStateOf(TradeoffTreeSelection.BALANCED) }
    val steps = remember { partitionSplitVisualizerSteps() }
    val sceneFrames = remember(steps) { DivideConquerTradeoffsSceneBuilder.build(steps) }
    val balancedPhases = remember(sceneFrames) { tradeoffPlaybackPhases(sceneFrames, TradeoffTreeSelection.BALANCED) }
    val unbalancedPhases = remember(sceneFrames) { tradeoffPlaybackPhases(sceneFrames, TradeoffTreeSelection.UNBALANCED) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val balancedPlayback = rememberVisualizerPlaybackState(
        stepCount = balancedPhases.size,
        initialStep = 0,
        stepDelayMillis = TRADEOFF_PLAYBACK_DELAY_MILLIS,
        key = "partition_split_balanced"
    )
    val unbalancedPlayback = rememberVisualizerPlaybackState(
        stepCount = unbalancedPhases.size,
        initialStep = 0,
        stepDelayMillis = TRADEOFF_PLAYBACK_DELAY_MILLIS,
        key = "partition_split_unbalanced"
    )
    val phases = if (selectedTree == TradeoffTreeSelection.BALANCED) balancedPhases else unbalancedPhases
    val playback = if (selectedTree == TradeoffTreeSelection.BALANCED) balancedPlayback else unbalancedPlayback
    val phaseIndex = playback.safeStepIndex
    val phase = phases[phaseIndex]
    val step = steps[phase.semanticStepIndex]
    val frame = sceneFrames[phase.semanticStepIndex]
    val detailsScrollState = rememberScrollState()
    val detailsScope = rememberCoroutineScope()
    val returnDetailsToStart = { detailsScope.launch { detailsScrollState.animateScrollTo(0) } }

    if (!autoExpand) {
        ArrayWindowButton(text = "Open Partition Split Balance visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        VisualizerModal(
            title = title,
            statusText = "${phase.label.uppercase()} · N = ${step.inputN}",
            isCompleted = playback.isCompleted,
            stepIndex = phaseIndex,
            stepCount = phases.size,
            isPlaying = playback.isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            fullWidthVisualWeight = 1.5f,
            statusColor = OrangeAccent,
            detailsScrollable = false,
            onReset = {
                playback.reset()
                returnDetailsToStart()
            },
            onPrevious = { playback.previous(); returnDetailsToStart() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next(); returnDetailsToStart() },
            onClose = {
                balancedPlayback.reset()
                unbalancedPlayback.reset()
                isExpanded = false
            },
            visualContent = {
                TradeoffRecursionTree(
                    frame = frame,
                    phase = phase,
                    selection = selectedTree,
                    onSelectionChanged = { selection ->
                        selectedTree = selectTradeoffTree(
                            current = selectedTree,
                            next = selection,
                            balancedPlayback = balancedPlayback,
                            unbalancedPlayback = unbalancedPlayback
                        )
                        returnDetailsToStart()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            },
            detailsContent = {
                DivideConquerTradeoffsDetailsAndStepsContent(
                    step = step,
                    phase = phase,
                    phaseIndex = phaseIndex,
                    totalPhases = phases.size,
                    detailsScrollState = detailsScrollState
                )
            }
        )
    }
}

@Composable
private fun DivideConquerTradeoffsDetailsAndStepsContent(
    step: TradeoffsTraceStep,
    phase: TradeoffPlaybackPhase,
    phaseIndex: Int,
    totalPhases: Int,
    detailsScrollState: androidx.compose.foundation.ScrollState
) {
    var showMetadata by rememberSaveable(step.scenario, phaseIndex) { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier.fillMaxSize().verticalScroll(detailsScrollState),
        verticalArrangement = Arrangement.Top
      ) {
        val baseStyle = androidx.compose.material3.LocalTextStyle.current
        val operationHeaderStyle = baseStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
        val operationValueStyle = baseStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 15.sp)
        StepsCard(
            label = "Steps",
            explanation = phase.explanation,
            stepIndex = phaseIndex,
            totalSteps = totalPhases,
            isCompleted = phase.kind == TradeoffPlaybackPhaseKind.COMPLETE
        )
        Spacer(Modifier.height(6.dp))
        key(phaseIndex, "tradeoff-operation") {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CURRENT OPERATION", color = TextSecondary, style = operationHeaderStyle, modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { showMetadata = true },
                        modifier = Modifier.heightIn(min = DETAILS_BUTTON_MIN_HEIGHT_DP.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("Details", fontSize = 10.sp) }
                }
                Text(
                    tradeoffOperationText(phase, step),
                    modifier = Modifier.fillMaxWidth(),
                    color = OrangeAccent,
                    style = operationValueStyle
                )
            }
        }
        Spacer(Modifier.height(32.dp))
      }
    }
    if (showMetadata) {
        AlertDialog(
            onDismissRequest = { showMetadata = false },
            title = { Text("Analysis Details") },
            text = {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(tradeoffMetadataLines(step).first(), color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        step.badgeText,
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(tradeoffMetadataLines(step).last(), color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(step.explanation, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }
            },
            confirmButton = { TextButton(onClick = { showMetadata = false }) { Text("Close") } }
        )
    }
}

internal fun tradeoffMetadataLines(step: TradeoffsTraceStep): List<String> =
    listOf("ANALYSIS SCENARIO", step.badgeText, "Divide & Conquer Complexity")

internal fun partitionSplitVisualizerSteps(): List<TradeoffsTraceStep> =
    DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)

internal fun tradeoffOperationText(phase: TradeoffPlaybackPhase, step: TradeoffsTraceStep): String =
    "${phase.label}\n${if (phase.kind == TradeoffPlaybackPhaseKind.COMPLETE) step.badgeText else phase.operation}"
