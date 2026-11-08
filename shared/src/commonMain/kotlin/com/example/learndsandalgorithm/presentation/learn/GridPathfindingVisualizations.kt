package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GridPathfindingVisualizationCard(
    title: String = "Grid Pathfinding & Matrix Backtracking"
) {
    val steps = GridPathfindingTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open grid pathfinding visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            GridPathAction.INIT -> "GRID PATHFINDING: START (0,0) -> TARGET (${step.targetCell.r},${step.targetCell.c})"
            GridPathAction.MOVE -> "MOVE TO (${step.currentCell?.r}, ${step.currentCell?.c})"
            GridPathAction.HIT_WALL -> "HIT OBSTACLE WALL AT (${step.currentCell?.r}, ${step.currentCell?.c})"
            GridPathAction.REVISITED -> "REVISITED CELL AT (${step.currentCell?.r}, ${step.currentCell?.c}) · PRUNE"
            GridPathAction.TARGET_REACHED -> "TARGET REACHED AT (${step.targetCell.r}, ${step.targetCell.c})!"
            GridPathAction.BACKTRACK -> "BACKTRACK FROM (${step.currentCell?.r}, ${step.currentCell?.c})"
            GridPathAction.FINISHED -> "PATHFINDING COMPLETE · ${step.currentPath.size} STEPS IN PATH"
        }

        val statusColor = when (step.action) {
            GridPathAction.TARGET_REACHED, GridPathAction.FINISHED -> GreenAccent
            GridPathAction.HIT_WALL, GridPathAction.REVISITED, GridPathAction.BACKTRACK -> RedAccent
            GridPathAction.MOVE -> OrangeAccent
            else -> BlueAccent
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
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    GridPathfindingVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GridPathfindingDetailsAndStepsContent(
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
private fun GridPathfindingVisualizationContent(step: GridPathStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Grid Map Render Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text("${step.rows}x${step.cols} MATRIX GRID MAP:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                step.grid.forEachIndexed { r, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        row.forEachIndexed { c, cellVal ->
                            val cell = GridCell(r, c)
                            val isWall = cellVal == 0
                            val isTarget = r == step.targetCell.r && c == step.targetCell.c
                            val isCurrent = step.currentCell == cell
                            val isInPath = step.currentPath.contains(cell)
                            val isVisited = step.visitedCells.contains(cell)

                            Surface(
                                color = when {
                                    isCurrent && step.action == GridPathAction.HIT_WALL -> Color(0xFF381215)
                                    isCurrent -> Color(0xFF2A1C12)
                                    isTarget && (isInPath || step.action == GridPathAction.TARGET_REACHED) -> Color(0xFF123820)
                                    isInPath -> Color(0xFF123820)
                                    isVisited -> Color(0xFF122C38)
                                    isWall -> Color(0xFF22222E)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isCurrent -> OrangeAccent
                                        isInPath || isTarget -> GreenAccent
                                        isVisited -> BlueAccent
                                        isWall -> CardBorderColor
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.size(46.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = when {
                                            isCurrent -> "📍"
                                            isTarget -> "🎯"
                                            isInPath -> "🟩"
                                            isWall -> "⬛"
                                            else -> "⬜"
                                        },
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Complexity Explanation Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp * scale),
                verticalArrangement = Arrangement.spacedBy(2.dp * scale)
            ) {
                Text(
                    text = "COMPLEXITY & BOUNDS EXPLANATION:",
                    color = BlueAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "O(4^(R×C)) is a broad upper bound assuming 4 directions per step. Each cell is visited at most once per simple path, and actual exploration depends heavily on grid dimensions, walls, boundaries, and visited-cell restrictions.",
                    color = TextPrimary,
                    fontSize = 9.sp,
                    lineHeight = 13.sp
                )
            }
        }

        // Path Sequence Summary Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Path: ${step.currentPath.map { "(${it.r},${it.c})" }}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${step.currentPath.size} Steps",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GridPathfindingDetailsAndStepsContent(
    step: GridPathStep,
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
                Text("GRID PATH DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        GridPathAction.TARGET_REACHED, GridPathAction.FINISHED -> GreenAccent
                        GridPathAction.HIT_WALL, GridPathAction.REVISITED, GridPathAction.BACKTRACK -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Upper Bound: O(4^(R×C))", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
