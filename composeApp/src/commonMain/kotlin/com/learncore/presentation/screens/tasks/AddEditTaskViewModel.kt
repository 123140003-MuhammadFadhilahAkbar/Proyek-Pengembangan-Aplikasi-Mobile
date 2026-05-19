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

data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    val quadrant: EisenhowerQuadrant = EisenhowerQuadrant.DO_FIRST,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
)

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
                _uiState.value = _uiState.value.copy(
                    title = task.title,
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

    fun onDescriptionChange(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun onQuadrantChange(quadrant: EisenhowerQuadrant) {
        _uiState.value = _uiState.value.copy(quadrant = quadrant)
    }

    fun saveTask() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val task = Task(
                id = editingTaskId ?: 0L,
                title = state.title.trim(),
                description = state.description.trim(),
                quadrant = state.quadrant
            )
            saveTaskUseCase(task)
                .onSuccess { _uiState.value = _uiState.value.copy(isSaved = true, isLoading = false) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to save task",
                        isLoading = false
                    )
                }
        }
    }
}
