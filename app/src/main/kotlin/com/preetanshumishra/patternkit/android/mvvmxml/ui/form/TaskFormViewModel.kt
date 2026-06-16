package com.preetanshumishra.patternkit.android.mvvmxml.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preetanshumishra.patternkit.android.mvvmxml.data.TaskRepository
import com.preetanshumishra.patternkit.android.mvvmxml.model.Priority
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskItem
import com.preetanshumishra.patternkit.android.mvvmxml.ui.Event
import kotlinx.coroutines.launch
import java.time.Instant

/** Distinguishes create-mode from edit-mode at construction time. */
sealed class TaskFormMode {
    object Create : TaskFormMode()
    data class Edit(val task: TaskItem) : TaskFormMode()
}

data class TaskFormUiState(
    val title: String = "",
    val notes: String = "",
    val hasDueDate: Boolean = false,
    val dueDate: Instant = Instant.now(),
    val priority: Priority = Priority.MEDIUM,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean get() = title.trim().isNotEmpty()
}

/**
 * Form ViewModel — LiveData flavour. Form fields live in a single
 * [TaskFormUiState] `LiveData`; the successful save is surfaced as a one-shot
 * `LiveData<Event<TaskItem>>` (see [Event]) so the fragment can navigate back
 * without re-firing on a config change. Talks to the repository directly.
 */
class TaskFormViewModel(
    val mode: TaskFormMode,
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(initialState(mode))
    val uiState: LiveData<TaskFormUiState> = _uiState

    private val _saved = MutableLiveData<Event<TaskItem>>()
    val saved: LiveData<Event<TaskItem>> = _saved

    val screenTitle: String
        get() = when (mode) {
            TaskFormMode.Create -> "New Task"
            is TaskFormMode.Edit -> "Edit Task"
        }

    private val current: TaskFormUiState get() = _uiState.value!!

    fun setTitle(value: String)       { _uiState.value = current.copy(title = value) }
    fun setNotes(value: String)       { _uiState.value = current.copy(notes = value) }
    fun setHasDueDate(value: Boolean) { _uiState.value = current.copy(hasDueDate = value) }
    fun setDueDate(value: Instant)    { _uiState.value = current.copy(dueDate = value) }
    fun setPriority(value: Priority)  { _uiState.value = current.copy(priority = value) }
    fun clearError()                  { _uiState.value = current.copy(errorMessage = null) }

    fun save() {
        val state = current
        if (!state.isValid) return

        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val trimmedTitle = state.title.trim()
                val trimmedNotes = state.notes.trim().takeIf { it.isNotEmpty() }
                val resolvedDueDate = state.dueDate.takeIf { state.hasDueDate }

                val saved = when (mode) {
                    TaskFormMode.Create -> repository.create(
                        TaskItem(
                            title = trimmedTitle,
                            notes = trimmedNotes,
                            dueDate = resolvedDueDate,
                            priority = state.priority
                        )
                    )
                    is TaskFormMode.Edit -> repository.update(
                        mode.task.copy(
                            title = trimmedTitle,
                            notes = trimmedNotes,
                            dueDate = resolvedDueDate,
                            priority = state.priority
                        )
                    )
                }
                _saved.value = Event(saved)
            } catch (e: Exception) {
                _uiState.value = current.copy(errorMessage = e.message)
            } finally {
                _uiState.value = current.copy(isSaving = false)
            }
        }
    }

    private fun initialState(mode: TaskFormMode): TaskFormUiState = when (mode) {
        TaskFormMode.Create -> TaskFormUiState()
        is TaskFormMode.Edit -> {
            val t = mode.task
            TaskFormUiState(
                title = t.title,
                notes = t.notes.orEmpty(),
                hasDueDate = t.dueDate != null,
                dueDate = t.dueDate ?: Instant.now(),
                priority = t.priority
            )
        }
    }
}
