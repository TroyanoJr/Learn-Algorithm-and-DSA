package com.example.learndsandalgorithm.presentation.learn

internal enum class StringMatchEvent {
    START,
    COMPARE_CHAR,
    ALIGNMENT_MATCH,
    ALIGNMENT_MISMATCH,
    MATCHING_COMPLETE
}

internal data class NaiveStringMatchingTraceStep(
    val text: String = "ABABA",
    val pattern: String = "ABA",
    val currentStart: Int? = null,
    val currentOffset: Int? = null,
    val isCharMatch: Boolean? = null,
    val foundMatches: List<Int> = emptyList(),
    val event: StringMatchEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object NaiveStringMatchingTrace {

    fun stepsForNaiveMatch(
        text: String = "ABABA",
        pattern: String = "ABA"
    ): List<NaiveStringMatchingTraceStep> {
        val steps = mutableListOf<NaiveStringMatchingTraceStep>()
        val matches = mutableListOf<Int>()
        val n = text.length
        val m = pattern.length

        fun snapshot(
            start: Int? = null,
            offset: Int? = null,
            charMatch: Boolean? = null,
            event: StringMatchEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = NaiveStringMatchingTraceStep(
            text = text,
            pattern = pattern,
            currentStart = start,
            currentOffset = offset,
            isCharMatch = charMatch,
            foundMatches = matches.toList(),
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = StringMatchEvent.START,
                explanation = "Naive Pattern Matching: Sliding pattern '$pattern' across text '$text' at all valid alignments [0..${n - m}].",
                badgeText = "READY · TEXT \"$text\" | PATTERN \"$pattern\""
            )
        )

        for (start in 0..(n - m)) {
            var matched = true
            for (offset in 0 until m) {
                val tChar = text[start + offset]
                val pChar = pattern[offset]
                val isCharEqual = tChar == pChar

                // Compare char step
                steps.add(
                    snapshot(
                        start = start,
                        offset = offset,
                        charMatch = isCharEqual,
                        event = StringMatchEvent.COMPARE_CHAR,
                        explanation = "Alignment $start, Offset $offset: Compare text[${start + offset}] ('$tChar') with pattern[$offset] ('$pChar'). ${if (isCharEqual) "Match!" else "Mismatch!"}",
                        badgeText = "ALIGN $start | OFFSET $offset: '$tChar' ${if (isCharEqual) "==" else "≠"} '$pChar'"
                    )
                )

                if (!isCharEqual) {
                    matched = false
                    steps.add(
                        snapshot(
                            start = start,
                            offset = offset,
                            charMatch = false,
                            event = StringMatchEvent.ALIGNMENT_MISMATCH,
                            explanation = "MISMATCH at alignment $start! text[${start + offset}] ('$tChar') != pattern[$offset] ('$pChar'). Shift pattern to start ${start + 1}.",
                            badgeText = "MISMATCH AT ALIGNMENT $start"
                        )
                    )
                    break
                }
            }

            if (matched) {
                matches.add(start)
                steps.add(
                    snapshot(
                        start = start,
                        offset = m - 1,
                        charMatch = true,
                        event = StringMatchEvent.ALIGNMENT_MATCH,
                        explanation = "FULL PATTERN MATCH AT ALIGNMENT $start! Entire pattern '$pattern' matches text substring at index $start.",
                        badgeText = "FULL MATCH AT INDEX $start"
                    )
                )
            }
        }

        // Step Complete
        steps.add(
            snapshot(
                event = StringMatchEvent.MATCHING_COMPLETE,
                isTerminal = true,
                explanation = "Naive Pattern Matching Complete! Pattern '$pattern' found at starting indices: [${matches.joinToString(", ")}]. Time: O(N * M), Space: O(1).",
                badgeText = "COMPLETE: MATCHES AT [${matches.joinToString(", ")}]"
            )
        )

        return steps
    }
}
