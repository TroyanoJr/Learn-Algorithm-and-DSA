# Topological Sorting

A **directed acyclic graph (DAG)** has directed edges and no directed cycle. A **topological ordering** lists every vertex so that each edge points from an earlier vertex to a later one. This is useful for course prerequisites, build steps, and other dependency schedules. An ordering exists only if the directed graph is acyclic.

## Kahn's Algorithm

Kahn's algorithm repeatedly removes a vertex whose **in-degree** is zero (it has no remaining prerequisites). It uses a queue of available vertices, appends each removed vertex to the output, and decreases the in-degree of its outgoing neighbors.

Use edges `A→C`, `B→C`, `B→D`, `C→E`, `D→E`. Initial in-degrees are `A:0, B:0, C:2, D:1, E:2`. Start with queue `[A,B]` (alphabetical order for this trace):

- Remove A; C's in-degree changes from 2 to 1. Queue: `[B]`.
- Remove B; C changes to 0 and D changes to 0. Enqueue C then D. Queue: `[C,D]`.
- Remove C; E changes from 2 to 1. Queue: `[D]`.
- Remove D; E changes to 0. Queue: `[E]`.
- Remove E. Output: `A, B, C, D, E`.

Other valid orderings can exist. If fewer than V vertices are output, some vertices remain in a directed cycle, so no topological ordering exists.

### Code Example: Kahn's Topological Sort

Each version computes in-degrees from the same directed edges, initially queues zero-in-degree vertices in alphabetical order, and produces `A, B, C, D, E`. Other valid topological orders may also exist.

```pseudocode
compute each vertex's in-degree
queue = all vertices with in-degree 0
order = []
while queue is not empty
    v = removeFront(queue)
    append v to order
    for each edge v -> u
        decrease in-degree[u]
        if in-degree[u] == 0
            addBack(queue, u)
        end if
    end for
end while
if length(order) < V: report directed cycle
else: return order
```
```cpp
#include <iostream>
#include <queue>
#include <vector>

int main() {
    std::vector<std::vector<int>> graph{{2},{2,3},{4},{4},{}};
    std::vector<int> indegree(5, 0);
    for (const auto& neighbors : graph)
        for (int u : neighbors) ++indegree[u];
    std::queue<int> ready;
    for (int v = 0; v < 5; ++v) if (indegree[v] == 0) ready.push(v);
    std::vector<int> order;
    while (!ready.empty()) {
        int v = ready.front(); ready.pop();
        order.push_back(v);
        for (int u : graph[v]) if (--indegree[u] == 0) ready.push(u);
    }
    if (order.size() != graph.size()) {
        std::cout << "cycle\n";
    } else {
        for (int v : order) std::cout << char('A' + v) << ' ';
    }
}
```
```java
import java.util.*;
class TopologicalSortExample {
    public static void main(String[] args) {
        int[][] graph = {{2},{2,3},{4},{4},{}};
        int[] indegree = new int[5];
        for (int[] neighbors : graph)
            for (int u : neighbors) indegree[u]++;
        Queue<Integer> ready = new ArrayDeque<>();
        for (int v = 0; v < 5; v++) if (indegree[v] == 0) ready.add(v);
        List<Integer> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            int v = ready.remove();
            order.add(v);
            for (int u : graph[v]) if (--indegree[u] == 0) ready.add(u);
        }
        if (order.size() != graph.length) {
            System.out.println("cycle");
        } else {
            for (int v : order) System.out.print((char)('A' + v) + " ");
        }
    }
}
```
```python
from collections import deque

graph = {"A": ["C"], "B": ["C", "D"], "C": ["E"],
         "D": ["E"], "E": []}
indegree = {vertex: 0 for vertex in graph}
for neighbors in graph.values():
    for neighbor in neighbors:
        indegree[neighbor] += 1
ready = deque(vertex for vertex in graph if indegree[vertex] == 0)
order = []
while ready:
    vertex = ready.popleft()
    order.append(vertex)
    for neighbor in graph[vertex]:
        indegree[neighbor] -= 1
        if indegree[neighbor] == 0:
            ready.append(neighbor)

if len(order) != len(graph):
    print("cycle")
else:
    print(order)  # ['A', 'B', 'C', 'D', 'E']
```

With adjacency lists and a queue, every vertex and edge is processed once: `O(V + E)` time and `O(V)` auxiliary space.

## Summary

Topological sorting orders dependencies in a DAG. Kahn's algorithm repeatedly schedules zero-in-degree vertices; producing fewer than V vertices reveals a directed cycle and means no topological order exists.
