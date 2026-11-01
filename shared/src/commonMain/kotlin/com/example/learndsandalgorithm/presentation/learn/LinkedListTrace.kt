package com.example.learndsandalgorithm.presentation.learn

internal data class LinkedListTraversalStep(
    val currentNode: Int,
    val visitedNodes: Set<Int>,
    val title: String,
    val explanation: String,
    val result: String
)

internal data class LinkedListOperationStep(
    val title: String,
    val explanation: String,
    val order: List<String>,
    val links: Set<Pair<String, String>>,
    val head: String,
    val highlighted: Set<String>,
    val activeNode: String,
    val detachedNodes: List<String> = emptyList(),
    val badges: Map<String, String> = emptyMap()
)

internal object LinkedListTrace {
    val initialTraversalNodeValues = listOf("12", "7", "19", "45", "33")

    fun traversalSteps(scenario: String = "TRAVERSE"): List<LinkedListTraversalStep> = when (scenario) {
        "ACCESS" -> listOf(
            LinkedListTraversalStep(
                currentNode = 0,
                visitedNodes = setOf(0),
                title = "Access index 3: start at head",
                explanation = "Goal: get the node at index 3. A linked list has no direct indexing, so we start at head (index 0) and follow next pointers.",
                result = "Hops: 0"
            ),
            LinkedListTraversalStep(
                currentNode = 1,
                visitedNodes = setOf(0, 1),
                title = "Follow next to index 1",
                explanation = "Follow the next pointer from 12 to reach 7 at index 1.",
                result = "Hops: 1"
            ),
            LinkedListTraversalStep(
                currentNode = 2,
                visitedNodes = setOf(0, 1, 2),
                title = "Follow next to index 2",
                explanation = "Follow the next pointer from 7 to reach 19 at index 2.",
                result = "Hops: 2"
            ),
            LinkedListTraversalStep(
                currentNode = 3,
                visitedNodes = setOf(0, 1, 2, 3),
                title = "Index 3 reached",
                explanation = "Follow next from 19 to reach 45 at index 3. This is the requested index, so return 45. Access costs O(n): we had to walk the list.",
                result = "Result: 45 at index 3 (3 hops)"
            )
        )
        "FOUND" -> listOf(
            LinkedListTraversalStep(
                currentNode = 0,
                visitedNodes = setOf(0),
                title = "Search 45: compare head",
                explanation = "Compare the head value 12 with the target 45. They are not equal, so move to the next node.",
                result = "Comparisons: 1"
            ),
            LinkedListTraversalStep(
                currentNode = 1,
                visitedNodes = setOf(0, 1),
                title = "Compare 7",
                explanation = "7 is not equal to 45. Follow next.",
                result = "Comparisons: 2"
            ),
            LinkedListTraversalStep(
                currentNode = 2,
                visitedNodes = setOf(0, 1, 2),
                title = "Compare 19",
                explanation = "19 is not equal to 45. Follow next.",
                result = "Comparisons: 3"
            ),
            LinkedListTraversalStep(
                currentNode = 3,
                visitedNodes = setOf(0, 1, 2, 3),
                title = "Match found",
                explanation = "45 equals the target, so the search stops and returns this node (index 3). Search is O(n) in the worst case.",
                result = "Found: 45 at index 3 (4 comparisons)"
            )
        )
        "MISSING" -> listOf(
            LinkedListTraversalStep(
                currentNode = 0,
                visitedNodes = setOf(0),
                title = "Search 99: compare head",
                explanation = "Compare the head value 12 with the target 99. Not equal, so move on.",
                result = "Comparisons: 1"
            ),
            LinkedListTraversalStep(
                currentNode = 1,
                visitedNodes = setOf(0, 1),
                title = "Compare 7",
                explanation = "7 is not equal to 99. Follow next.",
                result = "Comparisons: 2"
            ),
            LinkedListTraversalStep(
                currentNode = 2,
                visitedNodes = setOf(0, 1, 2),
                title = "Compare 19",
                explanation = "19 is not equal to 99. Follow next.",
                result = "Comparisons: 3"
            ),
            LinkedListTraversalStep(
                currentNode = 3,
                visitedNodes = setOf(0, 1, 2, 3),
                title = "Compare 45",
                explanation = "45 is not equal to 99. Follow next.",
                result = "Comparisons: 4"
            ),
            LinkedListTraversalStep(
                currentNode = 4,
                visitedNodes = setOf(0, 1, 2, 3, 4),
                title = "End of list: not found",
                explanation = "33 is not equal to 99 and its next pointer is null, so 99 is not in the list. Every node was checked: O(n).",
                result = "Not found (5 comparisons)"
            )
        )
        else -> listOf(
            LinkedListTraversalStep(
                currentNode = 0,
                visitedNodes = setOf(0),
                title = "Start traversal at head (12)",
                explanation = "Begin traversal at head node (12) at index 0.",
                result = "Traversed: 1 node"
            ),
            LinkedListTraversalStep(
                currentNode = 1,
                visitedNodes = setOf(0, 1),
                title = "Follow next to node 7",
                explanation = "Follow next pointer from 12 to reach 7 at index 1.",
                result = "Traversed: 2 nodes"
            ),
            LinkedListTraversalStep(
                currentNode = 2,
                visitedNodes = setOf(0, 1, 2),
                title = "Follow next to node 19",
                explanation = "Follow next pointer from 7 to reach 19 at index 2.",
                result = "Traversed: 3 nodes"
            ),
            LinkedListTraversalStep(
                currentNode = 3,
                visitedNodes = setOf(0, 1, 2, 3),
                title = "Follow next to node 45",
                explanation = "Follow next pointer from 19 to reach 45 at index 3.",
                result = "Traversed: 4 nodes"
            ),
            LinkedListTraversalStep(
                currentNode = 4,
                visitedNodes = setOf(0, 1, 2, 3, 4),
                title = "Reach tail node 33",
                explanation = "Follow next pointer from 45 to reach 33. Its next pointer is null, completing traversal.",
                result = "Traversal complete (5 nodes)"
            )
        )
    }

