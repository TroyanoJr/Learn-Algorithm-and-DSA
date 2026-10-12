package com.example.learndsandalgorithm.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

internal object HighlightColors {
    val Default = Color(0xFFE2E8F0)      // Slate light
    val Keyword = Color(0xFFC084FC)      // Soft purple
    val String = Color(0xFF4ADE80)       // Soft green
    val Number = Color(0xFFFACC15)       // Amber yellow
    val Comment = Color(0xFF6B7280)      // Cool gray
    val Literal = Color(0xFF38BDF8)      // Sky blue
    val Preprocessor = Color(0xFF2DD4BF) // Teal
    val Operator = Color(0xFF94A3B8)     // Light slate
}

private val PseudocodeKeywords = setOf(
    "IF", "THEN", "ELSE", "ELSEIF", "END", "ENDIF", "ENDFOR", "ENDWHILE",
    "FOR", "TO", "STEP", "EACH", "WHILE", "DO", "REPEAT", "UNTIL", "RETURN",
    "FUNCTION", "PROCEDURE", "PROGRAM", "ALGORITHM", "VAR", "DECLARE", "SET",
    "AND", "OR", "NOT", "IN", "IS", "MOD", "DIV", "BREAK", "CONTINUE",
    "PRINT", "OUTPUT", "INPUT", "CALL"
)

private val PseudocodeLiterals = setOf(
    "TRUE", "FALSE", "NULL", "NIL", "NONE"
)

private val CppKeywords = setOf(
    "auto", "break", "case", "catch", "class", "const", "constexpr", "continue",
    "default", "delete", "do", "double", "else", "enum", "explicit", "extern",
    "float", "for", "friend", "goto", "if", "inline", "int", "long", "namespace",
    "new", "operator", "private", "protected", "public", "return", "short",
    "signed", "sizeof", "static", "struct", "switch", "template", "this", "throw",
    "try", "typedef", "typename", "union", "unsigned", "using", "virtual", "void",
    "volatile", "while", "string", "vector", "override", "final", "char", "bool"
)

private val CppLiterals = setOf(
    "true", "false", "nullptr", "NULL"
)

private val JavaKeywords = setOf(
    "abstract", "boolean", "break", "byte", "case", "catch", "char", "class",
    "continue", "default", "do", "double", "else", "enum", "extends", "final",
    "finally", "float", "for", "if", "implements", "import", "instanceof", "int",
    "interface", "long", "native", "new", "package", "private", "protected",
    "public", "return", "short", "static", "super", "switch", "synchronized",
    "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
    "record", "var"
)

private val JavaLiterals = setOf(
    "true", "false", "null"
)

private val PythonKeywords = setOf(
    "and", "as", "assert", "async", "await", "break", "class", "continue", "def",
    "del", "elif", "else", "except", "finally", "for", "from", "global", "if",
    "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise",
    "return", "try", "while", "with", "yield", "self"
)

private val PythonLiterals = setOf(
    "True", "False", "None"
)

private val KotlinKeywords = setOf(
    "as", "break", "class", "continue", "do", "else", "for", "fun", "if", "in",
    "is", "null", "object", "package", "return", "super", "this", "throw", "try",
    "typealias", "val", "var", "when", "while", "by", "catch", "constructor",
    "delegate", "field", "file", "finally", "get", "import", "init", "param",
    "property", "receiver", "set", "setparam", "where", "actual", "abstract",
    "enum", "final", "internal", "open", "override", "private", "protected",
    "public", "sealed", "vararg", "expect", "data", "inline", "tailrec", "external",
    "annotation", "crossinline", "noinline", "out", "reified", "suspend"
)

private val KotlinLiterals = setOf(
    "true", "false", "null"
)

fun normalizeLanguageId(rawLanguage: String): String {
    val clean = rawLanguage.trim().lowercase()
    return when (clean) {
        "pseudocode", "pseudo" -> "pseudocode"
        "cpp", "c++", "c_cpp" -> "cpp"
        "java" -> "java"
        "python", "py", "python3" -> "python"
        "kotlin", "kt" -> "kotlin"
        else -> clean
    }
}

fun getDisplayLabel(normalizedLang: String, rawLang: String): String {
    return when (normalizedLang) {
        "pseudocode" -> "Pseudocode"
        "cpp" -> "C++"
        "java" -> "Java"
        "python" -> "Python"
        "kotlin" -> "Kotlin"
        else -> if (rawLang.isNotBlank()) rawLang.replaceFirstChar { it.uppercase() } else "Code"
    }
}

