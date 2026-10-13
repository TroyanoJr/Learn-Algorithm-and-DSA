# Inserting Nodes

Inserting a new element into a singly linked list involves creating a new node and updating pointer references so the new node is connected into the list.

Unlike arrays, which require shifting existing elements to create an open spot, linked lists insert nodes by modifying pointer addresses. The exact work required depends on where the node is placed and whether you already hold a reference to the insertion location.

## Safe Order of Pointer Updates

When inserting a new node after an existing node `prevNode`, pointer updates **must** occur in a specific order:

1. **Step 1:** Set `newNode.next = prevNode.next` (connect the new node to the rest of the list).
2. **Step 2:** Set `prevNode.next = newNode` (point the preceding node to the new node).

**Critical Pitfall:** If you update `prevNode.next = newNode` first, you overwrite the only pointer referencing the rest of the list, permanently losing access to all subsequent nodes!

## 1. Inserting at the Head

Inserting at the head places a new node at the very beginning of the list, making it the new `head`.

### Trace on List `[12] → [7] → [19] → null`:
Goal: Insert value `5` at the head.
- **Initial:** `head → [12] → [7] → [19] → null`
- **Step 1:** Create `newNode = Node(5)`.
- **Step 2:** Set `newNode.next = head` (so `newNode` points to `[12]`).
- **Step 3:** Update `head = newNode`.
- **Result:** `head → [5] → [12] → [7] → [19] → null`

**Empty List Case:** If `head` is `null`, `newNode.next = null`, and `head = newNode`. The node becomes the sole element in the list.

## 2. Inserting After a Known Node

If you already have a pointer to node `prevNode`, inserting immediately after it requires modifying only two links.

### Trace on List `[12] → [7] → [19] → null`:
Goal: Insert value `15` after node `[7]`.
- **Initial:** `head → [12] → [7] → [19] → null` (where `prevNode` points to `[7]`).
- **Step 1:** Create `newNode = Node(15)`.
- **Step 2:** Set `newNode.next = prevNode.next` (so `newNode` points to `[19]`).
- **Step 3:** Set `prevNode.next = newNode` (so `[7]` points to `[15]`).
- **Result:** `head → [12] → [7] → [15] → [19] → null`

## 3. Inserting at the End (Tail) or by Position

To insert at the end or at a specific zero-based index when you do not hold a direct reference:
1. Traverse the list starting from `head` to locate the target position or tail node.
2. Perform the two-step pointer update once the insertion location is reached.

### Trace on List `[12] → [7] → [19] → null`:
Goal: Insert value `25` at the tail.
- Traverse from `head` until reaching node `[19]` whose `next` is `null`.
- Create `newNode = Node(25)`.
- Set `[19].next = newNode`.
- Result: `head → [12] → [7] → [19] → [25] → null`

## Time and Auxiliary Space Complexity

- **Inserting at the Head:** O(1) constant time because no traversal is required.
- **Inserting After a Known Node:** O(1) constant time because updating two pointers takes constant work.
- **Inserting at Tail / Position k without Pointer:** O(n) time because traversing to the k-th node or tail requires following k sequential links.
- **Auxiliary Space:** O(1) space for pointer manipulation.

## Code Example: Node Insertion Operations

The following code demonstrates inserting at the head, inserting after a node, and inserting at the tail.

```pseudocode
CLASS Node
    data: INTEGER
    next: Node pointer
END CLASS

// Insert at Head - O(1)
FUNCTION insertAtHead(head, val)
    newNode = NEW Node(val)
    newNode.next = head
    RETURN newNode // New head
END FUNCTION

// Insert After a Known Node - O(1)
PROCEDURE insertAfter(prevNode, val)
    IF prevNode == NULL THEN RETURN
    newNode = NEW Node(val)
    newNode.next = prevNode.next // Step 1: Connect to rest of list
    prevNode.next = newNode     // Step 2: Connect prev to newNode
END PROCEDURE

// Insert at Tail - O(n) traversal
FUNCTION insertAtTail(head, val)
    newNode = NEW Node(val)
    IF head == NULL THEN
        RETURN newNode
    END IF
    current = head
    WHILE current.next != NULL DO
        current = current.next
    END WHILE
    current.next = newNode
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

Node* insertAtHead(Node* head, int val) {
    Node* newNode = new Node(val);
    newNode->next = head;
    return newNode; // Returns new head
}

void insertAfter(Node* prevNode, int val) {
    if (prevNode == nullptr) return;
    Node* newNode = new Node(val);
    newNode->next = prevNode->next; // Step 1
    prevNode->next = newNode;       // Step 2
}

Node* insertAtTail(Node* head, int val) {
    Node* newNode = new Node(val);
    if (head == nullptr) return newNode;
    Node* current = head;
    while (current->next != nullptr) {
        current = current->next;
    }
    current->next = newNode;
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

    head = insertAtHead(head, 5);          // 5 -> 12 -> 7 -> 19
    insertAfter(head->next->next, 15);     // 5 -> 12 -> 7 -> 15 -> 19
    head = insertAtTail(head, 25);         // 5 -> 12 -> 7 -> 15 -> 19 -> 25

    printList(head); // Output: 5 12 7 15 19 25
    return 0;
}
```
```java
public class LinkedListInsert {
    static class Node {
        int data;
        Node next;
        Node(int val) { this.data = val; this.next = null; }
    }

    public static Node insertAtHead(Node head, int val) {
        Node newNode = new Node(val);
        newNode.next = head;
        return newNode;
    }

    public static void insertAfter(Node prevNode, int val) {
        if (prevNode == null) return;
        Node newNode = new Node(val);
        newNode.next = prevNode.next; // Step 1
        prevNode.next = newNode;     // Step 2
    }

    public static Node insertAtTail(Node head, int val) {
        Node newNode = new Node(val);
        if (head == null) return newNode;
        Node current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = newNode;
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

        head = insertAtHead(head, 5);
        insertAfter(head.next.next, 15);
        head = insertAtTail(head, 25);

        printList(head); // Output: 5 12 7 15 19 25
    }
}
```
```python
class Node:
    def __init__(self, data):
        self.data = data
        self.next = None

def insert_at_head(head, val):
    new_node = Node(val)
    new_node.next = head
    return new_node

def insert_after(prev_node, val):
    if prev_node is None:
        return
    new_node = Node(val)
    new_node.next = prev_node.next # Step 1
    prev_node.next = new_node      # Step 2

def insert_at_tail(head, val):
    new_node = Node(val)
    if head is None:
        return new_node
    current = head
    while current.next is not None:
        current = current.next
    current.next = new_node
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

head = insert_at_head(head, 5)
insert_after(head.next.next, 15)
head = insert_at_tail(head, 25)

print_list(head) # Output: 5 12 7 15 19 25
```

## Summary

Inserting a node into a singly linked list requires updating pointer links in a safe sequence (`newNode.next = prev.next` before `prev.next = newNode`). Inserting at the head or after a known node takes O(1) constant time, while finding an arbitrary insertion position by traversing the list takes O(n) time.
