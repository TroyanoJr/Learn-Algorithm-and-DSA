# Introduction to Arrays

An array is a fundamental data structure that stores a collection of elements of the same type in contiguous memory locations.

Imagine a row of numbered lockers placed side-by-side in a hallway. Each locker has a specific position number, and you can open any locker directly if you know its number. An array works in a similar way!

## Key Concepts

- **Contiguous Memory**: Elements are placed side-by-side in adjacent, uninterrupted memory slots.
- **Index Access**: Every element is assigned a position number called an index.
- **Zero-Based Indexing**: Indexing starts at `0` for the first element.

## Memory Address Calculation

Because array elements occupy contiguous memory slots of equal size, the computer does not need to search through elements to access a specific index. Instead, it calculates the exact memory location using this formula:

`Address(A[i]) = Base Address + (i × Element Size)`

Where:
- **Base Address**: The memory location of the first element `A[0]`.
- **`i`**: The requested zero-based index.
- **Element Size**: The byte size of each data element (e.g., 4 bytes for a standard integer).

This mathematical calculation allows direct `O(1)` constant-time element access regardless of array length.

### Code Example

```pseudocode
// Array initialization with 4 elements
numbers = [10, 20, 30, 40]

// Instant O(1) index access
value = numbers[2] // Returns 30
```
```cpp
#include <iostream>

int main() {
    int numbers[] = {10, 20, 30, 40};

    // Instant O(1) index access
    int value = numbers[2]; // Returns 30
    return 0;
}
```
```java
public class ArrayExample {
    public static void main(String[] args) {
        int[] numbers = {10, 20, 30, 40};

        // Instant O(1) index access
        int value = numbers[2]; // Returns 30
    }
}
```
```python
numbers = [10, 20, 30, 40]

# Instant O(1) index access
value = numbers[2] # Returns 30
```

## Summary

Arrays provide instant `O(1)` time access because the computer calculates an element's memory address directly using its base address and index offset.
