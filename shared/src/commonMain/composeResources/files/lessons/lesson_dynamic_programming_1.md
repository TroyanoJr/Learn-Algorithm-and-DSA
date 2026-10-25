# Dynamic Programming Fundamentals and Memoization

**Dynamic Programming (DP)** is a powerful algorithmic technique used to solve complex problems by breaking them down into simpler, overlapping subproblems, solving each subproblem once, and storing their solutions to avoid redundant computations.

## When to Use Dynamic Programming

A problem can be efficiently solved using Dynamic Programming if it exhibits two key properties:

1. **Optimal Substructure**: An optimal solution to the overall problem can be constructed from optimal solutions to its subproblems.
2. **Overlapping Subproblems**: The recursive solution solves the exact same subproblems repeatedly rather than generating unique subproblems at every step.

## Top-Down DP (Memoization)

**Memoization** (Top-Down) retains a recursive problem-solving approach while using a lookup table (such as an array or hash map) to cache previously computed subproblem results:
- **Before computing**: Check if the result for the current subproblem state is already in the lookup table (cache). If present, return it immediately in `O(1)` time.
- **After computing**: Store the computed result in the lookup table before returning it.

### Code Example: Fibonacci with Top-Down Memoization

The standard recursive Fibonacci calculation takes exponential `O(2^N)` time because it recomputes `Fib(N-2)` repeatedly. Memoization stores computed values, reducing time complexity to linear `O(N)`. 

*Note on Integer Overflow*: Standard 32-bit signed integers overflow for `N >= 47` (`Fib(47) = 2,971,215,073`). We use 64-bit integers (`long long` in C++, `long` in Java) to safely support large Fibonacci numbers up to `N = 47` and beyond.

```pseudocode
function fibMemo(n, memo)
    if n <= 1 then
        return n
    end if
    if memo[n] != -1 then
        return memo[n] // Return cached result
    end if
    memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo)
    return memo[n]
end function

// n = 47
memo = array of size 48 filled with -1
print(fibMemo(47, memo))
```
```cpp
#include <iostream>
#include <vector>

long long fibMemo(int n, std::vector<long long>& memo) {
    if (n <= 1) {
        return n;
    }
    if (memo[n] != -1) {
        return memo[n]; // Return cached result
    }
    memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
    return memo[n];
}

int main() {
    int n = 47;
    std::vector<long long> memo(n + 1, -1);
    std::cout << fibMemo(n, memo) << '\n';
}
```
```java
import java.util.Arrays;

class FibonacciMemoization {
    static long fibMemo(int n, long[] memo) {
        if (n <= 1) {
            return n;
        }
        if (memo[n] != -1) {
            return memo[n]; // Return cached result
        }
        memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
        return memo[n];
    }

    public static void main(String[] args) {
        int n = 47;
        long[] memo = new long[n + 1];
        Arrays.fill(memo, -1L);
        System.out.println(fibMemo(n, memo));
    }
}
```
```python
def fib_memo(n, memo=None):
    if memo is None:
        memo = [-1] * (n + 1)
    if n <= 1:
        return n
    if memo[n] != -1:
        return memo[n]  # Return cached result
    memo[n] = fib_memo(n - 1, memo) + fib_memo(n - 2, memo)
    return memo[n]

print(fib_memo(47))
```

## Remember

- DP requires **optimal substructure** and **overlapping subproblems**.
- **Memoization (Top-Down)** caches recursive function calls to eliminate redundant operations.
- Memoizing Fibonacci reduces execution time from exponential `O(2^N)` to linear `O(N)`.

## Summary

Dynamic Programming solves overlapping subproblems once. Memoization stores recursive results in a lookup table, transforming exponential recursion into `O(N)` execution.
