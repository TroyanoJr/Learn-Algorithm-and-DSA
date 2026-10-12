package com.example.learndsandalgorithm.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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

private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)
private val CodeBgColor = Color(0xFF0F0F14)
private val CodeBorderColor = Color(0xFF262634)
private val InlineCodeBgColor = Color(0xFF22222E)
private val AccentColor = Color(0xFFFA6E13)

data class CodeVariant(
    val languageId: String,
    val displayLabel: String,
    val code: String
)

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class UnorderedListItem(val text: String) : MarkdownBlock
    data class OrderedListItem(val number: Int, val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class MultiLanguageCodeBlock(val variants: List<CodeVariant>) : MarkdownBlock
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

    return groupCodeBlocks(rawBlocks)
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
                            background = InlineCodeBgColor,
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
