# Sequence Alignment: Longest Common Subsequence (LCS)

String and sequence alignment problems compare two sequences to find similarities. The **Longest Common Subsequence (LCS)** problem finds the length of the longest subsequence present in both strings in the same relative order (though not necessarily contiguous).

## State Definition and Recurrence

- **State Definition**: Let `dp[i][j]` represent the length of the LCS for prefix `text1[0..i-1]` and prefix `text2[0..j-1]`.
- **Transitions**:
  - **Characters Match** (`text1[i-1] == text2[j-1]`):
    `dp[i][j] = 1 + dp[i-1][j-1]`
  - **Characters Mismatch**:
    `dp[i][j] = max(dp[i-1][j], dp[i][j-1])`
- **Base Cases**: `dp[0][j] = 0` and `dp[i][0] = 0` (empty string alignment has length 0).

### Code Example: Longest Common Subsequence

The following code computes the LCS length for strings `"abcde"` and `"ace"`.

```pseudocode
function longestCommonSubsequence(text1, text2)
    m = length(text1)
    n = length(text2)
    dp = 2D array of size (m + 1) x (n + 1) filled with 0
    
    for i from 1 to m do
        for j from 1 to n do
            if text1[i - 1] == text2[j - 1] then
                dp[i][j] = 1 + dp[i - 1][j - 1]
            else
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
            end if
        end for
    end for
    return dp[m][n]
end function

print(longestCommonSubsequence("abcde", "ace"))
```
```cpp
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>

int longestCommonSubsequence(std::string text1, std::string text2) {
    int m = text1.length();
    int n = text2.length();
    std::vector<std::vector<int>> dp(m + 1, std::vector<int>(n + 1, 0));
    
    for (int i = 1; i <= m; ++i) {
        for (int j = 1; j <= n; ++j) {
            if (text1[i - 1] == text2[j - 1]) {
                dp[i][j] = 1 + dp[i - 1][j - 1];
            } else {
                dp[i][j] = std::max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
    }
    return dp[m][n];
}

int main() {
    std::cout << longestCommonSubsequence("abcde", "ace") << '\n';
}
```
```java
class LCSIterative {
    static int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        System.out.println(longestCommonSubsequence("abcde", "ace"));
    }
}
```
```python
def longest_common_subsequence(text1, text2):
    m, n = len(text1), len(text2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if text1[i - 1] == text2[j - 1]:
                dp[i][j] = 1 + dp[i - 1][j - 1]
            else:
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
    return dp[m][n]

print(longest_common_subsequence("abcde", "ace"))
```

## Complexity Analysis

- **Time Complexity**: `O(M * N)`, where `M` and `N` are the lengths of the two strings.
- **Space Complexity**: `O(M * N)` for the 2D DP grid (can be optimized to `O(min(M, N))` using two rolling rows).

## Summary

Longest Common Subsequence aligns two strings using 2D DP. Matching characters add 1 to diagonal results, while mismatches inherit the maximum of top and left subproblems.
