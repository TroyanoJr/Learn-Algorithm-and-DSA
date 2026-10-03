# What Is Searching?

Searching is the process of locating a specific element, called the target, within a collection of data.

Imagine looking for a friend's name in an address book or finding a specific song in a playlist. In programming, searching is one of the most fundamental operations performed daily.

## Key Concepts

- **Target**: The specific value or item you are trying to find.
- **Worst Case**: The maximum number of steps required when the target is at the end or missing.
- **Search Strategy**: The algorithm or approach used to inspect elements.

### Code Example

```kotlin
val numbers = intArrayOf(3, 8, 7, 2, 9)
val target = 7
val found = target in numbers // Simple lookup
```

## Summary

Different search strategies require different numbers of steps depending on whether the data is sorted and how elements are inspected.
