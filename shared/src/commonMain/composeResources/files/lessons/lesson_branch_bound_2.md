# Bounds, Incumbents, and Pruning

Optimization problems require finding a solution that minimizes or maximizes an objective function. **Branch and Bound** achieves efficiency by maintaining an **incumbent** solution and calculating rigorous **bounds** to prune suboptimal branches.

## Feasible Solutions and Incumbents

- **Feasible Solution**: A complete assignment of choices that satisfies all problem constraints.
- **Incumbent**: The best feasible solution found so far. For minimization problems, the incumbent holds the minimum cost encountered; for maximization, it holds the maximum value.
- **Updating the Incumbent**: Whenever a new feasible solution is discovered with a cost superior to the current incumbent, the incumbent is updated.

## Optimistic Bounds and Pruning Rules

An **optimistic bound** is a mathematical guarantee on the best possible outcome obtainable from any extension of a partial state:
- **Minimization Problems**: The lower bound estimates the absolute minimum cost achievable down a subtree. If `lowerBound >= incumbentCost`, the subtree is pruned.
- **Maximization Problems**: The upper bound estimates the absolute maximum value achievable down a subtree. If `upperBound <= incumbentCost`, the subtree is pruned.

### Code Example: Pruning Decision Function

The following code evaluates whether a partial state node should be pruned based on its lower bound and the current minimum incumbent cost.

```pseudocode
function shouldPrune(nodeLowerBound, incumbentCost)
    if nodeLowerBound >= incumbentCost then
        return true // Prune subtree
    end if
    return false // Explore subtree
end function

print(shouldPrune(15, 10))
```
```cpp
#include <iostream>

bool shouldPrune(int nodeLowerBound, int incumbentCost) {
    if (nodeLowerBound >= incumbentCost) {
        return true; // Prune
    }
    return false; // Explore
}

int main() {
    std::cout << std::boolalpha << shouldPrune(15, 10) << '\n';
}
```
```java
class PruningCheck {
    static boolean shouldPrune(int nodeLowerBound, int incumbentCost) {
        if (nodeLowerBound >= incumbentCost) {
            return true; // Prune
        }
        return false; // Explore
    }

    public static void main(String[] args) {
        System.out.println(shouldPrune(15, 10));
    }
}
```
```python
def should_prune(node_lower_bound, incumbent_cost):
    if node_lower_bound >= incumbent_cost:
        return True  # Prune
    return False  # Explore

print(should_prune(15, 10))
```

## Remember

- The **incumbent** represents the best feasible solution found so far.
- Minimization uses **lower bounds**; maximization uses **upper bounds**.
- Subtrees with optimistic bounds that fail to beat the incumbent are pruned immediately.

## Summary

Bounds establish theoretical cost limits on partial states. Comparing node bounds against the incumbent enables aggressive tree pruning and faster optimization.
