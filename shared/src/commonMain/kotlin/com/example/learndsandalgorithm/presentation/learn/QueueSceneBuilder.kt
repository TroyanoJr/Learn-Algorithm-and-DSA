package com.example.learndsandalgorithm.presentation.learn

/** Reconstructs FIFO item occurrences so duplicate values keep distinct identities. */
internal object QueueSceneBuilder {
    fun buildScenes(steps: List<QueueTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()
        val items = mutableListOf<Item>()
        return steps.mapIndexed { stepIndex, step ->
            if (stepIndex == 0) step.values.forEachIndexed { index, value -> items += Item("queue:item:initial:$index", value) }
            else when (step.event) {
                QueueTraceEvent.ENQUEUE -> items += Item("queue:item:step:$stepIndex", step.operationValue.orEmpty())
                QueueTraceEvent.DEQUEUE -> if (items.isNotEmpty()) items.removeAt(0)
                else -> Unit
            }
            require(items.map { it.value } == step.values) { "Queue trace is inconsistent at step $stepIndex" }

            val arrayEntities = items.mapIndexed { offset, item ->
                val physicalSlot = (step.frontIndex + offset) % step.capacity
                item.entity("queue_array:$physicalSlot", if (step.highlightedSlot == physicalSlot) stateFor(step.event) else EntityVisualState.VISITED)
            }
            val linkedEntities = items.mapIndexed { index, item ->
                item.entity("queue_linked:$index", if (step.event == QueueTraceEvent.ENQUEUE && index == items.lastIndex) EntityVisualState.ACTIVE else EntityVisualState.VISITED)
            }
            val links = items.zipWithNext().map { (from, to) ->
                VisualConnection(
                    connectionKey = "queue:next:${from.id}->${to.id}",
                    fromVisualKey = from.id,
                    toVisualKey = to.id,
                    label = "NEXT",
                    type = ConnectionType.POINTER_LINK,
                    visualState = if (step.event == QueueTraceEvent.ENQUEUE && to == items.last()) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT,
                    directed = true
                )
            }
            val subScenes = buildList {
                add(StructureSubScene("queue_array", StructureType.QUEUE, minRequiredWidthDp = step.capacity * 64f, minRequiredHeightDp = 104f, entities = arrayEntities))
                if (step.representation == QueueRepresentation.ARRAY_AND_LINKED) {
                    add(StructureSubScene("queue_linked", StructureType.LINKED_LIST, minRequiredWidthDp = maxOf(step.capacity, items.size) * 78f, minRequiredHeightDp = 100f, entities = linkedEntities, connections = links))
                }
            }
            VisualizationScene(stepIndex, "queue_step_$stepIndex", subScenes, step.explanation)
        }
    }

    private data class Item(val id: String, val value: String) {
        fun entity(slot: String, state: EntityVisualState) = VisualEntity(id, id, value, slot, visualState = state)
    }

    private fun stateFor(event: QueueTraceEvent): EntityVisualState = when (event) {
        QueueTraceEvent.OVERFLOW, QueueTraceEvent.UNDERFLOW, QueueTraceEvent.EMPTY_ACCESS -> EntityVisualState.VIOLATION
        QueueTraceEvent.PEEK -> EntityVisualState.TARGET
        else -> EntityVisualState.ACTIVE
    }
}
