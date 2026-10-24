# Static vs. Dynamic Arrays & Memory

Arrays can be categorized into static arrays and dynamic arrays based on how memory is allocated and managed during execution.

## Static Arrays

A static array has a fixed capacity that is declared when created. Its size cannot change after allocation because memory is reserved in a single fixed contiguous block.

- **Fixed Capacity**: Cannot grow or shrink at runtime.
- **Fast Lookup**: `O(1)` constant time access by index.
- **Memory Overhead**: Low memory footprint with zero extra pointer overhead.

## Dynamic Arrays

A dynamic array can automatically grow or shrink its size as elements are added or removed.

When a dynamic array reaches its capacity, it automatically:
1. Allocates a new, larger memory block (typically doubling its capacity).
2. Copies all existing elements to the new memory block.
3. Frees the old memory block.

Although copying elements takes `O(N)` linear time when resizing occurs, adding an element at the end takes `O(1)` **amortized** constant time over a series of insertions.

## Language Terminology Differences

Different programming languages provide static and dynamic array implementations under different names:

- **C++**: Fixed-size static array (`int arr[5]`) vs. dynamic array (`std::vector<int>`).
- **Java**: Fixed-size static array (`int[] arr = new int[5]`) vs. dynamic array (`ArrayList<Integer>`).
- **Python**: Python's built-in `list` is implemented as a dynamic array under the hood.

### Code Example

```pseudocode
// Static array (Fixed size 4)
staticNumbers = [10, 20, 30, 40]

// Dynamic array (Can append elements)
dynamicNumbers = []
dynamicNumbers.append(10)
dynamicNumbers.append(20)
```
```cpp
#include <iostream>
#include <vector>

int main() {
    // Fixed-size static array
    int staticArr[4] = {10, 20, 30, 40};

    // Resizable dynamic array
    std::vector<int> dynamicArr;
    dynamicArr.push_back(10);
    dynamicArr.push_back(20);

    return 0;
}
```
```java
import java.util.ArrayList;

public class StaticVsDynamic {
    public static void main(String[] args) {
        // Fixed-size static array
        int[] staticArr = {10, 20, 30, 40};

        // Resizable dynamic array
        ArrayList<Integer> dynamicArr = new ArrayList<>();
        dynamicArr.add(10);
        dynamicArr.add(20);
    }
}
```
```python
# Python's built-in list is dynamically resizable and does not enforce fixed capacity.
python_list = [10, 20, 30, 40]

# Dynamic array (Python list resizes automatically)
dynamic_arr = []
dynamic_arr.append(10)
dynamic_arr.append(20)
```

## Summary

Static arrays offer low-overhead fixed memory allocation, while dynamic arrays provide automatic resizing with amortized `O(1)` append operations.
