# How Work Grows

An algorithm's **input size** is a simple way to describe how much data it receives. For a list, the input size is the number of items in that list. We can get an initial sense of an algorithm's efficiency by counting the steps it performs as the input gets larger.

## Compare the Work

Suppose a list has items stored in an array:

- If you already know an item's index, reading the item at that index takes one access, whether the array has 4 items or 8.
- If you need to find a value in an unsorted array by checking items one at a time, a search for a value that is not present checks every item. That is 4 comparisons for 4 items, or 8 comparisons for 8 items.

In the first situation, doubling the number of items leaves the number of accesses at one. In the second, doubling the number of items doubles the comparisons in this example. The situations have different information available: an index is already known in the first, while the second must discover whether a value is present.

This is an informal way to compare work, not a precise measure of running time. Individual steps may take different amounts of time, and real programs also depend on other factors. In upcoming Searching and Sorting lessons, you will see how an algorithm's choices affect the work it does as its input grows.
