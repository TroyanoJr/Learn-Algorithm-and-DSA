package com.example.learndsandalgorithm.data.repository

import com.example.learndsandalgorithm.domain.model.QuestionDifficulty
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
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
        "lesson_arrays_1", "lesson_arrays_2", "lesson_arrays_3", "lesson_arrays_4", "lesson_arrays_5",
        "searching_1", "searching_2", "searching_3", "searching_4",
        "sorting_1", "sorting_2", "sorting_3", "sorting_4", "sorting_5"
    )

    @Test
    fun testLoadAllTopics() = runBlocking {
        val topics = repository.getTopics()
        assertEquals(20, topics.size, "Should load exactly 20 topics")
    }

    @Test
    fun testLoadAllLessons() = runBlocking {
        val arraysLessons = repository.getLessonsByTopic("arrays")
        val searchLessons = repository.getLessonsByTopic("linear_search")
        val sortingLessons = repository.getLessonsByTopic("sorting")
        val totalLessons = arraysLessons.size + searchLessons.size + sortingLessons.size
        assertEquals(14, totalLessons, "Should load exactly 14 lessons total")
        assertEquals(5, arraysLessons.size, "Arrays should have 5 lessons")
        assertEquals(4, searchLessons.size, "Searching should have 4 lessons")
        assertEquals(5, sortingLessons.size, "Sorting should have 5 lessons")
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
    fun testLoadAllQuestions() = runBlocking {
        val arraysQuizQuestions = repository.getQuestionsByTopic("arrays")
        val searchQuizQuestions = repository.getQuestionsByTopic("linear_search")
        val quizTotal = arraysQuizQuestions.size + searchQuizQuestions.size
        assertEquals(6, quizTotal, "Topic quiz should return exactly 6 questions with lessonId == null")

        var lessonQuestionsTotal = 0
        allLessonIds.forEach { lessonId ->
            val lessonQs = repository.getQuestionsByLessonId(lessonId)
            lessonQuestionsTotal += lessonQs.size
        }
        assertEquals(14, lessonQuestionsTotal, "Should load 14 lesson-specific questions")
        assertEquals(20, quizTotal + lessonQuestionsTotal, "Total questions across quizzes and lessons must be 20")
    }

    @Test
    fun testAllQuestionIdsAreUnique() = runBlocking {
        val quizQuestions = listOf("arrays", "linear_search", "sorting").flatMap { repository.getQuestionsByTopic(it) }
        val lessonQuestions = allLessonIds.flatMap { repository.getQuestionsByLessonId(it) }
        val allQuestions = quizQuestions + lessonQuestions

        assertEquals(20, allQuestions.size, "Total collected questions must be 20")
        val uniqueIds = allQuestions.map { it.id }.toSet()
        assertEquals(20, uniqueIds.size, "All 20 question IDs must be unique")
    }

    @Test
    fun testEveryMigratedLessonExists() = runBlocking {
        val allLessons = repository.getTopics().flatMap { repository.getLessonsByTopic(it.id) }
        val allLessonIdsInRepo = allLessons.map { it.id }.toSet()

        allLessonIds.forEach { lessonId ->
            assertTrue(allLessonIdsInRepo.contains(lessonId), "Lesson ID '$lessonId' must exist in lessons.json")
            val questions = repository.getQuestionsByLessonId(lessonId)
            assertEquals(1, questions.size, "Lesson '$lessonId' must have exactly 1 question")
            assertEquals(lessonId, questions.first().lessonId, "Question lessonId must match")
        }
    }

    @Test
    fun testValidCorrectOptionIndex() = runBlocking {
        val quizQuestions = listOf("arrays", "linear_search", "sorting").flatMap { repository.getQuestionsByTopic(it) }
        val lessonQuestions = allLessonIds.flatMap { repository.getQuestionsByLessonId(it) }
        val allQuestions = quizQuestions + lessonQuestions

        allQuestions.forEach { q ->
            assertTrue(q.correctOptionIndex >= 0, "correctOptionIndex must be >= 0 for ${q.id}")
            assertTrue(q.correctOptionIndex < q.options.size, "correctOptionIndex must be < options.size for ${q.id}")
        }
    }

    @Test
    fun testTopicQuizRegressionExcludesLessonQuestions() = runBlocking {
        val arraysQuiz = repository.getQuestionsByTopic("arrays")
        val searchQuiz = repository.getQuestionsByTopic("linear_search")
        val sortingQuiz = repository.getQuestionsByTopic("sorting")

        assertEquals(3, arraysQuiz.size, "Arrays topic quiz must have 3 questions")
        assertEquals(3, searchQuiz.size, "Searching topic quiz must have 3 questions")
        assertEquals(0, sortingQuiz.size, "Sorting topic quiz must have 0 questions")

        val allQuizQuestions = arraysQuiz + searchQuiz + sortingQuiz
        assertEquals(6, allQuizQuestions.size, "Total topic quiz questions must be 6")
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
        assertEquals("What is the worst-case time complexity of inserting an element at the beginning of a fixed-size array?", q2.statement)
        assertEquals(listOf("O(1)", "O(n)", "O(log n)", "O(n log n)"), q2.options)
        assertEquals(1, q2.correctOptionIndex)
        assertEquals("Inserting at the beginning requires shifting all existing n elements one position to the right, taking O(n) time.", q2.explanation)
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
        assertEquals("Does Linear Search require the target array to be sorted before searching?", q5.statement)
        assertEquals(listOf("Yes, always", "No, it works on unsorted arrays", "Only if array size > 100", "Yes, but only in descending order"), q5.options)
        assertEquals(1, q5.correctOptionIndex)
        assertEquals("Linear search checks elements sequentially one by one, so array order or sorting is not required.", q5.explanation)
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
        assertEquals("What is the index of the first element in a typical array?", q2.statement)
        assertEquals(listOf("0", "1", "-1", "2"), q2.options)
        assertEquals(0, q2.correctOptionIndex)
        assertEquals("Arrays use zero-based indexing, so the first element is always stored at index 0.", q2.explanation)

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
        assertEquals("When searching for target 44 in an 11-element sorted array, how many steps does Binary Search take compared to Linear Search?", q9.statement)
        assertEquals(listOf("3 steps for Binary Search vs 10 steps for Linear Search", "11 steps for Binary Search vs 1 step for Linear Search", "5 steps for Binary Search vs 5 steps for Linear Search", "1 step for Binary Search vs 11 steps for Linear Search"), q9.options)
        assertEquals(0, q9.correctOptionIndex)
        assertEquals("Binary Search eliminates half the remaining items at each comparison, finding 44 in 3 steps versus 10 steps for Linear Search.", q9.explanation)

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
        assertEquals("sorting", q10.topicId)
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
