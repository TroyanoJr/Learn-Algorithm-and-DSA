# 0/1 Knapsack Problem

The **0/1 Knapsack Problem** is a classic optimization problem: given a set of items, each with a weight and a value, determine which items to include in a collection so that the total weight is less than or equal to a given capacity `W` and the total value is maximized. Each item can be taken whole or left behind (0/1 choice).

## State Definition and Recurrence

- **State Definition**: Let `dp[i][w]` represent the maximum value achievable using a subset of the first `i` items with a maximum weight capacity `w`.
- **Choices for Item `i`**:
  1. **Exclude Item `i`**: Value remains `dp[i - 1][w]`.
  2. **Include Item `i`** (if `weight[i] <= w`): Value becomes `value[i] + dp[i - 1][w - weight[i]]`.
- **Recurrence Relation**:
  `dp[i][w] = max(dp[i-1][w], value[i] + dp[i-1][w - weight[i]])`

## Space Optimization (`O(N * W) -> O(W)`)

Since row `i` only depends on row `i-1`, we can optimize 2D space down to a single 1D array of size `O(W)` by iterating weights **backwards** from `W` down to `weight[i]`.

### Code Example: 0/1 Knapsack Problem

The following code calculates the maximum knapsack value for weights `[2, 3, 4, 5]` and values `[3, 4, 5, 6]` with capacity `5`.

```pseudocode
function knapsack(weights, values, capacity)
    dp = array of size (capacity + 1) filled with 0
    
    for i from 0 to length(weights) - 1 do
        w = weights[i]
        v = values[i]
        for c from capacity down to w do
            dp[c] = max(dp[c], v + dp[c - w])
        end for
    end for
    return dp[capacity]
end function

print(knapsack([2, 3], [3, 4], 5))
```
```cpp
#include <iostream>
#include <vector>
#include <algorithm>

int knapsack(const std::vector<int>& weights, const std::vector<int>& values, int capacity) {
    std::vector<int> dp(capacity + 1, 0);
    
    for (size_t i = 0; i < weights.size(); ++i) {
        int w = weights[i];
        int v = values[i];
        for (int c = capacity; c >= w; --c) {
            dp[c] = std::max(dp[c], v + dp[c - w]);
        }
    }
    return dp[capacity];
}

int main() {
    std::vector<int> weights = {2, 3};
    std::vector<int> values = {3, 4};
    std::cout << knapsack(weights, values, 5) << '\n';
}
```
```java
class KnapsackExample {
    static int knapsack(int[] weights, int[] values, int capacity) {
        int[] dp = new int[capacity + 1];
        
        for (int i = 0; i < weights.length; i++) {
            int w = weights[i];
            int v = values[i];
            for (int c = capacity; c >= w; c--) {
                dp[c] = Math.max(dp[c], v + dp[c - w]);
            }
        }
        return dp[capacity];
    }

    public static void main(String[] args) {
        int[] weights = {2, 3};
        int[] values = {3, 4};
        System.out.println(knapsack(weights, values, 5));
    }
}
```
```python
def knapsack(weights, values, capacity):
    dp = [0] * (capacity + 1)
    for w_i, v_i in zip(weights, values):
        for c in range(capacity, w_i - 1, -1):
            dp[c] = max(dp[c], v_i + dp[c - w_i])
    return dp[capacity]

print(knapsack([2, 3], [3, 4], 5))
```

## Complexity Analysis

- **Time Complexity**: `O(N * W)`, where `N` is the number of items and `W` is knapsack capacity.
- **Space Complexity**: Optimized to `O(W)` auxiliary space using a 1D backward-iterating table.

## Summary

The 0/1 Knapsack problem evaluates whether to include or exclude items. Iterating capacity backwards in a 1D array optimizes space to `O(W)`.
