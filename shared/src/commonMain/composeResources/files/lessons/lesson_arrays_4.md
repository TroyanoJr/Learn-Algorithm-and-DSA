# Insert and Delete

Inserting or deleting an element in the middle of an array requires shifting adjacent elements to maintain contiguous memory storage.

Imagine a row of people sitting together on a bench. To insert a new person in the middle, everyone to the right must shift over one seat to create an open spot.

## Key Concepts

- **Element Shifting**: Elements after the target position must move right for insertion or left for deletion.
- **Insertion Cost**: Inserting at index `i` requires shifting `n - i` elements, taking O(n) worst-case time.
- **Deletion Cost**: Removing at index `i` requires shifting `n - i - 1` elements left, taking O(n) worst-case time.

### Code Example

```kotlin
val numbers = mutableListOf(10, 20, 30, 40)

// Insert 25 at index 2 (shifts 30 and 40 right)
numbers.add(2, 25)

// Delete element at index 1 (shifts 25, 30, 40 left)
numbers.removeAt(1)
```

## Summary

Because arrays occupy fixed contiguous memory slots, inserting or deleting in the middle takes O(n) linear time due to the necessary element shifts.
