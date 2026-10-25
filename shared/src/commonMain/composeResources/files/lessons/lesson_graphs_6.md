# Shortest Paths in Unweighted Graphs

In an unweighted graph, a shortest path means the path with the **fewest edges**. BFS can recover such a path by recording each vertex's predecessor when it is first discovered. First discovery is shortest because BFS explores vertices layer by layer.

Use the undirected graph with edges `{AB, AC, BD, CD, DE}` and start at A. Examine neighbors alphabetically.

| Discovered vertex | Distance from A | Predecessor |
|---|---:|---|
| A | 0 | — |
| B | 1 | A |
| C | 1 | A |
| D | 2 | B |
| E | 3 | D |

When BFS processes C, D is already discovered through B at distance 2, so its predecessor stays B. To reconstruct the route to E, follow predecessors backward: `E ← D ← B ← A`. Reverse that sequence to get `A-B-D-E`, which has three edges. `A-C-D-E` is another equally short path; the chosen one depends on neighbor order.

### Code Example: BFS Distances and Predecessors

Each version uses the same undirected adjacency lists, starts at A, marks a vertex when enqueuing it, and records distance and predecessor on first discovery. Distances in A-to-E order are `[0, 1, 1, 2, 3]`; with alphabetical neighbors, the reconstructed path to E is `A-B-D-E`.

**Pseudocode**

```pseudocode
distance[start] = 0
predecessor[start] = none
queue = [start]
while queue is not empty
    v = removeFront(queue)
    for each neighbor u of v in graph[v]
        if u has no distance
            distance[u] = distance[v] + 1
            predecessor[u] = v
            addBack(queue, u)
        end if
    end for
end while
```

**C++**

```cpp
#include <algorithm>
#include <iostream>
#include <queue>
#include <vector>

int main() {
    std::vector<std::vector<int>> graph{{1,2},{0,3},{0,3},{1,2,4},{3}};
    std::vector<int> distance(5, -1), predecessor(5, -1);
    std::queue<int> pending;
    distance[0] = 0;
    pending.push(0);
    while (!pending.empty()) {
        int v = pending.front(); pending.pop();
        for (int u : graph[v]) if (distance[u] == -1) {
            distance[u] = distance[v] + 1;
            predecessor[u] = v;
            pending.push(u);
        }
    }
    for (int d : distance) std::cout << d << ' ';
    std::vector<int> path;
    for (int v = 4; v != -1; v = predecessor[v]) path.push_back(v);
    std::reverse(path.begin(), path.end());
    std::cout << "\nPath: ";
    for (int i = 0; i < static_cast<int>(path.size()); ++i)
        std::cout << char('A' + path[i]) << (i + 1 == static_cast<int>(path.size()) ? '\n' : '-');
}
```

**Java**

```java
import java.util.*;
class UnweightedShortestPath {
    public static void main(String[] args) {
        int[][] graph = {{1,2},{0,3},{0,3},{1,2,4},{3}};
        int[] distance = new int[5], predecessor = new int[5];
        Arrays.fill(distance, -1);
        Arrays.fill(predecessor, -1);
        Queue<Integer> pending = new ArrayDeque<>();
        distance[0] = 0;
        pending.add(0);
        while (!pending.isEmpty()) {
            int v = pending.remove();
            for (int u : graph[v]) if (distance[u] == -1) {
                distance[u] = distance[v] + 1;
                predecessor[u] = v;
                pending.add(u);
            }
        }
        System.out.println(Arrays.toString(distance));
        List<Integer> path = new ArrayList<>();
        for (int v = 4; v != -1; v = predecessor[v]) path.add(v);
        Collections.reverse(path);
        System.out.print("Path: ");
        for (int i = 0; i < path.size(); i++)
            System.out.print((char)('A' + path.get(i)) + (i + 1 == path.size() ? "\n" : "-"));
    }
}
```

**Python**

```python
from collections import deque

graph = {"A": ["B", "C"], "B": ["A", "D"], "C": ["A", "D"],
         "D": ["B", "C", "E"], "E": ["D"]}
distance = {vertex: None for vertex in graph}
predecessor = {vertex: None for vertex in graph}
distance["A"] = 0
pending = deque(["A"])
while pending:
    vertex = pending.popleft()
    for neighbor in graph[vertex]:
        if distance[neighbor] is None:
            distance[neighbor] = distance[vertex] + 1
            predecessor[neighbor] = vertex
            pending.append(neighbor)

path = []
vertex = "E"
while vertex is not None:
    path.append(vertex)
    vertex = predecessor[vertex]
path.reverse()
print([distance[v] for v in graph])  # [0, 1, 1, 2, 3]
print("Path:", "-".join(path))  # Path: A-B-D-E
```

If a target has no recorded distance, it is unreachable from the start. Otherwise follow predecessor links from the target to the start and reverse the list. The BFS work with adjacency lists is `O(V + E)` time and `O(V)` space; storing predecessors and distances also uses `O(V)`.

## Summary

BFS distances count edges, and predecessor links turn those distances into an actual shortest path. This guarantee applies to unweighted graphs (or graphs where every edge has equal cost).
