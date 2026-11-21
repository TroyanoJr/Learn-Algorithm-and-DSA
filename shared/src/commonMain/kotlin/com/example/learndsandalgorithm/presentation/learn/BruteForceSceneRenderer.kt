package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BruteBg = Color(0xFF13131C)
private val BruteBorder = Color(0xFF30303E)
private val BruteOrange = Color(0xFFFA6E13)
private val BruteBlue = Color(0xFF38BDF8)
private val BruteGreen = Color(0xFF10B981)
private val BruteRed = Color(0xFFEF4444)
private val BrutePrimary = Color.White
private val BruteSecondary = Color(0xFF9CA3AF)

@Composable
internal fun BruteForceSceneRenderer(
    frame: BruteForceSceneFrame,
    sequence: List<BruteForceSceneFrame>,
    modifier: Modifier = Modifier,
    viewportKey: Any
) {
    val layout = remember(sequence) { BruteForceSceneLayout.calculate(sequence) }
    val viewport = rememberVisualizerCanvasState(key = viewportKey)
    val measurer = rememberTextMeasurer()

    VisualizerCanvasViewport(modifier.fillMaxSize(), VisualizerCanvasMode.HYBRID, viewport) {
        Canvas(Modifier.size(layout.requiredSceneWidth.dp, layout.requiredSceneHeight.dp)) {
            fun text(value: String, bounds: Rect2D, color: Color, size: Int, bold: Boolean = false, maxLines: Int = 1) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val measured = measurer.measure(
                    value,
                    TextStyle(color = color, fontSize = size.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    maxLines = maxLines,
                    softWrap = maxLines > 1,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )
                drawText(measured, topLeft = Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx() + (bounds.height.dp.toPx() - measured.size.height) / 2f))
            }
            fun card(bounds: Rect2D, fill: Color = BruteBg, border: Color = BruteBorder, radius: Float = 8f) {
                drawRoundRect(fill, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()))
                drawRoundRect(border, Offset(bounds.left.dp.toPx(), bounds.top.dp.toPx()), Size(bounds.width.dp.toPx(), bounds.height.dp.toPx()), CornerRadius(radius.dp.toPx()), style = Stroke(1.dp.toPx()))
            }
            fun centered(value: String, bounds: Rect2D, color: Color, size: Int, bold: Boolean = false) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val measured = measurer.measure(
                    value,
                    TextStyle(color = color, fontSize = size.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    maxLines = 1,
                    softWrap = false,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )
                drawText(
                    measured,
                    topLeft = Offset(
                        bounds.left.dp.toPx() + (bounds.width.dp.toPx() - measured.size.width) / 2f,
                        bounds.top.dp.toPx() + (bounds.height.dp.toPx() - measured.size.height) / 2f
                    )
                )
            }
            fun centeredClipped(value: String, bounds: Rect2D, color: Color, size: Int, bold: Boolean = false) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val measured = measurer.measure(
                    value,
                    TextStyle(color = color, fontSize = size.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    maxLines = 1,
                    softWrap = false,
                    constraints = Constraints(
                        maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                        maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                    )
                )
                val left = bounds.left.dp.toPx()
                val top = bounds.top.dp.toPx()
                val width = bounds.width.dp.toPx()
                val height = bounds.height.dp.toPx()
                clipRect(left = left, top = top, right = left + width, bottom = top + height) {
                    drawText(
                        measured,
                        topLeft = Offset(
                            left + (width - measured.size.width).coerceAtLeast(0f) / 2f,
                            top + (height - measured.size.height).coerceAtLeast(0f) / 2f
                        )
                    )
                }
            }
            fun centeredFitted(value: String, bounds: Rect2D, color: Color, size: Int, bold: Boolean = false, maxLines: Int = 1) {
                if (bounds.width <= 0f || bounds.height <= 0f) return
                val constraints = Constraints(
                    maxWidth = bounds.width.dp.roundToPx().coerceAtLeast(1),
                    maxHeight = bounds.height.dp.roundToPx().coerceAtLeast(1)
                )
                var fontSize = size.toFloat()
                var measured = measurer.measure(
                    value,
                    TextStyle(color = color, fontSize = fontSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                    maxLines = maxLines,
                    softWrap = maxLines > 1,
                    constraints = constraints
                )
                while ((measured.didOverflowWidth || measured.didOverflowHeight) && fontSize > 7f) {
                    fontSize -= 0.5f
                    measured = measurer.measure(
                        value,
                        TextStyle(color = color, fontSize = fontSize.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
                        maxLines = maxLines,
                        softWrap = maxLines > 1,
                        constraints = constraints
                    )
                }
                val left = bounds.left.dp.toPx()
                val top = bounds.top.dp.toPx()
                val width = bounds.width.dp.toPx()
                val height = bounds.height.dp.toPx()
                clipRect(left = left, top = top, right = left + width, bottom = top + height) {
                    drawText(
                        measured,
                        topLeft = Offset(
                            left + (width - measured.size.width).coerceAtLeast(0f) / 2f,
                            top + (height - measured.size.height).coerceAtLeast(0f) / 2f
                        )
                    )
                }
            }

            card(layout.headerBounds)
            when (frame.meta.lesson) {
                BruteForceLesson.FUNDAMENTALS -> {
                    text("BRUTE FORCE FUNDAMENTALS", layout.titleBounds, BruteOrange, 10, true)
                    text("GOAL: Find x in [${frame.meta.range?.first}..${frame.meta.range?.last}] where x² = ${frame.meta.target}", layout.goalBounds, BruteSecondary, 9, true)
                    card(layout.statusBounds, if (frame.meta.foundSolution != null) Color(0xFF123820) else Color(0xFF2A1C12), if (frame.meta.foundSolution != null) BruteGreen else BruteOrange, 6f)
                    centered(fundamentalsStatusLabel(frame.meta.eventName), layout.statusBounds, if (frame.meta.foundSolution != null) BruteGreen else BruteOrange, 8, true)
                    val evaluationAccent = when (frame.meta.eventName) {
                        "SOLUTION_FOUND" -> BruteGreen
                        "START" -> BruteBlue
                        else -> BruteOrange
                    }
                    card(layout.evaluationBounds, when (frame.meta.eventName) {
                        "SOLUTION_FOUND" -> Color(0xFF123820)
                        "START" -> Color(0xFF122B36)
                        else -> Color(0xFF2A1C12)
                    }, evaluationAccent, 7f)
                    centeredClipped(
                        "CANDIDATE EVALUATION",
                        Rect2D(layout.evaluationBounds.left + 8f, layout.evaluationBounds.top + 3f, layout.evaluationBounds.width - 16f, 15f),
                        BruteSecondary,
                        8,
                        true
                    )
                    centeredClipped(
                        fundamentalsEvaluationText(frame.meta),
                        Rect2D(layout.evaluationBounds.left + 8f, layout.evaluationBounds.top + 20f, layout.evaluationBounds.width - 16f, 22f),
                        evaluationAccent,
                        10,
                        true
                    )
                    val candidates = frame.meta.range?.toList().orEmpty()
                    val candidateEntities = frame.scene.subScenes.first().entities.associateBy { it.dataValue.toInt() }
                    candidates.forEachIndexed { slot, candidate ->
                        val evaluation = frame.meta.evaluations.firstOrNull { it.candidate == candidate }
                        val current = candidate == frame.meta.currentCandidate
                        val match = evaluation?.isMatch == true
                        val entityState = candidateEntities[candidate]?.visualState ?: EntityVisualState.DEFAULT
                        val label = fundamentalsDisplayLabel(
                            match,
                            evaluation != null,
                            current && frame.meta.eventName == "EVALUATE_CANDIDATE"
                        )
                        centered(label, layout.labelBounds.getValue(slot), when { label == "MATCH" -> BruteGreen; label == "TEST" -> BruteOrange; else -> BruteSecondary }, 8, true)
                        val bounds = layout.cellBounds.getValue(slot)
                        card(bounds, when (entityState) {
                            EntityVisualState.TARGET -> Color(0xFF123820)
                            EntityVisualState.VISITED -> Color(0xFF181824)
                            EntityVisualState.ACTIVE -> Color(0xFF2A1C12)
                            EntityVisualState.DEFAULT -> BruteBg
                            EntityVisualState.VIOLATION -> Color(0xFF381212)
                        }, when (entityState) {
                            EntityVisualState.TARGET -> BruteGreen
                            EntityVisualState.VISITED, EntityVisualState.DEFAULT -> BruteBorder
                            EntityVisualState.ACTIVE -> BruteOrange
                            EntityVisualState.VIOLATION -> BruteRed
                        })
                        centered("x = $candidate", layout.primaryTextBounds.getValue(slot), BrutePrimary, 11, true)
                        centered("x² = ${candidate * candidate}", layout.secondaryTextBounds.getValue(slot), BruteBlue, 9)
                    }
                }
                BruteForceLesson.PAIR_PROBLEMS -> {
                    centeredFitted("TWO-SUM PAIR SEARCH", layout.titleBounds, BruteOrange, 14, true)
                    text("GOAL: values[i] + values[j] = ${frame.meta.target}", layout.goalBounds, BruteSecondary, 9, true)
                    val pairStatusColor = when (frame.meta.eventName) {
                        "START" -> BruteBlue
                        "PAIR_FOUND" -> BruteGreen
                        "NO_PAIR_FOUND" -> BruteRed
                        else -> BruteOrange
                    }
                    card(layout.statusBounds, when (frame.meta.eventName) {
                        "PAIR_FOUND" -> Color(0xFF123820)
                        "NO_PAIR_FOUND" -> Color(0xFF381212)
                        else -> Color(0xFF2A1C12)
                    }, pairStatusColor, 6f)
                    centered(pairStatusLabel(frame.meta.eventName), layout.statusBounds, pairStatusColor, 8, true)
                    card(layout.evaluationBounds, when (frame.meta.eventName) {
                        "START" -> Color(0xFF122B36)
                        "PAIR_FOUND" -> Color(0xFF123820)
                        "NO_PAIR_FOUND" -> Color(0xFF381212)
                        else -> Color(0xFF2A1C12)
                    }, pairStatusColor, 7f)
                    centeredClipped("PAIR EVALUATION", Rect2D(layout.evaluationBounds.left + 8f, layout.evaluationBounds.top + 3f, layout.evaluationBounds.width - 16f, 14f), BruteSecondary, 8, true)
                    centeredFitted(pairEvaluationText(frame.meta), Rect2D(layout.evaluationBounds.left + 8f, layout.evaluationBounds.top + 18f, layout.evaluationBounds.width - 16f, layout.evaluationBounds.height - 22f), pairStatusColor, 14, true, maxLines = 3)
                    frame.meta.values.forEachIndexed { index, value ->
                        val found = frame.meta.foundPair?.let { index == it.first || index == it.second } == true
                        val isI = index == frame.meta.currentI
                        val isJ = index == frame.meta.currentJ
                        centeredFitted(when { found && isI -> "i · MATCH"; found && isJ -> "j · MATCH"; isI && isJ -> "i / j"; isI -> "i"; isJ -> "j"; else -> "" }, layout.labelBounds.getValue(index), when { found -> BruteGreen; isI -> BruteOrange; isJ -> BruteBlue; else -> BruteSecondary }, 10, true)
                        val bounds = layout.cellBounds.getValue(index)
                        card(bounds, if (found) Color(0xFF123820) else if (isI || isJ) Color(0xFF2A1C12) else BruteBg, when { found -> BruteGreen; isI -> BruteOrange; isJ -> BruteBlue; else -> BruteBorder })
                        centered(value.toString(), bounds, BrutePrimary, 14, true)
                        centeredFitted("[$index]", layout.indexBounds.getValue(index), BruteSecondary, 10)
                    }
                    centered("Current sum: ${frame.meta.currentSum ?: "—"}", layout.currentSumBounds, if (frame.meta.foundPair != null) BruteGreen else BruteBlue, 10, true)
                    frame.meta.evaluatedPairs.forEachIndexed { index, pair ->
                        val bounds = layout.historyBounds.getValue(index)
                        val currentPair = index == currentPairHistoryIndex(frame.meta)
                        val historyColor = when {
                            pair.isMatch -> BruteGreen
                            currentPair -> BruteOrange
                            else -> BruteBorder
                        }
                        card(bounds, if (pair.isMatch) Color(0xFF123820) else if (currentPair) Color(0xFF2A1C12) else Color(0xFF181824), historyColor, 5f)
                        val (indicesLine, arithmeticLine) = pairHistoryLines(pair)
                        centeredFitted(indicesLine, Rect2D(bounds.left + 8f, bounds.top + 2f, bounds.width - 16f, 16f), if (pair.isMatch) BruteGreen else BruteSecondary, 11, pair.isMatch)
                        centeredFitted(arithmeticLine, Rect2D(bounds.left + 8f, bounds.top + 20f, bounds.width - 16f, 17f), if (pair.isMatch) BruteGreen else BruteSecondary, 12, pair.isMatch)
                    }
                }
                BruteForceLesson.STRING_MATCHING -> {
                    val terminalPresentation = BruteForceSceneLayout.terminalStringMatchPresentation(frame, layout)
                    text("NAIVE PATTERN MATCHING", layout.titleBounds, BruteOrange, 10, true)
                    text("TEXT \"${frame.meta.text}\" · PATTERN \"${frame.meta.pattern}\"", layout.goalBounds, BruteSecondary, 9, true)
                    if (terminalPresentation == null) {
                        card(layout.statusBounds, when { frame.meta.fullAlignmentMatch -> Color(0xFF123820); frame.meta.mismatchOffset != null -> Color(0xFF381212); else -> Color(0xFF2A1C12) }, when { frame.meta.fullAlignmentMatch -> BruteGreen; frame.meta.mismatchOffset != null -> BruteRed; else -> BruteOrange }, 6f)
                        centered("ALIGN ${frame.meta.alignmentStart ?: "—"}", layout.statusBounds, when { frame.meta.fullAlignmentMatch -> BruteGreen; frame.meta.mismatchOffset != null -> BruteRed; else -> BruteOrange }, 8, true)
                    } else {
                        val foundMatches = frame.meta.foundMatches.isNotEmpty()
                        card(layout.statusBounds, if (foundMatches) Color(0xFF123820) else Color(0xFF381212), if (foundMatches) BruteGreen else BruteRed, 6f)
                        centeredFitted(terminalPresentation.resultText, layout.statusBounds, if (foundMatches) BruteGreen else BruteRed, 8, true)
                    }
                    frame.meta.text.forEachIndexed { index, char ->
                        val comparing = frame.meta.alignmentStart != null && frame.meta.comparisonOffset != null && index == frame.meta.alignmentStart + frame.meta.comparisonOffset
                        val previousMatch = terminalPresentation?.let { index in it.matchedTextIndices }
                            ?: frame.meta.foundMatches.any { start -> index in start until start + frame.meta.pattern.length }
                        centered(when { comparing -> "COMPARE"; previousMatch -> "MATCH"; index == frame.meta.alignmentStart -> "START"; else -> "" }, layout.labelBounds.getValue(index), when { comparing -> BruteOrange; previousMatch -> BruteGreen; else -> BruteBlue }, 8, true)
                        val bounds = layout.cellBounds.getValue(index)
                        val mismatch = comparing && frame.meta.comparisonMatches == false
                        card(bounds, when { mismatch -> Color(0xFF381212); comparing -> Color(0xFF2A1C12); previousMatch -> Color(0xFF123820); else -> BruteBg }, when { mismatch -> BruteRed; comparing -> BruteOrange; previousMatch -> BruteGreen; else -> BruteBorder })
                        centered("'$char'", bounds, BrutePrimary, 12, true)
                        centered("[$index]", layout.indexBounds.getValue(index), BruteSecondary, 9)
                    }
                    if (terminalPresentation != null) {
                        text("PATTERN", terminalPresentation.patternLabelBounds, BruteSecondary, 9, true)
                        terminalPresentation.patternCells.forEach { cell ->
                            card(cell.cellBounds, BruteBg, BruteBlue)
                            centered("'${cell.character}'", cell.cellBounds, BrutePrimary, 12, true)
                            centered("p[${cell.offset}]", cell.indexBounds, BruteSecondary, 8)
                        }
                    } else {
                        val patternBounds = BruteForceSceneLayout.patternCellBounds(frame, layout)
                        patternBounds.forEach { (offset, bounds) ->
                            val matched = offset in frame.meta.matchedOffsets
                            val mismatch = offset == frame.meta.mismatchOffset
                            card(bounds, when { mismatch -> Color(0xFF381212); matched -> Color(0xFF123820); else -> BruteBg }, when { mismatch -> BruteRed; matched -> BruteGreen; else -> BruteBlue })
                            centered("'${frame.meta.pattern[offset]}'", bounds, BrutePrimary, 12, true)
                            centered("p[$offset]", BruteForceSceneLayout.patternIndexBounds(frame, layout).getValue(offset), BruteSecondary, 8)
                        }
                        BruteForceSceneLayout.comparisonMarkerBounds(frame, layout)?.let { marker ->
                            centered(if (frame.meta.comparisonMatches == false) "≠" else "=", marker, if (frame.meta.comparisonMatches == false) BruteRed else BruteGreen, 12, true)
                        }
                    }
                }
            }
        }
    }
}

internal fun fundamentalsDisplayLabel(isMatch: Boolean, isEvaluated: Boolean, isCurrent: Boolean): String = when {
    isMatch -> "MATCH"
    isCurrent -> "TEST"
    isEvaluated -> "REJECT"
    else -> ""
}
