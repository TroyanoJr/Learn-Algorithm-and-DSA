package com.example.learndsandalgorithm.presentation.learn

internal data class MemoRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
}
internal data class MemoLayoutMetrics(
    val headingWidth: Float = 200f, val headingHeight: Float = 13f,
    val badgeWidth: Float = 70f, val valueWidth: Float = 30f, val valueHeight: Float = 14f,
    val indexHeight: Float = 12f, val stackHeadingWidth: Float = 145f, val stackHeadingHeight: Float = 13f,
    val callWidth: Float = 55f, val callHeight: Float = 22f, val emptyWidth: Float = 95f
)
internal data class MemoSceneLayout(val width: Float, val height: Float, val table: MemoRect,
    val tableHeading: MemoRect, val cacheBadge: MemoRect, val indexes: List<MemoRect>, val cells: List<MemoRect>,
    val stackRegion: MemoRect, val stackHeading: MemoRect, val callSlots: List<MemoRect>, val empty: MemoRect)

internal object DpMemoizationSceneLayout {
    fun calculate(scene: MemoScenePresentation, viewportWidth: Float, viewportHeight: Float,
                  m: MemoLayoutMetrics = MemoLayoutMetrics()): MemoSceneLayout {
        val cellWidth = maxOf(38f, m.valueWidth + 12f)
        val callWidth = maxOf(1f, m.callWidth + 10f)
        val callHeight = maxOf(1f, m.callHeight + 10f)
        val width = maxOf(viewportWidth, 36f + scene.tableSize * cellWidth + (scene.tableSize - 1).coerceAtLeast(0) * 4f,
            m.headingWidth + m.badgeWidth + 44f, m.stackHeadingWidth + 36f, m.emptyWidth + 36f, callWidth + 36f)
        val inner = width - 16f
        val table = MemoRect(8f, 8f, inner, 8f + m.headingHeight + 5f + m.indexHeight + 2f + maxOf(32f, m.valueHeight + 8f) + 8f)
        val heading = MemoRect(18f, 16f, m.headingWidth, m.headingHeight)
        val badge = MemoRect(table.right - 10f - m.badgeWidth, heading.y, m.badgeWidth, m.headingHeight)
        val totalWidth = scene.tableSize * cellWidth + (scene.tableSize - 1).coerceAtLeast(0) * 4f
        val first = (width - totalWidth) / 2f
        val indexes = (0 until scene.tableSize).map { i -> MemoRect(first + i * (cellWidth + 4f), heading.bottom + 5f, cellWidth, m.indexHeight) }
        val cells = indexes.map { MemoRect(it.x, it.bottom + 2f, it.width, maxOf(32f, m.valueHeight + 8f)) }
        val stackY = table.bottom + 6f
        val stackHeading = MemoRect(18f, stackY + 8f, inner - 20f, m.stackHeadingHeight)
        val columns = ((inner - 20f + 4f) / (callWidth + 4f)).toInt().coerceAtLeast(1)
        val rows = ((scene.maxDepth.coerceAtLeast(1) + columns - 1) / columns)
        val top = stackHeading.bottom + 4f
        val slots = (0 until scene.maxDepth).map { i -> MemoRect(18f + i % columns * (callWidth + 4f), top + i / columns * (callHeight + 4f), callWidth, callHeight) }
        val stack = MemoRect(8f, stackY, inner, top + rows * callHeight + (rows - 1) * 4f + 8f - stackY)
        return MemoSceneLayout(width, stack.bottom + 8f, table, heading, badge, indexes, cells, stack, stackHeading,
            slots, MemoRect(18f, top, inner - 20f, callHeight))
    }
}
