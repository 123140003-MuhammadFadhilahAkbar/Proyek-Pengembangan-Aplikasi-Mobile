package com.learncore.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Task(
    val id: Long = 0,
    val title: String,
    val category: String = "",
    val description: String = "",
    val quadrant: EisenhowerQuadrant = EisenhowerQuadrant.DO_FIRST,
    val isCompleted: Boolean = false,
    val deadline: Instant? = null,
    val reminderMinutes: Int? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    val isEmpty: Boolean
        get() = title.isBlank()
}

enum class EisenhowerQuadrant(
    val displayName: String,
    val action: String,
    val description: String,
    val isUrgent: Boolean,
    val isImportant: Boolean
) {
    DO_FIRST(
        displayName = "Do First",
        action = "Do First",
        description = "Urgent & Important",
        isUrgent = true,
        isImportant = true
    ),
    SCHEDULE(
        displayName = "Schedule",
        action = "Schedule",
        description = "Not Urgent & Important",
        isUrgent = false,
        isImportant = true
    ),
    DELEGATE(
        displayName = "Delegate",
        action = "Delegate",
        description = "Urgent & Not Important",
        isUrgent = true,
        isImportant = false
    ),
    ELIMINATE(
        displayName = "Eliminate",
        action = "Eliminate",
        description = "Not Urgent & Not Important",
        isUrgent = false,
        isImportant = false
    );

    companion object {
        fun fromString(value: String): EisenhowerQuadrant {
            return entries.find { it.name == value } ?: DO_FIRST
        }
    }
}

data class ProductivityStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalFocusSeconds: Long = 0,
    val pomodoroSessions: Int = 0,
    val tasksByQuadrant: Map<EisenhowerQuadrant, Int> = emptyMap()
) {
    val completionRate: Float
        get() = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks

    val totalFocusMinutes: Long
        get() = totalFocusSeconds / 60
}
