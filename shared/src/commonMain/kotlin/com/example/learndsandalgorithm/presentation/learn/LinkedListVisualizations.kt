package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

/** Tarjeta "Steps" que utiliza el componente compartido StepsCard. */
@Composable
private fun LinkedListStepsCard(
    label: String,
    explanation: String,
    stepIndex: Int,
    totalSteps: Int,
    isCompleted: Boolean,
    footer: String? = null
) {
    StepsCard(
        label = label,
        explanation = explanation,
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        isCompleted = isCompleted,
        footer = footer
    )
}

@Composable
private fun LinkedListIntroductionVisual(stepIndex: Int, nodeValues: List<String>) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isHeadHighlighted = stepIndex == 0 || stepIndex == 1
        val headBorderColor = if (isHeadHighlighted) OrangeAccent else CardBorderColor
        val headBgColor = if (isHeadHighlighted) Color(0xFF2A1C12) else Color(0xFF13131C)
        val headTextColor = if (isHeadHighlighted) OrangeAccent else TextSecondary

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(headBgColor)
                .border(1.dp, headBorderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "head •",
                color = headTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "──►",
            color = if (isHeadHighlighted) OrangeAccent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        nodeValues.forEachIndexed { index, value ->
            val isNodeHighlighted = when (stepIndex) {
                0 -> index == 0
                1 -> true
                2 -> index == 2
                else -> false
            }

            val nodeBg = when {
                isNodeHighlighted && stepIndex == 2 -> Color(0xFF092317)
                isNodeHighlighted -> Color(0xFF2A1C12)
                else -> Color(0xFF13131C)
            }

            val nodeBorder = when {
                isNodeHighlighted && stepIndex == 2 -> GreenAccent
                isNodeHighlighted -> OrangeAccent
                else -> CardBorderColor
            }

            Surface(
                modifier = Modifier,
                color = nodeBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(if (isNodeHighlighted) 1.5.dp else 1.dp, nodeBorder),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DATA",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = value,
                            color = if (isNodeHighlighted) TextPrimary else TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.size(6.dp))
                    Box(
                        modifier = Modifier
                            .size(1.dp, 24.dp)
                            .background(CardBorderColor)
                    )
                    Spacer(modifier = Modifier.size(6.dp))

                    val nextTextColor = when {
                        isNodeHighlighted && stepIndex == 2 -> GreenAccent
                        isNodeHighlighted -> OrangeAccent
                        else -> TextSecondary
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NEXT",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            color = nextTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val isArrowHighlighted = when (stepIndex) {
                0 -> index == 0
                1 -> index < nodeValues.size - 1
                2 -> index == nodeValues.size - 1
                else -> false
            }

            Text(
                text = "─►",
                color = if (isArrowHighlighted && stepIndex == 2) GreenAccent else if (isArrowHighlighted) OrangeAccent else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val isNullHighlighted = stepIndex == 2
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isNullHighlighted) Color(0xFF092317) else Color(0xFF13131C))
                .border(
                    1.dp,
                    if (isNullHighlighted) GreenAccent else CardBorderColor,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "null",
                color = if (isNullHighlighted) GreenAccent else TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LinkedListIntroductionDetails(stepIndex: Int, totalSteps: Int, isCompleted: Boolean) {
    val explanationText = when (stepIndex) {
        0 -> "Step 1 — Start at head: head references the first node (12). It is a pointer variable storing the memory address of node 12, not a data node itself."
        1 -> "Step 2 — Follow next: Each node contains a data value and a next reference that points to the next node in sequence (12 → 7 → 19)."
        else -> "Step 3 — Reach the end: Node 19's next reference points to null (nullptr in C++, None in Python), marking the end of the list."
    }
    LinkedListStepsCard(
        label = "Steps",
        explanation = explanationText,
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        isCompleted = isCompleted
    )
}

@Composable
internal fun LinkedListIntroductionVisualizationCard() {
    val nodeValues = listOf("12", "7", "19")
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val totalSteps = 3
    val playback = rememberVisualizerPlaybackState(stepCount = totalSteps, initialStep = 0)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted

    ArrayWindowButton(text = "Open linked list visualizer") { isExpanded = true }

    if (isExpanded) {
        VisualizerModal(
            title = "LINKED LIST INTRODUCTION",
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusText = when (stepIndex) {
                0 -> "START AT HEAD"
                1 -> "FOLLOW NEXT"
                else -> "REACH END"
            },
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            visualContent = {
                LinkedListIntroductionVisual(stepIndex = stepIndex, nodeValues = nodeValues)
            },
            detailsContent = {
                LinkedListIntroductionDetails(stepIndex = stepIndex, totalSteps = totalSteps, isCompleted = isCompleted)
            }
        )
    }
}

@Composable
private fun LinkedListTraversalVisual(
    nodeValues: List<String>,
    currentNode: Int?,
    focusedNode: Int?,
    visitedNodes: Set<Int>
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isHeadActive = currentNode == 0
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isHeadActive) Color(0xFF2A1C12) else Color(0xFF13131C))
                .border(1.dp, if (isHeadActive) OrangeAccent else CardBorderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "head •",
                color = if (isHeadActive) OrangeAccent else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "──►",
            color = if (isHeadActive) OrangeAccent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        nodeValues.forEachIndexed { index, value ->
            val isCurrent = currentNode == index
            val isFocused = focusedNode == index
            val isVisited = visitedNodes.contains(index)

            val nodeBg = when {
                isCurrent -> Color(0xFF2A1C12)
                isVisited -> Color(0xFF161F1A)
                else -> Color(0xFF13131C)
            }

            val nodeBorder = when {
                isCurrent -> OrangeAccent
                isVisited -> GreenAccent
                else -> CardBorderColor
            }

            Surface(
                modifier = Modifier,
                color = nodeBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, nodeBorder),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DATA",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = value,
                            color = if (isCurrent || isVisited) TextPrimary else TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.size(6.dp))
                    Box(
                        modifier = Modifier
                            .size(1.dp, 24.dp)
                            .background(CardBorderColor)
                    )
                    Spacer(modifier = Modifier.size(6.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NEXT",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            color = if (isCurrent) OrangeAccent else TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (index < nodeValues.lastIndex) {
                Text(
                    text = "─►",
                    color = if (index < (currentNode ?: -1)) GreenAccent else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LinkedListTraversalDetails(
    label: String,
    explanation: String,
    footer: String,
    stepIndex: Int,
    totalSteps: Int,
    isCompleted: Boolean
) {
    LinkedListStepsCard(
        label = label,
        explanation = explanation,
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        isCompleted = isCompleted,
        footer = footer
    )
}

@Composable
internal fun LinkedListTraversalSearchAccessVisualizationCard() {
    var scenario by rememberSaveable { mutableStateOf("ACCESS") } // "ACCESS", "FOUND", "MISSING"
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val nodeValues = LinkedListTrace.initialTraversalNodeValues
    val steps = remember(scenario) { LinkedListTrace.traversalSteps(scenario) }

    val totalSteps = steps.size
    val playback = rememberVisualizerPlaybackState(stepCount = totalSteps, initialStep = 0, key = scenario)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]

    ArrayWindowButton(text = "Open search & access visualizer") { isExpanded = true }

    if (isExpanded) {
        VisualizerModal(
            title = "LINKED LIST SEARCH & ACCESS",
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusText = if (isCompleted) "COMPLETE" else "STEP ${stepIndex + 1} / $totalSteps",
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            topContent = {
                LinkedListScenarioSelector(
                    options = listOf("TRAVERSE" to "Traversal", "ACCESS" to "Access index 3", "FOUND" to "Search 45", "MISSING" to "Search 99"),
                    selected = scenario,
                    onSelected = { newScenario: String ->
                        scenario = newScenario
                    }
                )
            },
            visualContent = {
                LinkedListTraversalVisual(
                    nodeValues = nodeValues,
                    currentNode = currentStep.currentNode,
                    focusedNode = currentStep.currentNode,
                    visitedNodes = currentStep.visitedNodes
                )
            },
            detailsContent = {
                LinkedListTraversalDetails(
                    label = currentStep.title,
                    explanation = currentStep.explanation,
                    footer = currentStep.result,
                    stepIndex = stepIndex,
                    totalSteps = totalSteps,
                    isCompleted = isCompleted
                )
            }
        )
    }
}

@Composable
internal fun LinkedListInsertionVisualizationCard() {
    var scenario by rememberSaveable { mutableStateOf("HEAD") } // "HEAD", "TAIL", "MIDDLE"
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val steps = remember(scenario) { LinkedListTrace.insertionSteps(scenario) }

    val totalSteps = steps.size
    val playback = rememberVisualizerPlaybackState(stepCount = totalSteps, initialStep = 0, key = scenario)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]

    ArrayWindowButton(text = "Open insertion visualizer") { isExpanded = true }

    if (isExpanded) {
        VisualizerModal(
            title = "LINKED LIST INSERTION",
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusText = if (isCompleted) "COMPLETE" else "STEP ${stepIndex + 1} / $totalSteps",
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            topContent = {
                LinkedListScenarioSelector(
                    options = listOf("HEAD" to "At Head O(1)", "TAIL" to "At Tail O(n)", "MIDDLE" to "In Middle"),
                    selected = scenario,
                    onSelected = { newScenario: String ->
                        scenario = newScenario
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize(), fitFillFraction = 0.95f, fitMaxScale = 1.1f) {
                    LinkedListOperationDiagram(
                        nodeOrder = currentStep.order,
                        links = currentStep.links,
                        headValue = currentStep.head,
                        highlightedNodes = currentStep.highlighted,
                        activeNode = currentStep.activeNode,
                        detachedNodes = currentStep.detachedNodes,
                        nodeBadges = currentStep.badges,
                        stepKey = scenario to stepIndex,
                        fitMode = true
                    )
                }
            },
            detailsContent = {
                LinkedListStepsCard(
                    label = currentStep.title,
                    explanation = currentStep.explanation,
                    stepIndex = stepIndex,
                    totalSteps = totalSteps,
                    isCompleted = isCompleted
                )
            }
        )
    }
}

@Composable
internal fun LinkedListRemovalVisualizationCard() {
    var scenario by rememberSaveable { mutableStateOf("HEAD") } // "HEAD", "TAIL", "MIDDLE"
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val steps = remember(scenario) { LinkedListTrace.removalSteps(scenario) }

    val totalSteps = steps.size
    val playback = rememberVisualizerPlaybackState(stepCount = totalSteps, initialStep = 0, key = scenario)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]

    ArrayWindowButton(text = "Open removal visualizer") { isExpanded = true }

    if (isExpanded) {
        VisualizerModal(
            title = "LINKED LIST REMOVAL",
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusText = if (isCompleted) "COMPLETE" else "STEP ${stepIndex + 1} / $totalSteps",
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            topContent = {
                LinkedListScenarioSelector(
                    options = listOf("HEAD" to "At Head O(1)", "TAIL" to "At Tail O(n)", "MIDDLE" to "In Middle"),
                    selected = scenario,
                    onSelected = { newScenario: String ->
                        scenario = newScenario
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize(), fitFillFraction = 0.95f, fitMaxScale = 1.1f) {
                    LinkedListOperationDiagram(
                        nodeOrder = currentStep.order,
                        links = currentStep.links,
                        headValue = currentStep.head,
                        highlightedNodes = currentStep.highlighted,
                        activeNode = currentStep.activeNode,
                        detachedNodes = currentStep.detachedNodes,
                        nodeBadges = currentStep.badges,
                        stepKey = scenario to stepIndex,
                        fitMode = true
                    )
                }
            },
            detailsContent = {
                LinkedListStepsCard(
                    label = currentStep.title,
                    explanation = currentStep.explanation,
                    stepIndex = stepIndex,
                    totalSteps = totalSteps,
                    isCompleted = isCompleted
                )
            }
        )
    }
}

@Composable
internal fun LinkedListPerformanceComparisonVisualizationCard() {
    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ARRAY VS LINKED LIST PERFORMANCE", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LinkedListTypeCard("Singly Linked List", "Every node stores data and a next pointer. One-directional traversal from head.", doubly = false)
                LinkedListTypeCard("Doubly Linked List", "Nodes store data plus prev and next pointers. Enables bidirectional traversal.", doubly = true)
            }

            Surface(color = Color(0xFF13131C), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderColor), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Time Complexity Summary", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LinkedListComplexityRow("Access by Index", "Array O(1)", "O(n) traversal required")
                    LinkedListComplexityRow("Insert / Delete at Head", "Array O(n) shift", "O(1) pointer rewire")
                    LinkedListComplexityRow("Insert / Delete at Tail", "Array O(1) amortized", "O(n) without tail ptr")
                }
            }
        }
    }
}

