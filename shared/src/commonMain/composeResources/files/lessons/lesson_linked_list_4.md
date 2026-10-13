# Removing Nodes

Removing a node from a singly linked list involves adjusting pointer links to bypass the node being deleted, effectively unlinking it from the chain.

Unlike arrays, which require shifting remaining elements left to fill a gap, removing a linked list node requires only updating pointer references. However, because pointers only flow forward in a singly linked list, removing a non-head node requires finding its **predecessor node**.

## 1. Removing the Head Node

Removing the first node in a list does not require finding a predecessor. You simply update `head` to point to the second node in the list.

### Trace on List `[12] → [7] → [19] → null`:
Goal: Remove the head node (`[12]`).
- **Initial:** `head → [12] → [7] → [19] → null`
- **Step 1:** Store `temp = head` (`[12]`).
- **Step 2:** Advance `head = head.next` (so `head` now points to `[7]`).
- **Step 3:** Free/unlink `temp` memory if required by the language.
- **Result:** `head → [7] → [19] → null`

**Empty List Case:** If `head` is `null`, there is nothing to remove; return `null`.
**Single-Node List Case:** If `head` points to `[12] → null`, `head = head.next` sets `head` to `null`, correctly leaving an empty list.

## 2. Removing an Interior or Tail Node

To remove a node inside or at the end of a singly linked list, you cannot unlink the target node directly using only a pointer to the target node. You must locate its **predecessor** node (`prev`) so you can execute:

`prev.next = targetNode.next`

This bypasses `targetNode`, routing the predecessor link directly to the node following `targetNode`.

### Trace on List `[12] → [7] → [19] → null`:
Goal: Remove node with value `7`.
- **Initial:** `head → [12] → [7] → [19] → null`
- **Step 1:** Traverse to find `target = [7]` and its predecessor `prev = [12]`.
- **Step 2:** Bypass `[7]` by setting `prev.next = target.next` (so `[12].next = [19]`).
- **Step 3:** Free/unlink target node `[7]`.
- **Result:** `head → [12] → [19] → null`

## 3. Handling Unfound Target Values

When searching for a value that is not in the list:
- Traverse until `curr` matches the target or `curr` becomes `null`.
- If `curr` becomes `null`, the value was not found; leave the list unchanged.

## Time and Auxiliary Space Complexity

- **Removing the Head:** O(1) constant time because no traversal is needed.
- **Unlinking a Node After Predecessor Is Known:** O(1) constant time to update `prev.next = target.next`.
- **Finding a Target or Predecessor by Traversal:** O(n) worst-case time to traverse up to n nodes.
- **Auxiliary Space:** O(1) space using iterative pointer variables.

## Code Example: Node Removal Operations

The following code demonstrates removing the head node and removing a node by value.

```pseudocode
CLASS Node
    data: INTEGER
    next: Node pointer
END CLASS

// Remove Head - O(1)
FUNCTION removeHead(head)
    IF head == NULL THEN
        RETURN NULL
    END IF
    newHead = head.next
    RETURN newHead
END FUNCTION

// Remove Node By Value - O(n)
FUNCTION removeByValue(head, targetVal)
    IF head == NULL THEN
        RETURN NULL
    END IF
    // Case 1: Target is at head
    IF head.data == targetVal THEN
        RETURN head.next
    END IF

    // Case 2: Target is in middle or tail
    curr = head
    WHILE curr.next != NULL AND curr.next.data != targetVal DO
        curr = curr.next
    END WHILE

    // If target was found, bypass it
    IF curr.next != NULL THEN
        curr.next = curr.next.next
    END IF

    RETURN head
END FUNCTION
```
```cpp
#include <iostream>

struct Node {
    int data;
    Node* next;
    Node(int val) : data(val), next(nullptr) {}
};

Node* removeHead(Node* head) {
    if (head == nullptr) return nullptr;
    Node* temp = head;
    head = head->next;
    delete temp; // Free memory in C++
    return head;
}

Node* removeByValue(Node* head, int targetVal) {
    if (head == nullptr) return nullptr;

    // Case 1: Target is at head
    if (head->data == targetVal) {
        Node* temp = head;
        head = head->next;
        delete temp;
        return head;
    }

    // Case 2: Target is in middle or tail
    Node* curr = head;
    while (curr->next != nullptr && curr->next->data != targetVal) {
        curr = curr->next;
    }

    if (curr->next != nullptr) {
        Node* temp = curr->next;
        curr->next = curr->next->next; // Bypass
        delete temp;
    }

    return head;
}

void printList(Node* head) {
    Node* curr = head;
    while (curr != nullptr) {
        std::cout << curr->data << " ";
        curr = curr->next;
    }
    std::cout << std::endl;
}

int main() {
    Node* head = new Node(12);
    head->next = new Node(7);
    head->next->next = new Node(19);

    head = removeByValue(head, 7);  // Removes 7 -> List: 12 -> 19
    printList(head);                // Output: 12 19

    head = removeHead(head);        // Removes 12 -> List: 19
    printList(head);                // Output: 19

    return 0;
}
```
```java
public class LinkedListRemove {
    static class Node {
        int data;
        Node next;
        Node(int val) { this.data = val; this.next = null; }
    }

    public static Node removeHead(Node head) {
        if (head == null) return null;
        return head.next;
    }

    public static Node removeByValue(Node head, int targetVal) {
        if (head == null) return null;

        // Case 1: Target is at head
        if (head.data == targetVal) {
            return head.next;
        }

        // Case 2: Target is in middle or tail
        Node curr = head;
        while (curr.next != null && curr.next.data != targetVal) {
            curr = curr.next;
        }

        if (curr.next != null) {
            curr.next = curr.next.next; // Bypass target
        }

        return head;
    }

    public static void printList(Node head) {
        Node curr = head;
        while (curr != null) {
            System.out.print(curr.data + " ");
            curr = curr.next;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Node head = new Node(12);
        head.next = new Node(7);
        head.next.next = new Node(19);

        head = removeByValue(head, 7);  // List: 12 -> 19
        printList(head);                // Output: 12 19

        head = removeHead(head);        // List: 19
        printList(head);                // Output: 19
    }
}
```
```python
class Node:
    def __init__(self, data):
        self.data = data
        self.next = None

def remove_head(head):
    if head is None:
        return None
    return head.next

def remove_by_value(head, target_val):
    if head is None:
        return None

    # Case 1: Target is at head
    if head.data == target_val:
        return head.next

    # Case 2: Target is in middle or tail
    curr = head
    while curr.next is not None and curr.next.data != target_val:
        curr = curr.next

    if curr.next is not None:
        curr.next = curr.next.next # Bypass target

    return head

def print_list(head):
    curr = head
    while curr is not None:
        print(curr.data, end=" ")
        curr = curr.next
    print()

# Execution
head = Node(12)
head.next = Node(7)
head.next.next = Node(19)

head = remove_by_value(head, 7)  # List: 12 -> 19
print_list(head)                # Output: 12 19

head = remove_head(head)        # List: 19
print_list(head)                # Output: 19
```

## Summary

Removing a node from a singly linked list requires unlinking it from the chain. Removing the head node takes O(1) constant time by advancing `head = head.next`. Removing an interior or tail node requires finding the target's predecessor (`prev`) so you can set `prev.next = target.next`, taking O(n) traversal time.
