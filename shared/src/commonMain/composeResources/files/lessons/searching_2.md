# Linear Search

Linear search is a straightforward search algorithm that inspects elements sequentially one by one from start to finish until the target is found or the array ends.

Imagine checking every door along a hallway sequentially until you find the room number you need.

## Key Concepts

- **Sequential Inspection**: Elements are checked one after another starting from index `0`.
- **Works on Unsorted Data**: Does not require input to be sorted beforehand.
- **Best-Case `O(1)`**: Target is located at index `0` on the very first comparison.
- **Worst-Case `O(N)`**: Target is at the last index or missing entirely, requiring `N` comparisons.
- **Auxiliary Space `O(1)`**: Operates in-place using only a loop variable.

### Code Example

```pseudocode
function linearSearch(arr, target):
    for i from 0 to length(arr) - 1 do
        if arr[i] == target then
            return i // Target found
        end if
    end for
    return -1 // Target not in array
```
```cpp
#include <vector>

int linearSearch(const std::vector<int>& arr, int target) {
    for (int i = 0; i < arr.size(); i++) {
        if (arr[i] == target) {
            return i; // Target found
        }
    }
    return -1; // Target not in array
}
```
```java
public class LinearSearch {
    public static int linearSearch(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i; // Target found
            }
        }
        return -1; // Target not in array
    }
}
```
```python
def linear_search(arr, target):
    for i in range(len(arr)):
        if arr[i] == target:
            return i # Target found
    return -1 # Target not in array
```

## Summary

Linear search works on both sorted and unsorted collections, making at most `N` comparisons in `O(N)` linear time and `O(1)` auxiliary space.
