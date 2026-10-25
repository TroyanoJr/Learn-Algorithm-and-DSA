# Connected Components and Cycle Detection

This lesson uses undirected graphs unless a section says otherwise. An **undirected connected component** is a maximal set of vertices where every pair is joined by a path.

## Finding Components

Suppose the edges are `{AB, BC, DE}` and vertex F is isolated. Start a traversal from each still-unvisited vertex:

- Start at A. DFS visits A, B, C: component `{A, B, C}`.
- B and C are already visited. Start at D: traversal visits D, E: component `{D, E}`.
- Start at F: component `{F}`.

The outer loop is important: a single traversal finds only the component containing its start. With adjacency lists, visiting all components takes `O(V + E)` time and `O(V)` additional space.

### Code Example: Connected Components

Each version uses the undirected graph `AB, BC, DE` with isolated vertex F and returns the same components: `{A,B,C}`, `{D,E}`, `{F}`. Vertices use alphabetical order.

**Pseudocode**

```pseudocode
procedure collect(graph)
    visited = empty set
    components = empty list
    for each vertex v in graph
        if v is not in visited
            component = empty list
            explore(v, graph, visited, component)
            append component to components
        end if
    end for
    return components
end procedure

procedure explore(v, graph, visited, component)
    add v to visited
    append v to component
    for each neighbor u of v
        if u is not in visited
            explore(u, graph, visited, component)
        end if
    end for
end procedure
```

**C++**

```cpp
#include <iostream>
#include <vector>

void explore(const std::vector<std::vector<int>>& graph, int v,
             std::vector<bool>& visited, std::vector<int>& component) {
    visited[v] = true;
    component.push_back(v);
    for (int u : graph[v]) if (!visited[u]) explore(graph, u, visited, component);
}

int main() {
    std::vector<std::vector<int>> graph{{1},{0,2},{1},{4},{3},{}};
    std::vector<bool> visited(6, false);
    for (int v = 0; v < 6; ++v) if (!visited[v]) {
        std::vector<int> component;
        explore(graph, v, visited, component);
        std::cout << '{';
        for (int u : component) std::cout << char('A' + u);
        std::cout << "} ";
    }
}
```

**Java**

```java
import java.util.*;
class ComponentsExample {
    static void explore(int[][] graph, int v, boolean[] visited, List<Integer> part) {
        visited[v] = true;
        part.add(v);
        for (int u : graph[v]) if (!visited[u]) explore(graph, u, visited, part);
    }
    public static void main(String[] args) {
        int[][] graph = {{1},{0,2},{1},{4},{3},{}};
        boolean[] visited = new boolean[6];
        for (int v = 0; v < 6; v++) if (!visited[v]) {
            List<Integer> part = new ArrayList<>();
            explore(graph, v, visited, part);
            System.out.print("{");
            for (int u : part) System.out.print((char)('A' + u));
            System.out.print("} ");
        }
    }
}
```

**Python**

```python
def components(graph):
    visited = set()
    result = []

    def explore(vertex, part):
        visited.add(vertex)
        part.append(vertex)
        for neighbor in graph[vertex]:
            if neighbor not in visited:
                explore(neighbor, part)

    for vertex in graph:
        if vertex not in visited:
            part = []
            explore(vertex, part)
            result.append(part)
    return result

graph = {"A": ["B"], "B": ["A", "C"], "C": ["B"],
         "D": ["E"], "E": ["D"], "F": []}
print(components(graph))  # [['A', 'B', 'C'], ['D', 'E'], ['F']]
```

Each traversal marks a vertex once and scans each adjacency entry once, so the full component search takes `O(V + E)` time and `O(V)` space.

## Cycle Detection in an Undirected Graph

In an undirected DFS, an edge to a visited vertex indicates a cycle only when that vertex is **not the parent** from which the current vertex was reached. For example, with edges `{AB, BC, CA}`, DFS from A reaches B, then C; from C, the visited neighbor A is not C's parent B, so the graph contains a cycle. Ignoring the parent edge avoids falsely calling every backtrack a cycle.

### Code Example: Undirected Cycle Detection

These versions test the triangle `AB, BC, CA` and return `true`. They assume a simple undirected graph (no parallel edges); the parent vertex is skipped while all other visited neighbors indicate a cycle.

**Pseudocode**

```pseudocode
function hasUndirectedCycle(graph)
    visited = empty set
    for each vertex v in graph
        if v is not in visited and search(v, none, graph, visited)
            return true
        end if
    end for
    return false
end function

function search(v, parent, graph, visited)
    add v to visited
    for each neighbor u of v
        if u is not in visited
            if search(u, v, graph, visited): return true
        else if u != parent
            return true
        end if
    end for
    return false
end function
```

**C++**

