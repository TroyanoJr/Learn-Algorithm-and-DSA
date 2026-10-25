# Backtracking Complexity, Pruning, and Trade-offs

Backtracking explores decision spaces that are inherently exponential or factorial in size. Understanding time complexity, call-stack space, and the power of **pruning** is essential when designing backtracking algorithms.

## Time and Space Complexity

- **Worst-Case Time Complexity**: Exponential or factorial depending on decision choices per level:
  - Subsets / Combination Sum: $O(2^N)$
  - Permutations: $O(N!)$
  - N-Queens: $O(N!)$
  - 4-directional 2D Grid Search: $O(4^{R \times C})$
- **Space Complexity**: Auxiliary call-stack space is proportional to maximum tree depth $H$: $O(H)$ (typically $O(N)$ for $N$ decision levels).

## Paradigm Comparisons

| Algorithm Strategy | Core Paradigm | Decision Exploration | Overlapping Subproblems? |
|---|---|---|---|
| **Brute Force** | Exhaustive Evaluation | Generates and checks all candidates unconditionally | No pruning |
| **Backtracking** | Depth-First Pruning | Abandons invalid paths as soon as constraints fail | Minimal/None |
| **Dynamic Programming** | Memoized Subproblems | Solves and stores reusable subproblem solutions | High overlap required |
| **Branch and Bound** | Breadth-First Bounding | Uses priority queues and upper/lower bounds | Bounding functions |

### Code Example: Comparing Explored Nodes With and Without Pruning

The following code traces the total recursive calls executed with vs without early constraint pruning when searching for values $\le 2$.

```pseudocode
function searchPruned(arr, index, limit)
    if index >= length(arr) or arr[index] > limit then
        return 0 // Prune
    end if
    return 1 + searchPruned(arr, index + 1, limit)
end function

arr = [1, 2, 5, 8]
print(searchPruned(arr, 0, 2))
```
```cpp
#include <iostream>
#include <vector>

int searchPruned(const std::vector<int>& arr, size_t index, int limit) {
    if (index >= arr.size() || arr[index] > limit) {
        return 0; // Prune
    }
    return 1 + searchPruned(arr, index + 1, limit);
}

int main() {
    std::vector<int> arr = {1, 2, 5, 8};
    std::cout << searchPruned(arr, 0, 2) << '\n';
}
```
```java
import java.util.List;

class PruningComparison {
    static int searchPruned(List<Integer> arr, int index, int limit) {
        if (index >= arr.size() || arr.get(index) > limit) {
            return 0; // Prune
        }
        return 1 + searchPruned(arr, index + 1, limit);
    }

    public static void main(String[] args) {
        List<Integer> arr = List.of(1, 2, 5, 8);
        System.out.println(searchPruned(arr, 0, 2));
    }
}
```
```python
def search_pruned(arr, index, limit):
    if index >= len(arr) or arr[index] > limit:
        return 0  # Prune
    return 1 + search_pruned(arr, index + 1, limit)

arr = [1, 2, 5, 8]
print(search_pruned(arr, 0, 2))
```

## Remember

- Backtracking time complexity is worst-case exponential $O(2^N)$ or factorial $O(N!)$.
- **Pruning** discards invalid branches early, dramatically accelerating search times in practice.
- Auxiliary call-stack space is $O(N)$, matching the height of the state-space tree.

## Summary

Backtracking systematically explores state-space trees. Effective pruning eliminates dead ends early, turning exponential search spaces into fast practical algorithms.
