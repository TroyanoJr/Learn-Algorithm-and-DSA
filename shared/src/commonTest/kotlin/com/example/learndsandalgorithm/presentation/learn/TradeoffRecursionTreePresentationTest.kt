package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TradeoffRecursionTreePresentationTest {

    @Test
    fun bothTreeSelectionsPreserveCompleteOriginalTopologiesAndConnectionIdentities() {
        val frames = splitFrames()
        val terminal = frames.last()
        val balanced = tradeoffTreePresentationModel(terminal, TradeoffTreeSelection.BALANCED)
        val unbalanced = tradeoffTreePresentationModel(terminal, TradeoffTreeSelection.UNBALANCED)

        assertEquals("BALANCED RECURSION · N/2 + N/2", balanced.title)
        assertEquals("UNBALANCED RECURSION · 1 + (N−1)", unbalanced.title)
        for ((selection, model, nodes) in listOf(
            Triple(TradeoffTreeSelection.BALANCED, balanced, terminal.meta.balancedNodes),
            Triple(TradeoffTreeSelection.UNBALANCED, unbalanced, terminal.meta.unbalancedNodes)
        )) {
            assertEquals(selection, model.selection)
            assertEquals(19, model.nodes.size)
            assertEquals(18, model.edges.size)
            assertEquals(nodes.map { it.id }.toSet(), model.nodes.map { it.id }.toSet())
            assertEquals(nodes.map { it.id }, model.nodes.map { it.id })
            val referenceKeys = terminal.scene.subScenes.single {
                it.subPaneId == if (selection == TradeoffTreeSelection.BALANCED)
                    DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID
                else DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID
            }.referenceConnections.map { it.connectionKey }
            assertEquals(referenceKeys.toSet(), model.edges.map { it.connectionKey }.toSet())
            val currentConnections = terminal.scene.subScenes.single {
                it.subPaneId == if (selection == TradeoffTreeSelection.BALANCED)
                    DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID
                else DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID
            }.connections.associateBy { it.connectionKey }
            model.edges.forEach { edge ->
                val parent = model.nodes.single { it.id == edge.parentId }
                val child = model.nodes.single { it.id == edge.childId }
                assertEquals(parent.id, child.parentId)
                assertEquals("${child.id}:branch", edge.connectionKey)
                assertEquals(parent.depth + 1, child.depth)
                assertEquals(parent.visible && child.visible, edge.visible)
                assertEquals(currentConnections[edge.connectionKey]?.visualState ?: ConnectionVisualState.DEFAULT, edge.visualState)
            }
            assertTrue(model.nodes.all { node ->
                node.size == node.highExclusive - node.low && node.rangeLabel == "[${node.low},${node.highExclusive})"
            })
            model.levels.forEach { level ->
                val sortedLevel = model.nodes.filter { it.depth == level.depth }.sortedBy { it.low }
                assertEquals(sortedLevel.map { it.id }, level.rangeSegments.map { it.nodeId })
                level.rangeSegments.forEachIndexed { index, segment ->
                    val node = sortedLevel[index]
                    assertEquals(node.low.toFloat() / model.inputSize, segment.startFraction)
                    assertEquals(node.highExclusive.toFloat() / model.inputSize, segment.endFraction)
                    assertTrue(segment.startFraction >= 0f && segment.endFraction <= 1f)
                    assertTrue(segment.startFraction < segment.endFraction)
                    if (index > 0) assertTrue(level.rangeSegments[index - 1].endFraction <= segment.startFraction)
                }
            }
        }
        assertTrue(balanced.nodes.map { it.id }.toSet().intersect(unbalanced.nodes.map { it.id }.toSet()).isEmpty())
        assertTrue(balanced.nodes.all { it.status == TradeoffNodeStatus.COMPLETED })
        assertTrue(unbalanced.nodes.all { it.status == TradeoffNodeStatus.COMPLETED })
    }

    @Test
    fun snapshotProgressionTracksOriginalVisibilityActiveCallAndDepths() {
        val frames = splitFrames()
        val balancedInitial = tradeoffTreePresentationModel(frames[0], TradeoffTreeSelection.BALANCED)
        val balancedActive = tradeoffTreePresentationModel(frames[1], TradeoffTreeSelection.BALANCED)
        val unbalancedInitial = tradeoffTreePresentationModel(frames[1], TradeoffTreeSelection.UNBALANCED)
        val unbalancedActive = tradeoffTreePresentationModel(frames[2], TradeoffTreeSelection.UNBALANCED)

        assertEquals(listOf("balanced:0:10"), tradeoffTreeRevealOrder(balancedInitial.nodes).map { it.id })
        assertEquals(19, tradeoffTreeRevealOrder(balancedActive.nodes).size)
        assertEquals(listOf(0, 1, 2, 3, 4), balancedActive.levels.filter { it.visibleCount > 0 }.map { it.depth })
        assertEquals(listOf(1, 2, 4, 8, 4), balancedActive.levels.map { it.visibleCount })
        assertEquals(listOf(0f to 0.5f, 0.5f to 1f), balancedActive.levels[1].rangeSegments.map { it.startFraction to it.endFraction })
        assertEquals("balanced:0:10", balancedActive.focusNode?.id)
        assertEquals(listOf("balanced:0:5", "balanced:5:10"), balancedActive.focusChildren.map { it.id })
        assertEquals(TradeoffNodeStatus.ACTIVE, balancedActive.focusNode?.status)

        assertEquals(listOf("unbalanced:0:10"), tradeoffTreeRevealOrder(unbalancedInitial.nodes).map { it.id })
        assertEquals(19, tradeoffTreeRevealOrder(unbalancedActive.nodes).size)
        assertEquals((0..9).toList(), unbalancedActive.levels.filter { it.visibleCount > 0 }.map { it.depth })
        assertEquals("unbalanced:0:10", unbalancedActive.focusNode?.id)
        assertEquals(listOf("unbalanced:0:1", "unbalanced:1:10"), unbalancedActive.focusChildren.map { it.id })
        assertEquals(TradeoffNodeStatus.ACTIVE, unbalancedActive.focusNode?.status)
        assertTrue(unbalancedActive.levels.drop(1).all { it.visibleCount > 0 })
        assertEquals(listOf(0f to 0.1f, 0.1f to 1f), unbalancedActive.levels[1].rangeSegments.map { it.startFraction to it.endFraction })
        assertEquals(listOf(0.1f to 0.2f, 0.2f to 1f), unbalancedActive.levels[2].rangeSegments.map { it.startFraction to it.endFraction })

        frames.forEach { frame ->
            listOf(TradeoffTreeSelection.BALANCED, TradeoffTreeSelection.UNBALANCED).forEach { selection ->
                val model = tradeoffTreePresentationModel(frame, selection)
                val originalNodes = if (selection == TradeoffTreeSelection.BALANCED) frame.meta.balancedNodes else frame.meta.unbalancedNodes
                assertEquals(originalNodes, model.nodes, "snapshot ${frame.meta.scenarioStepIndex} / $selection")
                assertEquals(19, model.nodes.size)
                assertEquals(18, model.edges.size)
            }
        }
    }

    @Test
    fun presentationPhasesRevealEachTreeByDepthWithoutChangingStableIdentities() {
        val frames = splitFrames()
        val balancedPhases = tradeoffPlaybackPhases(frames, TradeoffTreeSelection.BALANCED)
        val unbalancedPhases = tradeoffPlaybackPhases(frames, TradeoffTreeSelection.UNBALANCED)
        val phases = balancedPhases + unbalancedPhases
        assertEquals(6, balancedPhases.size)
        assertEquals(11, unbalancedPhases.size)
        assertEquals(listOf(0, 1, 2, 3), phases.map { it.semanticStepIndex }.distinct().sorted())
        assertTrue(phases.all { it.selection == if (it.id.startsWith("balanced")) TradeoffTreeSelection.BALANCED else TradeoffTreeSelection.UNBALANCED })
        assertEquals(0, balancedPhases.first().revealDepth)
        assertEquals(0, unbalancedPhases.first().revealDepth)
        assertEquals(TradeoffPlaybackPhaseKind.COMPLETE, balancedPhases.last().kind)
        assertEquals(TradeoffPlaybackPhaseKind.COMPLETE, unbalancedPhases.last().kind)
        assertEquals(3000L, TRADEOFF_PLAYBACK_DELAY_MILLIS)
        assertEquals(650, TRADEOFF_NODE_TRANSITION_MILLIS)
        assertTrue(TRADEOFF_NODE_TRANSITION_MILLIS < TRADEOFF_PLAYBACK_DELAY_MILLIS)

        phases.forEach { phase ->
            val frame = frames[phase.semanticStepIndex]
            val model = tradeoffTreePresentationModel(
                frame,
                phase.selection,
                revealDepth = phase.revealDepth,
                forceCompleted = phase.kind == TradeoffPlaybackPhaseKind.COMPLETE
            )
            val source = if (phase.selection == TradeoffTreeSelection.BALANCED) {
                frame.meta.balancedNodes
            } else {
                frame.meta.unbalancedNodes
            }
            assertEquals(source.map { it.id }, model.nodes.map { it.id })
            assertEquals(source.map { it.parentId }, model.nodes.map { it.parentId })
            assertEquals(source.map { it.rangeLabel }, model.nodes.map { it.rangeLabel })
            assertEquals(19, model.nodes.size)
            assertEquals(18, model.edges.size)
            assertTrue(model.nodes.filter { it.depth <= phase.revealDepth }.all { it.visible })
            assertTrue(model.nodes.filter { it.depth > phase.revealDepth }.none { it.visible })
            assertTrue(model.edges.filter { edge ->
                model.nodes.single { it.id == edge.childId }.depth <= phase.revealDepth
            }.all { it.visible })
            assertTrue(model.edges.filter { edge ->
                model.nodes.single { it.id == edge.childId }.depth > phase.revealDepth
            }.none { it.visible })
        }

        assertEquals(listOf(0, 1, 2, 3, 4, 4), balancedPhases.map { it.revealDepth })
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9), unbalancedPhases.map { it.revealDepth })
    }

    @Test
    fun nodesAndConnectionsWithinEachRevealedDepthAreStaggered() {
        val model = tradeoffTreePresentationModel(splitFrames().last(), TradeoffTreeSelection.BALANCED)
        val nodes = model.nodes.filter { it.depth == 3 }.sortedBy { it.low }
        val nodeDelays = nodes.map { tradeoffNodeRevealDelayMillis(model, it) }
        assertEquals(nodes.indices.map { it * TRADEOFF_REVEAL_STAGGER_MILLIS }, nodeDelays)
        val edges = model.edges.filter { edge -> model.nodes.single { it.id == edge.childId }.depth == 3 }
            .sortedBy { edge -> model.nodes.single { it.id == edge.childId }.low }
        val edgeDelays = edges.map { tradeoffConnectionRevealDelayMillis(model, it) }
        assertEquals(edges.indices.map { it * TRADEOFF_REVEAL_STAGGER_MILLIS }, edgeDelays)
        assertTrue(nodeDelays.last() + TRADEOFF_NODE_TRANSITION_MILLIS < TRADEOFF_PLAYBACK_DELAY_MILLIS)
        assertTrue(edgeDelays.last() + TRADEOFF_NODE_TRANSITION_MILLIS < TRADEOFF_PLAYBACK_DELAY_MILLIS)
    }

    @Test
    fun selectedPhaseDrivesExplanationAndOperationAndReservesConclusionsForCompletion() {
        val frames = splitFrames()
        val balanced = tradeoffPlaybackPhases(frames, TradeoffTreeSelection.BALANCED)
        val unbalanced = tradeoffPlaybackPhases(frames, TradeoffTreeSelection.UNBALANCED)
        assertTrue(balanced[0].explanation.contains("N = 10"))
        assertTrue(balanced[1].explanation.contains("5 [0,5)"))
        assertTrue(balanced[1].explanation.contains("5 [5,10)"))
        assertTrue(balanced[2].explanation.contains("2 [0,2)"))
        assertTrue(balanced[3].explanation.contains("2 [3,5)"))
        assertTrue(balanced[4].explanation.contains("size-one calls"))
        assertFalse(balanced.dropLast(1).any { it.explanation.contains("Partition Balance Conclusion") })
        assertEquals(partitionSplitVisualizerSteps().last().explanation, balanced.last().explanation)

        for (depth in 1..9) {
            val phase = unbalanced[depth]
            val size = 11 - depth
            val low = depth - 1
            assertTrue(phase.explanation.contains("$size [$low,10) → 1 [$low,${low + 1}) + ${size - 1} [${low + 1},10)"))
            val splitDescription = phase.explanation.substringAfter(": ").substringBefore(". ")
            assertTrue(phase.operation.contains(splitDescription))
        }
        assertTrue(unbalanced[10].explanation.contains("Partition Balance Conclusion"))
        assertFalse(unbalanced.dropLast(1).any { it.explanation.contains("Partition Balance Conclusion") })

        val playback = VisualizerPlaybackState(initialStep = 0, stepCount = balanced.size)
        assertEquals(balanced[0].explanation, balanced[playback.safeStepIndex].explanation)
        playback.next()
        assertEquals(balanced[1].explanation, balanced[playback.safeStepIndex].explanation)
        assertTrue(tradeoffOperationText(balanced[playback.safeStepIndex], partitionSplitVisualizerSteps()[1])
            .contains(balanced[playback.safeStepIndex].operation))
        playback.previous()
        assertEquals(balanced[0].explanation, balanced[playback.safeStepIndex].explanation)
        playback.reset()
        assertEquals(balanced[0].explanation, balanced[playback.safeStepIndex].explanation)
    }

    @Test
    fun balancedAndUnbalancedPlaybackProgressResetAndSelectionAreIndependent() {
        val frames = splitFrames()
        val balancedPhases = tradeoffPlaybackPhases(frames, TradeoffTreeSelection.BALANCED)
        val unbalancedPhases = tradeoffPlaybackPhases(frames, TradeoffTreeSelection.UNBALANCED)
        val balanced = VisualizerPlaybackState(0, balancedPhases.size)
        val unbalanced = VisualizerPlaybackState(0, unbalancedPhases.size)

        balanced.next()
        balanced.next()
        assertEquals(2, balanced.safeStepIndex)
        assertEquals(0, unbalanced.safeStepIndex)
        balanced.isPlaying = true
        val selected = selectTradeoffTree(
            current = TradeoffTreeSelection.BALANCED,
            next = TradeoffTreeSelection.UNBALANCED,
            balancedPlayback = balanced,
            unbalancedPlayback = unbalanced
        )
        assertEquals(TradeoffTreeSelection.UNBALANCED, selected)
        assertFalse(balanced.isPlaying)
        assertEquals(2, balanced.safeStepIndex)

        unbalanced.next()
        unbalanced.next()
        unbalanced.next()
        assertEquals(3, unbalanced.safeStepIndex)
        assertEquals(2, balanced.safeStepIndex)
        unbalanced.reset()
        assertEquals(0, unbalanced.safeStepIndex)
        assertEquals(2, balanced.safeStepIndex)
    }

    @Test
    fun baseCasesAreCompleteAndDepthsRemainLogarithmicVersusLinear() {
        val terminal = splitFrames().last()
        val balanced = tradeoffTreePresentationModel(terminal, TradeoffTreeSelection.BALANCED)
        val unbalanced = tradeoffTreePresentationModel(terminal, TradeoffTreeSelection.UNBALANCED)
        listOf(balanced, unbalanced).forEach { model ->
            val baseCases = model.nodes.filter { it.size == 1 }
            assertEquals(10, baseCases.size)
            assertEquals((0..9).toList(), baseCases.sortedBy { it.low }.map { it.low })
            assertTrue(baseCases.all { it.highExclusive == it.low + 1 })
        }
        assertEquals(4, balanced.nodes.maxOf { it.depth })
        assertEquals(9, unbalanced.nodes.maxOf { it.depth })
    }

    @Test
    fun explanationsAndComplexityAnnotationsRemainComplete() {
        val split = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        assertEquals(4, split.size)
        assertEquals(
            listOf(
                "Partition Split Comparison: Analyzing how partition balance affects recursion tree depth and overall algorithm performance for N = 10.",
                "BALANCED SPLIT: Splitting N=10 into Left=5 and Right=5. Binary tree depth is O(log N) = log2(10) ≈ 4 levels. Total Work: O(N log N).",
                "UNBALANCED SPLIT: Splitting N=10 into Left=1 and Right=9. Recursion tree degenerates into a linear chain of N=10 levels! Total Work: O(N²).",
                "Partition Balance Conclusion: Balanced halves yield O(N log N) time; worst-case unbalanced pivots (e.g. sorted input with last-element pivot) degrade Quick Sort to O(N²)."
            ), split.map { it.explanation }
        )
        val frame = splitFrames().last()
        assertEquals(
            TradeoffTreeAnnotations(
                "DEPTH O(log N) · 4 edges / 5 node levels",
                "WORK / LEVEL O(N) · TOTAL O(N log N)",
                "Near-equal splits keep recursion shallow"
            ), tradeoffTreePresentationModel(frame, TradeoffTreeSelection.BALANCED).annotations
        )
        assertEquals(
            TradeoffTreeAnnotations(
                "DEPTH O(N) · 10 calls / 10 node levels",
                "N + … + 1 = 55 · TOTAL O(N²)",
                "Sorted input + last pivot → 1 + (N−1)"
            ), tradeoffTreePresentationModel(frame, TradeoffTreeSelection.UNBALANCED).annotations
        )
        assertEquals("BALANCED_VS_UNBALANCED_SPLIT", frame.meta.source.scenario.name)
        assertTrue(frame.meta.source.isTerminal)
    }

    @Test
    fun comparisonExplanationsAndStaticEducationalMaterialRemainOutsidePartitionTrace() {
        val comparisonSteps = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.ALGORITHM_COMPARISON)
        assertEquals(
            listOf(
                "Algorithm Trade-offs: Comparing Merge Sort vs Quick Sort across Time Complexity, Memory, and Stability.",
                "1. MERGE SORT: Guarantees O(N log N) time in ALL cases (best, average, worst). Requires O(N) auxiliary space for merging. STABLE sorting.",
                "2. QUICK SORT: Runs in O(N log N) average time, but degrades to O(N²) worst-case with poor pivots. Sorts IN-PLACE (O(log N) stack space). UNSTABLE sorting.",
                "PRACTICAL DECISION RULE: Use Merge Sort when guaranteed O(N log N) time or stability is mandatory. Use Quick Sort when in-place memory efficiency and practical cache performance are paramount."
            ),
            comparisonSteps.map { it.explanation }
        )
        assertEquals(
            listOf("Typical time", "Worst-case time", "Auxiliary memory", "Stability", "Practical tradeoff"),
            DivideConquerTradeoffsSceneBuilder.comparisonRows.map { it.property }
        )
        assertEquals(listOf("5A", "5B", "1C", "0D", "3E"), DivideConquerTradeoffsSceneBuilder.inputRecords.map { it.label })
        assertEquals(
            listOf("0D", "1C", "3E", "5A", "5B"),
            DivideConquerTradeoffsSceneBuilder.mergeSortedRecords.map { it.label }
        )
        assertEquals(
            listOf("1C", "0D", "3E", "5B", "5A"),
            DivideConquerTradeoffsSceneBuilder.quickPartitionRecords.map { it.label }
        )
    }

    @Test
    fun treeUsesTheFullAvailableLandscapeWidth() {
        listOf(240f, 320f, 420f, 724f).forEach { available ->
            val width = TradeoffRecursionTreeLayout.widthFor(available)
            assertTrue(width <= available)
            assertTrue(width >= 0f)
        }
        assertEquals(240f, TradeoffRecursionTreeLayout.widthFor(240f))
        assertEquals(724f, TradeoffRecursionTreeLayout.widthFor(724f))

        val frame = splitFrames().last()
        listOf(TradeoffTreeSelection.BALANCED, TradeoffTreeSelection.UNBALANCED).forEach { selection ->
            val model = tradeoffTreePresentationModel(frame, selection)
            val focusAndChildren = listOfNotNull(model.focusNode) + model.focusChildren
            assertTrue(focusAndChildren.all { it.visible })
            assertTrue(focusAndChildren.zipWithNext().all { (first, second) -> first.id != second.id })
            assertTrue(model.levels.zipWithNext().all { (first, second) -> first.depth < second.depth })
        }
    }

    @Test
    fun completePartitionDiagramIsContainedStableAndNonOverlappingAcrossSnapshots() {
        val frames = splitFrames()
        listOf(TradeoffTreeSelection.BALANCED, TradeoffTreeSelection.UNBALANCED).forEach { selection ->
            val layouts = frames.map { frame ->
                TradeoffRecursionTreeLayout.calculateDiagram(
                    tradeoffTreePresentationModel(frame, selection),
                    availableWidth = 420f,
                    availableHeight = 190f
                )
            }
            val layout = layouts.first()
            assertTrue(layouts.all { it == layout })
            assertTrue(layout.width <= 420f)
            assertEquals(190f, layout.height)
            assertEquals(19, layout.barBounds.size)
            assertEquals(18, layout.connectors.size)

            val model = tradeoffTreePresentationModel(frames.last(), selection)
            if (selection == TradeoffTreeSelection.BALANCED) {
                val root = model.nodes.single { it.parentId == null }
                assertEquals(layout.width / 2f, layout.barBounds.getValue(root.id).bounds.center.x)
            }
            layout.barBounds.values.forEach { bar ->
                assertContained(Rect2D(0f, 0f, layout.width, layout.height), bar.bounds)
            }
            val bars = model.nodes.map { layout.barBounds.getValue(it.id).bounds }
            bars.forEachIndexed { index, first ->
                bars.drop(index + 1).forEach { second ->
                    assertFalse(first.overlaps(second), "Tree nodes overlap: $first / $second")
                }
            }
            model.nodes.filter { it.size > 1 }.forEach { parent ->
                val children = model.nodes.filter { it.parentId == parent.id }.sortedBy { it.low }
                assertEquals(2, children.size)
                assertEquals(parent.low, children.first().low)
                assertEquals(parent.highExclusive, children.last().highExclusive)
                assertEquals(children.first().highExclusive, children.last().low)
                assertEquals(parent.size, children.sumOf { it.size })
            }
            layout.connectors.forEach { connector ->
                val parent = layout.barBounds.getValue(connector.parentId).bounds
                val child = layout.barBounds.getValue(connector.childId).bounds
                if (selection == TradeoffTreeSelection.BALANCED) {
                    assertEquals(Point2D(parent.center.x, parent.bottom), connector.line.start)
                    assertEquals(Point2D(child.center.x, child.top), connector.line.end)
                } else {
                    assertTrue(
                        connector.line.start == Point2D(parent.center.x, parent.bottom) ||
                            connector.line.start == Point2D(parent.right, parent.center.y)
                    )
                    assertTrue(connector.line.end == Point2D(child.center.x, child.top) ||
                        connector.line.end == Point2D(child.left, child.center.y))
                }
                assertFalse(layout.barBounds.any { (id, bar) ->
                    id != connector.parentId && id != connector.childId && connector.line.intersectsInterior(bar.bounds)
                }, "Connection ${connector.connectionKey} crosses an unrelated node")
            }
            layout.connectors.forEachIndexed { index, connector ->
                layout.connectors.drop(index + 1).forEach { other ->
                    val sharesNode = setOf(connector.parentId, connector.childId)
                        .intersect(setOf(other.parentId, other.childId)).isNotEmpty()
                    if (!sharesNode) assertFalse(connector.line.intersectsProperly(other.line))
                }
            }
        }
    }

    @Test
    fun bothCompleteTreesFitWithinShortLandscapeViewports() {
        val terminal = splitFrames().last()
        val viewports = listOf(240f to 130f, 420f to 160f, 560f to 180f, 680f to 210f)
        TradeoffTreeSelection.entries.forEach { selection ->
            val model = tradeoffTreePresentationModel(terminal, selection)
            viewports.forEach { (width, height) ->
                val layout = TradeoffRecursionTreeLayout.calculateDiagram(model, width, height)
                assertTrue(layout.width <= width)
                assertEquals(height, layout.height)
                assertEquals(19, layout.barBounds.size)
                assertEquals(18, layout.connectors.size)
                layout.barBounds.values.forEach { bar ->
                    assertContained(Rect2D(0f, 0f, layout.width, layout.height), bar.bounds)
                }
                val bars = model.nodes.map { layout.barBounds.getValue(it.id).bounds }
                bars.forEachIndexed { index, first ->
                    bars.drop(index + 1).forEach { second -> assertFalse(first.overlaps(second)) }
                }
            }
        }
    }

    @Test
    fun initialCompactLayoutsContainAllPendingNodesAndConnectionsWithoutVerticalOverflow() {
        val firstFrame = splitFrames().first()
        listOf(
            TradeoffTreeSelection.BALANCED to (420f to 100f),
            TradeoffTreeSelection.UNBALANCED to (420f to 76f)
        ).forEach { (selection, viewport) ->
            val model = tradeoffTreePresentationModel(firstFrame, selection, revealDepth = 0)
            val layout = TradeoffRecursionTreeLayout.calculateDiagram(model, viewport.first, viewport.second)
            assertEquals(viewport.first, layout.width)
            assertEquals(viewport.second, layout.height)
            assertEquals(19, layout.barBounds.size)
            assertEquals(18, layout.connectors.size)
            assertEquals(model.nodes.map { it.id }.toSet(), layout.barBounds.keys)
            assertEquals(model.edges.map { it.connectionKey }.toSet(), layout.connectors.map { it.connectionKey }.toSet())
            assertEquals(1, model.nodes.count { it.visible })
            layout.barBounds.values.forEach { bar ->
                assertContained(Rect2D(0f, 0f, layout.width, layout.height), bar.bounds)
            }
        }
    }

    @Test
    fun unbalancedPresentationIsAContinuingChainWithOneBaseCaseBranchPerSplit() {
        val frame = splitFrames().last()
        val model = tradeoffTreePresentationModel(frame, TradeoffTreeSelection.UNBALANCED)
        val layout = TradeoffRecursionTreeLayout.calculateDiagram(model, availableWidth = 420f, availableHeight = 76f)
        val chain = model.nodes.filter { it.highExclusive == model.inputSize }.sortedBy { it.depth }
        assertEquals(10, chain.size)
        assertEquals((0..9).toList(), chain.map { it.depth })
        assertTrue(chain.zipWithNext().all { (first, second) ->
            layout.barBounds.getValue(first.id).bounds.center.x < layout.barBounds.getValue(second.id).bounds.center.x
        })

        chain.dropLast(1).forEachIndexed { depth, parent ->
            val children = model.nodes.filter { it.parentId == parent.id }
            val continuing = children.single { it.highExclusive == model.inputSize }
            val baseCase = children.single { it.id != continuing.id }
            assertEquals(1, baseCase.size)
            assertEquals(parent.low, baseCase.low)
            assertEquals(parent.low + 1, baseCase.highExclusive)
            assertEquals(parent.low + 1, continuing.low)
            assertEquals(model.inputSize, continuing.highExclusive)

            val parentBounds = layout.barBounds.getValue(parent.id).bounds
            val continuingBounds = layout.barBounds.getValue(continuing.id).bounds
            val baseBounds = layout.barBounds.getValue(baseCase.id).bounds
            assertEquals(parentBounds.top, continuingBounds.top)
            assertEquals(parentBounds.center.x, baseBounds.center.x)
            assertTrue(parentBounds.right < continuingBounds.left)
            assertTrue(parentBounds.bottom < baseBounds.top)

            val baseEdge = layout.connectors.single { it.parentId == parent.id && it.childId == baseCase.id }
            val chainEdge = layout.connectors.single { it.parentId == parent.id && it.childId == continuing.id }
            assertEquals(Point2D(parentBounds.center.x, parentBounds.bottom), baseEdge.line.start)
            assertEquals(Point2D(baseBounds.center.x, baseBounds.top), baseEdge.line.end)
            assertEquals(Point2D(parentBounds.right, parentBounds.center.y), chainEdge.line.start)
            assertEquals(Point2D(continuingBounds.left, continuingBounds.center.y), chainEdge.line.end)
        }
        assertEquals(19, model.nodes.size)
        assertEquals(18, layout.connectors.size)
        assertEquals(10, model.nodes.count { it.size == 1 })
    }

    @Test
    fun detailsExposeCompleteCurrentOperationAndConclusionContent() {
        val phases = tradeoffPlaybackPhases(splitFrames(), TradeoffTreeSelection.UNBALANCED)
        val conclusion = partitionSplitVisualizerSteps().last()
        val operation = tradeoffOperationText(phases.last(), conclusion)
        assertTrue(operation.contains(phases.last().label))
        assertTrue(operation.contains(conclusion.badgeText))
        assertEquals("CONCLUSION: BALANCE KEEPS O(N log N)", conclusion.badgeText)
        assertEquals(listOf("ANALYSIS SCENARIO", conclusion.badgeText, "Divide & Conquer Complexity"), tradeoffMetadataLines(conclusion))
        assertTrue(conclusion.explanation.contains("worst-case unbalanced pivots"))
        assertTrue(conclusion.explanation.contains("O(N²)"))
    }

    @Test
    fun comparisonContentBelongsToLessonAndVisualizerPlaybackContainsOnlyPartitionSteps() {
        assertTrue(hasDivideConquerTradeoffsComparisonLessonContent("lesson_divide_conquer_6"))
        assertFalse(hasDivideConquerTradeoffsComparisonLessonContent("lesson_divide_conquer_5"))
        val visualizerSteps = partitionSplitVisualizerSteps()
        assertEquals(4, visualizerSteps.size)
        assertTrue(visualizerSteps.all { it.scenario == TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT })
        assertTrue(
            TradeoffTreeSelection.entries.flatMap { tradeoffPlaybackPhases(splitFrames(), it) }
                .all { it.semanticStepIndex in visualizerSteps.indices }
        )
    }

    @Test
    fun onlyPartitionScenarioDrivesPlaybackAndResetRestoresOriginalTrace() {
        val steps = DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
        assertEquals(4, steps.size)
        assertTrue(steps.all { it.scenario == TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT })
        assertEquals((0..3).toList(), splitFrames().map { it.meta.scenarioStepIndex })
        val playback = VisualizerPlaybackState(initialStep = 0, stepCount = 4, savedStepIndex = 2)
        playback.reset()
        assertEquals(0, playback.safeStepIndex)
        assertFalse(playback.isPlaying)
    }

    private fun splitFrames() = DivideConquerTradeoffsSceneBuilder.build(
        DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT)
    )

    private fun assertContained(container: Rect2D, child: Rect2D) {
        assertTrue(child.left >= container.left)
        assertTrue(child.top >= container.top)
        assertTrue(child.right <= container.right)
        assertTrue(child.bottom <= container.bottom)
    }

    private fun Rect2D.overlaps(other: Rect2D): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

    private fun LineSegment2D.intersectsInterior(rect: Rect2D): Boolean {
        var low = 0f
        var high = 1f
        val dx = end.x - start.x
        val dy = end.y - start.y
        fun clip(p: Float, q: Float): Boolean {
            if (p == 0f) return q >= 0f
            val ratio = q / p
            if (p < 0f) {
                if (ratio > high) return false
                if (ratio > low) low = ratio
            } else {
                if (ratio < low) return false
                if (ratio < high) high = ratio
            }
            return true
        }
        if (!clip(-dx, start.x - rect.left) || !clip(dx, rect.right - start.x) ||
            !clip(-dy, start.y - rect.top) || !clip(dy, rect.bottom - start.y) || low > high) return false
        val t = (low + high) / 2f
        val x = start.x + t * dx
        val y = start.y + t * dy
        return x > rect.left && x < rect.right && y > rect.top && y < rect.bottom
    }

    private fun LineSegment2D.intersectsProperly(other: LineSegment2D): Boolean {
        fun orientation(a: Point2D, b: Point2D, c: Point2D): Float =
            (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
        return orientation(start, end, other.start) * orientation(start, end, other.end) < 0f &&
            orientation(other.start, other.end, start) * orientation(other.start, other.end, end) < 0f
    }
}