```cpp
#include <iostream>
#include <vector>

bool search(const std::vector<std::vector<int>>& graph, int v, int parent,
            std::vector<bool>& visited) {
    visited[v] = true;
    for (int u : graph[v]) {
        if (!visited[u]) {
            if (search(graph, u, v, visited)) return true;
        } else if (u != parent) return true;
    }
    return false;
}

int main() {
    std::vector<std::vector<int>> graph{{1,2},{0,2},{0,1}};
    std::vector<bool> visited(3, false);
    bool cycle = false;
    for (int v = 0; v < 3; ++v)
        if (!visited[v] && search(graph, v, -1, visited)) cycle = true;
    std::cout << std::boolalpha << cycle << '\n';
}
```

**Java**

```java
class UndirectedCycleExample {
    static boolean search(int[][] graph, int v, int parent, boolean[] visited) {
        visited[v] = true;
        for (int u : graph[v]) {
            if (!visited[u]) {
                if (search(graph, u, v, visited)) return true;
            } else if (u != parent) return true;
        }
        return false;
    }
    public static void main(String[] args) {
        int[][] graph = {{1,2},{0,2},{0,1}};
        boolean[] visited = new boolean[3];
        boolean cycle = false;
        for (int v = 0; v < 3; v++)
            if (!visited[v] && search(graph, v, -1, visited)) cycle = true;
        System.out.println(cycle);
    }
}
```

**Python**

```python
def has_undirected_cycle(graph):
    visited = set()

    def search(vertex, parent):
        visited.add(vertex)
        for neighbor in graph[vertex]:
            if neighbor not in visited:
                if search(neighbor, vertex):
                    return True
            elif neighbor != parent:
                return True
        return False

    return any(vertex not in visited and search(vertex, None) for vertex in graph)

graph = {"A": ["B", "C"], "B": ["A", "C"], "C": ["A", "B"]}
print(has_undirected_cycle(graph))  # True
```

With adjacency lists, this check takes `O(V + E)` time and `O(V)` space.

## Directed Graphs Need a Different Rule

Do not reuse the undirected parent rule for directed graphs. In a directed DFS, track vertices on the **current recursion path** (often called the active set). An edge to an active vertex indicates a directed cycle. For example, `A → B → C → A` returns to an active vertex. An edge to a vertex fully explored earlier does not by itself prove a directed cycle.

### Code Example: Directed Cycle Detection

This separate DFS uses three states: unvisited, active on the current recursion path, and finished. The directed triangle `A→B→C→A` returns `true`.

**Pseudocode**

```pseudocode
function hasDirectedCycle(graph)
    state[v] = unvisited for every vertex v
    for each vertex v in graph
        if state[v] is unvisited and search(v, graph, state)
            return true
        end if
    end for
    return false
end function

function search(v, graph, state)
    state[v] = active
    for each neighbor u of v
        if state[u] is active: return true
        if state[u] is unvisited and search(u, graph, state): return true
    end for
    state[v] = finished
    return false
end function
```

**C++**

```cpp
#include <iostream>
#include <vector>

bool search(const std::vector<std::vector<int>>& graph, int v,
            std::vector<int>& state) {
    state[v] = 1; // active
    for (int u : graph[v]) {
        if (state[u] == 1) return true;
        if (state[u] == 0 && search(graph, u, state)) return true;
    }
    state[v] = 2; // finished
    return false;
}

int main() {
    std::vector<std::vector<int>> graph{{1},{2},{0}};
    std::vector<int> state(3, 0);
    bool cycle = false;
    for (int v = 0; v < 3; ++v)
        if (state[v] == 0 && search(graph, v, state)) cycle = true;
    std::cout << std::boolalpha << cycle << '\n';
}
```

**Java**

```java
class DirectedCycleExample {
    static boolean search(int[][] graph, int v, int[] state) {
        state[v] = 1; // active
        for (int u : graph[v]) {
            if (state[u] == 1) return true;
            if (state[u] == 0 && search(graph, u, state)) return true;
        }
        state[v] = 2; // finished
        return false;
    }
    public static void main(String[] args) {
        int[][] graph = {{1},{2},{0}};
        int[] state = new int[3];
        boolean cycle = false;
        for (int v = 0; v < 3; v++)
            if (state[v] == 0 && search(graph, v, state)) cycle = true;
        System.out.println(cycle);
    }
}
```

**Python**

```python
def has_directed_cycle(graph):
    state = {vertex: 0 for vertex in graph}  # 0=unvisited, 1=active, 2=finished

    def search(vertex):
        state[vertex] = 1
        for neighbor in graph[vertex]:
            if state[neighbor] == 1:
                return True
            if state[neighbor] == 0 and search(neighbor):
                return True
        state[vertex] = 2
        return False

    return any(state[vertex] == 0 and search(vertex) for vertex in graph)

graph = {"A": ["B"], "B": ["C"], "C": ["A"]}
print(has_directed_cycle(graph))  # True
```

This adjacency-list traversal takes `O(V + E)` time and `O(V)` space. The active-state rule is specific to directed graphs.

## Summary

Repeatedly start traversal at unvisited vertices to enumerate undirected components. Cycle detection must respect edge direction: skip the parent edge in undirected DFS, and detect a back edge to an active ancestor in directed DFS.
