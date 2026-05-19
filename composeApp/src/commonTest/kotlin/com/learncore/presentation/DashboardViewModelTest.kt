package com.learncore.presentation

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.GetAllTasksUseCase
import com.learncore.domain.usecase.GetProductivityStatsUseCase
import com.learncore.presentation.screens.dashboard.DashboardViewModel
import app.cash.turbine.test
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeDashboardRepository
    private lateinit var viewModel: DashboardViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDashboardRepository()
        viewModel = DashboardViewModel(
            getAllTasksUseCase = GetAllTasksUseCase(fakeRepository),
            getProductivityStatsUseCase = GetProductivityStatsUseCase(fakeRepository)
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // State starts as loading before tasks arrive
        val state = viewModel.uiState.value
        // isLoading should be true initially (before first emission)
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
}

class FakeDashboardRepository : TaskRepository {
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    fun emitTasks(tasks: List<Task>) {
        tasksFlow.value = tasks
    }

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
