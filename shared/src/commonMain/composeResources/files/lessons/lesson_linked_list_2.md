# Traversal, Search, and Position Access

Once a linked list is built, working with its values requires navigating through nodes using pointers or references. A singly linked list does not provide direct array-style index access, so reaching position `k` requires following links sequentially from `head` and takes O(n) time in the worst case.

To reach a node, start at the **`head`** and follow `next` references step by step.

## Traversing a Linked List

**Traversal** means visiting each node in the list sequentially from the beginning to the end.

Consider this list with three elements:

```text
head → [12] → [7] → [19] → null
```

To traverse the list:
1. Initialize a pointer `current = head`.
2. While `current` is not `null`, process `current.data` (for example, print it).
3. Move to the next node by updating `current = current.next`.
4. Stop when `current` becomes `null`, which indicates the end of the list.

Tracing the traversal on `head → [12] → [7] → [19] → null`:
- Start at `head`: `current` points to node `[12]`. Print `12`. Advance `current = current.next`.
- `current` points to node `[7]`. Print `7`. Advance `current = current.next`.
- `current` points to node `[19]`. Print `19`. Advance `current = current.next`.
- `current` is `null`. The traversal loop terminates.

## Searching for a Value

To **search** for a target value, traverse the list while comparing each node's `data` with the target:
- **Target Found:** If `current.data == target`, return its zero-based position or node reference immediately.
- **Target Absent:** If `current` becomes `null` without finding a match, return `-1` (or a sentinel indicator) to signal that the value is not in the list.

Trace for `head → [12] → [7] → [19] → null`:
- **Search for `7`:**
  - Check index 0 (value `12`): `12 != 7`. Advance to index 1.
  - Check index 1 (value `7`): `7 == 7`. Match found at index `1`!
- **Search for `99` (Absent Target):**
  - Check index 0 (`12 != 99`), index 1 (`7 != 99`), index 2 (`19 != 99`).
  - Advance to next node: `current` is now `null`. Search ends and returns `-1`.

## Accessing a Value by Position (Index)

In an array, reaching position `k` takes constant O(1) time because memory is contiguous. In a linked list, reaching position `k` requires taking `k` steps from the `head`.

To access position `index` (using zero-based positioning):
1. Start at `current = head` and `currentIndex = 0`.
2. While `current` is not `null` and `currentIndex < index`:
   - Advance `current = current.next`.
   - Increment `currentIndex`.
3. If `current` is not `null` when `currentIndex == index`, return `current.data`.
4. If `index` is negative or `current` becomes `null` before reaching the requested position, return the invalid-position result.

The examples use a distinct invalid result (`INVALID_POSITION`, `std::nullopt`, `null`, or `None`) so it cannot be confused with any integer stored in the list.

Trace for accessing index `2` on `head → [12] → [7] → [19] → null`:
- Start at index 0 (value `12`).
- Step 1: Advance to index 1 (value `7`).
- Step 2: Advance to index 2 (value `19`).
- `currentIndex == 2`: Return value `19`.

## Edge Cases to Consider

- **Empty List (`head == null`):** Traversal, search, and position access immediately detect `current == null` and handle the empty state without error.
- **Target at the Head:** Search matches on the very first node (index 0) in O(1) time.
- **Target at the Tail:** Search inspects all n nodes before finding the match at the end.
- **Missing Target or Invalid Index:** The loop reaches `null` after inspecting all n nodes, requiring O(n) steps.

## Time and Auxiliary Space Complexity

- **Traversal:** Takes O(n) time because every node from `head` to tail must be visited.
- **Search:** Takes O(n) worst-case time (when target is at the tail or missing) and O(1) best-case time (when target is at `head`).
- **Position Access:** Takes O(n) worst-case time because reaching position k requires following k sequential links.

All three operations use O(1) auxiliary space when implemented iteratively, using only a loop variable (`current`).

## Code Example: Traversal, Search, and Position Access

The following examples define functions to traverse, search, and access nodes by position on the list `12 → 7 → 19`.

