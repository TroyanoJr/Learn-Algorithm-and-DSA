# Arrays and Complexity

Different array operations have different efficiency profiles depending on whether memory addresses can be calculated directly or require element shifting.

## Key Concepts

- **Access by Index**: Instant O(1) constant time lookup via direct memory offset.
- **Sequential Traversal**: Inspecting every item takes O(n) linear time.
- **Middle Insert/Delete**: Requires shifting elements, taking O(n) worst-case time.
- **Linear Search**: Best case `O(1)` when the first element matches; worst case `O(n)` when the match is last or the target is absent, because elements are checked one by one.

## Why the Costs Grow

With `n` elements, indexed access uses the index to locate the element directly, so it is typically `O(1)`. Traversal visits all `n` elements, so it is `O(n)`. For example, inserting `25` into `[10, 20, 30, 40]` at index 2 moves `40` and `30`; inserting at index 0 in a four-element array moves four elements. The number shifted grows with the number of elements after the insertion point. Deletion similarly shifts the elements after the removed item. At the end, either operation may need no shifts; actual costs depend on position and implementation.

Appending to a dynamic array is typically amortized `O(1)`: most appends use an already available slot, while an occasional append grows storage and copies up to `n` elements (`O(n)` for that append). Across many appends, those occasional expensive grows are spread over the sequence. Traversal and access use the array's existing storage; they do not require `O(n)` extra memory. Auxiliary space means extra working memory beyond the input, and is typically `O(1)` for these operations when done in place.

## Summary

Arrays excel at direct index access (O(1)), but operations that alter array size or inspect all items scale linearly (O(n)) with array length.
