# Unbounded Choice: Coin Change Problem

The **Coin Change Problem** asks for the minimum number of coins needed to make a target amount `W` using a given set of coin denominations. Because each coin denomination can be reused infinitely, this is an **unbounded choice** DP problem.

## Defining DP States and Recurrence

- **State Definition**: Let `dp[w]` represent the minimum number of coins needed to make amount `w`.
- **Base Case**: `dp[0] = 0` (0 coins are needed to make amount 0). All other `dp[w]` values are initialized to infinity (`infinity`).
- **State Transition**: For each amount `w` from 1 to `W`, iterate through every coin `c` in the denomination list:
  `if w >= c: dp[w] = min(dp[w], 1 + dp[w - c])`
- **Unreachable Amount**: If `dp[W]` remains infinity after evaluating all states, returning `-1` indicates that the amount cannot be formed.

### Code Example: Minimum Coin Change (Unbounded)

The following code computes the minimum coins needed to make amount `11` using coin denominations `[1, 2, 5]`.

```pseudocode
function coinChange(coins, amount)
    dp = array of size (amount + 1) filled with infinity
    dp[0] = 0
    
    for w from 1 to amount do
        for each coin in coins do
            if w >= coin and dp[w - coin] != infinity then
                dp[w] = min(dp[w], 1 + dp[w - coin])
            end if
        end for
    end for
    
    if dp[amount] == infinity then return -1
    else return dp[amount]
end function

print(coinChange([1, 2, 5], 11))
```
```cpp
#include <iostream>
#include <vector>
#include <algorithm>

int coinChange(const std::vector<int>& coins, int amount) {
    const int INF = 1e9;
    std::vector<int> dp(amount + 1, INF);
    dp[0] = 0;

    for (int w = 1; w <= amount; ++w) {
        for (int coin : coins) {
            if (w >= coin && dp[w - coin] != INF) {
                dp[w] = std::min(dp[w], 1 + dp[w - coin]);
            }
        }
    }
    return dp[amount] == INF ? -1 : dp[amount];
}

int main() {
    std::vector<int> coins = {1, 2, 5};
    std::cout << coinChange(coins, 11) << '\n';
}
```
```java
import java.util.Arrays;

class CoinChangeExample {
    static int coinChange(int[] coins, int amount) {
        int INF = 1000000000;
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;

        for (int w = 1; w <= amount; w++) {
            for (int coin : coins) {
                if (w >= coin && dp[w - coin] != INF) {
                    dp[w] = Math.min(dp[w], 1 + dp[w - coin]);
                }
            }
        }
        return dp[amount] == INF ? -1 : dp[amount];
    }

    public static void main(String[] args) {
        int[] coins = {1, 2, 5};
        System.out.println(coinChange(coins, 11));
    }
}
```
```python
def coin_change(coins, amount):
    INF = float('inf')
    dp = [INF] * (amount + 1)
    dp[0] = 0

    for w in range(1, amount + 1):
        for coin in coins:
            if w >= coin:
                dp[w] = min(dp[w], 1 + dp[w - coin])

    return dp[amount] if dp[amount] != INF else -1

print(coin_change([1, 2, 5], 11))
```

## Complexity Analysis

- **Time Complexity**: `O(W * C)`, where `W` is the target amount and `C` is the number of coin denominations.
- **Space Complexity**: `O(W)` for the 1D DP table.

## Summary

Coin Change solves unbounded choice problems by updating 1D DP state `dp[w]` using previous subproblems `dp[w - coin]`. Initialize states to infinity and set base case `dp[0] = 0`.
