package com.example.learndsandalgorithm.presentation.learn

internal enum class GraphConceptTopic {
    VERTICES_AND_EDGES,
    DIRECTED_VS_UNDIRECTED,
    VERTEX_DEGREE,
    PATHS_AND_CYCLES,
    CONNECTEDNESS
}

internal data class GraphFundamentalsTraceStep(
    val topic: GraphConceptTopic,
    val highlightedVertices: Set<String> = emptySet(),
    val highlightedEdges: Set<Pair<String, String>> = emptySet(),
    val degreeVal: Int? = null,
    val pathSequence: List<String>? = null,
    val isDirected: Boolean = false,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GraphFundamentalsTrace {

    fun stepsForTopic(topic: GraphConceptTopic): List<GraphFundamentalsTraceStep> {
        return when (topic) {
            GraphConceptTopic.VERTICES_AND_EDGES -> generateVerticesEdgesSteps()
            GraphConceptTopic.DIRECTED_VS_UNDIRECTED -> generateDirectedSteps()
            GraphConceptTopic.VERTEX_DEGREE -> generateDegreeSteps()
            GraphConceptTopic.PATHS_AND_CYCLES -> generatePathsCyclesSteps()
            GraphConceptTopic.CONNECTEDNESS -> generateConnectednessSteps()
        }
    }

    private fun generateVerticesEdgesSteps(): List<GraphFundamentalsTraceStep> {
        val steps = mutableListOf<GraphFundamentalsTraceStep>()

        fun snapshot(
            v: Set<String> = emptySet(),
            e: Set<Pair<String, String>> = emptySet(),
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphFundamentalsTraceStep(
            topic = GraphConceptTopic.VERTICES_AND_EDGES,
            highlightedVertices = v,
            highlightedEdges = e,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(emptySet(), emptySet(), false, "Graph Fundamentals: Graph G = (V, E) with 5 vertices {A, B, C, D, E} and 5 edges {AB, AC, BD, CD, DE}.", "READY · GRAPH G=(V,E)"))
        steps.add(snapshot(setOf("A", "B", "C", "D", "E"), emptySet(), false, "VERTICES (V): {A, B, C, D, E} represent nodes/entities in the network.", "VERTICES |V| = 5"))
        steps.add(snapshot(setOf("A", "B", "C", "D", "E"), setOf("A" to "B", "A" to "C", "B" to "D", "C" to "D", "D" to "E"), true, "EDGES (E): {AB, AC, BD, CD, DE} represent relationships/connections between vertex pairs.", "EDGES |E| = 5"))

        return steps
    }

    private fun generateDirectedSteps(): List<GraphFundamentalsTraceStep> {
        val steps = mutableListOf<GraphFundamentalsTraceStep>()

        fun snapshot(
            v: Set<String> = emptySet(),
            e: Set<Pair<String, String>> = emptySet(),
            isDir: Boolean = false,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphFundamentalsTraceStep(
            topic = GraphConceptTopic.DIRECTED_VS_UNDIRECTED,
            highlightedVertices = v,
            highlightedEdges = e,
            isDirected = isDir,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(setOf("A", "B"), setOf("A" to "B"), isDir = false, isTerminal = false, explanation = "UNDIRECTED GRAPH: Edge AB is bidirectional (connection goes both ways A ↔ B).", badgeText = "UNDIRECTED EDGE AB"))
        steps.add(snapshot(setOf("A", "B"), setOf("A" to "B"), isDir = true, isTerminal = true, explanation = "DIRECTED GRAPH: Edge A → B has direction (one-way relationship from A to B).", badgeText = "DIRECTED EDGE A → B"))

        return steps
    }

    private fun generateDegreeSteps(): List<GraphFundamentalsTraceStep> {
        val steps = mutableListOf<GraphFundamentalsTraceStep>()

        fun snapshot(
            v: Set<String>,
            e: Set<Pair<String, String>>,
            deg: Int,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphFundamentalsTraceStep(
            topic = GraphConceptTopic.VERTEX_DEGREE,
            highlightedVertices = v,
            highlightedEdges = e,
            degreeVal = deg,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(setOf("D"), setOf("B" to "D", "C" to "D", "D" to "E"), 3, true, "DEGREE OF VERTEX: Degree(D) = 3 because vertex D connects to 3 incident edges (neighbors B, C, E).", "degree(D) = 3"))

        return steps
    }

    private fun generatePathsCyclesSteps(): List<GraphFundamentalsTraceStep> {
        val steps = mutableListOf<GraphFundamentalsTraceStep>()

        fun snapshot(
            v: Set<String>,
            e: Set<Pair<String, String>>,
            path: List<String>,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphFundamentalsTraceStep(
            topic = GraphConceptTopic.PATHS_AND_CYCLES,
            highlightedVertices = v,
            highlightedEdges = e,
            pathSequence = path,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(setOf("A", "C", "D", "E"), setOf("A" to "C", "C" to "D", "D" to "E"), listOf("A", "C", "D", "E"), false, "PATH: A sequence of connected edges A-C-D-E (length 3 edges).", "PATH: A-C-D-E"))
        steps.add(snapshot(setOf("A", "B", "D", "C"), setOf("A" to "B", "B" to "D", "C" to "D", "A" to "C"), listOf("A", "B", "D", "C", "A"), true, "CYCLE: A closed path returning to start vertex without repeating vertices A-B-D-C-A.", "CYCLE: A-B-D-C-A"))

        return steps
    }

    private fun generateConnectednessSteps(): List<GraphFundamentalsTraceStep> {
        val steps = mutableListOf<GraphFundamentalsTraceStep>()

        fun snapshot(
            v: Set<String>,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphFundamentalsTraceStep(
            topic = GraphConceptTopic.CONNECTEDNESS,
            highlightedVertices = v,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(setOf("A", "B", "C", "D", "E"), true, "CONNECTED GRAPH: An undirected graph is connected if every pair of vertices has a path between them.", "GRAPH IS CONNECTED"))

        return steps
    }
}
