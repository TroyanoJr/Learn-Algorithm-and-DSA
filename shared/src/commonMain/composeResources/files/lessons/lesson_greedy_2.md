# Greedy Coin Change

The **coin change** problem asks us to make a target amount from available coin denominations. A common greedy rule repeatedly takes the largest denomination that does not exceed the remaining amount. This rule is easy to apply, but it does not produce a minimum number of coins for every denomination system.

## Applying the Largest-Coin Rule

Use denominations 1, 3, and 4, and make amount 11. The coins are considered from largest to smallest:

- Remaining 11: take 4; remaining 7.
- Remaining 7: take 4; remaining 3.
- Remaining 3: take 3; remaining 0.

The result is 4 + 4 + 3, using three coins. For this amount, the greedy result is also minimal: no two available coins sum to 11.

## A Counterexample

With the same denominations and amount 6, greedy takes 4, then 1, then 1, for three coins. The combination 3 + 3 uses only two. Thus largest-first is not a universally correct minimum-coin algorithm. Some denomination systems, such as many familiar currency systems, have properties that make greedy change work, but that depends on the system; do not assume it for arbitrary values.

### Code Example

The code assumes the denomination list is already sorted from largest to smallest. For amount 11, it prints `[4, 4, 3]`.

```pseudocode
denominations = [4, 3, 1]  // descending
remaining = 11
coins = []
for coin in denominations:
    while coin <= remaining:
        append coin to coins
        remaining = remaining - coin
print(coins)
```
```cpp
#include <iostream>
#include <vector>

int main() {
    const std::vector<int> denominations{4, 3, 1};
    int remaining = 11;
    std::vector<int> coins;
    for (int coin : denominations) {
        while (coin <= remaining) {
            coins.push_back(coin);
            remaining -= coin;
        }
    }
    for (int coin : coins) std::cout << coin << ' ';
    std::cout << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.List;

class GreedyCoinChange {
    public static void main(String[] args) {
        int[] denominations = {4, 3, 1}; // descending
        int remaining = 11;
        List<Integer> coins = new ArrayList<>();
        for (int coin : denominations) {
            while (coin <= remaining) {
                coins.add(coin);
                remaining -= coin;
            }
        }
        System.out.println(coins);
    }
}
```
```python
denominations = [4, 3, 1]  # descending
remaining = 11
coins = []
for coin in denominations:
    while coin <= remaining:
        coins.append(coin)
        remaining -= coin
print(coins)
```

## Complexity and Edge Cases

With `d` denominations already sorted and `k` coins selected, the loop takes `O(d + k)` time and stores `O(k)` coins in the output. If sorting is needed, that adds `O(d log d)`. A zero amount returns no coins. With positive denominations, if no denomination can be selected while the remainder is positive, the amount cannot be formed; a complete implementation should report that instead of claiming success. The examples use a formable amount.

Greedy coin change minimizes the coin count only when the denomination system supports that greedy rule. To guarantee a minimum for arbitrary denominations, another method such as dynamic programming may be needed.

## Summary

Largest-first is a useful strategy to test, not a universal proof of optimality. Check the denomination assumptions, confirm the remainder reaches zero, and look for counterexamples.
