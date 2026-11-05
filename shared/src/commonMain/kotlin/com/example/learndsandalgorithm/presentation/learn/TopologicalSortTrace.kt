package com.example.learndsandalgorithm.presentation.learn

internal enum class TopoSortEvent {
    START,
    DEQUEUE_ZERO_INDEGREE,
    DECREASE_INDEGREE,
    TOPO_COMPLETE
}

internal data class TopologicalSortTraceStep(
    val graph: Map<String, List<String>>,
    val inDegrees: Map<String, Int>,
    val queue: List<String>,
    val currentVertex: String? = null,
    val topoOrder: List<String> = emptyList(),
    val event: TopoSortEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object TopologicalSortTrace {

    val dag = mapOf(
        "A" to listOf("C"),
        "B" to listOf("C", "D"),
        "C" to listOf("E"),
        "D" to listOf("E"),
        "E" to emptyList()
    )

    fun stepsForTopoSort(): List<TopologicalSortTraceStep> {
        val steps = mutableListOf<TopologicalSortTraceStep>()
        val inDeg = mutableMapOf("A" to 0, "B" to 0, "C" to 2, "D" to 1, "E" to 2)
        val queue = mutableListOf("A", "B")
        val order = mutableListOf<String>()

        fun snapshot(
            curr: String? = null,
            event: TopoSortEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TopologicalSortTraceStep(
            graph = dag,
            inDegrees = inDeg.toMap(),
            queue = queue.toList(),
            currentVertex = curr,
            topoOrder = order.toList(),
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = TopoSortEvent.START,
                explanation = "Topological Sorting (Kahn's Algorithm): DAG dependency ordering. Initial in-degrees: A:0, B:0, C:2, D:1, E:2. Queue of 0-in-degree vertices = [A, B].",
                badgeText = "READY · KAHN'S ALGORITHM"
            )
        )

        // 1. Dequeue A
        val vA = queue.removeAt(0)
        order.add(vA)
        inDeg["C"] = 1

        steps.add(
            snapshot(
                curr = "A",
                event = TopoSortEvent.DEQUEUE_ZERO_INDEGREE,
                explanation = "Dequeue A (in-degree 0). Add A to topological output. Decrease neighbor C's in-degree from 2 to 1. Queue = [B].",
                badgeText = "SCHEDULED: A (inDegree[C]→1)"
            )
        )

        // 2. Dequeue B
        val vB = queue.removeAt(0)
        order.add(vB)
        inDeg["C"] = 0; queue.add("C")
        inDeg["D"] = 0; queue.add("D")

        steps.add(
            snapshot(
                curr = "B",
                event = TopoSortEvent.DEQUEUE_ZERO_INDEGREE,
                explanation = "Dequeue B (in-degree 0). Add B to output. Decrease neighbors C (2→0, enqueue C) and D (1→0, enqueue D). Queue = [C, D].",
                badgeText = "SCHEDULED: B (ENQUEUED C, D)"
            )
        )

        // 3. Dequeue C
        val vC = queue.removeAt(0)
        order.add(vC)
        inDeg["E"] = 1

        steps.add(
            snapshot(
                curr = "C",
                event = TopoSortEvent.DEQUEUE_ZERO_INDEGREE,
                explanation = "Dequeue C (in-degree 0). Add C to output. Decrease neighbor E's in-degree from 2 to 1. Queue = [D].",
                badgeText = "SCHEDULED: C (inDegree[E]→1)"
            )
        )

        // 4. Dequeue D
        val vD = queue.removeAt(0)
        order.add(vD)
        inDeg["E"] = 0; queue.add("E")

        steps.add(
            snapshot(
                curr = "D",
                event = TopoSortEvent.DEQUEUE_ZERO_INDEGREE,
                explanation = "Dequeue D (in-degree 0). Add D to output. Decrease neighbor E's in-degree from 1 to 0 (enqueue E). Queue = [E].",
                badgeText = "SCHEDULED: D (ENQUEUED E)"
            )
        )

        // 5. Dequeue E
        val vE = queue.removeAt(0)
        order.add(vE)

        steps.add(
            snapshot(
                curr = "E",
                event = TopoSortEvent.TOPO_COMPLETE,
                isTerminal = true,
                explanation = "TOPOLOGICAL SORT COMPLETE! Valid dependency order: [${order.joinToString(", ")}]. Time: O(V + E), Space: O(V).",
                badgeText = "ORDER: [${order.joinToString(", ")}]"
            )
        )

        return steps
    }
}
