package com.example.learndsandalgorithm.presentation.learn

internal data class ArraySceneRegion(
    val key: String,
    val slotCount: Int,
    val layoutSlotCount: Int,
    val columns: Int,
    val headerText: String? = null,
    val visible: Boolean = true,
    val highlightedIndices: Set<Int> = emptySet(),
    val cellHeightDp: Float = 36f,
    val columnGapDp: Float = 14f
)

internal data class ArraySceneFrame(
    val scene: VisualizationScene,
    val regions: List<ArraySceneRegion>,
    val statusText: String = "",
    val snapshotIndex: Int = 0,
    val snapshotCount: Int = 1,
    val isCompleted: Boolean = false,
    val detailsLabel: String = "Steps",
    val footer: String? = null
) {
    val explanation: String get() = scene.globalMessage.orEmpty()
}

internal object ArraySceneBuilder {
    fun buildStaticArray(
        values: List<String>,
        highlightedIndices: Set<Int>,
        explanation: String
    ): ArraySceneFrame {
        val region = ArraySceneRegion("array", values.size, values.size, values.size, cellHeightDp = 44f, columnGapDp = 6f)
        return frame(
            stepIndex = 0,
            explanation = explanation,
            regions = listOf(region),
            slotsByRegion = mapOf("array" to values.map { it as String? }),
            idsByRegion = mapOf("array" to values.indices.associateWith { "array:item:$it" }),
            highlightsByRegion = mapOf("array" to highlightedIndices)
        )
    }

    fun buildResizeFrames(steps: List<ArrayResizeStep>): List<ArraySceneFrame> {
        if (steps.isEmpty()) return emptyList()
        val initialValues = steps.first().storageSlots.dropLast(1)
        val ids = initialValues.indices.associateWith { "array:item:initial:$it" }.toMutableMap()
        val initialCapacity = steps.first().storageSlots.size
        val newCapacity = steps.maxOf { it.storageSlots.size }
        val count = steps.size

        return steps.mapIndexed { index, step ->
            val hasPreserved = step.preservedStorageSlots != null
            val topVisible = index <= 2
            val bottomVisible = index >= 2
            val topSlots = when {
                hasPreserved -> step.preservedStorageSlots.orEmpty()
                index <= 1 -> step.storageSlots
                else -> List(steps.first().storageSlots.size) { null }
            }
            val bottomSlots = if (bottomVisible) step.storageSlots else List(newCapacity) { null }

            val topIds = topSlots.indices.mapNotNull { slot ->
                if (topSlots[slot].isNullOrBlank()) null
                else slot to ids.getOrPut(slot) { "array:item:${topSlots[slot]}" }
            }.toMap()
            val bottomIds = bottomSlots.indices.mapNotNull { slot ->
                if (bottomSlots[slot].isNullOrBlank()) null
                else slot to if (slot < initialValues.size) "array:item:initial:$slot" else "array:item:${bottomSlots[slot]}"
            }.toMap()

            val topLabel = when {
                hasPreserved -> "OLD ARRAY (PRESERVED)"
                index <= 1 -> step.stageLabel
                else -> null
            }
            val bottomLabel = if (bottomVisible) step.stageLabel else null
            val topRegion = region("primary", topSlots, initialCapacity, topLabel, topVisible, cellHeightDp = 44f)
            val bottomRegion = region(
                "secondary", bottomSlots, newCapacity, bottomLabel, bottomVisible,
                logicalSize = if (index >= 2) topSlots.count { !it.isNullOrBlank() } else bottomSlots.count { !it.isNullOrBlank() },
                cellHeightDp = 44f
            )
            val regionList = listOf(topRegion, bottomRegion)
            val status = when {
                index == 0 -> "READY"
                index == steps.lastIndex -> "COMPLETED"
                else -> "TRANSITION $index OF ${steps.lastIndex}"
            }
            frame(
                stepIndex = index,
                explanation = step.explanationText,
                regions = regionList,
                slotsByRegion = mapOf("primary" to topSlots, "secondary" to bottomSlots),
                idsByRegion = mapOf("primary" to topIds, "secondary" to bottomIds),
                highlightsByRegion = mapOf(
                    "primary" to if (index <= 1) step.highlightedIndices else emptySet(),
                    "secondary" to if (index >= 3) step.highlightedIndices else emptySet()
                ),
                statusText = status,
                snapshotCount = count,
                isCompleted = index == steps.lastIndex,
                detailsLabel = "Size vs Capacity",
                footer = "SIZE = values stored | CAPACITY = allocated slots"
            )
        }
    }

    fun buildTraversalFrames(steps: List<ArrayTraversalStep>): List<ArraySceneFrame> =
        steps.mapIndexed { index, step ->
            val values = step.arrayData
            val region = ArraySceneRegion("array", values.size, values.size, values.size, columnGapDp = 14f)
            val highlighted = if (step.currentIndex in values.indices) setOf(step.currentIndex) else emptySet()
            val status = when {
                step.badgeText == "READY" -> "READY"
                step.isCompleted -> "COMPLETED"
                else -> "VISIT ${step.currentIndex + 1} OF ${values.size}"
            }
            frame(
                stepIndex = index,
                explanation = step.explanationText,
                regions = listOf(region),
                slotsByRegion = mapOf("array" to values.map { it as String? }),
                idsByRegion = mapOf("array" to values.indices.associateWith { "array:item:$it" }),
                highlightsByRegion = mapOf("array" to highlighted),
                statusText = status,
                snapshotCount = steps.size,
                isCompleted = step.isCompleted,
                detailsLabel = "Traversal Status",
                footer = "${step.visitedCount} / ${values.size} VISITED"
            )
        }

