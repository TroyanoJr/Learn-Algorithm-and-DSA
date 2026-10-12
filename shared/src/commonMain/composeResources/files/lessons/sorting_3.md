# Selection Sort

Selection sort divides the array into a sorted and an unsorted region. In each pass, it finds the smallest element in the unsorted region and swaps it directly into the end of the sorted region.

Imagine picking the smallest card from an unsorted pile in your hand and placing it into a new organized pile one by one.

## Key Concepts

- **Minimum Selection**: Scans the remaining unsorted subarray to find the minimum value.
- **Single Swap Per Pass**: Performs at most 1 swap per outer pass (`N - 1` swaps total).
- **Unconditional O(N²) Comparisons**: Always scans the entire unsorted region regardless of whether the array is already sorted.
- **Unstable Sort**: Selection Sort is generally **unstable** because swapping a minimum element over long distances can jump over duplicate keys and disrupt their relative order (e.g. sorting `[5a, 5b, 2]` swaps `5a` with `2`, placing `5a` after `5b`).
- **Complexity**:
  - **Best, Average, Worst Case**: `O(N²)` comparisons.
  - **Max Swaps**: `O(N)` swaps total.
  - **Auxiliary Space**: `O(1)` in-place.

### Code Example: Selection Sort Implementation

```pseudocode
function selectionSort(arr):
    n = length(arr)
    for i from 0 to n - 2 do
        minIdx = i
        // Find index of minimum element in unsorted region
        for j from i + 1 to n - 1 do
            if arr[j] < arr[minIdx] then
                minIdx = j
            end if
        end for
        // Swap found minimum element with element at index i
        temp = arr[minIdx]
        arr[minIdx] = arr[i]
        arr[i] = temp
    end for
```
```cpp
#include <vector>

void selectionSort(std::vector<int>& arr) {
    int n = arr.size();
    for (int i = 0; i < n - 1; i++) {
        int minIdx = i;
        // Find index of minimum element in unsorted region
        for (int j = i + 1; j < n; j++) {
            if (arr[j] < arr[minIdx]) {
                minIdx = j;
            }
        }
        // Swap found minimum element into place
        int temp = arr[minIdx];
        arr[minIdx] = arr[i];
        arr[i] = temp;
    }
}
```
```java
public class SelectionSort {
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            // Find index of minimum element in unsorted region
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            // Swap found minimum element into place
            int temp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = temp;
        }
    }
}
```
```python
def selection_sort(arr):
    n = len(arr)
    for i in range(n - 1):
        min_idx = i
        # Find index of minimum element in unsorted region
        for j in range(i + 1, n):
            if arr[j] < arr[min_idx]:
                min_idx = j
        # Swap found minimum element into place
        arr[i], arr[min_idx] = arr[min_idx], arr[i]
```

## Summary

Selection Sort minimizes memory writes (`O(N)` total swaps max) but unconditionally performs `O(N²)` comparisons and is generally unstable.