    fun insertionSteps(scenario: String = "HEAD"): List<LinkedListOperationStep> = when (scenario) {
        "TAIL" -> listOf(
            LinkedListOperationStep(
                title = "1. Create New Node (55)",
                explanation = "Allocate a new node with data 55. Next pointer is null.",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19"),
                head = "12",
                highlighted = setOf("55"),
                activeNode = "55",
                detachedNodes = listOf("55"),
                badges = mapOf("55" to "NEW")
            ),
            LinkedListOperationStep(
                title = "2. Traverse to Tail",
                explanation = "Traverse from head through 12 and 7 until reaching node 19 whose next is null.",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19"),
                head = "12",
                highlighted = setOf("19"),
                activeNode = "19",
                badges = emptyMap()
            ),
            LinkedListOperationStep(
                title = "3. Link Tail to New Node",
                explanation = "Update node 19's next pointer from null to reference the new node (55). O(n) traversal required unless tail pointer is maintained.",
                order = listOf("12", "7", "19", "55"),
                links = setOf("12" to "7", "7" to "19", "19" to "55"),
                head = "12",
                highlighted = setOf("19", "55"),
                activeNode = "55",
                badges = emptyMap()
            )
        )
        "MIDDLE" -> listOf(
            LinkedListOperationStep(
                title = "1. Create New Node (42)",
                explanation = "Allocate a new node with data 42 to insert between 7 and 19.",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19"),
                head = "12",
                highlighted = setOf("42"),
                activeNode = "42",
                detachedNodes = listOf("42"),
                badges = mapOf("42" to "NEW")
            ),
            LinkedListOperationStep(
                title = "2. Point New Node to Successor",
                explanation = "Set new node's next pointer to node 7's current successor (node 19).",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19", "42" to "19"),
                head = "12",
                highlighted = setOf("7", "42"),
                activeNode = "42",
                detachedNodes = listOf("42"),
                badges = mapOf("42" to "NEW")
            ),
            LinkedListOperationStep(
                title = "3. Rewire Predecessor Next",
                explanation = "Update node 7's next pointer to reference the new node (42). Order becomes 7 → 42 → 19.",
                order = listOf("12", "7", "42", "19"),
                links = setOf("12" to "7", "42" to "19", "7" to "42"),
                head = "12",
                highlighted = setOf("7", "42"),
                activeNode = "42",
                badges = emptyMap()
            )
        )
        else -> listOf(
            LinkedListOperationStep(
                title = "1. Create New Node (99)",
                explanation = "Allocate a new node with data 99. Its next pointer starts as unassigned.",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19"),
                head = "12",
                highlighted = setOf("99"),
                activeNode = "99",
                detachedNodes = listOf("99"),
                badges = mapOf("99" to "NEW")
            ),
            LinkedListOperationStep(
                title = "2. Point New Node to Head",
                explanation = "Assign new node's next pointer to reference current head (node 12).",
                order = listOf("99", "12", "7", "19"),
                links = setOf("99" to "12", "12" to "7", "7" to "19"),
                head = "12",
                highlighted = setOf("99", "12"),
                activeNode = "99",
                badges = mapOf("99" to "NEW")
            ),
            LinkedListOperationStep(
                title = "3. Update Head Pointer",
                explanation = "Redirect head pointer to reference the new node (99). Insertion at head is O(1).",
                order = listOf("99", "12", "7", "19"),
                links = setOf("99" to "12", "12" to "7", "7" to "19"),
                head = "99",
                highlighted = setOf("99"),
                activeNode = "99",
                badges = emptyMap()
            )
        )
    }

