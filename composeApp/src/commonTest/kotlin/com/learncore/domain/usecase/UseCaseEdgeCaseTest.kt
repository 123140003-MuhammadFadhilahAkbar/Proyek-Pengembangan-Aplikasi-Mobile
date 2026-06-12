package com.learncore.domain.usecase

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UseCaseEdgeCaseTest {

    private val repo = FakeEdgeCaseRepo()

    // ===== GetAllTasksUseCase =====

    @Test
    fun `getAllTasks returns empty list when no tasks`() = runTest {
        val result = GetAllTasksUseCase(repo)().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllTasks returns all tasks`() = runTest {
        repo.tasks.value = listOf(
            Task(id = 1, title = "T1"),
            Task(id = 2, title = "T2"),
            Task(id = 3, title = "T3")
        )
        val result = GetAllTasksUseCase(repo)().first()
        assertEquals(3, result.size)
    }

    // ===== GetActiveTasksUseCase =====

    @Test
    fun `getActiveTasks excludes completed tasks`() = runTest {
        repo.tasks.value = listOf(
            Task(id = 1, title = "Active"),
            Task(id = 2, title = "Done", isCompleted = true)
        )
        val result = GetActiveTasksUseCase(repo)().first()
        assertEquals(1, result.size)
        assertEquals("Active", result.first().title)
    }

    @Test
    fun `getActiveTasks returns empty when all done`() = runTest {
        repo.tasks.value = listOf(
            Task(id = 1, title = "Done 1", isCompleted = true),
            Task(id = 2, title = "Done 2", isCompleted = true)
        )
        val result = GetActiveTasksUseCase(repo)().first()
        assertTrue(result.isEmpty())
    }

    // ===== GetTasksByQuadrantUseCase =====

    @Test
    fun `getTasksByQuadrant filters correctly`() = runTest {
        repo.tasks.value = listOf(
            Task(id = 1, title = "Urgent", quadrant = EisenhowerQuadrant.DO_FIRST),
            Task(id = 2, title = "Scheduled", quadrant = EisenhowerQuadrant.SCHEDULE)
        )
        val result = GetTasksByQuadrantUseCase(repo)(EisenhowerQuadrant.DO_FIRST).first()
        assertEquals(1, result.size)
        assertEquals("Urgent", result.first().title)
    }

    @Test
    fun `getTasksByQuadrant returns empty for quadrant with no tasks`() = runTest {
        repo.tasks.value = listOf(Task(id = 1, title = "T", quadrant = EisenhowerQuadrant.DO_FIRST))
        val result = GetTasksByQuadrantUseCase(repo)(EisenhowerQuadrant.ELIMINATE).first()
        assertTrue(result.isEmpty())
    }

    // ===== GetTaskByIdUseCase =====

    @Test
    fun `getTaskById returns null for non-existent id`() = runTest {
        repo.tasks.value = listOf(Task(id = 1, title = "T"))
        val result = GetTaskByIdUseCase(repo)(999L).first()
        assertNull(result)
    }

    @Test
    fun `getTaskById returns correct task`() = runTest {
        repo.tasks.value = listOf(
            Task(id = 1, title = "Task One"),
            Task(id = 2, title = "Task Two")
        )
        val result = GetTaskByIdUseCase(repo)(2L).first()
        assertEquals("Task Two", result?.title)
    }

    // ===== ToggleTaskCompletionUseCase =====

    @Test
    fun `toggleTaskCompletion flips completion status`() = runTest {
        repo.tasks.value = listOf(Task(id = 1, title = "Toggle Me", isCompleted = false))
        ToggleTaskCompletionUseCase(repo)(1L)
        val task = repo.tasks.value.first { it.id == 1L }
        assertTrue(task.isCompleted)
    }

    @Test
    fun `toggleTaskCompletion twice returns to original`() = runTest {
        repo.tasks.value = listOf(Task(id = 1, title = "Toggle Me", isCompleted = false))
        ToggleTaskCompletionUseCase(repo)(1L)
        ToggleTaskCompletionUseCase(repo)(1L)
        val task = repo.tasks.value.first { it.id == 1L }
        assertTrue(!task.isCompleted)
    }

    // ===== DeleteTaskUseCase =====

    @Test
    fun `deleteTask removes task from repository`() = runTest {
        repo.tasks.value = listOf(
            Task(id = 1, title = "Delete Me"),
            Task(id = 2, title = "Keep Me")
        )
        DeleteTaskUseCase(repo)(1L)
        assertEquals(1, repo.tasks.value.size)
        assertEquals("Keep Me", repo.tasks.value.first().title)
    }

    @Test
    fun `deleteTask on non-existent id is safe`() = runTest {
        repo.tasks.value = listOf(Task(id = 1, title = "Keep"))
        DeleteTaskUseCase(repo)(999L)
        assertEquals(1, repo.tasks.value.size)
    }

    // ===== GetProductivityStatsUseCase =====

    @Test
    fun `getProductivityStats returns stats from repo`() = runTest {
        val stats = ProductivityStats(totalTasks = 10, completedTasks = 7)
        repo.stats = stats
        val result = GetProductivityStatsUseCase(repo)()
        assertEquals(10, result.totalTasks)
        assertEquals(7, result.completedTasks)
    }

    // ===== RecordPomodoroSessionUseCase =====

    @Test
    fun `recordPomodoroSession calls repository`() = runTest {
        RecordPomodoroSessionUseCase(repo)(taskId = 1L, durationSeconds = 1500)
        assertTrue(repo.pomodoroRecorded)
        assertEquals(1L, repo.lastPomodoroTaskId)
        assertEquals(1500, repo.lastPomodoroDuration)
    }

    @Test
    fun `recordPomodoroSession with null taskId works`() = runTest {
        RecordPomodoroSessionUseCase(repo)(taskId = null, durationSeconds = 300)
        assertTrue(repo.pomodoroRecorded)
        assertNull(repo.lastPomodoroTaskId)
    }
}

class FakeEdgeCaseRepo : TaskRepository {
    val tasks = MutableStateFlow<List<Task>>(emptyList())
    var stats = ProductivityStats()
    var pomodoroRecorded = false
    var lastPomodoroTaskId: Long? = null
    var lastPomodoroDuration: Int = 0

    override fun getAllTasks(): Flow<List<Task>> = tasks
    override fun getTasksByQuadrant(q: EisenhowerQuadrant): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.quadrant == q } }
    override fun getActiveTasks(): Flow<List<Task>> =
        tasks.map { list -> list.filter { !it.isCompleted } }
    override fun getTaskById(id: Long): Flow<Task?> =
        tasks.map { list -> list.find { it.id == id } }
    override suspend fun insertTask(task: Task): Long {
        tasks.value = tasks.value + task
        return task.id
    }
    override suspend fun updateTask(task: Task) {
        tasks.value = tasks.value.map { if (it.id == task.id) task else it }
    }
    override suspend fun toggleTaskCompletion(id: Long) {
        tasks.value = tasks.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }
    override suspend fun deleteTask(id: Long) {
        tasks.value = tasks.value.filter { it.id != id }
    }
    override suspend fun getProductivityStats(): ProductivityStats = stats
    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) {
        pomodoroRecorded = true
        lastPomodoroTaskId = taskId
        lastPomodoroDuration = durationSeconds
    }
}
