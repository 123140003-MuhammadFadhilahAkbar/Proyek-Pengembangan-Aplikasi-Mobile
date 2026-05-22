package com.learncore.data.local.entity

import com.learncore.data.local.TaskEntity
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import kotlinx.datetime.Instant

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    category = category,
    description = description,
    quadrant = EisenhowerQuadrant.fromString(quadrant),
    isCompleted = is_completed != 0L,
    deadline = deadline?.let { Instant.fromEpochMilliseconds(it) },
    createdAt = Instant.fromEpochMilliseconds(created_at),
    updatedAt = Instant.fromEpochMilliseconds(updated_at)
)

fun List<TaskEntity>.toDomainList(): List<Task> = map { it.toDomain() }

data class TaskEntityValues(
    val title: String,
    val category: String,
    val description: String,
    val quadrant: String,
    val isCompleted: Long,
    val deadline: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

fun Task.toEntityValues(): TaskEntityValues = TaskEntityValues(
    title = title,
    category = category,
    description = description,
    quadrant = quadrant.name,
    isCompleted = if (isCompleted) 1L else 0L,
    deadline = deadline?.toEpochMilliseconds(),
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds()
)
