# Recursion Fundamentals

**Recursion** solves a problem by having a function call itself on a smaller version of that problem. Each call pauses while the smaller call runs, so the program keeps call information on the call stack.

## Key Concepts

- A **base case** returns an answer without another recursive call.
- A **recursive case** makes progress toward a base case by using a smaller input.
- Calls return in reverse order: the deepest call returns first.
- Missing a reachable base case or failing to make progress can cause unbounded calls and stack exhaustion.

## Trace recursive calls

Consider `sumTo(n)`, which returns `1 + 2 + ... + n`. Its base case is `n == 0`; otherwise it returns `n + sumTo(n - 1)`. For input 3, calls descend to `sumTo(0)`, then return upward:

| Call | Waits for | Return value |
|---|---|---:|
| `sumTo(3)` | `sumTo(2)` | 6 |
| `sumTo(2)` | `sumTo(1)` | 3 |
| `sumTo(1)` | `sumTo(0)` | 1 |
| `sumTo(0)` | base case | 0 |

The return values combine as `1 + 0 = 1`, `2 + 1 = 3`, and `3 + 3 = 6`.

### Code Example

Each implementation returns the sum from 1 to `n`; the base case returns zero for `n == 0`.

```pseudocode
function sumTo(n):
    if n == 0: return 0
    return n + sumTo(n - 1)
print(sumTo(3))
```
```cpp
#include <iostream>
int sumTo(int n) {
    if (n == 0) return 0;
    return n + sumTo(n - 1);
}
int main() { std::cout << sumTo(3) << '\n'; }
```
```java
class RecursionExample {
    static int sumTo(int n) {
        if (n == 0) return 0;
        return n + sumTo(n - 1);
    }
    public static void main(String[] args) {
        System.out.println(sumTo(3));
    }
}
```
```python
def sum_to(n):
    if n == 0:
        return 0
    return n + sum_to(n - 1)

print(sum_to(3))
```

Each version prints `6`. For nonnegative n, this function takes O(n) time and O(n) call-stack space because it makes n recursive calls before returning.

## Summary

Every recursive function needs a base case and a recursive step that makes progress. The call stack stores unfinished calls until the base case allows results to return.
