# Constraint Satisfaction: The N-Queens Problem

The **N-Queens problem** is a classic constraint satisfaction problem: place $N$ chess queens on an $N \times N$ chessboard such that no two queens attack each other. A queen attacks any piece in the same row, column, or diagonal.

## Formulating Backtracking for N-Queens

1. **Row-by-Row Placement**: Place one queen per row, starting at row `0` down to row `N - 1`. This automatically satisfies the "one queen per row" constraint.
2. **Column & Diagonal Safety**: For row `r`, iterate through columns `c = 0` to `N - 1`. Check if placing a queen at `(r, c)` conflicts with previously placed queens:
   - Same column `c`.
   - Main diagonal (`r - c`).
   - Anti-diagonal (`r + c`).
3. **Explore and Backtrack**:
   - If `(r, c)` is safe, place the queen, mark column and diagonals as occupied, and recurse to row `r + 1`.
   - Upon return, unmark `(r, c)` (**backtrack**) and try column `c + 1`.
   - If row `r` has no safe columns, return to row `r - 1` to move the previous queen.

### Code Example: N-Queens Solution Count (N = 4)

The following code calculates the total number of valid solutions for placing $N=4$ queens on a $4 \times 4$ chessboard.

```pseudocode
function solveNQueens(row, n, cols, diag1, diag2)
    if row == n then
        return 1
    end if
    count = 0
    for col from 0 to n - 1 do
        if not cols[col] and not diag1[row - col + n] and not diag2[row + col] then
            cols[col] = true
            diag1[row - col + n] = true
            diag2[row + col] = true
            
            count = count + solveNQueens(row + 1, n, cols, diag1, diag2)
            
            cols[col] = false // Backtrack
            diag1[row - col + n] = false
            diag2[row + col] = false
        end if
    end for
    return count
end function

print(solveNQueens(0, 4, empty, empty, empty))
```
```cpp
#include <iostream>
#include <vector>

int solveNQueens(int row, int n, std::vector<bool>& cols, std::vector<bool>& diag1, std::vector<bool>& diag2) {
    if (row == n) {
        return 1;
    }
    int count = 0;
    for (int col = 0; col < n; ++col) {
        if (!cols[col] && !diag1[row - col + n] && !diag2[row + col]) {
            cols[col] = diag1[row - col + n] = diag2[row + col] = true;
            count += solveNQueens(row + 1, n, cols, diag1, diag2);
            cols[col] = diag1[row - col + n] = diag2[row + col] = false; // Backtrack
        }
    }
    return count;
}

int main() {
    int n = 4;
    std::vector<bool> cols(n, false), diag1(2 * n, false), diag2(2 * n, false);
    std::cout << solveNQueens(0, n, cols, diag1, diag2) << '\n';
}
```
```java
class NQueensExample {
    static int solveNQueens(int row, int n, boolean[] cols, boolean[] diag1, boolean[] diag2) {
        if (row == n) {
            return 1;
        }
        int count = 0;
        for (int col = 0; col < n; col++) {
            if (!cols[col] && !diag1[row - col + n] && !diag2[row + col]) {
                cols[col] = diag1[row - col + n] = diag2[row + col] = true;
                count += solveNQueens(row + 1, n, cols, diag1, diag2);
                cols[col] = diag1[row - col + n] = diag2[row + col] = false; // Backtrack
            }
        }
        return count;
    }

    public static void main(String[] args) {
        int n = 4;
        System.out.println(solveNQueens(0, n, new boolean[n], new boolean[2 * n], new boolean[2 * n]));
    }
}
```
```python
def solve_n_queens(row, n, cols, diag1, diag2):
    if row == n:
        return 1
    count = 0
    for col in range(n):
        if not cols[col] and not diag1[row - col + n] and not diag2[row + col]:
            cols[col] = diag1[row - col + n] = diag2[row + col] = True
            count += solve_n_queens(row + 1, n, cols, diag1, diag2)
            cols[col] = diag1[row - col + n] = diag2[row + col] = False  # Backtrack
    return count

n = 4
print(solve_n_queens(0, n, [False]*n, [False]*(2*n), [False]*(2*n)))
```

## Remember

- N-Queens places one queen per row, checking column and diagonal safety.
- Diagonals are tracked using `row - col + N` (main) and `row + col` (anti-diagonal).
- For $N=4$, there are exactly **2 valid solutions**.

## Summary

N-Queens demonstrates constraint satisfaction using row-by-row backtracking and fast $O(1)$ column/diagonal safety checks.
