package com.example.learndsandalgorithm.presentation.learn

internal enum class RepresentationView {
    ADJACENCY_LIST,
    ADJACENCY_MATRIX,
    COMPARISON
}

internal data class GraphRepresentationsTraceStep(
    val view: RepresentationView,
    val vertices: List<String>,
    val adjList: Map<String, List<String>>,
    val adjMatrix: List<List<Int>>,
    val inspectedVertex: String? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GraphRepresentationsTrace {

    val verticesList = listOf("A", "B", "C", "D", "E")
    val listData = mapOf(
        "A" to listOf("B", "C"),
        "B" to listOf("A", "D"),
        "C" to listOf("A", "D"),
        "D" to listOf("B", "C", "E"),
        "E" to listOf("D")
    )
    val matrixData = listOf(
        listOf(0, 1, 1, 0, 0), // A
        listOf(1, 0, 0, 1, 0), // B
        listOf(1, 0, 0, 1, 0), // C
        listOf(0, 1, 1, 0, 1), // D
        listOf(0, 0, 0, 1, 0)  // E
    )

    fun stepsForView(view: RepresentationView): List<GraphRepresentationsTraceStep> {
        return when (view) {
            RepresentationView.ADJACENCY_LIST -> generateListSteps()
            RepresentationView.ADJACENCY_MATRIX -> generateMatrixSteps()
            RepresentationView.COMPARISON -> generateComparisonSteps()
        }
    }

    private fun generateListSteps(): List<GraphRepresentationsTraceStep> {
        val steps = mutableListOf<GraphRepresentationsTraceStep>()

        fun snapshot(
            v: String? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphRepresentationsTraceStep(
            view = RepresentationView.ADJACENCY_LIST,
            vertices = verticesList,
            adjList = listData,
            adjMatrix = matrixData,
            inspectedVertex = v,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, false, "Adjacency List: Maps each vertex to a list of its direct neighbors. O(V + E) space complexity.", "READY · ADJACENCY LIST"))
        steps.add(snapshot("A", false, "Vertex A neighbors: [B, C]. Stores 2 pointers.", "VERTEX A: [B, C]"))
        steps.add(snapshot("D", true, "Vertex D neighbors: [B, C, E]. Ideal for sparse graphs and iterating over neighbors.", "VERTEX D: [B, C, E]"))

        return steps
    }

    private fun generateMatrixSteps(): List<GraphRepresentationsTraceStep> {
        val steps = mutableListOf<GraphRepresentationsTraceStep>()

        fun snapshot(
            v: String? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphRepresentationsTraceStep(
            view = RepresentationView.ADJACENCY_MATRIX,
            vertices = verticesList,
            adjList = listData,
            adjMatrix = matrixData,
            inspectedVertex = v,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(null, false, "Adjacency Matrix: 5x5 grid where cell [u][v] = 1 indicates an edge. O(V²) space complexity.", "READY · ADJACENCY MATRIX"))
        steps.add(snapshot("A", false, "Row A: [0, 1, 1, 0, 0]. O(1) edge lookup between any pair (e.g. is A adjacent to B? Yes, 1).", "ROW A MATRIX CHECK"))
        steps.add(snapshot("D", true, "Row D: [0, 1, 1, 0, 1]. Best for dense graphs with frequent O(1) edge-existence checks.", "ROW D MATRIX CHECK"))

        return steps
    }

    private fun generateComparisonSteps(): List<GraphRepresentationsTraceStep> {
        val steps = mutableListOf<GraphRepresentationsTraceStep>()

        fun snapshot(
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphRepresentationsTraceStep(
            view = RepresentationView.COMPARISON,
            vertices = verticesList,
            adjList = listData,
            adjMatrix = matrixData,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        steps.add(snapshot(false, "Representation Trade-offs: Both representations encode the exact same graph {AB, AC, BD, CD, DE}.", "READY · TRADE-OFF COMPARISON"))
        steps.add(snapshot(true, "Trade-off Rule: Use Adjacency List for O(V + E) space on sparse graphs; use Adjacency Matrix for O(1) edge lookups on dense graphs.", "LIST O(V+E) vs MATRIX O(V²)"))

        return steps
    }
}
