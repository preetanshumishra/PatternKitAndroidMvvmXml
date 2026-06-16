package com.preetanshumishra.patternkit.android.mvvmxml.data

import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskItem
import com.preetanshumishra.patternkit.android.mvvmxml.data.TaskRepository
import com.preetanshumishra.patternkit.android.mvvmxml.data.TaskRepositoryError
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * In-memory mock implementing the domain's [TaskRepository] contract.
 * Identical role to the iOS module's `MockTaskRepository` — single source
 * of truth, configurable latency + failure rate so the UI can be exercised
 * against loading / error states without a real network.
 *
 * Mutex-guarded so concurrent ViewModel calls don't race on `tasks`.
 */
@Singleton
class MockTaskRepository @Inject constructor() : TaskRepository {

    private val mutex = Mutex()
    private val tasks: MutableList<TaskItem> = TaskSeedData.tasks.toMutableList()

    var latencyMs: Long = 600
    var failureRate: Double = 0.0

    override suspend fun fetchAll(): List<TaskItem> {
        simulateWork()
        return mutex.withLock { tasks.toList() }
    }

    override suspend fun create(task: TaskItem): TaskItem {
        simulateWork()
        return mutex.withLock {
            tasks.add(task)
            task
        }
    }

    override suspend fun update(task: TaskItem): TaskItem {
        simulateWork()
        return mutex.withLock {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index == -1) throw TaskRepositoryError.NotFound
            val updated = task.copy(updatedAt = Instant.now())
            tasks[index] = updated
            updated
        }
    }

    override suspend fun delete(id: UUID) {
        simulateWork()
        mutex.withLock {
            val removed = tasks.removeAll { it.id == id }
            if (!removed) throw TaskRepositoryError.NotFound
        }
    }

    private suspend fun simulateWork() {
        if (latencyMs > 0) delay(latencyMs)
        if (failureRate > 0.0 && Random.nextDouble() < failureRate) {
            throw TaskRepositoryError.Simulated
        }
    }
}
