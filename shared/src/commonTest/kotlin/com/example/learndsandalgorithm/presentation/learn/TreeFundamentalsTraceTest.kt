package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeFundamentalsTraceTest {

    @Test
    fun conceptTitlesMatchTreeConcepts() {
        assertEquals("Root Node", treeFundamentalsConceptTitle(TreeConcept.ROOT))
        assertEquals("Parent & Child Relationships", treeFundamentalsConceptTitle(TreeConcept.PARENT_CHILD))
        assertEquals("Sibling Nodes", treeFundamentalsConceptTitle(TreeConcept.SIBLINGS))
        assertEquals("Leaf Nodes", treeFundamentalsConceptTitle(TreeConcept.LEAVES))
        assertEquals("Subtrees", treeFundamentalsConceptTitle(TreeConcept.SUBTREE))
        assertEquals("Tree Measurements: Depth & Height", treeFundamentalsConceptTitle(TreeConcept.DEPTH_AND_HEIGHT))
    }

    @Test
    fun nodeMeasurementsAndStepSequenceRemainStable() {
        val steps = TreeFundamentalsTrace.stepsForFundamentals()
        val nodes = steps.first().nodes.associateBy { it.id }

        assertEquals(7, steps.size)
        assertEquals(listOf(
            TreeConcept.ROOT,
            TreeConcept.ROOT,
            TreeConcept.PARENT_CHILD,
            TreeConcept.SIBLINGS,
            TreeConcept.LEAVES,
            TreeConcept.SUBTREE,
            TreeConcept.DEPTH_AND_HEIGHT
        ), steps.map { it.concept })
        assertEquals(ConceptNode("A", "A (Root)", null, 0, 2, false), nodes["A"])
        assertEquals(ConceptNode("B", "B", "A", 1, 1, false), nodes["B"])
        assertEquals(ConceptNode("C", "C", "A", 1, 1, false), nodes["C"])
        assertEquals(ConceptNode("D", "D (Leaf)", "A", 1, 0, true), nodes["D"])
        assertEquals(ConceptNode("E", "E (Leaf)", "B", 2, 0, true), nodes["E"])
        assertEquals(ConceptNode("F", "F (Leaf)", "B", 2, 0, true), nodes["F"])
        assertEquals(ConceptNode("G", "G (Leaf)", "C", 2, 0, true), nodes["G"])
    }

    @Test
    fun stepsForFundamentalsGeneratesTerminologySteps() {
        val steps = TreeFundamentalsTrace.stepsForFundamentals()
        assertTrue(steps.isNotEmpty())

        val rootStep = steps.first { it.concept == TreeConcept.ROOT && it.highlightedNodeIds.contains("A") }
        assertEquals("A", rootStep.highlightedNodeIds.first())

        val leafStep = steps.first { it.concept == TreeConcept.LEAVES }
        assertEquals(setOf("E", "F", "G", "D"), leafStep.highlightedNodeIds)

        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(TreeConcept.DEPTH_AND_HEIGHT, lastStep.concept)
    }
}
