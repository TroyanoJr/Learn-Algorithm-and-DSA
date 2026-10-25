# Extracting the Root and Sift Down

The root of a **min-heap** is the minimum; the root of a **max-heap** is the maximum. Extracting it returns that value, moves the final array item to the root, removes the final slot, and sifts the replacement down until parent-child order is restored.

## Min-Heap Trace

Begin with `[1, 5, 2, 9, 7, 8, 3]`:

- Save root `1`. Move final value `3` to the root and remove its old slot: `[3, 5, 2, 9, 7, 8]`.
- The root's children are 5 (index 1) and 2 (index 2). Choose the smaller child, 2, and swap: `[2, 5, 3, 9, 7, 8]`.
- At index 2, the only child is 8 (index 5). Since `3 <= 8`, stop.

The extracted value is `1` and the remaining array is a valid min-heap. For a max-heap, choose the larger child and swap while the parent is smaller.

### Pseudocode

```pseudocode
procedure extractMin(heap)
    if heap is empty: error
    result = heap[0]
    heap[0] = heap[last index]
    remove final array entry
    i = 0
    while left child index 2*i + 1 is inside heap
        child = left child index
        right = child + 1
        if right is inside heap and heap[right] < heap[child]
            child = right
        if heap[i] <= heap[child]
            break
        swap(heap[i], heap[child])
        i = child
    end while
    return result
end procedure
```

```cpp
int extractMin(std::vector<int>& heap) {
    int result = heap[0];
    heap[0] = heap.back();
    heap.pop_back();
    for (int i = 0; 2 * i + 1 < static_cast<int>(heap.size());) {
        int child = 2 * i + 1;
        if (child + 1 < static_cast<int>(heap.size()) && heap[child + 1] < heap[child]) ++child;
        if (heap[i] <= heap[child]) break;
        std::swap(heap[i], heap[child]);
        i = child;
    }
    return result;
}
```
```java
static int extractMin(List<Integer> heap) {
    int result = heap.get(0);
    heap.set(0, heap.get(heap.size() - 1));
    heap.remove(heap.size() - 1);
    for (int i = 0; 2 * i + 1 < heap.size();) {
        int child = 2 * i + 1;
        if (child + 1 < heap.size() && heap.get(child + 1) < heap.get(child)) child++;
        if (heap.get(i) <= heap.get(child)) break;
        Collections.swap(heap, i, child);
        i = child;
    }
    return result;
}
```
```python
def extract_min(heap):
    result = heap[0]
    heap[0] = heap[-1]
    heap.pop()
    i = 0
    while 2 * i + 1 < len(heap):
        child = 2 * i + 1
        if child + 1 < len(heap) and heap[child + 1] < heap[child]:
            child += 1
        if heap[i] <= heap[child]:
            break
        heap[i], heap[child] = heap[child], heap[i]
        i = child
    return result
```

These function examples assume a non-empty heap; production code should handle the empty case. The C++ version needs `<vector>` and `<utility>`; Java needs `java.util.*`.

Each step moves down at most one level, so extraction takes `O(log n)` worst-case time. Peeking at the root without removing it takes `O(1)`. A max-heap uses the same procedure with comparisons reversed.

## Summary

Move the last value to the root and repeatedly swap with the appropriate (smallest for min-heap, largest for max-heap) child. Choosing the correct child is essential when both children exist.
