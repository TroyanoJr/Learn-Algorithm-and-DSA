package com.example.learndsandalgorithm.presentation.learn

internal enum class CollisionStrategy {
    SEPARATE_CHAINING,
    LINEAR_PROBING
}

internal enum class CollisionTraceEvent {
    START,
    SELECT_KEY,
    COMPUTE_HASH,
    COLLISION_DETECTED,
    CHAIN_NODE,
    PROBE_NEXT_SLOT,
    INSERT_ENTRY,
    COMPLETE
}

internal data class ChainingNode(
    val key: String,
    val value: String
)

internal data class LinearSlot(
    val key: String,
    val value: String
)

internal data class CollisionTraceStep(
    val strategy: CollisionStrategy,
    val capacity: Int,
    val chainingBuckets: List<List<ChainingNode>>,
    val linearSlots: List<LinearSlot?>,
    val event: CollisionTraceEvent,
    val activeKey: String? = null,
    val activeValue: String? = null,
    val initialHashIndex: Int? = null,
    val currentProbeIndex: Int? = null,
    val highlightedBucketIndex: Int? = null,
    val probedIndices: Set<Int> = emptySet(),
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object HashTableCollisionTrace {

    fun hashKey(key: String, capacity: Int): Int {
        require(capacity > 0) { "Capacity must be positive" }
        return key.sumOf { it.code } % capacity
    }

    fun stepsForStrategy(strategy: CollisionStrategy, capacity: Int = 7): List<CollisionTraceStep> {
        return when (strategy) {
            CollisionStrategy.SEPARATE_CHAINING -> generateChainingSteps(capacity)
            CollisionStrategy.LINEAR_PROBING -> generateLinearProbingSteps(capacity)
        }
    }

    private fun generateChainingSteps(capacity: Int): List<CollisionTraceStep> {
        val steps = mutableListOf<CollisionTraceStep>()
        val buckets = List(capacity) { mutableListOf<ChainingNode>() }
        val emptySlots = List<LinearSlot?>(capacity) { null }

        fun snapshot(
            event: CollisionTraceEvent,
            activeKey: String? = null,
            activeValue: String? = null,
            initialHashIndex: Int? = null,
            highlightedBucketIndex: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = CollisionTraceStep(
            strategy = CollisionStrategy.SEPARATE_CHAINING,
            capacity = capacity,
            chainingBuckets = buckets.map { it.toList() },
            linearSlots = emptySlots,
            event = event,
            activeKey = activeKey,
            activeValue = activeValue,
            initialHashIndex = initialHashIndex,
            highlightedBucketIndex = highlightedBucketIndex,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(
            snapshot(
                event = CollisionTraceEvent.START,
                explanation = "Separate Chaining handles collisions by maintaining a linked list of entries at each array bucket index.",
                badgeText = "READY · SEPARATE CHAINING"
            )
        )

        val keysToInsert = listOf(
            ChainingNode("CAT", "Feline"),
            ChainingNode("DOG", "Canine"),
            ChainingNode("FOX", "Vixen"),
            ChainingNode("RAT", "Rodent")
        )

        keysToInsert.forEachIndexed { idx, node ->
            val key = node.key
            val value = node.value
            val hashIndex = hashKey(key, capacity)

            // Step A: Select key
            steps.add(
                snapshot(
                    event = CollisionTraceEvent.SELECT_KEY,
                    activeKey = key,
                    activeValue = value,
                    explanation = "Item ${idx + 1}/${keysToInsert.size}: Select key '$key' with value '$value'.",
                    badgeText = "KEY: $key"
                )
            )

            // Step B: Compute Hash
            steps.add(
                snapshot(
                    event = CollisionTraceEvent.COMPUTE_HASH,
                    activeKey = key,
                    activeValue = value,
                    initialHashIndex = hashIndex,
                    highlightedBucketIndex = hashIndex,
                    explanation = "hash('$key') % $capacity = ${key.sumOf { it.code }} % $capacity = $hashIndex.",
                    badgeText = "INDEX = $hashIndex"
                )
            )

            val bucketChain = buckets[hashIndex]
            if (bucketChain.isNotEmpty()) {
                // Step C: Collision Detected
                val existingKeys = bucketChain.joinToString(", ") { "'${it.key}'" }
                steps.add(
                    snapshot(
                        event = CollisionTraceEvent.COLLISION_DETECTED,
                        activeKey = key,
                        activeValue = value,
                        initialHashIndex = hashIndex,
                        highlightedBucketIndex = hashIndex,
                        explanation = "COLLISION DETECTED at bucket [$hashIndex]! Bucket already contains $existingKeys.",
                        badgeText = "COLLISION AT [$hashIndex]"
                    )
                )

                // Step D: Append to chain
                bucketChain.add(node)
                val chainStr = bucketChain.joinToString(" → ") { "${it.key}:${it.value}" }
                steps.add(
                    snapshot(
                        event = CollisionTraceEvent.CHAIN_NODE,
                        activeKey = key,
                        activeValue = value,
                        initialHashIndex = hashIndex,
                        highlightedBucketIndex = hashIndex,
                        explanation = "Appended ('$key' → '$value') to linked list chain at bucket [$hashIndex]: $chainStr.",
                        badgeText = "CHAINED AT [$hashIndex]"
                    )
                )
            } else {
                // Step C: Insert into empty bucket
                bucketChain.add(node)
                steps.add(
                    snapshot(
                        event = CollisionTraceEvent.INSERT_ENTRY,
                        activeKey = key,
                        activeValue = value,
                        initialHashIndex = hashIndex,
                        highlightedBucketIndex = hashIndex,
                        explanation = "Bucket [$hashIndex] is empty. Created head node ('$key' → '$value') at bucket [$hashIndex].",
                        badgeText = "STORED AT [$hashIndex]"
                    )
                )
            }
        }

        steps.add(
            snapshot(
                event = CollisionTraceEvent.COMPLETE,
                isTerminal = true,
                explanation = "Separate Chaining complete. Colliding keys ('CAT' & 'FOX' at index 6) reside cleanly in the same bucket chain.",
                badgeText = "COMPLETE"
            )
        )

        return steps
    }

    private fun generateLinearProbingSteps(capacity: Int): List<CollisionTraceStep> {
        val steps = mutableListOf<CollisionTraceStep>()
        val slots = MutableList<LinearSlot?>(capacity) { null }
        val emptyChaining = List(capacity) { emptyList<ChainingNode>() }

        fun snapshot(
            event: CollisionTraceEvent,
            activeKey: String? = null,
            activeValue: String? = null,
            initialHashIndex: Int? = null,
            currentProbeIndex: Int? = null,
            highlightedBucketIndex: Int? = null,
            probedIndices: Set<Int> = emptySet(),
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = CollisionTraceStep(
            strategy = CollisionStrategy.LINEAR_PROBING,
            capacity = capacity,
            chainingBuckets = emptyChaining,
            linearSlots = slots.toList(),
            event = event,
            activeKey = activeKey,
            activeValue = activeValue,
            initialHashIndex = initialHashIndex,
            currentProbeIndex = currentProbeIndex,
            highlightedBucketIndex = highlightedBucketIndex,
            probedIndices = probedIndices,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(
            snapshot(
                event = CollisionTraceEvent.START,
                explanation = "Linear Probing resolves collisions by checking consecutive slots (index + 1) % capacity until finding an empty slot.",
                badgeText = "READY · LINEAR PROBING"
            )
        )

        val keysToInsert = listOf(
            LinearSlot("CAT", "Feline"),
            LinearSlot("DOG", "Canine"),
            LinearSlot("FOX", "Vixen"),
            LinearSlot("RAT", "Rodent")
        )

        keysToInsert.forEachIndexed { idx, slotItem ->
            val key = slotItem.key
            val value = slotItem.value
            val hashIndex = hashKey(key, capacity)

            // Step A: Select Key
            steps.add(
                snapshot(
                    event = CollisionTraceEvent.SELECT_KEY,
                    activeKey = key,
                    activeValue = value,
                    explanation = "Item ${idx + 1}/${keysToInsert.size}: Select key '$key' with value '$value'.",
                    badgeText = "KEY: $key"
                )
            )

            // Step B: Compute Hash
            steps.add(
                snapshot(
                    event = CollisionTraceEvent.COMPUTE_HASH,
                    activeKey = key,
                    activeValue = value,
                    initialHashIndex = hashIndex,
                    highlightedBucketIndex = hashIndex,
                    explanation = "hash('$key') % $capacity = ${key.sumOf { it.code }} % $capacity = $hashIndex.",
                    badgeText = "INDEX = $hashIndex"
                )
            )

            if (slots[hashIndex] == null) {
                // Empty slot -> Direct Insert
                slots[hashIndex] = slotItem
                steps.add(
                    snapshot(
                        event = CollisionTraceEvent.INSERT_ENTRY,
                        activeKey = key,
                        activeValue = value,
                        initialHashIndex = hashIndex,
                        highlightedBucketIndex = hashIndex,
                        explanation = "Slot [$hashIndex] is empty. Insert ('$key' → '$value') directly into slot [$hashIndex].",
                        badgeText = "STORED AT [$hashIndex]"
                    )
                )
            } else {
                // Collision Detected
                val probed = mutableSetOf(hashIndex)
                steps.add(
                    snapshot(
                        event = CollisionTraceEvent.COLLISION_DETECTED,
                        activeKey = key,
                        activeValue = value,
                        initialHashIndex = hashIndex,
                        currentProbeIndex = hashIndex,
                        highlightedBucketIndex = hashIndex,
                        probedIndices = probed.toSet(),
                        explanation = "COLLISION AT SLOT [$hashIndex]! Occupied by '${slots[hashIndex]?.key}'. Begin linear probe sequence.",
                        badgeText = "COLLISION AT [$hashIndex]"
                    )
                )

                var probeIndex = (hashIndex + 1) % capacity
                while (slots[probeIndex] != null) {
                    probed.add(probeIndex)
                    steps.add(
                        snapshot(
                            event = CollisionTraceEvent.PROBE_NEXT_SLOT,
                            activeKey = key,
                            activeValue = value,
                            initialHashIndex = hashIndex,
                            currentProbeIndex = probeIndex,
                            highlightedBucketIndex = probeIndex,
                            probedIndices = probed.toSet(),
                            explanation = "Probe slot [$probeIndex]: Occupied by '${slots[probeIndex]?.key}'. Probe next slot.",
                            badgeText = "PROBE [$probeIndex] (FULL)"
                        )
                    )
                    probeIndex = (probeIndex + 1) % capacity
                }

                // Empty slot found
                probed.add(probeIndex)
                slots[probeIndex] = slotItem
                steps.add(
                    snapshot(
                        event = CollisionTraceEvent.INSERT_ENTRY,
                        activeKey = key,
                        activeValue = value,
                        initialHashIndex = hashIndex,
                        currentProbeIndex = probeIndex,
                        highlightedBucketIndex = probeIndex,
                        probedIndices = probed.toSet(),
                        explanation = "Found open slot [$probeIndex] after probing ${probed.size - 1} step(s). Stored ('$key' → '$value') at slot [$probeIndex].",
                        badgeText = "STORED AT [$probeIndex]"
                    )
                )
            }
        }

        steps.add(
            snapshot(
                event = CollisionTraceEvent.COMPLETE,
                isTerminal = true,
                explanation = "Linear Probing complete! Primary clustering occurs when probe sequences merge into contiguous occupied blocks.",
                badgeText = "COMPLETE"
            )
        )

        return steps
    }
}
