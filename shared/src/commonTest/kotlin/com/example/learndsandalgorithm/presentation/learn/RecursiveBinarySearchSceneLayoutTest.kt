package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RecursiveBinarySearchSceneLayoutTest {

    @Test
    fun defaultSceneIsCompactStableAndContainsAllElements() {
        val frames = RecursiveBinarySearchSceneBuilder.build(RecursiveBinarySearchTrace.stepsForSearch())
        val layout = RecursiveBinarySearchSceneLayout.calculate(frames)

        assertEquals(layout, RecursiveBinarySearchSceneLayout.calculate(frames))
        assertEquals(layout, RecursiveBinarySearchSceneLayout.calculate(frames.reversed()))
        assertEquals(376f, layout.requiredSceneWidth)
        assertEquals(326f, layout.requiredSceneHeight)
        assertTrue(layout.requiredSceneHeight < 360f)
        assertEquals(5, layout.cellBounds.size)
        assertEquals(2, layout.maximumStackDepth)
        assertContained(layout)
        assertNoOverlap(layout.markerBounds.values + layout.cellBounds.values + layout.indexBounds.values)
        assertNoOverlap(layout.stackRowBounds.values.toList())
        assertNoOverlap(listOf(layout.targetBounds, layout.rangeBounds))
        assertTrue(layout.indexBounds.values.none { label -> layout.stackRowBounds.values.any { overlaps(label, it) } })
        assertTrue(layout.stackRowBounds.values.none { overlaps(it, layout.midpointBounds) })
        assertTrue(layout.stackRowBounds.values.none { overlaps(it, layout.returnBounds) })
    }

    @Test
    fun emptyAndSingletonScenesHaveValidMarkerAndStackBounds() {
        val emptyFrames = RecursiveBinarySearchSceneBuilder.build(RecursiveBinarySearchTrace.stepsForSearch(emptyList()))
        val emptyLayout = RecursiveBinarySearchSceneLayout.calculate(emptyFrames)
        assertTrue(emptyLayout.cellBounds.isEmpty())
        assertTrue(emptyLayout.markerBounds.isEmpty())
        assertNotNull(emptyLayout.emptyArrayBounds)
        assertEquals(1, emptyLayout.maximumStackDepth)
        assertContained(emptyLayout)

        val singletonFrames = RecursiveBinarySearchSceneBuilder.build(
            RecursiveBinarySearchTrace.stepsForSearch(listOf(Int.MIN_VALUE), target = Int.MIN_VALUE)
        )
        val singletonLayout = RecursiveBinarySearchSceneLayout.calculate(singletonFrames)
        assertEquals(setOf(0), singletonLayout.cellBounds.keys)
        assertEquals(singletonLayout.cellBounds.getValue(0).left, singletonLayout.markerBounds.getValue(0).left)
        assertEquals(singletonLayout.cellBounds.getValue(0).left, singletonLayout.indexBounds.getValue(0).left)
        assertEquals(1, singletonLayout.maximumStackDepth)
        assertContained(singletonLayout)
        assertNoOverlap(singletonLayout.markerBounds.values + singletonLayout.cellBounds.values + singletonLayout.indexBounds.values)
    }

    @Test
    fun longerDuplicateNegativeAndExtremeArraysRemainContainedAndDeterministic() {
        val inputs = listOf(
            listOf(-8, -8, -1, 0, 0, 7, 7, 12, 12),
            listOf(Int.MIN_VALUE, -2_000_000_000, -1, 0, 1, Int.MAX_VALUE),
            (-8..8).toList()
        )
        inputs.forEach { input ->
            val frames = RecursiveBinarySearchSceneBuilder.build(
                RecursiveBinarySearchTrace.stepsForSearch(input, target = input[input.lastIndex / 2])
            )
            val layout = RecursiveBinarySearchSceneLayout.calculate(frames)
            assertEquals(layout, RecursiveBinarySearchSceneLayout.calculate(frames))
            assertEquals(layout, RecursiveBinarySearchSceneLayout.calculate(frames.reversed()))
            assertEquals(input.indices.toSet(), layout.cellBounds.keys)
            assertEquals(input.indices.toSet(), layout.indexBounds.keys)
            assertTrue(layout.markerBounds.keys.all { it in input.indices })
            assertTrue(layout.requiredSceneWidth >= input.size * 64f)
            assertContained(layout)
            assertNoOverlap(layout.markerBounds.values + layout.cellBounds.values + layout.indexBounds.values)
            assertNoOverlap(layout.stackRowBounds.values.toList())
            assertTrue(layout.indexBounds.values.none { label -> layout.stackRowBounds.values.any { overlaps(label, it) } })
        }
    }

    private fun assertContained(layout: RecursiveBinarySearchSceneLayoutResult) {
        val bounds = buildList {
            add(layout.headerBounds)
            add(layout.targetBounds)
            add(layout.rangeBounds)
            addAll(layout.markerBounds.values)
            addAll(layout.cellBounds.values)
            addAll(layout.indexBounds.values)
            layout.emptyArrayBounds?.let(::add)
            add(layout.midpointBounds)
            add(layout.stackHeaderBounds)
            addAll(layout.stackRowBounds.values)
            add(layout.returnBounds)
            add(layout.complexityBounds)
        }
        assertTrue(bounds.all {
            it.left >= 0f && it.top >= 0f &&
                it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight
        })
    }

    private fun assertNoOverlap(bounds: Collection<Rect2D>) {
        val items = bounds.toList()
        items.indices.forEach { first ->
            for (second in first + 1 until items.size) {
                assertTrue(!overlaps(items[first], items[second]), "Bounds overlap: ${items[first]} and ${items[second]}")
            }
        }
    }

    private fun overlaps(first: Rect2D, second: Rect2D): Boolean =
        first.left < second.right && second.left < first.right &&
            first.top < second.bottom && second.top < first.bottom
}
