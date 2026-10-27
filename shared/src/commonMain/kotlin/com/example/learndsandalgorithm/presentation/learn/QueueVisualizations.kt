package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.learndsandalgorithm.core.platform.LandscapeOrientationEffect
import kotlinx.coroutines.delay

private val DarkCardBgColor = Color(0xFF181820)
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
    var stepIndex by rememberSaveable(demonstration) { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable(demonstration) { mutableStateOf(false) }
    var isExpanded by rememberSaveable(demonstration) { mutableStateOf(false) }
    val step = steps[stepIndex]

    LaunchedEffect(isPlaying, stepIndex, steps) {
        if (isPlaying && stepIndex < steps.lastIndex) {
            delay(1000L)
            if (stepIndex < steps.lastIndex) stepIndex++ else isPlaying = false
        } else if (isPlaying) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = "Open queue visualizer") { isExpanded = true }

    if (isExpanded) {
        Dialog(
            onDismissRequest = { isPlaying = false; isExpanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            LandscapeOrientationEffect(enabled = true)
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val isLandscape = maxWidth > maxHeight
                val scale = minOf(maxWidth / 800.dp, maxHeight / 360.dp).coerceAtMost(1f)
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp * scale),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header Row with non-overlapping layout (Title + Status stacked on left, Close button on right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(title.uppercase(), color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    when (step.event) {
                                        QueueTraceEvent.START -> "READY · CAPACITY ${step.capacity}"
                                        QueueTraceEvent.ENQUEUE -> "ENQUEUED ${step.operationValue}"
                                        QueueTraceEvent.DEQUEUE -> "DEQUEUED ${step.returnedValue}"
                                        QueueTraceEvent.PEEK -> "FRONT ${step.returnedValue}"
                                        QueueTraceEvent.UNDERFLOW, QueueTraceEvent.EMPTY_ACCESS -> "EMPTY ACCESS"
                                        QueueTraceEvent.OVERFLOW -> "FULL"
                                        QueueTraceEvent.COMPLETE -> "COMPLETE"
                                    },
                                    color = when (step.event) {
                                        QueueTraceEvent.COMPLETE -> GreenAccent
                                        QueueTraceEvent.UNDERFLOW, QueueTraceEvent.EMPTY_ACCESS, QueueTraceEvent.OVERFLOW -> RedAccent
                                        else -> BlueAccent
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            ArrayWindowButton(text = "Close", icon = "✕") {
                                isPlaying = false
                                isExpanded = false
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Central Flexible Area: Distinct separation between Data Visualization (left) and Explanations/Results (right)
                        if (isLandscape) {
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Region: Queue Visualization (Array slots & Linked representation)
                                Column(
                                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    QueueVisualizationContent(step = step, scale = scale)
                                }

                                // Right Region: Logical view, Result, and Steps Explanation Card (scrollable if text is long)
                                Column(
                                    modifier = Modifier.weight(1.2f).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    QueueDetailsAndStepsContent(step = step, stepIndex = stepIndex, totalSteps = steps.size, scale = scale)
                                }
                            }
                        } else {
                            // Portrait Layout: Stacked with clear region separation
                            Column(
                                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                QueueVisualizationContent(step = step, scale = scale)
                                QueueDetailsAndStepsContent(step = step, stepIndex = stepIndex, totalSteps = steps.size, scale = scale)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bottom Playback Controls (Pinned region, never clipped or overlapped)
                        LinkedListPlaybackControls(
                            stepIndex = stepIndex,
                            stepCount = steps.size,
                            isPlaying = isPlaying,
                            onReset = { isPlaying = false; stepIndex = 0 },
                            onPrevious = { isPlaying = false; if (stepIndex > 0) stepIndex-- },
                            onTogglePlayback = { isPlaying = !isPlaying },
                            onNext = { isPlaying = false; if (stepIndex < steps.lastIndex) stepIndex++ }
                        )
                    }
                }
            }
        }
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
            modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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

        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale),
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Steps", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("STEP ${stepIndex + 1} / $totalSteps", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(step.explanation, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}
