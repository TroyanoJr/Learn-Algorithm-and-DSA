package com.example.learndsandalgorithm.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BlueAccent = Color(0xFF38BDF8)
private val BlueBgColor = Color(0xFF13233A)
private val BlueBorderColor = Color(0xFF38BDF8).copy(alpha = 0.35f)

private val OrangeAccent = Color(0xFFFA6E13)
private val OrangeBgColor = Color(0xFF2A1C12)
private val OrangeBorderColor = Color(0xFFFA6E13).copy(alpha = 0.35f)

data class TopicIconSpec(
    val icon: ImageVector,
    val isDataStructure: Boolean
)

private fun buildVectorIcon(name: String, block: PathBuilder.() -> Unit): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White),
        pathBuilder = block
    ).build()
}

// Self-contained custom ImageVector paths for all 20 topics
private val IconGrid = buildVectorIcon("Grid") {
    moveTo(4f, 4f)
    horizontalLineTo(10f)
    verticalLineTo(10f)
    horizontalLineTo(4f)
    close()
    moveTo(14f, 4f)
    horizontalLineTo(20f)
    verticalLineTo(10f)
    horizontalLineTo(14f)
    close()
    moveTo(4f, 14f)
    horizontalLineTo(10f)
    verticalLineTo(20f)
    horizontalLineTo(4f)
    close()
    moveTo(14f, 14f)
    horizontalLineTo(20f)
    verticalLineTo(20f)
    horizontalLineTo(14f)
    close()
}

private val IconSchema = buildVectorIcon("Schema") {
    moveTo(4f, 6f)
    horizontalLineTo(10f)
    verticalLineTo(10f)
    horizontalLineTo(4f)
    close()
    moveTo(14f, 14f)
    horizontalLineTo(20f)
    verticalLineTo(18f)
    horizontalLineTo(14f)
    close()
    moveTo(7f, 10f)
    verticalLineTo(16f)
    horizontalLineTo(14f)
}

private val IconLink = buildVectorIcon("Link") {
    // Left node outer circle (center 5, 12, r=3)
    moveTo(5f, 9f)
    curveTo(6.66f, 9f, 8f, 10.34f, 8f, 12f)
    curveTo(8f, 13.66f, 6.66f, 15f, 5f, 15f)
    curveTo(3.34f, 15f, 2f, 13.66f, 2f, 12f)
    curveTo(2f, 10.34f, 3.34f, 9f, 5f, 9f)
    close()
    // Left node inner circle hole (center 5, 12, r=1.5)
    moveTo(5f, 10.5f)
    curveTo(4.17f, 10.5f, 3.5f, 11.17f, 3.5f, 12f)
    curveTo(3.5f, 12.83f, 4.17f, 13.5f, 5f, 13.5f)
    curveTo(5.83f, 13.5f, 6.5f, 12.83f, 6.5f, 12f)
    curveTo(6.5f, 11.17f, 5.83f, 10.5f, 5f, 10.5f)
    close()

    // Center node outer circle (center 12, 12, r=3)
    moveTo(12f, 9f)
    curveTo(13.66f, 9f, 15f, 10.34f, 15f, 12f)
    curveTo(15f, 13.66f, 13.66f, 15f, 12f, 15f)
    curveTo(10.34f, 15f, 9f, 13.66f, 9f, 12f)
    curveTo(9f, 10.34f, 10.34f, 9f, 12f, 9f)
    close()
    // Center node inner circle hole (center 12, 12, r=1.5)
    moveTo(12f, 10.5f)
    curveTo(11.17f, 10.5f, 10.5f, 11.17f, 10.5f, 12f)
    curveTo(10.5f, 12.83f, 11.17f, 13.5f, 12f, 13.5f)
    curveTo(12.83f, 13.5f, 13.5f, 12.83f, 13.5f, 12f)
    curveTo(13.5f, 11.17f, 12.83f, 10.5f, 12f, 10.5f)
    close()

    // Right node outer circle (center 19, 12, r=3)
    moveTo(19f, 9f)
    curveTo(20.66f, 9f, 22f, 10.34f, 22f, 12f)
    curveTo(22f, 13.66f, 20.66f, 15f, 19f, 15f)
    curveTo(17.34f, 15f, 16f, 13.66f, 16f, 12f)
    curveTo(16f, 10.34f, 17.34f, 9f, 19f, 9f)
    close()
    // Right node inner circle hole (center 19, 12, r=1.5)
    moveTo(19f, 10.5f)
    curveTo(18.17f, 10.5f, 17.5f, 11.17f, 17.5f, 12f)
    curveTo(17.5f, 12.83f, 18.17f, 13.5f, 19f, 13.5f)
    curveTo(19.83f, 13.5f, 20.5f, 12.83f, 20.5f, 12f)
    curveTo(20.5f, 11.17f, 19.83f, 10.5f, 19f, 10.5f)
    close()

    // Connecting segment 1 (between node 1 and 2)
    moveTo(7.5f, 11.2f)
    horizontalLineTo(9.5f)
    verticalLineTo(12.8f)
    horizontalLineTo(7.5f)
    close()

    // Connecting segment 2 (between node 2 and 3)
    moveTo(14.5f, 11.2f)
    horizontalLineTo(16.5f)
    verticalLineTo(12.8f)
    horizontalLineTo(14.5f)
    close()
}

