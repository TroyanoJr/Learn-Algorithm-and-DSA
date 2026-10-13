# What Are Algorithms?

An algorithm is a clear, step-by-step procedure or set of rules designed to solve a specific problem or perform a computation. It describes the plan for solving the problem, independent of any particular programming language.

## Algorithm and Program

A **program** is an implementation of an algorithm written in a programming language so a computer can run it. The `findMax` algorithm below can be expressed in pseudocode, C++, Java, or Python: the code looks different, but each version follows the same plan.

## Why Algorithms Matter

Algorithms matter because different valid ways to solve a problem can require different amounts of work. The input size—the amount of data being handled—can affect how much work an algorithm does. The next lesson explores this idea with small examples.

## Key Properties of an Algorithm

- **Input**: Accepts zero or more well-defined inputs.
- **Output**: Produces at least one well-defined output or result.
- **Definiteness**: Each step is unambiguous and precise.
- **Finiteness**: Terminates after a finite number of steps.

### Code Example: Finding the Maximum of Two Numbers

The following simple algorithm compares two numbers and returns the larger value.

To trace it, use `a = 8` and `b = 3`. The algorithm checks whether `8 > 3`. That condition is true, so it returns `a`, which is `8`. For `a = 3` and `b = 8`, the condition `3 > 8` is false, so it takes the `ELSE` branch and returns `b`, which is `8`. The result is correct in both cases because `8` is the larger of the two inputs. An algorithm is **correct** when it produces a result that meets the problem's requirements for its valid inputs.

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

Searching and Sorting are two common algorithm categories. The upcoming lessons explore approaches to these problems and compare the work they require.
