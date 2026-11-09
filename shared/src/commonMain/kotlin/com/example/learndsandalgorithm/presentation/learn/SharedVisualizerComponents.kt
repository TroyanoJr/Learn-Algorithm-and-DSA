package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val VisualizerDarkBgColor = Color(0xFF111115)
internal val VisualizerDarkCardBgColor = Color(0xFF181820)
internal val VisualizerCardBorderColor = Color(0xFF262634)
internal val VisualizerOrangeAccent = Color(0xFFFA6E13)
internal val VisualizerBlueAccent = Color(0xFF38BDF8)
internal val VisualizerGreenAccent = Color(0xFF10B981)
internal val VisualizerRedAccent = Color(0xFFEF4444)
internal val VisualizerTextPrimary = Color(0xFFFFFFFF)
internal val VisualizerTextSecondary = Color(0xFF9CA3AF)

internal val LocalAutoExpandVisualizer = compositionLocalOf { false }
internal val LocalVisualizerClose = compositionLocalOf<() -> Unit> { {} }

@Composable
internal fun ArrayWindowButton(
    text: String,
    icon: String = "⛶",
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF22222E),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, VisualizerCardBorderColor),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = "$icon $text",
            color = VisualizerTextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
internal fun VisualizerPlaybackBar(
    isPlaying: Boolean,
    isCompleted: Boolean,
    canGoPrevious: Boolean,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    controlSize: Dp = 44.dp,
    spacing: Dp = 8.dp,
    cornerRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = VisualizerDarkCardBgColor,
            shape = RoundedCornerShape(cornerRadius),
            border = BorderStroke(1.dp, VisualizerCardBorderColor),
            modifier = Modifier.size(controlSize).clickable(onClick = onReset)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("↻", color = VisualizerTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Surface(
            color = VisualizerDarkCardBgColor,
            shape = RoundedCornerShape(cornerRadius),
            border = BorderStroke(1.dp, VisualizerCardBorderColor),
            modifier = Modifier.size(controlSize).clickable(enabled = canGoPrevious, onClick = onPrevious)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "‹",
                    color = if (canGoPrevious) VisualizerTextPrimary else VisualizerTextSecondary.copy(alpha = 0.3f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Surface(
            color = if (isCompleted) Color(0xFF22222E) else VisualizerOrangeAccent,
            shape = RoundedCornerShape(cornerRadius),
            modifier = Modifier.weight(1f).height(controlSize).clickable(
                enabled = !isCompleted,
                onClick = onPlayPause
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (isPlaying) "⏸" else "▶",
                        color = if (isCompleted) VisualizerTextSecondary else Color(0xFF111115),
                        fontSize = 14.sp
                    )
                    Text(
                        if (isPlaying) "Pause" else "Play",
                        color = if (isCompleted) VisualizerTextSecondary else Color(0xFF111115),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Surface(
            color = VisualizerDarkCardBgColor,
            shape = RoundedCornerShape(cornerRadius),
            border = BorderStroke(1.dp, VisualizerCardBorderColor),
            modifier = Modifier.size(controlSize).clickable(enabled = !isCompleted, onClick = onNext)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "›",
                    color = if (!isCompleted) VisualizerTextPrimary else VisualizerTextSecondary.copy(alpha = 0.3f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun VisualizerPlaybackBar(
    playback: VisualizerPlaybackState,
    controlSize: Dp = 44.dp,
    spacing: Dp = 8.dp,
    cornerRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    VisualizerPlaybackBar(
        isPlaying = playback.isPlaying,
        isCompleted = playback.isCompleted,
        canGoPrevious = playback.canGoPrevious,
        onReset = { playback.reset() },
        onPrevious = { playback.previous() },
        onPlayPause = { playback.togglePlayback() },
        onNext = { playback.next() },
        controlSize = controlSize,
        spacing = spacing,
        cornerRadius = cornerRadius,
        modifier = modifier
    )
}

@Composable
internal fun StepsCard(
    label: String,
    explanation: String,
    stepIndex: Int,
    totalSteps: Int,
    isCompleted: Boolean,
    footer: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF161622),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = VisualizerOrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "STEP ${stepIndex + 1} / $totalSteps",
                    color = if (isCompleted) VisualizerGreenAccent else VisualizerTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(explanation, color = VisualizerTextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
            if (footer != null) {
                Text(footer, color = VisualizerGreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun LinkedListStepExplanation(title: String, explanation: String, stepIndex: Int, stepCount: Int) {
    StepsCard(
        label = title,
        explanation = explanation,
        stepIndex = stepIndex,
        totalSteps = stepCount,
        isCompleted = stepIndex == stepCount - 1
    )
}
