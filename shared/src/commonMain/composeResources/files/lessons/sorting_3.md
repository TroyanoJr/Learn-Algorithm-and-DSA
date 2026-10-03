# Selection Sort

Selection sort divides the array into a sorted and an unsorted region, repeatedly finding the smallest element in the unsorted region and swapping it to the end of the sorted region.

Imagine picking the smallest card from a hand and placing it into a new organized pile one by one.

## Key Concepts

- **Minimum Selection**: Scans the remaining unsorted elements to find the absolute minimum value.
- **Single Swap Per Pass**: Swaps the found minimum directly into its target position.
- **Growing Sorted Region**: The left boundary of the sorted region expands by one element each pass.

### Code Example

```kotlin
fun selectionSort(arr: IntArray) {
    for (i in 0 until arr.size - 1) {
        var minIdx = i
        for (j in i + 1 until arr.size) {
            if (arr[j] < arr[minIdx]) minIdx = j
        }
        val temp = arr[minIdx]
        arr[minIdx] = arr[i]
        arr[i] = temp
    }
}
```

## Complexity

- **Best Case**: O(n²) because it always scans the entire unsorted region.
- **Worst Case**: O(n²).

## Summary

Selection sort minimizes the total number of swaps (O(n) swaps total) but always requires O(n²) comparisons.
