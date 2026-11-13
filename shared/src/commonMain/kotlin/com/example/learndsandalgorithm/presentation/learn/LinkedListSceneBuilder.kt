package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms Linked List trace models into abstract [VisualizationScene] models
 * with stable element identities (`elem:0`, `elem:1`, etc.) and explicit topology connections.
 */
internal object LinkedListSceneBuilder {

    fun buildIntroductionScenes(nodeValues: List<String>): List<VisualizationScene> {
        val n = nodeValues.size
        val identityAtSlot = Array(n) { i -> "elem:$i" }

        return (0 until 3).map { stepIndex ->
            val entities = mutableListOf<VisualEntity>()
            val connections = mutableListOf<VisualConnection>()

            // 1. HEAD pointer entity
            entities.add(
                VisualEntity(
                    visualKey = "linked_list:head_ptr",
                    elementId = "head_ptr",
                    dataValue = "HEAD",
                    slotKey = "slot:head",
                    visualState = if (stepIndex == 0 || stepIndex == 1) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT
                )
            )

            // 2. List Nodes
            for (i in 0 until n) {
                val elementId = identityAtSlot[i]
                val valStr = nodeValues[i]
                val isHighlighted = when (stepIndex) {
                    0 -> i == 0
                    1 -> true
                    2 -> i == 2
                    else -> false
                }
                val entityState = when {
                    isHighlighted && stepIndex == 2 -> EntityVisualState.VISITED
                    isHighlighted -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }

                entities.add(
                    VisualEntity(
                        visualKey = "linked_list:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "slot:$i",
                        badgeLabel = if (isHighlighted) "Current" else null,
                        visualState = entityState
                    )
                )

                // NEXT connection to subsequent node
                val nextSlotIdx = i + 1
                if (nextSlotIdx < n) {
                    val nextId = identityAtSlot[nextSlotIdx]
                    val isConnActive = (stepIndex == 0 && i == 0) || (stepIndex == 1 && i < n - 1) || (stepIndex == 2 && i == n - 1)
                    connections.add(
                        VisualConnection(
                            connectionKey = "link:$elementId->$nextId",
                            fromVisualKey = "linked_list:$elementId",
                            toVisualKey = "linked_list:$nextId",
                            visualState = if (isConnActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            // HEAD -> First Node connection
            if (n > 0) {
                val firstId = identityAtSlot[0]
                connections.add(
                    VisualConnection(
                        connectionKey = "link:head_ptr->$firstId",
                        fromVisualKey = "linked_list:head_ptr",
                        toVisualKey = "linked_list:$firstId",
                        visualState = ConnectionVisualState.HIGHLIGHTED
                    )
                )
            }

            // 3. NULL pointer entity & terminal connection
            val lastId = identityAtSlot[n - 1]
            val nullId = "null_ptr"
            entities.add(
                VisualEntity(
                    visualKey = "linked_list:$nullId",
                    elementId = nullId,
                    dataValue = "null",
                    slotKey = "slot:null",
                    visualState = if (stepIndex == 2) EntityVisualState.VISITED else EntityVisualState.DEFAULT
                )
            )
            connections.add(
                VisualConnection(
                    connectionKey = "link:$lastId->$nullId",
                    fromVisualKey = "linked_list:$lastId",
                    toVisualKey = "linked_list:$nullId",
                    visualState = if (stepIndex == 2) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                )
            )

            val subScene = StructureSubScene(
                subPaneId = "linked_list_pane",
                type = StructureType.LINKED_LIST,
                didacticPriority = 1,
                minRequiredWidthDp = 300f,
                minRequiredHeightDp = 140f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "intro_scene_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = "Step $stepIndex of introduction"
            )
        }
    }

    fun buildTraversalScenes(steps: List<LinkedListTraversalStep>, nodeValues: List<String>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()
        val n = nodeValues.size
        val identityAtSlot = Array(n) { i -> "elem:$i" }

        return steps.mapIndexed { stepIndex, step ->
            val entities = mutableListOf<VisualEntity>()
            val connections = mutableListOf<VisualConnection>()

            val headActive = step.currentNode == 0
            entities.add(
                VisualEntity(
                    visualKey = "linked_list:head_ptr",
                    elementId = "head_ptr",
                    dataValue = "HEAD",
                    slotKey = "slot:head",
                    visualState = if (headActive) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT
                )
            )

            if (n > 0) {
                val firstId = identityAtSlot[0]
                connections.add(
                    VisualConnection(
                        connectionKey = "link:head_ptr->$firstId",
                        fromVisualKey = "linked_list:head_ptr",
                        toVisualKey = "linked_list:$firstId",
                        visualState = ConnectionVisualState.HIGHLIGHTED
                    )
                )
            }

            for (i in 0 until n) {
                val elementId = identityAtSlot[i]
                val valStr = nodeValues[i]
                val isCurrent = step.currentNode == i
                val isVisited = step.visitedNodes.contains(i)

                val entityState = when {
                    isCurrent -> EntityVisualState.ACTIVE
                    isVisited -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }

                entities.add(
                    VisualEntity(
                        visualKey = "linked_list:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "slot:$i",
                        badgeLabel = if (isCurrent) "Current" else if (isVisited) "Visited" else null,
                        visualState = entityState
                    )
                )

                val nextSlotIdx = i + 1
                if (nextSlotIdx < n) {
                    val nextId = identityAtSlot[nextSlotIdx]
                    val isConnActive = isVisited && nextSlotIdx <= step.currentNode
                    connections.add(
                        VisualConnection(
                            connectionKey = "link:$elementId->$nextId",
                            fromVisualKey = "linked_list:$elementId",
                            toVisualKey = "linked_list:$nextId",
                            visualState = if (isConnActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            val lastId = identityAtSlot[n - 1]
            val nullId = "null_ptr"
            entities.add(
                VisualEntity(
                    visualKey = "linked_list:$nullId",
                    elementId = nullId,
                    dataValue = "null",
                    slotKey = "slot:null",
                    visualState = if (step.currentNode >= n) EntityVisualState.VISITED else EntityVisualState.DEFAULT
                )
            )
            connections.add(
                VisualConnection(
                    connectionKey = "link:$lastId->$nullId",
                    fromVisualKey = "linked_list:$lastId",
                    toVisualKey = "linked_list:$nullId",
                    visualState = if (step.currentNode >= n) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                )
            )

            val subScene = StructureSubScene(
                subPaneId = "linked_list_pane",
                type = StructureType.LINKED_LIST,
                didacticPriority = 1,
                minRequiredWidthDp = 300f,
                minRequiredHeightDp = 140f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "traversal_scene_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildInsertionScenes(steps: List<LinkedListOperationStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        // Assign stable identities to base list values and new inserted values
        val baseValues = listOf("12", "7", "19", "33", "45")
        val valueToId = mutableMapOf<String, String>()
        var baseCounter = 0
        baseValues.forEach { v ->
            if (!valueToId.containsKey(v)) {
                valueToId[v] = "elem:${baseCounter++}"
            }
        }
        var newCounter = 0

        return steps.mapIndexed { stepIndex, step ->
            val entities = mutableListOf<VisualEntity>()
            val connections = mutableListOf<VisualConnection>()

            // 1. HEAD pointer
            entities.add(
                VisualEntity(
                    visualKey = "linked_list:head_ptr",
                    elementId = "head_ptr",
                    dataValue = "HEAD",
                    slotKey = "slot:head",
                    visualState = EntityVisualState.ACTIVE
                )
            )

            val mainOrder = step.order
            // Ensure all values in mainOrder have an ID
            mainOrder.forEach { valStr ->
                if (!valueToId.containsKey(valStr)) {
                    valueToId[valStr] = "elem:new_${newCounter++}"
                }
            }

            // Head node connection
            val headVal = step.head
            val headNodeId = valueToId[headVal]
            if (headNodeId != null) {
                connections.add(
                    VisualConnection(
                        connectionKey = "link:head_ptr->$headNodeId",
                        fromVisualKey = "linked_list:head_ptr",
                        toVisualKey = "linked_list:$headNodeId",
                        visualState = ConnectionVisualState.HIGHLIGHTED
                    )
                )
            }

            // 2. Main Chain Nodes
            mainOrder.forEachIndexed { index, valStr ->
                val elementId = valueToId[valStr] ?: "elem:fallback_$index"
                val isHighlighted = valStr in step.highlighted || valStr == step.activeNode
                val badge = step.badges[valStr]

                val entityState = when {
                    isHighlighted -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }

                entities.add(
                    VisualEntity(
                        visualKey = "linked_list:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "slot:$index",
                        badgeLabel = badge,
                        visualState = entityState
                    )
                )
            }

            // 3. Explicit Links from step.links
            val linkedTargetValues = mutableSetOf<String>()
            step.links.forEach { (fromVal, toVal) ->
                val fromId = valueToId[fromVal]
                val toId = valueToId[toVal]
                if (fromId != null && toId != null) {
                    linkedTargetValues.add(toVal)
                    val isHigh = fromVal == step.activeNode || toVal == step.activeNode
                    connections.add(
                        VisualConnection(
                            connectionKey = "link:$fromId->$toId",
                            fromVisualKey = "linked_list:$fromId",
                            toVisualKey = "linked_list:$toId",
                            visualState = if (isHigh) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            // 4. NULL pointer entity & terminal connection for nodes not linking elsewhere
            val nullId = "null_ptr"
            entities.add(
                VisualEntity(
                    visualKey = "linked_list:$nullId",
                    elementId = nullId,
                    dataValue = "null",
                    slotKey = "slot:null",
                    visualState = EntityVisualState.DEFAULT
                )
            )

            mainOrder.forEach { valStr ->
                val nodeId = valueToId[valStr]
                if (nodeId != null && !step.links.any { it.first == valStr }) {
                    connections.add(
                        VisualConnection(
                            connectionKey = "link:$nodeId->$nullId",
                            fromVisualKey = "linked_list:$nodeId",
                            toVisualKey = "linked_list:$nullId",
                            visualState = ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            // 5. Detached nodes
            step.detachedNodes.forEachIndexed { dIdx, valStr ->
                if (!valueToId.containsKey(valStr)) {
                    valueToId[valStr] = "elem:detached_$dIdx"
                }
                val elementId = valueToId[valStr] ?: "elem:detached_$dIdx"
                val badge = step.badges[valStr] ?: "NEW"
                entities.add(
                    VisualEntity(
                        visualKey = "linked_list:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "slot:detached_$dIdx",
                        badgeLabel = badge,
                        visualState = EntityVisualState.ACTIVE
                    )
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "linked_list_pane",
                type = StructureType.LINKED_LIST,
                didacticPriority = 1,
                minRequiredWidthDp = 300f,
                minRequiredHeightDp = 180f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "insert_scene_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildRemovalScenes(steps: List<LinkedListOperationStep>): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val baseValues = listOf("12", "7", "19", "33", "45")
        val valueToId = mutableMapOf<String, String>()
        var baseCounter = 0
        baseValues.forEach { v ->
            if (!valueToId.containsKey(v)) {
                valueToId[v] = "elem:${baseCounter++}"
            }
        }

        return steps.mapIndexed { stepIndex, step ->
            val entities = mutableListOf<VisualEntity>()
            val connections = mutableListOf<VisualConnection>()

            entities.add(
                VisualEntity(
                    visualKey = "linked_list:head_ptr",
                    elementId = "head_ptr",
                    dataValue = "HEAD",
                    slotKey = "slot:head",
                    visualState = EntityVisualState.ACTIVE
                )
            )

            val mainOrder = step.order
            mainOrder.forEach { valStr ->
                if (!valueToId.containsKey(valStr)) {
                    valueToId[valStr] = "elem:rem_${baseCounter++}"
                }
            }
            step.detachedNodes.forEach { valStr ->
                if (!valueToId.containsKey(valStr)) {
                    valueToId[valStr] = "elem:rem_${baseCounter++}"
                }
            }

            val headVal = step.head
            val headNodeId = valueToId[headVal]
            if (headNodeId != null) {
                connections.add(
                    VisualConnection(
                        connectionKey = "link:head_ptr->$headNodeId",
                        fromVisualKey = "linked_list:head_ptr",
                        toVisualKey = "linked_list:$headNodeId",
                        visualState = ConnectionVisualState.HIGHLIGHTED
                    )
                )
            }

            mainOrder.forEachIndexed { index, valStr ->
                val elementId = valueToId[valStr] ?: "elem:fallback_$index"
                val isHighlighted = valStr in step.highlighted || valStr == step.activeNode
                val badge = step.badges[valStr]

                val entityState = when {
                    isHighlighted -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }

                entities.add(
                    VisualEntity(
                        visualKey = "linked_list:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "slot:$index",
                        badgeLabel = badge,
                        visualState = entityState
                    )
                )
            }

            step.links.forEach { (fromVal, toVal) ->
                val fromId = valueToId[fromVal]
                val toId = valueToId[toVal]
                if (fromId != null && toId != null) {
                    val isHigh = fromVal == step.activeNode || toVal == step.activeNode
                    connections.add(
                        VisualConnection(
                            connectionKey = "link:$fromId->$toId",
                            fromVisualKey = "linked_list:$fromId",
                            toVisualKey = "linked_list:$toId",
                            visualState = if (isHigh) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            val nullId = "null_ptr"
            entities.add(
                VisualEntity(
                    visualKey = "linked_list:$nullId",
                    elementId = nullId,
                    dataValue = "null",
                    slotKey = "slot:null",
                    visualState = EntityVisualState.DEFAULT
                )
            )

            mainOrder.forEach { valStr ->
                val nodeId = valueToId[valStr]
                if (nodeId != null && !step.links.any { it.first == valStr }) {
                    connections.add(
                        VisualConnection(
                            connectionKey = "link:$nodeId->$nullId",
                            fromVisualKey = "linked_list:$nodeId",
                            toVisualKey = "linked_list:$nullId",
                            visualState = ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            step.detachedNodes.forEachIndexed { dIdx, valStr ->
                val elementId = valueToId[valStr] ?: "elem:detached_$dIdx"
                val badge = step.badges[valStr] ?: "REMOVED"
                entities.add(
                    VisualEntity(
                        visualKey = "linked_list:$elementId",
                        elementId = elementId,
                        dataValue = valStr,
                        slotKey = "slot:detached_$dIdx",
                        badgeLabel = badge,
                        visualState = EntityVisualState.ACTIVE
                    )
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "linked_list_pane",
                type = StructureType.LINKED_LIST,
                didacticPriority = 1,
                minRequiredWidthDp = 300f,
                minRequiredHeightDp = 180f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "removal_scene_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }
}
