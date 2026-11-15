package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ArraySceneBuilderTest {
    @Test
    fun resizeScenesPreserveFiveExactCapacityStatesAndOldNewStorage() {
        val trace = ArrayTrace.resizeSteps()
        val frames = ArraySceneBuilder.buildResizeFrames(trace)
        val expectedInitial = listOf("10", "20", "30", "40")
        val expectedFull = expectedInitial + "50"

        assertEquals(5, frames.size)
        assertEquals(trace.map { it.explanationText }, frames.map { it.explanation })
        assertEquals((1..5).toList(), frames.map { it.snapshotIndex + 1 })
        assertEquals(listOf(5, 5, 5, 5, 5), frames.map { it.snapshotCount })

        assertEquals(expectedInitial, occupiedValues(frames[0], "primary"))
        assertEquals(5, frames[0].regions.first { it.key == "primary" }.slotCount)
        assertEquals(4, occupiedValues(frames[0], "primary").size)
        assertEquals(setOf(4), highlightedIndices(frames[0], "primary"))
        assertEquals("array:item:50", entityAt(frames[1], "primary", 4).elementId)
        assertEquals(expectedFull, occupiedValues(frames[1], "primary"))
        assertTrue(frames[1].regions.first { it.key == "primary" }.headerText!!.contains("capacity 5 · size 5 · occupied 5"))

        assertEquals(listOf("primary", "secondary"), frames[2].regions.map { it.key })
        assertEquals(expectedFull, occupiedValues(frames[2], "primary"))
        assertEquals(emptyList(), occupiedValues(frames[2], "secondary"))
        assertEquals(10, frames[2].regions.last().slotCount)
        assertEquals("NEW STORAGE · capacity 10 · size 5 · occupied 0", frames[2].regions.last().headerText)

        assertEquals(expectedFull, occupiedValues(frames[3], "secondary"))
        assertEquals((0..4).toSet(), highlightedIndices(frames[3], "secondary"))
        assertEquals(
            (0..4).map { entityAt(frames[2], "primary", it).elementId },
            (0..4).map { entityAt(frames[3], "secondary", it).elementId }
        )
        assertEquals(
            (0..4).map { entityAt(frames[2], "primary", it).visualKey },
            (0..4).map { entityAt(frames[3], "secondary", it).visualKey }
        )
        assertEquals(expectedFull + "60", occupiedValues(frames[4], "secondary"))
        assertEquals("array:item:60", entityAt(frames[4], "secondary", 5).elementId)
        assertEquals(setOf(5), activeIndices(frames[4], "secondary"))
        assertEquals(listOf("READY", "TRANSITION 1 OF 4", "TRANSITION 2 OF 4", "TRANSITION 3 OF 4", "COMPLETED"), frames.map { it.statusText })
        assertEquals(listOf(false, false, false, false, true), frames.map { it.isCompleted })
    }

    @Test
    fun traversalMapsEachTraceStateOnceAndCountersStayWithinSnapshotCount() {
        val trace = ArrayTrace.traversalSteps()
        val frames = ArraySceneBuilder.buildTraversalFrames(trace)

        assertEquals(12, frames.size)
        assertEquals(trace.map { it.explanationText }, frames.map { it.explanation })
        assertEquals(trace.indices.toList(), frames.map { it.scene.stepIndex })
        assertEquals(1, frames.count { it.isCompleted })
        assertEquals("READY", frames.first().statusText)
        assertEquals("COMPLETED", frames.last().statusText)
        frames.forEachIndexed { index, frame ->
            assertEquals(index + 1, frame.snapshotIndex + 1)
            assertEquals(12, frame.snapshotCount)
            assertTrue(frame.snapshotIndex + 1 <= frame.snapshotCount)
            assertEquals(trace[index].arrayData, occupiedValues(frame, "array"))
        }
        for (visit in 0 until 10) {
            val frame = frames[visit + 1]
            assertEquals(setOf(visit), activeIndices(frame, "array"))
            assertEquals("VISIT ${visit + 1} OF 10", frame.statusText)
            assertEquals("${visit + 1} / 10 VISITED", frame.footer)
        }
        assertEquals(emptySet(), activeIndices(frames.last(), "array"))
        assertEquals("10 / 10 VISITED", frames.last().footer)

        val playback = VisualizerPlaybackState(initialStep = 0, stepCount = frames.size)
        val displayedIndices = mutableListOf<Int>()
        while (true) {
            displayedIndices += playback.safeStepIndex
            if (playback.isCompleted) break
            playback.next()
        }
        assertEquals((0 until 12).toList(), displayedIndices)
        assertEquals(11, playback.safeStepIndex)
        assertTrue(playback.isCompleted)
    }

    @Test
    fun insertAndDeleteKeepAllElevenSnapshotsAndExactShiftIdentity() {
        val base = listOf("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")
        val inserts = ArraySceneBuilder.buildInsertDeleteFrames(ArrayTrace.insertDeleteSteps("INSERT"), "INSERT")
        val deletes = ArraySceneBuilder.buildInsertDeleteFrames(ArrayTrace.insertDeleteSteps("DELETE"), "DELETE")

        assertEquals(11, inserts.size)
        assertEquals(11, deletes.size)
        assertEquals(base, occupiedValues(inserts.first(), "array"))
        assertEquals(base, occupiedValues(deletes.first(), "array"))
        assertEquals(listOf("10", "20", "25", "30", "40", "50", "60", "70", "80", "90", "100"), occupiedValues(inserts.last(), "array"))
        assertEquals(listOf("10", "30", "40", "50", "60", "70", "80", "90", "100"), occupiedValues(deletes.last(), "array"))
        assertEquals(listOf("READY") + (1..9).map { "TRANSITION $it OF 10" } + "COMPLETED", inserts.map { it.statusText })
        assertEquals(listOf("READY") + (1..9).map { "TRANSITION $it OF 10" } + "COMPLETED", deletes.map { it.statusText })
        assertEquals(ArrayTrace.insertDeleteSteps("INSERT").map { it.explanationText }, inserts.map { it.explanation })
        assertEquals(ArrayTrace.insertDeleteSteps("DELETE").map { it.explanationText }, deletes.map { it.explanation })
        assertEquals(
            ArrayTrace.insertDeleteSteps("INSERT").map { it.displayItems.filterNot { value -> value.isBlank() } },
            inserts.map { occupiedValues(it, "array") }
        )
        assertEquals(
            ArrayTrace.insertDeleteSteps("DELETE").map { it.displayItems.filterNot { value -> value.isBlank() } },
            deletes.map { occupiedValues(it, "array") }
        )

        assertEquals(setOf(2), activeIndices(inserts.first(), "array"))
        assertEquals(setOf(10), activeIndices(inserts[2], "array"))
        assertEquals(setOf(9, 10), highlightedIndices(inserts[2], "array"))
        assertEquals(setOf(2), activeIndices(inserts.last(), "array"))
        assertEquals(setOf(1), activeIndices(deletes.first(), "array"))
        assertEquals(setOf(1), activeIndices(deletes[2], "array"))
        assertEquals(setOf(1, 2), highlightedIndices(deletes[2], "array"))
        assertEquals(setOf(8), activeIndices(deletes.last(), "array"))
        assertEquals("array:item:initial:9", entityAt(inserts[2], "array", 10).elementId)
        assertEquals("array:item:initial:2", entityAt(inserts[9], "array", 3).elementId)
        assertEquals("array:item:inserted:25", entityAt(inserts.last(), "array", 2).elementId)
        assertEquals("array:item:initial:2", entityAt(deletes[2], "array", 1).elementId)
        assertEquals("array:item:initial:9", entityAt(deletes[9], "array", 8).elementId)
        assertTrue((inserts + deletes).all { it.snapshotIndex + 1 <= it.snapshotCount })
    }

    @Test
    fun duplicateValuesKeepDistinctStableIdentitiesAcrossShifts() {
        val duplicateBase = listOf("A", "A", "B", "C", "D", "E", "F", "G", "H", "I")
        val insertTrace = ArrayTrace.insertDeleteSteps("INSERT", duplicateBase)
        val insertFrames = ArraySceneBuilder.buildInsertDeleteFrames(insertTrace, "INSERT")
        val firstA = entityAt(insertFrames.first(), "array", 0)
        val secondA = entityAt(insertFrames.first(), "array", 1)

        assertEquals("A", firstA.dataValue)
        assertEquals("A", secondA.dataValue)
        assertNotEquals(firstA.elementId, secondA.elementId)
        assertEquals(firstA.elementId, entityAt(insertFrames.last(), "array", 0).elementId)
        assertEquals(secondA.elementId, entityAt(insertFrames.last(), "array", 1).elementId)
        assertEquals("array:item:initial:9", entityAt(insertFrames[2], "array", 10).elementId)
    }

    private fun occupiedValues(frame: ArraySceneFrame, regionKey: String): List<String> =
        frame.scene.subScenes.firstOrNull { it.subPaneId == regionKey }?.entities.orEmpty()
            .sortedBy { it.slotKey.substringAfterLast(':').toInt() }
            .map { it.dataValue }

    private fun entityAt(frame: ArraySceneFrame, regionKey: String, index: Int): VisualEntity =
        frame.scene.subScenes.first { it.subPaneId == regionKey }.entities.first { it.slotKey.endsWith(":$index") }

    private fun activeIndices(frame: ArraySceneFrame, regionKey: String): Set<Int> =
        frame.scene.subScenes.firstOrNull { it.subPaneId == regionKey }?.entities.orEmpty()
            .filter { it.visualState == EntityVisualState.ACTIVE }
            .map { it.slotKey.substringAfterLast(':').toInt() }
            .toSet()

    private fun highlightedIndices(frame: ArraySceneFrame, regionKey: String): Set<Int> =
        frame.regions.first { it.key == regionKey }.highlightedIndices
}
