package com.example.learndsandalgorithm.presentation.learn

internal object HashTableApplicationsSceneBuilder {
    fun buildScenes(steps: List<ApplicationsTraceStep>): List<VisualizationScene> =
        steps.mapIndexed { index, step ->
            val entities = when (step.scenario) {
                ApplicationScenario.FREQUENCY_COUNTER -> buildFrequencyEntities(step)
                ApplicationScenario.RANGE_QUERY_COMPARISON -> buildRangeEntities(step)
                ApplicationScenario.MUTABLE_KEY_PITFALL -> buildMutableKeyEntities(step)
            }
            VisualizationScene(
                stepIndex = index,
                sceneId = "hash_applications_${step.scenario.name.lowercase()}_$index",
                subScenes = listOf(
                    StructureSubScene(
                        subPaneId = "applications_${step.scenario.name.lowercase()}",
                        type = HashTableApplicationsSceneLayout.structureType(step.scenario),
                        minRequiredWidthDp = HashTableApplicationsSceneLayout.sceneWidth(step.scenario),
                        minRequiredHeightDp = HashTableApplicationsSceneLayout.sceneHeight(step.scenario),
                        entities = entities
                    )
                ),
                globalMessage = step.explanation
            )
        }

    private fun buildFrequencyEntities(step: ApplicationsTraceStep): List<VisualEntity> = buildList {
        step.inputText.orEmpty().forEachIndexed { index, char ->
            add(
                VisualEntity(
                    visualKey = "frequency:input:$index",
                    elementId = "frequency:input:$index",
                    dataValue = char.toString(),
                    slotKey = "input:$index",
                    visualState = if (index == step.currentCharIndex) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT
                )
            )
        }
        step.freqMap.forEach { pair ->
            add(
                VisualEntity(
                    visualKey = "frequency:map:${pair.char}",
                    elementId = "frequency:char:${pair.char}",
                    dataValue = pair.count.toString(),
                    slotKey = "map:${pair.char}",
                    badgeLabel = "count: ${pair.count}",
                    visualState = if (pair.char == step.currentChar) EntityVisualState.ACTIVE else EntityVisualState.VISITED,
                    entryKey = pair.char.toString(),
                    entryValue = pair.count.toString()
                )
            )
        }
    }

    private fun buildRangeEntities(step: ApplicationsTraceStep): List<VisualEntity> = buildList {
        step.hashBucketKeys.orEmpty().forEachIndexed { index, value ->
            add(
                VisualEntity(
                    visualKey = "range:hash:$index",
                    elementId = "range:hash:$index",
                    dataValue = value?.toString() ?: "·",
                    slotKey = "hash:$index",
                    visualState = when {
                        index !in step.hashVisitedBuckets -> EntityVisualState.DEFAULT
                        value != null && value in (step.rangeMin ?: 0)..(step.rangeMax ?: 0) -> EntityVisualState.TARGET
                        else -> EntityVisualState.ACTIVE
                    }
                )
            )
        }
        step.bstKeys.orEmpty().forEachIndexed { index, value ->
            add(
                VisualEntity(
                    visualKey = "range:bst:$value",
                    elementId = "range:value:$value",
                    dataValue = value.toString(),
                    slotKey = "bst:$index",
                    visualState = if (value in step.bstVisitedKeys) EntityVisualState.TARGET else EntityVisualState.DEFAULT
                )
            )
        }
    }

    private fun buildMutableKeyEntities(step: ApplicationsTraceStep): List<VisualEntity> {
        return listOf(
            VisualEntity(
                visualKey = "mutable:user",
                elementId = "mutable:user",
                dataValue = "id:${if (step.keyMutated) step.mutatedKeyId else step.originalKeyId}",
                slotKey = "mutable:slot:${step.storedSlotIndex}",
                badgeLabel = if (step.keyMutated) "KEY MUTATED IN-PLACE" else null,
                visualState = if (step.keyMutated) EntityVisualState.VIOLATION else EntityVisualState.VISITED,
                entryKey = "User",
                entryValue = (if (step.keyMutated) step.mutatedKeyId else step.originalKeyId).toString()
            )
        )
    }
}
