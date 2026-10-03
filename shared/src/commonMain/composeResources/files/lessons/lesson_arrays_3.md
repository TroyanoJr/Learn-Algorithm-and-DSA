# Traversing an Array

Traversing an array means visiting each of its elements, one by one, usually using a loop. The index tells us which element we are currently visiting at each step.

Imagine walking down a street of numbered houses and looking inside each mailbox in order. You start at house 0, move to house 1, then house 2, until you reach the end. That's how array traversal works!

## Key Concepts

- **Index**: The position of the current element being visited.
- **Loop**: Repeats the operation for each position in the array.
- **Sequential Traversal**: Visits elements in order from start to end.

### Code Example

```kotlin
val numbers = intArrayOf(10, 20, 30, 40)

for (i in numbers.indices) {
    println(numbers[i])
}
```

## Summary

Traversing an array allows us to inspect or process all of its elements in O(n) time, advancing the index step by step in each iteration.
