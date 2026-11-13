package com.example.learndsandalgorithm.presentation.learn

/**
 * Deterministic builder that transforms Graph trace sequences into abstract [VisualizationScene] models.
 */
internal object GraphSceneBuilder {

    fun buildBfsScenes(
        steps: List<GraphBfsTraceStep>,
        graph: Map<String, List<String>> = GraphBfsTrace.graph
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val allVertices = (graph.keys + graph.values.flatten()).distinct().sorted()
        val uniqueEdges = normalizeUndirectedEdges(graph)

        return steps.mapIndexed { stepIndex, step ->
            val entities = allVertices.map { v ->
                val isActive = v == step.currentVertex
                val isQueued = v in step.queue
                val isVisited = v in step.visited

                val visualState = when {
                    isActive -> EntityVisualState.ACTIVE
                    isVisited -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }
                val badge = when {
                    isActive -> "Active"
                    isQueued -> "Queue"
                    else -> null
                }

                VisualEntity(
                    visualKey = "graph:vertex:$v",
                    elementId = "vertex:$v",
                    dataValue = v,
                    slotKey = "graph_slot:$v",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = uniqueEdges.map { (u, v) ->
                VisualConnection(
                    connectionKey = "edge:$u<->$v",
                    fromVisualKey = "graph:vertex:$u",
                    toVisualKey = "graph:vertex:$v",
                    type = ConnectionType.GRAPH_EDGE,
                    visualState = ConnectionVisualState.DEFAULT,
                    directed = false
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "graph_main_pane",
                type = StructureType.GRAPH,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "bfs_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildDfsScenes(
        steps: List<GraphDfsTraceStep>,
        graph: Map<String, List<String>> = GraphDfsTrace.graph
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val allVertices = (graph.keys + graph.values.flatten()).distinct().sorted()
        val uniqueEdges = normalizeUndirectedEdges(graph)

        return steps.mapIndexed { stepIndex, step ->
            val entities = allVertices.map { v ->
                val isActive = v == step.currentVertex
                val inStack = v in step.callStack
                val isVisited = v in step.visited

                val visualState = when {
                    isActive || inStack -> EntityVisualState.ACTIVE
                    isVisited -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }
                val badge = when {
                    isActive -> "Active"
                    inStack -> "Stack"
                    else -> null
                }

                VisualEntity(
                    visualKey = "graph:vertex:$v",
                    elementId = "vertex:$v",
                    dataValue = v,
                    slotKey = "graph_slot:$v",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = uniqueEdges.map { (u, v) ->
                VisualConnection(
                    connectionKey = "edge:$u<->$v",
                    fromVisualKey = "graph:vertex:$u",
                    toVisualKey = "graph:vertex:$v",
                    type = ConnectionType.GRAPH_EDGE,
                    visualState = ConnectionVisualState.DEFAULT,
                    directed = false
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "graph_main_pane",
                type = StructureType.GRAPH,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "dfs_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildComponentsCycleScenes(
        steps: List<ComponentCycleTraceStep>
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val topic = steps.first().topic
        val isDirected = topic == ComponentCycleTopic.DIRECTED_CYCLE

        val (vertices, rawEdges) = when (topic) {
            ComponentCycleTopic.CONNECTED_COMPONENTS -> listOf("A", "B", "C", "D", "E", "F") to listOf("A" to "B", "B" to "C", "D" to "E")
            ComponentCycleTopic.UNDIRECTED_CYCLE -> listOf("A", "B", "C") to listOf("A" to "B", "B" to "C", "C" to "A")
            ComponentCycleTopic.DIRECTED_CYCLE -> listOf("A", "B", "C") to listOf("A" to "B", "B" to "C", "C" to "A")
        }

        val uniqueEdges = if (isDirected) {
            rawEdges
        } else {
            rawEdges.map { (u, v) ->
                val minKey = if (u <= v) u else v
                val maxKey = if (u <= v) v else u
                minKey to maxKey
            }.distinct()
        }

        return steps.mapIndexed { stepIndex, step ->
            val cycleEndpoints = if (step.cycleDetected && step.cycleEdges != null) {
                setOf(step.cycleEdges.first, step.cycleEdges.second)
            } else {
                emptySet()
            }

            val vertexToCompIndex = mutableMapOf<String, Int>()
            if (topic == ComponentCycleTopic.CONNECTED_COMPONENTS) {
                step.components.forEachIndexed { compIdx, compList ->
                    compList.forEach { vNode ->
                        vertexToCompIndex[vNode] = compIdx + 1
                    }
                }
            }

            val entities = vertices.map { v ->
                val isCycleVertex = step.cycleDetected && (v in cycleEndpoints)
                val isCurrent = v == step.currentVertex
                val activeState = step.activeStates[v] ?: 0 // 0=unvisited, 1=active/stack, 2=finished
                val isVisited = v in step.visited || activeState == 2
                val isActive = isCurrent || activeState == 1

                // Precedence: VIOLATION > ACTIVE > VISITED > DEFAULT
                val visualState = when {
                    isCycleVertex -> EntityVisualState.VIOLATION
                    isActive -> EntityVisualState.ACTIVE
                    isVisited -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }

                val compIdx = vertexToCompIndex[v]
                val badge = when {
                    isCycleVertex -> "Cycle"
                    compIdx != null -> if (isCurrent) "Active \u00B7 Comp $compIdx" else "Comp $compIdx"
                    isCurrent -> "Active"
                    activeState == 1 -> "Stack"
                    else -> null
                }

                VisualEntity(
                    visualKey = "graph:vertex:$v",
                    elementId = "vertex:$v",
                    dataValue = v,
                    slotKey = "graph_slot:$v",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = uniqueEdges.map { (u, v) ->
                val isCycleEdge = step.cycleDetected && step.cycleEdges != null &&
                    ((u == step.cycleEdges.first && v == step.cycleEdges.second) ||
                     (!isDirected && u == step.cycleEdges.second && v == step.cycleEdges.first))

                val connKey = if (isDirected) "edge:$u->$v" else {
                    val minKey = if (u <= v) u else v
                    val maxKey = if (u <= v) v else u
                    "edge:$minKey<->$maxKey"
                }

                VisualConnection(
                    connectionKey = connKey,
                    fromVisualKey = "graph:vertex:$u",
                    toVisualKey = "graph:vertex:$v",
                    type = ConnectionType.GRAPH_EDGE,
                    visualState = if (isCycleEdge) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT,
                    directed = isDirected
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "graph_main_pane",
                type = StructureType.GRAPH,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "components_cycle_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildFundamentalsScenes(
        steps: List<GraphFundamentalsTraceStep>
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val vertices = listOf("A", "B", "C", "D", "E")
        val rawEdges = listOf("A" to "B", "A" to "C", "B" to "D", "C" to "D", "D" to "E")

        return steps.mapIndexed { stepIndex, step ->
            val isDirectedStep = step.isDirected

            val uniqueEdges = if (isDirectedStep) {
                rawEdges
            } else {
                rawEdges.map { (u, v) ->
                    val minKey = if (u <= v) u else v
                    val maxKey = if (u <= v) v else u
                    minKey to maxKey
                }.distinct()
            }

            val entities = vertices.map { v ->
                val isHighlighted = v in step.highlightedVertices
                val isInPath = step.pathSequence != null && v in step.pathSequence

                // Precedence: TARGET (highlighted/path) > DEFAULT
                val visualState = when {
                    isHighlighted || isInPath -> EntityVisualState.TARGET
                    else -> EntityVisualState.DEFAULT
                }
                val badge = when {
                    isHighlighted && step.degreeVal != null -> "deg=${step.degreeVal}"
                    isInPath -> "Path"
                    else -> null
                }

                VisualEntity(
                    visualKey = "graph:vertex:$v",
                    elementId = "vertex:$v",
                    dataValue = v,
                    slotKey = "graph_slot:$v",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = uniqueEdges.map { (u, v) ->
                val isEdgeHighlighted = step.highlightedEdges.any { (x, y) ->
                    (u == x && v == y) || (!isDirectedStep && ((u == x && v == y) || (u == y && v == x)))
                }

                val connKey = if (isDirectedStep) "edge:$u->$v" else {
                    val minKey = if (u <= v) u else v
                    val maxKey = if (u <= v) v else u
                    "edge:$minKey<->$maxKey"
                }

                VisualConnection(
                    connectionKey = connKey,
                    fromVisualKey = "graph:vertex:$u",
                    toVisualKey = "graph:vertex:$v",
                    type = ConnectionType.GRAPH_EDGE,
                    visualState = if (isEdgeHighlighted) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT,
                    directed = isDirectedStep
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "graph_main_pane",
                type = StructureType.GRAPH,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "fundamentals_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildShortestPathScenes(
        steps: List<UnweightedShortestPathTraceStep>,
        graph: Map<String, List<String>> = GraphUnweightedShortestPathTrace.graph
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val allVertices = (graph.keys + graph.values.flatten()).distinct().sorted()
        val uniqueEdges = normalizeUndirectedEdges(graph)

        return steps.mapIndexed { stepIndex, step ->
            val pathEdges = mutableSetOf<Pair<String, String>>()
            if (step.reconstructedPath != null && step.reconstructedPath.size >= 2) {
                for (i in 0 until step.reconstructedPath.size - 1) {
                    val u = step.reconstructedPath[i]
                    val v = step.reconstructedPath[i + 1]
                    val minK = if (u <= v) u else v
                    val maxK = if (u <= v) v else u
                    pathEdges.add(minK to maxK)
                }
            }

            val entities = allVertices.map { v ->
                val isCurrent = v == step.currentVertex
                val isInPath = step.reconstructedPath != null && v in step.reconstructedPath
                val dist = step.distances[v]
                val pred = step.predecessors[v]
                val qIdx = step.queue.indexOf(v)
                val isQueued = qIdx >= 0

                // Precedence: ACTIVE > TARGET > VISITED > DEFAULT
                val visualState = when {
                    isCurrent -> EntityVisualState.ACTIVE
                    isInPath -> EntityVisualState.TARGET
                    dist != null -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }

                val distText = when {
                    dist != null -> if (isCurrent) {
                        if (pred != null) "Active: d=$dist (p=$pred)" else "Active: d=$dist"
                    } else {
                        if (pred != null) "d=$dist (p=$pred)" else "d=$dist"
                    }
                    isCurrent -> "Active"
                    else -> null
                }

                val badge = when {
                    distText != null -> if (isQueued) "$distText \u00B7 Q[${qIdx + 1}]" else distText
                    isQueued -> "Q[${qIdx + 1}]"
                    else -> null
                }

                VisualEntity(
                    visualKey = "graph:vertex:$v",
                    elementId = "vertex:$v",
                    dataValue = v,
                    slotKey = "graph_slot:$v",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = uniqueEdges.map { (u, v) ->
                val isPathEdge = (u to v) in pathEdges

                VisualConnection(
                    connectionKey = "edge:$u<->$v",
                    fromVisualKey = "graph:vertex:$u",
                    toVisualKey = "graph:vertex:$v",
                    type = ConnectionType.GRAPH_EDGE,
                    visualState = if (isPathEdge) ConnectionVisualState.PATH else ConnectionVisualState.DEFAULT,
                    directed = false
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "graph_main_pane",
                type = StructureType.GRAPH,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "shortest_path_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildDijkstraScenes(
        steps: List<DijkstraTraceStep>,
        graph: Map<String, List<DijkstraEdge>> = DijkstraTrace.weightedGraph
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val allVertices = (graph.keys + graph.values.flatMap { edges -> edges.map { it.to } }).distinct().sorted()
        val allEdges = graph.values.flatten()

        return steps.mapIndexed { stepIndex, step ->
            val pathDirectedEdges = mutableSetOf<Pair<String, String>>()
            if (step.reconstructedPath != null && step.reconstructedPath.size >= 2) {
                for (i in 0 until step.reconstructedPath.size - 1) {
                    pathDirectedEdges.add(step.reconstructedPath[i] to step.reconstructedPath[i + 1])
                }
            }

            val entities = allVertices.map { v ->
                val isCurrent = v == step.currentVertex
                val isInPath = step.reconstructedPath != null && v in step.reconstructedPath
                val dist = step.distances[v]
                val pred = step.predecessors[v]
                val pqIndex = step.priorityQueue.indexOfFirst { it.second == v }
                val isQueued = pqIndex >= 0

                // Precedence: ACTIVE > TARGET > VISITED > DEFAULT
                val visualState = when {
                    isCurrent -> EntityVisualState.ACTIVE
                    isInPath -> EntityVisualState.TARGET
                    dist != null -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }

                val distText = when {
                    dist != null -> if (isCurrent) {
                        if (pred != null) "Active: d=$dist (p=$pred)" else "Active: d=$dist"
                    } else {
                        if (pred != null) "d=$dist (p=$pred)" else "d=$dist"
                    }
                    isCurrent -> "Active"
                    else -> null
                }

                val badge = when {
                    distText != null -> if (isQueued) "$distText \u00B7 Q[${pqIndex + 1}]" else distText
                    isQueued -> "Q[${pqIndex + 1}]"
                    else -> null
                }

                VisualEntity(
                    visualKey = "graph:vertex:$v",
                    elementId = "vertex:$v",
                    dataValue = v,
                    slotKey = "graph_slot:$v",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = allEdges.map { edge ->
                val u = edge.from
                val v = edge.to
                val isPathEdge = (u to v) in pathDirectedEdges
                val isRelaxedEdge = step.relaxedEdge?.from == u && step.relaxedEdge.to == v

                val connState = when {
                    isPathEdge -> ConnectionVisualState.PATH
                    isRelaxedEdge -> ConnectionVisualState.RELAXING
                    else -> ConnectionVisualState.DEFAULT
                }

                VisualConnection(
                    connectionKey = "edge:$u->$v",
                    fromVisualKey = "graph:vertex:$u",
                    toVisualKey = "graph:vertex:$v",
                    label = "w=${edge.weight}",
                    type = ConnectionType.GRAPH_EDGE,
                    visualState = connState,
                    directed = true
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "graph_main_pane",
                type = StructureType.GRAPH,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "dijkstra_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    fun buildTopologicalSortScenes(
        steps: List<TopologicalSortTraceStep>
    ): List<VisualizationScene> {
        if (steps.isEmpty()) return emptyList()

        val firstStepGraph = steps.first().graph
        val allVertices = (firstStepGraph.keys + firstStepGraph.values.flatten()).distinct().sorted()

        val allEdges = firstStepGraph.flatMap { (u, neighbors) ->
            neighbors.map { v -> u to v }
        }

        return steps.mapIndexed { stepIndex, step ->
            val entities = allVertices.map { v ->
                val isCurrent = v == step.currentVertex
                val isInTopo = v in step.topoOrder
                val inDeg = step.inDegrees[v]
                val qIdx = step.queue.indexOf(v)
                val isQueued = qIdx >= 0
                val topoIdx = step.topoOrder.indexOf(v)

                // Precedence: ACTIVE > VISITED > DEFAULT
                val visualState = when {
                    isCurrent -> EntityVisualState.ACTIVE
                    isInTopo -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }

                val statusText = when {
                    isCurrent -> if (inDeg != null) "Active: in=$inDeg" else "Active"
                    isInTopo -> "Order #${topoIdx + 1}"
                    inDeg != null -> "in=$inDeg"
                    else -> null
                }

                val badge = when {
                    statusText != null -> if (isQueued) "$statusText \u00B7 Q[${qIdx + 1}]" else statusText
                    isQueued -> "Q[${qIdx + 1}]"
                    else -> null
                }

                VisualEntity(
                    visualKey = "graph:vertex:$v",
                    elementId = "vertex:$v",
                    dataValue = v,
                    slotKey = "graph_slot:$v",
                    badgeLabel = badge,
                    visualState = visualState
                )
            }

            val connections = allEdges.map { (u, v) ->
                val isHighlighted = u in step.topoOrder || (u == step.currentVertex)

                VisualConnection(
                    connectionKey = "edge:$u->$v",
                    fromVisualKey = "graph:vertex:$u",
                    toVisualKey = "graph:vertex:$v",
                    type = ConnectionType.GRAPH_EDGE,
                    visualState = if (isHighlighted) ConnectionVisualState.HIGHLIGHTED else ConnectionVisualState.DEFAULT,
                    directed = true
                )
            }

            val subScene = StructureSubScene(
                subPaneId = "graph_main_pane",
                type = StructureType.GRAPH,
                didacticPriority = 1,
                minRequiredWidthDp = 220f,
                minRequiredHeightDp = 200f,
                entities = entities,
                connections = connections
            )

            VisualizationScene(
                stepIndex = stepIndex,
                sceneId = "topological_sort_step_$stepIndex",
                subScenes = listOf(subScene),
                globalMessage = step.explanation
            )
        }
    }

    private fun normalizeUndirectedEdges(graph: Map<String, List<String>>): List<Pair<String, String>> {
        val edgeSet = mutableSetOf<Pair<String, String>>()
        graph.forEach { (u, neighbors) ->
            neighbors.forEach { v ->
                if (u != v) {
                    val minKey = if (u <= v) u else v
                    val maxKey = if (u <= v) v else u
                    edgeSet.add(minKey to maxKey)
                }
            }
        }
        return edgeSet.sortedWith(compareBy({ it.first }, { it.second }))
    }
}
