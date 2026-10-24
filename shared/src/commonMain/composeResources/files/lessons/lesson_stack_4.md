# Call Stacks, Delimiters, and Applications

Stacks help manage nested work. A **call stack** records active function calls. Each call creates a frame containing information such as parameters, local variables, and where execution should return. A nested call places a new frame on top; when it returns, that frame is removed and execution resumes in the caller. Deep or unbounded recursion can exhaust available stack space, so iterative solutions or explicit depth limits may be needed.

## Key Concepts

- A function call adds an active frame; returning removes that frame and resumes its caller.
- Delimiter matching pushes openers and requires each closer to match the most recent opener.
- A valid delimiter sequence has no mismatched or extra closer and leaves no opener behind.

## Balanced delimiters

To check parentheses, brackets, and braces, push each opening delimiter. For every closing delimiter, require a non-empty stack and compare it with the most recent opening delimiter. At the end, the stack must be empty.

### Code Example: Delimiter Validation

Each implementation follows the same rule: a closer must match the latest opener, an unmatched closer fails immediately, and leftover openers fail at the end. These snippets assume the input consists only of the three delimiter kinds.

```pseudocode
isBalanced(text):
    openings = empty stack
    for each character c in text:
        if c is '(' or '[' or '{':
            push(openings, c)
        else if c is ')' or ']' or '}':
            if openings is empty: return false
            open = pop(openings)
            if (open, c) is not one of [('(', ')'), ('[', ']'), ('{', '}')]:
                return false
    return openings is empty
```

```cpp
#include <string>
#include <vector>

bool isBalanced(const std::string& text) {
    std::vector<char> openings;
    for (char c : text) {
        if (c == '(' || c == '[' || c == '{') openings.push_back(c);
        else {
            if (openings.empty()) return false;
            char open = openings.back();
            openings.pop_back();
            if (!((open == '(' && c == ')') || (open == '[' && c == ']') ||
                  (open == '{' && c == '}'))) return false;
        }
    }
    return openings.empty();
}
```
```java
import java.util.ArrayDeque;
import java.util.Deque;

static boolean isBalanced(String text) {
    Deque<Character> openings = new ArrayDeque<>();
    for (char c : text.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') openings.push(c);
        else {
            if (openings.isEmpty()) return false;
            char open = openings.pop();
            if (!((open == '(' && c == ')') || (open == '[' && c == ']') ||
                  (open == '{' && c == '}'))) return false;
        }
    }
    return openings.isEmpty();
}
```
```python
def is_balanced(text):
    openings = []
    for char in text:
        if char in "([{":
            openings.append(char)
        else:
            if not openings:
                return False
            opening = openings.pop()
            if (opening, char) not in (("(", ")"), ("[", "]"), ("{", "}")):
                return False
    return not openings
```

For example, each returns `True` for `([{}])` and `False` for `([)]`.

Trace `([{}])`:

| Character | Action | Stack (bottom → top) |
|---|---|---|
| `(` | push | `(` |
| `[` | push | `( [` |
| `{` | push | `( [ {` |
| `}` | matches `{`, pop | `( [` |
| `]` | matches `[`, pop | `(` |
| `)` | matches `(`, pop | empty |

The result is balanced. `([)]` is not balanced: when `)` arrives, the top is `[`, which does not match. A closing delimiter with an empty stack and leftover openings at end are also failures. The algorithm takes O(n) time and O(n) extra space in the worst case. If the input includes quoted strings or comments, a real parser must avoid treating delimiters inside them as syntax.

## Other applications

- **Undo and redo:** save prior states or inverse actions so the newest action is undone first.
- **Expression evaluation:** operators and grouping can be processed with stacks.
- **Depth-first search:** store pending nodes; the most recently discovered node is explored next.
- **Browser history and backtracking:** retain the path or prior locations to revisit.
- **Reversing data:** pushing a sequence and popping it yields the reverse order.

In each case, the stack is useful because the next item needed is the most recently added unfinished item.

## Summary

The call stack tracks nested execution, and an explicit stack can validate nested delimiters in O(n) time and O(n) extra space. Undo, depth-first search, and reversal also use LIFO ordering.
