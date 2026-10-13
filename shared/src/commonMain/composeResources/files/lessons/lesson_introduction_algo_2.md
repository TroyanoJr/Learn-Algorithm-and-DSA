# How Work Grows

An algorithm's **input size** is a simple way to describe how much data it receives. For a list, the input size is the number of items in that list. We can get an initial sense of an algorithm's efficiency by counting the steps it performs as the input gets larger.

## Compare the Work

Suppose a list has items stored in an array:

- If you already know an item's index, reading the item at that index takes one access, whether the array has 4 items or 8.
- If you need to find a value in an unsorted array by checking items one at a time, a search for a value that is not present checks every item. That is 4 comparisons for 4 items, or 8 comparisons for 8 items.

In the first situation, doubling the number of items leaves the number of accesses at one. In the second, doubling the number of items doubles the comparisons in this example. The situations have different information available: an index is already known in the first, while the second must discover whether a value is present.

**Big-O** describes how an algorithm's resource use, such as the number of operations, grows as the input size increases. The known-index access takes constant work, written `O(1)`, because it stays at one access as the list grows. The search for an absent value checks every item, so its work grows in proportion to the number of items, written `O(n)`.

Operation counts are a simplified way to reason about growth, not measurements of elapsed time. They do not mean every individual run takes longer whenever the input is larger: the work depends on the input and on what the algorithm has to do. In upcoming Searching and Sorting lessons, you will see how an algorithm's choices affect the work it does as its input grows.
