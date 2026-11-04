package com.example.learndsandalgorithm.presentation.learn

internal data class HashTableEntry(
    val key: String,
    val value: String
)

internal enum class HashTableEvent {
    STRUCTURE_OVERVIEW,
    SELECT_KEY,
    HASH_COMPUTATION,
    INDEX_MODULO,
    HIGHLIGHT_BUCKET,
    INSERT_ENTRY,
    COMPLETE
}

internal data class HashTableTraceStep(
    val buckets: List<HashTableEntry?>,
    val capacity: Int,
    val event: HashTableEvent,
    val activeKey: String? = null,
    val activeValue: String? = null,
    val charBreakdown: List<Pair<Char, Int>>? = null,
    val hashSum: Int? = null,
    val calculatedIndex: Int? = null,
    val highlightedBucketIndex: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HashTableTrace {

    fun hashKey(key: String, capacity: Int): Int {
        require(capacity > 0) { "Capacity must be positive" }
        return key.sumOf { it.code } % capacity
    }

    fun charCodes(key: String): List<Pair<Char, Int>> {
        return key.map { it to it.code }
    }

    fun stepsForFundamentals(capacity: Int = 8): List<HashTableTraceStep> {
        val steps = mutableListOf<HashTableTraceStep>()
        val emptyBuckets = List<HashTableEntry?>(capacity) { null }

        // Step 0: Initial structure introduction (empty table)
        steps.add(
            HashTableTraceStep(
                buckets = emptyBuckets,
                capacity = capacity,
                event = HashTableEvent.STRUCTURE_OVERVIEW,
                explanation = "A Hash Table uses an array of fixed capacity ($capacity buckets, indices 0..${capacity - 1}). Keys map to bucket indices to store values.",
                badgeText = "READY"
            )
        )

        // Step 1: Pre-populated table structure demonstration
        val demoBuckets = emptyBuckets.toMutableList().apply {
            this[2] = HashTableEntry("DOG", "Canine")
            this[6] = HashTableEntry("S102", "Alice")
        }
        steps.add(
            HashTableTraceStep(
                buckets = demoBuckets,
                capacity = capacity,
                event = HashTableEvent.STRUCTURE_OVERVIEW,
                highlightedBucketIndex = 2,
                explanation = "Buckets store key-value pairs. For example, key 'DOG' maps to bucket [2] ('Canine'), and key 'S102' maps to bucket [6] ('Alice').",
                badgeText = "STRUCTURE"
            )
        )

        // Reset to empty table for step-by-step key insertion sequence
        val currentBuckets = emptyBuckets.toMutableList()

        val keysToInsert = listOf(
            HashTableEntry("CAT", "Feline"),
            HashTableEntry("DOG", "Canine"),
            HashTableEntry("FOX", "Vixen"),
            HashTableEntry("S102", "Alice")
        )

        keysToInsert.forEachIndexed { itemIdx, entry ->
            val key = entry.key
            val value = entry.value
            val breakdown = charCodes(key)
            val sum = key.sumOf { it.code }
            val index = sum % capacity

            // Sub-step A: Select key
            steps.add(
                HashTableTraceStep(
                    buckets = currentBuckets.toList(),
                    capacity = capacity,
                    event = HashTableEvent.SELECT_KEY,
                    activeKey = key,
                    activeValue = value,
                    explanation = "Item ${itemIdx + 1}/${keysToInsert.size}: Select key '$key' with value '$value'. Next, convert string '$key' into an integer array index.",
                    badgeText = "KEY: $key"
                )
            )

            // Sub-step B: Hash calculation (character sum)
            val sumExpr = breakdown.joinToString(" + ") { "${it.first}(${it.second})" }
            steps.add(
                HashTableTraceStep(
                    buckets = currentBuckets.toList(),
                    capacity = capacity,
                    event = HashTableEvent.HASH_COMPUTATION,
                    activeKey = key,
                    activeValue = value,
                    charBreakdown = breakdown,
                    hashSum = sum,
                    explanation = "Hash Function: Sum ASCII character codes: $sumExpr = $sum.",
                    badgeText = "SUM = $sum"
                )
            )

            // Sub-step C: Modulo calculation
            steps.add(
                HashTableTraceStep(
                    buckets = currentBuckets.toList(),
                    capacity = capacity,
                    event = HashTableEvent.INDEX_MODULO,
                    activeKey = key,
                    activeValue = value,
                    charBreakdown = breakdown,
                    hashSum = sum,
                    calculatedIndex = index,
                    explanation = "Compute bucket index: hash(key) % capacity = $sum % $capacity = $index.",
                    badgeText = "INDEX = $index"
                )
            )

            // Sub-step D: Highlight target bucket
            steps.add(
                HashTableTraceStep(
                    buckets = currentBuckets.toList(),
                    capacity = capacity,
                    event = HashTableEvent.HIGHLIGHT_BUCKET,
                    activeKey = key,
                    activeValue = value,
                    charBreakdown = breakdown,
                    hashSum = sum,
                    calculatedIndex = index,
                    highlightedBucketIndex = index,
                    explanation = "Target bucket index [$index] selected for storage.",
                    badgeText = "TARGET: [$index]"
                )
            )

            // Sub-step E: Insert entry into bucket
            currentBuckets[index] = entry
            steps.add(
                HashTableTraceStep(
                    buckets = currentBuckets.toList(),
                    capacity = capacity,
                    event = HashTableEvent.INSERT_ENTRY,
                    activeKey = key,
                    activeValue = value,
                    calculatedIndex = index,
                    highlightedBucketIndex = index,
                    explanation = "Stored key-value entry ('$key' → '$value') into bucket [$index].",
                    badgeText = "STORED AT [$index]"
                )
            )
        }

        // Final completion step
        steps.add(
            HashTableTraceStep(
                buckets = currentBuckets.toList(),
                capacity = capacity,
                event = HashTableEvent.COMPLETE,
                isTerminal = true,
                explanation = "Sequence complete. The hash function mapped 4 keys to distinct array bucket indices in average O(1) time.",
                badgeText = "COMPLETE"
            )
        )

        return steps
    }
}
