package com.learncore.presentation

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.GetTaskByIdUseCase
import com.learncore.domain.usecase.SaveTaskUseCase
import com.learncore.presentation.screens.tasks.AddEditTaskViewModel
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTaskViewModelExtendedTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAddEditRepo
    private lateinit var viewModel: AddEditTaskViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAddEditRepo()
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
    fun `initial title is empty`() {
        assertEquals("", viewModel.uiState.value.title)
    }

    @Test
    fun `initial quadrant is DO_FIRST`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, viewModel.uiState.value.quadrant)
    }

    @Test
    fun `onTitleChange updates title`() {
        viewModel.onTitleChange("Tugas Baru")
        assertEquals("Tugas Baru", viewModel.uiState.value.title)
    }

    @Test
    fun `onTitleChange clears error`() {
        viewModel.saveTask() // trigger error
        viewModel.onTitleChange("Fix Title")
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onDescriptionChange updates description`() {
        viewModel.onDescriptionChange("Deskripsi lengkap")
        assertEquals("Deskripsi lengkap", viewModel.uiState.value.description)
    }

    @Test
    fun `onQuadrantChange updates quadrant`() {
        viewModel.onQuadrantChange(EisenhowerQuadrant.SCHEDULE)
        assertEquals(EisenhowerQuadrant.SCHEDULE, viewModel.uiState.value.quadrant)
    }

    @Test
    fun `onQuadrantChange to DELEGATE updates correctly`() {
        viewModel.onQuadrantChange(EisenhowerQuadrant.DELEGATE)
        assertEquals(EisenhowerQuadrant.DELEGATE, viewModel.uiState.value.quadrant)
    }

    @Test
    fun `onQuadrantChange to ELIMINATE updates correctly`() {
        viewModel.onQuadrantChange(EisenhowerQuadrant.ELIMINATE)
        assertEquals(EisenhowerQuadrant.ELIMINATE, viewModel.uiState.value.quadrant)
    }

    @Test
    fun `onCategorySelect updates category`() {
        viewModel.onCategorySelect("Kuliah")
        assertEquals("Kuliah", viewModel.uiState.value.category)
        assertFalse(viewModel.uiState.value.isCustomCategory)
    }

    @Test
    fun `onCategorySelect clears customCategory`() {
        viewModel.onCustomCategoryToggle()
        viewModel.onCustomCategoryChange("Custom Cat")
        viewModel.onCategorySelect("Pekerjaan")
        assertEquals("", viewModel.uiState.value.customCategory)
    }

    @Test
    fun `onCustomCategoryToggle sets isCustomCategory true`() {
        viewModel.onCustomCategoryToggle()
        assertTrue(viewModel.uiState.value.isCustomCategory)
    }

    @Test
    fun `onCustomCategoryToggle clears category`() {
        viewModel.onCategorySelect("Kuliah")
        viewModel.onCustomCategoryToggle()
        assertEquals("", viewModel.uiState.value.category)
    }

    @Test
    fun `onCustomCategoryChange updates customCategory`() {
        viewModel.onCustomCategoryToggle()
        viewModel.onCustomCategoryChange("Skripsi")
        assertEquals("Skripsi", viewModel.uiState.value.customCategory)
    }

    @Test
    fun `onDeadlineChange sets deadlineMillis`() {
        viewModel.onDeadlineChange(1_700_000_000_000L)
        assertEquals(1_700_000_000_000L, viewModel.uiState.value.deadlineMillis)
    }

    @Test
    fun `onDeadlineChange null clears deadline and reminder`() {
        viewModel.onDeadlineChange(1_700_000_000_000L)
        viewModel.onReminderChange(30)
        viewModel.onDeadlineChange(null)
        assertNull(viewModel.uiState.value.deadlineMillis)
        assertNull(viewModel.uiState.value.reminderMinutes)
    }

    @Test
    fun `onReminderChange updates reminderMinutes`() {
        viewModel.onReminderChange(60)
        assertEquals(60, viewModel.uiState.value.reminderMinutes)
    }

    @Test
    fun `onReminderChange null clears reminder`() {
        viewModel.onReminderChange(30)
        viewModel.onReminderChange(null)
        assertNull(viewModel.uiState.value.reminderMinutes)
    }

    @Test
    fun `saveTask with blank title sets error`() {
        viewModel.onTitleChange("")
        viewModel.saveTask()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveTask with blank title does not set isSaved`() {
        viewModel.onTitleChange("")
        viewModel.saveTask()
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saveTask with valid title sets isSaved true`() = runTest {
        viewModel.onTitleChange("Valid Task")
        viewModel.saveTask()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saveTask with valid title clears error`() = runTest {
        viewModel.onTitleChange("Valid Task")
        viewModel.saveTask()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `setDefaultQuadrant updates quadrant in non-edit mode`() {
        viewModel.setDefaultQuadrant(EisenhowerQuadrant.SCHEDULE)
        assertEquals(EisenhowerQuadrant.SCHEDULE, viewModel.uiState.value.quadrant)
    }

    @Test
    fun `setDefaultQuadrant ignored in edit mode`() = runTest {
        repo.addTask(Task(id = 1L, title = "Existing", quadrant = EisenhowerQuadrant.DO_FIRST))
        viewModel.loadTask(1L)
        advanceUntilIdle()
        viewModel.setDefaultQuadrant(EisenhowerQuadrant.ELIMINATE)
        // Should stay as the loaded task's quadrant
        assertEquals(EisenhowerQuadrant.DO_FIRST, viewModel.uiState.value.quadrant)
    }

    @Test
    fun `loadTask populates all fields`() = runTest {
        val task = Task(
            id = 1L,
            title = "Loaded Task",
            category = "Kuliah",
            description = "Deskripsi",
            quadrant = EisenhowerQuadrant.SCHEDULE,
            reminderMinutes = 15
        )
        repo.addTask(task)
        viewModel.loadTask(1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Loaded Task", state.title)
        assertEquals("Kuliah", state.category)
        assertEquals("Deskripsi", state.description)
        assertEquals(EisenhowerQuadrant.SCHEDULE, state.quadrant)
        assertEquals(15, state.reminderMinutes)
        assertTrue(state.isEditMode)
    }

    @Test
    fun `loadTask with custom category sets isCustomCategory`() = runTest {
        val task = Task(id = 1L, title = "T", category = "KategoriCustom")
        repo.addTask(task)
        viewModel.loadTask(1L)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCustomCategory)
        assertEquals("KategoriCustom", viewModel.uiState.value.customCategory)
    }
}

class FakeAddEditRepo : TaskRepository {
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    fun addTask(task: Task) { tasksFlow.value = tasksFlow.value + task }

    override fun getAllTasks(): Flow<List<Task>> = tasksFlow
    override fun getTasksByQuadrant(q: EisenhowerQuadrant): Flow<List<Task>> = flowOf(emptyList())
    override fun getActiveTasks(): Flow<List<Task>> = flowOf(emptyList())
    override fun getTaskById(id: Long): Flow<Task?> =
        tasksFlow.map { list -> list.find { it.id == id } }
    override suspend fun insertTask(task: Task): Long { return 1L }
    override suspend fun updateTask(task: Task) {}
    override suspend fun toggleTaskCompletion(id: Long) {}
    override suspend fun deleteTask(id: Long) {}
    override suspend fun getProductivityStats(): ProductivityStats = ProductivityStats()
    override suspend fun recordPomodoroSession(taskId: Long?, durationSeconds: Int) {}
}
