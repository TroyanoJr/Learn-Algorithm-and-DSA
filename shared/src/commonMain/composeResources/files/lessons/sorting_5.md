# Comparing Sorting Algorithms

Comparing Bubble Sort, Selection Sort, and Insertion Sort clarifies how different strategies impact algorithm efficiency and performance on various datasets.

## Key Comparison

- **Bubble Sort**: Repeatedly compares adjacent elements and swaps an out-of-order pair. Each pass moves a large remaining value toward the end. Standard Bubble Sort takes `O(n^2)` comparisons even on sorted input; a version that stops when a pass makes no swaps has best-case `O(n)` time on already-sorted input. Worst case is `O(n^2)`.
- **Selection Sort**: Scans the unsorted portion for its smallest value and places it in the next position. It makes about `n(n-1)/2` comparisons regardless of initial order, so best and worst cases are `O(n^2)`; it performs at most `n-1` swaps.
- **Insertion Sort**: Takes the next value and inserts it into the correct position in the already-sorted portion, shifting larger values right. It takes `O(n)` in the best case (already sorted) and `O(n^2)` in the worst case (for example, reverse order). Nearly sorted input usually needs few shifts, so it is often efficient there.

The standard versions of Bubble Sort and Insertion Sort are stable: equal values retain their relative order. Standard Selection Sort is not stable because swapping the minimum into place can move an equal value past another. All three use `O(1)` auxiliary space when sorting the input array in place; that does not count the input array itself.

## A Short Trace

Starting with `[5, 3, 4, 1]`, Insertion Sort first inserts `3` before `5`, giving `[3, 5, 4, 1]`. It then inserts `4` between them, giving `[3, 4, 5, 1]`. Finally it shifts `5`, `4`, and `3` right to insert `1`, giving `[1, 3, 4, 5]`. Each step grows the sorted prefix by one. Selection Sort instead scans the unsorted suffix for its minimum each pass; even after the first pass places `1`, it still scans the remaining suffixes, which explains why its comparisons do not benefit from nearly sorted input.

These algorithms are useful for learning, small inputs, and (especially for Insertion Sort) nearly sorted inputs. Their quadratic worst-case work makes them poor general choices for large datasets.

## Summary

Choice of elementary sorting algorithm depends on data size and initial ordering: Insertion Sort excels on nearly sorted data, Selection Sort minimizes swaps, and Bubble Sort provides a simple comparison baseline.
