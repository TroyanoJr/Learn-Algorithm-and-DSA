package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.floor

internal object VisualizerSizing {
    fun cellSize(available: Float, count: Int, gap: Float, min: Float, max: Float): Float {
        if (count <= 0) return min
        val totalGaps = gap * (count - 1)
        val calculated = (available - totalGaps) / count
        return calculated.coerceIn(min, max)
    }

    fun maxVisibleCount(available: Float, gap: Float, min: Float): Int {
        if (available <= 0f || min <= 0f) return 1
        val raw = floor((available + gap) / (min + gap)).toInt()
        return raw.coerceAtLeast(1)
    }

    fun visibleWindow(total: Int, focus: Int, maxVisible: Int): IntRange {
        if (total <= 0) return IntRange.EMPTY
        if (total <= maxVisible) return 0 until total
        val count = maxVisible.coerceIn(1, total)
        val half = count / 2
        val start = (focus - half).coerceIn(0, total - count)
        val end = start + count - 1
        return start..end
    }
}
