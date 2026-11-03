package com.example.learndsandalgorithm.presentation.learn

internal enum class ApplicationScenario {
    FREQUENCY_COUNTER,
    RANGE_QUERY_COMPARISON,
    MUTABLE_KEY_PITFALL
}

internal data class FrequencyPair(
    val char: Char,
    val count: Int
)

internal data class ApplicationsTraceStep(
    val scenario: ApplicationScenario,
    val inputText: String? = null,
    val currentCharIndex: Int? = null,
    val currentChar: Char? = null,
    val freqMap: List<FrequencyPair> = emptyList(),
    val rangeMin: Int? = null,
    val rangeMax: Int? = null,
    val bstKeys: List<Int>? = null,
    val bstVisitedKeys: Set<Int> = emptySet(),
    val hashBucketKeys: List<Int?>? = null,
    val hashVisitedBuckets: Set<Int> = emptySet(),
    val originalKeyId: Int? = null,
    val mutatedKeyId: Int? = null,
    val storedSlotIndex: Int? = null,
    val searchedSlotIndex: Int? = null,
    val keyMutated: Boolean = false,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HashTableApplicationsTrace {

    fun stepsForScenario(scenario: ApplicationScenario): List<ApplicationsTraceStep> {
        return when (scenario) {
            ApplicationScenario.FREQUENCY_COUNTER -> generateFrequencyCounterSteps()
            ApplicationScenario.RANGE_QUERY_COMPARISON -> generateRangeQuerySteps()
            ApplicationScenario.MUTABLE_KEY_PITFALL -> generateMutableKeySteps()
        }
    }

    private fun generateFrequencyCounterSteps(): List<ApplicationsTraceStep> {
        val steps = mutableListOf<ApplicationsTraceStep>()
        val text = "aabcc"
        val map = mutableMapOf<Char, Int>()

        fun snapshot(
            idx: Int? = null,
            char: Char? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = ApplicationsTraceStep(
            scenario = ApplicationScenario.FREQUENCY_COUNTER,
            inputText = text,
            currentCharIndex = idx,
            currentChar = char,
            freqMap = map.map { FrequencyPair(it.key, it.value) },
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(
            snapshot(
                explanation = "Frequency Counter: Processes text '$text' character by character, updating character counts in O(1) average time.",
                badgeText = "READY · INPUT: \"$text\""
            )
        )

        text.forEachIndexed { i, c ->
            val isDuplicate = map.containsKey(c)
            val newCount = (map[c] ?: 0) + 1
            map[c] = newCount

            val statusStr = if (isDuplicate) "DUPLICATE '$c' → count = $newCount" else "NEW CHAR '$c' → count = 1"
            steps.add(
                snapshot(
                    idx = i,
                    char = c,
                    explanation = "Index $i: Read char '$c'. ${if (isDuplicate) "Key '$c' found in map! Incremented count to $newCount." else "Key '$c' not in map. Added entry '$c': 1."}",
                    badgeText = "STEP ${i + 1}/5: $statusStr"
                )
            )
        }

        steps.add(
            snapshot(
                isTerminal = true,
                explanation = "Frequency counter complete! Result map: {'a': 2, 'b': 1, 'c': 2}. All updates executed in O(1) average time.",
                badgeText = "COMPLETE"
            )
        )

        return steps
    }

    private fun generateRangeQuerySteps(): List<ApplicationsTraceStep> {
        val steps = mutableListOf<ApplicationsTraceStep>()
        val rangeMin = 10
        val rangeMax = 25
        val hashBuckets = listOf(42, 15, 7, 91, 18, 3, 22, 60)
        val bstSortedKeys = listOf(3, 7, 15, 18, 22, 42, 60, 91)

        fun snapshot(
            hashVisited: Set<Int> = emptySet(),
            bstVisited: Set<Int> = emptySet(),
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = ApplicationsTraceStep(
            scenario = ApplicationScenario.RANGE_QUERY_COMPARISON,
            rangeMin = rangeMin,
            rangeMax = rangeMax,
            hashBucketKeys = hashBuckets,
            hashVisitedBuckets = hashVisited,
            bstKeys = bstSortedKeys,
            bstVisitedKeys = bstVisited,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(
            snapshot(
                explanation = "Trade-off Comparison: Range Query [10..25]. Hash tables store keys unordered; Balanced BSTs maintain keys in sorted order.",
                badgeText = "READY · RANGE [10..25]"
            )
        )

        // Hash Table Range Query Scan
        val matchingHash = mutableSetOf<Int>()
        hashBuckets.indices.forEach { idx ->
            matchingHash.add(idx)
            val valAtIdx = hashBuckets[idx]
            val inRange = valAtIdx in rangeMin..rangeMax
            steps.add(
                snapshot(
                    hashVisited = matchingHash.toSet(),
                    explanation = "Hash Table: Bucket [$idx] contains value $valAtIdx. ${if (inRange) "$valAtIdx is IN RANGE [10..25]!" else "$valAtIdx is outside range."} Must scan all N buckets (O(N) cost) because keys are unordered.",
                    badgeText = "HASH TABLE SCAN: [$idx] = $valAtIdx"
                )
            )
        }

        // BST Range Query Scan
        val matchingBst = bstSortedKeys.filter { it in rangeMin..rangeMax }.toSet()
        steps.add(
            snapshot(
                hashVisited = hashBuckets.indices.toSet(),
                bstVisited = matchingBst,
                isTerminal = true,
                explanation = "BST Range Query: Balanced BST traverses only keys in range [10..25] ({15, 18, 22}) in O(log N + k) time. Conclusion: Use BST when key ordering or range queries are required!",
                badgeText = "BST RANGE MATCH: {15, 18, 22}"
            )
        )

        return steps
    }

    private fun generateMutableKeySteps(): List<ApplicationsTraceStep> {
        val steps = mutableListOf<ApplicationsTraceStep>()
        val originalId = 101
        val mutatedId = 999
        val capacity = 8
        val originalSlot = originalId % capacity // 101 % 8 = 5
        val mutatedSlot = mutatedId % capacity   // 999 % 8 = 7

        fun snapshot(
            keyMutated: Boolean = false,
            searchedSlot: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = ApplicationsTraceStep(
            scenario = ApplicationScenario.MUTABLE_KEY_PITFALL,
            originalKeyId = originalId,
            mutatedKeyId = mutatedId,
            storedSlotIndex = originalSlot,
            searchedSlotIndex = searchedSlot,
            keyMutated = keyMutated,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(
            snapshot(
                explanation = "Mutable Key Pitfall: Demonstrates why keys stored in a hash table MUST be immutable.",
                badgeText = "READY · MUTABLE KEY PITFALL"
            )
        )

        // Step 1: Insert Key
        steps.add(
            snapshot(
                explanation = "Step 1: Insert key User(id=$originalId). hash($originalId) % 8 = $originalSlot. Entry stored in bucket [$originalSlot].",
                badgeText = "INSERT: User(id=$originalId) AT [$originalSlot]"
            )
        )

        // Step 2: Mutate Key in place
        steps.add(
            snapshot(
                keyMutated = true,
                explanation = "Step 2: MUTATE KEY IN-PLACE! user.id changes from $originalId to $mutatedId while stored in table.",
                badgeText = "KEY MUTATED: $originalId → $mutatedId"
            )
        )

        // Step 3: Attempt Lookup
        steps.add(
            snapshot(
                keyMutated = true,
                searchedSlot = mutatedSlot,
                explanation = "Step 3: Attempt get(user). Hash function recomputes hash($mutatedId) % 8 = $mutatedSlot. Lookup inspects bucket [$mutatedSlot].",
                badgeText = "LOOKUP: HASH($mutatedId) → BUCKET [$mutatedSlot]"
            )
        )

        // Step 4: Lookup Failure
        steps.add(
            snapshot(
                keyMutated = true,
                searchedSlot = mutatedSlot,
                isTerminal = true,
                explanation = "LOOKUP FAILED! Bucket [$mutatedSlot] is empty. The entry remains trapped in bucket [$originalSlot] under the old hash code. Lesson: Always use immutable keys!",
                badgeText = "ENTRY UNFINDABLE! (TRAPPED AT [$originalSlot])"
            )
        )

        return steps
    }
}
