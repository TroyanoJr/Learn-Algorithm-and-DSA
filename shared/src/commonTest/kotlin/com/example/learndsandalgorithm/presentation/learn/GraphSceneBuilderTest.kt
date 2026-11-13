package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraphSceneBuilderTest {

    @Test
    fun testBuildBfsScenesQueueOrdering() {
        val steps = GraphBfsTrace.stepsForBfs("A")
        val scenes = GraphSceneBuilder.buildBfsScenes(steps)

        assertEquals(steps.size, scenes.size)
        assertTrue(scenes.isNotEmpty())

        val step1 = scenes[0]
        val sub = step1.subScenes.first()
        val nodeA = sub.entities.first { it.elementId == "vertex:A" }
        assertEquals("Queue", nodeA.badgeLabel)
    }

    @Test
    fun testBuildDfsScenesCallStack() {
        val steps = GraphDfsTrace.stepsForDfs("A")
        val scenes = GraphSceneBuilder.buildDfsScenes(steps)

        assertEquals(steps.size, scenes.size)

        val step2 = scenes[2] // step exploring B
        val sub = step2.subScenes.first()
        val nodeA = sub.entities.first { it.elementId == "vertex:A" }
        assertEquals("Stack", nodeA.badgeLabel)

        val nodeB = sub.entities.first { it.elementId == "vertex:B" }
        assertEquals(EntityVisualState.ACTIVE, nodeB.visualState)
        assertEquals("Active", nodeB.badgeLabel)
    }

    @Test
    fun testComponentsAndCyclesStates0_1_2AndBadges() {
        val step0 = ComponentCycleTraceStep(
            topic = ComponentCycleTopic.DIRECTED_CYCLE,
            currentVertex = null,
            visited = emptyList(),
            activeStates = mapOf("A" to 0, "B" to 1, "C" to 2),
            explanation = "State test",
            badgeText = "State test"
        )
        val scenes = GraphSceneBuilder.buildComponentsCycleScenes(listOf(step0))
        val sub = scenes.first().subScenes.first()

        // state 0 (A) -> DEFAULT
        val nodeA = sub.entities.first { it.elementId == "vertex:A" }
        assertEquals(EntityVisualState.DEFAULT, nodeA.visualState)
        assertNull(nodeA.badgeLabel)

        // state 1 (B) -> ACTIVE with "Stack" badge when not current
        val nodeB = sub.entities.first { it.elementId == "vertex:B" }
        assertEquals(EntityVisualState.ACTIVE, nodeB.visualState)
        assertEquals("Stack", nodeB.badgeLabel)

        // state 2 (C) -> VISITED
        val nodeC = sub.entities.first { it.elementId == "vertex:C" }
        assertEquals(EntityVisualState.VISITED, nodeC.visualState)

        // Current vertex with activeState 1 receives "Active" badge
        val stepCurrentActive = ComponentCycleTraceStep(
            topic = ComponentCycleTopic.DIRECTED_CYCLE,
            currentVertex = "B",
            visited = emptyList(),
            activeStates = mapOf("B" to 1),
            explanation = "Current active test",
            badgeText = "Current active test"
        )
        val scenesCurrent = GraphSceneBuilder.buildComponentsCycleScenes(listOf(stepCurrentActive))
        val subCurrent = scenesCurrent.first().subScenes.first()
        val nodeBActive = subCurrent.entities.first { it.elementId == "vertex:B" }
        assertEquals(EntityVisualState.ACTIVE, nodeBActive.visualState)
        assertEquals("Active", nodeBActive.badgeLabel)

        // Directed cycle connections use edge:A->B, edge:B->C, edge:C->A and highlight correctly
        val directedSteps = GraphComponentsCycleTrace.stepsForTopic(ComponentCycleTopic.DIRECTED_CYCLE)
        val directedScenes = GraphSceneBuilder.buildComponentsCycleScenes(directedSteps)
        val terminalScene = directedScenes.last()
        val termSub = terminalScene.subScenes.first()

        val expectedDirectedEdges = setOf("edge:A->B", "edge:B->C", "edge:C->A")
        val actualDirectedEdges = termSub.connections.map { it.connectionKey }.toSet()
        assertEquals(expectedDirectedEdges, actualDirectedEdges)

        val cycleEdge = termSub.connections.first { it.connectionKey == "edge:C->A" }
        assertEquals(ConnectionVisualState.HIGHLIGHTED, cycleEdge.visualState)
    }

    @Test
    fun testBuildComponentsCycleScenesConnectedComponentsMemberships() {
        val steps = GraphComponentsCycleTrace.stepsForTopic(ComponentCycleTopic.CONNECTED_COMPONENTS)
        val scenes = GraphSceneBuilder.buildComponentsCycleScenes(steps)

        // Initial step 0: before F is discovered, F must not display Comp 3
        val initialSub = scenes[0].subScenes.first()
        val nodeFInitial = initialSub.entities.first { it.elementId == "vertex:F" }
        assertNull(nodeFInitial.badgeLabel)

        // Terminal step: F appears in step.components (Comp 3) and is currentVertex -> "Active · Comp 3"
        val terminalSub = scenes.last().subScenes.first()
        val nodeFTerminal = terminalSub.entities.first { it.elementId == "vertex:F" }
        assertEquals("Active \u00B7 Comp 3", nodeFTerminal.badgeLabel)
    }

    @Test
    fun testFundamentalsSemanticsTargetAndEdges() {
        val degreeSteps = GraphFundamentalsTrace.stepsForTopic(GraphConceptTopic.VERTEX_DEGREE)
        val degreeScenes = GraphSceneBuilder.buildFundamentalsScenes(degreeSteps)

        val nodeD = degreeScenes.first().subScenes.first().entities.first { it.elementId == "vertex:D" }
        assertEquals("deg=3", nodeD.badgeLabel)
        assertEquals(EntityVisualState.TARGET, nodeD.visualState) // highlightedVertices -> TARGET

        val pathSteps = GraphFundamentalsTrace.stepsForTopic(GraphConceptTopic.PATHS_AND_CYCLES)
        val pathScenes = GraphSceneBuilder.buildFundamentalsScenes(pathSteps)
        val pathNode = pathScenes.first().subScenes.first().entities.first { it.elementId == "vertex:A" }
        assertEquals(EntityVisualState.TARGET, pathNode.visualState) // pathSequence -> TARGET

        val directedSteps = GraphFundamentalsTrace.stepsForTopic(GraphConceptTopic.DIRECTED_VS_UNDIRECTED)
        val directedScenes = GraphSceneBuilder.buildFundamentalsScenes(directedSteps)
        val undirectedConn = directedScenes.first().subScenes.first().connections.first()
        assertEquals("edge:A<->B", undirectedConn.connectionKey) // undirected edge identity

        val directedConn = directedScenes.last().subScenes.first().connections.first()
        assertEquals("edge:A->B", directedConn.connectionKey) // directed edge identity
        assertEquals(ConnectionVisualState.HIGHLIGHTED, directedConn.visualState) // highlightedEdges -> HIGHLIGHTED
    }

    @Test
    fun testShortestPathOverlapActiveDistancePredecessorAndQueue() {
        val steps = GraphUnweightedShortestPathTrace.stepsForShortestPath("A", "E")
        val scenes = GraphSceneBuilder.buildShortestPathScenes(steps)

        assertEquals(steps.size, scenes.size)

        // Step 0: source A is currentVertex with distance 0 and queued
        val step0 = scenes[0]
        val sub0 = step0.subScenes.first()
        val nodeA0 = sub0.entities.first { it.elementId == "vertex:A" }
        assertEquals(EntityVisualState.ACTIVE, nodeA0.visualState)
        assertEquals("Active: d=0 \u00B7 Q[1]", nodeA0.badgeLabel)

        val terminalScene = scenes.last()
        val sub = terminalScene.subScenes.first()

        val nodeB = sub.entities.first { it.elementId == "vertex:B" }
        assertEquals("d=1 (p=A)", nodeB.badgeLabel)
        assertEquals(EntityVisualState.TARGET, nodeB.visualState)

        val pathEdgeAB = sub.connections.first { it.connectionKey == "edge:A<->B" }
        assertEquals(ConnectionVisualState.PATH, pathEdgeAB.visualState)
    }

    @Test
    fun testShortestPathNonCurrentQueuedVertexWithDistanceAndPredecessor() {
        val syntheticStep = UnweightedShortestPathTraceStep(
            source = "A",
            target = "E",
            currentVertex = "B", // B is current, NOT C
            distances = mapOf("C" to 2),
            predecessors = mapOf("C" to "A"),
            queue = listOf("C"), // C is present in queue
            event = ShortestPathEvent.DEQUEUE_AND_UPDATE_DIST,
            explanation = "synthetic test",
            badgeText = "syn"
        )
        val scenes = GraphSceneBuilder.buildShortestPathScenes(listOf(syntheticStep))
        val sub = scenes.first().subScenes.first()
        val nodeC = sub.entities.first { it.elementId == "vertex:C" }

        assertEquals(EntityVisualState.VISITED, nodeC.visualState)
        assertEquals("d=2 (p=A) \u00B7 Q[1]", nodeC.badgeLabel)
    }

    @Test
    fun testVisualConnectionDirectedPropertyAcrossFamilies() {
        // BFS -> false
        val bfsScenes = GraphSceneBuilder.buildBfsScenes(GraphBfsTrace.stepsForBfs())
        assertFalse(bfsScenes.first().subScenes.first().connections.first().directed)

        // DFS -> false
        val dfsScenes = GraphSceneBuilder.buildDfsScenes(GraphDfsTrace.stepsForDfs())
        assertFalse(dfsScenes.first().subScenes.first().connections.first().directed)

        // Connected Components -> false
        val compScenes = GraphSceneBuilder.buildComponentsCycleScenes(GraphComponentsCycleTrace.stepsForTopic(ComponentCycleTopic.CONNECTED_COMPONENTS))
        assertFalse(compScenes.first().subScenes.first().connections.first().directed)

        // Undirected Cycle -> false
        val undirCycleScenes = GraphSceneBuilder.buildComponentsCycleScenes(GraphComponentsCycleTrace.stepsForTopic(ComponentCycleTopic.UNDIRECTED_CYCLE))
        assertFalse(undirCycleScenes.first().subScenes.first().connections.first().directed)

        // Directed Cycle -> true
        val dirCycleScenes = GraphSceneBuilder.buildComponentsCycleScenes(GraphComponentsCycleTrace.stepsForTopic(ComponentCycleTopic.DIRECTED_CYCLE))
        assertTrue(dirCycleScenes.first().subScenes.first().connections.first().directed)

        // Fundamentals undirected -> false
        val fundUndirScenes = GraphSceneBuilder.buildFundamentalsScenes(GraphFundamentalsTrace.stepsForTopic(GraphConceptTopic.DIRECTED_VS_UNDIRECTED))
        assertFalse(fundUndirScenes.first().subScenes.first().connections.first().directed)

        // Fundamentals directed -> true
        val fundDirScenes = GraphSceneBuilder.buildFundamentalsScenes(GraphFundamentalsTrace.stepsForTopic(GraphConceptTopic.DIRECTED_VS_UNDIRECTED))
        assertTrue(fundDirScenes.last().subScenes.first().connections.first().directed)

        // Shortest Path -> false
        val spScenes = GraphSceneBuilder.buildShortestPathScenes(GraphUnweightedShortestPathTrace.stepsForShortestPath())
        assertFalse(spScenes.first().subScenes.first().connections.first().directed)

        // Dijkstra -> true
        val dijkstraScenes = GraphSceneBuilder.buildDijkstraScenes(DijkstraTrace.stepsForDijkstra())
        assertTrue(dijkstraScenes.first().subScenes.first().connections.first().directed)

        // Topological Sort -> true
        val topoScenes = GraphSceneBuilder.buildTopologicalSortScenes(TopologicalSortTrace.stepsForTopoSort())
        assertTrue(topoScenes.first().subScenes.first().connections.first().directed)
    }

    @Test
    fun testEmptyInputSafety() {
        assertTrue(GraphSceneBuilder.buildBfsScenes(emptyList()).isEmpty())
        assertTrue(GraphSceneBuilder.buildDfsScenes(emptyList()).isEmpty())
        assertTrue(GraphSceneBuilder.buildComponentsCycleScenes(emptyList()).isEmpty())
        assertTrue(GraphSceneBuilder.buildFundamentalsScenes(emptyList()).isEmpty())
        assertTrue(GraphSceneBuilder.buildShortestPathScenes(emptyList()).isEmpty())
        assertTrue(GraphSceneBuilder.buildDijkstraScenes(emptyList()).isEmpty())
        assertTrue(GraphSceneBuilder.buildTopologicalSortScenes(emptyList()).isEmpty())
    }

    @Test
    fun testBuildDijkstraScenes() {
        val steps = DijkstraTrace.stepsForDijkstra("A", "D")
        val scenes = GraphSceneBuilder.buildDijkstraScenes(steps)

        assertEquals(steps.size, scenes.size)
        assertTrue(scenes.isNotEmpty())

        val sub0 = scenes.first().subScenes.first()
        // Exactly 4 vertices
        assertEquals(4, sub0.entities.size)
        // Exactly 5 connections
        assertEquals(5, sub0.connections.size)
        // All connections directed
        assertTrue(sub0.connections.all { it.directed })

        // Check edge weights and keys
        val edgeAB = sub0.connections.first { it.connectionKey == "edge:A->B" }
        assertEquals("w=4", edgeAB.label)

        // Current vertex ACTIVE
        val nodeA0 = sub0.entities.first { it.elementId == "vertex:A" }
        assertEquals(EntityVisualState.ACTIVE, nodeA0.visualState)

        // Relaxed edge in step 1 (A -> C)
        val sub1 = scenes[1].subScenes.first()
        val relaxedAC = sub1.connections.first { it.connectionKey == "edge:A->C" }
        assertEquals(ConnectionVisualState.RELAXING, relaxedAC.visualState)

        // Terminal step: reconstructed path A -> C -> B -> D
        val subTerm = scenes.last().subScenes.first()
        val pathAC = subTerm.connections.first { it.connectionKey == "edge:A->C" }
        val pathCB = subTerm.connections.first { it.connectionKey == "edge:C->B" }
        val pathBD = subTerm.connections.first { it.connectionKey == "edge:B->D" }
        assertEquals(ConnectionVisualState.PATH, pathAC.visualState)
        assertEquals(ConnectionVisualState.PATH, pathCB.visualState)
        assertEquals(ConnectionVisualState.PATH, pathBD.visualState)

        // Path vertices should be TARGET when not active
        val nodeBTerm = subTerm.entities.first { it.elementId == "vertex:B" }
        assertEquals(EntityVisualState.TARGET, nodeBTerm.visualState)
    }

    @Test
    fun testBuildTopologicalSortScenes() {
        val steps = TopologicalSortTrace.stepsForTopoSort()
        val scenes = GraphSceneBuilder.buildTopologicalSortScenes(steps)

        assertEquals(steps.size, scenes.size)
        assertTrue(scenes.isNotEmpty())

        val sub0 = scenes.first().subScenes.first()
        // 5 vertices, 5 connections
        assertEquals(5, sub0.entities.size)
        assertEquals(5, sub0.connections.size)
        assertTrue(sub0.connections.all { it.directed })

        // Check DAG topology keys
        val expectedKeys = setOf("edge:A->C", "edge:B->C", "edge:B->D", "edge:C->E", "edge:D->E")
        val actualKeys = sub0.connections.map { it.connectionKey }.toSet()
        assertEquals(expectedKeys, actualKeys)

        // Step 1: Dequeue A -> A is currentVertex (ACTIVE), inDegrees and queue represented
        val sub1 = scenes[1].subScenes.first()
        val nodeA1 = sub1.entities.first { it.elementId == "vertex:A" }
        assertEquals(EntityVisualState.ACTIVE, nodeA1.visualState)

        // Terminal step: all processed vertices VISITED or ACTIVE
        val subTerm = scenes.last().subScenes.first()
        val nodeETerm = subTerm.entities.first { it.elementId == "vertex:E" }
        assertEquals(EntityVisualState.ACTIVE, nodeETerm.visualState)
        val nodeATerm = subTerm.entities.first { it.elementId == "vertex:A" }
        assertEquals(EntityVisualState.VISITED, nodeATerm.visualState)
    }

    @Test
    fun testBfsBidirectionalEdgesNormalizedToOneConnection() {
        val bidirectionalGraph = mapOf(
            "A" to listOf("B"),
            "B" to listOf("A")
        )
        val step = GraphBfsTraceStep(
            currentVertex = "A",
            event = BfsStepEvent.START,
            explanation = "test",
            badgeText = "test"
        )
        val scenes = GraphSceneBuilder.buildBfsScenes(listOf(step), bidirectionalGraph)
        val connections = scenes.first().subScenes.first().connections
        assertEquals(1, connections.size)
        assertEquals("edge:A<->B", connections.first().connectionKey)
    }
}
