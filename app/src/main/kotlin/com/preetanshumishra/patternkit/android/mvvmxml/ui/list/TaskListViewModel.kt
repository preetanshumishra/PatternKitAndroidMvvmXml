package com.preetanshumishra.patternkit.android.mvvmxml.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preetanshumishra.patternkit.android.mvvmxml.data.TaskRepository
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskFilter
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskItem
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskSort
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/**
 * List screen ViewModel — the LiveData flavour for the XML Views stack.
 *
 * Same single-source-of-truth shape as the Compose/StateFlow modules, but the
 * derived `uiState` is assembled with a [MediatorLiveData] that re-emits
 * whenever any of the five backing `MutableLiveData`s change — the LiveData
 * analog of StateFlow `combine` / iOS `CombineLatest`. This is the plain-MVVM
 * module: the ViewModel talks to the repository directly.
 */
class TaskListViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _allTasks = MutableLiveData<List<TaskItem>>(emptyList())
    private val _filter = MutableLiveData(TaskFilter.ALL)
    private val _sort = MutableLiveData(TaskSort.DUE_DATE)
    private val _isLoading = MutableLiveData(false)
    private val _errorMessage = MutableLiveData<String?>(null)

    val uiState: LiveData<TaskListUiState> = MediatorLiveData<TaskListUiState>().apply {
        fun recompute() {
            val all = _allTasks.value.orEmpty()
            val filter = _filter.value ?: TaskFilter.ALL
            val sort = _sort.value ?: TaskSort.DUE_DATE
            value = TaskListUiState(
                displayedTasks = sort.sorted(all.filter(filter::matches)),
                filter = filter,
                sort = sort,
                isLoading = _isLoading.value ?: false,
                errorMessage = _errorMessage.value
            )
        }
        addSource(_allTasks) { recompute() }
        addSource(_filter) { recompute() }
        addSource(_sort) { recompute() }
        addSource(_isLoading) { recompute() }
        addSource(_errorMessage) { recompute() }
    }

    private var hasLoaded = false

    /** Loads once for the lifetime of this (activity-scoped) ViewModel, so
     *  returning to the list from detail/form doesn't refetch. Use [refresh]
     *  for an explicit reload. */
    fun load() {
        if (hasLoaded) return
        hasLoaded = true
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allTasks.value = repository.fetchAll()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _allTasks.value = repository.fetchAll()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun setFilter(filter: TaskFilter) { _filter.value = filter }
    fun setSort(sort: TaskSort) { _sort.value = sort }

    fun toggleCompletion(task: TaskItem) {
        viewModelScope.launch {
            try {
                val saved = repository.update(
                    task.copy(isCompleted = !task.isCompleted, updatedAt = Instant.now())
                )
                replaceInAllTasks(saved)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun delete(task: TaskItem) {
        viewModelScope.launch {
            try {
                repository.delete(task.id)
                _allTasks.value = _allTasks.value.orEmpty().filterNot { it.id == task.id }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    /** Lookup across the full (unfiltered) set, so the detail screen can still
     *  find a task the active filter would hide. */
    fun task(id: UUID): TaskItem? = _allTasks.value?.firstOrNull { it.id == id }

    /** Upsert after the form ViewModel persists a create/edit. */
    fun apply(task: TaskItem) {
        val current = _allTasks.value.orEmpty()
        val index = current.indexOfFirst { it.id == task.id }
        _allTasks.value = if (index >= 0) {
            current.toMutableList().also { it[index] = task }
        } else {
            current + task
        }
    }

    private fun replaceInAllTasks(task: TaskItem) {
        val current = _allTasks.value.orEmpty()
        val index = current.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            _allTasks.value = current.toMutableList().also { it[index] = task }
        }
    }
}

data class TaskListUiState(
    val displayedTasks: List<TaskItem> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val sort: TaskSort = TaskSort.DUE_DATE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
