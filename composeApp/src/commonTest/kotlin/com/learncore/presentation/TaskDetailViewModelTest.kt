package com.learncore.presentation

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.DeleteTaskUseCase
import com.learncore.domain.usecase.GetTaskByIdUseCase
import com.learncore.domain.usecase.ToggleTaskCompletionUseCase
import com.learncore.presentation.screens.tasks.TaskDetailUiState
import com.learncore.presentation.screens.tasks.TaskDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeTaskDetailRepository
    private lateinit var viewModel: TaskDetailViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeTaskDetailRepository()
        viewModel = TaskDetailViewModel(
            getTaskByIdUseCase = GetTaskByIdUseCase(repo),
            toggleTaskCompletionUseCase = ToggleTaskCompletionUseCase(repo),
            deleteTaskUseCase = DeleteTaskUseCase(repo)
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        assertIs<TaskDetailUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `loadTask with valid id shows task in Success state`() = runTest {
        val task = Task(id = 1L, title = "Detail Task", quadrant = EisenhowerQuadrant.DO_FIRST)
        repo.addTask(task)
        viewModel.loadTask(1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<TaskDetailUiState.Success>(state)
        assertEquals("Detail Task", (state as TaskDetailUiState.Success).task.title)
    }

    @Test
    fun `loadTask with invalid id shows Error state`() = runTest {
        viewModel.loadTask(999L)
        advanceUntilIdle()
        assertIs<TaskDetailUiState.Error>(viewModel.uiState.value)
    }

    @Test
    fun `deleteTask sets deleted to true`() = runTest {
        val task = Task(id = 1L, title = "Hapus Ini")
        repo.addTask(task)
        viewModel.loadTask(1L)
        advanceUntilIdle()

        viewModel.deleteTask(1L)
        advanceUntilIdle()

        assertTrue(viewModel.deleted.value)
    }

    @Test
    fun `toggleCompletion updates task status`() = runTest {
        val task = Task(id = 1L, title = "Toggle Task", isCompleted = false)
        repo.addTask(task)
        viewModel.loadTask(1L)
        advanceUntilIdle()

        viewModel.toggleCompletion(1L)
        advanceUntilIdle()

        // Verifikasi di repository bahwa task sudah ter-toggle
        assertTrue(repo.isCompleted(1L))
    }

    @Test
    fun `initial deleted state is false`() {
        assertTrue(!viewModel.deleted.value)
    }
}

class FakeTaskDetailRepository : TaskRepository {
    // Gunakan MutableStateFlow agar reaktif setelah toggle/delete
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    fun addTask(task: Task) { tasksFlow.value = tasksFlow.value + task }

    fun isCompleted(id: Long): Boolean =
        tasksFlow.value.find { it.id == id }?.isCompleted ?: false

    override fun getAllTasks(): Flow<List<Task>> = tasksFlow
    override fun getTasksByQuadrant(q: EisenhowerQuadrant): Flow<List<Task>> = flowOf(emptyList())
    override fun getActiveTasks(): Flow<List<Task>> = flowOf(emptyList())

    // Reaktif: map dari tasksFlow sehingga auto-update setelah toggle
    override fun getTaskById(id: Long): Flow<Task?> =
        tasksFlow.map { list -> list.find { it.id == id } }

    override suspend fun insertTask(task: Task): Long {
        tasksFlow.value = tasksFlow.value + task
        return task.id
    }
    override suspend fun updateTask(task: Task) {
        tasksFlow.value = tasksFlow.value.map { if (it.id == task.id) task else it }
    }
    override suspend fun toggleTaskCompletion(id: Long) {
        tasksFlow.value = tasksFlow.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }
    override suspend fun deleteTask(id: Long) {
        tasksFlow.value = tasksFlow.value.filter { it.id != id }
    }
    override suspend fun getProductivityStats(): ProductivityStats = ProductivityStats()
    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) {}
}
