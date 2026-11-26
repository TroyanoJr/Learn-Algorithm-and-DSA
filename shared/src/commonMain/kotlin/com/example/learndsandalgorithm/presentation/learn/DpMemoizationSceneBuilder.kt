package com.example.learndsandalgorithm.presentation.learn

internal data class MemoCallOccurrence(val id: String, val argument: Int, val depth: Int)
internal data class MemoSceneFrame(val step: DpMemoizationStep, val calls: List<MemoCallOccurrence>)
internal data class MemoScenePresentation(val frames: List<MemoSceneFrame>, val maxDepth: Int, val tableSize: Int)

internal object DpMemoizationSceneBuilder {
    fun build(steps: List<DpMemoizationStep>): MemoScenePresentation {
        val stack = mutableListOf<MemoCallOccurrence>()
        var nextId = 0
        val frames = steps.map { step ->
            val prefix = stack.map { it.argument }.zip(step.callStack).takeWhile { (a, b) -> a == b }.size
            while (stack.size > prefix) stack.removeAt(stack.lastIndex)
            step.callStack.drop(prefix).forEach { n -> stack.add(MemoCallOccurrence("call-${nextId++}", n, stack.size)) }
            MemoSceneFrame(step, stack.toList())
        }
        return MemoScenePresentation(frames, frames.maxOfOrNull { it.calls.size } ?: 0,
            steps.maxOfOrNull { it.memoArray.size } ?: 0)
    }
}
