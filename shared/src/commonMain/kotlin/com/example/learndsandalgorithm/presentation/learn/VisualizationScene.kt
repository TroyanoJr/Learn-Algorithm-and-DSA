package com.example.learndsandalgorithm.presentation.learn

/**
 * High-level data structure domain type.
 */
internal enum class StructureType {
    TREE,
    GRAPH,
    ARRAY,
    TABLE,
    STACK,
    QUEUE
}

/**
 * Visual state of an individual entity (node, cell, or vertex).
 */
internal enum class EntityVisualState {
    DEFAULT,
    ACTIVE,
    VISITED,
    VIOLATION,
    TARGET
}

/**
 * Presence lifecycle state of a visual entity within a scene transition.
 */
internal enum class EntityPresenceState {
    APPEARING,
    PRESENT,
    DISAPPEARING
}

/**
 * Visual state of a structural connector or edge.
 */
internal enum class ConnectionVisualState {
    DEFAULT,
    HIGHLIGHTED,
    RELAXING,
    PATH
}

/**
 * Semantic type of a connection.
 */
internal enum class ConnectionType {
    STRUCTURAL_BRANCH,
    GRAPH_EDGE,
    POINTER_LINK
}

/**
 * Represents a discrete visual entity in the scene (a node, array cell, queue entry, or table row).
 *
 * @param visualKey Unique UI representation key in this sub-scene (e.g. "heap_tree:elem:0").
 * @param elementId Persistent logical identity of the underlying data item across steps (e.g. "elem:0").
 * @param dataValue Presentation payload string (e.g. "10"). Duplicate values share equal dataValue but distinct elementIds.
 * @param slotKey Structural location key occupied by this item at step N (e.g. "tree_slot:0" or "array_slot:2").
 * @param badgeLabel Optional badge label text (e.g. "Root 5", "dist=1").
 * @param occurrenceId Optional key for ephemeral/temporary entries (e.g. Priority Queue insertion instances).
 * @param visualState Visual state descriptor (DEFAULT, ACTIVE, VISITED, VIOLATION, TARGET).
 * @param presenceState Presence state descriptor (APPEARING, PRESENT, DISAPPEARING).
 */
internal data class VisualEntity(
    val visualKey: String,
    val elementId: String,
    val dataValue: String,
    val slotKey: String,
    val badgeLabel: String? = null,
    val occurrenceId: String? = null,
    val visualState: EntityVisualState = EntityVisualState.DEFAULT,
    val presenceState: EntityPresenceState = EntityPresenceState.PRESENT
)

/**
 * Represents a connection or edge between two visual entities in a sub-scene.
 *
 * @param connectionKey Canonical identity of the edge (e.g. "edge:A->C" or "branch:slot_0->slot_1").
 * @param fromVisualKey Source visual entity key.
 * @param toVisualKey Target visual entity key.
 * @param label Optional edge weight or relation label (e.g. "w=4").
 * @param type Connection classification (STRUCTURAL_BRANCH, GRAPH_EDGE, POINTER_LINK).
 * @param visualState Connection visual state (DEFAULT, HIGHLIGHTED, RELAXING, PATH).
 */
internal data class VisualConnection(
    val connectionKey: String,
    val fromVisualKey: String,
    val toVisualKey: String,
    val label: String? = null,
    val type: ConnectionType = ConnectionType.STRUCTURAL_BRANCH,
    val visualState: ConnectionVisualState = ConnectionVisualState.DEFAULT
)

/**
 * Sub-scene representing a specific data structure pane within the visualizer.
 *
 * @param subPaneId Identifier for the sub-pane (e.g. "tree_pane", "queue_pane", "table_pane").
 * @param type Data structure domain classification.
 * @param didacticPriority Priority weight for layout allocation (1 = Primary, 2 = Secondary).
 * @param minRequiredWidthDp Minimum required width in Dp for geometry calculation.
 * @param minRequiredHeightDp Minimum required height in Dp for geometry calculation.
 * @param entities Visual entities residing in this sub-scene.
 * @param connections Visual connections between entities in this sub-scene.
 */
internal data class StructureSubScene(
    val subPaneId: String,
    val type: StructureType,
    val didacticPriority: Int = 1,
    val minRequiredWidthDp: Float = 120f,
    val minRequiredHeightDp: Float = 120f,
    val entities: List<VisualEntity> = emptyList(),
    val connections: List<VisualConnection> = emptyList()
)

/**
 * Immutable abstract visual scene representing step N of an algorithm execution.
 * Contains no Compose, Canvas, coordinate, or layout dependencies.
 *
 * @param stepIndex Trace step index corresponding to this scene.
 * @param sceneId Unique scene identifier.
 * @param subScenes Sub-scenes for each active data structure pane.
 * @param globalMessage Optional top-level explanation message.
 */
internal data class VisualizationScene(
    val stepIndex: Int,
    val sceneId: String,
    val subScenes: List<StructureSubScene> = emptyList(),
    val globalMessage: String? = null
)
