package com.example.learndsandalgorithm.presentation.learn

internal data class GraphSceneAdapterResult(
    val vertices: List<GraphVertexData>,
    val edges: List<GraphEdgeData>,
    val vertexEntityMap: Map<String, VisualEntity>,
    val edgeConnectionMap: Map<String, VisualConnection>
)

internal object GraphSceneAdapter {
    fun adapt(subScene: StructureSubScene): GraphSceneAdapterResult {
        require(subScene.type == StructureType.GRAPH) {
            "GraphSceneAdapter requires StructureSubScene of type GRAPH, got ${subScene.type}"
        }

        val vertices = mutableListOf<GraphVertexData>()
        val vertexEntityMap = mutableMapOf<String, VisualEntity>()
        val visualKeyToVertexKey = mutableMapOf<String, String>()

        subScene.entities.forEach { entity ->
            val elementId = entity.elementId
            require(elementId.startsWith("vertex:")) {
                "Graph entity elementId must start with 'vertex:', got '$elementId'"
            }
            val vertexKey = elementId.removePrefix("vertex:")

            val expectedVisualKey = "graph:vertex:$vertexKey"
            require(entity.visualKey == expectedVisualKey) {
                "Graph entity visualKey '${entity.visualKey}' does not match expected '$expectedVisualKey'"
            }

            if (entity.slotKey.isNotBlank()) {
                val expectedSlot = "graph_slot:$vertexKey"
                require(entity.slotKey == expectedSlot) {
                    "Graph entity slotKey '${entity.slotKey}' does not match expected '$expectedSlot'"
                }
            }

            require(!vertexEntityMap.containsKey(vertexKey)) {
                "Duplicate vertex geometry key '$vertexKey' found in sub-scene entities"
            }

            vertices.add(GraphVertexData(key = vertexKey, label = entity.dataValue))
            vertexEntityMap[vertexKey] = entity
            visualKeyToVertexKey[expectedVisualKey] = vertexKey
        }

        val edges = mutableListOf<GraphEdgeData>()
        val edgeConnectionMap = mutableMapOf<String, VisualConnection>()

        subScene.connections.forEach { conn ->
            require(conn.fromVisualKey.startsWith("graph:vertex:")) {
                "Connection fromVisualKey '${conn.fromVisualKey}' must start with 'graph:vertex:'"
            }
            require(conn.toVisualKey.startsWith("graph:vertex:")) {
                "Connection toVisualKey '${conn.toVisualKey}' must start with 'graph:vertex:'"
            }

            val fromKey = visualKeyToVertexKey[conn.fromVisualKey]
            val toKey = visualKeyToVertexKey[conn.toVisualKey]

            if (fromKey != null && toKey != null) {
                require(!edgeConnectionMap.containsKey(conn.connectionKey)) {
                    "Duplicate edge connectionKey '${conn.connectionKey}' found in sub-scene connections"
                }
                edges.add(
                    GraphEdgeData(
                        key = conn.connectionKey,
                        fromVertexKey = fromKey,
                        toVertexKey = toKey,
                        directed = conn.directed
                    )
                )
                edgeConnectionMap[conn.connectionKey] = conn
            }
        }

        return GraphSceneAdapterResult(
            vertices = vertices,
            edges = edges,
            vertexEntityMap = vertexEntityMap,
            edgeConnectionMap = edgeConnectionMap
        )
    }
}
