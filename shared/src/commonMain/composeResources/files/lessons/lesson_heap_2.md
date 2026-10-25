# Heap Insertion and Sift Up

This lesson uses a **min-heap**. To insert a value, append it at the end of the array, then compare it with its parent. While it is smaller than its parent, swap the two. This upward repair is called **sift up** (or bubble up).

## Trace

Start with the valid min-heap `[2, 5, 3, 9, 7, 8]` and insert `1`:

- Append at the next position: `[2, 5, 3, 9, 7, 8, 1]`. The new value is at index 6; its parent is index 2, value 3.
- Since `1 < 3`, swap: `[2, 5, 1, 9, 7, 8, 3]`. At index 2, its parent is index 0, value 2.
- Since `1 < 2`, swap: `[1, 5, 2, 9, 7, 8, 3]`. The root has no parent, so stop.

Every swap restores the ordering with the element's former children; once the inserted element reaches a parent no larger than it, the entire heap is valid again.

### Pseudocode

```pseudocode
procedure insertMinHeap(heap, value)
    append value to heap
    i = length(heap) - 1
    while i > 0
        parent = (i - 1) // 2
        if heap[parent] <= heap[i]
            break
        swap(heap[parent], heap[i])
        i = parent
    end while
end procedure
```

```cpp
void insertMinHeap(std::vector<int>& heap, int value) {
    heap.push_back(value);
    int i = static_cast<int>(heap.size()) - 1;
    while (i > 0) {
        int parent = (i - 1) / 2;
        if (heap[parent] <= heap[i]) break;
        std::swap(heap[parent], heap[i]);
        i = parent;
    }
}
```
```java
static void insertMinHeap(List<Integer> heap, int value) {
    heap.add(value);
    int i = heap.size() - 1;
    while (i > 0) {
        int parent = (i - 1) / 2;
        if (heap.get(parent) <= heap.get(i)) break;
        Collections.swap(heap, parent, i);
        i = parent;
    }
}
```
```python
def insert_min_heap(heap, value):
    heap.append(value)
    i = len(heap) - 1
    while i > 0:
        parent = (i - 1) // 2
        if heap[parent] <= heap[i]:
            break
        heap[parent], heap[i] = heap[i], heap[parent]
        i = parent
```

The C++ version needs `<vector>` and `<utility>`; the Java version needs `java.util.*` and a mutable `List<Integer>`.

The same loop applies to a max-heap by changing the comparison to `heap[parent] >= heap[i]`. In the worst case the new value travels from a leaf to the root. A complete binary heap has height `O(log n)`, so insertion takes `O(log n)` time; appending alone is `O(1)` amortized for a dynamic array.

## Summary

Append first, then sift up only along the new node's ancestor path. The trace ends at `[1, 5, 2, 9, 7, 8, 3]`.
