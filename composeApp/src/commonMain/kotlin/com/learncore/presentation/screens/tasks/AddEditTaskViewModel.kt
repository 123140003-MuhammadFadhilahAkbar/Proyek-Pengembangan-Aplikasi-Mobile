package com.learncore.presentation.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.core.notification.DeadlineScheduler
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import com.learncore.domain.usecase.GetTaskByIdUseCase
import com.learncore.domain.usecase.SaveTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val DEFAULT_CATEGORIES = listOf("Kuliah", "Pekerjaan", "Lifestyle")

// Pilihan reminder: null = tidak ada, nilai = menit sebelum deadline
val REMINDER_OPTIONS = listOf(
    null to "Tidak ada",
    15 to "15 menit sebelum",
    30 to "30 menit sebelum",
    60 to "1 jam sebelum",
    120 to "2 jam sebelum",
    360 to "6 jam sebelum",
    1440 to "1 hari sebelum"
)

data class AddEditTaskUiState(
    val title: String = "",
    val category: String = "",
    val customCategory: String = "",
    val isCustomCategory: Boolean = false,
    val description: String = "",
    val quadrant: EisenhowerQuadrant = EisenhowerQuadrant.DO_FIRST,
    val deadlineMillis: Long? = null,
    val reminderMinutes: Int? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
) {
    val resolvedCategory: String
        get() = if (isCustomCategory) customCategory.trim() else category
}

class AddEditTaskViewModel(
    private val saveTaskUseCase: SaveTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase
) : ViewModel(), KoinComponent {

    private val deadlineScheduler: DeadlineScheduler by inject()

    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    private var editingTaskId: Long? = null

    fun loadTask(taskId: Long) {
        editingTaskId = taskId
        viewModelScope.launch {
            val task = getTaskByIdUseCase(taskId).first()
            if (task != null) {
                val isCustom = task.category.isNotBlank() &&
                        !DEFAULT_CATEGORIES.contains(task.category)
                _uiState.value = _uiState.value.copy(
                    title = task.title,
                    category = if (isCustom) "" else task.category,
                    customCategory = if (isCustom) task.category else "",
                    isCustomCategory = isCustom,
                    description = task.description,
                    quadrant = task.quadrant,
                    deadlineMillis = task.deadline?.toEpochMilliseconds(),
                    reminderMinutes = task.reminderMinutes,
                    isEditMode = true
                )
            }
        }
    }

    fun setDefaultQuadrant(quadrant: EisenhowerQuadrant) {
        if (!_uiState.value.isEditMode) {
            _uiState.value = _uiState.value.copy(quadrant = quadrant)
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title, error = null)
    }

    fun onCategorySelect(category: String) {
        _uiState.value = _uiState.value.copy(
            category = category,
            isCustomCategory = false,
            customCategory = "",
            error = null
        )
    }

    fun onCustomCategoryToggle() {
        _uiState.value = _uiState.value.copy(
            isCustomCategory = true,
            category = "",
            error = null
        )
    }

    fun onCustomCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(customCategory = value)
    }

    fun onDescriptionChange(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun onQuadrantChange(quadrant: EisenhowerQuadrant) {
        _uiState.value = _uiState.value.copy(quadrant = quadrant)
    }

    fun onDeadlineChange(millis: Long?) {
        _uiState.value = _uiState.value.copy(
            deadlineMillis = millis,
            // reset reminder jika deadline dihapus
            reminderMinutes = if (millis == null) null else _uiState.value.reminderMinutes
        )
    }

    fun onReminderChange(minutes: Int?) {
        _uiState.value = _uiState.value.copy(reminderMinutes = minutes)
    }

    fun saveTask() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Judul tugas tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val task = Task(
                id = editingTaskId ?: 0L,
                title = state.title.trim(),
                category = state.resolvedCategory,
                description = state.description.trim(),
                quadrant = state.quadrant,
                deadline = state.deadlineMillis?.let { Instant.fromEpochMilliseconds(it) },
                reminderMinutes = if (state.deadlineMillis != null) state.reminderMinutes else null
            )
            saveTaskUseCase(task)
                .onSuccess { savedId ->
                    // jadwalkan reminder jika ada deadline + reminder
                    val deadlineMs = state.deadlineMillis
                    val reminderMin = state.reminderMinutes
                    if (deadlineMs != null && reminderMin != null) {
                        val id = if (task.id == 0L) savedId else task.id
                        deadlineScheduler.cancel(id)
                        deadlineScheduler.schedule(id, task.title, deadlineMs, reminderMin)
                    } else if (task.id != 0L) {
                        // edit task, hapus alarm lama jika deadline/reminder dihapus
                        deadlineScheduler.cancel(task.id)
                    }
                    _uiState.value = _uiState.value.copy(isSaved = true, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Gagal menyimpan tugas",
                        isLoading = false
                    )
                }
        }
    }
}
