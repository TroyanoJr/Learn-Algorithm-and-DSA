# Backtracking Fundamentals and State-Space Trees

**Backtracking** is a systematic algorithmic technique for solving constraint satisfaction and optimization problems by building candidates incrementally and abandoning a candidate (**backtracking**) as soon as it is determined that the candidate cannot lead to a valid solution.

## The Choice, Explore, Undo Paradigm

Backtracking explores a decision space modeled as an implicit **state-space tree**. At each step, the algorithm executes three fundamental actions:

1. **Choose**: Select a candidate choice from the available options at the current decision level.
2. **Explore**: Recursively move deeper into the state-space tree with the chosen option included in the current solution state.
3. **Undo (Backtrack)**: Revert the choice, restoring the solution state so that alternative options at the current decision level can be explored.

## Base Cases and Pruning

- **Base Case (Valid Solution)**: Reaching the target depth or satisfying all constraints (e.g. constructing a subset of length `N`). The complete candidate is recorded or printed.
- **Dead End (Pruning)**: Reaching a state that violates problem constraints. Instead of exploring further down that invalid branch, the algorithm immediately backtracks to save computation.

### Code Example: Generating Binary Strings of Length N

The following code generates all binary strings of length `N` by trying `'0'`, recursing, and then backtracking to try `'1'`.

```pseudocode
function generateBinary(n, current)
    if length(current) == n then
        print(current)
        return
    end if
    // Choice 1: Append '0' and explore
    generateBinary(n, current + "0")
    
    // Choice 2: Append '1' and explore
    generateBinary(n, current + "1")
end function

generateBinary(2, "")
```
```cpp
#include <iostream>
#include <string>

void generateBinary(int n, std::string current) {
    if (current.length() == n) {
        std::cout << current << " ";
        return;
    }
    // Choice 1: Append '0' and explore
    generateBinary(n, current + "0");
    
    // Choice 2: Append '1' and explore
    generateBinary(n, current + "1");
}

int main() {
    generateBinary(2, "");
    std::cout << '\n';
}
```
```java
class BinaryStringGenerator {
    static void generateBinary(int n, String current) {
        if (current.length() == n) {
            System.out.print(current + " ");
            return;
        }
        // Choice 1: Append '0' and explore
        generateBinary(n, current + "0");
        
        // Choice 2: Append '1' and explore
        generateBinary(n, current + "1");
    }

    public static void main(String[] args) {
        generateBinary(2, "");
        System.out.println();
    }
}
```
```python
def generate_binary(n, current=""):
    if len(current) == n:
        print(current, end=" ")
        return
    # Choice 1: Append '0' and explore
    generate_binary(n, current + "0")
    
    # Choice 2: Append '1' and explore
    generate_binary(n, current + "1")

generate_binary(2)
print()
```

## Remember

- Backtracking explores candidates incrementally using the **Choice -> Explore -> Undo** pattern.
- The decision space forms an implicit **state-space tree**.
- Reaching a dead end triggers a return (backtrack) to try alternative choices at earlier decision levels.

## Summary

Backtracking systematically searches state-space trees by making choices recursively and unmaking them when dead ends or valid solutions are reached.
