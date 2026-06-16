package com.preetanshumishra.patternkit.android.mvvmxml.data

import com.preetanshumishra.patternkit.android.mvvmxml.model.Priority
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskItem
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Deterministic seed used by `MockTaskRepository` — same shape (12 tasks,
 * same titles, same priority mix) as the iOS module's `TaskSeedData`, so
 * the two pilots render visually identical lists for side-by-side review.
 */
object TaskSeedData {
    private fun daysFromNow(offset: Int): Instant =
        Instant.now().plus(offset.toLong(), ChronoUnit.DAYS)

    val tasks: List<TaskItem>
        get() = listOf(
            TaskItem(
                title = "Review PR for auth refactor",
                notes = "Pay attention to the token refresh path.",
                dueDate = daysFromNow(-1),
                priority = Priority.HIGH
            ),
            TaskItem(
                title = "Plan Q3 roadmap",
                dueDate = daysFromNow(2),
                priority = Priority.HIGH
            ),
            TaskItem(
                title = "Reply to support emails",
                priority = Priority.MEDIUM
            ),
            TaskItem(
                title = "Update SDK docs",
                notes = "Section on token refresh is stale.",
                dueDate = daysFromNow(5),
                priority = Priority.MEDIUM
            ),
            TaskItem(
                title = "Renew domain",
                dueDate = daysFromNow(14),
                priority = Priority.LOW
            ),
            TaskItem(
                title = "Pick up dry cleaning",
                dueDate = daysFromNow(0),
                priority = Priority.LOW
            ),
            TaskItem(
                title = "Investigate flaky integration test",
                notes = "Reproduces ~1 in 8 runs on CI.",
                priority = Priority.HIGH,
                isCompleted = true
            ),
            TaskItem(
                title = "Wireframe onboarding flow",
                dueDate = daysFromNow(-3),
                priority = Priority.MEDIUM,
                isCompleted = true
            ),
            TaskItem(
                title = "Book flight for conference",
                dueDate = daysFromNow(21),
                priority = Priority.MEDIUM
            ),
            TaskItem(
                title = "1:1 prep — direct reports",
                dueDate = daysFromNow(1),
                priority = Priority.HIGH
            ),
            TaskItem(
                title = "Refactor logging module",
                priority = Priority.LOW,
                isCompleted = true
            ),
            TaskItem(
                title = "Buy birthday gift",
                dueDate = daysFromNow(7),
                priority = Priority.MEDIUM
            ),
        )
}
