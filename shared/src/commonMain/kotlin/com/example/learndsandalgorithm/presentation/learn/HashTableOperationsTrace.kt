package com.example.learndsandalgorithm.presentation.learn

internal data class HashOpEntry(
    val key: String,
    val value: String
)

internal enum class HashOpType {
    INSERT,
    LOOKUP,
    DELETE,
    RESIZE_REHASH
}

internal data class OperationsTraceStep(
    val oldCapacity: Int? = null,
    val capacity: Int,
    val size: Int,
    val loadFactor: Double,
    val isThresholdExceeded: Boolean = false,
    val oldBuckets: List<HashOpEntry?>? = null,
    val buckets: List<HashOpEntry?>,
    val activeOp: HashOpType,
    val activeKey: String? = null,
    val activeValue: String? = null,
    val targetIndex: Int? = null,
    val highlightedIndex: Int? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HashTableOperationsTrace {

    fun hashKey(key: String, capacity: Int): Int {
        require(capacity > 0) { "Capacity must be positive" }
        return key.sumOf { it.code } % capacity
    }

    fun stepsForOperations(initialCapacity: Int = 4, threshold: Double = 0.75): List<OperationsTraceStep> {
        val steps = mutableListOf<OperationsTraceStep>()
        var currentCapacity = initialCapacity
        var currentBuckets = MutableList<HashOpEntry?>(currentCapacity) { null }
        var currentSize = 0

        fun snapshot(
            activeOp: HashOpType,
            activeKey: String? = null,
            activeValue: String? = null,
            targetIndex: Int? = null,
            highlightedIndex: Int? = null,
            oldCapacity: Int? = null,
            oldBuckets: List<HashOpEntry?>? = null,
            isThresholdExceeded: Boolean = false,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ): OperationsTraceStep {
            val lf = currentSize.toDouble() / currentCapacity
            return OperationsTraceStep(
                oldCapacity = oldCapacity,
                capacity = currentCapacity,
                size = currentSize,
                loadFactor = (lf * 100).toInt() / 100.0,
                isThresholdExceeded = isThresholdExceeded,
                oldBuckets = oldBuckets,
                buckets = currentBuckets.toList(),
                activeOp = activeOp,
                activeKey = activeKey,
                activeValue = activeValue,
                targetIndex = targetIndex,
                highlightedIndex = highlightedIndex,
                explanation = explanation,
                badgeText = badgeText,
                isTerminal = isTerminal
            )
        }

        // Step 0: Ready
        steps.add(
            snapshot(
                activeOp = HashOpType.INSERT,
                explanation = "Operations & Resizing: Hash table starts with capacity $currentCapacity and threshold alpha = $threshold.",
                badgeText = "READY · CAP $currentCapacity"
            )
        )

        val itemsToInsert = listOf(
            HashOpEntry("CAT", "Feline"),
            HashOpEntry("DOG", "Canine"),
            HashOpEntry("FOX", "Vixen")
        )

        // Insert first 3 items (capacity 4)
        itemsToInsert.forEach { entry ->
            val index = hashKey(entry.key, currentCapacity)
            currentBuckets[index] = entry
            currentSize++

            steps.add(
                snapshot(
                    activeOp = HashOpType.INSERT,
                    activeKey = entry.key,
                    activeValue = entry.value,
                    targetIndex = index,
                    highlightedIndex = index,
                    explanation = "INSERT '${entry.key}': hash('${entry.key}') % $currentCapacity = $index. Size = $currentSize/$currentCapacity (alpha = ${currentSize.toDouble() / currentCapacity}).",
                    badgeText = "INSERT: ${entry.key}"
                )
            )
        }

        // 4th Item: "RAT" -> triggers Load Factor > 0.75
        val ratEntry = HashOpEntry("RAT", "Rodent")

        // Step A: Attempt insert RAT & calculate new load factor
        val prospectiveSize = currentSize + 1
        val prospectiveLF = prospectiveSize.toDouble() / currentCapacity

        steps.add(
            snapshot(
                activeOp = HashOpType.RESIZE_REHASH,
                activeKey = ratEntry.key,
                activeValue = ratEntry.value,
                isThresholdExceeded = true,
                explanation = "INSERT '${ratEntry.key}': Adding item 4 makes size = $prospectiveSize/$currentCapacity (alpha = $prospectiveLF). alpha > $threshold THRESHOLD EXCEEDED! Resizing triggered.",
                badgeText = "RESIZE TRIGGERED (alpha=$prospectiveLF)"
            )
        )

        // Step B: Doubling capacity from 4 to 8 & allocating new array
        val oldCap = currentCapacity
        val oldBucketsCopy = currentBuckets.toList()
        currentCapacity *= 2
        val newBuckets = MutableList<HashOpEntry?>(currentCapacity) { null }

        steps.add(
            snapshot(
                activeOp = HashOpType.RESIZE_REHASH,
                activeKey = ratEntry.key,
                activeValue = ratEntry.value,
                oldCapacity = oldCap,
                oldBuckets = oldBucketsCopy,
                isThresholdExceeded = true,
                explanation = "REHASH: Allocated new array of capacity $currentCapacity (doubled from $oldCap). Existing elements must be rehashed.",
                badgeText = "NEW CAPACITY $currentCapacity"
            )
        )

        // Step C: Rehash existing elements into size 8 array
        val existingEntries = oldBucketsCopy.filterNotNull()
        existingEntries.forEach { entry ->
            val newIndex = hashKey(entry.key, currentCapacity)
            newBuckets[newIndex] = entry
            currentBuckets = newBuckets

            steps.add(
                snapshot(
                    activeOp = HashOpType.RESIZE_REHASH,
                    activeKey = entry.key,
                    activeValue = entry.value,
                    targetIndex = newIndex,
                    highlightedIndex = newIndex,
                    oldCapacity = oldCap,
                    oldBuckets = oldBucketsCopy,
                    explanation = "REHASH '${entry.key}': recomputed hash('${entry.key}') % $currentCapacity = $newIndex in new array.",
                    badgeText = "REHASH: ${entry.key} → [$newIndex]"
                )
            )
        }

        // Step D: Insert the 4th item "RAT" into new array
        val ratIndex = hashKey(ratEntry.key, currentCapacity)
        newBuckets[ratIndex] = ratEntry
        currentBuckets = newBuckets
        currentSize = prospectiveSize

        steps.add(
            snapshot(
                activeOp = HashOpType.INSERT,
                activeKey = ratEntry.key,
                activeValue = ratEntry.value,
                targetIndex = ratIndex,
                highlightedIndex = ratIndex,
                explanation = "INSERT '${ratEntry.key}': hash('${ratEntry.key}') % $currentCapacity = $ratIndex. Stored in new table. Load factor alpha = $currentSize/$currentCapacity = 0.50.",
                badgeText = "STORED '${ratEntry.key}' AT [$ratIndex]"
            )
        )

        // Step E: Lookup Operation get("FOX")
        val foxIndex = hashKey("FOX", currentCapacity)
        steps.add(
            snapshot(
                activeOp = HashOpType.LOOKUP,
                activeKey = "FOX",
                activeValue = "Vixen",
                targetIndex = foxIndex,
                highlightedIndex = foxIndex,
                explanation = "LOOKUP 'FOX': hash('FOX') % $currentCapacity = $foxIndex. Inspect slot [$foxIndex] → Found 'Vixen' in O(1) average time.",
                badgeText = "LOOKUP: FOX → Vixen [O(1)]"
            )
        )

        // Step F: Deletion Operation remove("DOG")
        val dogIndex = hashKey("DOG", currentCapacity)
        currentBuckets[dogIndex] = null
        currentSize--

        steps.add(
            snapshot(
                activeOp = HashOpType.DELETE,
                activeKey = "DOG",
                activeValue = "Canine",
                targetIndex = dogIndex,
                highlightedIndex = dogIndex,
                explanation = "DELETE 'DOG': hash('DOG') % $currentCapacity = $dogIndex. Cleared entry at slot [$dogIndex] in O(1) time. Size = $currentSize/$currentCapacity.",
                badgeText = "DELETED: DOG FROM [$dogIndex]"
            )
        )

        // Step G: Complete
        steps.add(
            snapshot(
                activeOp = HashOpType.INSERT,
                isTerminal = true,
                explanation = "Operations sequence complete. Load factor control (alpha <= 0.75) preserves O(1) average lookup, insert, and delete complexity.",
                badgeText = "COMPLETE"
            )
        )

        return steps
    }
}
