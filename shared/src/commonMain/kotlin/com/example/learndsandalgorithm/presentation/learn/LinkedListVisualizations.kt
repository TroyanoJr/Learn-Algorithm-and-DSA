package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.learndsandalgorithm.core.platform.LandscapeOrientationEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)


/** Escala el contenido para que SIEMPRE quepa en su panel (sin solapes ni recortes) y lo centra. */
@Composable
private fun FitToPane(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier.clipToBounds()) { measurables, constraints ->
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val placeable = measurables.firstOrNull()?.measure(Constraints())
        layout(w, h) {
            if (placeable != null && placeable.width > 0 && placeable.height > 0) {
                val s = minOf(w.toFloat() / placeable.width, h.toFloat() / placeable.height, 1.35f)
                placeable.placeWithLayer((w - placeable.width) / 2, (h - placeable.height) / 2) {
                    scaleX = s
                    scaleY = s
                }
            }
        }
    }
}

/**
 * Modal común de los visualizadores de Linked List. Misma estructura que el de Stack:
 * cabecera fija (título + estado + Close), zona central weight(1f) con [visual | detalles]
 * y controles de reproducción fijos abajo.
 */
@Composable
private fun LinkedListVisualizerModal(
    title: String,
    statusText: String,
    isCompleted: Boolean,
    stepIndex: Int,
    stepCount: Int,
    isPlaying: Boolean,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    topContent: (@Composable () -> Unit)? = null,
    visual: @Composable () -> Unit,
    details: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(title.uppercase(), color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                statusText,
                                color = if (isCompleted) GreenAccent else BlueAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ArrayWindowButton(text = "Close", icon = "✕", onClick = onClose)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isLandscape) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1.3f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                topContent?.invoke()
                                FitToPane(modifier = Modifier.weight(1f).fillMaxWidth()) { visual() }
                            }
                            Column(
                                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) { details() }
                        }
                    } else {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            topContent?.invoke()
                            FitToPane(modifier = Modifier.weight(1f).fillMaxWidth()) { visual() }
                            Column(
                                modifier = Modifier.heightIn(max = 140.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) { details() }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinkedListPlaybackControls(
                        stepIndex = stepIndex,
                        stepCount = stepCount,
                        isPlaying = isPlaying,
                        onReset = onReset,
                        onPrevious = onPrevious,
                        onTogglePlayback = onTogglePlayback,
                        onNext = onNext
                    )
                }
            }
        }
    }
}