    fun removalSteps(scenario: String = "HEAD"): List<LinkedListOperationStep> = when (scenario) {
        "TAIL" -> listOf(
            LinkedListOperationStep(
                title = "1. Traverse to Pre-Tail",
                explanation = "Traverse to node 7, which precedes the tail node (19).",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19"),
                head = "12",
                highlighted = setOf("7", "19"),
                activeNode = "7",
                detachedNodes = emptyList(),
                badges = mapOf("19" to "REMOVE")
            ),
            LinkedListOperationStep(
                title = "2. Unlink Tail",
                explanation = "Update node 7's next pointer to null, cutting off node 19.",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7"),
                head = "12",
                highlighted = setOf("7", "19"),
                activeNode = "7",
                detachedNodes = emptyList(),
                badges = mapOf("19" to "REMOVE")
            ),
            LinkedListOperationStep(
                title = "3. Detach Tail Node",
                explanation = "Node 19 is detached. O(n) traversal needed to find pre-tail in singly linked list.",
                order = listOf("12", "7"),
                links = setOf("12" to "7"),
                head = "12",
                highlighted = setOf("19"),
                activeNode = "7",
                detachedNodes = listOf("19"),
                badges = mapOf("19" to "REMOVED")
            )
        )
        "MIDDLE" -> listOf(
            LinkedListOperationStep(
                title = "1. Locate Predecessor",
                explanation = "Traverse to node 7, which precedes target node 19.",
                order = listOf("12", "7", "19", "45"),
                links = setOf("12" to "7", "7" to "19", "19" to "45"),
                head = "12",
                highlighted = setOf("7", "19"),
                activeNode = "7",
                detachedNodes = emptyList(),
                badges = mapOf("19" to "REMOVE")
            ),
            LinkedListOperationStep(
                title = "2. Bypass Target Node",
                explanation = "Rewire node 7's next pointer to point directly to node 45 (bypassing 19).",
                order = listOf("12", "7", "19", "45"),
                links = setOf("12" to "7", "7" to "45", "19" to "45"),
                head = "12",
                highlighted = setOf("7", "19", "45"),
                activeNode = "19",
                detachedNodes = emptyList(),
                badges = mapOf("19" to "REMOVE")
            ),
            LinkedListOperationStep(
                title = "3. Detach Target Node",
                explanation = "Node 19 is fully unlinked and detached. Deletion in middle is O(n) due to search.",
                order = listOf("12", "7", "45"),
                links = setOf("12" to "7", "7" to "45"),
                head = "12",
                highlighted = setOf("19"),
                activeNode = "45",
                detachedNodes = listOf("19"),
                badges = mapOf("19" to "REMOVED")
            )
        )
        else -> listOf(
            LinkedListOperationStep(
                title = "1. Identify Head Node",
                explanation = "Locate the current head node (12) to be removed.",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19"),
                head = "12",
                highlighted = setOf("12"),
                activeNode = "12",
                detachedNodes = emptyList(),
                badges = mapOf("12" to "REMOVE")
            ),
            LinkedListOperationStep(
                title = "2. Advance Head Pointer",
                explanation = "Redirect head pointer to node 12's successor (node 7).",
                order = listOf("12", "7", "19"),
                links = setOf("12" to "7", "7" to "19"),
                head = "7",
                highlighted = setOf("12", "7"),
                activeNode = "7",
                detachedNodes = emptyList(),
                badges = mapOf("12" to "REMOVE")
            ),
            LinkedListOperationStep(
                title = "3. Detach Old Head",
                explanation = "Node 12 is now unreferenced and freed from memory (via garbage collection in Java/Python or explicit deletion in C++). Removal at head is O(1).",
                order = listOf("7", "19"),
                links = setOf("7" to "19"),
                head = "7",
                highlighted = setOf("12"),
                activeNode = "7",
                detachedNodes = listOf("12"),
                badges = mapOf("12" to "REMOVED")
            )
        )
    }
}
