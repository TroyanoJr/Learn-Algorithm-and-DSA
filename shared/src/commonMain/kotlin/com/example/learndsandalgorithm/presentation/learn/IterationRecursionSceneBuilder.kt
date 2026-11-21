package com.example.learndsandalgorithm.presentation.learn

internal enum class IterationRecursionLesson {
    ITERATION_FUNDAMENTALS,
    NESTED_ITERATION,
    RECURSION_FUNDAMENTALS,
    RECURSION_TRADEOFFS
}

internal data class IterationRecursionTeachingMeta(
    val lesson: IterationRecursionLesson,
    val action: String,
    val badgeText: String,
    val explanation: String,
    val isTerminal: Boolean,
    val limit: Int = 0,
    val i: Int = 0,
    val total: Int = 0,
    val passNumber: Int = 0,
    val conditionSatisfied: Boolean = true,
    val outerLimit: Int = 0,
    val innerLimit: Int = 0,
    val j: Int = 0,
    val printedPairs: List<Pair<Int, Int>> = emptyList(),
    val inputN: Int = 0,
    val currentN: Int? = null,
    val callStack: List<Int> = emptyList(),
    val returnValues: List<Pair<Int, Int>> = emptyList(),
    val currentReturnValue: Int? = null,
    val returningFrameN: Int? = null,
    val receivingCallerN: Int? = null,
    val iterI: Int = 0,
    val iterTotal: Int = 0,
    val recurCallStack: List<Int> = emptyList(),
    val recurReturnValue: Int? = null
)

internal data class IterationRecursionSceneFrame(
    val scene: VisualizationScene,
    val meta: IterationRecursionTeachingMeta
)

internal object IterationRecursionSceneBuilder {
    fun iteration(steps: List<IterationFundamentalsStep>): List<IterationRecursionSceneFrame> =
        steps.mapIndexed { index, step ->
            val meta = IterationRecursionTeachingMeta(
                lesson = IterationRecursionLesson.ITERATION_FUNDAMENTALS,
                action = step.action.name,
                badgeText = step.badgeText,
                explanation = step.explanation,
                isTerminal = step.isTerminal,
                limit = step.limit,
                i = step.i,
                total = step.total,
                passNumber = step.passNumber,
                conditionSatisfied = step.conditionSatisfied
            )
            frame(index, meta, listOf(progressScene(step)))
        }

    fun nested(steps: List<NestedIterationStep>): List<IterationRecursionSceneFrame> =
        steps.mapIndexed { index, step ->
            val meta = IterationRecursionTeachingMeta(
                lesson = IterationRecursionLesson.NESTED_ITERATION,
                action = step.action.name,
                badgeText = step.badgeText,
                explanation = step.explanation,
                isTerminal = step.isTerminal,
                i = step.i,
                j = step.j,
                outerLimit = step.outerLimit,
                innerLimit = step.innerLimit,
                printedPairs = step.printedPairs,
                total = step.totalCount
            )
            frame(index, meta, listOf(matrixScene(step)))
        }

    fun recursion(steps: List<RecursionFundamentalsStep>): List<IterationRecursionSceneFrame> =
        steps.mapIndexed { index, step ->
            val meta = IterationRecursionTeachingMeta(
                lesson = IterationRecursionLesson.RECURSION_FUNDAMENTALS,
                action = step.action.name,
                badgeText = step.badgeText,
                explanation = step.explanation,
                isTerminal = step.isTerminal,
                inputN = step.inputN,
                currentN = step.currentN,
                callStack = step.callStack,
                returnValues = step.returnValues,
                currentReturnValue = step.currentReturnValue,
                returningFrameN = step.currentN.takeIf { step.action == RecursionAction.RETURN_ASCENT },
                receivingCallerN = step.callStack.dropLast(1).lastOrNull()
            )
            frame(
                index,
                meta,
                recursionSubScenes(
                    step.callStack,
                    step.returnValues,
                    step.currentN,
                    step.currentReturnValue,
                    step.callStack.dropLast(1).lastOrNull(),
                    step.action == RecursionAction.RETURN_ASCENT,
                    step.action == RecursionAction.BASE_CASE_REACHED
                )
            )
        }

    fun tradeoffs(steps: List<RecursionTradeoffsStep>): List<IterationRecursionSceneFrame> =
        steps.mapIndexed { index, step ->
            val meta = IterationRecursionTeachingMeta(
                lesson = IterationRecursionLesson.RECURSION_TRADEOFFS,
                action = step.action.name,
                badgeText = step.badgeText,
                explanation = step.explanation,
                isTerminal = step.isTerminal,
                inputN = step.inputN,
                iterI = step.iterI,
                iterTotal = step.iterTotal,
                recurCallStack = step.recurCallStack,
                recurReturnValue = step.recurReturnVal,
                currentN = step.recurCallStack.lastOrNull(),
                returningFrameN = step.recurCallStack.lastOrNull().takeIf { step.action == TradeoffAction.RECURSIVE_ASCENT },
                receivingCallerN = step.recurCallStack.dropLast(1).lastOrNull()
            )
            frame(index, meta, listOf(iterationMemoryScene(step), recursiveMemoryScene(step)))
        }

    private fun progressScene(step: IterationFundamentalsStep) = StructureSubScene(
        subPaneId = "iteration_progress",
        type = StructureType.ARRAY,
        entities = (1..step.limit).map { pass ->
            val completed = step.action == IterationAction.TERMINATED || step.passNumber > pass ||
                (step.passNumber == pass && step.action == IterationAction.UPDATE_STATE)
            val current = step.passNumber == pass && step.action != IterationAction.INIT && step.action != IterationAction.TERMINATED
            VisualEntity(
                visualKey = "iteration:pass:$pass",
                elementId = "iteration:pass:$pass",
                dataValue = "+$pass",
                slotKey = "pass:$pass",
                visualState = when {
                    completed -> EntityVisualState.VISITED
                    current -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }
            )
        }
    )

