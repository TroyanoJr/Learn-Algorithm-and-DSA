# Binary Trees and Binary Search Trees

A **binary tree** is a specialized type of tree in which every node has at most **two children**, referred to as the `left` child and the `right` child. Binary trees form the foundation for many efficient hierarchical data structures, most notably the **Binary Search Tree (BST)**.

## Key Concepts and Classifications

- **Binary Tree**: A tree where each node has 0, 1, or 2 children (`left` and `right`).
- **Full Binary Tree**: Every node other than the leaves has exactly two children.
- **Complete Binary Tree**: All levels except possibly the last are completely filled, and all nodes in the last level are as far left as possible.
- **Balanced vs. Skewed**: A balanced binary tree has its left and right subtrees roughly equal in height (`O(log N)` height), whereas a skewed binary tree degenerates into a linear chain resembling a linked list (`O(N)` height).

## Binary Search Tree (BST) Property

A **Binary Search Tree** is a binary tree that satisfies the strict **BST ordering property**:
1. For any node `X`, all values in its **left subtree** are strictly less than `X`'s value (`< value`).
2. For any node `X`, all values in its **right subtree** are strictly greater than `X`'s value (`> value`).
3. Both the left and right subtrees must also be binary search trees.

*Important Distinction*: Not every binary tree is a Binary Search Tree! A standard binary tree has no ordering rules between parents and children; a BST enforces strict left-less-than, right-greater-than ordering.

### Code Example: Binary Search Tree Node Insertion

The following versions demonstrate defining a BST node and inserting values recursively according to the BST property.

```pseudocode
structure BSTNode
    val
    left = null
    right = null
end structure

function insert(node, value)
    if node == null then
        newNode = new BSTNode
        newNode.val = value
        return newNode
    end if
    if value < node.val then
        node.left = insert(node.left, value)
    else if value > node.val then
        node.right = insert(node.right, value)
    end if
    return node
end function

root = null
root = insert(root, 5)
root = insert(root, 3)
root = insert(root, 7)
print(root.val, root.left.val, root.right.val)
```
```cpp
#include <iostream>

struct BSTNode {
    int val;
    BSTNode* left;
    BSTNode* right;

    BSTNode(int v) : val(v), left(nullptr), right(nullptr) {}
};

BSTNode* insert(BSTNode* node, int value) {
    if (node == nullptr) {
        return new BSTNode(value);
    }
    if (value < node->val) {
        node->left = insert(node->left, value);
    } else if (value > node->val) {
        node->right = insert(node->right, value);
    }
    return node;
}

int main() {
    BSTNode* root = nullptr;
    root = insert(root, 5);
    root = insert(root, 3);
    root = insert(root, 7);
    std::cout << root->val << " " << root->left->val << " " << root->right->val << '\n';
}
```
```java
class BSTNode {
    int val;
    BSTNode left;
    BSTNode right;

    BSTNode(int val) {
        this.val = val;
    }
}

class BSTExample {
    static BSTNode insert(BSTNode node, int value) {
        if (node == null) {
            return new BSTNode(value);
        }
        if (value < node.val) {
            node.left = insert(node.left, value);
        } else if (value > node.val) {
            node.right = insert(node.right, value);
        }
        return node;
    }

    public static void main(String[] args) {
        BSTNode root = null;
        root = insert(root, 5);
        root = insert(root, 3);
        root = insert(root, 7);
        System.out.println(root.val + " " + root.left.val + " " + root.right.val);
    }
}
```
```python
class BSTNode:
    def __init__(self, val):
        self.val = val
        self.left = None
        self.right = None

def insert(node, value):
    if node is None:
        return BSTNode(value)
    if value < node.val:
        node.left = insert(node.left, value)
    elif value > node.val:
        node.right = insert(node.right, value)
    return node

root = None
root = insert(root, 5)
root = insert(root, 3)
root = insert(root, 7)
print(f"{root.val} {root.left.val} {root.right.val}")
```

## Remember

- A binary tree restricts every node to at most **two children** (`left` and `right`).
- A **Binary Search Tree (BST)** enforces that left descendants are smaller and right descendants are larger than the parent node.
- Not all binary trees are ordered; BST ordering enables efficient search and traversal.

## Summary

Binary trees restrict children to two per node. Binary Search Trees add ordered rules (left < parent < right), allowing efficient hierarchical searching and insertion.
