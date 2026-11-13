package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms tree traversal [TreeTraversalsTraceStep] sequences
 * (PRE_ORDER, IN_ORDER, POST_ORDER, LEVEL_ORDER) into abstract [VisualizationScene] models.
 *
 * Generates three synchronized sub-scenes per step:
 * 1. `tree_traversal_tree`: Full binary tree structure with structural branch connections.
 * 2. `tree_traversal_queue`: BFS queue entries (empty for DFS traversals).
 * 3. `tree_traversal_visited`: Visited node array sequence in order of traversal.
 */
internal object TreeTraversalSceneBuilder {

    fun buildTraversalScenes(
        steps: List<TreeTraversalsTraceStep>,
        nodes: List<TraversalNode> = TreeTraversalsTrace.treeNodes
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        // 1. Validate node ID uniqueness
        val nodeIds = nodes.map { it.id }
        require(nodeIds.size == nodeIds.toSet().size) {
            "TreeTraversalSceneBuilder requires unique node IDs in tree definition"
        }

        // 2. Validate node value uniqueness (required because queueValues and visitedNodes store Ints)
        val nodeValues = nodes.map { it.value }
        require(nodeValues.size == nodeValues.toSet().size) {
            "TreeTraversalSceneBuilder requires unique node values in tree definition for value-to-node mapping"
        }

        val nodeMap = nodes.associateBy { it.id }
        val valueToNodeMap = nodes.associateBy { it.value }

        // 3. Validate leftId and rightId child references
        nodes.forEach { node ->
            node.leftId?.let { leftId ->
                require(nodeMap.containsKey(leftId)) {
                    "Node ${node.id} references invalid leftId $leftId"
                }
            }
            node.rightId?.let { rightId ->
                require(nodeMap.containsKey(rightId)) {
                    "Node ${node.id} references invalid rightId $rightId"
                }
            }
        }

        // 4. Validate steps consistency with node map
        steps.forEachIndexed { stepIdx, step ->
            step.currentNodeId?.let { currId ->
                require(nodeMap.containsKey(currId)) {
                    "Step $stepIdx references invalid currentNodeId $currId"
                }
            }

            step.queueValues.forEach { qVal ->
                require(valueToNodeMap.containsKey(qVal)) {
                    "Step $stepIdx queueValues contains unknown value $qVal"
                }
            }

            step.visitedNodes.forEach { vVal ->
                require(valueToNodeMap.containsKey(vVal)) {
                    "Step $stepIdx visitedNodes contains unknown value $vVal"
                }
            }
        }

        return steps.mapIndexed { stepIndex, step ->
            buildSceneForStep(
                stepIndex = stepIndex,
                step = step,
                nodes = nodes,
                valueToNodeMap = valueToNodeMap
            )
        }
    }

    /**
     * Alias for backward compatibility with existing Level-Order BFS callers.
     */
    fun buildLevelOrderScenes(
        steps: List<TreeTraversalsTraceStep>,
        nodes: List<TraversalNode> = TreeTraversalsTrace.treeNodes
    ): List<VisualizationScene> = buildTraversalScenes(steps, nodes)

