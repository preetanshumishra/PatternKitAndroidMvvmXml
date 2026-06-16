package com.preetanshumishra.patternkit.android.mvvmxml.ui

/**
 * Wraps a value so a LiveData can model a one-shot "event" (navigate, show a
 * snackbar, …) instead of a state. The classic LiveData answer to the
 * single-event problem — once consumed it won't fire again on a config-change
 * re-subscription. The Compose/StateFlow modules use a Channel for the same job.
 */
class Event<out T>(private val content: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? =
        if (handled) null else { handled = true; content }
}
