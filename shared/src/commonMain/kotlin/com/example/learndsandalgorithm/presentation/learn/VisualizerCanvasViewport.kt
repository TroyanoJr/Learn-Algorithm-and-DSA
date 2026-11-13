package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize

/**
 * Modes of presentation and interaction for [VisualizerCanvasViewport].
 */
internal enum class VisualizerCanvasMode {
    /**
     * Fits content automatically inside available viewport bounds without zoom/pan gestures.
     */
    AUTO_FIT,

    /**
     * Unconstrained horizontal scrolling with fixed 1:1 scale.
     */
    SCROLLABLE_HORIZONTAL,

    /**
     * Unconstrained vertical scrolling with fixed 1:1 scale.
     */
    SCROLLABLE_VERTICAL,

    /**
     * Unconstrained 2D scrolling (both X and Y directions) with fixed 1:1 scale.
     */
    SCROLLABLE_BOTH,

    /**
     * Full 2D panning and centroid-based pinch-to-zoom over an unconstrained visual canvas.
     */
    ZOOMABLE_PAN,

    /**
     * Initial auto-fit scale with user pinch-to-zoom and pan overrides.
     */
    HYBRID
}

/**
 * State object holding scale, translation offsets, and viewport/content dimensions.
 *
 * Coordinate Convention:
 * - Content is measured at natural unconstrained dimensions (cW, cH).
 * - Viewport bounds are (vW, vH).
 * - Initial fit scale s = minOf(vW / cW, vH / cH, 1.0f).
 * - Initial placement:
 *   - x = (cW * s <= vW) ? (vW - cW * s) / 2 : 0
 *   - y = (cH * s <= vH) ? (vH - cH * s) / 2 : 0
 * - Compose placeWithLayer scales around child center (cW / 2, cH / 2).
 * - Scale factor S shifts origin to (initialX - (cW * S - cW) / 2).
 * - Translation offsets (translationX, translationY) are applied in viewport space
 *   and clamped so scaled content edges never leave empty gaps when content > viewport.
 */
