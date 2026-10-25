# Graph Fundamentals and Terminology

A **graph** models relationships. It has **vertices** (also called nodes) and **edges** connecting pairs of vertices. We will use the same undirected, unweighted graph throughout this introduction:

```text
    B ----- D ----- E
   /       /
  A ----- C
```

Its vertices are `{A, B, C, D, E}` and its edges are `{AB, AC, BD, CD, DE}`. An edge `AB` means the same connection as `BA` in an undirected graph.

## Common Terms

- **Undirected graph:** an edge has no direction; if A is connected to B, B is connected to A.
- **Directed graph:** an edge has an arrow, such as `A → B`; it does not automatically imply `B → A`.
- **Unweighted graph:** edges represent connections without a numeric cost. A **weighted graph** assigns a value such as distance or time to each edge.
- **Degree:** in an undirected graph, the degree of a vertex is its number of incident edges. Here `degree(D) = 3` (neighbors B, C, and E). In a directed graph, we distinguish in-degree and out-degree.
- **Path:** a sequence of vertices connected by edges. `A-C-D-E` is a path of three edges.
- **Cycle:** a path that returns to its starting vertex without repeating other vertices; `A-B-D-C-A` is a cycle in this graph.
- **Connected:** in an undirected graph, every pair of vertices has a path between them. This example is connected.

## Why Graphs?

Roads, friendships, web links, and task dependencies can all be described with vertices and edges. Direction and weights capture different meanings: a one-way street is directed, while a two-way friendship is often modeled as undirected.

## Summary

First identify what the vertices and edges mean. Then decide whether edges are directed and whether they carry weights; these choices affect which algorithms are appropriate.
