# Brute Force Fundamentals

**Brute force** is a straightforward problem-solving strategy: define the possible candidates, examine them systematically, and check which candidates satisfy the goal. It is useful when the candidate set is small, when a simple solution is needed first, or when we need a reliable baseline to compare with a faster method.

## How Exhaustive Search Works

1. Describe what one candidate represents.
2. List or generate every relevant candidate in a predictable order.
3. Test each candidate against the problem's condition.
4. Return a valid candidate, or report that none exists.

If the task asks for *any* valid answer, a search may stop as soon as it finds one. To prove that no answer exists, or to find the best answer among all candidates, it must inspect every relevant candidate. The method is only correct when the candidate generation covers all possibilities the problem allows.

## Guided Trace

Find an integer from 1 through 5 whose square is 16. Let `candidate` be the number being tested and start `solution` at `-1` to mean “not found yet.”

- Candidate 1: `1 * 1 = 1`, so continue.
- Candidate 2: `2 * 2 = 4`, so continue.
- Candidate 3: `3 * 3 = 9`, so continue.
- Candidate 4: `4 * 4 = 16`, so save 4 and stop because any one solution is enough.

If no candidate matched, `solution` would remain `-1`. The bound matters: checking only 1 through 3 would incorrectly miss the solution.

### Code Example

Each version checks the same bounded candidate range and prints `4`.

```pseudocode
solution = -1
for candidate from 1 through 5:
    if candidate * candidate == 16:
        solution = candidate
        break
print(solution)
```
```cpp
#include <iostream>

int main() {
    int solution = -1;
    for (int candidate = 1; candidate <= 5; ++candidate) {
        if (candidate * candidate == 16) {
            solution = candidate;
            break;
        }
    }
    std::cout << solution << '\n';
}
```
```java
class BruteForceFundamentals {
    public static void main(String[] args) {
        int solution = -1;
        for (int candidate = 1; candidate <= 5; candidate++) {
            if (candidate * candidate == 16) {
                solution = candidate;
                break;
            }
        }
        System.out.println(solution);
    }
}
```
```python
solution = -1
for candidate in range(1, 6):
    if candidate * candidate == 16:
        solution = candidate
        break
print(solution)
```

## Complexity and Limitations

If there are `k` candidates and each check takes constant time, checking every candidate takes `O(k)` time. This example uses `O(1)` extra space. If a problem has many candidates, or each candidate has many parts to check, exhaustive search can become slow. Correctness also depends on choosing a complete candidate set and checking each candidate correctly.

## Summary

Brute force favors a simple, systematic check over clever pruning. It is dependable for small search spaces and useful as a baseline, but its running time grows with the number of candidates.