@Composable
internal fun LinkedListTypeCard(title: String, description: String, doubly: Boolean) {
    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if (doubly) Text("←", color = BlueAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(color = DarkCardBgColor, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, CardBorderColor)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (doubly) LinkedListNodeField("PREV", "•")
                        LinkedListNodeField("DATA", "12")
                        LinkedListNodeField("NEXT", "•")
                    }
                }
                Text("→", color = OrangeAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(if (doubly) "tail" else "null", color = TextSecondary, fontSize = 10.sp)
            }
            Text(description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
internal fun LinkedListNodeField(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(36.dp).height(38.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = BlueAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun LinkedListComplexityRow(operation: String, arrayCost: String, listCost: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(operation, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(arrayCost, color = BlueAccent, fontSize = 10.sp)
            Text("·", color = TextSecondary, fontSize = 10.sp)
            Text("Singly linked $listCost", color = GreenAccent, fontSize = 10.sp)
        }
    }
}

@Composable
internal fun LinkedListVisualizerHeader(title: String, stepIndex: Int, stepCount: Int, completed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(
            if (completed) "COMPLETE" else "STEP ${stepIndex + 1} / $stepCount",
            color = if (completed) GreenAccent else BlueAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun LinkedListScenarioSelector(
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier.weight(1f).clickable { onSelected(value) }
            ) {
                Box(Modifier.padding(horizontal = 4.dp, vertical = 7.dp), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
internal fun LinkedListOperationDiagram(
    nodeOrder: List<String>,
    links: Set<Pair<String, String>>,
    headValue: String,
    highlightedNodes: Set<String>,
    activeNode: String,
    visibilityTargets: List<String> = emptyList(),
    detachedNodes: List<String> = emptyList(),
    nodeBadges: Map<String, String> = emptyMap(),
    stepKey: Any,
    fitMode: Boolean = true
) {
    val scrollState = rememberScrollState()
    val diagramItems = remember(nodeOrder, detachedNodes) {
        (nodeOrder + "null" + detachedNodes).distinct()
    }
    val requesters = remember(diagramItems) {
        diagramItems.associateWith { BringIntoViewRequester() }
    }
    var skipInitialVisibilityRequest by remember { mutableStateOf(true) }

    LaunchedEffect(stepKey, activeNode, diagramItems) {
        if (skipInitialVisibilityRequest) {
            skipInitialVisibilityRequest = false
            return@LaunchedEffect
        }
        visibilityTargets.forEach { value -> requesters[value]?.bringIntoView() }
        requesters[activeNode]?.bringIntoView()
    }

    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = if (fitMode) Modifier else Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .then(if (fitMode) Modifier else Modifier.fillMaxWidth().horizontalScroll(scrollState)),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF2A1C12),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OrangeAccent)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HEAD", color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(headValue, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            nodeOrder.forEachIndexed { index, value ->
                LinkedListOperationNode(
                    value = value,
                    nextTarget = links.firstOrNull { it.first == value }?.second ?: "—",
                    badge = nodeBadges[value],
                    highlighted = value in highlightedNodes,
                    requester = requesters.getValue(value)
                )
                val nextValue = nodeOrder.getOrNull(index + 1) ?: "null"
                val isLinked = (value to nextValue) in links
                Text(
                    if (isLinked) "─►" else "···",
                    color = if (isLinked && (value in highlightedNodes || nextValue in highlightedNodes)) BlueAccent else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .bringIntoViewRequester(requesters.getValue("null"))
                    .clip(RoundedCornerShape(8.dp))
                    .background(if ("null" in highlightedNodes) Color(0xFF092317) else DarkCardBgColor)
                    .border(1.dp, if ("null" in highlightedNodes) GreenAccent else CardBorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("null", color = if ("null" in highlightedNodes) GreenAccent else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            detachedNodes.forEach { value ->
                LinkedListOperationNode(
                    value = value,
                    nextTarget = links.firstOrNull { it.first == value }?.second ?: "—",
                    badge = nodeBadges[value] ?: "DETACHED",
                    highlighted = value in highlightedNodes,
                    requester = requesters.getValue(value)
                )
            }
        }
    }
}

@Composable
internal fun LinkedListOperationNode(
    value: String,
    nextTarget: String,
    badge: String?,
    highlighted: Boolean,
    requester: BringIntoViewRequester
) {
    Column(
        modifier = Modifier.bringIntoViewRequester(requester),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(modifier = Modifier.height(15.dp), contentAlignment = Alignment.Center) {
            if (badge != null) Text(badge, color = if (badge == "REMOVED") RedAccent else OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            color = if (highlighted) Color(0xFF2A1C12) else DarkCardBgColor,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, if (highlighted) OrangeAccent else CardBorderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinkedListNodeField("DATA", value)
                Box(Modifier.width(1.dp).height(32.dp).background(CardBorderColor))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(20.dp).height(38.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("NEXT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(nextTarget, color = if (nextTarget == "—") TextSecondary else BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}