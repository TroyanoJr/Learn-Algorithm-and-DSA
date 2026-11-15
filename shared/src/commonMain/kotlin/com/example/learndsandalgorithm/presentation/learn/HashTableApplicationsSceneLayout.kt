package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.min

internal data class HashTableApplicationsLayoutResult(
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float,
    val primaryCard: Rect2D,
    val secondaryCard: Rect2D? = null,
    val annotationBounds: Map<String, Rect2D> = emptyMap(),
    val inputBounds: Map<Int, Rect2D> = emptyMap(),
    val mapBounds: Map<Char, Rect2D> = emptyMap(),
    val hashBounds: Map<Int, Rect2D> = emptyMap(),
    val bstBounds: Map<Int, Rect2D> = emptyMap(),
    val mutableSlotBounds: Map<Int, Rect2D> = emptyMap()
)

internal object HashTableApplicationsSceneLayout {
    fun structureType(scenario: ApplicationScenario) = if (scenario == ApplicationScenario.RANGE_QUERY_COMPARISON) StructureType.TABLE else StructureType.HASH_TABLE
    fun sceneWidth(scenario: ApplicationScenario) = when (scenario) {
        ApplicationScenario.FREQUENCY_COUNTER -> 380f
        ApplicationScenario.RANGE_QUERY_COMPARISON -> 380f
        ApplicationScenario.MUTABLE_KEY_PITFALL -> 400f
    }
    fun sceneHeight(scenario: ApplicationScenario) = when (scenario) {
        ApplicationScenario.FREQUENCY_COUNTER -> 174f
        ApplicationScenario.RANGE_QUERY_COMPARISON -> 184f
        ApplicationScenario.MUTABLE_KEY_PITFALL -> 154f
    }

    fun calculate(scenario: ApplicationScenario): HashTableApplicationsLayoutResult = when (scenario) {
        ApplicationScenario.FREQUENCY_COUNTER -> HashTableApplicationsLayoutResult(
            380f, 174f,
            primaryCard = Rect2D(10f, 8f, 360f, 70f),
            secondaryCard = Rect2D(10f, 86f, 360f, 78f),
            annotationBounds = mapOf(
                "input_title" to Rect2D(20f, 17f, 340f, 16f),
                "frequency_title" to Rect2D(20f, 94f, 340f, 15f)
            ),
            inputBounds = (0..4).associateWith { Rect2D(22f + it * 48f, 39f, 40f, 28f) },
            mapBounds = listOf('a', 'b', 'c').mapIndexed { index, c -> c to Rect2D(22f + index * 78f, 116f, 68f, 38f) }.toMap()
        )
        ApplicationScenario.RANGE_QUERY_COMPARISON -> {
            fun row(top: Float) = (0..7).associateWith { Rect2D(18f + it * 43f, top, 38f, 34f) }
            HashTableApplicationsLayoutResult(
                380f, 184f,
                primaryCard = Rect2D(10f, 8f, 360f, 78f),
                secondaryCard = Rect2D(10f, 96f, 360f, 78f),
                annotationBounds = mapOf(
                    "hash_title" to Rect2D(20f, 16f, 145f, 15f),
                    "hash_complexity" to Rect2D(174f, 16f, 186f, 15f),
                    "bst_title" to Rect2D(20f, 104f, 132f, 15f),
                    "bst_complexity" to Rect2D(160f, 104f, 200f, 15f)
                ),
                hashBounds = row(43f), bstBounds = row(131f)
            )
        }
        ApplicationScenario.MUTABLE_KEY_PITFALL -> HashTableApplicationsLayoutResult(
            400f, 154f,
            primaryCard = Rect2D(10f, 8f, 380f, 136f),
            annotationBounds = mapOf(
                "mutable_title" to Rect2D(20f, 17f, 205f, 16f),
                "mutation_badge" to Rect2D(242f, 17f, 128f, 15f),
                "stored_annotation" to Rect2D(20f, 43f, 350f, 15f),
                "lookup_annotation" to Rect2D(20f, 63f, 350f, 15f)
            ),
            mutableSlotBounds = (0..7).associateWith { Rect2D(12f + it * 47f, 86f, 43f, 46f) }
        )
    }

    fun initialFitScale(scenario: ApplicationScenario, viewportWidth: Float, viewportHeight: Float): Float =
        min(1f, min(viewportWidth / sceneWidth(scenario), viewportHeight / sceneHeight(scenario)))
}