/** Tarjeta "Steps" con el mismo diseño que el panel derecho del modal de Stack. */
@Composable
private fun LinkedListStepsCard(
    label: String,
    explanation: String,
    stepIndex: Int,
    totalSteps: Int,
    isCompleted: Boolean,
    footer: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (footer != null) {
            Text(footer, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("STEP ${stepIndex + 1} / $totalSteps", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    explanation,
                    color = if (isCompleted) GreenAccent else TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
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
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val totalSteps = 3
    val isCompleted = stepIndex >= totalSteps - 1

    LaunchedEffect(isPlaying, stepIndex) {
        if (isPlaying && stepIndex < totalSteps - 1) {
            delay(1000L)
            if (stepIndex < totalSteps - 1) stepIndex++ else isPlaying = false
        } else if (stepIndex >= totalSteps - 1) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = "Open linked list visualizer") { isExpanded = true }

    if (isExpanded) {
        LinkedListVisualizerModal(
            title = "LINKED LIST INTRODUCTION",
            statusText = when (stepIndex) {
                0 -> "START AT HEAD"
                1 -> "FOLLOW NEXT"
                else -> "REACH END"
            },
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { isPlaying = false; stepIndex = 0 },
            onPrevious = { isPlaying = false; if (stepIndex > 0) stepIndex-- },
            onTogglePlayback = { if (!isCompleted) isPlaying = !isPlaying },
            onNext = { isPlaying = false; if (stepIndex < totalSteps - 1) stepIndex++ },
            onClose = { isPlaying = false; isExpanded = false },
            visual = {
                LinkedListIntroductionVisual(stepIndex = stepIndex, nodeValues = nodeValues)
            },
            details = {
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
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val nodeValues = listOf("12", "7", "19", "45", "33")

    class Step(
        val currentNode: Int,
        val visitedNodes: Set<Int>,
        val title: String,
        val explanation: String,
        val result: String
    )

    val accessSteps = remember {
        listOf(
            Step(0, setOf(0), "Access index 3: start at head",
                "Goal: get the node at index 3. A linked list has no direct indexing, so we start at head (index 0) and follow next pointers.",
                "Hops: 0"),
            Step(1, setOf(0, 1), "Follow next to index 1",
                "Follow the next pointer from 12 to reach 7 at index 1.",
                "Hops: 1"),
            Step(2, setOf(0, 1, 2), "Follow next to index 2",
                "Follow the next pointer from 7 to reach 19 at index 2.",
                "Hops: 2"),
            Step(3, setOf(0, 1, 2, 3), "Index 3 reached",
                "Follow next from 19 to reach 45 at index 3. This is the requested index, so return 45. Access costs O(n): we had to walk the list.",
                "Result: 45 at index 3 (3 hops)")
        )
    }

    val foundSteps = remember {
        listOf(
            Step(0, setOf(0), "Search 45: compare head",
                "Compare the head value 12 with the target 45. They are not equal, so move to the next node.",
                "Comparisons: 1"),
            Step(1, setOf(0, 1), "Compare 7",
                "7 is not equal to 45. Follow next.",
                "Comparisons: 2"),
            Step(2, setOf(0, 1, 2), "Compare 19",
                "19 is not equal to 45. Follow next.",
                "Comparisons: 3"),
            Step(3, setOf(0, 1, 2, 3), "Match found",
                "45 equals the target, so the search stops and returns this node (index 3). Search is O(n) in the worst case.",
                "Found: 45 at index 3 (4 comparisons)")
        )
    }

    val missingSteps = remember {
        listOf(
            Step(0, setOf(0), "Search 99: compare head",
                "Compare the head value 12 with the target 99. Not equal, so move on.",
                "Comparisons: 1"),
            Step(1, setOf(0, 1), "Compare 7",
                "7 is not equal to 99. Follow next.",
                "Comparisons: 2"),
            Step(2, setOf(0, 1, 2), "Compare 19",
                "19 is not equal to 99. Follow next.",
                "Comparisons: 3"),
            Step(3, setOf(0, 1, 2, 3), "Compare 45",
                "45 is not equal to 99. Follow next.",
                "Comparisons: 4"),
            Step(4, setOf(0, 1, 2, 3, 4), "End of list: not found",
                "33 is not equal to 99 and its next pointer is null, so 99 is not in the list. Every node was checked: O(n).",
                "Not found (5 comparisons)")
        )
    }

    val steps = when (scenario) {
        "ACCESS" -> accessSteps
        "FOUND" -> foundSteps
        else -> missingSteps
    }

    val totalSteps = steps.size
    val isCompleted = stepIndex >= totalSteps - 1
    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]

    LaunchedEffect(isPlaying, stepIndex) {
        if (isPlaying && stepIndex < totalSteps - 1) {
            delay(1000L)
            if (stepIndex < totalSteps - 1) stepIndex++ else isPlaying = false
        } else if (stepIndex >= totalSteps - 1) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = "Open search & access visualizer") { isExpanded = true }

    if (isExpanded) {
        LinkedListVisualizerModal(
            title = "LINKED LIST SEARCH & ACCESS",
            statusText = if (isCompleted) "COMPLETE" else "STEP ${stepIndex + 1} / $totalSteps",
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { isPlaying = false; stepIndex = 0 },
            onPrevious = { isPlaying = false; if (stepIndex > 0) stepIndex-- },
            onTogglePlayback = { if (!isCompleted) isPlaying = !isPlaying },
            onNext = { isPlaying = false; if (stepIndex < totalSteps - 1) stepIndex++ },
            onClose = { isPlaying = false; isExpanded = false },
            topContent = {
                LinkedListScenarioSelector(
                    options = listOf("ACCESS" to "Access index 3", "FOUND" to "Search 45", "MISSING" to "Search 99"),
                    selected = scenario,
                    onSelected = { newScenario: String ->
                        isPlaying = false
                        scenario = newScenario
                        stepIndex = 0
                    }
                )
            },
            visual = {
                LinkedListTraversalVisual(
                    nodeValues = nodeValues,
                    currentNode = currentStep.currentNode,
                    focusedNode = currentStep.currentNode,
                    visitedNodes = currentStep.visitedNodes
                )
            },
            details = {
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
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    class Step(
        val title: String,
        val explanation: String,
        val order: List<String>,
        val links: Set<Pair<String, String>>,
        val head: String,
        val highlighted: Set<String>,
        val activeNode: String,
        val badges: Map<String, String>
    )

    val headSteps = remember {
        listOf(
            Step(
                "1. Create New Node (99)",
                "Allocate a new node with data 99. Its next pointer starts as unassigned.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19"),
                "12",
                setOf("99"),
                "99",
                mapOf("99" to "NEW")
            ),
            Step(
                "2. Point New Node to Head",
                "Assign new node's next pointer to reference current head (node 12).",
                listOf("99", "12", "7", "19"),
                setOf("99" to "12", "12" to "7", "7" to "19"),
                "12",
                setOf("99", "12"),
                "99",
                mapOf("99" to "NEW")
            ),
            Step(
                "3. Update Head Pointer",
                "Redirect head pointer to reference the new node (99). Insertion at head is O(1).",
                listOf("99", "12", "7", "19"),
                setOf("99" to "12", "12" to "7", "7" to "19"),
                "99",
                setOf("99"),
                "99",
                emptyMap()
            )
        )
    }

    val tailSteps = remember {
        listOf(
            Step(
                "1. Create New Node (55)",
                "Allocate a new node with data 55. Next pointer is null.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19"),
                "12",
                setOf("55"),
                "55",
                mapOf("55" to "NEW")
            ),
            Step(
                "2. Traverse to Tail",
                "Traverse from head through 12 and 7 until reaching node 19 whose next is null.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19"),
                "12",
                setOf("19"),
                "19",
                emptyMap()
            ),
            Step(
                "3. Link Tail to New Node",
                "Update node 19's next pointer from null to reference the new node (55). O(n) traversal required unless tail pointer is maintained.",
                listOf("12", "7", "19", "55"),
                setOf("12" to "7", "7" to "19", "19" to "55"),
                "12",
                setOf("19", "55"),
                "55",
                emptyMap()
            )
        )
    }

    val middleSteps = remember {
        listOf(
            Step(
                "1. Create New Node (42)",
                "Allocate a new node with data 42 to insert between 7 and 19.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19"),
                "12",
                setOf("42"),
                "42",
                mapOf("42" to "NEW")
            ),
            Step(
                "2. Point New Node to Successor",
                "Set new node's next pointer to node 7's current successor (node 19).",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "42" to "19"),
                "12",
                setOf("7", "42"),
                "42",
                mapOf("42" to "NEW")
            ),
            Step(
                "3. Rewire Predecessor Next",
                "Update node 7's next pointer to reference the new node (42). Order becomes 7 → 42 → 19.",
                listOf("12", "7", "42", "19"),
                setOf("12" to "7", "7" to "42", "42" to "19"),
                "12",
                setOf("7", "42"),
                "42",
                emptyMap()
            )
        )
    }

    val steps = when (scenario) {
        "HEAD" -> headSteps
        "TAIL" -> tailSteps
        else -> middleSteps
    }

    val totalSteps = steps.size
    val isCompleted = stepIndex >= totalSteps - 1
    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]


    LaunchedEffect(isPlaying, stepIndex) {
        if (isPlaying && stepIndex < totalSteps - 1) {
            delay(1000L)
            if (stepIndex < totalSteps - 1) stepIndex++ else isPlaying = false
        } else if (stepIndex >= totalSteps - 1) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = "Open insertion visualizer") { isExpanded = true }

    if (isExpanded) {
        LinkedListVisualizerModal(
            title = "LINKED LIST INSERTION",
            statusText = if (isCompleted) "COMPLETE" else "STEP ${stepIndex + 1} / $totalSteps",
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { isPlaying = false; stepIndex = 0 },
            onPrevious = { isPlaying = false; if (stepIndex > 0) stepIndex-- },
            onTogglePlayback = { if (!isCompleted) isPlaying = !isPlaying },
            onNext = { isPlaying = false; if (stepIndex < totalSteps - 1) stepIndex++ },
            onClose = { isPlaying = false; isExpanded = false },
            topContent = {
                LinkedListScenarioSelector(
                    options = listOf("HEAD" to "At Head O(1)", "TAIL" to "At Tail O(n)", "MIDDLE" to "In Middle"),
                    selected = scenario,
                    onSelected = { newScenario: String ->
                        isPlaying = false
                        scenario = newScenario
                        stepIndex = 0
                    }
                )
            },
            visual = {
                LinkedListOperationDiagram(
                    nodeOrder = currentStep.order,
                    links = currentStep.links,
                    headValue = currentStep.head,
                    highlightedNodes = currentStep.highlighted,
                    activeNode = currentStep.activeNode,
                    nodeBadges = currentStep.badges,
                    stepKey = scenario to stepIndex,
                    fitMode = true
                )
            },
            details = {
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
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    class Step(
        val title: String,
        val explanation: String,
        val order: List<String>,
        val links: Set<Pair<String, String>>,
        val head: String,
        val highlighted: Set<String>,
        val activeNode: String,
        val detachedNodes: List<String>,
        val badges: Map<String, String>
    )

    val headSteps = remember {
        listOf(
            Step(
                "1. Identify Head Node",
                "Locate the current head node (12) to be removed.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19"),
                "12",
                setOf("12"),
                "12",
                emptyList(),
                mapOf("12" to "REMOVE")
            ),
            Step(
                "2. Advance Head Pointer",
                "Redirect head pointer to node 12's successor (node 7).",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19"),
                "7",
                setOf("12", "7"),
                "7",
                emptyList(),
                mapOf("12" to "REMOVE")
            ),
            Step(
                "3. Detach Old Head",
                "Node 12 is now unreferenced and garbage-collected. Removal at head is O(1).",
                listOf("7", "19"),
                setOf("7" to "19"),
                "7",
                setOf("12"),
                "7",
                listOf("12"),
                mapOf("12" to "REMOVED")
            )
        )
    }

    val tailSteps = remember {
        listOf(
            Step(
                "1. Traverse to Pre-Tail",
                "Traverse to node 7, which precedes the tail node (19).",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19"),
                "12",
                setOf("7", "19"),
                "7",
                emptyList(),
                mapOf("19" to "REMOVE")
            ),
            Step(
                "2. Unlink Tail",
                "Update node 7's next pointer to null, cutting off node 19.",
                listOf("12", "7", "19"),
                setOf("12" to "7"),
                "12",
                setOf("7", "19"),
                "7",
                emptyList(),
                mapOf("19" to "REMOVE")
            ),
            Step(
                "3. Detach Tail Node",
                "Node 19 is detached. O(n) traversal needed to find pre-tail in singly linked list.",
                listOf("12", "7"),
                setOf("12" to "7"),
                "12",
                setOf("19"),
                "7",
                listOf("19"),
                mapOf("19" to "REMOVED")
            )
        )
    }

    val middleSteps = remember {
        listOf(
            Step(
                "1. Locate Predecessor",
                "Traverse to node 7, which precedes target node 19.",
                listOf("12", "7", "19", "45"),
                setOf("12" to "7", "7" to "19", "19" to "45"),
                "12",
                setOf("7", "19"),
                "7",
                emptyList(),
                mapOf("19" to "REMOVE")
            ),
            Step(
                "2. Bypass Target Node",
                "Rewire node 7's next pointer to point directly to node 45 (bypassing 19).",
                listOf("12", "7", "19", "45"),
                setOf("12" to "7", "7" to "45", "19" to "45"),
                "12",
                setOf("7", "19", "45"),
                "19",
                emptyList(),
                mapOf("19" to "REMOVE")
            ),
            Step(
                "3. Detach Target Node",
                "Node 19 is fully unlinked and detached. Deletion in middle is O(n) due to search.",
                listOf("12", "7", "45"),
                setOf("12" to "7", "7" to "45"),
                "12",
                setOf("19"),
                "45",
                listOf("19"),
                mapOf("19" to "REMOVED")
            )
        )
    }

    val steps = when (scenario) {
        "HEAD" -> headSteps
        "TAIL" -> tailSteps
        else -> middleSteps
    }

    val totalSteps = steps.size
    val isCompleted = stepIndex >= totalSteps - 1
    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]


    LaunchedEffect(isPlaying, stepIndex) {
        if (isPlaying && stepIndex < totalSteps - 1) {
            delay(1000L)
            if (stepIndex < totalSteps - 1) stepIndex++ else isPlaying = false
        } else if (stepIndex >= totalSteps - 1) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = "Open removal visualizer") { isExpanded = true }

    if (isExpanded) {
        LinkedListVisualizerModal(
            title = "LINKED LIST REMOVAL",
            statusText = if (isCompleted) "COMPLETE" else "STEP ${stepIndex + 1} / $totalSteps",
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            onReset = { isPlaying = false; stepIndex = 0 },
            onPrevious = { isPlaying = false; if (stepIndex > 0) stepIndex-- },
            onTogglePlayback = { if (!isCompleted) isPlaying = !isPlaying },
            onNext = { isPlaying = false; if (stepIndex < totalSteps - 1) stepIndex++ },
            onClose = { isPlaying = false; isExpanded = false },
            topContent = {
                LinkedListScenarioSelector(
                    options = listOf("HEAD" to "At Head O(1)", "TAIL" to "At Tail O(n)", "MIDDLE" to "In Middle"),
                    selected = scenario,
                    onSelected = { newScenario: String ->
                        isPlaying = false
                        scenario = newScenario
                        stepIndex = 0
                    }
                )
            },
            visual = {
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
            },
            details = {
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Text(value, color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    fitMode: Boolean = false
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
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .then(if (fitMode) Modifier else Modifier.fillMaxWidth().horizontalScroll(scrollState)),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                    fontWeight = FontWeight.Bold
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
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(modifier = Modifier.height(13.dp), contentAlignment = Alignment.Center) {
            if (badge != null) Text(badge, color = if (badge == "REMOVED") RedAccent else OrangeAccent, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            color = if (highlighted) Color(0xFF2A1C12) else DarkCardBgColor,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, if (highlighted) OrangeAccent else CardBorderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinkedListNodeField("DATA", value)
                Box(Modifier.width(1.dp).height(22.dp).background(CardBorderColor))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NEXT", color = TextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(nextTarget, color = if (nextTarget == "—") TextSecondary else BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}