package com.preetanshumishra.patternkit.android.mvvmxml.model

import java.time.Instant
import java.util.UUID

/**
 * Plain model entity. No framework types — the data layer maps any DTO/Room
 * entities to this; the UI layer renders it.
 */
data class TaskItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val notes: String? = null,
    val dueDate: Instant? = null,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
