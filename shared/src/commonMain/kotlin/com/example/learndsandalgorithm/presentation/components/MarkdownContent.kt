package com.example.learndsandalgorithm.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextAlign

private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)
private val CodeBgColor = Color(0xFF0F0F14)
  private val CodeBorderColor = Color(0xFF262634)
private val AccentColor = Color(0xFFFA6E13)
private val GreenAccent = Color(0xFF10B981)

data class CodeVariant(
    val languageId: String,
    val displayLabel: String,
    val code: String
)

data class TraceStepData(
    val actionLabel: String,
    val stateSnapshot: String,
    val detailNote: String? = null
)

enum class TableColumnAlignment {
    LEFT, CENTER, RIGHT
}

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class UnorderedListItem(val text: String) : MarkdownBlock
    data class OrderedListItem(val number: Int, val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class MultiLanguageCodeBlock(val variants: List<CodeVariant>) : MarkdownBlock
    data class TableBlock(
        val headers: List<String>,
        val rows: List<List<String>>,
        val alignments: List<TableColumnAlignment>
    ) : MarkdownBlock
    data class TraceSequenceBlock(
        val steps: List<TraceStepData>
    ) : MarkdownBlock
}

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    SelectionContainer {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            blocks.forEach { block ->
                when (block) {
                    is MarkdownBlock.Heading -> {
                        val (fontSize, fontWeight, color) = when (block.level) {
                            1 -> Triple(24.sp, FontWeight.Bold, TextPrimary)
                            2 -> Triple(20.sp, FontWeight.Bold, TextPrimary)
                            else -> Triple(17.sp, FontWeight.SemiBold, TextPrimary)
                        }
                        Text(
                            text = parseInlineMarkdown(block.text),
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            color = color,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    is MarkdownBlock.Paragraph -> {
                        Text(
                            text = parseInlineMarkdown(block.text),
                            fontSize = 15.sp,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }

                    is MarkdownBlock.UnorderedListItem -> {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "•",
                                color = AccentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = parseInlineMarkdown(block.text),
                                fontSize = 15.sp,
                                color = TextSecondary,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    is MarkdownBlock.OrderedListItem -> {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${block.number}.",
                                color = AccentColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = parseInlineMarkdown(block.text),
                                fontSize = 15.sp,
                                color = TextSecondary,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    is MarkdownBlock.CodeBlock -> {
                        RenderCodeBlock(
                            language = block.language,
                            code = block.code
                        )
                    }

                    is MarkdownBlock.MultiLanguageCodeBlock -> {
                        RenderMultiLanguageCodeBlock(
                            variants = block.variants
                        )
                    }

                    is MarkdownBlock.TableBlock -> {
                        RenderTableBlock(
                            headers = block.headers,
                            rows = block.rows,
                            alignments = block.alignments
                        )
                    }

                    is MarkdownBlock.TraceSequenceBlock -> {
                        RenderTraceSequenceBlock(
                            steps = block.steps
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderCodeBlock(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val normLang = normalizeLanguageId(language)
    val displayLabel = getDisplayLabel(normLang, language)
    val highlightedCode = remember(code, language) {
        highlightCode(code, language)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeBgColor)
            .border(1.dp, CodeBorderColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            if (language.isNotBlank()) {
                Text(
                    text = displayLabel.uppercase(),
                    color = AccentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Text(
                    text = highlightedCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun RenderMultiLanguageCodeBlock(
    variants: List<CodeVariant>,
    modifier: Modifier = Modifier
) {
    val preferredOrder = listOf("pseudocode", "cpp", "java", "python")
    val defaultLang = preferredOrder.firstOrNull { pref ->
        variants.any { it.languageId == pref }
    } ?: variants.first().languageId

    var selectedLangId by rememberSaveable { mutableStateOf(defaultLang) }

    val activeVariant = variants.find { it.languageId == selectedLangId } ?: variants.first()

    val highlightedCode = remember(activeVariant.code, activeVariant.languageId) {
        highlightCode(activeVariant.code, activeVariant.languageId)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeBgColor)
            .border(1.dp, CodeBorderColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            // Tab Row Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                variants.forEach { variant ->
                    val isSelected = variant.languageId == activeVariant.languageId
                    val tabBg = if (isSelected) AccentColor else Color(0xFF1E1E28)
                    val tabTextColor = if (isSelected) Color(0xFF111115) else TextSecondary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tabBg)
                            .clickable { selectedLangId = variant.languageId }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = variant.displayLabel,
                            color = tabTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Text(
                    text = highlightedCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun RenderTableBlock(
    headers: List<String>,
    rows: List<List<String>>,
    alignments: List<TableColumnAlignment>,
    modifier: Modifier = Modifier
) {
    val numCols = headers.size.coerceAtLeast(1)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val availableWidth = maxWidth
        val minColWidth = 85.dp
        val totalPaddingAndSpacing = 32.dp + 12.dp * (numCols - 1)
        val minTotalTableWidth = (minColWidth * numCols) + totalPaddingAndSpacing

        val fitsInViewport = minTotalTableWidth <= availableWidth

        val scrollState = rememberScrollState()

        Surface(
            color = CodeBgColor,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CodeBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!fitsInViewport) {
                            Modifier.horizontalScroll(scrollState)
                        } else {
                            Modifier
                        }
                    )
                    .padding(10.dp)
            ) {
                Column(
                    modifier = if (fitsInViewport) Modifier.fillMaxWidth() else Modifier,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .then(if (fitsInViewport) Modifier.fillMaxWidth() else Modifier)
                            .background(Color(0xFF1E1E28), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        headers.forEachIndexed { colIndex, header ->
                            val align = alignments.getOrElse(colIndex) { TableColumnAlignment.LEFT }
                            val textAlign = when (align) {
                                TableColumnAlignment.LEFT -> TextAlign.Start
                                TableColumnAlignment.CENTER -> TextAlign.Center
                                TableColumnAlignment.RIGHT -> TextAlign.End
                            }
                            val boxAlignment = when (align) {
                                TableColumnAlignment.LEFT -> Alignment.CenterStart
                                TableColumnAlignment.CENTER -> Alignment.Center
                                TableColumnAlignment.RIGHT -> Alignment.CenterEnd
                            }

                            val cellModifier = if (fitsInViewport) {
                                Modifier.weight(1f)
                            } else {
                                Modifier.width(minColWidth)
                            }

                            Box(
                                modifier = cellModifier,
                                contentAlignment = boxAlignment
                            ) {
                                Text(
                                    text = parseInlineMarkdown(header),
                                    color = AccentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = textAlign
                                )
                            }
                        }
                    }

                    // Divider
                    Spacer(modifier = Modifier.height(2.dp))

                    // Data Rows
                    rows.forEachIndexed { rowIndex, row ->
                        val rowBg = if (rowIndex % 2 == 1) Color(0xFF13131C) else Color.Transparent
                        Row(
                            modifier = Modifier
                                .then(if (fitsInViewport) Modifier.fillMaxWidth() else Modifier)
                                .clip(RoundedCornerShape(6.dp))
                                .background(rowBg)
                                .padding(vertical = 6.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEachIndexed { colIndex, cellText ->
                                val align = alignments.getOrElse(colIndex) { TableColumnAlignment.LEFT }
                                val textAlign = when (align) {
                                    TableColumnAlignment.LEFT -> TextAlign.Start
                                    TableColumnAlignment.CENTER -> TextAlign.Center
                                    TableColumnAlignment.RIGHT -> TextAlign.End
                                }
                                val boxAlignment = when (align) {
                                    TableColumnAlignment.LEFT -> Alignment.CenterStart
                                    TableColumnAlignment.CENTER -> Alignment.Center
                                    TableColumnAlignment.RIGHT -> Alignment.CenterEnd
                                }

                                val cellModifier = if (fitsInViewport) {
                                    Modifier.weight(1f)
                                } else {
                                    Modifier.width(minColWidth)
                                }

                                Box(
                                    modifier = cellModifier,
                                    contentAlignment = boxAlignment
                                ) {
                                    Text(
                                        text = parseInlineMarkdown(cellText),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        textAlign = textAlign
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderTraceSequenceBlock(
    steps: List<TraceStepData>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeBgColor)
            .border(1.dp, CodeBorderColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "EXECUTION TRACE SEQUENCE",
                color = AccentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            steps.forEachIndexed { stepIdx, step ->
                val isResultStep = step.detailNote?.contains("result:", ignoreCase = true) == true ||
                        step.detailNote?.contains("popped", ignoreCase = true) == true ||
                        step.detailNote?.contains("stop", ignoreCase = true) == true ||
                        step.detailNote?.contains("complete", ignoreCase = true) == true ||
                        step.detailNote?.contains("sorted", ignoreCase = true) == true

                Surface(
                    color = Color(0xFF13131C),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isResultStep) GreenAccent.copy(alpha = 0.5f) else CodeBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color(0xFF2A1C12),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, AccentColor)
                            ) {
                                Text(
                                    text = "STEP ${stepIdx + 1}",
                                    color = AccentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = parseInlineMarkdown(step.actionLabel),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F0F14))
                                .border(1.dp, Color(0xFF22222E), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = parseInlineMarkdown("State: `${step.stateSnapshot}`"),
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (!step.detailNote.isNullOrBlank()) {
                            Text(
                                text = parseInlineMarkdown(step.detailNote),
                                color = if (isResultStep) GreenAccent else TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun splitTableCells(line: String): List<String> {
    var str = line.trim()
    if (str.startsWith("|")) str = str.substring(1)
    if (str.endsWith("|") && !str.endsWith("\\|")) str = str.substring(0, str.length - 1)

    val cells = mutableListOf<String>()
    val current = StringBuilder()
    var i = 0
    while (i < str.length) {
        if (str[i] == '\\' && i + 1 < str.length && str[i + 1] == '|') {
            current.append('|')
            i += 2
        } else if (str[i] == '|') {
            cells.add(current.toString().trim())
            current.clear()
            i++
        } else {
            current.append(str[i])
            i++
        }
    }
    cells.add(current.toString().trim())
    return cells
}

fun isTableSeparatorRow(line: String): Boolean {
    val trimmedLine = line.trim()
    if (!trimmedLine.contains("|") && !trimmedLine.contains("-")) return false
    val cells = splitTableCells(trimmedLine)
    if (cells.isEmpty()) return false
    return cells.all { cell ->
        val trimmed = cell.trim()
        trimmed.isNotEmpty() && trimmed.all { it == '-' || it == ':' } && trimmed.any { it == '-' }
    }
}

fun parseTableAlignments(separatorLine: String): List<TableColumnAlignment> {
    val cells = splitTableCells(separatorLine)
    return cells.map { cell ->
        val trimmed = cell.trim()
        val startsWithColon = trimmed.startsWith(":")
        val endsWithColon = trimmed.endsWith(":")
        when {
            startsWithColon && endsWithColon -> TableColumnAlignment.CENTER
            endsWithColon -> TableColumnAlignment.RIGHT
            else -> TableColumnAlignment.LEFT
        }
    }
}

private val SupportedGroupLanguages = setOf("pseudocode", "cpp", "java", "python")

fun parseMarkdownBlocks(rawContent: String): List<MarkdownBlock> {
    val rawBlocks = mutableListOf<MarkdownBlock>()
    val lines = rawContent.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.isEmpty() || trimmed.startsWith("[animation:")) {
            i++
            continue
        }

        // Table Block
        if (i + 1 < lines.size && isTableSeparatorRow(lines[i + 1])) {
            val headers = splitTableCells(trimmed)
            if (headers.isNotEmpty()) {
                val alignments = parseTableAlignments(lines[i + 1])
                i += 2
                val rows = mutableListOf<List<String>>()

                while (i < lines.size) {
                    val rowLine = lines[i].trim()
                    if (rowLine.isEmpty() || rowLine.startsWith("```") || rowLine.startsWith("#") || isTableSeparatorRow(rowLine)) {
                        break
                    }
                    if (!rowLine.contains("|")) {
                        break
                    }
                    val rawCells = splitTableCells(rowLine)
                    val adjustedCells = ArrayList<String>(headers.size)
                    for (colIdx in 0 until headers.size) {
                        adjustedCells.add(rawCells.getOrNull(colIdx) ?: "")
                    }
                    rows.add(adjustedCells)
                    i++
                }

                rawBlocks.add(
                    MarkdownBlock.TableBlock(
                        headers = headers,
                        rows = rows,
                        alignments = alignments
                    )
                )
                continue
            }
        }

        // Fenced Code Block
        if (trimmed.startsWith("```")) {
            val language = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size) i++ // Consume closing ```
            rawBlocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            continue
        }

        // Headings
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            val text = trimmed.drop(level).trim()
            rawBlocks.add(MarkdownBlock.Heading(level = level.coerceIn(1, 3), text = text))
            i++
            continue
        }

        // Unordered List Item
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            val text = trimmed.drop(2).trim()
            rawBlocks.add(MarkdownBlock.UnorderedListItem(text))
            i++
            continue
        }

        // Ordered List Item
        val orderedMatch = Regex("^(\\d+)\\.\\s+(.*)").find(trimmed)
        if (orderedMatch != null) {
            val number = orderedMatch.groupValues[1].toIntOrNull() ?: 1
            val text = orderedMatch.groupValues[2].trim()
            rawBlocks.add(MarkdownBlock.OrderedListItem(number, text))
            i++
            continue
        }

        // Paragraph
        rawBlocks.add(MarkdownBlock.Paragraph(line.trim()))
        i++
    }

    return groupTraceSequenceBlocks(groupCodeBlocks(rawBlocks))
}

private fun beforeStoreAction(beforeQueue: String): String {
    val clean = beforeQueue.replace("**", "").replace("*", "").replace("`", "").trim()
    if (clean.contains(";")) {
        return clean.split(";", limit = 2)[0].trim()
    }
    if (clean.contains(".")) {
        return clean.split(".", limit = 2)[0].trim()
    }
    return clean
}

private fun beforeStoreDetail(beforeQueue: String): String? {
    val clean = beforeQueue.replace("**", "").replace("*", "").replace("`", "").trim()
    if (clean.contains(";")) {
        return clean.split(";", limit = 2)[1].trim()
    }
    if (clean.contains(".") && !clean.endsWith(".")) {
        val parts = clean.split(".", limit = 2)
        val extra = parts[1].trim()
        if (extra.isNotEmpty()) return extra
    }
    return null
}

fun parseTraceStepItem(itemText: String): TraceStepData? {
    val trimmed = itemText.trim().removePrefix("-").removePrefix("*").trim()

    // 1. Check for Queue: [...] pattern (Kahn's algorithm)
    if (trimmed.contains("Queue:")) {
        val queueIdx = trimmed.indexOf("Queue:")
        val beforeQueue = trimmed.substring(0, queueIdx).trim().removeSuffix(".").removeSuffix(";").trim()
        val queuePart = trimmed.substring(queueIdx + "Queue:".length).replace("`", "").trim().removeSuffix(".")

        if (queuePart.startsWith("[") && queuePart.endsWith("]")) {
            val action = beforeStoreAction(beforeQueue)
            val detail = beforeStoreDetail(beforeQueue)
            if (action.isNotEmpty()) {
                return TraceStepData(
                    actionLabel = action,
                    stateSnapshot = queuePart,
                    detailNote = detail
                )
            }
        }
    }

    // 2. Check for Output: or Result: pattern (e.g. final step of Kahn's: "Remove E. Output: A, B, C, D, E.")
    if (trimmed.contains("Output:") || trimmed.contains("Result:")) {
        val splitKey = if (trimmed.contains("Output:")) "Output:" else "Result:"
        val parts = trimmed.split(splitKey, limit = 2)
        val action = parts[0].trim().removeSuffix(".").removeSuffix(";").replace("**", "").replace("*", "").replace("`", "").trim()
        val state = parts[1].trim().removeSuffix(".").replace("`", "").trim()
        if (action.isNotEmpty() && state.isNotEmpty()) {
            return TraceStepData(
                actionLabel = action,
                stateSnapshot = state,
                detailNote = "Final Output"
            )
        }
    }

    val firstOpen = trimmed.indexOf('[')
    if (firstOpen == -1) return null

    val lastClose = trimmed.indexOf(']', firstOpen)
    if (lastClose == -1) return null

    val candidateSnapshot = trimmed.substring(firstOpen, lastClose + 1).trim()
    val inside = candidateSnapshot.substring(1, candidateSnapshot.length - 1).trim()
    if (inside.contains("\n") || inside.contains("#")) return null

    val rawBefore = trimmed.substring(0, firstOpen).trim()
    val rawAfter = trimmed.substring(lastClose + 1).trim()

    if (rawBefore.isEmpty()) return null

    var action = rawBefore
    var detail: String? = rawAfter.ifBlank { null }

    if (action.contains("—")) {
        val parts = action.split("—", limit = 2)
        action = parts[0].trim()
        val extra = parts[1].trim()
        if (extra.isNotEmpty()) {
            detail = if (detail != null) "$extra; $detail" else extra
        }
    } else if (action.contains(":") && !action.startsWith("http")) {
        val parts = action.split(":", limit = 2)
        action = parts[0].trim()
        val extra = parts[1].trim()
        if (extra.isNotEmpty()) {
            detail = if (detail != null) "$extra; $detail" else extra
        }
    }

    action = action.replace("**", "").replace("*", "").replace("`", "").trim()
    action = action.removeSuffix(":").removeSuffix("—").trim()

    if (action.isEmpty()) return null

    var cleanDetail = detail?.trim()
    if (cleanDetail != null) {
        while (cleanDetail!!.startsWith(".") || cleanDetail.startsWith("`") || cleanDetail.startsWith(";") || cleanDetail.startsWith("—") || cleanDetail.startsWith(",")) {
            cleanDetail = cleanDetail.removePrefix(".").removePrefix("`").removePrefix(";").removePrefix("—").removePrefix(",").trim()
        }
        if (cleanDetail.isEmpty()) cleanDetail = null
    }

    return TraceStepData(
        actionLabel = action,
        stateSnapshot = candidateSnapshot,
        detailNote = cleanDetail
    )
}

private fun groupTraceSequenceBlocks(blocks: List<MarkdownBlock>): List<MarkdownBlock> {
    val result = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < blocks.size) {
        if (blocks[i] is MarkdownBlock.UnorderedListItem) {
            val group = mutableListOf<MarkdownBlock.UnorderedListItem>()
            var j = i
            while (j < blocks.size && blocks[j] is MarkdownBlock.UnorderedListItem) {
                group.add(blocks[j] as MarkdownBlock.UnorderedListItem)
                j++
            }

            val traceSteps = group.mapNotNull { parseTraceStepItem(it.text) }

            if (group.size >= 2 && traceSteps.size == group.size) {
                result.add(MarkdownBlock.TraceSequenceBlock(traceSteps))
                i = j
            } else {
                result.add(blocks[i])
                i++
            }
        } else {
            result.add(blocks[i])
            i++
        }
    }

    return result
}

private fun groupCodeBlocks(blocks: List<MarkdownBlock>): List<MarkdownBlock> {
    val result = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < blocks.size) {
        val current = blocks[i]
        if (current is MarkdownBlock.CodeBlock) {
            val group = mutableListOf<MarkdownBlock.CodeBlock>()
            var j = i
            val seenLanguages = mutableSetOf<String>()

            while (j < blocks.size && blocks[j] is MarkdownBlock.CodeBlock) {
                val cb = blocks[j] as MarkdownBlock.CodeBlock
                val normLang = normalizeLanguageId(cb.language)

                if (normLang in SupportedGroupLanguages && normLang !in seenLanguages) {
                    seenLanguages.add(normLang)
                    group.add(cb)
                    j++
                } else {
                    break
                }
            }

            if (group.size >= 2) {
                val variants = group.map { cb ->
                    val norm = normalizeLanguageId(cb.language)
                    CodeVariant(
                        languageId = norm,
                        displayLabel = getDisplayLabel(norm, cb.language),
                        code = cb.code
                    )
                }
                result.add(MarkdownBlock.MultiLanguageCodeBlock(variants))
                i = j
            } else {
                result.add(current)
                i++
            }
        } else {
            result.add(current)
            i++
        }
    }

    return result
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            // Inline code `code`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp
                        )
                    ) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }

            // Bold **text**
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }

            // Italic *text*
            if (text[i] == '*') {
                val end = text.indexOf('*', i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }

            // Regular char
            append(text[i])
            i++
        }
    }
}
