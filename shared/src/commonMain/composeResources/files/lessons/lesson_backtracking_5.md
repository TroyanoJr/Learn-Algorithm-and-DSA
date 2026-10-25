# Grid Pathfinding and Matrix Backtracking

Backtracking is widely used to find paths in 2D grid matrices (such as mazes or word search puzzles). The search moves through valid neighboring cells while marking visited positions to avoid cycles.

## 2D Matrix Search Protocol

When exploring a 2D grid from cell `(r, c)`:
1. **Check Validity**: Return false if `(r, c)` is out of bounds, an obstacle, or already visited.
2. **Base Case**: Return true if `(r, c)` reaches the target cell (e.g. bottom-right corner `(R - 1, C - 1)`).
3. **Mark Visited**: Mark `grid[r][c]` as visited (or occupied).
4. **Explore Directions**: Recursively attempt moving in 4 directions: Down `(r + 1, c)`, Right `(r, c + 1)`, Up `(r - 1, c)`, and Left `(r, c - 1)`.
5. **Unmark (Backtrack)**: Unmark `grid[r][c]` so that other potential paths can pass through this cell if needed.

### Code Example: Grid Path Existence Search

The following code checks if a valid path exists from `(0, 0)` to `(1, 1)` in a $2 \times 2$ grid with open cells `1` and wall `0`.

```pseudocode
function hasPath(grid, r, c, targetR, targetC)
    if r < 0 or r >= 2 or c < 0 or c >= 2 or grid[r][c] == 0 then
        return false
    end if
    if r == targetR and c == targetC then
        return true
    end if
    grid[r][c] = 0 // Mark visited
    
    found = hasPath(grid, r + 1, c, targetR, targetC) or
            hasPath(grid, r, c + 1, targetR, targetC) or
            hasPath(grid, r - 1, c, targetR, targetC) or
            hasPath(grid, r, c - 1, targetR, targetC)
            
    grid[r][c] = 1 // Backtrack
    return found
end function

grid = [ [1, 1], [0, 1] ]
print(hasPath(grid, 0, 0, 1, 1))
```
```cpp
#include <iostream>
#include <vector>

bool hasPath(std::vector<std::vector<int>>& grid, int r, int c, int targetR, int targetC) {
    if (r < 0 || r >= 2 || c < 0 || c >= 2 || grid[r][c] == 0) {
        return false;
    }
    if (r == targetR && c == targetC) {
        return true;
    }
    grid[r][c] = 0; // Mark visited

    bool found = hasPath(grid, r + 1, c, targetR, targetC) ||
                 hasPath(grid, r, c + 1, targetR, targetC) ||
                 hasPath(grid, r - 1, c, targetR, targetC) ||
                 hasPath(grid, r, c - 1, targetR, targetC);

    grid[r][c] = 1; // Backtrack
    return found;
}

int main() {
    std::vector<std::vector<int>> grid = {{1, 1}, {0, 1}};
    std::cout << std::boolalpha << hasPath(grid, 0, 0, 1, 1) << '\n';
}
```
```java
class GridPathfindingExample {
    static boolean hasPath(int[][] grid, int r, int c, int targetR, int targetC) {
        if (r < 0 || r >= 2 || c < 0 || c >= 2 || grid[r][c] == 0) {
            return false;
        }
        if (r == targetR && c == targetC) {
            return true;
        }
        grid[r][c] = 0; // Mark visited

        boolean found = hasPath(grid, r + 1, c, targetR, targetC) ||
                        hasPath(grid, r, c + 1, targetR, targetC) ||
                        hasPath(grid, r - 1, c, targetR, targetC) ||
                        hasPath(grid, r, c - 1, targetR, targetC);

        grid[r][c] = 1; // Backtrack
        return found;
    }

    public static void main(String[] args) {
        int[][] grid = {{1, 1}, {0, 1}};
        System.out.println(hasPath(grid, 0, 0, 1, 1));
    }
}
```
```python
def has_path(grid, r, c, target_r, target_c):
    if r < 0 or r >= 2 or c < 0 or c >= 2 or grid[r][c] == 0:
        return False
    if r == target_r and c == target_c:
        return True
    grid[r][c] = 0  # Mark visited

    found = (has_path(grid, r + 1, c, target_r, target_c) or
             has_path(grid, r, c + 1, target_r, target_c) or
             has_path(grid, r - 1, c, target_r, target_c) or
             has_path(grid, r, c - 1, target_r, target_c))

    grid[r][c] = 1  # Backtrack
    return found

grid = [[1, 1], [0, 1]]
print(has_path(grid, 0, 0, 1, 1))
```

## Remember

- Grid search explores 4 directions (Down, Right, Up, Left).
- Temporary marking prevents infinite loop cycles between adjacent cells.
- Restoring grid cells during backtracking allows other search branches to use those cells.

## Summary

2D grid backtracking explores paths directionally while temporarily marking cells to prevent cycles and restoring them upon backtracking.
