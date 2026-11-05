package com.example.learndsandalgorithm.presentation.learn

internal enum class DijkstraStepEvent {
    START,
    DEQUEUE_MIN,
    RELAX_EDGE,
    DIJKSTRA_COMPLETE
}

internal data class DijkstraEdge(
    val from: String,
    val to: String,
    val weight: Int
)

internal data class DijkstraTraceStep(
    val source: String = "A",
    val target: String = "D",
    val currentVertex: String? = null,
    val distances: Map<String, Int?> = emptyMap(), // null represents Infinity
    val predecessors: Map<String, String?> = emptyMap(),
    val priorityQueue: List<Pair<Int, String>> = emptyList(), // (dist, vertex)
    val relaxedEdge: DijkstraEdge? = null,
    val reconstructedPath: List<String>? = null,
    val event: DijkstraStepEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object DijkstraTrace {

    val weightedGraph = mapOf(
        "A" to listOf(DijkstraEdge("A", "B", 4), DijkstraEdge("A", "C", 1)),
        "B" to listOf(DijkstraEdge("B", "D", 1)),
        "C" to listOf(DijkstraEdge("C", "B", 2), DijkstraEdge("C", "D", 5)),
        "D" to emptyList()
    )

    fun stepsForDijkstra(source: String = "A", target: String = "D"): List<DijkstraTraceStep> {
        val steps = mutableListOf<DijkstraTraceStep>()
        val dist = mutableMapOf("A" to null as Int?, "B" to null, "C" to null, "D" to null)
        val pred = mutableMapOf("A" to null as String?, "B" to null, "C" to null, "D" to null)
        val pq = mutableListOf<Pair<Int, String>>() // (distance, vertex)

        fun snapshot(
            curr: String? = null,
            edge: DijkstraEdge? = null,
            path: List<String>? = null,
            event: DijkstraStepEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = DijkstraTraceStep(
            source = source,
            target = target,
            currentVertex = curr,
            distances = dist.toMap(),
            predecessors = pred.toMap(),
            priorityQueue = pq.sortedBy { it.first },
            relaxedEdge = edge,
            reconstructedPath = path,
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        dist[source] = 0
        pq.add(0 to source)

        steps.add(
            snapshot(
                curr = source,
                event = DijkstraStepEvent.START,
                explanation = "Dijkstra's Algorithm: Finding shortest weighted paths from $source to all vertices using non-negative edge weights.",
                badgeText = "READY · SOURCE $source"
            )
        )

        // Trace steps from lesson:
        // 1. Process A: relax A->B(4), A->C(1)
        pq.removeAt(0)
        dist["B"] = 4; pred["B"] = "A"; pq.add(4 to "B")
        dist["C"] = 1; pred["C"] = "A"; pq.add(1 to "C")

        steps.add(
            snapshot(
                curr = "A",
                edge = DijkstraEdge("A", "C", 1),
                event = DijkstraStepEvent.RELAX_EDGE,
                explanation = "Process A (dist 0): Relax A→B (dist B becomes 4) & A→C (dist C becomes 1). PQ: [(1, C), (4, B)].",
                badgeText = "RELAXED FROM A → dist[C]=1, dist[B]=4"
            )
        )

        // 2. Dequeue C (min dist 1): relax C->B(1+2=3 < 4!), C->D(1+5=6)
        pq.removeAll { it.second == "C" }
        dist["B"] = 3; pred["B"] = "C"; pq.removeAll { it.second == "B" }; pq.add(3 to "B")
        dist["D"] = 6; pred["D"] = "C"; pq.add(6 to "D")

        steps.add(
            snapshot(
                curr = "C",
                edge = DijkstraEdge("C", "B", 2),
                event = DijkstraStepEvent.RELAX_EDGE,
                explanation = "Process C (min dist 1): Relax C→B (1+2=3 < 4! Dist B improves to 3) & C→D (1+5=6). PQ: [(3, B), (6, D)].",
                badgeText = "IMPROVED dist[B]=3 via C!"
            )
        )

        // 3. Dequeue B (min dist 3): relax B->D(3+1=4 < 6!)
        pq.removeAll { it.second == "B" }
        dist["D"] = 4; pred["D"] = "B"; pq.removeAll { it.second == "D" }; pq.add(4 to "D")

        steps.add(
            snapshot(
                curr = "B",
                edge = DijkstraEdge("B", "D", 1),
                event = DijkstraStepEvent.RELAX_EDGE,
                explanation = "Process B (min dist 3): Relax B→D (3+1=4 < 6! Dist D improves to 4). PQ: [(4, D)].",
                badgeText = "IMPROVED dist[D]=4 via B!"
            )
        )

        // 4. Dequeue D (min dist 4): Finalized
        pq.removeAll { it.second == "D" }

        val path = listOf("A", "C", "B", "D")

        steps.add(
            snapshot(
                curr = "D",
                path = path,
                event = DijkstraStepEvent.DIJKSTRA_COMPLETE,
                isTerminal = true,
                explanation = "DIJKSTRA COMPLETE! Final shortest distance to D is 4 via path A-C-B-D (total weight 1+2+1 = 4).",
                badgeText = "PATH: A-C-B-D (WEIGHT 4)"
            )
        )

        return steps
    }
}
