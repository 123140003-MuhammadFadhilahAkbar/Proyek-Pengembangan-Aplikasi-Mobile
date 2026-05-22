package com.learncore.domain.usecase

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ==================== FAKE REPOSITORY ====================

class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1L

    override fun getAllTasks(): Flow<List<Task>> = flowOf(tasks.toList())

    override fun getTasksByQuadrant(quadrant: EisenhowerQuadrant): Flow<List<Task>> =
        flowOf(tasks.filter { it.quadrant == quadrant })

    override fun getActiveTasks(): Flow<List<Task>> =
        flowOf(tasks.filter { !it.isCompleted })

    override fun getTaskById(id: Long): Flow<Task?> =
        flowOf(tasks.find { it.id == id })

    override suspend fun insertTask(task: Task): Long {
        val id = nextId++
        tasks.add(task.copy(id = id))
        return id
    }

    override suspend fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) tasks[index] = task
    }

    override suspend fun toggleTaskCompletion(id: Long) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            tasks[index] = tasks[index].copy(isCompleted = !tasks[index].isCompleted)
        }
    }

    override suspend fun deleteTask(id: Long) {
        tasks.removeAll { it.id == id }
    }

    override suspend fun getProductivityStats(): ProductivityStats = ProductivityStats(
        totalTasks = tasks.size,
        completedTasks = tasks.count { it.isCompleted },
        totalFocusSeconds = 3600,
        pomodoroSessions = 4,
        tasksByQuadrant = EisenhowerQuadrant.entries.associateWith { q ->
            tasks.count { it.quadrant == q && !it.isCompleted }
        }
    )

    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) { /* no-op */ }
}

// ==================== TESTS ====================

class SaveTaskUseCaseTest {

    private val repository = FakeTaskRepository()
    private val saveTask = SaveTaskUseCase(repository)

    @Test
    fun `save task with valid title succeeds`() = runTest {
        val task = Task(title = "Study Kotlin", quadrant = EisenhowerQuadrant.DO_FIRST)
        val result = saveTask(task)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `save task with blank title fails`() = runTest {
        val task = Task(title = "  ", quadrant = EisenhowerQuadrant.DO_FIRST)
        val result = saveTask(task)
        assertTrue(result.isFailure)
    }

    @Test
    fun `save task returns valid id`() = runTest {
        val task = Task(title = "Complete assignment")
        val result = saveTask(task)
        assertTrue(result.getOrNull()!! > 0)
    }

    @Test
    fun `update existing task (id != 0) calls updateTask`() = runTest {
        val insertTask = Task(title = "Original")
        val id = saveTask(insertTask).getOrThrow()

        val updatedTask = Task(id = id, title = "Updated")
        val result = saveTask(updatedTask)
        assertTrue(result.isSuccess)
        assertEquals(id, result.getOrNull())
    }
}

class GetAllTasksUseCaseTest {
    private val repository = FakeTaskRepository()
    private val getAllTasks = GetAllTasksUseCase(repository)
    private val saveTask = SaveTaskUseCase(repository)

    @Test
    fun `returns empty list initially`() = runTest {
        val tasks = getAllTasks().first()
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun `returns all tasks after inserts`() = runTest {
        saveTask(Task(title = "Task 1"))
        saveTask(Task(title = "Task 2"))
        saveTask(Task(title = "Task 3"))
        val tasks = getAllTasks().first()
        assertEquals(3, tasks.size)
    }
}

class GetTasksByQuadrantUseCaseTest {
    private val repository = FakeTaskRepository()
    private val getByQuadrant = GetTasksByQuadrantUseCase(repository)
    private val saveTask = SaveTaskUseCase(repository)

    @Test
    fun `filters tasks by quadrant correctly`() = runTest {
        saveTask(Task(title = "Urgent task", quadrant = EisenhowerQuadrant.DO_FIRST))
        saveTask(Task(title = "Scheduled task", quadrant = EisenhowerQuadrant.SCHEDULE))
        saveTask(Task(title = "Another urgent", quadrant = EisenhowerQuadrant.DO_FIRST))

        val q1Tasks = getByQuadrant(EisenhowerQuadrant.DO_FIRST).first()
        assertEquals(2, q1Tasks.size)
        assertTrue(q1Tasks.all { it.quadrant == EisenhowerQuadrant.DO_FIRST })
    }

