# Linear vs. Binary Search

Comparing Linear Search and Binary Search highlights why algorithm selection depends on data ordering and search efficiency requirements.

## Key Differences

- **Data Requirement**: Linear search works on any array; Binary search requires sorted data.
- **Search Method**: Linear inspects elements one by one; Binary eliminates half the search space at each step.
- **Complexity**: Linear search can inspect every element (`O(n)` worst case). Binary search discards about half the remaining interval per step (`O(log n)`) when the input is sorted.

## Worked Example

Use the ascending array `[4, 12, 19, 27, 33, 44, 51, 60, 72, 81, 95]` and search for `72`. Here, one element comparison means comparing the target with one inspected array value; binary search's interval update follows from that comparison.

### Linear Search

Check in order: `4`, `12`, `19`, `27`, `33`, `44`, `51`, `60`, `72`. The ninth examined value matches, so the target takes 9 element comparisons to find.

### Binary Search

The array is sorted ascending. The boundaries `left` and `right` are inclusive, and each midpoint is `floor((left + right) / 2)`.

1. `left = 0`, `right = 10`; `mid = floor((0 + 10) / 2) = 5`. The value is `44`. Since `72 > 44`, continue at indices `6..10` (`left = 6`, `right = 10`).
2. `left = 6`, `right = 10`; `mid = floor((6 + 10) / 2) = 8`. The value is `72`, so the target is found.

That is 2 element comparisons: one with `44` and one with `72`. These counts describe this target in this array, not every search. Linear search works whether or not data is sorted; binary search needs sorted input. Binary search can greatly reduce inspections on large sorted data, but sorting data first has a cost, and linear search can be simpler or preferable for small or unsorted data.

## Summary

For this example, linear search inspects 9 elements and binary search inspects 2. The result illustrates the algorithms' different growth rates; it is not a universal comparison count.
