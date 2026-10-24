# Fractional Knapsack

In the **knapsack problem**, each item has a weight and a value, and the bag has a maximum capacity. In the fractional version, an item may be divided, so we can take part of it. The greedy rule is to consider items in decreasing value-per-weight ratio and take as much as possible of each.

Assume item weights are positive and values are non-negative. Positive weights make each ratio well-defined; non-negative values mean taking a fitting item does not reduce the total value.

## Worked Example

The capacity is 50. The items are:

- Item A: weight 10, value 60, ratio 6 per weight unit.
- Item B: weight 20, value 100, ratio 5 per weight unit.
- Item C: weight 30, value 120, ratio 4 per weight unit.

Take all of A and B (weight 30, value 160). There are 20 capacity units left. Take 20/30 of C, worth `120 * (20/30) = 80`. The bag is full at weight 50, with total value 240.

### Code Example

The examples use the same items and capacity. They sort by value/weight, take whole items while they fit, then take a fraction of the first item that does not fit. Each prints total value `240`.

```pseudocode
items = [(10, 60), (20, 100), (30, 120)]  // (weight, value)
sort items by value / weight descending
capacity = 50
totalValue = 0
for (weight, value) in items:
    amount = min(weight, capacity)
    totalValue = totalValue + amount * (value / weight)
    capacity = capacity - amount
    if capacity == 0:
        break
print(totalValue)
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>

struct Item { double weight; double value; };

int main() {
    std::vector<Item> items{{10,60},{20,100},{30,120}};
    std::sort(items.begin(), items.end(), [](const Item& a, const Item& b) {
        return a.value / a.weight > b.value / b.weight;
    });
    double capacity = 50;
    double totalValue = 0;
    for (const auto& item : items) {
        double amount = std::min(item.weight, capacity);
        totalValue += amount * (item.value / item.weight);
        capacity -= amount;
        if (capacity == 0) break;
    }
    std::cout << totalValue << '\n';
}
```
```java
import java.util.Arrays;

class FractionalKnapsack {
    static class Item {
        double weight; double value;
        Item(double weight, double value) { this.weight = weight; this.value = value; }
    }
    public static void main(String[] args) {
        Item[] items = {new Item(10,60), new Item(20,100), new Item(30,120)};
        Arrays.sort(items, (a, b) -> Double.compare(b.value / b.weight, a.value / a.weight));
        double capacity = 50;
        double totalValue = 0;
        for (Item item : items) {
            double amount = Math.min(item.weight, capacity);
            totalValue += amount * (item.value / item.weight);
            capacity -= amount;
            if (capacity == 0) break;
        }
        System.out.println(totalValue);
    }
}
```
```python
items = [(10, 60), (20, 100), (30, 120)]  # (weight, value)
items.sort(key=lambda item: item[1] / item[0], reverse=True)
capacity = 50
total_value = 0
for weight, value in items:
    amount = min(weight, capacity)
    total_value += amount * (value / weight)
    capacity -= amount
    if capacity == 0:
        break
print(total_value)
```

## Why Fractions Matter

The ratio rule is optimal for the fractional problem because every taken weight unit can be chosen independently: if a lower-ratio item is used while a higher-ratio available item is not full, shifting weight toward the higher ratio cannot lower value. In the 0/1 version, each item must be taken whole or left behind; sorting by ratio is not always optimal there.

## Complexity

For `n` items, sorting by ratio takes `O(n log n)` time and the scan takes `O(n)`, for total `O(n log n)`. The item collection uses `O(n)` storage; auxiliary sorting space depends on the sorting implementation. The capacity must be non-negative, and item weights must be positive so each value/weight ratio is defined.

## Summary

Fractional knapsack is a classic setting where a locally best ratio choice can be justified. The same rule should not be transferred automatically to 0/1 knapsack.
