package com.example.learndsandalgorithm.presentation.learn

internal enum class StackDemonstration {
    FUNDAMENTALS,
    OPERATIONS,
    IMPLEMENTATIONS,
    DELIMITER_MATCHING
}

internal enum class StackRepresentation {
    ABSTRACT,
    ARRAY_AND_LINKED
}

internal sealed class StackOperation {
    data class Push(val value: String) : StackOperation()
    object Pop : StackOperation()
    object Peek : StackOperation()
    data class OpenDelimiter(val value: String) : StackOperation()
    data class CloseDelimiter(val value: String) : StackOperation()
}

internal enum class StackTraceEvent {
    START,
    PUSH,
    POP,
    PEEK,
    UNDERFLOW,
    OVERFLOW,
    DELIMITER_MATCH,
    DELIMITER_MISMATCH,
    UNMATCHED_CLOSER,
    COMPLETE
}

internal data class StackTraceStep(
    val values: List<String>,
    val capacity: Int?,
    val event: StackTraceEvent,
    val highlightedIndex: Int?,
    val returnedValue: String?,
    val isTerminal: Boolean,
    val explanation: String,
    val representation: StackRepresentation = StackRepresentation.ABSTRACT,
    val operationValue: String? = null,
    val isValid: Boolean? = null
)

internal object StackTrace {
    private val fundamentalsOperations = listOf(
        StackOperation.Push("A"), StackOperation.Push("B"), StackOperation.Push("C"),
        StackOperation.Push("D"), StackOperation.Push("E"), StackOperation.Pop,
        StackOperation.Push("F"), StackOperation.Pop, StackOperation.Pop,
        StackOperation.Pop, StackOperation.Pop, StackOperation.Pop
    )

    private val operationsOperations = listOf(
        StackOperation.Push("7"), StackOperation.Push("4"), StackOperation.Peek,
        StackOperation.Pop, StackOperation.Pop
    )

    private val implementationOperations = listOf(
        StackOperation.Push("6"), StackOperation.Peek, StackOperation.Pop
    )

    private val delimiterOperations = listOf(
        StackOperation.OpenDelimiter("("), StackOperation.OpenDelimiter("["),
        StackOperation.OpenDelimiter("{"), StackOperation.CloseDelimiter("}"),
        StackOperation.CloseDelimiter("]"), StackOperation.CloseDelimiter(")")
    )

    fun stepsFor(demonstration: StackDemonstration): List<StackTraceStep> = when (demonstration) {
        StackDemonstration.FUNDAMENTALS -> generate(
            operations = fundamentalsOperations,
            initialValues = emptyList()
        )
        StackDemonstration.OPERATIONS -> generate(
            operations = operationsOperations,
            initialValues = emptyList()
        )
        StackDemonstration.IMPLEMENTATIONS -> generate(
            operations = implementationOperations,
            initialValues = listOf("10", "20", "30", "40", "50"),
            representation = StackRepresentation.ARRAY_AND_LINKED
        )
        StackDemonstration.DELIMITER_MATCHING -> generate(
            operations = delimiterOperations,
            explanationPrefix = "Delimiter validation: "
        )
    }

