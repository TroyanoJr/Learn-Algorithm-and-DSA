# Divide & Conquer Complexity and Trade-offs

Comparing divide-and-conquer sorting algorithms like **Merge Sort** and **Quick Sort** highlights important trade-offs between time complexity, memory usage, and stability.

## Complexity Comparison

- **Merge Sort** — Best case: O(N log N); average case: O(N log N); worst case: O(N log N); auxiliary space: O(N); stable: yes.
- **Quick Sort** — Best case: O(N log N); average case: O(N log N); worst case: O(N^2); auxiliary space: O(log N) typical/average with reasonably balanced partitions and O(N) worst case (call stack); stable: no.

## Key Trade-offs

1. **Worst-Case Performance**:
   - Merge Sort guarantees O(N log N) performance regardless of input initial order.
   - Quick Sort degrades to O(N^2) if the pivot consistently divides the array into unbalanced partitions (e.g., choosing the smallest or largest element as pivot on already sorted data). Randomized pivot selection mitigates this.
2. **Memory (Auxiliary Space)**:
   - Merge Sort requires O(N) extra memory for temporary merging buffers.
   - Quick Sort sorts *in-place*. With reasonably balanced partitions, its call stack is typically O(log N); consistently unbalanced partitions can produce O(N) recursive depth and stack space.
3. **Stability**:
   - Merge Sort is stable (preserves relative order of equal keys).
   - Standard Quick Sort partitions by swapping elements across the array, which can alter the relative order of equal keys (unstable).

### Code Example: Comparing Partition Splits

The following code traces how partition sizes differ between balanced and unbalanced divides.

```pseudocode
function evaluatePartition(n)
    goodLeft = n / 2
    goodRight = n - goodLeft
    badLeft = 1
    badRight = n - badLeft
    print("Balanced:", goodLeft, goodRight, "Unbalanced:", badLeft, badRight)
end function

evaluatePartition(10)
```
```cpp
#include <iostream>

void evaluatePartition(int n) {
    int goodLeft = n / 2;
    int goodRight = n - goodLeft;
    int badLeft = 1;
    int badRight = n - badLeft;
    std::cout << "Balanced: " << goodLeft << " " << goodRight
              << " Unbalanced: " << badLeft << " " << badRight << '\n';
}

int main() {
    evaluatePartition(10);
}
```
```java
class PartitionComparison {
    static void evaluatePartition(int n) {
        int goodLeft = n / 2;
        int goodRight = n - goodLeft;
        int badLeft = 1;
        int badRight = n - badLeft;
        System.out.println("Balanced: " + goodLeft + " " + goodRight
                + " Unbalanced: " + badLeft + " " + badRight);
    }

    public static void main(String[] args) {
        evaluatePartition(10);
    }
}
```
```python
def evaluate_partition(n):
    good_left = n // 2
    good_right = n - good_left
    bad_left = 1
    bad_right = n - bad_left
    print("Balanced:", good_left, good_right, "Unbalanced:", bad_left, bad_right)

evaluate_partition(10)
```

## Remember

- **Merge Sort** guarantees O(N log N) time and O(N) space and is **stable**.
- **Quick Sort** runs in O(N log N) average time and sorts **in-place**. Its call stack is typically O(log N) with reasonably balanced partitions, but can reach O(N) for consistently unbalanced partitions; time can degrade to O(N^2).
- Not all divide-and-conquer algorithms have identical complexity; partition balance dictates Quick Sort's efficiency.

## Summary

Merge Sort and Quick Sort both use divide and conquer but offer distinct trade-offs: Merge Sort trades O(N) extra memory for guaranteed O(N log N) stability, while Quick Sort trades worst-case protection for fast in-place execution.
