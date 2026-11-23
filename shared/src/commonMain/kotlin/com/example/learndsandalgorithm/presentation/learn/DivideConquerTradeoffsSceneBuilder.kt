package com.example.learndsandalgorithm.presentation.learn

internal enum class TradeoffNodeStatus { PENDING, ACTIVE, COMPLETED }

internal data class TradeoffRecursionNode(
    val id: String,
    val size: Int,
    val low: Int,
    val highExclusive: Int,
    val parentId: String?,
    val branch: Char?,
    val depth: Int,
    val visible: Boolean,
    val status: TradeoffNodeStatus
) {
    val rangeLabel: String get() = "[$low,$highExclusive)"
}

internal data class TradeoffComparisonRow(
    val id: String,
    val property: String,
    val mergeSort: String,
    val quickSort: String
)

internal data class TradeoffStableRecord(val identity: String, val key: Int) {
    val label: String get() = "$key${identity.substringAfterLast(':')}"
}

internal data class DivideConquerTradeoffsSceneMeta(
    val source: TradeoffsTraceStep,
    val scenarioStepIndex: Int,
    val balancedNodes: List<TradeoffRecursionNode>,
    val unbalancedNodes: List<TradeoffRecursionNode>,
    val comparisonRows: List<TradeoffComparisonRow>,
    val activeComparisonSection: String?,
    val inputRecords: List<TradeoffStableRecord>,
    val mergeSortedRecords: List<TradeoffStableRecord>,
    val quickPartitionRecords: List<TradeoffStableRecord>,
    val mergeBufferRecords: List<TradeoffStableRecord>
)

internal data class DivideConquerTradeoffsSceneFrame(
    val scene: VisualizationScene,
    val meta: DivideConquerTradeoffsSceneMeta
)

internal object DivideConquerTradeoffsSceneBuilder {
    const val BALANCED_TREE_SCENE_ID = "tradeoffs_balanced_recursion_tree"
    const val UNBALANCED_TREE_SCENE_ID = "tradeoffs_unbalanced_recursion_tree"
    const val COMPARISON_TABLE_SCENE_ID = "tradeoffs_comparison_table"
    const val MERGE_EXAMPLE_SCENE_ID = "tradeoffs_merge_stability_example"
    const val QUICK_EXAMPLE_SCENE_ID = "tradeoffs_quick_partition_example"
    const val BUFFER_SCENE_ID = "tradeoffs_merge_buffer"

    internal val comparisonRows = listOf(
        TradeoffComparisonRow(
            "typical_time",
            "Typical time",
            "O(N log N) · guaranteed across cases",
            "O(N log N) · average behavior"
        ),
        TradeoffComparisonRow(
            "worst_time",
            "Worst-case time",
            "O(N log N) · guaranteed",
            "O(N²) · possible with repeatedly poor pivots"
        ),
        TradeoffComparisonRow(
            "auxiliary_memory",
            "Auxiliary memory",
            "O(N) temporary array merge buffer",
            "O(1) extra array storage; stack O(log N) expected, O(N) worst case"
        ),
        TradeoffComparisonRow(
            "stability",
            "Stability",
            "Stable · equal keys preserve input order",
            "Not stable · in-place swaps may reorder equal keys"
        ),
        TradeoffComparisonRow(
            "practical_tradeoff",
            "Practical tradeoff",
            "Predictable time and stable ordering; uses extra array memory",
            "In-place partitioning can have cache benefits; it is not always faster"
        )
    )

    internal val inputRecords = listOf(
        TradeoffStableRecord("record:A", 5),
        TradeoffStableRecord("record:B", 5),
        TradeoffStableRecord("record:C", 1),
        TradeoffStableRecord("record:D", 0),
        TradeoffStableRecord("record:E", 3)
    )
    internal val mergeSortedRecords = listOf("record:D", "record:C", "record:E", "record:A", "record:B")
        .map { identity -> inputRecords.single { it.identity == identity } }
    // One Lomuto partition using pivot 3, not a complete Quick Sort execution.
    internal val quickPartitionRecords = listOf("record:C", "record:D", "record:E", "record:B", "record:A")
        .map { identity -> inputRecords.single { it.identity == identity } }

    fun build(steps: List<TradeoffsTraceStep>): List<DivideConquerTradeoffsSceneFrame> {
        if (steps.isEmpty()) return emptyList()
        require(steps.all { it.scenario == steps.first().scenario }) { "A tradeoff scene sequence must contain one scenario" }
        return when (steps.first().scenario) {
            TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> buildSplitSequence(steps)
            TradeoffScenario.ALGORITHM_COMPARISON -> buildComparisonSequence(steps)
        }
    }

