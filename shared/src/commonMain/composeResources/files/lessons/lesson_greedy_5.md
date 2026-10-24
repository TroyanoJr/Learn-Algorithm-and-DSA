# Greedy Counterexamples and Correctness

A greedy rule can be simple and still need a proof. To evaluate one, first state the objective and the rule precisely, then check whether each local choice can be part of an optimal complete answer. A counterexample is a small valid input where the rule returns a worse answer than another method; it disproves a universal claim, but does not show that every greedy algorithm is wrong.

## Build and Test a Counterexample

For coin denominations 1, 3, and 4 with target 6, largest-first chooses 4, 1, 1 (three coins), while 3, 3 uses two. The input is valid and the objective is minimum coin count, so this is enough to show that this greedy rule does not always minimize coins for this denomination system.

By contrast, the activity-selection rule in the previous lesson is correct for maximizing the number of compatible activities, and the value-per-weight rule is correct for fractional knapsack. Correctness depends on the problem's structure and assumptions, not on whether the code looks greedy.

### Code Example

This short greedy trace exposes the coin-change counterexample. It reports the three coins chosen for target 6; it does not claim they are optimal.

```pseudocode
denominations = [4, 3, 1]  // descending
remaining = 6
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
    int remaining = 6;
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

class GreedyCounterexample {
    public static void main(String[] args) {
        int[] denominations = {4, 3, 1};
        int remaining = 6;
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
denominations = [4, 3, 1]
remaining = 6
coins = []
for coin in denominations:
    while coin <= remaining:
        coins.append(coin)
        remaining -= coin
print(coins)
```

## Two Useful Proof Ideas

- **Greedy-choice property:** there is an optimal solution that begins with the greedy choice. This lets us justify committing to that choice.
- **Optimal substructure:** after a safe choice, the remaining problem has an optimal solution that completes the overall optimum.

These are reasoning tools, not labels that automatically prove an algorithm. A proof must connect them to the exact rules and constraints of the problem. For activity selection, an earliest-finishing compatible activity leaves at least as much time for the rest. For fractional knapsack, taking the highest remaining value-per-weight portion cannot be improved by replacing it with a lower-ratio portion.

## A Practical Review Checklist

1. State the objective, legal candidates, and assumptions.
2. State exactly what “best next choice” means.
3. Try small inputs that create ties, scarce resources, or a choice that blocks later options.
4. If a counterexample appears, do not use the rule as a universal solution.
5. If no counterexample is obvious, still give a correctness argument; testing alone is not proof.

## Complexity

The counterexample trace examines at most the listed denominations plus one iteration per chosen coin, so with `d` denominations and `k` selected coins its time is `O(d + k)` when ordered from largest to smallest. This is only the cost of this implementation, not a general complexity claim for every greedy problem. Many greedy algorithms sort candidates first, adding `O(n log n)` time.

## Summary

Use counterexamples to test a greedy claim and proof reasoning to establish a correct one. Some greedy strategies fail on particular problems; others are efficient and optimal under clearly stated conditions.