    fun generate(
        operations: List<StackOperation>,
        initialValues: List<String> = emptyList(),
        capacity: Int? = null,
        representation: StackRepresentation = StackRepresentation.ABSTRACT,
        explanationPrefix: String = ""
    ): List<StackTraceStep> {
        require(capacity == null || capacity > 0) { "Stack capacity must be positive." }
        require(capacity == null || initialValues.size <= capacity) { "Initial values exceed stack capacity." }

        val values = initialValues.toMutableList()
        var valid: Boolean? = null
        var delimiterFailure = false
        return buildList {
            fun snapshot(
                event: StackTraceEvent,
                highlightedIndex: Int? = values.lastIndex.takeIf { it >= 0 },
                returnedValue: String? = null,
                operationValue: String? = null,
                isTerminal: Boolean = false,
                explanation: String
            ) = StackTraceStep(
                values = values.toList(), capacity = capacity, event = event,
                highlightedIndex = highlightedIndex, returnedValue = returnedValue,
                isTerminal = isTerminal, explanation = explanationPrefix + explanation,
                representation = representation, operationValue = operationValue, isValid = valid
            )

            add(snapshot(
                StackTraceEvent.START,
                explanation = if (values.isEmpty()) "Stack is empty. The top is where push, pop, and peek operate."
                else "Stack starts with ${values.size} item(s); ${values.last()} is at the top."
            ))

            operations.forEach { operation ->
                if (delimiterFailure) return@forEach
                when (operation) {
                    is StackOperation.Push -> {
                        if (capacity != null && values.size == capacity) {
                            add(snapshot(StackTraceEvent.OVERFLOW, highlightedIndex = null, operationValue = operation.value,
                                explanation = "Cannot push ${operation.value}: the stack is full at capacity $capacity."))
                        } else {
                            values.add(operation.value)
                            add(snapshot(StackTraceEvent.PUSH, operationValue = operation.value,
                                explanation = "Push ${operation.value} onto the top; the newest item is now first to be popped."))
                        }
                    }
                    StackOperation.Pop -> {
                        if (values.isEmpty()) {
                            add(snapshot(StackTraceEvent.UNDERFLOW, highlightedIndex = null,
                                explanation = "Cannot pop: the stack is empty, so there is no top item."))
                        } else {
                            val removed = values.removeAt(values.lastIndex)
                            add(snapshot(StackTraceEvent.POP, returnedValue = removed,
                                explanation = "Pop $removed from the top; it was the most recently pushed item."))
                        }
                    }
                    StackOperation.Peek -> {
                        if (values.isEmpty()) {
                            add(snapshot(StackTraceEvent.UNDERFLOW, highlightedIndex = null,
                                explanation = "Cannot peek: the stack is empty, so there is no top item."))
                        } else {
                            add(snapshot(StackTraceEvent.PEEK, returnedValue = values.last(),
                                explanation = "Peek returns ${values.last()} from the top without removing it."))
                        }
                    }
                    is StackOperation.OpenDelimiter -> {
                        values.add(operation.value)
                        add(snapshot(StackTraceEvent.PUSH, operationValue = operation.value,
                            explanation = "Push opening delimiter ${operation.value}; it must be closed by a matching delimiter."))
                    }
                    is StackOperation.CloseDelimiter -> {
                        if (values.isEmpty()) {
                            valid = false
                            delimiterFailure = true
                            add(snapshot(StackTraceEvent.UNMATCHED_CLOSER, highlightedIndex = null, operationValue = operation.value,
                                explanation = "Closing delimiter ${operation.value} has no opener to match."))
                        } else {
                            val opener = values.removeAt(values.lastIndex)
                            val matches = (opener to operation.value) in setOf("(" to ")", "[" to "]", "{" to "}")
                            if (matches) {
                                add(snapshot(StackTraceEvent.DELIMITER_MATCH, returnedValue = opener, operationValue = operation.value,
                                    explanation = "${operation.value} matches the most recent opener $opener; pop that opener."))
                            } else {
                                valid = false
                                delimiterFailure = true
                                add(snapshot(StackTraceEvent.DELIMITER_MISMATCH, returnedValue = opener, operationValue = operation.value,
                                    explanation = "${operation.value} does not match the most recent opener $opener; validation fails."))
                            }
                        }
                    }
                }
            }

            if (valid == null) valid = values.isEmpty().takeIf { explanationPrefix.isNotEmpty() }
            val completionText = when (valid) {
                true -> "Delimiter sequence is balanced: every closer matched the latest opener and no opener remains."
                false -> if (delimiterFailure) "Delimiter sequence is invalid." else "Delimiter sequence is invalid: an opener remains unmatched."
                null -> if (values.isEmpty()) "Sequence complete. The stack is empty." else
                    "Sequence complete. ${values.last()} remains at the top."
            }
            add(snapshot(StackTraceEvent.COMPLETE, isTerminal = true, explanation = completionText))
        }
    }
}
