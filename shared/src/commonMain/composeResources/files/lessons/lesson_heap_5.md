# Priority Queues and Heap Sort

A **priority queue** removes the item with highest priority, not necessarily the item that arrived first. A binary heap is a common implementation. A min-heap treats the smallest key as highest priority; a max-heap treats the largest key as highest priority.

Typical binary-heap costs are:

- Insert an item: `O(log n)`.
- Peek at the highest-priority item: `O(1)`.
- Extract that item: `O(log n)`.

## Heap Sort with a Max-Heap

Heap Sort sorts an array in place by first making a **max-heap**, then repeatedly swapping the root (the largest remaining value) with the last item in the unsorted prefix. Shrink the heap boundary and sift the new root down. The sorted suffix grows from right to left.

For `[4, 1, 3, 2]`, bottom-up construction produces max-heap `[4, 2, 3, 1]`:

- Swap root 4 with the last heap item 1: `[1, 2, 3, 4]`; heap boundary is now index 2. Sift down by swapping 1 with larger child 3: `[3, 2, 1, 4]`.
- Swap root 3 with the last item in the heap, 1: `[1, 2, 3, 4]`; heap boundary is index 1. Sift down to get `[2, 1, 3, 4]`.
- Swap 2 and 1: `[1, 2, 3, 4]`. The full array is sorted ascending.

Building the heap takes `O(n)`. There are `n - 1` extractions, each with at most `O(log n)` sift-down, so Heap Sort takes `O(n log n)` time. The standard array implementation sorts in place and uses `O(1)` auxiliary space (ignoring a constant-sized temporary for swaps). It is not generally stable.

## Data Structure vs. Algorithm

A **heap** is the parent-child-ordered tree structure that supports efficient priority operations. **Heap Sort** is a sorting algorithm that uses a heap arrangement to order an entire array. Using a heap as a priority queue does not by itself mean the program is performing Heap Sort.

## Summary

Heaps make repeated priority access efficient. Heap Sort uses a max-heap and an in-place shrinking boundary to produce ascending order in `O(n log n)` time and `O(1)` auxiliary space.
