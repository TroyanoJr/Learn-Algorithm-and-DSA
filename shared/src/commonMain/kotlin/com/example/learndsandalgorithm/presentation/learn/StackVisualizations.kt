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
internal fun StackVisualizationCard(
    demonstration: StackDemonstration,
    title: String = "Stack Visualizer"
) {
    val steps = StackTrace.stepsFor(demonstration)
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

    ArrayWindowButton(text = "Open stack visualizer") { isExpanded = true }

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
                                        StackTraceEvent.START -> "READY"
                                        StackTraceEvent.PUSH -> "PUSH ${step.operationValue ?: step.values.lastOrNull()}"
                                        StackTraceEvent.POP -> "POPPED ${step.returnedValue}"
                                        StackTraceEvent.PEEK -> "TOP ${step.returnedValue}"
                                        StackTraceEvent.UNDERFLOW -> "EMPTY ACCESS"
                                        StackTraceEvent.DELIMITER_MATCH -> "MATCH ${step.operationValue}"
                                        StackTraceEvent.DELIMITER_MISMATCH, StackTraceEvent.UNMATCHED_CLOSER -> "INVALID CLOSER ${step.operationValue}"
                                        StackTraceEvent.OVERFLOW -> "FULL"
                                        StackTraceEvent.COMPLETE -> if (step.isValid == false) "UNBALANCED" else "COMPLETE"
                                    },
                                    color = when (step.event) {
                                        StackTraceEvent.UNDERFLOW, StackTraceEvent.OVERFLOW, StackTraceEvent.DELIMITER_MISMATCH, StackTraceEvent.UNMATCHED_CLOSER -> RedAccent
                                        StackTraceEvent.COMPLETE -> if (step.isValid == false) RedAccent else GreenAccent
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
                                // Left Region: Stack Visualization (Unclipped, scrollable if height/width is tight)
                                Column(
                                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    StackVisualizationContent(step = step, scale = scale)
                                }

                                // Right Region: Operation Result, Logical View, and Steps Explanation Card (scrollable if text is long)
                                Column(
                                    modifier = Modifier.weight(1.2f).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StackDetailsAndStepsContent(step = step, stepIndex = stepIndex, totalSteps = steps.size, scale = scale)
                                }
                            }
                        } else {
                            // Portrait Layout: Stacked with clear region separation
                            Column(
                                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                StackVisualizationContent(step = step, scale = scale)
                                StackDetailsAndStepsContent(step = step, stepIndex = stepIndex, totalSteps = steps.size, scale = scale)
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
private fun StackVisualizationContent(step: StackTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp * scale)
    ) {
        if (step.representation == StackRepresentation.ARRAY_AND_LINKED) {
            Text("ARRAY · TOP = LAST INDEX", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            if (step.values.isEmpty()) {
                Text("EMPTY", color = TextSecondary, fontSize = 10.sp)
            } else {
                step.values.indices.reversed().forEach { index ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        Text("$index", color = TextSecondary, fontSize = 8.sp)
                        Surface(
                            color = if (index == step.highlightedIndex) Color(0xFF2A1C12) else Color(0xFF13131C),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (index == step.highlightedIndex) OrangeAccent else CardBorderColor),
                            modifier = Modifier.width(72.dp * scale).height(22.dp * scale)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(step.values[index], color = if (index == step.highlightedIndex) OrangeAccent else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (index == step.values.lastIndex) Text("TOP", color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("LINKED · TOP = HEAD", color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            if (step.values.isEmpty()) {
                Text("HEAD = null", color = TextSecondary, fontSize = 9.sp)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HEAD →", color = BlueAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    step.values.indices.reversed().forEach { index ->
                        Surface(
                            color = if (index == step.highlightedIndex) Color(0xFF2A1C12) else Color(0xFF13131C),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (index == step.highlightedIndex) OrangeAccent else CardBorderColor),
                            modifier = Modifier.width(46.dp * scale).height(20.dp * scale)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${step.values[index]} | next", color = if (index == step.highlightedIndex) OrangeAccent else TextPrimary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(if (index == 0) "→ null" else "→", color = BlueAccent, fontSize = 8.sp)
                    }
                }
            }
        } else {
            Text("TOP ↓", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            if (step.values.isEmpty()) {
                Surface(
                    color = Color(0xFF13131C), shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.width(130.dp * scale).height(38.dp * scale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("EMPTY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                step.values.indices.reversed().forEach { index ->
                    val highlighted = index == step.highlightedIndex
                    Surface(
                        color = if (highlighted) Color(0xFF2A1C12) else Color(0xFF13131C),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (highlighted) OrangeAccent else CardBorderColor),
                        modifier = Modifier.width(130.dp * scale).height(26.dp * scale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(step.values[index], color = if (highlighted) OrangeAccent else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text("BOTTOM", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StackDetailsAndStepsContent(step: StackTraceStep, stepIndex: Int, totalSteps: Int, scale: Float) {
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
            Text(
                "Bottom → top: ${step.values.joinToString(prefix = "[", postfix = "]")}",
                color = TextSecondary,
                fontSize = 11.sp
            )
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
                Text(step.explanation, color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}