internal class VisualizerCanvasState(
    val initialScale: Float = 1.0f,
    val minScale: Float = 0.5f,
    val maxScale: Float = 3.5f
) {
    var scale by mutableStateOf(initialScale)
        private set

    var translationX by mutableStateOf(0f)
        private set

    var translationY by mutableStateOf(0f)
        private set

    private var _viewportSize by mutableStateOf(IntSize.Zero)
    var viewportSize: IntSize
        get() = _viewportSize
        internal set(value) {
            if (_viewportSize != value) {
                _viewportSize = value
                recalculateInitialFitIfNeeded()
            }
        }

    private var _contentSize by mutableStateOf(IntSize.Zero)
    var contentSize: IntSize
        get() = _contentSize
        internal set(value) {
            if (_contentSize != value) {
                _contentSize = value
                recalculateInitialFitIfNeeded()
            }
        }

    var hasUserInteracted by mutableStateOf(false)
        internal set

    var autoFitScale by mutableStateOf(initialScale)
        private set

    private var hasCalculatedInitialFit by mutableStateOf(false)

    fun updateViewportSize(size: IntSize) {
        viewportSize = size
    }

    fun updateContentSize(size: IntSize) {
        contentSize = size
    }

    private fun recalculateInitialFitIfNeeded() {
        if (viewportSize.width > 0 && viewportSize.height > 0 && contentSize.width > 0 && contentSize.height > 0) {
            if (!hasCalculatedInitialFit) {
                val fitScale = calculateFitScale().coerceAtMost(maxScale)
                autoFitScale = fitScale
                scale = fitScale
                hasCalculatedInitialFit = true
                applyClampedTranslation(0f, 0f)
            } else if (!hasUserInteracted) {
                // Recalculate automatic fit when dimensions change before manual interaction
                val fitScale = calculateFitScale().coerceAtMost(maxScale)
                autoFitScale = fitScale
                scale = fitScale
                applyClampedTranslation(0f, 0f)
            } else {
                applyClampedTranslation(translationX, translationY)
            }
        } else {
            applyClampedTranslation(translationX, translationY)
        }
    }

    fun calculateFitScale(): Float {
        val cW = contentSize.width.toFloat()
        val cH = contentSize.height.toFloat()
        val vW = viewportSize.width.toFloat()
        val vH = viewportSize.height.toFloat()
        if (cW <= 0f || cH <= 0f || vW <= 0f || vH <= 0f) return initialScale
        val fitX = vW / cW
        val fitY = vH / cH
        return minOf(fitX, fitY, 1.0f)
    }

    fun reset() {
        hasUserInteracted = false
        val fitScale = calculateFitScale().coerceAtMost(maxScale)
        autoFitScale = fitScale
        scale = fitScale
        translationX = 0f
        translationY = 0f
    }

    private val initialX: Float
        get() {
            val cW = contentSize.width.toFloat()
            val vW = viewportSize.width.toFloat()
            val scaledW = cW * scale
            return if (scaledW <= vW) (vW - cW) / 2f else 0f
        }

    private val initialY: Float
        get() {
            val cH = contentSize.height.toFloat()
            val vH = viewportSize.height.toFloat()
            val scaledH = cH * scale
            return if (scaledH <= vH) (vH - cH) / 2f else 0f
        }

    /**
     * Transforms scale around a gesture centroid and applies pan displacement,
     * keeping the content point under the centroid stationary and clamping within bounds.
     */
    fun onTransform(
        zoomFactor: Float,
        panChange: Offset,
        centroid: Offset,
        allowZoom: Boolean = true,
        allowPanX: Boolean = true,
        allowPanY: Boolean = true
    ) {
        if (viewportSize.width <= 0 || viewportSize.height <= 0 || contentSize.width <= 0 || contentSize.height <= 0) {
            return
        }

        hasUserInteracted = true
        val oldScale = scale
        val effectiveMinScale = minOf(minScale, autoFitScale)
        val newScale = if (allowZoom) (oldScale * zoomFactor).coerceIn(effectiveMinScale, maxScale) else oldScale
        val effectiveZoom = if (oldScale > 0f) newScale / oldScale else 1f

        val cW = contentSize.width.toFloat()
        val cH = contentSize.height.toFloat()

        val shiftWOld = (cW * oldScale - cW) / 2f
        val shiftHOld = (cH * oldScale - cH) / 2f

        val currentOriginX = initialX - shiftWOld + translationX
        val currentOriginY = initialY - shiftHOld + translationY

        // Target origin after scaling around gesture centroid
        val targetOriginX = centroid.x - effectiveZoom * (centroid.x - currentOriginX) + (if (allowPanX) panChange.x else 0f)
        val targetOriginY = centroid.y - effectiveZoom * (centroid.y - currentOriginY) + (if (allowPanY) panChange.y else 0f)

        val shiftWNew = (cW * newScale - cW) / 2f
        val shiftHNew = (cH * newScale - cH) / 2f

        val targetTx = targetOriginX - initialX + shiftWNew
        val targetTy = targetOriginY - initialY + shiftHNew

        scale = newScale
        applyClampedTranslation(targetTx, targetTy)
    }

    /**
     * Clamps translation offsets so content cannot be panned entirely out of the viewport.
     */
    fun applyClampedTranslation(rawTx: Float, rawTy: Float) {
        if (viewportSize.width <= 0 || viewportSize.height <= 0 || contentSize.width <= 0 || contentSize.height <= 0) {
            translationX = 0f
            translationY = 0f
            return
        }

        val vW = viewportSize.width.toFloat()
        val vH = viewportSize.height.toFloat()
        val cW = contentSize.width.toFloat()
        val cH = contentSize.height.toFloat()

        val scaledW = cW * scale
        val scaledH = cH * scale

        val shiftW = (scaledW - cW) / 2f
        val shiftH = (scaledH - cH) / 2f

        val xRight0 = initialX + cW + shiftW
        val yBottom0 = initialY + cH + shiftH

        val clampedTx = if (scaledW > vW) {
            val maxTx = shiftW - initialX
            val minTx = vW - xRight0
            rawTx.coerceIn(minTx, maxTx)
        } else {
            0f
        }

        val clampedTy = if (scaledH > vH) {
            val maxTy = shiftH - initialY
            val minTy = vH - yBottom0
            rawTy.coerceIn(minTy, maxTy)
        } else {
            0f
        }

        translationX = clampedTx
        translationY = clampedTy
    }
}

