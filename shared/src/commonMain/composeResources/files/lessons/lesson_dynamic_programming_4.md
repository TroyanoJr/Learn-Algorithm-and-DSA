# 2D Subproblem Grids: Unique Paths in a Grid

Many dynamic programming problems extend 1D arrays into 2D grid matrices where each state depends on adjacent cells (such as top and left neighbors).

## Defining 2D Grid DP States

- **State Definition**: Let `dp[r][c]` represent the total number of unique paths to reach cell `(r, c)` from top-left `(0, 0)`.
- **Transitions**: Since movement is restricted to Down `(r+1, c)` and Right `(r, c+1)`, a cell can only be reached from its top cell `(r-1, c)` or left cell `(r, c-1)`:
  `dp[r][c] = dp[r-1][c] + dp[r][c-1]`
- **Base Cases**: The first row `dp[0][c] = 1` and first column `dp[r][0] = 1` each have exactly 1 unique path (moving strictly right or strictly down).

## Space Optimization (`O(R * C) -> O(C)`)

Because computing row `r` only requires values from row `r` (left) and row `r-1` (top), we can optimize 2D grid space from `O(R * C)` down to a single 1D row buffer of size `O(C)`.

### Code Example: Grid Unique Paths

The following code calculates unique paths in an `R x C` grid. 

*Note on Integer Overflow*: For larger grids (such as a `20 x 20` grid), the number of unique paths is `35,345,263,800`, which exceeds 32-bit signed integer limits. We use 64-bit integers (`long long` in C++, `long` in Java) to safely support large grid path counts up to `20 x 20` and beyond.

```pseudocode
function uniquePaths(rows, cols)
    dp = array of size cols filled with 1
    
    for r from 1 to rows - 1 do
        for c from 1 to cols - 1 do
            dp[c] = dp[c] + dp[c - 1]
        end for
    end for
    return dp[cols - 1]
end function

print(uniquePaths(20, 20))
```
```cpp
#include <iostream>
#include <vector>

long long uniquePaths(int rows, int cols) {
    std::vector<long long> dp(cols, 1);
    
    for (int r = 1; r < rows; ++r) {
        for (int c = 1; c < cols; ++c) {
            dp[c] += dp[c - 1];
        }
    }
    return dp[cols - 1];
}

int main() {
    std::cout << uniquePaths(20, 20) << '\n';
}
```
```java
import java.util.Arrays;

class UniquePathsExample {
    static long uniquePaths(int rows, int cols) {
        long[] dp = new long[cols];
        Arrays.fill(dp, 1L);
        
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                dp[c] += dp[c - 1];
            }
        }
        return dp[cols - 1];
    }

    public static void main(String[] args) {
        System.out.println(uniquePaths(20, 20));
    }
}
```
```python
def unique_paths(rows, cols):
    dp = [1] * cols
    for r in range(1, rows):
        for c in range(1, cols):
            dp[c] += dp[c - 1]
    return dp[cols - 1]

print(unique_paths(20, 20))
```

## Complexity Analysis

- **Time Complexity**: `O(R * C)`, visiting every cell once.
- **Space Complexity**: Optimized to `O(C)` auxiliary space using a 1D running row.

## Summary

2D grid DP problems sum transition paths from top and left neighbors. Using a 1D rolling array optimizes memory from `O(R * C)` to `O(C)`.
