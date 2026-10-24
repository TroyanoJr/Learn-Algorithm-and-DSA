# Binary Search Tree Operations, Complexity, and Trade-offs

A **Binary Search Tree (BST)** organizes data for efficient searching, insertion, and deletion. Understanding its time complexity requires examining how tree shape (balanced versus skewed) affects height.

## Core BST Operations

1. **Search**: Compare target key with current node. If equal, found. If target < current node, search left subtree. If target > current node, search right subtree.
2. **Insertion**: Similar to search, traverse down left or right branches until reaching a null reference, then create and attach the new node.
3. **Deletion**: Three cases:
   - *Case 1 (Leaf node)*: Remove directly by setting parent's child reference to null.
   - *Case 2 (Node with one child)*: Bypass the node by linking its parent directly to its single child.
   - *Case 3 (Node with two children)*: Find the **in-order successor** (the minimum value in the right subtree) or in-order predecessor, copy its value into the node, and delete the successor node.

## Time and Space Complexity

| Operation | Average Case (Balanced) | Worst Case (Skewed) | Explanation |
|---|---|---|---|
| **Search (`search`)** | $O(\log N)$ | $O(N)$ | Eliminates half the remaining nodes per level if balanced; degenerates to linear linked list if skewed. |
| **Insertion (`insert`)** | $O(\log N)$ | $O(N)$ | Traverses down one path from root to leaf. |
| **Deletion (`remove`)** | $O(\log N)$ | $O(N)$ | Finds node and handles child restructuring. |

## Balanced vs. Skewed Trees

- **Balanced BST**: Subtree heights differ by at most 1, keeping height $H = O(\log N)$. Operations run efficiently in $O(\log N)$ time.
- **Skewed BST**: If elements are inserted in already sorted order (e.g. 1, 2, 3, 4, 5), every node attaches only to the right child, forming a straight chain. Height becomes $H = O(N)$, reducing performance to $O(N)$ linear time. Self-balancing trees (such as AVL trees or Red-Black trees) solve this by rotating nodes automatically.

### Code Example: BST Search Operation

The following code searches for a key in a Binary Search Tree recursively.

```pseudocode
function search(node, target)
    if node == null or node.val == target then
        return node
    end if
    if target < node.val then
        return search(node.left, target)
    else
        return search(node.right, target)
    end if
end function
```
```cpp
#include <iostream>

struct BSTNode {
    int val;
    BSTNode* left;
    BSTNode* right;
    BSTNode(int v) : val(v), left(nullptr), right(nullptr) {}
};

BSTNode* search(BSTNode* node, int target) {
    if (node == nullptr || node->val == target) {
        return node;
    }
    if (target < node->val) {
        return search(node->left, target);
    } else {
        return search(node->right, target);
    }
}

int main() {
    BSTNode* root = new BSTNode(10);
    root->left = new BSTNode(5);
    root->right = new BSTNode(15);
    BSTNode* found = search(root, 5);
    if (found != nullptr) {
        std::cout << "Found: " << found->val << '\n';
    }
}
```
```java
class BSTNode {
    int val;
    BSTNode left;
    BSTNode right;
    BSTNode(int val) { this.val = val; }
}

class BSTSearch {
    static BSTNode search(BSTNode node, int target) {
        if (node == null || node.val == target) {
            return node;
        }
        if (target < node.val) {
            return search(node.left, target);
        } else {
            return search(node.right, target);
        }
    }

    public static void main(String[] args) {
        BSTNode root = new BSTNode(10);
        root.left = new BSTNode(5);
        root.right = new BSTNode(15);
        BSTNode found = search(root, 5);
        if (found != null) {
            System.out.println("Found: " + found.val);
        }
    }
}
```
```python
class BSTNode:
    def __init__(self, val):
        self.val = val
        self.left = None
        self.right = None

def search(node, target):
    if node is None or node.val == target:
        return node
    if target < node.val:
        return search(node.left, target)
    else:
        return search(node.right, target)

root = BSTNode(10)
root.left = BSTNode(5)
root.right = BSTNode(15)
found = search(root, 5)
if found:
    print(f"Found: {found.val}")
```

## Remember

- BST operations run in **$O(\log N)$ average time** when the tree remains balanced.
- In a **skewed** BST (e.g., inserted sorted data), height becomes $O(N)$, degrading operations to $O(N)$ linear time.
- Deletion has three cases: leaf, single child, and two children (requiring an in-order successor).

## Summary

Binary Search Trees provide efficient hierarchical search and modification in $O(\log N)$ average time. Tree balance is critical: skewed trees lose their logarithmic efficiency and perform like linked lists.