    @Test
    fun `returns empty list for quadrant with no tasks`() = runTest {
        saveTask(Task(title = "A task", quadrant = EisenhowerQuadrant.DO_FIRST))
        val tasks = getByQuadrant(EisenhowerQuadrant.ELIMINATE).first()
        assertTrue(tasks.isEmpty())
    }
}

class DeleteTaskUseCaseTest {
    private val repository = FakeTaskRepository()
    private val deleteTask = DeleteTaskUseCase(repository)
    private val saveTask = SaveTaskUseCase(repository)
    private val getAllTasks = GetAllTasksUseCase(repository)

    @Test
    fun `deleting a task removes it from repository`() = runTest {
        val id = saveTask(Task(title = "To delete")).getOrThrow()
        deleteTask(id)
        val tasks = getAllTasks().first()
        assertFalse(tasks.any { it.id == id })
    }
}

class ToggleTaskCompletionUseCaseTest {
    private val repository = FakeTaskRepository()
    private val toggle = ToggleTaskCompletionUseCase(repository)
    private val saveTask = SaveTaskUseCase(repository)
    private val getTaskById = GetTaskByIdUseCase(repository)

    @Test
    fun `toggling incomplete task marks it complete`() = runTest {
        val id = saveTask(Task(title = "Incomplete task")).getOrThrow()
        toggle(id)
        val task = getTaskById(id).first()
        assertTrue(task?.isCompleted == true)
    }

    @Test
    fun `toggling complete task marks it incomplete`() = runTest {
        val id = saveTask(Task(title = "Already done")).getOrThrow()
        toggle(id) // mark complete
        toggle(id) // mark incomplete
        val task = getTaskById(id).first()
        assertFalse(task?.isCompleted == true)
    }
}

class GetProductivityStatsUseCaseTest {
    private val repository = FakeTaskRepository()
    private val getStats = GetProductivityStatsUseCase(repository)
    private val saveTask = SaveTaskUseCase(repository)
    private val toggle = ToggleTaskCompletionUseCase(repository)

    @Test
    fun `stats reflect correct total and completed counts`() = runTest {
        val id1 = saveTask(Task(title = "T1")).getOrThrow()
        val id2 = saveTask(Task(title = "T2")).getOrThrow()
        saveTask(Task(title = "T3"))
        toggle(id1)
        toggle(id2)

        val stats = getStats()
        assertEquals(3, stats.totalTasks)
        assertEquals(2, stats.completedTasks)
    }

    @Test
    fun `completion rate is computed correctly`() = runTest {
        val id = saveTask(Task(title = "Done")).getOrThrow()
        saveTask(Task(title = "Pending"))
        toggle(id)

        val stats = getStats()
        assertEquals(0.5f, stats.completionRate, absoluteTolerance = 0.01f)
    }
}

class EisenhowerQuadrantTest {

    @Test
    fun `fromString returns correct quadrant`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, EisenhowerQuadrant.fromString("DO_FIRST"))
        assertEquals(EisenhowerQuadrant.SCHEDULE, EisenhowerQuadrant.fromString("SCHEDULE"))
        assertEquals(EisenhowerQuadrant.DELEGATE, EisenhowerQuadrant.fromString("DELEGATE"))
        assertEquals(EisenhowerQuadrant.ELIMINATE, EisenhowerQuadrant.fromString("ELIMINATE"))
    }

    @Test
    fun `fromString with unknown value returns DO_FIRST as default`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, EisenhowerQuadrant.fromString("UNKNOWN"))
    }

    @Test
    fun `DO_FIRST is urgent and important`() {
        assertTrue(EisenhowerQuadrant.DO_FIRST.isUrgent)
        assertTrue(EisenhowerQuadrant.DO_FIRST.isImportant)
    }

    @Test
    fun `SCHEDULE is not urgent but important`() {
        assertFalse(EisenhowerQuadrant.SCHEDULE.isUrgent)
        assertTrue(EisenhowerQuadrant.SCHEDULE.isImportant)
    }

    @Test
    fun `ELIMINATE is not urgent and not important`() {
        assertFalse(EisenhowerQuadrant.ELIMINATE.isUrgent)
        assertFalse(EisenhowerQuadrant.ELIMINATE.isImportant)
    }
}
