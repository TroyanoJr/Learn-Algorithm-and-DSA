package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppBorder = Color(0xFF262634)
private val AppOrange = Color(0xFFFA6E13)
private val AppBlue = Color(0xFF38BDF8)
private val AppGreen = Color(0xFF10B981)
private val AppRed = Color(0xFFEF4444)
private val AppText = Color.White
private val AppSecondary = Color(0xFF9CA3AF)
private val AppCard = Color(0xFF13131C)

@Composable
internal fun HashTableApplicationsSceneRenderer(
    scene: VisualizationScene,
    step: ApplicationsTraceStep,
    modifier: Modifier = Modifier
) {
    val layout = HashTableApplicationsSceneLayout.calculate(step.scenario)
    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID) {
        ApplicationsCanvas(scene, step, layout)
    }
}

@Composable
private fun ApplicationsCanvas(scene: VisualizationScene, step: ApplicationsTraceStep, layout: HashTableApplicationsLayoutResult) {
    val measurer = rememberTextMeasurer()
    Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
        fun card(bounds: Rect2D, border: Color = AppBorder) {
            val top = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx())
            val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
            drawRoundRect(AppCard, top, size, CornerRadius(9.dp.toPx()))
            drawRoundRect(border, top, size, CornerRadius(9.dp.toPx()), style = Stroke(1.dp.toPx()))
        }
        fun centered(text: String, bounds: Rect2D, color: Color, font: Int, bold: Boolean = false) {
            val result = measurer.measure(text, TextStyle(color = color, fontSize = font.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    bounds.left.dp.toPx() + (bounds.width.dp.toPx() - result.size.width) / 2f,
                    bounds.top.dp.toPx() + (bounds.height.dp.toPx() - result.size.height) / 2f
                )
            )
        }
        fun label(text: String, bounds: Rect2D, color: Color, font: Int, bold: Boolean = false) {
            val result = measurer.measure(
                text = text,
                style = TextStyle(color = color, fontSize = font.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
            )
            drawText(textLayoutResult = result, topLeft = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()))
        }
        fun cell(bounds: Rect2D, text: String, fill: Color, border: Color, textColor: Color, font: Int = 9) {
            val top = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx())
            val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
            drawRoundRect(fill, top, size, CornerRadius(6.dp.toPx()))
            drawRoundRect(border, top, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
            centered(text, bounds, textColor, font, true)
        }
        fun frequencyCell(bounds: Rect2D, char: Char, count: Int, active: Boolean) {
            val top = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx())
            val size = Size(bounds.width.dp.toPx(), bounds.height.dp.toPx())
            drawRoundRect(if (active) Color(0xFF1A2B3C) else Color(0xFF181824), top, size, CornerRadius(6.dp.toPx()))
            drawRoundRect(if (active) AppBlue else AppBorder, top, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
            centered("'$char'", Rect2D(bounds.left, bounds.top + 1f, bounds.width, 18f), AppText, 12, true)
            centered("count: $count", Rect2D(bounds.left, bounds.top + 19f, bounds.width, 18f), AppGreen, 11, true)
        }
        when (step.scenario) {
            ApplicationScenario.FREQUENCY_COUNTER -> {
                card(layout.primaryCard); card(layout.secondaryCard!!)
                label("INPUT TEXT: \"${step.inputText}\"", layout.annotationBounds.getValue("input_title"), AppOrange, 11, true)
                step.inputText.orEmpty().forEachIndexed { index, char ->
                    val active = index == step.currentCharIndex
                    cell(layout.inputBounds.getValue(index), "'$char'", if (active) Color(0xFF2A1C12) else Color(0xFF1F1F2C), if (active) AppOrange else AppBorder, if (active) AppOrange else AppText, 12)
                }
                label("FREQUENCY HASH MAP (O(1) Avg Update):", layout.annotationBounds.getValue("frequency_title"), AppBlue, 10, true)
                if (step.freqMap.isEmpty()) centered("Map empty", Rect2D(22f, 116f, 90f, 38f), AppSecondary, 11)
                step.freqMap.forEach { pair ->
                    val active = pair.char == step.currentChar
                    val bounds = layout.mapBounds.getValue(pair.char)
                    frequencyCell(bounds, pair.char, pair.count, active)
                }
            }
            ApplicationScenario.RANGE_QUERY_COMPARISON -> {
                card(layout.primaryCard); card(layout.secondaryCard!!)
                label("HASH TABLE (UNORDERED):", layout.annotationBounds.getValue("hash_title"), AppOrange, 10, true)
                label("Range [10..25] requires O(N) full scan", layout.annotationBounds.getValue("hash_complexity"), AppSecondary, 9)
                step.hashBucketKeys.orEmpty().forEachIndexed { index, value ->
                    val visited = index in step.hashVisitedBuckets
                    val inRange = value != null && value in (step.rangeMin ?: 0)..(step.rangeMax ?: 0)
                    cell(layout.hashBounds.getValue(index), value?.toString() ?: "·", when { visited && inRange -> Color(0xFF123820); visited -> Color(0xFF2A1C12); else -> Color(0xFF181824) }, when { visited && inRange -> AppGreen; visited -> AppOrange; else -> AppBorder }, if (inRange) AppGreen else AppText)
                }
                label("BALANCED BST (SORTED):", layout.annotationBounds.getValue("bst_title"), AppBlue, 10, true)
                label("Range [10..25] O(log N + k) tree traversal", layout.annotationBounds.getValue("bst_complexity"), AppSecondary, 9)
                step.bstKeys.orEmpty().forEachIndexed { index, value ->
                    val matched = value in step.bstVisitedKeys
                    cell(layout.bstBounds.getValue(index), value.toString(), if (matched) Color(0xFF123820) else Color(0xFF181824), if (matched) AppGreen else AppBorder, if (matched) AppGreen else AppSecondary)
                }
            }
            ApplicationScenario.MUTABLE_KEY_PITFALL -> {
                card(layout.primaryCard, if (step.keyMutated) AppRed else AppBorder)
                label("MUTABLE KEY PITFALL", layout.annotationBounds.getValue("mutable_title"), if (step.keyMutated) AppRed else AppOrange, 11, true)
                if (step.keyMutated) label("KEY MUTATED IN-PLACE", layout.annotationBounds.getValue("mutation_badge"), AppRed, 9, true)
                label("Entry stored at bucket [${step.storedSlotIndex}] (hash(${step.originalKeyId}) % 8)", layout.annotationBounds.getValue("stored_annotation"), AppBlue, 10)
                step.searchedSlotIndex?.let { label("➔ Lookup checked bucket [$it] (hash(${step.mutatedKeyId}) % 8)", layout.annotationBounds.getValue("lookup_annotation"), AppRed, 9, true) }
                (0..7).forEach { index ->
                    val stored = scene.subScenes.first().entities.any { it.slotKey == "mutable:slot:$index" }
                    val searched = index == step.searchedSlotIndex
                    val text = if (stored) "id:${if (step.keyMutated) step.mutatedKeyId else step.originalKeyId}\n[$index]" else "·\n[$index]"
                    cell(layout.mutableSlotBounds.getValue(index), text, when { searched -> Color(0xFF381212); stored -> Color(0xFF13233A); else -> Color(0xFF181824) }, when { searched -> AppRed; stored -> AppBlue; else -> AppBorder }, if (searched) AppRed else AppText)
                }
            }
        }
    }
}
