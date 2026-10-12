package com.example.learndsandalgorithm.presentation.components

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CodeHighlighterTest {

    @Test
    fun test1ValidGroupAllFourLanguages() {
        val markdown = """
            ```pseudocode
            FUNCTION linearSearch(arr, target)
                RETURN -1
            ```
            ```cpp
            int linearSearch(vector<int> arr, int target) {
                return -1;
            }
            ```
            ```java
            public int linearSearch(int[] arr, int target) {
                return -1;
            }
            ```
            ```python
            def linear_search(arr, target):
                return -1
            ```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(1, blocks.size, "Should group 4 consecutive supported code blocks into 1 MultiLanguageCodeBlock")

        val multiBlock = assertIs<MarkdownBlock.MultiLanguageCodeBlock>(blocks[0])
        assertEquals(4, multiBlock.variants.size, "Should contain exactly 4 language variants")

        val languages = multiBlock.variants.map { it.languageId }
        assertEquals(listOf("pseudocode", "cpp", "java", "python"), languages)

        val labels = multiBlock.variants.map { it.displayLabel }
        assertEquals(listOf("Pseudocode", "C++", "Java", "Python"), labels)
    }

    @Test
    fun test2GroupContainingSubsetOfSupportedLanguages() {
        val markdown = """
            ```cpp
            int binarySearch();
            ```
            ```python
            def binary_search():
                pass
            ```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(1, blocks.size, "Should group 2 consecutive supported language blocks")

        val multiBlock = assertIs<MarkdownBlock.MultiLanguageCodeBlock>(blocks[0])
        assertEquals(2, multiBlock.variants.size)
        assertEquals(listOf("cpp", "python"), multiBlock.variants.map { it.languageId })
        assertEquals(listOf("C++", "Python"), multiBlock.variants.map { it.displayLabel })
    }

    @Test
    fun test3StandaloneCodeBlockRemainsStandalone() {
        val markdown = """
            # Array Traversal

            Here is how you iterate over an array in pseudocode:

            ```pseudocode
            FOR i FROM 0 TO n - 1 DO
                PRINT arr[i]
            END FOR
            ```

            This algorithm takes O(n) linear time.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        val codeBlocks = blocks.filterIsInstance<MarkdownBlock.CodeBlock>()
        val multiBlocks = blocks.filterIsInstance<MarkdownBlock.MultiLanguageCodeBlock>()

        assertEquals(1, codeBlocks.size, "Standalone block should remain a single CodeBlock")
        assertEquals(0, multiBlocks.size, "Standalone block should not create a MultiLanguageCodeBlock")
        assertEquals("pseudocode", codeBlocks[0].language)
    }

    @Test
    fun test4UnsupportedLanguageFencesRemainIntact() {
        val markdown = """
            ```rust
            fn main() {
                println!("Hello Rust");
            }
            ```
            ```sql
            SELECT * FROM users;
            ```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(2, blocks.size, "Unsupported language fences should remain 2 separate standalone blocks")

        val b1 = assertIs<MarkdownBlock.CodeBlock>(blocks[0])
        assertEquals("rust", b1.language)
        assertTrue(b1.code.contains("println!"))

        val b2 = assertIs<MarkdownBlock.CodeBlock>(blocks[1])
        assertEquals("sql", b2.language)
        assertTrue(b2.code.contains("SELECT"))
    }

    @Test
    fun test5DuplicateLanguageVariantsNotSilentlyLost() {
        val markdown = """
            ```python
            # Example 1
            x = 10
            ```
            ```python
            # Example 2
            y = 20
            ```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(2, blocks.size, "Duplicate language blocks must not merge; must yield 2 separate CodeBlocks")

        val b1 = assertIs<MarkdownBlock.CodeBlock>(blocks[0])
        val b2 = assertIs<MarkdownBlock.CodeBlock>(blocks[1])

        assertEquals("python", b1.language)
        assertTrue(b1.code.contains("x = 10"))

        assertEquals("python", b2.language)
        assertTrue(b2.code.contains("y = 20"))
    }

    @Test
    fun test6CorrectAliasNormalization() {
        assertEquals("pseudocode", normalizeLanguageId("pseudo"))
        assertEquals("pseudocode", normalizeLanguageId("Pseudocode"))
        assertEquals("cpp", normalizeLanguageId("c++"))
        assertEquals("cpp", normalizeLanguageId("C_CPP"))
        assertEquals("python", normalizeLanguageId("py"))
        assertEquals("python", normalizeLanguageId("python3"))
        assertEquals("java", normalizeLanguageId("Java"))
        assertEquals("kotlin", normalizeLanguageId("kt"))

        assertEquals("Pseudocode", getDisplayLabel("pseudocode", "pseudo"))
        assertEquals("C++", getDisplayLabel("cpp", "c++"))
        assertEquals("Java", getDisplayLabel("java", "java"))
        assertEquals("Python", getDisplayLabel("python", "py"))
    }

    @Test
    fun test7KeywordsStringsNumbersCommentsHighlighting() {
        val code = """
            // This is a comment
            int x = 42;
            string s = "Hello";
            boolean flag = true;
        """.trimIndent()

        val highlighted = highlightCode(code, "java")
        assertEquals(code, highlighted.text, "Highlighted AnnotatedString text must match original code exactly")

        val spanStyles = highlighted.spanStyles
        assertTrue(spanStyles.isNotEmpty(), "Highlighting should produce SpanStyles")

        // Verify Comment Style
        val commentStyle = spanStyles.find { it.item.color == HighlightColors.Comment }
        assertTrue(commentStyle != null, "Comments must be highlighted with Comment color")
        assertEquals(FontStyle.Italic, commentStyle.item.fontStyle)

        // Verify Keyword Style (int, boolean)
        val keywordStyle = spanStyles.find { it.item.color == HighlightColors.Keyword }
        assertTrue(keywordStyle != null, "Keywords must be highlighted with Keyword color")
        assertEquals(FontWeight.Bold, keywordStyle.item.fontWeight)

        // Verify String Style ("Hello")
        val stringStyle = spanStyles.find { it.item.color == HighlightColors.String }
        assertTrue(stringStyle != null, "Strings must be highlighted with String color")

        // Verify Number Style (42)
        val numberStyle = spanStyles.find { it.item.color == HighlightColors.Number }
        assertTrue(numberStyle != null, "Numbers must be highlighted with Number color")

        // Verify Literal Style (true)
        val literalStyle = spanStyles.find { it.item.color == HighlightColors.Literal }
        assertTrue(literalStyle != null, "Literals (true) must be highlighted with Literal color")
    }

    @Test
    fun test8KeywordsInsideStringsAndCommentsNotHighlightedAsKeywords() {
        val codeWithKeywordInString = """
            String msg = "IF THEN ELSE for while return";
            // IF THEN return in comment
        """.trimIndent()

        val highlighted = highlightCode(codeWithKeywordInString, "java")
        assertEquals(codeWithKeywordInString, highlighted.text)

        // Search for string region "IF THEN ELSE for while return"
        val stringStart = codeWithKeywordInString.indexOf("\"IF THEN")
        val stringEnd = codeWithKeywordInString.indexOf("\";") + 1

        val keywordSpansInString = highlighted.spanStyles.filter { span ->
            span.item.color == HighlightColors.Keyword &&
                    span.start >= stringStart && span.end <= stringEnd
        }

        assertTrue(keywordSpansInString.isEmpty(), "Keywords inside string literals must NOT be highlighted as keywords")

        // Search for comment region "// IF THEN return in comment"
        val commentStart = codeWithKeywordInString.indexOf("// IF")
        val commentEnd = codeWithKeywordInString.length

        val keywordSpansInComment = highlighted.spanStyles.filter { span ->
            span.item.color == HighlightColors.Keyword &&
                    span.start >= commentStart && span.end <= commentEnd
        }

        assertTrue(keywordSpansInComment.isEmpty(), "Keywords inside comments must NOT be highlighted as keywords")
    }

    @Test
    fun test9MultilineCodePreservingContentAndFormatting() {
        val code = """
            def bubble_sort(arr):
                n = len(arr)
                for i in range(n):
                    swapped = False
                    for j in range(0, n - i - 1):
                        if arr[j] > arr[j + 1]:
                            arr[j], arr[j + 1] = arr[j + 1], arr[j]
                            swapped = True
                    if not swapped:
                        break
        """.trimIndent()

        val highlighted = highlightCode(code, "python")

        assertEquals(code, highlighted.text, "Multiline code formatting, spaces, and line breaks must be preserved 100%")
        assertTrue(highlighted.text.contains("    for i in range(n):"))
    }

    @Test
    fun test10EmptyOrUnfamiliarCodeNotCausingFailures() {
        val emptyResult = highlightCode("", "cpp")
        assertEquals("", emptyResult.text)

        val unfamiliarCode = "SELECT * FROM users WHERE id = 100; ### %% $$$"
        val unfamiliarResult = highlightCode(unfamiliarCode, "unknown_lang_xyz")
        assertEquals(unfamiliarCode, unfamiliarResult.text)
    }

    @Test
    fun testCppIncludeVectorPreprocessorStylingNotComment() {
        val code = "#include <vector>"
        val highlighted = highlightCode(code, "cpp")

        assertEquals(code, highlighted.text)

        val preprocessorSpan = highlighted.spanStyles.find { it.item.color == HighlightColors.Preprocessor }
        assertTrue(preprocessorSpan != null, "#include <vector> must receive Preprocessor style")

        val commentSpan = highlighted.spanStyles.find { it.item.color == HighlightColors.Comment }
        assertTrue(commentSpan == null, "#include <vector> must NOT receive Comment style")
    }

    @Test
    fun testEscapedQuotesInsideStringsDoNotTerminateStringPrematurely() {
        val code = """String msg = "Hello \"escaped\" world";"""
        val highlighted = highlightCode(code, "java")

        assertEquals(code, highlighted.text)

        val stringStart = code.indexOf("\"Hello")
        val stringEnd = code.length - 1

        val stringSpan = highlighted.spanStyles.find {
            it.item.color == HighlightColors.String && it.start == stringStart && it.end == stringEnd
        }

        assertTrue(stringSpan != null, "String with escaped quotes must be tokenized as a single unbroken String span")
    }

    @Test
    fun testPythonTripleQuotedStringsPreserveStringHighlightingWithHash() {
        val code = """s = ""${'"'}This is a # comment inside triple quotes""${'"'}"""
        val highlighted = highlightCode(code, "python")

        assertEquals(code, highlighted.text)

        val commentSpan = highlighted.spanStyles.find { it.item.color == HighlightColors.Comment }
        assertTrue(commentSpan == null, "# inside Python triple-quoted string must NOT be highlighted as a comment")

        val stringSpan = highlighted.spanStyles.find { it.item.color == HighlightColors.String }
        assertTrue(stringSpan != null, "Triple-quoted string containing # must be highlighted as String")
    }

    @Test
    fun testPseudocodeCaseInsensitiveKeywordsPreserveSourceCasing() {
        val code = """
            if x > 0 then
                IF y > 0 THEN
                    If z > 0 Then
        """.trimIndent()

        val highlighted = highlightCode(code, "pseudocode")

        assertEquals(code, highlighted.text, "Original text casing must be preserved 100%")

        val keywordSpans = highlighted.spanStyles.filter { it.item.color == HighlightColors.Keyword }
        assertTrue(keywordSpans.size >= 6, "All 6 occurrences of if/IF/If/then/THEN/Then must receive Keyword style")
    }
}
