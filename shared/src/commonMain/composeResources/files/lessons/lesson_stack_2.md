# Stack Operations and Complexity

The core stack interface is small. `push(x)` adds `x` to the top, `pop()` removes and returns the top item, and `peek()` (also called `top()`) reads the top item without removing it. `isEmpty()` lets a caller check whether an item exists; `size()` reports the number of stored items.

## Key Concepts

- **Push** adds an item; **pop** removes and returns the top item; **peek/top** observes it without removing it.
- `pop` and `peek` require a non-empty stack; fixed-capacity stacks may also reject a push when full.
- Push is O(1) for a linked top or an available fixed-array slot; dynamic-array push is amortized O(1), with occasional O(n) resizing.

## Operations and edge cases

The pseudocode and language examples below push 7 and 4, inspect the top without removing it, then remove 4. The APIs differ slightly: C++ `top()` and Java `peek()` observe the top, while Java `poll()` returns `null` on an empty deque. Python lists raise `IndexError` on an empty `pop()` or invalid last-item access, so check first.

### Code Example: Guarded Stack Operations

```pseudocode
push(stack, value): add value at top
pop(stack):
    if isEmpty(stack): report underflow
    value = top item
    remove top item
    return value
peek(stack):
    if isEmpty(stack): report empty
    return top item
```

```cpp
#include <stack>

std::stack<int> values;
values.push(7);
values.push(4);
if (!values.empty()) {
    int observed = values.top(); // 4; still present
    int removed = values.top();
    values.pop();                // removes 4
}
```
```java
import java.util.ArrayDeque;
import java.util.Deque;

Deque<Integer> values = new ArrayDeque<>();
values.push(7);
values.push(4);
Integer observed = values.peek(); // 4; still present
Integer removed = values.poll();  // removes 4, or null if empty
```
```python
values = []
values.append(7)
values.append(4)
if values:                       # avoid empty-stack access
    observed = values[-1]        # 4; still present
    removed = values.pop()       # removes 4
```

After `push(7), push(4)`, `peek()` returns 4 and leaves `[7, 4]` unchanged. `pop()` returns 4 and leaves `[7]`. Calling either `pop` or `peek` on an empty stack is an edge case: implementations may throw, return an optional/result value, or use a documented sentinel. Callers must follow that contract. Fixed-capacity stacks can also reject a push when full (overflow); a dynamically growing stack instead requests more storage.

## Complexity

For a stack with a linked-list top or a fixed-capacity array with available space, push, pop, and peek each take **O(1)** time. `isEmpty` and `size` are also O(1) when the size is stored.

A dynamic-array-backed stack has **amortized O(1)** push: most pushes write one slot, and the occasional resize copies the existing n items and costs **O(n)**. Across many pushes, that copy cost is spread over the operations. Pop remains O(1) unless the implementation deliberately shrinks its storage; shrinking policies may introduce occasional copying too. These bounds assume the top is at the array's end or a linked-list endpoint that is directly referenced.

- **push** — Typical time: O(1) amortized for a dynamic array. A resize can cost O(n); a fixed array can be full.
- **pop** — Typical time: O(1). Requires a non-empty stack.
- **peek / top** — Typical time: O(1). Does not remove the item and requires a non-empty stack.
- **isEmpty / size** — Typical time: O(1) if size or top state is maintained.

The stack stores n elements, so space is **O(n)**. The abstract interface stays the same even when its implementation changes.

In these examples, each ordinary top operation touches one item. A dynamic array's append is amortized O(1), but one append can be O(n) when capacity growth copies existing items; a linked stack's top update is O(1) when allocation succeeds.

## Summary

Push, pop, and peek operate at the top. Handle empty-stack behavior explicitly. Dynamic-array push is amortized O(1), even though a resizing push can take O(n).
