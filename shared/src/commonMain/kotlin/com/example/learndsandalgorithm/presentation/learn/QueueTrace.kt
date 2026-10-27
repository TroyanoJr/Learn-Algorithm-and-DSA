package com.example.learndsandalgorithm.presentation.learn

internal enum class QueueDemonstration {
    FUNDAMENTALS,
    OPERATIONS,
    IMPLEMENTATIONS,
    CIRCULAR_BUFFER
}

internal enum class QueueRepresentation {
    CIRCULAR_ARRAY,
    ARRAY_AND_LINKED
}

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
    val explanation: String,
    val representation: QueueRepresentation = QueueRepresentation.CIRCULAR_ARRAY
)

internal object QueueTrace {
    const val defaultCapacity = 5
    const val lessonCircularCapacity = 4

    private val fundamentalsOperations = listOf(
        QueueOperation.Enqueue("A"), QueueOperation.Enqueue("B"), QueueOperation.Enqueue("C"),
        QueueOperation.Enqueue("D"), QueueOperation.Enqueue("E"),
        QueueOperation.Dequeue, QueueOperation.Enqueue("F"), QueueOperation.Dequeue
    )
    private val operationsOperations = listOf(
        QueueOperation.Enqueue("7"), QueueOperation.Enqueue("4"), QueueOperation.Peek,
        QueueOperation.Dequeue, QueueOperation.Dequeue
    )
    private val implementationOperations = listOf(
        QueueOperation.Enqueue("8"), QueueOperation.Enqueue("5"), QueueOperation.Enqueue("6"),
        QueueOperation.Dequeue, QueueOperation.Enqueue("9"), QueueOperation.Peek
    )
    private val circularBufferOperations = listOf(
        QueueOperation.Enqueue("A"), QueueOperation.Enqueue("B"), QueueOperation.Enqueue("C"), QueueOperation.Enqueue("D"),
        QueueOperation.Dequeue, QueueOperation.Dequeue, QueueOperation.Enqueue("E"), QueueOperation.Enqueue("F")
    )

    fun stepsFor(demonstration: QueueDemonstration): List<QueueTraceStep> = when (demonstration) {
        QueueDemonstration.FUNDAMENTALS -> generate(
            operations = fundamentalsOperations,
            capacity = defaultCapacity,
            initialValues = emptyList()
        )
        QueueDemonstration.OPERATIONS -> generate(
            operations = operationsOperations,
            capacity = defaultCapacity,
            initialValues = emptyList()
        )
        QueueDemonstration.IMPLEMENTATIONS -> generate(
            operations = implementationOperations,
            capacity = defaultCapacity,
            initialValues = listOf("10", "20"),
            representation = QueueRepresentation.ARRAY_AND_LINKED
        )
        QueueDemonstration.CIRCULAR_BUFFER -> generate(
            operations = circularBufferOperations,
            capacity = lessonCircularCapacity
        )
    }

    fun generate(
        operations: List<QueueOperation>,
        capacity: Int,
        initialValues: List<String> = emptyList(),
        representation: QueueRepresentation = QueueRepresentation.CIRCULAR_ARRAY
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
                explanation = explanation,
                representation = representation
            )
        }

        return buildList {
            add(snapshot(
                event = QueueTraceEvent.START,
                explanation = if (size == 0) "Queue is empty. Enqueue at the rear; dequeue from the front." else
                    "Queue starts with $size item(s); front is ${slots[frontIndex]}."
            ))
            operations.forEach { operation ->
                when (operation) {
                    is QueueOperation.Enqueue -> {
                        if (size == capacity) {
                            add(snapshot(
                                event = QueueTraceEvent.OVERFLOW,
                                operationValue = operation.value,
                                highlightedSlot = (frontIndex + size) % capacity,
                                explanation = "Cannot enqueue ${operation.value}: the queue is full at capacity $capacity."
                            ))
                        } else {
                            val rearIndex = (frontIndex + size) % capacity
                            slots[rearIndex] = operation.value
                            size++
                            add(snapshot(
                                event = QueueTraceEvent.ENQUEUE,
                                operationValue = operation.value,
                                highlightedSlot = rearIndex,
                                explanation = "Enqueue ${operation.value} at the rear. Logical queue (front → rear): ${valuesText(List(size) { offset -> slots[(frontIndex + offset) % capacity]!! })}."
                            ))
                        }
                    }
                    QueueOperation.Dequeue -> {
                        if (size == 0) {
                            add(snapshot(
                                event = QueueTraceEvent.UNDERFLOW,
                                explanation = "Cannot dequeue: the queue is empty, so there is no front item."
                            ))
                        } else {
                            val removedIndex = frontIndex
                            val removedValue = slots[removedIndex]!!
                            slots[removedIndex] = null
                            frontIndex = (frontIndex + 1) % capacity
                            size--
                            add(snapshot(
                                event = QueueTraceEvent.DEQUEUE,
                                highlightedSlot = removedIndex,
                                returnedValue = removedValue,
                                explanation = if (size == 0) "Dequeue $removedValue from the front; the queue is now empty."
                                else "Dequeue $removedValue from the front; next front is ${slots[frontIndex]}."
                            ))
                        }
                    }
                    QueueOperation.Peek -> {
                        if (size == 0) {
                            add(snapshot(
                                event = QueueTraceEvent.EMPTY_ACCESS,
                                explanation = "Cannot peek: the queue is empty, so there is no front item."
                            ))
                        } else {
                            add(snapshot(
                                event = QueueTraceEvent.PEEK,
                                highlightedSlot = frontIndex,
                                returnedValue = slots[frontIndex],
                                explanation = "Peek observes ${slots[frontIndex]} at the front without removing it."
                            ))
                        }
                    }
                }
            }
            add(snapshot(
                event = QueueTraceEvent.COMPLETE,
                isTerminal = true,
                explanation = if (size == 0) "Sequence complete. The queue is empty." else
                    "Sequence complete. Logical queue (front → rear): ${valuesText(List(size) { offset -> slots[(frontIndex + offset) % capacity]!! })}."
            ))
        }
    }

    private fun valuesText(values: List<String>): String = values.joinToString(prefix = "[", postfix = "]")
}
