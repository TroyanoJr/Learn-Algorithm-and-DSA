# Binary Search

Binary search is an efficient search algorithm that repeatedly divides a sorted search space in half to locate a target element.

Imagine opening a dictionary in the middle. If the target word comes alphabetically before the middle page, you discard the entire right half and repeat the process on the left half.

## Key Concepts

- **Prerequisite: Sorted Input**: This algorithm requires input elements to be sorted in **ascending order**. The comparisons below rely on that order.
- **Divide and Conquer**: `left` and `right` are the inclusive indices of the current search interval. `mid` is an index halfway between them. If `arr[mid]` is smaller than the target, every index through `mid` can be discarded, so set `left = mid + 1`. If it is larger, discard `mid` and everything after it by setting `right = mid - 1`. Equality means the target is found.
- **Integer Overflow Protection**: Rather than calculating `mid = (left + right) / 2` (which can cause integer overflow in fixed-precision types like C++ and Java when `left + right` exceeds maximum integer value), we calculate:
  `mid = left + (right - left) / 2`
- **Time Complexity O(log N)**: Halves the remaining search range at each step, taking roughly log₂(N) comparisons in the worst case.
- **Auxiliary Space `O(1)`**: Operates iteratively using only boundary variables.

### Worked Example

The sorted array is `[1, 3, 5, 7, 9, 11, 13, 15, 17, 19]`, and the target is `11`. Indices start at `0`, so the values are at indices `0` through `9`.

1. `left = 0`, `right = 9`; `mid = 4`, where the value is `9`. Since `9 < 11`, eliminate indices `0` through `4` and continue in the range `5` through `9` by setting `left = 5`.
2. `left = 5`, `right = 9`; `mid = 7`, where the value is `15`. Since `15 > 11`, eliminate indices `7` through `9` and continue in the range `5` through `6` by setting `right = 6`.
3. `left = 5`, `right = 6`; `mid = 5`, where the value is `11`. The target is found at index `5` on the third comparison.

### Code Example: Iterative Binary Search

```pseudocode
function binarySearch(arr, target):
    left = 0
    right = length(arr) - 1

    while left <= right do
        // Midpoint calculation with integer overflow protection
        mid = left + (right - left) / 2

        if arr[mid] == target then
            return mid // Target found at index mid
        else if arr[mid] < target then
            left = mid + 1 // Target is in right half
        else
            right = mid - 1 // Target is in left half
        end if
    end while

    return -1 // Target not found
```
```cpp
int binarySearch(const int arr[], int size, int target) {
    int left = 0;
    int right = size - 1;

    while (left <= right) {
        // Overflow-safe midpoint calculation
        int mid = left + (right - left) / 2;

        if (arr[mid] == target) {
            return mid; // Target found
        } else if (arr[mid] < target) {
            left = mid + 1; // Search right half
        } else {
            right = mid - 1; // Search left half
        }
    }

    return -1; // Target not found
}
```
```java
static int binarySearch(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;

    while (left <= right) {
        // Overflow-safe midpoint calculation
        int mid = left + (right - left) / 2;

        if (arr[mid] == target) {
            return mid; // Target found
        } else if (arr[mid] < target) {
            left = mid + 1; // Search right half
        } else {
            right = mid - 1; // Search left half
        }
    }

    return -1; // Target not found
}
```
```python
def binary_search(arr, target):
    left = 0
    right = len(arr) - 1

    while left <= right:
        mid = left + (right - left) // 2

        if arr[mid] == target:
            return mid # Target found
        elif arr[mid] < target:
            left = mid + 1 # Search right half
        else:
            right = mid - 1 # Search left half

    return -1 # Target not found
```

## Summary

By eliminating half the remaining elements with every comparison, Binary Search locates items in sorted data in logarithmic `O(log N)` time and `O(1)` space.
