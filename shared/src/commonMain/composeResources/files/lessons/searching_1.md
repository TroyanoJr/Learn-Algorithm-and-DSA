# What Is Searching?

Searching is the process of locating a specific element, called the **target**, within a collection of data.

Imagine looking for a friend's name in an address book or finding a specific song in a playlist. In computer science, searching is one of the most fundamental operations performed daily.

## Key Concepts

- **Target**: The specific value or item you are trying to find.
- **Index Return**: Search functions typically return the zero-based index where the target is located.
- **Sentinel Return Value (-1)**: If the target is not present in the collection, the search function conventionally returns `-1`.
- **Search Strategy**: The algorithm or approach used to inspect elements (e.g., checking sequentially vs. halving search space).

### Code Example: Explicit Target Search Loop

The following search logic checks elements sequentially, returning the index if matched or `-1` if not found.

```pseudocode
function findTarget(arr, target):
    for i from 0 to length(arr) - 1 do
        if arr[i] == target then
            return i // Target found at index i
        end if
    end for
    return -1 // Target not found
```
```cpp
#include <vector>

int findTarget(const std::vector<int>& arr, int target) {
    for (int i = 0; i < arr.size(); i++) {
        if (arr[i] == target) {
            return i; // Target found at index i
        }
    }
    return -1; // Target not found
}
```
```java
public class SearchConcept {
    public static int findTarget(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i; // Target found at index i
            }
        }
        return -1; // Target not found
    }
}
```
```python
def find_target(arr, target):
    for i in range(len(arr)):
        if arr[i] == target:
            return i # Target found at index i
    return -1 # Target not found
```

## Summary

Different search strategies require different numbers of steps depending on whether the data is sorted and how elements are inspected.
