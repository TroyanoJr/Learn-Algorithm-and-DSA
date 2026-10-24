# Hash Table Fundamentals and Hash Functions

A **hash table** (or hash map) is a data structure that stores data as **key-value pairs**. It provides extremely fast search, insertion, and deletion operations by transforming a key into an array index using a mathematical function called a **hash function**.

## Key Concepts

- **Key-Value Pair**: Data stored as an association where a unique **key** maps to an associated **value** (e.g. student ID `"S102"` maps to name `"Alice"`).
- **Hash Function**: A deterministic function that takes an arbitrary key and computes an integer hash value.
- **Bucket Index**: The array index calculated by taking the hash value modulo the table capacity: `index = hash(key) % capacity`.
- **Buckets**: The array slots that store the entries of the hash table.

## How Hash Tables Work

In a standard array, accessing element `A[i]` is instant $O(1)$ because `i` is an integer index. However, if keys are strings (like `"apple"`), floating-point numbers, or complex objects, they cannot be directly used as array indices.

A hash function solves this problem by mapping non-integer keys to integer indices:

1. Pass the key into the hash function to get a numerical hash value: `hash("apple") = 97321`.
2. Map the hash value to a valid array index within `0` to `capacity - 1`: `index = 97321 % 10 = 1`.
3. Store or look up the value associated with `"apple"` at array index `1`.

## Properties of a Good Hash Function

- **Deterministic**: The same key must always produce the exact same hash value.
- **Fast Computation**: Hash computation cost depends on the key and hash function; this character-by-character example takes O(m) time for a key of length m. Hash-table lookup and insertion are typically average O(1) when keys are well distributed and the load factor is controlled, but collisions can make them O(N).
- **Uniform Distribution**: Keys should be evenly distributed across all array buckets to minimize collisions.

### Code Example: Basic Hash Calculation

The following versions demonstrate calculating a bucket index from a string key using a simple character-sum hash function.

```pseudocode
function hashKey(key, capacity)
    sum = 0
    for each char in key do
        sum = sum + charCode(char)
    end for
    return sum % capacity
end function

capacity = 10
index = hashKey("CAT", capacity)
print(index)
```
```cpp
#include <iostream>
#include <string>

int hashKey(const std::string& key, int capacity) {
    int sum = 0;
    for (char c : key) {
        sum += static_cast<int>(c);
    }
    return sum % capacity;
}

int main() {
    int capacity = 10;
    int index = hashKey("CAT", capacity);
    std::cout << index << '\n';
}
```
```java
class HashFunctionExample {
    static int hashKey(String key, int capacity) {
        int sum = 0;
        for (int i = 0; i < key.length(); i++) {
            sum += (int) key.charAt(i);
        }
        return sum % capacity;
    }

    public static void main(String[] args) {
        int capacity = 10;
        int index = hashKey("CAT", capacity);
        System.out.println(index);
    }
}
```
```python
def hash_key(key, capacity):
    total = sum(ord(c) for c in key)
    return total % capacity

capacity = 10
index = hash_key("CAT", capacity)
print(index)
```

## Remember

- A hash table uses a **hash function** to convert arbitrary keys into integer array indices.
- `index = hash(key) % capacity` maps large hash codes into valid array bucket indices.
- Deterministic output is required so a key can always be relocated at the same index.

## Summary

Hash tables map keys to array bucket indices using a hash function. When designed well, this mapping enables constant-time $O(1)$ key-value lookups.
