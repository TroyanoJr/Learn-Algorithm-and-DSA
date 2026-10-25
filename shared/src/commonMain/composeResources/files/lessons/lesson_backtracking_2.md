# Permutations and Unique Arrangements

A **permutation** is an ordered arrangement of all elements in a set. For `N` unique elements, there are `N!` (N factorial) possible permutations. Backtracking is the standard approach for generating all permutations.

## Generating Permutations via Swapping

To generate permutations of an array `arr`:
1. Fix an element at index `start` by swapping `arr[start]` with `arr[i]` for each `i` from `start` to `N - 1`.
2. Recursively generate permutations for the remaining subarray (`start + 1` to `N - 1`).
3. **Backtrack**: Swap `arr[start]` back with `arr[i]` to restore the original array order before testing the next choice.

## Tracking Used Elements

An alternative to element swapping is maintaining a `visited` or `used` boolean array:
- Iterate through all input elements. If `used[i]` is false, mark `used[i] = true`, append `arr[i]` to the current permutation list, recurse, and then unmark `used[i] = false` upon backtracking.

### Code Example: Generating All Permutations

The following code generates all permutations of `[1, 2]` using element swapping and backtracking.

```pseudocode
function permute(arr, start)
    if start == length(arr) - 1 then
        print(arr)
        return
    end if
    for i from start to length(arr) - 1 do
        swap(arr[start], arr[i])
        permute(arr, start + 1)
        swap(arr[start], arr[i]) // Backtrack
    end for
end function

arr = [1, 2]
permute(arr, 0)
```
```cpp
#include <iostream>
#include <vector>
#include <algorithm>

void permute(std::vector<int>& arr, int start) {
    if (start == arr.size() - 1) {
        std::cout << "[" << arr[0] << "," << arr[1] << "] ";
        return;
    }
    for (size_t i = start; i < arr.size(); ++i) {
        std::swap(arr[start], arr[i]);
        permute(arr, start + 1);
        std::swap(arr[start], arr[i]); // Backtrack
    }
}

int main() {
    std::vector<int> arr = {1, 2};
    permute(arr, 0);
    std::cout << '\n';
}
```
```java
import java.util.Arrays;

class PermutationExample {
    static void permute(int[] arr, int start) {
        if (start == arr.length - 1) {
            System.out.print(Arrays.toString(arr) + " ");
            return;
        }
        for (int i = start; i < arr.length; i++) {
            swap(arr, start, i);
            permute(arr, start + 1);
            swap(arr, start, i); // Backtrack
        }
    }

    static void swap(int[] arr, int i, int j) {
        int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
    }

    public static void main(String[] args) {
        int[] arr = {1, 2};
        permute(arr, 0);
        System.out.println();
    }
}
```
```python
def permute(arr, start=0):
    if start == len(arr) - 1:
        print(arr, end=" ")
        return
    for i in range(start, len(arr)):
        arr[start], arr[i] = arr[i], arr[start]
        permute(arr, start + 1)
        arr[start], arr[i] = arr[i], arr[start]  # Backtrack

arr = [1, 2]
permute(arr)
print()
```

## Remember

- Generating permutations explores `N!` leaf choices in the state-space tree.
- Element swapping fixes one item at `start` and restores order via a second swap (**backtrack**).
- Restoring state after recursive calls ensures the array remains intact for subsequent branches.

## Summary

Permutation generation uses backtracking to fix elements at each index, recurse on remaining indices, and undo swaps to explore all `N!` orderings.
