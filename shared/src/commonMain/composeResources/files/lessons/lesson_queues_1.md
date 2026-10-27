# Queue Fundamentals and FIFO

A **queue** stores items in arrival order. New items join the **rear** and leave from the **front**. This is **first in, first out (FIFO)**: the earliest item added is removed first. A checkout line is a familiar example.

## Key Concepts

- **Enqueue** adds at the rear.
- **Dequeue** removes and returns the front item.
- **Front/peek** observes the next item without removing it.
- An empty queue has no front item; check before reading or removing.

## Guided trace

| Operation | Queue (front → rear) | Result |
|---|---|---|
| start | `[]` | — |
| enqueue A | `[A]` | — |
| enqueue B | `[A, B]` | — |
| enqueue C | `[A, B, C]` | — |
| enqueue D | `[A, B, C, D]` | — |
| enqueue E | `[A, B, C, D, E]` | — |
| dequeue | `[B, C, D, E]` | `A` |
| enqueue F | `[B, C, D, E, F]` | — |
| dequeue | `[C, D, E, F]` | `B` |

Items leave in arrival order. Removing the newest item first would be stack behavior, not queue behavior.

### Code Example

Each version enqueues `first` and `second`, observes the front, removes the first item, then observes the next front.

```pseudocode
queue = empty queue
enqueue(queue, "first")
enqueue(queue, "second")
print(front(queue))       // first
print(dequeue(queue))     // first
print(front(queue))       // second
```
```cpp
#include <iostream>
#include <queue>
#include <string>
int main() {
    std::queue<std::string> values;
    values.push("first"); values.push("second");
    std::cout << values.front() << '\n';
    std::cout << values.front() << '\n'; values.pop();
    std::cout << values.front() << '\n';
}
```
```java
import java.util.ArrayDeque;
import java.util.Queue;
class QueueExample {
    public static void main(String[] args) {
        Queue<String> values = new ArrayDeque<>();
        values.add("first"); values.add("second");
        System.out.println(values.peek());
        System.out.println(values.remove());
        System.out.println(values.peek());
    }
}
```
```python
from collections import deque
values = deque(["first", "second"])
print(values[0])
print(values.popleft())
print(values[0])
```

The queue is now `[second]`. Check for emptiness before an operation that requires a front item; standard libraries differ in how they report an empty removal.

## Summary

Queues preserve arrival order: enqueue at the rear and dequeue from the front to serve items FIFO.
