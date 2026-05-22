package com.learncore.presentation.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.domain.model.Task
import com.learncore.domain.usecase.DeleteTaskUseCase
import com.learncore.domain.usecase.GetTaskByIdUseCase
import com.learncore.domain.usecase.ToggleTaskCompletionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TaskDetailUiState {
    data object Loading : TaskDetailUiState
    data class Success(val task: Task) : TaskDetailUiState
    data class Error(val message: String) : TaskDetailUiState
}

class TaskDetailViewModel(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            getTaskByIdUseCase(taskId).collect { task ->
                _uiState.value = if (task != null) {
                    TaskDetailUiState.Success(task)
                } else {
                    TaskDetailUiState.Error("Task not found")
                }
            }
        }
    }

    fun toggleCompletion(taskId: Long) {
        viewModelScope.launch {
            runCatching { toggleTaskCompletionUseCase(taskId) }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            runCatching { deleteTaskUseCase(taskId) }
                .onSuccess { _deleted.value = true }
        }
    }
}
