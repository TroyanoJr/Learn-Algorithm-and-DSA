# Bottom-Up Tabulation and Space Optimization

While Top-Down DP uses recursion with memoization, **Bottom-Up DP (Tabulation)** solves subproblems iteratively starting from the base cases and building up to the final target answer.

## Bottom-Up Tabulation vs. Top-Down Memoization

| Property | Top-Down (Memoization) | Bottom-Up (Tabulation) |
|---|---|---|
| **Approach** | Recursive (start at target, recurse down to base cases) | Iterative (start at base cases, compute up to target) |
| **Storage** | Hash map or array cache | Table array (`dp[]`) filled sequentially |
| **Call Stack** | Uses `O(N)` recursive call stack space | No call stack overhead |
| **Subproblem Evaluation** | Only computes required subproblems | Evaluates all table entries in order |

## Space Optimization (`O(N) -> O(1)`)

In many 1D DP problems, calculating state `dp[i]` only depends on a fixed number of immediately preceding states (such as `dp[i - 1]` and `dp[i - 2]`). In such cases, we can eliminate the full array and maintain only two variable values, reducing auxiliary space complexity from `O(N)` to `O(1)`.

### Code Example: Space-Optimized Bottom-Up DP (Climbing Stairs)

Finding the number of ways to climb `N` stairs (where each step takes 1 or 2 stairs) follows `dp[i] = dp[i-1] + dp[i-2]`. The following code computes this iteratively in `O(N)` time and `O(1)` space. 

*Note on Integer Overflow*: For `N >= 46`, the number of ways to climb stairs exceeds 32-bit signed integer limits (`climbStairs(46) = 2,971,215,073`). We use 64-bit integers (`long long` in C++, `long` in Java) to prevent overflow up to `N = 46` and beyond.

```pseudocode
function climbStairs(n)
    if n <= 2 then
        return n
    end if
    prev2 = 1 // Ways to climb 1 stair
    prev1 = 2 // Ways to climb 2 stairs
    
    for i from 3 to n do
        current = prev1 + prev2
        prev2 = prev1
        prev1 = current
    end for
    return prev1
end function

print(climbStairs(46))
```
```cpp
#include <iostream>

long long climbStairs(int n) {
    if (n <= 2) {
        return n;
    }
    long long prev2 = 1; // Ways for 1 stair
    long long prev1 = 2; // Ways for 2 stairs
    
    for (int i = 3; i <= n; ++i) {
        long long current = prev1 + prev2;
        prev2 = prev1;
        prev1 = current;
    }
    return prev1;
}

int main() {
    std::cout << climbStairs(46) << '\n';
}
```
```java
class StairClimbingTabulation {
    static long climbStairs(int n) {
        if (n <= 2) {
            return n;
        }
        long prev2 = 1; // Ways for 1 stair
        long prev1 = 2; // Ways for 2 stairs
        
        for (int i = 3; i <= n; i++) {
            long current = prev1 + prev2;
            prev2 = prev1;
            prev1 = current;
        }
        return prev1;
    }

    public static void main(String[] args) {
        System.out.println(climbStairs(46));
    }
}
```
```python
def climb_stairs(n):
    if n <= 2:
        return n
    prev2 = 1
    prev1 = 2
    for _ in range(3, n + 1):
        current = prev1 + prev2
        prev2 = prev1
        prev1 = current
    return prev1

print(climb_stairs(46))
```

## Remember

- **Tabulation (Bottom-Up)** builds solutions iteratively from base cases up to target `N`.
- It avoids recursion and function call stack overhead.
- When state `dp[i]` depends only on a few prior values, space can be optimized to `O(1)`.

## Summary

Bottom-Up Tabulation evaluates DP tables iteratively without recursive stack overhead. Using rolling variables optimizes 1D DP auxiliary space to `O(1)`.
