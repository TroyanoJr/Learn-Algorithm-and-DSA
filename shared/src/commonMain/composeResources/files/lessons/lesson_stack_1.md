# Stack Fundamentals and LIFO

A **stack** is a collection where adding and removing items happens at one end, called the **top**. Its rule is **last in, first out (LIFO)**: the newest item is the first one removed. A pile of plates is a useful model: add a plate on top, and take the top plate first.

## Key Concepts

- **LIFO**: the most recently pushed item is the next item popped.
- **Top**: the end used for stack insertion, removal, and inspection.
- **Empty stack**: has no item to peek at or pop; callers must follow the API's empty behavior.

## Guided trace

Write the top at the right. Start empty, then perform `push(A)`, `push(B)`, `push(C)`, `push(D)`, `push(E)`, `pop()`, `push(F)`, `pop()`:

| Operation | Stack (bottom → top) | Result |
|---|---|---|
| start | `[]` | — |
| push A | `[A]` | — |
| push B | `[A, B]` | — |
| push C | `[A, B, C]` | — |
| push D | `[A, B, C, D]` | — |
| push E | `[A, B, C, D, E]` | — |
| pop | `[A, B, C, D]` | `E` |
| push F | `[A, B, C, D, F]` | — |
| pop | `[A, B, C, D]` | `F` |

The bottom item `A` remains untouched: a stack does not provide ordinary access to arbitrary positions. Repeated pops now return D, C, B, then A.

## Why LIFO Matters

LIFO is useful whenever the most recent unfinished action should be handled first: undo history, nested function calls, and parsing grouped expressions. If the stack is empty, there is no top item to inspect or remove; a real API should report that condition safely rather than inventing a value.

### Code Example: Push and Pop

The following versions perform the same sequence: push two strings, inspect the top, remove it, and inspect the next item. Each prints `second` and then `first`.

```pseudocode
stack = empty stack
push(stack, "first")
push(stack, "second")
print(pop(stack)) // second
print(pop(stack)) // first
```
```cpp
#include <iostream>
#include <stack>
#include <string>

int main() {
    std::stack<std::string> values;
    values.push("first");
    values.push("second");
    std::cout << values.top() << '\n'; // second
    values.pop();
    std::cout << values.top() << '\n'; // first
}
```
```java
import java.util.ArrayDeque;
import java.util.Deque;

class StackExample {
    public static void main(String[] args) {
        Deque<String> values = new ArrayDeque<>();
        values.push("first");
        values.push("second");
        System.out.println(values.peek()); // second
        values.pop();
        System.out.println(values.peek()); // first
    }
}
```
```python
values = []
values.append("first")
values.append("second")
print(values[-1])  # second
values.pop()
print(values[-1])  # first
```

Before using `top`, `peek`, or indexing the last Python list element, check that the stack is not empty.

## Remember

- `push` adds at the top; `pop` removes from the top.
- The last pushed item is the next popped item.
- An empty stack has no top. Check before removing or inspecting when the API requires it.
- Stack terminology describes the access rule, not a particular memory layout.

## Summary

A stack provides LIFO access: push and pop act at the top, so the newest item is removed first. Check for an empty stack before inspecting or removing its top item.
