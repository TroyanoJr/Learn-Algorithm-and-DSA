package com.example.learndsandalgorithm.presentation.learn

internal data class DivideConquerNodePresentation(
    val id: String,
    val low: Int,
    val high: Int,
    val elements: List<Int>,
    val isBaseCase: Boolean,
    val leftChildId: String?,
    val rightChildId: String?,
    val maxValue: Int?
) {
    val valuesLabel: String get() = elements.joinToString(", ", prefix = "[", postfix = "]")
    val rangeLabel: String get() = "range [$low..$high]"
    val roleLabel: String get() = if (isBaseCase) "BASE CASE" else "SUBPROBLEM"
    val maxLabel: String? get() = maxValue?.let { "max = $it" }
}

internal data class DivideConquerSceneMeta(
    val phase: DivideConquerPhase,
    val activeSubproblemId: String?,
    val nodePresentations: Map<String, DivideConquerNodePresentation>,
    val leftValue: Int?,
    val rightValue: Int?,
    val combinedValue: Int?,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean
)

internal data class DivideConquerSceneFrame(
    val scene: VisualizationScene,
    val meta: DivideConquerSceneMeta
)

internal object DivideConquerSceneBuilder {
    private const val SUB_SCENE_ID = "divide_conquer_tree"
    private const val VISUAL_KEY_PREFIX = "divide_conquer:node:"

    fun build(steps: List<FundamentalsTraceStep>): List<DivideConquerSceneFrame> {
        if (steps.isEmpty()) return emptyList()

        val referenceConnections = linkedMapOf<String, VisualConnection>()
        steps.forEach { step ->
            val presentIds = step.nodes.mapTo(mutableSetOf()) { it.id }
            step.nodes.forEach { node ->
                node.leftChildId?.takeIf { it in presentIds }?.let { childId ->
                    val connection = connection(node.id, childId, "L")
                    if (connection.connectionKey !in referenceConnections) referenceConnections[connection.connectionKey] = connection
                }
                node.rightChildId?.takeIf { it in presentIds }?.let { childId ->
                    val connection = connection(node.id, childId, "R")
                    if (connection.connectionKey !in referenceConnections) referenceConnections[connection.connectionKey] = connection
                }
            }
        }

        return steps.mapIndexed { stepIndex, step ->
            val nodesById = step.nodes.associateBy { it.id }
            val presentations = step.nodes.associate { node ->
                node.id to DivideConquerNodePresentation(
                    id = node.id,
                    low = node.low,
                    high = node.high,
                    elements = node.elements,
                    isBaseCase = node.isBaseCase,
                    leftChildId = node.leftChildId,
                    rightChildId = node.rightChildId,
                    maxValue = node.maxValue
                )
            }
            val entities = step.nodes.map { node ->
                val visualState = when {
                    node.id == step.activeSubproblemId && step.phase == DivideConquerPhase.COMBINE -> EntityVisualState.TARGET
                    node.id == step.activeSubproblemId -> EntityVisualState.ACTIVE
                    node.maxValue != null -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }
                VisualEntity(
                    visualKey = visualKey(node.id),
                    elementId = "node:${node.id}",
                    dataValue = node.elements.joinToString(", ", prefix = "[", postfix = "]"),
                    slotKey = "tree_slot:${node.id}",
                    badgeLabel = "${node.low}..${node.high}",
                    visualState = visualState
                )
            }
            val connections = buildList {
                step.nodes.forEach { node ->
                    node.leftChildId?.takeIf { it in nodesById }?.let { childId ->
                        add(connection(node.id, childId, "L", isActiveCombineEdge(step, node.id)))
                    }
                    node.rightChildId?.takeIf { it in nodesById }?.let { childId ->
                        add(connection(node.id, childId, "R", isActiveCombineEdge(step, node.id)))
                    }
                }
            }
            val meta = DivideConquerSceneMeta(
                phase = step.phase,
                activeSubproblemId = step.activeSubproblemId,
                nodePresentations = presentations,
                leftValue = step.leftValue,
                rightValue = step.rightValue,
                combinedValue = step.combinedValue,
                explanation = step.explanation,
                badgeText = step.badgeText,
                isTerminal = step.isTerminal
            )
            val treeScene = StructureSubScene(
                subPaneId = SUB_SCENE_ID,
                type = StructureType.TREE,
                minRequiredWidthDp = 396f,
                minRequiredHeightDp = 300f,
                entities = entities,
                connections = connections,
                referenceConnections = referenceConnections.values.toList()
            )
            DivideConquerSceneFrame(
                scene = VisualizationScene(
                    stepIndex = stepIndex,
                    sceneId = "divide_conquer_fundamentals_step_$stepIndex",
                    subScenes = listOf(treeScene),
                    globalMessage = step.explanation
                ),
                meta = meta
            )
        }
    }

    private fun isActiveCombineEdge(step: FundamentalsTraceStep, parentId: String): Boolean =
        step.phase == DivideConquerPhase.COMBINE && step.activeSubproblemId == parentId

    private fun connection(parentId: String, childId: String, label: String, highlighted: Boolean = false) =
        VisualConnection(
            connectionKey = "branch:tree_slot:$parentId->tree_slot:$childId",
            fromVisualKey = visualKey(parentId),
            toVisualKey = visualKey(childId),
            label = label,
            type = ConnectionType.STRUCTURAL_BRANCH,
            visualState = if (highlighted) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT
        )

    private fun visualKey(id: String): String = "$VISUAL_KEY_PREFIX$id"
}
