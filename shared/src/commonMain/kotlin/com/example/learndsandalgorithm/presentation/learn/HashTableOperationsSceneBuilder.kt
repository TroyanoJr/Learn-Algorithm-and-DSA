package com.example.learndsandalgorithm.presentation.learn

internal object HashTableOperationsSceneBuilder {
    fun buildScenes(steps: List<OperationsTraceStep>): List<VisualizationScene> =
        steps.mapIndexed { index, step -> buildScene(index, step) }

    private fun buildScene(index: Int, step: OperationsTraceStep): VisualizationScene {
        fun entity(entry: HashOpEntry, slot: Int, pane: String, highlighted: Boolean) = VisualEntity(
            visualKey = "$pane:${entry.key}",
            elementId = "hash-op:${entry.key}",
            dataValue = "${entry.key}\n${entry.value}",
            slotKey = "$pane:slot:$slot",
            badgeLabel = if (highlighted) step.activeOp.name else null,
            visualState = if (highlighted) when (step.activeOp) {
                HashOpType.DELETE -> EntityVisualState.VIOLATION
                HashOpType.LOOKUP -> EntityVisualState.TARGET
                else -> EntityVisualState.ACTIVE
            } else EntityVisualState.VISITED,
            entryKey = entry.key,
            entryValue = entry.value
        )

        val oldEntities = step.oldBuckets.orEmpty().mapIndexedNotNull { slot, entry ->
            entry?.let { entity(it, slot, "operations_old", it.key == step.activeKey) }
        }
        val currentEntities = step.buckets.mapIndexedNotNull { slot, entry ->
            entry?.let { entity(it, slot, "operations_current", slot == step.highlightedIndex) }
        }
        val connections = if (step.activeOp == HashOpType.RESIZE_REHASH && step.targetIndex != null && step.activeKey != null &&
            oldEntities.any { it.entryKey == step.activeKey } && currentEntities.any { it.entryKey == step.activeKey }
        ) listOf(
            VisualConnection(
                connectionKey = "rehash:${step.activeKey}",
                fromVisualKey = "operations_old:${step.activeKey}",
                toVisualKey = "operations_current:${step.activeKey}",
                label = "rehash",
                visualState = ConnectionVisualState.HIGHLIGHTED,
                directed = true
            )
        ) else emptyList()

        return VisualizationScene(
            stepIndex = index,
            sceneId = "hash_operations_step_$index",
            subScenes = listOf(
                StructureSubScene(
                    subPaneId = "operations_old",
                    type = StructureType.HASH_TABLE,
                    didacticPriority = 2,
                    minRequiredWidthDp = 260f,
                    minRequiredHeightDp = 48f,
                    entities = oldEntities
                ),
                StructureSubScene(
                    subPaneId = "operations_current",
                    type = StructureType.HASH_TABLE,
                    didacticPriority = 1,
                    minRequiredWidthDp = 520f,
                    minRequiredHeightDp = 78f,
                    entities = currentEntities,
                    connections = connections
                )
            ),
            globalMessage = step.explanation
        )
    }
}
