# Array Operations & Memory

Inserting or deleting an element at an arbitrary index requires shifting remaining elements, resulting in O(n) worst-case time complexity.

## Common Operations

- **Traversal**: O(n) time
- **Insertion**: O(n) worst case
- **Deletion**: O(n) worst case

### Code Example

```kotlin
// Iterating through an array
for (i in numbers.indices) {
    println("Element at $i: ${numbers[i]}")
}
```
