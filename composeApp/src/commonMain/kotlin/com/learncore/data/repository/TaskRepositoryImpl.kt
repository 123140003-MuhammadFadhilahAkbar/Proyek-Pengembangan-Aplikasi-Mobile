package com.learncore.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.learncore.data.local.LearnCoreDatabase
import com.learncore.data.local.entity.toDomain
import com.learncore.data.local.entity.toDomainList
import com.learncore.data.local.entity.toEntityValues
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class TaskRepositoryImpl(private val database: LearnCoreDatabase) : TaskRepository {

    private val queries = database.learnCoreQueries

    override fun getAllTasks(): Flow<List<Task>> {
        return queries.getAllTasks()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.toDomainList() }
    }

    override fun getTasksByQuadrant(quadrant: EisenhowerQuadrant): Flow<List<Task>> {
        return queries.getTasksByQuadrant(quadrant.name)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.toDomainList() }
    }

    override fun getActiveTasks(): Flow<List<Task>> {
        return queries.getActiveTasks()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.toDomainList() }
    }

    override fun getTaskById(id: Long): Flow<Task?> {
        return queries.getTaskById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override suspend fun insertTask(task: Task): Long = withContext(Dispatchers.Default) {
        val v = task.toEntityValues()
        queries.insertTask(
            title = v.title,
            category = v.category,
            description = v.description,
            quadrant = v.quadrant,
            is_completed = v.isCompleted,
            deadline = v.deadline,
            created_at = v.createdAt,
            updated_at = v.updatedAt
        )
        queries.lastInsertId().executeAsOne()
    }

    override suspend fun updateTask(task: Task) = withContext(Dispatchers.Default) {
        val v = task.toEntityValues()
        queries.updateTask(
            id = task.id,
            title = v.title,
            category = v.category,
            description = v.description,
            quadrant = v.quadrant,
            deadline = v.deadline,
            updated_at = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun toggleTaskCompletion(id: Long) = withContext(Dispatchers.Default) {
        queries.toggleTaskCompletion(
            updated_at = Clock.System.now().toEpochMilliseconds(),
            id = id
        )
    }

    override suspend fun deleteTask(id: Long) = withContext(Dispatchers.Default) {
        queries.deleteTaskById(id)
    }

    override suspend fun getProductivityStats(): ProductivityStats = withContext(Dispatchers.Default) {
        val totalTasks = queries.countTotal().executeAsOne().toInt()
        val completedTasks = queries.countCompleted().executeAsOne().toInt()
        val totalFocusSeconds = queries.getTotalFocusSeconds().executeAsOne()
        val pomodoroSessions = queries.getSessionsCount().executeAsOne().toInt()

        val tasksByQuadrant = mutableMapOf<EisenhowerQuadrant, Int>()
        queries.countByQuadrant().executeAsList().forEach { row ->
            val quadrant = EisenhowerQuadrant.fromString(row.quadrant)
            tasksByQuadrant[quadrant] = row.count.toInt()
        }

        ProductivityStats(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            totalFocusSeconds = totalFocusSeconds,
            pomodoroSessions = pomodoroSessions,
            tasksByQuadrant = tasksByQuadrant
        )
    }

    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) =
        withContext(Dispatchers.Default) {
            queries.insertPomodoroSession(
                task_id = taskId,
                duration_seconds = durationSeconds.toLong(),
                completed_at = Clock.System.now().toEpochMilliseconds()
            )
        }
}
