package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

private val MemoOrange = Color(0xFFFA6E13)
private val MemoBlue = Color(0xFF38BDF8)
private val MemoGreen = Color(0xFF10B981)
private val MemoBorder = Color(0xFF262634)
private val MemoSecondary = Color(0xFF9CA3AF)

internal enum class MemoCellState { EMPTY, CACHED, ACTIVE, SUCCESS }

internal fun memoCellState(step: DpMemoizationStep, index: Int): MemoCellState {
    val value = step.memoArray[index]
    val active = step.currentN == index && step.action != MemoAction.FINISHED
    return when {
        value != null && (active && (step.cacheHit || step.action == MemoAction.COMPUTE_AND_STORE) ||
            step.action == MemoAction.FINISHED && index == step.targetN) -> MemoCellState.SUCCESS
        active -> MemoCellState.ACTIVE
        value != null -> MemoCellState.CACHED
        else -> MemoCellState.EMPTY
    }
}

@Composable
internal fun DpMemoizationSceneRenderer(scene: MemoScenePresentation, frame: MemoSceneFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun measure(text: String, size: Int): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(text), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(text: Iterable<String>, size: Int): Pair<Float, Float> {
            val measured = text.map { measure(it, size) }
            return (measured.maxOfOrNull { it.first } ?: 1f) to (measured.maxOfOrNull { it.second } ?: 1f)
        }
        val layout = remember(scene, maxWidth, maxHeight, density.density, density.fontScale, measurer) {
            val heading = measure("MEMOIZATION LOOKUP TABLE memo[]:", 10)
            val badge = measure("CACHE HIT!", 9)
            val values = maximum(scene.frames.flatMap { it.step.memoArray }.map { it?.toString() ?: "null" }, 12)
            val indexes = maximum((0 until scene.tableSize).map { "[$it]" }, 9)
            val stackHeading = maximum(scene.frames.map { "Recursive Call Stack (top →): Depth: ${it.calls.size}" }, 10)
            val calls = maximum(scene.frames.flatMap { it.calls }.flatMap { listOf("Fib(${it.argument})", "TOP · Fib(${it.argument})") }, 12)
            val empty = measure("Empty call stack", 11)
            DpMemoizationSceneLayout.calculate(scene, maxWidth.value, maxHeight.value,
                MemoLayoutMetrics(heading.first, heading.second, badge.first + 10f, maxOf(values.first, indexes.first), values.second,
                    indexes.second, stackHeading.first, stackHeading.second, calls.first, calls.second, empty.first))
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-memoization", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun card(rect: MemoRect, fill: Color, border: Color) {
                    val pos = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(fill, pos, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, pos, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                fun text(value: String, rect: MemoRect, color: Color, size: Int, center: Boolean = false) {
                    val measured = measurer.measure(AnnotatedString(value), TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    val x = rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - measured.size.width) / 2f else 0f
                    drawText(measured, topLeft = Offset(x, rect.y.dp.toPx() + (rect.height.dp.toPx() - measured.size.height) / 2f), color = color)
                }
                card(layout.table, Color(0xFF13131C), MemoBorder)
                text("MEMOIZATION LOOKUP TABLE memo[]:", layout.tableHeading, MemoOrange, 10)
                if (frame.step.cacheHit) {
                    card(layout.cacheBadge, Color(0xFF123820), MemoGreen)
                    text("CACHE HIT!", layout.cacheBadge, MemoGreen, 9, true)
                }
                frame.step.memoArray.forEachIndexed { index, value ->
                    val active = frame.step.currentN == index && frame.step.action != MemoAction.FINISHED
                    val state = memoCellState(frame.step, index)
                    val color = when (state) { MemoCellState.SUCCESS -> MemoGreen; MemoCellState.ACTIVE -> MemoOrange
                        MemoCellState.CACHED -> MemoBlue; MemoCellState.EMPTY -> MemoBorder }
                    text("[$index]", layout.indexes[index], if (active) MemoOrange else MemoSecondary, 9, true)
                    card(layout.cells[index], when (state) { MemoCellState.SUCCESS -> Color(0xFF123820)
                        MemoCellState.ACTIVE -> Color(0xFF2A1C12); MemoCellState.CACHED -> Color(0xFF122C38)
                        MemoCellState.EMPTY -> Color(0xFF181824) }, color)
                    text(value?.toString() ?: "null", layout.cells[index], if (value == null) MemoSecondary else Color.White, 12, true)
                }
                card(layout.stackRegion, Color(0xFF13131C), MemoBorder)
                text("Recursive Call Stack (top →): Depth: ${frame.calls.size}", layout.stackHeading, MemoBlue, 10)
                if (frame.calls.isEmpty()) text("Empty call stack", layout.empty, MemoSecondary, 11)
                frame.calls.forEachIndexed { index, call ->
                    val active = index == frame.calls.lastIndex
                    card(layout.callSlots[index], if (active) Color(0xFF2A1C12) else Color(0xFF181824), if (active) MemoOrange else MemoBorder)
                    text(if (active) "TOP · Fib(${call.argument})" else "Fib(${call.argument})", layout.callSlots[index], if (active) MemoOrange else Color.White, 12, true)
                }
            }
        }
    }
}
