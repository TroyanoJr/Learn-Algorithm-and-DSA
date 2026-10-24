# Collisions and Collision Resolution

A **collision** occurs when two distinct keys produce the exact same bucket index after hashing: `hash(key1) % capacity == hash(key2) % capacity`. Because array buckets have finite capacity, every hash table must implement a strategy to resolve collisions.

## Key Concepts

- **Collision**: Two different keys hashing to the same bucket index in the table array.
- **Separate Chaining**: A collision resolution technique where each array bucket holds a linked list (or chain) of key-value pairs.
- **Open Addressing**: A collision resolution technique where all entries are stored directly in the array itself.
- **Linear Probing**: An open addressing method where, upon collision at index `h`, consecutive slots `h + 1, h + 2, ...` are inspected until an empty slot is found.

## Collision Resolution Strategies

### 1. Separate Chaining

In **separate chaining**, each array bucket contains a pointer to a linked list.
- When inserting `("CAT", 10)` and `("DOG", 20)`, if both hash to bucket `3`, `"DOG"` is appended to the linked list at bucket `3`.
- Searching for `"DOG"` hashes to bucket `3`, then traverses the short linked list at index `3` until `"DOG"` is matched.
- **Advantage**: Degrades gracefully as the table fills; can store more items than table capacity.
- **Disadvantage**: Requires extra memory overhead for node pointers (`next` references).

### 2. Open Addressing (Linear Probing)

In **linear probing**, no extra data structures (like linked lists) are used.
- If slot `index = hash(key) % capacity` is already occupied, check `(index + 1) % capacity`.
- Repeat probing sequentially until an empty slot is found for insertion.
- **Advantage**: Uses memory compactly in a single contiguous array.
- **Disadvantage**: Vulnerable to **primary clustering** (long contiguous blocks of occupied slots that increase search times).

### Code Example: Separate Chaining Search Trace

The following code illustrates searching through a bucket chain when two keys hash to the same bucket index `1`.

```pseudocode
bucketList = [ ("CAT", 100), ("DOG", 200) ]
targetKey = "DOG"
foundValue = -1

for each pair in bucketList do
    if pair.key == targetKey then
        foundValue = pair.value
        break
    end if
end for

print(foundValue)
```
```cpp
#include <iostream>
#include <vector>
#include <string>

struct Entry {
    std::string key;
    int value;
};

int main() {
    std::vector<Entry> bucketList = { {"CAT", 100}, {"DOG", 200} };
    std::string targetKey = "DOG";
    int foundValue = -1;

    for (const auto& entry : bucketList) {
        if (entry.key == targetKey) {
            foundValue = entry.value;
            break;
        }
    }

    std::cout << foundValue << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.List;

class Entry {
    String key;
    int value;
    Entry(String key, int value) { this.key = key; this.value = value; }
}

class ChainingSearch {
    public static void main(String[] args) {
        List<Entry> bucketList = new ArrayList<>();
        bucketList.add(new Entry("CAT", 100));
        bucketList.add(new Entry("DOG", 200));

        String targetKey = "DOG";
        int foundValue = -1;

        for (Entry entry : bucketList) {
            if (entry.key.equals(targetKey)) {
                foundValue = entry.value;
                break;
            }
        }

        System.out.println(foundValue);
    }
}
```
```python
bucket_list = [("CAT", 100), ("DOG", 200)]
target_key = "DOG"
found_value = -1

for key, value in bucket_list:
    if key == target_key:
        found_value = value
        break

print(found_value)
```

## Remember

- Collisions are inevitable due to the Pigeonhole Principle when the number of possible keys exceeds table capacity.
- **Separate Chaining** uses a linked list at each bucket.
- **Linear Probing** searches consecutive array slots until an open slot is located.

## Summary

Collisions happen when different keys hash to the same index. Separate chaining resolves collisions using bucket linked lists, while open addressing probes alternative array slots.
