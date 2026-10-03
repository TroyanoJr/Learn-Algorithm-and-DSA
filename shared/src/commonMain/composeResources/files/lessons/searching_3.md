# Binary Search

Binary search is an efficient search algorithm that repeatedly divides a sorted search space in half to locate the target.

Imagine opening a dictionary in the middle. If the target word comes before the middle page, you discard the entire right half and repeat.

## Key Concepts

- **Requires Sorted Data**: Binary search MUST have sorted input to eliminate halves correctly.
- **Divide and Conquer**: Compares target with middle element mid = (left + right) / 2.
- **Worst Case O(log n)**: Eliminates 50% of remaining items at each step.

### Code Example

```kotlin
fun binarySearch(arr: IntArray, target: Int): Int {
    var left = 0
    var right = arr.lastIndex
    while (left <= right) {
        val mid = left + (right - left) / 2
        if (arr[mid] == target) return mid
        if (arr[mid] < target) left = mid + 1 else right = mid - 1
    }
    return -1
}
```

## Summary

By eliminating half the remaining elements with each comparison, Binary Search achieves logarithmic O(log n) performance on sorted data.
