package com.learncore.presentation

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.GetTaskByIdUseCase
import com.learncore.domain.usecase.SaveTaskUseCase
import com.learncore.presentation.screens.tasks.AddEditTaskViewModel
import com.learncore.presentation.screens.tasks.REMINDER_OPTIONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAddEditRepository
    private lateinit var viewModel: AddEditTaskViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAddEditRepository()
        viewModel = AddEditTaskViewModel(
            saveTaskUseCase = SaveTaskUseCase(repo),
            getTaskByIdUseCase = GetTaskByIdUseCase(repo)
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty title`() {
        assertEquals("", viewModel.uiState.value.title)
    }

    @Test
    fun `onTitleChange updates title in state`() {
        viewModel.onTitleChange("Belajar Kotlin")
        assertEquals("Belajar Kotlin", viewModel.uiState.value.title)
    }

    @Test
    fun `onTitleChange clears error`() {
        viewModel.saveTask() // trigger error
        viewModel.onTitleChange("Fixed title")
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveTask with blank title sets error`() = runTest {
        viewModel.onTitleChange("   ")
        viewModel.saveTask()
        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saveTask with valid title sets isSaved true`() = runTest {
        viewModel.onTitleChange("Valid Task")
        viewModel.saveTask()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `onQuadrantChange updates quadrant`() {
        viewModel.onQuadrantChange(EisenhowerQuadrant.SCHEDULE)
        assertEquals(EisenhowerQuadrant.SCHEDULE, viewModel.uiState.value.quadrant)
    }

    @Test
    fun `onDeadlineChange updates deadlineMillis`() {
        val millis = 1700000000000L
        viewModel.onDeadlineChange(millis)
        assertEquals(millis, viewModel.uiState.value.deadlineMillis)
    }

    @Test
    fun `onDeadlineChange with null resets reminderMinutes`() {
        viewModel.onDeadlineChange(1700000000000L)
        viewModel.onReminderChange(60)
        viewModel.onDeadlineChange(null)
        assertNull(viewModel.uiState.value.deadlineMillis)
        assertNull(viewModel.uiState.value.reminderMinutes)
    }

    @Test
    fun `onReminderChange updates reminderMinutes`() {
        viewModel.onReminderChange(120)
        assertEquals(120, viewModel.uiState.value.reminderMinutes)
    }

    @Test
    fun `onCategorySelect sets category and clears custom`() {
        viewModel.onCustomCategoryToggle()
        viewModel.onCategorySelect("Kuliah")
        assertEquals("Kuliah", viewModel.uiState.value.category)
        assertFalse(viewModel.uiState.value.isCustomCategory)
    }

    @Test
    fun `onCustomCategoryToggle sets isCustomCategory true`() {
        viewModel.onCustomCategoryToggle()
        assertTrue(viewModel.uiState.value.isCustomCategory)
        assertEquals("", viewModel.uiState.value.category)
    }

    @Test
    fun `resolvedCategory uses customCategory when isCustomCategory`() {
        viewModel.onCustomCategoryToggle()
        viewModel.onCustomCategoryChange("Olahraga")
        assertEquals("Olahraga", viewModel.uiState.value.resolvedCategory)
    }

    @Test
    fun `REMINDER_OPTIONS contains null option as first`() {
        assertNull(REMINDER_OPTIONS.first().first)
    }

    @Test
    fun `REMINDER_OPTIONS contains 60 minutes option`() {
        assertTrue(REMINDER_OPTIONS.any { it.first == 60 })
    }
}

class FakeAddEditRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1L

    override fun getAllTasks(): Flow<List<Task>> = flowOf(tasks.toList())
    override fun getTasksByQuadrant(q: EisenhowerQuadrant): Flow<List<Task>> = flowOf(emptyList())
    override fun getActiveTasks(): Flow<List<Task>> = flowOf(emptyList())
    override fun getTaskById(id: Long): Flow<Task?> = flowOf(tasks.find { it.id == id })
    override suspend fun insertTask(task: Task): Long {
        val id = nextId++
        tasks.add(task.copy(id = id))
        return id
    }
    override suspend fun updateTask(task: Task) {
        val i = tasks.indexOfFirst { it.id == task.id }
        if (i != -1) tasks[i] = task
    }
    override suspend fun toggleTaskCompletion(id: Long) {}
    override suspend fun deleteTask(id: Long) { tasks.removeAll { it.id == id } }
    override suspend fun getProductivityStats(): ProductivityStats = ProductivityStats()
    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) {}
}