```pseudocode
CLASS Node
    data: INTEGER
    next: Node pointer
END CLASS

// Traversal: print all values
FUNCTION traverseList(head)
    current = head
    WHILE current != NULL DO
        PRINT current.data
        current = current.next
    END WHILE
END FUNCTION

// Search: return zero-based index or -1
FUNCTION searchList(head, target)
    current = head
    index = 0
    WHILE current != NULL DO
        IF current.data == target THEN
            RETURN index
        END IF
        current = current.next
        index = index + 1
    END WHILE
    RETURN -1
END FUNCTION

// Access by position: return value or INVALID_POSITION if invalid
FUNCTION getAtPosition(head, position)
    current = head
    index = 0
    WHILE current != NULL DO
        IF index == position THEN
            RETURN current.data
        END IF
        current = current.next
        index = index + 1
    END WHILE
    RETURN INVALID_POSITION
END FUNCTION
```
```cpp
#include <iostream>
#include <optional>

struct Node {
    int data;
    Node* next;
    Node(int val) : data(val), next(nullptr) {}
};

void traverseList(Node* head) {
    Node* current = head;
    while (current != nullptr) {
        std::cout << current->data << " ";
        current = current->next;
    }
    std::cout << std::endl;
}

int searchList(Node* head, int target) {
    Node* current = head;
    int index = 0;
    while (current != nullptr) {
        if (current->data == target) return index;
        current = current->next;
        index++;
    }
    return -1;
}

std::optional<int> getAtPosition(Node* head, int position) {
    Node* current = head;
    int index = 0;
    while (current != nullptr) {
        if (index == position) return current->data;
        current = current->next;
        index++;
    }
    return std::nullopt;
}

int main() {
    Node* head = new Node(12);
    head->next = new Node(7);
    head->next->next = new Node(19);

    traverseList(head);                           // Output: 12 7 19
    std::cout << searchList(head, 7) << std::endl; // Output: 1 (found)
    std::cout << searchList(head, 99) << std::endl;// Output: -1 (absent)
    auto value = getAtPosition(head, 2);
    if (value.has_value()) {
        std::cout << *value << std::endl; // Output: 19
    } else {
        std::cout << "Invalid position" << std::endl;
    }

    return 0;
}
```
```java
public class LinkedListOperations {
    static class Node {
        int data;
        Node next;
        Node(int val) { this.data = val; this.next = null; }
    }

    public static void traverseList(Node head) {
        Node current = head;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.next;
        }
        System.out.println();
    }

    public static int searchList(Node head, int target) {
        Node current = head;
        int index = 0;
        while (current != null) {
            if (current.data == target) return index;
            current = current.next;
            index++;
        }
        return -1;
    }

    public static Integer getAtPosition(Node head, int position) {
        Node current = head;
        int index = 0;
        while (current != null) {
            if (index == position) return current.data;
            current = current.next;
            index++;
        }
        return null;
    }

    public static void main(String[] args) {
        Node head = new Node(12);
        head.next = new Node(7);
        head.next.next = new Node(19);

        traverseList(head);                     // Output: 12 7 19
        System.out.println(searchList(head, 7)); // Output: 1
        System.out.println(searchList(head, 99));// Output: -1
        System.out.println(getAtPosition(head, 2)); // Output: 19
    }
}
```
```python
class Node:
    def __init__(self, data):
        self.data = data
        self.next = None

def traverse_list(head):
    current = head
    while current is not None:
        print(current.data, end=" ")
        current = current.next
    print()

def search_list(head, target):
    current = head
    index = 0
    while current is not None:
        if current.data == target:
            return index
        current = current.next
        index += 1
    return -1

def get_at_position(head, position):
    current = head
    index = 0
    while current is not None:
        if index == position:
            return current.data
        current = current.next
        index += 1
    return None

# Main execution
head = Node(12)
head.next = Node(7)
head.next.next = Node(19)

traverse_list(head)              # Output: 12 7 19
print(search_list(head, 7))      # Output: 1
print(search_list(head, 99))     # Output: -1
print(get_at_position(head, 2))  # Output: 19
```

## Summary

A singly linked list does not provide direct array-style index access. Position access and searching for a target require following links sequentially from `head`; each takes O(n) time in the worst case and O(1) auxiliary space.
