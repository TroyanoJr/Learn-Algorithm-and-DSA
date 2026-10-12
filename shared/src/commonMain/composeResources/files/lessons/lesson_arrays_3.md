# Traversing an Array

Traversing an array means visiting each of its elements, one by one, usually using a loop. The index tells us which element we are currently visiting at each step.

Imagine walking down a street of numbered houses and looking inside each mailbox in order. You start at house 0, move to house 1, then house 2, until you reach the end. That's how array traversal works!

## Key Concepts

- **Index**: The zero-based position of the current element being visited.
- **Loop Bounds**: Starts at index `0` and continues up to index `N - 1` (where `N` is the total number of elements).
- **Sequential Inspection**: Visits elements in order from start to end without skipping any index.

### Code Example

```pseudocode
numbers = [10, 20, 30, 40]
n = length(numbers)

// Explicit index loop from 0 to n - 1
for i from 0 to n - 1 do
    print(numbers[i])
end for
```
```cpp
#include <iostream>

int main() {
    int numbers[] = {10, 20, 30, 40};
    int n = sizeof(numbers) / sizeof(numbers[0]);

    // Explicit index loop from 0 to n - 1
    for (int i = 0; i < n; i++) {
        std::cout << numbers[i] << std::endl;
    }

    return 0;
}
```
```java
public class TraversalExample {
    public static void main(String[] args) {
        int[] numbers = {10, 20, 30, 40};

        // Explicit index loop from 0 to numbers.length - 1
        for (int i = 0; i < numbers.length; i++) {
            System.out.println(numbers[i]);
        }
    }
}
```
```python
numbers = [10, 20, 30, 40]

# Explicit index loop from 0 to len(numbers) - 1
for i in range(len(numbers)):
    print(numbers[i])
```

## Summary

Traversing an array allows us to inspect or process all `N` elements in `O(N)` linear time and `O(1)` auxiliary space, advancing the index step by step in each loop iteration.
