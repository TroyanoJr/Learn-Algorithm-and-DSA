package com.example.learndsandalgorithm.presentation.learn

internal data class CombinationRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
}
internal fun combinationProgress(sum: Int, target: Int): Float =
    if (target == 0) { if (sum == 0) 1f else 0f } else (sum.toFloat() / target).coerceIn(0f, 1f)
internal data class CombinationLayoutMetrics(
    val targetWidth: Float = 100f, val currentWidth: Float = 100f, val headerHeight: Float = 14f,
    val differenceWidth: Float = 100f, val differenceHeight: Float = 12f,
    val headingWidth: Float = 210f, val headingHeight: Float = 12f,
    val candidateHeadingWidth: Float = 95f, val candidateHeight: Float = 12f,
    val itemWidth: Float = 40f, val itemHeight: Float = 24f,
    val emptyWidth: Float = 160f, val emptyHeight: Float = 14f,
    val operationWidth: Float = 70f, val operationHeight: Float = 12f,
    val solutionHeadingWidth: Float = 100f, val solutionCountWidth: Float = 60f,
    val solutionHeight: Float = 14f, val solutionWidth: Float = 65f,
    val conditionWidth: Float = 180f, val conditionHeight: Float = 12f
)
internal data class CombinationSceneLayoutResult(
    val width: Float, val height: Float,
    val header: CombinationRect, val target: CombinationRect, val current: CombinationRect,
    val difference: CombinationRect, val progress: CombinationRect,
    val stackRegion: CombinationRect, val stackHeading: CombinationRect,
    val candidateHeading: CombinationRect, val candidates: List<CombinationRect>,
    val operation: CombinationRect, val stack: List<CombinationRect>, val empty: CombinationRect,
    val solutionsRegion: CombinationRect, val solutionsHeading: CombinationRect,
    val solutionsCount: CombinationRect, val solutions: List<CombinationRect>, val emptySolution: CombinationRect, val condition: CombinationRect
)

