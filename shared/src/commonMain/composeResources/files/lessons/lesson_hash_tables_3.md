# Hash Table Operations and Complexity

Hash tables provide fast $O(1)$ average-case performance for primary dictionary operations: **insertion**, **lookup**, and **deletion**. Understanding their performance requires analyzing the **load factor** and the process of **resizing and rehashing**.

## Key Concepts

- **Load Factor ($\alpha$)**: The ratio of stored keys $k$ to table capacity $N$: $\alpha = \frac{k}{N}$.
- **Rehashing**: The process of allocating a larger array and reinserting all existing keys into the new array using the new capacity.
- **Average-Case Time Complexity**: $O(1)$ constant time for `insert`, `get`, and `remove`.
- **Worst-Case Time Complexity**: $O(N)$ linear time when all $N$ keys hash to the same bucket.

## Operations and Complexity Summary

| Operation | Average Case | Worst Case | Explanation |
|---|---|---|---|
| **Lookup (`get`)** | $O(1)$ | $O(N)$ | $O(1)$ when keys distribute uniformly; $O(N)$ if all $N$ keys collide in one bucket. |
| **Insertion (`insert`)** | $O(1)$ amortized | $O(N)$ | $O(1)$ average; $O(N)$ when a resize/rehash is triggered or all keys collide. |
| **Deletion (`remove`)** | $O(1)$ | $O(N)$ | Finds key in $O(1)$ avg time and unlinks or marks as deleted. |

## Load Factor and Resizing

As elements are added to a hash table, the load factor $\alpha = \frac{k}{N}$ increases:
1. When $\alpha$ is low (e.g. $\alpha < 0.5$), collisions are rare, keeping operations fast.
2. When $\alpha$ exceeds a threshold (typically $0.75$), collisions become frequent and chains grow longer.
3. To restore $O(1)$ efficiency, the table **resizes** (doubles its capacity $N \to 2N$) and performs **rehashing**:
   - Allocate a new array of size $2N$.
   - Recompute `index = hash(key) % (2N)` for every existing entry and insert it into the new array.
   - Although rehashing takes $O(N)$ time, it occurs infrequently, yielding **amortized $O(1)$** insertion time.

### Code Example: Rehashing / Resizing Check

The following code checks if the load factor exceeds $0.75$ after adding an item, triggering a capacity doubling.

```pseudocode
capacity = 4
size = 3
// Adding 4th item
size = size + 1
loadFactor = size / capacity

if loadFactor > 0.75 then
    capacity = capacity * 2
    print("Resized to", capacity)
else
    print("Capacity remains", capacity)
end if
```
```cpp
#include <iostream>

int main() {
    int capacity = 4;
    int size = 3;

    // Adding 4th item
    size++;
    double loadFactor = static_cast<double>(size) / capacity;

    if (loadFactor > 0.75) {
        capacity *= 2;
        std::cout << "Resized to " << capacity << '\n';
    } else {
        std::cout << "Capacity remains " << capacity << '\n';
    }
}
```
```java
class ResizingExample {
    public static void main(String[] args) {
        int capacity = 4;
        int size = 3;

        size++;
        double loadFactor = (double) size / capacity;

        if (loadFactor > 0.75) {
            capacity *= 2;
            System.out.println("Resized to " + capacity);
        } else {
            System.out.println("Capacity remains " + capacity);
        }
    }
}
```
```python
capacity = 4
size = 3

size += 1
load_factor = size / capacity

if load_factor > 0.75:
    capacity *= 2
    print("Resized to", capacity)
else:
    print("Capacity remains", capacity)
```

## Remember

- Hash table operations run in **$O(1)$ average time** when the load factor is controlled.
- High load factor $\alpha > 0.75$ increases collisions and degrades search performance.
- Resizing doubles array capacity and rehashes all elements in $O(N)$ time, preserving amortized $O(1)$ insertions.

## Summary

Hash tables offer $O(1)$ average-case operations. When the load factor exceeds a threshold, the table resizes and rehashes entries to maintain constant-time performance.
