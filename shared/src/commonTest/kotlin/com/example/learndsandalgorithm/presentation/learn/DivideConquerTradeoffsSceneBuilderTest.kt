package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DivideConquerTradeoffsSceneBuilderTest {

    @Test
    fun splitScenarioLocksOriginalEducationalSnapshotsAndExplanations() {
        val expectedExplanations = listOf(
            "Partition Split Comparison: Analyzing how partition balance affects recursion tree depth and overall algorithm performance for N = 10.",
            "BALANCED SPLIT: Splitting N=10 into Left=5 and Right=5. Binary tree depth is O(log N) = log2(10) ≈ 4 levels. Total Work: O(N log N).",
            "UNBALANCED SPLIT: Splitting N=10 into Left=1 and Right=9. Recursion tree degenerates into a linear chain of N=10 levels! Total Work: O(N²).",
            "Partition Balance Conclusion: Balanced halves yield O(N log N) time; worst-case unbalanced pivots (e.g. sorted input with last-element pivot) degrade Quick Sort to O(N²)."
        )
        val expectedBadges = listOf(
            "READY · INPUT N = 10",
            "BALANCED: 5 & 5 (DEPTH log N)",
            "UNBALANCED: 1 & 9 (DEPTH N)",
            "CONCLUSION: BALANCE KEEPS O(N log N)"
        )
        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        )

        assertEquals(4, frames.size)
        assertEquals(expectedExplanations, frames.map { it.meta.source.explanation })
        assertEquals(expectedExplanations, frames.map { it.scene.globalMessage })
        assertEquals(expectedBadges, frames.map { it.meta.source.badgeText })
        assertEquals(listOf(false, false, false, true), frames.map { it.meta.source.isTerminal })
    }

    @Test
    fun splitScenarioLocksBothCompleteTopologiesAndEverySnapshotState() {
        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        )
        val expectedBalanced = expectedTopology(10, balanced = true)
        val expectedUnbalanced = expectedTopology(10, balanced = false)

        assertEquals(19, expectedBalanced.size)
        assertEquals(19, expectedUnbalanced.size)
        frames.forEachIndexed { snapshot, frame ->
            listOf(
                DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID to (frame.meta.balancedNodes to expectedBalanced),
                DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID to (frame.meta.unbalancedNodes to expectedUnbalanced)
            ).forEach { (sceneId, pair) ->
                val (actualNodes, expectedNodes) = pair
                assertEquals(expectedNodes.map { it.id }.toSet(), actualNodes.map { it.id }.toSet())
                assertEquals(19, actualNodes.size)
                actualNodes.forEach { actual ->
                    val expected = expectedNodes.single { it.id == actual.id }
                    assertEquals(expected.size, actual.size)
                    assertEquals(expected.low, actual.low)
                    assertEquals(expected.highExclusive, actual.highExclusive)
                    assertEquals(expected.parentId, actual.parentId)
                    assertEquals(expected.branch, actual.branch)
                    assertEquals(expected.depth, actual.depth)
                }

                val scene = frame.scene.subScenes.single { it.subPaneId == sceneId }
                val expectedConnectionKeys = expectedNodes.filter { it.parentId != null }.map { "${it.id}:branch" }.toSet()
                assertEquals(18, expectedConnectionKeys.size)
                assertEquals(expectedConnectionKeys, scene.referenceConnections.map { it.connectionKey }.toSet())

                val expectedVisible = when {
                    sceneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID && snapshot == 0 -> setOf("balanced:0:10")
                    sceneId == DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID && snapshot < 2 -> setOf("unbalanced:0:10")
                    else -> expectedNodes.map { it.id }.toSet()
                }
                assertEquals(expectedVisible, actualNodes.filter { it.visible }.map { it.id }.toSet())
                assertEquals(expectedVisible, scene.entities.map { it.elementId }.toSet())

                actualNodes.forEach { node ->
                    val expectedStatus = when {
                        !node.visible -> TradeoffNodeStatus.PENDING
                        sceneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID && snapshot == 1 && node.id == "balanced:0:10" -> TradeoffNodeStatus.ACTIVE
                        sceneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID && snapshot >= 2 -> TradeoffNodeStatus.COMPLETED
                        sceneId == DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID && snapshot == 2 && node.id == "unbalanced:0:10" -> TradeoffNodeStatus.ACTIVE
                        sceneId == DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID && snapshot == 3 -> TradeoffNodeStatus.COMPLETED
                        else -> TradeoffNodeStatus.PENDING
                    }
                    assertEquals(expectedStatus, node.status, "Snapshot $snapshot, ${node.id}")
                }

                scene.connections.forEach { connection ->
                    val parentId = connection.fromVisualKey.substringAfterLast(":node:")
                    val parentStatus = actualNodes.single { it.id == parentId }.status
                    val expectedState = when (parentStatus) {
                        TradeoffNodeStatus.ACTIVE -> ConnectionVisualState.HIGHLIGHTED
                        TradeoffNodeStatus.COMPLETED -> ConnectionVisualState.PATH
                        TradeoffNodeStatus.PENDING -> ConnectionVisualState.DEFAULT
                    }
                    assertEquals(expectedState, connection.visualState)
                }
            }
        }
    }

    @Test
    fun splitScenarioPreservesAllFourTraceSnapshotsAndRevealsBothTopologiesInOrder() {
        val steps = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        val frames = DivideConquerTradeoffsSceneBuilder.build(steps)

        assertEquals(4, frames.size)
        frames.forEachIndexed { index, frame ->
            assertEquals(index, frame.scene.stepIndex)
            assertEquals("divide_conquer_tradeoffs_split_$index", frame.scene.sceneId)
            assertEquals(steps[index].explanation, frame.scene.globalMessage)
            assertEquals(steps[index], frame.meta.source)
            assertEquals(2, frame.scene.subScenes.size)
        }
        assertEquals(setOf("balanced:0:10"), frames.first().meta.balancedNodes.filter { it.visible }.map { it.id }.toSet())
        assertEquals(setOf("unbalanced:0:10"), frames.first().meta.unbalancedNodes.filter { it.visible }.map { it.id }.toSet())
        assertEquals(19, frames[1].meta.balancedNodes.size)
        assertEquals(19, frames[2].meta.unbalancedNodes.size)
        assertEquals(4, frames.last().meta.balancedNodes.maxOf { it.depth })
        assertEquals(9, frames.last().meta.unbalancedNodes.maxOf { it.depth })
        assertEquals(steps.last().badTreeDepth, 10)
        assertTrue(frames[1].meta.balancedNodes.single { it.id == "balanced:0:10" }.status == TradeoffNodeStatus.ACTIVE)
        assertTrue(frames[2].meta.unbalancedNodes.single { it.id == "unbalanced:0:10" }.status == TradeoffNodeStatus.ACTIVE)
        assertTrue(frames.last().meta.balancedNodes.all { it.status == TradeoffNodeStatus.COMPLETED })
        assertTrue(frames.last().meta.unbalancedNodes.all { it.status == TradeoffNodeStatus.COMPLETED })
        assertTrue(frames[1].scene.subScenes.single { it.subPaneId == DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID }
            .connections.any { it.visualState == ConnectionVisualState.HIGHLIGHTED })
        assertTrue(frames.last().scene.subScenes.all { scene ->
            scene.connections.all { it.visualState == ConnectionVisualState.PATH }
        })
    }

    @Test
    fun splitNodesPreserveSubproblemRangesBranchesAndStableIdentityAcrossSnapshots() {
        val frames = DivideConquerTradeoffsSceneBuilder.build(
            DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        )
        val balanced = frames.last().meta.balancedNodes
        val unbalanced = frames.last().meta.unbalancedNodes

        assertEquals("[0,10)", balanced.single { it.parentId == null }.rangeLabel)
        assertEquals(setOf("[0,5)", "[5,10)"), balanced.filter { it.depth == 1 }.map { it.rangeLabel }.toSet())
        assertEquals(setOf('L', 'R'), balanced.filter { it.depth == 1 }.mapNotNull { it.branch }.toSet())
        assertEquals(setOf("[0,1)", "[1,10)"), unbalanced.filter { it.depth == 1 }.map { it.rangeLabel }.toSet())
        assertEquals(4, balanced.maxOf { it.depth })
        assertEquals(10, balanced.count { it.size == 1 })
        assertEquals(10, unbalanced.count { it.size == 1 })

        listOf(
            DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID,
            DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID
        ).forEach { sceneId ->
            val identitySets = frames.map { frame ->
                frame.scene.subScenes.single { it.subPaneId == sceneId }.referenceConnections
                    .flatMap { listOf(it.fromVisualKey, it.toVisualKey) }.toSet()
            }
            assertTrue(identitySets.all { it == identitySets.first() })
            frames.forEach { frame ->
                val subScene = frame.scene.subScenes.single { it.subPaneId == sceneId }
                val referenceKeys = subScene.referenceConnections.map { it.connectionKey }.toSet()
                assertTrue(subScene.connections.all { it.connectionKey in referenceKeys })
                subScene.connections.forEach { connection ->
                    assertTrue(subScene.entities.any { it.visualKey == connection.fromVisualKey })
                    assertTrue(subScene.entities.any { it.visualKey == connection.toVisualKey })
                }
            }
        }
    }

    @Test
    fun comparisonScenarioRetainsStructuredFactsAndShowsEqualKeyReordering() {
        val steps = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.ALGORITHM_COMPARISON)
        val frames = DivideConquerTradeoffsSceneBuilder.build(steps)

        assertEquals(4, frames.size)
        frames.forEachIndexed { index, frame ->
            assertEquals(steps[index], frame.meta.source)
            assertEquals(steps[index].explanation, frame.scene.globalMessage)
            assertEquals(5, frame.meta.comparisonRows.size)
        }
        val terminal = frames.last().meta
        val byId = terminal.comparisonRows.associateBy { it.id }
        assertTrue(byId.getValue("typical_time").mergeSort.contains("O(N log N)"))
        assertTrue(byId.getValue("typical_time").quickSort.contains("average"))
        assertTrue(byId.getValue("worst_time").quickSort.contains("O(N²)"))
        assertTrue(byId.getValue("auxiliary_memory").mergeSort.contains("O(N)"))
        assertTrue(byId.getValue("auxiliary_memory").quickSort.contains("O(N) worst case"))
        assertTrue(byId.getValue("practical_tradeoff").quickSort.contains("not always faster"))

        assertEquals(listOf(0, 1, 3, 5, 5), terminal.mergeSortedRecords.map { it.key })
        assertEquals(listOf("record:A", "record:B"), terminal.mergeSortedRecords.filter { it.key == 5 }.map { it.identity })
        assertEquals(listOf("record:B", "record:A"), terminal.quickPartitionRecords.filter { it.key == 5 }.map { it.identity })
        assertEquals(listOf(1, 0, 3, 5, 5), terminal.quickPartitionRecords.map { it.key })
        assertFalse(terminal.quickPartitionRecords.zipWithNext().all { (a, b) -> a.key <= b.key })
        assertEquals(terminal.mergeSortedRecords, terminal.mergeBufferRecords)

        val comparisonScene = frames.first().scene.subScenes.single { it.subPaneId == DivideConquerTradeoffsSceneBuilder.COMPARISON_TABLE_SCENE_ID }
        assertEquals(5, comparisonScene.entities.size)
        assertEquals((1..3).map { "record:${('A' + it - 1)}" }.toSet(), terminal.inputRecords.take(3).map { it.identity }.toSet())
        assertEquals(null, frames.first().meta.activeComparisonSection)
        assertEquals("merge", frames[1].meta.activeComparisonSection)
        assertEquals("quick", frames[2].meta.activeComparisonSection)
        assertEquals("decision", frames[3].meta.activeComparisonSection)
        val mergeStates = frames[1].scene.subScenes.single { it.subPaneId == DivideConquerTradeoffsSceneBuilder.COMPARISON_TABLE_SCENE_ID }
            .entities.associate { it.elementId to it.visualState }
        assertEquals(EntityVisualState.ACTIVE, mergeStates.getValue("comparison:typical_time"))
        assertEquals(EntityVisualState.DEFAULT, mergeStates.getValue("comparison:practical_tradeoff"))
        val decisionStates = frames[3].scene.subScenes.single { it.subPaneId == DivideConquerTradeoffsSceneBuilder.COMPARISON_TABLE_SCENE_ID }
            .entities.associate { it.elementId to it.visualState }
        assertEquals(EntityVisualState.DEFAULT, decisionStates.getValue("comparison:worst_time"))
        assertEquals(EntityVisualState.ACTIVE, decisionStates.getValue("comparison:practical_tradeoff"))
        val buffer = frames[3].scene.subScenes.single { it.subPaneId == "${DivideConquerTradeoffsSceneBuilder.BUFFER_SCENE_ID}:buffer" }
        assertEquals(terminal.mergeSortedRecords.map { it.label }, buffer.entities.map { it.dataValue })
    }

    @Test
    fun eachScenarioUsesDistinctStableSceneAndElementKeys() {
        listOf(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT, TradeoffScenario.ALGORITHM_COMPARISON).forEach { scenario ->
            val frames = DivideConquerTradeoffsSceneBuilder.build(DivideConquerTradeoffsTrace.stepsForScenario(scenario))
            val paneIds = frames.first().scene.subScenes.map { it.subPaneId }
            assertEquals(paneIds.size, paneIds.toSet().size)
            val keysByPane = frames.map { frame ->
                frame.scene.subScenes.map { pane -> pane.subPaneId to pane.entities.map { it.visualKey }.toSet() }.toMap()
            }
            assertTrue(keysByPane.all { it.keys == keysByPane.first().keys })
            keysByPane.first().keys.forEach { paneId ->
                val union = keysByPane.map { it.getValue(paneId) }.reduce(Set<String>::union)
                assertTrue(union.isNotEmpty())
            }
        }
    }

    private data class ExpectedNode(
        val id: String,
        val low: Int,
        val highExclusive: Int,
        val parentId: String?,
        val branch: Char?,
        val depth: Int
    ) {
        val size: Int get() = highExclusive - low
    }

    /** Independent test oracle for the documented N/2+N/2 and 1+(N-1) split rules. */
    private fun expectedTopology(n: Int, balanced: Boolean): List<ExpectedNode> {
        val prefix = if (balanced) "balanced" else "unbalanced"
        val expected = mutableListOf<ExpectedNode>()
        fun visit(low: Int, high: Int, parentId: String?, branch: Char?, depth: Int) {
            val id = "$prefix:$low:$high"
            expected += ExpectedNode(id, low, high, parentId, branch, depth)
            if (high - low <= 1) return
            val leftSize = if (balanced) (high - low) / 2 else 1
            val split = low + leftSize
            visit(low, split, id, 'L', depth + 1)
            visit(split, high, id, 'R', depth + 1)
        }
        visit(0, n, null, null, 0)
        return expected
    }
}
