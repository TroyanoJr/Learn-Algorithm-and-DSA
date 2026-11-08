package com.example.learndsandalgorithm.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MarkdownContentTest {

    @Test
    fun testParseStandardGfmTable() {
        val markdown = """
            # BFS Trace

            | Step | Action | Queue | Visited |
            |---|---|---|---|
            | 1 | Enqueue A | `[A]` | `{A}` |
            | 2 | Remove A; enqueue B, C | `[B,C]` | `{A,B,C}` |

            Done.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Heading)
        assertTrue(blocks[1] is MarkdownBlock.TableBlock)

        val table = blocks[1] as MarkdownBlock.TableBlock
        assertEquals(listOf("Step", "Action", "Queue", "Visited"), table.headers)
        assertEquals(2, table.rows.size)
        assertEquals(listOf("1", "Enqueue A", "`[A]`", "`{A}`"), table.rows[0])
        assertEquals(listOf("2", "Remove A; enqueue B, C", "`[B,C]`", "`{A,B,C}`"), table.rows[1])

        assertTrue(blocks[2] is MarkdownBlock.Paragraph)
    }

    @Test
    fun testTableColumnAlignments() {
        val markdown = """
            | Left | Center | Right | Default |
            |:---|:---:|---:|---|
            | L1 | C1 | R1 | D1 |
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.TableBlock)

        val table = blocks[0] as MarkdownBlock.TableBlock
        assertEquals(
            listOf(
                TableColumnAlignment.LEFT,
                TableColumnAlignment.CENTER,
                TableColumnAlignment.RIGHT,
                TableColumnAlignment.LEFT
            ),
            table.alignments
        )
    }

    @Test
    fun testTableWithoutOuterPipes() {
        val markdown = """
            Header 1 | Header 2
            ---|---
            Val 1 | Val 2
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.TableBlock)

        val table = blocks[0] as MarkdownBlock.TableBlock
        assertEquals(listOf("Header 1", "Header 2"), table.headers)
        assertEquals(1, table.rows.size)
        assertEquals(listOf("Val 1", "Val 2"), table.rows[0])
    }

    @Test
    fun testTableWithEscapedPipesAndEmptyCells() {
        val markdown = """
            | Item | Description | Code |
            |---|---|---|
            | A | Cell with \| pipe | |
            | B | Second cell | `foo()` |
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.TableBlock)

        val table = blocks[0] as MarkdownBlock.TableBlock
        assertEquals(listOf("A", "Cell with | pipe", ""), table.rows[0])
        assertEquals(listOf("B", "Second cell", "`foo()`"), table.rows[1])
    }

    @Test
    fun testMalformedTableFallbackToParagraph() {
        val markdown = """
            This is a normal paragraph with a | pipe character.
            This line is not a separator line.

            Another paragraph.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Paragraph)
        assertTrue(blocks[1] is MarkdownBlock.Paragraph)
        assertTrue(blocks[2] is MarkdownBlock.Paragraph)
    }

    @Test
    fun testCodeBlockWithPipesNotParsedAsTable() {
        val markdown = """
            ```cpp
            if (a | b) {
                // bitwise OR
            }
            ```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.CodeBlock)
    }

    @Test
    fun testStackTraceSequenceParsing() {
        val markdown = """
            - **Start** — Stack (bottom → top): `[]`; result: —.
            - **Push A** — Stack: `[A]`; result: —.
            - **Push B** — Stack: `[A, B]`; result: —.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.TraceSequenceBlock)

        val trace = blocks[0] as MarkdownBlock.TraceSequenceBlock
        assertEquals(3, trace.steps.size)
        assertEquals("Start", trace.steps[0].actionLabel)
        assertEquals("[]", trace.steps[0].stateSnapshot)
        assertEquals("Push A", trace.steps[1].actionLabel)
        assertEquals("[A]", trace.steps[1].stateSnapshot)
        assertEquals("Push B", trace.steps[2].actionLabel)
        assertEquals("[A, B]", trace.steps[2].stateSnapshot)
    }

    @Test
    fun testHeapTraceSequenceParsing() {
        val markdown = """
            - Append at the next position: `[2, 5, 3, 9, 7, 8, 1]`. The new value is at index 6; parent is index 2, value 3.
            - Since `1 < 3`, swap: `[2, 5, 1, 9, 7, 8, 3]`. At index 2, parent is index 0, value 2.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.TraceSequenceBlock)

        val trace = blocks[0] as MarkdownBlock.TraceSequenceBlock
        assertEquals(2, trace.steps.size)
        assertEquals("Append at the next position", trace.steps[0].actionLabel)
        assertEquals("[2, 5, 3, 9, 7, 8, 1]", trace.steps[0].stateSnapshot)
        assertEquals("The new value is at index 6; parent is index 2, value 3.", trace.steps[0].detailNote)
    }

    @Test
    fun testTopologicalSortTraceParsing() {
        val item1 = "- Remove A; C's in-degree changes from 2 to 1. Queue: `[B]`."
        val item2 = "- Remove B; C changes to 0 and D changes to 0. Enqueue C then D. Queue: `[C,D]`."
        val item3 = "- Remove C; E changes from 2 to 1. Queue: `[D]`."
        val item4 = "- Remove D; E changes to 0. Queue: `[E]`."
        val item5 = "- Remove E. Output: `A, B, C, D, E`."

        assertNotNull(parseTraceStepItem(item1))
        assertNotNull(parseTraceStepItem(item2))
        assertNotNull(parseTraceStepItem(item3))
        assertNotNull(parseTraceStepItem(item4))
        assertNotNull(parseTraceStepItem(item5))

        val markdown = """
            $item1
            $item2
            $item3
            $item4
            $item5
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.TraceSequenceBlock)

        val trace = blocks[0] as MarkdownBlock.TraceSequenceBlock
        assertEquals(5, trace.steps.size)

        assertEquals("Remove A", trace.steps[0].actionLabel)
        assertEquals("[B]", trace.steps[0].stateSnapshot)
        assertEquals("C's in-degree changes from 2 to 1", trace.steps[0].detailNote)

        assertEquals("Remove B", trace.steps[1].actionLabel)
        assertEquals("[C,D]", trace.steps[1].stateSnapshot)
        assertEquals("C changes to 0 and D changes to 0. Enqueue C then D", trace.steps[1].detailNote)

        assertEquals("Remove C", trace.steps[2].actionLabel)
        assertEquals("[D]", trace.steps[2].stateSnapshot)
        assertEquals("E changes from 2 to 1", trace.steps[2].detailNote)

        assertEquals("Remove D", trace.steps[3].actionLabel)
        assertEquals("[E]", trace.steps[3].stateSnapshot)
        assertEquals("E changes to 0", trace.steps[3].detailNote)

        assertEquals("Remove E", trace.steps[4].actionLabel)
        assertEquals("A, B, C, D, E", trace.steps[4].stateSnapshot)
        assertEquals("Final Output", trace.steps[4].detailNote)
    }

    @Test
    fun testNonTraceBulletListFallback() {
        val markdown = """
            - **LIFO**: the most recently pushed item is popped.
            - **Top**: the end used for stack insertion.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.UnorderedListItem)
        assertTrue(blocks[1] is MarkdownBlock.UnorderedListItem)
    }

    @Test
    fun testIncompleteOrAmbiguousSequenceFallback() {
        val markdown = """
            - **Push A** — Stack: `[A]`; result: —.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.UnorderedListItem)
    }
}
