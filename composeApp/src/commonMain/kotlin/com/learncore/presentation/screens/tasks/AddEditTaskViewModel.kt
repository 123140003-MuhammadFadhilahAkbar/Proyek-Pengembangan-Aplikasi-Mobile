package com.learncore.presentation.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import com.learncore.domain.usecase.GetTaskByIdUseCase
import com.learncore.domain.usecase.SaveTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val DEFAULT_CATEGORIES = listOf("Kuliah", "Pekerjaan", "Lifestyle")

data class AddEditTaskUiState(
    val title: String = "",
    val category: String = "",
    val customCategory: String = "",
    val isCustomCategory: Boolean = false,
    val description: String = "",
    val quadrant: EisenhowerQuadrant = EisenhowerQuadrant.DO_FIRST,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
) {
    /** Nilai kategori final yang akan disimpan */
    val resolvedCategory: String
        get() = if (isCustomCategory) customCategory.trim() else category
}

class AddEditTaskViewModel(
    private val saveTaskUseCase: SaveTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase
) : ViewModel() {

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
                quadrant = state.quadrant
            )
            saveTaskUseCase(task)
                .onSuccess { _uiState.value = _uiState.value.copy(isSaved = true, isLoading = false) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Gagal menyimpan tugas",
                        isLoading = false
                    )
                }
        }
    }
}
