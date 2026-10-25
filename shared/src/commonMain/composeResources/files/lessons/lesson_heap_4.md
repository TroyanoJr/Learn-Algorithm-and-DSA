# Building a Heap

There are two common ways to turn `n` values into a **min-heap**:

- Insert values one at a time and sift each new value up. This takes `O(n log n)` in the worst case.
- Use **bottom-up build-heap**: treat the array as a complete tree and sift down each internal node, starting with the last non-leaf and moving toward the root. This takes `O(n)` overall.

Leaves already satisfy the heap property because they have no children. In a zero-based array of length `n`, the last non-leaf index is `floor(n / 2) - 1`.

## Bottom-Up Trace

Build a min-heap from `[9, 4, 7, 1, 0, 3]`. The last non-leaf is index 2.

- Sift down index 2: value 7 has child 3 at index 5, so swap. Array: `[9, 4, 3, 1, 0, 7]`.
- Move to index 1: value 4 has children 1 and 0. Choose smaller child 0 and swap. Array: `[9, 0, 3, 1, 4, 7]`. Index 4 has no children.
- Move to index 0: value 9 has children 0 and 3. Choose 0 and swap: `[0, 9, 3, 1, 4, 7]`. At index 1, children are 1 and 4; choose 1 and swap: `[0, 1, 3, 9, 4, 7]`.

The result `[0, 1, 3, 9, 4, 7]` is a min-heap.

### Pseudocode

```pseudocode
procedure buildMinHeap(array)
    for i = floor(length(array) / 2) - 1 down to 0
        siftDownMin(array, i)
    end for
end procedure
```

```cpp
void siftDownMin(std::vector<int>& a, int i, int n) {
    while (2 * i + 1 < n) {
        int child = 2 * i + 1;
        if (child + 1 < n && a[child + 1] < a[child]) ++child;
        if (a[i] <= a[child]) break;
        std::swap(a[i], a[child]);
        i = child;
    }
}
void buildMinHeap(std::vector<int>& a) {
    for (int i = static_cast<int>(a.size()) / 2 - 1; i >= 0; --i)
        siftDownMin(a, i, static_cast<int>(a.size()));
}
```
```java
static void siftDownMin(int[] a, int i, int n) {
    while (2 * i + 1 < n) {
        int child = 2 * i + 1;
        if (child + 1 < n && a[child + 1] < a[child]) child++;
        if (a[i] <= a[child]) break;
        int temp = a[i]; a[i] = a[child]; a[child] = temp;
        i = child;
    }
}
static void buildMinHeap(int[] a) {
    for (int i = a.length / 2 - 1; i >= 0; i--) siftDownMin(a, i, a.length);
}
```
```python
def sift_down_min(a, i, n):
    while 2 * i + 1 < n:
        child = 2 * i + 1
        if child + 1 < n and a[child + 1] < a[child]:
            child += 1
        if a[i] <= a[child]:
            break
        a[i], a[child] = a[child], a[i]
        i = child

def build_min_heap(a):
    for i in range(len(a) // 2 - 1, -1, -1):
        sift_down_min(a, i, len(a))
```

The C++ example needs `<vector>` and `<utility>`. All three implementations mutate the input array in place and use the same child-selection rule as the trace.

### Why the Cost Is O(n)

Although one sift-down can take `O(log n)`, most nodes are near the leaves and can move only a short distance. Fewer than `n/2` nodes can move one level, fewer than `n/4` can move two levels, and so on. The total work is bounded by `n(1/2 + 2/4 + 3/8 + ...)`, a convergent series, so bottom-up construction is `O(n)`, not `O(n log n)`. Building by repeated insertion does not have this advantage.

## Summary

Start at the last parent and sift down toward index 0. This uses the complete-tree shape to build a heap in linear time.
