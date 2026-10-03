# Introduction to Arrays

An array is a fundamental data structure that stores a collection of elements of the same type in contiguous memory locations.

Imagine a row of numbered lockers placed side-by-side in a hallway. Each locker has a specific position number, and you can open any locker directly if you know its number. An array works in a similar way!

## Key Concepts

- **Contiguous Memory**: Elements are placed side-by-side in adjacent memory slots.
- **Index Access**: Every element is assigned a position number called an index.
- **Zero-Based Indexing**: Indexing starts at `0` for the first element.

### Code Example

```kotlin
val numbers = intArrayOf(10, 20, 30, 40)
val value = numbers[2] // Instant O(1) access -> 30
```

## Summary

Arrays provide instant O(1) time access because the computer calculates an element's memory address directly using its base address and index offset.