fun highlightCode(code: String, rawLanguage: String): AnnotatedString {
    if (code.isEmpty()) return buildAnnotatedString { }

    val lang = normalizeLanguageId(rawLanguage)
    val len = code.length

    return buildAnnotatedString {
        var i = 0

        while (i < len) {
            val ch = code[i]

            // 1. C-style Multi-line Comment /* ... */
            if ((lang == "cpp" || lang == "java" || lang == "kotlin" || lang == "pseudocode") &&
                i + 1 < len && code[i] == '/' && code[i + 1] == '*'
            ) {
                val closeIdx = code.indexOf("*/", i + 2)
                val end = if (closeIdx != -1) closeIdx + 2 else len
                withStyle(SpanStyle(color = HighlightColors.Comment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // 2. C-style Single-line Comment // ...
            if ((lang == "cpp" || lang == "java" || lang == "kotlin" || lang == "pseudocode") &&
                i + 1 < len && code[i] == '/' && code[i + 1] == '/'
            ) {
                val lineEnd = code.indexOf('\n', i + 2)
                val end = if (lineEnd != -1) lineEnd else len
                withStyle(SpanStyle(color = HighlightColors.Comment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // 3. Python Triple-Quoted Strings ("""...""" or '''...''')
            if (lang == "python" && i + 2 < len &&
                (code.startsWith("\"\"\"", i) || code.startsWith("'''", i))
            ) {
                val quote = code.substring(i, i + 3)
                val closeIdx = code.indexOf(quote, i + 3)
                val end = if (closeIdx != -1) closeIdx + 3 else len
                withStyle(SpanStyle(color = HighlightColors.String)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // 4. Hash Comments (# ...) for Python and Pseudocode
            if ((lang == "python" || lang == "pseudocode") && ch == '#') {
                val lineEnd = code.indexOf('\n', i + 1)
                val end = if (lineEnd != -1) lineEnd else len
                withStyle(SpanStyle(color = HighlightColors.Comment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // 5. Preprocessor Directives (#include, #define) for C++
            if (lang == "cpp" && ch == '#') {
                val lineEnd = code.indexOf('\n', i + 1)
                val end = if (lineEnd != -1) lineEnd else len
                withStyle(SpanStyle(color = HighlightColors.Preprocessor, fontWeight = FontWeight.Bold)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // 6. Single & Double Quoted Strings / Chars ("..." or '...')
            if (ch == '"' || ch == '\'') {
                val quote = ch
                var j = i + 1
                var escaped = false
                var end = len

                while (j < len) {
                    val c = code[j]
                    if (escaped) {
                        escaped = false
                    } else if (c == '\\') {
                        escaped = true
                    } else if (c == quote) {
                        end = j + 1
                        break
                    } else if (c == '\n' && lang != "python") {
                        end = j
                        break
                    }
                    j++
                }

                withStyle(SpanStyle(color = HighlightColors.String)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // 7. Number Literals (e.g. 42, 3.14, 0x1F, 100L)
            if (ch.isDigit()) {
                var j = i + 1
                while (j < len) {
                    val c = code[j]
                    if (c.isLetterOrDigit() || c == '.' || c == '_') {
                        j++
                    } else {
                        break
                    }
                }
                withStyle(SpanStyle(color = HighlightColors.Number)) {
                    append(code.substring(i, j))
                }
                i = j
                continue
            }

            // 8. Identifiers (Keywords, Literals, Identifiers)
            if (ch.isLetter() || ch == '_') {
                var j = i + 1
                while (j < len && (code[j].isLetterOrDigit() || code[j] == '_')) {
                    j++
                }
                val word = code.substring(i, j)

                when (lang) {
                    "pseudocode" -> {
                        val upper = word.uppercase()
                        if (upper in PseudocodeKeywords) {
                            withStyle(SpanStyle(color = HighlightColors.Keyword, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else if (upper in PseudocodeLiterals) {
                            withStyle(SpanStyle(color = HighlightColors.Literal, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else {
                            withStyle(SpanStyle(color = HighlightColors.Default)) {
                                append(word)
                            }
                        }
                    }
                    "cpp" -> {
                        if (word in CppKeywords) {
                            withStyle(SpanStyle(color = HighlightColors.Keyword, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else if (word in CppLiterals) {
                            withStyle(SpanStyle(color = HighlightColors.Literal, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else {
                            withStyle(SpanStyle(color = HighlightColors.Default)) {
                                append(word)
                            }
                        }
                    }
                    "java" -> {
                        if (word in JavaKeywords) {
                            withStyle(SpanStyle(color = HighlightColors.Keyword, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else if (word in JavaLiterals) {
                            withStyle(SpanStyle(color = HighlightColors.Literal, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else {
                            withStyle(SpanStyle(color = HighlightColors.Default)) {
                                append(word)
                            }
                        }
                    }
                    "python" -> {
                        if (word in PythonKeywords) {
                            withStyle(SpanStyle(color = HighlightColors.Keyword, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else if (word in PythonLiterals) {
                            withStyle(SpanStyle(color = HighlightColors.Literal, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else {
                            withStyle(SpanStyle(color = HighlightColors.Default)) {
                                append(word)
                            }
                        }
                    }
                    else -> { // kotlin / fallback
                        if (word in KotlinKeywords) {
                            withStyle(SpanStyle(color = HighlightColors.Keyword, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else if (word in KotlinLiterals) {
                            withStyle(SpanStyle(color = HighlightColors.Literal, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else {
                            withStyle(SpanStyle(color = HighlightColors.Default)) {
                                append(word)
                            }
                        }
                    }
                }
                i = j
                continue
            }

            // 9. Operators & Punctuation
            if (ch in "+-*/=<>!&|%^~:;,.") {
                withStyle(SpanStyle(color = HighlightColors.Operator)) {
                    append(ch.toString())
                }
                i++
                continue
            }

            // 10. Default (Whitespace / Brackets / Unhandled chars)
            withStyle(SpanStyle(color = HighlightColors.Default)) {
                append(ch.toString())
            }
            i++
        }
    }
}
