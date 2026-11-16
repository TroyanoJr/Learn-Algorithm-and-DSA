package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HashTableSceneLayoutTest {

    @Test
    fun collisionStructureFitsTheRestoredLandscapeVisualPane() {
        val viewportWidths = listOf(640f, 740f, 840f)
        val detailsWidths = listOf(216f, 236f, 260f)
        val visualWidths = viewportWidths.zip(detailsWidths).map { (viewport, details) ->
            viewport - 16f - 8f - details
        }
        val metrics = HashTableStructuralMetrics(
            bucketWidth = 48f,
            nodeHeight = 38f,
            cellHeight = 52f,
            labelHeight = 18f
        )

        CollisionStrategy.entries.forEach { strategy ->
            val scenes = HashTableSceneBuilder.buildCollisionScenes(
                HashTableCollisionTrace.stepsForStrategy(strategy)
            )
            visualWidths.forEach { visualWidth ->
                val layouts = scenes.map { scene ->
                    HashTableSceneLayout.calculateLayout(
                        capacity = 7,
                        availableWidthDp = visualWidth,
                        subScene = scene.subScenes.first(),
                        strategy = strategy,
                        maxChainDepth = scene.hashTableMeta!!.maxChainDepth,
                        mode = HashTableSceneMode.COLLISION_STRUCTURE,
                        metrics = metrics
                    )
                }
                assertEquals(1, layouts.map { it.requiredSceneWidth }.distinct().size)
                assertEquals(1, layouts.map { it.requiredSceneHeight }.distinct().size)
                val fitScale = minOf(visualWidth / layouts.first().requiredSceneWidth, 1f)
                assertTrue(fitScale >= 0.98f, "Initial fit scale $fitScale at visual width $visualWidth")
            }
        }
    }

    @Test
    fun calculationRegionIsLeftAlignedStableAndBelowEverySnapshot() {
        CollisionStrategy.entries.forEach { strategy ->
            val scenes = HashTableSceneBuilder.buildCollisionScenes(
                HashTableCollisionTrace.stepsForStrategy(strategy)
            )
            val layouts = scenes.map { scene ->
                HashTableSceneLayout.calculateLayout(
                    capacity = 7,
                    availableWidthDp = 400f,
                    subScene = scene.subScenes.first(),
                    strategy = strategy,
                    maxChainDepth = scene.hashTableMeta!!.maxChainDepth,
                    mode = HashTableSceneMode.COLLISION_STRUCTURE,
                    metrics = HashTableStructuralMetrics(48f, 38f, 52f, 18f)
                )
            }
            val calculationHeights = scenes.mapIndexed { index, _ ->
                when {
                    index == scenes.lastIndex -> 112f
                    index % 3 == 0 -> 96f
                    else -> 72f
                }
            }
            val region = calculateHashTableCalculationRegion(
                sceneWidth = layouts.first().requiredSceneWidth,
                structuralLayouts = layouts,
                candidates = listOf(
                    HashTableCalculationCandidate(360f, calculationHeights)
                )
            )

            assertEquals(HashTableSceneLayout.HORIZONTAL_MARGIN_DP, region.bounds.left)
            assertEquals(calculationHeights.max(), region.bounds.height)
            assertTrue(region.bounds.right <= layouts.first().requiredSceneWidth)
            assertTrue(region.bounds.bottom <= region.requiredSceneHeight)

            layouts.forEach { layout ->
                val occupied = layout.bucketBounds.values + layout.chainNodeBounds.values +
                    layout.chainNullBounds.values + layout.indexLabelBounds.values +
                    layout.targetArrowBounds.values + layout.probeLabelBounds.values
                occupied.forEach { bounds ->
                    assertTrue(
                        bounds.bottom + 8f <= region.bounds.top,
                        "$strategy calculation overlaps structural bounds $bounds"
                    )
                }
                layout.chainConnectionLines.values.forEach { line ->
                    val arrowBottom = maxOf(line.start.y, line.end.y) + 6f
                    assertTrue(
                        arrowBottom + 8f <= region.bounds.top,
                        "$strategy calculation overlaps connection $line"
                    )
                }
            }

            val perSnapshotBounds = scenes.map { region.bounds }
            assertEquals(1, perSnapshotBounds.distinct().size)
        }
    }

    @Test
    fun leftAlignedCalculationAvoidsDeepRightChainAndRecoversInitialFit() {
        val viewportWidth = 400f
        val viewportHeight = 211f
        val candidateMeasurements = listOf(
            HashTableCalculationCandidate(216f, listOf(132f)),
            HashTableCalculationCandidate(272f, listOf(112f)),
            HashTableCalculationCandidate(328f, listOf(98f)),
            HashTableCalculationCandidate(376f, listOf(98f))
        )

        CollisionStrategy.entries.forEach { strategy ->
            val scenes = HashTableSceneBuilder.buildCollisionScenes(
                HashTableCollisionTrace.stepsForStrategy(strategy)
            )
            val layouts = scenes.map { scene ->
                HashTableSceneLayout.calculateLayout(
                    capacity = 7,
                    availableWidthDp = viewportWidth,
                    subScene = scene.subScenes.first(),
                    strategy = strategy,
                    maxChainDepth = scene.hashTableMeta!!.maxChainDepth,
                    mode = HashTableSceneMode.COLLISION_STRUCTURE,
                    metrics = HashTableStructuralMetrics(48f, 38f, 52f, 18f)
                )
            }
            val corrected = calculateHashTableCalculationRegion(
                sceneWidth = layouts.first().requiredSceneWidth,
                structuralLayouts = layouts,
                candidates = candidateMeasurements
            )
            val initialScale = minOf(
                viewportWidth / layouts.first().requiredSceneWidth,
                viewportHeight / corrected.requiredSceneHeight,
                1f
            )
            val globalOccupiedBottom = layouts.maxOf { layout ->
                val entityBottom = (
                    layout.bucketBounds.values + layout.chainNodeBounds.values +
                        layout.chainNullBounds.values + layout.indexLabelBounds.values +
                        layout.targetArrowBounds.values + layout.probeLabelBounds.values
                    ).maxOf { it.bottom }
                val connectionBottom = layout.chainConnectionLines.values.maxOfOrNull { line ->
                    maxOf(line.start.y, line.end.y) + 6f
                } ?: 0f
                maxOf(entityBottom, connectionBottom)
            }
            val previousSceneHeight = maxOf(
                layouts.first().requiredSceneHeight,
                globalOccupiedBottom + 8f + candidateMeasurements.last().cardHeights.max() + 12f
            )
            val previousScale = minOf(
                viewportWidth / layouts.first().requiredSceneWidth,
                viewportHeight / previousSceneHeight,
                1f
            )

            assertEquals(HashTableSceneLayout.HORIZONTAL_MARGIN_DP, corrected.bounds.left)
            assertTrue(corrected.bounds.right <= layouts.first().requiredSceneWidth)
            if (strategy == CollisionStrategy.SEPARATE_CHAINING) {
                assertEquals(328f, corrected.bounds.width)
                assertEquals(290f, previousSceneHeight)
                assertEquals(242f, corrected.requiredSceneHeight)
                assertTrue(initialScale > previousScale)
                assertTrue(initialScale >= 0.85f, "Chaining initial scale was $initialScale")
            } else {
                assertEquals(376f, corrected.bounds.width)
                assertEquals(222f, previousSceneHeight)
                assertEquals(222f, corrected.requiredSceneHeight)
                assertTrue(initialScale >= 0.90f, "Probing initial scale was $initialScale")
            }

            val startLayout = layouts.first()
            val heavyIndex = scenes.indices.maxBy { index ->
                if (strategy == CollisionStrategy.SEPARATE_CHAINING) {
                    layouts[index].chainNodeBounds.size
                } else {
                    scenes[index].hashTableMeta!!.probedIndices.size
                }
            }
            val heavyLayout = layouts[heavyIndex]
            listOf(startLayout, heavyLayout).forEach { layout ->
                val occupied = layout.bucketBounds.values + layout.chainNodeBounds.values +
                    layout.chainNullBounds.values + layout.indexLabelBounds.values +
                    layout.targetArrowBounds.values + layout.probeLabelBounds.values
                occupied.filter { bounds ->
                    bounds.right + 4f > corrected.bounds.left &&
                        bounds.left - 4f < corrected.bounds.right
                }.forEach { bounds ->
                    assertTrue(bounds.bottom + 8f <= corrected.bounds.top)
                }
            }
        }
    }


    @Test
    fun collisionStructureReservesOnlyStructureAcrossEverySnapshot() {
        CollisionStrategy.entries.forEach { strategy ->
            val steps = HashTableCollisionTrace.stepsForStrategy(strategy)
            val scenes = HashTableSceneBuilder.buildCollisionScenes(steps)
            assertEquals(if (strategy == CollisionStrategy.SEPARATE_CHAINING) 15 else 17, scenes.size)
            val layouts = scenes.map { scene ->
                val meta = scene.hashTableMeta!!
                val pane = scene.subScenes.first()
                val layout = HashTableSceneLayout.calculateLayout(
                    capacity = meta.capacity,
                    hasActiveKey = meta.activeKey != null,
                    hasBreakdown = meta.charBreakdown != null,
                    subScene = pane,
                    strategy = strategy,
                    maxChainDepth = meta.maxChainDepth,
                    mode = HashTableSceneMode.COLLISION_STRUCTURE
                )
                assertEquals(0f, layout.hashCardBounds.height)
                assertEquals(null, layout.breakdownBounds)
                assertEquals(null, layout.keyBadgeBounds)
                assertEquals(
                    pane.entities.count { it.slotKey.startsWith("chain_slot:") },
                    layout.chainNodeBounds.size
                )
                val rects = layout.bucketBounds.values + layout.chainNodeBounds.values +
                    layout.chainNullBounds.values + layout.targetArrowBounds.values +
                    layout.probeLabelBounds.values + layout.indexLabelBounds.values
                rects.forEach { rect ->
                    assertTrue(rect.left >= 0f && rect.top >= 0f)
                    assertTrue(rect.right <= layout.requiredSceneWidth)
                    assertTrue(rect.bottom <= layout.requiredSceneHeight)
                }
                layout.chainConnectionLines.values.forEach { line ->
                    listOf(line.start, line.end).forEach { point ->
                        assertTrue(point.x in 0f..layout.requiredSceneWidth)
                        assertTrue(point.y in 0f..layout.requiredSceneHeight)
                    }
                }
                layout
            }
            assertEquals(1, layouts.map { it.requiredSceneWidth to it.requiredSceneHeight }.distinct().size)
            assertEquals(520f, layouts.first().requiredSceneWidth)
            assertEquals(
                if (strategy == CollisionStrategy.SEPARATE_CHAINING) 186f else 116f,
                layouts.first().requiredSceneHeight
            )
        }
    }

    @Test
    fun largerTextMetricsKeepCollisionGeometryStableAndContained() {
        val scenes = HashTableSceneBuilder.buildCollisionScenes(
            HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING)
        )
        val metrics = HashTableStructuralMetrics(90f, 60f, 64f, 28f)
        val layouts = scenes.map { scene ->
            HashTableSceneLayout.calculateLayout(
                capacity = 7, subScene = scene.subScenes.first(),
                strategy = CollisionStrategy.SEPARATE_CHAINING,
                maxChainDepth = scene.hashTableMeta!!.maxChainDepth,
                mode = HashTableSceneMode.COLLISION_STRUCTURE, metrics = metrics
            )
        }
        assertEquals(1, layouts.map { it.requiredSceneHeight }.distinct().size)
        layouts.forEach { layout ->
            (layout.chainNodeBounds.values + layout.chainNullBounds.values).forEach {
                assertTrue(it.bottom <= layout.requiredSceneHeight)
                assertTrue(it.right <= layout.requiredSceneWidth)
            }
        }
    }

    @Test
    fun structuralModeCannotRemoveFundamentalsCalculation() {
        val original = HashTableSceneLayout.calculateLayout(7, hasActiveKey = true, hasBreakdown = true)
        val requested = HashTableSceneLayout.calculateLayout(
            7, hasActiveKey = true, hasBreakdown = true,
            mode = HashTableSceneMode.COLLISION_STRUCTURE
        )
        assertEquals(original, requested)
        assertEquals(105f, requested.hashCardBounds.height)
    }

    @Test
    fun testHashTableLayoutCalculatesAllExplicitBounds() {
        val layout = HashTableSceneLayout.calculateLayout(
            capacity = 8,
            availableWidthDp = 360f,
            availableHeightDp = 400f,
            hasActiveKey = true,
            hasBreakdown = true
        )

        assertEquals(8, layout.bucketBounds.size)
        assertEquals(8, layout.entryBounds.size)
        assertEquals(8, layout.indexLabelBounds.size)
        assertEquals(8, layout.targetArrowBounds.size)

        assertNotNull(layout.keyBadgeBounds)
        assertNotNull(layout.breakdownBounds)
        assertNotNull(layout.sumBounds)
        assertNotNull(layout.moduloBounds)

        assertTrue(layout.requiredSceneWidth > 0f)
        assertTrue(layout.requiredSceneHeight > 0f)

        // Check hash card bounds
        assertTrue(layout.hashCardBounds.width > 0f)
        assertTrue(layout.hashCardBounds.height > 0f)
        assertTrue(layout.hashCardBounds.top >= 0f)

        // Check internal card bounds stay inside hash card
        val card = layout.hashCardBounds
        assertTrue(layout.titleBounds.left >= card.left)
        assertTrue(layout.titleBounds.top >= card.top)
        assertTrue(layout.formulaBounds.right <= card.right)
        val kBadge = layout.keyBadgeBounds
        if (kBadge != null) {
            assertTrue(kBadge.left >= card.left)
            assertTrue(kBadge.right <= card.right)
        }
        val bDown = layout.breakdownBounds
        if (bDown != null) {
            assertTrue(bDown.left >= card.left)
        }

        // Check hash card does not overlap with buckets region
        val firstArrow = layout.targetArrowBounds["bucket_slot:0"]!!
        val firstBucket = layout.bucketBounds["bucket_slot:0"]!!
        assertTrue(layout.hashCardBounds.bottom <= firstArrow.top, "Hash card must be entirely above target arrows")
        assertTrue(firstArrow.bottom <= firstBucket.top, "Target arrow must be entirely above bucket cell")

        layout.bucketBounds.values.forEach { rect ->
            assertTrue(rect.left >= 0f)
            assertTrue(rect.top >= 0f)
            assertTrue(rect.width > 0f)
            assertTrue(rect.height > 0f)
            assertTrue(rect.right <= layout.requiredSceneWidth)
        }
    }

    @Test
    fun testCanvasBoundariesAndPositiveDimensionsAcrossViewports() {
        val widths = listOf(240f, 320f, 360f, 800f)
        val heights = listOf(160f, 200f, 400f, 600f)

        widths.forEach { width ->
            heights.forEach { height ->
                val layout = HashTableSceneLayout.calculateLayout(
                    capacity = 8,
                    availableWidthDp = width,
                    availableHeightDp = height,
                    hasActiveKey = true,
                    hasBreakdown = true
                )

                assertEquals(8, layout.bucketBounds.size)
                assertEquals(8, layout.indexLabelBounds.size)
                assertEquals(8, layout.targetArrowBounds.size)

                assertTrue(layout.hashCardBounds.width > 0f)
                assertTrue(layout.hashCardBounds.height > 0f)

                layout.bucketBounds.forEach { (slotKey, rect) ->
                    assertTrue(rect.left >= 0f, "Width $width, Height $height: Bucket $slotKey left should be >= 0")
                    assertTrue(rect.top >= 0f, "Width $width, Height $height: Bucket $slotKey top should be >= 0")
                    assertTrue(rect.width > 0f, "Width $width, Height $height: Bucket $slotKey width should be > 0")
                    assertTrue(rect.height > 0f, "Width $width, Height $height: Bucket $slotKey height should be > 0")
                    assertTrue(rect.right <= layout.requiredSceneWidth + 0.01f, "Width $width, Height $height: Bucket $slotKey right should be within required width")
                }

                layout.indexLabelBounds.forEach { (slotKey, labelRect) ->
                    val bucketRect = layout.bucketBounds[slotKey]!!
                    assertTrue(labelRect.left >= 0f)
                    assertTrue(labelRect.top >= bucketRect.bottom, "Width $width, Height $height: Label for $slotKey must be below bucket bottom")
                    assertTrue(labelRect.width > 0f)
                    assertTrue(labelRect.height > 0f)
                    assertTrue(labelRect.right <= layout.requiredSceneWidth + 0.01f)
                }

                layout.targetArrowBounds.forEach { (slotKey, arrowRect) ->
                    val bucketRect = layout.bucketBounds[slotKey]!!
                    assertTrue(arrowRect.bottom <= bucketRect.top, "Width $width, Height $height: Arrow for $slotKey must be above bucket top")
                }
            }
        }
    }

    @Test
    fun testAbsenceOfBucketOverlapsAndLogicalOrder() {
        val layout = HashTableSceneLayout.calculateLayout(
            capacity = 8,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        val buckets = (0 until 8).map { layout.bucketBounds["bucket_slot:$it"]!! }

        for (i in buckets.indices) {
            for (j in i + 1 until buckets.size) {
                val r1 = buckets[i]
                val r2 = buckets[j]
                val overlaps = r1.left < r2.right && r1.right > r2.left && r1.top < r2.bottom && r1.bottom > r2.top
                assertTrue(!overlaps, "Buckets $i and $j should not overlap")
            }
        }

        for (i in 0 until 7) {
            val current = buckets[i]
            val next = buckets[i + 1]
            assertTrue(next.left >= current.right - 0.01f, "Bucket ${i + 1} must be to the right of Bucket $i")
        }
    }

    @Test
    fun testSeparateChainingVerticalLayoutGeometry() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING, capacity = 7)
        val scenes = HashTableSceneBuilder.buildCollisionScenes(steps)
        val chainedScene = scenes.first { scene ->
            scene.subScenes.first().entities.any { it.slotKey == "chain_slot:6:1" }
        }

        val layout = HashTableSceneLayout.calculateLayout(
            capacity = 7,
            availableWidthDp = 360f,
            availableHeightDp = 400f,
            hasActiveKey = true,
            subScene = chainedScene.subScenes.first(),
            strategy = CollisionStrategy.SEPARATE_CHAINING,
            maxChainDepth = chainedScene.hashTableMeta?.maxChainDepth ?: 0
        )

        val node0 = layout.chainNodeBounds["chain_slot:6:0"]
        val node1 = layout.chainNodeBounds["chain_slot:6:1"]

        assertNotNull(node0)
        assertNotNull(node1)
        assertTrue(node1.top >= node0.bottom, "Chain node 1 must be vertically below chain node 0")

        val connLine = layout.chainConnectionLines["link:chain_slot:6:0->chain_slot:6:1"]
        assertNotNull(connLine)
        assertTrue(connLine.start.y >= node0.bottom)
        assertTrue(connLine.end.y <= node1.top)
    }

    @Test
    fun testEdgeCapacityReturnsEmptyLayout() {
        val layout = HashTableSceneLayout.calculateLayout(
            capacity = 0,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertTrue(layout.bucketBounds.isEmpty())
        assertTrue(layout.entryBounds.isEmpty())
        assertTrue(layout.indexLabelBounds.isEmpty())
        assertEquals(0f, layout.requiredSceneWidth)
        assertEquals(0f, layout.requiredSceneHeight)
    }
}
