# Comparing Sorting Algorithms

Comparing Bubble Sort, Selection Sort, and Insertion Sort clarifies how different strategies impact algorithm efficiency and performance on various datasets.

## Key Comparison

- **Bubble Sort**: Compares adjacent elements and swaps them. $O(n)$ best with early exit, $O(n^2)$ worst.
- **Selection Sort**: Finds the minimum element and swaps once per pass. Always $O(n^2)$ comparisons.
- **Insertion Sort**: Shifts larger elements right and inserts key into sorted portion. $O(n)$ best on nearly sorted data, $O(n^2)$ worst.

## Summary

Choice of elementary sorting algorithm depends on data size and initial ordering: Insertion Sort excels on nearly sorted data, Selection Sort minimizes swaps, and Bubble Sort provides a simple comparison baseline.