private val IconStack = buildVectorIcon("Stack") {
    moveTo(12f, 3f)
    lineTo(2f, 8f)
    lineTo(12f, 13f)
    lineTo(22f, 8f)
    close()
    moveTo(2f, 12f)
    lineTo(12f, 17f)
    lineTo(22f, 12f)
    moveTo(2f, 16f)
    lineTo(12f, 21f)
    lineTo(22f, 16f)
}

private val IconQueue = buildVectorIcon("Queue") {
    moveTo(4f, 6f)
    horizontalLineTo(20f)
    verticalLineTo(8f)
    horizontalLineTo(4f)
    close()
    moveTo(4f, 11f)
    horizontalLineTo(20f)
    verticalLineTo(13f)
    horizontalLineTo(4f)
    close()
    moveTo(4f, 16f)
    horizontalLineTo(16f)
    verticalLineTo(18f)
    horizontalLineTo(4f)
    close()
}

private val IconKey = buildVectorIcon("Key") {
    moveTo(12.65f, 10f)
    curveTo(11.83f, 7.67f, 9.61f, 6f, 7f, 6f)
    curveTo(3.69f, 6f, 1f, 8.69f, 1f, 12f)
    curveTo(1f, 15.31f, 3.69f, 18f, 7f, 18f)
    curveTo(9.61f, 18f, 11.83f, 16.33f, 12.65f, 14f)
    horizontalLineTo(17f)
    verticalLineTo(18f)
    horizontalLineTo(21f)
    verticalLineTo(14f)
    horizontalLineTo(23f)
    verticalLineTo(10f)
    horizontalLineTo(12.65f)
    close()
    moveTo(7f, 14f)
    curveTo(5.9f, 14f, 5f, 13.1f, 5f, 12f)
    curveTo(5f, 10.9f, 5.9f, 10f, 7f, 10f)
    curveTo(8.1f, 10f, 9f, 10.9f, 9f, 12f)
    curveTo(9f, 13.1f, 8.1f, 14f, 7f, 14f)
    close()
}

private val IconTree = buildVectorIcon("Tree") {
    moveTo(11f, 3f)
    horizontalLineTo(13f)
    verticalLineTo(7f)
    horizontalLineTo(11f)
    close()
    moveTo(6f, 9f)
    lineTo(12f, 15f)
    lineTo(18f, 9f)
    moveTo(11f, 15f)
    horizontalLineTo(13f)
    verticalLineTo(21f)
    horizontalLineTo(11f)
}

private val IconHeap = buildVectorIcon("Heap") {
    moveTo(12f, 3f)
    lineTo(3f, 19f)
    horizontalLineTo(21f)
    close()
    moveTo(12f, 8f)
    lineTo(16f, 16f)
    horizontalLineTo(8f)
    close()
}

private val IconGraph = buildVectorIcon("Graph") {
    moveTo(6f, 6f)
    lineTo(18f, 6f)
    lineTo(12f, 18f)
    close()
}

private val IconCode = buildVectorIcon("Code") {
    moveTo(9.4f, 16.6f)
    lineTo(4.8f, 12f)
    lineTo(9.4f, 7.4f)
    lineTo(8f, 6f)
    lineTo(2f, 12f)
    lineTo(8f, 18f)
    close()
    moveTo(14.6f, 16.6f)
    lineTo(19.2f, 12f)
    lineTo(14.6f, 7.4f)
    lineTo(16f, 6f)
    lineTo(22f, 12f)
    lineTo(16f, 18f)
    close()
}

