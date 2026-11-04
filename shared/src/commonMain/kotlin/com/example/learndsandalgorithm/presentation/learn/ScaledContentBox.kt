package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints

@Composable
internal fun ScaledContentBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val unconstrained = Constraints()
        val placeables = subcompose(Unit, content).map { it.measure(unconstrained) }
        val naturalWidth = placeables.maxOfOrNull { it.width } ?: 0
        val naturalHeight = placeables.maxOfOrNull { it.height } ?: 0

        val availableWidth = constraints.maxWidth
        val availableHeight = constraints.maxHeight

        val widthRatio = if (naturalWidth > 0 && availableWidth < Int.MAX_VALUE) availableWidth.toFloat() / naturalWidth else 1f
        val heightRatio = if (naturalHeight > 0 && availableHeight < Int.MAX_VALUE) availableHeight.toFloat() / naturalHeight else 1f

        val effectiveFactor = minOf(1f, widthRatio, heightRatio)

        val scaledWidth = if (constraints.hasBoundedWidth) {
            (naturalWidth * effectiveFactor).toInt().coerceAtMost(constraints.maxWidth)
        } else {
            (naturalWidth * effectiveFactor).toInt()
        }

        val scaledHeight = if (constraints.hasBoundedHeight) {
            (naturalHeight * effectiveFactor).toInt().coerceAtMost(constraints.maxHeight)
        } else {
            (naturalHeight * effectiveFactor).toInt()
        }

        layout(scaledWidth, scaledHeight) {
            val offsetX = (scaledWidth - naturalWidth) / 2
            val offsetY = (scaledHeight - naturalHeight) / 2
            placeables.forEach { placeable ->
                placeable.placeWithLayer(offsetX, offsetY) {
                    scaleX = effectiveFactor
                    scaleY = effectiveFactor
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
            }
        }
    }
}
