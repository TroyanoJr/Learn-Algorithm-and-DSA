package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun QueueVisualizationCard(
    demonstration: QueueDemonstration,
    title: String = "Queue Visualizer"
) {
    val steps = QueueTrace.stepsFor(demonstration)
    var isExpanded by rememberSaveable(demonstration) { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0, key = demonstration)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open queue visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            QueueTraceEvent.START -> "READY · CAPACITY ${step.capacity}"
            QueueTraceEvent.ENQUEUE -> "ENQUEUED ${step.operationValue}"
            QueueTraceEvent.DEQUEUE -> "DEQUEUED ${step.returnedValue}"
            QueueTraceEvent.PEEK -> "FRONT ${step.returnedValue}"
            QueueTraceEvent.UNDERFLOW, QueueTraceEvent.EMPTY_ACCESS -> "EMPTY ACCESS"
            QueueTraceEvent.OVERFLOW -> "FULL"
            QueueTraceEvent.COMPLETE -> "COMPLETE"
        }

        val statusColor = when (step.event) {
            QueueTraceEvent.COMPLETE -> GreenAccent
            QueueTraceEvent.UNDERFLOW, QueueTraceEvent.EMPTY_ACCESS, QueueTraceEvent.OVERFLOW -> RedAccent
            else -> BlueAccent
        }

        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = isPlaying,
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
                QueueVisualizationContent(step = step, scale = 1f)
            },
            detailsContent = {
                QueueDetailsAndStepsContent(step = step, stepIndex = stepIndex, totalSteps = steps.size, scale = 1f)
            }
        )
    }
}

@Composable
private fun QueueVisualizationContent(step: QueueTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp * scale)
    ) {
        // Circular Array Slots Row (compact, nicely proportioned cells, fully visible simultaneously without scrolling)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            step.slots.forEachIndexed { index, value ->
                val isFront = index == step.frontIndex
                val isRear = index == step.nextRearIndex
                val highlighted = index == step.highlightedSlot
                Column(
                    modifier = Modifier.width(56.dp * scale).padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                ) {
                    Text(
                        when {
                            isFront && isRear -> "FRONT/REAR"
                            isFront -> "FRONT"
                            isRear -> "REAR"
                            else -> " "
                        },
                        color = if (isFront) OrangeAccent else BlueAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Surface(
                        color = when {
                            highlighted -> Color(0xFF2A1C12)
                            value != null -> Color(0xFF13233A)
                            else -> Color(0xFF13131C)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (highlighted) OrangeAccent else CardBorderColor),
                        modifier = Modifier.width(52.dp * scale).height(44.dp * scale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(value ?: "·", color = if (highlighted) OrangeAccent else TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("$index", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        // Linked Representation (if applicable)
        if (step.representation == QueueRepresentation.ARRAY_AND_LINKED) {
            Text(
                "LINKED NODES · FRONT = ${step.values.firstOrNull() ?: "null"} · REAR = ${step.values.lastOrNull() ?: "null"}",
                color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step.values.isEmpty()) {
                    Text("FRONT / REAR = null", color = TextSecondary, fontSize = 10.sp)
                } else {
                    Text("FRONT →", color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    step.values.forEachIndexed { index, value ->
                        Surface(
                            color = if (step.event == QueueTraceEvent.ENQUEUE && index == step.values.lastIndex) Color(0xFF2A1C12) else Color(0xFF13233A),
                            shape = RoundedCornerShape(7.dp), border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.width(54.dp * scale).height(28.dp * scale)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("$value | next", color = TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(if (index == step.values.lastIndex) "→ null · REAR" else "→", color = BlueAccent, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueDetailsAndStepsContent(step: QueueTraceStep, stepIndex: Int, totalSteps: Int, scale: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp * scale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("OPERATION RESULT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.returnedValue ?: "—",
                    color = if (step.returnedValue != null) GreenAccent else TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Logical front → rear: ${step.values.joinToString(prefix = "[", postfix = "]")}", color = TextPrimary, fontSize = 11.sp)
                Text("size ${step.size} / ${step.capacity}", color = TextSecondary, fontSize = 10.sp)
            }
        }

        StepsCard(
            label = "Steps",
            explanation = step.explanation,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isCompleted = stepIndex == totalSteps - 1
        )
    }
}
