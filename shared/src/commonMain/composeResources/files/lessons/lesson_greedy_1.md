# Greedy Algorithm Fundamentals

A **greedy algorithm** builds an answer through a sequence of choices. At each step it selects the candidate that looks best according to a local rule, then continues without undoing earlier choices. The local choice is not automatically the best choice for the whole problem: a greedy method is correct only when the problem's structure makes those choices safe.

## Local and Global Choices

A **local optimum** is the best-looking choice at the current step. A **global optimum** is the best complete answer among all valid answers. Greedy methods are attractive because they are often simple and fast, but an appealing local choice can lead to a poor global result.

For example, with coin values 1, 3, and 4, a largest-coin-first rule for amount 6 chooses 4, then 1, then 1: three coins. Choosing 3 and 3 uses two. This does not make greedy algorithms useless; it shows why a rule must be justified for the particular problem.

## A Small Greedy Trace

Suppose a delivery robot wants to visit as many stops as possible before a distance budget of 10 is used. Stop distances are positive and independent: 2, 3, 4, and 6 units. To maximize the number of stops, choose the shortest remaining stop each time.

- Start with budget 10 and zero stops.
- Choose distance 2: remaining budget 8, one stop.
- Choose distance 3: remaining budget 5, two stops.
- Choose distance 4: remaining budget 1, three stops.
- Distance 6 no longer fits, so stop. The selected set is 2, 3, and 4.

For this particular objective, choosing shorter distances first is safe: replacing a selected longer stop with a shorter available one cannot reduce the number of stops that fit. A different objective or constraints could require a different rule.

### Code Example

The pseudocode and programs apply the shortest-first rule to the same independent stop distances. The selected distances are 2, 3, and 4.

```pseudocode
distances = [2, 3, 4, 6]
sort distances in ascending order
remaining = 10
chosen = []
for distance in distances:
    if distance <= remaining:
        append distance to chosen
        remaining = remaining - distance
print(chosen)
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>

int main() {
    std::vector<int> distances{2, 3, 4, 6};
    std::sort(distances.begin(), distances.end());
    int remaining = 10;
    std::vector<int> chosen;
    for (int distance : distances) {
        if (distance <= remaining) {
            chosen.push_back(distance);
            remaining -= distance;
        }
    }
    for (int distance : chosen) std::cout << distance << ' ';
    std::cout << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GreedyFundamentals {
    public static void main(String[] args) {
        int[] distances = {2, 3, 4, 6};
        Arrays.sort(distances);
        int remaining = 10;
        List<Integer> chosen = new ArrayList<>();
        for (int distance : distances) {
            if (distance <= remaining) {
                chosen.add(distance);
                remaining -= distance;
            }
        }
        System.out.println(chosen);
    }
}
```
```python
distances = [2, 3, 4, 6]
distances.sort()
remaining = 10
chosen = []
for distance in distances:
    if distance <= remaining:
        chosen.append(distance)
        remaining -= distance
print(chosen)
```

## Complexity and When to Use Greedy

Sorting `n` candidates costs `O(n log n)` time; the following scan costs `O(n)`, so sorting dominates. The chosen list uses `O(n)` extra space in these examples. If candidates are already sorted and the answer is emitted directly, the scan is `O(n)` and extra storage can be constant.

Greedy methods work well when a locally best choice can be shown to belong to an optimal complete solution. The next lessons examine specific problems where the rule succeeds and where it can fail.

## Summary

A greedy algorithm commits to locally attractive choices. Always identify its rule, the conditions under which choices are safe, and whether a counterexample can defeat it before relying on the result.
