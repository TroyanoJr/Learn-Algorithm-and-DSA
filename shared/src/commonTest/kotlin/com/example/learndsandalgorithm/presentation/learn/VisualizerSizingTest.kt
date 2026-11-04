package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals

class VisualizerSizingTest {

    @Test
    fun cellSizeCalculatesCorrectWidthAndClampsToMinMax() {
        val size = VisualizerSizing.cellSize(available = 200f, count = 5, gap = 10f, min = 20f, max = 50f)
        assertEquals(32f, size)

        val clampedMin = VisualizerSizing.cellSize(available = 50f, count = 5, gap = 10f, min = 20f, max = 50f)
        assertEquals(20f, clampedMin)

        val clampedMax = VisualizerSizing.cellSize(available = 500f, count = 5, gap = 10f, min = 20f, max = 50f)
        assertEquals(50f, clampedMax)
    }

    @Test
    fun maxVisibleCountReturnsAtLeastOne() {
        val count = VisualizerSizing.maxVisibleCount(available = 300f, gap = 10f, min = 36f)
        assertEquals(6, count)

        val smallSpace = VisualizerSizing.maxVisibleCount(available = 10f, gap = 10f, min = 36f)
        assertEquals(1, smallSpace)
    }

    @Test
    fun visibleWindowHandlesEdgeCasesCorrectly() {
        // Single element
        val single = VisualizerSizing.visibleWindow(total = 1, focus = 0, maxVisible = 5)
        assertEquals(0..0, single)

        // Focus at first element
        val firstFocus = VisualizerSizing.visibleWindow(total = 11, focus = 0, maxVisible = 5)
        assertEquals(0..4, firstFocus)

        // Focus at last element
        val lastFocus = VisualizerSizing.visibleWindow(total = 11, focus = 10, maxVisible = 5)
        assertEquals(6..10, lastFocus)

        // Focus in middle
        val midFocus = VisualizerSizing.visibleWindow(total = 11, focus = 5, maxVisible = 5)
        assertEquals(3..7, midFocus)

        // Available space smaller than 1 element
        val tiny = VisualizerSizing.visibleWindow(total = 10, focus = 2, maxVisible = 1)
        assertEquals(2..2, tiny)
    }
}
