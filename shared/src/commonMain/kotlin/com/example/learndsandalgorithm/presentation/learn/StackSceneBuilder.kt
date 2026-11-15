package com.example.learndsandalgorithm.presentation.learn

/** Builds stable stack snapshots from the existing pedagogical trace. */
internal object StackSceneBuilder {
    fun buildScenes(steps: List<StackTraceStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()
        val items = mutableListOf<Item>()
        return steps.mapIndexed { stepIndex, step ->
            if (stepIndex == 0) {
                step.values.forEachIndexed { index, value -> items += Item("stack:item:initial:$index", value) }
            } else {
                when (step.event) {
                    StackTraceEvent.PUSH -> items += Item("stack:item:step:$stepIndex", step.operationValue ?: step.values.last())
                    StackTraceEvent.POP, StackTraceEvent.DELIMITER_MATCH, StackTraceEvent.DELIMITER_MISMATCH -> if (items.isNotEmpty()) items.removeAt(items.lastIndex)
                    else -> Unit
                }
            }
            require(items.map { it.value } == step.values) { "Stack trace is inconsistent at step $stepIndex" }

            val arrayEntities = items.mapIndexed { index, item ->
                item.entity("stack_array:$index", if (index == step.highlightedIndex) stateFor(step.event) else EntityVisualState.VISITED)
            }
            val linkedItems = items.asReversed()
            val linkedEntities = linkedItems.mapIndexed { index, item ->
                val logicalIndex = items.lastIndex - index
                item.entity("stack_linked:$index", if (logicalIndex == step.highlightedIndex) stateFor(step.event) else EntityVisualState.VISITED)
            }
            val linkedConnections = linkedItems.zipWithNext().mapIndexed { index, (from, to) ->
                VisualConnection(
                    connectionKey = "stack:next:${from.id}->${to.id}",
                    fromVisualKey = from.id,
                    toVisualKey = to.id,
                    label = "NEXT",
                    type = ConnectionType.POINTER_LINK,
                    visualState = if (step.event == StackTraceEvent.PUSH && index == 0) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT,
                    directed = true
                )
            }
            val subScenes = buildList {
                add(StructureSubScene("stack_array", StructureType.STACK, minRequiredWidthDp = 140f, minRequiredHeightDp = 140f, entities = arrayEntities))
                if (step.representation == StackRepresentation.ARRAY_AND_LINKED) {
                    add(StructureSubScene("stack_linked", StructureType.LINKED_LIST, minRequiredWidthDp = 300f, minRequiredHeightDp = 100f, entities = linkedEntities, connections = linkedConnections))
                }
            }
            VisualizationScene(stepIndex, "stack_step_$stepIndex", subScenes, step.explanation)
        }
    }

    private data class Item(val id: String, val value: String) {
        fun entity(slot: String, state: EntityVisualState) = VisualEntity(
            visualKey = id,
            elementId = id,
            dataValue = value,
            slotKey = slot,
            visualState = state
        )
    }

    private fun stateFor(event: StackTraceEvent): EntityVisualState = when (event) {
        StackTraceEvent.DELIMITER_MISMATCH, StackTraceEvent.UNMATCHED_CLOSER, StackTraceEvent.UNDERFLOW, StackTraceEvent.OVERFLOW -> EntityVisualState.VIOLATION
        StackTraceEvent.PEEK, StackTraceEvent.DELIMITER_MATCH -> EntityVisualState.TARGET
        else -> EntityVisualState.ACTIVE
    }
}
