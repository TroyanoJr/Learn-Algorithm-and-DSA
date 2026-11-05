package com.example.learndsandalgorithm.presentation.learn

internal enum class DfsStepEvent {
    START,
    EXPLORE_VERTEX,
    BACKTRACK,
    DFS_COMPLETE
}

internal data class GraphDfsTraceStep(
    val currentVertex: String? = null,
    val callStack: List<String> = emptyList(),
    val visited: List<String> = emptyList(),
    val event: DfsStepEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GraphDfsTrace {

    val graph = mapOf(
        "A" to listOf("B", "C"),
        "B" to listOf("A", "D"),
        "C" to listOf("A", "D"),
        "D" to listOf("B", "C", "E"),
        "E" to listOf("D")
    )

    fun stepsForDfs(start: String = "A"): List<GraphDfsTraceStep> {
        val steps = mutableListOf<GraphDfsTraceStep>()
        val callStack = mutableListOf<String>()
        val visited = mutableListOf<String>()

        fun snapshot(
            v: String? = null,
            event: DfsStepEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = GraphDfsTraceStep(
            currentVertex = v,
            callStack = callStack.toList(),
            visited = visited.toList(),
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = DfsStepEvent.START,
                explanation = "DFS Start: Begin recursive exploration from vertex $start. DFS uses a Call Stack and explores as deep as possible before backtracking.",
                badgeText = "READY · START $start"
            )
        )

        // Trace from lesson: A -> B -> D -> C (backtrack) -> E
        val sequence = listOf(
            Triple("A", false, "EXPLORE A: Push A onto Call Stack. Mark A visited. Alphabetical neighbor: B."),
            Triple("B", false, "EXPLORE B: Push B onto Call Stack. Mark B visited. Alphabetical neighbor: D."),
            Triple("D", false, "EXPLORE D: Push D onto Call Stack. Mark D visited. Alphabetical neighbor: C."),
            Triple("C", false, "EXPLORE C: Push C onto Call Stack. Mark C visited. All neighbors of C (A, D) are already visited! Backtrack to D."),
            Triple("D", true, "BACKTRACK to D: Pop C. Unvisited neighbor of D remains: E!"),
            Triple("E", false, "EXPLORE E: Push E onto Call Stack. Mark E visited. All neighbors visited.")
        )

        sequence.forEach { (vertex, isBacktrack, expl) ->
            if (isBacktrack) {
                if (callStack.isNotEmpty()) callStack.removeAt(callStack.lastIndex)
                steps.add(
                    snapshot(
                        v = vertex,
                        event = DfsStepEvent.BACKTRACK,
                        explanation = expl,
                        badgeText = "BACKTRACK TO $vertex"
                    )
                )
            } else {
                callStack.add(vertex)
                if (!visited.contains(vertex)) visited.add(vertex)
                steps.add(
                    snapshot(
                        v = vertex,
                        event = DfsStepEvent.EXPLORE_VERTEX,
                        explanation = expl,
                        badgeText = "EXPLORE $vertex"
                    )
                )
            }
        }

        // Complete
        steps.add(
            snapshot(
                event = DfsStepEvent.DFS_COMPLETE,
                isTerminal = true,
                explanation = "DFS Complete! Visited order: [${visited.joinToString(", ")}]. Time: O(V + E), Space: O(V) stack depth.",
                badgeText = "DFS COMPLETE: [${visited.joinToString(", ")}]"
            )
        )

        return steps
    }
}
