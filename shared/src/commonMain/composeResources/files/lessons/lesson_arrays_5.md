# Arrays and Complexity

Different array operations have different efficiency profiles depending on whether memory addresses can be calculated directly or require element shifting.

## Key Concepts

- **Access by Index**: Instant O(1) constant time lookup via direct memory offset.
- **Sequential Traversal**: Inspecting every item takes O(n) linear time.
- **Middle Insert/Delete**: Requires shifting elements, taking O(n) worst-case time.

## Summary

Arrays excel at direct index access (O(1)), but operations that alter array size or inspect all items scale linearly (O(n)) with array length.
