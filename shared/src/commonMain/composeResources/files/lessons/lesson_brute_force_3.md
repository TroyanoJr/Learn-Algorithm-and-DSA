# Naive String Pattern Matching

**Naive pattern matching** looks for a pattern by trying it at every alignment where it could fit in a text. At each alignment, compare the pattern characters with the text characters from left to right. A mismatch rules out that alignment; a full match records its starting index.

## Guided Trace

Let the text be `ABABA` and the pattern be `ABA`. The pattern has length 3, so it can start at indices 0, 1, or 2 in the text:

- Start at index 0: `A` matches `A`, `B` matches `B`, and `A` matches `A`. Record index 0.
- Start at index 1: the text character `B` does not match the pattern's first character `A`. Move to the next alignment.
- Start at index 2: `A`, `B`, and `A` all match. Record index 2.

The matches start at indices `[0, 2]`. Each new alignment advances the start by one; a failed comparison does not skip later possible starts.

### Code Example

All four versions return every starting index where `ABA` occurs in `ABABA`.

```pseudocode
function naiveMatch(text, pattern):
    matches = []
    for start from 0 to length(text) - length(pattern):
        matched = true
        for offset from 0 to length(pattern) - 1:
            if text[start + offset] != pattern[offset]:
                matched = false
                break
        if matched:
            append start to matches
    return matches

print(naiveMatch("ABABA", "ABA"))
```
```cpp
#include <iostream>
#include <string>
#include <vector>

std::vector<int> naiveMatch(const std::string& text, const std::string& pattern) {
    std::vector<int> matches;
    const int n = static_cast<int>(text.size());
    const int m = static_cast<int>(pattern.size());
    for (int start = 0; start <= n - m; ++start) {
        bool matched = true;
        for (int offset = 0; offset < m; ++offset) {
            if (text[start + offset] != pattern[offset]) {
                matched = false;
                break;
            }
        }
        if (matched) matches.push_back(start);
    }
    return matches;
}

int main() {
    const auto matches = naiveMatch("ABABA", "ABA");
    for (int index : matches) std::cout << index << ' ';
    std::cout << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.List;

class NaivePatternMatching {
    static List<Integer> naiveMatch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        for (int start = 0; start <= n - m; start++) {
            boolean matched = true;
            for (int offset = 0; offset < m; offset++) {
                if (text.charAt(start + offset) != pattern.charAt(offset)) {
                    matched = false;
                    break;
                }
            }
            if (matched) matches.add(start);
        }
        return matches;
    }

    public static void main(String[] args) {
        System.out.println(naiveMatch("ABABA", "ABA"));
    }
}
```
```python
def naive_match(text, pattern):
    matches = []
    for start in range(len(text) - len(pattern) + 1):
        matched = True
        for offset in range(len(pattern)):
            if text[start + offset] != pattern[offset]:
                matched = False
                break
        if matched:
            matches.append(start)
    return matches

print(naive_match("ABABA", "ABA"))
```

The examples use the convention that an empty pattern matches at every boundary in the text, producing indices from 0 through the text length. If the pattern is longer than the text, there are no alignments and the result is empty.

## Complexity

For text length `n` and pattern length `m`, there are at most `n - m + 1` alignments when `m <= n`. Each alignment may compare up to `m` characters, so worst-case time is `O((n - m + 1)m)`, commonly written `O(nm)`. This worst case occurs when many alignments match for a long prefix before failing, or when the whole pattern matches repeatedly. The extra working space is `O(1)`, excluding the list of result indices, which uses `O(r)` space for `r` matches.

## Summary

Naive matching is easy to understand because it checks every possible start position. It can repeat many character comparisons, which is why its worst-case running time grows with both the text and pattern lengths.