private val IconSearch = buildVectorIcon("Search") {
    moveTo(15.5f, 14f)
    horizontalLineTo(14.71f)
    lineTo(14.43f, 13.73f)
    curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
    curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
    curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f)
    curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f)
    curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
    lineTo(14f, 14.71f)
    verticalLineTo(15.5f)
    lineTo(19f, 20.49f)
    lineTo(20.49f, 19f)
    lineTo(15.5f, 14f)
    close()
    moveTo(9.5f, 14f)
    curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
    curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
    curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
    curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
    close()
}

private val IconBarChart = buildVectorIcon("BarChart") {
    moveTo(5f, 9.2f)
    horizontalLineTo(9f)
    verticalLineTo(19f)
    horizontalLineTo(5f)
    close()
    moveTo(10.6f, 5f)
    horizontalLineTo(14.6f)
    verticalLineTo(19f)
    horizontalLineTo(10.6f)
    close()
    moveTo(16.2f, 13f)
    horizontalLineTo(20.2f)
    verticalLineTo(19f)
    horizontalLineTo(16.2f)
    close()
}

private val IconLoop = buildVectorIcon("Loop") {
    moveTo(12f, 4f)
    verticalLineTo(1f)
    lineTo(8f, 5f)
    lineTo(12f, 9f)
    verticalLineTo(6f)
    curveTo(15.31f, 6f, 18f, 8.69f, 18f, 12f)
    curveTo(18f, 13.01f, 17.75f, 13.97f, 17.3f, 14.8f)
    lineTo(18.76f, 16.26f)
    curveTo(19.55f, 15.01f, 20f, 13.56f, 20f, 12f)
    curveTo(20f, 7.58f, 16.42f, 4f, 12f, 4f)
    close()
    moveTo(12f, 18f)
    curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
    curveTo(6f, 10.99f, 6.25f, 10.03f, 6.7f, 9.2f)
    lineTo(5.24f, 7.74f)
    curveTo(4.45f, 8.99f, 4f, 10.44f, 4f, 12f)
    curveTo(4f, 16.42f, 7.58f, 20f, 12f, 20f)
    verticalLineTo(23f)
    lineTo(16f, 19f)
    lineTo(12f, 15f)
    verticalLineTo(18f)
    close()
}

private val IconDivide = buildVectorIcon("Divide") {
    moveTo(19f, 11f)
    horizontalLineTo(5f)
    verticalLineTo(13f)
    horizontalLineTo(19f)
    close()
    moveTo(12f, 7f)
    curveTo(13.1f, 7f, 14f, 6.1f, 14f, 5f)
    curveTo(14f, 3.9f, 13.1f, 3f, 12f, 3f)
    curveTo(10.9f, 3f, 10f, 3.9f, 10f, 5f)
    curveTo(10f, 6.1f, 10.9f, 7f, 12f, 7f)
    close()
    moveTo(12f, 17f)
    curveTo(10.9f, 17f, 10f, 17.9f, 10f, 19f)
    curveTo(10f, 20.1f, 10.9f, 21f, 12f, 21f)
    curveTo(13.1f, 21f, 14f, 20.1f, 14f, 19f)
    curveTo(14f, 17.9f, 13.1f, 17f, 12f, 17f)
    close()
}

private val IconRefresh = buildVectorIcon("Refresh") {
    moveTo(17.65f, 6.35f)
    curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
    curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f)
    curveTo(4.01f, 16.42f, 7.58f, 20f, 12f, 20f)
    curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f)
    horizontalLineTo(17.65f)
    curveTo(16.83f, 16.33f, 14.61f, 18f, 12f, 18f)
    curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
    curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
    curveTo(13.66f, 6f, 15.14f, 6.69f, 16.22f, 7.78f)
    lineTo(13f, 11f)
    horizontalLineTo(20f)
    verticalLineTo(4f)
    lineTo(17.65f, 6.35f)
    close()
}

