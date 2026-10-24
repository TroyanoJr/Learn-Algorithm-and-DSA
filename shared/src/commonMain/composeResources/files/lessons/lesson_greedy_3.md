# Activity Selection

The **activity selection problem** asks us to choose the largest possible number of non-overlapping activities that share one resource. Each activity has a start time and a finish time. This lesson treats intervals as half-open: an activity finishing at time `t` is compatible with another starting at `t`.

## Earliest-Finish-Time Rule

Sort activities by finish time. Select the first activity, then repeatedly select the next activity whose start time is at or after the finish time of the last selected activity. For this problem, choosing the compatible activity that finishes earliest is safe: it leaves at least as much time for every later activity as any alternative first choice.

Consider these activities, listed in finish-time order:

- A: (1, 4)
- B: (3, 5)
- C: (0, 6)
- D: (5, 7)
- E: (3, 9)
- F: (5, 9)
- G: (8, 11)
- H: (8, 12)
- I: (2, 14)
- J: (12, 16)

## Guided Trace

- Select A (1, 4), the earliest finisher; set `lastFinish = 4`.
- B and C start before 4, so skip them.
- Select D (5, 7), because 5 >= 4; set `lastFinish = 7`.
- Skip E and F because each starts before 7.
- Select G (8, 11), because 8 >= 7; set `lastFinish = 11`.
- Skip H and I because each starts before 11.
- Select J (12, 16), because 12 >= 11.

The selected activities are A, D, G, and J. Their intervals do not overlap under the half-open convention.

### Code Example

Each version sorts by finish time and scans once. It prints the selected activity names `A D G J`.

```pseudocode
activities = [("A", 1, 4), ("B", 3, 5), ("C", 0, 6),
              ("D", 5, 7), ("E", 3, 9), ("F", 5, 9),
              ("G", 8, 11), ("H", 8, 12), ("I", 2, 14), ("J", 12, 16)]
sort activities by finish time
selected = []
lastFinish = negative infinity
for activity in activities:
    if activity.start >= lastFinish:
        append activity to selected
        lastFinish = activity.finish
print names in selected
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>

struct Activity { std::string name; int start; int finish; };

int main() {
    std::vector<Activity> activities{{"A",1,4},{"B",3,5},{"C",0,6},{"D",5,7},{"E",3,9},
        {"F",5,9},{"G",8,11},{"H",8,12},{"I",2,14},{"J",12,16}};
    std::sort(activities.begin(), activities.end(), [](const Activity& a, const Activity& b) {
        return a.finish < b.finish;
    });
    int lastFinish = -1;
    for (const auto& activity : activities) {
        if (activity.start >= lastFinish) {
            std::cout << activity.name << ' ';
            lastFinish = activity.finish;
        }
    }
    std::cout << '\n';
}
```
```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ActivitySelection {
    static class Activity {
        String name; int start; int finish;
        Activity(String name, int start, int finish) {
            this.name = name; this.start = start; this.finish = finish;
        }
    }
    public static void main(String[] args) {
        List<Activity> activities = new ArrayList<>(List.of(
            new Activity("A",1,4), new Activity("B",3,5), new Activity("C",0,6),
            new Activity("D",5,7), new Activity("E",3,9), new Activity("F",5,9),
            new Activity("G",8,11), new Activity("H",8,12), new Activity("I",2,14),
            new Activity("J",12,16)));
        activities.sort(Comparator.comparingInt(a -> a.finish));
        int lastFinish = -1;
        for (Activity activity : activities) {
            if (activity.start >= lastFinish) {
                System.out.print(activity.name + " ");
                lastFinish = activity.finish;
            }
        }
        System.out.println();
    }
}
```
```python
activities = [("A", 1, 4), ("B", 3, 5), ("C", 0, 6), ("D", 5, 7),
              ("E", 3, 9), ("F", 5, 9), ("G", 8, 11), ("H", 8, 12),
              ("I", 2, 14), ("J", 12, 16)]
activities.sort(key=lambda activity: activity[2])
last_finish = -1
selected = []
for activity in activities:
    name, start, finish = activity
    if start >= last_finish:
        selected.append(name)
        last_finish = finish
print(selected)
```

## Complexity and Assumptions

Sorting takes `O(n log n)` time and the scan takes `O(n)`, so the total is `O(n log n)`. If activities arrive already sorted by finish time, selection takes `O(n)`. The algorithm maximizes the number of activities for one shared resource with fixed start and finish times. It does not maximize total duration or value; those are different objectives.

## Summary

The earliest-finish-time rule leaves the most room for future compatible activities. Sorting makes the greedy scan efficient, and the rule is justified for maximizing the number of non-overlapping activities.
