# What Are Data Structures?

A data structure is a specialized format for organizing, processing, retrieving, and storing data in a computer so that operations can be performed efficiently.

## Data Types, Data Structures, and Algorithms

These terms describe different parts of a program:

- A **data type** describes what kind of value something is and which operations make sense for it. For example, a student number might be an integer, while a student's name is text.
- A **data structure** describes how a program organizes a group of values so it can work with them. An array, for example, keeps items in an ordered sequence with positions called indices.
- An **algorithm** is a set of steps for solving a problem using data. It might look through the roster to find one student's record and then mark that student present.

In this example, the student number and name have data types, the roster is stored in a data structure, and the steps for finding and updating a record are an algorithm. A program combines data and instructions to carry out that task.

## Why Data Structures Matter

Data structures are foundational to computer science because they determine how effectively a program can access and modify memory. Selecting the right data structure reduces memory usage and speeds up program execution.

Programs often need to store many related values and perform different actions on them: find one value, visit every value, add a new one, or remove one. How the values are organized affects how those actions work. For instance, a class roster stored in order is easy to read from beginning to end; adding a student in the middle while keeping that order may require moving or relinking other entries, depending on the structure.

## Common Examples

- **Arrays**: Store elements in contiguous memory slots for instant index lookup.
- **Linked Lists**: Store elements in nodes linked by memory pointers.
- **Stacks & Queues**: Organize elements with specific access rules like LIFO (Last-In, First-Out) or FIFO (First-In, First-Out).

Different data structures excel at different operations. For example, arrays excel at fast index lookups, while stacks excel at tracking temporary operation history.

There is no single best data structure for every problem. The useful question is: what actions must the program perform, and what order should those actions follow? The next lesson compares a few common structures to help answer that question.
