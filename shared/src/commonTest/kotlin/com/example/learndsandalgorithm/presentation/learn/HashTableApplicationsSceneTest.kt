package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HashTableApplicationsSceneTest {
    @Test
    fun scenarioScenesHaveStableGeometryAndNoPhantomEntities() {
        ApplicationScenario.entries.forEach { scenario ->
            val steps = HashTableApplicationsTrace.stepsForScenario(scenario)
            val scenes = HashTableApplicationsSceneBuilder.buildScenes(steps)
            val layout = HashTableApplicationsSceneLayout.calculate(scenario)
            assertEquals(steps.size, scenes.size)
            assertEquals(HashTableApplicationsSceneLayout.sceneWidth(scenario), layout.requiredSceneWidth)
            assertEquals(HashTableApplicationsSceneLayout.sceneHeight(scenario), layout.requiredSceneHeight)
            scenes.forEachIndexed { index, scene ->
                assertEquals(index, scene.stepIndex)
                if (scenario == ApplicationScenario.FREQUENCY_COUNTER) {
                    assertEquals(5 + steps[index].freqMap.size, scene.subScenes.first().entities.size)
                    assertEquals((0..4).map { "frequency:input:$it" }, scene.subScenes.first().entities.take(5).map { it.elementId })
                }
                if (scenario == ApplicationScenario.RANGE_QUERY_COMPARISON) {
                    assertEquals(16, scene.subScenes.first().entities.size)
                    assertEquals((0..7).map { "range:hash:$it" }, scene.subScenes.first().entities.take(8).map { it.elementId })
                }
                if (scenario == ApplicationScenario.MUTABLE_KEY_PITFALL) {
                    assertEquals(1, scene.subScenes.first().entities.size)
                    assertEquals("mutable:user", scene.subScenes.first().entities.single().elementId)
                    assertEquals("mutable:slot:5", scene.subScenes.first().entities.single().slotKey)
                }
            }
        }
    }

    @Test
    fun allApplicationGeometryIsContainedAndNonOverlapping() {
        ApplicationScenario.entries.forEach { scenario ->
            val layout = HashTableApplicationsSceneLayout.calculate(scenario)
            val bounds = layout.annotationBounds.values + layout.inputBounds.values + layout.mapBounds.values + layout.hashBounds.values + layout.bstBounds.values + layout.mutableSlotBounds.values
            assertTrue(bounds.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
            listOf(layout.inputBounds.values, layout.mapBounds.values, layout.hashBounds.values, layout.bstBounds.values, layout.mutableSlotBounds.values).forEach { row ->
                row.sortedBy { it.left }.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
            }
            if (scenario == ApplicationScenario.RANGE_QUERY_COMPARISON) {
                assertTrue(layout.annotationBounds.getValue("hash_title").right <= layout.annotationBounds.getValue("hash_complexity").left)
                assertTrue(layout.annotationBounds.getValue("bst_title").right <= layout.annotationBounds.getValue("bst_complexity").left)
            }
        }
    }

    @Test
    fun initialFitCalculationsUseCompleteScenarioBounds() {
        val smallestVisualWidth = 329.45456f
        assertEquals(0.8669857f, HashTableApplicationsSceneLayout.initialFitScale(ApplicationScenario.FREQUENCY_COUNTER, smallestVisualWidth, 194f))
        assertEquals(0.8669857f, HashTableApplicationsSceneLayout.initialFitScale(ApplicationScenario.RANGE_QUERY_COMPARISON, smallestVisualWidth, 194f))
        assertEquals(0.8236364f, HashTableApplicationsSceneLayout.initialFitScale(ApplicationScenario.MUTABLE_KEY_PITFALL, smallestVisualWidth, 194f))
        assertEquals(1f, HashTableApplicationsSceneLayout.initialFitScale(ApplicationScenario.FREQUENCY_COUNTER, 381.81818f, 220f))
        assertEquals(1f, HashTableApplicationsSceneLayout.initialFitScale(ApplicationScenario.RANGE_QUERY_COMPARISON, 381.81818f, 220f))
        assertEquals(0.95454544f, HashTableApplicationsSceneLayout.initialFitScale(ApplicationScenario.MUTABLE_KEY_PITFALL, 381.81818f, 220f))
        ApplicationScenario.entries.forEach { scenario ->
            assertEquals(1f, HashTableApplicationsSceneLayout.initialFitScale(scenario, 436.36365f, 253f))
        }
    }

    @Test
    fun longestScenarioBadgesAndExplanationsRemainComplete() {
        ApplicationScenario.entries.forEach { scenario ->
            val steps = HashTableApplicationsTrace.stepsForScenario(scenario)
            val longestBadge = steps.maxBy { it.badgeText.length }.badgeText
            val longestExplanation = steps.maxBy { it.explanation.length }.explanation
            assertTrue(longestBadge.isNotBlank())
            assertTrue(longestExplanation.isNotBlank())
            assertTrue(steps.any { it.badgeText == longestBadge })
            assertTrue(steps.any { it.explanation == longestExplanation })
        }
        val range = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.RANGE_QUERY_COMPARISON)
        assertTrue(range.last().explanation.contains("Conclusion: Use BST when key ordering or range queries are required!"))
        val mutable = HashTableApplicationsTrace.stepsForScenario(ApplicationScenario.MUTABLE_KEY_PITFALL)
        assertEquals("ENTRY UNFINDABLE! (TRAPPED AT [5])", mutable.last().badgeText)
        assertTrue(mutable.last().explanation.endsWith("Lesson: Always use immutable keys!"))
    }
}
