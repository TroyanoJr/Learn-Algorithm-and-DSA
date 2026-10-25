# Heap Properties and Array Representation

A **heap** is a complete binary tree stored compactly in an array. Complete means every level is full except possibly the last, whose nodes fill from left to right. This shape keeps the tree height logarithmic.

## Min-Heaps and Max-Heaps

- In a **min-heap**, each parent is less than or equal to its children, so the minimum is at the root.
- In a **max-heap**, each parent is greater than or equal to its children, so the maximum is at the root.

Only parent-child order is guaranteed. A heap is not a fully sorted array: siblings and nodes in separate branches need not be ordered relative to one another.

For example, the min-heap array `[2, 5, 3, 9, 7, 8]` represents:

```text
          2
        /   \
       5     3
      / \   /
     9   7 8
```

Each array position maps to a tree position. For zero-based index `i`:

- Parent (when `i > 0`): `(i - 1) / 2`, using integer division.
- Left child: `2 * i + 1`.
- Right child: `2 * i + 2`.

In the example, index 1 contains 5; its parent is index 0 (`(1 - 1) / 2`) and its children are indices 3 and 4 (9 and 7). Index 2 has only a left child at index 5. This still forms a complete tree because the final level fills left to right.

## Uses and Costs

Heaps are useful when an application repeatedly needs the smallest or largest item, such as a scheduler, event simulator, or priority queue. In a binary heap, reading the root is `O(1)`, while insertion and root extraction are `O(log n)`. Searching for an arbitrary value is `O(n)` because the heap does not sort every branch. The array representation uses `O(n)` storage.

## Summary

Choose a min-heap for quick access to the minimum and a max-heap for quick access to the maximum. The complete-tree shape and parent-child rule—not total sorted order—are the defining properties.
