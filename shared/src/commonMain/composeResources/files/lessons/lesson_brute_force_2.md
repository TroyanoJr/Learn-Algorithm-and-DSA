# Brute Force for Pair Problems

A common brute-force task is to find two different values that satisfy a relationship. For example, given a list and a target, find two elements whose sum equals the target. We do not assume that the list is sorted or that a faster lookup structure has been built.

## Examine Each Pair Once

Use an outer loop to choose the first index `i`. For each `i`, an inner loop starts at `i + 1` and checks every later index `j`. Starting after `i` avoids pairing an element with itself and avoids checking the same unordered pair twice. For a list of `n` elements, the checks are:

- `i = 0`: compare index 0 with indices 1, 2, and 3.
- `i = 1`: compare index 1 with indices 2 and 3.
- `i = 2`: compare index 2 with index 3.
- `i = 3`: there are no later indices to compare.

For `[2, 7, 11, 15]` and target `9`, the first comparison is `2 + 7 = 9`, so the algorithm returns indices `(0, 1)`. If it finds no pair, it returns an empty result after checking all pairs.

### Code Example

Each version returns the indices of the first matching pair, or an empty result when no pair exists.

```pseudocode
function findPair(values, target):
    for i from 0 to length(values) - 1:
        for j from i + 1 to length(values) - 1:
            if values[i] + values[j] == target:
                return (i, j)
    return NONE

print(findPair([2, 7, 11, 15], 9))
```
```cpp
#include <iostream>
#include <utility>
#include <vector>

std::pair<int, int> findPair(const std::vector<int>& values, int target) {
    for (int i = 0; i < static_cast<int>(values.size()); ++i) {
        for (int j = i + 1; j < static_cast<int>(values.size()); ++j) {
            if (values[i] + values[j] == target) return {i, j};
        }
    }
    return {-1, -1};
}

int main() {
    const std::vector<int> values{2, 7, 11, 15};
    const auto pair = findPair(values, 9);
    if (pair.first != -1) std::cout << pair.first << ", " << pair.second << '\n';
}
```
```java
class BruteForcePairs {
    static int[] findPair(int[] values, int target) {
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                if (values[i] + values[j] == target) return new int[] {i, j};
            }
        }
        return new int[] {-1, -1};
    }

    public static void main(String[] args) {
        int[] pair = findPair(new int[] {2, 7, 11, 15}, 9);
        if (pair[0] != -1) System.out.println(pair[0] + ", " + pair[1]);
    }
}
```
```python
def find_pair(values, target):
    for i in range(len(values)):
        for j in range(i + 1, len(values)):
            if values[i] + values[j] == target:
                return (i, j)
    return None

print(find_pair([2, 7, 11, 15], 9))
```

## Complexity

There are `n(n - 1) / 2` distinct pairs. In the worst case, the algorithm checks all of them, so its time complexity is `O(n^2)`. It uses `O(1)` extra space, not counting the returned pair. Returning as soon as a match is found can make a particular run faster, but does not change the worst-case bound.

## Edge Cases

- With fewer than two values, no pair can be formed.
- Duplicate values at different indices are allowed; for example, `[4, 4]` can make target `8`.
- Negative numbers and zero work with the same sum comparison.
- This version returns one pair only. To return every matching pair, it must continue checking after a match.

## Summary

The nested loops enumerate each unordered pair once without requiring sorted input. This makes the approach easy to verify, while the quadratic number of checks can be expensive for large lists.