internal object CombinationsPruningSceneLayout {
    fun calculate(presentation: CombinationsPresentation, viewportWidth: Float, viewportHeight: Float,
                  m: CombinationLayoutMetrics = CombinationLayoutMetrics()): CombinationSceneLayoutResult {
        val itemWidth = maxOf(34f, m.itemWidth + 12f)
        val itemHeight = maxOf(24f, m.itemHeight + 8f, m.candidateHeight, m.emptyHeight)
        val solutionWidth = maxOf(1f, m.solutionWidth + 12f)
        val solutionHeight = maxOf(1f, m.solutionHeight + 8f)
        val width = maxOf(viewportWidth, m.targetWidth + m.currentWidth + 48f, m.headingWidth + 36f,
            m.emptyWidth + 36f, m.candidateHeadingWidth + itemWidth + 42f,
            m.solutionHeadingWidth + m.solutionCountWidth + 48f, m.conditionWidth + 36f,
            m.differenceWidth + 36f, m.operationWidth + 36f, solutionWidth + 36f)
        val inner = width - 16f
        val available = inner - 20f
        val candidateColumns = ((available - m.candidateHeadingWidth - 6f + 6f) / (itemWidth + 6f)).toInt().coerceAtLeast(1)
        val candidateRows = ((presentation.candidates.size.coerceAtLeast(1) + candidateColumns - 1) / candidateColumns)
        val stackColumns = ((available + 6f) / (itemWidth + 6f)).toInt().coerceAtLeast(1)
        val stackRows = ((presentation.maximumStackSize.coerceAtLeast(1) + stackColumns - 1) / stackColumns)
        val solutionColumns = ((available + 6f) / (solutionWidth + 6f)).toInt().coerceAtLeast(1)
        val solutionRows = ((presentation.maximumSolutions.coerceAtLeast(1) + solutionColumns - 1) / solutionColumns)
        // Choose spacing from the actual available height; all fields retain their measured size.
        fun estimated(gap: Float): Float = 16f + (8f + m.headerHeight + 3f + m.differenceHeight + 3f + 10f + 8f) + gap +
            (8f + m.headingHeight + 3f + candidateRows * itemHeight + (candidateRows - 1) * 4f + 3f + m.operationHeight + 3f +
                stackRows * itemHeight + (stackRows - 1) * 4f + 8f) + gap +
            (8f + m.solutionHeight + 4f + solutionRows * solutionHeight + (solutionRows - 1) * 4f + 4f + m.conditionHeight + 8f)
        val normal = minOf(viewportWidth / width, viewportHeight / estimated(8f), 1f)
        val compact = minOf(viewportWidth / width, viewportHeight / estimated(4f), 1f) > normal
        val gap = if (compact) 4f else 8f
        val header = CombinationRect(8f, 8f, inner, 8f + m.headerHeight + 3f + m.differenceHeight + 3f + 10f + 8f)
        val target = CombinationRect(18f, header.y + 8f, m.targetWidth, m.headerHeight)
        val current = CombinationRect(header.right - 10f - m.currentWidth, target.y, m.currentWidth, m.headerHeight)
        val difference = CombinationRect(18f, target.bottom + 3f, available, m.differenceHeight)
        val progress = CombinationRect(18f, difference.bottom + 3f, available, 10f)
        val stackY = header.bottom + gap
        val stackHeading = CombinationRect(18f, stackY + 8f, available, m.headingHeight)
        val candidateTop = stackHeading.bottom + 3f
        val candidateHeading = CombinationRect(18f, candidateTop, m.candidateHeadingWidth, itemHeight)
        val candidateStart = candidateHeading.right + 6f
        val candidates = presentation.candidates.mapIndexed { index, _ ->
            CombinationRect(candidateStart + index % candidateColumns * (itemWidth + 6f), candidateTop + index / candidateColumns * (itemHeight + 4f), itemWidth, itemHeight)
        }
        val operation = CombinationRect(18f, candidateTop + candidateRows * itemHeight + (candidateRows - 1) * 4f + 3f, available, m.operationHeight)
        val stackTop = operation.bottom + 3f
        val stack = (0 until presentation.maximumStackSize).map { index ->
            CombinationRect(18f + index % stackColumns * (itemWidth + 6f), stackTop + index / stackColumns * (itemHeight + 4f), itemWidth, itemHeight)
        }
        val stackHeight = stackTop + stackRows * itemHeight + (stackRows - 1) * 4f + 8f - stackY
        val stackRegion = CombinationRect(8f, stackY, inner, stackHeight)
        val empty = CombinationRect(18f, stackTop, available, itemHeight)
        val solutionY = stackRegion.bottom + gap
        val heading = CombinationRect(18f, solutionY + 8f, m.solutionHeadingWidth, m.solutionHeight)
        val count = CombinationRect(8f + inner - 10f - m.solutionCountWidth, heading.y, m.solutionCountWidth, m.solutionHeight)
        val solutionTop = heading.bottom + 4f
        val solutions = (0 until presentation.maximumSolutions).map { index ->
            CombinationRect(18f + index % solutionColumns * (solutionWidth + 6f), solutionTop + index / solutionColumns * (solutionHeight + 4f), solutionWidth, solutionHeight)
        }
        val conditionY = solutionTop + solutionRows * solutionHeight + (solutionRows - 1) * 4f + 4f
        val condition = CombinationRect(18f, conditionY, available, m.conditionHeight)
        val solutionsRegion = CombinationRect(8f, solutionY, inner, condition.bottom - solutionY + 8f)
        return CombinationSceneLayoutResult(width, solutionsRegion.bottom + 8f, header, target, current, difference, progress,
            stackRegion, stackHeading, candidateHeading, candidates, operation, stack, empty, solutionsRegion, heading, count, solutions,
            CombinationRect(18f, solutionTop, available, solutionHeight), condition)
    }
}
