# Depth-First Search (DFS)

**Depth-First Search** follows one path as far as possible, then backtracks to try another. It can use recursion (the program's call stack) or an explicit stack. Use a visited set to avoid revisiting vertices or looping forever on a cycle.

Use the same undirected graph as the BFS lesson, start at A, and examine neighbors alphabetically. Recursive DFS proceeds `A → B → D → C`, then backtracks and visits `E` from D. The order is `A, B, D, C, E`. A different neighbor order can produce a different valid DFS order.

### Code Example: Depth-First Search

The following versions all run recursive DFS from A on the same graph, examine neighbors alphabetically, and produce `A, B, D, C, E`. The C++ and Java versions use integer labels A=0 through E=4, in the same alphabetical neighbor order as the Python lists.

```pseudocode
procedure DFS(graph, v, visited)
    add v to visited
    visit(v)
    for each neighbor u of v in graph[v]
        if u is not in visited
            DFS(graph, u, visited)
        end if
    end for
end procedure
```
```cpp
#include <iostream>
#include <vector>

void dfs(const std::vector<std::vector<int>>& graph, int v,
         std::vector<bool>& visited) {
    visited[v] = true;
    std::cout << char('A' + v) << ' ';
    for (int u : graph[v]) if (!visited[u]) dfs(graph, u, visited);
}

int main() {
    std::vector<std::vector<int>> graph{{1,2},{0,3},{0,3},{1,2,4},{3}};
    std::vector<bool> visited(5, false);
    dfs(graph, 0, visited);
}
```
```java
class DfsExample {
    static void dfs(int[][] graph, int v, boolean[] visited) {
        visited[v] = true;
        System.out.print((char)('A' + v) + " ");
        for (int u : graph[v]) if (!visited[u]) dfs(graph, u, visited);
    }
    public static void main(String[] args) {
        int[][] graph = {{1,2},{0,3},{0,3},{1,2,4},{3}};
        dfs(graph, 0, new boolean[5]);
    }
}
```
```python
def dfs(graph, start):
    visited = set()
    order = []

    def explore(vertex):
        visited.add(vertex)
        order.append(vertex)
        for neighbor in graph[vertex]:  # alphabetical neighbor order
            if neighbor not in visited:
                explore(neighbor)

    explore(start)
    return order

graph = {"A": ["B", "C"], "B": ["A", "D"], "C": ["A", "D"],
         "D": ["B", "C", "E"], "E": ["D"]}
print(dfs(graph, "A"))  # ['A', 'B', 'D', 'C', 'E']
```

With adjacency lists, DFS takes `O(V + E)` time and `O(V)` space for visited state and the recursion/explicit stack. A very deep graph may exceed a language's recursion limit, so an explicit stack can be safer.

## Summary

DFS explores deeply before backtracking. Traversal order depends on neighbor order, but the adjacency-list complexity remains `O(V + E)`.