    private fun buildSplitSequence(steps: List<TradeoffsTraceStep>): List<DivideConquerTradeoffsSceneFrame> {
        val n = steps.first().inputN
        require(n > 0) { "The recursion comparison requires a positive input size" }
        val balancedTopology = recursionTopology(n, balanced = true)
        val unbalancedTopology = recursionTopology(n, balanced = false)
        val balancedReferences = treeConnections(balancedTopology, BALANCED_TREE_SCENE_ID)
        val unbalancedReferences = treeConnections(unbalancedTopology, UNBALANCED_TREE_SCENE_ID)

        return steps.mapIndexed { stepIndex, step ->
            val balancedVisible = when {
                stepIndex == 0 -> setOf("balanced:0:$n")
                else -> balancedTopology.map { it.id }.toSet()
            }
            val unbalancedVisible = when {
                stepIndex < 2 -> setOf("unbalanced:0:$n")
                else -> unbalancedTopology.map { it.id }.toSet()
            }
            val balancedNodes = statusNodes(
                balancedTopology,
                visibleIds = balancedVisible,
                activeId = if (stepIndex == 1) "balanced:0:$n" else null,
                completed = stepIndex >= 2
            )
            val unbalancedNodes = statusNodes(
                unbalancedTopology,
                visibleIds = unbalancedVisible,
                activeId = if (stepIndex == 2) "unbalanced:0:$n" else null,
                completed = step.isTerminal
            )
            val scenes = listOf(
                treeSubScene(BALANCED_TREE_SCENE_ID, balancedNodes, balancedReferences),
                treeSubScene(UNBALANCED_TREE_SCENE_ID, unbalancedNodes, unbalancedReferences)
            )
            DivideConquerTradeoffsSceneFrame(
                scene = VisualizationScene(
                    stepIndex = stepIndex,
                    sceneId = "divide_conquer_tradeoffs_split_$stepIndex",
                    subScenes = scenes,
                    globalMessage = step.explanation
                ),
                meta = DivideConquerTradeoffsSceneMeta(
                    source = step,
                    scenarioStepIndex = stepIndex,
                    balancedNodes = balancedNodes,
                    unbalancedNodes = unbalancedNodes,
                    comparisonRows = emptyList(),
                    activeComparisonSection = null,
                    inputRecords = emptyList(),
                    mergeSortedRecords = emptyList(),
                    quickPartitionRecords = emptyList(),
                    mergeBufferRecords = emptyList()
                )
            )
        }
    }

    private fun buildComparisonSequence(steps: List<TradeoffsTraceStep>): List<DivideConquerTradeoffsSceneFrame> =
        steps.mapIndexed { stepIndex, step ->
            val activeSection = when (stepIndex) {
                1 -> "merge"
                2 -> "quick"
                3 -> "decision"
                else -> null
            }
            val tableEntities = comparisonRows.map { row ->
                val isActiveRow = when (activeSection) {
                    "merge", "quick" -> row.id != "practical_tradeoff"
                    "decision" -> row.id == "practical_tradeoff"
                    else -> false
                }
                VisualEntity(
                    visualKey = "tradeoffs:row:${row.id}",
                    elementId = "comparison:${row.id}",
                    dataValue = "${row.property} | ${row.mergeSort} | ${row.quickSort}",
                    slotKey = "comparison_row:${row.id}",
                    badgeLabel = row.property,
                    visualState = if (isActiveRow) EntityVisualState.ACTIVE else EntityVisualState.DEFAULT
                )
            }
            val scenes = listOf(
                StructureSubScene(
                    subPaneId = COMPARISON_TABLE_SCENE_ID,
                    type = StructureType.TABLE,
                    minRequiredWidthDp = 700f,
                    minRequiredHeightDp = 300f,
                    entities = tableEntities
                ),
                recordSubScene(MERGE_EXAMPLE_SCENE_ID, inputRecords, "input"),
                recordSubScene(MERGE_EXAMPLE_SCENE_ID, mergeSortedRecords, "stable_output"),
                recordSubScene(QUICK_EXAMPLE_SCENE_ID, inputRecords, "input"),
                recordSubScene(QUICK_EXAMPLE_SCENE_ID, quickPartitionRecords, "partition_output"),
                recordSubScene(BUFFER_SCENE_ID, mergeSortedRecords, "buffer")
            )
            DivideConquerTradeoffsSceneFrame(
                scene = VisualizationScene(
                    stepIndex = stepIndex,
                    sceneId = "divide_conquer_tradeoffs_comparison_$stepIndex",
                    subScenes = scenes,
                    globalMessage = step.explanation
                ),
                meta = DivideConquerTradeoffsSceneMeta(
                    source = step,
                    scenarioStepIndex = stepIndex,
                    balancedNodes = emptyList(),
                    unbalancedNodes = emptyList(),
                    comparisonRows = comparisonRows,
                    activeComparisonSection = activeSection,
                    inputRecords = inputRecords,
                    mergeSortedRecords = mergeSortedRecords,
                    quickPartitionRecords = quickPartitionRecords,
                    mergeBufferRecords = mergeSortedRecords
                )
            )
        }

