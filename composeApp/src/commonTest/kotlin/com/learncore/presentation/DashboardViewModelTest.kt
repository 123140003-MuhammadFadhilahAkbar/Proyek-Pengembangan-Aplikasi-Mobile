package com.learncore.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.GetAllTasksUseCase
import com.learncore.domain.usecase.GetProductivityStatsUseCase
import com.learncore.presentation.screens.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Fake DataStore untuk UserPreferences
class FakeDataStore : DataStore<Preferences> {
    private val flow = MutableStateFlow<Preferences>(emptyPreferences())
    override val data: Flow<Preferences> = flow
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(flow.value)
        flow.value = updated
        return updated
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeDashboardRepository
    private lateinit var viewModel: DashboardViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDashboardRepository()
        val fakePrefs = UserPreferences(FakeDataStore())
        viewModel = DashboardViewModel(
            getAllTasksUseCase = GetAllTasksUseCase(fakeRepository),
            getProductivityStatsUseCase = GetProductivityStatsUseCase(fakeRepository),
            userPreferences = fakePrefs
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading or empty`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(true, state.isLoading || state.tasks.isEmpty())
    }

    @Test
    fun `quadrant counts computed correctly from tasks`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "T1", quadrant = EisenhowerQuadrant.DO_FIRST),
            Task(id = 2, title = "T2", quadrant = EisenhowerQuadrant.DO_FIRST),
            Task(id = 3, title = "T3", quadrant = EisenhowerQuadrant.SCHEDULE),
            Task(id = 4, title = "T4", quadrant = EisenhowerQuadrant.DO_FIRST, isCompleted = true)
        )
        fakeRepository.emitTasks(tasks)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.quadrantCounts[EisenhowerQuadrant.DO_FIRST])
        assertEquals(1, state.quadrantCounts[EisenhowerQuadrant.SCHEDULE])
        assertEquals(0, state.quadrantCounts[EisenhowerQuadrant.DELEGATE])
    }

    @Test
    fun `completed tasks not counted in quadrant matrix`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Done", quadrant = EisenhowerQuadrant.DO_FIRST, isCompleted = true)
        )
        fakeRepository.emitTasks(tasks)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.quadrantCounts[EisenhowerQuadrant.DO_FIRST])
    }

    @Test
    fun `tasks update when repository emits new list`() = runTest {
        fakeRepository.emitTasks(listOf(Task(id = 1, title = "Task A")))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.tasks.size)

        fakeRepository.emitTasks(listOf(
            Task(id = 1, title = "Task A"),
            Task(id = 2, title = "Task B")
        ))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.tasks.size)
    }

    @Test
    fun `isLoading becomes false after tasks emitted`() = runTest {
        fakeRepository.emitTasks(emptyList())
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `all four quadrant counts initialized to zero when no tasks`() = runTest {
        fakeRepository.emitTasks(emptyList())
        testDispatcher.scheduler.advanceUntilIdle()

        val counts = viewModel.uiState.value.quadrantCounts
        assertEquals(4, counts.size)
        assertTrue(counts.values.all { it == 0 })
    }

    @Test
    fun `ELIMINATE quadrant tasks counted correctly`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Low prio 1", quadrant = EisenhowerQuadrant.ELIMINATE),
            Task(id = 2, title = "Low prio 2", quadrant = EisenhowerQuadrant.ELIMINATE)
        )
        fakeRepository.emitTasks(tasks)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.quadrantCounts[EisenhowerQuadrant.ELIMINATE])
    }

    @Test
    fun `mixed completed and active tasks - only active counted per quadrant`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Active DELEGATE", quadrant = EisenhowerQuadrant.DELEGATE),
            Task(id = 2, title = "Done DELEGATE", quadrant = EisenhowerQuadrant.DELEGATE, isCompleted = true),
            Task(id = 3, title = "Active SCHEDULE", quadrant = EisenhowerQuadrant.SCHEDULE),
        )
        fakeRepository.emitTasks(tasks)
        testDispatcher.scheduler.advanceUntilIdle()

        val counts = viewModel.uiState.value.quadrantCounts
        assertEquals(1, counts[EisenhowerQuadrant.DELEGATE])
        assertEquals(1, counts[EisenhowerQuadrant.SCHEDULE])
    }
}

class FakeDashboardRepository : TaskRepository {
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    fun emitTasks(tasks: List<Task>) { tasksFlow.value = tasks }

    override fun getAllTasks(): Flow<List<Task>> = tasksFlow
    override fun getTasksByQuadrant(quadrant: EisenhowerQuadrant): Flow<List<Task>> = flowOf(emptyList())
    override fun getActiveTasks(): Flow<List<Task>> = flowOf(emptyList())
    override fun getTaskById(id: Long): Flow<Task?> = flowOf(null)
    override suspend fun insertTask(task: Task): Long = 0L
    override suspend fun updateTask(task: Task) {}
    override suspend fun toggleTaskCompletion(id: Long) {}
    override suspend fun deleteTask(id: Long) {}
    override suspend fun getProductivityStats(): ProductivityStats = ProductivityStats()
    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) {}
}
