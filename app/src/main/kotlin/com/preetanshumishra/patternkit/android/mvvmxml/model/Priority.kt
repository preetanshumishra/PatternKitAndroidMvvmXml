package com.preetanshumishra.patternkit.android.mvvmxml.model

enum class Priority(val displayName: String, val sortOrder: Int) {
    HIGH("High", 0),
    MEDIUM("Medium", 1),
    LOW("Low", 2);
}
