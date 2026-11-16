package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms [HashTableTraceStep] and [CollisionTraceStep] into abstract [VisualizationScene] models
 * with stable element identities for buckets, slots, entries, and chains, along with [HashTableStepMeta].
 */
internal object HashTableSceneBuilder {

    fun buildFundamentalsScenes(steps: List<HashTableTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val keyToIdentity = mutableMapOf<String, String>()
        var keyCounter = 0

        return steps.mapIndexed { stepIndex, step ->
            val capacity = step.capacity
            val buckets = step.buckets
            val entities = mutableListOf<VisualEntity>()
            val connections = mutableListOf<VisualConnection>()

            for (i in 0 until capacity) {
                val bucketSlotKey = "bucket_slot:$i"
                val bucketId = "bucket:$i"
                val entry = buckets.getOrNull(i)

                val isHighlighted = step.highlightedBucketIndex == i
                val isCalculated = step.calculatedIndex == i

                val entityState = when {
                    isHighlighted || isCalculated -> EntityVisualState.ACTIVE
                    entry != null -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }

                entities.add(
                    VisualEntity(
                        visualKey = "hash_table:$bucketId",
                        elementId = bucketId,
                        dataValue = "[$i]",
                        slotKey = bucketSlotKey,
                        badgeLabel = if (isCalculated) "Target" else null,
                        visualState = entityState
                    )
                )

                if (entry != null) {
                    val entryId = keyToIdentity.getOrPut(entry.key) { "elem:${keyCounter++}" }
                    val isEntryActive = step.activeKey == entry.key

                    entities.add(
                        VisualEntity(
                            visualKey = "hash_table:entry:$entryId",
                            elementId = entryId,
                            dataValue = "${entry.key}\n${entry.value}",
                            slotKey = "entry_slot:$i",
                            badgeLabel = if (isEntryActive) "Active" else null,
                            visualState = if (isEntryActive) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT,
                            entryKey = entry.key,
                            entryValue = entry.value
                        )
                    )
                }
            }

            val subScene = StructureSubScene(
                subPaneId = "hash_table_pane",
                type = StructureType.HASH_TABLE,
                didacticPriority = 1,
                minRequiredWidthDp = 320f,
                minRequiredHeightDp = 220f,
                entities = entities,
                connections = connections
            )

            val meta = HashTableStepMeta(
                activeKey = step.activeKey,
                activeValue = step.activeValue,
                charBreakdown = step.charBreakdown,
                hashSum = step.hashSum,
                calculatedIndex = step.calculatedIndex,
                capacity = step.capacity,
                event = step.event,
                badgeText = step.badgeText,
                isTerminal = step.isTerminal,
                explanation = step.explanation
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "hash_table_scene_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation,
                hashTableMeta = meta
            )
        }
    }

