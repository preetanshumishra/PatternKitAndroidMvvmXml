package com.preetanshumishra.patternkit.android.mvvmxml.di

import com.preetanshumishra.patternkit.android.mvvmxml.data.MockTaskRepository
import com.preetanshumishra.patternkit.android.mvvmxml.data.TaskRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * Binds the repository contract to its concrete mock impl.
 * Swap this binding to point at a real (Retrofit-backed) repository
 * later — the rest of the app is unaffected.
 */
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: MockTaskRepository): TaskRepository
}
