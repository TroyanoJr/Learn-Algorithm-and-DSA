# Quick Sort Fundamentals and Partitioning

**Quick Sort** is another highly efficient, comparison-based sorting algorithm using divide and conquer. Developed by Tony Hoare in 1959, Quick Sort is often faster in practice than Merge Sort due to excellent cache locality and in-place sorting, though its worst-case time complexity can be quadratic (O(N^2)).

## Key Concepts of Quick Sort

1. **Pivot Selection**: Choose an element from the array to act as the **pivot** (e.g. the first element, last element, median, or a random element).
2. **Partitioning**: Rearrange the array so that:
   - All elements smaller than the pivot are placed to its left.
   - All elements greater than the pivot are placed to its right.
   - The pivot is placed in its exact final sorted position.
3. **Conquer**: Recursively apply quick sort to the sub-array of elements to the left of the pivot and the sub-array to the right of the pivot.
4. **Combine**: No explicit combine step is needed because partitioning places elements into their correct sorted positions in place.

## The Partitioning Step

Partitioning is the heart of Quick Sort. This lesson uses the Lomuto partition scheme: it scans the array, swaps elements smaller than or equal to the pivot into a growing left region, and finally swaps the pivot into its correct dividing index.

### Code Example: Partitioning and Quick Sort

The following code implements Quick Sort using an in-place partition scheme.

```pseudocode
function quickSort(arr, low, high)
    if low < high then
        pi = partition(arr, low, high)
        quickSort(arr, low, pi - 1)
        quickSort(arr, pi + 1, high)
    end if
end function

function partition(arr, low, high)
    pivot = arr[high]
    i = low - 1
    for j from low to high - 1 do
        if arr[j] <= pivot then
            i = i + 1
            swap(arr[i], arr[j])
        end if
    end for
    swap(arr[i + 1], arr[high])
    return i + 1
end function
```
```cpp
#include <iostream>
#include <vector>
#include <algorithm>

int partition(std::vector<int>& arr, int low, int high) {
    int pivot = arr[high];
    int i = low - 1;
    for (int j = low; j < high; ++j) {
        if (arr[j] <= pivot) {
            i++;
            std::swap(arr[i], arr[j]);
        }
    }
    std::swap(arr[i + 1], arr[high]);
    return i + 1;
}

void quickSort(std::vector<int>& arr, int low, int high) {
    if (low < high) {
        int pi = partition(arr, low, high);
        quickSort(arr, low, pi - 1);
        quickSort(arr, pi + 1, high);
    }
}

int main() {
    std::vector<int> arr = {10, 7, 8, 9, 1, 5};
    quickSort(arr, 0, arr.size() - 1);
    for (int v : arr) std::cout << v << " ";
    std::cout << '\n';
}
```
```java
import java.util.Arrays;

class QuickSortExample {
    static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
            }
        }
        int temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;
        return i + 1;
    }

    static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    public static void main(String[] args) {
        int[] arr = {10, 7, 8, 9, 1, 5};
        quickSort(arr, 0, arr.length - 1);
        System.out.println(Arrays.toString(arr));
    }
}
```
```python
def partition(arr, low, high):
    pivot = arr[high]
    i = low - 1
    for j in range(low, high):
        if arr[j] <= pivot:
            i += 1
            arr[i], arr[j] = arr[j], arr[i]
    arr[i + 1], arr[high] = arr[high], arr[i + 1]
    return i + 1

def quick_sort(arr, low, high):
    if low < high:
        pi = partition(arr, low, high)
        quick_sort(arr, low, pi - 1)
        quick_sort(arr, pi + 1, high)

arr = [10, 7, 8, 9, 1, 5]
quick_sort(arr, 0, len(arr) - 1)
print(arr)
```

## Remember

- Quick Sort selects a **pivot**, **partitions** elements around it, and recurses on sub-arrays.
- Partitioning places the pivot in its exact final sorted position without needing a combine step.
- Performance depends heavily on pivot balance and partition quality.

## Summary

Quick Sort uses divide and conquer by partitioning arrays around a pivot element. Its in-place partitioning makes it extremely fast in practice.
