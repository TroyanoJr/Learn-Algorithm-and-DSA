package com.example.learndsandalgorithm.presentation.learn

internal sealed class QueueOperation {
    data class Enqueue(val value: String) : QueueOperation()
    object Dequeue : QueueOperation()
    object Peek : QueueOperation()
}

internal enum class QueueTraceEvent {
    START,
    ENQUEUE,
    DEQUEUE,
    PEEK,
    UNDERFLOW,
    EMPTY_ACCESS,
    OVERFLOW,
    COMPLETE
}

internal data class QueueTraceStep(
    val values: List<String>,
    val slots: List<String?>,
    val capacity: Int,
    val frontIndex: Int,
    val nextRearIndex: Int,
    val size: Int,
    val event: QueueTraceEvent,
    val operationValue: String?,
    val highlightedSlot: Int?,
    val returnedValue: String?,
    val isTerminal: Boolean,
    val explanation: String
)

internal object QueueTrace {
    const val capacity = 4

    val circularBufferOperations = listOf(
        QueueOperation.Enqueue("A"),
        QueueOperation.Enqueue("B"),
        QueueOperation.Enqueue("C"),
        QueueOperation.Peek,
        QueueOperation.Dequeue,
        QueueOperation.Dequeue,
        QueueOperation.Enqueue("D"),
        QueueOperation.Enqueue("E"),
        QueueOperation.Peek,
        QueueOperation.Dequeue,
        QueueOperation.Dequeue,
        QueueOperation.Dequeue,
        QueueOperation.Dequeue,
        QueueOperation.Peek
    )

    val steps = generate(circularBufferOperations, capacity)

    fun generate(
        operations: List<QueueOperation>,
        capacity: Int,
        initialValues: List<String> = emptyList()
    ): List<QueueTraceStep> {
        require(capacity > 0) { "Queue capacity must be positive." }
        require(initialValues.size <= capacity) { "Initial values exceed queue capacity." }

        val slots = MutableList<String?>(capacity) { null }
        initialValues.forEachIndexed { index, value -> slots[index] = value }
        var frontIndex = 0
        var size = initialValues.size

        fun snapshot(
            event: QueueTraceEvent,
            operationValue: String? = null,
            highlightedSlot: Int? = null,
            returnedValue: String? = null,
            isTerminal: Boolean = false,
            explanation: String
        ): QueueTraceStep {
            val values = List(size) { offset -> slots[(frontIndex + offset) % capacity]!! }
            return QueueTraceStep(
                values = values,
                slots = slots.toList(),
                capacity = capacity,
                frontIndex = frontIndex,
                nextRearIndex = (frontIndex + size) % capacity,
                size = size,
                event = event,
                operationValue = operationValue,
                highlightedSlot = highlightedSlot,
                returnedValue = returnedValue,
                isTerminal = isTerminal,
                explanation = explanation
            )
        }

        return buildList {
            add(
                snapshot(
                    event = QueueTraceEvent.START,
                    explanation = if (size == 0) "Queue is empty. Enqueue at the rear; dequeue from the front." else
                        "Queue starts with $size item(s); front is ${slots[frontIndex]}."
                )
            )

            operations.forEach { operation ->
                when (operation) {
                    is QueueOperation.Enqueue -> {
                        if (size == capacity) {
                            add(
                                snapshot(
                                    event = QueueTraceEvent.OVERFLOW,
                                    operationValue = operation.value,
                                    highlightedSlot = (frontIndex + size) % capacity,
                                    explanation = "Cannot enqueue ${operation.value}: the queue is full at capacity $capacity."
                                )
                            )
                        } else {
                            val rearIndex = (frontIndex + size) % capacity
                            slots[rearIndex] = operation.value
                            size++
                            add(
                                snapshot(
                                    event = QueueTraceEvent.ENQUEUE,
                                    operationValue = operation.value,
                                    highlightedSlot = rearIndex,
                                    explanation = "Enqueue ${operation.value} at rear slot $rearIndex. " +
                                        "Logical queue (front → rear): ${valuesText(List(size) { offset -> slots[(frontIndex + offset) % capacity]!! })}."
                                )
                            )
                        }
                    }

                    QueueOperation.Dequeue -> {
                        if (size == 0) {
                            add(
                                snapshot(
                                    event = QueueTraceEvent.UNDERFLOW,
                                    explanation = "Cannot dequeue: the queue is empty, so there is no front item."
                                )
                            )
                        } else {
                            val removedIndex = frontIndex
                            val removedValue = slots[removedIndex]!!
                            slots[removedIndex] = null
                            frontIndex = (frontIndex + 1) % capacity
                            size--
                            add(
                                snapshot(
                                    event = QueueTraceEvent.DEQUEUE,
                                    highlightedSlot = removedIndex,
                                    returnedValue = removedValue,
                                    explanation = if (size == 0) {
                                        "Dequeue $removedValue from front slot $removedIndex; the queue is now empty."
                                    } else {
                                        "Dequeue $removedValue from front slot $removedIndex; next front is ${slots[frontIndex]}."
                                    }
                                )
                            )
                        }
                    }

                    QueueOperation.Peek -> {
                        if (size == 0) {
                            add(
                                snapshot(
                                    event = QueueTraceEvent.EMPTY_ACCESS,
                                    explanation = "Cannot peek: the queue is empty, so there is no front item."
                                )
                            )
                        } else {
                            add(
                                snapshot(
                                    event = QueueTraceEvent.PEEK,
                                    highlightedSlot = frontIndex,
                                    returnedValue = slots[frontIndex],
                                    explanation = "Peek observes ${slots[frontIndex]} at the front without removing it."
                                )
                            )
                        }
                    }
                }
            }

            add(
                snapshot(
                    event = QueueTraceEvent.COMPLETE,
                    isTerminal = true,
                    explanation = if (size == 0) "Sequence complete. The queue is empty." else
                        "Sequence complete. Logical queue (front → rear): ${valuesText(List(size) { offset -> slots[(frontIndex + offset) % capacity]!! })}."
                )
            )
        }
    }

    private fun valuesText(values: List<String>): String = values.joinToString(prefix = "[", postfix = "]")
}
