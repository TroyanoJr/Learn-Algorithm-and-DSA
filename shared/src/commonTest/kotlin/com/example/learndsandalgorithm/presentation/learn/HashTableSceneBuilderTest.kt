package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HashTableSceneBuilderTest {


    @Test
    fun collisionCalculationPreservesMetadataAndOnlyRevealsAvailableArithmetic() {
        CollisionStrategy.entries.forEach { strategy ->
            val steps = HashTableCollisionTrace.stepsForStrategy(strategy)
            val scenes = HashTableSceneBuilder.buildCollisionScenes(steps)
            val identities = mutableMapOf<String, Pair<String, String>>()
            steps.zip(scenes).forEach { (step, scene) ->
                val pane = scene.subScenes.first()
                pane.entities.filter { it.entryKey != null }.forEach { entry ->
                    val identity = entry.elementId to entry.visualKey
                    assertEquals(identity, identities.getOrPut(entry.entryKey!!) { identity })
                }
                pane.connections.forEach { connection ->
                    assertTrue(pane.entities.any { it.visualKey == connection.fromVisualKey })
                    assertTrue(pane.entities.any { it.visualKey == connection.toVisualKey })
                }
                if (step.event == CollisionTraceEvent.PROBE_NEXT_SLOT) {
                    val target = pane.entities.first { it.slotKey == "linear_slot:${step.currentProbeIndex}" }
                    assertEquals("PROBE", target.badgeLabel)
                    assertEquals(EntityVisualState.ACTIVE, target.visualState)
                }
                val original = scene.hashTableMeta!!
                val calculation = collisionCalculationMetadata(original)
                assertEquals(step.activeKey, calculation.activeKey)
                assertEquals(step.activeValue, calculation.activeValue)
                assertEquals(step.badgeText, calculation.badgeText)
                assertEquals(step.explanation, calculation.explanation)
                assertEquals(step.currentProbeIndex, calculation.currentProbeIndex)
                assertEquals(step.probedIndices, calculation.probedIndices)
                assertEquals(step.event, calculation.collisionEvent)
                if (step.initialHashIndex == null) {
                    assertEquals(original, calculation)
                } else {
                    val parts = step.activeKey!!.map { it to it.code }
                    assertEquals(parts, calculation.charBreakdown)
                    assertEquals(parts.sumOf { it.second }, calculation.hashSum)
                    assertEquals(step.initialHashIndex, calculation.calculatedIndex)
                    assertEquals(
                        original,
                        calculation.copy(charBreakdown = original.charBreakdown,
                            hashSum = original.hashSum, calculatedIndex = original.calculatedIndex)
                    )
                }
            }
        }
    }

    @Test
    fun testEmptyStepsListReturnsEmptyScenesList() {
        val scenes = HashTableSceneBuilder.buildFundamentalsScenes(emptyList())
        assertTrue(scenes.isEmpty())
    }

    @Test
    fun testFundamentalsScenesGenerationAndMetadataCompleteness() {
        val steps = HashTableTrace.stepsForFundamentals(capacity = 8)
        val scenes = HashTableSceneBuilder.buildFundamentalsScenes(steps)

        assertEquals(steps.size, scenes.size)
        scenes.forEachIndexed { idx, scene ->
            val subScene = scene.subScenes.first()
            assertEquals(StructureType.HASH_TABLE, subScene.type)
            assertEquals(8, subScene.entities.count { it.slotKey.startsWith("bucket_slot:") })

            val meta = scene.hashTableMeta
            assertNotNull(meta)
            val expectedStep = steps[idx]
            assertEquals(expectedStep.activeKey, meta.activeKey)
            assertEquals(expectedStep.activeValue, meta.activeValue)
            assertEquals(expectedStep.charBreakdown, meta.charBreakdown)
            assertEquals(expectedStep.hashSum, meta.hashSum)
            assertEquals(expectedStep.calculatedIndex, meta.calculatedIndex)
            assertEquals(expectedStep.capacity, meta.capacity)
            assertEquals(expectedStep.event, meta.event)
            assertEquals(expectedStep.badgeText, meta.badgeText)
            assertEquals(expectedStep.isTerminal, meta.isTerminal)
            assertEquals(expectedStep.explanation, meta.explanation)
        }
    }

    @Test
    fun testStableIdentitiesAcrossStepsForSameKey() {
        val steps = HashTableTrace.stepsForFundamentals(capacity = 8)
        val scenes = HashTableSceneBuilder.buildFundamentalsScenes(steps)

        val catSelectIdx = steps.indexOfFirst { it.activeKey == "CAT" && it.event == HashTableEvent.SELECT_KEY }
        val catInsertIdx = steps.indexOfFirst { it.activeKey == "CAT" && it.event == HashTableEvent.INSERT_ENTRY }
        val finalIdx = steps.size - 1

        assertTrue(catSelectIdx >= 0)
        assertTrue(catInsertIdx >= 0)

        val catInsertScene = scenes[catInsertIdx]
        val catInsertSub = catInsertScene.subScenes.first()
        val catInsertEntity = catInsertSub.entities.first { it.entryKey == "CAT" }

        val finalScene = scenes[finalIdx]
        val finalSub = finalScene.subScenes.first()
        val finalCatEntity = finalSub.entities.first { it.entryKey == "CAT" }

        assertEquals(catInsertEntity.elementId, finalCatEntity.elementId)
        assertEquals("CAT", finalCatEntity.entryKey)
        assertEquals("Feline", finalCatEntity.entryValue)
    }

    @Test
    fun testEntryAssociatedWithCorrectBucketAndStructuredFields() {
        val steps = HashTableTrace.stepsForFundamentals(capacity = 8)
        val scenes = HashTableSceneBuilder.buildFundamentalsScenes(steps)

        val lastScene = scenes.last()
        val subScene = lastScene.subScenes.first()

        val catEntity = subScene.entities.first { it.entryKey == "CAT" }
        assertEquals("entry_slot:0", catEntity.slotKey)
        assertEquals("CAT", catEntity.entryKey)
        assertEquals("Feline", catEntity.entryValue)

        val dogEntity = subScene.entities.first { it.entryKey == "DOG" }
        assertEquals("entry_slot:2", dogEntity.slotKey)
        assertEquals("DOG", dogEntity.entryKey)
        assertEquals("Canine", dogEntity.entryValue)

        val foxEntity = subScene.entities.first { it.entryKey == "FOX" }
        assertEquals("entry_slot:5", foxEntity.slotKey)
        assertEquals("FOX", foxEntity.entryKey)
        assertEquals("Vixen", foxEntity.entryValue)

        val s102Entity = subScene.entities.first { it.entryKey == "S102" }
        assertEquals("entry_slot:6", s102Entity.slotKey)
        assertEquals("S102", s102Entity.entryKey)
        assertEquals("Alice", s102Entity.entryValue)
    }

    @Test
    fun testCollisionSnapshotsExactCount() {
        val chainingSteps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING, capacity = 7)
        val chainingScenes = HashTableSceneBuilder.buildCollisionScenes(chainingSteps)
        assertEquals(15, chainingScenes.size)

        val probingSteps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 7)
        val probingScenes = HashTableSceneBuilder.buildCollisionScenes(probingSteps)
        assertEquals(17, probingScenes.size)

        assertEquals(32, chainingScenes.size + probingScenes.size)
    }

    @Test
    fun testBuildCollisionScenesSeparateChainingSequenceLevelMaxDepth() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.SEPARATE_CHAINING, capacity = 7)
        val scenes = HashTableSceneBuilder.buildCollisionScenes(steps)

        assertEquals(steps.size, scenes.size)
        val expectedMaxDepth = steps.maxOf { s -> s.chainingBuckets.maxOf { it.size } }

        scenes.forEach { scene ->
            val meta = scene.hashTableMeta
            assertNotNull(meta)
            assertEquals(expectedMaxDepth, meta.maxChainDepth)
            assertEquals(CollisionStrategy.SEPARATE_CHAINING, meta.strategy)
        }
    }

    @Test
    fun testBuildCollisionScenesLinearProbingMetadata() {
        val steps = HashTableCollisionTrace.stepsForStrategy(CollisionStrategy.LINEAR_PROBING, capacity = 7)
        val scenes = HashTableSceneBuilder.buildCollisionScenes(steps)

        assertEquals(steps.size, scenes.size)
        scenes.forEachIndexed { idx, scene ->
            val meta = scene.hashTableMeta
            assertNotNull(meta)
            assertEquals(CollisionStrategy.LINEAR_PROBING, meta.strategy)
            val expectedStep = steps[idx]
            assertEquals(expectedStep.activeKey, meta.activeKey)
            assertEquals(expectedStep.event, meta.collisionEvent)
        }
    }
}
