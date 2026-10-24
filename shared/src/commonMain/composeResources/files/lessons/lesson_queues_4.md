# Queue Applications and Circular Buffers

Queues model work that should be handled in arrival order. A printer can process submitted jobs FIFO, and a service system can serve waiting requests in order. Breadth-first search also uses a queue so the oldest discovered node is processed next.

## Key Concepts

- A work queue separates when work arrives from when it is processed.
- A **circular buffer** reuses a fixed array by wrapping the rear to the beginning after the last slot.
- When a buffer is full, choose a policy such as reject, wait, or overwrite; overwriting is unsuitable when every item must be preserved.

## Circular-buffer trace

For capacity 4, enqueue A, B, C, dequeue twice, then enqueue D and E:

| Operation | Logical queue (front → rear) | Front index | Next rear index |
|---|---|---:|---:|
| start | `[]` | 0 | 0 |
| enqueue A, B, C | `[A, B, C]` | 0 | 3 |
| dequeue A, B | `[C]` | 2 | 3 |
| enqueue D | `[C, D]` | 2 | 0 |
| enqueue E | `[C, D, E]` | 2 | 1 |

After the final enqueue, the rear wrapped to index 0 for E. Logical order remains C, D, E although storage occupies both ends of the array.

### Code Example

Each version adds two jobs and processes them in arrival order. The empty check prevents removal from an empty queue.

```pseudocode
jobs = empty queue
enqueue(jobs, "print report")
enqueue(jobs, "print receipt")
while not isEmpty(jobs):
    job = dequeue(jobs)
    print(job)
```
```cpp
#include <iostream>
#include <queue>
#include <string>
int main() {
    std::queue<std::string> jobs;
    jobs.push("print report"); jobs.push("print receipt");
    while (!jobs.empty()) { std::cout << jobs.front() << '\n'; jobs.pop(); }
}
```
```java
import java.util.ArrayDeque;
import java.util.Queue;
class JobQueue {
    public static void main(String[] args) {
        Queue<String> jobs = new ArrayDeque<>();
        jobs.add("print report"); jobs.add("print receipt");
        while (!jobs.isEmpty()) System.out.println(jobs.remove());
    }
}
```
```python
from collections import deque
jobs = deque(["print report", "print receipt"])
while jobs:
    print(jobs.popleft())
```

All versions print `print report` before `print receipt`. Circular buffers are useful for fixed-capacity streams such as recent samples or producer-consumer buffers; the full policy depends on whether old items may be discarded.

## Summary

Queues support fair arrival-order processing, including job scheduling and breadth-first search. Circular buffers apply FIFO while reusing bounded array storage.
