# 0/1 Knapsack with Branch and Bound

The **0/1 Knapsack Problem** is a classic optimization task: given items with weights and values and a maximum capacity `W`, choose a subset of items to maximize total value without exceeding capacity. Branch and Bound solves this efficiently using fractional knapsack relaxations as optimistic upper bounds.

## Fractional Relaxation as an Upper Bound

To estimate the maximum possible value achievable down any subtree in a maximization problem:
- **Fractional Relaxation**: Allow items to be divided into fractions. This can be solved greedily in `O(N log N)` time by sorting items by value-to-weight ratio.
- **Optimistic Bound**: The fractional knapsack value serves as an upper bound for the 0/1 integer knapsack problem. If the relaxed upper bound is less than or equal to the current incumbent value, the subtree is pruned.

### Code Example: 0/1 Knapsack Branch and Bound Upper Bound

The following code calculates the fractional knapsack upper bound for remaining items given current weight and value.

```pseudocode
function fractionalUpperBound(capacity, weight, value, currentIndex)
    upperBound = currentValue
    remainingCapacity = capacity - currentWeight
    
    for i from currentIndex to length(items) - 1 do
        if items[i].weight <= remainingCapacity then
            remainingCapacity = remainingCapacity - items[i].weight
            upperBound = upperBound + items[i].value
        else
            upperBound = upperBound + items[i].value * (remainingCapacity / items[i].weight)
            break
        end if
    end for
    return upperBound
end function

print("Knapsack upper bound calculation ready")
```
```cpp
#include <iostream>
#include <vector>
#include <algorithm>

struct Item {
    double weight;
    double value;
};

double computeUpperBound(double capacity, double currentWeight, double currentValue, 
                         const std::vector<Item>& items, size_t startIndex) {
    double upperBound = currentValue;
    double remainingCapacity = capacity - currentWeight;

    for (size_t i = startIndex; i < items.size(); ++i) {
        if (items[i].weight <= remainingCapacity) {
            remainingCapacity -= items[i].weight;
            upperBound += items[i].value;
        } else {
            upperBound += items[i].value * (remainingCapacity / items[i].weight);
            break;
        }
    }
    return upperBound;
}

int main() {
    std::vector<Item> items = {{2.0, 40.0}, {3.0, 50.0}, {5.0, 100.0}};
    std::cout << "Upper bound: " << computeUpperBound(10.0, 0.0, 0.0, items, 0) << '\n';
}
```
```java
class KnapsackBranchBound {
    static class Item {
        double weight, value;
        Item(double w, double v) { this.weight = w; this.value = v; }
    }

    static double computeUpperBound(double capacity, double currentWeight, double currentValue,
                                    Item[] items, int startIndex) {
        double upperBound = currentValue;
        double remainingCapacity = capacity - currentWeight;

        for (int i = startIndex; i < items.length; i++) {
            if (items[i].weight <= remainingCapacity) {
                remainingCapacity -= items[i].weight;
                upperBound += items[i].value;
            } else {
                upperBound += items[i].value * (remainingCapacity / items[i].weight);
                break;
            }
        }
        return upperBound;
    }

    public static void main(String[] args) {
        Item[] items = {new Item(2.0, 40.0), new Item(3.0, 50.0), new Item(5.0, 100.0)};
        System.out.println("Upper bound: " + computeUpperBound(10.0, 0.0, 0.0, items, 0));
    }
}
```
```python
def compute_upper_bound(capacity, current_weight, current_value, items, start_index):
    upper_bound = current_value
    remaining_capacity = capacity - current_weight

    for i in range(start_index, len(items)):
        if items[i]["weight"] <= remaining_capacity:
            remaining_capacity -= items[i]["weight"]
            upper_bound += items[i]["value"]
        else:
            upper_bound += items[i]["value"] * (remaining_capacity / items[i]["weight"])
            break
    return upper_bound

items = [{"weight": 2.0, "value": 40.0}, {"weight": 3.0, "value": 50.0}, {"weight": 5.0, "value": 100.0}]
print("Upper bound:", compute_upper_bound(10.0, 0.0, 0.0, items, 0))
```

## Complexity Analysis

- **Time Complexity**: Fractional relaxations take $O(N \log N)$ sorting time, reducing overall search space exploration time significantly.
- **Space Complexity**: $O(N)$ auxiliary call stack and priority queue space.

## Summary

Solving 0/1 Knapsack with Branch and Bound uses fractional knapsack relaxations to establish optimistic upper bounds, effectively pruning subtrees that cannot surpass the incumbent value.
