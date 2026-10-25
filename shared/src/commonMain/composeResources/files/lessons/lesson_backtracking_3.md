# Combinations and Subsets with Pruning

While permutations focus on element order, **combinations** and **subsets** focus on element selection regardless of order. Backtracking with **pruning** allows us to find valid combinations efficiently while discarding invalid branches early.

## Avoiding Duplicate Combinations

To generate unique combinations without duplicates:
- Pass a `startIndex` parameter into the recursive function.
- In each recursive call, only consider candidates at index `i >= startIndex`. This enforces that elements are selected in left-to-right order, preventing duplicate arrangements (like `[1, 2]` and `[2, 1]`).

## Bounding Functions and Pruning

**Pruning** is the act of stopping recursion down a branch that cannot possibly lead to a valid solution.
- For example, in **Combination Sum** (finding elements that sum to target `T`):
  - If `currentSum == T`, record the combination (valid base case).
  - If `currentSum > T`, stop recursing and backtrack immediately (**prune**).

### Code Example: Combination Sum with Pruning

The following code finds combinations of `[2, 3]` that sum to target `5`, pruning branches whenever the sum exceeds `5`.

```pseudocode
function findCombinations(candidates, target, startIndex, currentList, currentSum)
    if currentSum == target then
        print(currentList)
        return
    end if
    if currentSum > target then
        return // Prune invalid branch
    end if
    for i from startIndex to length(candidates) - 1 do
        currentList.add(candidates[i])
        findCombinations(candidates, target, i, currentList, currentSum + candidates[i])
        currentList.removeLast() // Backtrack
    end for
end function

findCombinations([2, 3], 5, 0, [], 0)
```
```cpp
#include <iostream>
#include <vector>

void findCombinations(const std::vector<int>& candidates, int target, int startIndex,
                      std::vector<int>& currentList, int currentSum) {
    if (currentSum == target) {
        std::cout << "[";
        for (size_t i = 0; i < currentList.size(); ++i) {
            std::cout << currentList[i] << (i + 1 == currentList.size() ? "" : ",");
        }
        std::cout << "] ";
        return;
    }
    if (currentSum > target) {
        return; // Prune
    }
    for (size_t i = startIndex; i < candidates.size(); ++i) {
        currentList.push_back(candidates[i]);
        findCombinations(candidates, target, i, currentList, currentSum + candidates[i]);
        currentList.pop_back(); // Backtrack
    }
}

int main() {
    std::vector<int> candidates = {2, 3};
    std::vector<int> currentList;
    findCombinations(candidates, 5, 0, currentList, 0);
    std::cout << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.List;

class CombinationSumExample {
    static void findCombinations(int[] candidates, int target, int startIndex,
                                 List<Integer> currentList, int currentSum) {
        if (currentSum == target) {
            System.out.print(currentList + " ");
            return;
        }
        if (currentSum > target) {
            return; // Prune
        }
        for (int i = startIndex; i < candidates.length; i++) {
            currentList.add(candidates[i]);
            findCombinations(candidates, target, i, currentList, currentSum + candidates[i]);
            currentList.remove(currentList.size() - 1); // Backtrack
        }
    }

    public static void main(String[] args) {
        int[] candidates = {2, 3};
        findCombinations(candidates, 5, 0, new ArrayList<>(), 0);
        System.out.println();
    }
}
```
```python
def find_combinations(candidates, target, start_index, current_list, current_sum):
    if current_sum == target:
        print(current_list, end=" ")
        return
    if current_sum > target:
        return  # Prune
    for i in range(start_index, len(candidates)):
        current_list.append(candidates[i])
        find_combinations(candidates, target, i, current_list, current_sum + candidates[i])
        current_list.pop()  # Backtrack

find_combinations([2, 3], 5, 0, [], 0)
print()
```

## Remember

- Use `startIndex` to prevent duplicate combination ordering.
- **Pruning** halts recursion early when constraints are violated (`currentSum > target`).
- Backtracking cleans up `currentList` (`pop_back()`) to restore state for subsequent candidate loops.

## Summary

Combinations explore element subsets. Using `startIndex` avoids duplicate arrangements, and pruning invalid sums dramatically reduces state-space search time.
