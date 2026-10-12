# Insert and Delete

Inserting or deleting an element in the middle of an array requires shifting adjacent elements to maintain contiguous memory storage.

For an array with `n` current elements, insertion is valid at an index from `0` through `n` (`0 <= index <= n`). Index `n` means “after the last element”; it is a valid insertion position even though it cannot be used to access an existing element. Deletion must name an existing element, so its index must satisfy `0 <= index < n`. An invalid index may produce an error, be rejected, or have other behavior depending on the language and data structure; check that API's rules.

These are rules for the abstract operation. A fixed-capacity array needs an unused slot before insertion, or its storage must be resized. Python lists are dynamic arrays: they manage capacity internally and are not fixed-size arrays, although inserting into a list can still shift elements.

Imagine a row of people sitting together on a bench. To insert a new person in the middle, everyone to the right must shift over one seat to create an open spot. Conversely, if someone leaves, everyone to the right must shift one seat left to close the gap.

## Key Concepts

- **Right Shift for Insertion**: To insert at index `targetIndex`, elements from `size - 1` down to `targetIndex` must move right by one position.
- **Left Shift for Deletion**: To delete at index `targetIndex`, elements from `targetIndex + 1` up to `size - 1` must move left by one position.
- **Worst-Case Cost**: Inserting or deleting at index `0` requires shifting all `N` elements, taking `O(N)` linear time.

In a right shift, work from the last element down toward the insertion point so each value is copied before its old slot is overwritten. In a left shift, work from the element after the deletion point toward the end, copying each value into the gap behind it.

## Step-by-Step Example

Suppose we have array `[10, 20, 30, 40]` with capacity for 5 items:
1. **Insert 25 at index 2** (there is one free slot):
   - Shift `40` from index 3 to 4.
   - Shift `30` from index 2 to 3.
   - Place `25` at index 2.
   - Result: `[10, 20, 25, 30, 40]`.

2. **Delete element at index 1 (20)**:
   - Shift `25` from index 2 to 1.
   - Shift `30` from index 3 to 2.
   - Shift `40` from index 4 to 3.
   - Result: `[10, 25, 30, 40]`.

Insertion at index `0` shifts every existing element; insertion at `size` needs no shifts. Deleting index `0` shifts all later elements left; deleting the last index needs no shifts.

### Code Example: Manual Element Shifting

These examples assume the index satisfies the bounds above and insertion has enough capacity. They demonstrate shifting; they do not add bounds checks or grow fixed-capacity storage.

```pseudocode
// Array insertion with explicit element shifting
function insertAt(arr, size, targetIndex, newValue):
    // Shift elements right starting from the end
    for j from size - 1 down to targetIndex do
        arr[j + 1] = arr[j]
    end for
    arr[targetIndex] = newValue
    return size + 1

// Array deletion with explicit element shifting
function deleteAt(arr, size, targetIndex):
    // Shift elements left to fill the gap
    for j from targetIndex to size - 2 do
        arr[j] = arr[j + 1]
    end for
    return size - 1
```
```cpp
// Explicit element shifting in C++
void insertAt(int arr[], int& size, int targetIndex, int newValue) {
    for (int j = size - 1; j >= targetIndex; j--) {
        arr[j + 1] = arr[j]; // Shift right
    }
    arr[targetIndex] = newValue;
    size++;
}

void deleteAt(int arr[], int& size, int targetIndex) {
    for (int j = targetIndex; j < size - 1; j++) {
        arr[j] = arr[j + 1]; // Shift left
    }
    size--;
}
```
```java
// Explicit element shifting in Java
public class ArrayShiftExample {
    public static int insertAt(int[] arr, int size, int targetIndex, int newValue) {
        for (int j = size - 1; j >= targetIndex; j--) {
            arr[j + 1] = arr[j]; // Shift right
        }
        arr[targetIndex] = newValue;
        return size + 1;
    }

    public static int deleteAt(int[] arr, int size, int targetIndex) {
        for (int j = targetIndex; j < size - 1; j++) {
            arr[j] = arr[j + 1]; // Shift left
        }
        return size - 1;
    }
}
```
```python
# Explicit shifting with a Python list. Python lists are dynamic; append manages
# capacity internally and may resize, unlike a fixed-capacity array.
def insert_at(arr, size, target_index, new_value):
    arr.append(0)  # Add one element; Python manages the list's capacity internally.
    for j in range(size - 1, target_index - 1, -1):
        arr[j + 1] = arr[j] # Shift right
    arr[target_index] = new_value
    return size + 1

def delete_at(arr, size, target_index):
    for j in range(target_index, size - 1):
        arr[j] = arr[j + 1] # Shift left
    arr.pop() # Remove unused last slot
    return size - 1
```

## Summary

In a contiguous array, inserting or deleting in the middle takes `O(N)` worst-case time because elements may need to shift. The exact work depends on the index and the data structure's implementation.
