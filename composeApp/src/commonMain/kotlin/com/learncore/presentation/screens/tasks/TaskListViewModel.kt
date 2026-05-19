package com.learncore.presentation.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import com.learncore.domain.usecase.DeleteTaskUseCase
import com.learncore.domain.usecase.GetAllTasksUseCase
import com.learncore.domain.usecase.GetTasksByQuadrantUseCase
import com.learncore.domain.usecase.ToggleTaskCompletionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TaskListUiState {
    data object Loading : TaskListUiState
    data class Success(
        val tasks: List<Task>,
        val selectedQuadrant: EisenhowerQuadrant?
    ) : TaskListUiState
    data class Error(val message: String) : TaskListUiState
}

class TaskListViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _selectedQuadrant = MutableStateFlow<EisenhowerQuadrant?>(null)
    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<TaskListUiState> = combine(
        _allTasks,
        _selectedQuadrant,
        _isLoading
    ) { tasks, quadrant, loading ->
        if (loading) return@combine TaskListUiState.Loading
        val filtered = if (quadrant != null) tasks.filter { it.quadrant == quadrant } else tasks
        TaskListUiState.Success(filtered, quadrant)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TaskListUiState.Loading
    )

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            getAllTasksUseCase()
                .collect { tasks ->
                    _allTasks.value = tasks
                    _isLoading.value = false
                }
        }
    }

    fun setQuadrantFilter(quadrant: EisenhowerQuadrant?) {
        _selectedQuadrant.value = quadrant
    }

    fun toggleCompletion(taskId: Long) {
        viewModelScope.launch {
            runCatching { toggleTaskCompletionUseCase(taskId) }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            runCatching { deleteTaskUseCase(taskId) }
        }
    }
}