// Custom semantic ImageVector definitions for optimized topics
private val IconMemoTable = buildVectorIcon("MemoTable") {
    // 3x3 memoization results grid
    moveTo(4f, 4f); horizontalLineTo(8f); verticalLineTo(8f); horizontalLineTo(4f); close()
    moveTo(10f, 4f); horizontalLineTo(14f); verticalLineTo(8f); horizontalLineTo(10f); close()
    moveTo(16f, 4f); horizontalLineTo(20f); verticalLineTo(8f); horizontalLineTo(16f); close()

    moveTo(4f, 10f); horizontalLineTo(8f); verticalLineTo(14f); horizontalLineTo(4f); close()
    moveTo(10f, 10f); horizontalLineTo(14f); verticalLineTo(14f); horizontalLineTo(10f); close()
    moveTo(16f, 10f); horizontalLineTo(20f); verticalLineTo(14f); horizontalLineTo(16f); close()

    moveTo(4f, 16f); horizontalLineTo(8f); verticalLineTo(20f); horizontalLineTo(4f); close()
    moveTo(10f, 16f); horizontalLineTo(14f); verticalLineTo(20f); horizontalLineTo(10f); close()
    moveTo(16f, 16f); horizontalLineTo(20f); verticalLineTo(20f); horizontalLineTo(16f); close()
}

private val IconPrunedTree = buildVectorIcon("PrunedTree") {
    // Root node
    moveTo(10f, 3f); horizontalLineTo(14f); verticalLineTo(7f); horizontalLineTo(10f); close()
    // Left child node
    moveTo(5f, 11f); horizontalLineTo(9f); verticalLineTo(15f); horizontalLineTo(5f); close()
    // Right child node (bound)
    moveTo(15f, 11f); horizontalLineTo(19f); verticalLineTo(15f); horizontalLineTo(15f); close()
    // Left leaf node
    moveTo(2f, 18f); horizontalLineTo(6f); verticalLineTo(22f); horizontalLineTo(2f); close()

    // Connectors
    moveTo(11f, 7f); lineTo(8f, 11f); lineTo(9.5f, 11f); lineTo(12.5f, 7f); close()
    moveTo(13f, 7f); lineTo(16f, 11f); lineTo(14.5f, 11f); lineTo(11.5f, 7f); close()
    moveTo(6f, 15f); lineTo(4f, 18f); lineTo(5.5f, 18f); lineTo(7.5f, 15f); close()

    // Pruned X mark on right branch
    moveTo(14.5f, 18f); lineTo(18f, 21.5f); lineTo(19.5f, 20f); lineTo(16f, 16.5f); close()
    moveTo(18f, 16.5f); lineTo(19.5f, 18f); lineTo(16f, 21.5f); lineTo(14.5f, 20f); close()
}

private val IconChoice = buildVectorIcon("Choice") {
    // Top origin node
    moveTo(10f, 3f); horizontalLineTo(14f); verticalLineTo(7f); horizontalLineTo(10f); close()

    // Branch connectors
    moveTo(11f, 7f); lineTo(5f, 13f); lineTo(6.5f, 13f); lineTo(12.5f, 7f); close()
    moveTo(11.2f, 7f); lineTo(11.2f, 13f); horizontalLineTo(12.8f); verticalLineTo(7f); close()
    moveTo(13f, 7f); lineTo(19f, 13f); lineTo(17.5f, 13f); lineTo(11.5f, 7f); close()

    // Unselected left/right option nodes
    moveTo(3f, 13f); horizontalLineTo(7f); verticalLineTo(17f); horizontalLineTo(3f); close()
    moveTo(17f, 13f); horizontalLineTo(21f); verticalLineTo(17f); horizontalLineTo(17f); close()

    // Selected center choice node
    moveTo(10f, 12f); horizontalLineTo(14f); verticalLineTo(16f); horizontalLineTo(10f); close()

    // Selected choice checkmark below center
    moveTo(9f, 18.5f); lineTo(11f, 21f); lineTo(16f, 15.5f); lineTo(14.5f, 14f); lineTo(11f, 18f); lineTo(10.5f, 17f); close()
}

