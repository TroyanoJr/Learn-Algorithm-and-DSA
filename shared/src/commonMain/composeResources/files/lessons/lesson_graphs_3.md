# Breadth-First Search (BFS)

**Breadth-First Search** explores a graph in layers. It visits the start vertex, then all its unvisited neighbors, then their unvisited neighbors. A **queue** preserves this first-in, first-out order. Mark each vertex visited when it is enqueued so it cannot be added repeatedly.

Use the undirected adjacency lists `A: [B,C]`, `B: [A,D]`, `C: [A,D]`, `D: [B,C,E]`, `E: [D]`. Start at A and examine neighbors alphabetically:

| Step | Action | Queue after action | Visited |
|---|---|---|---|
| 1 | Enqueue A | `[A]` | `{A}` |
| 2 | Remove A; enqueue B, C | `[B,C]` | `{A,B,C}` |
| 3 | Remove B; enqueue D | `[C,D]` | `{A,B,C,D}` |
| 4 | Remove C; D is already visited | `[D]` | `{A,B,C,D}` |
| 5 | Remove D; enqueue E | `[E]` | `{A,B,C,D,E}` |
| 6 | Remove E | `[]` | `{A,B,C,D,E}` |

The visit order is `A, B, C, D, E`. If neighbor order changes, the order within a layer may change too. In an **unweighted** graph, BFS discovers each vertex by a path with the fewest edges: all paths of length 1 are considered before length 2, and so on.

### Code Example: Breadth-First Search

The following versions all run BFS from A on the same graph, examine neighbors alphabetically, and produce `A, B, C, D, E`.

**Pseudocode**

```pseudocode
procedure BFS(graph, start)
    visited = {start}
    queue = [start]
    while queue is not empty
        v = removeFront(queue)
        visit(v)
        for each neighbor u of v in graph[v]
            if u is not in visited
                add u to visited
                addBack(queue, u)
            end if
        end for
    end while
end procedure
```

The C++ and Java versions store the vertices as indices A=0 through E=4, in the same alphabetical neighbor order as the Python lists.

**C++**

```cpp
#include <iostream>
#include <queue>
#include <vector>

int main() {
    std::vector<std::vector<int>> graph{{1,2},{0,3},{0,3},{1,2,4},{3}};
    std::vector<bool> visited(5, false);
    std::queue<int> pending;
    pending.push(0); visited[0] = true;
    while (!pending.empty()) {
        int v = pending.front(); pending.pop();
        std::cout << char('A' + v) << ' ';
        for (int u : graph[v]) if (!visited[u]) {
            visited[u] = true;
            pending.push(u);
        }
    }
}
```

**Java**

```java
import java.util.*;
class BfsExample {
    public static void main(String[] args) {
        int[][] graph = {{1,2},{0,3},{0,3},{1,2,4},{3}};
        boolean[] visited = new boolean[5];
        Queue<Integer> pending = new ArrayDeque<>();
        pending.add(0); visited[0] = true;
        while (!pending.isEmpty()) {
            int v = pending.remove();
            System.out.print((char)('A' + v) + " ");
            for (int u : graph[v]) if (!visited[u]) {
                visited[u] = true;
                pending.add(u);
            }
        }
    }
}
```

**Python**

```python
from collections import deque

def bfs(graph, start):
    visited = {start}
    queue = deque([start])
    order = []
    while queue:
        vertex = queue.popleft()
        order.append(vertex)
        for neighbor in graph[vertex]:  # alphabetical neighbor order
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    return order

graph = {"A": ["B", "C"], "B": ["A", "D"], "C": ["A", "D"],
         "D": ["B", "C", "E"], "E": ["D"]}
print(bfs(graph, "A"))  # ['A', 'B', 'C', 'D', 'E']
```

With adjacency lists, each vertex is enqueued once and each edge is examined at most twice for an undirected graph, so time is `O(V + E)` and auxiliary space is `O(V)`.

## Summary

BFS uses a queue to explore by distance in edges. It finds shortest paths by edge count in unweighted graphs, and with adjacency lists takes `O(V + E)` time.
