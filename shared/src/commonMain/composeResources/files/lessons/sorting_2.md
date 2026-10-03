# Bubble Sort

Bubble sort is a simple comparison-based algorithm that repeatedly steps through the list, compares adjacent elements, and swaps them if they are in the wrong order.

Imagine bubbles rising in water. Larger elements gradually "bubble up" to the end of the array with each pass.

## Key Concepts

- **Adjacent Comparison**: Compares pairs of elements `arr[i]` and `arr[i+1]`.
- **Bubbling Effect**: The largest unsorted element moves to its final position at the end of each pass.
- **Early Exit Optimization**: If a pass completes with zero swaps, the array is already sorted.

### Code Example

```kotlin
fun bubbleSort(arr: IntArray) {
    for (i in 0 until arr.size - 1) {
        var swapped = false
        for (j in 0 until arr.size - 1 - i) {
            if (arr[j] > arr[j + 1]) {
                val temp = arr[j]
                arr[j] = arr[j + 1]
                arr[j + 1] = temp
                swapped = true
            }
        }
        if (!swapped) break // Early exit O(n) best case
    }
}
```

## Complexity

- **Best Case**: O(n) with early-exit optimization on pre-sorted data.
- **Worst Case**: O(n²) when array is in reverse order.

## Summary

Bubble sort is simple to understand but inefficient (O(n²)) for large datasets due to repeated adjacent swaps.