private val IconExhaustive = buildVectorIcon("Exhaustive") {
    // Center target / combination lock box
    moveTo(9f, 9f); horizontalLineTo(15f); verticalLineTo(15f); horizontalLineTo(9f); close()

    // 4 directional trial arrows exploring all angles
    // Top arrow (down)
    moveTo(11f, 2f); horizontalLineTo(13f); verticalLineTo(6f); lineTo(15f, 6f); lineTo(12f, 9f); lineTo(9f, 6f); horizontalLineTo(11f); close()
    // Bottom arrow (up)
    moveTo(11f, 22f); horizontalLineTo(13f); verticalLineTo(18f); lineTo(15f, 18f); lineTo(12f, 15f); lineTo(9f, 18f); horizontalLineTo(11f); close()
    // Left arrow (right)
    moveTo(2f, 11f); verticalLineTo(13f); horizontalLineTo(6f); verticalLineTo(15f); lineTo(9f, 12f); lineTo(6f, 9f); verticalLineTo(11f); close()
    // Right arrow (left)
    moveTo(22f, 11f); verticalLineTo(13f); horizontalLineTo(18f); verticalLineTo(15f); lineTo(15f, 12f); lineTo(18f, 9f); verticalLineTo(11f); close()
}

private val IconBlocks = buildVectorIcon("Blocks") {
    // Top block
    moveTo(7f, 3f); horizontalLineTo(17f); verticalLineTo(9f); horizontalLineTo(7f); close()
    // Bottom-left block
    moveTo(3f, 11f); horizontalLineTo(11f); verticalLineTo(17f); horizontalLineTo(3f); close()
    // Bottom-right block
    moveTo(13f, 11f); horizontalLineTo(21f); verticalLineTo(17f); horizontalLineTo(13f); close()
    // Foundation base bar
    moveTo(3f, 19f); horizontalLineTo(21f); verticalLineTo(21f); horizontalLineTo(3f); close()
}

fun getTopicIconSpec(topicId: String): TopicIconSpec {
    return when (topicId) {
        // Data Structures (Blue Accents)
        "introduction_ds" -> TopicIconSpec(IconBlocks, isDataStructure = true)
        "arrays" -> TopicIconSpec(IconGrid, isDataStructure = true)
        "structures" -> TopicIconSpec(IconSchema, isDataStructure = true)
        "linked_list" -> TopicIconSpec(IconLink, isDataStructure = true)
        "stack" -> TopicIconSpec(IconStack, isDataStructure = true)
        "queues" -> TopicIconSpec(IconQueue, isDataStructure = true)
        "hash_tables" -> TopicIconSpec(IconKey, isDataStructure = true)
        "trees" -> TopicIconSpec(IconTree, isDataStructure = true)
        "heap" -> TopicIconSpec(IconHeap, isDataStructure = true)
        "graphs" -> TopicIconSpec(IconGraph, isDataStructure = true)

        // Algorithms (Orange Accents)
        "introduction_algo" -> TopicIconSpec(IconCode, isDataStructure = false)
        "linear_search" -> TopicIconSpec(IconSearch, isDataStructure = false)
        "sorting" -> TopicIconSpec(IconBarChart, isDataStructure = false)
        "brute_force" -> TopicIconSpec(IconExhaustive, isDataStructure = false)
        "iteration_recursion" -> TopicIconSpec(IconLoop, isDataStructure = false)
        "divide_conquer" -> TopicIconSpec(IconDivide, isDataStructure = false)
        "greedy" -> TopicIconSpec(IconChoice, isDataStructure = false)
        "backtracking" -> TopicIconSpec(IconRefresh, isDataStructure = false)
        "dynamic_programming" -> TopicIconSpec(IconMemoTable, isDataStructure = false)
        "branch_bound" -> TopicIconSpec(IconPrunedTree, isDataStructure = false)

        // Fallback for unknown topic IDs
        else -> TopicIconSpec(IconCode, isDataStructure = false)
    }
}

@Composable
fun TopicIcon(
    topicId: String,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    isLocked: Boolean = false
) {
    val spec = getTopicIconSpec(topicId)
    val bgColor = if (isLocked) Color(0xFF1E1E28) else if (spec.isDataStructure) BlueBgColor else OrangeBgColor
    val borderColor = if (isLocked) Color(0xFF262634) else if (spec.isDataStructure) BlueBorderColor else OrangeBorderColor
    val tintColor = if (isLocked) Color(0xFF9CA3AF) else if (spec.isDataStructure) BlueAccent else OrangeAccent

    Box(
        modifier = modifier
            .size(containerSize)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = spec.icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
