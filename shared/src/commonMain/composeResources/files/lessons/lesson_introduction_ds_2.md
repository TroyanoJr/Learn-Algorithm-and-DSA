# Choosing How to Organize Data

Different data structures organize values in different ways. That organization makes some operations natural and others less convenient. Choose based on what the program needs to do and the order in which it should handle values.

## Four Common Structures

- **Array**: Keeps items in an ordered sequence, each at a numbered index. It is useful when you need to visit items in order or reach one by its position. Inserting or removing an item in the middle while preserving the order may require moving later items.
- **Linked list**: Stores items in nodes, where each node refers to the next one. You follow these links to visit items in order. Inserting or removing a node can be convenient when you already have a reference to the relevant position. However, finding an item by its index usually requires walking through earlier nodes.
- **Stack**: Gives access to the most recently added item first: last in, first out (LIFO). This fits actions such as undoing the latest change. The top item is easy to use, while reaching an older item is not the stack's intended operation.
- **Queue**: Gives access to the earliest added item first: first in, first out (FIFO). This fits waiting lines and jobs that should be handled in arrival order. New items join at the back, and the next item is taken from the front.

These are conceptual descriptions. The same structure can be implemented in different ways, and the exact cost of an operation depends on that implementation.

## Choosing for a Waiting Line

Imagine an office that serves students in the order they arrive. A queue is a natural fit: each arriving student joins the back, and the next student to arrive is served from the front. A stack would serve the newest arrival first, reversing the intended order. An array or linked list could store the students too, but the program would still need to enforce the same front-to-back rule.

The choice depends on the required behavior. If the program instead needed to jump to a student by a known position, an array might be more convenient. If it needed to undo the most recent action, a stack would fit better. No one structure is best for every situation.

## Summary

Arrays and linked lists keep sequences of items, while stacks and queues add rules about which item is handled next. Consider how the program needs to access, add, remove, and order its data before choosing a structure.
