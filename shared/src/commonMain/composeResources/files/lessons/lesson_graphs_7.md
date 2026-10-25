# Dijkstra's Algorithm

For a weighted graph with **non-negative** edge weights, Dijkstra's algorithm finds shortest distances from a start vertex. It keeps a tentative distance for each vertex and repeatedly chooses the unprocessed vertex with the smallest tentative distance. From that vertex, it tries to improve (relax) each neighbor's distance.

Consider directed weighted edges `A→B (4)`, `A→C (1)`, `C→B (2)`, `B→D (1)`, `C→D (5)`. Start at A:

| Chosen vertex | Relaxations | Tentative distances `(A,B,C,D)` |
|---|---|---|
| Start | A is 0 | `(0,∞,∞,∞)` |
| A | B becomes 4; C becomes 1 | `(0,4,1,∞)` |
| C | B improves to 3; D becomes 6 | `(0,3,1,6)` |
| B | D improves to 4 | `(0,3,1,4)` |
| D | No outgoing improvements | `(0,3,1,4)` |

The order of processed vertices is A, C, B, D. The shortest route to D is `A-C-B-D`, total weight `1+2+1=4`.

**Standard Dijkstra is not correct when negative edge weights are present.** A vertex treated as finalized may later have a cheaper route through a negative edge. Use an algorithm designed for negative weights instead.

### Code Example: Dijkstra with a Min-Priority Queue

Each version uses the directed graph in the trace, starts at A, and stores a predecessor when a shorter distance is found. The final distances in A, B, C, D order are `[0, 3, 1, 4]`, with path `A-C-B-D` to D. All edge weights must be non-negative. Unreachable vertices keep the infinity value and have no predecessor. The C++ and Java examples use `MAX_VALUE / 4` as a sentinel, guard additions against overflow, and assume valid shortest distances are smaller than that sentinel.

**Pseudocode**

```pseudocode
for each vertex v
    distance[v] = infinity
    predecessor[v] = none
end for
distance[start] = 0
priorityQueue = [(0, start)]

while priorityQueue is not empty
    (candidateDistance, v) = removeMinimum(priorityQueue)
    if candidateDistance != distance[v]
        continue  // stale entry
    end if
    for each edge (v, u, weight) in graph[v]
        if candidateDistance + weight < distance[u]
            distance[u] = candidateDistance + weight
            predecessor[u] = v
            insert (distance[u], u) into priorityQueue
        end if
    end for
end while

function reconstructPath(target)
    if distance[target] == infinity: return no path
    path = empty list
    current = target
    while current is not none
        append current to path
        current = predecessor[current]
    end while
    reverse path
    return path
end function
```

**C++**

```cpp
#include <algorithm>
#include <functional>
#include <iostream>
#include <limits>
#include <queue>
#include <utility>
#include <vector>

int main() {
    using Edge = std::pair<int, long long>;
    using Entry = std::pair<long long, int>; // distance, vertex
    const long long INF = std::numeric_limits<long long>::max() / 4;
    std::vector<std::vector<Edge>> graph(4);
    graph[0] = {{1,4},{2,1}}; // A -> B, A -> C
    graph[2] = {{1,2},{3,5}}; // C -> B, C -> D
    graph[1] = {{3,1}};       // B -> D
    std::vector<long long> distance(4, INF);
    std::vector<int> predecessor(4, -1);
    std::priority_queue<Entry, std::vector<Entry>, std::greater<Entry>> pending;
    distance[0] = 0;
    pending.push({0, 0});
    while (!pending.empty()) {
        auto [candidate, v] = pending.top(); pending.pop();
        if (candidate != distance[v]) continue; // stale entry
        for (auto [u, weight] : graph[v]) {
            if (candidate > INF - weight) continue; // avoid overflow
            long long next = candidate + weight;
            if (next < distance[u]) {
                distance[u] = next;
                predecessor[u] = v;
                pending.push({next, u});
            }
        }
    }
    for (long long d : distance) {
        if (d == INF) std::cout << "inf ";
        else std::cout << d << ' ';
    }
    if (distance[3] == INF) {
        std::cout << "\nD is unreachable\n";
    } else {
        std::vector<int> path;
        for (int v = 3; v != -1; v = predecessor[v]) path.push_back(v);
        std::reverse(path.begin(), path.end());
        std::cout << "\nPath: ";
        for (int i = 0; i < static_cast<int>(path.size()); ++i)
            std::cout << char('A' + path[i]) << (i + 1 == static_cast<int>(path.size()) ? '\n' : '-');
    }
}
```

