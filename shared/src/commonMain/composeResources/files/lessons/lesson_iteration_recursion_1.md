# Iteration Fundamentals

**Iteration** repeats a block of instructions, usually while a condition remains true. A loop needs an initial state, a condition that decides whether to continue, and an update that moves the state toward stopping.

## Key Concepts

- **Initialization** gives the loop variable its starting value.
- **Condition** is checked before each pass; a false condition skips or ends the loop.
- **Body** performs the work for one iteration.
- **Update** changes the state so the loop can eventually stop.

## Trace a loop

To add the integers from 1 through 4, initialize `total` to 0 and `i` to 1. Each pass adds `i`, then increments it. The condition `i <= 4` is checked again after every update:

| Pass | `i` before body | `total` after body | `i` after update |
|---:|---:|---:|---:|
| 1 | 1 | 1 | 2 |
| 2 | 2 | 3 | 3 |
| 3 | 3 | 6 | 4 |
| 4 | 4 | 10 | 5 |

At `i = 5`, the condition is false and the loop stops. A missing update could create an infinite loop; an incorrect bound can skip the final value or include an extra one.

### Code Example

All four versions sum the integers from 1 through 4 and print 10.

```pseudocode
total = 0
for i from 1 through 4:
    total = total + i
print(total)
```
```cpp
#include <iostream>
int main() {
    int total = 0;
    for (int i = 1; i <= 4; ++i) total += i;
    std::cout << total << '\n';
}
```
```java
class IterationExample {
    public static void main(String[] args) {
        int total = 0;
        for (int i = 1; i <= 4; i++) total += i;
        System.out.println(total);
    }
}
```
```python
total = 0
for i in range(1, 5):
    total += i
print(total)
```

The pseudocode uses an inclusive upper bound. Python's `range(1, 5)` stops before 5, so it visits the same values: 1, 2, 3, and 4.

## Summary

A loop initializes state, checks a condition, performs work, and updates state. Trace those parts in order to understand how many times a loop runs and whether it terminates.
