package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
internal fun GreedyCoinChangeVisualizationCard(
    title: String = "Greedy Coin Change"
) {
    val steps = GreedyCoinChangeTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open coin change visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.status) {
            CoinChangeStatus.INIT -> "GREEDY COIN CHANGE: TARGET ${step.targetAmount}"
            CoinChangeStatus.CONSIDERING -> "CONSIDERING COIN ${step.currentCoinValue}"
            CoinChangeStatus.TAKE_COIN -> "TAKING COIN ${step.currentCoinValue}"
            CoinChangeStatus.SKIP_COIN -> "SKIPPING COIN ${step.currentCoinValue} (TOO LARGE)"
            CoinChangeStatus.FINISHED -> "TARGET REACHED · ${step.selectedCoins.size} COINS"
        }

        val statusColor = when (step.status) {
            CoinChangeStatus.TAKE_COIN, CoinChangeStatus.FINISHED -> GreenAccent
            CoinChangeStatus.SKIP_COIN -> RedAccent
            CoinChangeStatus.CONSIDERING -> OrangeAccent
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
                    GreedyCoinChangeVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GreedyCoinChangeDetailsAndStepsContent(
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
private fun GreedyCoinChangeVisualizationContent(step: GreedyCoinChangeStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Target & Remaining Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text(
                        text = "TARGET AMOUNT: ${step.targetAmount}",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Denominations: [${step.denominations.joinToString()}]",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = if (step.remainingAmount == 0) Color(0xFF123820) else Color(0xFF2A1C12),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (step.remainingAmount == 0) GreenAccent else OrangeAccent)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp * scale, vertical = 4.dp * scale),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("REMAINING", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${step.remainingAmount}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Available Denomination Cards
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
                Text("DENOMINATIONS (LARGEST FIRST):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.denominations.forEachIndexed { idx, coin ->
                        val isCurrent = step.currentCoinIndex == idx
                        val isSkipped = isCurrent && step.status == CoinChangeStatus.SKIP_COIN
                        val isTaking = isCurrent && step.status == CoinChangeStatus.TAKE_COIN

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = when {
                                    isTaking -> "TAKE"
                                    isSkipped -> "SKIP"
                                    isCurrent -> "ACTIVE"
                                    else -> "PENDING"
                                },
                                color = when {
                                    isTaking -> GreenAccent
                                    isSkipped -> RedAccent
                                    isCurrent -> OrangeAccent
                                    else -> TextSecondary
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isTaking -> Color(0xFF123820)
                                    isSkipped -> Color(0xFF381215)
                                    isCurrent -> Color(0xFF2A1C12)
                                    else -> Color(0xFF181824)
                                },
                                shape = CircleShape,
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isTaking -> GreenAccent
                                        isSkipped -> RedAccent
                                        isCurrent -> OrangeAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.size(48.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$coin", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Coins Row
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SELECTED COINS SEQUENCE:", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Total: ${step.selectedCoins.size} coins", color = TextSecondary, fontSize = 10.sp)
                }

                if (step.selectedCoins.isEmpty()) {
                    Text(
                        text = "No coins selected yet.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp * scale)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        step.selectedCoins.forEach { coinVal ->
                            Surface(
                                color = Color(0xFF123820),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, GreenAccent),
                                modifier = Modifier.size(36.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$coinVal", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
private fun GreedyCoinChangeDetailsAndStepsContent(
    step: GreedyCoinChangeStep,
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
                Text("GREEDY DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.status) {
                        CoinChangeStatus.TAKE_COIN, CoinChangeStatus.FINISHED -> GreenAccent
                        CoinChangeStatus.SKIP_COIN -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Time: O(D + K)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
