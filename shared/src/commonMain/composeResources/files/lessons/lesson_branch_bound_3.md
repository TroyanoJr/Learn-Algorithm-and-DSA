# Best-First Search and Priority Queues

While basic backtracking typically explores state-space trees using Depth-First Search (DFS) or Breadth-First Search (BFS), **Branch and Bound** frequently employs **Best-First Search** backed by a **priority queue** (min-heap or max-heap).

## Best-First Node Selection

Instead of blindly following a rigid DFS or BFS order, Best-First Search always expands the live node with the most promising bound (e.g., the minimum lower bound for minimization tasks):
- **Priority Queue**: Stores active nodes ordered by their bound values.
- **Immediate Promise**: By expanding the most promising node first, the algorithm often discovers high-quality feasible solutions early, lowering the incumbent cost and enabling more aggressive pruning of remaining branches.

## Search Strategy vs. Correctness

- **Strategy**: Queue ordering and node selection order affect search efficiency and speed in finding the optimal solution.
- **Correctness**: Correctness depends entirely on the validity and tightness of the bounding function, ensuring true optimal solutions are never mistakenly pruned.

### Code Example: Priority Queue Node Selection

The following code demonstrates selecting the most promising node using a min-heap priority queue comparison.

```pseudocode
function selectNextNode(pq)
    if pq is empty then
        return null
    end if
    bestNode = pq.poll() // Extract node with minimum bound
    return bestNode
end function

print("Priority queue selector ready")
```
```cpp
#include <iostream>
#include <queue>
#include <vector>

struct StateNode {
    int id;
    int bound;
    bool operator>(const StateNode& other) const {
        return bound > other.bound; // Min-heap ordering
    }
};

int main() {
    std::priority_queue<StateNode, std::vector<StateNode>, std::greater<StateNode>> pq;
    pq.push({1, 12});
    pq.push({2, 8});
    
    StateNode nextNode = pq.top();
    std::cout << "Next node ID: " << nextNode.id << " with bound: " << nextNode.bound << '\n';
}
```
```java
import java.util.PriorityQueue;
import java.util.Comparator;

class BestFirstSearchExample {
    static class StateNode {
        int id, bound;
        StateNode(int id, int bound) { this.id = id; this.bound = bound; }
    }

    public static void main(String[] args) {
        PriorityQueue<StateNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.bound));
        pq.offer(new StateNode(1, 12));
        pq.offer(new StateNode(2, 8));
        
        StateNode nextNode = pq.peek();
        System.out.println("Next node ID: " + nextNode.id + " with bound: " + nextNode.bound);
    }
}
```
```python
import heapq

pq = []
heapq.heappush(pq, (12, 1))  # (bound, node_id)
heapq.heappush(pq, (8, 2))

best_bound, best_node = heapq.heappop(pq)
print(f"Next node ID: {best_node} with bound: {best_bound}")
```

## Remember

- **Best-First Search** uses a priority queue to always expand the most promising node first.
- Discovering good feasible solutions early helps establish tighter incumbents.
- Queue order optimizes search efficiency, while bounding functions guarantee mathematical correctness.

## Summary

Priority queues enable Best-First Branch and Bound, prioritizing nodes with superior bounds to discover optimal solutions rapidly and prune effectively.