    private fun buildSceneForStep(
        stepIndex: Int,
        step: TreeTraversalsTraceStep,
        nodes: List<TraversalNode>,
        valueToNodeMap: Map<Int, TraversalNode>
    ): VisualizationScene {
        // --- 1. Tree Sub-Scene ---
        val treeEntities = mutableListOf<VisualEntity>()
        val treeConnections = mutableListOf<VisualConnection>()

        nodes.forEach { node ->
            val elementId = "node:${node.id}"
            val isActive = node.id == step.currentNodeId
            val isVisited = node.value in step.visitedNodes

            val visualState = when {
                isActive -> EntityVisualState.ACTIVE
                isVisited -> EntityVisualState.VISITED
                else -> EntityVisualState.DEFAULT
            }

            val badge = when {
                isActive -> "Active"
                isVisited -> "Visited"
                else -> null
            }

            treeEntities.add(
                VisualEntity(
                    visualKey = "tree:node:${node.id}",
                    elementId = elementId,
                    dataValue = node.value.toString(),
                    slotKey = "tree_slot:${node.id}",
                    badgeLabel = badge,
                    visualState = visualState
                )
            )

            // Left child branch
            node.leftId?.let { leftChildId ->
                val isLeftBranchActive = isActive && step.queueValues.any { valueToNodeMap[it]?.id == leftChildId }
                treeConnections.add(
                    VisualConnection(
                        connectionKey = "branch:tree_slot:${node.id}->tree_slot:$leftChildId",
                        fromVisualKey = "tree:node:${node.id}",
                        toVisualKey = "tree:node:$leftChildId",
                        label = "L",
                        type = ConnectionType.STRUCTURAL_BRANCH,
                        visualState = if (isLeftBranchActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                    )
                )
            }

            // Right child branch
            node.rightId?.let { rightChildId ->
                val isRightBranchActive = isActive && step.queueValues.any { valueToNodeMap[it]?.id == rightChildId }
                treeConnections.add(
                    VisualConnection(
                        connectionKey = "branch:tree_slot:${node.id}->tree_slot:$rightChildId",
                        fromVisualKey = "tree:node:${node.id}",
                        toVisualKey = "tree:node:$rightChildId",
                        label = "R",
                        type = ConnectionType.STRUCTURAL_BRANCH,
                        visualState = if (isRightBranchActive) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                    )
                )
            }
        }

        val treeSubScene = StructureSubScene(
            subPaneId = "tree_traversal_tree",
            type = StructureType.TREE,
            didacticPriority = 1,
            minRequiredWidthDp = 220f,
            minRequiredHeightDp = 200f,
            entities = treeEntities,
            connections = treeConnections
        )

        // --- 2. Queue Sub-Scene (Populated for BFS, empty list for DFS) ---
        val queueEntities = step.queueValues.mapIndexed { idx, qVal ->
            val targetNode = valueToNodeMap.getValue(qVal)
            VisualEntity(
                visualKey = "queue:node:${targetNode.id}",
                elementId = "node:${targetNode.id}",
                dataValue = qVal.toString(),
                slotKey = "queue_slot:$idx",
                occurrenceId = "q_occ:${targetNode.id}",
                badgeLabel = if (idx == 0) "Front" else null,
                visualState = EntityVisualState.DEFAULT
            )
        }

        val queueSubScene = StructureSubScene(
            subPaneId = "tree_traversal_queue",
            type = StructureType.QUEUE,
            didacticPriority = 2,
            minRequiredWidthDp = 200f,
            minRequiredHeightDp = 50f,
            entities = queueEntities,
            connections = emptyList()
        )

        // --- 3. Visited Sub-Scene ---
        val visitedEntities = step.visitedNodes.mapIndexed { idx, vVal ->
            val targetNode = valueToNodeMap.getValue(vVal)
            val isJustVisited = idx == step.visitedNodes.lastIndex
            VisualEntity(
                visualKey = "visited:node:${targetNode.id}",
                elementId = "node:${targetNode.id}",
                dataValue = vVal.toString(),
                slotKey = "visited_slot:$idx",
                badgeLabel = if (isJustVisited) "New" else null,
                visualState = if (isJustVisited) EntityVisualState.ACTIVE else EntityVisualState.VISITED
            )
        }

        val visitedSubScene = StructureSubScene(
            subPaneId = "tree_traversal_visited",
            type = StructureType.ARRAY,
            didacticPriority = 2,
            minRequiredWidthDp = 200f,
            minRequiredHeightDp = 50f,
            entities = visitedEntities,
            connections = emptyList()
        )

        return VisualizationScene(
            stepIndex = stepIndex,
            sceneId = "tree_traversal_scene_step_$stepIndex",
            subScenes = listOf(treeSubScene, queueSubScene, visitedSubScene),
            globalMessage = step.explanation
        )
    }
}
