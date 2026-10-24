# Recursion Patterns and Trade-offs

Recursion can express repeated work compactly, but it uses a call frame for each active call. An iterative loop often uses constant extra space; a recursive version may use space proportional to its maximum call depth. Choose the form that is clearest and safe for the expected input size.

## Key Concepts

- A recursive trace first follows calls down to a base case, then follows returned values back up.
- A recursive sum of n values takes O(n) time and O(n) call-stack space.
- Deep recursion can exceed the runtime's call-stack limit even when the algorithm's total work is otherwise reasonable.
- Common defects include a missing base case, a recursive argument that does not get smaller, and a base case that is unreachable for some input.

## Compare recursion with iteration

For summing values 1 through n, both approaches perform O(n) additions. A loop can use O(1) auxiliary space. A direct recursive version creates O(n) active calls. Recursion can make a naturally nested problem easier to express, but it does not automatically reduce the amount of work.

### Code Example

This recursive version returns the sum from 1 through `n`; the base case stops when `n` reaches zero.

```pseudocode
function sumTo(n):
    if n == 0: return 0
    return n + sumTo(n - 1)
print(sumTo(4))
```
```cpp
#include <iostream>
int sumTo(int n) {
    if (n == 0) return 0;
    return n + sumTo(n - 1);
}
int main() { std::cout << sumTo(4) << '\n'; }
```
```java
class RecursiveSum {
    static int sumTo(int n) {
        if (n == 0) return 0;
        return n + sumTo(n - 1);
    }
    public static void main(String[] args) {
        System.out.println(sumTo(4));
    }
}
```
```python
def sum_to(n):
    if n == 0:
        return 0
    return n + sum_to(n - 1)

print(sum_to(4))
```

The result is 10. For large n, a loop avoids the O(n) call-stack depth; for either version, use a numeric type large enough for the expected sum.

## Summary

Iteration and recursion can perform the same repeated work with different control flow and space costs. Trace both the descent and return steps of recursion, and ensure every call moves toward a reachable base case.
