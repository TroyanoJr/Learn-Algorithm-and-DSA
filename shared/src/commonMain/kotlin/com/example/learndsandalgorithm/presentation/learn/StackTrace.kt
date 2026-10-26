package com.example.learndsandalgorithm.presentation.learn

internal sealed class StackOperation {
    data class Push(val value: String) : StackOperation()
    object Pop : StackOperation()
    object Peek : StackOperation()
}

internal enum class StackTraceEvent {
    START,
    PUSH,
    POP,
    PEEK,
    UNDERFLOW,
    OVERFLOW,
    COMPLETE
}

internal data class StackTraceStep(
    val values: List<String>,
    val capacity: Int?,
    val event: StackTraceEvent,
    val highlightedIndex: Int?,
    val returnedValue: String?,
    val isTerminal: Boolean,
    val explanation: String
)

internal object StackTrace {
    val demonstrationOperations = listOf(
        StackOperation.Push("A"),
        StackOperation.Push("B"),
        StackOperation.Push("C"),
        StackOperation.Push("D"),
        StackOperation.Push("E"),
        StackOperation.Peek,
        StackOperation.Pop,
        StackOperation.Push("F"),
        StackOperation.Pop,
        StackOperation.Pop,
        StackOperation.Pop,
        StackOperation.Pop,
        StackOperation.Pop,
        StackOperation.Peek,
        StackOperation.Pop
    )

    val steps = generate(demonstrationOperations)

    fun generate(
        operations: List<StackOperation>,
        initialValues: List<String> = emptyList(),
        capacity: Int? = null
    ): List<StackTraceStep> {
        require(capacity == null || capacity > 0) { "Stack capacity must be positive." }
        require(capacity == null || initialValues.size <= capacity) { "Initial values exceed stack capacity." }

        val values = initialValues.toMutableList()
        return buildList {
            add(
                StackTraceStep(
                    values = values.toList(),
                    capacity = capacity,
                    event = StackTraceEvent.START,
                    highlightedIndex = values.lastIndex.takeIf { it >= 0 },
                    returnedValue = null,
                    isTerminal = false,
                    explanation = if (values.isEmpty()) "Stack is empty. Push and pop both use the top." else
                        "Stack starts with ${values.size} item(s); the top is ${values.last()}."
                )
            )

            operations.forEach { operation ->
                when (operation) {
                    is StackOperation.Push -> {
                        if (capacity != null && values.size == capacity) {
                            add(
                                StackTraceStep(
                                    values = values.toList(), capacity = capacity,
                                    event = StackTraceEvent.OVERFLOW,
                                    highlightedIndex = null, returnedValue = null, isTerminal = false,
                                    explanation = "Cannot push ${operation.value}: the stack is full at capacity $capacity."
                                )
                            )
                        } else {
                            values.add(operation.value)
                            add(
                                StackTraceStep(
                                    values = values.toList(), capacity = capacity,
                                    event = StackTraceEvent.PUSH,
                                    highlightedIndex = values.lastIndex, returnedValue = null, isTerminal = false,
                                    explanation = "Push ${operation.value} onto the top."
                                )
                            )
                        }
                    }

                    StackOperation.Pop -> {
                        if (values.isEmpty()) {
                            add(
                                StackTraceStep(
                                    values = values.toList(), capacity = capacity,
                                    event = StackTraceEvent.UNDERFLOW,
                                    highlightedIndex = null, returnedValue = null, isTerminal = false,
                                    explanation = "Cannot pop: the stack is empty, so there is no top item."
                                )
                            )
                        } else {
                            val removed = values.removeAt(values.lastIndex)
                            add(
                                StackTraceStep(
                                    values = values.toList(), capacity = capacity,
                                    event = StackTraceEvent.POP,
                                    highlightedIndex = values.lastIndex.takeIf { it >= 0 },
                                    returnedValue = removed, isTerminal = false,
                                    explanation = "Pop $removed from the top; it was the most recently pushed item."
                                )
                            )
                        }
                    }

                    StackOperation.Peek -> {
                        if (values.isEmpty()) {
                            add(
                                StackTraceStep(
                                    values = values.toList(), capacity = capacity,
                                    event = StackTraceEvent.UNDERFLOW,
                                    highlightedIndex = null, returnedValue = null, isTerminal = false,
                                    explanation = "Cannot peek: the stack is empty, so there is no top item."
                                )
                            )
                        } else {
                            add(
                                StackTraceStep(
                                    values = values.toList(), capacity = capacity,
                                    event = StackTraceEvent.PEEK,
                                    highlightedIndex = values.lastIndex,
                                    returnedValue = values.last(), isTerminal = false,
                                    explanation = "Peek returns ${values.last()} from the top without removing it."
                                )
                            )
                        }
                    }
                }
            }

            add(
                StackTraceStep(
                    values = values.toList(), capacity = capacity,
                    event = StackTraceEvent.COMPLETE,
                    highlightedIndex = values.lastIndex.takeIf { it >= 0 },
                    returnedValue = null, isTerminal = true,
                    explanation = if (values.isEmpty()) "Sequence complete. The stack is empty." else
                        "Sequence complete. ${values.last()} remains at the top."
                )
            )
        }
    }
}
