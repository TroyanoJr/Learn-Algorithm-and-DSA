# Array and Linked Stack Implementations

A stack is an abstract behavior; arrays and linked lists are two ways to store its items. In either design, keep the top at a position where insertion and removal do not require shifting or searching.

## Key Concepts

- An array stack commonly keeps its top at the last occupied index.
- A linked stack can keep its top at the head node.
- Both can push and pop in O(1) at a directly addressed top; arrays offer compact storage, while linked nodes need link and allocation overhead.

## Array-backed stack

Store items in a dynamic array and treat its last occupied index as the top. Push appends; pop removes the last item. Indexing at the end is direct. A dynamic array's push is **amortized O(1)**, although a resize may copy n elements and cost **O(n)**. A fixed array avoids resizing but must reject push when full. Arrays have compact storage and good cache locality.

### Code Example: Array-Backed Operations

```pseudocode
push(a, x): a.append(x)
pop(a):
    if a.length == 0: error "empty stack"
    return a.removeLast()
peek(a):
    if a.length == 0: error "empty stack"
    return a[a.length - 1]
```

```cpp
// std::vector<int> values; // dynamic array
values.push_back(8);        // push
int top = values.back();    // peek (only when not empty)
values.pop_back();          // pop (only when not empty)
```

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(8);
int top = stack.peek(); // null if empty
Integer removed = stack.poll(); // null if empty
```

```python
stack = []
stack.append(8)       # push
top = stack[-1]       # peek only after checking stack
removed = stack.pop() # pop only when non-empty
```

## Linked-list-backed stack

Keep a pointer to the head node as the top. Push creates a node whose `next` points to the old head, then makes it the new head. Pop saves the head value and advances the head. Both take O(1) time, including when the stack grows, provided allocation succeeds. Each item needs a node and link, so per-item memory overhead is higher and nodes are less contiguous.

### Code Example: Linked-List-Backed Operations

These implementations use the head node as the top: push links a node before the old head, and pop checks for emptiness before advancing the head and returning the value.

```pseudocode
push(value):
    node = Node(value)
    node.next = top
    top = node
pop():
    if top == null: error "empty stack"
    value = top.value
    top = top.next
    return value
```

```cpp
#include <stdexcept>

struct Node {
    int value;
    Node* next;
};

Node* top = nullptr;
void push(int value) {
    top = new Node{value, top};
}
int pop() {
    if (top == nullptr) throw std::underflow_error("empty stack");
    Node* oldTop = top;
    int value = oldTop->value;
    top = oldTop->next;
    delete oldTop;
    return value;
}
```
```java
class Node {
    int value;
    Node next;
    Node(int value, Node next) { this.value = value; this.next = next; }
}

class LinkedStack {
    private Node top;
    void push(int value) { top = new Node(value, top); }
    int pop() {
        if (top == null) throw new IllegalStateException("empty stack");
        int value = top.value;
        top = top.next;
        return value;
    }
}
```
```python
class Node:
    def __init__(self, value, next_node=None):
        self.value = value
        self.next = next_node

class LinkedStack:
    def __init__(self):
        self.top = None
    def push(self, value):
        self.top = Node(value, self.top)
    def pop(self):
        if self.top is None:
            raise IndexError("empty stack")
        value = self.top.value
        self.top = self.top.next
        return value
```

## Choosing

Use an array-backed stack when compact storage and locality matter and resizing (or a known capacity) is acceptable. Use a linked implementation when avoiding array resizing or accommodating variable growth is useful and node overhead is acceptable. Both use O(n) space; either is correct if it consistently enforces LIFO and defines empty/full behavior. In production, prefer a standard library stack/deque abstraction unless a specific representation is required.

## Summary

Array and linked-list representations can both implement a stack efficiently when the top is at the array end or linked head. Choose based on storage overhead, locality, and capacity needs.
