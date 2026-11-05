package com.example.learndsandalgorithm.presentation.learn

internal enum class ComponentCycleTopic {
    CONNECTED_COMPONENTS,
    UNDIRECTED_CYCLE,
    DIRECTED_CYCLE
}

internal data class ComponentCycleTraceStep(
    val topic: ComponentCycleTopic,
    val currentVertex: String? = null,
    val visited: List<String> = emptyList(),
    val components: List<List<String>> = emptyList(),
    val activeStates: Map<String, Int> = emptyMap(), // 0=unvisited, 1=active, 2=finished
    val cycleDetected: Boolean = false,
    val cycleEdges: Pair<String, String>? = null,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GraphComponentsCycleTrace {

    fun stepsForTopic(topic: ComponentCycleTopic): List<ComponentCycleTraceStep> {
        return when (topic) {
            ComponentCycleTopic.CONNECTED_COMPONENTS -> generateComponentsSteps()
            ComponentCycleTopic.UNDIRECTED_CYCLE -> generateUndirectedCycleSteps()
            ComponentCycleTopic.DIRECTED_CYCLE -> generateDirectedCycleSteps()
        }
    }

    private fun generateComponentsSteps(): List<ComponentCycleTraceStep> {
        val steps = mutableListOf<ComponentCycleTraceStep>()
        val visited = mutableListOf<String>()
        val componentsList = mutableListOf<List<String>>()

        fun snapshot(
            v: String? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = ComponentCycleTraceStep(
            topic = ComponentCycleTopic.CONNECTED_COMPONENTS,
            currentVertex = v,
            visited = visited.toList(),
            components = componentsList.toList(),
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(null, false, "Connected Components: Graph with edges {AB, BC, DE} and isolated vertex F. Outer loop explores unvisited vertices.", "READY · DISCOVER COMPONENTS"))

        // Component 1: {A, B, C}
        visited.addAll(listOf("A", "B", "C"))
        componentsList.add(listOf("A", "B", "C"))
        steps.add(snapshot("A", false, "Start outer loop at A: DFS explores component 1 = {A, B, C}.", "DISCOVERED {A, B, C}"))

        // Component 2: {D, E}
        visited.addAll(listOf("D", "E"))
        componentsList.add(listOf("D", "E"))
        steps.add(snapshot("D", false, "Next unvisited in outer loop is D: DFS explores component 2 = {D, E}.", "DISCOVERED {D, E}"))

        // Component 3: {F}
        visited.add("F")
        componentsList.add(listOf("F"))
        steps.add(snapshot("F", true, "Next unvisited in outer loop is isolated F: Component 3 = {F}. All components discovered: {A,B,C}, {D,E}, {F}.", "COMPLETE: 3 COMPONENTS"))

        return steps
    }

    private fun generateUndirectedCycleSteps(): List<ComponentCycleTraceStep> {
        val steps = mutableListOf<ComponentCycleTraceStep>()
        val visited = mutableListOf<String>()

        fun snapshot(
            v: String? = null,
            cycle: Boolean = false,
            cEdge: Pair<String, String>? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = ComponentCycleTraceStep(
            topic = ComponentCycleTopic.UNDIRECTED_CYCLE,
            currentVertex = v,
            visited = visited.toList(),
            cycleDetected = cycle,
            cycleEdges = cEdge,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(null, false, null, false, "Undirected Cycle Detection: Triangle {AB, BC, CA}. Skip parent edge during DFS.", "READY · UNDIRECTED CYCLE"))

        visited.add("A")
        steps.add(snapshot("A", false, null, false, "DFS at A: Visited set = {A}.", "VISITED A"))

        visited.add("B")
        steps.add(snapshot("B", false, null, false, "DFS from A to B: Parent of B is A. Visited set = {A, B}.", "VISITED B"))

        visited.add("C")
        steps.add(snapshot("C", false, null, false, "DFS from B to C: Parent of C is B. Visited set = {A, B, C}.", "VISITED C"))

        steps.add(snapshot("C", true, Pair("C", "A"), true, "CYCLE DETECTED! From C, neighbor A is visited and A != parent B. Edge C-A closes a cycle!", "UNDIRECTED CYCLE DETECTED"))

        return steps
    }

    private fun generateDirectedCycleSteps(): List<ComponentCycleTraceStep> {
        val steps = mutableListOf<ComponentCycleTraceStep>()
        val states = mutableMapOf("A" to 0, "B" to 0, "C" to 0)

        fun snapshot(
            v: String? = null,
            cycle: Boolean = false,
            cEdge: Pair<String, String>? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = ComponentCycleTraceStep(
            topic = ComponentCycleTopic.DIRECTED_CYCLE,
            currentVertex = v,
            activeStates = states.toMap(),
            cycleDetected = cycle,
            cycleEdges = cEdge,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(snapshot(null, false, null, false, "Directed Cycle Detection: Directed graph A → B → C → A. 3 States: 0=unvisited, 1=active (on stack), 2=finished.", "READY · DIRECTED CYCLE"))

        states["A"] = 1
        steps.add(snapshot("A", false, null, false, "DFS A: Set state[A] = 1 (Active on stack).", "A = ACTIVE (1)"))

        states["B"] = 1
        steps.add(snapshot("B", false, null, false, "DFS A → B: Set state[B] = 1 (Active on stack).", "B = ACTIVE (1)"))

        states["C"] = 1
        steps.add(snapshot("C", false, null, false, "DFS B → C: Set state[C] = 1 (Active on stack).", "C = ACTIVE (1)"))

        steps.add(snapshot("C", true, Pair("C", "A"), true, "DIRECTED CYCLE DETECTED! Edge C → A points to active ancestor A (state[A] == 1). Back-edge proves cycle!", "DIRECTED CYCLE DETECTED"))

        return steps
    }
}
