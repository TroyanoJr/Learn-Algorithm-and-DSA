package com.example.learndsandalgorithm.presentation.learn

internal data class Activity(
    val name: String,
    val start: Int,
    val finish: Int
)

internal enum class ActivitySelectionStatus {
    INIT,
    SORTED,
    CHECKING,
    SELECTED,
    REJECTED,
    FINISHED
}

internal data class ActivitySelectionStep(
    val allActivities: List<Activity> = emptyList(),
    val sortedActivities: List<Activity> = emptyList(),
    val lastFinish: Int = -1,
    val currentActivityIndex: Int? = null,
    val currentActivity: Activity? = null,
    val selectedActivities: List<Activity> = emptyList(),
    val rejectedActivities: List<Activity> = emptyList(),
    val status: ActivitySelectionStatus = ActivitySelectionStatus.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object ActivitySelectionTrace {

    val defaultActivities = listOf(
        Activity("A", 1, 4),
        Activity("B", 3, 5),
        Activity("C", 0, 6),
        Activity("D", 5, 7),
        Activity("E", 3, 9),
        Activity("F", 5, 9),
        Activity("G", 8, 11),
        Activity("H", 8, 12),
        Activity("I", 2, 14),
        Activity("J", 12, 16)
    )

    fun createTrace(activities: List<Activity> = defaultActivities): List<ActivitySelectionStep> {
        val steps = mutableListOf<ActivitySelectionStep>()
        val sorted = activities.sortedBy { it.finish }
        val selected = mutableListOf<Activity>()
        val rejected = mutableListOf<Activity>()
        var lastFinish = -1

        // Step 0: INIT
        steps.add(
            ActivitySelectionStep(
                allActivities = activities,
                sortedActivities = sorted,
                lastFinish = lastFinish,
                status = ActivitySelectionStatus.INIT,
                explanation = "Activity Selection: Given ${activities.size} activities. Goal: select maximum number of non-overlapping activities.",
                badgeText = "READY · ${activities.size} ACTIVITIES"
            )
        )

        // Step 1: SORTED
        steps.add(
            ActivitySelectionStep(
                allActivities = activities,
                sortedActivities = sorted,
                lastFinish = lastFinish,
                status = ActivitySelectionStatus.SORTED,
                explanation = "Sort activities by finish time ascending: ${sorted.map { "${it.name}(${it.start},${it.finish})" }}. Earliest finish leaves maximum space for future activities.",
                badgeText = "SORTED BY FINISH TIME"
            )
        )

        for ((idx, act) in sorted.withIndex()) {
            val isCompatible = act.start >= lastFinish

            // CHECKING
            steps.add(
                ActivitySelectionStep(
                    allActivities = activities,
                    sortedActivities = sorted,
                    lastFinish = lastFinish,
                    currentActivityIndex = idx,
                    currentActivity = act,
                    selectedActivities = selected.toList(),
                    rejectedActivities = rejected.toList(),
                    status = ActivitySelectionStatus.CHECKING,
                    explanation = "Check activity ${act.name} (${act.start}, ${act.finish}). Start time ${act.start} vs last finish $lastFinish.",
                    badgeText = "CHECK ${act.name} (${act.start}..${act.finish}) VS LAST FINISH $lastFinish"
                )
            )

            if (isCompatible) {
                selected.add(act)
                val prevFinish = lastFinish
                lastFinish = act.finish

                // SELECTED
                steps.add(
                    ActivitySelectionStep(
                        allActivities = activities,
                        sortedActivities = sorted,
                        lastFinish = lastFinish,
                        currentActivityIndex = idx,
                        currentActivity = act,
                        selectedActivities = selected.toList(),
                        rejectedActivities = rejected.toList(),
                        status = ActivitySelectionStatus.SELECTED,
                        explanation = "Select activity ${act.name} (${act.start}, ${act.finish}). Compatible because start ${act.start} ≥ prev finish $prevFinish. Updated lastFinish = ${act.finish}.",
                        badgeText = "SELECTED ${act.name} · LAST FINISH = $lastFinish"
                    )
                )
            } else {
                rejected.add(act)

                // REJECTED
                steps.add(
                    ActivitySelectionStep(
                        allActivities = activities,
                        sortedActivities = sorted,
                        lastFinish = lastFinish,
                        currentActivityIndex = idx,
                        currentActivity = act,
                        selectedActivities = selected.toList(),
                        rejectedActivities = rejected.toList(),
                        status = ActivitySelectionStatus.REJECTED,
                        explanation = "Reject activity ${act.name} (${act.start}, ${act.finish}). Overlaps with previously selected activity (start ${act.start} < last finish $lastFinish).",
                        badgeText = "REJECTED ${act.name} (OVERLAPS)"
                    )
                )
            }
        }

        // FINISHED
        steps.add(
            ActivitySelectionStep(
                allActivities = activities,
                sortedActivities = sorted,
                lastFinish = lastFinish,
                selectedActivities = selected.toList(),
                rejectedActivities = rejected.toList(),
                status = ActivitySelectionStatus.FINISHED,
                explanation = "Greedy activity selection complete! Selected ${selected.size} non-overlapping activities: ${selected.map { it.name }}. Earliest-finish rule is globally optimal. Complexity: O(N log N).",
                badgeText = "COMPLETE: ${selected.size} ACTIVITIES SELECTED",
                isTerminal = true
            )
        )

        return steps
    }
}
