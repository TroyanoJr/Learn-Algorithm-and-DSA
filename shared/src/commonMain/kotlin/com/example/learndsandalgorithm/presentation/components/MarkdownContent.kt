package com.example.learndsandalgorithm.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val blocks = parseMarkdownBlocks(content)

    SelectionContainer {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            modifier = Modifier.padding(top = 8.dp)
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CodeBgColor)
                                .border(1.dp, CodeBorderColor, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                if (block.language.isNotBlank()) {
                                    Text(
                                        text = block.language.uppercase(),
                                        color = AccentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    Text(
                                        text = block.code,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = Color(0xFFE2E8F0),
                                        lineHeight = 20.sp
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

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class UnorderedListItem(val text: String) : MarkdownBlock
    data class OrderedListItem(val number: Int, val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
}

private fun parseMarkdownBlocks(rawContent: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawContent.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // Skip blank lines or custom animation markers like [animation:...] for MVP
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
            blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            continue
        }

        // Headings
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            val text = trimmed.drop(level).trim()
            blocks.add(MarkdownBlock.Heading(level = level.coerceIn(1, 3), text = text))
            i++
            continue
        }

        // Unordered List Item
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            val text = trimmed.drop(2).trim()
            blocks.add(MarkdownBlock.UnorderedListItem(text))
            i++
            continue
        }

        // Ordered List Item
        val orderedMatch = Regex("^(\\d+)\\.\\s+(.*)").find(trimmed)
        if (orderedMatch != null) {
            val number = orderedMatch.groupValues[1].toIntOrNull() ?: 1
            val text = orderedMatch.groupValues[2].trim()
            blocks.add(MarkdownBlock.OrderedListItem(number, text))
            i++
            continue
        }

        // Paragraph
        blocks.add(MarkdownBlock.Paragraph(line.trim()))
        i++
    }

    return blocks
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
                        append(" ")
                        append(text.substring(i + 1, end))
                        append(" ")
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
