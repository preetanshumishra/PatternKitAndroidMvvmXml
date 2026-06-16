package com.preetanshumishra.patternkit.android.mvvmxml.di

import com.preetanshumishra.patternkit.android.mvvmxml.data.TaskRepository
import dagger.Component
import javax.inject.Singleton

/**
 * Application-scoped Dagger graph. With no domain layer, the only thing the
 * UI needs from the graph is the repository — the `ViewModelFactory` pulls it
 * out and hands it to each ViewModel. (Contrast the MVVM+Clean module, where
 * the component exposed five use cases instead.)
 */
@Singleton
@Component(modules = [RepositoryModule::class])
interface AppComponent {
    fun taskRepository(): TaskRepository
}
