# What Is Sorting?

Sorting is the process of arranging data elements into a specific logical order, typically ascending or descending.

Imagine sorting playing cards in your hand or organizing files alphabetically. In computer science, sorted data dramatically improves search speeds and analysis efficiency.

## Key Concepts

- **Ascending Order**: Arranging elements from smallest to largest (`[1, 2, 4, 5, 8]`).
- **Descending Order**: Arranging elements from largest to smallest (`[8, 5, 4, 2, 1]`).
- **In-Place Sorting**: Reordering elements directly within the original array without requiring significant auxiliary memory (`O(1)` extra space).

## Sorting Stability

An essential property of sorting algorithms is **stability**:

- **Stable Sort**: Preserves the relative order of elements that have equal keys. If element `A` equals element `B` and `A` appeared before `B` in the input, `A` is guaranteed to appear before `B` in the sorted output.
- **Unstable Sort**: May change the relative order of equal elements during reordering.

Stability is crucial when sorting complex objects by multiple fields (such as sorting students first by grade, then by last name).

### Code Example: Conceptual Comparison & Swap Procedure

Sorting algorithms rely on comparing pairs of elements and swapping their positions if they are out of order.

```pseudocode
// Swap two elements in an array
procedure swap(arr, i, j)
    temp = arr[i]
    arr[i] = arr[j]
    arr[j] = temp
end procedure

// Check and swap if out of ascending order
if arr[0] > arr[1] then
    swap(arr, 0, 1)
end if
```

## Summary

Sorting transforms unstructured data into an ordered sequence, enabling fast `O(log N)` search algorithms like Binary Search.
