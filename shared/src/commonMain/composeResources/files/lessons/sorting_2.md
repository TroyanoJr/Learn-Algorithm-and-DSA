# Bubble Sort

Bubble sort is a simple comparison-based algorithm that repeatedly steps through the list, compares adjacent elements, and swaps them if they are in the wrong order.

Imagine bubbles rising in water. Larger elements gradually "bubble up" to their final sorted positions at the end of the array with each pass.

## Key Concepts

- **Adjacent Comparison**: Compares pairs `arr[j]` and `arr[j + 1]` in each pass.
- **Bubbling Effect**: After outer pass `i`, the `i`-th largest element is guaranteed to be in its correct final position at index `N - 1 - i`.
- **Early-Exit Optimization**: If an entire pass completes with zero swaps (`swapped = false`), the array is already sorted and execution terminates early.
- **Stability**: Bubble Sort is **stable** because equal elements are never swapped (`arr[j] > arr[j + 1]` strict comparison).
- **Complexity**:
  - **Best Case**: `O(N)` with early exit optimization on pre-sorted data.
  - **Worst Case**: `O(N²)` on reverse-ordered data.
  - **Auxiliary Space**: `O(1)` in-place.

### Code Example: Bubble Sort with Early-Exit Optimization

```pseudocode
function bubbleSort(arr):
    n = length(arr)
    for i from 0 to n - 2 do
        swapped = false
        // Inner loop ignores elements already bubbled to the end
        for j from 0 to n - 2 - i do
            if arr[j] > arr[j + 1] then
                // Swap adjacent elements
                temp = arr[j]
                arr[j] = arr[j + 1]
                arr[j + 1] = temp
                swapped = true
            end if
        end for
        // Early exit if no swaps occurred during pass
        if not swapped then
            break
        end if
    end for
```
```cpp
#include <vector>

void bubbleSort(std::vector<int>& arr) {
    int n = arr.size();
    for (int i = 0; i < n - 1; i++) {
        bool swapped = false;
        // Inner loop ignores elements already bubbled to the end
        for (int j = 0; j < n - 1 - i; j++) {
            if (arr[j] > arr[j + 1]) {
                // Swap adjacent elements
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
                swapped = true;
            }
        }
        // Early exit if no swaps occurred
        if (!swapped) break;
    }
}
```
```java
public class BubbleSort {
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            // Inner loop ignores elements already bubbled to the end
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    // Swap adjacent elements
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            // Early exit if no swaps occurred
            if (!swapped) break;
        }
    }
}
```
```python
def bubble_sort(arr):
    n = len(arr)
    for i in range(n - 1):
        swapped = False
        # Inner loop ignores elements already bubbled to the end
        for j in range(0, n - 1 - i):
            if arr[j] > arr[j + 1]:
                # Swap adjacent elements
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        # Early exit if no swaps occurred
        if not swapped:
            break
```

## Summary

Bubble Sort provides a simple comparison baseline with `O(N)` best-case performance on sorted data, but scales quadratically in `O(N²)` time for unsorted inputs.
