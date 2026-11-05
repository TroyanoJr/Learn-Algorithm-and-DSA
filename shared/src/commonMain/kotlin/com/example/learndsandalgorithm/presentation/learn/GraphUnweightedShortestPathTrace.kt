package com.example.learndsandalgorithm.presentation.learn

internal enum class ShortestPathEvent {
    START,
    DEQUEUE_AND_UPDATE_DIST,
    RECONSTRUCT_PATH,
    PATH_COMPLETE
}

internal data class UnweightedShortestPathTraceStep(
    val source: String = "A",
    val target: String = "E",
    val currentVertex: String? = null,
    val distances: Map<String, Int?> = emptyMap(),
    val predecessors: Map<String, String?> = emptyMap(),
    val queue: List<String> = emptyList(),
    val reconstructedPath: List<String>? = null,
    val event: ShortestPathEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GraphUnweightedShortestPathTrace {

    val graph = mapOf(
        "A" to listOf("B", "C"),
        "B" to listOf("A", "D"),
        "C" to listOf("A", "D"),
        "D" to listOf("B", "C", "E"),
        "E" to listOf("D")
    )

    fun stepsForShortestPath(source: String = "A", target: String = "E"): List<UnweightedShortestPathTraceStep> {
        val steps = mutableListOf<UnweightedShortestPathTraceStep>()
        val dist = mutableMapOf("A" to null as Int?, "B" to null, "C" to null, "D" to null, "E" to null)
        val pred = mutableMapOf("A" to null as String?, "B" to null, "C" to null, "D" to null, "E" to null)
        val queue = mutableListOf<String>()

        fun snapshot(
            curr: String? = null,
            path: List<String>? = null,
            event: ShortestPathEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = UnweightedShortestPathTraceStep(
            source = source,
            target = target,
            currentVertex = curr,
            distances = dist.toMap(),
            predecessors = pred.toMap(),
            queue = queue.toList(),
            reconstructedPath = path,
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        dist[source] = 0
        queue.add(source)

        steps.add(
            snapshot(
                curr = source,
                event = ShortestPathEvent.START,
                explanation = "Unweighted Shortest Path: Finding path with fewest edges from $source to $target using BFS.",
                badgeText = "READY · SOURCE $source TO $target"
            )
        )

        val tableTrace = listOf(
            Triple("A", listOf("B" to 1, "C" to 1), "A: dist[A]=0. Discover B and C at dist=1 with pred=A."),
            Triple("B", listOf("D" to 2), "B: dist[B]=1. Discover D at dist=2 with pred=B."),
            Triple("C", emptyList<Pair<String, Int>>(), "C: dist[C]=1. D is already discovered at dist=2. pred[D] stays B."),
            Triple("D", listOf("E" to 3), "D: dist[D]=2. Discover target E at dist=3 with pred=D."),
            Triple("E", emptyList<Pair<String, Int>>(), "E: dist[E]=3. All vertices processed.")
        )

        tableTrace.forEach { (vertex, newUpdates, expl) ->
            if (queue.isNotEmpty()) queue.removeAt(0)

            newUpdates.forEach { (nbr, dVal) ->
                if (dist[nbr] == null) {
                    dist[nbr] = dVal
                    pred[nbr] = vertex
                    queue.add(nbr)
                }
            }

            steps.add(
                snapshot(
                    curr = vertex,
                    event = ShortestPathEvent.DEQUEUE_AND_UPDATE_DIST,
                    explanation = expl,
                    badgeText = "PROCESSED $vertex (dist=${dist[vertex]})"
                )
            )
        }

        // Reconstruct Path: E -> D -> B -> A => A -> B -> D -> E
        val reconstructed = listOf("A", "B", "D", "E")

        steps.add(
            snapshot(
                curr = target,
                path = reconstructed,
                event = ShortestPathEvent.RECONSTRUCT_PATH,
                explanation = "Reconstructing Shortest Path from target E backward via predecessors: E ← pred[E]=D ← pred[D]=B ← pred[B]=A. Reversing yields A-B-D-E.",
                badgeText = "RECONSTRUCTED: A-B-D-E"
            )
        )

        steps.add(
            snapshot(
                curr = target,
                path = reconstructed,
                event = ShortestPathEvent.PATH_COMPLETE,
                isTerminal = true,
                explanation = "Shortest Path Complete! Route A-B-D-E has fewest edges (3 edges). Total distance = 3.",
                badgeText = "PATH: A-B-D-E (3 EDGES)"
            )
        )

        return steps
    }
}
