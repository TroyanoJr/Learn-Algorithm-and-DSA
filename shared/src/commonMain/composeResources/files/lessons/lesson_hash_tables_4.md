# Applications, Trade-offs, and Practical Design

Hash tables are among the most versatile data structures in software engineering. Understanding when to use them—and when another structure is superior—is a fundamental skill for algorithm design.

## Key Applications of Hash Tables

- **Dictionaries / Associated Maps**: Storing key-value pairs (e.g., user profiles mapped by user ID).
- **Frequency Counting**: Counting occurrences of elements (e.g. word frequency in a document).
- **Caching (LRU Cache)**: Storing expensive database query results for instant $O(1)$ retrieval.
- **Set Operations & Duplicate Detection**: Checking if an item has been seen before in $O(1)$ average time.

## Data Structure Comparisons

| Property / Operation | Hash Table | Array | Unsorted Linked List | Balanced BST (Tree) |
|---|---|---|---|---|
| **Lookup by Key** | $O(1)$ average | $O(N)$ | $O(N)$ | $O(\log N)$ |
| **Insertion** | $O(1)$ amortized | $O(1)$ at end / $O(N)$ middle | $O(1)$ at head | $O(\log N)$ |
| **Key Ordering** | Unordered | Unordered | Unordered | **Sorted** |
| **Range Queries** | Inefficient ($O(N)$) | Inefficient ($O(N)$) | Inefficient ($O(N)$) | Efficient ($O(\log N + k)$) |

## Key Trade-offs & Common Pitfalls

1. **Unordered Keys**: Hash tables do not maintain keys in sorted order. If you need range queries (e.g. "find all items between 10 and 20") or finding min/max, a **Balanced Binary Search Tree** (or Binary Heap) is more appropriate.
2. **Mutable Keys**: Modifying a key after insertion changes its hash code, making the entry unfindable in its original bucket. Keys should always be immutable (e.g. strings or primitive integers).
3. **Worst-case Degradation**: If a malicious user supplies keys that all hash to the same bucket (hash collision attack), performance degrades from $O(1)$ to $O(N)$. Modern runtimes use randomized seed hashing to mitigate this risk.

### Code Example: Frequency Counting with a Hash Map

The following code counts how many times each character appears in a string using a frequency map.

```pseudocode
text = "aab"
counts = empty map

for each char in text do
    if keyExists(counts, char) then
        counts[char] = counts[char] + 1
    else
        counts[char] = 1
    end if
end for

print(counts["a"])
```
```cpp
#include <iostream>
#include <unordered_map>
#include <string>

int main() {
    std::string text = "aab";
    std::unordered_map<char, int> counts;

    for (char c : text) {
        counts[c]++;
    }

    std::cout << counts['a'] << '\n';
}
```
```java
import java.util.HashMap;
import java.util.Map;

class FrequencyCounter {
    public static void main(String[] args) {
        String text = "aab";
        Map<Character, Integer> counts = new HashMap<>();

        for (char c : text.toCharArray()) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }

        System.out.println(counts.get('a'));
    }
}
```
```python
text = "aab"
counts = {}

for char in text:
    counts[char] = counts.get(char, 0) + 1

print(counts['a'])
```

## Remember

- Hash tables excel at $O(1)$ average-case key lookup and frequency counting.
- Hash tables do not keep keys sorted; use balanced trees for range queries or ordered iterations.
- Keys must be immutable so their hash codes and bucket positions remain stable.

## Summary

Hash tables enable fast key-based lookups and set operations. Choose hash tables when average $O(1)$ lookup is paramount and key ordering is not required.
