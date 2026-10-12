# What Are Algorithms?

An algorithm is a clear, step-by-step procedure or set of rules designed to solve a specific problem or perform a computation.

## Why Algorithms Matter

Algorithms dictate how efficiently a problem is solved. Even with powerful hardware, an inefficient algorithm can take hours or days, whereas an efficient algorithm can solve the same problem in milliseconds.

## Key Properties of an Algorithm

- **Input**: Accepts zero or more well-defined inputs.
- **Output**: Produces at least one well-defined output or result.
- **Definiteness**: Each step is unambiguous and precise.
- **Finiteness**: Terminates after a finite number of steps.

### Code Example: Finding the Maximum of Two Numbers

The following simple algorithm compares two numbers and returns the larger value.

```pseudocode
ALGORITHM findMax(a, b)
    IF a > b THEN
        RETURN a
    ELSE
        RETURN b
    END IF
END ALGORITHM
```
```cpp
int findMax(int a, int b) {
    if (a > b) {
        return a;
    } else {
        return b;
    }
}
```
```java
public static int findMax(int a, int b) {
    if (a > b) {
        return a;
    } else {
        return b;
    }
}
```
```python
def find_max(a, b):
    if a > b:
        return a
    else:
        return b
```

## Common Categories

- **Searching Algorithms**: Locate a target item within a collection (e.g., Linear Search, Binary Search).
- **Sorting Algorithms**: Reorganize elements into a defined order (e.g., Bubble Sort, Selection Sort, Insertion Sort).

Different algorithms can solve the exact same problem with drastically different efficiency depending on the input size and data layout.
