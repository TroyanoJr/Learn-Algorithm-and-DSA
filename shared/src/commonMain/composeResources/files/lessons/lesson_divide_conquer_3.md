# Merge Sort Fundamentals

**Merge Sort** is an efficient, comparison-based sorting algorithm built on the divide-and-conquer paradigm. Invented by John von Neumann in 1945, Merge Sort guarantees O(N \log N) performance in all cases (best, average, and worst).

## Key Concepts of Merge Sort

1. **Divide**: Split the unsorted array of size $N$ into two halves approximately equal in size ($N/2$ each).
2. **Conquer**: Sort each half recursively by applying merge sort to them repeatedly until subproblems reach size 1 (a single element is inherently sorted).
3. **Combine**: **Merge** the two sorted halves back together into a single, fully sorted array.

## The Role of the Merge Step

The power of Merge Sort lies entirely in the **merge** operation. Merging two separate sorted arrays into one combined sorted array can be performed efficiently in linear time (O(N)). 

- Compare the front elements of both sorted halves.
- Select the smaller element and append it to the temporary merged output.
- Advance the pointer in the half from which the element was chosen.
- Repeat until all elements from both halves are combined in sorted order.

### Code Example: Merging Two Sorted Halves

The following code illustrates merging two separate sorted arrays into one sorted result.

```pseudocode
function mergeArrays(leftArr, rightArr)
    result = []
    i = 0
    j = 0
    while i < length(leftArr) and j < length(rightArr) do
        if leftArr[i] <= rightArr[j] then
            result.add(leftArr[i])
            i = i + 1
        else
            result.add(rightArr[j])
            j = j + 1
        end if
    end while
    // Append remaining elements
    while i < length(leftArr) do
        result.add(leftArr[i])
        i = i + 1
    end while
    while j < length(rightArr) do
        result.add(rightArr[j])
        j = j + 1
    end while
    return result
end function
```
```cpp
#include <iostream>
#include <vector>

std::vector<int> mergeArrays(const std::vector<int>& leftArr, const std::vector<int>& rightArr) {
    std::vector<int> result;
    size_t i = 0, j = 0;
    while (i < leftArr.size() && j < rightArr.size()) {
        if (leftArr[i] <= rightArr[j]) {
            result.push_back(leftArr[i++]);
        } else {
            result.push_back(rightArr[j++]);
        }
    }
    while (i < leftArr.size()) result.push_back(leftArr[i++]);
    while (j < rightArr.size()) result.push_back(rightArr[j++]);
    return result;
}

int main() {
    std::vector<int> left = {1, 4, 7};
    std::vector<int> right = {2, 5, 8};
    std::vector<int> merged = mergeArrays(left, right);
    for (int val : merged) std::cout << val << " ";
    std::cout << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.List;

class MergeStepExample {
    static List<Integer> mergeArrays(List<Integer> leftArr, List<Integer> rightArr) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < leftArr.size() && j < rightArr.size()) {
            if (leftArr.get(i) <= rightArr.get(j)) {
                result.add(leftArr.get(i++));
            } else {
                result.add(rightArr.get(j++));
            }
        }
        while (i < leftArr.size()) result.add(leftArr.get(i++));
        while (j < rightArr.size()) result.add(rightArr.get(j++));
        return result;
    }

    public static void main(String[] args) {
        List<Integer> left = List.of(1, 4, 7);
        List<Integer> right = List.of(2, 5, 8);
        List<Integer> merged = mergeArrays(left, right);
        System.out.println(merged);
    }
}
```
```python
def merge_arrays(left_arr, right_arr):
    result = []
    i = j = 0
    while i < len(left_arr) and j < len(right_arr):
        if left_arr[i] <= right_arr[j]:
            result.append(left_arr[i])
            i += 1
        else:
            result.append(right_arr[j])
            j += 1
    result.extend(left_arr[i:])
    result.extend(right_arr[j:])
    return result

left = [1, 4, 7]
right = [2, 5, 8]
print(merge_arrays(left, right))
```

## Remember

- Merge Sort divides an array in half, sorts recursively, and combines via a linear-time **merge** step.
- Base cases occur when sublists have length 1 (already sorted).
- The merge step preserves all elements and guarantees sorted output.

## Summary

Merge Sort divides arrays recursively down to single elements and merges sorted halves back together in linear time, guaranteeing predictable performance.
