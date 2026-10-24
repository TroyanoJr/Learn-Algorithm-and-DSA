package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.model.QuestionDifficulty
import com.example.learndsandalgorithm.domain.model.QuestionType
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import com.example.learndsandalgorithm.presentation.learn.shuffleQuestionOptions
import com.example.learndsandalgorithm.presentation.learn.prepareLessonQuestions
import com.example.learndsandalgorithm.presentation.practice.prepareQuizQuestions
import com.example.learndsandalgorithm.presentation.practice.shuffleQuizQuestionOrder
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContentRepositoryTest {

    private val repository: ContentRepository = ContentRepositoryImpl(
        readResourceText = { path ->
            val resourcePath = "composeResources/learndsandalgorithm.shared.generated.resources/$path"
            val classLoader = Thread.currentThread().contextClassLoader
            val stream = classLoader?.getResourceAsStream(resourcePath)
                ?: File("src/commonMain/composeResources/$path").takeIf { it.exists() }?.inputStream()
                ?: File("shared/src/commonMain/composeResources/$path").takeIf { it.exists() }?.inputStream()
                ?: throw IllegalArgumentException("Resource not found: $path")
            stream.bufferedReader().use { it.readText() }
        }
    )

    private val allLessonIds = listOf(
        "lesson_introduction_ds_1", "lesson_introduction_ds_2", "lesson_introduction_algo_1", "lesson_introduction_algo_2",
        "lesson_arrays_1", "lesson_arrays_2", "lesson_arrays_3", "lesson_arrays_4", "lesson_arrays_5",
        "searching_1", "searching_2", "searching_3", "searching_4",
        "sorting_1", "sorting_2", "sorting_3", "sorting_4", "sorting_5",
        "lesson_linked_list_1", "lesson_linked_list_2", "lesson_linked_list_3", "lesson_linked_list_4", "lesson_linked_list_5",
        "lesson_stack_1", "lesson_stack_2", "lesson_stack_3", "lesson_stack_4",
        "lesson_queues_1", "lesson_queues_2", "lesson_queues_3", "lesson_queues_4",
        "lesson_iteration_recursion_1", "lesson_iteration_recursion_2", "lesson_iteration_recursion_3", "lesson_iteration_recursion_4",
        "lesson_hash_tables_1", "lesson_hash_tables_2", "lesson_hash_tables_3", "lesson_hash_tables_4",
        "lesson_trees_1", "lesson_trees_2", "lesson_trees_3", "lesson_trees_4"
    )

    @Test
    fun testLoadAllTopics() = runBlocking {
        val topics = repository.getTopics()
        assertEquals(19, topics.size, "Should load exactly 19 topics")
    }

    @Test
    fun testLoadAllLessons() = runBlocking {
        val dsIntroLessons = repository.getLessonsByTopic("introduction_ds")
        val algoIntroLessons = repository.getLessonsByTopic("introduction_algo")
        val arraysLessons = repository.getLessonsByTopic("arrays")
        val searchLessons = repository.getLessonsByTopic("linear_search")
        val sortingLessons = repository.getLessonsByTopic("sorting")
        val linkedListLessons = repository.getLessonsByTopic("linked_list")
        val stackLessons = repository.getLessonsByTopic("stack")
        val queueLessons = repository.getLessonsByTopic("queues")
        val totalLessons = dsIntroLessons.size + algoIntroLessons.size + arraysLessons.size + searchLessons.size + sortingLessons.size + linkedListLessons.size + stackLessons.size + queueLessons.size
        val iterationLessons = repository.getLessonsByTopic("iteration_recursion")
        val hashTablesLessons = repository.getLessonsByTopic("hash_tables")
        val treesLessons = repository.getLessonsByTopic("trees")
        assertEquals(43, totalLessons + iterationLessons.size + hashTablesLessons.size + treesLessons.size, "Should load exactly 43 lessons total")
        assertEquals(2, dsIntroLessons.size, "DS Intro should have 2 lessons")
        assertEquals(2, algoIntroLessons.size, "Algo Intro should have 2 lessons")
        assertEquals(5, arraysLessons.size, "Arrays should have 5 lessons")
        assertEquals(4, searchLessons.size, "Searching should have 4 lessons")
        assertEquals(5, sortingLessons.size, "Sorting should have 5 lessons")
        assertEquals(5, linkedListLessons.size, "Linked List should have 5 lessons")
        assertEquals(4, stackLessons.size, "Stack should have 4 lessons")
        assertEquals(4, queueLessons.size, "Queues should have 4 lessons")
        assertEquals(4, iterationLessons.size, "Iteration & Recursion should have 4 lessons")
        assertEquals(4, hashTablesLessons.size, "Hash Tables should have 4 lessons")
        assertEquals(4, treesLessons.size, "Trees should have 4 lessons")
    }

    @Test
    fun testLoadLessonContent() = runBlocking {
        val lesson = repository.getLessonById("lesson_arrays_1")
        assertNotNull(lesson, "Lesson 'lesson_arrays_1' should exist")
        val content = repository.getLessonContent(lesson)
        assertTrue(content.isNotBlank(), "Lesson content should not be blank")
        assertTrue(content.contains("Introduction to Arrays"), "Content should contain lesson title heading")
    }

    @Test
    fun testDataStructureChoiceLessonAndQuestion() = runBlocking {
        val lesson = repository.getLessonById("lesson_introduction_ds_2")
        assertNotNull(lesson, "The data structure choice lesson should exist")
        assertEquals("introduction_ds", lesson.topicId)
        assertEquals("Choosing How to Organize Data", lesson.title)
        assertEquals(2, lesson.order)
        assertEquals(20, lesson.xp)
        assertEquals("files/lessons/lesson_introduction_ds_2.md", lesson.contentPath)

        val content = repository.getLessonContent(lesson)
        assertTrue(content.contains("Choosing How to Organize Data"), "The lesson resource should load")
        assertTrue(content.contains("first in, first out"), "The lesson should explain queue ordering")

        val questions = repository.getQuestionsByLessonId(lesson.id)
        assertEquals(3, questions.size, "The lesson should have three linked questions")
        val question = questions.first { it.id == "q_lesson_introduction_ds_2" }
        assertEquals("q_lesson_introduction_ds_2", question.id)
        assertEquals("introduction_ds", question.topicId)
        assertEquals(lesson.id, question.lessonId)
        assertEquals(4, question.options.size)
        assertEquals(1, question.correctOptionIndex)
        assertEquals(QuestionDifficulty.MEDIUM, question.difficulty)
        assertTrue(question.explanation.isNotBlank())
    }

    @Test
    fun testAlgorithmWorkGrowthLessonAndQuestion() = runBlocking {
        val lesson = repository.getLessonById("lesson_introduction_algo_2")
        assertNotNull(lesson, "The algorithm work growth lesson should exist")
        assertEquals("introduction_algo", lesson.topicId)
        assertEquals("How Work Grows", lesson.title)
        assertEquals(2, lesson.order)
        assertEquals(20, lesson.xp)
        assertEquals("files/lessons/lesson_introduction_algo_2.md", lesson.contentPath)

        val content = repository.getLessonContent(lesson)
        assertTrue(content.contains("How Work Grows"), "The lesson resource should load")
        assertTrue(content.contains("input size"), "The lesson should explain input size")
        assertTrue(content.contains("operation counts are a simplified way to reason about growth", ignoreCase = true))
        assertTrue(content.contains("not measurements of elapsed time", ignoreCase = true))

        val questions = repository.getQuestionsByLessonId(lesson.id)
        assertEquals(3, questions.size, "The lesson should have three linked questions")
        val question = questions.first { it.id == "q_lesson_introduction_algo_2" }
        assertEquals("q_lesson_introduction_algo_2", question.id)
        assertEquals("introduction_algo", question.topicId)
        assertEquals(lesson.id, question.lessonId)
        assertEquals(4, question.options.size)
        assertEquals(1, question.correctOptionIndex)
        assertEquals(QuestionDifficulty.MEDIUM, question.difficulty)
        assertTrue(question.explanation.isNotBlank())
    }

    @Test
    fun testLinkedListNodesAndHeadLessonAndQuestion() = runBlocking {
        val lesson = repository.getLessonById("lesson_linked_list_1")
        assertNotNull(lesson, "The linked list lesson 1 should exist")
        assertEquals("linked_list", lesson.topicId)
        assertEquals("Linked Lists: Nodes and the Head", lesson.title)
        assertEquals(1, lesson.order)
        assertEquals(30, lesson.xp)
        assertEquals("files/lessons/lesson_linked_list_1.md", lesson.contentPath)

        val content = repository.getLessonContent(lesson)
        assertTrue(content.contains("Linked Lists: Nodes and the Head"), "The lesson resource should load")
        assertTrue(content.contains("nullptr"), "The lesson should explain nullptr")

        val questions = repository.getQuestionsByLessonId(lesson.id)
        assertEquals(3, questions.size, "The lesson should have three linked questions")
        val question = questions.first { it.id == "q_lesson_linked_list_1" }
        assertEquals("q_lesson_linked_list_1", question.id)
        assertEquals("linked_list", question.topicId)
        assertEquals(lesson.id, question.lessonId)
        assertEquals(4, question.options.size)
        assertEquals(1, question.correctOptionIndex)
        assertEquals(QuestionDifficulty.EASY, question.difficulty)
        assertTrue(question.explanation.isNotBlank())
    }

    @Test
    fun testLinkedListTraversalSearchAccessLessonAndQuestion() = runBlocking {
        val lesson = repository.getLessonById("lesson_linked_list_2")
        assertNotNull(lesson, "The linked list lesson 2 should exist")
        assertEquals("linked_list", lesson.topicId)
        assertEquals("Traversal, Search, and Position Access", lesson.title)
        assertEquals(2, lesson.order)
        assertEquals(30, lesson.xp)
        assertEquals("files/lessons/lesson_linked_list_2.md", lesson.contentPath)

        val content = repository.getLessonContent(lesson)
        assertTrue(content.contains("Traversal, Search, and Position Access"), "The lesson resource should load")
        assertTrue(content.contains("O(n)"), "The lesson should explain time complexity")

        val questions = repository.getQuestionsByLessonId(lesson.id)
        assertEquals(3, questions.size, "The lesson should have three linked questions")
        val question = questions.first { it.id == "q_lesson_linked_list_2" }
        assertEquals("q_lesson_linked_list_2", question.id)
        assertEquals("linked_list", question.topicId)
        assertEquals(lesson.id, question.lessonId)
        assertEquals(4, question.options.size)
        assertEquals(0, question.correctOptionIndex)
        assertEquals(QuestionDifficulty.EASY, question.difficulty)
        assertTrue(question.explanation.isNotBlank())
    }

    @Test
    fun testLinkedListInsertingNodesLessonAndQuestion() = runBlocking {
        val lesson = repository.getLessonById("lesson_linked_list_3")
        assertNotNull(lesson, "The linked list lesson 3 should exist")
        assertEquals("linked_list", lesson.topicId)
        assertEquals("Inserting Nodes", lesson.title)
        assertEquals(3, lesson.order)
        assertEquals(30, lesson.xp)
        assertEquals("files/lessons/lesson_linked_list_3.md", lesson.contentPath)

        val content = repository.getLessonContent(lesson)
        assertTrue(content.contains("Inserting Nodes"), "The lesson resource should load")
        assertTrue(content.contains("newNode.next = prevNode.next"), "The lesson should explain safe link update order")

        val questions = repository.getQuestionsByLessonId(lesson.id)
        assertEquals(3, questions.size, "The lesson should have three linked questions")
        val question = questions.first { it.id == "q_lesson_linked_list_3" }
        assertEquals("q_lesson_linked_list_3", question.id)
        assertEquals("linked_list", question.topicId)
        assertEquals(lesson.id, question.lessonId)
        assertEquals(4, question.options.size)
        assertEquals(0, question.correctOptionIndex)
        assertEquals(QuestionDifficulty.EASY, question.difficulty)
        assertTrue(question.explanation.isNotBlank())
    }

    @Test
    fun testLinkedListRemovingNodesLessonAndQuestion() = runBlocking {
        val lesson = repository.getLessonById("lesson_linked_list_4")
        assertNotNull(lesson, "The linked list lesson 4 should exist")
        assertEquals("linked_list", lesson.topicId)
        assertEquals("Removing Nodes", lesson.title)
        assertEquals(4, lesson.order)
        assertEquals(30, lesson.xp)
        assertEquals("files/lessons/lesson_linked_list_4.md", lesson.contentPath)

        val content = repository.getLessonContent(lesson)
        assertTrue(content.contains("Removing Nodes"), "The lesson resource should load")
        assertTrue(content.contains("prev.next = targetNode.next"), "The lesson should explain unlinking via predecessor")

        val questions = repository.getQuestionsByLessonId(lesson.id)
        assertEquals(3, questions.size, "The lesson should have three linked questions")
        val question = questions.first { it.id == "q_lesson_linked_list_4" }
        assertEquals("q_lesson_linked_list_4", question.id)
        assertEquals("linked_list", question.topicId)
        assertEquals(lesson.id, question.lessonId)
        assertEquals(4, question.options.size)
        assertEquals(0, question.correctOptionIndex)
        assertEquals(QuestionDifficulty.EASY, question.difficulty)
        assertTrue(question.explanation.isNotBlank())
    }

    @Test
    fun testLinkedListPerformanceReviewLessonAndQuestion() = runBlocking {
        val lesson = repository.getLessonById("lesson_linked_list_5")
        assertNotNull(lesson, "The linked list lesson 5 should exist")
        assertEquals("linked_list", lesson.topicId)
        assertEquals("Performance, Singly vs. Doubly Linked Lists, and Review", lesson.title)
        assertEquals(5, lesson.order)
        assertEquals(30, lesson.xp)
        assertEquals("files/lessons/lesson_linked_list_5.md", lesson.contentPath)

        val content = repository.getLessonContent(lesson)
        assertTrue(content.contains("Performance, Singly vs. Doubly Linked Lists, and Review"), "The lesson resource should load")
        assertTrue(content.contains("Doubly Linked Lists"), "The lesson should explain doubly linked lists")

        val questions = repository.getQuestionsByLessonId(lesson.id)
        assertEquals(3, questions.size, "The lesson should have three linked questions")
        val question = questions.first { it.id == "q_lesson_linked_list_5" }
        assertEquals("q_lesson_linked_list_5", question.id)
        assertEquals("linked_list", question.topicId)
        assertEquals(lesson.id, question.lessonId)
        assertEquals(4, question.options.size)
        assertEquals(0, question.correctOptionIndex)
        assertEquals(QuestionDifficulty.EASY, question.difficulty)
        assertTrue(question.explanation.isNotBlank())
    }

    @Test
    fun testLinkedListTopicQuizQuestions() = runBlocking {
        val linkedListQuizQuestions = repository.getQuestionsByTopic("linked_list")
        assertEquals(5, linkedListQuizQuestions.size, "Linked List topic quiz should return 5 questions with lessonId == null")
        assertTrue(linkedListQuizQuestions.all { it.lessonId == null }, "All topic quiz questions must have lessonId == null")
        val uniqueIds = linkedListQuizQuestions.map { it.id }.toSet()
        assertEquals(5, uniqueIds.size, "All 5 topic quiz questions must have unique IDs")
    }

    @Test
    fun testLoadAllQuestions() = runBlocking {
        val arraysQuizQuestions = repository.getQuestionsByTopic("arrays")
        val searchQuizQuestions = repository.getQuestionsByTopic("linear_search")
        val sortingQuizQuestions = repository.getQuestionsByTopic("sorting")
        val linkedListQuizQuestions = repository.getQuestionsByTopic("linked_list")
        val stackQuizQuestions = repository.getQuestionsByTopic("stack")
        val queueQuizQuestions = repository.getQuestionsByTopic("queues")
        val iterationQuizQuestions = repository.getQuestionsByTopic("iteration_recursion")
        val hashTablesQuizQuestions = repository.getQuestionsByTopic("hash_tables")
        val treesQuizQuestions = repository.getQuestionsByTopic("trees")
        val quizTotal = arraysQuizQuestions.size + searchQuizQuestions.size + sortingQuizQuestions.size + linkedListQuizQuestions.size + stackQuizQuestions.size + queueQuizQuestions.size + iterationQuizQuestions.size + hashTablesQuizQuestions.size + treesQuizQuestions.size
        assertEquals(69, quizTotal, "Topic quizzes should return exactly 69 questions with lessonId == null")

        var lessonQuestionsTotal = 0
        allLessonIds.forEach { lessonId ->
            val lessonQs = repository.getQuestionsByLessonId(lessonId)
            lessonQuestionsTotal += lessonQs.size
        }
        assertEquals(129, lessonQuestionsTotal, "Should load 129 lesson-specific questions")
        assertEquals(198, quizTotal + lessonQuestionsTotal, "Total questions across quizzes and lessons must be 198")
    }

    @Test
    fun testAllQuestionIdsAreUnique() = runBlocking {
        val quizQuestions = listOf("arrays", "linear_search", "sorting", "linked_list", "stack", "queues", "iteration_recursion", "hash_tables", "trees").flatMap { repository.getQuestionsByTopic(it) }
        val lessonQuestions = allLessonIds.flatMap { repository.getQuestionsByLessonId(it) }
        val allQuestions = quizQuestions + lessonQuestions

        assertEquals(198, allQuestions.size, "Total collected questions must be 198")
        val uniqueIds = allQuestions.map { it.id }.toSet()
        assertEquals(198, uniqueIds.size, "All question IDs must be unique")
    }

    @Test
    fun testEveryMigratedLessonExists() = runBlocking {
        val allLessons = repository.getTopics().flatMap { repository.getLessonsByTopic(it.id) }
        val allLessonIdsInRepo = allLessons.map { it.id }.toSet()

        allLessonIds.forEach { lessonId ->
            assertTrue(allLessonIdsInRepo.contains(lessonId), "Lesson ID '$lessonId' must exist in lessons.json")
            val questions = repository.getQuestionsByLessonId(lessonId)
            assertEquals(3, questions.size, "Lesson '$lessonId' must have 3 questions")
            assertEquals(lessonId, questions.first().lessonId, "Question lessonId must match")
        }
    }

    @Test
    fun testExpandedQuestionTypesAndLessonMappings() = runBlocking {
        val topicQuestions = listOf("arrays", "linear_search", "sorting", "linked_list", "stack", "queues", "iteration_recursion", "hash_tables", "trees")
            .flatMap { repository.getQuestionsByTopic(it) }
        assertEquals(69, topicQuestions.size)
        assertTrue(topicQuestions.all { it.lessonId == null })

        val lessonQuestions = allLessonIds.flatMap { repository.getQuestionsByLessonId(it) }
        assertEquals(129, lessonQuestions.size)
        assertTrue(lessonQuestions.all { it.lessonId in allLessonIds })
        assertEquals(198, topicQuestions.size + lessonQuestions.size)
        val registeredTopicIds = repository.getTopics().map { it.id }.toSet()
        assertTrue((topicQuestions + lessonQuestions).all { it.topicId in registeredTopicIds })

        val addedIds = allLessonIds.filter { it != "lesson_arrays_1" }
            .flatMap { listOf("q_${it}_2", "q_${it}_3") }.toSet()
        val addedQuestions = lessonQuestions.filter { it.id in addedIds }
        assertEquals(84, addedQuestions.size)
        assertEquals(84, addedQuestions.map { it.id }.toSet().size)
        addedQuestions.filter { it.type == QuestionType.TRUE_FALSE }.forEach { question ->
            assertEquals(listOf("True", "False"), question.options, question.id)
            assertTrue(question.correctOptionIndex in 0..1, question.id)
        }
        addedQuestions.filter { it.type == QuestionType.MULTIPLE_CHOICE }.forEach { question ->
            assertTrue(question.options.size >= 2, question.id)
            assertTrue(question.correctOptionIndex in question.options.indices, question.id)
        }
        addedQuestions.filter { it.type == QuestionType.CODE_OUTPUT }.forEach { question ->
            assertTrue(question.statement.contains("```"), "${question.id} must contain a fenced snippet")
            assertTrue(question.options.size >= 2, question.id)
            assertTrue(question.explanation.isNotBlank(), question.id)
            assertTrue(question.correctOptionIndex in question.options.indices, question.id)
        }
        topicQuestions.filter { it.type == QuestionType.CODE_OUTPUT }.forEach { question ->
            assertTrue(question.statement.contains("```"), "${question.id} must contain a fenced snippet")
            assertTrue(question.options.size >= 2, question.id)
            assertTrue(question.explanation.isNotBlank(), question.id)
        }

        allLessonIds.forEach { lessonId ->
            val lessonSet = lessonQuestions.filter { it.lessonId == lessonId }
            assertEquals(3, lessonSet.size, "Lesson '$lessonId' must have three questions")
        }

        assertEquals(45, lessonQuestions.count { it.type == QuestionType.MULTIPLE_CHOICE })
        assertEquals(43, lessonQuestions.count { it.type == QuestionType.TRUE_FALSE })
        assertEquals(41, lessonQuestions.count { it.type == QuestionType.CODE_OUTPUT })
        assertEquals(QuestionType.MULTIPLE_CHOICE, lessonQuestions.single { it.id == "q_lesson_introduction_ds_1_2" }.type)
        assertEquals(QuestionType.MULTIPLE_CHOICE, lessonQuestions.single { it.id == "q_lesson_arrays_5_2" }.type)

        topicQuestions.groupBy { it.topicId }.forEach { (topicId, questions) ->
            val expectedTypes = if (topicId in setOf("queues", "iteration_recursion")) {
                setOf(QuestionType.MULTIPLE_CHOICE, QuestionType.CODE_OUTPUT)
            } else {
                setOf(QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE, QuestionType.CODE_OUTPUT)
            }
            assertEquals(
                expectedTypes,
                questions.map { it.type }.toSet(),
                "Topic '$topicId' must cover all question types"
            )
        }
    }

    @Test
    fun testCorrectedQuestionTracesAndLessonQuestionOrderPreparation() = runBlocking {
        val searching = repository.getQuestionsByLessonId("searching_2").single { it.id == "q_searching_2_2" }
        assertTrue(searching.statement.contains("comparisons = comparisons + 1"))
        assertTrue(searching.statement.contains("print(comparisons)"))
        assertEquals("3", searching.options[searching.correctOptionIndex])

        val sortingLesson = repository.getQuestionsByLessonId("sorting_2").single { it.id == "q_sorting_2_2" }
        val sortingQuiz = repository.getQuestionsByTopic("sorting").single { it.id == "q_sorting_quiz_5" }
        listOf(sortingLesson, sortingQuiz).forEach { question ->
            assertEquals("[1, 2, 3]", question.options[question.correctOptionIndex], question.id)
            assertTrue(question.explanation.contains("second comparison swaps 3 and 2"), question.id)
        }

        val lessonQuestions = repository.getQuestionsByLessonId("lesson_arrays_1")
        val preparedLesson = prepareLessonQuestions(lessonQuestions) { it.reversed() }
        assertEquals(lessonQuestions.map { it.id }.reversed(), preparedLesson.map { it.id })
        lessonQuestions.zip(preparedLesson.reversed()).forEach { (original, prepared) ->
            assertEquals(original.options[original.correctOptionIndex], prepared.options[prepared.correctOptionIndex], original.id)
            if (original.type == QuestionType.TRUE_FALSE) assertEquals(listOf("True", "False"), prepared.options)
        }

        val topicQuestions = repository.getQuestionsByTopic("sorting")
        val preparedTopic = prepareQuizQuestions(topicQuestions)
        val retriedOrder = shuffleQuizQuestionOrder(preparedTopic) { it.reversed() }
        assertEquals(preparedTopic.map { it.id }.reversed(), retriedOrder.map { it.id })
        preparedTopic.zip(retriedOrder.reversed()).forEach { (original, prepared) ->
            assertEquals(original.options[original.correctOptionIndex], prepared.options[prepared.correctOptionIndex], original.id)
            if (original.type == QuestionType.TRUE_FALSE) assertEquals(listOf("True", "False"), prepared.options)
        }
    }

    @Test
    fun testLinkedListCodeOutputFieldsAndDeterministicTraces() = runBlocking {
        val topicQuestion = repository.getQuestionsByTopic("linked_list").single { it.id == "q_linked_list_1" }
        val lessonQuestions = (1..5).flatMap { order ->
            repository.getQuestionsByLessonId("lesson_linked_list_$order")
        }
        val affectedLessonIds = setOf(
            "q_lesson_linked_list_1_2",
            "q_lesson_linked_list_2_2",
            "q_lesson_linked_list_3_2",
            "q_lesson_linked_list_4_2",
            "q_lesson_linked_list_5_2"
        )
        val affectedLessonQuestions = lessonQuestions.filter { it.id in affectedLessonIds }
        assertEquals(5, affectedLessonQuestions.size)
        val expectedOutputs = mapOf(
            "q_linked_list_1" to "4 then 9",
            "q_lesson_linked_list_1_2" to "4 then 9",
            "q_lesson_linked_list_2_2" to "19",
            "q_lesson_linked_list_3_2" to "5, 8, 9",
            "q_lesson_linked_list_4_2" to "4, 9",
            "q_lesson_linked_list_5_2" to "9 then 4"
        )
        val affectedQuestions = listOf(topicQuestion) + affectedLessonQuestions
        assertEquals(expectedOutputs.keys, affectedQuestions.map { it.id }.toSet())
        affectedQuestions.forEach { question ->
            assertEquals(QuestionType.CODE_OUTPUT, question.type, question.id)
            assertTrue(question.statement.contains(".data"), question.id)
            assertFalse(question.statement.contains(".value"), question.id)
            assertEquals(expectedOutputs.getValue(question.id), question.options[question.correctOptionIndex], question.id)
            assertTrue(question.explanation.isNotBlank(), question.id)
        }

        val lessonOneTrace = affectedLessonQuestions.single { it.id == "q_lesson_linked_list_1_2" }
        assertFalse(lessonOneTrace.statement.contains("while "))
        assertFalse(lessonOneTrace.statement.contains("for "))
        assertFalse(lessonOneTrace.statement.contains("if "))
        assertTrue(lessonOneTrace.statement.contains("print(head.data)"))
        assertTrue(lessonOneTrace.statement.contains("print(head.next.data)"))

        val lessonFiveTrace = affectedLessonQuestions.single { it.id == "q_lesson_linked_list_5_2" }
        assertTrue(lessonFiveTrace.statement.contains("first.prev = null"))
        assertTrue(lessonFiveTrace.statement.contains("tail.prev = first"))
        assertTrue(lessonFiveTrace.statement.contains("tail.next = null"))
        assertTrue(lessonFiveTrace.explanation.contains("prints 9 then 4 and stops"))

        val allLessonQuestions = allLessonIds.flatMap { repository.getQuestionsByLessonId(it) }
        val allTopicQuestions = listOf("arrays", "linear_search", "sorting", "linked_list", "stack", "queues", "iteration_recursion", "hash_tables", "trees")
            .flatMap { repository.getQuestionsByTopic(it) }
        assertEquals(129, allLessonQuestions.size)
        assertEquals(69, allTopicQuestions.size)
        assertEquals(198, allLessonQuestions.size + allTopicQuestions.size)
        allLessonIds.forEach { lessonId ->
            assertEquals(3, allLessonQuestions.count { it.lessonId == lessonId }, lessonId)
        }
    }

    @Test
    fun testArrayIntroductionQuestionTypesAndAnswerMapping() = runBlocking {
        val questions = repository.getQuestionsByLessonId("lesson_arrays_1")
        assertEquals(3, questions.size)

        val original = questions[0]
        assertEquals("q_lesson_arrays_1", original.id)
        assertEquals("What is the index of the first element in a typical array?", original.statement)
        assertEquals(listOf("0", "1", "-1", "2"), original.options)
        assertEquals(0, original.correctOptionIndex)
        assertEquals("Arrays use zero-based indexing, so the first element is always stored at index 0.", original.explanation)
        assertEquals(QuestionType.MULTIPLE_CHOICE, original.type)

        val formulaQuestion = questions[1]
        assertEquals("q_lesson_arrays_1_2", formulaQuestion.id)
        assertEquals(QuestionType.CODE_OUTPUT, formulaQuestion.type)
        assertTrue(formulaQuestion.statement.contains("```pseudocode"))
        assertEquals("30", formulaQuestion.options[formulaQuestion.correctOptionIndex])
        val shuffledFormulaQuestion = shuffleQuestionOptions(formulaQuestion)
        assertEquals(
            formulaQuestion.options[formulaQuestion.correctOptionIndex],
            shuffledFormulaQuestion.options[shuffledFormulaQuestion.correctOptionIndex]
        )
        val topicPrepared = prepareQuizQuestions(listOf(formulaQuestion)).single()
        assertEquals(QuestionType.CODE_OUTPUT, topicPrepared.type)
        assertEquals(
            formulaQuestion.options[formulaQuestion.correctOptionIndex],
            topicPrepared.options[topicPrepared.correctOptionIndex]
        )
        val preparedMultipleChoice = prepareQuizQuestions(listOf(original)).single()
        assertEquals(
            original.options[original.correctOptionIndex],
            preparedMultipleChoice.options[preparedMultipleChoice.correctOptionIndex]
        )

        val directAccessQuestion = questions[2]
        assertEquals("q_lesson_arrays_1_3", directAccessQuestion.id)
        assertEquals(QuestionType.TRUE_FALSE, directAccessQuestion.type)
        assertEquals("To access A[i], the computer must inspect each earlier element one by one.", directAccessQuestion.statement)
        assertEquals(listOf("True", "False"), directAccessQuestion.options)
        assertEquals(1, directAccessQuestion.correctOptionIndex)
        assertEquals("False", directAccessQuestion.options[directAccessQuestion.correctOptionIndex])
        assertEquals(
            "Because array elements are contiguous and equal in size, the address can be calculated directly from the base address, index, and element size. The lesson describes this as O(1) access.",
            directAccessQuestion.explanation
        )
        val shuffledDirectAccessQuestion = shuffleQuestionOptions(directAccessQuestion)
        assertEquals(directAccessQuestion.options, shuffledDirectAccessQuestion.options)
        assertEquals(directAccessQuestion.correctOptionIndex, shuffledDirectAccessQuestion.correctOptionIndex)
        val preparedDirectAccess = prepareQuizQuestions(listOf(directAccessQuestion)).single()
        assertEquals(directAccessQuestion.options, preparedDirectAccess.options)
        assertEquals(directAccessQuestion.correctOptionIndex, preparedDirectAccess.correctOptionIndex)
    }

    @Test
    fun testValidCorrectOptionIndex() = runBlocking {
        val quizQuestions = listOf("arrays", "linear_search", "sorting", "linked_list", "stack", "queues", "iteration_recursion", "hash_tables").flatMap { repository.getQuestionsByTopic(it) }
        val lessonQuestions = allLessonIds.flatMap { repository.getQuestionsByLessonId(it) }
        val allQuestions = quizQuestions + lessonQuestions

        allQuestions.forEach { q ->
            assertTrue(q.correctOptionIndex >= 0, "correctOptionIndex must be >= 0 for ${q.id}")
            assertTrue(q.correctOptionIndex < q.options.size, "correctOptionIndex must be < options.size for ${q.id}")
            assertTrue(q.statement.isNotBlank(), "statement must be present for ${q.id}")
            assertTrue(q.explanation.isNotBlank(), "explanation must be present for ${q.id}")
            assertTrue(q.options.size >= 2, "at least two options are required for ${q.id}")
            if (q.type == QuestionType.TRUE_FALSE) assertEquals(listOf("True", "False"), q.options, q.id)
            else {
                val correctOption = q.options[q.correctOptionIndex]
                val lessonPrepared = shuffleQuestionOptions(q)
                assertEquals(correctOption, lessonPrepared.options[lessonPrepared.correctOptionIndex], q.id)
                val topicPrepared = prepareQuizQuestions(listOf(q)).single()
                assertEquals(correctOption, topicPrepared.options[topicPrepared.correctOptionIndex], q.id)
            }
        }
    }

    @Test
    fun invalidQuestionTypeIsObservableWithQuestionId() = runBlocking {
        val invalidRepository = ContentRepositoryImpl(
            readResourceText = { path ->
                if (path == "files/questions.json") {
                    """[{"id":"broken_question_42","topicId":"arrays","statement":"Prompt","options":["A","B"],"correctOptionIndex":0,"explanation":"Reason","difficulty":"EASY","type":"UNKNOWN_KIND"}]"""
                } else {
                    "[]"
                }
            }
        )

        val error = assertFailsWith<IllegalStateException> {
            invalidRepository.getQuestionsByTopic("arrays")
        }
        assertTrue(error.message.orEmpty().contains("broken_question_42"))
        assertTrue(error.message.orEmpty().contains("UNKNOWN_KIND"))
    }

    @Test
    fun testTopicQuizRegressionExcludesLessonQuestions() = runBlocking {
        val arraysQuiz = repository.getQuestionsByTopic("arrays")
        val searchQuiz = repository.getQuestionsByTopic("linear_search")
        val sortingQuiz = repository.getQuestionsByTopic("sorting")
        val linkedListQuiz = repository.getQuestionsByTopic("linked_list")
        val stackQuiz = repository.getQuestionsByTopic("stack")
        val queuesQuiz = repository.getQuestionsByTopic("queues")
        val iterationQuiz = repository.getQuestionsByTopic("iteration_recursion")
        val hashTablesQuiz = repository.getQuestionsByTopic("hash_tables")

        assertEquals(8, arraysQuiz.size, "Arrays topic quiz must have 8 questions")
        assertEquals(8, searchQuiz.size, "Searching topic quiz must have 8 questions")
        assertEquals(8, sortingQuiz.size, "Sorting topic quiz must have 8 questions")
        assertEquals(5, linkedListQuiz.size, "Linked List topic quiz must have 5 questions")

        assertEquals(8, stackQuiz.size, "Stack topic quiz must have 8 questions")
        assertEquals(8, queuesQuiz.size, "Queues topic quiz must have 8 questions")
        assertEquals(8, iterationQuiz.size, "Iteration & Recursion topic quiz must have 8 questions")
        assertEquals(8, hashTablesQuiz.size, "Hash Tables topic quiz must have 8 questions")
        val allQuizQuestions = arraysQuiz + searchQuiz + sortingQuiz + linkedListQuiz + stackQuiz + queuesQuiz + iterationQuiz + hashTablesQuiz
        assertEquals(61, allQuizQuestions.size, "Total topic quiz questions must be 61")
        assertTrue(allQuizQuestions.all { it.lessonId == null }, "All topic quiz questions must have lessonId == null")
    }

    @Test
    fun testOriginalSixQuestionsRegression() = runBlocking {
        val arraysQuiz = repository.getQuestionsByTopic("arrays")
        val searchQuiz = repository.getQuestionsByTopic("linear_search")

        val q1 = arraysQuiz[0]
        assertEquals("q_arrays_1", q1.id)
        assertEquals("arrays", q1.topicId)
        assertNull(q1.lessonId)
        assertEquals("What is the time complexity of accessing an element in an array by its index?", q1.statement)
        assertEquals(listOf("O(1)", "O(n)", "O(log n)", "O(n^2)"), q1.options)
        assertEquals(0, q1.correctOptionIndex)
        assertEquals("Arrays store elements in contiguous memory, allowing direct index calculation in constant O(1) time.", q1.explanation)
        assertEquals(QuestionDifficulty.EASY, q1.difficulty)

        val q2 = arraysQuiz[1]
        assertEquals("q_arrays_2", q2.id)
        assertEquals("arrays", q2.topicId)
        assertNull(q2.lessonId)
        assertEquals("Assuming a fixed-size array has available capacity, what is the worst-case time complexity of inserting an element at the beginning?", q2.statement)
        assertEquals(listOf("O(1)", "O(n)", "O(log n)", "O(n log n)"), q2.options)
        assertEquals(1, q2.correctOptionIndex)
        assertEquals("With capacity available, inserting at the beginning may require shifting existing elements one position to the right. In the worst case, this shifts n elements and takes O(n) time.", q2.explanation)
        assertEquals(QuestionDifficulty.EASY, q2.difficulty)

        val q3 = arraysQuiz[2]
        assertEquals("q_arrays_3", q3.id)
        assertEquals("arrays", q3.topicId)
        assertNull(q3.lessonId)
        assertEquals("Which memory layout property allows fast element access in an array?", q3.statement)
        assertEquals(listOf("Non-linear pointers", "Contiguous memory allocation", "Hash bucket indexing", "Dynamic tree nodes"), q3.options)
        assertEquals(1, q3.correctOptionIndex)
        assertEquals("Contiguous memory allocation enables calculating memory addresses directly using base_address + index * element_size.", q3.explanation)
        assertEquals(QuestionDifficulty.MEDIUM, q3.difficulty)

        val q4 = searchQuiz[0]
        assertEquals("q_linear_search_1", q4.id)
        assertEquals("linear_search", q4.topicId)
        assertNull(q4.lessonId)
        assertEquals("What is the worst-case time complexity of Linear Search on an unsorted array of size n?", q4.statement)
        assertEquals(listOf("O(1)", "O(log n)", "O(n)", "O(n^2)"), q4.options)
        assertEquals(2, q4.correctOptionIndex)
        assertEquals("In the worst case, the target element is at the last index or not present at all, requiring n comparisons.", q4.explanation)
        assertEquals(QuestionDifficulty.EASY, q4.difficulty)

        val q5 = searchQuiz[1]
        assertEquals("q_linear_search_2", q5.id)
        assertEquals("linear_search", q5.topicId)
        assertNull(q5.lessonId)
        assertEquals("Linear Search requires the array to be sorted before it can search for a target.", q5.statement)
        assertEquals(QuestionType.TRUE_FALSE, q5.type)
        assertEquals(listOf("True", "False"), q5.options)
        assertEquals(1, q5.correctOptionIndex)
        assertEquals("Linear Search checks items in order and also works when values are unsorted.", q5.explanation)
        assertEquals(QuestionDifficulty.EASY, q5.difficulty)

        val q6 = searchQuiz[2]
        assertEquals("q_linear_search_3", q6.id)
        assertEquals("linear_search", q6.topicId)
        assertNull(q6.lessonId)
        assertEquals("What is the best-case time complexity of Linear Search?", q6.statement)
        assertEquals(listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"), q6.options)
        assertEquals(0, q6.correctOptionIndex)
        assertEquals("The best case occurs when the target element is at the very first index (index 0), taking O(1) time.", q6.explanation)
        assertEquals(QuestionDifficulty.MEDIUM, q6.difficulty)
    }

    @Test
    fun testMigratedFourteenQuestionsParity() = runBlocking {
        val q1 = repository.getQuestionsByLessonId("lesson_arrays_1").first()
        assertEquals("q_lesson_arrays_1", q1.id)
        assertEquals("arrays", q1.topicId)
        assertEquals("lesson_arrays_1", q1.lessonId)
        assertEquals("What is the index of the first element in a typical array?", q1.statement)
        assertEquals(listOf("0", "1", "-1", "2"), q1.options)
        assertEquals(0, q1.correctOptionIndex)
        assertEquals("Arrays use zero-based indexing, so the first element is always stored at index 0.", q1.explanation)

        val q2 = repository.getQuestionsByLessonId("lesson_arrays_2").first()
        assertEquals("q_lesson_arrays_2", q2.id)
        assertEquals("arrays", q2.topicId)
        assertEquals("lesson_arrays_2", q2.lessonId)
        assertEquals("Why does inserting or deleting an element at an arbitrary index in an array have an O(n) worst-case time complexity?", q2.statement)
        assertEquals(listOf("Remaining elements must be shifted in memory to maintain contiguous order", "The entire array must be converted to a linked list first", "Arrays automatically double their size on every insertion", "Memory pointers must be re-sorted after every deletion"), q2.options)
        assertEquals(0, q2.correctOptionIndex)
        assertEquals("Inserting or deleting an element requires shifting the surrounding elements in memory to maintain contiguous allocation, taking O(n) time in the worst case.", q2.explanation)

        val q3 = repository.getQuestionsByLessonId("lesson_arrays_3").first()
        assertEquals("q_lesson_arrays_3", q3.id)
        assertEquals("arrays", q3.topicId)
        assertEquals("lesson_arrays_3", q3.lessonId)
        assertEquals("What is the purpose of the index while traversing an array?", q3.statement)
        assertEquals(listOf("It identifies the current element", "It changes the array size", "It sorts the array", "It deletes the current element"), q3.options)
        assertEquals(0, q3.correctOptionIndex)
        assertEquals("The index identifies the position of the element currently being visited during traversal.", q3.explanation)

        val q4 = repository.getQuestionsByLessonId("lesson_arrays_4").first()
        assertEquals("q_lesson_arrays_4", q4.id)
        assertEquals("arrays", q4.topicId)
        assertEquals("lesson_arrays_4", q4.lessonId)
        assertEquals("Why does inserting an element in the middle of an array take O(n) time?", q4.statement)
        assertEquals(listOf("Elements after the insertion index must shift right", "The entire array must be sorted", "Memory addresses are randomized", "Array index lookup is O(n)"), q4.options)
        assertEquals(0, q4.correctOptionIndex)
        assertEquals("Inserting in the middle requires shifting all subsequent elements right by one position to create room.", q4.explanation)

        val q5 = repository.getQuestionsByLessonId("lesson_arrays_5").first()
        assertEquals("q_lesson_arrays_5", q5.id)
        assertEquals("arrays", q5.topicId)
        assertEquals("lesson_arrays_5", q5.lessonId)
        assertEquals("Which array operation has O(1) constant time complexity?", q5.statement)
        assertEquals(listOf("Accessing an element by index", "Searching for an item in an unsorted array", "Inserting an element in the middle", "Deleting an element from the middle"), q5.options)
        assertEquals(0, q5.correctOptionIndex)
        assertEquals("Arrays use contiguous memory offsets, allowing instant O(1) element access directly by index.", q5.explanation)

        val q6 = repository.getQuestionsByLessonId("searching_1").first()
        assertEquals("q_searching_1", q6.id)
        assertEquals("linear_search", q6.topicId)
        assertEquals("searching_1", q6.lessonId)
        assertEquals("In algorithm analysis, what does the term 'target' refer to?", q6.statement)
        assertEquals(listOf("The specific element you are searching for", "The size of the array", "The total execution time", "The last element in memory"), q6.options)
        assertEquals(0, q6.correctOptionIndex)
        assertEquals("The target is the specific value or element the search algorithm is looking to locate.", q6.explanation)

        val q7 = repository.getQuestionsByLessonId("searching_2").first()
        assertEquals("q_searching_2", q7.id)
        assertEquals("linear_search", q7.topicId)
        assertEquals("searching_2", q7.lessonId)
        assertEquals("What is the worst-case time complexity of Linear Search on an array of n items?", q7.statement)
        assertEquals(listOf("O(n)", "O(1)", "O(log n)", "O(n²)"), q7.options)
        assertEquals(0, q7.correctOptionIndex)
        assertEquals("In the worst case, Linear Search must check all n elements if the target is at the end or missing.", q7.explanation)

        val q8 = repository.getQuestionsByLessonId("searching_3").first()
        assertEquals("q_searching_3", q8.id)
        assertEquals("linear_search", q8.topicId)
        assertEquals("searching_3", q8.lessonId)
        assertEquals("What is the essential prerequisite for applying Binary Search to an array?", q8.statement)
        assertEquals(listOf("The array must be sorted", "The array size must be even", "All elements must be positive", "The array must contain unique values"), q8.options)
        assertEquals(0, q8.correctOptionIndex)
        assertEquals("Binary Search relies on sorted order to eliminate half the search space at each comparison.", q8.explanation)

        val q9 = repository.getQuestionsByLessonId("searching_4").first()
        assertEquals("q_searching_4", q9.id)
        assertEquals("linear_search", q9.topicId)
        assertEquals("searching_4", q9.lessonId)
        assertEquals("When searching for target 72 in the sorted array [4, 12, 19, 27, 33, 44, 51, 60, 72, 81, 95], how many comparisons does Binary Search take compared to Linear Search?", q9.statement)
        assertEquals(listOf("2 comparisons for Binary Search vs 9 comparisons for Linear Search", "11 comparisons for Binary Search vs 1 comparison for Linear Search", "5 comparisons for Binary Search vs 5 comparisons for Linear Search", "1 comparison for Binary Search vs 11 comparisons for Linear Search"), q9.options)
        assertEquals(0, q9.correctOptionIndex)
        assertEquals("Binary Search inspects midpoint 44 and then midpoint 72 (2 comparisons), while Linear Search checks each item sequentially from 4 up to 72 (9 comparisons).", q9.explanation)

        val q10 = repository.getQuestionsByLessonId("sorting_1").first()
        assertEquals("q_sorting_1", q10.id)
        assertEquals("sorting", q10.topicId)
        assertEquals("sorting_1", q10.lessonId)
        assertEquals("What is the primary benefit of organizing data into a sorted order?", q10.statement)
        assertEquals(listOf("It enables faster search and lookup operations like Binary Search", "It reduces the memory size of the array", "It converts integers to strings automatically", "It prevents duplicate elements from being added"), q10.options)
        assertEquals(0, q10.correctOptionIndex)
        assertEquals("Sorted order is required by algorithms like Binary Search, reducing search time from O(n) to O(log n).", q10.explanation)

        val q11 = repository.getQuestionsByLessonId("sorting_2").first()
        assertEquals("q_sorting_2", q11.id)
        assertEquals("sorting", q11.topicId)
        assertEquals("sorting_2", q11.lessonId)
        assertEquals("What is the best-case time complexity of Bubble Sort when an early-exit optimization is used on pre-sorted data?", q11.statement)
        assertEquals(listOf("O(n)", "O(1)", "O(n²)", "O(log n)"), q11.options)
        assertEquals(0, q11.correctOptionIndex)
        assertEquals("With an early-exit flag, Bubble Sort makes a single O(n) pass over pre-sorted data, detecting zero swaps and finishing early.", q11.explanation)

        val q12 = repository.getQuestionsByLessonId("sorting_3").first()
        assertEquals("q_sorting_3", q12.id)
        assertEquals("sorting", q12.topicId)
        assertEquals("sorting_3", q12.lessonId)
        assertEquals("Why does Selection Sort always have a time complexity of O(n²), even on already sorted data?", q12.statement)
        assertEquals(listOf("It must scan the entire unsorted region to find the minimum in every pass", "It performs n² swaps in every pass", "It doubles the array size at each step", "It uses recursion to find the minimum"), q12.options)
        assertEquals(0, q12.correctOptionIndex)
        assertEquals("Selection Sort does not check if the array is already sorted; it unconditionally scans the remaining unsorted items to find the minimum in every pass.", q12.explanation)

        val q13 = repository.getQuestionsByLessonId("sorting_4").first()
        assertEquals("q_sorting_4", q13.id)
        assertEquals("sorting", q13.topicId)
        assertEquals("sorting_4", q13.lessonId)
        assertEquals("How does Insertion Sort perform when given an array that is already almost completely sorted?", q13.statement)
        assertEquals(listOf("It runs efficiently in near O(n) time with very few element shifts", "It takes O(n²) time regardless of initial order", "It throws an index out of bounds error", "It requires O(n log n) additional memory"), q13.options)
        assertEquals(0, q13.correctOptionIndex)
        assertEquals("Insertion Sort is adaptive: if elements are already in order, it makes only 1 comparison per element and 0 shifts, taking O(n) time.", q13.explanation)

        val q14 = repository.getQuestionsByLessonId("sorting_5").first()
        assertEquals("q_sorting_5", q14.id)
        assertEquals("sorting", q14.topicId)
        assertEquals("sorting_5", q14.lessonId)
        assertEquals("Which elementary sorting algorithm minimizes the total number of element swaps to at most O(n)?", q14.statement)
        assertEquals(listOf("Selection Sort", "Bubble Sort", "Insertion Sort", "Merge Sort"), q14.options)
        assertEquals(0, q14.correctOptionIndex)
        assertEquals("Selection Sort performs at most 1 swap per pass (n - 1 total swaps max), minimizing memory write operations.", q14.explanation)
    }
}
