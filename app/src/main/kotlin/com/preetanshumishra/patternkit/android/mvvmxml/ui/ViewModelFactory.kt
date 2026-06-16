package com.preetanshumishra.patternkit.android.mvvmxml.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.preetanshumishra.patternkit.android.mvvmxml.di.AppComponent
import com.preetanshumishra.patternkit.android.mvvmxml.ui.list.TaskListViewModel
import com.preetanshumishra.patternkit.android.mvvmxml.ui.form.TaskFormViewModel
import com.preetanshumishra.patternkit.android.mvvmxml.ui.form.TaskFormMode

/**
 * Hand-rolled VM factory — explicit composition root for the UI layer.
 * Pulls the single dependency (the repository) out of the Dagger graph and
 * hands it to each ViewModel. With no use-case layer there is nothing else
 * to wire.
 */
class ViewModelFactory(
    private val component: AppComponent,
    private val formMode: TaskFormMode? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(TaskListViewModel::class.java) ->
                TaskListViewModel(repository = component.taskRepository()) as T

            modelClass.isAssignableFrom(TaskFormViewModel::class.java) ->
                TaskFormViewModel(
                    mode = formMode ?: TaskFormMode.Create,
                    repository = component.taskRepository()
                ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        /** Convenience for screens that don't need a form-mode argument. */
        fun forList(component: AppComponent) = ViewModelFactory(component)

        /** Convenience for form screens, supplying the create-vs-edit mode. */
        fun forForm(component: AppComponent, mode: TaskFormMode) =
            ViewModelFactory(component, mode)
    }
}
