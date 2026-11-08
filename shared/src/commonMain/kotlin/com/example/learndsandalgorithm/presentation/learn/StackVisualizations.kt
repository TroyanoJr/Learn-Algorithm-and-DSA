package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

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

    var selectedView by rememberSaveable(demonstration) { mutableStateOf("Array") }

    // Independent state for Array view in lesson 3
    var arrayStepIndex by rememberSaveable(demonstration, "Array") { mutableIntStateOf(0) }
    var arrayIsPlaying by rememberSaveable(demonstration, "Array") { mutableStateOf(false) }

    // Independent state for Linked list view in lesson 3
    var linkedStepIndex by rememberSaveable(demonstration, "Linked list") { mutableIntStateOf(0) }
    var linkedIsPlaying by rememberSaveable(demonstration, "Linked list") { mutableStateOf(false) }

    // Common state for Abstract demonstrations
    var abstractStepIndex by rememberSaveable(demonstration) { mutableIntStateOf(0) }
    var abstractIsPlaying by rememberSaveable(demonstration) { mutableStateOf(false) }

    var isExpanded by rememberSaveable(demonstration) { mutableStateOf(false) }

    val isArrayAndLinked = demonstration == StackDemonstration.IMPLEMENTATIONS

    val activeStepIndex = when {
        isArrayAndLinked -> if (selectedView == "Array") arrayStepIndex else linkedStepIndex
        else -> abstractStepIndex
    }

    val activeIsPlaying = when {
        isArrayAndLinked -> if (selectedView == "Array") arrayIsPlaying else linkedIsPlaying
        else -> abstractIsPlaying
    }

    fun updateActiveStepIndex(newIndex: Int) {
        when {
            isArrayAndLinked -> if (selectedView == "Array") arrayStepIndex = newIndex else linkedStepIndex = newIndex
            else -> abstractStepIndex = newIndex
        }
    }

    fun updateActiveIsPlaying(playing: Boolean) {
        when {
            isArrayAndLinked -> if (selectedView == "Array") arrayIsPlaying = playing else linkedIsPlaying = playing
            else -> abstractIsPlaying = playing
        }
    }

    val step = steps[activeStepIndex]

    LaunchedEffect(activeIsPlaying, activeStepIndex, steps) {
        if (activeIsPlaying && activeStepIndex < steps.lastIndex) {
            delay(1000L)
            if (activeStepIndex < steps.lastIndex) {
                updateActiveStepIndex(activeStepIndex + 1)
            } else {
                updateActiveIsPlaying(false)
            }
        } else if (activeIsPlaying) {
            updateActiveIsPlaying(false)
        }
    }

    ArrayWindowButton(text = "Open stack visualizer") { isExpanded = true }

    if (isExpanded) {
        val statusText = when (step.event) {
            StackTraceEvent.START -> "READY"
            StackTraceEvent.PUSH -> "PUSH ${step.operationValue ?: step.values.lastOrNull()}"
            StackTraceEvent.POP -> "POPPED ${step.returnedValue}"
            StackTraceEvent.PEEK -> "TOP ${step.returnedValue}"
            StackTraceEvent.UNDERFLOW -> "EMPTY ACCESS"
            StackTraceEvent.DELIMITER_MATCH -> "MATCH ${step.operationValue}"
            StackTraceEvent.DELIMITER_MISMATCH, StackTraceEvent.UNMATCHED_CLOSER -> "INVALID CLOSER ${step.operationValue}"
            StackTraceEvent.OVERFLOW -> "FULL"
            StackTraceEvent.COMPLETE -> if (step.isValid == false) "UNBALANCED" else "COMPLETE"
        }

        val statusColor = when (step.event) {
            StackTraceEvent.UNDERFLOW, StackTraceEvent.OVERFLOW, StackTraceEvent.DELIMITER_MISMATCH, StackTraceEvent.UNMATCHED_CLOSER -> RedAccent
            StackTraceEvent.COMPLETE -> if (step.isValid == false) RedAccent else GreenAccent
            else -> BlueAccent
        }

        val isCompleted = activeStepIndex == steps.lastIndex

        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = isCompleted,
            stepIndex = activeStepIndex,
            stepCount = steps.size,
            isPlaying = activeIsPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = statusColor,
            onReset = { updateActiveIsPlaying(false); updateActiveStepIndex(0) },
            onPrevious = { updateActiveIsPlaying(false); if (activeStepIndex > 0) updateActiveStepIndex(activeStepIndex - 1) },
            onTogglePlayback = { updateActiveIsPlaying(!activeIsPlaying) },
            onNext = { updateActiveIsPlaying(false); if (activeStepIndex < steps.lastIndex) updateActiveStepIndex(activeStepIndex + 1) },
            onClose = { updateActiveIsPlaying(false); isExpanded = false },
            topContent = if (isArrayAndLinked) {
                {
                    StackImplementationSelector(
                        selectedView = selectedView,
                        onViewSelected = { newView ->
                            if (newView != selectedView) {
                                updateActiveIsPlaying(false)
                                selectedView = newView
                            }
                        }
                    )
                }
            } else null,
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    StackVisualizationContent(step = step, scale = 1f, selectedView = selectedView)
                }
            },
            detailsContent = {
                StackDetailsAndStepsContent(
                    step = step,
                    stepIndex = activeStepIndex,
                    totalSteps = steps.size,
                    scale = 1f
                )
            }
        )
    }
}

