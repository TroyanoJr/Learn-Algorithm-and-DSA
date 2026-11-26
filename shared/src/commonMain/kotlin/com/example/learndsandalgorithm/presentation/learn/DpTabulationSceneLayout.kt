package com.example.learndsandalgorithm.presentation.learn

internal data class TabRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
}
internal data class TabMetrics(val headingWidth: Float = 150f, val headingHeight: Float = 12f,
    val cellWidth: Float = 38f, val valueHeight: Float = 16f, val indexHeight: Float = 12f,
    val rollingHeadingWidth: Float = 200f, val variableWidth: Float = 85f, val variableHeight: Float = 16f,
    val formulaWidth: Float = 260f, val formulaHeight: Float = 14f)
internal data class TabSceneLayout(val width: Float, val height: Float, val table: TabRect, val heading: TabRect,
    val arrowLane: TabRect, val indices: List<TabRect>, val cells: List<TabRect>,
    val rolling: TabRect, val rollingHeading: TabRect, val variables: List<TabRect>, val formula: TabRect)
internal object DpTabulationSceneLayout {
    fun calculate(scene: TabulationPresentation, viewportWidth: Float, viewportHeight: Float, m: TabMetrics = TabMetrics()): TabSceneLayout {
        val cellWidth = maxOf(36f, m.cellWidth + 8f)
        val width = maxOf(viewportWidth, 36f + scene.tableSize * cellWidth + (scene.tableSize - 1).coerceAtLeast(0) * 4f,
            m.headingWidth + 36f, m.rollingHeadingWidth + 36f, 3f * m.variableWidth + 44f, m.formulaWidth + 36f)
        val inner = width - 16f
        val heading = TabRect(18f, 16f, inner - 20f, m.headingHeight)
        val arrows = TabRect(18f, heading.bottom + 3f, inner - 20f, 14f)
        val total = scene.tableSize * cellWidth + (scene.tableSize - 1).coerceAtLeast(0) * 4f
        val start = (width - total) / 2f
        val indices = (0 until scene.tableSize).map { TabRect(start + it * (cellWidth + 4f), arrows.bottom + 2f, cellWidth, m.indexHeight) }
        val cells = indices.map { TabRect(it.x, it.bottom + 2f, cellWidth, maxOf(32f, m.valueHeight + 8f)) }
        val tableBottom = (cells.maxOfOrNull { it.bottom } ?: arrows.bottom) + 8f
        val table = TabRect(8f, 8f, inner, tableBottom - 8f)
        val rollingHeading = TabRect(18f, table.bottom + 14f, inner - 20f, m.headingHeight)
        val variableWidth = (inner - 28f) / 3f
        val variables = (0..2).map { TabRect(18f + it * (variableWidth + 4f), rollingHeading.bottom + 4f, variableWidth, m.variableHeight) }
        val formula = TabRect(18f, variables.first().bottom + 4f, inner - 20f, m.formulaHeight)
        val rolling = TabRect(8f, table.bottom + 6f, inner, formula.bottom + 8f - table.bottom - 6f)
        return TabSceneLayout(width, rolling.bottom + 8f, table, heading, arrows, indices, cells, rolling, rollingHeading, variables, formula)
    }
}
