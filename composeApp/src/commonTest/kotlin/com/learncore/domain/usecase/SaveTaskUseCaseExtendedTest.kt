package com.learncore.domain.usecase

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveTaskUseCaseExtendedTest {

    private val repo = FakeSaveRepo()
    private val useCase = SaveTaskUseCase(repo)

    @Test
    fun `insert new task returns success with id`() = runTest {
        val task = Task(id = 0L, title = "New Task")
        val result = useCase(task)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `insert task with id 0 calls insertTask`() = runTest {
        val task = Task(id = 0L, title = "Insert Me")
        useCase(task)
        assertTrue(repo.insertCalled)
    }

    @Test
    fun `update existing task calls updateTask`() = runTest {
        val task = Task(id = 5L, title = "Update Me")
        useCase(task)
        assertTrue(repo.updateCalled)
    }

    @Test
    fun `blank title returns failure`() = runTest {
        val task = Task(title = "")
        val result = useCase(task)
        assertTrue(result.isFailure)
    }

    @Test
    fun `blank title error message is correct`() = runTest {
        val task = Task(title = "  ")
        val result = useCase(task)
        assertTrue(result.exceptionOrNull()?.message?.contains("empty") == true)
    }

    @Test
    fun `task with all fields saves correctly`() = runTest {
        val task = Task(
            id = 0L,
            title = "Full Task",
            category = "Kuliah",
            description = "Belajar Kotlin",
            quadrant = EisenhowerQuadrant.DO_FIRST,
            reminderMinutes = 30
        )
        val result = useCase(task)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `update returns task id on success`() = runTest {
        val task = Task(id = 42L, title = "Update Task")
        val result = useCase(task)
        assertEquals(42L, result.getOrNull())
    }

    @Test
    fun `title with only spaces is rejected`() = runTest {
        val task = Task(title = "   ")
        val result = useCase(task)
        assertTrue(result.isFailure)
    }

    @Test
    fun `title with single character is accepted`() = runTest {
        val task = Task(title = "A")
        val result = useCase(task)
        assertTrue(result.isSuccess)
    }
}

class FakeSaveRepo : TaskRepository {
    var insertCalled = false
    var updateCalled = false
    private val flow = MutableStateFlow<List<Task>>(emptyList())

    override fun getAllTasks(): Flow<List<Task>> = flow
    override fun getTasksByQuadrant(q: EisenhowerQuadrant): Flow<List<Task>> = flowOf(emptyList())
    override fun getActiveTasks(): Flow<List<Task>> = flowOf(emptyList())
    override fun getTaskById(id: Long): Flow<Task?> = flowOf(null)
    override suspend fun insertTask(task: Task): Long {
        insertCalled = true
        return 99L
    }
    override suspend fun updateTask(task: Task) { updateCalled = true }
    override suspend fun toggleTaskCompletion(id: Long) {}
    override suspend fun deleteTask(id: Long) {}
    override suspend fun getProductivityStats(): ProductivityStats = ProductivityStats()
    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) {}
}
