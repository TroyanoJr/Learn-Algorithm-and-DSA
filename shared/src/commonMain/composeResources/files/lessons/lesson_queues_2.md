# Queue Operations and Complexity

The queue interface centers on adding at one end and removing at the other. `enqueue(x)` adds `x` at the rear, `dequeue()` removes and returns the front item, and `front()` (often named `peek()`) reads the front without removing it. `isEmpty()` and `size()` report queue state.

## Key Concepts

- Enqueue and dequeue preserve FIFO order.
- `dequeue` and `front` require a non-empty queue; a fixed-capacity queue may reject enqueue when full.
- With a linked queue or a circular array tracking front and size, enqueue, dequeue, and front are O(1).

## Operations and edge cases

A dequeue from an empty queue is **underflow**. A fixed-size buffer may also be full when enqueue is attempted; it should report overflow or apply a documented policy. A plain array that shifts items after each dequeue takes O(n), so efficient array queues advance a front index or use a circular buffer.

### Code Example

Each implementation enqueues 7 and 4, checks the front, and removes 7. Empty behavior varies by API and is handled before access.

```pseudocode
enqueue(queue, value): add value at rear
dequeue(queue):
    if isEmpty(queue): report underflow
    value = front item
    remove front item
    return value
front(queue):
    if isEmpty(queue): report empty
    return front item
```
```cpp
#include <queue>
#include <stdexcept>
std::queue<int> values;
values.push(7); values.push(4);
if (values.empty()) throw std::underflow_error("empty queue");
int observed = values.front();
int removed = values.front(); values.pop();
```
```java
import java.util.ArrayDeque;
import java.util.Queue;
Queue<Integer> values = new ArrayDeque<>();
values.add(7); values.add(4);
Integer observed = values.peek();
Integer removed = values.poll(); // null if empty
```
```python
from collections import deque
values = deque([7, 4])
if not values:
    raise IndexError("empty queue")
observed = values[0]
removed = values.popleft()
```

## Complexity

When front and rear positions are maintained, enqueue and dequeue take **O(1)** time; front, isEmpty, and size are also O(1). In a circular array, indices wrap rather than shifting elements. In a linked queue, a rear pointer makes insertion O(1) and a front pointer makes removal O(1). Without these maintained positions, traversal or shifting can make an operation O(n).

The queue stores n items, so space is **O(n)**. A fixed-capacity queue has bounded allocated storage; a linked queue allocates nodes as needed.

## Summary

Efficient queues maintain both ends. Enqueue, dequeue, and front are O(1) with a circular array or linked queue; empty and full behavior must be defined.
