# Linked Lists: Nodes and the Head

A **singly linked list** is a linear data structure where elements are stored in individual objects called **nodes**. Unlike an array, which stores elements in contiguous memory slots, a linked list connects its nodes using **pointers** or **references**.

Linked lists are useful when a collection grows and shrinks frequently, because new nodes can be allocated individually without needing a single contiguous block of memory.

## What Is a Node?

Each node in a singly linked list consists of two primary components:
1. **Data**: The value stored in the node (such as an integer, string, or object).
2. **Next Pointer / Reference**: A link pointing to the next node in the list.

The first node in the list is referenced by a pointer called the **`head`**. The last node's `next` pointer points to `nullptr` (or `null` / `None`), signaling the end of the list.

## Anatomy of a Linked List

Consider a list containing three values: `12`, `7`, and `19`.

```text
head → [12 | next] → [7 | next] → [19 | nullptr]
```

Let's break down each part of this diagram:
- **`head`**: A reference variable storing the memory address of the first node (`[12 | next]`).
- **First Node (`[12 | next]`)**: Holds data value `12`. Its `next` field points to the node holding `7`.
- **Second Node (`[7 | next]`)**: Holds data value `7`. Its `next` field points to the node holding `19`.
- **Third Node (`[19 | nullptr]`)**: The tail node. Holds value `19`. Its `next` field is `nullptr`, indicating there are no further nodes.

### Representation of Special Cases

- **Empty List**: The `head` pointer equals `nullptr` (or `null`), meaning the list contains zero nodes.
  ```text
  head → nullptr
  ```
- **One-Node List**: The `head` points to a single node whose `next` pointer is `nullptr`.
  ```text
  head → [12 | nullptr]
  ```

## Linked Lists vs. Arrays: Key Conceptual Differences

While both arrays and linked lists store ordered sequences of items, they manage memory and operations differently:

- **Memory Layout**: Array elements occupy contiguous (adjacent) memory slots. Linked-list nodes do not need to occupy contiguous memory locations; each node can be allocated anywhere in available memory.
- **Element Access**: Arrays calculate exact element memory addresses directly using an index formula, allowing instant `O(1)` access. Linked lists must follow `next` references sequentially from the `head` node, taking `O(n)` time to reach a specific position.
- **Dynamic Growth**: Dynamic arrays automatically grow when full, but resizing requires allocating a new memory block and copying existing elements (`O(n)` for that resize). Linked lists can add individual nodes without moving existing nodes.
- **Memory Overhead**: Array elements do not each need a `next` reference. However, a dynamic-array container may still store metadata such as its size and capacity. Linked lists incur additional per-node memory and link-management overhead because every node must store its `next` pointer alongside its data value.

## Code Example: Building a Linked List

The following examples define a `Node` class/structure and connect three nodes holding values `12`, `7`, and `19`.

```pseudocode
// Pseudocode definition of a Node
CLASS Node
    data: INTEGER
    next: Node pointer

    CONSTRUCTOR(val)
        data = val
        next = NULL
    END CONSTRUCTOR
END CLASS

// Creating nodes and linking them
head = NEW Node(12)
head.next = NEW Node(7)
head.next.next = NEW Node(19)
```
```cpp
#include <iostream>

struct Node {
    int data;
    Node* next;

    Node(int val) : data(val), next(nullptr) {}
};

int main() {
    // Creating nodes and connecting pointers
    Node* head = new Node(12);
    head->next = new Node(7);
    head->next->next = new Node(19);

    // Accessing values by following next pointers
    std::cout << head->data << std::endl;         // Prints 12
    std::cout << head->next->data << std::endl;   // Prints 7
    std::cout << head->next->next->data << std::endl; // Prints 19

    return 0;
}
```
```java
public class LinkedListExample {
    static class Node {
        int data;
        Node next;

        Node(int val) {
            this.data = val;
            this.next = null;
        }
    }

    public static void main(String[] args) {
        // Creating nodes and connecting object references
        Node head = new Node(12);
        head.next = new Node(7);
        head.next.next = new Node(19);

        // Accessing values by following next references
        System.out.println(head.data);            // Prints 12
        System.out.println(head.next.data);       // Prints 7
        System.out.println(head.next.next.data);  // Prints 19
    }
}
```
```python
class Node:
    def __init__(self, data):
        self.data = data
        self.next = None

# Creating nodes and connecting object references
head = Node(12)
head.next = Node(7)
head.next.next = Node(19)

# Accessing values by following next references
print(head.data)       # Prints 12
print(head.next.data)  # Prints 7
print(head.next.next.data) # Prints 19
```

### Language Differences in References

- **C++**: Uses explicit raw memory pointers (`Node*`). The `nullptr` value represents a null pointer.
- **Java**: Uses implicit object references. Uninitialized or terminating links are set to `null`.
- **Python**: Uses object variables and references. Terminating links are set to `None`.

In all four languages, the underlying conceptual model remains identical: a `head` variable holds a reference to the first node, and each node links to the next.

## Summary

A singly linked list stores data in individual **nodes** connected sequentially by pointers or references. The list is accessed starting at the **`head`**, and the sequence terminates when a node's `next` pointer equals `nullptr` (or `null`).