    private fun recordSubScene(
        id: String,
        records: List<TradeoffStableRecord>,
        rowPrefix: String
    ) = StructureSubScene(
        subPaneId = "$id:$rowPrefix",
        type = StructureType.ARRAY,
        didacticPriority = if (rowPrefix == "buffer") 2 else 1,
        minRequiredWidthDp = records.size * 42f,
        minRequiredHeightDp = 34f,
        entities = records.mapIndexed { index, record ->
            VisualEntity(
                visualKey = "$id:${rowPrefix}:${record.identity}",
                elementId = record.identity,
                dataValue = record.label,
                slotKey = "$rowPrefix:$index",
                badgeLabel = "key ${record.key}",
                visualState = if (record.identity == "record:A" || record.identity == "record:B") {
                    EntityVisualState.TARGET
                } else EntityVisualState.DEFAULT
            )
        }
    )

    private fun recursionTopology(n: Int, balanced: Boolean): List<TradeoffRecursionNode> {
        val nodes = mutableListOf<TradeoffRecursionNode>()
        fun visit(low: Int, high: Int, parentId: String?, branch: Char?, depth: Int) {
            val size = high - low
            val id = "${if (balanced) "balanced" else "unbalanced"}:$low:$high"
            nodes += TradeoffRecursionNode(
                id = id,
                size = size,
                low = low,
                highExclusive = high,
                parentId = parentId,
                branch = branch,
                depth = depth,
                visible = false,
                status = TradeoffNodeStatus.PENDING
            )
            if (size <= 1) return
            val leftSize = if (balanced) size / 2 else 1
            val split = low + leftSize
            visit(low, split, id, 'L', depth + 1)
            visit(split, high, id, 'R', depth + 1)
        }
        visit(0, n, null, null, 0)
        return nodes
    }

    private fun statusNodes(
        topology: List<TradeoffRecursionNode>,
        visibleIds: Set<String>,
        activeId: String?,
        completed: Boolean
    ) = topology.map { node ->
        val isVisible = node.id in visibleIds
        node.copy(
            visible = isVisible,
            status = when {
                !isVisible -> TradeoffNodeStatus.PENDING
                node.id == activeId -> TradeoffNodeStatus.ACTIVE
                completed -> TradeoffNodeStatus.COMPLETED
                else -> TradeoffNodeStatus.PENDING
            }
        )
    }

    private fun treeSubScene(
        id: String,
        nodes: List<TradeoffRecursionNode>,
        references: List<VisualConnection>
    ): StructureSubScene {
        val visibleIds = nodes.filter { it.visible }.map { it.id }.toSet()
        val visibleNodes = nodes.filter { it.visible }
        val nodePrefix = "$id:node:"
        val statusById = nodes.associateBy { it.id }
        val connections = references.filter { edge ->
            edge.fromVisualKey.removePrefix(nodePrefix) in visibleIds &&
                edge.toVisualKey.removePrefix(nodePrefix) in visibleIds
        }.map { edge ->
            val parentStatus = statusById[edge.fromVisualKey.removePrefix(nodePrefix)]?.status
            edge.copy(
                visualState = when (parentStatus) {
                    TradeoffNodeStatus.ACTIVE -> ConnectionVisualState.HIGHLIGHTED
                    TradeoffNodeStatus.COMPLETED -> ConnectionVisualState.PATH
                    else -> ConnectionVisualState.DEFAULT
                }
            )
        }
        return StructureSubScene(
            subPaneId = id,
            type = StructureType.TREE,
            didacticPriority = 1,
            minRequiredWidthDp = 420f,
            minRequiredHeightDp = (nodes.maxOfOrNull { it.depth } ?: 0) * 34f + 80f,
            entities = visibleNodes.map { node ->
                VisualEntity(
                    visualKey = nodeVisualKey(id, node.id),
                    elementId = node.id,
                    dataValue = "N=${node.size} ${node.rangeLabel}",
                    slotKey = "node:${node.id}",
                    badgeLabel = node.branch?.toString() ?: "root",
                    visualState = when (node.status) {
                        TradeoffNodeStatus.PENDING -> EntityVisualState.DEFAULT
                        TradeoffNodeStatus.ACTIVE -> EntityVisualState.ACTIVE
                        TradeoffNodeStatus.COMPLETED -> EntityVisualState.VISITED
                    }
                )
            },
            connections = connections,
            referenceConnections = references
        )
    }

    private fun treeConnections(nodes: List<TradeoffRecursionNode>, sceneId: String): List<VisualConnection> =
        nodes.mapNotNull { node ->
            val parentId = node.parentId ?: return@mapNotNull null
            val parent = nodes.single { it.id == parentId }
            VisualConnection(
                connectionKey = "${node.id}:branch",
                fromVisualKey = nodeVisualKey(sceneId, parent.id),
                toVisualKey = nodeVisualKey(sceneId, node.id),
                label = "${node.branch}:${node.size}",
                type = ConnectionType.STRUCTURAL_BRANCH,
                visualState = ConnectionVisualState.DEFAULT
            )
        }

    private fun nodeVisualKey(sceneId: String, nodeId: String) = "$sceneId:node:$nodeId"
}
