package com.example.learndsandalgorithm.presentation.learn

internal enum class LcsAction {
    INIT,
    COMPUTE_CELL,
    RECONSTRUCT,
    FINISHED
}

internal data class LcsDpStep(
    val text1: String = "abcde",
    val text2: String = "ace",
    val currentI: Int? = null,
    val currentJ: Int? = null,
    val dpMatrix: List<List<Int>> = List(6) { List(4) { 0 } },
    val isMatch: Boolean? = null,
    val lcsString: String = "",
    val action: LcsAction = LcsAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object LcsDpTrace {

    fun createTrace(
        text1: String = "abcde",
        text2: String = "ace"
    ): List<LcsDpStep> {
        val steps = mutableListOf<LcsDpStep>()
        val m = text1.length
        val n = text2.length
        val dp = List(m + 1) { MutableList(n + 1) { 0 } }

        fun snapshot(
            i: Int?,
            j: Int?,
            match: Boolean?,
            lcs: String,
            action: LcsAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = LcsDpStep(
            text1 = text1,
            text2 = text2,
            currentI = i,
            currentJ = j,
            dpMatrix = dp.map { it.toList() },
            isMatch = match,
            lcsString = lcs,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                i = null,
                j = null,
                match = null,
                lcs = "",
                action = LcsAction.INIT,
                explanation = "Longest Common Subsequence (LCS): Finding LCS between '$text1' and '$text2'. Matrix size ${m+1} x ${n+1} initialized to 0.",
                badgeText = "READY · LCS('$text1', '$text2')"
            )
        )

        for (i in 1..m) {
            for (j in 1..n) {
                val char1 = text1[i - 1]
                val char2 = text2[j - 1]
                val isMatch = char1 == char2

                if (isMatch) {
                    val prevDiag = dp[i - 1][j - 1]
                    dp[i][j] = 1 + prevDiag
                    steps.add(
                        snapshot(
                            i = i,
                            j = j,
                            match = true,
                            lcs = "",
                            action = LcsAction.COMPUTE_CELL,
                            explanation = "Cell ($i, $j): '$char1' == '$char2' MATCH! dp[$i][$j] = 1 + diagonal (${prevDiag}) = ${dp[i][j]}.",
                            badgeText = "MATCH '$char1' == '$char2' -> dp[$i][$j] = ${dp[i][j]}"
                        )
                    )
                } else {
                    val top = dp[i - 1][j]
                    val left = dp[i][j - 1]
                    val maxVal = maxOf(top, left)
                    dp[i][j] = maxVal
                    steps.add(
                        snapshot(
                            i = i,
                            j = j,
                            match = false,
                            lcs = "",
                            action = LcsAction.COMPUTE_CELL,
                            explanation = "Cell ($i, $j): '$char1' != '$char2' MISMATCH! dp[$i][$j] = max(top $top, left $left) = $maxVal.",
                            badgeText = "MISMATCH '$char1' != '$char2' -> dp[$i][$j] = $maxVal"
                        )
                    )
                }
            }
        }

        // Reconstruct LCS string
        val lcsChars = mutableListOf<Char>()
        var currI = m
        var currJ = n
        while (currI > 0 && currJ > 0) {
            if (text1[currI - 1] == text2[currJ - 1]) {
                lcsChars.add(text1[currI - 1])
                currI--
                currJ--
            } else if (dp[currI - 1][currJ] >= dp[currI][currJ - 1]) {
                currI--
            } else {
                currJ--
            }
        }
        val reconstructedLcs = lcsChars.reversed().joinToString("")

        steps.add(
            snapshot(
                i = m,
                j = n,
                match = null,
                lcs = reconstructedLcs,
                action = LcsAction.RECONSTRUCT,
                explanation = "Reconstructing LCS: Traced back from dp[$m][$n] = ${dp[m][n]} to recover sequence '$reconstructedLcs'.",
                badgeText = "RECONSTRUCTED LCS: '$reconstructedLcs'"
            )
        )

        val finalAns = dp[m][n]

        // FINISHED
        steps.add(
            snapshot(
                i = m,
                j = n,
                match = null,
                lcs = reconstructedLcs,
                action = LcsAction.FINISHED,
                explanation = "LCS DP complete! Longest Common Subsequence length = $finalAns ('$reconstructedLcs'). Time: O(M * N), Space: O(M * N).",
                badgeText = "COMPLETE: LCS LENGTH = $finalAns ('$reconstructedLcs')",
                isTerminal = true
            )
        )

        return steps
    }
}
