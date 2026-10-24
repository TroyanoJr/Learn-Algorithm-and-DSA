# Recursive Binary Search

**Binary Search** is a classic divide-and-conquer algorithm used to find the position of a target value within a **sorted array**. Instead of scanning every element linearly (O(N)), binary search cuts the search space in half with every comparison, achieving O(\log N) time complexity.

## Key Concepts and Prerequisites

- **Sorted Input**: The implementation below requires the input array to be sorted in ascending order. If unsorted, results are unpredictable.
- **Divide**: Compare the target with the middle element of the current range `[left, right]`.
- **Conquer**: If the target matches the middle element, return its index. If the target is smaller, recurse on the left half. If larger, recurse on the right half.
- **Base Cases**: Either the target is found, or the search range becomes invalid (`left > right`), indicating the target is absent (return `-1`).

### Code Example: Recursive Binary Search

The following code implements binary search recursively using divide and conquer.

```pseudocode
function binarySearch(arr, target, left, right)
    if left > right then
        return -1
    end if
    mid = left + (right - left) / 2
    if arr[mid] == target then
        return mid
    else if arr[mid] > target then
        return binarySearch(arr, target, left, mid - 1)
    else
        return binarySearch(arr, target, mid + 1, right)
    end if
end function
```
```cpp
#include <iostream>
#include <vector>

int binarySearch(const std::vector<int>& arr, int target, int left, int right) {
    if (left > right) {
        return -1;
    }
    int mid = left + (right - left) / 2;
    if (arr[mid] == target) {
        return mid;
    } else if (arr[mid] > target) {
        return binarySearch(arr, target, left, mid - 1);
    } else {
        return binarySearch(arr, target, mid + 1, right);
    }
}

int main() {
    std::vector<int> arr = {2, 5, 8, 12, 16};
    std::cout << binarySearch(arr, 12, 0, arr.size() - 1) << '\n';
}
```
```java
class RecursiveBinarySearch {
    static int binarySearch(int[] arr, int target, int left, int right) {
        if (left > right) {
            return -1;
        }
        int mid = left + (right - left) / 2;
        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] > target) {
            return binarySearch(arr, target, left, mid - 1);
        } else {
            return binarySearch(arr, target, mid + 1, right);
        }
    }

    public static void main(String[] args) {
        int[] arr = {2, 5, 8, 12, 16};
        System.out.println(binarySearch(arr, 12, 0, arr.length - 1));
    }
}
```
```python
def binary_search(arr, target, left, right):
    if left > right:
        return -1
    mid = left + (right - left) // 2
    if arr[mid] == target:
        return mid
    elif arr[mid] > target:
        return binary_search(arr, target, left, mid - 1)
    else:
        return binary_search(arr, target, mid + 1, right)

arr = [2, 5, 8, 12, 16]
print(binary_search(arr, 12, 0, len(arr) - 1))
```

## Remember

- Binary search requires a **sorted** array.
- At each step, it eliminates half the remaining elements, yielding **O(\log N)** search time.
- Termination occurs when `left > right` (target absent) or when `arr[mid] == target`.

## Summary

Recursive binary search applies divide and conquer to sorted arrays by halving the search range in logarithmic time.
