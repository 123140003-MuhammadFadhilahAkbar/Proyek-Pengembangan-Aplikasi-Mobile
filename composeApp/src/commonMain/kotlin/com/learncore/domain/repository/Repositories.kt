package com.learncore.domain.repository

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository { 
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByQuadrant(quadrant: EisenhowerQuadrant): Flow<List<Task>>
    fun getActiveTasks(): Flow<List<Task>>
    fun getTaskById(id: Long): Flow<Task?>
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun toggleTaskCompletion(id: Long)
    suspend fun deleteTask(id: Long)
    suspend fun getProductivityStats(): ProductivityStats
    suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int)
}

interface AIRepository {
    suspend fun generateResponse(prompt: String, systemPrompt: String? = null): Result<String>
}
