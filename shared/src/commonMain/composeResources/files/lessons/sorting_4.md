# Insertion Sort

Insertion sort builds the final sorted array one item at a time by taking each element from the unsorted portion and inserting it into its correct position within the sorted portion.

Imagine sorting cards in your hand: you take one card at a time and slide it into its proper spot among the cards you already hold.

## Key Concepts

- **Sorted Subarray**: Maintains a growing sorted region on the left (`0` to `i - 1`).
- **Shifting Elements**: Rather than swapping repeatedly like Bubble Sort, Insertion Sort shifts larger elements one position to the right to create an open slot for `key`.
- **Adaptive Performance**: Highly efficient (`O(N)` best case) on nearly sorted data because few or no element shifts are required.
- **Stability**: Insertion Sort is **stable** because items are only shifted when strictly greater than `key` (`arr[j] > key`).
- **Complexity**:
  - **Best Case**: `O(N)` on pre-sorted data (1 comparison per item, 0 shifts).
  - **Worst Case**: `O(N²)` on reverse-ordered data.
  - **Auxiliary Space**: `O(1)` in-place.

### Code Example: Insertion Sort Implementation

```pseudocode
function insertionSort(arr):
    n = length(arr)
    for i from 1 to n - 1 do
        key = arr[i]
        j = i - 1
        // Shift elements of sorted region that are greater than key right
        while j >= 0 and arr[j] > key do
            arr[j + 1] = arr[j]
            j = j - 1
        end while
        // Insert key into created open slot
        arr[j + 1] = key
    end for
```
```cpp
#include <vector>

void insertionSort(std::vector<int>& arr) {
    int n = arr.size();
    for (int i = 1; i < n; i++) {
        int key = arr[i];
        int j = i - 1;
        // Shift elements of sorted region that are greater than key right
        while (j >= 0 && arr[j] > key) {
            arr[j + 1] = arr[j];
            j--;
        }
        // Insert key into created open slot
        arr[j + 1] = key;
    }
}
```
```java
public class InsertionSort {
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            int key = arr[i];
            int j = i - 1;
            // Shift elements of sorted region that are greater than key right
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            // Insert key into created open slot
            arr[j + 1] = key;
        }
    }
}
```
```python
def insertion_sort(arr):
    n = len(arr)
    for i in range(1, n):
        key = arr[i]
        j = i - 1
        # Shift elements of sorted region that are greater than key right
        while j >= 0 and arr[j] > key:
            arr[j + 1] = arr[j]
            j -= 1
        # Insert key into created open slot
        arr[j + 1] = key
```

## Summary

Insertion Sort is adaptive, stable, and highly efficient (`O(N)` best-case time) for small or nearly sorted datasets.
