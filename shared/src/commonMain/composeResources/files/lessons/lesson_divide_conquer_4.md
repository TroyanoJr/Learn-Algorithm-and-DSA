# Merge Sort Implementation and Complexity

Building upon the split-and-merge concept, a complete **Merge Sort** implementation recursively divides an array down to individual elements and merges them back together. Analyzing its time and space complexity reveals why it is a reliable sorting algorithm.

## Complexity Analysis

- **Time Complexity**: 
  - **Best, Average, and Worst Case**: O(N \log N).
  - *Explanation*: The array is divided in half log N times (recursion tree depth). At each level of recursion, merging all sublists requires O(N) total work. Multiplying depth (log N) by work per level (O(N)) yields O(N \log N).
- **Space Complexity**: 
  - **Auxiliary Space**: O(N).
  - *Explanation*: Merge sort requires temporary arrays or buffers during the merge step to store combined elements, making it an *out-of-place* sorting algorithm.
- **Stability**: Merge sort is a **stable** sorting algorithm because equal elements retain their relative input order during the merge step.

### Code Example: Full Merge Sort Implementation

The following code implements the complete recursive Merge Sort algorithm.

```pseudocode
function mergeSort(arr)
    if length(arr) <= 1 then
        return arr
    end if
    mid = length(arr) / 2
    left = subArray(arr, 0, mid)
    right = subArray(arr, mid, length(arr))
    
    sortedLeft = mergeSort(left)
    sortedRight = mergeSort(right)
    
    return merge(sortedLeft, sortedRight)
end function
```
```cpp
#include <iostream>
#include <vector>

std::vector<int> merge(const std::vector<int>& left, const std::vector<int>& right) {
    std::vector<int> result;
    size_t i = 0, j = 0;
    while (i < left.size() && j < right.size()) {
        if (left[i] <= right[j]) result.push_back(left[i++]);
        else result.push_back(right[j++]);
    }
    while (i < left.size()) result.push_back(left[i++]);
    while (j < right.size()) result.push_back(right[j++]);
    return result;
}

std::vector<int> mergeSort(std::vector<int> arr) {
    if (arr.size() <= 1) return arr;
    size_t mid = arr.size() / 2;
    std::vector<int> left(arr.begin(), arr.begin() + mid);
    std::vector<int> right(arr.begin() + mid, arr.end());
    return merge(mergeSort(left), mergeSort(right));
}

int main() {
    std::vector<int> arr = {38, 27, 43, 3, 9, 82, 10};
    std::vector<int> sorted = mergeSort(arr);
    for (int v : sorted) std::cout << v << " ";
    std::cout << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.List;

class MergeSortFull {
    static List<Integer> merge(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i) <= right.get(j)) result.add(left.get(i++));
            else result.add(right.get(j++));
        }
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    static List<Integer> mergeSort(List<Integer> arr) {
        if (arr.size() <= 1) return arr;
        int mid = arr.size() / 2;
        List<Integer> left = new ArrayList<>(arr.subList(0, mid));
        List<Integer> right = new ArrayList<>(arr.subList(mid, arr.size()));
        return merge(mergeSort(left), mergeSort(right));
    }

    public static void main(String[] args) {
        List<Integer> arr = List.of(38, 27, 43, 3, 9, 82, 10);
        List<Integer> sorted = mergeSort(arr);
        System.out.println(sorted);
    }
}
```
```python
def merge(left, right):
    result = []
    i = j = 0
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1
    result.extend(left[i:])
    result.extend(right[j:])
    return result

def merge_sort(arr):
    if len(arr) <= 1:
        return arr
    mid = len(arr) // 2
    left = merge_sort(arr[:mid])
    right = merge_sort(arr[mid:])
    return merge(left, right)

arr = [38, 27, 43, 3, 9, 82, 10]
print(merge_sort(arr))
```

## Remember

- Merge Sort runs in **O(N \log N)** time across all cases (best, average, worst).
- It requires **O(N) auxiliary space** for temporary merging buffers.
- Merge Sort is **stable**, preserving the relative order of equal elements.

## Summary

Merge Sort achieves guaranteed O(N \log N) sorting time by dividing arrays recursively and merging sorted halves, at the cost of linear auxiliary memory.
