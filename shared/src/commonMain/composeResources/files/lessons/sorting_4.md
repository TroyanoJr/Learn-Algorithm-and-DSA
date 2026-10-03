# Insertion Sort

Insertion sort builds the final sorted array one item at a time by taking each element from the unsorted portion and inserting it into its correct position within the sorted portion.

Imagine sorting cards in your hand: you take one card at a time and slide it into its proper spot among the cards you already hold.

## Key Concepts

- **Sorted Subarray**: Maintains a growing sorted subarray on the left.
- **Shifting Elements**: Shifts larger elements to the right to make space for the inserted item.
- **Adaptive Performance**: Performs very few operations when data is nearly sorted.

### Code Example

```kotlin
fun insertionSort(arr: IntArray) {
    for (i in 1 until arr.size) {
        val key = arr[i]
        var j = i - 1
        while (j >= 0 && arr[j] > key) {
            arr[j + 1] = arr[j] // Shift right
            j--
        }
        arr[j + 1] = key // Insert key
    }
}
```

## Complexity

- **Best Case**: O(n) when input is already sorted (0 shifts needed).
- **Worst Case**: O(n²) when input is in reverse order.

## Summary

Insertion sort is efficient for small datasets or nearly sorted data, taking O(n) best-case time.
