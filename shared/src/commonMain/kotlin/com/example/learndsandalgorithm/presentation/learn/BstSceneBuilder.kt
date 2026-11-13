package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms Binary Search Tree trace sequences
 * ([BinaryTreeBstTraceStep] and [BstOperationsTraceStep]) into abstract [VisualizationScene] models.
 * Separates sequence-wide reference geometry (chronologically built referenceConnections enforcing valid binary tree invariants)
 * from active step rendering topology (step.nodes active connections).
 */
internal object BstSceneBuilder {

    fun buildBstComparisonScenes(
        steps: List<BinaryTreeBstTraceStep>
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val allNodesMap = mutableMapOf<String, BstNodeData>()
        steps.forEach { step ->
            step.nodes.forEach { node ->
                allNodesMap[node.id] = node
            }
        }
        val allNodes = allNodesMap.values.toList()

        val referenceConnections = buildChronologicalReferenceConnectionsComparison(steps, allNodesMap)

        return steps.mapIndexed { stepIndex, step ->
            val stepNodeMap = step.nodes.associateBy { it.id }

            val entities = allNodes.map { nodeDef ->
                val stepNode = stepNodeMap[nodeDef.id]
                val isActive = stepNode != null && stepNode.id == step.inspectedNodeId
                val isViolation = stepNode != null && stepNode.isViolation
                val isPresent = stepNode != null

                // State Priority: VIOLATION > ACTIVE > DEFAULT
                val visualState = when {
                    !isPresent -> EntityVisualState.DEFAULT
                    isViolation -> EntityVisualState.VIOLATION
                    isActive -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }
                val badge = when {
                    !isPresent -> null
                    isViolation -> "Violation"
                    isActive -> "Inspecting"
                    else -> null
                }
                val presenceState = if (isPresent) EntityPresenceState.PRESENT else EntityPresenceState.DISAPPEARING

                VisualEntity(
                    visualKey = "bst:node:${nodeDef.id}",
                    elementId = "node:${nodeDef.id}",
                    dataValue = (stepNode?.value ?: nodeDef.value).toString(),
                    slotKey = "tree_slot:${nodeDef.id}",
                    badgeLabel = badge,
                    visualState = visualState,
                    presenceState = presenceState
                )
            }

            // Active connections built exclusively from current step.nodes
            val activeConnections = mutableListOf<VisualConnection>()
            step.nodes.forEach { node ->
                node.leftId?.let { leftId ->
                    if (stepNodeMap.containsKey(leftId)) {
                        activeConnections.add(
                            VisualConnection(
                                connectionKey = "branch:tree_slot:${node.id}->tree_slot:$leftId",
                                fromVisualKey = "bst:node:${node.id}",
                                toVisualKey = "bst:node:$leftId",
                                label = "L",
                                type = ConnectionType.STRUCTURAL_BRANCH,
                                visualState = ConnectionVisualState.DEFAULT
                            )
                        )
                    }
                }
                node.rightId?.let { rightId ->
                    if (stepNodeMap.containsKey(rightId)) {
                        activeConnections.add(
                            VisualConnection(
                                connectionKey = "branch:tree_slot:${node.id}->tree_slot:$rightId",
                                fromVisualKey = "bst:node:${node.id}",
                                toVisualKey = "bst:node:$rightId",
                                label = "R",
                                type = ConnectionType.STRUCTURAL_BRANCH,
                                visualState = ConnectionVisualState.DEFAULT
                            )
                        )
                    }
                }
            }

            val treeSubScene = StructureSubScene(
                subPaneId = "tree_traversal_tree",
                type = StructureType.TREE,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = activeConnections,
                referenceConnections = referenceConnections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "bst_comparison_step_$stepIndex",
                subScenes = listOf(treeSubScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildBstOperationsScenes(
        steps: List<BstOperationsTraceStep>
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val allNodesMap = mutableMapOf<String, BstNode>()
        steps.forEach { step ->
            step.nodes.forEach { node ->
                allNodesMap[node.id] = node
            }
        }
        val allNodes = allNodesMap.values.toList()

        val referenceConnections = buildChronologicalReferenceConnectionsOperations(steps, allNodesMap)

        return steps.mapIndexed { stepIndex, step ->
            val stepNodeMap = step.nodes.associateBy { it.id }

            val entities = allNodes.map { nodeDef ->
                val stepNode = stepNodeMap[nodeDef.id]
                val isSuccessor = stepNode != null && stepNode.isSuccessor
                val isActive = stepNode != null && (stepNode.id == step.inspectedNodeId || stepNode.isHighlighted)
                val isPresent = stepNode != null

                // State Priority: SUCCESSOR (VISITED) > ACTIVE > DEFAULT
                val visualState = when {
                    !isPresent -> EntityVisualState.DEFAULT
                    isSuccessor -> EntityVisualState.VISITED
                    isActive -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }
                val badge = when {
                    !isPresent -> null
                    isSuccessor -> "Successor"
                    isActive -> "Inspecting"
                    else -> null
                }
                val presenceState = if (isPresent) EntityPresenceState.PRESENT else EntityPresenceState.DISAPPEARING

                VisualEntity(
                    visualKey = "bst_op:node:${nodeDef.id}",
                    elementId = "node:${nodeDef.id}",
                    dataValue = (stepNode?.value ?: nodeDef.value).toString(),
                    slotKey = "tree_slot:${nodeDef.id}",
                    badgeLabel = badge,
                    visualState = visualState,
                    presenceState = presenceState
                )
            }

            // Active connections built exclusively from current step.nodes
            val activeConnections = mutableListOf<VisualConnection>()
            step.nodes.forEach { node ->
                node.leftId?.let { leftId ->
                    if (stepNodeMap.containsKey(leftId)) {
                        activeConnections.add(
                            VisualConnection(
                                connectionKey = "branch:tree_slot:${node.id}->tree_slot:$leftId",
                                fromVisualKey = "bst_op:node:${node.id}",
                                toVisualKey = "bst_op:node:$leftId",
                                label = "L",
                                type = ConnectionType.STRUCTURAL_BRANCH,
                                visualState = ConnectionVisualState.DEFAULT
                            )
                        )
                    }
                }
                node.rightId?.let { rightId ->
                    if (stepNodeMap.containsKey(rightId)) {
                        activeConnections.add(
                            VisualConnection(
                                connectionKey = "branch:tree_slot:${node.id}->tree_slot:$rightId",
                                fromVisualKey = "bst_op:node:${node.id}",
                                toVisualKey = "bst_op:node:$rightId",
                                label = "R",
                                type = ConnectionType.STRUCTURAL_BRANCH,
                                visualState = ConnectionVisualState.DEFAULT
                            )
                        )
                    }
                }
            }

            val treeSubScene = StructureSubScene(
                subPaneId = "tree_traversal_tree",
                type = StructureType.TREE,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = activeConnections,
                referenceConnections = referenceConnections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "bst_operations_step_$stepIndex",
                subScenes = listOf(treeSubScene),
                globalMessage = step.explanation
            )
        }
    }

    private fun tryAddReferenceEdge(
        parentId: String,
        childId: String,
        isLeft: Boolean,
        visualKeyPrefix: String,
        allNodesKeys: Set<String>,
        childToParentMap: MutableMap<String, String>,
        parentLeftChildMap: MutableMap<String, String>,
        parentRightChildMap: MutableMap<String, String>,
        referenceConnections: MutableList<VisualConnection>
    ): Boolean {
        if (parentId !in allNodesKeys || childId !in allNodesKeys) return false
        if (parentId == childId) return false
        if (childToParentMap.containsKey(childId)) return false
        if (isLeft && parentLeftChildMap.containsKey(parentId)) return false
        if (!isLeft && parentRightChildMap.containsKey(parentId)) return false

        // Cycle check: verify child is not an ancestor of parent
        var curr: String? = parentId
        while (curr != null) {
            if (curr == childId) return false
            curr = childToParentMap[curr]
        }

        childToParentMap[childId] = parentId
        if (isLeft) {
            parentLeftChildMap[parentId] = childId
        } else {
            parentRightChildMap[parentId] = childId
        }

        val label = if (isLeft) "L" else "R"
        referenceConnections.add(
            VisualConnection(
                connectionKey = "branch:tree_slot:$parentId->tree_slot:$childId",
                fromVisualKey = "$visualKeyPrefix:node:$parentId",
                toVisualKey = "$visualKeyPrefix:node:$childId",
                label = label,
                type = ConnectionType.STRUCTURAL_BRANCH,
                visualState = ConnectionVisualState.DEFAULT
            )
        )
        return true
    }

    private fun buildChronologicalReferenceConnectionsComparison(
        steps: List<BinaryTreeBstTraceStep>,
        allNodesMap: Map<String, BstNodeData>
    ): List<VisualConnection> {
        val childToParentMap = mutableMapOf<String, String>()
        val parentLeftMap = mutableMapOf<String, String>()
        val parentRightMap = mutableMapOf<String, String>()
        val referenceConnections = mutableListOf<VisualConnection>()
        val allNodesKeys = allNodesMap.keys

        steps.forEach { step ->
            step.nodes.forEach { node ->
                node.leftId?.let { leftId ->
                    tryAddReferenceEdge(
                        parentId = node.id,
                        childId = leftId,
                        isLeft = true,
                        visualKeyPrefix = "bst",
                        allNodesKeys = allNodesKeys,
                        childToParentMap = childToParentMap,
                        parentLeftChildMap = parentLeftMap,
                        parentRightChildMap = parentRightMap,
                        referenceConnections = referenceConnections
                    )
                }
                node.rightId?.let { rightId ->
                    tryAddReferenceEdge(
                        parentId = node.id,
                        childId = rightId,
                        isLeft = false,
                        visualKeyPrefix = "bst",
                        allNodesKeys = allNodesKeys,
                        childToParentMap = childToParentMap,
                        parentLeftChildMap = parentLeftMap,
                        parentRightChildMap = parentRightMap,
                        referenceConnections = referenceConnections
                    )
                }
            }
        }
        return referenceConnections
    }

    private fun buildChronologicalReferenceConnectionsOperations(
        steps: List<BstOperationsTraceStep>,
        allNodesMap: Map<String, BstNode>
    ): List<VisualConnection> {
        val childToParentMap = mutableMapOf<String, String>()
        val parentLeftMap = mutableMapOf<String, String>()
        val parentRightMap = mutableMapOf<String, String>()
        val referenceConnections = mutableListOf<VisualConnection>()
        val allNodesKeys = allNodesMap.keys

        steps.forEach { step ->
            step.nodes.forEach { node ->
                node.leftId?.let { leftId ->
                    tryAddReferenceEdge(
                        parentId = node.id,
                        childId = leftId,
                        isLeft = true,
                        visualKeyPrefix = "bst_op",
                        allNodesKeys = allNodesKeys,
                        childToParentMap = childToParentMap,
                        parentLeftChildMap = parentLeftMap,
                        parentRightChildMap = parentRightMap,
                        referenceConnections = referenceConnections
                    )
                }
                node.rightId?.let { rightId ->
                    tryAddReferenceEdge(
                        parentId = node.id,
                        childId = rightId,
                        isLeft = false,
                        visualKeyPrefix = "bst_op",
                        allNodesKeys = allNodesKeys,
                        childToParentMap = childToParentMap,
                        parentLeftChildMap = parentLeftMap,
                        parentRightChildMap = parentRightMap,
                        referenceConnections = referenceConnections
                    )
                }
            }
        }
        return referenceConnections
    }
}
