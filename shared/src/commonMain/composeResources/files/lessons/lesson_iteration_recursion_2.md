# Nested Iteration and Loop Control

A **nested loop** places one loop inside another. For each pass of the outer loop, the inner loop runs from its own initialization until its own condition becomes false. If the outer loop runs `m` times and the inner loop runs `n` times on every outer pass, the body runs `m × n` times.

## Key Concepts

- The inner loop starts over for each outer-loop pass.
- `break` exits the nearest enclosing loop.
- `continue` skips the rest of the current pass of the nearest loop and proceeds to its next pass.
- If inner-loop work varies by outer pass, count each pass rather than assuming a product.

## Count nested iterations

If `i` runs from 1 through 2 and `j` runs from 1 through 3 each time, the body runs six times: two outer passes times three inner passes. A break in the inner loop stops only that inner loop; the outer loop can continue.

### Code Example

The nested loops print six coordinate pairs in row order. The inner loop resets `j` to 1 for each new `i`.

```pseudocode
for i from 1 through 2:
    for j from 1 through 3:
        print(i, j)
```
```cpp
#include <iostream>
int main() {
    for (int i = 1; i <= 2; ++i)
        for (int j = 1; j <= 3; ++j)
            std::cout << i << ", " << j << '\n';
}
```
```java
class NestedLoopExample {
    public static void main(String[] args) {
        for (int i = 1; i <= 2; i++)
            for (int j = 1; j <= 3; j++)
                System.out.println(i + ", " + j);
    }
}
```
```python
for i in range(1, 3):
    for j in range(1, 4):
        print(i, j)
```

The output is `(1, 1)`, `(1, 2)`, `(1, 3)`, then `(2, 1)`, `(2, 2)`, `(2, 3)`. `continue` is useful when one input should be skipped but later passes should still run; use it carefully so loop updates still occur.

## Summary

Nested loops repeat inner work for every outer pass. Count the passes of each loop, and remember that break and continue affect the nearest loop only.
