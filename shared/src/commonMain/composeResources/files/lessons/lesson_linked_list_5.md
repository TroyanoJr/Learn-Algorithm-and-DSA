# Performance, Singly vs. Doubly Linked Lists, and Review

Throughout this module, you learned how singly linked lists connect data in individual nodes using pointer references. To conclude the topic, this lesson compares the performance profiles of arrays and linked lists, introduces doubly linked lists, and reviews the entire five-lesson learning sequence.

## Singly vs. Doubly Linked Lists

In a **singly linked list**, each node contains a data value and a single `next` pointer pointing forward to the subsequent node. Traversal flows strictly in one direction from `head` to tail.

In a **doubly linked list**, each node contains two pointer references:
- **`next`**: Points to the subsequent node.
- **`prev`**: Points to the preceding node.

The doubly linked list also maintains both a **`head`** reference (pointing to the first node) and a **`tail`** reference (pointing to the last node).

### Advantages of Doubly Linked Lists
- **Bidirectional Traversal:** You can traverse forward from `head` or backward from `tail`.
- **O(1) Unlinking:** If you hold a direct pointer to an interior node, you can unlink it in O(1) time using its `prev` reference without traversing from `head` to find its predecessor.

### Trade-offs of Doubly Linked Lists
- **Higher Memory Overhead:** Every node must store two pointer addresses (`next` and `prev`) alongside its data value, nearly doubling pointer memory overhead compared to a singly linked list.
- **Link Updates:** A doubly linked list maintains both `next` and `prev` links, and the number of links to update depends on the operation and position. For example, removing an interior node when the node and its neighbors are already known requires updating the neighboring nodes' links.

## Linked Lists vs. Arrays: Performance Breakdown

Comparing time complexity clarifies why algorithm selection depends on operation frequency:

### Element Access and Search
- **Access by Index:** Arrays compute memory offsets directly in O(1) constant time. Linked lists must follow pointer chains sequentially, taking O(n) worst-case time to reach position k.
- **Linear Search:** Both arrays and linked lists inspect elements sequentially, taking O(n) worst-case time.

### Insertion and Removal
- **Head Operations:** Linked lists insert or remove at `head` in O(1) constant time without moving other elements. Arrays can shift n existing elements for insertion at the beginning and n - 1 remaining elements for removal of the first element. Both take O(n) time because the work grows linearly with the number of elements.
- **Operations After Known Node:** Once a pointer to node `prev` is known, inserting or unlinking the adjacent node takes O(1) time in a linked list. Finding that node by position or value first takes O(n) traversal time.
- **Tail Operations:** Appending to a singly linked list with only a `head` reference takes O(n) time to reach the end. With a maintained `tail` reference, appending takes O(1) constant time.

## When to Choose Which Structure

- **Choose an Array when:** You need fast O(1) index lookup, predictable memory footprint with low pointer overhead, or cache-friendly sequential memory reads.
- **Choose a Linked List when:** You perform frequent O(1) insertions or deletions at the beginning of the collection, or when you need dynamic growth without allocating contiguous memory blocks.

## Five-Lesson Module Summary

1. **Nodes and the Head:** Nodes store data and a `next` reference; `head` points to the start and `null` marks the tail.
2. **Traversal, Search, and Access:** Sequential pointer navigation from `head` takes O(n) time for search and position access.
3. **Inserting Nodes:** Safe pointer updates require setting `newNode.next = prev.next` before `prev.next = newNode`.
4. **Removing Nodes:** Removing `head` takes O(1) time; removing interior nodes requires bypassing via a predecessor (`prev.next = curr.next`).
5. **Performance & Doubly Linked Lists:** Doubly linked lists add a `prev` reference for O(1) predecessor access at the cost of higher memory overhead per node.
