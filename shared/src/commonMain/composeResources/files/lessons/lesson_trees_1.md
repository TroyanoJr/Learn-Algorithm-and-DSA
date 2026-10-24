# Tree Fundamentals and Terminology

A **tree** is a non-linear, hierarchical data structure composed of **nodes** connected by **edges**. Unlike linear data structures (such as arrays, stacks, and linked lists) that store elements sequentially, trees organize data in parent-child relationships, mirroring real-world hierarchies like file systems, organization charts, and HTML document object models (DOM).

## Key Concepts and Terminology

- **Node**: An element in the tree that contains data and links (references) to other nodes.
- **Edge**: The link or connection between two nodes.
- **Root**: The topmost node in the tree. A tree has exactly one root node, which has no parent.
- **Parent**: A node that has one or more child nodes connected below it.
- **Child**: A node connected directly below a parent node.
- **Siblings**: Nodes that share the exact same parent node.
- **Leaf (Terminal Node)**: A node that has no children (its child links are null).
- **Subtree**: A tree structure formed by a child node and all of its descendants.

## Measuring Trees: Height and Depth

To analyze tree structures and algorithm performance, two key measurements are used:

- **Depth of a Node**: The number of edges along the unique path from the **root** to that specific node. The root node has a depth of `0`.
- **Height of a Node**: The number of edges on the longest path from that node down to a **leaf**. A leaf node has a height of `0`.
- **Height of a Tree**: The height of the root node (the length of the longest path from root to any leaf).

## General Trees vs. Restricted Trees

In a **general tree**, a parent node can have an arbitrary number of children (0, 1, 2, or more). To store a general tree, a node typically maintains a data value and a dynamic list or collection of references to its children.

### Code Example: General Tree Node and Child Addition

The following examples demonstrate creating a general tree node and appending child nodes to its collection.

```pseudocode
structure TreeNode
    data
    children = []
end structure

function createNode(val)
    node = new TreeNode
    node.data = val
    return node
end function

function addChild(parent, child)
    parent.children.add(child)
end function

root = createNode("Root")
childA = createNode("Child A")
childB = createNode("Child B")
addChild(root, childA)
addChild(root, childB)
print(root.data, length(root.children))
```
```cpp
#include <iostream>
#include <vector>
#include <string>

struct TreeNode {
    std::string data;
    std::vector<TreeNode*> children;

    TreeNode(const std::string& val) : data(val) {}
};

int main() {
    TreeNode* root = new TreeNode("Root");
    TreeNode* childA = new TreeNode("Child A");
    TreeNode* childB = new TreeNode("Child B");

    root->children.push_back(childA);
    root->children.push_back(childB);

    std::cout << root->data << " " << root->children.size() << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.List;

class TreeNode {
    String data;
    List<TreeNode> children = new ArrayList<>();

    TreeNode(String data) {
        this.data = data;
    }
}

class TreeExample {
    public static void main(String[] args) {
        TreeNode root = new TreeNode("Root");
        TreeNode childA = new TreeNode("Child A");
        TreeNode childB = new TreeNode("Child B");

        root.children.add(childA);
        root.children.add(childB);

        System.out.println(root.data + " " + root.children.size());
    }
}
```
```python
class TreeNode:
    def __init__(self, data):
        self.data = data
        self.children = []

root = TreeNode("Root")
child_a = TreeNode("Child A")
child_b = TreeNode("Child B")

root.children.append(child_a)
root.children.append(child_b)

print(f"{root.data} {len(root.children)}")
```

## Remember

- A tree is a **hierarchical** non-linear data structure with one **root** node and 0 or more subtrees.
- **Depth** measures distance down from the root; **height** measures distance up from a leaf.
- A leaf node has no children (`children` list is empty or links are null).

## Summary

Trees organize data hierarchically. A tree consists of a root node, edges connecting parents to children, and leaves at the bottom. Measuring tree height and depth helps evaluate algorithm efficiency.
