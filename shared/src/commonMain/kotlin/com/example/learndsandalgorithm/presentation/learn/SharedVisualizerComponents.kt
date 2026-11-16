package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.semantics.contentDescription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
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

private enum class PlaybackGlyph { Replay, SkipPrevious, PlayArrow, Pause, SkipNext }

@Composable
private fun PlaybackIcon(glyph: PlaybackGlyph, description: String, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = description }) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        fun point(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(x * sx, y * sy)
        fun filled(vararg points: Pair<Float, Float>) {
            val path = Path().apply {
                moveTo(point(points.first().first, points.first().second).x, point(points.first().first, points.first().second).y)
                points.drop(1).forEach { (x, y) -> lineTo(point(x, y).x, point(x, y).y) }
                close()
            }
            drawPath(path, tint)
        }
        when (glyph) {
            PlaybackGlyph.Replay -> {
                val r = size.minDimension * 0.39f
                drawArc(tint, 45f, 300f, false, topLeft = point(3f, 3f), size = androidx.compose.ui.geometry.Size(r * 2, r * 2), style = Stroke(width = size.minDimension * 0.1f))
                filled(17f to 2f, 22f to 3f, 21f to 8f)
            }
            PlaybackGlyph.SkipPrevious -> {
                filled(6f to 5f, 6f to 19f, 2f to 19f, 2f to 5f)
                filled(20f to 5f, 20f to 19f, 8f to 12f)
            }
            PlaybackGlyph.PlayArrow -> filled(6f to 3f, 21f to 12f, 6f to 21f)
            PlaybackGlyph.Pause -> {
                filled(5f to 4f, 10f to 4f, 10f to 20f, 5f to 20f)
                filled(14f to 4f, 19f to 4f, 19f to 20f, 14f to 20f)
            }
            PlaybackGlyph.SkipNext -> {
                filled(4f to 5f, 16f to 12f, 4f to 19f)
                filled(18f to 5f, 22f to 5f, 22f to 19f, 18f to 19f)
            }
        }
    }
}

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
                PlaybackIcon(PlaybackGlyph.Replay, "Reset playback", VisualizerTextPrimary, Modifier.size(20.dp))
            }
        }
        Surface(
            color = VisualizerDarkCardBgColor,
            shape = RoundedCornerShape(cornerRadius),
            border = BorderStroke(1.dp, VisualizerCardBorderColor),
            modifier = Modifier.size(controlSize).clickable(enabled = canGoPrevious, onClick = onPrevious)
        ) {
            Box(contentAlignment = Alignment.Center) {
                PlaybackIcon(PlaybackGlyph.SkipPrevious, "Previous step", if (canGoPrevious) VisualizerTextPrimary else VisualizerTextSecondary.copy(alpha = 0.3f), Modifier.size(20.dp))
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
                    PlaybackIcon(if (isPlaying) PlaybackGlyph.Pause else PlaybackGlyph.PlayArrow, if (isPlaying) "Pause playback" else "Start playback", if (isCompleted) VisualizerTextSecondary else Color(0xFF111115), Modifier.size(20.dp))
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
                PlaybackIcon(PlaybackGlyph.SkipNext, "Next step", if (!isCompleted) VisualizerTextPrimary else VisualizerTextSecondary.copy(alpha = 0.3f), Modifier.size(20.dp))
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
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF161622),
        shape = RoundedCornerShape(if (compact) 8.dp else 12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 6.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)
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
internal fun GraphStepsCard(
    label: String,
    explanation: String,
    allExplanations: List<String>,
    stepIndex: Int,
    totalSteps: Int,
    isCompleted: Boolean
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val textMeasurer = rememberTextMeasurer()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val explanationWidthPx = with(density) { (maxWidth - 16.dp).roundToPx().coerceAtLeast(1) }
        val headerWidthPx = with(density) { (maxWidth / 2 - 8.dp).roundToPx().coerceAtLeast(1) }
        val measuredCardHeight = remember(allExplanations, explanationWidthPx, headerWidthPx, density.density, density.fontScale) {
            val explanationStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
            val headerStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
            val stepStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
            val explanationHeight = allExplanations.maxOfOrNull { text ->
                textMeasurer.measure(
                    text = androidx.compose.ui.text.AnnotatedString(text),
                    style = explanationStyle,
                    constraints = Constraints(maxWidth = explanationWidthPx)
                ).size.height
            } ?: 0
            val headerHeight = maxOf(
                textMeasurer.measure(androidx.compose.ui.text.AnnotatedString(label), style = headerStyle, constraints = Constraints(maxWidth = headerWidthPx)).size.height,
                textMeasurer.measure(androidx.compose.ui.text.AnnotatedString("STEP ${totalSteps} / $totalSteps"), style = stepStyle, constraints = Constraints(maxWidth = headerWidthPx)).size.height
            )
            with(density) { (explanationHeight + headerHeight).toDp() } + 18.dp
        }
        StepsCard(
            label = label,
            explanation = explanation,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isCompleted = isCompleted,
            compact = true,
            modifier = Modifier.heightIn(min = measuredCardHeight)
        )
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