@Composable
private fun StackImplementationSelector(
    selectedView: String,
    onViewSelected: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf("Array" to "Array", "Linked list" to "Linked list").forEach { (viewKey, label) ->
            val isSelected = selectedView == viewKey
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clickable { onViewSelected(viewKey) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StackVisualizationContent(
    step: StackTraceStep,
    scale: Float,
    selectedView: String = "Array"
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp * scale)
    ) {
        if (step.representation == StackRepresentation.ARRAY_AND_LINKED) {
            if (selectedView == "Array") {
                Text("ARRAY - TOP = LAST INDEX", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (step.values.isEmpty()) {
                    Text("EMPTY", color = TextSecondary, fontSize = 12.sp)
                } else {
                    step.values.indices.reversed().forEach { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp * scale)
                        ) {
                            Text(
                                text = "$index",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.width(16.dp * scale),
                                textAlign = TextAlign.End
                            )
                            Surface(
                                color = if (index == step.highlightedIndex) Color(0xFF2A1C12) else Color(0xFF13131C),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (index == step.highlightedIndex) OrangeAccent else CardBorderColor),
                                modifier = Modifier.width(80.dp * scale).height(32.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(step.values[index], color = if (index == step.highlightedIndex) OrangeAccent else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier.width(28.dp * scale),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (index == step.values.lastIndex) {
                                    Text("TOP", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Text("LINKED - TOP = HEAD", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (step.values.isEmpty()) {
                    Text("HEAD = null", color = TextSecondary, fontSize = 12.sp)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HEAD →", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        step.values.indices.reversed().forEach { index ->
                            Surface(
                                color = if (index == step.highlightedIndex) Color(0xFF2A1C12) else Color(0xFF13131C),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (index == step.highlightedIndex) OrangeAccent else CardBorderColor),
                                modifier = Modifier.width(52.dp * scale).height(36.dp * scale)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(step.values[index], color = if (index == step.highlightedIndex) OrangeAccent else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = CardBorderColor,
                                        shape = RoundedCornerShape(3.dp),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text("•", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 3.dp))
                                    }
                                }
                            }
                            Text(if (index == 0) "→ null" else "→", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Text("TOP ↓", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (step.values.isEmpty()) {
                Surface(
                    color = Color(0xFF13131C), shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.width(130.dp * scale).height(38.dp * scale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("EMPTY", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                step.values.indices.reversed().forEach { index ->
                    val highlighted = index == step.highlightedIndex
                    Surface(
                        color = if (highlighted) Color(0xFF2A1C12) else Color(0xFF13131C),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (highlighted) OrangeAccent else CardBorderColor),
                        modifier = Modifier.width(130.dp * scale).height(32.dp * scale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(step.values[index], color = if (highlighted) OrangeAccent else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text("BOTTOM", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                Text("OPERATION RESULT", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                fontSize = 12.sp
            )
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
