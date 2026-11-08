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

private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun HashTableApplicationsVisualizationCard(
    title: String = "Applications, Trade-offs, and Practical Design"
) {
    var selectedScenario by rememberSaveable { mutableStateOf(ApplicationScenario.FREQUENCY_COUNTER) }
    val steps = HashTableApplicationsTrace.stepsForScenario(selectedScenario)
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedScenario
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open applications visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.scenario) {
            ApplicationScenario.FREQUENCY_COUNTER -> "FREQUENCY COUNTER"
            ApplicationScenario.RANGE_QUERY_COMPARISON -> "HASH MAP vs BST (RANGE QUERY)"
            ApplicationScenario.MUTABLE_KEY_PITFALL -> if (step.keyMutated) "KEY MUTATED (LOOKUP FAILED)" else "MUTABLE KEY PITFALL"
        }

        val statusColor = when (step.scenario) {
            ApplicationScenario.MUTABLE_KEY_PITFALL -> if (step.keyMutated) RedAccent else OrangeAccent
            ApplicationScenario.RANGE_QUERY_COMPARISON -> BlueAccent
            ApplicationScenario.FREQUENCY_COUNTER -> GreenAccent
        }

        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = statusColor,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            topContent = {
                ApplicationScenarioSelector(
                    selectedScenario = selectedScenario,
                    onScenarioSelected = { newScenario ->
                        if (newScenario != selectedScenario) {
                            playback.reset()
                            selectedScenario = newScenario
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    HashTableApplicationsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                HashTableApplicationsDetailsAndStepsContent(
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
private fun ApplicationScenarioSelector(
    selectedScenario: ApplicationScenario,
    onScenarioSelected: (ApplicationScenario) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            ApplicationScenario.FREQUENCY_COUNTER to "1. Frequency Counter",
            ApplicationScenario.RANGE_QUERY_COMPARISON to "2. Range Queries (BST vs Hash)",
            ApplicationScenario.MUTABLE_KEY_PITFALL to "3. Mutable Key Pitfall"
        ).forEach { (scenario, label) ->
            val isSelected = selectedScenario == scenario
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .clickable { onScenarioSelected(scenario) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HashTableApplicationsVisualizationContent(step: ApplicationsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        when (step.scenario) {
            ApplicationScenario.FREQUENCY_COUNTER -> {
                // Input String breakdown header
                Surface(
                    color = Color(0xFF13131C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                    ) {
                        Text(
                            "INPUT TEXT: \"${step.inputText}\"",
                            color = OrangeAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            step.inputText?.forEachIndexed { idx, char ->
                                val isActive = idx == step.currentCharIndex
                                Surface(
                                    color = if (isActive) Color(0xFF2A1C12) else Color(0xFF1F1F2C),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isActive) OrangeAccent else CardBorderColor)
                                ) {
                                    Text(
                                        text = "'$char'",
                                        color = if (isActive) OrangeAccent else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp * scale, vertical = 4.dp * scale)
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Frequency Map Result
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
                        Text(
                            "FREQUENCY HASH MAP (O(1) Avg Update):",
                            color = BlueAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (step.freqMap.isEmpty()) {
                            Text("Map empty", color = TextSecondary, fontSize = 11.sp)
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp * scale),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                step.freqMap.forEach { pair ->
                                    val isCurrentChar = pair.char == step.currentChar
                                    Surface(
                                        color = if (isCurrentChar) Color(0xFF1A2B3C) else Color(0xFF181824),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (isCurrentChar) BlueAccent else CardBorderColor)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp * scale, vertical = 6.dp * scale),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("'${pair.char}'", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("count: ${pair.count}", color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ApplicationScenario.RANGE_QUERY_COMPARISON -> {
                // Range Query Side-by-side comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp * scale)
                ) {
                    // Hash Table Card
                    Surface(
                        color = Color(0xFF13131C),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp * scale),
                            verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                        ) {
                            Text(
                                "HASH TABLE (UNORDERED):",
                                color = OrangeAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Range [10..25] requires O(N) full scan", color = TextSecondary, fontSize = 9.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                step.hashBucketKeys?.forEachIndexed { idx, key ->
                                    val isVisited = idx in step.hashVisitedBuckets
                                    val inRange = key != null && key in (step.rangeMin ?: 0)..(step.rangeMax ?: 0)
                                    Surface(
                                        color = when {
                                            isVisited && inRange -> Color(0xFF123820)
                                            isVisited -> Color(0xFF2A1C12)
                                            else -> Color(0xFF181824)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (isVisited && inRange) GreenAccent else if (isVisited) OrangeAccent else CardBorderColor),
                                        modifier = Modifier.weight(1f).padding(horizontal = 1.dp * scale)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp * scale)) {
                                            Text(
                                                "${key ?: "·"}",
                                                color = if (inRange) GreenAccent else TextPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Balanced BST Card
                    Surface(
                        color = Color(0xFF13131C),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp * scale),
                            verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                        ) {
                            Text(
                                "BALANCED BST (SORTED):",
                                color = BlueAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Range [10..25] O(log N + k) tree traversal", color = TextSecondary, fontSize = 9.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                step.bstKeys?.forEach { key ->
                                    val isMatched = key in step.bstVisitedKeys
                                    Surface(
                                        color = if (isMatched) Color(0xFF123820) else Color(0xFF181824),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (isMatched) GreenAccent else CardBorderColor),
                                        modifier = Modifier.weight(1f).padding(horizontal = 1.dp * scale)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp * scale)) {
                                            Text(
                                                "$key",
                                                color = if (isMatched) GreenAccent else TextSecondary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ApplicationScenario.MUTABLE_KEY_PITFALL -> {
                // Mutable Key Pitfall visualization
                Surface(
                    color = Color(0xFF13131C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (step.keyMutated) RedAccent else CardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "MUTABLE KEY PITFALL",
                                color = if (step.keyMutated) RedAccent else OrangeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (step.keyMutated) {
                                Surface(
                                    color = Color(0xFF381212),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, RedAccent)
                                ) {
                                    Text(
                                        "KEY MUTATED IN-PLACE",
                                        color = RedAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp * scale, vertical = 3.dp * scale)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Entry stored at bucket [${step.storedSlotIndex}] (hash(${step.originalKeyId}) % 8)",
                                color = BlueAccent,
                                fontSize = 10.sp
                            )
                            if (step.searchedSlotIndex != null) {
                                Text(
                                    text = "➔ Lookup checked bucket [${step.searchedSlotIndex}] (hash(${step.mutatedKeyId}) % 8)",
                                    color = RedAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Array slots 0..7
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (0..7).forEach { idx ->
                                val isStored = idx == step.storedSlotIndex
                                val isSearched = idx == step.searchedSlotIndex
                                Surface(
                                    color = when {
                                        isSearched -> Color(0xFF381212)
                                        isStored -> Color(0xFF13233A)
                                        else -> Color(0xFF181824)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, when {
                                        isSearched -> RedAccent
                                        isStored -> BlueAccent
                                        else -> CardBorderColor
                                    }),
                                    modifier = Modifier.weight(1f).height(44.dp * scale).padding(horizontal = 1.dp * scale)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (isStored) "id:${if (step.keyMutated) step.mutatedKeyId else step.originalKeyId}" else "·",
                                            color = if (isSearched) RedAccent else TextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text("[$idx]", color = TextSecondary, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HashTableApplicationsDetailsAndStepsContent(
    step: ApplicationsTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float
) {
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
                Text("SCENARIO RESULT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.scenario) {
                        ApplicationScenario.MUTABLE_KEY_PITFALL -> if (step.keyMutated) RedAccent else OrangeAccent
                        ApplicationScenario.RANGE_QUERY_COMPARISON -> BlueAccent
                        ApplicationScenario.FREQUENCY_COUNTER -> GreenAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val recommendation = when (step.scenario) {
                    ApplicationScenario.FREQUENCY_COUNTER -> "Best for: Fast O(1) frequency counting"
                    ApplicationScenario.RANGE_QUERY_COMPARISON -> "Rule: Use BST for range queries & order"
                    ApplicationScenario.MUTABLE_KEY_PITFALL -> "Rule: Always use immutable keys"
                }
                Text(recommendation, color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
