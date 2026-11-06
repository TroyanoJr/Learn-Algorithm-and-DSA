package com.example.learndsandalgorithm.presentation.learn

internal enum class IterationAction {
    INIT,
    CHECK_CONDITION,
    EXECUTE_BODY,
    UPDATE_STATE,
    TERMINATED
}

internal data class IterationFundamentalsStep(
    val limit: Int = 4,
    val i: Int = 1,
    val total: Int = 0,
    val passNumber: Int = 0,
    val conditionSatisfied: Boolean = true,
    val action: IterationAction = IterationAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object IterationFundamentalsTrace {

    fun createTrace(limit: Int = 4): List<IterationFundamentalsStep> {
        val steps = mutableListOf<IterationFundamentalsStep>()
        var total = 0
        var i = 1
        var pass = 0

        fun snapshot(
            act: IterationAction,
            cond: Boolean,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = IterationFundamentalsStep(
            limit = limit,
            i = i,
            total = total,
            passNumber = pass,
            conditionSatisfied = cond,
            action = act,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                act = IterationAction.INIT,
                cond = true,
                explanation = "Iteration Fundamentals: Summing numbers from 1 to $limit. Initial state: total = 0, i = 1.",
                badgeText = "INITIAL STATE: total = 0, i = 1"
            )
        )

        while (i <= limit) {
            pass++

            // Check condition
            steps.add(
                snapshot(
                    act = IterationAction.CHECK_CONDITION,
                    cond = true,
                    explanation = "Pass $pass: Check condition (i = $i) <= $limit -> True. Enter loop body.",
                    badgeText = "PASS $pass: CHECK $i ≤ $limit (TRUE)"
                )
            )

            total += i

            // Execute body
            steps.add(
                snapshot(
                    act = IterationAction.EXECUTE_BODY,
                    cond = true,
                    explanation = "Pass $pass: Execute loop body. total = total + i -> ${total - i} + $i = $total.",
                    badgeText = "BODY: ADD $i -> total = $total"
                )
            )

            i++

            // Update state
            steps.add(
                snapshot(
                    act = IterationAction.UPDATE_STATE,
                    cond = true,
                    explanation = "Pass $pass: Update loop variable. i++ -> i = $i.",
                    badgeText = "UPDATE: i++ -> i = $i"
                )
            )
        }

        // Loop terminated condition check
        steps.add(
            snapshot(
                act = IterationAction.TERMINATED,
                cond = false,
                explanation = "Loop Termination: Check condition (i = $i) <= $limit -> False! Loop terminates. Final sum = $total.",
                badgeText = "TERMINATED: $i > $limit (total = $total)",
                isTerminal = true
            )
        )

        return steps
    }
}
