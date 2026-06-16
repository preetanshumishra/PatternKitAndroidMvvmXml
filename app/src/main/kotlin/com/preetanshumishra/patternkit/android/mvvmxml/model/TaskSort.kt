package com.preetanshumishra.patternkit.android.mvvmxml.model

enum class TaskSort(val displayName: String) {
    DUE_DATE("Due date"),
    PRIORITY("Priority");

    fun sorted(tasks: List<TaskItem>): List<TaskItem> = when (this) {
        DUE_DATE -> tasks.sortedWith(
            compareBy<TaskItem, java.time.Instant?>(nullsLast()) { it.dueDate }
                .thenByDescending { it.createdAt }
        )
        PRIORITY -> tasks.sortedWith(
            compareBy<TaskItem> { it.priority.sortOrder }
                .thenByDescending { it.createdAt }
        )
    }
}
