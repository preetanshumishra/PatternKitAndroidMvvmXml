package com.preetanshumishra.patternkit.android.mvvmxml.data

import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskItem
import java.util.UUID

/**
 * The data-source contract. In this MVVM (no-Clean) module the repository is
 * the single abstraction between the UI and the data — ViewModels depend on it
 * directly. There is no domain/use-case layer in front of it; that's the whole
 * difference from the MVVM+Clean module.
 */
interface TaskRepository {
    suspend fun fetchAll(): List<TaskItem>
    suspend fun create(task: TaskItem): TaskItem
    suspend fun update(task: TaskItem): TaskItem
    suspend fun delete(id: UUID)
}

sealed class TaskRepositoryError(message: String) : Exception(message) {
    object NotFound : TaskRepositoryError("Task not found.")
    object Simulated : TaskRepositoryError("Something went wrong. Please try again.")
}