**Java**

```java
import java.util.*;
class DijkstraExample {
    static class Edge {
        int to; long weight;
        Edge(int to, long weight) { this.to = to; this.weight = weight; }
    }
    public static void main(String[] args) {
        final long INF = Long.MAX_VALUE / 4;
        List<List<Edge>> graph = new ArrayList<>();
        for (int i = 0; i < 4; i++) graph.add(new ArrayList<>());
        graph.get(0).add(new Edge(1, 4)); graph.get(0).add(new Edge(2, 1));
        graph.get(2).add(new Edge(1, 2)); graph.get(2).add(new Edge(3, 5));
        graph.get(1).add(new Edge(3, 1));
        long[] distance = new long[4];
        Arrays.fill(distance, INF);
        int[] predecessor = new int[4];
        Arrays.fill(predecessor, -1);
        PriorityQueue<long[]> pending = new PriorityQueue<>(Comparator.comparingLong(e -> e[0]));
        distance[0] = 0;
        pending.add(new long[]{0, 0});
        while (!pending.isEmpty()) {
            long[] entry = pending.remove();
            long candidate = entry[0]; int v = (int) entry[1];
            if (candidate != distance[v]) continue; // stale entry
            for (Edge edge : graph.get(v)) {
                if (candidate > INF - edge.weight) continue; // avoid overflow
                long next = candidate + edge.weight;
                if (next < distance[edge.to]) {
                    distance[edge.to] = next;
                    predecessor[edge.to] = v;
                    pending.add(new long[]{next, edge.to});
                }
            }
        }
        for (long d : distance) System.out.print((d == INF ? "inf" : d) + " ");
        if (distance[3] == INF) {
            System.out.println("\nD is unreachable");
        } else {
            List<Integer> path = new ArrayList<>();
            for (int v = 3; v != -1; v = predecessor[v]) path.add(v);
            Collections.reverse(path);
            System.out.print("\nPath: ");
            for (int i = 0; i < path.size(); i++) {
                System.out.print((char)('A' + path.get(i)) + (i + 1 == path.size() ? "\n" : "-"));
            }
        }
    }
}
```

**Python**

```python
import heapq
from math import inf

graph = {"A": [("B", 4), ("C", 1)], "B": [("D", 1)],
         "C": [("B", 2), ("D", 5)], "D": []}
distance = {vertex: inf for vertex in graph}
predecessor = {vertex: None for vertex in graph}
distance["A"] = 0
pending = [(0, "A")]
while pending:
    candidate, vertex = heapq.heappop(pending)
    if candidate != distance[vertex]:  # stale entry
        continue
    for neighbor, weight in graph[vertex]:  # weights are non-negative
        next_distance = candidate + weight
        if next_distance < distance[neighbor]:
            distance[neighbor] = next_distance
            predecessor[neighbor] = vertex
            heapq.heappush(pending, (next_distance, neighbor))

print([distance[v] for v in graph])  # [0, 3, 1, 4]
if distance["D"] == inf:
    print("D is unreachable")
else:
    path = []
    vertex = "D"
    while vertex is not None:
        path.append(vertex)
        vertex = predecessor[vertex]
    path.reverse()
    print("Path:", "-".join(path))  # Path: A-C-B-D
```

## Complexity Depends on the Implementation

- A simple implementation that scans all unprocessed vertices to choose the smallest tentative distance takes `O(V² + E)`, commonly written `O(V²)`, with adjacency lists and an array of distances.
- With an adjacency list and a binary min-priority queue, the common bound is `O((V + E) log V)` (often written `O(E log V)` for connected graphs). These implementations use lazy queue entries and skip an entry `(candidateDistance, v)` when `candidateDistance != distance[v]`; a smaller route was recorded after this older entry was added, so it is stale. Their distance and predecessor arrays use `O(V)` space, and the queue may hold `O(E)` entries, for `O(V + E)` auxiliary space in the worst case.
- An adjacency matrix with a simple scan also naturally leads to `O(V²)` time.

These bounds assume non-negative weights and a graph search from one source. Unreachable vertices keep infinite distance.

## Summary

Dijkstra repeatedly finalizes the closest tentative vertex and relaxes its outgoing edges. The non-negative weight condition is essential, and the exact complexity depends on the data structures used to select the next vertex.