    fun buildInsertDeleteFrames(steps: List<ArrayInsertDeleteStep>, operationMode: String): List<ArraySceneFrame> {
        if (steps.isEmpty()) return emptyList()
        val insertion = operationMode == "INSERT"
        val identities: MutableMap<Int, String> = steps.first().displayItems.mapIndexedNotNull { slot, value ->
            if (value.isBlank()) null else slot to "array:item:initial:$slot"
        }.toMap().toMutableMap()
        val maximumSlots = steps.maxOf { it.displayItems.size }

        return steps.mapIndexed { index, step ->
            if (index > 0) {
                when {
                    insertion && index in 2..9 -> {
                        val source = 11 - index
                        identities.remove(source)?.let { identities[source + 1] = it }
                    }
                    insertion && index == steps.lastIndex -> identities[2] = "array:item:inserted:25"
                    !insertion && index == 1 -> identities.remove(1)
                    !insertion && index in 2..9 -> {
                        val source = index
                        identities.remove(source)?.let { identities[source - 1] = it }
                    }
                }
            }
            val slots = step.displayItems
            require(slots.indices.filter { !slots[it].isBlank() }.all { identities[it] != null }) {
                "Array identity mapping is incomplete for $operationMode snapshot $index"
            }
            val region = ArraySceneRegion("array", slots.size, maximumSlots, maximumSlots, columnGapDp = 14f)
            frame(
                stepIndex = index,
                explanation = step.explanationText,
                regions = listOf(region),
                slotsByRegion = mapOf("array" to slots.map { it as String? }),
                idsByRegion = mapOf("array" to identities.toMap()),
                highlightsByRegion = mapOf("array" to step.highlightedIndices),
                statusText = when {
                    index == 0 -> "READY"
                    step.isCompleted -> "COMPLETED"
                    else -> "TRANSITION $index OF ${steps.lastIndex}"
                },
                snapshotCount = steps.size,
                isCompleted = step.isCompleted,
                detailsLabel = if (insertion) "Insertion Step" else "Deletion Step"
            )
        }
    }

    private fun region(
        key: String,
        slots: List<String?>,
        layoutSlots: Int,
        label: String?,
        visible: Boolean,
        logicalSize: Int = slots.count { !it.isNullOrBlank() },
        cellHeightDp: Float = 36f
    ) = ArraySceneRegion(
        key = key,
        slotCount = slots.size,
        layoutSlotCount = layoutSlots,
        columns = if (key == "primary" || key == "secondary") layoutSlots.coerceAtLeast(1) else if (layoutSlots <= 16) 8 else 10,
        headerText = label?.let { "$it · capacity ${slots.size} · size $logicalSize · occupied ${slots.count { value -> !value.isNullOrBlank() }}" },
        visible = visible,
        cellHeightDp = cellHeightDp,
        columnGapDp = 4f
    )

    private fun frame(
        stepIndex: Int,
        explanation: String,
        regions: List<ArraySceneRegion>,
        slotsByRegion: Map<String, List<String?>>,
        idsByRegion: Map<String, Map<Int, String>>,
        highlightsByRegion: Map<String, Set<Int>>,
        statusText: String = "",
        snapshotCount: Int = 1,
        isCompleted: Boolean = false,
        detailsLabel: String = "Steps",
        footer: String? = null
    ): ArraySceneFrame {
        val renderedRegions = regions.map { region ->
            region.copy(highlightedIndices = highlightsByRegion[region.key].orEmpty())
        }
        val subScenes = renderedRegions.filter { it.visible }.map { region ->
            val values = slotsByRegion[region.key].orEmpty()
            val ids = idsByRegion[region.key].orEmpty()
            val entities = values.mapIndexedNotNull { slot, value ->
                if (value.isNullOrBlank()) return@mapIndexedNotNull null
                val elementId = ids[slot] ?: error("Missing stable identity for ${region.key}[$slot]")
                VisualEntity(
                    visualKey = elementId,
                    elementId = elementId,
                    dataValue = value,
                    slotKey = "${region.key}:$slot",
                    visualState = if (slot in region.highlightedIndices) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT
                )
            }
            StructureSubScene(
                subPaneId = region.key,
                type = StructureType.ARRAY,
                minRequiredWidthDp = 120f,
                minRequiredHeightDp = 60f,
                entities = entities
            )
        }
        return ArraySceneFrame(
            scene = VisualizationScene(stepIndex, "array_step_$stepIndex", subScenes, explanation),
            regions = renderedRegions,
            statusText = statusText,
            snapshotIndex = stepIndex,
            snapshotCount = snapshotCount,
            isCompleted = isCompleted,
            detailsLabel = detailsLabel,
            footer = footer
        )
    }
}
