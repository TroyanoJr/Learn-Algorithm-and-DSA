package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms Tree Fundamentals trace sequences
 * ([TreeFundamentalsTraceStep]) into abstract [VisualizationScene] models compatible with [TreeSceneRenderer].
 */
internal object TreeFundamentalsSceneBuilder {

    fun buildFundamentalsScenes(
        steps: List<TreeFundamentalsTraceStep>
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        return steps.mapIndexed { stepIndex, step ->
            val nodeMap = step.nodes.associateBy { it.id }

            val entities = step.nodes.map { conceptNode ->
                val isHighlighted = conceptNode.id in step.highlightedNodeIds
                val visualState = if (isHighlighted) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT

                val badge = when {
                    conceptNode.id == "A" && step.concept == TreeConcept.ROOT -> "Root (d=0, h=2)"
                    conceptNode.isLeaf -> "Leaf"
                    conceptNode.id == step.focusedParentId -> "Parent"
                    conceptNode.id == step.focusedSubtreeRootId -> "Subtree Root"
                    else -> null
                }

                VisualEntity(
                    visualKey = "fundamentals:node:${conceptNode.id}",
                    elementId = "node:${conceptNode.id}",
                    dataValue = conceptNode.id,
                    slotKey = "tree_slot:${conceptNode.id}",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = mutableListOf<VisualConnection>()
            step.nodes.forEach { node ->
                val parentId = node.parentId
                if (parentId != null && nodeMap.containsKey(parentId)) {
                    val isBranchHighlighted = node.id in step.highlightedNodeIds && parentId in step.highlightedNodeIds
                    connections.add(
                        VisualConnection(
                            connectionKey = "branch:tree_slot:$parentId->tree_slot:${node.id}",
                            fromVisualKey = "fundamentals:node:$parentId",
                            toVisualKey = "fundamentals:node:${node.id}",
                            label = if (node.id == "B") "L" else if (node.id == "C") "R" else "",
                            type = ConnectionType.STRUCTURAL_BRANCH,
                            visualState = if (isBranchHighlighted) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
                        )
                    )
                }
            }

            val treeSubScene = StructureSubScene(
                subPaneId = "tree_fundamentals_tree",
                type = StructureType.TREE,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "tree_fundamentals_step_$stepIndex",
                subScenes = listOf(treeSubScene),
                globalMessage = step.explanation
            )
        }
    }
}
