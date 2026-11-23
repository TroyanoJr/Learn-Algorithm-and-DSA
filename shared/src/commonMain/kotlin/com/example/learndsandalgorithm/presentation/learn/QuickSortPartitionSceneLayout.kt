package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

internal enum class QuickPartitionRegion {
    LESS_EQUAL,
    PROCESSED_GREATER,
    CURRENT_COMPARISON,
    UNPROCESSED,
    PIVOT
}

internal data class QuickSortPartitionStepLayout(
    val iPointerBounds: Rect2D,
    val jPointerBounds: Rect2D,
    val iPointerLine: LineSegment2D,
    val jPointerLine: LineSegment2D,
    val regionBounds: Map<QuickPartitionRegion, Rect2D>,
    val swapCellBounds: Map<Int, Rect2D>,
    val partitionBoundary: LineSegment2D?,
    val partitionBoundaryLabelBounds: Rect2D?
)

internal data class QuickSortPartitionSceneLayoutResult(
    val sceneBounds: Rect2D,
    val titleBounds: Rect2D,
    val pivotInfoBounds: Rect2D,
    val legendBounds: Map<QuickPartitionRegion, Rect2D>,
    val arrayRegionBounds: Rect2D,
    val arrayCellBounds: Map<Int, Rect2D>,
    val arrayValueBounds: Map<Int, Rect2D>,
    val arrayIndexBounds: Map<Int, Rect2D>,
    val zoneRibbonBounds: Rect2D,
    val comparisonResultBounds: Rect2D,
    val stepLayouts: Map<Int, QuickSortPartitionStepLayout>,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object QuickSortPartitionSceneLayout {
    private const val MIN_SCENE_WIDTH = 480f
    private const val LEFT_SENTINEL_GUTTER = 64f
    private const val RIGHT_MARGIN = 24f
    private const val TOP_MARGIN = 8f
    private const val CELL_GAP = 8f
    private const val CELL_HEIGHT = 52f
    private const val TITLE_HEIGHT = 16f
    private const val PIVOT_INFO_TOP = 26f
    private const val PIVOT_INFO_HEIGHT = 14f
    private const val LEGEND_TOP = 44f
    private const val LEGEND_HEIGHT = 18f
    private const val ZONE_RIBBON_TOP = 66f
    private const val ZONE_RIBBON_HEIGHT = 12f
    private const val I_POINTER_TOP = 82f
    private const val J_POINTER_TOP = 102f
    private const val POINTER_HEIGHT = 18f
    private const val CELL_TOP = 124f
    private const val INDEX_TOP = 176f
    private const val INDEX_HEIGHT = 14f
    private const val BOUNDARY_LABEL_TOP = 192f
    private const val BOUNDARY_LABEL_HEIGHT = 14f
    private const val COMPARISON_TOP = 210f
    private const val COMPARISON_HEIGHT = 40f
    private const val BOTTOM_MARGIN = 8f
    private const val POINTER_WIDTH = 52f
    private const val LEGEND_GAP = 5f
    private const val BOUNDARY_LABEL_WIDTH = 136f

    fun calculate(sequence: List<QuickSortPartitionSceneFrame>): QuickSortPartitionSceneLayoutResult {
        if (sequence.isEmpty()) return emptyLayout()

        val arraySize = sequence.maxOf { it.meta.array.size }
        val maxValueChars = sequence.maxOf { frame -> frame.meta.array.maxOfOrNull { it.toString().length } ?: 1 }
        val maxIndexChars = (arraySize - 1).coerceAtLeast(0).toString().length
        val cellWidth = max(64f, max(maxValueChars * 9f + 18f, maxIndexChars * 8f + 22f))
        val cellPitch = cellWidth + CELL_GAP
        val gridLeft = LEFT_SENTINEL_GUTTER
        val gridRight = if (arraySize == 0) gridLeft else gridLeft + (arraySize - 1) * cellPitch + cellWidth
        val requiredWidth = max(MIN_SCENE_WIDTH, gridRight + RIGHT_MARGIN)
        val requiredHeight = COMPARISON_TOP + COMPARISON_HEIGHT + BOTTOM_MARGIN
        val cellBounds = (0 until arraySize).associateWith { index ->
            Rect2D(gridLeft + index * cellPitch, CELL_TOP, cellWidth, CELL_HEIGHT)
        }
        val valueBounds = cellBounds.mapValues { (_, bounds) ->
            Rect2D(bounds.left + 2f, bounds.top + 2f, bounds.width - 4f, bounds.height - 4f)
        }
        val indexBounds = cellBounds.mapValues { (_, bounds) ->
            Rect2D(bounds.left, INDEX_TOP, bounds.width, INDEX_HEIGHT)
        }
        val legendInnerLeft = 16f
        val legendInnerWidth = requiredWidth - 32f
        val legendWidth = (legendInnerWidth - LEGEND_GAP * (QuickPartitionRegion.entries.size - 1)) /
            QuickPartitionRegion.entries.size
        val legendBounds = QuickPartitionRegion.entries.mapIndexed { index, region ->
            region to Rect2D(
                left = legendInnerLeft + index * (legendWidth + LEGEND_GAP),
                top = LEGEND_TOP,
                width = legendWidth,
                height = LEGEND_HEIGHT
            )
        }.toMap()

        val stepLayouts = sequence.associate { frame ->
            val meta = frame.meta
            val iBounds = if (meta.i < 0) {
                Rect2D(8f, I_POINTER_TOP, LEFT_SENTINEL_GUTTER - 16f, POINTER_HEIGHT)
            } else {
                centeredPointerBounds(cellBounds.getValue(meta.i), I_POINTER_TOP)
            }
            val jBounds = centeredPointerBounds(cellBounds.getValue(meta.j), J_POINTER_TOP)
            val iLine = if (meta.i < 0) {
                LineSegment2D(
                    Point2D(iBounds.right, iBounds.center.y),
                    Point2D(gridLeft - 2f, iBounds.center.y)
                )
            } else {
                LineSegment2D(
                    Point2D(iBounds.center.x, iBounds.bottom),
                    Point2D(iBounds.center.x - 8f, CELL_TOP)
                )
            }
            val jLine = LineSegment2D(
                Point2D(jBounds.center.x, jBounds.bottom),
                Point2D(jBounds.center.x + 8f, CELL_TOP)
            )
            val regionBounds = buildMap {
                fun addRegion(region: QuickPartitionRegion, indices: List<Int>) {
                    if (indices.isEmpty()) return
                    val firstCell = cellBounds.getValue(indices.first())
                    val lastCell = cellBounds.getValue(indices.last())
                    put(
                        region,
                        Rect2D(firstCell.left, ZONE_RIBBON_TOP, lastCell.right - firstCell.left, ZONE_RIBBON_HEIGHT)
                    )
                }
                addRegion(QuickPartitionRegion.LESS_EQUAL, meta.lessEqualIndices)
                addRegion(QuickPartitionRegion.PROCESSED_GREATER, meta.processedGreaterIndices)
                meta.activeComparisonIndex?.let { addRegion(QuickPartitionRegion.CURRENT_COMPARISON, listOf(it)) }
                addRegion(QuickPartitionRegion.UNPROCESSED, meta.unprocessedIndices)
                cellBounds[meta.pivotPosition]?.let { pivot ->
                    put(QuickPartitionRegion.PIVOT, Rect2D(pivot.left, ZONE_RIBBON_TOP, pivot.width, ZONE_RIBBON_HEIGHT))
                }
            }
            val swapCellBounds = meta.swappedIndices
                ?.toList()
                ?.distinct()
                ?.associateWith { cellBounds.getValue(it) }
                .orEmpty()
            val boundaryX = meta.finalPivotPosition?.let { pivotIndex ->
                val pivot = cellBounds.getValue(pivotIndex)
                if (pivotIndex == arraySize - 1) pivot.right + CELL_GAP / 2f else pivot.right + CELL_GAP / 2f
            }
            val boundaryLine = boundaryX?.let { x ->
                LineSegment2D(Point2D(x, ZONE_RIBBON_TOP - 3f), Point2D(x, INDEX_TOP + INDEX_HEIGHT))
            }
            val boundaryLabel = boundaryX?.let { x ->
                Rect2D(
                    left = (x - BOUNDARY_LABEL_WIDTH / 2f).coerceIn(0f, requiredWidth - BOUNDARY_LABEL_WIDTH),
                    top = BOUNDARY_LABEL_TOP,
                    width = BOUNDARY_LABEL_WIDTH,
                    height = BOUNDARY_LABEL_HEIGHT
                )
            }
            frame.scene.stepIndex to QuickSortPartitionStepLayout(
                iPointerBounds = iBounds,
                jPointerBounds = jBounds,
                iPointerLine = iLine,
                jPointerLine = jLine,
                regionBounds = regionBounds,
                swapCellBounds = swapCellBounds,
                partitionBoundary = boundaryLine,
                partitionBoundaryLabelBounds = boundaryLabel
            )
        }

        val cellsBottom = cellBounds.values.maxOfOrNull { it.bottom } ?: CELL_TOP
        return QuickSortPartitionSceneLayoutResult(
            sceneBounds = Rect2D(0f, 0f, requiredWidth, requiredHeight),
            titleBounds = Rect2D(16f, TOP_MARGIN, requiredWidth - 32f, TITLE_HEIGHT),
            pivotInfoBounds = Rect2D(16f, PIVOT_INFO_TOP, requiredWidth - 32f, PIVOT_INFO_HEIGHT),
            legendBounds = legendBounds,
            arrayRegionBounds = Rect2D(gridLeft - 6f, CELL_TOP - 4f, gridRight - gridLeft + 12f, cellsBottom - CELL_TOP + 4f),
            arrayCellBounds = cellBounds,
            arrayValueBounds = valueBounds,
            arrayIndexBounds = indexBounds,
            zoneRibbonBounds = Rect2D(gridLeft, ZONE_RIBBON_TOP, gridRight - gridLeft, ZONE_RIBBON_HEIGHT),
            comparisonResultBounds = Rect2D(16f, COMPARISON_TOP, requiredWidth - 32f, COMPARISON_HEIGHT),
            stepLayouts = stepLayouts,
            requiredSceneWidth = requiredWidth,
            requiredSceneHeight = requiredHeight
        )
    }

    private fun centeredPointerBounds(cell: Rect2D, top: Float): Rect2D =
        Rect2D(cell.center.x - POINTER_WIDTH / 2f, top, POINTER_WIDTH, POINTER_HEIGHT)

    private fun emptyLayout() = QuickSortPartitionSceneLayoutResult(
        sceneBounds = Rect2D(0f, 0f, 0f, 0f),
        titleBounds = Rect2D(0f, 0f, 0f, 0f),
        pivotInfoBounds = Rect2D(0f, 0f, 0f, 0f),
        legendBounds = emptyMap(),
        arrayRegionBounds = Rect2D(0f, 0f, 0f, 0f),
        arrayCellBounds = emptyMap(),
        arrayValueBounds = emptyMap(),
        arrayIndexBounds = emptyMap(),
        zoneRibbonBounds = Rect2D(0f, 0f, 0f, 0f),
        comparisonResultBounds = Rect2D(0f, 0f, 0f, 0f),
        stepLayouts = emptyMap(),
        requiredSceneWidth = 0f,
        requiredSceneHeight = 0f
    )
}
