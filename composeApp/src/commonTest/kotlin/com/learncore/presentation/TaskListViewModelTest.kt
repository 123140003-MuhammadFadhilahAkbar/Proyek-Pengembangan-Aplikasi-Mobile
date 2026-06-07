package com.learncore.presentation

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.DeleteTaskUseCase
import com.learncore.domain.usecase.GetAllTasksUseCase
import com.learncore.domain.usecase.ToggleTaskCompletionUseCase
import com.learncore.presentation.screens.tasks.TaskListUiState
import com.learncore.presentation.screens.tasks.TaskListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeTaskListRepository
    private lateinit var viewModel: TaskListViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeTaskListRepository()
        viewModel = TaskListViewModel(
            getAllTasksUseCase = GetAllTasksUseCase(repo),
            toggleTaskCompletionUseCase = ToggleTaskCompletionUseCase(repo),
            deleteTaskUseCase = DeleteTaskUseCase(repo)
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    // Helper: aktifkan StateFlow (WhileSubscribed butuh subscriber)
    private fun kotlinx.coroutines.test.TestScope.collectUiState() {
        backgroundScope.launch { viewModel.uiState.collect {} }
    }

    @Test
    fun `initial state is Loading before tasks emitted`() = runTest {
        // Tanpa advanceUntilIdle, coroutine loadTasks belum jalan -> state masih Loading
        assertIs<TaskListUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `state becomes Success after tasks loaded`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(Task(id = 1, title = "Task 1")))
        advanceUntilIdle()
        assertIs<TaskListUiState.Success>(viewModel.uiState.value)
    }

    @Test
    fun `success state contains all tasks when no filter`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(
            Task(id = 1, title = "T1", quadrant = EisenhowerQuadrant.DO_FIRST),
            Task(id = 2, title = "T2", quadrant = EisenhowerQuadrant.SCHEDULE)
        ))
        advanceUntilIdle()
        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun `setQuadrantFilter filters tasks by quadrant`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(
            Task(id = 1, title = "Urgent", quadrant = EisenhowerQuadrant.DO_FIRST),
            Task(id = 2, title = "Scheduled", quadrant = EisenhowerQuadrant.SCHEDULE),
            Task(id = 3, title = "Urgent 2", quadrant = EisenhowerQuadrant.DO_FIRST)
        ))
        advanceUntilIdle()
        viewModel.setQuadrantFilter(EisenhowerQuadrant.DO_FIRST)
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(2, state.tasks.size)
        assertTrue(state.tasks.all { it.quadrant == EisenhowerQuadrant.DO_FIRST })
    }

    @Test
    fun `setQuadrantFilter null shows all tasks`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(
            Task(id = 1, title = "T1", quadrant = EisenhowerQuadrant.DO_FIRST),
            Task(id = 2, title = "T2", quadrant = EisenhowerQuadrant.DELEGATE)
        ))
        advanceUntilIdle()
        viewModel.setQuadrantFilter(EisenhowerQuadrant.DO_FIRST)
        advanceUntilIdle()
        viewModel.setQuadrantFilter(null)
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(2, state.tasks.size)
        assertNull(state.selectedQuadrant)
    }

    @Test
    fun `setSearchQuery filters by title`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(
            Task(id = 1, title = "Belajar Kotlin"),
            Task(id = 2, title = "Olahraga pagi"),
            Task(id = 3, title = "Belajar Compose")
        ))
        advanceUntilIdle()
        viewModel.setSearchQuery("Belajar")
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(2, state.tasks.size)
        assertTrue(state.tasks.all { it.title.contains("Belajar", ignoreCase = true) })
    }

    @Test
    fun `setSearchQuery case insensitive`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(Task(id = 1, title = "Belajar Kotlin")))
        advanceUntilIdle()
        viewModel.setSearchQuery("belajar")
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(1, state.tasks.size)
    }

    @Test
    fun `setSearchQuery empty string shows all tasks`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(
            Task(id = 1, title = "Task A"),
            Task(id = 2, title = "Task B")
        ))
        advanceUntilIdle()
        viewModel.setSearchQuery("Task A")
        advanceUntilIdle()
        viewModel.setSearchQuery("")
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun `deleteTask removes task from list`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(
            Task(id = 1, title = "Akan Dihapus"),
            Task(id = 2, title = "Tetap Ada")
        ))
        advanceUntilIdle()
        viewModel.deleteTask(1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(1, state.tasks.size)
        assertTrue(state.tasks.none { it.id == 1L })
    }

    @Test
    fun `toggleCompletion updates task completion status`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(Task(id = 1, title = "Toggle Me", isCompleted = false)))
        advanceUntilIdle()
        viewModel.toggleCompletion(1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertTrue(state.tasks.first { it.id == 1L }.isCompleted)
    }

    @Test
    fun `quadrant filter and search combined work correctly`() = runTest {
        collectUiState()
        repo.emitTasks(listOf(
            Task(id = 1, title = "Belajar Compose", quadrant = EisenhowerQuadrant.DO_FIRST),
            Task(id = 2, title = "Belajar SQL", quadrant = EisenhowerQuadrant.SCHEDULE),
            Task(id = 3, title = "Olahraga", quadrant = EisenhowerQuadrant.DO_FIRST)
        ))
        advanceUntilIdle()
        viewModel.setQuadrantFilter(EisenhowerQuadrant.DO_FIRST)
        viewModel.setSearchQuery("Belajar")
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals(1, state.tasks.size)
        assertEquals("Belajar Compose", state.tasks.first().title)
    }
}

class FakeTaskListRepository : TaskRepository {
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    fun emitTasks(tasks: List<Task>) { tasksFlow.value = tasks }

    override fun getAllTasks(): Flow<List<Task>> = tasksFlow
    override fun getTasksByQuadrant(q: EisenhowerQuadrant): Flow<List<Task>> =
        flowOf(tasksFlow.value.filter { it.quadrant == q })
    override fun getActiveTasks(): Flow<List<Task>> =
        flowOf(tasksFlow.value.filter { !it.isCompleted })
    override fun getTaskById(id: Long): Flow<Task?> =
        flowOf(tasksFlow.value.find { it.id == id })
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
