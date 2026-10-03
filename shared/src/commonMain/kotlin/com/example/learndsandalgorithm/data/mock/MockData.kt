package com.example.learndsandalgorithm.data.mock

import com.example.learndsandalgorithm.domain.model.Activity
import com.example.learndsandalgorithm.domain.model.Challenge
import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.model.Progress
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.TopicCategory
import com.example.learndsandalgorithm.domain.model.TopicLevel
import com.example.learndsandalgorithm.domain.model.UserStats

val mockUserStats = UserStats(
    streakDays = 5,
    totalXp = 2480,
    completedLessons = 12
)

val mockTopics = listOf(
    // DATA STRUCTURES
    Topic("introduction_ds", "Introduction", TopicCategory.DATA_STRUCTURES, TopicLevel.FOUNDATION, 1),
    Topic("arrays", "Arrays", TopicCategory.DATA_STRUCTURES, TopicLevel.FOUNDATION, 2),
    Topic("structures", "Structures", TopicCategory.DATA_STRUCTURES, TopicLevel.FOUNDATION, 3),
    Topic("linked_list", "Linked List", TopicCategory.DATA_STRUCTURES, TopicLevel.FOUNDATION, 4),
    Topic("stack", "Stack", TopicCategory.DATA_STRUCTURES, TopicLevel.FOUNDATION, 5),
    Topic("queues", "Queues", TopicCategory.DATA_STRUCTURES, TopicLevel.FOUNDATION, 6),
    Topic("hash_tables", "Hash Tables", TopicCategory.DATA_STRUCTURES, TopicLevel.INTERMEDIATE, 7),
    Topic("trees", "Trees", TopicCategory.DATA_STRUCTURES, TopicLevel.INTERMEDIATE, 8),
    Topic("heap", "Heap", TopicCategory.DATA_STRUCTURES, TopicLevel.INTERMEDIATE, 9),
    Topic("graphs", "Graphs", TopicCategory.DATA_STRUCTURES, TopicLevel.ADVANCED, 10),

    // ALGORITHMS
    Topic("introduction_algo", "Introduction", TopicCategory.ALGORITHMS, TopicLevel.FOUNDATION, 1),
    Topic("linear_search", "Searching", TopicCategory.ALGORITHMS, TopicLevel.FOUNDATION, 2),
    Topic("sorting", "Sorting", TopicCategory.ALGORITHMS, TopicLevel.FOUNDATION, 3),
    Topic("brute_force", "Brute Force", TopicCategory.ALGORITHMS, TopicLevel.FOUNDATION, 4),
    Topic("iteration_recursion", "Iteration & Recursion", TopicCategory.ALGORITHMS, TopicLevel.INTERMEDIATE, 5),
    Topic("divide_conquer", "Divide and Conquer", TopicCategory.ALGORITHMS, TopicLevel.INTERMEDIATE, 6),
    Topic("greedy", "Greedy", TopicCategory.ALGORITHMS, TopicLevel.INTERMEDIATE, 7),
    Topic("backtracking", "Backtracking", TopicCategory.ALGORITHMS, TopicLevel.ADVANCED, 8),
    Topic("dynamic_programming", "Dynamic Programming", TopicCategory.ALGORITHMS, TopicLevel.ADVANCED, 9),
    Topic("branch_bound", "Branch and Bound", TopicCategory.ALGORITHMS, TopicLevel.ADVANCED, 10)
)

val mockLessons = listOf(
    // Arrays
    Lesson(
        id = "lesson_arrays_1",
        topicId = "arrays",
        title = "Introduction to Arrays",
        order = 1,
        xp = 50,
        contentPath = "files/lessons/lesson_arrays_1.md"
    ),
    Lesson(
        id = "lesson_arrays_2",
        topicId = "arrays",
        title = "Array Operations & Memory",
        order = 2,
        xp = 50,
        contentPath = "files/lessons/lesson_arrays_2.md"
    ),
    Lesson(
        id = "lesson_arrays_3",
        topicId = "arrays",
        title = "Traversing an Array",
        order = 3,
        xp = 20,
        contentPath = "files/lessons/lesson_arrays_3.md"
    ),
    Lesson(
        id = "lesson_arrays_4",
        topicId = "arrays",
        title = "Insert and Delete",
        order = 4,
        xp = 30,
        contentPath = "files/lessons/lesson_arrays_4.md"
    ),
    Lesson(
        id = "lesson_arrays_5",
        topicId = "arrays",
        title = "Arrays and Complexity",
        order = 5,
        xp = 15,
        contentPath = "files/lessons/lesson_arrays_5.md"
    ),

    // Searching
    Lesson(
        id = "searching_1",
        topicId = "linear_search",
        title = "What Is Searching?",
        order = 1,
        xp = 15,
        contentPath = "files/lessons/searching_1.md"
    ),
    Lesson(
        id = "searching_2",
        topicId = "linear_search",
        title = "Linear Search",
        order = 2,
        xp = 25,
        contentPath = "files/lessons/searching_2.md"
    ),
    Lesson(
        id = "searching_3",
        topicId = "linear_search",
        title = "Binary Search",
        order = 3,
        xp = 30,
        contentPath = "files/lessons/searching_3.md"
    ),
    Lesson(
        id = "searching_4",
        topicId = "linear_search",
        title = "Linear vs. Binary Search",
        order = 4,
        xp = 25,
        contentPath = "files/lessons/searching_4.md"
    ),

    // Sorting
    Lesson(
        id = "sorting_1",
        topicId = "sorting",
        title = "What Is Sorting?",
        order = 1,
        xp = 15,
        contentPath = "files/lessons/sorting_1.md"
    ),
    Lesson(
        id = "sorting_2",
        topicId = "sorting",
        title = "Bubble Sort",
        order = 2,
        xp = 25,
        contentPath = "files/lessons/sorting_2.md"
    ),
    Lesson(
        id = "sorting_3",
        topicId = "sorting",
        title = "Selection Sort",
        order = 3,
        xp = 25,
        contentPath = "files/lessons/sorting_3.md"
    ),
    Lesson(
        id = "sorting_4",
        topicId = "sorting",
        title = "Insertion Sort",
        order = 4,
        xp = 30,
        contentPath = "files/lessons/sorting_4.md"
    ),
    Lesson(
        id = "sorting_5",
        topicId = "sorting",
        title = "Comparing Sorting Algorithms",
        order = 5,
        xp = 25,
        contentPath = "files/lessons/sorting_5.md"
    )
)

val mockProgress = Progress(
    overallProgress = 32,
    dataStructuresProgress = 6,
    algorithmsProgress = 4,
    quizScore = 32
)

val mockActivities = listOf(
    Activity(
        id = "act_1",
        title = "Completed Searching",
        description = "Finished Linear Search and Binary Search modules.",
        type = "completion"
    ),
    Activity(
        id = "act_2",
        title = "Started Arrays",
        description = "Explored Array Operations & Complexity lesson.",
        type = "started"
    )
)

val mockRecommendedPractice = Challenge(
    id = "challenge_arrays_complexity",
    title = "Arrays & Complexity",
    description = "Contiene 5 preguntas para reforzar la última lección sobre arrays y análisis de complejidad.",
    topicId = "arrays",
    questionCount = 5
)
