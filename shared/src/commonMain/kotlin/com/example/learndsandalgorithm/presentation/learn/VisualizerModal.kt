package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.learndsandalgorithm.core.platform.LandscapeOrientationEffect

/**
 * Modos de maquetado explícitos para el contenedor compartido de visualizadores.
 */
internal enum class VisualizerLayoutMode {
    /**
     * Disposición en 2 columnas (Landscape): Visualización a la izquierda, detalles a la derecha.
     * Utilizado para estructuras compactas o verticales como Stack, Queue y Linked List.
     */
    SPLIT_VIEW,

    /**
     * Disposición a ancho completo (Landscape): La visualización ocupa el 100% del ancho horizontal disponible,
     * situando el panel de pasos/explicación inmediatamente debajo.
     * Utilizado para arreglos horizontales extensos como Searching, Sorting y Arrays.
     */
    FULL_WIDTH
}

/**
 * Escala el contenido gráfico para que SIEMPRE quepa en el panel disponible
 * de forma proporcional (sin recortes ni solapes) y lo centra.
 */
@Composable
internal fun FitToPane(
    modifier: Modifier = Modifier,
    maxScale: Float = 1.25f,
    unboundedWidth: Boolean = false,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier.clipToBounds()) { measurables, constraints ->
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val childConstraints = Constraints(
            minWidth = 0,
            maxWidth = w,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        ).let { base ->
            // Only for content WITHOUT scroll containers or weights: measure at natural width
            // so that horizontal overflow is scaled down instead of being squeezed or cut.
            if (unboundedWidth) base.copy(maxWidth = Constraints.Infinity) else base
        }
        val placeable = measurables.firstOrNull()?.measure(childConstraints)
        val pWidth = placeable?.width ?: 0
        val pHeight = placeable?.height ?: 0
        val scaleX = if (pWidth > 0) w.toFloat() / pWidth else 1f
        val scaleY = if (pHeight > 0) h.toFloat() / pHeight else 1f
        val s = minOf(scaleX, scaleY, maxScale)
        layout(w, h) {
            if (placeable != null && placeable.width > 0 && placeable.height > 0) {
                placeable.placeWithLayer((w - placeable.width) / 2, (h - placeable.height) / 2) {
                    this.scaleX = s
                    this.scaleY = s
                }
            }
        }
    }
}

/**
 * Modal compartido universal para las vistas ampliadas de visualizadores.
 * Proporciona el marco modal, la cabecera, la distribución responsiva (Landscape/Portrait)
 * y la barra inferior de controles de reproducción.
 */
@Composable
internal fun VisualizerModal(
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
    layoutMode: VisualizerLayoutMode = VisualizerLayoutMode.SPLIT_VIEW,
    fitUnboundedWidth: Boolean = false,
    statusColor: Color = if (isCompleted) VisualizerGreenAccent else VisualizerBlueAccent,
    topContent: (@Composable () -> Unit)? = null,
    visualContent: @Composable () -> Unit,
    detailsContent: @Composable () -> Unit
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
                color = VisualizerDarkCardBgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VisualizerCardBorderColor),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp * scale),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = title.uppercase(),
                                color = VisualizerOrangeAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ArrayWindowButton(text = "Close", icon = "✕", onClick = onClose)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Central Area
                    if (isLandscape) {
                        when (layoutMode) {
                            VisualizerLayoutMode.SPLIT_VIEW -> {
                                Row(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Region: Visual Content
                                    Column(
                                        modifier = Modifier.weight(1.2f).fillMaxHeight(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        topContent?.invoke()
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            visualContent()
                                        }
                                    }
                                    // Right Region: Explanation / Steps / Result Details
                                    Column(
                                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        detailsContent()
                                    }
                                }
                            }
                            VisualizerLayoutMode.FULL_WIDTH -> {
                                Column(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    topContent?.invoke()
                                    Box(
                                        modifier = Modifier.weight(1.2f).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        visualContent()
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        detailsContent()
                                    }
                                }
                            }
                        }
                    } else {
                        // Portrait Layout
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            topContent?.invoke()
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                visualContent()
                            }
                            Column(
                                modifier = Modifier.heightIn(max = 140.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                detailsContent()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Playback Controls Row
                    VisualizerPlaybackBar(
                        isPlaying = isPlaying,
                        isCompleted = isCompleted,
                        canGoPrevious = stepIndex > 0,
                        onReset = onReset,
                        onPrevious = onPrevious,
                        onPlayPause = onTogglePlayback,
                        onNext = onNext
                    )
                }
            }
        }
    }
}