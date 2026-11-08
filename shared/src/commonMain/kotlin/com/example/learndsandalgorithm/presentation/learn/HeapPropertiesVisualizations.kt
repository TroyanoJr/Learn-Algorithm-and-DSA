package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun HeapPropertiesVisualizationCard(
    title: String = "Heap Properties and Array Representation"
) {
    var selectedTopic by rememberSaveable { mutableStateOf(HeapPropertyTopic.MIN_HEAP_STRUCTURE) }
    val steps = HeapPropertiesTrace.stepsForTopic(selectedTopic)
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedTopic
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open heap properties visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.topic) {
            HeapPropertyTopic.MIN_HEAP_STRUCTURE -> "MIN-HEAP PROPERTY (parent <= children)"
            HeapPropertyTopic.INDEX_MAPPING -> "INDEX FORMULAS: parent = (i-1)/2, left = 2i+1, right = 2i+2"
            HeapPropertyTopic.MAX_HEAP_STRUCTURE -> "MAX-HEAP PROPERTY (parent >= children)"
        }

        val statusColor = when (step.topic) {
            HeapPropertyTopic.MIN_HEAP_STRUCTURE -> GreenAccent
            HeapPropertyTopic.INDEX_MAPPING -> BlueAccent
            HeapPropertyTopic.MAX_HEAP_STRUCTURE -> OrangeAccent
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
                HeapTopicSelector(
                    selectedTopic = selectedTopic,
                    onTopicSelected = { newTopic ->
                        if (newTopic != selectedTopic) {
                            playback.reset()
                            selectedTopic = newTopic
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    HeapPropertiesVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                HeapPropertiesDetailsAndStepsContent(
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
private fun HeapTopicSelector(
    selectedTopic: HeapPropertyTopic,
    onTopicSelected: (HeapPropertyTopic) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            HeapPropertyTopic.MIN_HEAP_STRUCTURE to "1. Min-Heap Property",
            HeapPropertyTopic.INDEX_MAPPING to "2. Index Mapping Formulas",
            HeapPropertyTopic.MAX_HEAP_STRUCTURE to "3. Max-Heap Property"
        ).forEach { (topic, label) ->
            val isSelected = selectedTopic == topic
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .clickable { onTopicSelected(topic) }
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
private fun HeapPropertiesVisualizationContent(step: HeapPropertiesTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Tree Representation Box
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text(
                    text = "COMPLETE BINARY TREE REPRESENTATION:",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                // Level 0: Root [0]
                if (step.array.isNotEmpty()) {
                    HeapTreeNode(
                        value = step.array[0],
                        index = 0,
                        isHighlighted = step.inspectedIndex == 0,
                        isParent = step.parentIndex == 0
                    )
                    TreeBranchConnector(label = "↙     ↘")
                }

                // Level 1: [1], [2]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (step.array.size > 1) {
                        HeapTreeNode(
                            value = step.array[1],
                            index = 1,
                            isHighlighted = step.inspectedIndex == 1 || step.leftChildIndex == 1,
                            isParent = step.parentIndex == 1
                        )
                    }
                    if (step.array.size > 2) {
                        HeapTreeNode(
                            value = step.array[2],
                            index = 2,
                            isHighlighted = step.inspectedIndex == 2 || step.rightChildIndex == 2,
                            isParent = step.parentIndex == 2
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeBranchConnector(label = "↙   ↘")
                    TreeBranchConnector(label = "↙")
                }

                // Level 2: [3], [4], [5]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (step.array.size > 3) HeapTreeNode(step.array[3], 3, isHighlighted = step.inspectedIndex == 3 || step.leftChildIndex == 3)
                    if (step.array.size > 4) HeapTreeNode(step.array[4], 4, isHighlighted = step.inspectedIndex == 4 || step.rightChildIndex == 4)
                    if (step.array.size > 5) HeapTreeNode(step.array[5], 5, isHighlighted = step.inspectedIndex == 5 || step.leftChildIndex == 5)
                }
            }
        }

        // Array Row Component
        HeapArrayRow(
            array = step.array,
            highlightedIndex = step.inspectedIndex,
            parentIndex = step.parentIndex
        )
    }
}

@Composable
private fun HeapPropertiesDetailsAndStepsContent(
    step: HeapPropertiesTraceStep,
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
                Text("CURRENT CONCEPT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.topic) {
                        HeapPropertyTopic.MIN_HEAP_STRUCTURE -> GreenAccent
                        HeapPropertyTopic.INDEX_MAPPING -> BlueAccent
                        HeapPropertyTopic.MAX_HEAP_STRUCTURE -> OrangeAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Root Access: O(1) Constant Time", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
