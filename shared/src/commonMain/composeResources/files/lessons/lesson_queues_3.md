# Array and Linked Queue Implementations

A queue can use a circular array or linked nodes. Both store the front and rear so enqueue and dequeue do not shift or scan all items.

## Key Concepts

- A **circular array** reuses freed slots by wrapping indices to the start.
- A **linked queue** keeps references to front and rear nodes.
- Both support O(1) enqueue, dequeue, and front when those positions are maintained.

## Circular-array queue

Track `front` (the next slot to remove) and `size`. The rear insertion slot is `(front + size) mod capacity`. Dequeue advances front modulo capacity and decreases size. A fixed-capacity queue must handle full and empty conditions explicitly.

### Code Example

**Illustrative trace:** For a queue with capacity four, enqueue 8 and 5, dequeue 8, then enqueue 9; the front remains 5.

```pseudocode
create(capacity): data = new array[capacity]; front = 0; size = 0
enqueue(x):
    if size == capacity: error "full queue"
    rear = (front + size) mod capacity
    data[rear] = x
    size = size + 1
dequeue():
    if size == 0: error "empty queue"
    value = data[front]
    front = (front + 1) mod capacity
    size = size - 1
    return value
frontValue():
    if size == 0: error "empty queue"
    return data[front]
```
```cpp
#include <stdexcept>
#include <vector>
class CircularQueue {
    std::vector<int> data; int head = 0; int size = 0;
public:
    explicit CircularQueue(int capacity) {
        if (capacity <= 0) throw std::invalid_argument("capacity must be positive");
        data.resize(capacity);
    }
    void enqueue(int x) {
        if (size == static_cast<int>(data.size())) throw std::overflow_error("full queue");
        data[(head + size) % data.size()] = x; ++size;
    }
    int dequeue() {
        if (size == 0) throw std::underflow_error("empty queue");
        int x = data[head]; head = (head + 1) % data.size(); --size; return x;
    }
    int front() const {
        if (size == 0) throw std::underflow_error("empty queue");
        return data[head];
    }
};
```
```java
class CircularQueue {
    private final int[] data; private int head = 0, size = 0;
    CircularQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        data = new int[capacity];
    }
    void enqueue(int x) {
        if (size == data.length) throw new IllegalStateException("full queue");
        data[(head + size) % data.length] = x; size++;
    }
    int dequeue() {
        if (size == 0) throw new IllegalStateException("empty queue");
        int x = data[head]; head = (head + 1) % data.length; size--; return x;
    }
    int front() {
        if (size == 0) throw new IllegalStateException("empty queue");
        return data[head];
    }
}
```
```python
class CircularQueue:
    def __init__(self, capacity):
        if capacity <= 0: raise ValueError("capacity must be positive")
        self.data = [None] * capacity; self.head = 0; self.size = 0
    def enqueue(self, x):
        if self.size == len(self.data): raise OverflowError("full queue")
        self.data[(self.head + self.size) % len(self.data)] = x; self.size += 1
    def dequeue(self):
        if self.size == 0: raise IndexError("empty queue")
        x = self.data[self.head]; self.head = (self.head + 1) % len(self.data); self.size -= 1
        return x
    def front(self):
        if self.size == 0: raise IndexError("empty queue")
        return self.data[self.head]
```

Modulo reuses freed slots. The `size` field distinguishes empty from full when head and rear coincide.

## Linked queue

A linked queue keeps `front` and `rear` pointers. Enqueue links after rear (or sets both pointers when empty); dequeue advances front. When the last node is removed, rear must also become null. These pointer updates are O(1), with one link per node.

### Code Example

```pseudocode
enqueue(x):
    node = Node(x)
    if rear == null: front = node
    else: rear.next = node
    rear = node
dequeue():
    if front == null: error "empty queue"
    value = front.value
    front = front.next
    if front == null: rear = null
    return value
```
```cpp
#include <stdexcept>
struct Node { int value; Node* next = nullptr; };
class LinkedQueue {
    Node* head = nullptr; Node* tail = nullptr;
public:
    void enqueue(int x) {
        Node* n = new Node{x, nullptr};
        if (tail == nullptr) head = n; else tail->next = n;
        tail = n;
    }
    int dequeue() {
        if (head == nullptr) throw std::underflow_error("empty queue");
        Node* old = head; int x = old->value; head = old->next;
        if (head == nullptr) tail = nullptr;
        delete old; return x;
    }
};
```
```java
class Node { int value; Node next; Node(int x) { value = x; } }
class LinkedQueue {
    private Node head, tail;
    void enqueue(int x) {
        Node n = new Node(x);
        if (tail == null) head = n; else tail.next = n;
        tail = n;
    }
    int dequeue() {
        if (head == null) throw new IllegalStateException("empty queue");
        int x = head.value; head = head.next;
        if (head == null) tail = null;
        return x;
    }
}
```
```python
class Node:
    def __init__(self, value): self.value, self.next = value, None
class LinkedQueue:
    def __init__(self): self.head = self.tail = None
    def enqueue(self, x):
        n = Node(x)
        if self.tail is None: self.head = n
        else: self.tail.next = n
        self.tail = n
    def dequeue(self):
        if self.head is None: raise IndexError("empty queue")
        x = self.head.value; self.head = self.head.next
        if self.head is None: self.tail = None
        return x
```

## Choosing

A circular array offers compact storage and known capacity but needs a full-queue policy. A linked queue grows as nodes are allocated and has link overhead. Both use O(n) space and provide O(1) operations at maintained ends.

## Summary

Circular arrays reuse slots with modulo arithmetic; linked queues connect nodes through front and rear references. Correctly updating size or both pointers handles wraparound and empty transitions.
