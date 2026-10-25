# Branch and Bound Fundamentals and State-Space Search

**Branch and Bound** is a systematic algorithmic technique used to solve combinatorial optimization problems. While backtracking explores all valid solutions in constraint satisfaction problems, Branch and Bound focuses on finding the **optimal** (minimum or maximum) solution by methodically partitioning the search space into subproblems (**branching**) and using numerical estimates (**bounding**) to eliminate suboptimal subtrees early.

## State-Space Search and Exhaustive Baselines

Like backtracking, Branch and Bound models the solution space as an implicit **state-space tree**:
- **Branching**: Divides a current decision node into child nodes representing subsequent choices.
- **Exhaustive Limitation**: Pure brute force or naive backtracking explores exponential or factorial node permutations without regard to cost, often visiting unpromising branches that cannot improve upon known solutions.

## The Incumbent and Bounding Concept

Branch and Bound introduces two critical mechanisms to prune the state-space tree:
1. **Incumbent Solution**: Stores the best valid solution found so far during the search. Its objective cost serves as a threshold.
2. **Bounding Function**: Computes an optimistic cost estimate (lower bound for minimization, upper bound for maximization) for any partial node state. If a node's optimistic bound is worse than the current incumbent's cost, that entire subtree is pruned immediately.

### Code Example: Exploring State-Space Nodes

The following code illustrates a basic state-space tree traversal structure for comparing candidate costs against an incumbent best cost.

```pseudocode
function searchBranchBound(node, incumbentCost)
    if isLeaf(node) then
        if node.cost < incumbentCost then
            return node.cost
        end if
        return incumbentCost
    end if
    
    // Branch to child nodes
    for each child in node.children do
        if optimisticBound(child) < incumbentCost then
            incumbentCost = searchBranchBound(child, incumbentCost)
        end if
    end for
    return incumbentCost
end function

print("Branch and Bound initialized")
```
```cpp
#include <iostream>
#include <vector>
#include <algorithm>

struct Node {
    int cost;
    int bound;
    std::vector<Node*> children;
};

int searchBranchBound(Node* node, int incumbentCost) {
    if (node->children.empty()) {
        if (node->cost < incumbentCost) {
            return node->cost;
        }
        return incumbentCost;
    }
    for (Node* child : node->children) {
        if (child->bound < incumbentCost) {
            incumbentCost = searchBranchBound(child, incumbentCost);
        }
    }
    return incumbentCost;
}

int main() {
    std::cout << "Branch and Bound initialized\n";
}
```
```java
import java.util.List;
import java.util.ArrayList;

class BranchBoundBase {
    static class Node {
        int cost;
        int bound;
        List<Node> children = new ArrayList<>();
    }

    static int searchBranchBound(Node node, int incumbentCost) {
        if (node.children.isEmpty()) {
            if (node.cost < incumbentCost) {
                return node.cost;
            }
            return incumbentCost;
        }
        for (Node child : node.children) {
            if (child.bound < incumbentCost) {
                incumbentCost = searchBranchBound(child, incumbentCost);
            }
        }
        return incumbentCost;
    }

    public static void main(String[] args) {
        System.out.println("Branch and Bound initialized");
    }
}
```
```python
def search_branch_bound(node, incumbent_cost):
    if not node.get("children"):
        if node["cost"] < incumbent_cost:
            return node["cost"]
        return incumbent_cost
    
    for child in node["children"]:
        if child["bound"] < incumbent_cost:
            incumbent_cost = search_branch_bound(child, incumbent_cost)
    return incumbent_cost

print("Branch and Bound initialized")
```

## Remember

- Branch and Bound solves optimization problems by systematically searching state-space trees.
- **Branching** divides nodes, while **Bounding** computes optimistic cost limits.
- Subtrees whose bounds fail to beat the **incumbent** cost are pruned.

## Summary

Branch and Bound combines state-space branching with cost bounding to prune unpromising subtrees and efficiently locate optimal solutions.