@Composable
internal fun rememberVisualizerCanvasState(
    initialScale: Float = 1.0f,
    minScale: Float = 0.5f,
    maxScale: Float = 3.5f,
    key: Any? = Unit
): VisualizerCanvasState {
    return remember(key) {
        VisualizerCanvasState(
            initialScale = initialScale,
            minScale = minScale,
            maxScale = maxScale
        )
    }
}

/**
 * Interactive canvas viewport providing natural size content measurement,
 * centroid-based pinch-to-zoom, 2D panning, and double-tap reset.
 */
@Composable
internal fun VisualizerCanvasViewport(
    modifier: Modifier = Modifier,
    mode: VisualizerCanvasMode = VisualizerCanvasMode.ZOOMABLE_PAN,
    state: VisualizerCanvasState = rememberVisualizerCanvasState(),
    content: @Composable () -> Unit
) {
    val allowZoom = mode == VisualizerCanvasMode.ZOOMABLE_PAN || mode == VisualizerCanvasMode.HYBRID
    val allowPanX = mode == VisualizerCanvasMode.ZOOMABLE_PAN || mode == VisualizerCanvasMode.HYBRID ||
        mode == VisualizerCanvasMode.SCROLLABLE_HORIZONTAL || mode == VisualizerCanvasMode.SCROLLABLE_BOTH
    val allowPanY = mode == VisualizerCanvasMode.ZOOMABLE_PAN || mode == VisualizerCanvasMode.HYBRID ||
        mode == VisualizerCanvasMode.SCROLLABLE_VERTICAL || mode == VisualizerCanvasMode.SCROLLABLE_BOTH

    val isGestureEnabled = allowZoom || allowPanX || allowPanY

    val gestureModifier = if (isGestureEnabled) {
        Modifier
            .pointerInput(state) {
                detectTapGestures(
                    onDoubleTap = {
                        state.reset()
                    }
                )
            }
            .pointerInput(state, mode) {
                detectTransformGestures(panZoomLock = true) { centroid, pan, zoom, _ ->
                    state.onTransform(
                        zoomFactor = zoom,
                        panChange = pan,
                        centroid = centroid,
                        allowZoom = allowZoom,
                        allowPanX = allowPanX,
                        allowPanY = allowPanY
                    )
                }
            }
    } else Modifier

    Layout(
        content = content,
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { state.updateViewportSize(it) }
            .then(gestureModifier)
    ) { measurables, constraints ->
        val vW = constraints.maxWidth
        val vH = constraints.maxHeight

        // Measure child at natural unconstrained dimensions
        val childConstraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )

        val placeable = measurables.firstOrNull()?.measure(childConstraints)
        val cW = placeable?.width ?: 0
        val cH = placeable?.height ?: 0

        state.updateContentSize(IntSize(cW, cH))

        layout(vW, vH) {
            if (placeable != null && cW > 0 && cH > 0) {
                val initialX = if (cW * state.scale <= vW) (vW - cW) / 2 else 0
                val initialY = if (cH * state.scale <= vH) (vH - cH) / 2 else 0

                placeable.placeWithLayer(
                    x = initialX,
                    y = initialY
                ) {
                    scaleX = state.scale
                    scaleY = state.scale
                    translationX = state.translationX
                    translationY = state.translationY
                }
            }
        }
    }
}
