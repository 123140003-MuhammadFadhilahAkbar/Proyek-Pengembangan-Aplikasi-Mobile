package com.learncore.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.usecase.GetAllTasksUseCase
import com.learncore.domain.usecase.GetProductivityStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val tasks: List<Task> = emptyList(),
    val stats: ProductivityStats = ProductivityStats(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val userName: String = "",
    val userPhotoUri: String = ""
) {
    val quadrantCounts: Map<EisenhowerQuadrant, Int>
        get() = EisenhowerQuadrant.entries.associateWith { q ->
            tasks.count { it.quadrant == q && !it.isCompleted }
        }
}

class DashboardViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getProductivityStatsUseCase: GetProductivityStatsUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
        loadStats()
        observeUserName()
        observeUserPhotoUri()
    }

    private fun observeUserName() {
        viewModelScope.launch {
            userPreferences.userName.collect { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
        }
    }

    private fun observeUserPhotoUri() {
        viewModelScope.launch {
            userPreferences.userPhotoUri.collect { uri ->
                _uiState.value = _uiState.value.copy(userPhotoUri = uri)
            }
        }
    }

    private fun observeTasks() {
        viewModelScope.launch {
            getAllTasksUseCase()
                .collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        tasks = tasks,
                        isLoading = false
                    )
                    loadStats()
                }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            runCatching { getProductivityStatsUseCase() }
                .onSuccess { stats ->
                    _uiState.value = _uiState.value.copy(stats = stats)
                }
        }
    }
}
