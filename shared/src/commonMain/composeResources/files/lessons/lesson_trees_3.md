# Tree Traversals and Their Applications

Unlike linear data structures (such as arrays or linked lists) that have a single unambiguous start and end, trees can be visited in multiple systematic orders. **Tree traversal** refers to the process of visiting every node in a tree exactly once.

## Depth-First Search (DFS) Traversals

Depth-first traversals explore as deep as possible down each branch before backtracking. There are three classic recursive orders, named by when the parent (`Root`) is visited relative to its `Left` and `Right` subtrees:

1. **Pre-order (Root, Left, Right)**: Visit the root node first, then traverse the left subtree recursively, then traverse the right subtree recursively.
   - *Use case*: Creating a deep copy of a tree or serializing a tree structure.
2. **In-order (Left, Root, Right)**: Traverse the left subtree first, visit the root node, then traverse the right subtree recursively.
   - *Use case*: Visiting a Binary Search Tree (BST) visits all keys in **sorted ascending order**.
3. **Post-order (Left, Right, Root)**: Traverse the left subtree first, traverse the right subtree, and visit the root node last.
   - *Use case*: Deleting a tree bottom-up or evaluating mathematical expression trees.

## Breadth-First Search (BFS) / Level-Order Traversal

Instead of going deep immediately, **Level-order traversal** visits nodes level by level from top to bottom, and from left to right across each level.
- Implemented using a **Queue** data structure.
- Enqueue the root node, then repeatedly dequeue a node, visit its data, and enqueue its left and right children.

### Code Example: Recursive In-Order Traversal

The following code demonstrates a recursive in-order traversal that visits BST nodes in sorted order.

```pseudocode
function inOrder(node)
    if node != null then
        inOrder(node.left)
        print(node.val)
        inOrder(node.right)
    end if
end function

// For BST with root 5, left 3, right 7:
// Prints 3, then 5, then 7
```
```cpp
#include <iostream>

struct BSTNode {
    int val;
    BSTNode* left;
    BSTNode* right;
    BSTNode(int v) : val(v), left(nullptr), right(nullptr) {}
};

void inOrder(BSTNode* node) {
    if (node != nullptr) {
        inOrder(node->left);
        std::cout << node->val << " ";
        inOrder(node->right);
    }
}

int main() {
    BSTNode* root = new BSTNode(5);
    root->left = new BSTNode(3);
    root->right = new BSTNode(7);
    inOrder(root);
}
```
```java
class BSTNode {
    int val;
    BSTNode left;
    BSTNode right;
    BSTNode(int val) { this.val = val; }
}

class InOrderTraversal {
    static void inOrder(BSTNode node) {
        if (node != null) {
            inOrder(node.left);
            System.out.print(node.val + " ");
            inOrder(node.right);
        }
    }

    public static void main(String[] args) {
        BSTNode root = new BSTNode(5);
        root.left = new BSTNode(3);
        root.right = new BSTNode(7);
        inOrder(root);
    }
}
```
```python
class BSTNode:
    def __init__(self, val):
        self.val = val
        self.left = None
        self.right = None

def in_order(node):
    if node is not None:
        in_order(node.left)
        print(node.val, end=" ")
        in_order(node.right)

root = BSTNode(5)
root.left = BSTNode(3)
root.right = BSTNode(7)
in_order(root)
```

## Remember

- **Pre-order**: Root -> Left -> Right.
- **In-order**: Left -> Root -> Right (visits BST nodes in sorted order).
- **Post-order**: Left -> Right -> Root.
- **Level-order**: Top to bottom, left to right (uses a Queue).

## Summary

Tree traversals visit every node systematically. DFS uses recursion (pre-order, in-order, post-order), while BFS uses a queue for level-order traversal. In-order traversal uniquely prints BST nodes in sorted order.
