package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DivideConquerFundamentalsTraceTest {

    @Test
    fun stepsForMaxFinderGeneratesAllThreePhases() {
        val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder(listOf(4, 2, 9, 7, 5))
        assertTrue(steps.isNotEmpty())

        // Initial Step
        val step0 = steps.first()
        assertEquals(DivideConquerPhase.DIVIDE, step0.phase)

        // Verify Divide phase steps
        val divideSteps = steps.filter { it.phase == DivideConquerPhase.DIVIDE }
        assertTrue(divideSteps.isNotEmpty())

        // Verify Conquer phase steps
        val conquerStep = steps.firstOrNull { it.phase == DivideConquerPhase.CONQUER }
        assertNotNull(conquerStep)

        // Verify Combine phase steps
        val combineSteps = steps.filter { it.phase == DivideConquerPhase.COMBINE }
        assertTrue(combineSteps.isNotEmpty())

        // Verify Terminal step
        val lastStep = steps.last()
        assertTrue(lastStep.isTerminal)
        assertEquals(9, lastStep.combinedValue)
    }

    @Test
    fun defaultExamplePreservesTenEducationalStepsAndExactCopy() {
        val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder()

        assertEquals(10, steps.size)
        assertEquals(
            listOf(
                "READY · INPUT [4, 2, 9, 7, 5]",
                "DIVIDE ROOT → LEFT & RIGHT",
                "DIVIDE LEFT → [4, 2] & [9]",
                "DIVIDE → BASE CASES [4] & [2]",
                "DIVIDE RIGHT → [7] & [5]",
                "CONQUER: BASE CASES SOLVED",
                "COMBINE [4, 2] → 4",
                "COMBINE LEFT [4, 2, 9] → 9",
                "COMBINE RIGHT [7, 5] → 7",
                "FINAL RESULT: MAX = 9"
            ),
            steps.map { it.badgeText }
        )
        assertEquals(
            listOf(
                "Divide & Conquer: Finding the maximum element in [4, 2, 9, 7, 5]. 3 Phases: Divide → Conquer → Combine.",
                "1. DIVIDE: Split root [4, 2, 9, 7, 5] at mid = index 2 into Left [4, 2, 9] and Right [7, 5].",
                "1. DIVIDE: Split Left subproblem [4, 2, 9] at mid = index 1 into [4, 2] and [9]. Note: [9] is a base case (length 1).",
                "1. DIVIDE: Split [4, 2] into singletons [4] and [2] (base cases).",
                "1. DIVIDE: Split Right subproblem [7, 5] into base cases [7] and [5].",
                "2. CONQUER: Base cases reached (length = 1). Subproblem results solved directly: [4]→4, [2]→2, [9]→9, [7]→7, [5]→5.",
                "3. COMBINE: max(leftMax=4, rightMax=2) = 4 for subproblem [4, 2].",
                "3. COMBINE: max(leftMax=4, rightMax=9) = 9 for Left half [4, 2, 9].",
                "3. COMBINE: max(leftMax=7, rightMax=5) = 7 for Right half [7, 5].",
                "3. COMBINE COMPLETE: max(leftMax=9, rightMax=7) = 9. Original problem solved: Maximum value in [4, 2, 9, 7, 5] is 9!"
            ),
            steps.map { it.explanation }
        )
        assertEquals(listOf(DivideConquerPhase.DIVIDE, DivideConquerPhase.CONQUER, DivideConquerPhase.COMBINE), steps.map { it.phase }.distinct())
        assertEquals(9, steps.last().combinedValue)
        assertTrue(steps.last().isTerminal)
    }

    @Test
    fun baseCasesAreUnsolvedDuringDivideAndSolvedTogetherAtConquer() {
        val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder()
        val divideSteps = steps.filter { it.phase == DivideConquerPhase.DIVIDE }
        val conquerStep = steps.single { it.phase == DivideConquerPhase.CONQUER }

        divideSteps.forEach { step ->
            step.nodes.filter { it.isBaseCase }.forEach { node ->
                assertEquals(null, node.maxValue, "${node.id} must not expose a result during DIVIDE")
            }
        }
        val conqueredLeaves = conquerStep.nodes.filter { it.isBaseCase }
        assertEquals(steps.last().nodes.filter { it.isBaseCase }.map { it.id }.toSet(), conqueredLeaves.map { it.id }.toSet())
        conqueredLeaves.forEach { node ->
            assertEquals(node.elements.single(), node.maxValue)
        }
        assertTrue(conquerStep.nodes.filterNot { it.isBaseCase }.all { it.maxValue == null })
    }

    @Test
    fun combineRevealsInternalResultsInPostorderWithoutFutureValues() {
        val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder()
        val combineSteps = steps.filter { it.phase == DivideConquerPhase.COMBINE }
        assertEquals(listOf("LL", "L1", "R1", "root"), combineSteps.mapNotNull { it.activeSubproblemId })

        val revealedInternalIds = mutableSetOf<String>()
        combineSteps.forEach { step ->
            val activeId = assertNotNull(step.activeSubproblemId)
            val active = step.nodes.single { it.id == activeId }
            assertEquals(maxOf(step.leftValue!!, step.rightValue!!), active.maxValue)
            revealedInternalIds += activeId
            step.nodes.filter { !it.isBaseCase }.forEach { node ->
                assertEquals(node.id in revealedInternalIds, node.maxValue != null, "Unexpected result visibility for ${node.id}")
            }
        }
    }

    @Test
    fun defaultTopologyIsCorrectAndSnapshotsDoNotLeakFutureNodes() {
        val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder()
        assertEquals(
            listOf(
                listOf("root"),
                listOf("root", "L1", "R1"),
                listOf("root", "L1", "R1", "LL", "LR"),
                listOf("root", "L1", "R1", "LL", "LR", "LLL", "LLR"),
                listOf("root", "L1", "R1", "LL", "LR", "LLL", "LLR", "RL", "RR")
            ),
            steps.take(5).map { it.nodes.map(SubproblemNode::id) }
        )
        assertEquals(null, steps[3].nodes.single { it.id == "R1" }.leftChildId)
        assertTrue(steps[3].nodes.none { it.id == "RL" || it.id == "RR" })

        val nodes = steps.last().nodes.associateBy { it.id }
        assertEquals("L1" to "R1", nodes.getValue("root").leftChildId to nodes.getValue("root").rightChildId)
        assertEquals("LL" to "LR", nodes.getValue("L1").leftChildId to nodes.getValue("L1").rightChildId)
        assertEquals("LLL" to "LLR", nodes.getValue("LL").leftChildId to nodes.getValue("LL").rightChildId)
        assertEquals("RL" to "RR", nodes.getValue("R1").leftChildId to nodes.getValue("R1").rightChildId)
        assertEquals(mapOf("root" to 9, "L1" to 9, "R1" to 7, "LL" to 4, "LR" to 9, "RL" to 7, "RR" to 5, "LLL" to 4, "LLR" to 2), nodes.mapValues { it.value.maxValue })

        steps.forEach { step ->
            val ids = step.nodes.map { it.id }.toSet()
            step.nodes.forEach { node ->
                assertTrue(node.leftChildId == null || node.leftChildId in ids)
                assertTrue(node.rightChildId == null || node.rightChildId in ids)
                assertEquals(node.high - node.low + 1, node.elements.size)
            }
        }
    }

    @Test
    fun arbitraryNonEmptyInputsSupportSingleEvenNegativeAndDuplicateValues() {
        val inputs = listOf(
            listOf(42),
            listOf(7, -1, 3, 3),
            listOf(-9, -2, -2, -4, -12),
            listOf(5, 5, 5, 5, 5, 5),
            listOf(8, -3, 14, 0, 2, 11, -1, 4)
        )
        inputs.forEach { input ->
            val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder(input)
            val result = steps.last().nodes.single { it.id == "root" }.maxValue
            assertEquals(input.maxOrNull(), result)
            assertTrue(steps.last().isTerminal)
            steps.forEach { step ->
                step.nodes.forEach { node ->
                    assertEquals(input.subList(node.low, node.high + 1), node.elements)
                    assertEquals(node.elements.size == 1, node.isBaseCase)
                }
            }
        }

        val singleton = DivideConquerFundamentalsTrace.stepsForMaxFinder(listOf(-42))
        assertEquals(2, singleton.size)
        assertEquals(DivideConquerPhase.CONQUER, singleton.last().phase)
        assertEquals(null, singleton.first().nodes.single().maxValue)
        assertEquals(null, singleton.last().activeSubproblemId)
        assertEquals(-42, singleton.last().nodes.single().maxValue)
        assertEquals(-42, singleton.last().nodes.single().elements.single())
    }

    @Test
    fun emptyInputIsRejectedExplicitly() {
        assertFailsWith<IllegalArgumentException> {
            DivideConquerFundamentalsTrace.stepsForMaxFinder(emptyList())
        }
    }
}
