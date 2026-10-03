# Introduction to Linear Search

Linear Search sequentially checks each element of a collection from start to finish until a match is found or the end is reached.

## Algorithm Overview

1. Start from index 0.
2. Compare current element with target value.
3. If matched, return the index.
4. Otherwise, move to the next index.

### Code Example

```kotlin
fun linearSearch(arr: IntArray, target: Int): Int {
    for (i in arr.indices) {
        if (arr[i] == target) return i
    }
    return -1
}
```
