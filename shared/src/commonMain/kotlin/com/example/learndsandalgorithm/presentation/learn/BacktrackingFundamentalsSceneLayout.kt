package com.example.learndsandalgorithm.presentation.learn

internal data class BacktrackingFundamentalsRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
}

internal data class FundamentalsLayoutMetrics(
    val headerHeight: Float = 12f,
    val candidateHeight: Float = 22f,
    val levelHeight: Float = 12f,
    val headingHeight: Float = 12f,
    val candidateWidth: Float = 160f,
    val headingWidth: Float = 200f,
    val nodeWidth: Float = 60f,
    val nodeHeight: Float = 20f,
    val summaryHeight: Float = 14f,
    val summaryWidth: Float = 160f,
    val countWidth: Float = 90f
)

internal data class FundamentalsTreeLayout(
    val width: Float,
    val height: Float,
    val candidateRegion: BacktrackingFundamentalsRect,
    val candidateHeader: BacktrackingFundamentalsRect,
    val candidateValue: BacktrackingFundamentalsRect,
    val level: BacktrackingFundamentalsRect,
    val treeRegion: BacktrackingFundamentalsRect,
    val treeHeading: BacktrackingFundamentalsRect,
    val nodes: Map<String, BacktrackingFundamentalsRect>,
    val summaryRegion: BacktrackingFundamentalsRect,
    val summary: BacktrackingFundamentalsRect,
    val count: BacktrackingFundamentalsRect
)

internal object BacktrackingFundamentalsSceneLayout {
    fun calculate(
        presentation: FundamentalsTreePresentation,
        viewportWidth: Float,
        viewportHeight: Float,
        metrics: FundamentalsLayoutMetrics = FundamentalsLayoutMetrics()
    ): FundamentalsTreeLayout {
        val maxDepth = presentation.nodes.maxOfOrNull { it.path.length } ?: 0
        val maxNodes = (0..maxDepth).maxOf { depth -> presentation.nodes.count { it.path.length == depth } }
        val nodeWidth = metrics.nodeWidth.coerceAtLeast(1f)
        val nodeHeight = metrics.nodeHeight.coerceAtLeast(1f)
        val levelWidth = maxOf(72f, metrics.countWidth)
        var width = maxOf(viewportWidth, 24f + maxNodes * (nodeWidth + 6f), metrics.summaryWidth + metrics.countWidth + 34f,
            metrics.candidateWidth + levelWidth + 40f, metrics.headingWidth + 32f)
        // The viewport compares the rounded fitted width with its bounds. Reserve a
        // pixel only when Float rounding would otherwise create a spurious pan range.
        repeat(4) {
            if (viewportWidth > 0f && width > viewportWidth && width * (viewportWidth / width) > viewportWidth) width += 1f
        }
        val inset = 8f
        val inner = width - 2f * inset
        val candidateHeight = maxOf(metrics.headerHeight + metrics.candidateHeight + 10f, metrics.levelHeight + 12f)
        val normalHeight = 8f + candidateHeight + 8f + 4f + metrics.headingHeight + 5f +
            (maxDepth + 1) * nodeHeight + maxDepth * 18f + 5f + 8f + metrics.summaryHeight + 12f + 8f
        val normalScale = minOf(viewportWidth / width, viewportHeight / normalHeight, 1f)
        val compactHeight = normalHeight - 8f - maxDepth * 6f
        val compactScale = minOf(viewportWidth / width, viewportHeight / compactHeight, 1f)
        val compact = compactScale > normalScale
        val gap = if (compact) 4f else 8f
        val candidateRegion = BacktrackingFundamentalsRect(inset, inset, inner, candidateHeight)
        val candidateHeader = BacktrackingFundamentalsRect(inset + 8f, inset + 4f, inner - levelWidth - 20f, metrics.headerHeight)
        val candidateValue = BacktrackingFundamentalsRect(candidateHeader.x, candidateHeader.bottom + 2f, candidateHeader.width, metrics.candidateHeight)
        val level = BacktrackingFundamentalsRect(candidateRegion.right - levelWidth - 6f, inset + 6f, levelWidth, metrics.levelHeight + 8f)
        val treeY = candidateRegion.bottom + gap
        val heading = BacktrackingFundamentalsRect(inset + 8f, treeY + 4f, inner - 16f, metrics.headingHeight)
        val levelGap = if (compact) 12f else 18f
        val nodeTop = heading.bottom + 5f
        val nodes = presentation.nodes.associate { node ->
            val depth = node.path.length
            val index = if (node.path.isEmpty()) 0 else node.path.fold(0) { acc, bit -> acc * 2 + if (bit == '1') 1 else 0 }
            val slots = 1 shl depth.coerceAtMost(20)
            val center = inset + inner * (index + .5f) / slots
            node.path to BacktrackingFundamentalsRect(center - nodeWidth / 2f, nodeTop + depth * (nodeHeight + levelGap), nodeWidth, nodeHeight)
        }
        val treeBottom = nodeTop + (maxDepth + 1) * nodeHeight + maxDepth * levelGap + 5f
        val treeRegion = BacktrackingFundamentalsRect(inset, treeY, inner, treeBottom - treeY)
        val summaryY = treeBottom + gap
        val summaryHeight = metrics.summaryHeight + 12f
        val summaryRegion = BacktrackingFundamentalsRect(inset, summaryY, inner, summaryHeight)
        val summary = BacktrackingFundamentalsRect(inset + 6f, summaryY + 6f, inner - metrics.countWidth - 18f, metrics.summaryHeight)
        val count = BacktrackingFundamentalsRect(summary.right + 6f, summaryY + 6f, metrics.countWidth, metrics.summaryHeight)
        return FundamentalsTreeLayout(width, summaryRegion.bottom + inset, candidateRegion, candidateHeader, candidateValue,
            level, treeRegion, heading, nodes, summaryRegion, summary, count)
    }
}
