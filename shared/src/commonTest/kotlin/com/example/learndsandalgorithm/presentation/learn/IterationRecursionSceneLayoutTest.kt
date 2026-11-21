package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IterationRecursionSceneLayoutTest {
    @Test
    fun sceneConstructionIsDeterministicAndUsesStableEntityIds() {
        val builders = listOf(
            { IterationRecursionSceneBuilder.iteration(IterationFundamentalsTrace.createTrace()) },
            { IterationRecursionSceneBuilder.nested(NestedIterationTrace.createTrace()) },
            { IterationRecursionSceneBuilder.recursion(RecursionFundamentalsTrace.createTrace()) },
            { IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace()) }
        )
        builders.forEach { build ->
            val first = build()
            val second = build()
            assertEquals(first, second)
            assertEquals(first.indices.map { "${first.first().meta.lesson.name.lowercase()}_step_$it" }, first.map { it.scene.sceneId })
            first.forEach { frame ->
                val entities = frame.scene.subScenes.flatMap { it.entities }
                assertEquals(entities.size, entities.map { it.visualKey }.toSet().size)
                assertTrue(entities.all { it.visualKey.isNotBlank() && it.elementId.isNotBlank() && it.slotKey.isNotBlank() })
            }
            val identities = first.flatMap { it.scene.subScenes }.flatMap { it.entities }
                .groupBy { it.visualKey }
            identities.forEach { (visualKey, occurrences) ->
                assertEquals(1, occurrences.map { it.elementId }.toSet().size, "Element identity changed for $visualKey")
                assertEquals(1, occurrences.map { it.slotKey }.toSet().size, "Slot identity changed for $visualKey")
            }
        }
    }

    @Test
    fun sequenceWideLayoutsAreStableContainedAndHaveNonnegativeDimensions() {
        val sequences = allSequences()
        sequences.forEach { frames ->
            val layout = IterationRecursionSceneLayout.calculate(frames)
            assertEquals(layout, IterationRecursionSceneLayout.calculate(frames))
            assertTrue(layout.requiredSceneWidth > 0f && layout.requiredSceneHeight > 0f)
            layout.bounds.values.forEach { bounds ->
                assertTrue(bounds.width > 0f && bounds.height > 0f)
                assertTrue(bounds.left >= 0f && bounds.top >= 0f)
                assertTrue(bounds.right <= layout.requiredSceneWidth && bounds.bottom <= layout.requiredSceneHeight)
            }
            layout.connectors.forEach { connector ->
                assertTrue(connector.fromX in 0f..layout.requiredSceneWidth && connector.toX in 0f..layout.requiredSceneWidth)
                assertTrue(connector.fromY in 0f..layout.requiredSceneHeight && connector.toY in 0f..layout.requiredSceneHeight)
            }
            val scale = IterationRecursionSceneLayout.initialFitScale(layout, 360f, 190f)
            assertTrue(layout.requiredSceneWidth * scale <= 360.01f)
            assertTrue(layout.requiredSceneHeight * scale <= 190.01f)
        }
    }

    @Test
    fun iterationPhaseAndProgressGeometryMatchesTraceSemantics() {
        val frames = IterationRecursionSceneBuilder.iteration(IterationFundamentalsTrace.createTrace())
        val layout = IterationRecursionSceneLayout.calculate(frames)
        assertNoOverlap((0..2).map { layout.boundsFor("phase:$it") })
        assertNoOverlap((1..4).map { layout.boundsFor("iteration:pass:$it") })
        assertEquals(2, layout.connectors.size)
        assertEquals("CHECK_CONDITION", frames[1].meta.action)
        assertEquals(EntityVisualState.ACTIVE, frames[1].scene.subScenes.first().entities.first().visualState)
        assertEquals(EntityVisualState.VISITED, frames[3].scene.subScenes.first().entities.first().visualState)
        assertTrue(frames.first().scene.subScenes.first().entities.all { it.visualState == EntityVisualState.DEFAULT })
    }

    @Test
    fun nestedMatrixPositionsEveryCoordinateAndDerivesResetFromOuterPass() {
        val frames = IterationRecursionSceneBuilder.nested(NestedIterationTrace.createTrace())
        val layout = IterationRecursionSceneLayout.calculate(frames)
        val cells = (1..2).flatMap { row -> (1..3).map { column -> layout.boundsFor("nested:cell:$row:$column") } }
        assertNoOverlap(cells)
        assertTrue(cells.take(3).all { it.top == cells.first().top })
        assertTrue(cells.drop(3).all { it.top > cells.first().top })
        val secondOuter = frames.first { it.meta.action == "OUTER_PASS" && it.meta.i == 2 }
        assertEquals(1, secondOuter.meta.j)
        assertEquals(listOf(1 to 1, 1 to 2, 1 to 3), secondOuter.meta.printedPairs)
        assertFalse(frames.any { it.meta.action == "INNER_RESET" })
    }

    @Test
    fun recursionCoversEveryFrameReturnAndReturningStateWithoutPrematureValues() {
        val frames = IterationRecursionSceneBuilder.recursion(RecursionFundamentalsTrace.createTrace())
        val layout = IterationRecursionSceneLayout.calculate(frames)
        (0..3).forEach { n ->
            assertNotNull(layout.bounds["recursion:frame:$n"])
            assertNotNull(layout.bounds["recursion:return:$n"])
        }
        assertEquals(3, layout.connectors.size)
        val base = frames.first { it.meta.action == "BASE_CASE_REACHED" }
        assertEquals(setOf(0, 1, 2, 3), base.scene.subScenes.first { it.subPaneId == "recursive_calls" }.entities.map { it.dataValue.removePrefix("sumTo(").removeSuffix(")").toInt() }.toSet())
        assertEquals("BASE CASE", base.scene.subScenes.first { it.subPaneId == "recursive_calls" }.entities.last().badgeLabel)
        frames.filter { it.meta.action == "RETURN_ASCENT" }.forEach { returning ->
            val entity = returning.scene.subScenes.first { it.subPaneId == "recursive_calls" }.entities.last()
            assertTrue(entity.badgeLabel.orEmpty().startsWith("RETURNING ${returning.meta.currentReturnValue}"))
            assertEquals(returning.meta.currentN, entity.dataValue.removePrefix("sumTo(").removeSuffix(")").toInt())
        }
        frames.filter { it.meta.currentReturnValue == null }.forEach { frame ->
            assertTrue(frame.scene.subScenes.first { it.subPaneId == "return_history" }.entities.none { it.dataValue.isBlank() })
        }
        assertEquals(listOf("0", "1", "3", "6"), frames.last().scene.subScenes.first { it.subPaneId == "return_history" }.entities.map { it.dataValue })
    }

    @Test
    fun tradeoffExecutionBandsUseFullWidthAndStableIdentity() {
        listOf(4, 12).forEach { inputN ->
            val frames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace(inputN))
            val layout = IterationRecursionSceneLayout.calculate(frames)
            val iteration = layout.boundsFor("iteration_panel")
            val recursion = layout.boundsFor("recursion_panel")
            assertTrue(iteration.bottom < recursion.top)
            assertEquals(iteration.left, recursion.left)
            assertEquals(iteration.width, recursion.width)
            assertTrue(recursion.bottom < layout.boundsFor("return_history_label").top)
            val cells = (1..inputN).map { layout.boundsFor("tradeoff:iteration:$it") }
            val stack = (0..inputN).map { layout.boundsFor("tradeoff:frame:$it") }
            val history = (0..inputN).map { layout.boundsFor("tradeoff:return:$it") }
            assertNoOverlap(cells)
            assertNoOverlap(stack)
            assertNoOverlap(history)
            cells.forEach { assertContained(it, iteration) }
            stack.forEach { assertContained(it, recursion) }
            history.forEach { assertTrue(it.bottom <= layout.requiredSceneHeight) }
            frames.forEach { frame ->
                frame.scene.subScenes.flatMap { it.entities }.forEach { entity ->
                    assertTrue(entity.visualKey in layout.bounds)
                }
            }
            assertEquals(layout, IterationRecursionSceneLayout.calculate(frames))
        }
    }

    @Test
    fun tradeoffAuxiliaryMemoryTracksOneAccumulatorAndOnlyActiveStackFrames() {
        listOf(4, 12).forEach { inputN ->
            val frames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace(inputN))
            val memory = frames.map(IterationRecursionSceneLayout::tradeoffAuxiliaryMemory)

            assertTrue(memory.all { it.iterationStateCount == 1 })
            assertTrue(memory.all { it.recursionMaximumFrameCount == inputN + 1 })
            assertEquals(0, memory.first().recursionActiveFrameCount)
            assertTrue(
                frames.filter { it.meta.action == "ITERATIVE_LOOP" }
                    .all { IterationRecursionSceneLayout.tradeoffAuxiliaryMemory(it).recursionActiveFrameCount == 0 }
            )
            assertEquals(
                inputN + 1,
                IterationRecursionSceneLayout.tradeoffAuxiliaryMemory(
                    frames.first { it.meta.action == "RECURSIVE_BASE" }
                ).recursionActiveFrameCount
            )
            assertEquals(0, memory.last().recursionActiveFrameCount)
        }

        val defaultFrames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace())
        assertEquals(
            List(5) { 0 } + listOf(1, 2, 3, 4, 5, 4, 3, 2, 1, 0),
            defaultFrames.map {
                IterationRecursionSceneLayout.tradeoffAuxiliaryMemory(it).recursionActiveFrameCount
            }
        )
    }

    @Test
    fun recursiveStatesIdentifyReturningFrameReceiverAndCompletion() {
        val fundamentals = IterationRecursionSceneBuilder.recursion(RecursionFundamentalsTrace.createTrace())
        fundamentals.filter { it.meta.action == "RETURN_ASCENT" }.forEach { frame ->
            assertEquals(frame.meta.callStack.last(), frame.meta.returningFrameN)
            assertEquals(frame.meta.callStack.dropLast(1).lastOrNull(), frame.meta.receivingCallerN)
            val returning = frame.scene.subScenes.first { it.subPaneId == "recursive_calls" }.entities.last()
            assertTrue(returning.badgeLabel.orEmpty().startsWith("RETURNING ${frame.meta.currentReturnValue}"))
        }
        val tradeoffs = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace())
        val base = tradeoffs.first { it.meta.action == "RECURSIVE_BASE" }
        assertTrue(base.scene.subScenes.first { it.subPaneId == "recursion_memory" }.entities.last().badgeLabel.orEmpty().startsWith("BASE CASE"))
        tradeoffs.filter { it.meta.action == "RECURSIVE_ASCENT" }.forEach { frame ->
            assertEquals(frame.meta.recurCallStack.last(), frame.meta.returningFrameN)
            assertEquals(frame.meta.recurCallStack.dropLast(1).lastOrNull(), frame.meta.receivingCallerN)
            assertTrue(frame.scene.subScenes.first { it.subPaneId == "recursion_memory" }.entities.last().badgeLabel.orEmpty().contains(frame.meta.recurReturnValue.toString()))
        }
        assertTrue(tradeoffs.last().meta.recurCallStack.isEmpty())
        assertTrue(tradeoffs.last().scene.subScenes.first { it.subPaneId == "recursion_memory" }.entities.isEmpty())
    }

    @Test
    fun tradeoffReturnConnectorsRunBelowLabelsFromReturningFrameToReceiver() {
        listOf(4, 12).forEach { inputN ->
            val frames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace(inputN))
            val layout = IterationRecursionSceneLayout.calculate(frames)
            val recursionPanel = layout.boundsFor("recursion_panel")
            val ascents = frames.filter { it.meta.action == "RECURSIVE_ASCENT" }

            ascents.forEach { frame ->
                assertEquals(frame.meta.recurCallStack.last(), frame.meta.returningFrameN)
                assertEquals(frame.meta.recurCallStack.dropLast(1).lastOrNull(), frame.meta.receivingCallerN)

                val receivingN = frame.meta.receivingCallerN
                if (receivingN == null) {
                    assertEquals(null, IterationRecursionSceneLayout.tradeoffReturnConnector(layout, frame))
                } else {
                    val returningN = assertNotNull(frame.meta.returningFrameN)
                    val connector = assertNotNull(
                        IterationRecursionSceneLayout.tradeoffReturnConnector(layout, frame)
                    )
                    val returning = layout.boundsFor("tradeoff:frame:$returningN")
                    val receiving = layout.boundsFor("tradeoff:frame:$receivingN")

                    assertEquals(returning.left + returning.width / 2f, connector.fromX)
                    assertEquals(receiving.left + receiving.width / 2f, connector.toX)
                    assertTrue(connector.fromX > connector.toX)
                    assertEquals(connector.fromY, connector.toY)
                    assertTrue(connector.fromY > returning.bottom)
                    assertTrue(connector.toY > receiving.bottom)
                    assertTrue(connector.fromY < recursionPanel.bottom)
                    assertTrue(connector.fromX in recursionPanel.left..recursionPanel.right)
                    assertTrue(connector.toX in recursionPanel.left..recursionPanel.right)

                    (0..inputN).flatMap { n ->
                        listOf(
                            layout.boundsFor("tradeoff:frame:$n:name"),
                            layout.boundsFor("tradeoff:frame:$n:status")
                        )
                    }.forEach { label -> assertTrue(label.bottom < connector.fromY) }
                }
            }

            assertEquals(null, IterationRecursionSceneLayout.tradeoffReturnConnector(layout, frames.first()))
            assertEquals(null, IterationRecursionSceneLayout.tradeoffReturnConnector(layout, ascents.last()))
            val ascentWithReceiver = ascents.first { it.meta.receivingCallerN != null }
            val missingReceiverLayout = layout.copy(
                bounds = layout.bounds - "tradeoff:frame:${ascentWithReceiver.meta.receivingCallerN}"
            )
            assertEquals(
                null,
                IterationRecursionSceneLayout.tradeoffReturnConnector(missingReceiverLayout, ascentWithReceiver)
            )
            assertEquals(layout, IterationRecursionSceneLayout.calculate(frames))
        }
    }

    @Test
    fun recursiveFrameNamesAndStatusesHaveDedicatedContainedGeometry() {
        listOf(3, 12).forEach { inputN ->
            val fundamentalsFrames = IterationRecursionSceneBuilder.recursion(RecursionFundamentalsTrace.createTrace(inputN))
            val fundamentals = IterationRecursionSceneLayout.calculate(fundamentalsFrames)
            (0..inputN).forEach { n ->
                val frame = fundamentals.boundsFor("recursion:frame:$n")
                val name = fundamentals.boundsFor("recursion:frame:$n:name")
                val status = fundamentals.boundsFor("recursion:frame:$n:status")
                assertContained(name, frame)
                assertContained(status, frame)
                assertNoOverlap(listOf(name, status))

                val returned = fundamentals.boundsFor("recursion:return:$n")
                val returnName = fundamentals.boundsFor("recursion:return:$n:name")
                val returnValue = fundamentals.boundsFor("recursion:return:$n:value")
                assertContained(returnName, returned)
                assertContained(returnValue, returned)
                assertNoOverlap(listOf(returnName, returnValue))
            }
            assertEquals(fundamentals, IterationRecursionSceneLayout.calculate(fundamentalsFrames))

            val tradeoffFrames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace(inputN))
            val tradeoffs = IterationRecursionSceneLayout.calculate(tradeoffFrames)
            val recursionPanel = tradeoffs.boundsFor("recursion_panel")
            (0..inputN).forEach { n ->
                val frame = tradeoffs.boundsFor("tradeoff:frame:$n")
                val name = tradeoffs.boundsFor("tradeoff:frame:$n:name")
                val status = tradeoffs.boundsFor("tradeoff:frame:$n:status")
                assertContained(frame, recursionPanel)
                assertContained(name, frame)
                assertContained(status, frame)
                assertNoOverlap(listOf(name, status))

                val history = tradeoffs.boundsFor("tradeoff:return:$n")
                val historyName = tradeoffs.boundsFor("tradeoff:return:$n:name")
                val historyValue = tradeoffs.boundsFor("tradeoff:return:$n:value")
                assertTrue(history.top >= recursionPanel.bottom && history.right <= tradeoffs.requiredSceneWidth)
                assertContained(historyName, history)
                assertContained(historyValue, history)
                assertNoOverlap(listOf(historyName, historyValue))
            }
            assertEquals(tradeoffs, IterationRecursionSceneLayout.calculate(tradeoffFrames))
        }
    }

    @Test
    fun returningLabelsPreserveValuesAndReceivingCallersForLargeInput() {
        val fundamentals = IterationRecursionSceneBuilder.recursion(RecursionFundamentalsTrace.createTrace(12))
        fundamentals.filter { it.meta.action == "RETURN_ASCENT" }.forEach { frame ->
            val label = frame.scene.subScenes.first { it.subPaneId == "recursive_calls" }.entities.last().badgeLabel.orEmpty()
            assertTrue(label.contains("RETURNING ${frame.meta.currentReturnValue}"))
            frame.meta.receivingCallerN?.let { assertTrue(label.contains("sumTo($it)")) }
                ?: assertTrue(label.contains("RESULT"))
        }

        val tradeoffs = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace(12))
        tradeoffs.filter { it.meta.action == "RECURSIVE_ASCENT" }.forEach { frame ->
            val label = frame.scene.subScenes.first { it.subPaneId == "recursion_memory" }.entities.last().badgeLabel.orEmpty()
            assertTrue(label.contains("RETURNING ${frame.meta.recurReturnValue}"))
            frame.meta.receivingCallerN?.let { assertTrue(label.contains("sumTo($it)")) }
                ?: assertTrue(label.contains("RESULT"))
        }
    }

    @Test
    fun theoryHasNoCanvasGeometry() {
        val layout = IterationRecursionSceneLayout.calculate(
            IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace())
        )
        listOf("jvm_note", "complexity_strip", "iteration_aux", "recursion_aux", "header")
            .forEach { assertFalse(it in layout.bounds) }
    }

    @Test
    fun textFallbackScaleKeepsCompleteWrappedLayoutInsideItsRegion() {
        assertEquals(1f, IterationRecursionSceneLayout.textFallbackScale(100f, 40f, 120f, 60f))
        assertEquals(0.5f, IterationRecursionSceneLayout.textFallbackScale(200f, 40f, 100f, 60f))
        assertEquals(0.5f, IterationRecursionSceneLayout.textFallbackScale(80f, 120f, 100f, 60f))

        val scale = IterationRecursionSceneLayout.textFallbackScale(240f, 180f, 120f, 60f)
        assertTrue(240f * scale <= 120f)
        assertTrue(180f * scale <= 60f)
        assertTrue(scale > 0f)
    }

    @Test
    fun defaultTradeoffSceneFitsExecutionBands() {
        val layout = IterationRecursionSceneLayout.calculate(
            IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace())
        )
        assertEquals(480f, layout.requiredSceneWidth)
        assertEquals(292f, layout.requiredSceneHeight)
        assertTrue((4 downTo 0).map { layout.boundsFor("tradeoff:frame:$it") }
            .zipWithNext().all { (a, b) -> a.left < b.left })
        assertTrue((0..4).map { layout.boundsFor("tradeoff:return:$it") }
            .zipWithNext().all { (a, b) -> a.left < b.left })
    }

    @Test
    fun tradeoffReturnHistoryAppearsProgressivelyWithoutPrematureFinalResult() {
        val frames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace())
        assertEquals(
            listOf("INIT") + List(4) { "ITERATIVE_LOOP" } + List(4) { "RECURSIVE_DESCENT" } +
                listOf("RECURSIVE_BASE") + List(4) { "RECURSIVE_ASCENT" } + listOf("FINISHED"),
            frames.map { it.meta.action }
        )
        assertTrue(frames.first().meta.recurCallStack.isEmpty())
        assertEquals(listOf(4, 3, 2, 1, 0), frames.first { it.meta.action == "RECURSIVE_BASE" }.meta.recurCallStack)
        assertTrue(frames.last().meta.recurCallStack.isEmpty())
        frames.forEachIndexed { index, frame ->
            val history = IterationRecursionSceneLayout.completedTradeoffReturns(frames, index)
            val latestReturn = IterationRecursionSceneLayout.latestCompletedTradeoffReturn(frames, index)
            when (frame.meta.action) {
                "INIT", "ITERATIVE_LOOP", "RECURSIVE_DESCENT" -> {
                    assertTrue(history.isEmpty())
                    assertEquals(null, latestReturn)
                }
                "RECURSIVE_BASE" -> {
                    assertEquals(mapOf(0 to 0), history)
                    assertEquals(0, latestReturn)
                }
                "RECURSIVE_ASCENT" -> {
                    assertEquals(frame.meta.recurReturnValue, history[frame.meta.returningFrameN])
                    assertEquals((0..frame.meta.returningFrameN!!).toList(), history.keys.toList())
                    assertEquals(frame.meta.returningFrameN, latestReturn)
                }
                "FINISHED" -> {
                    assertEquals(mapOf(0 to 0, 1 to 1, 2 to 3, 3 to 6, 4 to 10), history)
                    assertEquals(4, latestReturn)
                }
            }
            if (frame.meta.action != "FINISHED") assertFalse(frame.meta.isTerminal)
        }
    }

    @Test
    fun largeTradeoffGridStaysCompactAndOtherLessonGeometryIsUnchanged() {
        val largeFrames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace(12))
        val large = IterationRecursionSceneLayout.calculate(largeFrames)
        assertEquals(1162f, large.requiredSceneWidth)
        assertEquals(292f, large.requiredSceneHeight)

        val iteration = IterationRecursionSceneLayout.calculate(IterationRecursionSceneBuilder.iteration(IterationFundamentalsTrace.createTrace()))
        val nested = IterationRecursionSceneLayout.calculate(IterationRecursionSceneBuilder.nested(NestedIterationTrace.createTrace()))
        val recursion = IterationRecursionSceneLayout.calculate(IterationRecursionSceneBuilder.recursion(RecursionFundamentalsTrace.createTrace()))
        assertEquals(480f to 290f, iteration.requiredSceneWidth to iteration.requiredSceneHeight)
        assertEquals(480f to 286f, nested.requiredSceneWidth to nested.requiredSceneHeight)
        assertEquals(520f to 310f, recursion.requiredSceneWidth to recursion.requiredSceneHeight)
    }

    @Test
    fun compactTradeoffInitialFitScalesForRepresentativeLandscapeViewports() {
        val frames = IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace())
        val layout = IterationRecursionSceneLayout.calculate(frames)
        val scale400x250 = IterationRecursionSceneLayout.initialFitScale(layout, 400f, 250f)
        val scale500x300 = IterationRecursionSceneLayout.initialFitScale(layout, 500f, 300f)
        assertEquals(400f / 480f, scale400x250)
        assertEquals(1f, scale500x300)
        assertTrue(16f * scale400x250 >= 13f)
        assertTrue(16f * scale500x300 >= 16f)
    }

    @Test
    fun defaultSnapshotCountsAndTerminalResultsRemainUnchanged() {
        val iteration = IterationFundamentalsTrace.createTrace()
        val nested = NestedIterationTrace.createTrace()
        val recursion = RecursionFundamentalsTrace.createTrace()
        val tradeoffs = RecursionTradeoffsTrace.createTrace()
        assertEquals(14, iteration.size)
        assertEquals(10, nested.size)
        assertEquals(9, recursion.size)
        assertEquals(15, tradeoffs.size)
        assertEquals(10, iteration.last().total)
        assertEquals(listOf(1 to 1, 1 to 2, 1 to 3, 2 to 1, 2 to 2, 2 to 3), nested.last().printedPairs)
        assertEquals(6, recursion.last().currentReturnValue)
        assertEquals(10, tradeoffs.last().iterTotal)
        assertEquals(10, tradeoffs.last().recurReturnVal)
    }

    private fun allSequences() = listOf(
        IterationRecursionSceneBuilder.iteration(IterationFundamentalsTrace.createTrace()),
        IterationRecursionSceneBuilder.nested(NestedIterationTrace.createTrace()),
        IterationRecursionSceneBuilder.recursion(RecursionFundamentalsTrace.createTrace()),
        IterationRecursionSceneBuilder.tradeoffs(RecursionTradeoffsTrace.createTrace())
    )

    private fun assertNoOverlap(bounds: List<Rect2D>) {
        bounds.indices.forEach { first ->
            for (second in first + 1 until bounds.size) {
                val a = bounds[first]
                val b = bounds[second]
                assertTrue(a.right <= b.left || b.right <= a.left || a.bottom <= b.top || b.bottom <= a.top)
            }
        }
    }

    private fun assertContained(inner: Rect2D, outer: Rect2D) {
        assertTrue(inner.left >= outer.left && inner.top >= outer.top && inner.right <= outer.right && inner.bottom <= outer.bottom)
    }
}
