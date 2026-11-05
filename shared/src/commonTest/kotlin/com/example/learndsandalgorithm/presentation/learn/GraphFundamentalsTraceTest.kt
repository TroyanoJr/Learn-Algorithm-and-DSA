package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphFundamentalsTraceTest {

    @Test
    fun stepsForTopicGeneratesVerticesAndEdgesTrace() {
        val steps = GraphFundamentalsTrace.stepsForTopic(GraphConceptTopic.VERTICES_AND_EDGES)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(5, lastStep.highlightedVertices.size)
    }

    @Test
    fun stepsForTopicCalculatesCorrectDegreeForVertexD() {
        val steps = GraphFundamentalsTrace.stepsForTopic(GraphConceptTopic.VERTEX_DEGREE)
        assertTrue(steps.isNotEmpty())

        val lastStep = steps.last()
        assertEquals(3, lastStep.degreeVal)
    }
}
