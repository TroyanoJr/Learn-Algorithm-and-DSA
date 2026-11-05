package com.example.learndsandalgorithm.presentation.learn

internal enum class BfsStepEvent {
    START,
    DEQUEUE_VERTEX,
    ENQUEUE_NEIGHBORS,
    BFS_COMPLETE
}

internal data class GraphBfsTraceStep(
    val currentVertex: String? = null,
    val queue: List<String> = emptyList(),
    val visited: List<String> = emptyList(),
    val event: BfsStepEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GraphBfsTrace {

    val graph = mapOf(
        "A" to listOf("B", "C"),
        "B" to listOf("A", "D"),
        "C" to listOf("A", "D"),
        "D" to listOf("B", "C", "E"),
        "E" to listOf("D")
    )

    fun stepsForBfs(start: String = "A"): List<GraphBfsTraceStep> {
        val steps = mutableListOf<GraphBfsTraceStep>()
        val queue = mutableListOf<String>()
        val visited = mutableListOf<String>()

        fun snapshot(
            v: String? = null,
            event: BfsStepEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphBfsTraceStep(
            currentVertex = v,
            queue = queue.toList(),
            visited = visited.toList(),
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 1: Start & Enqueue Start
        visited.add(start)
        queue.add(start)

        steps.add(
            snapshot(
                event = BfsStepEvent.START,
                explanation = "BFS Start: Enqueue $start. Visited set = {$start}. Queue = [$start].",
                badgeText = "ENQUEUED START: $start"
            )
        )

        val tableSteps = listOf(
            Triple("A", listOf("B", "C"), "Dequeue A. Explore neighbors of A: Enqueue B and C."),
            Triple("B", listOf("D"), "Dequeue B. Explore neighbors of B: Enqueue D."),
            Triple("C", emptyList<String>(), "Dequeue C. Neighbor D is already visited. No new enqueues."),
            Triple("D", listOf("E"), "Dequeue D. Explore neighbors of D: Enqueue E."),
            Triple("E", emptyList<String>(), "Dequeue E. Neighbors already visited. Queue empty.")
        )

        tableSteps.forEach { (vertex, newNeighbors, expl) ->
            queue.removeAt(0)

            steps.add(
                snapshot(
                    v = vertex,
                    event = BfsStepEvent.DEQUEUE_VERTEX,
                    explanation = "DEQUEUE $vertex. $expl",
                    badgeText = "DEQUEUED: $vertex"
                )
            )

            if (newNeighbors.isNotEmpty()) {
                newNeighbors.forEach { neighbor ->
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor)
                        queue.add(neighbor)
                    }
                }

                steps.add(
                    snapshot(
                        v = vertex,
                        event = BfsStepEvent.ENQUEUE_NEIGHBORS,
                        explanation = "ENQUEUED NEIGHBORS of $vertex: [${newNeighbors.joinToString(", ")}]. Queue = [${queue.joinToString(", ")}].",
                        badgeText = "ENQUEUED [${newNeighbors.joinToString(", ")}]"
                    )
                )
            }
        }

        // Terminal step
        steps.add(
            snapshot(
                event = BfsStepEvent.BFS_COMPLETE,
                isTerminal = true,
                explanation = "BFS Complete! Visited order: [${visited.joinToString(", ")}]. Explores layer-by-layer in O(V + E) time.",
                badgeText = "BFS COMPLETE: [${visited.joinToString(", ")}]"
            )
        )

        return steps
    }
}