    fun buildCollisionScenes(steps: List<CollisionTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val maxChainDepth = steps.maxOfOrNull { step ->
            step.chainingBuckets.maxOfOrNull { it.size } ?: 0
        } ?: 0

        val keyToIdentity = mutableMapOf<String, String>()
        var keyCounter = 0

        return steps.mapIndexed { stepIndex, step ->
            val capacity = step.capacity
            val entities = mutableListOf<VisualEntity>()
            val connections = mutableListOf<VisualConnection>()

            if (step.strategy == CollisionStrategy.SEPARATE_CHAINING) {
                // Separate Chaining: 7 base buckets + vertical chain nodes across sequence-level maxChainDepth
                for (i in 0 until capacity) {
                    val bucketSlotKey = "bucket_slot:$i"
                    val bucketId = "bucket:$i"
                    val chain = step.chainingBuckets.getOrNull(i) ?: emptyList()

                    val isHighlighted = step.highlightedBucketIndex == i
                    val isCollision = isHighlighted && step.event == CollisionTraceEvent.COLLISION_DETECTED

                    val bucketState = when {
                        isCollision -> EntityVisualState.VIOLATION
                        isHighlighted -> EntityVisualState.ACTIVE
                        chain.isNotEmpty() -> EntityVisualState.VISITED
                        else -> EntityVisualState.DEFAULT
                    }

                    entities.add(
                        VisualEntity(
                            visualKey = "hash_table:$bucketId",
                            elementId = bucketId,
                            dataValue = "[$i]",
                            slotKey = bucketSlotKey,
                            badgeLabel = if (isCollision) "Collision" else if (isHighlighted) "Target" else null,
                            visualState = bucketState
                        )
                    )

                    chain.forEachIndexed { j, node ->
                        val entryId = keyToIdentity.getOrPut(node.key) { "elem:${keyCounter++}" }
                        val isNodeActive = isHighlighted && j == chain.lastIndex && (step.event == CollisionTraceEvent.CHAIN_NODE || step.event == CollisionTraceEvent.INSERT_ENTRY)

                        val chainState = when {
                            isCollision && j == chain.lastIndex -> EntityVisualState.VIOLATION
                            isNodeActive -> EntityVisualState.ACTIVE
                            else -> EntityVisualState.VISITED
                        }

                        val chainSlotKey = "chain_slot:$i:$j"
                        entities.add(
                            VisualEntity(
                                visualKey = "hash_table:chain:$entryId",
                                elementId = entryId,
                                dataValue = "${node.key}\n${node.value}",
                                slotKey = chainSlotKey,
                                badgeLabel = if (isNodeActive) "Chained" else null,
                                visualState = chainState,
                                entryKey = node.key,
                                entryValue = node.value
                            )
                        )

                        // Chain connection (j -> j+1)
                        if (j < chain.lastIndex) {
                            val nextNode = chain[j + 1]
                            val nextEntryId = keyToIdentity.getOrPut(nextNode.key) { "elem:${keyCounter++}" }
                            connections.add(
                                VisualConnection(
                                    connectionKey = "link:chain_slot:$i:$j->chain_slot:$i:${j + 1}",
                                    fromVisualKey = "hash_table:chain:$entryId",
                                    toVisualKey = "hash_table:chain:$nextEntryId",
                                    visualState = ConnectionVisualState.HIGHLIGHTED
                                )
                            )
                        }
                    }
                }
            } else {
                // Linear Probing: 7 horizontal slots with probing highlights & badges
                for (i in 0 until capacity) {
                    val slotKey = "linear_slot:$i"
                    val slot = step.linearSlots.getOrNull(i)

                    val isHighlighted = step.highlightedBucketIndex == i
                    val isProbed = step.probedIndices.contains(i)
                    val isCollision = isHighlighted && step.event == CollisionTraceEvent.COLLISION_DETECTED
                    val isProbeTarget = step.currentProbeIndex == i && step.event == CollisionTraceEvent.PROBE_NEXT_SLOT

                    val slotState = when {
                        isCollision -> EntityVisualState.VIOLATION
                        isProbeTarget || isHighlighted -> EntityVisualState.ACTIVE
                        isProbed -> EntityVisualState.VISITED
                        slot != null -> EntityVisualState.VISITED
                        else -> EntityVisualState.DEFAULT
                    }

                    val badge = when {
                        isCollision -> "COLLISION"
                        isProbeTarget -> "PROBE"
                        isHighlighted -> "TARGET"
                        isProbed -> "PROBED"
                        else -> null
                    }

                    val slotId = if (slot != null) {
                        keyToIdentity.getOrPut(slot.key) { "elem:${keyCounter++}" }
                    } else {
                        "linear:$i"
                    }

                    entities.add(
                        VisualEntity(
                            visualKey = "hash_table:slot:$slotId",
                            elementId = slotId,
                            dataValue = if (slot != null) "${slot.key}\n${slot.value}" else "·",
                            slotKey = slotKey,
                            badgeLabel = badge,
                            visualState = slotState,
                            entryKey = slot?.key,
                            entryValue = slot?.value
                        )
                    )
                }
            }

            val subScene = StructureSubScene(
                subPaneId = "hash_table_pane",
                type = StructureType.HASH_TABLE,
                didacticPriority = 1,
                minRequiredWidthDp = 320f,
                minRequiredHeightDp = 240f,
                entities = entities,
                connections = connections
            )

            val meta = HashTableStepMeta(
                activeKey = step.activeKey,
                activeValue = step.activeValue,
                capacity = step.capacity,
                badgeText = step.badgeText,
                isTerminal = step.isTerminal,
                explanation = step.explanation,
                strategy = step.strategy,
                initialHashIndex = step.initialHashIndex,
                currentProbeIndex = step.currentProbeIndex,
                probedIndices = step.probedIndices,
                collisionEvent = step.event,
                maxChainDepth = maxChainDepth
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "collision_scene_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation,
                hashTableMeta = meta
            )
        }
    }
}
