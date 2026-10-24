# Divide & Conquer Fundamentals

**Divide and Conquer** is a powerful algorithmic paradigm that solves a complex problem by breaking it down into smaller, manageable subproblems of the same type, solving those subproblems recursively, and then combining their results to solve the original problem.

## The Three Steps of Divide and Conquer

Every divide-and-conquer algorithm operates through three distinct phases:

1. **Divide**: Split the original problem into two or more smaller subproblems that are independent instances of the same problem.
2. **Conquer**: Solve each subproblem recursively. When subproblems become small enough (the base case), solve them directly without further recursion.
3. **Combine**: Take the solutions of the subproblems and merge or combine them together to form the solution to the original problem.

## When to Use Divide and Conquer

Divide and conquer is exceptionally effective for problems where:
- A problem can be divided into independent subproblems.
- Combining subproblem solutions is efficient.
- Breaking down the problem significantly reduces overall work (such as halving input size at each step, leading to logarithmic or quasilinear time complexity).

### Code Example: Finding Maximum via Divide and Conquer

The following code finds the maximum value in an array by splitting the array into halves, finding the maximum of each half recursively, and combining the results.

Assume the array is non-empty and `low` and `high` define a valid inclusive range.

```pseudocode
function findMax(arr, low, high)
    if low == high then
        return arr[low]
    end if
    mid = low + (high - low) / 2
    leftMax = findMax(arr, low, mid)
    rightMax = findMax(arr, mid + 1, high)
    if leftMax > rightMax then
        return leftMax
    else
        return rightMax
    end if
end function
```
```cpp
#include <iostream>
#include <vector>
#include <algorithm>

int findMax(const std::vector<int>& arr, int low, int high) {
    if (low == high) {
        return arr[low];
    }
    int mid = low + (high - low) / 2;
    int leftMax = findMax(arr, low, mid);
    int rightMax = findMax(arr, mid + 1, high);
    return std::max(leftMax, rightMax);
}

int main() {
    std::vector<int> arr = {4, 2, 9, 7, 5};
    std::cout << findMax(arr, 0, arr.size() - 1) << '\n';
}
```
```java
class DivideAndConquerMax {
    static int findMax(int[] arr, int low, int high) {
        if (low == high) {
            return arr[low];
        }
        int mid = low + (high - low) / 2;
        int leftMax = findMax(arr, low, mid);
        int rightMax = findMax(arr, mid + 1, high);
        return Math.max(leftMax, rightMax);
    }

    public static void main(String[] args) {
        int[] arr = {4, 2, 9, 7, 5};
        System.out.println(findMax(arr, 0, arr.length - 1));
    }
}
```
```python
def find_max(arr, low, high):
    if low == high:
        return arr[low]
    mid = low + (high - low) // 2
    left_max = find_max(arr, low, mid)
    right_max = find_max(arr, mid + 1, high)
    return max(left_max, right_max)

arr = [4, 2, 9, 7, 5]
print(find_max(arr, 0, len(arr) - 1))
```

## Remember

- The three phases are **Divide**, **Conquer**, and **Combine**.
- Base cases stop recursion when subproblems are small and directly solvable.
- Combining subproblem solutions should be efficient to maintain optimal performance.

## Summary

Divide and conquer solves complex tasks by splitting them into smaller recursive subproblems, conquering them at the base case, and combining results.
