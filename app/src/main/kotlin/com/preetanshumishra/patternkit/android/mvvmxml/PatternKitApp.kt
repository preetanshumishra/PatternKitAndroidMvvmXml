package com.preetanshumishra.patternkit.android.mvvmxml

import android.app.Application
import com.preetanshumishra.patternkit.android.mvvmxml.di.AppComponent
import com.preetanshumishra.patternkit.android.mvvmxml.di.DaggerAppComponent

class PatternKitApp : Application() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.create()
    }
}
