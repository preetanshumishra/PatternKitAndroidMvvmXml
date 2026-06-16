package com.preetanshumishra.patternkit.android.mvvmxml.model

enum class TaskFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed");

    fun matches(task: TaskItem): Boolean = when (this) {
        ALL       -> true
        ACTIVE    -> !task.isCompleted
        COMPLETED -> task.isCompleted
    }
}
