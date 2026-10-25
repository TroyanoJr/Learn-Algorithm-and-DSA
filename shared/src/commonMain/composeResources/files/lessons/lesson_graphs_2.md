# Graph Representations

We will represent the undirected, unweighted graph with vertices `{A, B, C, D, E}` and edges `{AB, AC, BD, CD, DE}`.

## Adjacency List

An adjacency list stores each vertex with its neighbors:

```text
A: B, C
B: A, D
C: A, D
D: B, C, E
E: D
```

For an undirected graph, each edge appears in the lists of both endpoints. With `V` vertices and `E` edges, the representation uses `O(V + E)` space (there are `2E` neighbor entries for an undirected graph, plus the vertex lists). It is a good fit for sparse graphs and for algorithms that iterate over neighbors.

## Adjacency Matrix

Rows and columns use the order A, B, C, D, E. A `1` means an edge exists; `0` means there is none.

```text
    A B C D E
A [ 0 1 1 0 0 ]
B [ 1 0 0 1 0 ]
C [ 1 0 0 1 0 ]
D [ 0 1 1 0 1 ]
E [ 0 0 0 1 0 ]
```

The matrix uses `O(V²)` space whether or not many edges exist. Checking whether a particular pair is adjacent takes `O(1)`, but listing a vertex's neighbors scans a row in `O(V)`. Matrices suit dense graphs or workloads with frequent edge-existence checks. For weighted graphs, store each edge weight instead of `1`, and use a separate convention (often infinity) for no edge.

## Summary

Adjacency lists store only existing neighbor relationships and use `O(V + E)` space. Adjacency matrices reserve a cell for every vertex pair and use `O(V²)` space. Both describe the same graph, so choose according to graph density and the operations you need.
