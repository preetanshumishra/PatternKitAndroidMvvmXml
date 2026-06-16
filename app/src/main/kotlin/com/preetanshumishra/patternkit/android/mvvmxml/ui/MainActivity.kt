package com.preetanshumishra.patternkit.android.mvvmxml.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.preetanshumishra.patternkit.android.mvvmxml.R

/**
 * Single-activity host. The whole screen flow lives in `nav_graph.xml`, driven
 * by the `NavHostFragment` declared in `activity_main.xml`. Fragments reach the
 * Dagger graph via `(application as PatternKitApp).component`.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
