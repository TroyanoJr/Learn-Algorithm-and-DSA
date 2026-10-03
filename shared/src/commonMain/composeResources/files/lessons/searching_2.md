# Linear Search

Linear search is a straightforward search algorithm that inspects elements sequentially one by one from start to finish until the target is found or the array ends.

Imagine checking every door along a hallway sequentially until you find the room number you need.

## Key Concepts

- **Sequential Inspection**: Elements are checked one after another from index 0.
- **Best Case O(1)**: Target is at index 0 on the very first comparison.
- **Worst Case O(n)**: Target is at the last position or not in the array.

### Code Example

```kotlin
fun linearSearch(arr: IntArray, target: Int): Int {
    for (i in arr.indices) {
        if (arr[i] == target) return i // Target found
    }
    return -1 // Target not found
}
```

## Summary

Linear search works on both sorted and unsorted collections, making n comparisons in the worst case.