    private fun matrixScene(step: NestedIterationStep) = StructureSubScene(
        subPaneId = "nested_matrix",
        type = StructureType.TABLE,
        entities = (1..step.outerLimit).flatMap { row ->
            (1..step.innerLimit).map { column ->
                val pair = row to column
                VisualEntity(
                    visualKey = "nested:cell:$row:$column",
                    elementId = "nested:pair:$row:$column",
                    dataValue = "($row, $column)",
                    slotKey = "matrix:$row:$column",
                    visualState = when {
                        step.action == NestedIterationAction.INNER_PASS && step.i == row && step.j == column -> EntityVisualState.ACTIVE
                        pair in step.printedPairs -> EntityVisualState.VISITED
                        else -> EntityVisualState.DEFAULT
                    }
                )
            }
        }
    )

    private fun recursionSubScenes(
        stack: List<Int>,
        returns: List<Pair<Int, Int>>,
        currentN: Int?,
        currentReturnValue: Int?,
        receivingCallerN: Int?,
        returning: Boolean,
        baseCase: Boolean
    ): List<StructureSubScene> {
        val frames = stack.mapIndexed { index, n ->
            VisualEntity(
                visualKey = "recursion:frame:$n",
                elementId = "recursion:frame:$n",
                dataValue = "sumTo($n)",
                slotKey = "depth:$index",
                badgeLabel = when {
                    baseCase && n == 0 -> "BASE CASE"
                    returning && n == currentN -> buildString {
                        append("RETURNING")
                        currentReturnValue?.let { append(" $it") }
                        append(receivingCallerN?.let { " → sumTo($it)" } ?: " → RESULT")
                    }
                    index == stack.lastIndex -> "ACTIVE"
                    else -> "WAITING"
                },
                visualState = when {
                    baseCase && n == 0 -> EntityVisualState.TARGET
                    returning && n == currentN -> EntityVisualState.TARGET
                    index == stack.lastIndex -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }
            )
        }
        val connections = frames.zipWithNext().map { (from, to) ->
            VisualConnection(
                connectionKey = "recursion:call:${from.elementId}->${to.elementId}",
                fromVisualKey = from.visualKey,
                toVisualKey = to.visualKey,
                label = "calls",
                type = ConnectionType.POINTER_LINK,
                directed = true
            )
        }
        val history = returns.map { (n, value) ->
            VisualEntity(
                visualKey = "recursion:return:$n",
                elementId = "recursion:return:$n",
                dataValue = value.toString(),
                slotKey = "return:$n",
                badgeLabel = "sumTo($n)"
            )
        }
        return listOf(
            StructureSubScene("recursive_calls", StructureType.STACK, entities = frames, connections = connections),
            StructureSubScene("return_history", StructureType.ARRAY, entities = history)
        )
    }

    private fun iterationMemoryScene(step: RecursionTradeoffsStep) = StructureSubScene(
        subPaneId = "iteration_memory",
        type = StructureType.ARRAY,
        entities = (1..step.inputN).map { value ->
            VisualEntity(
                visualKey = "tradeoff:iteration:$value",
                elementId = "tradeoff:iteration:$value",
                dataValue = value.toString(),
                slotKey = "iteration:$value",
                visualState = when {
                    step.action == TradeoffAction.ITERATIVE_LOOP && value == step.iterI -> EntityVisualState.ACTIVE
                    value <= step.iterI && step.iterTotal > 0 -> EntityVisualState.VISITED
                    else -> EntityVisualState.DEFAULT
                }
            )
        }
    )

    private fun recursiveMemoryScene(step: RecursionTradeoffsStep) = StructureSubScene(
        subPaneId = "recursion_memory",
        type = StructureType.STACK,
        entities = step.recurCallStack.mapIndexed { index, n ->
            VisualEntity(
                visualKey = "tradeoff:frame:$n",
                elementId = "tradeoff:frame:$n",
                dataValue = "sumTo($n)",
                slotKey = "stack:$index",
                badgeLabel = when {
                    step.action == TradeoffAction.RECURSIVE_BASE && index == step.recurCallStack.lastIndex -> "BASE CASE · RETURNS 0"
                    step.action == TradeoffAction.RECURSIVE_ASCENT && index == step.recurCallStack.lastIndex -> buildString {
                        append("RETURNING ${step.recurReturnVal}")
                        append(step.recurCallStack.dropLast(1).lastOrNull()?.let { " → sumTo($it)" } ?: " → RESULT")
                    }
                    index == step.recurCallStack.lastIndex -> "ACTIVE"
                    else -> "WAITING"
                },
                visualState = when {
                    (step.action == TradeoffAction.RECURSIVE_ASCENT || step.action == TradeoffAction.RECURSIVE_BASE) && index == step.recurCallStack.lastIndex -> EntityVisualState.TARGET
                    index == step.recurCallStack.lastIndex -> EntityVisualState.ACTIVE
                    else -> EntityVisualState.DEFAULT
                }
            )
        }
    )

    private fun frame(index: Int, meta: IterationRecursionTeachingMeta, subScenes: List<StructureSubScene>) =
        IterationRecursionSceneFrame(
            scene = VisualizationScene(
                stepIndex = index,
                sceneId = "${meta.lesson.name.lowercase()}_step_$index",
                subScenes = subScenes,
                globalMessage = meta.explanation
            ),
            meta = meta
        )
}
